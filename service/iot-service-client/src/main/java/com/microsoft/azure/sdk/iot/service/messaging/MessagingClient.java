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
 * A client for sending cloud to device and cloud to module messages. For more details on what cloud to device messages
 * are, see <a href="https://docs.microsoft.com/azure/iot-hub/iot-hub-devguide-messages-c2d#message-feedback">this document</>.
 *
 *<p>
 *     This client relies on a persistent amqp/amqp_ws connection to IoT Hub that may break due to network instability.
 *     While optional to monitor, users are highly encouraged to utilize the errorProcessorHandler defined in the
 *     {@link MessagingClientOptions} when constructing this client in order to monitor the connection state and to re-open
 *     the connection when needed. See the messaging client sample in this repo for best practices for monitoring and handling
 *     disconnection events.
 *</p>
 */
@Slf4j
public final class MessagingClient
{
    private static final int START_REACTOR_TIMEOUT_MILLISECONDS = 60 * 1000; // 60 seconds
    private static final int STOP_REACTOR_TIMEOUT_MILLISECONDS = 60 * 1000; // 60 seconds
    private static final int MESSAGE_SEND_TIMEOUT_MILLISECONDS = 60 * 1000; // 60 seconds

    private final Consumer<ErrorContext> errorProcessor; // may be null if user doesn't provide one
    private final CloudToDeviceMessageConnectionHandler cloudToDeviceMessageConnectionHandler;
    private ReactorRunner reactorRunner;

    /**
     * Construct a MessagingClient from the specified connection string
     * @param connectionString The connection string for the IotHub
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     */
    public MessagingClient(String connectionString, IotHubServiceClientProtocol protocol)
    {
        this(connectionString, protocol, MessagingClientOptions.builder().build());
    }

    /**
     * Construct a MessagingClient from the specified connection string
     * @param connectionString The connection string for the IotHub
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param options The connection options to use when connecting to the service. May not be null.
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
                this.errorProcessor,
                options.getProxyOptions(),
                options.getSslContext(),
                options.getKeepAliveInterval());

        commonConstructorSetup();
    }

    /**
     * Create a {@link MessagingClient} instance with a custom {@link TokenCredential} to allow for finer grain control
     * of authentication tokens used in the underlying connection.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
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
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param options The connection options to use when connecting to the service. May not be null.
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
                this.errorProcessor,
                options.getProxyOptions(),
                options.getSslContext(),
                options.getKeepAliveInterval());

        commonConstructorSetup();
    }

    /**
     * Create a {@link MessagingClient} instance with an instance of {@link AzureSasCredential}.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param protocol The protocol that the client will communicate to IoT Hub over.
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
     * @param protocol The protocol that the client will communicate to IoT Hub over.
     * @param options The connection options to use when connecting to the service. May not be null.
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
                this.errorProcessor,
                options.getProxyOptions(),
                options.getSslContext(),
                options.getKeepAliveInterval());

        commonConstructorSetup();
    }

    private static void commonConstructorSetup()
    {
        log.debug("Initialized a MessagingClient instance using SDK version {}", TransportUtils.serviceVersion);
    }

    /**
     * Open this client so that it can begin sending cloud to device and/or cloud to module messages. Once opened, this
     * client should call {@link #close()} once no more messages will be sent in order to free up network resources. If this
     * client is already open, then this function will do nothing.
     *
     * @throws IotHubException If any IoT Hub level exceptions occur such as an {@link com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException}.
     * @throws IOException If any network level exceptions occur such as the connection timing out.
     * @throws InterruptedException If this thread is interrupted while waiting for the connection to the service to open.
     */
    public synchronized void open() throws IotHubException, IOException, InterruptedException
    {
        open(START_REACTOR_TIMEOUT_MILLISECONDS);
    }

