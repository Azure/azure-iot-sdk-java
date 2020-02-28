/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.ErrorLoggingBaseHandlerWithCleanup;
import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Tools;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;

import java.io.IOException;

public class AmqpConnectionHandler extends ErrorLoggingBaseHandlerWithCleanup
{
    public static final String WEBSOCKET_PATH = "/$iothub/websocket";
    public static final String WEBSOCKET_SUB_PROTOCOL = "AMQPWSB10";
    public static final String AMQPS_PORT = ":5671";
    public static final String AMQPS_WS_PORT = ":443";

    protected Exception savedException;
    protected boolean connectionOpenedRemotely;
    protected boolean sessionOpenedRemotely;
    protected boolean linkOpenedRemotely;

    protected final String hostName;
    protected final String userName;
    protected final String sasToken;
    protected final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    protected final String webSocketHostName;

    protected AmqpConnectionHandler(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName can not be null or empty");
        }
        if (Tools.isNullOrEmpty(userName))
        {
            throw new IllegalArgumentException("userName can not be null or empty");
        }
        if (Tools.isNullOrEmpty(sasToken))
        {
            throw new IllegalArgumentException("sasToken can not be null or empty");
        }

        if (iotHubServiceClientProtocol == null)
        {
            throw new IllegalArgumentException("iotHubServiceClientProtocol cannot be null");
        }

        this.savedException = null;
        this.connectionOpenedRemotely = false;
        this.sessionOpenedRemotely = false;
        this.linkOpenedRemotely = false;

        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.webSocketHostName = hostName;
        if (this.iotHubServiceClientProtocol == IotHubServiceClientProtocol.AMQPS_WS)
        {
            this.hostName = hostName + AMQPS_WS_PORT;
        }
        else
        {
            this.hostName = hostName + AMQPS_PORT;
        }

        this.userName = userName;
        this.sasToken = sasToken;
    }

    @Override
    public void onReactorInit(Event event)
    {
        event.getReactor().connection(this);
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
                WebSocketImpl webSocket = new WebSocketImpl();
                webSocket.configure(this.webSocketHostName, WEBSOCKET_PATH, 0, WEBSOCKET_SUB_PROTOCOL, null, null);
                ((TransportInternal)transport).addTransportLayer(webSocket);
            }
            Sasl sasl = transport.sasl();
            sasl.plain(this.userName, this.sasToken);

            SslDomain domain = makeDomain(SslDomain.Mode.CLIENT);
            domain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
            Ssl ssl = transport.ssl(domain);
        }
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
        this.connectionOpenedRemotely = true;
    }

    /**
     * If an exception was encountered while opening the AMQP connection, this function shall throw that saved exception
     * @throws IOException if an exception was encountered while openinging the AMQP connection. The encountered
     * exception will be the inner exception
     */
    protected void verifyConnectionWasOpened() throws IOException
    {
        if (this.protonJExceptionParser != null)
        {
            throw new IOException("Encountered exception during amqp connection: " + protonJExceptionParser.getError() + " with description " + protonJExceptionParser.getErrorDescription());
        }

        if (this.savedException != null)
        {
            throw new IOException("Connection failed to be established", this.savedException);
        }

        if (!this.connectionOpenedRemotely)
        {
            throw new IOException("Amqp Connection failed to open");
        }

        if (!this.sessionOpenedRemotely)
        {
            throw new IOException("Amqp Session failed to open");
        }

        if (!this.linkOpenedRemotely)
        {
            throw new IOException("Amqp link never opened");
        }
    }

    /**
     * Create Proton SslDomain object from Address using the given Ssl mode
     * @param mode The proton enum value of requested Ssl mode
     * @return The created Ssl domain
     */
    private SslDomain makeDomain(SslDomain.Mode mode)
    {
        SslDomain domain = Proton.sslDomain();

        try
        {
            // Need the base trusted certs for IotHub in our ssl context. IotHubSSLContext handles that
            domain.setSslContext(new IotHubSSLContext().getSSLContext());
        }
        catch (Exception e)
        {
            this.savedException = e;
        }

        domain.init(mode);

        return domain;
    }
}
