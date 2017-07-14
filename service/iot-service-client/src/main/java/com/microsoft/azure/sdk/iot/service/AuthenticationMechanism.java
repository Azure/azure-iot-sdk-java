/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;

/**
 * Authentication mechanism, used to store the device symmetric key.
 */
public class AuthenticationMechanism
{
    @SerializedName("symmetricKey")
    private SymmetricKey SymmetricKey;

    /**
     * Getter for symmetric key.
     * @return The symmetric key.
     */
    public SymmetricKey getSymmetricKey()
    {
        return this.SymmetricKey;
    }

    /**
     * Constructor for initialization.
     * @param symmetricKey symmetricKey used for Auth
     */
    public AuthenticationMechanism(SymmetricKey symmetricKey)
    {
        this.SymmetricKey = symmetricKey;
    }
}
