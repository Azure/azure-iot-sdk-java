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

import java.util.List;

/**
 * Class that represents the REST API format for DeviceRegistrationResult
 * Format : https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_deviceregistrationresult
 */
@SuppressWarnings("unused") // A number of private fields are unused but may be filled in by serialization
public class DeviceRegistrationResultParser
{
    @SerializedName("registrationId")
    @Getter
    private String registrationId;

    @SerializedName("createdDateTimeUtc")
    @Getter
    private String createdDateTimeUtc;

    @SerializedName("assignedHub")
    @Getter
    private String assignedHub;

    @SerializedName("deviceId")
    @Getter
    private String deviceId;

    @SerializedName("status")
    @Getter
    private String status;

    @SerializedName("substatus")
    @Getter
    private String substatus;

    @SerializedName("etag")
    @Getter
    private String eTag;

    @SerializedName("lastUpdatedDateTimeUtc")
    @Getter
    private String lastUpdatesDateTimeUtc;

    @SerializedName("errorCode")
    @Getter
    private Integer errorCode;

    @SerializedName("errorMessage")
    @Getter
    private String errorMessage;

    @SerializedName("tpm")
    @Getter
    private TpmRegistrationResultParser tpm;

    @SerializedName("x509")
    @Getter
    private X509RegistrationResultParser x509;

    @SerializedName("payload")
    @Getter
    private JsonObject jsonPayload;

    @SerializedName("issuedCertificateChain")
    @Getter
    private List<String> issuedCertificateChain;

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
