// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A pageable set of {@link Twin} objects returned from a query.
 */
public class TwinQueryResponse
{
    private final transient Gson gson;

    Iterator<Twin> twins;

    @Setter(AccessLevel.PACKAGE) // value is retrieved from header, not json payload
    @Getter
    String continuationToken = "";

    final QueryClient queryClient;
    final String originalQuery;

    public TwinQueryResponse(String json, QueryClient queryClient, String originalQuery)
    {
        gson = new GsonBuilder().disableHtmlEscaping().create();

        try
        {
            JsonObject[] twinJsonArray = gson.fromJson(json, JsonObject[].class);
            List<Twin> twinArray = new ArrayList<>();
            for (JsonObject twinJson : twinJsonArray)
            {
                twinArray.add(Twin.fromJson(twinJson.toString()));
            }

            this.twins = twinArray.iterator();
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json:" + malformed);
        }

        this.queryClient = queryClient;
        this.originalQuery = originalQuery;
    }

    /**
     * @return True if the query has at least one more twin to return. False otherwise.
     */
    public boolean hasNext()
    {
        return this.twins.hasNext() || this.continuationToken != null;
    }

    /**
     * Return the next Twin from the query. If the previous page of query results has been exhausted, then this method
     * will make a request to the service to get the next page of results using the default paging options.
     * @return the next Twin from the query.
     * @throws IotHubException If any IoT Hub level errors occur such as an {@link com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException}.
     * @throws IOException If any network level errors occur.
     */
    public Twin next() throws IotHubException, IOException
    {
        return next(QueryPageOptions.builder().build());
    }

    /**
     * Return the next Twin from the query. If the previous page of query results has been exhausted, then this method
     * will make a request to the service to get the next page of results using the provided paging options.
     * @param pageOptions the options for the next page of results if the next page is retrieved to fulfil this request
     * for the next Twin. May not be null.
     * @return the next Twin from the query.
     * @throws IotHubException If any IoT Hub level errors occur such as an {@link com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException}.
     * @throws IOException If any network level errors occur.
     */
    public Twin next(QueryPageOptions pageOptions) throws IotHubException, IOException
    {
        Objects.requireNonNull(pageOptions);

        try
        {
            return this.twins.next();
        }
        catch (NoSuchElementException ex)
        {
            // previous list of jobs has been exhausted. Get next page of results if there is a next page

            if (this.continuationToken == null)
            {
                throw ex;
            }

            QueryPageOptions nextPageOptions =
                QueryPageOptions.builder()
                    .continuationToken(this.continuationToken)
                    .pageSize(pageOptions.getPageSize())
                    .build();

            TwinQueryResponse nextPage = this.queryClient.queryTwins(this.originalQuery, nextPageOptions);
            this.twins = nextPage.twins;
            this.continuationToken = nextPage.continuationToken;

            return this.twins.next();
        }
    }
}
