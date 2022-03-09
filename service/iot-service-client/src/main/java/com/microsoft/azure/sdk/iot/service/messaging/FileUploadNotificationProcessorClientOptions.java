// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import lombok.Builder;
import lombok.Getter;

import javax.net.ssl.SSLContext;
import java.util.function.Consumer;

/**
 * The optional parameters that can be configured for an {@link FileUploadNotificationProcessorClient} instance.
 */
@Builder
public class FileUploadNotificationProcessorClientOptions
{
    public static final int DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS = 230;

    /**
     * The options that specify what proxy to tunnel through. If null, no proxy will be used
     */
    @Getter
    private final ProxyOptions proxyOptions;

    /**
     * The SSL context to use when opening the AMQPS/AMQPS_WS connections. If not set, this library will generate the default
     * SSL context that trusts the IoT Hub public certificates.
     */
    @Getter
    private final SSLContext sslContext;

    /**
     * The callback to be executed when a connection level error occurs on an active connection for this client.
     */
    @Getter
    private final Consumer<ErrorContext> errorProcessor;

    /**
     * This value defines the maximum time interval between messages sent or received. It enables the
     * client to detect if the server is no longer available, without having to wait
     * for the TCP/IP timeout. The client will ensure that at least one message
     * travels across the network within each keep alive period. In the absence of a
     * data-related message during the time period, the client sends a very small
     * "ping" message, which the server will acknowledge. The default value is 230 seconds.
     */
    @Getter
    @Builder.Default
    private final int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;
}
