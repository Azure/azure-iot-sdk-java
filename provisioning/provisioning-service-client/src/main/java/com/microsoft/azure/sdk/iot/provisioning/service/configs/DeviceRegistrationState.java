// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;
import lombok.Getter;

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
public class DeviceRegistrationState
{
    // the registration identifier
    private static final String REGISTRATION_ID_TAG = "registrationId";
    @Expose
    @SerializedName(REGISTRATION_ID_TAG)
    @Getter
    private String registrationId;

    // the device identifier
    private static final String DEVICE_ID_TAG = "deviceId";
    @Expose
    @SerializedName(DEVICE_ID_TAG)
    @Getter
    private String deviceId;

    // the created date and time
    private static final String CREATED_DATETIME_UTC_TAG = "createdDateTimeUtc";
    @Expose
    @SerializedName(CREATED_DATETIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String createdDateTimeUtcString;
    @Getter
    private transient Date createdDateTimeUtc;

    // last update date and time
    private static final String LAST_UPDATED_DATETIME_UTC_TAG = "lastUpdatedDateTimeUtc";
    @Expose
    @SerializedName(LAST_UPDATED_DATETIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String lastUpdatedDateTimeUtcString;
    @Getter
    private transient Date lastUpdatedDateTimeUtc;

    // assigned hub
    private static final String ASSIGNED_HUB_TAG = "assignedHub";
    @Expose
    @SerializedName(ASSIGNED_HUB_TAG)
    @Getter
    private String assignedHub;

    // registration status
    private static final String STATE_TAG = "status";
    private static final String QUOTED_STATE_TAG = "\"" + STATE_TAG + "\"";
    @Expose
    @SerializedName(STATE_TAG)
    @Getter
    private EnrollmentStatus status;

    // error code
    private static final String ERROR_CODE_TAG = "errorCode";
    @Expose
    @SerializedName(ERROR_CODE_TAG)
    @Getter
    private Integer errorCode;

    // error message
    private static final String ERROR_MESSAGE_TAG = "errorMessage";
    @Expose
    @SerializedName(ERROR_MESSAGE_TAG)
    @Getter
    private String errorMessage;

    // the eTag
    private static final String ETAG_TAG = "etag";
    @Expose
    @SerializedName(ETAG_TAG)
    @Getter
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

        if (result.createdDateTimeUtcString != null)
        {
            this.createdDateTimeUtc = ParserUtility.getDateTimeUtc(result.createdDateTimeUtcString);
        }

        if (result.lastUpdatedDateTimeUtcString != null)
        {
            this.lastUpdatedDateTimeUtc = ParserUtility.getDateTimeUtc(result.lastUpdatedDateTimeUtcString);
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
