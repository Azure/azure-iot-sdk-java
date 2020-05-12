// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
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
public abstract class AmqpsDeviceOperations
{
    // Codes_SRS_AMQPSDEVICEOPERATIONS_12_032: [The class has static members for version identifier and api version keys.]
    protected static final String VERSION_IDENTIFIER_KEY = "com.microsoft:client-version";
    protected static final String API_VERSION_KEY = "com.microsoft:api-version";

    protected static final String TO_KEY = "to";
    protected static final String USER_ID_KEY = "userId";
    protected static final String AMQPS_APP_PROPERTY_PREFIX = "iothub-app-";
    protected static final String INPUT_NAME_PROPERTY_KEY = "x-opt-input-name";

    protected AmqpsDeviceOperationLinkState amqpsSendLinkState = AmqpsDeviceOperationLinkState.UNKNOWN;
    protected AmqpsDeviceOperationLinkState amqpsRecvLinkState = AmqpsDeviceOperationLinkState.UNKNOWN;

    Map<Symbol, Object> amqpProperties;

    protected String senderLinkTag;
    protected String receiverLinkTag;
    protected String linkCorrelationId;

    protected String senderLinkEndpointPath;
    protected String receiverLinkEndpointPath;

    protected String senderLinkAddress;
    protected String receiverLinkAddress;

    protected Sender senderLink;
    protected Receiver receiverLink;

