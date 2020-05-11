package com.microsoft.azure.sdk.iot.service;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    private ProxyOptions proxyOptions;
}
