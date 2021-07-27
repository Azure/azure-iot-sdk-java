// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.deps.convention.DefaultPayloadConvention;
import com.microsoft.azure.sdk.iot.deps.convention.PayloadConvention;
import lombok.*;

import javax.net.ssl.SSLContext;

/**
 * Options that allow configuration of the device client instance during initialization.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class ClientOptions
{
    /**
     * The Digital Twin Model Id associated with the device and module identity.
     * Non plug and play users should not set this value
     * This feature is currently supported only over MQTT
     */
    @Setter
    @Getter
    public String ModelId;

    /**
     * The ssl context that will be used during authentication. If the provided connection string does not contain
     *  SAS based credentials, then the sslContext will be used for x509 authentication. If the provided connection string
     *  does contain SAS based credentials, the sslContext will still be used during SSL negotiation. By default, this SDK will
     *  create an SSLContext instance for you that trusts the IoT Hub public certificates.
     */
    @Setter
    @Getter
    public SSLContext sslContext;

    /**
     * The convention to be used for convention based operations.
     */
    @Setter
    @Getter
    @Builder.Default
    private PayloadConvention payloadConvention = DefaultPayloadConvention.getInstance();
}
