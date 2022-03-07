/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;

/**
 * Base Handler that logs connection/session/link opening. It also has logic to tear down the connection/session/link and
 * stop the reactor if the connection, session, and/or link is closed remotely. This base handler assumes a single link
 * in a single session in a single connection.
 */
@Slf4j
public class ErrorLoggingBaseHandlerWithCleanup extends ErrorLoggingBaseHandler
{
    @Override
    public void onLinkRemoteClose(Event event)
    {
        super.onLinkRemoteClose(event);

        if (event.getLink().getLocalState() == EndpointState.ACTIVE)
        {
            log.debug("Closing amqp link locally since amqp link was closed remotely");
            event.getLink().close();
        }
    }

    @Override
    public void onSessionRemoteClose(Event event)
    {
        super.onSessionRemoteClose(event);

        if (event.getSession().getLocalState() == EndpointState.ACTIVE)
        {
            log.debug("Closing amqp session locally since amqp session was closed remotely");
            event.getSession().close();
        }
    }

    @Override
    public void onConnectionRemoteClose(Event event)
    {
        super.onConnectionRemoteClose(event);

        if (event.getConnection().getLocalState() == EndpointState.CLOSED)
        {
            log.trace("Stopping reactor now that connection is closed locally and remotely");
            event.getReactor().stop();
        }
        else
        {
            log.debug("Closing amqp connection locally since amqp connection was closed remotely");
            event.getConnection().close();
        }
    }

    @Override
    public void onTransportError(Event event)
    {
        super.onTransportError(event);

        if (event.getConnection() != null)
        {
            log.debug("Closing amqp connection locally since amqp transport error was thrown");
            event.getConnection().close();
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event)
    {
        super.onLinkRemoteOpen(event);
        log.debug("Amqp Link with name {} opened remotely", event.getLink().getName());
    }

    @Override
    public void onConnectionRemoteOpen(Event event)
    {
        super.onConnectionRemoteOpen(event);
        log.debug("Amqp Connection opened remotely");
    }

    @Override
    public void onSessionRemoteOpen(Event event)
    {
        super.onSessionRemoteOpen(event);
        log.debug("Amqp Session opened remotely");
    }

    @Override
    public void onLinkLocalOpen(Event event)
    {
        super.onLinkLocalOpen(event);
        log.trace("Amqp Link with name {} opened locally", event.getLink().getName());
    }

    @Override
    public void onConnectionLocalOpen(Event event)
    {
        super.onConnectionLocalOpen(event);
        log.trace("Amqp Connection opened locally");
    }

    @Override
    public void onSessionLocalOpen(Event event)
    {
        super.onSessionLocalOpen(event);
        log.trace("Amqp Session opened locally");
    }

    @Override
    public void onLinkLocalClose(Event event)
    {
        super.onLinkLocalClose(event);

        //Reactor should respond to this session close with onSessionLocalClose call, where we close the connection.
        // Cannot close the connection here because it can only be done when all of its sessions have closed
        log.debug("Closing amqp session locally since amqp link was closed locally");
        event.getSession().close();
    }

    @Override
    public void onSessionLocalClose(Event event)
    {
        super.onSessionLocalClose(event);

        //Reactor should respond to this connection close with onConnectionLocalClose call, where we close the reactor.
        // Cannot close the reactor here because it can only be done when all of its connections have closed
        log.debug("Closing amqp connection locally since amqp session was closed locally");
        event.getConnection().close();
    }

    @Override
    public void onConnectionLocalClose(Event event)
    {
        super.onConnectionLocalClose(event);

        if (event.getConnection().getRemoteState() == EndpointState.CLOSED)
        {
            //Only stop the reactor once the connection is closed locally and remotely
            log.trace("Stopping reactor now that amqp connection is closed locally and remotely");
            event.getReactor().stop();
        }
    }
}
