/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.transport.amqp;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;

@Slf4j
public class ErrorLoggingBaseHandler extends BaseHandler
{
    protected ProtonJExceptionParser protonJExceptionParser;

    @Override
    public void onLinkRemoteClose(Event event)
    {
        protonJExceptionParser = new ProtonJExceptionParser(event);
        if (protonJExceptionParser.getError() == null)
        {
            log.debug("Amqp link {} was closed remotely", event.getLink().getName());
        }
        else
        {
            if (event.getLink() != null && event.getLink().getName() != null)
            {
                log.warn("Amqp link {} was closed remotely with exception {} with description {}", event.getLink().getName(), protonJExceptionParser.getError(), protonJExceptionParser.getErrorDescription());
            }
            else
            {
                log.warn("Unknown amqp link was closed remotely with exception {} with description {}", protonJExceptionParser.getError(), protonJExceptionParser.getErrorDescription());
            }
        }
    }

    @Override
    public void onSessionRemoteClose(Event event)
    {
        protonJExceptionParser = new ProtonJExceptionParser(event);
        if (protonJExceptionParser.getError() == null)
        {
            log.warn("Amqp session was closed remotely with an unknown exception");
        }
        else
        {
            log.warn("Amqp session was closed remotely with exception {} with description {}", protonJExceptionParser.getError(), protonJExceptionParser.getErrorDescription());
        }
    }

    @Override
    public void onConnectionRemoteClose(Event event)
    {
        protonJExceptionParser = new ProtonJExceptionParser(event);
        if (protonJExceptionParser.getError() == null)
        {
            log.warn("Amqp connection was closed remotely with an unknown exception");
        }
        else
        {
            log.warn("Amqp connection was closed remotely with exception {} with description {}", protonJExceptionParser.getError(), protonJExceptionParser.getErrorDescription());
        }
    }

    @Override
    public void onTransportError(Event event)
    {
        protonJExceptionParser = new ProtonJExceptionParser(event);
        if (protonJExceptionParser.getError() == null)
        {
            log.warn("Amqp transport closed with an unknown exception");
        }
        else
        {
            log.warn("Amqp transport closed due to exception {} with description {}", protonJExceptionParser.getError(), protonJExceptionParser.getErrorDescription());
        }
    }
}
