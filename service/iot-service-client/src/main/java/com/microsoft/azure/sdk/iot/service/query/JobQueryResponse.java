// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.query;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.Job;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.JobType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class JobQueryResponse //TODO probably need to split this in two since one job query returns jobs and the other returns results I think?
{
    private final transient Gson gson;

    Iterator<Job> jobs;

    @Setter(AccessLevel.PACKAGE) // value is retrieved from header, not json payload
    @Getter
    String continuationToken = "";

    final QueryClient queryClient;
    final String originalQuery;
    final JobType jobType;
    final JobStatus jobStatus;

    JobQueryResponse(String json, QueryClient queryClient, String originalQuery)
    {
        gson = new GsonBuilder().disableHtmlEscaping().create();

        JsonObject[] twinJsonArray = gson.fromJson(json, JsonObject[].class);
        List<Job> jobsArray = new ArrayList<>();
        for (JsonObject twinJson : twinJsonArray)
        {
            jobsArray.add(new Job(twinJson.toString()));
        }

        this.jobs = jobsArray.iterator();

        this.queryClient = queryClient;
        this.originalQuery = originalQuery;
        this.jobType = null;
        this.jobStatus = null;
    }

    JobQueryResponse(String json, QueryClient queryClient, JobType jobType, JobStatus jobStatus)
    {
        gson = new GsonBuilder().disableHtmlEscaping().create();

        JsonObject[] twinJsonArray = gson.fromJson(json, JsonObject[].class);
        List<Job> jobsArray = new ArrayList<>();
        for (JsonObject twinJson : twinJsonArray)
        {
            jobsArray.add(new Job(twinJson.toString()));
        }

        this.jobs = jobsArray.iterator();

        this.queryClient = queryClient;
        this.originalQuery = null;
        this.jobType = jobType;
        this.jobStatus = jobStatus;
    }

    public boolean hasNext()
    {
        return this.jobs.hasNext() || this.continuationToken != null;
    }

    public Job next() throws IotHubException, IOException
    {
        return next(QueryPageOptions.builder().build());
    }

    public Job next(QueryPageOptions pageOptions) throws IotHubException, IOException
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
