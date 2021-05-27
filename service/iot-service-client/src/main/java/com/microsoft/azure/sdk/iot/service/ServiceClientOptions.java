package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.auth.AuthenticationScope;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import lombok.Builder;
import lombok.Getter;

import javax.net.ssl.SSLContext;

/**
 * Configurable options for all service client operations
 */
@Builder
public class ServiceClientOptions
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
