/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import lombok.Getter;

/**
 * Authentication mechanism, used to store the device symmetric key.
 */
public class AuthenticationMechanism
{
    @SerializedName("symmetricKey")
    @Getter
    private SymmetricKey SymmetricKey;

    //empty constructor for Gson
    AuthenticationMechanism()
    {
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
