// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.digitaltwin;

import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import lombok.Builder;
import lombok.Getter;

/**
 * Configurable options for all digital twin client operations. This options bundle is used by both
 * {@link DigitalTwinClient} and {@link DigitalTwinAsyncClient}.
 */
@Builder
public class DigitalTwinClientOptions
{
    protected static final Integer DEFAULT_HTTP_READ_TIMEOUT_SECONDS = 24;
    protected static final Integer DEFAULT_HTTP_CONNECT_TIMEOUT_SECONDS = 24;

    /**
     * The options that specify what proxy to tunnel through. If null, no proxy will be used
     */
    @Getter
    private final ProxyOptions proxyOptions;

    /**
     * The http read timeout to a specified timeout, in milliseconds. A non-zero value specifies the timeout when reading from
     * Input stream after a connection is established to a resource. If the timeout expires before there is data available
     * for read, a java.net.SocketTimeoutException is raised. A timeout of zero is interpreted as an infinite timeout.
     * By default, this value is {@link #DEFAULT_HTTP_READ_TIMEOUT_SECONDS}. Must be a non-negative value.
     */
    @Getter
    @Builder.Default
    private final int httpReadTimeoutSeconds = DEFAULT_HTTP_READ_TIMEOUT_SECONDS;

    /**
     * The http connect timeout value, in milliseconds, to be used when connecting to the service. If the timeout expires
     * before the connection can be established, a java.net.SocketTimeoutException is thrown.
     * A timeout of zero is interpreted as an infinite timeout. Must be a non-negative value.
     * By default, this value is {@link #DEFAULT_HTTP_CONNECT_TIMEOUT_SECONDS}.
     */
    @Getter
    @Builder.Default
    private final int httpConnectTimeoutSeconds = DEFAULT_HTTP_CONNECT_TIMEOUT_SECONDS;
}
