package com.microsoft.azure.sdk.iot.device.certificatesigning;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.device.twin.ParserUtility;
import lombok.Getter;

import java.util.Date;

/**
 * <p>
 * Additional context for why a certificate signing operation failed.
 * </p>
 * <p>
 * Depending on the type of error, some fields will be present and others will not. For example, if the error was that
 * certificate signing failed because a certificate signing operation was already in progress, {@link #operationExpires}
 * and {@link #requestId} will be present.
 * </p>
 */
public class IotHubCertificateSigningErrorInfo
{

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

    /**
     * The correlation Id associated with this certificate signing request. For diagnostic purposes only.
     */
    @SerializedName("correlationId")
    @Getter
    private String correlationId;

    /**
     * The credential error code
     */
    @SerializedName("credentialError")
    @Getter
    private String credentialError;

    /**
     * The human readable credential error message
     */
    @SerializedName("credentialMessage")
    @Getter
    private String credentialMessage;

    /**
     * The request Id associated with this certificate signing request failure
     */
    @SerializedName("requestId")
    private String requestId;

    /**
     * Only present if this error details a "certificate signing operation already in progress" error. This value is
     * when the already-in-progress certificate signing operation for this device will expire.
     */
    @SerializedName("operationExpires")
    private String operationExpiresString;

    @Getter
    private transient Date operationExpires;

    public IotHubCertificateSigningErrorInfo(String json) throws IllegalArgumentException
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        IotHubCertificateSigningErrorInfo deserialized;

        ParserUtility.validateStringUTF8(json);
        try
        {
            deserialized = gson.fromJson(json, IotHubCertificateSigningErrorInfo.class);
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
    IotHubCertificateSigningErrorInfo()
    {
    }
}
