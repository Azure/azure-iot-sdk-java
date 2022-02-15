/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSendHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

/**
 * Use the MessagingClient to send and monitor messages to devices in IoT hubs.
 * It can also be used to know when files have been uploaded by devices.
 */
@Slf4j
public final class MessagingClient
{
    private final AmqpSendHandler amqpSendHandler;

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

        this.amqpSendHandler =
            new AmqpSendHandler(
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

        this.amqpSendHandler =
            new AmqpSendHandler(
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
        this(hostName,
                azureSasCredential,
                protocol,
                MessagingClientOptions.builder().build());
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

        this.amqpSendHandler =
            new AmqpSendHandler(
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
        this.amqpSendHandler.send(deviceId, moduleId, message);
    }
}
