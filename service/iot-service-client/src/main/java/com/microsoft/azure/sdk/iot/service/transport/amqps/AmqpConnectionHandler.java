/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.proton.transport.proxy.ProxyHandler;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyHandlerImpl;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyImpl;
import com.microsoft.azure.proton.transport.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
abstract class AmqpConnectionHandler extends ErrorLoggingBaseHandlerWithCleanup implements CbsSessionStateCallback
{
    private static final int AMQPS_PORT = 5671;
    private static final int AMQPS_WS_PORT = 443;
    private static final String WEB_SOCKET_PATH = "/$iothub/websocket";
    private static final String WEB_SOCKET_SUB_PROTOCOL = "AMQPWSB10";
    private static final String WEB_SOCKET_QUERY = "iothub-no-client-cert=true";
    private static final int MAX_MESSAGE_PAYLOAD_SIZE = 65 * 1024; //max IoT Hub cloud to device message size is 65 kb, so amqp websocket layer should buffer at most that much space

    private String connectionId;
    private final int keepAliveIntervalSeconds;

    @Getter
    protected final String hostName;
    private TokenCredential credential;
    private AzureSasCredential azureSasCredential;
    private String connectionString;
    private IotHubServiceClientProtocol protocol;
    private ProxyOptions proxyOptions;
    private SSLContext sslContext;

    Connection connection;
    private CbsSessionHandler cbsSessionHandler;
    private Runnable onConnectionClosedCallback;

    AmqpConnectionHandler(
        String connectionString,
        IotHubServiceClientProtocol protocol,
        Consumer<ErrorContext> errorProcessor,
        ProxyOptions proxyOptions,
        SSLContext sslContext,
        int keepAliveIntervalSeconds)
    {
        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("connectionString can not be null or empty");
        }

        Objects.requireNonNull(protocol);

