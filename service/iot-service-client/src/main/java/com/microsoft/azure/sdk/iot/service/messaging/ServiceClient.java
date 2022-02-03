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
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSendHandler;
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
    private final String hostName;
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
        this.sasToken = new IotHubServiceSasToken(iotHubConnectionString).toString();
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.options = options;

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
        this.sasToken = iotHubServiceSasToken.toString();
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.options = options;

        if (this.options.getProxyOptions() != null && this.iotHubServiceClientProtocol != IotHubServiceClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Proxies are only supported over AMQPS_WS");
        }

        commonConstructorSetup();
    }

    private static void commonConstructorSetup()
    {
        log.debug("Initialized a ServiceClient instance using SDK version {}", TransportUtils.serviceVersion);
    }

    /**
     * Send a one-way message to the specified device.
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
     * Send a one-way message to the specified module.
     *
     * @param deviceId The device identifier for the target device
     * @param moduleId The module identifier for the target device
     * @param message The message for the device
     * @throws IOException This exception is thrown if the AmqpSender object is not initialized
     * @throws IotHubException This exception is thrown if IotHub rejects the message for any reason
     */
    public void send(String deviceId, String moduleId, Message message) throws IOException, IotHubException
    {
        AmqpSendHandler amqpSendHandler;
        if (this.credential != null)
        {
            amqpSendHandler =
                new AmqpSendHandler(
                    this.hostName,
                    this.credential,
                    this.iotHubServiceClientProtocol,
                    this.options.getProxyOptions(),
                    this.options.getSslContext());
        }
        else if (this.sasTokenProvider != null)
        {
            amqpSendHandler =
                new AmqpSendHandler(
                    this.hostName,
                    this.sasTokenProvider,
                    this.iotHubServiceClientProtocol,
                    this.options.getProxyOptions(),
                    this.options.getSslContext());
        }
        else
        {
            amqpSendHandler =
                new AmqpSendHandler(
                    this.hostName,
                    this.sasToken,
                    this.iotHubServiceClientProtocol,
                    this.options.getProxyOptions(),
                    this.options.getSslContext());
        }

        amqpSendHandler.send(deviceId, moduleId, message);
    }

    /**
     * Instantiate a new FeedbackReceiver object.
     *
     * @return The instance of the FeedbackReceiver
     */
     public FeedbackReceiver getFeedbackReceiver(FeedbackMessageReceivedCallback feedbackMessageReceivedCallback)
     {
         if (this.credential != null)
         {
             return new FeedbackReceiver(
                 feedbackMessageReceivedCallback,
                 this.hostName,
                 this.credential,
                 this.iotHubServiceClientProtocol,
                 this.options.getProxyOptions(),
                 this.options.getSslContext());
         }
         else if (this.sasTokenProvider != null)
         {
             return new FeedbackReceiver(
                 feedbackMessageReceivedCallback,
                 this.hostName,
                 this.sasTokenProvider,
                 this.iotHubServiceClientProtocol,
                 this.options.getProxyOptions(),
                 this.options.getSslContext());
         }

        return new FeedbackReceiver(
            feedbackMessageReceivedCallback,
            this.hostName,
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
    public FileUploadNotificationReceiver getFileUploadNotificationReceiver(FileUploadNotificationReceivedCallback fileUploadNotificationReceivedCallback)
    {
        if (this.credential != null)
        {
            return new FileUploadNotificationReceiver(
                fileUploadNotificationReceivedCallback,
                this.hostName,
                this.credential,
                this.iotHubServiceClientProtocol,
                this.options.getProxyOptions(),
                this.options.getSslContext());
        }
        else if (this.sasTokenProvider != null)
        {
            return new FileUploadNotificationReceiver(
                fileUploadNotificationReceivedCallback,
                this.hostName,
                this.sasTokenProvider,
                this.iotHubServiceClientProtocol,
                this.options.getProxyOptions(),
                this.options.getSslContext());
        }

        return new FileUploadNotificationReceiver(
            fileUploadNotificationReceivedCallback,
            this.hostName,
            this.sasToken,
            this.iotHubServiceClientProtocol,
            this.options.getProxyOptions(),
            this.options.getSslContext());
    }
}
