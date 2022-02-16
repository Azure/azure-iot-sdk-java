/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.messaging.SendResult;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;

import javax.net.ssl.SSLContext;
import java.nio.BufferOverflowException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Instance of the QPID-Proton-J BaseHandler class to override
 * the events what are needed to handle the send operation
 * Contains and sets connection parameters (path, port, endpoint)
 * Maintains the layers of AMQP protocol (Link, Session, Connection, Transport)
 * Creates and sets SASL authentication for transport
 */
@Slf4j
public class AmqpSendHandler extends AmqpConnectionHandler
{
    private static final String SEND_TAG = "sender";
    private static final String ENDPOINT = "/messages/devicebound";
    private static final String DEVICE_PATH_FORMAT = "/devices/%s/messages/devicebound";
    private static final String MODULE_PATH_FORMAT = "/devices/%s/modules/%s/messages/devicebound";

    private Sender cloudToDeviceMessageSendingLink;
    private Session cloudToDeviceMessageSession;

    //TODO can this be simplified? This is a lot of state
    private final Queue<org.apache.qpid.proton.message.Message> outgoingMessageQueue = new ConcurrentLinkedQueue<>();
    private final Map<Integer, org.apache.qpid.proton.message.Message> unacknowledgedMessages = new ConcurrentHashMap<>();
    private final Map<org.apache.qpid.proton.message.Message, Message> protonMessageToIotHubMessageMap = new ConcurrentHashMap<>();
    private final Map<Message, Consumer<SendResult>> iotHubMessageToCallbackMap = new ConcurrentHashMap<>();
    private final Map<Message, Object> iotHubMessageToCallbackContextMap = new ConcurrentHashMap<>();


    private int nextTag = 0;

    /**
     * Constructor to set up connection parameters and initialize handshaker for transport
     *
     * @param connectionString The IoT Hub connection string
     * @param iotHubServiceClientProtocol protocol to use
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     * @param sslContext the SSL context to use during the TLS handshake when opening the connection. If null, a default
     *                   SSL context will be generated. This default SSLContext trusts the IoT Hub public certificates.
     */
    public AmqpSendHandler(
            String connectionString,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        super(connectionString, iotHubServiceClientProtocol, proxyOptions, sslContext);
    }

    public AmqpSendHandler(
            String hostName,
            TokenCredential tokenProvider,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        super(hostName, tokenProvider, iotHubServiceClientProtocol, proxyOptions, sslContext);
    }

    public AmqpSendHandler(
            String hostName,
            AzureSasCredential sasTokenProvider,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        super(hostName, sasTokenProvider, iotHubServiceClientProtocol, proxyOptions, sslContext);
    }

    public void sendAsync(String deviceId, String moduleId, Message iotHubMessage, Consumer<SendResult> callback, Object context)
    {
        org.apache.qpid.proton.message.Message protonMessageToQueue;
        if (moduleId == null)
        {
            protonMessageToQueue = createProtonMessage(deviceId, iotHubMessage);
            log.debug("Queueing cloud to device message with correlation id {}", iotHubMessage.getCorrelationId());
        }
        else
        {
            protonMessageToQueue = createProtonMessage(deviceId, moduleId, iotHubMessage);
            log.debug("Queueing cloud to module message with correlation id {}", iotHubMessage.getCorrelationId());
        }

        outgoingMessageQueue.add(protonMessageToQueue);
        protonMessageToIotHubMessageMap.put(protonMessageToQueue, iotHubMessage);
        iotHubMessageToCallbackMap.put(iotHubMessage, callback);
        iotHubMessageToCallbackContextMap.put(iotHubMessage, context);
    }

    static org.apache.qpid.proton.message.Message createProtonMessage(String deviceId, Message message)
    {
        return populateProtonMessage(String.format(DEVICE_PATH_FORMAT, deviceId), message);
    }

    static org.apache.qpid.proton.message.Message createProtonMessage(String deviceId, String moduleId, Message message)
    {
        return populateProtonMessage(String.format(MODULE_PATH_FORMAT, deviceId, moduleId), message);
    }

    static org.apache.qpid.proton.message.Message populateProtonMessage(String targetPath, Message message)
    {
        org.apache.qpid.proton.message.Message protonMessage = Proton.message();

        Properties properties = new Properties();
        properties.setMessageId(message.getMessageId());
        properties.setTo(targetPath);
        properties.setCorrelationId(message.getCorrelationId());
        if (message.getUserId() != null)
        {
            properties.setUserId(new Binary(message.getUserId().getBytes(StandardCharsets.UTF_8)));
        }
        protonMessage.setProperties(properties);

        if (message.getProperties() != null && message.getProperties().size() > 0)
        {
            Map<String, Object> applicationPropertiesMap = new HashMap<>(message.getProperties().size());
            for (Map.Entry<String, String> entry : message.getProperties().entrySet())
            {
                applicationPropertiesMap.put(entry.getKey(), entry.getValue());
            }
            ApplicationProperties applicationProperties = new ApplicationProperties(applicationPropertiesMap);
            protonMessage.setApplicationProperties(applicationProperties);
        }

        Binary binary;
        //Messages may have no payload, so check that the message has a payload before giving message.getBytes(StandardCharsets.UTF_8) as the payload
        if (message.getBytes() != null)
        {
            binary = new Binary(message.getBytes());
        }
        else
        {
            binary = new Binary(new byte[0]);
        }

        Section section = new Data(binary);
        protonMessage.setBody(section);
        return protonMessage;
    }

