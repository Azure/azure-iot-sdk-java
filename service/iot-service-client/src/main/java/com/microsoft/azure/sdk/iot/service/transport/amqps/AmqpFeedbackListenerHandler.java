/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;

import java.util.Map;

@Slf4j
public class AmqpFeedbackListenerHandler extends AmqpConnectionHandler
{
    public static final String RECEIVE_TAG = "receiver";
    public static final String ENDPOINT = "/messages/servicebound/feedback";
    private static final String THREAD_POSTFIX_NAME = "FeedbackMessageListenerClient";

    private FeedbackBatchMessageCallback listener;
    private static final int expectedLinkCount = 1;
    private Receiver feedbackReceiver;

    /**
     * Constructor to set up connection parameters and initialize
     * handshaker and flow controller for transport
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param iotHubServiceClientProtocol protocol to use
     * @param listener callback to delegate the received message to the user API
     */
    public AmqpFeedbackListenerHandler(String hostName, String userName, IotHubServiceClientProtocol iotHubServiceClientProtocol, FeedbackBatchMessageCallback listener)
    {
        super(hostName, userName, iotHubServiceClientProtocol, RECEIVE_TAG, ENDPOINT, expectedLinkCount);

        Tools.throwIfNull(listener, "Feedback batch message callback cannot be null");

        this.listener = listener;
    }

    @Override
    public DeliveryOutcome onMessageArrived(Message message)
    {
        String feedbackString = message.getBody().toString();
        FeedbackBatch feedbackBatch = FeedbackBatchMessage.parse(feedbackString);

        log.trace("Notifying listener that a feedback message was received");
        return listener.onFeedbackMessageReceived(feedbackBatch);
    }

    @Override
    public void onMessageAcknowledged(DeliveryState deliveryState)
    {
        //Never called, do nothing
    }

    @Override
    public void openLinks(Session session, Map<Symbol, Object> properties)
    {
        log.debug("Opening links to listen for feedback messages");
        this.feedbackReceiver = session.receiver(tag);
        this.feedbackReceiver.setProperties(properties);
        this.feedbackReceiver.open();
    }

    @Override
    public void closeLinks()
    {
        if (this.feedbackReceiver != null)
        {
            log.debug("Closing feedback receiver link");
            this.feedbackReceiver.close();
        }
    }

    @Override
    public String getThreadNamePostfix()
    {
        return THREAD_POSTFIX_NAME;
    }
}