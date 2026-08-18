/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport;

import lombok.NonNull;
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
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private final String proxyUsername;
    private final char[] proxyPassword;

    private static final String HTTP = "HTTP/";
    private static final String HTTP_VERSION_1_1 = HTTP + "1.1";


    ProxiedSSLSocket(SSLSocketFactory socketFactory, Socket proxySocket, String proxyUsername, char[] proxyPassword)
    {
        this.socketFactory = socketFactory;
        this.proxySocket = proxySocket;

        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }

    @Override
    public void connect(SocketAddress socketAddress) throws IOException
    {
        connect(socketAddress, 0);
    }

    @Override
    public void connect(SocketAddress socketAddress, int timeout) throws IOException
    {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;

        log.debug("Sending tunnel handshake to HTTP proxy");

        // The proxy's response to the CONNECT request is read from a blocking stream, so without a timeout an
        // unresponsive proxy would block this thread indefinitely rather than surfacing an error that the layers above
        // can retry. The read timeout is applied as a deadline across the whole CONNECT response rather than per read,
        // because the response is consumed a byte at a time and a proxy that trickles out one byte per interval would
        // otherwise keep this call blocked forever. The previous value is restored once the tunnel is established so
        // that it does not affect traffic sent through it. A timeout of 0 means "no timeout", which matches the
        // behaviour of Socket.connect(SocketAddress, int).
        int previousSoTimeout = this.proxySocket.getSoTimeout();

        try
        {
            doTunnelHandshake(this.proxySocket, inetSocketAddress.getHostName(), inetSocketAddress.getPort(), timeout);
        }
        catch (IOException | RuntimeException e)
        {
            // Don't leak the socket if the tunnel could not be established. A malformed proxy response can surface as
            // an unchecked exception rather than an IOException, so those have to be cleaned up after as well.
            closeProxySocketQuietly(e);
            throw e;
        }

        this.proxySocket.setSoTimeout(previousSoTimeout);

        log.debug("Handshake to HTTP proxy succeeded");

        //Wrap the proxy socket into the new SSLSocket so all further communication gets forwarded through the proxy
        this.sslSocket = (SSLSocket) socketFactory.createSocket(this.proxySocket, inetSocketAddress.getHostName(), inetSocketAddress.getPort(), true);
    }

    /**
     * Close the proxy socket while propagating the failure that caused the tunnel to be abandoned. Any problem closing
     * the socket is attached to that failure rather than replacing it.
     * @param cause The failure that caused the tunnel handshake to be abandoned
     */
    private void closeProxySocketQuietly(Throwable cause)
    {
        try
        {
            this.proxySocket.close();
        }
        catch (IOException closeException)
        {
            cause.addSuppressed(closeException);
        }
    }

    @Override
    public void close() throws IOException {
        this.proxySocket.close();

        // May be null if the tunnel handshake never completed, so this socket was never fully connected
        if (this.sslSocket != null)
        {
            this.sslSocket.close();
        }
    }

    /**
     * Send a CONNECT request to the HTTP proxy whose endpoint is defined within the tunnel socket
     * @param tunnel The socket to communicate to the HTTP proxy through
     * @param host The destination host the proxy will forward communication to
     * @param port The destination port the proxy will forward communication to
     * @param timeoutMillis How long to wait for the proxy's complete response, where 0 means wait indefinitely
     * @throws IOException If unable to read or send to the HTTP proxy, or if the proxy did not respond in time
     */
    private void doTunnelHandshake(Socket tunnel, String host, int port, int timeoutMillis) throws IOException
    {
        Charset byteEncoding = StandardCharsets.UTF_8;
        OutputStream out = tunnel.getOutputStream();
        String hostWithPort = host + ":" + port;

        String proxyConnectMessage = String.format("CONNECT %s %s\r\nHost: %s\r\nUser-Agent: %s\r\n", hostWithPort, HTTP_VERSION_1_1, hostWithPort, TransportUtils.USER_AGENT_STRING);
        if (this.proxyUsername != null && this.proxyPassword != null)
        {
            String base64EncodedCredentials = new String(Base64.encodeBase64(String.format("%s:%s", this.proxyUsername, new String(this.proxyPassword)).getBytes(byteEncoding)), byteEncoding);
            proxyConnectMessage += String.format("Proxy-Authorization: Basic %s\r\nUser-Agent: %s\r\n", base64EncodedCredentials, TransportUtils.USER_AGENT_STRING);
        }

        proxyConnectMessage += "\r\n";

        byte[] proxyConnectBytes = proxyConnectMessage.getBytes(byteEncoding);

        out.write(proxyConnectBytes);
        out.flush();

        //Cannot do any buffering while reading, only read what is relevant to the connect response
        HttpConnectResponseReader in = new HttpConnectResponseReader(tunnel.getInputStream(), byteEncoding, tunnel, timeoutMillis);

        String connectResponse = in.readHttpConnectResponse();

        String[] connectResponseLines = connectResponse.split("\r\n");

        int connectResponseStart = 0;
        while (connectResponseStart < connectResponseLines.length && connectResponseLines[connectResponseStart].isEmpty())
        {
            connectResponseStart++;
        }

        // A response made up entirely of blank lines has no status line to inspect. Split discards trailing empty
        // strings, so a response of just "\r\n\r\n" produces an empty array rather than a set of empty lines.
        if (connectResponseStart == connectResponseLines.length)
        {
            tunnel.close();
            throw new IOException(String.format("Unable to tunnel through %s:%d. Proxy response to CONNECT did not contain a status line", host, port));
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

    static class HttpConnectResponseReader
    {
        private boolean alreadyRead = false;
        @NonNull private final InputStream inputStream;
        @NonNull private final Charset byteEncoding;

        // Socket whose read timeout is narrowed as the deadline approaches. Null when no deadline is being enforced.
        private final Socket socket;
        private final int timeoutMillis;
        private final long deadlineNanos;

        HttpConnectResponseReader(@NonNull InputStream inputStream, @NonNull Charset byteEncoding, Socket socket, int timeoutMillis)
        {
            this.inputStream = inputStream;
            this.byteEncoding = byteEncoding;
            this.socket = socket;
            this.timeoutMillis = timeoutMillis;
            this.deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(Math.max(timeoutMillis, 0));
        }

        /**
         * Narrow the socket's read timeout to whatever is left of the caller's timeout, so that the total time spent
         * reading the proxy's response is bounded even though it is read one byte at a time.
         * @throws SocketTimeoutException If the deadline has already passed
         * @throws IOException If the socket's timeout could not be updated
         */
        private void applyRemainingTimeout() throws IOException
        {
            // 0 means "no timeout", matching the behaviour of Socket.connect(SocketAddress, int)
            if (this.socket == null || this.timeoutMillis <= 0)
            {
                return;
            }

            long remainingMillis = TimeUnit.NANOSECONDS.toMillis(this.deadlineNanos - System.nanoTime());
            if (remainingMillis <= 0)
            {
                throw new SocketTimeoutException("Timed out waiting for the HTTP proxy to respond to the CONNECT request");
            }

            this.socket.setSoTimeout((int) Math.min(remainingMillis, Integer.MAX_VALUE));
        }

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
                applyRemainingTimeout();

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
