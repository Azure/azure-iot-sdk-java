package com.microsoft.azure.sdk.iot.service.messaging;

import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import lombok.Builder;
import lombok.Getter;

import javax.net.ssl.SSLContext;

/**
 * Configurable options for all service client operations
 */
@Builder
public final class MessagingClientOptions
{
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
}
