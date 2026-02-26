/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

// FieldCanBeLocal suppression in this class is addressing warnings of fields used for serialization and in constructor.
@SuppressWarnings("unused") // A number of private fields are unused but may be filled in by serialization
public class DeviceRegistrationParser
{
    @SerializedName("registrationId")
    private String registrationId;

    @SuppressWarnings("FieldCanBeLocal")
    @SerializedName("tpm")
    private TpmAttestation tpmAttestation;

    @SerializedName("payload")
    private String customPayload = null;

    @SerializedName("csr")
    private String certificateSigningRequest = null;

    /**
     * Inner class describing TPM Attestation i.e it holds EndorsementKey and StorageRootKey
     */
    static class TpmAttestation
    {
        @SuppressWarnings("FieldCanBeLocal")
        private final String endorsementKey;
        @SuppressWarnings("FieldCanBeLocal")
        private final String storageRootKey;

        TpmAttestation(String endorsementKey, String storageRootKey)
        {
            this.endorsementKey = endorsementKey;
            this.storageRootKey = storageRootKey;
        }
    }

    //empty constructor for Gson
    DeviceRegistrationParser()
    {
    }

    /**
     * Constructor for Device Registration for X509 flow
     * @param registrationId Registration Id to be sent to the service. Cannot be a {@code null} or empty value.
     * @param customPayload Custom Payload being sent to the DPS service. Can be a {@code null} or empty value.
     * @throws IllegalArgumentException If the provided registration id was {@code null} or empty.
     */
    public DeviceRegistrationParser(String registrationId, String customPayload, String certificateSigningRequest) throws IllegalArgumentException
    {
        this(registrationId, customPayload, certificateSigningRequest, null, null);
    }

    /**
     * Constructor for Device Registration for TPM flow
     * @param registrationId Registration Id to be sent to the service. Cannot be a {@code null} or empty value.
     * @param customPayload Custom Payload being sent to the DPS service. Can be a {@code null} or empty value.
     * @param certificateSigningRequest The certificate signing request to be sent. Can be a {@code null} or empty value.
     * @param endorsementKey endorsement key to be sent to the service. Cannot be a {@code null} or empty value.
     * @param storageRootKey Storage Root Key to be sent to the service. Can be a {@code null} value.
     * @throws IllegalArgumentException is thrown if any of the input parameters are invalid.
     */
    public DeviceRegistrationParser(String registrationId, String customPayload, String certificateSigningRequest, String endorsementKey, String storageRootKey) throws IllegalArgumentException
    {
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new IllegalArgumentException("Registration Id cannot be null or empty");
        }

        this.registrationId = registrationId;
        if (customPayload != null && !customPayload.isEmpty())
        {
            this.customPayload = customPayload;
        }

        this.certificateSigningRequest = certificateSigningRequest;

        if (endorsementKey != null && !endorsementKey.isEmpty())
        {
            this.tpmAttestation = new TpmAttestation(endorsementKey, storageRootKey);
        }
    }

    /**
     * Generates JSON output for this class.
     * Expected format :
     * For TPM :
         * <pre>
     *     {@code
     *     "{\"registrationId\":\"[RegistrationID value]\"," +
            "\"tpm\":{" +
            "\"endorsementKey\":\"[Endorsement Key value]\"," +
            "\"storageRootKey\":\"[Storage root key value]\"" +
            "}
            "\"payload\":\"[Custom Data]\""
            }"
     *     }
         * </pre>
     * For X509:
     * <pre>
     *     {@code
     *     "{\"registrationId\":\"[RegistrationID value]\"," +
            }"
     *     }
     * </pre>
     * @return A string that is JSON formatted.
     */
    public String toJson()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
}
