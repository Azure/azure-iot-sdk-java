// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.ProxySettings;
import org.junit.Test;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

                // Asserted here rather than relying on try-with-resources, which would close the socket regardless and
                // would let this test pass even if connect() stopped cleaning up after a failed handshake.
                assertTrue(
                    "Expected connect to close the proxy socket after the handshake timed out",
                    proxySocket.isClosed());
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

    /**
     * Starts a local server that answers the CONNECT request one byte at a time, pausing between each byte. Used to
     * show that the caller's timeout bounds the whole exchange rather than each individual read.
     */
    private static void startTricklingProxy(final ServerSocket serverSocket, final String response, final long millisBetweenBytes)
    {
        Thread thread = new Thread(() ->
        {
            try (Socket accepted = serverSocket.accept())
            {
                OutputStream outputStream = accepted.getOutputStream();
                for (byte b : response.getBytes(StandardCharsets.UTF_8))
                {
                    outputStream.write(b);
                    outputStream.flush();
                    Thread.sleep(millisBetweenBytes);
                }

                Thread.sleep(2000);
            }
            catch (IOException | InterruptedException e)
            {
                // Expected once the test closes its end of the connection
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    // A read timeout on its own only bounds each individual read, and the proxy's response is consumed one byte at a
    // time. A proxy that sends a byte just before each interval expires would keep connect() blocked forever, so the
    // timeout has to be enforced as a deadline across the whole response.
    @Test(timeout = 30000)
    public void connectTimesOutWhenProxyTricklesResponseSlowerThanTimeout() throws Exception
    {
        try (ServerSocket tricklingProxy = new ServerSocket(0))
        {
            // Each byte arrives well within the 500ms timeout, but the full response would take far longer than it
            startTricklingProxy(tricklingProxy, CONNECT_ESTABLISHED_RESPONSE, 200);

            try (Socket proxySocket = new Socket("127.0.0.1", tricklingProxy.getLocalPort()))
            {
                ProxiedSSLSocket proxiedSSLSocket = new ProxiedSSLSocket(
                    (SSLSocketFactory) SSLSocketFactory.getDefault(), proxySocket, null, null);

                long startMillis = System.currentTimeMillis();

                try
                {
                    proxiedSSLSocket.connect(DESTINATION, 500);
                    fail("Expected connect to time out rather than following the proxy's pace indefinitely");
                }
                catch (SocketTimeoutException expected)
                {
                    // Expected
                }

                long elapsedMillis = System.currentTimeMillis() - startMillis;

                // Generous upper bound; without a deadline this would run until the whole response had trickled in
                assertTrue(
                    "Expected connect to give up near its 500ms timeout, but it took " + elapsedMillis + "ms",
                    elapsedMillis < 5000);

                assertTrue(
                    "Expected connect to close the proxy socket after the handshake timed out",
                    proxySocket.isClosed());
            }
        }
    }

    // "\r\n\r\n".split("\r\n") discards trailing empty strings and yields an empty array, so a response with no status
    // line used to fail with an ArrayIndexOutOfBoundsException that escaped the IOException cleanup path.
    @Test(timeout = 30000)
    public void connectThrowsIOExceptionAndClosesSocketWhenProxyResponseHasNoStatusLine() throws Exception
    {
        try (ServerSocket proxy = new ServerSocket(0))
        {
            startFakeProxy(proxy, "\r\n\r\n");

            try (Socket proxySocket = new Socket("127.0.0.1", proxy.getLocalPort()))
            {
                ProxiedSSLSocket proxiedSSLSocket = new ProxiedSSLSocket(
                    (SSLSocketFactory) SSLSocketFactory.getDefault(), proxySocket, null, null);

                try
                {
                    proxiedSSLSocket.connect(DESTINATION, 5000);
                    fail("Expected connect to throw when the proxy response contains no status line");
                }
                catch (IOException expected)
                {
                    // Expected. An unchecked exception here would mean malformed responses bypass the cleanup path.
                }

                assertTrue(
                    "Expected connect to close the proxy socket after a malformed proxy response",
                    proxySocket.isClosed());
            }
        }
    }

    /**
     * Connecting to the proxy has to happen while handling connect(), not while creating the socket, because only then
     * is a connect timeout available. If the socket returned by the factory were already connected, an unreachable
     * proxy would block the calling thread for as long as the operating system's default TCP connect timeout allows,
     * with no error for the transport layer to retry on.
     */
    @Test
    public void createSocketDoesNotConnectToTheProxy() throws IOException
    {
        int closedPort;
        try (ServerSocket temporaryServer = new ServerSocket(0))
        {
            closedPort = temporaryServer.getLocalPort();
        }

        //Nothing is listening on that port now, so any attempt to connect to it fails rather than hangs
        HttpProxySocketFactory socketFactory = new HttpProxySocketFactory(
            (SSLSocketFactory) SSLSocketFactory.getDefault(),
            new ProxySettings(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", closedPort))));

        // Creating the socket must not attempt to reach the proxy at all
        Socket socket = socketFactory.createSocket();

        try
        {
            socket.connect(DESTINATION, 5000);
            fail("Expected connect to fail because nothing is listening on the proxy's port");
        }
        catch (IOException expected)
        {
            // Expected. The connection to the proxy is attempted while handling connect, where the timeout applies.
        }
    }

    /**
     * Guards the case above from being satisfied by a socket that never connects to the proxy at all.
     */
    @Test
    public void connectEstablishesTheTunnelWhenTheFactoryReturnsAnUnconnectedSocket() throws IOException
    {
        try (ServerSocket proxy = new ServerSocket(0))
        {
            startFakeProxy(proxy, CONNECT_ESTABLISHED_RESPONSE);

            HttpProxySocketFactory socketFactory = new HttpProxySocketFactory(
                (SSLSocketFactory) SSLSocketFactory.getDefault(),
                new ProxySettings(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", proxy.getLocalPort()))));

            Socket socket = socketFactory.createSocket();

            socket.connect(DESTINATION, 5000);

            assertTrue("Expected the tunnel to be established through the proxy", socket.isConnected());
        }
    }
}
