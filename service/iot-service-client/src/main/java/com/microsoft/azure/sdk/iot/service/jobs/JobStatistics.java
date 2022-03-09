// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs;

import com.microsoft.azure.sdk.iot.service.jobs.serializers.JobsStatisticsParser;
import lombok.Getter;

/**
 * Collection of jobs statistics.
 */
public class JobStatistics
{
    /**
     * Number of devices in the job.
     */
    @Getter
    private final int deviceCount;

    /**
     * The number of failed jobs.
     */
    @Getter
    private final int failedCount;

    /**
     * The number of successfully completed jobs.
     */
    @Getter
    private final int succeededCount;

    /**
     * The number of running jobs.
     */
    @Getter
    private final int runningCount;

    /**
     * The number of pending (scheduled) jobs.
     */
    @Getter
    private final int pendingCount;


    /**
     * CONSTRUCTOR
     * 
     * @param jobsStatisticsParser is the result of a deserialization for json with jobs statistics
     * @throws IllegalArgumentException is the provided jobsStatisticsParser is {@code null}
     */
    JobStatistics(JobsStatisticsParser jobsStatisticsParser) throws IllegalArgumentException
    {
        /* Codes_SRS_JOBSTATISTICS_21_001: [The constructor shall throw IllegalArgumentException if the input jobsStatisticsParser is null.] */
        if (jobsStatisticsParser == null)
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
}
