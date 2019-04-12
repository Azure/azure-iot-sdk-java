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

public class DeviceRegistrationParser
{
    private static final String REGISTRATION_ID = "registrationId";
    @SerializedName(REGISTRATION_ID)
    private String registrationId;

    private static final String TPM = "tpm";
    @SerializedName(TPM)
    private TpmAttestation tpmAttestation;

    private static final String CUSTOM_PAYLOAD = "payload";
    @SerializedName(CUSTOM_PAYLOAD)
    private String customPayload = null;

    /**
     * Inner class describing TPM Attestation i.e it holds EndorsementKey and StorageRootKey
     */
    class TpmAttestation
    {
        private String endorsementKey;
        private String storageRootKey;

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
     * @throws IllegalArgumentException If the provided registration id was {@code null} or empty.
     */
    public DeviceRegistrationParser(String registrationId, String customPayload) throws IllegalArgumentException
    {
        //SRS_DeviceRegistration_25_001: [ The constructor shall throw IllegalArgumentException if Registration Id is null or empty. ]
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new IllegalArgumentException("Registration Id cannot be null or empty");
        }

        //SRS_DeviceRegistration_25_002: [ The constructor shall save the provided Registration Id. ]
        this.registrationId = registrationId;
        if (!customPayload.isEmpty())
        {
            this.customPayload = customPayload;
        }
    }

    /**
     * Constructor for Device Registration for TPM flow
     * @param registrationId Registration Id to be sent to the service. Cannot be a {@code null} or empty value.
     * @param endorsementKey endorsement key to be sent to the service. Cannot be a {@code null} or empty value.
     * @param storageRootKey Storage Root Key to be sent to the service. Can be a {@code null} value.
     * @throws IllegalArgumentException is thrown if any of the input parameters are invalid.
     */
    public DeviceRegistrationParser(String registrationId, String customPayload, String endorsementKey, String storageRootKey) throws IllegalArgumentException
    {
        //SRS_DeviceRegistration_25_003: [ The constructor shall throw IllegalArgumentException if Registration Id is null or empty. ]
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new IllegalArgumentException("Registration Id cannot be null or empty");
        }

        //SRS_DeviceRegistration_25_004: [ The constructor shall throw IllegalArgumentException if EndorsementKey is null or empty. ]
        if (endorsementKey == null || endorsementKey.isEmpty())
        {
            throw new IllegalArgumentException("endorsementKey cannot be null or empty");
        }

        //SRS_DeviceRegistration_25_006: [ The constructor shall save the provided Registration Id, EndorsementKey and StorageRootKey. ]
        this.registrationId = registrationId;
        if (!customPayload.isEmpty())
        {
            this.customPayload = customPayload;
        }
        this.tpmAttestation = new TpmAttestation(endorsementKey, storageRootKey);
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
        //SRS_DeviceRegistration_25_007: [ This method shall create the expected Json with the provided Registration Id, EndorsementKey and StorageRootKey. ]
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
}
