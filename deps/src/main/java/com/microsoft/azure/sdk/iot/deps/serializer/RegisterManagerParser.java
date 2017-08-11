// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

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
    @SerializedName("deviceId")
    protected String deviceId = null;

    /**
     * Device generation Id
     */
    @SerializedName("generationId")
    protected String generationId = null;

    /**
     * A string representing a weak ETAG version
     * of this JSON description. This is a hash.
     */
    @SerializedName("etag")
    protected String eTag = null;

    /**
     * An Integer representing a Twin version.
     */
    @SerializedName("version")
    protected Integer version = null;

    /**
     * "Enabled", "Disabled".
     * If "enabled", this device is authorized to connect.
     * If "disabled" this device cannot receive or send messages, and statusReason must be set.
     */
    @SerializedName("status")
    protected TwinStatus status = null;

    /**
     * A 128 char long string storing the reason of suspension.
     * (all UTF-8 chars allowed).
     */
    @SerializedName("statusReason")
    protected String statusReason = null;

    /**
     * Datetime of last time the state was updated.
     */
    @SerializedName("statusUpdatedTime")
    protected String statusUpdatedTime = null;

    /**
     * Status of the device:
     * {"connected" | "disconnected"}
     */
    @SerializedName("connectionState")
    protected TwinConnectionState connectionState = null;

    /**
     * Datetime of last time the connection state was updated.
     */
    @SerializedName("connectionStateUpdatedTime")
    protected String connectionStateUpdatedTime = null;

    /**
     * Datetime of last time the device authenticated, received, or sent a message.
     */
    @SerializedName("lastActivityTime")
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
