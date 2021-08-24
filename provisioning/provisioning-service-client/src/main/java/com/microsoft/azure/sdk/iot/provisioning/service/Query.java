// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.QueryResult;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.QuerySpecification;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

import java.util.*;

/**
 * The query iterator.
 *
 * <p> The {@code Query} iterator is the result of the query factory for
 * <table>
 *     <caption>Query factories</caption>
 *     <tr>
 *         <td><b>IndividualEnrollment:</b></td>
 *         <td>{@link ProvisioningServiceClient#createIndividualEnrollmentQuery(QuerySpecification, int)}</td>
 *     </tr>
 *     <tr>
 *         <td><b>EnrollmentGroup:</b></td>
 *         <td>{@link ProvisioningServiceClient#createEnrollmentGroupQuery(QuerySpecification, int)}</td>
 *     </tr>
 *     <tr>
 *         <td><b>RegistrationStatus:</b></td>
 *         <td>{@link ProvisioningServiceClient#createEnrollmentGroupRegistrationStateQuery(QuerySpecification, String, int)}</td>
 *     </tr>
 * </table>
 * <p> On all cases, the <b>QuerySpecification</b> contains a SQL query that must follow the
 *     <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language">Query Language</a>
 *     for the Device Provisioning Service.
 *
 * <p> Optionally, an {@code Integer} with the <b>pageSize</b>, can determine the maximum number of the items in the
 *     {@link QueryResult} returned by the {@link #next()}. It must be any positive integer, and if it contains 0,
 *     the Device Provisioning Service will ignore it and use a standard page size.
 *
 * <p> You can use this Object as a standard Iterator, just using the {@link #hasNext()} and {@link #next()} in a
 *     {@code while} loop, up to the point where the {@link #hasNext()} return {@code false}. But, keep in mind
 *     that the {@link QueryResult} can contain a empty list, even if the {@link #hasNext()} returned {@code true}.
 *     For example, image that you have 10 IndividualEnrollment in the Device Provisioning Service and you created
 *     new query with the {@code pageSize} equals 5. The first {@code hasNext()} will return {@code true}, and the
 *     first {@code next()} will return a {@code QueryResult} with 5 items. After that you call the {@code hasNext},
 *     which will returns {@code true}. Now, before you get the next page, somebody delete all the IndividualEnrollment,
 *     What happened, when you call the {@code next()}, it will return a valid {@code QueryResult}, but the
 *     QueryResult.getItems() will return a empty list.
 *
 * <p> You can also store a query context (QuerySpecification + ContinuationToken) and restart it in the future, from
 *     the point where you stopped.
 *
 * <p> Besides the Items, the queryResult contains the continuationToken, the {@link QueryResult#getContinuationToken()}
 *     shall return it. In any point in the future, you may recreate the query using the same query factories that you
 *     used for the first time, and call {@link #next(String)} providing the stored continuationToken to get the next page.
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps/">Azure IoT Hub Device Provisioning Service</a>
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language">Query Language</a>
 */
public class Query implements Iterator<QueryResult>
{
    private static final String CONTINUATION_TOKEN_KEY = "x-ms-continuation";
    private static final String ITEM_TYPE_KEY = "x-ms-item-type";
    private static final String PAGE_SIZE_KEY = "x-ms-max-item-count";
    private static final String PATH_SEPARATOR = "/";
    private static final String PATH_QUERY = "query";

    private final String querySpecificationJson;
    private int pageSize;
    private String continuationToken;
    private final ContractApiHttp contractApiHttp;
    private boolean hasNext;
    private final String queryPath;

    /**
     * INTERNAL CONSTRUCTOR
     *
     * <p> Use one of the factories to create a new query.
     * <table>
     *     <caption>Query factories</caption>
     *     <tr>
     *         <td><b>IndividualEnrollment:</b></td>
     *         <td>{@link ProvisioningServiceClient#createIndividualEnrollmentQuery(QuerySpecification, int)}</td>
     *     </tr>
     *     <tr>
     *         <td><b>EnrollmentGroup:</b></td>
     *         <td>{@link ProvisioningServiceClient#createEnrollmentGroupQuery(QuerySpecification, int)}</td>
     *     </tr>
     *     <tr>
     *         <td><b>RegistrationStatus:</b></td>
     *         <td>{@link ProvisioningServiceClient#createEnrollmentGroupRegistrationStateQuery(QuerySpecification, String, int)}</td>
     *     </tr>
     * </table>
     *
     * @param contractApiHttp the {@link ContractApiHttp} that send request messages to the Device Provisioning Service. It cannot be {@code null}.
     * @param targetPath the {@code String} with the path that will be part of the URL in the rest API. It cannot be {@code null}.
     * @param querySpecification the {@link QuerySpecification} with the SQL query. It cannot be {@code null}.
     * @param pageSize the {@code int} with the maximum number of items per iteration. It cannot be negative.
     * @throws IllegalArgumentException if one of the parameters is invalid.
     */
    Query(ContractApiHttp contractApiHttp, String targetPath, QuerySpecification querySpecification, int pageSize)
    {
        if (contractApiHttp == null)
        {
            throw new IllegalArgumentException("contractApiHttp cannot be null.");
        }

        if (Tools.isNullOrEmpty(targetPath))
        {
            throw new IllegalArgumentException("targetPath cannot be null.");
        }

        if (querySpecification == null)
        {
            throw new IllegalArgumentException("querySpecification cannot be null.");
        }

        if (pageSize < 0)
        {
            throw new IllegalArgumentException("pageSize cannot be negative.");
        }

        this.contractApiHttp = contractApiHttp;
        this.pageSize = pageSize;
        this.querySpecificationJson = querySpecification.toJson();
        this.queryPath = targetPath + PATH_SEPARATOR + PATH_QUERY;
        this.continuationToken = null;
        this.hasNext = true;
    }

