// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.deps.transport.amqp;

/**
 * An enumeration of supported authorization methods for AMQP connections that use Claims Based Security links.
 */
public enum CbsAuthorizationType
{
    /**
     * Authorize with CBS through a shared access signature.
     */
    SHARED_ACCESS_SIGNATURE("azure-devices.net:sastoken");

    private final String scheme;

    CbsAuthorizationType(String scheme) {
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