    /**
     * Open this client so that it can begin sending cloud to device and/or cloud to module messages. Once opened, this
     * client should call {@link #close()} once no more messages will be sent in order to free up network resources. If this
     * client is already open, then this function will do nothing.
     *
     * @param timeoutMilliseconds the maximum number of milliseconds to wait for the underlying amqp connection to open.
     * If this value is 0, it will have an infinite timeout.
     * @throws IotHubException If any IoT Hub level exceptions occur such as an {@link com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException}.
     * @throws IOException If any network level exceptions occur such as the connection timing out.
     * @throws InterruptedException If this thread is interrupted while waiting for the connection to the service to open.
     */
    public synchronized void open(int timeoutMilliseconds) throws IotHubException, IOException, InterruptedException
    {
        if (this.reactorRunner != null && this.cloudToDeviceMessageConnectionHandler != null && this.cloudToDeviceMessageConnectionHandler.isOpen())
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

        log.debug("Opening MessagingClient");

        this.reactorRunner = new ReactorRunner(
            this.cloudToDeviceMessageConnectionHandler.getHostName(),
            "MessagingClient",
            this.cloudToDeviceMessageConnectionHandler);

        final CountDownLatch openLatch = new CountDownLatch(1);
        this.cloudToDeviceMessageConnectionHandler.setOnConnectionOpenedCallback(() -> openLatch.countDown());

        new Thread(() ->
        {
            try
            {
                reactorRunner.run();

                log.trace("MessagingClient Amqp reactor stopped, checking that the connection was opened");
                this.cloudToDeviceMessageConnectionHandler.verifyConnectionWasOpened();

                log.trace("MessagingClient reactor did successfully open the connection, returning without exception");
            }
            catch (IOException e)
            {
                log.debug("MessagingClient Amqp connection encountered an exception", e);

                ioException.set(e);
            }
            catch (IotHubException e)
            {
                log.debug("MessagingClient Amqp connection encountered an exception", e);

                iotHubException.set(e);
            }
            finally
            {
                openLatch.countDown();
            }
        }).start();

        boolean timedOut = !openLatch.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);

        if (timedOut)
        {
            throw new IOException("Timed out waiting for the connection to the service to open");
        }

        if (ioException.get() != null)
        {
            throw ioException.get();
        }

        if (iotHubException.get() != null)
        {
            throw iotHubException.get();
        }

