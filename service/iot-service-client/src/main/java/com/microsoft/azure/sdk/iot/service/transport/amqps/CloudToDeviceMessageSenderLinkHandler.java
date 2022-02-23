/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.messaging.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sender;

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
public class CloudToDeviceMessageSenderLinkHandler extends SenderLinkHandler
{
    private final Queue<CloudToDeviceMessage> outgoingMessageQueue = new ConcurrentLinkedQueue<>();
    private final Map<Integer, CloudToDeviceMessage> unacknowledgedMessages = new ConcurrentHashMap<>();

    public CloudToDeviceMessageSenderLinkHandler(Sender sender, String linkCorrelationId, LinkStateCallback linkStateCallback)
    {
        super(sender, linkCorrelationId, linkStateCallback);
    }

    public void sendAsync(String deviceId, String moduleId, Message iotHubMessage, Consumer<SendResult> callback, Object context)
    {
        if (moduleId == null)
        {
            log.trace("Queueing cloud to device message with correlation id {}", iotHubMessage.getCorrelationId());
        }
        else
        {
            log.trace("Queueing cloud to module message with correlation id {}", iotHubMessage.getCorrelationId());
        }

        outgoingMessageQueue.add(new CloudToDeviceMessage(deviceId, moduleId, iotHubMessage, callback, context));
    }

    /**
     * Event handler for the link flow event
     * @param event The proton event object
     */
    @Override
    public void onLinkFlow(Event event)
    {
        //TODO log
        event.getReactor().schedule(200, this);
    }


    @Override
    public void onTimerTask(Event event)
    {
        sendQueuedMessages();

        // schedule the next onTimerTask event so that messages can be sent again later
        event.getReactor().schedule(200, this);
    }

    private void sendQueuedMessages()
    {
        CloudToDeviceMessage outgoingMessage = this.outgoingMessageQueue.poll();
        while (outgoingMessage != null)
        {
            int deliveryTag = this.sendMessageAndGetDeliveryTag(outgoingMessage.getProtonMessage());
            this.unacknowledgedMessages.put(deliveryTag, outgoingMessage);
            outgoingMessage = this.outgoingMessageQueue.poll();
        }
    }

    @Override
    public void onDelivery(Event event)
    {
        Delivery delivery = event.getDelivery();

        DeliveryState remoteState = delivery.getRemoteState();

        int deliveryTag = Integer.parseInt(new String(delivery.getTag(), StandardCharsets.UTF_8));

        CloudToDeviceMessage message = unacknowledgedMessages.remove(deliveryTag);

        if (message != null)
        {
            String correlationId = message.getCorrelationId();
            log.trace("Acknowledgement arrived for sent cloud to device message with correlation id {}", correlationId);
            AmqpResponseVerification amqpResponse = new AmqpResponseVerification(remoteState);

            Consumer<SendResult> onMessageSentCallback = message.getOnMessageSentCallback();

            if (onMessageSentCallback != null)
            {
                SendResult sendResult =
                    new SendResult(
                        amqpResponse.getException() == null,
                        correlationId,
                        message.getOnMessageSentCallbackContext(),
                        amqpResponse.getException());

                onMessageSentCallback.accept(sendResult);
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
    void close()
    {
        super.close();

        for (CloudToDeviceMessage unsentMessage : outgoingMessageQueue)
        {
            IotHubException exception = new IotHubException("Message failed to send because the client was closed while it was still queued.");
            Consumer<SendResult> callback = unsentMessage.getOnMessageSentCallback();
            if (callback != null)
            {
                callback.accept(
                    new SendResult(
                        false,
                        unsentMessage.getCorrelationId(),
                        unsentMessage.getOnMessageSentCallbackContext(),
                        exception));
            }
        }

        for (CloudToDeviceMessage unacknowledgedMessage : unacknowledgedMessages.values())
        {
            IotHubException exception = new IotHubException("Message failed to send because the client was closed after it was sent, but before it was acknowledged by the service.");
            Consumer<SendResult> callback = unacknowledgedMessage.getOnMessageSentCallback();
            if (callback != null)
            {
                callback.accept(
                    new SendResult(
                        false,
                        unacknowledgedMessage.getCorrelationId(),
                        unacknowledgedMessage.getOnMessageSentCallbackContext(),
                        exception));
            }
        }

        outgoingMessageQueue.clear();
        unacknowledgedMessages.clear();
    }

    @Override
    protected String getLinkInstanceType()
    {
        return "cloudToDeviceSender";
    }
}
