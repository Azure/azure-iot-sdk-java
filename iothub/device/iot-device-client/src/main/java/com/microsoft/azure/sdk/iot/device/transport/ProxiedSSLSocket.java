/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.ProxySettings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

/**
 * Extension of an SSLSocket that sends an HTTP CONNECT packet to the proxy socket before sending the SSL handshake upstream.
 */
@Slf4j
class ProxiedSSLSocket extends SSLSocket
{
    private final SSLSocketFactory socketFactory;

    //Socket used for sending the CONNECT to the HTTP proxy
    private final Socket proxySocket;

    //Socket used for ssl negotiation with the actual host
    @Delegate(excludes = ProxiedSSLSocketNonDelegatedFunctions.class)
    private SSLSocket sslSocket;

    private final ProxySettings proxySettings;

    private static final String HTTP = "HTTP/";
    private static final String HTTP_VERSION_1_1 = HTTP + "1.1";

    // Callers may ask to connect without a timeout (a timeout of 0 means "block forever" for a plain socket).
    // Blocking forever while connecting to a proxy, or while waiting for the proxy to answer the CONNECT request,
    // would hang the calling thread with no way to recover, so this timeout is used instead in that case.
    private static final int DEFAULT_PROXY_CONNECT_TIMEOUT_MILLISECONDS = 60 * 1000;

    ProxiedSSLSocket(SSLSocketFactory socketFactory, Socket proxySocket, ProxySettings proxySettings)
    {
        this.socketFactory = socketFactory;
        this.proxySocket = proxySocket;
        this.proxySettings = proxySettings;
    }

    @Override
    public void connect(SocketAddress socketAddress) throws IOException
    {
        connect(socketAddress, 0);
    }

    @Override
    public void connect(SocketAddress socketAddress, int timeout) throws IOException
    {
        int effectiveTimeout = timeout > 0 ? timeout : DEFAULT_PROXY_CONNECT_TIMEOUT_MILLISECONDS;
        long deadline = System.currentTimeMillis() + effectiveTimeout;

        InetSocketAddress destination = (InetSocketAddress) socketAddress;
        String destinationHost = destination.getHostName();
        int destinationPort = destination.getPort();

        log.debug("Connecting to HTTP proxy");
        this.proxySocket.connect(new InetSocketAddress(proxySettings.getHostname(), proxySettings.getPort()), effectiveTimeout);

        log.debug("Sending tunnel handshake to HTTP proxy");
        int previousSoTimeout = this.proxySocket.getSoTimeout();

        // Without a read timeout, a proxy that accepts the TCP connection but never answers the CONNECT request
        // would block this thread forever.
        this.proxySocket.setSoTimeout(getRemainingTimeout(deadline));

        try
        {
            doTunnelHandshake(this.proxySocket, destinationHost, destinationPort);
        }
        finally
        {
            // doTunnelHandshake() closes the socket when the proxy rejects the tunnel, and setting a timeout on a
            // closed socket would throw an exception that masks the actual failure.
            if (!this.proxySocket.isClosed())
            {
                this.proxySocket.setSoTimeout(previousSoTimeout);
            }
        }

        log.debug("Handshake to HTTP proxy succeeded");

        //Wrap the proxy socket into the new SSLSocket so all further communication gets forwarded through the proxy
        this.sslSocket = (SSLSocket) socketFactory.createSocket(proxySocket, destinationHost, destinationPort, true);
    }

    @Override
    public void close() throws IOException {
        this.proxySocket.close();

        // The ssl socket is only created once the tunnel to the proxy has been established, so it may be null if this
        // socket is closed after a failed connect attempt.
        if (this.sslSocket != null)
        {
            this.sslSocket.close();
        }
    }

    private static int getRemainingTimeout(long deadline)
    {
        long remainingMilliseconds = deadline - System.currentTimeMillis();

        // A SO_TIMEOUT of 0 means "block forever", so the remaining time must never be allowed to reach 0.
        return remainingMilliseconds < 1 ? 1 : (int) Math.min(remainingMilliseconds, Integer.MAX_VALUE);
    }

