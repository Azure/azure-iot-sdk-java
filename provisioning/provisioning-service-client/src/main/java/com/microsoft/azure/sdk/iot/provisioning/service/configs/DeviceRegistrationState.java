// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;

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
public class DeviceRegistrationState
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
    private final String createdDateTimeUtc = null;
    private transient Date createdDateTimeUtcDate;

    // last update date and time
    private static final String LAST_UPDATED_DATETIME_UTC_TAG = "lastUpdatedDateTimeUtc";
    @Expose
    @SerializedName(LAST_UPDATED_DATETIME_UTC_TAG)
    private final String lastUpdatedDateTimeUtc = null;
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
        /* SRS_DEVICE_REGISTRATION_STATE_21_001: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
        if(Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        /* SRS_DEVICE_REGISTRATION_STATE_21_002: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
        /* SRS_DEVICE_REGISTRATION_STATE_21_003: [The constructor shall deserialize the provided JSON for the DeviceRegistrationState class.] */
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        DeviceRegistrationState result = gson.fromJson(json, DeviceRegistrationState.class);

        /* SRS_DEVICE_REGISTRATION_STATE_21_005: [The constructor shall store the provided registrationId.] */
        this.registrationId = result.registrationId;

        /* SRS_DEVICE_REGISTRATION_STATE_21_006: [The constructor shall throw IllegalArgumentException if the provided deviceId is empty, or invalid.] */
        if(result.deviceId != null)
        {
            /* SRS_DEVICE_REGISTRATION_STATE_21_007: [The constructor shall store the provided deviceId.] */
            this.deviceId = result.deviceId;
        }

        /* SRS_DEVICE_REGISTRATION_STATE_21_008: [If the createdDateTimeUtc is provided, the constructor shall parse it as date and time UTC.] */
        if(result.createdDateTimeUtc != null)
        {
            /* SRS_DEVICE_REGISTRATION_STATE_21_031: [Te constructor shall throw IllegalArgumentException if the createdDateTimeUtc is empty or invalid.] */
            this.createdDateTimeUtcDate = ParserUtility.getDateTimeUtc(result.createdDateTimeUtc);
        }

        /* SRS_DEVICE_REGISTRATION_STATE_21_009: [If the lastUpdatedDateTimeUtc is provided, the constructor shall parse it as date and time UTC.] */
        if(result.lastUpdatedDateTimeUtc != null)
        {
            /* SRS_DEVICE_REGISTRATION_STATE_21_032: [Te constructor shall throw IllegalArgumentException if the lastUpdatedDateTimeUtc is empty or invalid.] */
            this.lastUpdatedDateTimeUtcDate = ParserUtility.getDateTimeUtc(result.lastUpdatedDateTimeUtc);
        }

        /* SRS_DEVICE_REGISTRATION_STATE_21_010: [If the assignedHub is not null, the constructor shall judge and store it.] */
        if(result.assignedHub != null)
        {
            this.assignedHub = result.assignedHub;
        }

        /* SRS_DEVICE_REGISTRATION_STATE_21_014: [The constructor shall throw IllegalArgumentException if the provided status is invalid.] */
        if(result.status == null)
        {
            if(json.contains(QUOTED_STATE_TAG))
            {
                throw new IllegalArgumentException("status is nor valid");
            }
        }
        else
        {
            /* SRS_DEVICE_REGISTRATION_STATE_21_015: [If the errorCode is not null, the constructor shall store the provided status.] */
            this.status = result.status;
        }

        /* SRS_DEVICE_REGISTRATION_STATE_21_016: [If the errorCode is not null, the constructor shall store it.] */
        if(result.errorCode != null)
        {
            this.errorCode = result.errorCode;
        }

        /* SRS_DEVICE_REGISTRATION_STATE_21_017: [If the errorMessage is not null, the constructor shall store it.] */
        if(result.errorMessage != null)
        {
            this.errorMessage = result.errorMessage;
        }

        /* SRS_DEVICE_REGISTRATION_STATE_21_018: [If the etag is not null, the constructor shall judge and store it.] */
        if(result.etag != null)
        {
            /* SRS_DEVICE_REGISTRATION_STATE_21_019: [The constructor shall throw IllegalArgumentException if an provided etag is empty or invalid.] */
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
        /* SRS_DEVICE_REGISTRATION_STATE_21_020: [The getRegistrationId shall return a String with the stored registrationId.] */
        return this.registrationId;
    }

    /**
     * Getter for the deviceId.
     *
     * @return The {@code String} with the deviceID content. It can be {@code null}.
     */
    public String getDeviceId()
    {
        /* SRS_DEVICE_REGISTRATION_STATE_21_021: [The getDeviceId shall return a String with the stored deviceId.] */
        return this.deviceId;
    }

    /**
     * Getter for the createdDateTimeUtc.
     *
     * @return The {@code Date} with the createdDateTimeUtc content. It can be {@code null}.
     */
    public Date getCreatedDateTimeUtc()
    {
        /* SRS_DEVICE_REGISTRATION_STATE_21_022: [The getCreatedDateTimeUtc shall return a Date with the stored createdDateTimeUtc.] */
        return this.createdDateTimeUtcDate;
    }

    /**
     * Getter for the lastUpdatedDateTimeUtc.
     *
     * @return The {@code Date} with the lastUpdatedDateTimeUtc content. It can be {@code null}.
     */
    public Date getLastUpdatedDateTimeUtc()
    {
        /* SRS_DEVICE_REGISTRATION_STATE_21_023: [The getLastUpdatedDateTimeUtc shall return a Date with the stored lastUpdatedDateTimeUtc.] */
        return this.lastUpdatedDateTimeUtcDate;
    }

    /**
     * Getter for the assignedHub.
     *
     * @return The {@code String} with the assignedHub content. It can be {@code null}.
     */
    public String getAssignedHub()
    {
        /* SRS_DEVICE_REGISTRATION_STATE_21_024: [The getAssignedHub shall return a String with the stored assignedHub.] */
        return this.assignedHub;
    }

    /**
     * Getter for the status.
     *
     * @return The {@code EnrollmentStatus} with the status content. It can be {@code null}.
     */
    public EnrollmentStatus getStatus()
    {
        /* SRS_DEVICE_REGISTRATION_STATE_21_026: [The getStatus shall return an EnrollmentStatus with the stored status.] */
        return this.status;
    }

    /**
     * Getter for the errorCode.
     *
     * @return The {@code Integer} with the errorCode content. It can be {@code null}.
     */
    public Integer getErrorCode()
    {
        /* SRS_DEVICE_REGISTRATION_STATE_21_027: [The getErrorCode shall return a Integer with the stored errorCode.] */
        return this.errorCode;
    }

    /**
     * Getter for the errorMessage.
     *
     * @return The {@code String} with the errorMessage content. It can be {@code null}.
     */
    public String getErrorMessage()
    {
        /* SRS_DEVICE_REGISTRATION_STATE_21_028: [The getErrorMessage shall return a String with the stored errorMessage.] */
        return this.errorMessage;
    }

    /**
     * Getter for the etag.
     *
     * @return The {@code String} with the etag content. It can be {@code null}.
     */
    public String getEtag()
    {
        /* SRS_DEVICE_REGISTRATION_STATE_21_029: [The getEtag shall return a String with the stored etag.] */
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
        /* SRS_DEVICE_REGISTRATION_STATE_21_030: [The DeviceRegistrationState shall provide an empty constructor to make GSON happy.] */
    }
}
