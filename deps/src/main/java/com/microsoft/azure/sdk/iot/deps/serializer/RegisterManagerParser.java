// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * INNER TWINPARSER CLASS
 *
 * Twin management representation
 *
 * This class is part of the Twin. It contains the Device identity management.
 */
public class RegisterManagerParser
{
    /**
     * Device name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    private static final String DEVICE_ID_TAG = "deviceId";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(DEVICE_ID_TAG)
    protected String deviceId = null;

    /**
     * Device generation Id
     */
    private static final String GENERATION_ID_TAG = "generationId";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(GENERATION_ID_TAG)
    protected String generationId = null;

    /**
     * A string representing a weak ETAG version
     * of this JSON description. This is a hash.
     */
    private static final String ETAG_TAG = "etag";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(ETAG_TAG)
    protected String eTag = null;

    /**
     * An Integer representing a Twin version.
     */
    private static final String VERSION_TAG = "version";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(VERSION_TAG)
    protected Integer version = null;

    /**
     * "Enabled", "Disabled".
     * If "enabled", this device is authorized to connect.
     * If "disabled" this device cannot receive or send messages, and statusReason must be set.
     */
    private static final String STATUS_TAG = "status";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(STATUS_TAG)
    protected TwinStatus status = null;

    /**
     * A 128 char long string storing the reason of suspension.
     * (all UTF-8 chars allowed).
     */
    private static final String STATUS_REASON_TAG = "statusReason";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(STATUS_REASON_TAG)
    protected String statusReason = null;

    /**
     * Datetime of last time the state was updated.
     */
    private static final String STATUS_UPDATED_TIME_TAG = "statusUpdatedTime";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(STATUS_UPDATED_TIME_TAG)
    protected String statusUpdatedTime = null;

    /**
     * Status of the device:
     * {"connected" | "disconnected"}
     */
    private static final String CONNECTION_STATE_TAG = "connectionState";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CONNECTION_STATE_TAG)
    protected TwinConnectionState connectionState = null;

    /**
     * Datetime of last time the connection state was updated.
     */
    private static final String CONNECTION_STATE_UPDATED_TIME_TAG = "connectionStateUpdatedTime";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CONNECTION_STATE_UPDATED_TIME_TAG)
    protected String connectionStateUpdatedTime = null;

    /**
     * Datetime of last time the device authenticated, received, or sent a message.
     */
    private static final String LAST_ACTIVITY_TIME_TAG = "lastActivityTime";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(LAST_ACTIVITY_TIME_TAG)
    protected String lastActivityTime = null;


    protected boolean setDeviceId(String deviceId) throws IllegalArgumentException
    {
        validateDeviceManager(deviceId, null, null);

        /* Codes_SRS_TWIN_21_159: [The updateDeviceManager shall replace the `deviceId` by the provided one.] */
        if((this.deviceId == null) || (deviceId == null) || (!this.deviceId.equals(deviceId)))
        {
            this.deviceId = deviceId;
            if(this.deviceId != null)
            {
                return true;
            }
        }
        return false;
    }

    protected boolean setStatus(TwinStatus status, String statusReason) throws IllegalArgumentException
    {
        validateDeviceManager(null, status, statusReason);

        /* Codes_SRS_TWIN_21_162: [The updateDeviceManager shall replace the `status` by the provided one.] */
        if(status == null)
        {
            if(this.status != null)
            {
                return true;
            }
            this.status = null;
            this.statusReason = null;
            this.statusUpdatedTime = null;
        }
        else
        {
            if(statusReason == null)
            {
                /* Codes_SRS_TWIN_21_165: [If the provided `status` is different than the previous one, and the `statusReason` is null, The updateDeviceManager shall throw IllegalArgumentException.] */
                throw new IllegalArgumentException("Change status without statusReason");
            }
            else if((this.status == null) || (!this.status.equals(status)))
            {
                this.status = status;

                /* Codes_SRS_TWIN_21_163: [If the provided `status` is different than the previous one, The updateDeviceManager shall replace the `statusReason` by the provided one.] */
                this.statusReason = statusReason;

                return true;
            }
        }
        return false;
    }

    protected void validateDeviceManager(String deviceId, TwinStatus status, String statusReason) throws IllegalArgumentException
    {
        if((deviceId != null) && (deviceId.length()>128))
        {
            throw new IllegalArgumentException("DeviceId bigger than 128 chars");
        }

        if((status != null) && (statusReason == null))
        {
            throw new IllegalArgumentException("Change status without statusReason");
        }

        if((statusReason != null) && (statusReason.length()>128))
        {
            throw new IllegalArgumentException("StatusReason bigger than 128 chars");
        }
    }
}
