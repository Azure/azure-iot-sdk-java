// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpEventProcessorHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class MessageFeedbackProcessorClient
{
    private static final int STOP_REACTOR_TIMEOUT_MILLISECONDS = 10 * 1000; // 10 seconds

    private final AmqpEventProcessorHandler amqpEventProcessorHandler;
    private final Consumer<ErrorContext> errorProcessor; // may be null if user doesn't provide one

    private ReactorRunner reactorRunner;

    public MessageFeedbackProcessorClient(
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor)
    {
        this(hostName, credential, iotHubServiceClientProtocol, feedbackMessageProcessor, MessageFeedbackProcessorClientOptions.builder().build());
    }

    public MessageFeedbackProcessorClient(
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol protocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor,
        MessageFeedbackProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");

        this.errorProcessor = options.getErrorProcessor();
        this.amqpEventProcessorHandler =
            new AmqpEventProcessorHandler(
                hostName,
                credential,
                protocol,
                null,
                feedbackMessageProcessor,
                options.getProxyOptions(),
                options.getSslContext());
    }

    public MessageFeedbackProcessorClient(
        String hostName,
        AzureSasCredential sasTokenProvider,
        IotHubServiceClientProtocol protocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor)
    {
        this(hostName, sasTokenProvider, protocol, feedbackMessageProcessor, MessageFeedbackProcessorClientOptions.builder().build());
    }

    public MessageFeedbackProcessorClient(
        String hostName,
        AzureSasCredential sasTokenProvider,
        IotHubServiceClientProtocol protocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor,
        MessageFeedbackProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");

        this.errorProcessor = options.getErrorProcessor();
        this.amqpEventProcessorHandler =
            new AmqpEventProcessorHandler(
                hostName,
                sasTokenProvider,
                protocol,
                null,
                feedbackMessageProcessor,
                options.getProxyOptions(),
                options.getSslContext());
    }

    public MessageFeedbackProcessorClient(
        String connectionString,
        IotHubServiceClientProtocol protocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor)
    {
        this(connectionString, protocol, feedbackMessageProcessor, MessageFeedbackProcessorClientOptions.builder().build());
    }

    public MessageFeedbackProcessorClient(
        String connectionString,
        IotHubServiceClientProtocol protocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor,
        MessageFeedbackProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");

        this.errorProcessor = options.getErrorProcessor();
        this.amqpEventProcessorHandler =
            new AmqpEventProcessorHandler(
                connectionString,
                protocol,
                null,
                feedbackMessageProcessor,
                options.getProxyOptions(),
                options.getSslContext());
    }

    public synchronized void start() throws IotHubException, IOException, InterruptedException
    {
        if (this.reactorRunner != null && this.amqpEventProcessorHandler != null && this.amqpEventProcessorHandler.isOpen())
        {
            //already open
            return;
        }

        log.debug("Opening MessageFeedbackProcessorClient");

        this.reactorRunner = new ReactorRunner(
            this.amqpEventProcessorHandler.getHostName(),
            "AmqpFileUploadNotificationAndCloudToDeviceFeedbackReceiver",
            this.amqpEventProcessorHandler);

        final CountDownLatch openLatch = new CountDownLatch(1);
        this.amqpEventProcessorHandler.setOnConnectionOpenedCallback(e ->
        {
            //TODO check for exception?

            openLatch.countDown();
        });

        new Thread(() ->
        {
            try
            {
                reactorRunner.run();

                log.trace("EventProcessorClient Amqp reactor stopped, checking that the connection was opened");
                this.amqpEventProcessorHandler.verifyConnectionWasOpened();

                log.trace("EventProcessorClient  reactor did successfully open the connection, returning without exception");
            }
            catch (IOException | IotHubException e)
            {
                log.warn("EventProcessorClient Amqp connection encountered an exception", e);

                if (this.errorProcessor != null)
                {
                    this.errorProcessor.accept(new ErrorContext(e));
                }
            }
        }).start();

        boolean timedOut = !openLatch.await(10 * 1000, TimeUnit.MILLISECONDS);

        if (timedOut)
        {
            //TODO
        }

        log.debug("Opened MessageFeedbackProcessorClient");
    }

    public synchronized void stop() throws InterruptedException
    {
        this.stop(STOP_REACTOR_TIMEOUT_MILLISECONDS);
    }

    public synchronized void stop(int timeoutMilliseconds) throws InterruptedException
    {
        if (this.reactorRunner == null)
        {
            return;
        }

        log.debug("Closing MessageFeedbackProcessorClient");

        this.reactorRunner.stop(timeoutMilliseconds);
        this.reactorRunner = null;

        log.debug("Closed MessageFeedbackProcessorClient");
    }
}
