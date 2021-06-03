// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.jobs;

import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubAuthenticationScopes;
import lombok.Builder;
import lombok.Getter;

/**
 * Configurable options for all job client operations
 */
@Builder
public class JobClientOptions
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

    /**
     * The authentication scopes to use when requesting authentication tokens from Azure Active Directory for
     * authenticating with IoT Hub. This value is only used by the client if the client is configured to use role based
     * access credentials rather than symmetric key based credentials. This value defaults to the authentication scopes
     * that are used for all public cloud deployments and all private cloud deployments other than those in the
     * Fairfax cloud. For Fairfax cloud users, this value must be set to
     * {@link IotHubAuthenticationScopes#FAIRFAX_AUTHENTICATION_SCOPES}.
     */
    @Getter
    @Builder.Default
    private final String[] tokenCredentialAuthenticationScopes = IotHubAuthenticationScopes.DEFAULT_AUTHENTICATION_SCOPES;
}
