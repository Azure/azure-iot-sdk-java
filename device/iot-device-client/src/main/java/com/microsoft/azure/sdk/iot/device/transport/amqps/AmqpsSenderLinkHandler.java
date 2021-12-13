// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.nio.BufferOverflowException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AmqpsSenderLinkHandler extends BaseHandler
{
    static final String VERSION_IDENTIFIER_KEY = "com.microsoft:client-version";
    private static final String API_VERSION_KEY = "com.microsoft:api-version";
    private static final String PNP_MODEL_ID_KEY = "com.microsoft:model-id";
    final Map<Integer, Message> inProgressMessages = new ConcurrentHashMap<>();
    final Map<Symbol, Object> amqpProperties;
    final String linkCorrelationId;
    String senderLinkAddress;
    final Sender senderLink;
    private long nextTag = 0;
    private final AmqpsLinkStateCallback amqpsLinkStateCallback;

    AmqpsSenderLinkHandler(Sender sender, AmqpsLinkStateCallback amqpsLinkStateCallback, String linkCorrelationId, String modelId)
    {
        this.amqpProperties = new HashMap<>();
        this.amqpProperties.put(Symbol.getSymbol(API_VERSION_KEY), TransportUtils.IOTHUB_API_VERSION);

        if (modelId != null && !modelId.isEmpty())
        {
            this.amqpProperties.put(Symbol.getSymbol(PNP_MODEL_ID_KEY), modelId);
        }

        this.linkCorrelationId = linkCorrelationId;
        this.senderLink = sender;
        this.amqpsLinkStateCallback = amqpsLinkStateCallback;

        //All events that happen to this sender link will be handled in this class (onLinkRemoteOpen, for instance)
        BaseHandler.setHandler(sender, this);
    }

    protected abstract String getLinkInstanceType();

    @Override
    public void onLinkRemoteOpen(Event event)
    {
        log.debug("{} sender link with address {} and link correlation id {} was successfully opened", getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId);
        this.amqpsLinkStateCallback.onLinkOpened(this);

        boolean hasFlowController = false;
        Iterator<Handler> children = children();
        while (children.hasNext())
        {
            hasFlowController |= children.next() instanceof LoggingFlowController;
        }

        if (!hasFlowController)
        {
            log.warn("No flow controller detected in {} link with address {} and link correlation id {}. Adding a new flow controller.", getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId);
            add(new LoggingFlowController(this.linkCorrelationId));
        }
    }

    @Override
    public void onLinkLocalOpen(Event event)
    {
        log.trace("{} sender link with address {} and link correlation id {} opened locally", getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId);
    }

    @Override
    public void onDelivery(Event event)
    {
        //Safe to cast here because this callback will only ever fire for acknowledgements received on this sender link
        Delivery delivery = event.getDelivery();

        int deliveryTag = Integer.parseInt(new String(event.getDelivery().getTag(), StandardCharsets.UTF_8));

        Message acknowledgedIotHubMessage = this.inProgressMessages.remove(deliveryTag);
        if (acknowledgedIotHubMessage == null)
        {
            log.warn("Received acknowledgement for a message with delivery tag {} that this sender did not send on {} link with address {}", deliveryTag, getLinkInstanceType(), this.senderLinkAddress);
        }
        else
        {
            this.amqpsLinkStateCallback.onMessageAcknowledged(acknowledgedIotHubMessage, deliveryTag, delivery.getRemoteState());
        }

        delivery.free();
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
        log.trace("Opening {} sender link with address {} and link correlation id {}", this.getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId);
    }

    @Override
    public void onLinkRemoteClose(Event event)
    {
        Link link = event.getLink();
        if (link.getLocalState() == EndpointState.ACTIVE)
        {
            log.debug("{} sender link with address {} and link correlation id {} was closed remotely unexpectedly", getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId);
            link.close();
            this.amqpsLinkStateCallback.onLinkClosedUnexpectedly(link.getRemoteCondition());
        }
        else
        {
            log.trace("Closing amqp session now that its {} sender link with address {} and link correlation id {} has closed remotely and locally", getLinkInstanceType(), this.senderLinkAddress, linkCorrelationId);
            event.getSession().close();
        }
    }

    @Override
    public void onLinkLocalClose(Event event)
    {
        Link link = event.getLink();
        if (link.getRemoteState() == EndpointState.CLOSED)
        {
            log.trace("Closing amqp session now that its {} sender link with address {} and link correlation id {} has closed remotely and locally", getLinkInstanceType(), this.senderLinkAddress, linkCorrelationId);
            event.getSession().close();
        }
        else
        {
            log.trace("{} sender link with address {} and link correlation id {} was closed locally", this.getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId);
        }
    }

    void close()
    {
        if (this.senderLink.getLocalState() != EndpointState.CLOSED)
        {
            log.debug("Closing {} sender link with address {} and link correlation id {}", getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId);
            this.senderLink.close();
        }
    }

    AmqpsSendResult sendMessageAndGetDeliveryTag(Message iotHubMessage)
    {
        MessageImpl protonMessage = this.iotHubMessageToProtonMessage(iotHubMessage);
        AmqpsSendResult sendResult = this.sendMessageAndGetDeliveryTag(protonMessage);
        inProgressMessages.put(sendResult.getDeliveryTag(), iotHubMessage);
        return sendResult;
    }

    AmqpsSendResult sendMessageAndGetDeliveryTag(MessageImpl protonMessage)
    {
        //Callers of this method are responsible for putting the returned delivery tag into the inProgressMessages map
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

        byte[] deliveryTag = String.valueOf(this.nextTag).getBytes(StandardCharsets.UTF_8);

        Delivery delivery = this.senderLink.delivery(deliveryTag);
        try
        {
            log.trace("Sending {} bytes over the amqp {} sender link with address {} and link correlation id {}", length, getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId);
            int bytesSent = this.senderLink.send(msgData, 0, length);

            if (bytesSent != length)
            {
                throw new ProtocolException(String.format("Amqp send operation did not send all of the expected bytes for %s sender link with link correlation id %s, retrying to send the message", getLinkInstanceType(), this.linkCorrelationId));
            }

            boolean canAdvance = this.senderLink.advance();

            if (!canAdvance)
            {
                throw new ProtocolException(String.format("Failed to advance the senderLink after sending a message on %s sender link with link correlation id %s, retrying to send the message", getLinkInstanceType(), this.linkCorrelationId));
            }

            log.trace("Message was sent over {} sender link with address {} and link correlation id {} with delivery tag {}", getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId, new String(deliveryTag, StandardCharsets.UTF_8));
            log.trace("Current link credit on {} sender link with address {} and link correlation id {} is {}", this.getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId, senderLink.getCredit());
            return new AmqpsSendResult(deliveryTag);
        }
        catch (Exception e)
        {
            log.warn("Encountered a problem while sending a message on {} sender link with address {} and link correlation id {}", getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId, e);
            this.senderLink.advance();
            delivery.free();
            return new AmqpsSendResult();
        }
    }

    MessageImpl iotHubMessageToProtonMessage(Message message)
    {
        log.trace("Converting IoT Hub message to proton message for {} sender link with address {} and link correlation id {}. IoT Hub message correlationId {}", getLinkInstanceType(), this.senderLinkAddress, this.linkCorrelationId, message.getCorrelationId());
        MessageImpl outgoingMessage = (MessageImpl) Proton.message();

        Properties properties = new Properties();
        if (message.getMessageId() != null)
        {
            properties.setMessageId(message.getMessageId());
        }

        if (message.getCorrelationId() != null)
        {
            properties.setCorrelationId(message.getCorrelationId());
        }

        if (message.getContentType() != null)
        {
            properties.setContentType(Symbol.valueOf(message.getContentType()));
        }

        if (message.getContentEncoding() != null)
        {
            properties.setContentEncoding(Symbol.valueOf(message.getContentEncoding()));
        }

        outgoingMessage.setProperties(properties);

        Map<String, Object> userProperties = new HashMap<>();
        if (message.getProperties().length > 0)
        {
            for (MessageProperty messageProperty : message.getProperties())
            {
                if (!MessageProperty.RESERVED_PROPERTY_NAMES.contains(messageProperty.getName()))
                {
                    userProperties.put(messageProperty.getName(), messageProperty.getValue());
                }
            }
        }

        if (message.getConnectionDeviceId() != null)
        {
            userProperties.put(MessageProperty.CONNECTION_DEVICE_ID, message.getConnectionDeviceId());
        }

        if (message.getConnectionModuleId() != null)
        {
            userProperties.put(MessageProperty.CONNECTION_MODULE_ID, message.getConnectionModuleId());
        }

        if (message.getCreationTimeUTC() != null)
        {
            userProperties.put(MessageProperty.IOTHUB_CREATION_TIME_UTC, message.getCreationTimeUTCString());
        }

        ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
        outgoingMessage.setApplicationProperties(applicationProperties);

        Map<Symbol, Object> messageAnnotationsMap = new HashMap<>();
        if (message.isSecurityMessage())
        {
            messageAnnotationsMap.put(Symbol.valueOf(MessageProperty.IOTHUB_SECURITY_INTERFACE_ID), MessageProperty.IOTHUB_SECURITY_INTERFACE_ID_VALUE);
        }

        if (message.getComponentName() != null && !message.getComponentName().isEmpty())
        {
            messageAnnotationsMap.put(Symbol.valueOf(MessageProperty.COMPONENT_ID), message.getComponentName());
        }

        MessageAnnotations messageAnnotations = new MessageAnnotations(messageAnnotationsMap);
        outgoingMessage.setMessageAnnotations(messageAnnotations);

        Binary binary = new Binary(message.getBytes());
        Section section = new Data(binary);
        outgoingMessage.setBody(section);
        return outgoingMessage;
    }
}
