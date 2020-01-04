/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.*;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;

import java.util.Map;

public class AmqpFeedbackListenerHandler extends AmqpConnectionHandler
{
    public static final String RECEIVE_TAG = "receiver";
    public static final String ENDPOINT = "/messages/servicebound/feedback";

    private FeedbackBatchMessageCallback listener;
    private static final int expectedLinkCount = 2;

    /**
     * Constructor to set up connection parameters and initialize
     * handshaker and flow controller for transport
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     * @param listener callback to delegate the received message to the user API
     */
    public AmqpFeedbackListenerHandler(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, FeedbackBatchMessageCallback listener)
    {
        super(hostName, userName, sasToken, iotHubServiceClientProtocol, RECEIVE_TAG, ENDPOINT, expectedLinkCount);

        Tools.throwIfNull(listener, "Feedback batch message callback cannot be null");

        this.listener = listener;
    }

    @Override
    public DeliveryOutcome onMessageArrived(Message message)
    {
        String feedbackString = message.getBody().toString();
        FeedbackBatch feedbackBatch = FeedbackBatchMessage.parse(feedbackString);
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
        Receiver feedbackReceiver = session.receiver(tag);
        feedbackReceiver.setProperties(properties);
        feedbackReceiver.open();
    }
}