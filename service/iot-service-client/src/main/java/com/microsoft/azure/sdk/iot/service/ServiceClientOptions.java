package com.microsoft.azure.sdk.iot.service;

import lombok.Getter;
import lombok.Setter;

/**
 * Configurable options for all service client operations
 */
public class ServiceClientOptions
{
    /**
     * The options that specify what proxy to tunnel through. If null, no proxy will be used
     */
    @Getter
    @Setter
    private ProxyOptions proxyOptions;
}
