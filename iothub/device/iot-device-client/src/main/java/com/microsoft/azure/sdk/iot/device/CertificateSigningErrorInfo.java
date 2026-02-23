package com.microsoft.azure.sdk.iot.device;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.device.twin.ParserUtility;
import lombok.Getter;

import java.util.Date;

public class CertificateSigningErrorInfo {
/* Example:
{
  "correlationId": "8819e8d8-1324-4a9c-acde-ce0318e93f31",
  "credentialError": "FailedToDecodeCsr",
  "credentialMessage": "Failed to decode CSR: invalid base64 encoding"
}

alternatively, in the case of an "operation already in progress" error:
{
    "requestId": "aabbcc",
    "correlationId": "8819e8d8-1324-4a9c-acde-ce0318e93f31",
    "operationExpires": "2025-06-09T17:31:31.426Z"
}
*/
    @SerializedName("correlationId")
    @Getter
    private String correlationId;

    @SerializedName("credentialError")
    @Getter
    private String credentialError; //TODO is this an enum?

    @SerializedName("credentialMessage")
    @Getter
    private String credentialMessage;

    @SerializedName("requestId")
    private String requestId;

    @SerializedName("operationExpires")
    private String operationExpiresString;

    @Getter
    private transient Date operationExpires;

    public CertificateSigningErrorInfo(String json) throws IllegalArgumentException
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        CertificateSigningErrorInfo deserialized;

        ParserUtility.validateStringUTF8(json);
        try
        {
            deserialized = gson.fromJson(json, CertificateSigningErrorInfo.class);
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json", malformed);
        }

        this.correlationId = deserialized.correlationId;
        this.credentialError = deserialized.credentialError;
        this.credentialMessage = deserialized.credentialMessage;
        this.operationExpiresString = deserialized.operationExpiresString;
        this.operationExpires = ParserUtility.getDateTimeUtc(this.operationExpiresString);
        this.requestId = deserialized.requestId;
    }

    @SuppressWarnings("unused") // used by gson
    CertificateSigningErrorInfo()
    {
    }
}
