// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.CustomLogger;
import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AmqpsDeviceOperations
{
    // Codes_SRS_AMQPSDEVICEOPERATIONS_12_032: [The class has static members for version identifier, api version keys and api version value.]
    protected static final String VERSION_IDENTIFIER_KEY = "com.microsoft:client-version";
    protected static final String API_VERSION_KEY = "com.microsoft:api-version";
    protected static final String API_VERSION_VALUE = "2016-11-14";

    protected static final String TO_KEY = "to";
    protected static final String USER_ID_KEY = "userId";
    protected static final String AMQPS_APP_PROPERTY_PREFIX = "iothub-app-";

    protected AmqpsDeviceOperationLinkState amqpsSendLinkState = AmqpsDeviceOperationLinkState.UNKNOWN;
    protected AmqpsDeviceOperationLinkState amqpsRecvLinkState = AmqpsDeviceOperationLinkState.UNKNOWN;

    Map<Symbol, Object> amqpProperties;

    protected String senderLinkTag;
    protected String receiverLinkTag;

    protected String senderLinkEndpointPath;
    protected String receiverLinkEndpointPath;

    protected String senderLinkAddress;
    protected String receiverLinkAddress;

    protected Sender senderLink;
    protected Receiver receiverLink;

    private CustomLogger logger;

    /**
     * This constructor creates an instance of device operation class and initializes member variables
     */
    AmqpsDeviceOperations()
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_001: [The constructor shall initialize amqpProperties with device client identifier and version.]
        this.amqpProperties = new HashMap<>();
        this.amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), TransportUtils.JAVA_DEVICE_CLIENT_IDENTIFIER + TransportUtils.CLIENT_VERSION);

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_002: [The constructor shall initialize sender and receiver tags with UUID string.]
        String uuidStr = UUID.randomUUID().toString();
        this.senderLinkTag = uuidStr;
        this.receiverLinkTag = uuidStr;

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_003: [The constructor shall initialize sender and receiver endpoint path members to empty string.]
        this.senderLinkEndpointPath = "";
        this.receiverLinkEndpointPath = "";

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_004: [The constructor shall initialize sender and receiver link address members to empty string.]
        this.senderLinkAddress = "";
        this.receiverLinkAddress = "";

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_005: [The constructor shall initialize sender and receiver link objects to null.]
        this.senderLink = null;
        this.receiverLink = null;

        this.amqpsSendLinkState = AmqpsDeviceOperationLinkState.CLOSED;
        this.amqpsRecvLinkState = AmqpsDeviceOperationLinkState.CLOSED;

        this.logger = new CustomLogger(this.getClass());
    }

    /**
     * Opens receiver and sender link
     * @param session The session where the links shall be created
     * @throws IllegalArgumentException if session argument is null
     * @throws TransportException if Proton throws
     */
    protected synchronized void openLinks(Session session) throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_006: [The function shall throw IllegalArgumentException if the session argument is null.]
        if (session == null)
        {
            throw new IllegalArgumentException("The session cannot be null.");
        }

        if ((this.senderLink == null) && (this.amqpsSendLinkState == AmqpsDeviceOperationLinkState.CLOSED))
        {
            try
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

            }
            catch (Exception e)
            {
                throw new TransportException(e);
            }
        }

        if ((this.receiverLink == null) && (this.amqpsRecvLinkState == AmqpsDeviceOperationLinkState.CLOSED))
        {
            try
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
            }
            catch (Exception e)
            {
                throw new TransportException(e);
            }
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Closes receiver and sender link if they are not null
     */
    protected void closeLinks()
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_011: [If the sender link is not null the function shall close it and sets it to null.]
        if (this.senderLink != null)
        {
            this.senderLink.close();
            this.senderLink = null;
        }
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_012: [If the receiver link is not null the function shall close it and sets it to null.]
        if (this.receiverLink != null)
        {
            this.receiverLink.close();
            this.receiverLink = null;
        }
        this.amqpsSendLinkState = AmqpsDeviceOperationLinkState.CLOSED;
        this.amqpsRecvLinkState = AmqpsDeviceOperationLinkState.CLOSED;

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Initializes the link's other endpoint according to its type
     * @param link The link which shall be initialize.
     * @throws IllegalArgumentException if link argument is null
     * @throws TransportException if Proton throws
     */
    protected synchronized void initLink(Link link) throws TransportException, IllegalArgumentException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        if (link == null)
        {
            throw new IllegalArgumentException("The link cannot be null.");
        }

        String linkName = link.getName();

        if (linkName.equals(this.getSenderLinkTag()))
        {
            if (this.amqpsSendLinkState == AmqpsDeviceOperationLinkState.OPENING)
            {
                try
                {
                    // Codes_SRS_AMQPSIOTHUBCONNECTION_15_043: [If the link is the Sender link, the event handler shall create a new Target (Proton) object using the sender endpoint address member variable.]
                    Target target = new Target();
                    target.setAddress(this.getSenderLinkAddress());

                    // Codes_SRS_AMQPSIOTHUBCONNECTION_15_044: [If the link is the Sender link, the event handler shall set its target to the created Target (Proton) object.]
                    link.setTarget(target);

                    // Codes_SRS_AMQPSIOTHUBCONNECTION_14_045: [If the link is the Sender link, the event handler shall set the SenderSettleMode to UNSETTLED.]
                    link.setSenderSettleMode(SenderSettleMode.UNSETTLED);
                }
                catch (Exception e)
                {
                    throw new TransportException(e);
                }
            }
        }

        if (linkName.equals(this.getReceiverLinkTag()))
        {
            if (this.amqpsRecvLinkState == AmqpsDeviceOperationLinkState.OPENING)
            {
                try
                {
                    // Codes_SRS_AMQPSIOTHUBCONNECTION_14_046: [If the link is the Receiver link, the event handler shall create a new Source (Proton) object using the receiver endpoint address member variable.]
                    Source source = new Source();
                    source.setAddress(this.getReceiverLinkAddress());

                    // Codes_SRS_AMQPSIOTHUBCONNECTION_14_047: [If the link is the Receiver link, the event handler shall set its source to the created Source (Proton) object.]
                    link.setSource(source);

                    link.setReceiverSettleMode(ReceiverSettleMode.FIRST);
                }
                catch (Exception e)
                {
                    throw new TransportException(e);
                }
            }
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Sends the given message and returns with the delivery hash
     * @param messageType The message operation type.
     * @param msgData The binary array of the bytes to send
     * @param offset The start offset to copy the bytes from
     * @param length The number of bytes to be send related to the offset
     * @param deliveryTag The unique identfier of the delivery
     * @return delivery tag
     * @throws IllegalStateException if sender link has not been initialized
     * @throws IllegalArgumentException if deliveryTag's length is 0
     */
    protected synchronized AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException
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
            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_022: [The function shall try to send the message data using the sender link with the offset and length argument.]
            this.senderLink.send(msgData, offset, length);
            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_023: [The function shall advance the sender link.]
            this.senderLink.advance();
            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_024: [The function shall set the delivery hash to the value returned by the sender link.]
            return new AmqpsSendReturnValue(true, delivery.hashCode());
        }
        catch (Exception e)
        {
            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_025: [If proton failed sending the function shall advance the sender link, release the delivery object and sets the delivery hash to -1.]
            this.senderLink.advance();
            delivery.free();
            // Codes_SRS_AMQPSDEVICEOPERATIONS_12_026: [The function shall return with the delivery hash.]
            return new AmqpsSendReturnValue(false, -1);
        }
    }

    /**
     * Reads the received buffer and handles the link
     * @param linkName The receiver link's name to read from
     * @return the received message
     * @throws IllegalArgumentException if linkName argument is empty
     * @throws TransportException if Proton throws
     */
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, TransportException
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_036: [The function shall do nothing and return null if the linkName is empty.]
        if (linkName.isEmpty())
        {
            throw new IllegalArgumentException("The linkName cannot be empty.");
        }

        if (linkName.equals(getReceiverLinkTag()))
        {
            try
            {
                if (receiverLink != null)
                {
                    // Codes_SRS_AMQPSDEVICEOPERATIONS_12_037: [The function shall create a Delivery object from the link.]
                    Delivery delivery = this.receiverLink.current();

                    if ((delivery != null) && delivery.isReadable() && !delivery.isPartial())
                    {
                        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_034: [The function shall read the full message into a buffer.]
                        int size = delivery.pending();
                        byte[] buffer = new byte[size];
                        int read = this.receiverLink.recv(buffer, 0, buffer.length);

                        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_035: [The function shall advance the receiver link.]
                        this.receiverLink.advance();

                        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_038: [The function shall create a Proton message from the received buffer and return with it.]
                        AmqpsMessage amqpsMessage = new AmqpsMessage();
                        amqpsMessage.setDelivery(delivery);
                        amqpsMessage.decode(buffer, 0, read);

                        return amqpsMessage;
                    }
                }
            }
            catch (Exception e)
            {
                throw new TransportException(e);
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
    public Boolean operationLinksOpened()
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_047: [The function shall return true if all link are opened, false otherwise.]
        return ((this.amqpsSendLinkState == AmqpsDeviceOperationLinkState.OPENED) && (this.amqpsRecvLinkState == AmqpsDeviceOperationLinkState.OPENED));
    }

    /**
     * Prototype (empty) function for operation specific implementations to identify if the given link is owned by the operation
     *
     * @param linkName the name of the link to find.
     * @return true if the link is owned by the operation, false otherwise
     */
    protected Boolean isLinkFound(String linkName)
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_046: [The prototype function shall return null.]
        return null;
    }

    /**
     * Prototype (empty) function for operation specific implementations to convert Proton message to IoTHubMessage
     *
     * @param amqpsMessage The Proton message to convert
     * @param deviceClientConfig The device client configuration
     * @return the converted message
     * @throws TransportException if conversion fails.
     */
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws TransportException
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_039: [The prototype function shall return null.]
        return null;
    }

    /**
     * Prototype (empty) function for operation specific implementations to convert IoTHubMessage to Proton message
     *
     * @param message The IoTHubMessage to convert
     * @return the converted message
     * @throws TransportException if conversion fails.
     */
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message) throws TransportException
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_040: [The prototype function shall return null.]
        return null;
    }

    /**
     * Prototype (empty) function for protected converter function
     *
     * @param protonMsg The Proton message to convert
     * @return the converted message
     * @throws TransportException if conversion fails.
     */
    protected Message protonMessageToIoTHubMessage(MessageImpl protonMsg) throws TransportException
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_041: [The prototype function shall return null.]
        return null;
    }

    /**
     * Prototype (empty) function for protected converter function
     * @param message The IoTHubMessage to convert
     * @return the converted message
     * @throws TransportException if conversion fails.
     */
    protected MessageImpl iotHubMessageToProtonMessage(com.microsoft.azure.sdk.iot.device.Message message) throws TransportException
    {
        // Codes_SRS_AMQPSDEVICEOPERATIONS_12_042: [The prototype function shall return null.]
        return null;
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
}
