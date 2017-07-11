// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.annotations.SerializedName;

/**
 * Representation of a single Jobs statistics collection with a Json deserializer.
 */
public class JobsStatisticsParser
{
    // Number of devices in the job
    private static final String DEVICECOUNT_TAG = "deviceCount";
    @SerializedName(DEVICECOUNT_TAG)
    private int deviceCount;

    // The number of failed jobs
    private static final String FAILEDCOUNT_TAG = "failedCount";
    @SerializedName(FAILEDCOUNT_TAG)
    private int failedCount;

    // The number of Succeeded jobs
    private static final String SUCCEEDEDCOUNT_TAG = "succeededCount";
    @SerializedName(SUCCEEDEDCOUNT_TAG)
    private int succeededCount;

    // The number of running jobs
    private static final String RUNNINGCOUNT_TAG = "runningCount";
    @SerializedName(RUNNINGCOUNT_TAG)
    private int runningCount;

    // The number of pending (scheduled) jobs
    private static final String PENDINGCOUNT_TAG = "pendingCount";
    @SerializedName(PENDINGCOUNT_TAG)
    private int pendingCount;

    /**
     * Getter for device counter
     * @return the number of devices in the job
     */
    public int getDeviceCount()
    {
        /* Codes_SRS_JOBSSTATISTICSPARSER_21_001: [The getDeviceCount shall return the value of the deviceCount counter.] */
        return this.deviceCount;
    }

    /**
     * Getter for the failed counter
     * @return the number of failed jobs
     */
    public int getFailedCount()
    {
        /* Codes_SRS_JOBSSTATISTICSPARSER_21_002: [The getFailedCount shall return the value of the failedCount counter.] */
        return this.failedCount;
    }

    /**
     * Getter for succeeded counter
     * @return the number of succeeded jobs
     */
    public int getSucceededCount()
    {
        /* Codes_SRS_JOBSSTATISTICSPARSER_21_003: [The getSucceededCount shall return the value of the succeededCount counter.] */
        return this.succeededCount;
    }

    /**
     * Getter for running counter
     * @return the number of running jobs
     */
    public int getRunningCount()
    {
        /* Codes_SRS_JOBSSTATISTICSPARSER_21_004: [The getRunningCount shall return the value of the runningCount counter.] */
        return this.runningCount;
    }

    /**
     * Getter for pending counter
     * @return the number of pending jobs
     */
    public int getPendingCount()
    {
        /* Codes_SRS_JOBSSTATISTICSPARSER_21_005: [The getPendingCount shall return the value of the pendingCount counter.] */
        return this.pendingCount;
    }
}