    /**
     * Event handler for the link flow event
     * @param event The proton event object
     */
    @Override
    public void onLinkFlow(Event event)
    {
        //TODO log
    }

    @Override
    public void onDelivery(Event event)
    {
        Delivery delivery = event.getDelivery();

        DeliveryState remoteState = delivery.getRemoteState();

        int deliveryTag = Integer.parseInt(new String(delivery.getTag(), StandardCharsets.UTF_8));

        org.apache.qpid.proton.message.Message protonMessage = unacknowledgedMessages.remove(deliveryTag);

        if (protonMessage != null)
        {
            Message iotHubMessage = this.protonMessageToIotHubMessageMap.remove(protonMessage);
            if (iotHubMessage != null)
            {
                log.trace("Acknowledgement arrived for sent cloud to device message with correlation id {}", iotHubMessage.getCorrelationId());
                AmqpResponseVerification amqpResponse = new AmqpResponseVerification(remoteState);

                Consumer<SendResult> onMessageSentCallback = this.iotHubMessageToCallbackMap.remove(iotHubMessage);
                Object context = this.iotHubMessageToCallbackContextMap.remove(iotHubMessage);

                if (onMessageSentCallback != null)
                {
                    SendResult sendResult = new SendResult(amqpResponse.getException() != null, context, amqpResponse.getException());
                    onMessageSentCallback.accept(sendResult);
                }
            }
            else
            {
                log.debug("Received an acknowledgement for a cloud to device message that this client sent, but has no record of being asked to send");
            }
        }
        else
        {
            log.debug("Received an acknowledgement for a cloud to device message that this client did not send");
        }

        delivery.settle();
    }

    @Override
    public void onConnectionRemoteClose(Event event)
    {
        super.onConnectionRemoteClose(event);
        event.getTransport().close_tail();
    }


    @Override
    public void onTimerTask(Event event)
    {
        org.apache.qpid.proton.message.Message outgoingMessage = this.outgoingMessageQueue.poll();
        while (outgoingMessage != null)
        {

            log.debug("Sending cloud to device message with correlation id {}", outgoingMessage.getCorrelationId());
            byte[] msgData = new byte[1024];
            int length;
            while (true)
            {
                try
                {
                    length = outgoingMessage.encode(msgData, 0, msgData.length);
                    break;
                }
                catch (BufferOverflowException e)
                {
                    msgData = new byte[msgData.length * 2];
                }
            }

            byte[] tag = String.valueOf(this.nextTag).getBytes(StandardCharsets.UTF_8);

            this.cloudToDeviceMessageSendingLink.delivery(tag);
            this.cloudToDeviceMessageSendingLink.send(msgData, 0, length);
            this.cloudToDeviceMessageSendingLink.advance();

            this.unacknowledgedMessages.put(nextTag, outgoingMessage);

            //want to avoid negative delivery tags since -1 is the designated failure value
            if (this.nextTag == Integer.MAX_VALUE || this.nextTag < 0)
            {
                this.nextTag = 0;
            }
            else
            {
                this.nextTag++;
            }

            outgoingMessage = this.outgoingMessageQueue.poll();
        }

        // schedule the next onTimerTask event so that messages can be sent again later
        event.getReactor().schedule(200, this);
    }

    @Override
    public void onAuthenticationSucceeded()
    {
        // Only open the session and sending link if this authentication was for the first open. This callback
        // will be executed again after every proactive renewal, but nothing needs to be done after a proactive renewal
        if (this.cloudToDeviceMessageSendingLink == null)
        {
            // Every session or link could have their own handler(s) if we
            // wanted simply by adding the handler to the given session
            // or link

            this.cloudToDeviceMessageSession = this.connection.session();

            // If a link doesn't have an event handler, the events go to
            // its parent session. If the session doesn't have a handler
            // the events go to its parent connection. If the connection
            // doesn't have a handler, the events go to the reactor.

            Map<Symbol, Object> properties = new HashMap<>();
            properties.put(Symbol.getSymbol(TransportUtils.versionIdentifierKey), TransportUtils.USER_AGENT_STRING);
            this.cloudToDeviceMessageSession.open();

            this.cloudToDeviceMessageSendingLink = this.cloudToDeviceMessageSession.sender(SEND_TAG);
            this.cloudToDeviceMessageSendingLink.setProperties(properties);
            Target t = new Target();
            t.setAddress(ENDPOINT);
            this.cloudToDeviceMessageSendingLink.setTarget(t);
            this.cloudToDeviceMessageSendingLink.open();

            log.debug("Opening sender link for amqp cloud to device messages");
        }
    }

    @Override
    public void closeAsync()
    {
        if (this.cloudToDeviceMessageSendingLink != null)
        {
            log.debug("Shutdown event occurred, closing cloud to device message sender link");
            this.cloudToDeviceMessageSendingLink.close();
        }

        if (this.cloudToDeviceMessageSession != null)
        {
            log.debug("Shutdown event occurred, closing cloud to device message sender session");
            this.cloudToDeviceMessageSession.close();
        }

        super.closeAsync();
    }
}
