/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSend;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

/**
 * Use the ServiceClient to send and monitor messages to devices in IoT hubs.
 * It can also be used to know when files have been uploaded by devices.
 */
@Slf4j
public final class ServiceClient
{
    private final AmqpSend amqpMessageSender;
    private final String hostName;
    private String userName;
    private String sasToken;
    private final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private TokenCredential credential;
    private AzureSasCredential sasTokenProvider;

    private final ServiceClientOptions options;

    /**
     * Create ServiceClient from the specified connection string
     * @param iotHubServiceClientProtocol  protocol to use
     * @param connectionString The connection string for the IotHub
     */
    public ServiceClient(String connectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        this(connectionString, iotHubServiceClientProtocol, ServiceClientOptions.builder().build());
    }

    /**
     * Create ServiceClient from the specified connection string
     * @param iotHubServiceClientProtocol  protocol to use
     * @param connectionString The connection string for the IotHub
     * @param options The connection options to use when connecting to the service.
     */
    public ServiceClient(
            String connectionString,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ServiceClientOptions options)
    {
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException(connectionString);
        }

        if (options == null)
        {
            throw new IllegalArgumentException("ServiceClientOptions cannot be null for this constructor");
        }

        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);

        this.hostName = iotHubConnectionString.getHostName();
        this.userName = iotHubConnectionString.getUserString();
        this.sasToken = new IotHubServiceSasToken(iotHubConnectionString).toString();
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.options = options;
        this.amqpMessageSender =
                new AmqpSend(
                        this.hostName,
                        this.userName,
                        this.sasToken,
                        iotHubServiceClientProtocol,
                        options.getProxyOptions(),
                        options.getSslContext());

        commonConstructorSetup();
    }

    /**
     * Create a {@link ServiceClient} instance with a custom {@link TokenCredential} to allow for finer grain control
     * of authentication tokens used in the underlying connection.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param iotHubServiceClientProtocol The protocol to open the connection with.
     */
    public ServiceClient(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        this(hostName,
             credential,
             iotHubServiceClientProtocol,
             ServiceClientOptions.builder().build());
    }

    /**
     * Create a {@link ServiceClient} instance with a custom {@link TokenCredential} to allow for finer grain control
     * of authentication tokens used in the underlying connection.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     * @param iotHubServiceClientProtocol The protocol to open the connection with.
     * @param options The connection options to use when connecting to the service.
     */
    public ServiceClient(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ServiceClientOptions options)
    {
        Objects.requireNonNull(credential);

        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("HostName cannot be null or empty");
        }

        if (options == null)
        {
            throw new IllegalArgumentException("ServiceClientOptions cannot be null for this constructor");
        }

        this.credential = credential;
        this.hostName = hostName;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.options = options;

        if (this.options.getProxyOptions() != null && this.iotHubServiceClientProtocol != IotHubServiceClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Proxies are only supported over AMQPS_WS");
        }

        this.amqpMessageSender =
                new AmqpSend(
                        hostName,
                        credential,
                        this.iotHubServiceClientProtocol,
                        options.getProxyOptions(),
                        options.getSslContext());

        commonConstructorSetup();
    }

    /**
     * Create a {@link ServiceClient} instance with an instance of {@link AzureSasCredential}.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param iotHubServiceClientProtocol The protocol to open the connection with.
     */
    public ServiceClient(
            String hostName,
            AzureSasCredential azureSasCredential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        this(hostName,
                azureSasCredential,
                iotHubServiceClientProtocol,
                ServiceClientOptions.builder().build());
    }

    /**
     * Create a {@link ServiceClient} instance with an instance of {@link AzureSasCredential}.
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     * @param iotHubServiceClientProtocol The protocol to open the connection with.
     * @param options The connection options to use when connecting to the service.
     */
    public ServiceClient(
            String hostName,
            AzureSasCredential azureSasCredential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ServiceClientOptions options)
    {
        Objects.requireNonNull(azureSasCredential);
        Objects.requireNonNull(options);

        this.hostName = hostName;
        this.sasTokenProvider = azureSasCredential;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.options = options;
        this.amqpMessageSender =
                new AmqpSend(
                        hostName,
                        azureSasCredential,
                        iotHubServiceClientProtocol,
                        options.getProxyOptions(),
                        options.getSslContext());

        commonConstructorSetup();
    }

    /**
     * Initialize AMQP sender using given connection string
     *
     * @param iotHubConnectionString The ConnectionString object for the IotHub
     * @param iotHubServiceClientProtocol protocol to use
     */
    private ServiceClient(IotHubConnectionString iotHubConnectionString, IotHubServiceClientProtocol iotHubServiceClientProtocol)
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
    private ServiceClient(
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

        commonConstructorSetup();
    }

    private static void commonConstructorSetup()
    {
        log.debug("Initialized a ServiceClient instance using SDK version {}", TransportUtils.serviceVersion);
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
     * Instantiate a new FeedbackReceiver object.
     *
     * @return The instance of the FeedbackReceiver
     */
     public FeedbackReceiver getFeedbackReceiver()
     {
         if (this.credential != null)
         {
             return new FeedbackReceiver(
                     this.hostName,
                     this.credential,
                     this.iotHubServiceClientProtocol,
                     this.options.getProxyOptions(),
                     this.options.getSslContext());
         }
         else if (this.sasTokenProvider != null)
         {
             return new FeedbackReceiver(
                     this.hostName,
                     this.sasTokenProvider,
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
        if (this.credential != null)
        {
            return new FileUploadNotificationReceiver(
                    this.hostName,
                    this.credential,
                    this.iotHubServiceClientProtocol,
                    this.options.getProxyOptions(),
                    this.options.getSslContext());
        }
        else if (this.sasTokenProvider != null)
        {
            return new FileUploadNotificationReceiver(
                    this.hostName,
                    this.sasTokenProvider,
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