    /**
     * Getter for hasNext.
     *
     * <p> It will return {@code true} if the query is not finished in the Device Provisioning Service, and another
     *     iteration with {@link #next()} may return more items. Call {@link #next()} after receive a {@code hasNext}
     *     {@code true} will result in a {@link QueryResult} that can or cannot contains elements. And call
     *     {@link #next()} after receive a {@code hasNext} {@code false} will result in a exception.
     *
     * @return The{@code boolean} {@code true} if query is not finalize in the Service.
     */
    @Override
    public boolean hasNext()
    {
        return hasNext;
    }

    /**
     * Return the next page of result for the query.
     *
     * @return A {@link QueryResult} with the next page of items for the query.
     * @throws NoSuchElementException if the query does no have more pages to return.
     */
    @Override
    public QueryResult next()
    {
        if (!hasNext)
        {
            throw new NoSuchElementException("There are no more pending elements");
        }

        Map<String, String> headerParameters = new HashMap<>();
        if (pageSize != 0)
        {
            headerParameters.put(PAGE_SIZE_KEY, Integer.toString(pageSize));
        }
        if (!Tools.isNullOrEmpty(this.continuationToken))
        {
            headerParameters.put(CONTINUATION_TOKEN_KEY, this.continuationToken);
        }

        HttpResponse httpResponse;
        try
        {
            httpResponse =
                    contractApiHttp.request(
                            HttpMethod.POST,
                            queryPath,
                            headerParameters,
                            querySpecificationJson);
        }
        catch (ProvisioningServiceClientException e)
        {
            // Because Query implements the iterator interface, the next cannot throws ProvisioningServiceClientException.
            throw new IllegalArgumentException(e);
        }

        byte[] body = httpResponse.getBody();
        if (body == null)
        {
            throw new IllegalArgumentException("Http response for next cannot contains a null body");
        }

        String bodyStr = new String(body);
        Map<String, String> headers = httpResponse.getHeaderFields();
        String type = headers.get(ITEM_TYPE_KEY);
        this.continuationToken = headers.get(CONTINUATION_TOKEN_KEY);

        hasNext = (this.continuationToken != null);

        return new QueryResult(type, bodyStr, this.continuationToken);
    }

    /**
     * Return the next page of result for the query using a new continuationToken.
     *
     * @param continuationToken the {@code String} with the previous continuationToken. It cannot be {@code null} or empty.
     * @return A {@link QueryResult} with the next page of items for the query.
     * @throws NoSuchElementException if the query does no have more pages to return.
     */
    public QueryResult next(String continuationToken)
    {
        if (Tools.isNullOrEmpty(continuationToken))
        {
            throw new NoSuchElementException("There is no Continuation Token to get pending elements,");
        }

        this.continuationToken = continuationToken;
        return next();
    }

    /**
     * Getter for the pageSize.
     *
     * <p> PageSize is the maximum number of items in the {@link QueryResult} per iteration.
     *
     * @return An {@code int} with the current pageSize.
     */
    public int getPageSize()
    {
        return pageSize;
    }

    /**
     * Setter for the pageSize.
     *
     * <p> PageSize is the maximum number of items in the {@link QueryResult} per iteration.
     *
     * @param pageSize an {@code int} with the new pageSize. It cannot be negative. The Device Service Client
     *                 will use its own default pageSize if it is <b>0</b>.
     * @throws IllegalArgumentException if the provided pageSize is negative.
     */
    public void setPageSize(int pageSize)
    {
        if (pageSize < 0)
        {
            throw new IllegalArgumentException("pageSize cannot be null");
        }

        this.pageSize = pageSize;
    }
}
