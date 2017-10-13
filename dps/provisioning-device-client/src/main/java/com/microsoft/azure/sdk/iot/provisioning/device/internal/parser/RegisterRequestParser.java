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

public class RegisterRequestParser
{
    /*
    For TPM :
    {"registrationId":"qToVs4wjJWaTfm7CP5WptArEKqNk7LgQCH4s9Ez-P+8=","tpm":{"endorsementKey":"AToAAQALAAMAsgAgg3GXZ0SEs/gakMyNRqXXJP1S124GUgtk8qHaGzMUaaoABgCAAEMAEAgAAAAAAAEAzKJy5Ar/TZrRNU3jAq1knxz/6QkDAD3SIs8yf87D9560p4ikwbwE63RD1mghFnenLUexfAikMOqCZBOQ0Hcn6rVQMqhO8vTCg+0eKkb2KoqP7OwlZoLx5ZTsOav2fpX09PRtnlGV/E2u6Ih9lNDYlYcFyM3zJ7UKWBSkLx9Api3xfCUtzN4rhvbJVepzGxrrBvzR8b4QP4UVUTcO1Ptsr3LnXAw8xcI1c64vsFIdALcLZrEgJhEB9CCG9wSuBnr9SwRF7c+hVYrX2ffn+JGjUyexra7MjDTPDEREMwERmskmjHxXo8kKxbxwClBnMa+B4hFCwMfjtmvBITfe+YnHSw==", "storageRootKey":"ARoAAQALAAMEcgAAAAYAgABDABAIAAAAAAABAOtkhwZsvTyGIda0EjHC88xtr/fRzS21rxDkqNkqILJxzwEnQIQJaEtqezc+dvxDQP7ANLMtEzdrK8AyYz7DQ5wSoypbj8KOdeOltff/bH61SxkcJ2HszJjF9nEFkDRxY6ikUU7pGJ5D/VvtidPJkh53nbRPyirsU69qIQIF6vCPd9TlYSYLMUu1H4qh3ER+vP5h59iMd4qLrvseta4o9gmOxE6oLHTQoojnoiIAKGPGAedqPsAaPJ2bjTHVJDkfmvKc1VqJIgaC8HC3YD/g56EP4Ppe53e45BckumbusvPfFLC/WsZ1lOFEVVXb4nyFpDm/myG+PwhnWCLTMjJ6w10="}}
    For DICE :
    { "registrationId":"RIoT_COMMON_device" }
    */

    private static final String REGISTRATION_ID = "registrationId";
    @SerializedName(REGISTRATION_ID)
    private String registrationId;

    private static final String TPM = "tpm";
    @SerializedName(TPM)
    private TPM tpm;

    class TPM
    {
        private String endorsementKey;
        private String storageRootKey;

        public TPM(String endorsementKey, String storageRootKey)
        {
            this.endorsementKey = endorsementKey;
            this.storageRootKey = storageRootKey;
        }
    }

    public RegisterRequestParser(String registrationId) throws IllegalArgumentException
    {
        if (registrationId == null)
        {
            throw new IllegalArgumentException("Registration Id cannot be null");
        }

        this.registrationId = registrationId;
    }

    public RegisterRequestParser(String registrationId, String endorsementKey, String storageRootKey) throws IllegalArgumentException
    {
        if (registrationId == null)
        {
            throw new IllegalArgumentException("Registration Id cannot be null");
        }

        if (endorsementKey == null || storageRootKey == null)
        {
            throw new IllegalArgumentException(" endorsementKey or storageRootKey cannot be null");
        }

        this.registrationId = registrationId;
        this.tpm = new TPM(endorsementKey, storageRootKey);
    }

    public String toJson()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
}
