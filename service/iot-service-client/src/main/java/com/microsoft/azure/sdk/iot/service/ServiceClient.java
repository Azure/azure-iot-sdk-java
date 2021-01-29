/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.TokenCredentialType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringCredential;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSend;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Extend the ServiceClient class and provide AMPQ specific implementation.
 */
@Slf4j
public class ServiceClient
{
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private final AmqpSend amqpMessageSender;
    private final String hostName;
    private String userName;
    private String sasToken;
    private final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    protected TokenCredential authenticationTokenProvider;
    protected final TokenCredentialType authorizationType;

    private final ServiceClientOptions options;

    /**
     * Create ServiceClient from the specified connection string
     * @param iotHubServiceClientProtocol  protocol to use
     * @param connectionString The connection string for the IotHub
     * @return The created ServiceClient object
     * @throws IOException This exception is thrown if the object creation failed
     */
    public static ServiceClient createFromConnectionString(
            String connectionString,
            IotHubServiceClientProtocol iotHubServiceClientProtocol)
            throws IOException
    {
        return createFromConnectionString(connectionString, iotHubServiceClientProtocol, ServiceClientOptions.builder().build());
    }

    /**
     * Create ServiceClient from the specified connection string
     * @param iotHubServiceClientProtocol  protocol to use
     * @param connectionString The connection string for the IotHub
     * @param options The connection options to use when connecting to the service.
     * @return The created ServiceClient object
     * @throws IOException This exception is thrown if the object creation failed
     */
    public static ServiceClient createFromConnectionString(
            String connectionString,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ServiceClientOptions options)
            throws IOException
    {
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException(connectionString);
        }

        if (options == null)
        {
            throw new IllegalArgumentException("ServiceClientOptions cannot be null for this constructor");
        }

        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        TokenCredential authenticationTokenProvider = new IotHubConnectionStringCredential(connectionString);

