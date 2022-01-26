// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;
import lombok.Getter;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * Representation of a single Job scheduled on the Iothub.
 */
class Job
{
    @Getter
    private final String jobId;
    private final JobClient jobClient;

    Job(String connectionString)
    {
        if ((connectionString == null) || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        this.jobId = "JOB-" + UUID.randomUUID();
        this.jobClient = new JobClient(connectionString);
    }

    Job(String hostName, TokenCredential credential)
    {
        this.jobId = "JOB-" + UUID.randomUUID();
        this.jobClient = new JobClient(hostName, credential);
    }

    Job(String hostName, AzureSasCredential azureSasCredential)
    {
        this.jobId = "JOB-" + UUID.randomUUID();
        this.jobClient = new JobClient(hostName, azureSasCredential);
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
    void scheduleUpdateTwin(
        String queryCondition,
        Twin updateTwin,
        Date startTimeUtc,
        long maxExecutionTimeInSeconds)
        throws IOException, IotHubException
    {
        if (updateTwin == null)
        {
            throw new IllegalArgumentException("updateTwin cannot be null");
        }

        if (startTimeUtc == null)
        {
            throw new IllegalArgumentException("startTimeUtc cannot be null");
        }

        if (maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("maxExecutionTimeInSeconds cannot be negative");
        }

        JobResult jobResult = this.jobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);

        if (jobResult.getJobStatus() == JobStatus.failed)
        {
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
    void scheduleDeviceMethod(
        String queryCondition,
        String methodName,
        Long responseTimeoutInSeconds,
        Long connectTimeoutInSeconds,
        Object payload,
        Date startTimeUtc,
        long maxExecutionTimeInSeconds)
        throws IOException, IotHubException
    {
        if (methodName == null || methodName.isEmpty())
        {
            throw new IllegalArgumentException("method name cannot be null or empty");
        }

        if (startTimeUtc == null)
        {
            throw new IllegalArgumentException("startTimeUtc cannot be null");
        }

        if (maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("maxExecutionTimeInSeconds cannot be negative");
        }

        JobResult jobResult = this.jobClient.scheduleDeviceMethod(
            jobId,
            queryCondition,
            methodName,
            responseTimeoutInSeconds,
            connectTimeoutInSeconds,
            payload,
            startTimeUtc,
            maxExecutionTimeInSeconds);

        if (jobResult.getJobStatus() == JobStatus.failed)
        {
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
        return this.jobClient.cancelJob(jobId);
    }
}
