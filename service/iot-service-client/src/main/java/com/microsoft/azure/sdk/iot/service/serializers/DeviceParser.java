// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.serializers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceParser
{
    private static final String E_TAG_NAME = "etag";
    @Expose
    @SerializedName(E_TAG_NAME)
    @Getter
    @Setter
    private String eTag;

    private static final String DEVICE_ID_NAME = "deviceId";
    @Expose
    @SerializedName(DEVICE_ID_NAME)
    @Getter
    @Setter
    private String deviceId;

    private static final String MODULE_ID_NAME = "moduleId";
    @Expose
    @SerializedName(MODULE_ID_NAME)
    @Getter
    @Setter
    private String moduleId;

    private static final String GENERATION_ID_NAME = "generationId";
    @Expose
    @SerializedName(GENERATION_ID_NAME)
    @Getter
    @Setter
    private String generationId;

    private static final String STATUS_NAME = "status";
    @Expose
    @SerializedName(STATUS_NAME)
    @Getter
    @Setter
    private String status;

    private static final String STATUS_REASON = "statusReason";
    @Expose
    @SerializedName(STATUS_REASON)
    @Getter
    @Setter
    private String statusReason;

    private static final String STATUS_UPDATED_TIME_NAME = "statusUpdatedTime";
    @Expose(deserialize = false)
    @SerializedName(STATUS_UPDATED_TIME_NAME)
    @Getter
    @Setter
    private String statusUpdatedTimeString;
    private transient Date statusUpdatedTime;

    private static final String CONNECTION_STATE_NAME = "connectionState";
    @Expose
    @SerializedName(CONNECTION_STATE_NAME)
    @Getter
    @Setter
    private String connectionState;

    private static final String CONNECTION_STATE_UPDATED_TIME_NAME = "connectionStateUpdatedTime";
    @Expose(deserialize = false)
    @SerializedName(CONNECTION_STATE_UPDATED_TIME_NAME)
    private String connectionStateUpdatedTimeString;
    private transient Date connectionStateUpdatedTime;

    private static final String LAST_ACTIVITY_TIME_NAME = "lastActivityTime";
    @Expose(deserialize = false)
    @SerializedName(LAST_ACTIVITY_TIME_NAME)
    private String lastActivityTimeString;
    private transient Date lastActivityTime;

    private static final String CLOUD_TO_MESSAGE_COUNT_NAME = "cloudToDeviceMessageCount";
    @Expose
    @SerializedName(CLOUD_TO_MESSAGE_COUNT_NAME)
    @Getter
    @Setter
    private long cloudToDeviceMessageCount;

    private static final String AUTHENTICATION_NAME = "authentication";
    @Expose
    @SerializedName(AUTHENTICATION_NAME)
    @Getter
    @Setter
    private AuthenticationParser authenticationParser;

    private static final String MANAGED_BY = "managedBy";
    @Expose
    @SerializedName(MANAGED_BY)
    @Getter
    @Setter
    private String managedBy;

    private static final String CAPABILITIES_NAME = "capabilities";
    @Expose
    @SerializedName(CAPABILITIES_NAME)
    @Getter
    @Setter
    private DeviceCapabilitiesParser capabilities;

    private static final String SCOPE_NAME = "deviceScope";
    @Expose
    @SerializedName(SCOPE_NAME)
    @Getter
    @Setter
    private String scope;

    private static final String PARENT_SCOPES_NAMES = "parentScopes";
    @Expose
    @SerializedName(PARENT_SCOPES_NAMES)
    @Getter
    @Setter
    private List<String> parentScopes = new ArrayList<>();

    private final transient Gson gson = new Gson();

    /**
     * Converts this into JSON format and returns it.
     *
     * @return the JSON representation of this.
     */
    public String toJson()
    {
        if (this.statusUpdatedTime != null)
        {
            this.statusUpdatedTimeString = ParserUtility.getUTCDateStringFromDate(this.statusUpdatedTime);
        }

        if (this.connectionStateUpdatedTime != null)
        {
            this.connectionStateUpdatedTimeString = ParserUtility.getUTCDateStringFromDate(this.connectionStateUpdatedTime);
        }

        if (this.lastActivityTime != null)
        {
            this.lastActivityTimeString = ParserUtility.getUTCDateStringFromDate(this.lastActivityTime);
        }

        return gson.toJson(this);
    }

    /**
     * Empty constructor.
     */
    public DeviceParser()
    {
    }

    /**
     * Constructor for a DeviceParser object that is built from the provided JSON.
     *
     * @param json The JSON to build the object from.
     * @throws IllegalArgumentException If the provided JSON is {@code null}, empty, or not the expected format.
     */
    public DeviceParser(String json)
    {
        if (json == null || json.isEmpty())
        {
            throw new IllegalArgumentException("The provided json cannot be null or empty.");
        }

        DeviceParser deviceParser;
        try
        {
            deviceParser = gson.fromJson(json, DeviceParser.class);
        }
        catch (JsonSyntaxException e)
        {
            throw new IllegalArgumentException("The provided json could not be parsed.");
        }

        if (deviceParser.deviceId == null || deviceParser.deviceId.isEmpty())
        {
            throw new IllegalArgumentException(
                    "The provided json must contain the field for deviceId and its value may not be empty.");
        }

        if (deviceParser.authenticationParser == null)
        {
            throw new IllegalArgumentException(
                    "The provided json must contain the field for authentication and its value may not be empty.");
        }

        this.authenticationParser = deviceParser.authenticationParser;
        this.connectionState = deviceParser.connectionState;
        this.deviceId = deviceParser.deviceId;
        this.moduleId = deviceParser.moduleId;
        this.statusReason = deviceParser.statusReason;
        this.cloudToDeviceMessageCount = deviceParser.cloudToDeviceMessageCount;
        this.connectionState = deviceParser.connectionState;
        this.generationId = deviceParser.generationId;
        this.eTag = deviceParser.eTag;
        this.status = deviceParser.status;
        this.managedBy = deviceParser.managedBy;
        this.capabilities = deviceParser.capabilities;
        this.scope = deviceParser.scope;
        this.parentScopes = deviceParser.parentScopes;

        // convert to date format
        if (deviceParser.lastActivityTimeString != null)
        {
            this.lastActivityTimeString = deviceParser.lastActivityTimeString;
            this.lastActivityTime = ParserUtility.getDateTimeUtc(deviceParser.lastActivityTimeString);
        }

        if (deviceParser.connectionStateUpdatedTimeString != null)
        {
            this.connectionStateUpdatedTimeString = deviceParser.connectionStateUpdatedTimeString;
            this.connectionStateUpdatedTime = ParserUtility.getDateTimeUtc(deviceParser.connectionStateUpdatedTimeString);
        }

        if (deviceParser.statusUpdatedTimeString != null)
        {
            this.statusUpdatedTimeString = deviceParser.statusUpdatedTimeString;
            this.statusUpdatedTime = ParserUtility.getDateTimeUtc(deviceParser.statusUpdatedTimeString);
        }
    }

    /**
     * Getter for StatusUpdatedTime.
     *
     * @return The value of StatusUpdatedTime.
     */
    public Date getStatusUpdatedTime()
    {
        return statusUpdatedTime;
    }

    /**
     * Setter for StatusUpdatedTime.
     *
     * @param statusUpdatedTime The value to set StatusUpdatedTime to.
     */
    public void setStatusUpdatedTime(Date statusUpdatedTime)
    {
        this.statusUpdatedTime = statusUpdatedTime;

        if (statusUpdatedTime == null)
        {
            this.statusUpdatedTimeString = null;
        }
        else
        {
            this.statusUpdatedTimeString = ParserUtility.getUTCDateStringFromDate(statusUpdatedTime);
        }
    }

    /**
     * Getter for ConnectionStateUpdatedTime.
     *
     * @return The value of ConnectionStateUpdatedTime.
     */
    public Date getConnectionStateUpdatedTime()
    {
        return connectionStateUpdatedTime;
    }

    /**
     * Setter for ConnectionStateUpdatedTime.
     *
     * @param connectionStateUpdatedTime The value to set ConnectionStateUpdatedTime to.
     */
    public void setConnectionStateUpdatedTime(Date connectionStateUpdatedTime)
    {
        this.connectionStateUpdatedTime = connectionStateUpdatedTime;

        if (connectionStateUpdatedTime == null)
        {
            this.connectionStateUpdatedTimeString = null;
        }
        else
        {
            this.connectionStateUpdatedTimeString = ParserUtility.getUTCDateStringFromDate(connectionStateUpdatedTime);
        }
    }

    /**
     * Getter for LastActivityTime.
     *
     * @return The value of LastActivityTime.
     */
    public Date getLastActivityTime()
    {
        return lastActivityTime;
    }

    /**
     * Setter for LastActivityTime.
     *
     * @param lastActivityTime The value to set LastActivityTime to.
     */
    public void setLastActivityTime(Date lastActivityTime)
    {
        this.lastActivityTime = lastActivityTime;

        if (lastActivityTime == null)
        {
            this.lastActivityTimeString = null;
        }
        else
        {
            this.lastActivityTimeString = ParserUtility.getUTCDateStringFromDate(lastActivityTime);
        }
    }
}
