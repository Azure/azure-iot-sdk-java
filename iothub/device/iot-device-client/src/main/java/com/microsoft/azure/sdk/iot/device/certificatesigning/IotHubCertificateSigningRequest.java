// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.certificatesigning;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.UUID;

public class IotHubCertificateSigningRequest
{
    /**
     * Required. The device ID the certificate will be issued for.
     * Must match the currently authenticated device ID.
     */
    @SerializedName("id")
    private String deviceId = null;

    /**
     * The Base64-encoded PKCS#10 CSR without PEM headers/footers or newlines.
     */
    @SerializedName("csr")
    private String certificateSigningRequest = null;

    /**
     * Optional. Request ID to replace, or "*" to replace any active request.
     * Use when:
     *  - The CSR is known to be different from a previous incomplete request
     *  - Client received 409005 and doesn't know if CSR has changed (e.g., storage failure)
     * Default: null (will fail with 409005 if an active operation exists)
     */
    @SerializedName("replace")
    private String replace = null;

    /**
     * The randomly generated request Id associated with this certificate signing request. Users may assign this value via
     * {@link #IotHubCertificateSigningRequest(String, String, String, String)}.
     */
    @Getter
    private final transient String requestId;

    /**
     * Create a certificate signing request that will fail if any certificate signing requests for this device are already in progress.
     *
     * @param deviceId The device ID the certificate will be issued for. Must match the device Id of the device that will send this request.
     * @param certificateSigningRequest The Base64-encoded PKCS#10 CSR without PEM headers/footers or newlines.
     */
    public IotHubCertificateSigningRequest(String deviceId, String certificateSigningRequest)
    {
        this(deviceId, certificateSigningRequest, null);
    }

    /**
     * Create a certificate signing request that will be accepted by IoT hub depending on the provided "replace" value and depending on
     * if any certificate signing requests for this device are already in progress.
     *
     * @param deviceId The device ID the certificate will be issued for. Must match the device Id of the device that will send this request.
     * @param certificateSigningRequest The Base64-encoded PKCS#10 CSR without PEM headers/footers or newlines.
     * @param replace the request ID to replace, or "*" to replace any active request. For use if a
     * previous certificate signing request has failed and you want to start over. To not replace any pending certificate
     * signing operation, this value should be null. The request Id of a previous certificate signing request can be retrieved
     * with {@link #getRequestId()}.
     */
    public IotHubCertificateSigningRequest(String deviceId, String certificateSigningRequest, String replace)
    {
        this(deviceId, certificateSigningRequest, replace, null);
    }

    /**
     * Create a certificate signing request that will be accepted by IoT hub depending on the provided "replace" value and depending on
     * if any certificate signing requests for this device are already in progress.
     *
     * @param deviceId The device ID the certificate will be issued for. Must match the device Id of the device that will send this request.
     * @param certificateSigningRequest The Base64-encoded PKCS#10 CSR without PEM headers/footers or newlines.
     * @param replace the request ID to replace, or "*" to replace any active request. For use if a
     * previous certificate signing request has failed and you want to start over. To not replace any pending certificate
     * signing operation, this value should be null.
     * @param requestId The request Id to associate with this certifiate signing request. This value should be unique from
     * any ongoing certificate signing request (for example, a UUID). If null or empty, a random value will be provided for you.
     * The use case for providing a specific value here is for re-submitting a certificate signing request which should
     * be done if the client loses connection at any point during the certificate signing request process.
     */
    public IotHubCertificateSigningRequest(String deviceId, String certificateSigningRequest, String replace, String requestId)
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("Id must be non-null and not empty");
        }

        if (certificateSigningRequest == null || certificateSigningRequest.isEmpty())
        {
            throw new IllegalArgumentException("certificateSigningRequestData must be non-null and not empty");
        }

        this.deviceId = deviceId;
        this.certificateSigningRequest = certificateSigningRequest;
        this.replace = replace;

        if (requestId == null || requestId.isEmpty())
        {
            this.requestId = UUID.randomUUID().toString();
        }
        else
        {
            this.requestId = requestId;
        }
    }

    public String toJson()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

        return gson.toJson(this);
    }

    @SuppressWarnings("unused") // used by gson
    IotHubCertificateSigningRequest()
    {
        this.requestId = UUID.randomUUID().toString();
    }
}
