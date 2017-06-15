package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.annotations.SerializedName;

/**
 * INNER JOBS CLASS
 *
 * Enum for jobs type
 */
public enum JobsType
{
    @SerializedName("scheduleDirectRequest")
    scheduleDirectRequest,

    @SerializedName("scheduleTwinUpdate")
    scheduleTwinUpdate
}
