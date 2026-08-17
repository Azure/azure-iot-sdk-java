// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import org.junit.Test;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for ProxiedSSLSocket.
 */
public class ProxiedSSLSocketTest
{
    // Deliberately unresolved so that these tests never depend on DNS. ProxiedSSLSocket only reads the hostname and
    // port out of the address in order to build the CONNECT request.
    private static final InetSocketAddress DESTINATION =
        InetSocketAddress.createUnresolved("some-iot-hub.azure-devices.net", 443);

    private static final String CONNECT_ESTABLISHED_RESPONSE = "HTTP/1.1 200 Connection established\r\n\r\n";

    /**
     * Starts a local server that accepts a single connection and then optionally writes the given response. If the
     * response is null, the server accepts the connection but never answers, simulating an unresponsive proxy.
     */
    private static Thread startFakeProxy(final ServerSocket serverSocket, final String response)
    {
        Thread thread = new Thread(() ->
        {
            try (Socket accepted = serverSocket.accept())
            {
                if (response == null)
                {
                    // Hold the connection open without responding until the test closes the client side
                    InputStream inputStream = accepted.getInputStream();
                    while (inputStream.read() != -1)
                    {
                        // discard the CONNECT request and never reply
                    }
                }
                else
                {
                    OutputStream outputStream = accepted.getOutputStream();
                    outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();

                    // Keep the socket open so that the client can finish wrapping it
                    Thread.sleep(2000);
                }
            }
            catch (IOException | InterruptedException e)
            {
                // Expected once the test closes its end of the connection
            }
        });

        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    // Without a read timeout applied to the proxy socket, an unresponsive proxy blocks the connecting thread forever,
    // which prevents the layers above from ever retrying the connection. The JUnit timeout below fails the test rather
    // than hanging the build if that regresses.
    @Test(timeout = 30000)
    public void connectTimesOutWhenProxyDoesNotRespondToConnectRequest() throws Exception
    {
        try (ServerSocket unresponsiveProxy = new ServerSocket(0))
        {
            startFakeProxy(unresponsiveProxy, null);

            try (Socket proxySocket = new Socket("127.0.0.1", unresponsiveProxy.getLocalPort()))
            {
                ProxiedSSLSocket proxiedSSLSocket = new ProxiedSSLSocket(
                    (SSLSocketFactory) SSLSocketFactory.getDefault(), proxySocket, null, null);

                try
                {
                    proxiedSSLSocket.connect(DESTINATION, 500);
                    fail("Expected connect to time out instead of blocking indefinitely");
                }
                catch (SocketTimeoutException expected)
                {
                    // Expected
                }
            }
        }
    }

    // The timeout only applies while the tunnel is being established. Once the proxy has accepted the CONNECT request,
    // the socket must go back to its previous timeout so that tunneled traffic isn't cut short.
    @Test(timeout = 30000)
    public void connectRestoresPreviousSoTimeoutAfterTunnelIsEstablished() throws Exception
    {
        try (ServerSocket proxy = new ServerSocket(0))
        {
            startFakeProxy(proxy, CONNECT_ESTABLISHED_RESPONSE);

            try (Socket proxySocket = new Socket("127.0.0.1", proxy.getLocalPort()))
            {
                proxySocket.setSoTimeout(1234);

                ProxiedSSLSocket proxiedSSLSocket = new ProxiedSSLSocket(
                    (SSLSocketFactory) SSLSocketFactory.getDefault(), proxySocket, null, null);

                proxiedSSLSocket.connect(DESTINATION, 5000);

                assertEquals(
                    "Expected the socket's original read timeout to be restored once the tunnel was established",
                    1234,
                    proxySocket.getSoTimeout());
            }
        }
    }

    // A proxy that rejects the CONNECT request must surface an error rather than leaving a connected socket behind.
    @Test(timeout = 30000)
    public void connectThrowsWhenProxyRejectsConnectRequest() throws Exception
    {
        try (ServerSocket proxy = new ServerSocket(0))
        {
            startFakeProxy(proxy, "HTTP/1.1 407 Proxy Authentication Required\r\n\r\n");

            try (Socket proxySocket = new Socket("127.0.0.1", proxy.getLocalPort()))
            {
                ProxiedSSLSocket proxiedSSLSocket = new ProxiedSSLSocket(
                    (SSLSocketFactory) SSLSocketFactory.getDefault(), proxySocket, null, null);

                try
                {
                    proxiedSSLSocket.connect(DESTINATION, 5000);
                    fail("Expected connect to throw when the proxy rejects the CONNECT request");
                }
                catch (IOException expected)
                {
                    // Expected
                }

                // The failed tunnel must not leave a connected socket behind
                proxiedSSLSocket.close();
            }
        }
    }
}
