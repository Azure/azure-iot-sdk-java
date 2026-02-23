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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The information provided from IoT Hub that can be used with the Azure Storage SDK to upload a file from your device, including authentication.
 */
public class CertificateSigningResponse
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
    private List<String> correlationId;

    public CertificateSigningResponse(String json) throws IllegalArgumentException
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        CertificateSigningResponse deserialized;

        ParserUtility.validateStringUTF8(json);
        try
        {
            deserialized = gson.fromJson(json, CertificateSigningResponse.class);
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json", malformed);
        }

        this.correlationId = deserialized.correlationId;
        this.certificates = deserialized.certificates;
    }

    @SuppressWarnings("unused") // used by gson
    CertificateSigningResponse()
    {
    }
}
