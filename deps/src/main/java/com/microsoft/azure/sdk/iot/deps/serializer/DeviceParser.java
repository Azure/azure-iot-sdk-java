// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class DeviceParser
{
    private static final String E_TAG_NAME = "etag";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(E_TAG_NAME)
    private String eTag;

    private static final String DEVICE_ID_NAME = "deviceId";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(DEVICE_ID_NAME)
    private String deviceId;

    private static final String MODULE_ID_NAME = "moduleId";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(MODULE_ID_NAME)
    private String moduleId;

    private static final String GENERATION_ID_NAME = "generationId";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(GENERATION_ID_NAME)
    private String generationId;

    private static final String STATUS_NAME = "status";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(STATUS_NAME)
    private String status;

    private static final String STATUS_REASON = "statusReason";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(STATUS_REASON)
    private String statusReason;

    private static final String STATUS_UPDATED_TIME_NAME = "statusUpdatedTime";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(STATUS_UPDATED_TIME_NAME)
    private String statusUpdatedTimeString;
    private transient Date statusUpdatedTime;

    private static final String CONNECTION_STATE_NAME = "connectionState";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CONNECTION_STATE_NAME)
    private String connectionState;

    private static final String CONNECTION_STATE_UPDATED_TIME_NAME = "connectionStateUpdatedTime";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(CONNECTION_STATE_UPDATED_TIME_NAME)
    private String connectionStateUpdatedTimeString;
    private transient Date connectionStateUpdatedTime;

    private static final String LAST_ACTIVITY_TIME_NAME = "lastActivityTime";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(LAST_ACTIVITY_TIME_NAME)
    private String lastActivityTimeString;
    private transient Date lastActivityTime;

    private static final String CLOUD_TO_MESSAGE_COUNT_NAME = "cloudToDeviceMessageCount";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CLOUD_TO_MESSAGE_COUNT_NAME)
    private long cloudToDeviceMessageCount;

    private static final String AUTHENTICATION_NAME = "authentication";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(AUTHENTICATION_NAME)
    private AuthenticationParser authenticationParser;

    private static final String MANAGED_BY = "managedBy";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(MANAGED_BY)
    private String managedBy;

    private static final String CAPABILITIES_NAME = "capabilities";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CAPABILITIES_NAME)
    private DeviceCapabilitiesParser capabilities;

    private transient Gson gson = new Gson();

    /**
     * Converts this into json format and returns it
     * @return the json representation of this
     */
    public String toJson()
    {
        if (this.statusUpdatedTime != null)
        {
            this.statusUpdatedTimeString = ParserUtility.getDateStringFromDate(this.statusUpdatedTime);
        }

        if (this.connectionStateUpdatedTime != null)
        {
            this.connectionStateUpdatedTimeString = ParserUtility.getDateStringFromDate(this.connectionStateUpdatedTime);
        }

        if (this.lastActivityTime != null)
        {
            this.lastActivityTimeString = ParserUtility.getDateStringFromDate(this.lastActivityTime);
        }

        //Codes_SRS_DEVICE_PARSER_34_001: [This method shall return a json representation of this.]
        return gson.toJson(this);
    }

    /**
     * Empty constructor
     */
    public DeviceParser()
    {
    }

    /**
     * Constructor for a DeviceParser object that is built from the provided json.
     * @param json the json to build the object from
     * @throws IllegalArgumentException if the provided json is null, empty, or not the expected format
     */
    public DeviceParser(String json)
    {
        if (json == null || json.isEmpty())
        {
            //Codes_SRS_DEVICE_PARSER_34_005: [If the provided json is null or empty, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json cannot be null or empty");
        }

        DeviceParser deviceParser;
        try
        {
            deviceParser = gson.fromJson(json, DeviceParser.class);
        }
        catch (JsonSyntaxException e)
        {
            //Codes_SRS_DEVICE_PARSER_34_006: [If the provided json cannot be parsed into a DeviceParser object, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json could not be parsed");
        }

        //Codes_SRS_DEVICE_PARSER_34_011: [If the provided json is missing the DeviceId field or its value is empty, an IllegalArgumentException shall be thrown.]
        if (deviceParser.deviceId == null || deviceParser.deviceId.isEmpty())
        {
            throw new IllegalArgumentException("The provided json must contain the field for deviceId and its value may not be empty");
        }

        //Codes_SRS_DEVICE_PARSER_34_012: [If the provided json is missing the authentication field or its value is empty, an IllegalArgumentException shall be thrown.]
        if (deviceParser.authenticationParser == null)
        {
            throw new IllegalArgumentException("The provided json must contain the field for authentication and its value may not be empty");
        }

        //Codes_SRS_DEVICE_PARSER_34_002: [This constructor shall create a DeviceParser object based off of the provided json.]
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

        //convert to date format
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
     * Getter for moduleId
     *
     * @return The value of moduleId
     */
    public String getModuleId()
    {
        //Codes_SRS_DEVICE_PARSER_28_001: [This method shall return the value of this object's Module.]
        return moduleId;
    }

    /**
     * Setter for moduleId
     * @param moduleId the value to set moduleId to
     * @throws IllegalArgumentException if moduleId is null
     */
    public void setModuleId(String moduleId) throws IllegalArgumentException
    {
        //Codes_SRS_DEVICE_PARSER_28_002: [If the provided deviceId value is null, an IllegalArgumentException shall be thrown.]
        if (moduleId == null || moduleId.isEmpty())
        {
            throw new IllegalArgumentException("DeviceId cannot not be null");
        }

        //Codes_SRS_DEVICE_PARSER_34_009: [This method shall set the value of deviceId to the provided value.]
        this.moduleId = moduleId;
    }

    /**
     * Getter for DeviceId
     *
     * @return The value of DeviceId
     */
    public String getDeviceId()
    {
        //Codes_SRS_DEVICE_PARSER_34_032: [This method shall return the value of this object's DeviceId.]
        return deviceId;
    }

    /**
     * Setter for DeviceId
     * @param deviceId the value to set deviceId to
     * @throws IllegalArgumentException if deviceId is null
     */
    public void setDeviceId(String deviceId) throws IllegalArgumentException
    {

        //Codes_SRS_DEVICE_PARSER_34_010: [If the provided deviceId value is null, an IllegalArgumentException shall be thrown.]
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("DeviceId cannot not be null");
        }

        //Codes_SRS_DEVICE_PARSER_34_009: [This method shall set the value of deviceId to the provided value.]
        this.deviceId = deviceId;
    }

    /**
     * Getter for AuthenticationParser
     *
     * @return The value of AuthenticationParser
     */
    public AuthenticationParser getAuthenticationParser()
    {
        //Codes_SRS_DEVICE_PARSER_34_031: [This method shall return the value of this object's AuthenticationParser.]
        return authenticationParser;
    }

    /**
     * Setter for AuthenticationParser
     * @param authenticationParser the value to set authenticationParser to
     * @throws IllegalArgumentException if authenticationParser is null
     */
    public void setAuthenticationParser(AuthenticationParser authenticationParser) throws IllegalArgumentException
    {
        //Codes_SRS_DEVICE_PARSER_34_008: [If the provided authenticationParser value is null, an IllegalArgumentException shall be thrown.]
        if (authenticationParser == null)
        {
            throw new IllegalArgumentException("Authentication cannot not be null");
        }

        //Codes_SRS_DEVICE_PARSER_34_007: [This method shall set the value of authenticationParser to the provided value.]
        this.authenticationParser = authenticationParser;
    }

    /**
     * Getter for eTag
     *
     * @return The value of eTag
     */
    public String geteTag()
    {
        //Codes_SRS_DEVICE_PARSER_34_014: [This method shall return the value of this object's ETag.]
        return "\"" + eTag + "\"";
    }

    /**
     * Setter for eTag
     * @param eTag the value to set eTag to
     */
    public void seteTag(String eTag)
    {
        //Codes_SRS_DEVICE_PARSER_34_013: [This method shall set the value of this object's ETag equal to the provided value.]
        this.eTag = eTag;
    }

    /**
     * Getter for GenerationId
     *
     * @return The value of GenerationId
     */
    public String getGenerationId()
    {
        //Codes_SRS_DEVICE_PARSER_34_016: [This method shall return the value of this object's Generation Id.]
        return generationId;
    }

    /**
     * Setter for GenerationId
     * @param generationId the value to set generationId to
     */
    public void setGenerationId(String generationId)
    {
        //Codes_SRS_DEVICE_PARSER_34_015: [This method shall set the value of this object's Generation Id equal to the provided value.]
        this.generationId = generationId;
    }

    /**
     * Getter for Status
     *
     * @return The value of Status
     */
    public String getStatus()
    {
        //Codes_SRS_DEVICE_PARSER_34_018: [This method shall return the value of this object's Status.]
        return status;
    }

    /**
     * Setter for Status
     * @param status the value to set status to
     */
    public void setStatus(String status)
    {
        //Codes_SRS_DEVICE_PARSER_34_017: [This method shall set the value of this object's Status equal to the provided value.]
        this.status = status;
    }

    /**
     * Getter for StatusReason
     *
     * @return The value of StatusReason
     */
    public String getStatusReason()
    {
        //Codes_SRS_DEVICE_PARSER_34_020: [This method shall return the value of this object's Status Reason.]
        return statusReason;
    }

    /**
     * Setter for StatusReason
     * @param statusReason the value to set statusReason to
     */
    public void setStatusReason(String statusReason)
    {
        //Codes_SRS_DEVICE_PARSER_34_019: [This method shall set the value of this object's Status Reason equal to the provided value.]
        this.statusReason = statusReason;
    }

    /**
     * Getter for StatusUpdatedTime
     *
     * @return The value of StatusUpdatedTime
     */
    public Date getStatusUpdatedTime()
    {
        //Codes_SRS_DEVICE_PARSER_34_022: [This method shall return the value of this object's statusUpdatedTime.]
        return statusUpdatedTime;
    }

    /**
     * Setter for StatusUpdatedTime
     *
     * @param statusUpdatedTime the value to set StatusUpdatedTime to
     */
    public void setStatusUpdatedTime(Date statusUpdatedTime)
    {
        //Codes_SRS_DEVICE_PARSER_34_021: [This method shall set the value of this object's statusUpdatedTime equal to the provided value.]
        this.statusUpdatedTime = statusUpdatedTime;

        if (statusUpdatedTime == null)
        {
            this.statusUpdatedTimeString = null;
        }
        else
        {
            this.statusUpdatedTimeString = ParserUtility.getDateStringFromDate(statusUpdatedTime);
        }
    }

    /**
     * Getter for ConnectionState
     *
     * @return The value of ConnectionState
     */
    public String getConnectionState()
    {
        //Codes_SRS_DEVICE_PARSER_34_024: [This method shall return the value of this object's connectionState.]
        return connectionState;
    }

    /**
     * Setter for ConnectionState
     *
     * @param connectionState the value to set ConnectionState to
     */
    public void setConnectionState(String connectionState)
    {
        //Codes_SRS_DEVICE_PARSER_34_023: [This method shall set the value of this object's connectionState equal to the provided value.]
        this.connectionState = connectionState;
    }

    /**
     * Getter for ConnectionStateUpdatedTime
     *
     * @return The value of ConnectionStateUpdatedTime
     */
    public Date getConnectionStateUpdatedTime()
    {
        //Codes_SRS_DEVICE_PARSER_34_026: [This method shall return the value of this object's connectionStateUpdatedTime.]
        return connectionStateUpdatedTime;
    }

    /**
     * Setter for ConnectionStateUpdatedTime
     *
     * @param connectionStateUpdatedTime the value to set ConnectionStateUpdatedTime to
     */
    public void setConnectionStateUpdatedTime(Date connectionStateUpdatedTime)
    {
        //Codes_SRS_DEVICE_PARSER_34_025: [This method shall set the value of this object's connectionStateUpdatedTime equal to the provided value.]
        this.connectionStateUpdatedTime = connectionStateUpdatedTime;

        if (connectionStateUpdatedTime == null)
        {
            this.connectionStateUpdatedTimeString = null;
        }
        else
        {
            this.connectionStateUpdatedTimeString = ParserUtility.getDateStringFromDate(connectionStateUpdatedTime);
        }
    }

    /**
     * Getter for LastActivityTime
     *
     * @return The value of LastActivityTime
     */
    public Date getLastActivityTime()
    {
        //Codes_SRS_DEVICE_PARSER_34_028: [This method shall return the value of this object's lastActivityTime.]
        return lastActivityTime;
    }

    /**
     * Setter for LastActivityTime
     *
     * @param lastActivityTime the value to set LastActivityTime to
     */
    public void setLastActivityTime(Date lastActivityTime)
    {
        //Codes_SRS_DEVICE_PARSER_34_027: [This method shall set the value of this object's lastActivityTime equal to the provided value.]
        this.lastActivityTime = lastActivityTime;

        if (lastActivityTime == null)
        {
            this.lastActivityTimeString = null;
        }
        else
        {
            this.lastActivityTimeString = ParserUtility.getDateStringFromDate(lastActivityTime);
        }
    }

    /**
     * Getter for CloudToDeviceMessageCount
     *
     * @return The value of CloudToDeviceMessageCount
     */
    public long getCloudToDeviceMessageCount()
    {
        //Codes_SRS_DEVICE_PARSER_34_030: [This method shall return the value of this object's cloudToDeviceMessageCount.]
        return cloudToDeviceMessageCount;
    }

    /**
     * Setter for CloudToDeviceMessageCount
     *
     * @param cloudToDeviceMessageCount the value to set CloudToDeviceMessageCount to
     */
    public void setCloudToDeviceMessageCount(long cloudToDeviceMessageCount)
    {
        //Codes_SRS_DEVICE_PARSER_34_029: [This method shall set the value of this object's cloudToDeviceMessageCount equal to the provided value.]
        this.cloudToDeviceMessageCount = cloudToDeviceMessageCount;
    }

    /**
     * Getter for ManagedBy
     *
     * @return The value of ManagedBy
     */
    public String getManagedBy()
    {
        //Codes_SRS_DEVICE_PARSER_34_018: [This method shall return the value of this object's Status.]
        return managedBy;
    }

    /**
     * Setter for ManagedBy
     * @param managedBy the value to set managedBy to
     */
    public void setManagedBy(String managedBy)
    {
        //Codes_SRS_DEVICE_PARSER_34_017: [This method shall set the value of this object's Status equal to the provided value.]
        this.managedBy = managedBy;
    }

    /**
     * Getter for capabilities
     *
     * @return The value of capabilities
     */
    public DeviceCapabilitiesParser getCapabilities()
    {
        //Codes_SRS_DEVICE_PARSER_34_018: [This method shall return the value of this object's IotEdge.]
        return capabilities;
    }

    /**
     * Setter for capabilities
     * @param capabilities the value to set capabilities to
     */
    public void setCapabilities(DeviceCapabilitiesParser capabilities)
    {
        //Codes_SRS_DEVICE_PARSER_34_017: [This method shall set the value of this object's capabilities equal to the provided value.]
        this.capabilities = capabilities;
    }
}
