/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

public class ProxySettings
{
    private final Proxy proxy;
    private final String username;
    private final char[] password;

    /**
     * Create proxy settings for connecting to a proxy with a username and password
     * @param proxy the proxy to communicate through
     * @param username the username to authenticate against the proxy with
     * @param password the password to authenticate against the proxy with
     */
    public ProxySettings(Proxy proxy, final String username, final char[] password)
    {
        if (proxy == null)
        {
            throw new IllegalArgumentException("Proxy cannot be null");
        }

        if (proxy.type() == Type.DIRECT)
        {
            throw new IllegalArgumentException("Proxy settings cannot be configured to be DIRECT");
        }

        this.proxy = proxy;

        if (username != null && username.isEmpty())
        {
            throw new IllegalArgumentException("Username cannot be an empty string");
        }

        if (password != null && password.length == 0)
        {
            throw new IllegalArgumentException("Password cannot be an empty string");
        }

        this.username = username;
        this.password = password;
    }

    /**
     * Create proxy settings for connecting to a proxy without a username and password
     * @param proxy the proxy to communicate through
     */
    public ProxySettings(Proxy proxy)
    {
        this(proxy, null, null);
    }

    /**
     * @return the saved proxy instance
     */
    public Proxy getProxy()
    {
        return this.proxy;
    }

    /**
     * @return the saved proxy hostname
     */
    public String getHostname()
    {
        if (this.proxy.address() instanceof InetSocketAddress)
        {
            InetSocketAddress addr = (InetSocketAddress) this.proxy.address();
            return addr.getHostName();
        }
        else
        {
            //Should only happen if proxy is of type DIRECT, which isn't support
            throw new UnsupportedOperationException("Could not get port from saved proxy");
        }
    }

    /**
     * @return the saved proxy port
     */
    public int getPort()
    {
        if (this.proxy.address() instanceof InetSocketAddress)
        {
            InetSocketAddress addr = (InetSocketAddress) this.proxy.address();
            return addr.getPort();
        }
        else
        {
            //Should only happen if proxy is of type DIRECT, which isn't support
            throw new UnsupportedOperationException("Could not get port from saved proxy");
        }
    }

    /**
     * @return the saved proxy username
     */
    public String getUsername()
    {
        return this.username;
    }

    /**
     * @return the saved proxy password
     */
    public char[] getPassword()
    {
        return this.password;
    }
}
