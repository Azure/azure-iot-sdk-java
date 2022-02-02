// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;

import java.io.Serializable;
import java.util.Date;

/**
 * Representation of a single Device Provisioning Service device registration state with a JSON deserializer.
 *
 * <p> Example of JSON format:
 * <pre>
 * {@code
 * {
 *     "registrationId":"validRegistrationId",
 *     "createdDateTimeUtc": "2017-09-28T16:29:42.3447817Z",
 *     "assignedHub":"ContosoIoTHub.azure-devices.net",
 *     "deviceId":"ContosoDevice-123",
 *     "status":"assigned"
 *     "lastUpdatedDateTimeUtc": "2017-09-28T16:29:42.3447817Z",
 *     "errorCode":200
 *     "errorMessage":"Succeeded"
 *     "etag": "\"00000000-0000-0000-0000-00000000000\""
 * }
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
@SuppressWarnings("unused") // A number of private fields are unused but may be filled in by serialization
public class DeviceRegistrationState implements Serializable
{
    // the registration identifier
    private static final String REGISTRATION_ID_TAG = "registrationId";
    @Expose
    @SerializedName(REGISTRATION_ID_TAG)
    private String registrationId;

    // the device identifier
    private static final String DEVICE_ID_TAG = "deviceId";
    @Expose
    @SerializedName(DEVICE_ID_TAG)
    private String deviceId;

    // the created date and time
    private static final String CREATED_DATETIME_UTC_TAG = "createdDateTimeUtc";
    @Expose
    @SerializedName(CREATED_DATETIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String createdDateTimeUtc;
    private transient Date createdDateTimeUtcDate;

    // last update date and time
    private static final String LAST_UPDATED_DATETIME_UTC_TAG = "lastUpdatedDateTimeUtc";
    @Expose
    @SerializedName(LAST_UPDATED_DATETIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String lastUpdatedDateTimeUtc;
    private transient Date lastUpdatedDateTimeUtcDate;

    // assigned hub
    private static final String ASSIGNED_HUB_TAG = "assignedHub";
    @Expose
    @SerializedName(ASSIGNED_HUB_TAG)
    private String assignedHub;

    // registration status
    private static final String STATE_TAG = "status";
    private static final String QUOTED_STATE_TAG = "\"" + STATE_TAG + "\"";
    @Expose
    @SerializedName(STATE_TAG)
    private EnrollmentStatus status;

    /// substatus for 'assigned' devices
    private static final String SUBSTATUS_TAG = "substatus";
    @Expose
    @SerializedName(SUBSTATUS_TAG)
    private ProvisioningServiceClientSubstatus substatus;

    // error code
    private static final String ERROR_CODE_TAG = "errorCode";
    @Expose
    @SerializedName(ERROR_CODE_TAG)
    private Integer errorCode;

    // error message
    private static final String ERROR_MESSAGE_TAG = "errorMessage";
    @Expose
    @SerializedName(ERROR_MESSAGE_TAG)
    private String errorMessage;

    // the eTag
    private static final String ETAG_TAG = "etag";
    @Expose
    @SerializedName(ETAG_TAG)
    private String etag;

    /**
     * CONSTRUCTOR
     *
     * <p> This constructor creates an instance of the device registration
     *     state filling the class with the information provided in the JSON.
     *
     * @param json the {@code String} with the JSON received from the provisioning service.
     * @throws IllegalArgumentException If the provided JSON is null, empty, or invalid.
     */
    public DeviceRegistrationState(String json)
    {
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        DeviceRegistrationState result = gson.fromJson(json, DeviceRegistrationState.class);

        this.registrationId = result.registrationId;

        if (result.deviceId != null)
        {
            this.deviceId = result.deviceId;
        }

        if (result.createdDateTimeUtc != null)
        {
            this.createdDateTimeUtcDate = ParserUtility.getDateTimeUtc(result.createdDateTimeUtc);
        }

        if (result.lastUpdatedDateTimeUtc != null)
        {
            this.lastUpdatedDateTimeUtcDate = ParserUtility.getDateTimeUtc(result.lastUpdatedDateTimeUtc);
        }

        if (result.assignedHub != null)
        {
            this.assignedHub = result.assignedHub;
        }

        if (result.status == null)
        {
            if (json.contains(QUOTED_STATE_TAG))
            {
                throw new IllegalArgumentException("status is nor valid");
            }
        }
        else
        {
            this.status = result.status;
        }

        if (result.errorCode != null)
        {
            this.errorCode = result.errorCode;
        }

        if (result.errorMessage != null)
        {
            this.errorMessage = result.errorMessage;
        }

        if (result.etag != null)
        {
            ParserUtility.validateStringUTF8(result.etag);
            this.etag = result.etag;
        }
    }

    /**
     * Getter for the registrationId.
     *
     * @return The {@code String} with the registrationID content. It cannot be {@code null} or empty.
     */
    public String getRegistrationId()
    {
        return this.registrationId;
    }

    /**
     * Getter for the deviceId.
     *
     * @return The {@code String} with the deviceID content. It can be {@code null}.
     */
    public String getDeviceId()
    {
        return this.deviceId;
    }

    /**
     * Getter for the createdDateTimeUtc.
     *
     * @return The {@code Date} with the createdDateTimeUtc content. It can be {@code null}.
     */
    public Date getCreatedDateTimeUtc()
    {
        return this.createdDateTimeUtcDate;
    }

    /**
     * Getter for the lastUpdatedDateTimeUtc.
     *
     * @return The {@code Date} with the lastUpdatedDateTimeUtc content. It can be {@code null}.
     */
    public Date getLastUpdatedDateTimeUtc()
    {
        return this.lastUpdatedDateTimeUtcDate;
    }

    /**
     * Getter for the assignedHub.
     *
     * @return The {@code String} with the assignedHub content. It can be {@code null}.
     */
    public String getAssignedHub()
    {
        return this.assignedHub;
    }

    /**
     * Getter for the status.
     *
     * @return The {@code EnrollmentStatus} with the status content. It can be {@code null}.
     */
    public EnrollmentStatus getStatus()
    {
        return this.status;
    }

    /**
     * Getter for the errorCode.
     *
     * @return The {@code Integer} with the errorCode content. It can be {@code null}.
     */
    public Integer getErrorCode()
    {
        return this.errorCode;
    }

    /**
     * Getter for the errorMessage.
     *
     * @return The {@code String} with the errorMessage content. It can be {@code null}.
     */
    public String getErrorMessage()
    {
        return this.errorMessage;
    }

    /**
     * Getter for the etag.
     *
     * @return The {@code String} with the etag content. It can be {@code null}.
     */
    public String getEtag()
    {
        return this.etag;
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    DeviceRegistrationState()
    {
        // Empty constructor for gson to use when deserializing
    }
}
