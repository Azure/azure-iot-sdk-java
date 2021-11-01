// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * Twin management representation
 *
 * This class is part of the Twin. It contains the Device identity management.
 */
@SuppressWarnings({"unused", "CanBeFinal"}) // This class is the base for TwinState and should be preserved
public class RegisterManager
{
    /**
     * Device name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    private static final String DEVICE_ID_TAG = "deviceId";
    @Expose
    @SerializedName(DEVICE_ID_TAG)
    @Getter
    @Setter
    private String deviceId = null;

    /**
     * Module name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    private static final String MODULE_ID_TAG = "moduleId";
    @Expose
    @SerializedName(MODULE_ID_TAG)
    @Getter
    @Setter
    private String moduleId = null;

    /**
     * Device generation Id
     */
    private static final String GENERATION_ID_TAG = "generationId";
    @Expose
    @SerializedName(GENERATION_ID_TAG)
    @Getter
    @Setter
    private String generationId = null;

    /**
     * A string representing a weak ETAG version
     * of this JSON description. This is a hash.
     */
    private static final String ETAG_TAG = "etag";
    @Expose
    @SerializedName(ETAG_TAG)
    @Getter
    @Setter
    private String eTag = null;

    /**
     * An Integer representing a Twin version.
     */
    private static final String VERSION_TAG = "version";
    @Expose
    @SerializedName(VERSION_TAG)
    @Getter
    @Setter
    private Integer version = null;

    /**
     * "Enabled", "Disabled".
     * If "enabled", this device is authorized to connect.
     * If "disabled" this device cannot receive or send messages, and statusReason must be set.
     */
    private static final String STATUS_TAG = "status";
    @Expose
    @SerializedName(STATUS_TAG)
    @Getter
    @Setter
    private TwinStatus status = null;

    /**
     * A 128 char long string storing the reason of suspension.
     * (all UTF-8 chars allowed).
     */
    private static final String STATUS_REASON_TAG = "statusReason";
    @Expose
    @SerializedName(STATUS_REASON_TAG)
    @Getter
    @Setter
    private String statusReason = null;

    /**
     * Datetime of last time the state was updated.
     */
    private static final String STATUS_UPDATED_TIME_TAG = "statusUpdatedTime";
    @Expose
    @SerializedName(STATUS_UPDATED_TIME_TAG)
    @Getter
    @Setter
    private String statusUpdatedTime = null;

    /**
     * Status of the device:
     * {"connected" | "disconnected"}
     */
    private static final String CONNECTION_STATE_TAG = "connectionState";
    @Expose
    @SerializedName(CONNECTION_STATE_TAG)
    protected TwinConnectionState connectionState = null;

    /**
     * Datetime of last time the connection state was updated.
     */
    private static final String CONNECTION_STATE_UPDATED_TIME_TAG = "connectionStateUpdatedTime";
    @Expose
    @SerializedName(CONNECTION_STATE_UPDATED_TIME_TAG)
    @Getter
    @Setter
    private String connectionStateUpdatedTime = null;

    /**
     * Datetime of last time the device authenticated, received, or sent a message.
     */
    private static final String LAST_ACTIVITY_TIME_TAG = "lastActivityTime";
    @Expose
    @SerializedName(LAST_ACTIVITY_TIME_TAG)
    @Getter
    @Setter
    private String lastActivityTime = null;

    /**
     * Datetime of last time the device authenticated, received, or sent a message.
     */
    private static final String CAPABILITIES_TAG = "capabilities";
    @Expose
    @SerializedName(CAPABILITIES_TAG)
    @Getter
    @Setter
    private DeviceCapabilities capabilities = null;

    /**
     * The Digital Twin model id of the device and module
     * The value will be null for a non-pnp device.
     * The value will be null for a pnp device until the device connects and registers with the model id.
     */
    private static final String MODEL_ID = "modelId";
    @Expose
    @SerializedName(MODEL_ID)
    @Getter
    @Setter
    private String modelId = null;

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    RegisterManager()
    {
        /* Codes_SRS_REGISTER_MANAGER_21_007: [The RegisterManager shall provide an empty constructor to make GSON happy.] */
    }
}
