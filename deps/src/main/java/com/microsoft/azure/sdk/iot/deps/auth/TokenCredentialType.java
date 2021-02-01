// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.deps.auth;

/**
 * The enumeration of all the supported authentication token types that can be used to authenticate an AMQP connection
 */
public enum TokenCredentialType
{
    /**
     * Authorize with CBS through a shared access signature.
     */
    SHARED_ACCESS_SIGNATURE("servicebus.windows.net:sastoken");

    private final String scheme;

    TokenCredentialType(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Gets the token type scheme.
     *
     * @return The token type scheme.
     */
    public String getTokenType() {
        return scheme;
    }
}
