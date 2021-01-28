// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.deps.transport.amqp;

/**
 * An enumeration of supported authorization methods for service client connections.
 */
public enum TokenCredentialType
{
    /**
     * Authorize with CBS through a shared access signature.
     */
    SHARED_ACCESS_SIGNATURE("azure-devices.net:sastoken");

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
