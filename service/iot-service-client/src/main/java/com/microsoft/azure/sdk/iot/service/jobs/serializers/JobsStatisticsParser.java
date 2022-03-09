// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs.serializers;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * Representation of a single Jobs statistics collection with a Json deserializer.
 */
@SuppressWarnings("unused") // A number of private members are unused but may be filled in or used by serialization
public class JobsStatisticsParser
{
    // Number of devices in the job
    private static final String DEVICECOUNT_TAG = "deviceCount";
    @SerializedName(DEVICECOUNT_TAG)
    @Getter
    private int deviceCount;

    // The number of failed jobs
    private static final String FAILEDCOUNT_TAG = "failedCount";
    @SerializedName(FAILEDCOUNT_TAG)
    @Getter
    private int failedCount;

    // The number of Succeeded jobs
    private static final String SUCCEEDEDCOUNT_TAG = "succeededCount";
    @SerializedName(SUCCEEDEDCOUNT_TAG)
    @Getter
    private int succeededCount;

    // The number of running jobs
    private static final String RUNNINGCOUNT_TAG = "runningCount";
    @SerializedName(RUNNINGCOUNT_TAG)
    @Getter
    private int runningCount;

    // The number of pending (scheduled) jobs
    private static final String PENDINGCOUNT_TAG = "pendingCount";
    @SerializedName(PENDINGCOUNT_TAG)
    @Getter
    private int pendingCount;
}
