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
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
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

@Slf4j
abstract class AmqpConnectionHandler extends ErrorLoggingBaseHandlerWithCleanup implements CbsSessionStateCallback
{
    private static final int AMQPS_PORT = 5671;
    private static final int AMQPS_WS_PORT = 443;
    private static final String WEB_SOCKET_PATH = "/$iothub/websocket";
    private static final String WEB_SOCKET_SUB_PROTOCOL = "AMQPWSB10";
    private static final String WEB_SOCKET_QUERY = "iothub-no-client-cert=true";
    private static final int MAX_MESSAGE_PAYLOAD_SIZE = 65 * 1024; //max IoT Hub cloud to device message size is 65 kb, so amqp websocket layer should buffer at most that much space

    private Exception savedException;
    private boolean connectionOpenedRemotely;
    private boolean sessionOpenedRemotely;
    protected boolean linkOpenedRemotely;
    private String connectionId;

    @Getter
    protected final String hostName;
    private TokenCredential credential;
    private AzureSasCredential sasTokenProvider;
    private String connectionString;
    private IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private ProxyOptions proxyOptions;
    private SSLContext sslContext;

    Connection connection;
    private CbsSessionHandler cbsSessionHandler;

    AmqpConnectionHandler(
        String connectionString,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("connectionString can not be null or empty");
        }

        Objects.requireNonNull(iotHubServiceClientProtocol);

        this.proxyOptions = proxyOptions;
        this.hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString).getHostName();
        this.connectionString = connectionString;

        commonConstructorSetup(iotHubServiceClientProtocol, proxyOptions, sslContext);
    }

    AmqpConnectionHandler(
        String hostName,
        AzureSasCredential sasTokenProvider,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName can not be null or empty");
        }

        Objects.requireNonNull(iotHubServiceClientProtocol);
        Objects.requireNonNull(sasTokenProvider);

        this.hostName = hostName;
        this.sasTokenProvider = sasTokenProvider;

        commonConstructorSetup(iotHubServiceClientProtocol, proxyOptions, sslContext);
    }

    AmqpConnectionHandler(
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("hostName can not be null or empty");
        }

        Objects.requireNonNull(iotHubServiceClientProtocol);
        Objects.requireNonNull(credential);

        this.hostName = hostName;
        this.credential = credential;

        commonConstructorSetup(iotHubServiceClientProtocol, proxyOptions, sslContext);
    }

    private void commonConstructorSetup(
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext; // if null, a default SSLContext will be generated for the user
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.savedException = null;
        this.connectionOpenedRemotely = false;
        this.sessionOpenedRemotely = false;
        this.linkOpenedRemotely = false;
        this.connectionId = UUID.randomUUID().toString();

        if (proxyOptions != null && this.iotHubServiceClientProtocol != IotHubServiceClientProtocol.AMQPS_WS)
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

        if (this.iotHubServiceClientProtocol == IotHubServiceClientProtocol.AMQPS_WS)
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
        if (transport != null)
        {
            if (this.iotHubServiceClientProtocol == IotHubServiceClientProtocol.AMQPS_WS)
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
        this.linkOpenedRemotely = true;
    }

    @Override
    public void onSessionRemoteOpen(Event event)
    {
        super.onSessionRemoteOpen(event);
        this.sessionOpenedRemotely = true;
    }

    @Override
    public void onConnectionRemoteOpen(Event event)
    {
        super.onConnectionRemoteOpen(event);
        this.connection = event.getConnection();

        this.connectionOpenedRemotely = true;

        // Once the connection opens, get that connection and make it create a new session that will serve as the CBS
        // session where authentication will take place.
        Session cbsSession = event.getConnection().session();

        if (this.credential != null)
        {
            this.cbsSessionHandler = new CbsSessionHandler(cbsSession, this, this.credential);
        }
        else if (this.sasTokenProvider != null)
        {
            this.cbsSessionHandler = new CbsSessionHandler(cbsSession, this, this.sasTokenProvider);
        }
        else
        {
            this.cbsSessionHandler = new CbsSessionHandler(cbsSession, this, this.connectionString);
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
    public void verifyConnectionWasOpened() throws IOException
    {
        if (this.protonJExceptionParser != null)
        {
            throw new IOException("Encountered exception during amqp connection: " + protonJExceptionParser.getError() + " with description " + protonJExceptionParser.getErrorDescription());
        }

        if (this.savedException != null)
        {
            throw new IOException("Connection failed to be established", this.savedException);
        }

        if (!this.connectionOpenedRemotely || !this.sessionOpenedRemotely || !this.linkOpenedRemotely)
        {
            throw new IOException("Amqp connection timed out waiting for service to respond");
        }
    }

    /**
     * Create Proton SslDomain object from Address using the given Ssl mode
     * @return The created Ssl domain
     */
    private SslDomain makeDomain()
    {
        SslDomain domain = Proton.sslDomain();

        try
        {
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
        }
        catch (Exception e)
        {
            this.savedException = e;
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

    @Override
    public void onAuthenticationFailed(IotHubException e)
    {
        this.savedException = e;
    }

    public boolean isOpen()
    {
        return this.connection.getLocalState() == EndpointState.ACTIVE && this.connection.getRemoteState() == EndpointState.ACTIVE;
    }

    public void closeAsync()
    {
        if (this.connection != null)
        {
            log.debug("Shutdown event occurred, closing file upload notification receiver link");
            this.connection.close();
        }

        if (this.cbsSessionHandler != null)
        {
            log.debug("Shutdown event occurred, closing file upload notification receiver link");
            this.cbsSessionHandler.close();
        }
    }
}
