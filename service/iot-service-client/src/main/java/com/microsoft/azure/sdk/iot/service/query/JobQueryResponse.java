// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.ScheduledJob;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.ScheduledJobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.ScheduledJobType;
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
 * A pageable set of {@link ScheduledJob} objects returned from a query.
 */
public class JobQueryResponse
{
    private final transient Gson gson;

    Iterator<ScheduledJob> jobs;

    @Setter(AccessLevel.PACKAGE) // value is retrieved from header, not json payload
    @Getter
    String continuationToken = "";

    final QueryClient queryClient;
    final String originalQuery;
    final ScheduledJobType jobType;
    final ScheduledJobStatus jobStatus;

    JobQueryResponse(String json, QueryClient queryClient, String originalQuery)
    {
        gson = new GsonBuilder().disableHtmlEscaping().create();

        JsonObject[] twinJsonArray = gson.fromJson(json, JsonObject[].class);
        List<ScheduledJob> jobsArray = new ArrayList<>();
        for (JsonObject twinJson : twinJsonArray)
        {
            jobsArray.add(new ScheduledJob(twinJson.toString()));
        }

        this.jobs = jobsArray.iterator();

        this.queryClient = queryClient;
        this.originalQuery = originalQuery;
        this.jobType = null;
        this.jobStatus = null;
    }

    JobQueryResponse(String json, QueryClient queryClient, ScheduledJobType jobType, ScheduledJobStatus jobStatus)
    {
        gson = new GsonBuilder().disableHtmlEscaping().create();

        JsonObject[] twinJsonArray = gson.fromJson(json, JsonObject[].class);
        List<ScheduledJob> jobsArray = new ArrayList<>();
        for (JsonObject twinJson : twinJsonArray)
        {
            jobsArray.add(new ScheduledJob(twinJson.toString()));
        }

        this.jobs = jobsArray.iterator();

        this.queryClient = queryClient;
        this.originalQuery = null;
        this.jobType = jobType;
        this.jobStatus = jobStatus;
    }

    /**
     * @return True if the query has at least one more job to return. False otherwise.
     */
    public boolean hasNext()
    {
        return this.jobs.hasNext() || this.continuationToken != null;
    }

    /**
     * Return the next job from the query. If the previous page of query results has been exhausted, then this method
     * will make a request to the service to get the next page of results using the default paging options.
     * @return the next job from the query.
     * @throws IotHubException If any IoT Hub level errors occur such as an {@link com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException}.
     * @throws IOException If any network level errors occur.
     */
    public ScheduledJob next() throws IotHubException, IOException
    {
        return next(QueryPageOptions.builder().build());
    }

    /**
     * Return the next job from the query. If the previous page of query results has been exhausted, then this method
     * will make a request to the service to get the next page of results using the provided paging options.
     * @return the next job from the query.
     * @param pageOptions the options for the next page of results if the next page is retrieved to fulfil this request
     * for the next job. May not be null.
     * @throws IotHubException If any IoT Hub level errors occur such as an {@link com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException}.
     * @throws IOException If any network level errors occur.
     */
    public ScheduledJob next(QueryPageOptions pageOptions) throws IotHubException, IOException
    {
        Objects.requireNonNull(pageOptions);

        try
        {
            return this.jobs.next();
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

            JobQueryResponse nextPage;
            if (this.originalQuery != null)
            {
                nextPage = this.queryClient.queryJobs(this.originalQuery, nextPageOptions);
            }
            else
            {
                nextPage = this.queryClient.queryJobs(this.jobType, this.jobStatus, nextPageOptions);
            }

            this.jobs = nextPage.jobs;
            this.continuationToken = nextPage.continuationToken;

            return this.jobs.next();
        }
    }
}
