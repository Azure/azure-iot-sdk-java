package com.microsoft.azure.sdk.iot.device;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.device.twin.ParserUtility;
import lombok.Getter;

import java.util.Date;

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

    @Getter
    private transient IotHubCertificateSigningErrorCode errorCode;

    @SerializedName("message")
    @Getter
    private String message;

    @SerializedName("trackingId")
    @Getter
    private String trackingId;

    @SerializedName("timestampUtc")
    private String timestampUtcString;

    @Getter
    private transient Date timestampUtc;

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
