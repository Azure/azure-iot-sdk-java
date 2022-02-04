/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFeedbackReceivedHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Objects;

/**
 * A receiver that listens for cloud to device feedback messages and completes/abandons each notification after
 * they are received.
 */
@Slf4j
public class FeedbackReceiver
{
    private final AmqpFeedbackReceivedHandler amqpReceiveHandler;
    private final String hostName;
    private ReactorRunner amqpConnectionReactorRunner;

    FeedbackReceiver(
        FeedbackMessageReceivedCallback feedbackMessageReceivedCallback,
        String hostName,
        String sasToken,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }
        if (Tools.isNullOrEmpty(sasToken))
        {
            throw new IllegalArgumentException("sasToken cannot be null or empty");
        }
        if (iotHubServiceClientProtocol == null)
        {
            throw new IllegalArgumentException("iotHubServiceClientProtocol cannot be null");
        }

        Objects.requireNonNull(feedbackMessageReceivedCallback);

        this.hostName = hostName;
        this.amqpReceiveHandler = new AmqpFeedbackReceivedHandler(
            hostName,
            sasToken,
            iotHubServiceClientProtocol,
            feedbackMessageReceivedCallback,
            proxyOptions,
            sslContext);
    }

    FeedbackReceiver(
        FeedbackMessageReceivedCallback feedbackMessageReceivedCallback,
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        Objects.requireNonNull(credential);
        Objects.requireNonNull(iotHubServiceClientProtocol);
        Objects.requireNonNull(feedbackMessageReceivedCallback);

        this.hostName = hostName;
        this.amqpReceiveHandler = new AmqpFeedbackReceivedHandler(
            hostName,
            credential,
            iotHubServiceClientProtocol,
            feedbackMessageReceivedCallback,
            proxyOptions,
            sslContext);
    }

    FeedbackReceiver(
        FeedbackMessageReceivedCallback feedbackMessageReceivedCallback,
        String hostName,
        AzureSasCredential sasTokenProvider,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        Objects.requireNonNull(sasTokenProvider);
        Objects.requireNonNull(iotHubServiceClientProtocol);
        Objects.requireNonNull(feedbackMessageReceivedCallback);

        this.hostName = hostName;
        this.amqpReceiveHandler = new AmqpFeedbackReceivedHandler(
            hostName,
            sasTokenProvider,
            iotHubServiceClientProtocol,
            feedbackMessageReceivedCallback,
            proxyOptions,
            sslContext);
    }

    /**
     * Open AmqpReceive object
     *
     */
    public void open() throws IOException
    {
        log.debug("Opening feedback receiver");

        this.amqpConnectionReactorRunner =
            new ReactorRunner(amqpReceiveHandler, hostName, "AmqpFeedbackReceiver");

        new Thread(() ->
        {
            try
            {
                amqpConnectionReactorRunner.run();

                log.trace("Amqp receive reactor stopped, checking that the connection was opened");
                amqpReceiveHandler.verifyConnectionWasOpened();
                log.trace("Amqp receive reactor did successfully open the connection, returning without exception");
            }
            catch (IOException e)
            {
                //TODO add some connection status callback to the user?
                log.warn("Amqp connection thread encountered an exception", e);
            }
        }).start();

        log.debug("Opened feedback receiver");
    }

    /**
     * Close AmqpReceive object
     *
     */
    public void close()
    {
        log.debug("Closing feedback receiver");

        this.amqpConnectionReactorRunner.stop();

        log.debug("Closed feedback receiver");
    }
}
