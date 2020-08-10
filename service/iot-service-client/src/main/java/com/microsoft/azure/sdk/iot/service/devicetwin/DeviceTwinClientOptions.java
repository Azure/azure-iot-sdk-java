package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import lombok.Builder;
import lombok.Getter;

/**
 * Configurable options for all twin client operations.
 */
@Builder
public class DeviceTwinClientOptions
{
    protected static final Integer DEFAULT_HTTP_READ_TIMEOUT_MS = 24000; // 24 seconds
    protected static final Integer DEFAULT_HTTP_CONNECT_TIMEOUT_MS = 24000; // 24 seconds

    /**
     * The options that specify what proxy to tunnel through. If null, no proxy will be used.
     */
    @Getter
    private ProxyOptions proxyOptions;

    /**
     * The http read timeout to a specified timeout, in milliseconds. A non-zero value specifies the timeout when reading from
     * Input stream after a connection is established to a resource. If the timeout expires before there is data available
     * for read, a java.net.SocketTimeoutException is raised. A timeout of zero is interpreted as an infinite timeout.
     * By default, this value is {@link #DEFAULT_HTTP_READ_TIMEOUT_MS}. Must be a non-negative value.
     */
    @Getter
    private int httpReadTimeout;

    /**
     * The http connect timeout value, in milliseconds, to be used when connecting to the service. If the timeout expires
     * before the connection can be established, a java.net.SocketTimeoutException is thrown.
     * A timeout of zero is interpreted as an infinite timeout.
     * By default, this value is {@link #DEFAULT_HTTP_CONNECT_TIMEOUT_MS}. Must be a non-negative value.
     */
    @Getter
    private int httpConnectTimeout;
}
