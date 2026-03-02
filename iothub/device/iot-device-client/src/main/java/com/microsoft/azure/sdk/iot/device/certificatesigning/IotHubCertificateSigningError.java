package com.microsoft.azure.sdk.iot.device.certificatesigning;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.device.twin.ParserUtility;
import lombok.Getter;

import java.util.Date;

/**
 * The error reported by IoT hub if certificate signing fails.
 */
public class IotHubCertificateSigningError
{
/* Example:
{
  "errorCode": 400040,
  "message": "Credential management operation failed",
  "trackingId": "59b2922c-f1c9-451b-b02d-5b64bc31685a",
  "timestampUtc": "2025-06-09T17:31:31.426574675Z",
  "info": {
      "correlationId": "8819e8d8-1324-4a9c-acde-ce0318e93f31",
      "credentialError": "FailedToDecodeCsr",
      "credentialMessage": "Failed to decode CSR: invalid base64 encoding"
  }
} 
*/
    @SerializedName("errorCode")
    private String errorCodeString;

    /**
     * The error code that explains why the operation failed.
     */
    @Getter
    private transient IotHubCertificateSigningErrorCode errorCode;

    /**
     * The human readable error message
     */
    @SerializedName("message")
    @Getter
    private String message;

    /**
     * The tracking Id associated with this failure. If you request support for this failure, please include this tracking Id.
     */
    @SerializedName("trackingId")
    @Getter
    private String trackingId;

    @SerializedName("timestampUtc")
    private String timestampUtcString;

    /**
     * The UTC time at which this error happened.
     */
    @Getter
    private transient Date timestampUtc;

    /**
     * Further information about this error.
     */
    @SerializedName("info")
    @Getter
    private IotHubCertificateSigningErrorInfo info;

    public IotHubCertificateSigningError(String json) throws IllegalArgumentException
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        IotHubCertificateSigningError deserialized;

        ParserUtility.validateStringUTF8(json);
        try
        {
            deserialized = gson.fromJson(json, IotHubCertificateSigningError.class);
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json", malformed);
        }

        this.errorCodeString = deserialized.errorCodeString;
        this.errorCode = IotHubCertificateSigningErrorCode.GetValue(Integer.parseInt(this.errorCodeString));
        this.trackingId = deserialized.trackingId;
        this.message = deserialized.message;
        this.timestampUtcString = deserialized.timestampUtcString;
        this.timestampUtc = ParserUtility.getDateTimeUtc(this.timestampUtcString);
        this.info = deserialized.getInfo();
    }

    @SuppressWarnings("unused") // used by gson
    IotHubCertificateSigningError()
    {
    }
}