        log.info("Opened MessagingClient");
    }

    /**
     * Close this client and release all network resources tied to it. Once closed, this client can be re-opened by
     * calling {@link #open()}. If this client is already closed, this function will do nothing.
     *
     * @throws InterruptedException if this function is interrupted while waiting for the connection to close down all
     * network resources.
     */
    public synchronized void close() throws InterruptedException
    {
        this.close(STOP_REACTOR_TIMEOUT_MILLISECONDS);
    }

    /**
     * Close this client and release all network resources tied to it. Once closed, this client can be re-opened by
     * calling {@link #open()}. If this client is already closed, this function will do nothing.
     *
     * @param timeoutMilliseconds the maximum number of milliseconds to wait for the underlying amqp connection to close.
     * If this value is 0, it will have an infinite timeout.
     * @throws InterruptedException if this function is interrupted while waiting for the connection to close down all
     * network resources.
     */
    public synchronized void close(int timeoutMilliseconds) throws InterruptedException
    {
        if (this.reactorRunner == null)
        {
            return;
        }

        if (timeoutMilliseconds < 0)
        {
            throw new IllegalArgumentException("timeoutMilliseconds must be greater than or equal to 0");
        }

        this.reactorRunner.stop(timeoutMilliseconds);
        this.reactorRunner = null;
        log.info("Closed MessagingClient");
    }

    /**
     * Send a cloud to device message to the device with the provided device id.
     *
     * <p>
     *     This method is a blocking call that will wait for the sent message to be acknowledged by the service before returning.
     *     This is provided for simplicity and for applications that aren't concerned with throughput. For applications that
     *     need to provided higher throughput of sent cloud to device messages, users should use {@link #sendAsync(String, Message, Consumer, Object)}
     *     as demonstrated in the messaging client performance sample in this repo.
     * </p>
     * @param deviceId the Id of the device to send the cloud to device message to.
     * @param message the message to send to the device.
     * @throws IotHubException If any IoT Hub level exception is thrown. For instance, if the provided message exceeds
     * the IoT Hub message size limit, {@link com.microsoft.azure.sdk.iot.service.exceptions.IotHubMessageTooLargeException} will be thrown.
     * @throws InterruptedException If this function is interrupted while waiting for the cloud to device message to be acknowledged
     * by the service.
     */
    public void send(String deviceId, Message message) throws IotHubException, InterruptedException
    {
        this.send(deviceId, null, message, MESSAGE_SEND_TIMEOUT_MILLISECONDS);
    }

    /**
     * Send a cloud to device message to the device with the provided device id.
     *
     * <p>
     *     This method is a blocking call that will wait for the sent message to be acknowledged by the service before returning.
     *     This is provided for simplicity and for applications that aren't concerned with throughput. For applications that
     *     need to provided higher throughput of sent cloud to device messages, users should use {@link #sendAsync(String, Message, Consumer, Object)}
     *     as demonstrated in the messaging client performance sample in this repo.
     * </p>
     * @param deviceId the Id of the device to send the cloud to device message to.
     * @param message the message to send to the device.
     * @param timeoutMilliseconds the maximum number of milliseconds to wait for the message to be sent before timing out and throwing an {@link IotHubException}.
     * @throws IotHubException If any IoT Hub level exception is thrown. For instance, if the provided message exceeds
     * the IoT Hub message size limit, {@link com.microsoft.azure.sdk.iot.service.exceptions.IotHubMessageTooLargeException} will be thrown.
     * @throws InterruptedException If this function is interrupted while waiting for the cloud to device message to be acknowledged
     * by the service.
     */
    public void send(String deviceId, Message message, int timeoutMilliseconds) throws IotHubException, InterruptedException
    {
        this.send(deviceId, null, message, timeoutMilliseconds);
    }

    /**
     * Send a cloud to device message to the module with the provided module id on the device with the provided device Id.
     *
     * <p>
     *     This method is a blocking call that will wait for the sent message to be acknowledged by the service before returning.
     *     This is provided for simplicity and for applications that aren't concerned with throughput. For applications that
     *     need to provided higher throughput of sent cloud to device messages, users should use {@link #sendAsync(String, String, Message, Consumer, Object)}
     *     as demonstrated in the messaging client performance sample in this repo.
     * </p>
     * @param deviceId the Id of the device that contains the module that the message is being sent to.
     * @param moduleId the Id of the module to send the cloud to device message to.
     * @param message the message to send to the device.
     * @throws IotHubException If any IoT Hub level exception is thrown. For instance, if the provided message exceeds
     * the IoT Hub message size limit, {@link com.microsoft.azure.sdk.iot.service.exceptions.IotHubMessageTooLargeException} will be thrown.
     * @throws InterruptedException If this function is interrupted while waiting for the cloud to device message to be acknowledged
     * by the service.
     */
    public void send(String deviceId, String moduleId, Message message) throws IotHubException, InterruptedException
    {
        this.send(deviceId, moduleId, message, MESSAGE_SEND_TIMEOUT_MILLISECONDS);
    }

    /**
     * Send a cloud to device message to the module with the provided module id on the device with the provided device Id.
     *
     * <p>
     *     This method is a blocking call that will wait for the sent message to be acknowledged by the service before returning.
     *     This is provided for simplicity and for applications that aren't concerned with throughput. For applications that
     *     need to provided higher throughput of sent cloud to device messages, users should use {@link #sendAsync(String, String, Message, Consumer, Object)}
     *     as demonstrated in the messaging client performance sample in this repo.
     * </p>
     * @param deviceId the Id of the device that contains the module that the message is being sent to.
     * @param moduleId the Id of the module to send the cloud to device message to.
     * @param message the message to send to the device.
     * @param timeoutMilliseconds the maximum number of milliseconds to wait for the message to be sent before timing out and throwing an {@link IotHubException}.
     * @throws IotHubException If any IoT Hub level exception is thrown. For instance, if the provided message exceeds
     * the IoT Hub message size limit, {@link com.microsoft.azure.sdk.iot.service.exceptions.IotHubMessageTooLargeException} will be thrown.
     * @throws InterruptedException If this function is interrupted while waiting for the cloud to device message to be acknowledged
     * by the service.
     */
    public void send(String deviceId, String moduleId, Message message, int timeoutMilliseconds) throws IotHubException, InterruptedException
    {
        if (timeoutMilliseconds < 0)
        {
            throw new IllegalArgumentException("timeoutMilliseconds must be greater than or equal to 0");
        }

        AtomicReference<IotHubException> exception = new AtomicReference<>();
        final CountDownLatch messageSentLatch = new CountDownLatch(1);

        Consumer<SendResult> onMessageAcknowledgedCallback = sendResult ->
        {
            if (sendResult.wasSentSuccessfully())
            {
                log.trace("Message acknowledged callback executed for cloud to device message with correlation id {} that was successfully sent.", sendResult.getCorrelationId());
            }
            else
            {
                log.trace("Message acknowledged callback executed for cloud to device message with correlation id {} that failed to send.", sendResult.getCorrelationId());
                exception.set(sendResult.getException());
            }

            messageSentLatch.countDown();
        };

        this.sendAsync(deviceId, moduleId, message, onMessageAcknowledgedCallback, null);

        if (timeoutMilliseconds == 0)
        {
            // wait indefinitely
            messageSentLatch.await();
        }
        else
        {
            boolean timedOut = !messageSentLatch.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw new IotHubException("Timed out waiting for message to be acknowledged");
            }
        }

        if (exception.get() != null)
        {
            throw exception.get();
        }
    }

    /**
     * Asynchronously send a cloud to device message to the device with the provided device Id.
     * <p>
     *     Unlike the synchronous version of this function, this function does not throw any exceptions. Instead, any exception
     *     encountered while sending the message will be provided in the {@link SendResult} provided in the onMessageSentCallback.
     *     To see an example of how this looks, see the messaging client performance sample in this repo.
     * </p>
     * @param deviceId the Id of the device to send the cloud to device message to.
     * @param message the message to send to the device.
     * @param onMessageSentCallback the callback that will be executed when the message has either successfully been
     * sent, or has failed to send. May be null if you don't care if the sent message is acknowledged by the service.
     * @param context user defined context that will be provided in the onMessageSentCallback callback when it executes. May be null.
     */
    public void sendAsync(String deviceId, Message message, Consumer<SendResult> onMessageSentCallback, Object context)
    {
        this.sendAsync(deviceId, null, message, onMessageSentCallback, context);
    }

    /**
     * Asynchronously send a cloud to device message to the module with the provided module id on the device with the provided device Id.
     * <p>
     *     Unlike the synchronous version of this function, this function does not throw any exceptions. Instead, any exception
     *     encountered while sending the message will be provided in the {@link SendResult} provided in the onMessageSentCallback.
     *     To see an example of how this looks, see the messaging client performance sample in this repo.
     * </p>
     * @param deviceId the Id of the device that contains the module that the message is being sent to.
     * @param moduleId the Id of the module to send the cloud to device message to.
     * @param message the message to send to the device.
     * @param onMessageSentCallback the callback that will be executed when the message has either successfully been
     * sent, or has failed to send. May be null if you don't care if the sent message is acknowledged by the service.
     * @param context user defined context that will be provided in the onMessageSentCallback callback when it executes. May be null.
     */
    public void sendAsync(String deviceId, String moduleId, Message message, Consumer<SendResult> onMessageSentCallback, Object context)
    {
        this.cloudToDeviceMessageConnectionHandler.sendAsync(deviceId, moduleId, message, onMessageSentCallback, context);
    }

    /**
     * @return true if this client is currently open and false otherwise.
     */
    public boolean isOpen()
    {
        return this.reactorRunner != null && this.reactorRunner.isRunning();
    }
}
