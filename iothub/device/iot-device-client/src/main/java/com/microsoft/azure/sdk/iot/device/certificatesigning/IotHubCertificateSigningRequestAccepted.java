// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.certificatesigning;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.device.twin.ParserUtility;
import lombok.Getter;

import java.util.Date;

/**
 * The information provided from IoT Hub when it accepts a certificate signing request.
 */
public class IotHubCertificateSigningRequestAccepted
{
    /**
     * The correlation Id for this certificate signing request flow. For diagnostic purposes only.
     */
    @SerializedName("correlationId")
    @Getter
    private String correlationId;

    @SerializedName("operationExpires")
    private String operationExpiresString;

    /**
     * The UTC time at which this accepted certificate signing request will have expired if IoT Hub does not send any further updates.
     */
    @Getter
    private transient Date operationExpires;

    public IotHubCertificateSigningRequestAccepted(String json) throws IllegalArgumentException
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        IotHubCertificateSigningRequestAccepted deserialized;

        ParserUtility.validateStringUTF8(json);
        try
        {
            deserialized = gson.fromJson(json, IotHubCertificateSigningRequestAccepted.class);
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json", malformed);
        }

        this.operationExpires = ParserUtility.getDateTimeUtc(deserialized.operationExpiresString);
        this.correlationId = deserialized.correlationId;
    }

    @SuppressWarnings("unused") // used by gson
    IotHubCertificateSigningRequestAccepted()
    {
    }
}
