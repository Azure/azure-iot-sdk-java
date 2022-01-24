// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.SSLContext;

import static com.microsoft.azure.sdk.iot.device.DeviceClientConfig.DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;

/**
 * Options that allow configuration of the device client instance during initialization.
 */
@Builder
public final class ClientOptions
{
    /**
     * The Digital Twin Model Id associated with the device and module identity.
     * Non plug and play users should not set this value
     * This feature is currently supported only over MQTT, MQTT_WS, AMQPS, and AMQPS_WS.
     */
    @Getter
    private final String modelId;

    /**
     * The ssl context that will be used during authentication. If the provided connection string does not contain
     *  SAS based credentials, then the sslContext will be used for x509 authentication. If the provided connection string
     *  does contain SAS based credentials, the sslContext will still be used during SSL negotiation. By default, this SDK will
     *  create an SSLContext instance for you that trusts the IoT Hub public certificates.
     */
    @Getter
    private final SSLContext sslContext;

    /**
     * Gets the keep alive interval in seconds. This value defines the
     * maximum time interval between messages sent or received. It enables the
     * client to detect if the server is no longer available, without having to wait
     * for the TCP/IP timeout. The client will ensure that at least one message
     * travels across the network within each keep alive period. In the absence of a
     * data-related message during the time period, the client sends a very small
     * "ping" message, which the server will acknowledge. The default value is 230 seconds.
     *
     * <p>
     * This value is only used in stateful connection oriented protocols such as AMQPS, AMQPS_WS, MQTT, and MQTT_WS. If
     * the client is using HTTPS, then this value is ignored.
     * </p>
     */
    @Getter
    @Builder.Default
    private int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;

    /**
     * Sets the keep alive interval in seconds. This value defines the
     * maximum time interval between messages sent or received. It enables the
     * client to detect if the server is no longer available, without having to wait
     * for the TCP/IP timeout. The client will ensure that at least one message
     * travels across the network within each keep alive period. In the absence of a
     * data-related message during the time period, the client sends a very small
     * "ping" message, which the server will acknowledge. The default value is 230 seconds.
     *
     * <p>
     * This value is only used in stateful connection oriented protocols such as AMQPS, AMQPS_WS, MQTT, and MQTT_WS. If
     * the client is using HTTPS, then this value is ignored.
     * </p>
     *
     * @param keepAliveInterval the number of seconds that the keep alive interval will be. Must be greater than 0.
     */
    public void setKeepAliveInterval(int keepAliveInterval)
    {
        if (keepAliveInterval <= 0)
        {
            throw new IllegalArgumentException("Keep alive interval must be greater than 0 seconds");
        }

        this.keepAliveInterval = keepAliveInterval;
    }
}
