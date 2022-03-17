// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.microsoft.azure.sdk.iot.service.jobs.serializers.JobsResponseParser;
import com.microsoft.azure.sdk.iot.service.jobs.serializers.JobsStatisticsParser;
import com.microsoft.azure.sdk.iot.service.twin.TwinState;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodResponse;
import com.microsoft.azure.sdk.iot.service.twin.Pair;
import lombok.Getter;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Metadata for a particular job created with the {@link ScheduledJobsClient}.
 */
public class ScheduledJob
{
    /**
     * The unique identifier for this job.
     */
    @Getter
    private final String jobId;

    /**
     * Condition for device query to get devices to execute the job on.
     */
    @Getter
    private final String queryCondition;

    /**
     * Scheduled job start time in UTC.
     */
    @Getter
    private final Date createdTime;

    /**
     * System generated start time in UTC.
     */
    @Getter
    private final Date startTime;

    /**
     * System generated last Updated Time in UTC.
     */
    @Getter
    private final Date lastUpdatedDateTime;

    /**
     * System generated end time in UTC. Represents the time the job stopped processing.
     */
    @Getter
    private final Date endTime;

    /**
     * Max execution time in seconds.
     */
    @Getter
    private final Long maxExecutionTimeInSeconds;

    /**
     * The type of job to execute.
     */
    @Getter
    private final ScheduledJobType jobType;

    /**
     * The status of the job.
     */
    @Getter
    private final ScheduledJobStatus jobStatus;

    /**
     * The method type and parameters.
     */
    @Getter
    private String cloudToDeviceMethod;

    /**
     * The Update Twin tags and desired properties.
     */
    @Getter
    private Twin updateTwin;

    /**
     * The reason the job failed if the job failed.
     */
    @Getter
    private final String failureReason;

    /**
     * A string with status about the job execution.
     */
    @Getter
    private final String statusMessage;

    /**
     * The relevant statistics for this job.
     */
    @Getter
    private JobStatistics jobStatistics;

    /**
     * The deviceId related to this response. It can be null (e.g. in case of a parent orchestration).
     */
    @Getter
    private final String deviceId;

    /**
     * The jobId of the parent orchestration, if any.
     */
    @Getter
    private final String parentJobId;

    @Getter
    private DirectMethodResponse outcomeResult;

    /**
     * The error message of the job in query, if any.
     */
    @Getter
    private String error;

    /**
     * CONSTRUCTOR
     *
     * @param json the json string to be deserialized
     * @throws JsonParseException if the content of body is a invalid json
     * @throws IllegalArgumentException if the provided body is null
     */
    public ScheduledJob(String json) throws JsonParseException, IllegalArgumentException
    {
        if (json == null)
        {
            throw new IllegalArgumentException("null body");
        }

        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        this.jobId = jobsResponseParser.getJobId();
        this.queryCondition = jobsResponseParser.getQueryCondition();
        this.createdTime = jobsResponseParser.getCreatedTimeDate();
        this.startTime = jobsResponseParser.getStartTimeDate();
        this.endTime = jobsResponseParser.getEndTimeDate();
        this.lastUpdatedDateTime = jobsResponseParser.getLastUpdatedTimeDate();
        this.maxExecutionTimeInSeconds = jobsResponseParser.getMaxExecutionTimeInSeconds();
        this.jobType = ScheduledJobType.valueOf(jobsResponseParser.getType());
        this.jobStatus = ScheduledJobStatus.valueOf(jobsResponseParser.getJobsStatus());

        if (jobsResponseParser.getCloudToDeviceMethod() != null)
        {
            this.cloudToDeviceMethod = jobsResponseParser.getCloudToDeviceMethod().toJson();
        }

        if (jobsResponseParser.getOutcome() != null)
        {
            if (this.jobType == ScheduledJobType.scheduleDeviceMethod)
            {
                try
                {
                    this.outcomeResult = new DirectMethodResponse(
                        jobsResponseParser.getCloudToDeviceMethod().getStatus(),
                        new GsonBuilder().create().toJsonTree(jobsResponseParser.getCloudToDeviceMethod().getPayload()));
                }
                catch (IllegalArgumentException e)
                {
                    this.outcomeResult = null;
                }
            }
        }

        if (jobsResponseParser.getError() != null)
        {
            this.error = jobsResponseParser.getError().toJson();
        }

        TwinState twinState = jobsResponseParser.getUpdateTwin();
        if (twinState != null)
        {
            this.updateTwin = twinState.getDeviceId() == null || twinState.getDeviceId().isEmpty() ?
                new Twin() : new Twin(twinState.getDeviceId());
            this.updateTwin.setETag(twinState.getETag());
            this.updateTwin.setTags(mapToSet(twinState.getTags()));
            this.updateTwin.setDesiredProperties(mapToSet(twinState.getDesiredProperty()));
        }

        this.failureReason = jobsResponseParser.getFailureReason();
        this.statusMessage = jobsResponseParser.getStatusMessage();
        JobsStatisticsParser jobsStatisticsParser = jobsResponseParser.getDeviceJobStatistics();
        if (jobsStatisticsParser != null)
        {
            this.jobStatistics = new JobStatistics(jobsStatisticsParser);
        }

        this.deviceId = jobsResponseParser.getDeviceId();
        this.parentJobId = jobsResponseParser.getParentJobId();
    }

    /**
     * Return a string with a pretty print json with the content of this class
     *
     * @return a String with a json that represents the content of this class
     */
    @Override
    public String toString()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    private Set<Pair> mapToSet(Map<String, Object> map)
    {
        Set<Pair> setPair = new HashSet<>();

        if (map != null)
        {
            for (Map.Entry<String, Object> setEntry : map.entrySet())
            {
                setPair.add(new Pair(setEntry.getKey(), setEntry.getValue()));
            }
        }

        return setPair;
    }
}
