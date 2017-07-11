// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs;

import com.microsoft.azure.sdk.iot.deps.serializer.JobsStatisticsParser;

/**
 * Collection of jobs statistics.
 */
public class JobStatistics
{
    // Number of devices in the job
    private int deviceCount;

    // The number of failed jobs
    private int failedCount;

    // The number of Succeeded jobs
    private int succeededCount;

    // The number of running jobs
    private int runningCount;

    // The number of pending (scheduled) jobs
    private int pendingCount;


    /**
     * CONSTRUCTOR
     * 
     * @param jobsStatisticsParser is the result of a deserialization for json with jobs statistics
     * @throws IllegalArgumentException is the provided jobsStatisticsParser is {@code null}
     */
    JobStatistics(JobsStatisticsParser jobsStatisticsParser) throws IllegalArgumentException
    {
        /* Codes_SRS_JOBSTATISTICS_21_001: [The constructor shall throw IllegalArgumentException if the input jobsStatisticsParser is null.] */
        if(jobsStatisticsParser == null)
        {
            throw new IllegalArgumentException("null jobsStatisticsParser");
        }

        /* Codes_SRS_JOBSTATISTICS_21_002: [The constructor shall locally store all statistics information in jobsStatisticsParser.] */
        this.deviceCount = jobsStatisticsParser.getDeviceCount();
        this.failedCount = jobsStatisticsParser.getFailedCount();
        this.succeededCount = jobsStatisticsParser.getSucceededCount();
        this.runningCount = jobsStatisticsParser.getRunningCount();
        this.pendingCount = jobsStatisticsParser.getPendingCount();
    }

    /**
     * Getter for device counter
     * @return the number of devices in the job
     */
    public int getDeviceCount()
    {
        /* Codes_SRS_JOBSTATISTICS_21_003: [The getDeviceCount shall return the stored device count.] */
        return this.deviceCount;
    }

    /**
     * Getter for the failed counter
     * @return the number of failed jobs
     */
    public int getFailedCount()
    {
        /* Codes_SRS_JOBSTATISTICS_21_004: [The getFailedCount shall return the stored failed count.] */
        return this.failedCount;
    }

    /**
     * Getter for succeeded counter
     * @return number of succeeded jobs
     */
    public int getSucceededCount()
    {
        /* Codes_SRS_JOBSTATISTICS_21_005: [The getSucceededCount shall return the stored succeeded count.] */
        return this.succeededCount;
    }

    /**
     * Getter for running counter
     * @return the number of running jobs
     */
    public int getRunningCount()
    {
        /* Codes_SRS_JOBSTATISTICS_21_006: [The getRunningCount shall return the stored running count.] */
        return this.runningCount;
    }

    /**
     * Getter for pending counter
     * @return the number of pending jobs
     */
    public int getPendingCount()
    {
        /* Codes_SRS_JOBSTATISTICS_21_007: [The getPendingCount shall return the stored pending count.] */
        return this.pendingCount;
    }
}
