// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.certificatesigning;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.device.twin.ParserUtility;
import lombok.Getter;

import java.util.List;

/**
 * The information provided from IoT Hub that can be used with the Azure Storage SDK to upload a file from your device, including authentication.
 */
public class IotHubCertificateSigningResponse
{
    /**
     * <p>
     * List of Base64-encoded certificates in the certificate chain.
     * </p>
     * <p>
     * The first certificate is the issued device certificate, followed by intermediates.
     * </p>
     */
    @SerializedName("certificates")
    @Getter
    private List<String> certificates;

    /**
     * Correlation ID for diagnostic and support purposes.
     */
    @SerializedName("correlationId")
    @Getter
    private String correlationId;

    public IotHubCertificateSigningResponse(String json) throws IllegalArgumentException
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        IotHubCertificateSigningResponse deserialized;

        ParserUtility.validateStringUTF8(json);
        try
        {
            deserialized = gson.fromJson(json, IotHubCertificateSigningResponse.class);
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json", malformed);
        }

        this.correlationId = deserialized.correlationId;
        this.certificates = deserialized.certificates;
    }

    @SuppressWarnings("unused") // used by gson
    IotHubCertificateSigningResponse()
    {
    }
}