        return new ServiceClient(
                iotHubConnectionString.hostName,
                authenticationTokenProvider,
                TokenCredentialType.SHARED_ACCESS_SIGNATURE,
                iotHubServiceClientProtocol,
                options);
    }

    /**
     * Create a {@link ServiceClient} instance with a custom {@link TokenCredential} to allow for finer grain control
     * of authentication tokens used in the underlying connection.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed.
     * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
     *                          implementation will always give.
     * @param iotHubServiceClientProtocol The protocol to open the connection with.
     * @return The created {@link ServiceClient} instance.
     */
    public static ServiceClient createFromTokenCredential(
            String hostName,
            TokenCredential authenticationTokenProvider,
            TokenCredentialType tokenCredentialType,
            IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        return createFromTokenCredential(
                hostName,
                authenticationTokenProvider,
                tokenCredentialType,
                iotHubServiceClientProtocol,
                ServiceClientOptions.builder().build());
    }

    /**
     * Create a {@link ServiceClient} instance with a custom {@link TokenCredential} to allow for finer grain control
     * of authentication tokens used in the underlying connection.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed.
     * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
     *                          implementation will always give.
     * @param iotHubServiceClientProtocol The protocol to open the connection with.
     * @param options The connection options to use when connecting to the service.
     * @return The created {@link ServiceClient} instance.
     */
    public static ServiceClient createFromTokenCredential(
            String hostName,
            TokenCredential authenticationTokenProvider,
            TokenCredentialType tokenCredentialType,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ServiceClientOptions options)
    {
        Objects.requireNonNull(authenticationTokenProvider);
        Objects.requireNonNull(tokenCredentialType);

        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("HostName cannot be null or empty");
        }

        if (options == null)
        {
            throw new IllegalArgumentException("ServiceClientOptions cannot be null for this constructor");
        }

        return new ServiceClient(
                hostName,
                authenticationTokenProvider,
                tokenCredentialType,
                iotHubServiceClientProtocol,
                options);
    }

    /**
     * Initialize AMQP sender using given connection string
     *
     * @param iotHubConnectionString The ConnectionString object for the IotHub
     * @param iotHubServiceClientProtocol protocol to use
     */
    protected ServiceClient(IotHubConnectionString iotHubConnectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        this(iotHubConnectionString, iotHubServiceClientProtocol, ServiceClientOptions.builder().build());
    }

    /**
     * Initialize AMQP sender using given connection string
     *
     * @param iotHubConnectionString The ConnectionString object for the IotHub
     * @param iotHubServiceClientProtocol protocol to use
     * @param options options for proxy
     */
    protected ServiceClient(
            IotHubConnectionString iotHubConnectionString,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ServiceClientOptions options)
    {
        Objects.requireNonNull(iotHubConnectionString);

        IotHubServiceSasToken iotHubServiceSasToken = new IotHubServiceSasToken(iotHubConnectionString);

        this.hostName = iotHubConnectionString.getHostName();
        this.userName = iotHubConnectionString.getUserString();
        this.sasToken = iotHubServiceSasToken.toString();
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.authorizationType = TokenCredentialType.SHARED_ACCESS_SIGNATURE;
        this.options = options;

        if (this.options.getProxyOptions() != null && this.iotHubServiceClientProtocol != IotHubServiceClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Proxies are only supported over AMQPS_WS");
        }

        this.amqpMessageSender = new AmqpSend(
                this.hostName,
                this.userName,
                this.sasToken,
                this.iotHubServiceClientProtocol,
                options.getProxyOptions(),
                options.getSslContext());
    }

    private ServiceClient(
            String hostName,
            TokenCredential authenticationTokenProvider,
            TokenCredentialType authorizationType,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ServiceClientOptions options)
    {
        this.authenticationTokenProvider = authenticationTokenProvider;
        this.hostName = hostName;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.options = options;
        this.authorizationType = authorizationType;

        if (this.options.getProxyOptions() != null && this.iotHubServiceClientProtocol != IotHubServiceClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Proxies are only supported over AMQPS_WS");
        }

        this.amqpMessageSender = new AmqpSend(
                hostName,
                authenticationTokenProvider,
                this.authorizationType,
                this.iotHubServiceClientProtocol,
                options.getProxyOptions(),
                options.getSslContext());
    }

    /**
     * Open AMQP sender
     * @throws IOException This exception is thrown if the AmqpSender object is not initialized
     */
    public void open() throws IOException
    {
        if (this.amqpMessageSender == null)
        {
            throw new IOException("AMQP sender is not initialized");
        }

        log.info("Opening service client...");

        this.amqpMessageSender.open();
        log.info("Service client opened successfully");
    }

    /**
     * Close AMQP sender
     * @throws IOException This exception is thrown if the AmqpSender object is not initialized
     */
    public void close() throws IOException
    {
        if (this.amqpMessageSender == null)
        {
            throw new IOException("AMQP sender is not initialized");
        }

        log.info("Closing service client...");
        this.amqpMessageSender.close();
        log.info("Service client closed successfully");
    }

    /**
     * Send a one-way message to the specified device. This function is synchronized internally so that only one send operation
     * is allowed at a time. In order to do more send operations at a time, you will need to instantiate another service client instance.
     *
     * @param deviceId The device identifier for the target device
     * @param message The message for the device
     * @throws IOException This exception is thrown if the AmqpSender object is not initialized
     * @throws IotHubException This exception is thrown if IotHub rejects the message for any reason
     */
    public void send(String deviceId, Message message) throws IOException, IotHubException
    {
        this.send(deviceId, null, message);
    }

    /**
     * Send a one-way message to the specified module. This function is synchronized internally so that only one send operation
     * is allowed at a time. In order to do more send operations at a time, you will need to instantiate another service client instance.
     *
     * @param deviceId The device identifier for the target device
     * @param moduleId The module identifier for the target device
     * @param message The message for the device
     * @throws IOException This exception is thrown if the AmqpSender object is not initialized
     * @throws IotHubException This exception is thrown if IotHub rejects the message for any reason
     */
    public void send(String deviceId, String moduleId, Message message) throws IOException, IotHubException
    {
        if (this.amqpMessageSender == null)
        {
            throw new IOException("AMQP sender is not initialized");
        }

        this.amqpMessageSender.send(deviceId, moduleId, message);
    }

    /**
     * Provide asynchronous access to open()
     *
     * @return The future object for the requested operation
     */
    public CompletableFuture<Void> openAsync()
    {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        executor.submit(() -> {
            try
            {
                open();
                future.complete(null);
            }
            catch (IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Provide asynchronous access to close()
     *
     * @return The future object for the requested operation
     */
    public CompletableFuture<Void> closeAsync()
    {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        executor.submit(() -> {
            try
            {
                close();
                future.complete(null);
            }
            catch (IOException e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Provide asynchronous access to send()
     *
     * @param deviceId The device identifier for the target device
     * @param message The message for the device
     * @return The future object for the requested operation
     */
    public CompletableFuture<Void> sendAsync(String deviceId, Message message)
    {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        executor.submit(() -> {
            try
            {
                send(deviceId, message);
                future.complete(null);
            }
            catch (Exception e)
            {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Get FeedbackReceiver object.This API has been deprecated. Use new API without deviceId as an input parameter.
     * @deprecated As of release 1.1.15, replaced by {@link #getFeedbackReceiver()}
     * @param deviceId The device identifier for the target device
     * @return The instance of the FeedbackReceiver
     */
    @Deprecated public FeedbackReceiver getFeedbackReceiver(String deviceId)
    {
        if (options.getProxyOptions() != null)
        {
            throw new UnsupportedOperationException("This deprecated API does not support proxies. Use the non-deprecated version of this API for proxy enabled feedback receiving");
        }

        return new FeedbackReceiver(hostName, userName, sasToken, iotHubServiceClientProtocol, deviceId);
    }
    
    /**
     * Instantiate a new FeedbackReceiver object.
     *
     * @return The instance of the FeedbackReceiver
     */
     public FeedbackReceiver getFeedbackReceiver()
     {
         if (this.authenticationTokenProvider != null)
         {
             return new FeedbackReceiver(
                     this.hostName,
                     this.authenticationTokenProvider,
                     this.authorizationType,
                     this.iotHubServiceClientProtocol,
                     this.options.getProxyOptions(),
                     this.options.getSslContext());
         }

        return new FeedbackReceiver(
                this.hostName,
                this.userName,
                this.sasToken,
                this.iotHubServiceClientProtocol,
                this.options.getProxyOptions(),
                this.options.getSslContext());
     }

    /**
     * Instantiate a new FileUploadNotificationReceiver object.
     *
     * @return The instance of the FileUploadNotificationReceiver
     */
    public FileUploadNotificationReceiver getFileUploadNotificationReceiver()
    {
        if (this.authenticationTokenProvider != null)
        {
            return new FileUploadNotificationReceiver(
                    this.hostName,
                    this.authenticationTokenProvider,
                    this.authorizationType,
                    this.iotHubServiceClientProtocol,
                    this.options.getProxyOptions(),
                    this.options.getSslContext());
        }

        return new FileUploadNotificationReceiver(
                this.hostName,
                this.userName,
                this.sasToken,
                this.iotHubServiceClientProtocol,
                this.options.getProxyOptions(),
                this.options.getSslContext());
    }
}
