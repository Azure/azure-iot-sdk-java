// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.microsoft.azure.sdk.iot.service.serializers.JobsResponseParser;
import com.microsoft.azure.sdk.iot.service.serializers.JobsStatisticsParser;
import com.microsoft.azure.sdk.iot.service.devicetwin.TwinState;
import com.microsoft.azure.sdk.iot.service.devicetwin.Twin;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Collection with the result of a job operation.
 */
public class JobResult
{
    private static final Charset DEFAULT_IOTHUB_MESSAGE_CHARSET = StandardCharsets.UTF_8;

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
    private final JobType jobType;

    /**
     * The status of the job.
     */
    @Getter
    private final JobStatus jobStatus;

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
    private MethodResult outcomeResult;

    /**
     * The error message of the job in query, if any.
     */
    @Getter
    private String error;

    /**
     * CONSTRUCTOR
     *
     * @param body is a array of bytes that contains the response message for jobs
     * @throws JsonParseException if the content of body is a invalid json
     * @throws IllegalArgumentException if the provided body is null
     */
    JobResult(byte[] body) throws JsonParseException, IllegalArgumentException
    {
        /* Codes_SRS_JOBRESULT_21_001: [The constructor shall throw IllegalArgumentException if the input body is null.] */
        if(body == null)
        {
            throw new IllegalArgumentException("null body");
        }

        /* Codes_SRS_JOBRESULT_21_002: [The constructor shall parse the body using the JobsResponseParser.] */
        /* Codes_SRS_JOBRESULT_21_003: [The constructor shall throw JsonParseException if the input body contains a invalid json.] */
        String json = new String(body, DEFAULT_IOTHUB_MESSAGE_CHARSET);
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        /* Codes_SRS_JOBRESULT_21_004: [The constructor shall locally store all results information in the provided body.] */
        this.jobId = jobsResponseParser.getJobId();
        this.queryCondition = jobsResponseParser.getQueryCondition();
        this.createdTime = jobsResponseParser.getCreatedTime();
        this.startTime = jobsResponseParser.getStartTime();
        this.endTime = jobsResponseParser.getEndTime();
        this.lastUpdatedDateTime = jobsResponseParser.getLastUpdatedTimeDate();
        this.maxExecutionTimeInSeconds = jobsResponseParser.getMaxExecutionTimeInSeconds();
        this.jobType = JobType.valueOf(jobsResponseParser.getType());
        this.jobStatus = JobStatus.valueOf(jobsResponseParser.getJobsStatus());

        if(jobsResponseParser.getCloudToDeviceMethod() != null)
        {
            this.cloudToDeviceMethod = jobsResponseParser.getCloudToDeviceMethod().toJson();
        }

        if (jobsResponseParser.getOutcome() != null)
        {
            if (this.jobType == JobType.scheduleDeviceMethod)
            {
                try
                {
                    this.outcomeResult = new MethodResult(jobsResponseParser.getOutcome().getStatus(), jobsResponseParser.getOutcome().getPayload());
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

        TwinState twinState = jobsResponseParser.getUpdateTwinState();
        if(twinState != null)
        {
            this.updateTwin = twinState.getDeviceId() == null || twinState.getDeviceId().isEmpty() ?
                new Twin() : new Twin(twinState.getDeviceId());
            this.updateTwin.setETag(twinState.getETag());
            this.updateTwin.setTags(mapToSet(twinState.getTags()));
            this.updateTwin.setDesiredProperties(mapToSet(twinState.getDesiredProperty()));
        }
        this.failureReason = jobsResponseParser.getFailureReason();
        this.statusMessage = jobsResponseParser.getStatusMessage();
        JobsStatisticsParser jobsStatisticsParser = jobsResponseParser.getJobStatistics();
        if(jobsStatisticsParser != null)
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
        /* Codes_SRS_JOBRESULT_21_020: [The toString shall return a String with a pretty print json that represents this class.] */
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
