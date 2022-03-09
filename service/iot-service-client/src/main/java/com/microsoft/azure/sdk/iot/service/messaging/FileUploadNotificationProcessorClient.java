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
 * A client for handling file upload notifications. For more details on what file upload notifications are, see
 * <a href="https://docs.microsoft.com/azure/iot-hub/iot-hub-devguide-file-upload#service-file-upload-notifications">this document</a>.
 *
 * <p>
 *     This client relies on a persistent amqp/amqp_ws connection to IoT Hub that may break due to network instability.
 *     While optional to monitor, users are highly encouraged to utilize the errorProcessorHandler defined in the
 *     {@link FileUploadNotificationProcessorClientOptions} when constructing this client in order to monitor the connection state and to re-open
 *     the connection when needed. See the message feedback processor client sample in this repo for best practices for
 *     monitoring and handling disconnection events.
 * </p>
 */
@Slf4j
public class FileUploadNotificationProcessorClient
{
    private static final int START_REACTOR_TIMEOUT_MILLISECONDS = 60 * 1000; // 60 seconds
    private static final int STOP_REACTOR_TIMEOUT_MILLISECONDS = 5 * 1000; // 5 seconds

    private final EventReceivingConnectionHandler eventReceivingConnectionHandler;
    private final Consumer<ErrorContext> errorProcessor; // may be null if user doesn't provide one

    private ReactorRunner reactorRunner;
    private final String hostName;

    /**
     * Construct a FileUploadNotificationProcessorClient using a {@link TokenCredential} instance for authentication.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param fileUploadNotificationProcessor The callback to be executed each time a file upload notification is received
     * from the service. May not be null.
     */
    public FileUploadNotificationProcessorClient(
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol protocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor)
    {
        this(hostName, credential, protocol, fileUploadNotificationProcessor, FileUploadNotificationProcessorClientOptions.builder().build());
    }

    /**
     * Construct a FileUploadNotificationProcessorClient using a {@link TokenCredential} instance for authentication.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param fileUploadNotificationProcessor The callback to be executed each time a file upload notification is received
     * from the service. May not be null.
     * @param options The connection options to use when connecting to the service. May not be null.
     */
    public FileUploadNotificationProcessorClient(
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol protocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor,
        FileUploadNotificationProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");

        this.errorProcessor = options.getErrorProcessor();
        this.hostName = hostName;
        this.eventReceivingConnectionHandler =
            new EventReceivingConnectionHandler(
                hostName,
                credential,
                protocol,
                fileUploadNotificationProcessor,
                null,
                this.errorProcessor,
                options.getProxyOptions(),
                options.getSslContext(),
                options.getKeepAliveInterval());
    }

    /**
     * Construct a FileUploadNotificationProcessorClient using a {@link AzureSasCredential} instance for authentication.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param fileUploadNotificationProcessor The callback to be executed each time a file upload notification is received
     * from the service. May not be null.
     */
    public FileUploadNotificationProcessorClient(
        String hostName,
        AzureSasCredential azureSasCredential,
        IotHubServiceClientProtocol protocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor)
    {
        this(hostName, azureSasCredential, protocol, fileUploadNotificationProcessor, FileUploadNotificationProcessorClientOptions.builder().build());
    }

    /**
     * Construct a FileUploadNotificationProcessorClient using a {@link AzureSasCredential} instance for authentication.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param fileUploadNotificationProcessor The callback to be executed each time a file upload notification is received
     * from the service. May not be null.
     * @param options The connection options to use when connecting to the service. May not be null.
     */
    public FileUploadNotificationProcessorClient(
        String hostName,
        AzureSasCredential azureSasCredential,
        IotHubServiceClientProtocol protocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor,
        FileUploadNotificationProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");

        this.errorProcessor = options.getErrorProcessor();
        this.hostName = hostName;
        this.eventReceivingConnectionHandler =
            new EventReceivingConnectionHandler(
                hostName,
                azureSasCredential,
                protocol,
                fileUploadNotificationProcessor,
                null,
                this.errorProcessor,
                options.getProxyOptions(),
                options.getSslContext(),
                options.getKeepAliveInterval());
    }

    /**
     * Construct a FileUploadNotificationProcessorClient from the provided connection string.
     *
     * @param connectionString The connection string for the Iot Hub.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param fileUploadNotificationProcessor The callback to be executed each time a file upload notification is received
     * from the service. May not be null.
     */
    public FileUploadNotificationProcessorClient(
        String connectionString,
        IotHubServiceClientProtocol protocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor)
    {
        this(connectionString, protocol, fileUploadNotificationProcessor, FileUploadNotificationProcessorClientOptions.builder().build());
    }

    /**
     * Construct a FileUploadNotificationProcessorClient from the provided connection string.
     *
     * @param connectionString The connection string for the Iot Hub.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param fileUploadNotificationProcessor The callback to be executed each time a file upload notification is received
     * from the service. May not be null.
     * @param options The connection options to use when connecting to the service. May not be null.
     */
    public FileUploadNotificationProcessorClient(
        String connectionString,
        IotHubServiceClientProtocol protocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor,
        FileUploadNotificationProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");

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
                fileUploadNotificationProcessor,
                null,
                this.errorProcessor,
                options.getProxyOptions(),
                options.getSslContext(),
                options.getKeepAliveInterval());
    }

    /**
     * Open this client so that it can begin processing file upload notifications. When you want to stop processing file
     * upload notifications, you should should call {@link #stop()} to free up network resources. If this
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
     * Open this client so that it can begin processing file upload notifications. When you want to stop processing file
     * upload notifications, you should should call {@link #stop()} to free up network resources. If this
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
        if (this.isRunning())
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

        log.debug("Opening FileUploadNotificationProcessorClient");

        this.reactorRunner = new ReactorRunner(
            this.hostName,
            "FileUploadNotificationProcessor",
            this.eventReceivingConnectionHandler);

        final CountDownLatch openLatch = new CountDownLatch(1);
        this.eventReceivingConnectionHandler.setOnConnectionOpenedCallback(openLatch::countDown);

        new Thread(() ->
        {
            try
            {
                reactorRunner.run();

                log.trace("FileUploadNotificationProcessorClient Amqp reactor stopped, checking that the connection was opened");
                this.eventReceivingConnectionHandler.verifyConnectionWasOpened();

                log.trace("FileUploadNotificationProcessorClient  reactor did successfully open the connection, returning without exception");
            }
            catch (IOException e)
            {
                ioException.set(e);
            }
            catch (IotHubException e)
            {
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

        log.info("Started FileUploadNotificationProcessorClient");
    }

    /**
     * Stops this client from processing any more file upload notifications and releases all network resources tied to
     * it. Once stopped, this client can be restarted by calling {@link #start()}. If this client has already been stopped,
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
     * Stops this client from processing any more file upload notifications and releases all network resources tied to
     * it. Once stopped, this client can be restarted by calling {@link #start()}. If this client has already been stopped,
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

        this.reactorRunner.stop(timeoutMilliseconds);
        this.reactorRunner = null;

        log.info("Stopped FileUploadNotificationProcessorClient");
    }

    /**
     * Returns true if this client's underlying amqp connection is currently open and false otherwise. This client may
     * lose connectivity due to network issues, so this value may be false even if you have not closed the client
     * yourself. Monitoring the optional errorProcessor that can be set in {@link FileUploadNotificationProcessorClientOptions}
     * will provide the context on when connection loss events occur, and why they occurred.
     *
     * @return true if this client is currently open and false otherwise.
     */
    public synchronized boolean isRunning()
    {
        return this.reactorRunner != null && this.reactorRunner.isRunning();
    }
}
