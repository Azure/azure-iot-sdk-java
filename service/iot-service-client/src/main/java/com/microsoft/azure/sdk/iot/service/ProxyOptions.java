package com.microsoft.azure.sdk.iot.service;

import lombok.Getter;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * The settings supported by this SDK when communicating to IoT Hub through a proxy. HTTP proxies are supported by this SDK,
 * but only if the proxy does not require authentication.
 */
public class ProxyOptions
{
    @Getter
    private Proxy proxy;

    /**
     * Create the proxy options for connecting to a proxy
     * @param proxy the proxy to communicate through
     */
    public ProxyOptions(Proxy proxy)
    {
        if (proxy == null)
        {
            throw new IllegalArgumentException("Proxy cannot be null");
        }

        if (proxy.type() != Proxy.Type.HTTP)
        {
            throw new IllegalArgumentException("Service proxy options only support using HTTP proxies");
        }

        this.proxy = proxy;
    }

    /**
     * @return the saved proxy hostname
     */
    public String getHostName()
    {
        if (this.proxy.address() instanceof InetSocketAddress)
        {
            InetSocketAddress addr = (InetSocketAddress) this.proxy.address();
            return addr.getHostName();
        }
        else
        {
            //Should only happen if proxy is of type DIRECT, which isn't supported
            throw new UnsupportedOperationException("Unsupported proxy type, could not get host name. Proxy address was not an instance of java.net.InetSocketAddress.");
        }
    }

    public int getPort()
    {
        if (this.proxy.address() instanceof InetSocketAddress)
        {
            InetSocketAddress addr = (InetSocketAddress) this.proxy.address();
            return addr.getPort();
        }
        else
        {
            //Should only happen if proxy is of type DIRECT, which isn't supported
            throw new UnsupportedOperationException("Unsupported proxy type, could not get port. Proxy address was not an instance of java.net.InetSocketAddress.");
        }
    }
}

