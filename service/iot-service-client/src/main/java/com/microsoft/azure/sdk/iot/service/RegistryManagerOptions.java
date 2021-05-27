package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.auth.AuthenticationScope;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import lombok.Builder;
import lombok.Getter;

/**
 * Configurable options for all registry manager operations
 */
@Builder
public class RegistryManagerOptions
{
    protected static final Integer DEFAULT_HTTP_READ_TIMEOUT_MS = 24000; // 24 seconds
    protected static final Integer DEFAULT_HTTP_CONNECT_TIMEOUT_MS = 24000; // 24 seconds

    /**
     * The options that specify what proxy to tunnel through. If null, no proxy will be used
     */
    @Getter
    private final ProxyOptions proxyOptions;

    /**
     * The http read timeout to a specified timeout, in milliseconds. A non-zero value specifies the timeout when reading from
     * Input stream after a connection is established to a resource. If the timeout expires before there is data available
     * for read, a java.net.SocketTimeoutException is raised. A timeout of zero is interpreted as an infinite timeout.
     * By default, this value is {@link #DEFAULT_HTTP_READ_TIMEOUT_MS}. Must be a non-negative value.
     */
    @Getter
    @Builder.Default
    private final int httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT_MS;

    /**
     * The http connect timeout value, in milliseconds, to be used when connecting to the service. If the timeout expires
     * before the connection can be established, a java.net.SocketTimeoutException is thrown.
     * A timeout of zero is interpreted as an infinite timeout. Must be a non-negative value.
     * By default, this value is {@link #DEFAULT_HTTP_CONNECT_TIMEOUT_MS}.
     */
    @Getter
    @Builder.Default
    private final int httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT_MS;

    @Getter
    @Builder.Default
    private final AuthenticationScope tokenCredentialAuthenticationScopes = AuthenticationScope.DEFAULT;
}
