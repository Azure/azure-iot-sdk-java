// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnauthorizedException;
import com.microsoft.azure.sdk.iot.service.transport.amqps.EventReceivingConnectionHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A client for handling cloud to device message feedback. For more details on what cloud to device message feedback
 * is, see <a href="https://docs.microsoft.com/azure/iot-hub/iot-hub-devguide-messages-c2d#message-feedback">this document</a>.
 *
 * <p>
 *     This client relies on a persistent amqp/amqp_ws connection to IoT Hub that may break due to network instability.
 *     While optional to monitor, users are highly encouraged to utilize the errorProcessorHandler defined in the
 *     {@link MessageFeedbackProcessorClientOptions} when constructing this client in order to monitor the connection state and to re-open
 *     the connection when needed. See the message feedback processor client sample in this repo for best practices for
 *     monitoring and handling disconnection events.
 * </p>
 */
@Slf4j
public class MessageFeedbackProcessorClient
{
    private static final int START_REACTOR_TIMEOUT_MILLISECONDS = 60 * 1000; // 60 seconds
    private static final int STOP_REACTOR_TIMEOUT_MILLISECONDS = 60 * 1000; // 60 seconds

    private final EventReceivingConnectionHandler eventReceivingConnectionHandler;
    private final Consumer<ErrorContext> errorProcessor; // may be null if user doesn't provide one

    private ReactorRunner reactorRunner;
    private final String hostName;

    /**
     * Construct a MessageFeedbackProcessorClient using a {@link TokenCredential} instance for authentication.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param feedbackMessageProcessor The callback to be executed each time message feedback is received from the service. May not be null.
     */
    public MessageFeedbackProcessorClient(
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol protocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor)
    {
        this(hostName, credential, protocol, feedbackMessageProcessor, MessageFeedbackProcessorClientOptions.builder().build());
    }

