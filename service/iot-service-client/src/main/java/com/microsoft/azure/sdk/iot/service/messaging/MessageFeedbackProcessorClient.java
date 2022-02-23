// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.amqps.EventReceivingConnectionHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class MessageFeedbackProcessorClient
{
    private static final int START_REACTOR_TIMEOUT_MILLISECONDS = 60 * 1000; // 60 seconds
    private static final int STOP_REACTOR_TIMEOUT_MILLISECONDS = 60 * 1000; // 60 seconds

    private final EventReceivingConnectionHandler eventReceivingConnectionHandler;
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
        this.eventReceivingConnectionHandler =
            new EventReceivingConnectionHandler(
                hostName,
                credential,
                protocol,
                null,
                feedbackMessageProcessor,
                this.errorProcessor,
                options.getProxyOptions(),
                options.getSslContext(),
                options.getKeepAliveInterval());
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
        this.eventReceivingConnectionHandler =
            new EventReceivingConnectionHandler(
                hostName,
                sasTokenProvider,
                protocol,
                null,
                feedbackMessageProcessor,
                this.errorProcessor,
                options.getProxyOptions(),
                options.getSslContext(),
                options.getKeepAliveInterval());
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
        this.eventReceivingConnectionHandler =
            new EventReceivingConnectionHandler(
                connectionString,
                protocol,
                null,
                feedbackMessageProcessor,
                this.errorProcessor,
                options.getProxyOptions(),
                options.getSslContext(),
                options.getKeepAliveInterval());
    }

    public synchronized void start() throws IotHubException, IOException, InterruptedException
    {
        start(START_REACTOR_TIMEOUT_MILLISECONDS);
    }

    public synchronized void start(int timeoutMilliseconds) throws IotHubException, IOException, InterruptedException
    {
        if (this.reactorRunner != null && this.eventReceivingConnectionHandler != null && this.eventReceivingConnectionHandler.isOpen())
        {
            //already open
            return;
        }

        if (timeoutMilliseconds < 0)
        {
            throw new IllegalArgumentException("timeoutMilliseconds must be greater than or equal to 0");
        }

        AtomicReference<IotHubException> iotHubException = new AtomicReference<>(null);
        AtomicReference<IOException> ioException = new AtomicReference<>(null);

        log.debug("Opening MessageFeedbackProcessorClient");

        this.reactorRunner = new ReactorRunner(
            this.eventReceivingConnectionHandler.getHostName(),
            "MessageFeedbackProcessor",
            this.eventReceivingConnectionHandler);

        final CountDownLatch openLatch = new CountDownLatch(1);
        this.eventReceivingConnectionHandler.setOnConnectionOpenedCallback(() -> openLatch.countDown());

        new Thread(() ->
        {
            try
            {
                reactorRunner.run();

                log.trace("MessageFeedbackProcessorClient Amqp reactor stopped, checking that the connection was opened");
                this.eventReceivingConnectionHandler.verifyConnectionWasOpened();

                log.trace("MessageFeedbackProcessorClient reactor did successfully open the connection, returning without exception");
            }
            catch (IOException e)
            {
                log.debug("MessageFeedbackProcessorClient Amqp connection encountered an exception", e);

                ioException.set(e);
            }
            catch (IotHubException e)
            {
                log.debug("MessageFeedbackProcessorClient Amqp connection encountered an exception", e);

                iotHubException.set(e);
            }
            finally
            {
                openLatch.countDown();
            }
        }).start();

        if (timeoutMilliseconds == 0)
        {
            // wait indefinitely
            openLatch.await();
        }
        else
        {
            boolean timedOut = !openLatch.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw new IOException("Timed out waiting for the connection to the service to open");
            }
        }

        if (ioException.get() != null)
        {
            throw ioException.get();
        }

        if (iotHubException.get() != null)
        {
            throw iotHubException.get();
        }

        log.info("Started MessageFeedbackProcessorClient");
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

        log.info("Stopped MessageFeedbackProcessorClient");
    }

    public synchronized boolean isRunning()
    {
        return this.reactorRunner != null && this.reactorRunner.isRunning();
    }
}
