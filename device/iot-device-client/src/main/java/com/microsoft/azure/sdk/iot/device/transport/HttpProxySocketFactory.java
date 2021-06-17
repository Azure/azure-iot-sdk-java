/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.ProxySettings;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

@AllArgsConstructor
public class HttpProxySocketFactory extends SSLSocketFactory
{
    @Delegate(excludes = SSLSocketFactoryNonDelegatedFunctions.class)
    private final SSLSocketFactory delegate;

    private final ProxySettings proxySettings;

    @Override
    public Socket createSocket() throws IOException
    {
        Socket proxySocket = new Socket(proxySettings.getHostname(), proxySettings.getPort());
        return new ProxiedSSLSocket(delegate, proxySocket, proxySettings.getUsername(), proxySettings.getPassword());
    }

    @SuppressWarnings("unused") // Seems as if it's used in the Lombok delegate
    private interface SSLSocketFactoryNonDelegatedFunctions
    {
        Socket createSocket();
    }
}
