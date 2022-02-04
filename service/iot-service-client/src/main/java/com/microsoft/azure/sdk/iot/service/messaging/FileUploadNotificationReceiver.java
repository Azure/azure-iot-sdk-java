/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFileUploadNotificationReceivedHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ConnectionLossCallback;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Objects;

/**
 * A receiver that listens for file upload notifications and completes/abandons each notification after they are received.
 */
@Slf4j
public class FileUploadNotificationReceiver
{
    private final AmqpFileUploadNotificationReceivedHandler amqpReceiveHandler;
    private final String hostName;
    private ReactorRunner amqpConnectionReactorRunner;

    /**
     * The callback to be executed if this receiver loses its connection unexpectedly
     */
    @Setter
    @Getter
    private ConnectionLossCallback connectionLossCallback;

    FileUploadNotificationReceiver(
        FileUploadNotificationReceivedCallback notificationReceivedCallback,
        String hostName,
        String sasToken,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }
        if (Tools.isNullOrEmpty(sasToken))
        {
            throw new IllegalArgumentException("sasToken cannot be null or empty");
        }
        if (iotHubServiceClientProtocol == null)
        {
            throw new IllegalArgumentException("iotHubServiceClientProtocol cannot be null");
        }

        Objects.requireNonNull(notificationReceivedCallback);

        this.hostName = hostName;
        this.amqpReceiveHandler = new AmqpFileUploadNotificationReceivedHandler(
            hostName,
            sasToken,
            iotHubServiceClientProtocol,
            notificationReceivedCallback,
            proxyOptions,
            sslContext);
    }

    FileUploadNotificationReceiver(
        FileUploadNotificationReceivedCallback notificationReceivedCallback,
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        Objects.requireNonNull(credential);
        Objects.requireNonNull(iotHubServiceClientProtocol);
        Objects.requireNonNull(notificationReceivedCallback);

        this.hostName = hostName;
        this.amqpReceiveHandler = new AmqpFileUploadNotificationReceivedHandler(
            hostName,
            credential,
            iotHubServiceClientProtocol,
            notificationReceivedCallback,
            proxyOptions,
            sslContext);
    }

    FileUploadNotificationReceiver(
        FileUploadNotificationReceivedCallback notificationReceivedCallback,
        String hostName,
        AzureSasCredential sasTokenProvider,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        Objects.requireNonNull(sasTokenProvider);
        Objects.requireNonNull(iotHubServiceClientProtocol);
        Objects.requireNonNull(notificationReceivedCallback);

        this.hostName = hostName;
        this.amqpReceiveHandler = new AmqpFileUploadNotificationReceivedHandler(
            hostName,
            sasTokenProvider,
            iotHubServiceClientProtocol,
            notificationReceivedCallback,
            proxyOptions,
            sslContext);
    }

    /**
     * Open AmqpReceive object
     *
     */
    public void open() throws IOException
    {
        log.debug("Opening file upload notification receiver");

        this.amqpConnectionReactorRunner =
            new ReactorRunner(amqpReceiveHandler, hostName, "AmqpFileUploadNotificationReceiver");

        new Thread(() ->
        {
            try
            {
                amqpConnectionReactorRunner.run();

                log.trace("Amqp receive reactor stopped, checking that the connection was opened");
                amqpReceiveHandler.verifyConnectionWasOpened();
                log.trace("Amqp receive reactor did successfully open the connection, returning without exception");
            }
            catch (IOException e)
            {
                log.warn("Amqp connection thread encountered an exception", e);

                if (this.connectionLossCallback != null)
                {
                    this.connectionLossCallback.onConnectionLost(e);
                }
            }
        }).start();

        log.debug("Opened file upload notification receiver");
    }

    /**
     * Close AmqpReceive object
     */
    public void close()
    {
        log.debug("Closing file upload notification receiver");

        this.amqpConnectionReactorRunner.stop();

        log.debug("Closed file upload notification receiver");
    }
}
