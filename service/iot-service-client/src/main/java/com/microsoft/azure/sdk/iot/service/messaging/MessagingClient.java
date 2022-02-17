/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.amqps.CloudToDeviceMessageConnectionHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Use the MessagingClient to send and monitor messages to devices in IoT hubs.
 * It can also be used to know when files have been uploaded by devices.
 */
@Slf4j
public final class MessagingClient
{
    private static final int STOP_REACTOR_TIMEOUT_MILLISECONDS = 10 * 1000; // 10 seconds
    private static final int MESSAGE_SEND_TIMEOUT_MILLISECONDS = 30 * 1000;

    private final Consumer<ErrorContext> errorProcessor; // may be null if user doesn't provide one
    private final CloudToDeviceMessageConnectionHandler cloudToDeviceMessageConnectionHandler;
    private ReactorRunner reactorRunner;

    /**
     * Create MessagingClient from the specified connection string
     * @param protocol  protocol to use
     * @param connectionString The connection string for the IotHub
     */
    public MessagingClient(String connectionString, IotHubServiceClientProtocol protocol)
    {
        this(connectionString, protocol, MessagingClientOptions.builder().build());
    }

    /**
     * Create MessagingClient from the specified connection string
     * @param protocol  protocol to use
     * @param connectionString The connection string for the IotHub
     * @param options The connection options to use when connecting to the service.
     */
    public MessagingClient(
            String connectionString,
            IotHubServiceClientProtocol protocol,
            MessagingClientOptions options)
    {
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException(connectionString);
        }

        if (options == null)
        {
            throw new IllegalArgumentException("MessagingClientOptions cannot be null for this constructor");
        }

        this.errorProcessor = options.getErrorProcessor();
        this.cloudToDeviceMessageConnectionHandler =
            new CloudToDeviceMessageConnectionHandler(
                connectionString,
                protocol,
                options.getProxyOptions(),
                options.getSslContext());

