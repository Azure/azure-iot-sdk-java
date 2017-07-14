/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.FeedbackBatchMessage;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;

import java.io.IOException;

/**
 * Instance of the QPID-Proton-J BaseHandler class
 * overriding the events what are needed to handle
 * high level open, close methods and feedback received event.
 */
public class AmqpReceive extends BaseHandler implements AmqpFeedbackReceivedEvent
{
    private final String hostName;
    private final String userName;
    private final String sasToken;
    private AmqpFeedbackReceivedHandler amqpReceiveHandler;
    private IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private Reactor reactor = null;
    private FeedbackBatch feedbackBatch;
    private static final int REACTOR_TIMEOUT = 3141; // reactor timeout in milliseconds

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     */
    public AmqpReceive(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_001: [The constructor shall copy all input parameters to private member variables for event processing]
        this.hostName = hostName;
        this.userName = userName;
        this.sasToken = sasToken;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
    }

    /**
     * Event handler for the reactor init event
     * @param event The proton event object
     */
    @Override
    public void onReactorInit(Event event) {
        // You can use the connection method to create AMQP connections.

        // This connection's handler is the AmqpSendHandler object. All the events
        // for this connection will go to the AmqpSendHandler object instead of
        // going to the reactor. If you were to omit the AmqpSendHandler object,
        // all the events would go to the reactor.

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_002: [The event handler shall set the member AmqpsReceiveHandler object to handle the given connection events]
        event.getReactor().connection(amqpReceiveHandler);
    }

    /**
     * Create AmqpsReceiveHandler and store it in a member variable
     */
    public void open()
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_003: [The function shall create an AmqpsReceiveHandler object to handle reactor events]
        amqpReceiveHandler = new AmqpFeedbackReceivedHandler(this.hostName, this.userName, this.sasToken, this.iotHubServiceClientProtocol, this);
    }

    /**
     * Invalidate AmqpsReceiveHandler member variable
     */
    public void close()
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_004: [The function shall invalidate the member AmqpsReceiveHandler object]
        amqpReceiveHandler = null;
    }

    /**
     * Synchronized call to receive feedback batch
     * Hide the event based receiving mechanism from the user API
     * @param timeoutMs The timeout in milliseconds to wait for the feedback
     * @return The received feedback batch
     * @throws IOException This exception is thrown if the input AmqpReceive object is null
     * @throws InterruptedException This exception is thrown if the receive process has been interrupted
     */
    public synchronized FeedbackBatch receive(long timeoutMs) throws IOException, InterruptedException
    {
        feedbackBatch = null;
        if  (amqpReceiveHandler != null)
        {
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_005: [The function shall initialize the Proton reactor object]
            this.reactor = Proton.reactor(this);
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_006: [The function shall start the Proton reactor object]
            this.reactor.setTimeout(REACTOR_TIMEOUT);
            this.reactor.start();
            
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_007: [The function shall wait for specified timeout to check for any feedback message]
            long startTime = System.currentTimeMillis();
            long endTime = startTime + timeoutMs;
            
            while(this.reactor.process())
            {
             if (System.currentTimeMillis() > endTime) break;
            }
            
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_008: [The function shall stop and free the Proton reactor object]
            this.reactor.stop();
            this.reactor.process();
            this.reactor.free();   
            
          
        }
        else
        {
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_008: [The function shall throw IOException if the send handler object is not initialized]
            throw new IOException("receive handler is not initialized. call open before receive");
        }
        return feedbackBatch;
    }

    /**
     * Handle on feedback received Proton event
     * Parse received json and save result to a member variable
     * Release semaphore for wait function
     * @param feedbackJson Received Json string to process
     */
    public void onFeedbackReceived(String feedbackJson)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_010: [The function shall parse the received Json string to FeedbackBath object]
        feedbackBatch = FeedbackBatchMessage.parse(feedbackJson);
    
    }

}
