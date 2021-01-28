// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpsMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;

import java.util.Map;
import java.util.UUID;

/**
 * Every SAS token based authentication over AMQP requires a CBS session with a sender and receiver link. This
 * class defines the receiver link which receives authentication status codes corresponding to each authentication attempt.
 */
@Slf4j
public final class CbsReceiverLinkHandler extends ReceiverLinkHandler
{
    private final static String APPLICATION_PROPERTY_STATUS_CODE = "status-code";
    private final static String APPLICATION_PROPERTY_STATUS_DESCRIPTION = "status-description";

    private static final String RECEIVER_LINK_ENDPOINT_PATH = "$cbs";
    private static final String RECEIVER_LINK_TAG_PREFIX = "cbs-receiver";
    private static final String LINK_TYPE = "cbs";

    private UUID authenticationMessageCorrelationId;
    private final AuthenticationMessageCallback authenticationMessageCallback;

    CbsReceiverLinkHandler(Receiver receiver, AuthenticationMessageCallback authenticationMessageCallback, LinkStateCallback linkStateCallback)
    {
        super(receiver, UUID.randomUUID().toString(), linkStateCallback);
        this.receiverLinkTag = RECEIVER_LINK_TAG_PREFIX;
        this.receiverLinkAddress = RECEIVER_LINK_ENDPOINT_PATH;
        this.authenticationMessageCallback = authenticationMessageCallback;
    }

    protected void setAuthenticationMessageCorrelationId(UUID authenticationMessageCorrelationId)
    {
        this.authenticationMessageCorrelationId = authenticationMessageCorrelationId;
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
        Delivery delivery = event.getDelivery();
        log.trace("Received a message on the CBS receiver link");
        handleCBSResponseMessage();
        delivery.free();
    }

    private void handleCBSResponseMessage()
    {
        AmqpsMessage amqpsMessage = super.getMessageFromReceiverLink();
        if (amqpsMessage != null)
        {
            if (amqpsMessage.getApplicationProperties() != null && amqpsMessage.getProperties() != null)
            {
                Properties properties = amqpsMessage.getProperties();
                UUID correlationId = (UUID) properties.getCorrelationId();
                Map<String, Object> applicationProperties = amqpsMessage.getApplicationProperties().getValue();

                if (!this.authenticationMessageCorrelationId.equals(correlationId))
                {
                    log.error("Received cbs authentication message with unexpected correlation id. Ignoring it...");
                    amqpsMessage.acknowledge(Released.getInstance());
                    return;
                }

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

                        DeliveryState ackType = authenticationMessageCallback.handleAuthenticationResponseMessage(authenticationResponseCode, statusDescription);
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
        else
        {
            log.warn("Failed to read the message on the CBS receiver link");
        }

        // By default, we can't process the message
        if (amqpsMessage != null)
        {
            amqpsMessage.acknowledge(Released.getInstance());
        }
    }
}