        commonConstructorSetup();
    }

    /**
     * Create a {@link MessagingClient} instance with a custom {@link TokenCredential} to allow for finer grain control
     * of authentication tokens used in the underlying connection.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param protocol The protocol to open the connection with.
     */
    public MessagingClient(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol protocol)
    {
        this(hostName, credential, protocol, MessagingClientOptions.builder().build());
    }

    /**
     * Create a {@link MessagingClient} instance with a custom {@link TokenCredential} to allow for finer grain control
     * of authentication tokens used in the underlying connection.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param protocol The protocol to open the connection with.
     * @param options The connection options to use when connecting to the service.
     */
    public MessagingClient(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol protocol,
            MessagingClientOptions options)
    {
        Objects.requireNonNull(credential);

        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("HostName cannot be null or empty");
        }

        if (options == null)
        {
            throw new IllegalArgumentException("MessagingClientOptions cannot be null for this constructor");
        }

        if (options.getProxyOptions() != null && protocol != IotHubServiceClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Proxies are only supported over AMQPS_WS");
        }

        this.errorProcessor = options.getErrorProcessor();
        this.cloudToDeviceMessageConnectionHandler =
            new CloudToDeviceMessageConnectionHandler(
                hostName,
                credential,
                protocol,
                options.getProxyOptions(),
                options.getSslContext());

        commonConstructorSetup();
    }

    /**
     * Create a {@link MessagingClient} instance with an instance of {@link AzureSasCredential}.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param protocol The protocol to open the connection with.
     */
    public MessagingClient(
            String hostName,
            AzureSasCredential azureSasCredential,
            IotHubServiceClientProtocol protocol)
    {
        this(hostName, azureSasCredential, protocol, MessagingClientOptions.builder().build());
    }

    /**
     * Create a {@link MessagingClient} instance with an instance of {@link AzureSasCredential}.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param protocol The protocol to open the connection with.
     * @param options The connection options to use when connecting to the service.
     */
    public MessagingClient(
            String hostName,
            AzureSasCredential azureSasCredential,
            IotHubServiceClientProtocol protocol,
            MessagingClientOptions options)
    {
        Objects.requireNonNull(azureSasCredential);
        Objects.requireNonNull(options);

        if (options.getProxyOptions() != null && protocol != IotHubServiceClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Proxies are only supported over AMQPS_WS");
        }

        this.errorProcessor = options.getErrorProcessor();
        this.cloudToDeviceMessageConnectionHandler =
            new CloudToDeviceMessageConnectionHandler(
                hostName,
                azureSasCredential,
                protocol,
                options.getProxyOptions(),
                options.getSslContext());

        commonConstructorSetup();
    }

    private static void commonConstructorSetup()
    {
        log.debug("Initialized a MessagingClient instance using SDK version {}", TransportUtils.serviceVersion);
    }

    public synchronized void open() throws IotHubException, IOException, InterruptedException
    {
        if (this.reactorRunner != null && this.cloudToDeviceMessageConnectionHandler != null && this.cloudToDeviceMessageConnectionHandler.isOpen())
        {
            //already open
            return;
        }

        log.debug("Opening MessagingClient");

        this.reactorRunner = new ReactorRunner(
            this.cloudToDeviceMessageConnectionHandler.getHostName(),
            "AmqpFileUploadNotificationAndCloudToDeviceFeedbackReceiver",
            this.cloudToDeviceMessageConnectionHandler);

        final CountDownLatch openLatch = new CountDownLatch(1);
        this.cloudToDeviceMessageConnectionHandler.setOnConnectionOpenedCallback(e ->
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
                this.cloudToDeviceMessageConnectionHandler.verifyConnectionWasOpened();

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

        log.debug("Opened MessagingClient");
    }

    public synchronized void close() throws InterruptedException
    {
        this.close(STOP_REACTOR_TIMEOUT_MILLISECONDS);
    }

    public synchronized void close(int timeoutMilliseconds) throws InterruptedException
    {
        if (this.reactorRunner == null)
        {
            return;
        }

        this.reactorRunner.stop(timeoutMilliseconds);
        this.reactorRunner = null;
    }

    public void send(String deviceId, Message message) throws IOException, IotHubException, InterruptedException
    {
        this.send(deviceId, null, message, MESSAGE_SEND_TIMEOUT_MILLISECONDS);
    }

    public void send(String deviceId, Message message, int timeoutMilliseconds) throws IOException, IotHubException, InterruptedException
    {
        this.send(deviceId, null, message, timeoutMilliseconds);
    }

    public void send(String deviceId, String moduleId, Message message) throws IOException, IotHubException, InterruptedException
    {
        this.send(deviceId, moduleId, message, MESSAGE_SEND_TIMEOUT_MILLISECONDS);
    }

    public void send(String deviceId, String moduleId, Message message, int timeoutMilliseconds) throws IOException, IotHubException, InterruptedException
    {
        if (timeoutMilliseconds < 0)
        {
            throw new IllegalArgumentException("timeoutMilliseconds must be greater than or equal to 0");
        }

        AtomicReference<IotHubException> exception = new AtomicReference<>();
        final CountDownLatch messageSentLock = new CountDownLatch(1);

        Consumer<SendResult> onMessageAcknowledgedCallback = sendResult ->
        {
            if (!sendResult.wasSentSuccessfully())
            {
                exception.set(sendResult.getException());
            }

            messageSentLock.countDown();
        };

        this.sendAsync(deviceId, moduleId, message, onMessageAcknowledgedCallback, null);

        if (timeoutMilliseconds == 0)
        {
            // wait indefinitely
            messageSentLock.await();
        }
        else
        {
            boolean timedOut = !messageSentLock.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw new IotHubException("Timed out waiting for message to be acknowledged");
            }
        }
    }

    public void sendAsync(String deviceId, Message message, Consumer<SendResult> onMessageSentCallback, Object context)
    {
        this.sendAsync(deviceId, null, message, onMessageSentCallback, context);
    }

    public void sendAsync(String deviceId, String moduleId, Message message, Consumer<SendResult> onMessageSentCallback, Object context)
    {
        this.cloudToDeviceMessageConnectionHandler.sendAsync(deviceId, moduleId, message, onMessageSentCallback, context);
    }
}