        this.proxyOptions = proxyOptions;
        this.hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString).getHostName();
        this.connectionString = connectionString;
        this.errorProcessor = errorProcessor;
        this.keepAliveIntervalSeconds = keepAliveIntervalSeconds;

        commonConstructorSetup(protocol, proxyOptions, sslContext, keepAliveIntervalSeconds);
    }

    AmqpConnectionHandler(
        String hostName,
        AzureSasCredential azureSasCredential,
        IotHubServiceClientProtocol protocol,
        Consumer<ErrorContext> errorProcessor,
        ProxyOptions proxyOptions,
        SSLContext sslContext,
        int keepAliveIntervalSeconds)
    {
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName can not be null or empty");
        }

        Objects.requireNonNull(protocol);
        Objects.requireNonNull(azureSasCredential);

        this.hostName = hostName;
        this.azureSasCredential = azureSasCredential;
        this.errorProcessor = errorProcessor;
        this.keepAliveIntervalSeconds = keepAliveIntervalSeconds;

        commonConstructorSetup(protocol, proxyOptions, sslContext, keepAliveIntervalSeconds);
    }

    AmqpConnectionHandler(
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol protocol,
        Consumer<ErrorContext> errorProcessor,
        ProxyOptions proxyOptions,
        SSLContext sslContext,
        int keepAliveIntervalSeconds)
    {
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName can not be null or empty");
        }

        Objects.requireNonNull(protocol);
        Objects.requireNonNull(credential);

        this.hostName = hostName;
        this.credential = credential;
        this.errorProcessor = errorProcessor;
        this.keepAliveIntervalSeconds = keepAliveIntervalSeconds;

        commonConstructorSetup(protocol, proxyOptions, sslContext, keepAliveIntervalSeconds);
    }

    private void commonConstructorSetup(
            IotHubServiceClientProtocol protocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext,
            int keepAliveIntervalSeconds)
    {
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext; // if null, a default SSLContext will be generated for the user
        this.protocol = protocol;
        this.connectionId = UUID.randomUUID().toString();

        if (keepAliveIntervalSeconds <= 0)
        {
            throw new IllegalArgumentException("Keep alive interval must be greater than 0 milliseconds");
        }

        if (proxyOptions != null && this.protocol != IotHubServiceClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Proxies are only supported over AMQPS_WS");
        }

        // Enables proton-j to automatically mirror the local state of the client with the remote state. For instance,
        // if the service closes a session, this handshaker will automatically close the session locally as well.
        add(new Handshaker());
    }

    @Override
    public void onReactorInit(Event event)
    {
        Reactor reactor = event.getReactor();

        if (this.protocol == IotHubServiceClientProtocol.AMQPS_WS)
        {
            if (proxyOptions != null)
            {
                reactor.connectionToHost(proxyOptions.getHostName(), proxyOptions.getPort(), this);
            }
            else
            {
                reactor.connectionToHost(this.hostName, AMQPS_WS_PORT, this);
            }
        }
        else
        {
            reactor.connectionToHost(this.hostName, AMQPS_PORT, this);
        }
    }

    /**
     * Event handler for the connection bound event
     * @param event The proton event object
     */
    @Override
    public void onConnectionBound(Event event)
    {
        Transport transport = event.getConnection().getTransport();

        // Convert from seconds to milliseconds since this proton-j API only accepts keep alive in milliseconds
        transport.setIdleTimeout(this.keepAliveIntervalSeconds * 1000);

        if (this.protocol == IotHubServiceClientProtocol.AMQPS_WS)
        {
            WebSocketImpl webSocket = new WebSocketImpl(MAX_MESSAGE_PAYLOAD_SIZE);
            webSocket.configure(this.hostName, WEB_SOCKET_PATH, WEB_SOCKET_QUERY, AMQPS_WS_PORT, WEB_SOCKET_SUB_PROTOCOL, null, null);
            ((TransportInternal) transport).addTransportLayer(webSocket);
        }

        // Note that this does not mean that the connection will not be authenticated. This simply defers authentication
        // to the claims based security model that IoT Hub implements wherein the client sends the authentication token
        // over the CBS link rather than doing a sasl.plain(username, password) call at this point.
        transport.sasl().setMechanisms("ANONYMOUS");

        SslDomain domain = makeDomain();
        domain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
        transport.ssl(domain);

        if (this.proxyOptions != null)
        {
            addProxyLayer(transport, this.hostName);
        }
    }

    @Override
    public void onConnectionInit(Event event)
    {
        Connection conn = event.getConnection();
        conn.setHostname(hostName);
        log.debug("Opening AMQP connection");
        conn.open();
    }

    @Override
    public void onLinkRemoteOpen(Event event)
    {
        super.onLinkRemoteOpen(event);
    }

    @Override
    public void onSessionRemoteOpen(Event event)
    {
        super.onSessionRemoteOpen(event);
    }

    @Override
    public void onConnectionRemoteOpen(Event event)
    {
        super.onConnectionRemoteOpen(event);
        this.connection = event.getConnection();

        // Once the connection opens, get that connection and make it create a new session that will serve as the CBS
        // session where authentication will take place.
        Session cbsSession = event.getConnection().session();

        if (this.credential != null)
        {
            this.cbsSessionHandler = new CbsSessionHandler(cbsSession, this, this.credential);
        }
        else if (this.azureSasCredential != null)
        {
            this.cbsSessionHandler = new CbsSessionHandler(cbsSession, this, this.azureSasCredential);
        }
        else
        {
            this.cbsSessionHandler = new CbsSessionHandler(cbsSession, this, this.connectionString);
        }
    }

    @Override
    public void onConnectionRemoteClose(Event event)
    {
        super.onConnectionRemoteClose(event);

        if (this.onConnectionClosedCallback != null)
        {
            this.onConnectionClosedCallback.run();
        }
    }

    public String getConnectionId()
    {
        return this.connectionId;
    }

    /**
     * If an exception was encountered while opening the AMQP connection, this function shall throw that saved exception
     * @throws IOException if an exception was encountered while openinging the AMQP connection. The encountered
     * exception will be the inner exception
     */
    public void verifyConnectionWasOpened() throws IOException, IotHubException
    {
        if (this.protonJExceptionParser != null)
        {
            if (this.protonJExceptionParser.getIotHubException() != null)
            {
                throw this.protonJExceptionParser.getIotHubException();
            }
            else if (this.protonJExceptionParser.getNetworkException() != null)
            {
                throw this.protonJExceptionParser.getNetworkException();
            }
        }
    }

    /**
     * Create Proton SslDomain object from Address using the given Ssl mode
     * @return The created Ssl domain
     */
    private SslDomain makeDomain()
    {
        SslDomain domain = Proton.sslDomain();

        if (this.sslContext == null)
        {
            // Need the base trusted certs for IotHub in our ssl context. IotHubSSLContext handles that
            domain.setSslContext(new IotHubSSLContext().getSSLContext());
        }
        else
        {
            // Custom SSLContext set by user from service client options
            domain.setSslContext(this.sslContext);
        }

        domain.init(SslDomain.Mode.CLIENT);

        return domain;
    }

    private void addProxyLayer(Transport transport, String hostName)
    {
        log.trace("Adding proxy layer to amqp_ws connection");
        ProxyImpl proxy = new ProxyImpl();
        final ProxyHandler proxyHandler = new ProxyHandlerImpl();
        proxy.configure(hostName + ":" + AmqpConnectionHandler.AMQPS_WS_PORT, null, proxyHandler, transport);
        ((TransportInternal) transport).addTransportLayer(proxy);
    }

    public boolean isOpen()
    {
        return this.connection != null
            && this.connection.getLocalState() == EndpointState.ACTIVE
            && this.connection.getRemoteState() == EndpointState.ACTIVE
            && this.cbsSessionHandler.isOpen();
    }

    public void closeAsync(Runnable onConnectionClosedCallback)
    {
        if (this.connection != null)
        {
            log.debug("Shutdown event occurred, closing amqp connection");
            this.connection.close();
        }

        if (this.cbsSessionHandler != null)
        {
            log.debug("Shutdown event occurred, closing cbs session");
            this.cbsSessionHandler.close();
        }

        this.onConnectionClosedCallback = onConnectionClosedCallback;
    }

    @Override
    public void onAuthenticationFailed(IotHubException e)
    {
        this.protonJExceptionParser = new ProtonJExceptionParser(e);
    }
}
