/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.ProxySettings;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for HttpProxySocketFactory and the ProxiedSSLSocket that it creates.
 */
public class HttpProxySocketFactoryTest
{
    private static final int CONNECT_TIMEOUT_MILLISECONDS = 2 * 1000;

    /**
     * A proxy that accepts the TCP connection but never answers the HTTP CONNECT request used to block the calling
     * thread forever because no timeout was applied while waiting for that answer.
     */
    @Test
    public void connectTimesOutWhenProxyNeverAnswersConnectRequest() throws Exception
    {
        // The TCP connection is completed by the OS and left in this server socket's backlog since accept() is never
        // called, so the proxy never answers the CONNECT request that the socket under test sends it.
        try (ServerSocket unresponsiveProxy = new ServerSocket(0, 1, InetAddress.getLoopbackAddress()))
        {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(InetAddress.getLoopbackAddress(), unresponsiveProxy.getLocalPort()));
            ProxySettings proxySettings = new ProxySettings(proxy);

            SSLSocketFactory sslSocketFactory = SSLContext.getDefault().getSocketFactory();
            HttpProxySocketFactory httpProxySocketFactory = new HttpProxySocketFactory(sslSocketFactory, proxySettings);

            long startTime = System.currentTimeMillis();

            try (Socket socket = httpProxySocketFactory.createSocket())
            {
                socket.connect(new InetSocketAddress("some-host.example.com", 443), CONNECT_TIMEOUT_MILLISECONDS);
                fail("Expected the connect to time out since the proxy never answered the CONNECT request");
            }
            catch (SocketTimeoutException expected)
            {
                long elapsedTime = System.currentTimeMillis() - startTime;
                assertTrue(
                    "Expected the connect to give up at around the supplied timeout, but it took " + elapsedTime + " milliseconds",
                    elapsedTime < 4 * CONNECT_TIMEOUT_MILLISECONDS);
            }
        }
    }
}
