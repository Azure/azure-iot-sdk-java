// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnauthorizedException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A pageable set of undefined json objects returned from a query.
 */
public class RawQueryResponse
{
    private final transient Gson gson;

    Iterator<JsonObject> jsonObjects;

    @Setter(AccessLevel.PACKAGE) // value is retrieved from header, not json payload
    @Getter
    String continuationToken = "";

    final QueryClient queryClient;
    final String originalQuery;

    public RawQueryResponse(String json, QueryClient queryClient, String originalQuery)
    {
        gson = new GsonBuilder().disableHtmlEscaping().create();

        try
        {
            this.jsonObjects = Arrays.asList(gson.fromJson(json, JsonObject[].class)).iterator();
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json:" + malformed);
        }

        this.queryClient = queryClient;
        this.originalQuery = originalQuery;
    }

    /**
     * @return True if the query has at least one more json object to return. False otherwise.
     */
    public boolean hasNext()
    {
        return this.jsonObjects.hasNext() || this.continuationToken != null;
    }

    /**
     * Return the next json object from the query. If the previous page of query results has been exhausted, then this method
     * will make a request to the service to get the next page of results using the default paging options.
     * @return the next json object from the query.
     * @throws IotHubException If any IoT Hub level errors occur such as an {@link IotHubUnauthorizedException}.
     * @throws IOException If any network level errors occur.
     * @throws NoSuchElementException If there is no next object to return anymore.
     */
    public String next() throws IotHubException, IOException, NoSuchElementException
    {
        return next(QueryPageOptions.builder().build());
    }

    /**
     * Return the next json object from the query. If the previous page of query results has been exhausted, then this method
     * will make a request to the service to get the next page of results using the provided paging options.
     * @return the next json object from the query.
     * @param pageOptions the options for the next page of results if the next page is retrieved to fulfil this request
     * for the next json object. May not be null.
     * @throws IotHubException If any IoT Hub level errors occur such as an {@link IotHubUnauthorizedException}.
     * @throws IOException If any network level errors occur.
     * @throws NoSuchElementException If there is no next object to return anymore.
     */
    public String next(QueryPageOptions pageOptions) throws IotHubException, IOException
    {
        Objects.requireNonNull(pageOptions);

        try
        {
            return this.jsonObjects.next().toString();
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

            RawQueryResponse nextPage = this.queryClient.queryRaw(this.originalQuery, nextPageOptions);
            this.jsonObjects = nextPage.jsonObjects;
            this.continuationToken = nextPage.continuationToken;

            return this.jsonObjects.next().toString();
        }
    }
}
