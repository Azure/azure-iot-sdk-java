// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.certificatesigning;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class IotHubCertificateSigningRequest
{
    /**
     * Required. The device ID the certificate will be issued for.
     * Must match the currently authenticated device ID.
     */
    @SerializedName("id")
    private String id = null;

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
     * Create a certificate signing request that will fail if any certificate signing requests for this device are already in progress.
     *
     * @param id The device ID the certificate will be issued for. Must match the device Id of the device that will send this request.
     * @param certificateSigningRequest The Base64-encoded PKCS#10 CSR without PEM headers/footers or newlines.
     */
    public IotHubCertificateSigningRequest(String id, String certificateSigningRequest)
    {
        this(id, certificateSigningRequest, null);
    }

    /**
     * Create a certificate signing request that will be accepted by IoT hub depending on the provided "replace" value and depending on
     * if any certificate signing requests for this device are already in progress.
     *
     * @param id The device ID the certificate will be issued for. Must match the device Id of the device that will send this request.
     * @param certificateSigningRequest The Base64-encoded PKCS#10 CSR without PEM headers/footers or newlines.
     * @param replace the request ID to replace, or "*" to replace any active request. For use if a
     * previous certificate signing request has failed and you want to start over.
     */
    public IotHubCertificateSigningRequest(String id, String certificateSigningRequest, String replace)
    {
        if (id == null || id.isEmpty())
        {
            throw new IllegalArgumentException("Id must be non-null and not empty");
        }

        if (certificateSigningRequest == null || certificateSigningRequest.isEmpty())
        {
            throw new IllegalArgumentException("certificateSigningRequestData must be non-null and not empty");
        }

        this.id = id;
        this.certificateSigningRequest = certificateSigningRequest;
        this.replace = replace;
    }

    public String toJson()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

        return gson.toJson(this);
    }

    @SuppressWarnings("unused") // used by gson
    IotHubCertificateSigningRequest()
    {
    }
}
