// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Setter;

/**
 * The request payload to send to IoT Hub to notify it when a file upload is completed, whether successful or not.
 * Must set whether the file upload was a success or not, and must set the correlation Id, but all other fields are optional.
 */
public class CertificateSigningRequest
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
    private String certificateSigningRequestData = null;

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
     *
     * @param id The device ID the certificate will be issued for. Must match the device Id of the device that will send this request.
     * @param certificateSigningRequestData The Base64-encoded PKCS#10 CSR without PEM headers/footers or newlines.
     */
    public CertificateSigningRequest(String id, String certificateSigningRequestData)
    {
        this(id, certificateSigningRequestData, null);
    }

    /**
     *
     * @param id The device ID the certificate will be issued for. Must match the device Id of the device that will send this request.
     * @param certificateSigningRequestData The Base64-encoded PKCS#10 CSR without PEM headers/footers or newlines.
     * @param replace the request ID to replace, or "*" to replace any active request. For use if a
     * previous certificate signing request has failed and you want to start over.
     */
    public CertificateSigningRequest(String id, String certificateSigningRequestData, String replace)
    {
        if (id == null || id.isEmpty())
        {
            throw new IllegalArgumentException("Id must be non-null and not empty");
        }

        if (certificateSigningRequestData == null || certificateSigningRequestData.isEmpty())
        {
            throw new IllegalArgumentException("certificateSigningRequestData must be non-null and not empty");
        }

        this.id = id;
        this.certificateSigningRequestData = certificateSigningRequestData;
    }

    String toJson()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

        return gson.toJson(this);
    }

    @SuppressWarnings("unused") // used by gson
    CertificateSigningRequest()
    {
    }
}
