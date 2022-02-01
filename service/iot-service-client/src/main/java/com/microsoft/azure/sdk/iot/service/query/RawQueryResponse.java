// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

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

    public boolean hasNext()
    {
        return this.jsonObjects.hasNext() || this.continuationToken != null;
    }

    public String next() throws IotHubException, IOException
    {
        return next(QueryPageOptions.builder().build());
    }

    public String next(QueryPageOptions pageOptions) throws IotHubException, IOException
    {
        try
        {
            return this.jsonObjects.next().toString();
        }
        catch (NoSuchElementException ex)
        {
            // previous list of jobs has been exhausted. Get next page of results if there is a next page

            if (this.continuationToken == null)
            {
                return null;
            }

            QueryPageOptions nextPageOptions =
                QueryPageOptions.builder()
                    .continuationToken(this.continuationToken)
                    .pageSize(pageOptions.pageSize)
                    .build();

            RawQueryResponse nextPage = this.queryClient.queryRaw(this.originalQuery, nextPageOptions);
            this.jsonObjects = nextPage.jsonObjects;
            this.continuationToken = nextPage.continuationToken;

            return this.jsonObjects.next().toString();
        }
    }
}