    /**
     * Construct a MessageFeedbackProcessorClient using a {@link TokenCredential} instance for authentication.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param feedbackMessageProcessor The callback to be executed each time message feedback is received from the service. May not be null.
     * @param options The connection options to use when connecting to the service. May not be null.
     */
    public MessageFeedbackProcessorClient(
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol protocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor,
        MessageFeedbackProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");
        Objects.requireNonNull(feedbackMessageProcessor, "feedbackMessageProcessor cannot be null");

        this.errorProcessor = options.getErrorProcessor();
        this.hostName = hostName;
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

    /**
     * Construct a MessageFeedbackProcessorClient using a {@link AzureSasCredential} instance for authentication.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param feedbackMessageProcessor The callback to be executed each time message feedback is received from the service. May not be null.
     */
    public MessageFeedbackProcessorClient(
        String hostName,
        AzureSasCredential azureSasCredential,
        IotHubServiceClientProtocol protocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor)
    {
        this(hostName, azureSasCredential, protocol, feedbackMessageProcessor, MessageFeedbackProcessorClientOptions.builder().build());
    }

    /**
     * Construct a MessageFeedbackProcessorClient using a {@link AzureSasCredential} instance for authentication.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param feedbackMessageProcessor The callback to be executed each time message feedback is received from the service. May not be null.
     * @param options The connection options to use when connecting to the service. May not be null.
     */
    public MessageFeedbackProcessorClient(
        String hostName,
        AzureSasCredential azureSasCredential,
        IotHubServiceClientProtocol protocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor,
        MessageFeedbackProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");
        Objects.requireNonNull(feedbackMessageProcessor, "feedbackMessageProcessor cannot be null");

        this.errorProcessor = options.getErrorProcessor();
        this.hostName = hostName;
        this.eventReceivingConnectionHandler =
            new EventReceivingConnectionHandler(
                hostName,
                azureSasCredential,
                protocol,
                null,
                feedbackMessageProcessor,
                this.errorProcessor,
                options.getProxyOptions(),
                options.getSslContext(),
                options.getKeepAliveInterval());
    }

    /**
     * Construct a MessageFeedbackProcessorClient from the provided connection string.
     *
     * @param connectionString The connection string for the Iot Hub.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param feedbackMessageProcessor The callback to be executed each time message feedback is received from the service. May not be null.
     */
    public MessageFeedbackProcessorClient(
        String connectionString,
        IotHubServiceClientProtocol protocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor)
    {
        this(connectionString, protocol, feedbackMessageProcessor, MessageFeedbackProcessorClientOptions.builder().build());
    }

    /**
     * Construct a MessageFeedbackProcessorClient from the provided connection string.
     *
     * @param connectionString The connection string for the Iot Hub.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param feedbackMessageProcessor The callback to be executed each time message feedback is received from the service. May not be null.
     * @param options The connection options to use when connecting to the service. May not be null.
     */
    public MessageFeedbackProcessorClient(
        String connectionString,
        IotHubServiceClientProtocol protocol,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor,
        MessageFeedbackProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");
        Objects.requireNonNull(feedbackMessageProcessor, "feedbackMessageProcessor cannot be null");

        if (connectionString == null || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        this.errorProcessor = options.getErrorProcessor();
        this.hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString).getHostName();
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

    /**
     * Open this client so that it can begin processing message feedback. When you want to stop processing message
     * feedback, you should should call {@link #stop()} to free up network resources. If this
     * client is already started, then this function will do nothing.
     *
     * @throws IotHubException If any IoT Hub level exceptions occur such as an {@link IotHubUnauthorizedException}.
     * @throws IOException If any network level exceptions occur such as the connection timing out.
     * @throws InterruptedException If this thread is interrupted while waiting for the connection to the service to open.
     * @throws TimeoutException If the connection is not established before the default timeout.
     */
    public synchronized void start() throws IotHubException, IOException, InterruptedException, TimeoutException
    {
        start(START_REACTOR_TIMEOUT_MILLISECONDS);
    }

    /**
     * Open this client so that it can begin processing message feedback. When you want to stop processing message
     * feedback, you should should call {@link #stop()} to free up network resources. If this
     * client is already started, then this function will do nothing.
     *
     * @param timeoutMilliseconds the maximum number of milliseconds to wait for the underlying amqp connection to open.
     * If this value is 0, it will have an infinite timeout.
     * @throws IotHubException If any IoT Hub level exceptions occur such as an {@link IotHubUnauthorizedException}.
     * @throws IOException If any network level exceptions occur such as the connection timing out.
     * @throws InterruptedException If this thread is interrupted while waiting for the connection to the service to open.
     * @throws TimeoutException If the connection is not established before the provided timeout.
     */
    public synchronized void start(int timeoutMilliseconds) throws IotHubException, IOException, InterruptedException, TimeoutException
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
            this.hostName,
            "MessageFeedbackProcessor",
            this.eventReceivingConnectionHandler);

        final CountDownLatch openLatch = new CountDownLatch(1);
        this.eventReceivingConnectionHandler.setOnConnectionOpenedCallback(openLatch::countDown);

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
                throw new TimeoutException("Timed out waiting for the connection to the service to open");
            }
        }

        // if an IOException or IotHubException was encountered in the reactor thread, throw it here
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

    /**
     * Stops this client from processing any more message feedback and releases all network resources tied to it. Once
     * stopped, this client can be restarted by calling {@link #start()}. If this client is already closed,
     * this function will do nothing.
     *
     * @throws InterruptedException if this function is interrupted while waiting for the connection to close down all
     * network resources.
     */
    public synchronized void stop() throws InterruptedException
    {
        this.stop(STOP_REACTOR_TIMEOUT_MILLISECONDS);
    }

    /**
     * Stops this client from processing any more message feedback and releases all network resources tied to it. Once
     * stopped, this client can be restarted by calling {@link #start()}. If this client has already been stopped,
     * this function will do nothing.
     *
     * @param timeoutMilliseconds the maximum number of milliseconds to wait for the underlying amqp connection to close.
     * If this value is 0, it will have an infinite timeout. If the provided timeout has passed and the connection has
     * not closed gracefully, then the connection will be forcefully closed and no exception will be thrown.
     * @throws InterruptedException if this function is interrupted while waiting for the connection to close down all
     * network resources.
     */
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

    /**
     * Returns true if this client's underlying amqp connection is currently open and false otherwise. This client may
     * lose connectivity due to network issues, so this value may be false even if you have not closed the client
     * yourself. Monitoring the optional errorProcessor that can be set in {@link MessageFeedbackProcessorClientOptions}
     * will provide the context on when connection loss events occur, and why they occurred.
     *
     * @return true if this client is currently open and false otherwise.
     */
    public synchronized boolean isRunning()
    {
        return this.reactorRunner != null && this.reactorRunner.isRunning();
    }
}
