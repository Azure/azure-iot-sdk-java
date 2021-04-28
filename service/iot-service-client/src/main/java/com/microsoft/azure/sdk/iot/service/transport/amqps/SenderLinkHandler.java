// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.apache.qpid.proton.reactor.FlowController;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class SenderLinkHandler extends BaseHandler
{
    private static final String API_VERSION_KEY = "com.microsoft:api-version";
    Map<Symbol, Object> amqpProperties;
    String senderLinkTag;
    String linkCorrelationId;
    String senderLinkAddress;
    Sender senderLink;
    private long nextTag = 0;

    protected final LinkStateCallback linkStateCallback;

    protected abstract String getLinkInstanceType();

    SenderLinkHandler(Sender sender, String linkCorrelationId, LinkStateCallback linkStateCallback)
    {
        this.amqpProperties = new HashMap<>();
        this.amqpProperties.put(Symbol.getSymbol(API_VERSION_KEY), TransportUtils.IOTHUB_API_VERSION);
        this.linkCorrelationId = linkCorrelationId;
        this.senderLink = sender;

        this.linkStateCallback = linkStateCallback;

        //All events that happen to this sender link will be handled in this class (onLinkRemoteOpen, for instance)
        BaseHandler.setHandler(sender, this);

        //This flow controller handles all link credit handling on our behalf
        add(new FlowController());
    }

    @Override
    public void onLinkRemoteOpen(Event event)
    {
        log.debug("{} sender link with link correlation id {} was successfully opened", getLinkInstanceType(), this.linkCorrelationId);
        this.linkStateCallback.onSenderLinkRemoteOpen();
    }

    @Override
    public void onLinkLocalOpen(Event event)
    {
        log.trace("{} sender link with link correlation id {} opened locally", getLinkInstanceType(), this.linkCorrelationId);
    }

    @Override
    public void onLinkInit(Event event)
    {
        // This function is called per sender/receiver link after it is instantiated, and before it is opened.
        // It sets some properties on that link, and then opens it.
        Link link = event.getLink();

        Target target = new Target();
        target.setAddress(this.senderLinkAddress);

        link.setTarget(target);

        link.setSenderSettleMode(SenderSettleMode.UNSETTLED);
        link.setProperties(this.amqpProperties);
        link.open();
        log.trace("Opening {} sender link with correlation id {}", this.getLinkInstanceType(), this.linkCorrelationId);
    }

    @Override
    public void onLinkRemoteClose(Event event)
    {
        Link link = event.getLink();
        if (link.getLocalState() == EndpointState.ACTIVE)
        {
            log.debug("{} sender link with link correlation id {} was closed remotely unexpectedly", getLinkInstanceType(), this.linkCorrelationId);
            link.close();
        }
        else
        {
            log.trace("Closing amqp session now that this {} sender link with link correlation id {} has closed remotely and locally", getLinkInstanceType(), linkCorrelationId);
            event.getSession().close();
        }
    }

    @Override
    public void onLinkLocalClose(Event event)
    {
        Link link = event.getLink();
        if (link.getRemoteState() == EndpointState.CLOSED)
        {
            log.trace("Closing amqp session now that this {} sender link with link correlation id {} has closed remotely and locally", getLinkInstanceType(), linkCorrelationId);
            event.getSession().close();
        }
        else
        {
            log.trace("{} sender link with correlation id {} was closed locally", this.getLinkInstanceType(), this.linkCorrelationId);
        }
    }

    void close()
    {
        if (this.senderLink.getLocalState() != EndpointState.CLOSED)
        {
            log.debug("Closing {} sender link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId);
            this.senderLink.close();
        }
    }

    int sendMessageAndGetDeliveryTag(MessageImpl protonMessage)
    {
        // Callers of this method are responsible for putting the returned delivery tag into the inProgressMessages map
        // so that this link can respond to this message being acknowledged appropriately

        //want to avoid negative delivery tags since -1 is the designated failure value
        if (this.nextTag == Integer.MAX_VALUE || this.nextTag < 0)
        {
            this.nextTag = 0;
        }
        else
        {
            this.nextTag++;
        }

        byte[] msgData = new byte[1024];
        int length;

        while (true)
        {
            try
            {
                length = protonMessage.encode(msgData, 0, msgData.length);
                break;
            }
            catch (BufferOverflowException e)
            {
                msgData = new byte[msgData.length * 2];
            }
        }

        byte[] deliveryTag = String.valueOf(this.nextTag).getBytes();

        Delivery delivery = this.senderLink.delivery(deliveryTag);
        try
        {
            log.trace("Sending {} bytes over the amqp {} sender link with link correlation id {}", length, getLinkInstanceType(), this.linkCorrelationId);
            int bytesSent = this.senderLink.send(msgData, 0, length);
            log.trace("{} bytes sent over the amqp {} sender link with link correlation id {}", bytesSent, getLinkInstanceType(), this.linkCorrelationId);

            if (bytesSent != length)
            {
                throw new IOException(String.format("Amqp send operation did not send all of the expected bytes for %s sender link with link correlation id %s, retrying to send the message", getLinkInstanceType(), this.linkCorrelationId));
            }

            boolean canAdvance = this.senderLink.advance();

            if (!canAdvance)
            {
                throw new IOException(String.format("Failed to advance the senderLink after sending a message on %s sender link with link correlation id %s, retrying to send the message", getLinkInstanceType(), this.linkCorrelationId));
            }

            log.trace("Message was sent over {} sender link with delivery tag {} and hash {}", getLinkInstanceType(), new String(deliveryTag), delivery.hashCode());
            return Integer.parseInt(new String(deliveryTag));
        }
        catch (Exception e)
        {
            log.warn("Encountered a problem while sending a message on {} sender link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId, e);
            this.senderLink.advance();
            delivery.free();
            return -1;
        }
    }
}
