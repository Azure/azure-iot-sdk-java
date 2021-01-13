/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.FeedbackBatchMessage;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;

/**
 * Instance of the QPID-Proton-J BaseHandler class
 * overriding the events what are needed to handle
 * high level open, close methods and feedback received event.
 */
@Slf4j
public class AmqpReceive implements AmqpFeedbackReceivedEvent
{
    private final String hostName;
    private final String userName;
    private final String sasToken;
    private AmqpFeedbackReceivedHandler amqpReceiveHandler;
    private final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private FeedbackBatch feedbackBatch;
    private final ProxyOptions proxyOptions;
    private final SSLContext sslContext;

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     */
    public AmqpReceive(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        this(hostName, userName, sasToken, iotHubServiceClientProtocol, null);
    }

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     */
    public AmqpReceive(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, ProxyOptions proxyOptions)
    {
        this(hostName, userName, sasToken, iotHubServiceClientProtocol, proxyOptions, null);
    }

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     * @param sslContext the SSL context to use during the TLS handshake when opening the connection. If null, a default
     *                   SSL context will be generated. This default SSLContext trusts the IoT Hub public certificates.
     */
    public AmqpReceive(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, ProxyOptions proxyOptions, SSLContext sslContext)
    {
        this.hostName = hostName;
        this.userName = userName;
        this.sasToken = sasToken;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
    }

    /**
     * Create AmqpsReceiveHandler and store it in a member variable
     */
    public void open()
    {
        amqpReceiveHandler = new AmqpFeedbackReceivedHandler(
                this.hostName,
                this.userName,
                this.sasToken,
                this.iotHubServiceClientProtocol,
                this,
                this.proxyOptions,
                this.sslContext);
    }

    /**
     * Invalidate AmqpsReceiveHandler member variable
     */
    public void close()
    {
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
            log.info("Receiving on feedback receiver for up to {} milliseconds", timeoutMs);

            // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_007: [The function shall wait for specified timeout to check for any feedback message]
            new ReactorRunner(this.amqpReceiveHandler, "AmqpFeedbackReceiver").run(timeoutMs);

            log.trace("Feedback receiver reactor finished running, verifying that the connection opened correctly");
            this.amqpReceiveHandler.verifyConnectionWasOpened();
            log.trace("Feedback receiver reactor did successfully open the connection, returning without exception");
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
        log.info("Feedback message received: {}", feedbackJson);

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPRECEIVE_12_010: [The function shall parse the received Json string to FeedbackBath object]
        feedbackBatch = FeedbackBatchMessage.parse(feedbackJson);
    }
}
