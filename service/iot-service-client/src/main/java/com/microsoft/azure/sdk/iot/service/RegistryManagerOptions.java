package com.microsoft.azure.sdk.iot.service;

import lombok.Builder;
import lombok.Getter;

/**
 * Configurable options for all registry manager operations
 */
@Builder
public class RegistryManagerOptions
{
    protected static final Integer DEFAULT_HTTP_READ_TIMEOUT_MS = 24000; // 24 seconds
    protected static final Integer DEFAULT_HTTP_CONNECT_TIMEOUT_MS = 0; // infinite

    /**
     * The options that specify what proxy to tunnel through. If null, no proxy will be used
     */
    @Getter
    private ProxyOptions proxyOptions;

    @Getter
    private int httpReadTimeout;

    @Getter
    private int httpConnectTimeout;
}
