// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class AmqpsLinksHandler
{
    protected static final String VERSION_IDENTIFIER_KEY = "com.microsoft:client-version";
    protected static final String API_VERSION_KEY = "com.microsoft:api-version";

    protected static final String TO_KEY = "to";
    protected static final String USER_ID_KEY = "userId";
    protected static final String AMQPS_APP_PROPERTY_PREFIX = "iothub-app-";

    Map<Symbol, Object> amqpProperties;

    protected String senderLinkTag;
    protected String receiverLinkTag;

    protected String linkCorrelationId;

    protected String senderLinkAddress;
    protected String receiverLinkAddress;

    protected Sender senderLink;
    protected Receiver receiverLink;

    /**
     * This constructor creates an instance of device operation class and initializes member variables
     */
    public AmqpsLinksHandler()
    {
        this.amqpProperties = new HashMap<>();
        this.amqpProperties.put(Symbol.getSymbol(API_VERSION_KEY), TransportUtils.IOTHUB_API_VERSION);

        this.linkCorrelationId = UUID.randomUUID().toString();

        this.senderLink = null;
        this.receiverLink = null;
    }

    /**
     * Opens receiver and sender link
     * @param session The session where the links shall be created
     * @throws IllegalArgumentException if session argument is null
     */
    protected synchronized void openLinks(Session session)
    {
        if (session == null)
        {
            throw new IllegalArgumentException("The session cannot be null.");
        }

        if ((this.senderLink == null) || (this.senderLink.getLocalState() == EndpointState.CLOSED))
        {
            this.senderLink = session.sender(this.getSenderLinkTag());

            this.senderLink.setProperties(this.getAmqpProperties());

            Target target = new Target();
            Source source = new Source();
            target.setAddress(this.getSenderLinkAddress());
            source.setAddress(this.getReceiverLinkAddress());
            this.senderLink.setTarget(target);
            this.senderLink.setSource(source);

            this.senderLink.open();
            this.log.debug("Opening {} sender link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId);
        }

        if ((this.receiverLink == null) || (this.receiverLink.getLocalState() == EndpointState.CLOSED))
        {
            this.receiverLink = session.receiver(this.getReceiverLinkTag());

            this.receiverLink.setProperties(this.getAmqpProperties());

            Target target = new Target();
            Source source = new Source();
            target.setAddress(this.getSenderLinkAddress());
            source.setAddress(this.getReceiverLinkAddress());
            this.receiverLink.setTarget(target);
            this.receiverLink.setSource(source);

            this.receiverLink.open();
            this.log.debug("Opening {} receiver link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId);
        }
    }

    /**
     * Closes receiver and sender link if they are not null
     */
    protected void closeLinks()
    {
        if (this.senderLink != null)
        {
            this.log.debug("Closing {} sender link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId);
            this.senderLink.close();
            this.senderLink = null;
        }
        else
        {
            this.log.trace("Sender link was already closed, so nothing was done to the link");
        }

        if (this.receiverLink != null)
        {
            this.log.debug("Closing {} receiver link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId);
            this.receiverLink.close();
            this.receiverLink = null;
        }
        else
        {
            this.log.trace("Receiver link was already closed, so nothing was done to the link");
        }
    }

    /**
     * Initializes the link's other endpoint according to its type
     * @param link The link which shall be initialize.
     */
    protected synchronized void initLink(Link link)
    {
        if (link == null)
        {
            throw new IllegalArgumentException("The link cannot be null.");
        }

        String linkName = link.getName();

        if (link.equals(this.senderLink))
        {
            Target target = new Target();
            target.setAddress(this.getSenderLinkAddress());

            link.setTarget(target);

            link.setSenderSettleMode(SenderSettleMode.UNSETTLED);

            this.log.trace("Initializing sender link with correlation id {}", this.linkCorrelationId);
        }
        if (link.equals(this.receiverLink))
        {
            Source source = new Source();
            source.setAddress(this.getReceiverLinkAddress());

            link.setSource(source);

            link.setReceiverSettleMode(ReceiverSettleMode.FIRST);

            this.log.trace("Initializing receiver link with correlation id {}", this.linkCorrelationId);
        }
        else
        {
            this.log.trace("InitLink called, but no link names matched {} or the link was not opening yet", linkName);
        }
    }

    /**
     * Sends the given message and returns with the delivery hash
     * @param messageType The message operation type.
     * @param msgData The binary array of the bytes to send
     * @param offset The start offset to copy the bytes from
     * @param length The number of bytes to be send related to the offset
     * @param deliveryTag The unique identifier of the delivery
     * @return delivery tag
     */
    protected synchronized AmqpsSendReturnValue sendMessageAndGetDeliveryTag(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag)
    {
        if (this.senderLink == null)
        {
            return new AmqpsSendReturnValue(false, -1);
        }

        if (deliveryTag.length == 0)
        {
            return new AmqpsSendReturnValue(false, -1);
        }

        if (this.senderLink.getLocalState() != EndpointState.ACTIVE || this.senderLink.getRemoteState() != EndpointState.ACTIVE)
        {
            return new AmqpsSendReturnValue(false, -1);
        }

        Delivery delivery = this.senderLink.delivery(deliveryTag);
        try
        {
            this.log.trace("Sending {} bytes over the amqp {} sender link with link correlation id {}", length, getLinkInstanceType(), this.linkCorrelationId);
            int bytesSent = this.senderLink.send(msgData, offset, length);
            this.log.trace("{} bytes sent over the amqp {} sender link with link correlation id {}", bytesSent, getLinkInstanceType(), this.linkCorrelationId);

            if (bytesSent != length)
            {
                ProtocolException amqpSendFailedException = new ProtocolException(String.format("Amqp send operation did not send all of the expected bytes for %s sender link with link correlation id %s, retrying to send the message", getLinkInstanceType(), this.linkCorrelationId));
                throw amqpSendFailedException;
            }

            boolean canAdvance = this.senderLink.advance();

            if (!canAdvance)
            {
                ProtocolException amqpSendFailedException = new ProtocolException(String.format("Failed to advance the senderLink after sending a message on %s sender link with link correlation id %s, retrying to send the message", getLinkInstanceType(), this.linkCorrelationId));
                throw amqpSendFailedException;
            }

            this.log.trace("Message was sent over {} sender link with delivery tag {} and hash {}", getLinkInstanceType(), new String(deliveryTag), delivery.hashCode());
            return new AmqpsSendReturnValue(true, delivery.hashCode(), deliveryTag);
        }
        catch (Exception e)
        {
            this.log.warn("Encountered a problem while sending a message on {} sender link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId, e);
            this.senderLink.advance();
            delivery.free();
            return new AmqpsSendReturnValue(false, -1);
        }
    }

    public abstract String getLinkInstanceType();

    /**
     * Reads the received buffer and handles the link
     * @param linkName The receiver link's name to read from
     * @return the received message
     */
    protected AmqpsMessage getMessageFromReceiverLink(String linkName)
    {
        if (linkName.isEmpty())
        {
            throw new IllegalArgumentException("The linkName cannot be empty.");
        }

        if (linkName.equals(getReceiverLinkTag()) && receiverLink != null)
        {
            Delivery delivery = this.receiverLink.current();

            if ((delivery != null) && delivery.isReadable() && !delivery.isPartial())
            {
                int size = delivery.pending();
                byte[] buffer = new byte[size];
                int bytesRead = this.receiverLink.recv(buffer, 0, buffer.length);
                this.log.trace("read {} bytes from receiver link {}", bytesRead, this.receiverLinkTag);

                boolean receiverLinkAdvanced = this.receiverLink.advance();

                if (!receiverLinkAdvanced)
                {
                    this.log.warn("{} receiver link with link correlation id {} did not advance after bytes were read from it", getLinkInstanceType(), this.linkCorrelationId);
                }

                if (size != bytesRead)
                {
                    log.warn("Amqp read from {} receiver link with link correlation id {} did not read the expected amount of bytes. Read {} but expected {}", getLinkInstanceType(), this.linkCorrelationId, bytesRead, size);
                }

                AmqpsMessage amqpsMessage = new AmqpsMessage();
                amqpsMessage.setDelivery(delivery);
                amqpsMessage.decode(buffer, 0, bytesRead);

                return amqpsMessage;
            }
        }

        return null;
    }

    /**
     * Identify if the given link is owned by the operation
     *
     * @param link The link that opened remotely.
     * @return true if the link is owned by the operation, false otherwise
     */
    protected boolean onLinkRemoteOpen(Link link)
    {
        if (link.equals(this.senderLink))
        {
            this.log.debug("{} sender link with link correlation id {} was successfully opened", getLinkInstanceType(), this.linkCorrelationId);
            return true;
        }

        if (link.equals(this.receiverLink))
        {
            this.log.debug("{} receiver link with link correlation id {} was successfully opened", getLinkInstanceType(), this.linkCorrelationId);
            return true;
        }

        return false;
    }

    /**
     * Convert a proton message to an IotHub transport message
     *
     * @param protonMsg The Proton message to convert
     * @param deviceClientConfig the config of the device that is sending this message
     * @return the converted message
     */
    protected IotHubTransportMessage protonMessageToIoTHubMessage(AmqpsMessage protonMsg, DeviceClientConfig deviceClientConfig)
    {
        this.log.trace("Converting proton message to iot hub message for {} receiver link with link correlation id {}. Proton message correlation id {}", getLinkInstanceType(), this.linkCorrelationId, protonMsg.getCorrelationId());
        byte[] msgBody;
        Data d = (Data) protonMsg.getBody();
        if (d != null)
        {
            Binary b = d.getValue();
            msgBody = new byte[b.getLength()];
            ByteBuffer buffer = b.asByteBuffer();
            buffer.get(msgBody);
        }
        else
        {
            msgBody = new byte[0];
        }

        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(msgBody, MessageType.UNKNOWN);

        Properties properties = protonMsg.getProperties();
        if (properties != null)
        {
            if (properties.getCorrelationId() != null)
            {
                iotHubTransportMessage.setCorrelationId(properties.getCorrelationId().toString());
            }

            if (properties.getMessageId() != null)
            {
                iotHubTransportMessage.setMessageId(properties.getMessageId().toString());
            }

            if (properties.getTo() != null)
            {
                iotHubTransportMessage.setProperty(AMQPS_APP_PROPERTY_PREFIX + TO_KEY, properties.getTo());
            }

            if (properties.getUserId() != null)
            {
                iotHubTransportMessage.setProperty(AMQPS_APP_PROPERTY_PREFIX + USER_ID_KEY, properties.getUserId().toString());
            }

            if (properties.getContentEncoding() != null)
            {
                iotHubTransportMessage.setContentEncoding(properties.getContentEncoding().toString());
            }

            if (properties.getContentType() != null)
            {
                iotHubTransportMessage.setContentType(properties.getContentType().toString());
            }
        }

        if (protonMsg.getApplicationProperties() != null)
        {
            Map<String, Object> applicationProperties = protonMsg.getApplicationProperties().getValue();
            for (Map.Entry<String, Object> entry : applicationProperties.entrySet())
            {
                String propertyKey = entry.getKey();
                if (propertyKey.equalsIgnoreCase(MessageProperty.CONNECTION_DEVICE_ID))
                {
                    iotHubTransportMessage.setConnectionDeviceId(entry.getValue().toString());
                }
                else if (propertyKey.equalsIgnoreCase(MessageProperty.CONNECTION_MODULE_ID))
                {
                    iotHubTransportMessage.setConnectionModuleId(entry.getValue().toString());
                }
                else if (!MessageProperty.RESERVED_PROPERTY_NAMES.contains(propertyKey))
                {
                    iotHubTransportMessage.setProperty(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        return iotHubTransportMessage;
    }

    /**
     * Converts an iothub message to a proton message
     * @param message The IoTHubMessage to convert
     * @return the converted message
     */
    protected MessageImpl iotHubMessageToProtonMessage(Message message)
    {
        this.log.trace("Converting IoT Hub message to proton message for {} sender link with link correlation id {}. IoT Hub message correlationId {}", getLinkInstanceType(), this.linkCorrelationId, message.getCorrelationId());
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
            for(MessageProperty messageProperty : message.getProperties())
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

        if (message.isSecurityMessage())
        {
            userProperties.put(MessageProperty.IOTHUB_SECURITY_INTERFACE_ID, MessageProperty.IOTHUB_SECURITY_INTERFACE_ID_VALUE);
        }

        ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
        outgoingMessage.setApplicationProperties(applicationProperties);

        Binary binary = new Binary(message.getBytes());
        Section section = new Data(binary);
        outgoingMessage.setBody(section);
        return outgoingMessage;
    }

    /**
     * Getter for the AmqpsProperties map
     * @return Map of AmqpsProperties of the given operation
     */
    Map<Symbol, Object> getAmqpProperties()
    {
        return this.amqpProperties;
    }

    /**
     * Getter for the SenderLinkTag string
     * @return String od SenderLinkTag of the given operation
     */
    String getSenderLinkTag()
    {
        return this.senderLinkTag;
    }

    /**
     * Getter for the ReceiverLinkTag string
     * @return String od ReceiverLinkTag of the given operation
     */
    String getReceiverLinkTag()
    {
        return this.receiverLinkTag;
    }

    /**
     * Getter for the SenderLinkAddress string
     * @return String od SenderLinkAddress of the given operation
     */
    String getSenderLinkAddress()
    {
        return this.senderLinkAddress;
    }

    /**
     * Getter for the ReceiverLinkAddress string
     * @return String od ReceiverLinkAddress of the given operation
     */
    String getReceiverLinkAddress()
    {
        return this.receiverLinkAddress;
    }

    boolean onLinkRemoteClose(String linkName)
    {
        if (linkName.equals(this.getSenderLinkTag()))
        {
            this.log.debug("{} sender link with link correlation id {} was closed", getLinkInstanceType(), this.linkCorrelationId);
            return true;
        }

        if (linkName.equals(this.getReceiverLinkTag()))
        {
            this.log.debug("{} receiver link with link correlation id {} was closed", getLinkInstanceType(), this.linkCorrelationId);
            return true;
        }

        return false;
    }

    public boolean isOpen()
    {
        return this.senderLink != null && this.senderLink.getRemoteState() == EndpointState.ACTIVE && this.senderLink.getLocalState() == EndpointState.ACTIVE
                && this.receiverLink != null && this.receiverLink.getRemoteState() == EndpointState.ACTIVE && this.receiverLink.getLocalState() == EndpointState.ACTIVE;
    }

    public boolean hasLink(Link link)
    {
        return link.equals(this.senderLink) || link.equals(this.receiverLink);
    }
}
