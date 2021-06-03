package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.auth.IotHubAuthenticationScopes;
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

    /**
     * The authentication scopes to use when requesting authentication tokens from Azure Active Directory for
     * authenticating with IoT Hub. This value is only used by the client if the client is configured to use role-based
     * access credentials rather than symmetric key based credentials. This value defaults to the authentication scopes
     * that are used for all public cloud deployments and all private cloud deployments other than those in the
     * Fairfax cloud. For Fairfax cloud users, this value must be set to
     * {@link IotHubAuthenticationScopes#FAIRFAX_AUTHENTICATION_SCOPES}.
     */
    @Getter
    @Builder.Default
    private final String[] tokenCredentialAuthenticationScopes = IotHubAuthenticationScopes.DEFAULT_AUTHENTICATION_SCOPES;
}
