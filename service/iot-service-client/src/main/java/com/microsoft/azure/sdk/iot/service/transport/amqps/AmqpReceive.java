/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackBatchMessage;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
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
    private String userName;
    private String sasToken;
    private TokenCredential credential;
    private AzureSasCredential sasTokenProvider;
    private AmqpFeedbackReceivedHandler amqpReceiveHandler;
    private final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private FeedbackBatch feedbackBatch;
    private final ProxyOptions proxyOptions;
    private final SSLContext sslContext;
    private boolean isOpen = false;

    /**
     * Constructor to set up connection parameters
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     */
    public AmqpReceive(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol)
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
    private AmqpReceive(
        String hostName,
        String userName,
        String sasToken,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        ProxyOptions proxyOptions)
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
    public AmqpReceive(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        this.hostName = hostName;
        this.userName = userName;
        this.sasToken = sasToken;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
    }

    public AmqpReceive(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        this.hostName = hostName;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
        this.credential = credential;
    }

    public AmqpReceive(
            String hostName,
            AzureSasCredential sasTokenProvider,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        this.hostName = hostName;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
        this.sasTokenProvider = sasTokenProvider;
    }

    /**
     * Create AmqpsReceiveHandler and store it in a member variable
     */
    public void open()
    {
        // This is a no-op at this point since all the actual amqp handler instantiation lives in the receive() call
        // instead. We have a check in the receive call that throws if this method isn't called first though, so we do
        // have to set this flag
        isOpen = true;
    }

    private void initializeHandler()
    {
        if (credential != null)
        {
            this.amqpReceiveHandler = new AmqpFeedbackReceivedHandler(
                this.hostName,
                this.credential,
                this.iotHubServiceClientProtocol,
                this,
                this.proxyOptions,
                this.sslContext);
        }
        else if (sasTokenProvider != null)
        {
            this.amqpReceiveHandler = new AmqpFeedbackReceivedHandler(
                this.hostName,
                this.sasTokenProvider,
                this.iotHubServiceClientProtocol,
                this,
                this.proxyOptions,
                this.sslContext);
        }
        else
        {
            this.amqpReceiveHandler = new AmqpFeedbackReceivedHandler(
                this.hostName,
                this.userName,
                this.sasToken,
                this.iotHubServiceClientProtocol,
                this,
                this.proxyOptions,
                this.sslContext);
        }
    }

    /**
     * Invalidate AmqpsReceiveHandler member variable
     */
    public void close()
    {
        isOpen = false;
    }

    /**
     * Synchronized call to receive feedback batch
     * Hide the event based receiving mechanism from the user API
     * @param timeoutMs The timeout in milliseconds to wait for the feedback
     * @return The received feedback batch
     * @throws IOException This exception is thrown if the input AmqpReceive object is null
     */
    public synchronized FeedbackBatch receive(long timeoutMs) throws IOException
    {
        // This value is set in the reactor thread once we receive the file upload notification from the service over AMQP.
        // It's important to set this to null since receive may be called multiple times in a row. Otherwise, a single
        // notification could be returned to the user multiple times if nothing overwrote a previous value.
        feedbackBatch = null;
        
        if (isOpen)
        {
            // instantiating the amqp handler each receive call because each receive call opens a new AMQP connection
            initializeHandler();

            log.info("Receiving on feedback receiver for up to {} milliseconds", timeoutMs);

            String reactorRunnerPrefix = this.hostName + "-" + "Cxn" + this.amqpReceiveHandler.getConnectionId();
            new ReactorRunner(this.amqpReceiveHandler, reactorRunnerPrefix, "AmqpFeedbackReceiver").run(timeoutMs);

            log.trace("Feedback receiver reactor finished running, verifying that the connection opened correctly");
            this.amqpReceiveHandler.verifyConnectionWasOpened();
            log.trace("Feedback receiver reactor did successfully open the connection, returning without exception");
        }
        else
        {
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

        feedbackBatch = FeedbackBatchMessage.parse(feedbackJson);
    }
}
