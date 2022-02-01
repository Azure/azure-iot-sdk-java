// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class JobsQueryResponse //TODO probably need to split this in two since one job query returns jobs and the other returns results I think?
{
    private final transient Gson gson;

    List<JobResult> jobs;

    @Setter(AccessLevel.PACKAGE) // value is retrieved from header, not json payload
    @Getter
    String continuationToken = "";

    final QueryClient queryClient;
    final String originalQuery;

    JobsQueryResponse(String json, QueryClient queryClient, String originalQuery)
    {
        gson = new GsonBuilder().disableHtmlEscaping().create();

        try
        {
            this.jobs = Arrays.asList(gson.fromJson(json, JobResult[].class));
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
        return this.jobs.iterator().hasNext() || this.continuationToken != null;
    }

    public JobResult next() throws IotHubException, IOException
    {
        return next(QueryPageOptions.builder().build());
    }

    public JobResult next(QueryPageOptions pageOptions) throws IotHubException, IOException
    {
        try
        {
            return this.jobs.iterator().next();
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

            JobsQueryResponse nextPage = this.queryClient.queryJobs(this.originalQuery, nextPageOptions);
            this.jobs = nextPage.jobs;
            this.continuationToken = nextPage.continuationToken;

            return this.jobs.iterator().next();
        }
    }
}