    /**
     * Send a CONNECT request to the HTTP proxy whose endpoint is defined within the tunnel socket
     * @param tunnel The socket to communicate to the HTTP proxy through
     * @param host The destination host the proxy will forward communication to
     * @param port The destination port the proxy will forward communication to
     * @throws IOException If unable to read or send to the HTTP proxy
     */
    private void doTunnelHandshake(Socket tunnel, String host, int port) throws IOException
    {
        Charset byteEncoding = StandardCharsets.UTF_8;
        OutputStream out = tunnel.getOutputStream();
        String hostWithPort = host + ":" + port;
        String proxyUsername = this.proxySettings.getUsername();
        char[] proxyPassword = this.proxySettings.getPassword();

        String proxyConnectMessage = String.format("CONNECT %s %s\r\nHost: %s\r\nUser-Agent: %s\r\n", hostWithPort, HTTP_VERSION_1_1, hostWithPort, TransportUtils.USER_AGENT_STRING);
        if (proxyUsername != null && proxyPassword != null)
        {
            String base64EncodedCredentials = new String(Base64.encodeBase64(String.format("%s:%s", proxyUsername, new String(proxyPassword)).getBytes(byteEncoding)), byteEncoding);
            proxyConnectMessage += String.format("Proxy-Authorization: Basic %s\r\nUser-Agent: %s\r\n", base64EncodedCredentials, TransportUtils.USER_AGENT_STRING);
        }

        proxyConnectMessage += "\r\n";

        byte[] proxyConnectBytes = proxyConnectMessage.getBytes(byteEncoding);

        out.write(proxyConnectBytes);
        out.flush();

        //Cannot do any buffering while reading, only read what is relevant to the connect response
        HttpConnectResponseReader in = new HttpConnectResponseReader(tunnel.getInputStream(), byteEncoding);

        String connectResponse = in.readHttpConnectResponse();

        String[] connectResponseLines = connectResponse.split("\r\n");

        int connectResponseStart = 0;
        while (connectResponseLines[connectResponseStart].isEmpty())
        {
            connectResponseStart++;
        }

        //Expects the same http version in the response as the request
        String firstLine = connectResponseLines[connectResponseStart];
        if (!firstLine.startsWith(HTTP))
        {
            tunnel.close();
            throw new IOException(String.format("Unable to tunnel through %s:%d.  Expected first response line to start with %s, but proxy returns \"%s\"", host, port, HTTP, firstLine));
        }

        String[] replyStrParts = firstLine.split(" ");
        if (replyStrParts.length < 2)
        {
            tunnel.close();
            throw new IOException(String.format("Unable to tunnel through %s:%d. Expected proxy response to CONNECT to contain a space between http version and status code, but was %s", host, port, firstLine));
        }

        int connectResponseStatusCode;
        try
        {
            connectResponseStatusCode = Integer.parseInt(replyStrParts[1]);
        }
        catch (NumberFormatException e)
        {
            tunnel.close();
            throw new IOException(String.format("Unable to tunnel through %s:%d. Expected proxy response to CONNECT to contain a status code but status code could not be parsed. Response was %s", host, port, firstLine));
        }

        if (connectResponseStatusCode <= 199 || connectResponseStatusCode >= 300)
        {
            tunnel.close();
            throw new IOException(String.format("Unable to tunnel through %s:%d. Expected proxy response to CONNECT to return status code 2XX but status code was %d", host, port, connectResponseStatusCode));
        }

        log.trace("HTTP proxy responded to connect request with status {}, so the proxy connect was successful", connectResponseStatusCode);
    }

    @SuppressWarnings("unused") // Interface should not change
    private interface ProxiedSSLSocketNonDelegatedFunctions
    {
        void connect(SocketAddress socketAddress, int timeout);
        void connect(SocketAddress socketAddress);
        void close();
    }

    @RequiredArgsConstructor
    static class HttpConnectResponseReader
    {
        private boolean alreadyRead = false;
        @NonNull private final InputStream inputStream;
        @NonNull private final Charset byteEncoding;

        String readHttpConnectResponse() throws IOException
        {
            if (alreadyRead)
            {
                throw new IOException("Http connect response has already been read");
            }

            ByteArrayOutputStream httpLineOutputStream = new ByteArrayOutputStream();
            LinkedList<Integer> mostRecentFourCharacters = new LinkedList<>();

            //until the 4 most recently read characters were \r\n\r\n
            while (!isCRLF(mostRecentFourCharacters))
            {
                int i = inputStream.read();
                if (i == -1)
                {
                    inputStream.close();
                    throw new IOException("Unexpected EOF from proxy");
                }

                httpLineOutputStream.write(i);

                if (mostRecentFourCharacters.size() == 4)
                {
                    mostRecentFourCharacters.poll();
                }

                mostRecentFourCharacters.offer(i);
            }

            // Suppressed inspection because the suggestion is only valid for Java10+
            //noinspection StringOperationCanBeSimplified
            String httpHeaderLine = new String(httpLineOutputStream.toByteArray(), byteEncoding);
            httpLineOutputStream.close();
            alreadyRead = true;
            return httpHeaderLine;
        }

        boolean isCRLF(List<Integer> list)
        {
            if (list.size() < 4)
            {
                return false;
            }
            return list.get(0) == '\r' && list.get(1) == '\n' && list.get(2) == '\r' && list.get(3) == '\n';
        }
    }
}