    /**
     *
     * @param deviceClientConfig the config to pull the user agent string from
     *
     * @throws IllegalArgumentException if the provided deviceClientConfig is null
     */
    /**
     * This constructor creates an instance of device operation class and initializes member variables
     *
     * @param deviceClientConfig the config to pull the user agent string from
     * @param senderLinkEndpointPath the sender link path to use for device clients
     * @param receiverLinkEndpointpath the receiver link path to use for device clients
     * @param senderLinkEndpointPathModules the sender link path to use for module clients
     * @param receiverLinkEndpointPathModules the receiver link path to use for module clients
     * @param senderLinkTagPrefix the prefix for the sender links
     * @param receiverLinkTagPrefix the prefix for the receiver links
     */
    public AmqpsDeviceOperations(DeviceClientConfig deviceClientConfig, String senderLinkEndpointPath, String receiverLinkEndpointpath,
                          String senderLinkEndpointPathModules, String receiverLinkEndpointPathModules,
                          String senderLinkTagPrefix, String receiverLinkTagPrefix)
    {
        if (deviceClientConfig == null)
        {
            // Codes_SRS_AMQPSDEVICEOPERATIONS_34_048: [If the provided deviceClientConfig is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("device config cannot be null");
        }

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_001: [The constructor shall initialize amqpProperties with device client identifier and version.]
        this.amqpProperties = new HashMap<>();
        this.amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), deviceClientConfig.getProductInfo().getUserAgentString());

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_002: [The constructor shall initialize sender and receiver tags with UUID string.]
        this.linkCorrelationId = UUID.randomUUID().toString();

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_005: [The constructor shall initialize sender and receiver link objects to null.]
        this.senderLink = null;
        this.receiverLink = null;

        this.amqpsSendLinkState = AmqpsDeviceOperationLinkState.CLOSED;
        this.amqpsRecvLinkState = AmqpsDeviceOperationLinkState.CLOSED;

        String moduleId = deviceClientConfig.getModuleId();
        String deviceId = deviceClientConfig.getDeviceId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            this.senderLinkEndpointPath = senderLinkEndpointPathModules;
            this.receiverLinkEndpointPath = receiverLinkEndpointPathModules;

            this.senderLinkTag = senderLinkTagPrefix + deviceId + "/" + moduleId + "-" + this.linkCorrelationId;
            this.receiverLinkTag = receiverLinkTagPrefix + deviceId + "/" + moduleId + "-" + this.linkCorrelationId;

            this.senderLinkAddress = String.format(this.senderLinkEndpointPath, deviceId, moduleId);
            this.receiverLinkAddress = String.format(receiverLinkEndpointPath, deviceId, moduleId);
        }
        else
        {
            this.senderLinkEndpointPath = senderLinkEndpointPath;
            this.receiverLinkEndpointPath = receiverLinkEndpointpath;

            this.senderLinkTag = senderLinkTagPrefix + deviceId + "-" + this.linkCorrelationId;
            this.receiverLinkTag = receiverLinkTagPrefix + deviceId + "-" + this.linkCorrelationId;

            this.senderLinkAddress = String.format(this.senderLinkEndpointPath, deviceId);
            this.receiverLinkAddress = String.format(receiverLinkEndpointPath, deviceId);
        }
    }

    /**
     * Opens receiver and sender link
     * @param session The session where the links shall be created
     * @throws IllegalArgumentException if session argument is null
     */
    protected synchronized void openLinks(Session session)
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_006: [The function shall throw IllegalArgumentException if the session argument is null.]
        if (session == null)
        {
            throw new IllegalArgumentException("The session cannot be null.");
        }

        if ((this.senderLink == null) && (this.amqpsSendLinkState == AmqpsDeviceOperationLinkState.CLOSED))
        {
            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_008: [**The function shall create sender link with the senderlinkTag member value.]
            this.senderLink = session.sender(this.getSenderLinkTag());

            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_009: [The function shall set both receiver and sender link properties to the amqpProperties member value.]
            this.senderLink.setProperties(this.getAmqpProperties());

            Target target = new Target();
            Source source = new Source();
            target.setAddress(this.getSenderLinkAddress());
            source.setAddress(this.getReceiverLinkAddress());
            this.senderLink.setTarget(target);
            this.senderLink.setSource(source);

            this.amqpsSendLinkState = AmqpsDeviceOperationLinkState.OPENING;

            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_010: [The function^ shall onConnectionInit both receiver and sender link.]
            this.senderLink.open();
            this.log.debug("Opening {} sender link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId);
        }

        if ((this.receiverLink == null) && (this.amqpsRecvLinkState == AmqpsDeviceOperationLinkState.CLOSED))
        {
            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_007: [The function shall create receiver link with the receiverlinkTag member value.]
            this.receiverLink = session.receiver(this.getReceiverLinkTag());

            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_009: [The function shall set both receiver and sender link properties to the amqpProperties member value.]
            this.receiverLink.setProperties(this.getAmqpProperties());

            Target target = new Target();
            Source source = new Source();
            target.setAddress(this.getSenderLinkAddress());
            source.setAddress(this.getReceiverLinkAddress());
            this.receiverLink.setTarget(target);
            this.receiverLink.setSource(source);

            this.amqpsRecvLinkState = AmqpsDeviceOperationLinkState.OPENING;

            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_010: [The function shall onConnectionInit both receiver and sender link.]
            this.receiverLink.open();
            this.log.debug("Opening {} receiver link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId);
        }
    }

    /**
     * Closes receiver and sender link if they are not null
     */
    protected void closeLinks()
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_011: [If the sender link is not null the function shall close it and sets it to null.]
        if (this.senderLink != null)
        {
            this.senderLink.close();
            this.senderLink = null;
            this.log.debug("Closed {} sender link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId);
        }
        else
        {
            this.log.trace("Sender link was already closed, so nothing was done to the link");
        }

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_012: [If the receiver link is not null the function shall close it and sets it to null.]
        if (this.receiverLink != null)
        {
            this.receiverLink.close();
            this.receiverLink = null;
            this.log.debug("Closed {} receiver link with link correlation id {}", getLinkInstanceType(), this.linkCorrelationId);
        }
        else
        {
            this.log.trace("Receiver link was already closed, so nothing was done to the link");
        }

        this.amqpsSendLinkState = AmqpsDeviceOperationLinkState.CLOSED;
        this.amqpsRecvLinkState = AmqpsDeviceOperationLinkState.CLOSED;
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

        if (linkName.equals(this.getSenderLinkTag()) && this.amqpsSendLinkState == AmqpsDeviceOperationLinkState.OPENING)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_043: [If the link is the Sender link, the event handler shall create a new Target (Proton) object using the sender endpoint address member variable.]
            Target target = new Target();
            target.setAddress(this.getSenderLinkAddress());

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_044: [If the link is the Sender link, the event handler shall set its target to the created Target (Proton) object.]
            link.setTarget(target);

            // Codes_SRS_AMQPSIOTHUBCONNECTION_14_045: [If the link is the Sender link, the event handler shall set the SenderSettleMode to UNSETTLED.]
            link.setSenderSettleMode(SenderSettleMode.UNSETTLED);

            this.log.trace("Initializing sender link with correlation id {}", this.linkCorrelationId);
        }
        else if (linkName.equals(this.getReceiverLinkTag()) && this.amqpsRecvLinkState == AmqpsDeviceOperationLinkState.OPENING)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_14_046: [If the link is the Receiver link, the event handler shall create a new Source (Proton) object using the receiver endpoint address member variable.]
            Source source = new Source();
            source.setAddress(this.getReceiverLinkAddress());

            // Codes_SRS_AMQPSIOTHUBCONNECTION_14_047: [If the link is the Receiver link, the event handler shall set its source to the created Source (Proton) object.]
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
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_019: [The function shall throw IllegalStateException if the sender link is not initialized.]
        if (this.senderLink == null)
        {
            throw new IllegalStateException("Trying to send but Sender link is not initialized.");
        }

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_020: [The function shall throw IllegalArgumentException if the deliveryTag length is zero.]
        if (deliveryTag.length == 0)
        {
            throw new IllegalArgumentException("Trying deliveryTag cannot be null.");
        }

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_021: [The function shall create a Delivery object using the sender link and the deliveryTag.]
        Delivery delivery = this.senderLink.delivery(deliveryTag);
        try
        {
            this.log.trace("Sending {} bytes over the amqp {} sender link with link correlation id {}", length, getLinkInstanceType(), this.linkCorrelationId);
            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_022: [The function shall try to send the message data using the sender link with the offset and length argument.]
            int bytesSent = this.senderLink.send(msgData, offset, length);
            this.log.trace("{} bytes sent over the amqp {} sender link with link correlation id {}", bytesSent, getLinkInstanceType(), this.linkCorrelationId);

            if (bytesSent != length)
            {
                ProtocolException amqpSendFailedException = new ProtocolException(String.format("Amqp send operation did not send all of the expected bytes for %s sender link with link correlation id %s, retrying to send the message", getLinkInstanceType(), this.linkCorrelationId));
                throw amqpSendFailedException;
            }

            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_023: [The function shall advance the sender link.]
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
            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_025: [If proton failed sending the function shall advance the sender link, release the delivery object and sets the delivery hash to -1.]
            this.senderLink.advance();
            delivery.free();
            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_026: [The function shall return with the delivery hash.]
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
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_036: [The function shall do nothing and return null if the linkName is empty.]
        if (linkName.isEmpty())
        {
            throw new IllegalArgumentException("The linkName cannot be empty.");
        }

        if (linkName.equals(getReceiverLinkTag()) && receiverLink != null)
        {
            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_037: [The function shall create a Delivery object from the link.]
            Delivery delivery = this.receiverLink.current();

            if ((delivery != null) && delivery.isReadable() && !delivery.isPartial())
            {
                // Codes_SRS_AMQPSDEVICEOPERATIONS_12_034: [The function shall read the full message into a buffer.]
                int size = delivery.pending();
                byte[] buffer = new byte[size];
                int bytesRead = this.receiverLink.recv(buffer, 0, buffer.length);
                this.log.trace("read {} bytes from receiver link {}", bytesRead, this.receiverLinkTag);

                // Codes_SRS_AMQPSDEVICEOPERATIONS_12_035: [The function shall advance the receiver link.]
                boolean receiverLinkAdvanced = this.receiverLink.advance();

                if (!receiverLinkAdvanced)
                {
                    this.log.warn("{} receiver link with link correlation id {} did not advance after bytes were read from it", getLinkInstanceType(), this.linkCorrelationId);
                }

                if (size != bytesRead)
                {
                    log.warn("Amqp read from {} receiver link with link correlation id {} did not read the expected amount of bytes. Read {} but expected {}", getLinkInstanceType(), this.linkCorrelationId, bytesRead, size);
                }

                // Codes_SRS_AMQPSDEVICEOPERATIONS_12_038: [The function shall create a Proton message from the received buffer and return with it.]
                AmqpsMessage amqpsMessage = new AmqpsMessage();
                amqpsMessage.setDelivery(delivery);
                amqpsMessage.decode(buffer, 0, bytesRead);

                return amqpsMessage;
            }
        }

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_043: [The function shall return null if the linkName does not match with the receiverLink tag.]
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_033: [The function shall try to read the full message from the delivery object and if it fails return null.]
        return null;
    }

    /**
     * Gets the status of the operation's links
     * @return true if the all the operation links are opened, false otherwise
     */
    public boolean operationLinksOpened()
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_047: [The function shall return true if all link are opened, false otherwise.]
        return ((this.amqpsSendLinkState == AmqpsDeviceOperationLinkState.OPENED) && (this.amqpsRecvLinkState == AmqpsDeviceOperationLinkState.OPENED));
    }

    /**
     * Identify if the given link is owned by the operation
     *
     * @return true if the link is owned by the operation, false otherwise
     */
    protected boolean onLinkRemoteOpen(String linkName)
    {
        // Codes_SRS_AMQPSDEVICETELEMETRY_12_026: [The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.]
        if (linkName.equals(this.getSenderLinkTag()))
        {
            this.amqpsSendLinkState = AmqpsDeviceOperationLinkState.OPENED;
            this.log.debug("{} sender link with link correlation id {} was successfully opened {}", getLinkInstanceType(), this.linkCorrelationId);
            return true;
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_027: [The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.]
        if (linkName.equals(this.getReceiverLinkTag()))
        {
            this.amqpsRecvLinkState = AmqpsDeviceOperationLinkState.OPENED;
            this.log.debug("{} receiver link with link correlation id {} was successfully opened", getLinkInstanceType(), this.linkCorrelationId);
            return true;
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_028: [The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.]
        return false;
    }
    /**
     * abstract function for operation specific implementations to convert Proton message to IoTHubMessage
     *
     * @param amqpsMessage The Proton message to convert
     * @param deviceClientConfig The device client configuration
     * @throws TransportException if the conversion fails
     * @return the converted message
     */
    abstract protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws TransportException;

    /**
     * abstract function for operation specific implementations to convert IoTHubMessage to Proton message
     *
     * @param message The IoTHubMessage to convert
     * @throws TransportException if the conversion fails
     * @return the converted message
     */
    abstract protected AmqpsConvertToProtonReturnValue convertToProton(Message message) throws TransportException;

    protected MessageImpl buildSubscribeToDesiredPropertiesProtonMessage()
    {
        return null;
    }

    /**
     * Convert a proton message to an IotHub transport message
     *
     * @param protonMsg The Proton message to convert
     * @throws TransportException if the conversion fails
     * @return the converted message
     */
    protected IotHubTransportMessage protonMessageToIoTHubMessage(MessageImpl protonMsg) throws com.microsoft.azure.sdk.iot.device.exceptions.TransportException
    {
        //Codes_SRS_AMQPSDEVICEOPERATION_34_009: [The function shall create a new IoTHubMessage using the Proton message body.]
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
            // Codes_SRS_AMQPSDEVICEOPERATION_34_025: [The function shall create a new empty buffer for message body if the proton message body is null.]
            msgBody = new byte[0];
        }

        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(msgBody, MessageType.UNKNOWN);

        //Codes_SRS_AMQPSDEVICEOPERATION_34_010: [The function shall copy the correlationId, messageId, To, userId, contenty type, and content encoding properties to the IotHubMessage properties.]
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
            //Codes_SRS_AMQPSDEVICEOPERATION_34_011: [The function shall copy the Proton application properties to IoTHubMessage properties excluding the reserved property names.]
            Map<String, Object> applicationProperties = protonMsg.getApplicationProperties().getValue();
            for (Map.Entry<String, Object> entry : applicationProperties.entrySet())
            {
                String propertyKey = entry.getKey();
                if (propertyKey.equalsIgnoreCase(MessageProperty.CONNECTION_DEVICE_ID))
                {
                    //Codes_SRS_AMQPSDEVICEOPERATION_34_053: [If the amqp message contains an application property for the connection device id, this function shall assign its value to the IotHub message's connection device id.]
                    iotHubTransportMessage.setConnectionDeviceId(entry.getValue().toString());
                }
                else if (propertyKey.equalsIgnoreCase(MessageProperty.CONNECTION_MODULE_ID))
                {
                    //Codes_SRS_AMQPSDEVICEOPERATION_34_054: [If the amqp message contains an application property for the connection module id, this function shall assign its value to the IotHub message's connection module id.]
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
     * @throws TransportException if the conversion fails
     * @return the converted message
     */
    protected MessageImpl iotHubMessageToProtonMessage(Message message) throws com.microsoft.azure.sdk.iot.device.exceptions.TransportException
    {
        this.log.trace("Converting IoT Hub message to proton message for {} sender link with link correlation id {}. IoT Hub message correlationId {}", getLinkInstanceType(), this.linkCorrelationId, message.getCorrelationId());
        MessageImpl outgoingMessage = (MessageImpl) Proton.message();

        Properties properties = new Properties();
        //Codes_SRS_AMQPSDEVICEOPERATION_34_016: [The function shall copy the correlationId, messageId, content type and content encoding properties to the Proton message properties.]
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

        //Codes_SRS_AMQPSDEVICEOPERATION_34_017: [The function shall copy the user properties to Proton message application properties excluding the reserved property names.]
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
            //Codes_SRS_AMQPSDEVICEOPERATION_34_055: [This function shall set the message's saved creationTimeUTC in the application properties of the new proton message.]
            userProperties.put(MessageProperty.IOTHUB_CREATION_TIME_UTC, message.getCreationTimeUTCString());
        }

        if (message.isSecurityMessage())
        {
            userProperties.put(MessageProperty.IOTHUB_SECURITY_INTERFACE_ID, MessageProperty.IOTHUB_SECURITY_INTERFACE_ID_VALUE);
        }

        ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
        outgoingMessage.setApplicationProperties(applicationProperties);

        //Codes_SRS_AMQPSDEVICEOPERATION_34_015: [The function shall create a new Proton message using the IoTHubMessage body.]
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
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_027: [The getter shall return with the value of the amqpProperties.]
        return this.amqpProperties;
    }

    /**
     * Getter for the SenderLinkTag string
     * @return String od SenderLinkTag of the given operation
     */
    String getSenderLinkTag()
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_028: [The getter shall return with the value of the sender link tag.]
        return this.senderLinkTag;
    }

    /**
     * Getter for the ReceiverLinkTag string
     * @return String od ReceiverLinkTag of the given operation
     */
    String getReceiverLinkTag()
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_029: [The getter shall return with the value of the receiver link tag.]
        return this.receiverLinkTag;
    }

    /**
     * Getter for the SenderLinkAddress string
     * @return String od SenderLinkAddress of the given operation
     */
    String getSenderLinkAddress()
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_030: [The getter shall return with the value of the sender link address.]
        return this.senderLinkAddress;
    }

    /**
     * Getter for the ReceiverLinkAddress string
     * @return String od ReceiverLinkAddress of the given operation
     */
    String getReceiverLinkAddress()
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_031: [The getter shall return with the value of the receiver link address.]
        return this.receiverLinkAddress;
    }

    boolean onLinkRemoteClose(String linkName)
    {
        // Codes_SRS_AMQPSDEVICETELEMETRY_12_026: [The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.]
        if (linkName.equals(this.getSenderLinkTag()))
        {
            this.amqpsSendLinkState = AmqpsDeviceOperationLinkState.CLOSED;
            this.log.debug("{} sender link with link correlation id {} was closed", getLinkInstanceType(), this.linkCorrelationId);
            return true;
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_027: [The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.]
        if (linkName.equals(this.getReceiverLinkTag()))
        {
            this.amqpsRecvLinkState = AmqpsDeviceOperationLinkState.CLOSED;
            this.log.debug("{} receiver link with link correlation id {} was closed", getLinkInstanceType(), this.linkCorrelationId);
            return true;
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_028: [The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.]
        return false;
    }
}
