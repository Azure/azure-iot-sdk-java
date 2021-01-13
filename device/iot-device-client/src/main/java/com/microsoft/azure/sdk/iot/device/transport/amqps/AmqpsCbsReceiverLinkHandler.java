// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Every SAS token based authentication over AMQP requires a CBS session with a sender and receiver link. This
 * class defines the receiver link which receives authentication status codes corresponding to each authentication attempt.
 */
@Slf4j
public final class AmqpsCbsReceiverLinkHandler extends AmqpsReceiverLinkHandler
{
    private final static String APPLICATION_PROPERTY_STATUS_CODE = "status-code";
    private final static String APPLICATION_PROPERTY_STATUS_DESCRIPTION = "status-description";

    private static final String RECEIVER_LINK_ENDPOINT_PATH = "$cbs";
    private static final String RECEIVER_LINK_TAG_PREFIX = "cbs-receiver";
    private static final String LINK_TYPE = "cbs";

    // When a CBS receiver link gets a message, it has a correlation id (UUID) that can be used to map it back to whatever
    // handler is waiting on that authentication message.
    private final Map<UUID, AuthenticationMessageCallback> correlationMap = new ConcurrentHashMap<>();

    AmqpsCbsReceiverLinkHandler(Receiver receiver, AmqpsLinkStateCallback amqpsLinkStateCallback)
    {
        super(receiver, amqpsLinkStateCallback, UUID.randomUUID().toString());
        this.receiverLinkTag = RECEIVER_LINK_TAG_PREFIX;
        this.receiverLinkAddress = RECEIVER_LINK_ENDPOINT_PATH;
    }

    static String getCbsTag()
    {
        return RECEIVER_LINK_TAG_PREFIX;
    }

    @Override
    public String getLinkInstanceType()
    {
        return LINK_TYPE;
    }

    @Override
    public void onDelivery(Event event)
    {
        //Safe to cast as receiver as this callback will only ever be fired when messages are received on this receiver link
        Receiver receiverLink = (Receiver) event.getLink();
        Delivery delivery = event.getDelivery();
        log.trace("Received a message on the CBS receiver link");
        handleCBSResponseMessage(receiverLink);
        delivery.free();
    }

    private void handleCBSResponseMessage(Receiver receiver)
    {
        AmqpsMessage amqpsMessage = super.getMessageFromReceiverLink(receiver);
        if (amqpsMessage != null)
        {
            if (amqpsMessage.getApplicationProperties() != null && amqpsMessage.getProperties() != null)
            {
                Properties properties = amqpsMessage.getProperties();
                Object correlationId = properties.getCorrelationId();
                Map<String, Object> applicationProperties = amqpsMessage.getApplicationProperties().getValue();

                if (!this.correlationMap.containsKey(correlationId))
                {
                    log.error("Received cbs authentication message with no correlation id. Ignoring it...");
                    amqpsMessage.acknowledge(Released.getInstance());
                    return;
                }

                AuthenticationMessageCallback authenticationMessageCallback = this.correlationMap.remove(correlationId);
                for (Map.Entry<String, Object> entry : applicationProperties.entrySet())
                {
                    String propertyKey = entry.getKey();
                    if (propertyKey.equals(APPLICATION_PROPERTY_STATUS_CODE) && entry.getValue() instanceof Integer)
                    {
                        int authenticationResponseCode = (int) entry.getValue();

                        String statusDescription = "";
                        if (applicationProperties.containsKey(APPLICATION_PROPERTY_STATUS_DESCRIPTION))
                        {
                            statusDescription = (String) applicationProperties.get(APPLICATION_PROPERTY_STATUS_DESCRIPTION);
                        }

                        DeliveryState ackType = authenticationMessageCallback.handleAuthenticationResponseMessage(authenticationResponseCode, statusDescription, receiver.getSession().getConnection().getReactor());
                        amqpsMessage.acknowledge(ackType);
                        return;
                    }
                }
            }
            else
            {
                log.warn("Could not handle authentication message because it had no application properties or had no system properties");
            }
        }

        // By default, we can't process the message
        if (amqpsMessage != null)
        {
            amqpsMessage.acknowledge(Released.getInstance());
        }
    }

    void addAuthenticationMessageCorrelation(UUID correlationId, AuthenticationMessageCallback authenticationMessageCallback)
    {
        correlationMap.put(correlationId, authenticationMessageCallback);
    }
}
