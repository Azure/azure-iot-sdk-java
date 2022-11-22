/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * Class that represents the REST API format for DeviceRegistrationResult
 * Format : https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_deviceregistrationresult
 */
@SuppressWarnings("unused") // A number of private fields are unused but may be filled in by serialization
public class DeviceRegistrationResultParser
{
    private static final String REGISTRATION_ID = "registrationId";
    @SerializedName(REGISTRATION_ID)
    @Getter
    private String registrationId;

    private static final String CREATED_DATE_TIME_UTC = "createdDateTimeUtc";
    @SerializedName(CREATED_DATE_TIME_UTC)
    @Getter
    private String createdDateTimeUtc;

    private static final String ASSIGNED_HUB = "assignedHub";
    @SerializedName(ASSIGNED_HUB)
    @Getter
    private String assignedHub;

    private static final String DEVICE_ID = "deviceId";
    @SerializedName(DEVICE_ID)
    @Getter
    private String deviceId;

    private static final String STATUS = "status";
    @SerializedName(STATUS)
    @Getter
    private String status;

    private static final String SUBSTATUS = "substatus";
    @SerializedName(SUBSTATUS)
    @Getter
    private String substatus;

    private static final String ETAG = "etag";
    @SerializedName(ETAG)
    @Getter
    private String eTag;

    private static final String LAST_UPDATES_DATE_TIME_UTC = "lastUpdatedDateTimeUtc";
    @SerializedName(LAST_UPDATES_DATE_TIME_UTC)
    @Getter
    private String lastUpdatesDateTimeUtc;

    private static final String ERROR_CODE = "errorCode";
    @SerializedName(ERROR_CODE)
    @Getter
    private Integer errorCode;

    private static final String ERROR_MESSAGE = "errorMessage";
    @SerializedName(ERROR_MESSAGE)
    @Getter
    private String errorMessage;

    private static final String TPM = "tpm";
    @SerializedName(TPM)
    @Getter
    private TpmRegistrationResultParser tpm;

    private static final String X509 = "x509";
    @SerializedName(X509)
    @Getter
    private X509RegistrationResultParser x509;

    private static final String CERTIFICATE = "certificate";
    @SerializedName(CERTIFICATE)
    @Getter
    private String certificate;

    private static final String PAYLOAD = "payload";
    @SerializedName(PAYLOAD)
    @Getter
    private JsonObject jsonPayload;

    //empty constructor for Gson
    DeviceRegistrationResultParser()
    {
    }

    public String getPayload()
    {
        if (jsonPayload == null)
        {
            return null;
        }
        else
        {
            return jsonPayload.toString();
        }
    }
}
