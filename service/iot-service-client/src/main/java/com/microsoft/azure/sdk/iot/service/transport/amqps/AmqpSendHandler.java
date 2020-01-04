/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.DeliveryOutcome;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Tools;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.Message;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.Map;

/**
 * Instance of the QPID-Proton-J BaseHandler class to override
 * the events what are needed to handle the send operation
 * Contains and sets connection parameters (path, port, endpoint)
 * Maintains the layers of AMQP protocol (Link, Session, Connection, Transport)
 * Creates and sets SASL authentication for transport
 */
public class AmqpSendHandler extends AmqpConnectionHandler
{
    public static final String SEND_TAG = "sender";
    public static final String ENDPOINT = "/messages/devicebound";
    public static final String DEVICE_PATH_FORMAT = "/devices/%s/messages/devicebound";
    public static final String MODULE_PATH_FORMAT = "/devices/%s/modules/%s/messages/devicebound";
    private AmqpResponseVerification deliveryAcknowledgement;
    private org.apache.qpid.proton.message.Message messageToBeSent;
    private static final int expectedLinkCount = 2;
    private AmqpSendHandlerMessageSentCallback callback;

    /**
     * Constructor to set up connection parameters and initialize handshaker for transport
     *
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     */
    public AmqpSendHandler(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, AmqpSendHandlerMessageSentCallback callback)
    {
        super(hostName, userName, sasToken, iotHubServiceClientProtocol, SEND_TAG, ENDPOINT, expectedLinkCount);

        Tools.throwIfNull(callback, "Callback cannot be null");
        this.callback = callback;
    }

    /**
     * Create Proton message from deviceId and content string
     * @param deviceId The device name string
     * @param message The message to be sent
     */
    public void createProtonMessage(String deviceId, com.microsoft.azure.sdk.iot.service.Message message)
    {
        populateProtonMessage(String.format(DEVICE_PATH_FORMAT, deviceId), message);
    }

    /**
     * Create Proton message from deviceId and content string
     * @param deviceId The device name string
     * @param moduleId The device name string
     * @param message The message to be sent
     */
    public void createProtonMessage(String deviceId, String moduleId, com.microsoft.azure.sdk.iot.service.Message message)
    {
        populateProtonMessage(String.format(MODULE_PATH_FORMAT, deviceId, moduleId), message);
    }

    private void populateProtonMessage(String targetPath, com.microsoft.azure.sdk.iot.service.Message message)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_005: [The function shall create a new Message (Proton) object]
        org.apache.qpid.proton.message.Message protonMessage = Proton.message();

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_006: [The function shall set
        // the standard properties on the Proton Message object]
        Properties properties = new Properties();
        properties.setMessageId(message.getMessageId());
        properties.setTo(targetPath);
        properties.setAbsoluteExpiryTime(message.getExpiryTimeUtc());
        properties.setCorrelationId(message.getCorrelationId());
        if (message.getUserId() != null)
        {
            properties.setUserId(new Binary(message.getUserId().getBytes()));
        }
        protonMessage.setProperties(properties);

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_023: [The function shall set
        // the application properties on the Proton Message object]
        if (message.getProperties() != null && message.getProperties().size() > 0)
        {
            Map<String, Object> applicationPropertiesMap = new HashMap<>(message.getProperties().size());
            for(Map.Entry<String, String> entry : message.getProperties().entrySet())
            {
                applicationPropertiesMap.put(entry.getKey(), entry.getValue());
            }
            ApplicationProperties applicationProperties = new ApplicationProperties(applicationPropertiesMap);
            protonMessage.setApplicationProperties(applicationProperties);
        }

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_007: [The function shall create a Binary (Proton) object from the content string]
        Binary binary = new Binary(message.getBytes());
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_008: [The function shall create a data Section (Proton) object from the Binary]
        Section section = new Data(binary);
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_009: [The function shall set the Message body to the created data section]
        protonMessage.setBody(section);
        messageToBeSent = protonMessage;
    }

    /**
     * Event handler for the link flow event
     * @param event The proton event object
     */
    @Override
    public void onLinkFlow(Event event)
    {
        if (messageToBeSent != null)
        {
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_018: [The event handler shall get the Sender (Proton) object from the link]
            Sender snd = (Sender) event.getLink();
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_019: [The event handler shall encode the message and copy to the byte buffer]
            if (snd.getCredit() > 0)
            {
                byte[] msgData = new byte[1024];
                int length;
                while (true)
                {
                    try
                    {
                        length = messageToBeSent.encode(msgData, 0, msgData.length);
                        break;
                    }
                    catch (BufferOverflowException e)
                    {
                        msgData = new byte[msgData.length * 2];
                    }
                }
                // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_020: [The event handler shall set the delivery tag on the Sender (Proton) object]
                byte[] tag = String.valueOf(nextSendTag).getBytes();

                //want to avoid negative delivery tags since -1 is the designated failure value
                if (this.nextSendTag == Integer.MAX_VALUE || this.nextSendTag < 0)
                {
                    this.nextSendTag = 0;
                }
                else
                {
                    this.nextSendTag++;
                }

                Delivery dlv = snd.delivery(tag);
                // Codes_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_021: [The event handler shall send the encoded bytes]
                snd.send(msgData, 0, length);

                snd.advance();

                messageToBeSent = null;
            }
        }
    }

    @Override
    public DeliveryOutcome onMessageArrived(Message message)
    {
        // should never be called since this is a sending only connection. Any received messages should be rejected
        return DeliveryOutcome.Reject;
    }

    @Override
    public void onMessageAcknowledged(DeliveryState deliveryState)
    {
        deliveryAcknowledgement = new AmqpResponseVerification(deliveryState);
        this.callback.onMessageSent(deliveryAcknowledgement);
    }

    @Override
    public void openLinks(Session session, Map<Symbol, Object> properties)
    {
        Sender messageSender = session.sender(tag);
        messageSender.setProperties(properties);
        messageSender.open();
    }

    @Override
    public void onLinkInit(Event event)
    {
        Link link = event.getLink();
        Target t = new Target();
        t.setAddress(endpoint);
        link.setTarget(t);
    }

    public void validateConnectionWasSuccessful() throws IotHubException, IOException {
        super.validateConnectionWasSuccessful();

        if (deliveryAcknowledgement != null && deliveryAcknowledgement.getException() != null)
        {
            throw deliveryAcknowledgement.getException();
        }
    }
}
