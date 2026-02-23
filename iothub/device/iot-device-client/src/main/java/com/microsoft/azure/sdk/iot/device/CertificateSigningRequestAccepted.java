// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.device.twin.ParserUtility;
import lombok.Getter;

import java.util.Date;
import java.util.List;

/**
 * The information provided from IoT Hub that can be used with the Azure Storage SDK to upload a file from your device, including authentication.
 */
public class CertificateSigningRequestAccepted
{
    @SerializedName("correlationId")
    @Getter
    private String correlationId;

    @SerializedName("operationExpires")
    private String operationExpiresString;

    @Getter
    private transient Date operationExpires;

    public CertificateSigningRequestAccepted(String json) throws IllegalArgumentException
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        CertificateSigningRequestAccepted deserialized;

        ParserUtility.validateStringUTF8(json);
        try
        {
            deserialized = gson.fromJson(json, CertificateSigningRequestAccepted.class);
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json", malformed);
        }

        this.operationExpires = ParserUtility.getDateTimeUtc(deserialized.operationExpiresString);
        this.correlationId = deserialized.correlationId;
    }

    @SuppressWarnings("unused") // used by gson
    CertificateSigningRequestAccepted()
    {
    }
}
