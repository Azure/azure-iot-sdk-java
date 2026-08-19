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
        // The socket to the proxy is deliberately left unconnected here. Connecting it in this factory method would put
        // the connection attempt outside of the connect timeout that the caller later passes to
        // Socket.connect(SocketAddress, int), so an unreachable or overloaded proxy would block the calling thread for
        // as long as the operating system's default TCP connect timeout allows. That surfaces as an unexplained hang
        // with no error for the transport layer to retry on. ProxiedSSLSocket connects it instead, so that the caller's
        // timeout covers connecting to the proxy as well as tunnelling through it.
        return new ProxiedSSLSocket(
            delegate,
            new Socket(),
            proxySettings.getHostname(),
            proxySettings.getPort(),
            proxySettings.getUsername(),
            proxySettings.getPassword());
    }

    @SuppressWarnings("unused") // Seems as if it's used in the Lombok delegate
    private interface SSLSocketFactoryNonDelegatedFunctions
    {
        Socket createSocket();
    }
}
