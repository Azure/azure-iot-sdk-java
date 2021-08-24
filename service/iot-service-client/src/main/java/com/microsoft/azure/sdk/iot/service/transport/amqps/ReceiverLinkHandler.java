// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpsMessage;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.reactor.FlowController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
abstract class ReceiverLinkHandler extends BaseHandler
{
    private static final String API_VERSION_KEY = "com.microsoft:api-version";

    // Upon opening a receiver link, the client must extend link credit to the service so that the service
    // can send messages over that link to the client. Each "link credit" corresponds to 1 service to client message.
    // Upon receiving a message over a receiving link, a credit should be refunded to the service so that
    // this initial credit doesn't run out.
    private final Map<Symbol, Object> amqpProperties;
    @SuppressWarnings("unused") // Used in sub classes for future expansion.
    String receiverLinkTag;
    private final String linkCorrelationId;
    String receiverLinkAddress;
    private final Receiver receiverLink;

    private final LinkStateCallback linkStateCallback;

    abstract String getLinkInstanceType();

    ReceiverLinkHandler(Receiver receiver, String linkCorrelationId, LinkStateCallback linkStateCallback)
    {
        this.amqpProperties = new HashMap<>();
        this.amqpProperties.put(Symbol.getSymbol(API_VERSION_KEY), TransportUtils.IOTHUB_API_VERSION);
        this.receiverLink = receiver;
        this.linkCorrelationId = linkCorrelationId;

        this.linkStateCallback = linkStateCallback;

        //All events that happen to this receiver link will be handled in this class (onLinkRemoteOpen, for instance)
        BaseHandler.setHandler(receiver, this);

        //This flow controller handles all link credit handling on our behalf
        add(new FlowController());
    }

    @Override
    public void onLinkRemoteOpen(Event event)
    {
        log.debug("{} receiver link with link correlation id {} was successfully opened", getLinkInstanceType(), this.linkCorrelationId);
        this.linkStateCallback.onReceiverLinkRemoteOpen();
    }

    @Override
    public void onLinkLocalOpen(Event event)
    {
        log.trace("{} receiver link with link correlation id {} opened locally", getLinkInstanceType(), this.linkCorrelationId);
    }

    AmqpsMessage getMessageFromReceiverLink()
    {
        Delivery delivery = receiverLink.current();
        if (delivery.isReadable() && !delivery.isPartial()) {
            int size = delivery.pending();
            byte[] buffer = new byte[size];
            int read = receiverLink.recv(buffer, 0, buffer.length);
            receiverLink.advance();

            AmqpsMessage message = new AmqpsMessage();
            message.decode(buffer, 0, read);
            message.setDelivery(delivery);

            return message;
        }

        return null;
    }

    @Override
    public void onLinkInit(Event event)
    {
        // This function is called per sender/receiver link after it is instantiated, and before it is opened.
        // It sets some properties on that link, and then opens it.
        Link link = event.getLink();

        Source source = new Source();
        source.setAddress(this.receiverLinkAddress);

        link.setSource(source);

        link.setReceiverSettleMode(ReceiverSettleMode.FIRST);
        link.setProperties(this.amqpProperties);
        link.open();
        log.trace("Opening {} receiver link with correlation id {}", this.getLinkInstanceType(), this.linkCorrelationId);
    }

    @Override
    public void onLinkRemoteClose(Event event)
    {
        Link link = event.getLink();
        if (link.getLocalState() == EndpointState.ACTIVE)
        {
            log.debug("{} receiver link with link correlation id {} was closed remotely unexpectedly", getLinkInstanceType(), this.linkCorrelationId);
            link.close();
        }
        else
        {
            log.trace("Closing amqp session now that this {} receiver link with link correlation id {} has closed remotely and locally", getLinkInstanceType(), linkCorrelationId);
            event.getSession().close();
        }
    }

    @Override
    public void onLinkLocalClose(Event event)
    {
        Link link = event.getLink();
        if (link.getRemoteState() == EndpointState.CLOSED)
        {
            log.trace("Closing amqp session now that this {} receiver link with link correlation id {} has closed remotely and locally", getLinkInstanceType(), linkCorrelationId);
            event.getSession().close();
        }
        else
        {
            log.trace("{} receiver link with correlation id {} was closed locally", this.getLinkInstanceType(), this.linkCorrelationId);
        }
    }

    void close()
    {
        if (this.receiverLink.getLocalState() != EndpointState.CLOSED)
        {
            log.debug("Closing {} receiver link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId);
            this.receiverLink.close();
        }
    }
}
