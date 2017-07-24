// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * Representation of a single Job scheduled on the Iothub.
 */
public class Job
{
    private String jobId;
    private JobClient jobClient;

    /**
     * CONSTRUCTOR
     *
     * @param connectionString is the IoTHub connection string.
     * @throws IOException This exception is thrown if the object creation failed
     */
    Job(String connectionString) throws IOException
    {
        if ((connectionString == null) || connectionString.isEmpty())
        {
            /* Codes_SRS_JOB_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        /* Codes_SRS_JOB_21_003: [If no jobId is provided, the constructor shall generate a unique jobId to identify the Job in the Iothub.] */
        this.jobId = "JOB-" + UUID.randomUUID();

        /* Codes_SRS_JOB_21_004: [The constructor shall create a new instance of JobClient to manage the Job.] */
        /* Codes_SRS_JOB_21_005: [The constructor shall throw IOException if it failed to create a new instance of the JobClient. Threw by the JobClient constructor.] */
        this.jobClient = JobClient.createFromConnectionString(connectionString);
    }

    /**
     * CONSTRUCTOR
     * 
     * @param jobId is the Job name.
     * @param connectionString is the IoTHub connection string.
     * @throws IOException This exception is thrown if the object creation failed
     */
    Job(String jobId, String connectionString) throws IOException
    {
        if ((connectionString == null) || connectionString.isEmpty())
        {
            /* Codes_SRS_JOB_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        /* Codes_SRS_JOB_21_002: [If a jobId is provided, the constructor shall use this jobId to identify the Job in the Iothub.] */
        this.jobId = jobId;

        /* Codes_SRS_JOB_21_004: [The constructor shall create a new instance of JobClient to manage the Job.] */
        /* Codes_SRS_JOB_21_005: [The constructor shall throw IOException if it failed to create a new instance of the JobClient. Threw by the JobClient constructor.] */
        this.jobClient = JobClient.createFromConnectionString(connectionString);
    }

    /**
     * Creates a new Job to update twin tags and desired properties on one or multiple devices
     *
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param updateTwin Twin object to use for the update
     * @param startTimeUtc Date time in Utc to start the job
     * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run
     * @throws IOException if the function contains invalid parameters
     * @throws IotHubException if the http request failed
     */
    void scheduleUpdateTwin(String queryCondition,
                            DeviceTwinDevice updateTwin,
                            Date startTimeUtc, long maxExecutionTimeInSeconds) throws IOException, IotHubException
    {
        /* Codes_SRS_JOB_21_006: [If the updateTwin is null, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
        if(updateTwin == null)
        {
            throw new IllegalArgumentException("null updateTwin");
        }

        /* Codes_SRS_JOB_21_007: [If the startTimeUtc is null, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
        if(startTimeUtc == null)
        {
            throw new IllegalArgumentException("null startTimeUtc");
        }

        /* Codes_SRS_JOB_21_008: [If the maxExecutionTimeInSeconds is negative, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
        if(maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("negative maxExecutionTimeInSeconds");
        }

        /* Codes_SRS_JOB_21_009: [The scheduleUpdateTwin shall invoke the scheduleUpdateTwin in the JobClient class with the received parameters.] */
        /* Codes_SRS_JOB_21_010: [If scheduleUpdateTwin failed, the scheduleUpdateTwin shall throws IotHubException. Threw by the scheduleUpdateTwin.] */
        JobResult jobResult = this.jobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);

        if(jobResult.getJobStatus() == JobStatus.failed)
        {
            /* Codes_SRS_JOB_21_011: [If the Iothub reported fail as result of the scheduleUpdateTwin, the scheduleUpdateTwin shall throws IotHubException.] */
            throw new IotHubException(jobResult.getStatusMessage() == null ? "Iothub failed to create the job" : jobResult.getStatusMessage());
        }
    }

    /**
     * Creates a new Job to invoke method on one or multiple devices
     *
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param methodName Method name to be invoked
     * @param responseTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for answer. It can be {@code null}.
     * @param connectTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for the connection. It can be {@code null}.
     * @param payload Object that contains the payload defined by the user. It can be {@code null}.
     * @param startTimeUtc Date time in Utc to start the job
     * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run
     * @throws IOException if the function contains invalid parameters
     * @throws IotHubException if the http request failed
     */
    void scheduleDeviceMethod(String queryCondition,
                              String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload,
                              Date startTimeUtc, long maxExecutionTimeInSeconds) throws IOException, IotHubException
    {
        /* Codes_SRS_JOB_21_012: [If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
        if((methodName == null) || methodName.isEmpty())
        {
            throw new IllegalArgumentException("null updateTwin");
        }

        /* Codes_SRS_JOB_21_013: [If the startTimeUtc is null, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
        if(startTimeUtc == null)
        {
            throw new IllegalArgumentException("null startTimeUtc");
        }

        /* Codes_SRS_JOB_21_014: [If the maxExecutionTimeInSeconds is negative, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
        if(maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("negative maxExecutionTimeInSeconds");
        }

        /* Codes_SRS_JOB_21_015: [The scheduleDeviceMethod shall invoke the scheduleDeviceMethod in the JobClient class with the received parameters.] */
        /* Codes_SRS_JOB_21_016: [If scheduleDeviceMethod failed, the scheduleDeviceMethod shall throws IotHubException. Threw by the scheduleUpdateTwin.] */
        JobResult jobResult = this.jobClient.scheduleDeviceMethod(
                jobId, queryCondition,
                methodName, responseTimeoutInSeconds, connectTimeoutInSeconds, payload,
                startTimeUtc, maxExecutionTimeInSeconds);

        if(jobResult.getJobStatus() == JobStatus.failed)
        {
            /* Codes_SRS_JOB_21_017: [If the Iothub reported fail as result of the scheduleDeviceMethod, the scheduleDeviceMethod shall throws IotHubException.] */
            throw new IotHubException(jobResult.getStatusMessage() == null ? "Iothub failed to create the job" : jobResult.getStatusMessage());
        }
    }

    /**
     * Get the current job status on the iotHub.
     *
     * @return a jobResult object with the current job status.
     * @throws IOException if the function failed to get the status with the current job information
     * @throws IotHubException if the http request failed
     */
    public JobResult get() throws IOException, IotHubException
    {
        /* Codes_SRS_JOB_21_018: [The get shall invoke getJob on JobClient with the current jobId and return its result.] */
        /* Codes_SRS_JOB_21_019: [If getJob failed, the get shall throws IOException. Threw by the getJob.] */
        return this.jobClient.getJob(jobId);
    }

    /**
     * Cancel a current jod on the IoTHub
     *
     * @return a jobResult object with the current job status.
     * @throws IOException if the function failed to cancel the job with the current job information
     * @throws IotHubException if the http request failed
     */
    public JobResult cancel() throws IOException, IotHubException
    {
        /* Codes_SRS_JOB_21_020: [The cancel shall invoke cancelJob on JobClient with the current jobId and return its result.] */
        /* Codes_SRS_JOB_21_021: [If cancelJob failed, the cancel shall throws IOException. Threw by the cancelJob.] */
        return this.jobClient.cancelJob(jobId);
    }

    /**
     * Getter for the JobId.
     * 
     * @return the current jobId.
     */
    public String getJobId()
    {
        /* Codes_SRS_JOB_21_022: [The getJobId shall return the store value of jobId.] */
        return this.jobId;
    }
}
