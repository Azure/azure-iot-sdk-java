// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;

/**
 * Twin management representation
 *
 * This class is part of the Twin. It contains the Device identity management.
 */
public class RegisterManager
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


    /**
     * Setter for the DeviceId
     *
     * @param deviceId the {@code String} that contains the new DeviceID.
     * @throws IllegalArgumentException If the new DeviceId do not fits the ID criteria.
     */
    public void setDeviceId(String deviceId) throws IllegalArgumentException
    {
        /* Codes_SRS_REGISTER_MANAGER_21_001: [The setDeviceId shall throw IllegalArgumentException if the provided deviceId do not fits the criteria.] */
        ParserUtility.validateId(deviceId);

        /* Codes_SRS_REGISTER_MANAGER_21_002: [The setDeviceId shall replace the `deviceId` by the provided one.] */
        this.deviceId = deviceId;
    }

    /**
     * Setter for the ETag
     *
     * @param eTag the {@code String} that contains the new ETag.
     */
    public void setETag(String eTag)
    {
        /* Codes_SRS_REGISTER_MANAGER_21_003: [The setETag shall replace the `eTag` by the provided one.] */
        this.eTag = eTag;
    }

    /**
     * Getter for the ETag
     * @return The {@code String} with the stored ETag.
     */
    public String getETag()
    {
        /* Codes_SRS_REGISTER_MANAGER_21_004: [The getETag shall return the stored `eTag` content.] */
        return this.eTag;
    }

    /**
     * Getter for the DeviceId
     * @return The {@code String} with the stored DeviceID.
     */
    public String getDeviceId()
    {
        /* Codes_SRS_REGISTER_MANAGER_21_005: [The getDeviceId shall return the stored `deviceId` content.] */
        return this.deviceId;
    }

    /**
     * Getter for the Version
     * @return The {@code Integer} with the stored version.
     */
    public Integer getVersion()
    {
        /* Codes_SRS_REGISTER_MANAGER_21_006: [The getVersion shall return the stored `version` content.] */
        return this.version;
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    protected RegisterManager()
    {
        /* Codes_SRS_REGISTER_MANAGER_21_007: [The RegisterManager shall provide an empty constructor to make GSON happy.] */
    }
}
