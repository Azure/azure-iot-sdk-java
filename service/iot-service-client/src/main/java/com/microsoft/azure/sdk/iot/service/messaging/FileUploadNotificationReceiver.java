/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFeedbackReceivedEvent;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFileUploadNotificationReceivedHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.reactor.impl.IO;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Objects;

@Slf4j
public class FileUploadNotificationReceiver implements AmqpFeedbackReceivedEvent
{
    private AmqpFileUploadNotificationReceivedHandler amqpReceiveHandler;
    private FileUploadNotificationReceivedCallback notificationReceivedCallback;
    private final String hostName;
    private ReactorRunner amqpConnectionReactorRunner;

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
        this.notificationReceivedCallback = notificationReceivedCallback;
        this.amqpReceiveHandler = new AmqpFileUploadNotificationReceivedHandler(
            hostName,
            sasToken,
            iotHubServiceClientProtocol,
            this,
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
        this.notificationReceivedCallback = notificationReceivedCallback;
        this.amqpReceiveHandler = new AmqpFileUploadNotificationReceivedHandler(
            hostName,
            credential,
            iotHubServiceClientProtocol,
            this,
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
        this.notificationReceivedCallback = notificationReceivedCallback;
        this.amqpReceiveHandler = new AmqpFileUploadNotificationReceivedHandler(
            hostName,
            sasTokenProvider,
            iotHubServiceClientProtocol,
            this,
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
                //TODO add some connection status callback to the user?
                log.warn("Amqp connection thread encountered an exception", e);
            }
        }).start();

        log.debug("Opened file upload notification receiver");
    }

    /**
     * Close AmqpReceive object
     *
     */
    public void close()
    {
        log.debug("Closing file upload notification receiver");

        this.amqpConnectionReactorRunner.stop();

        log.debug("Closed file upload notification receiver");
    }

    /**
     * Handle on feedback received Proton event
     * Parse received json and save result to a member variable
     * Release semaphore for wait function
     * @param feedbackJson Received Json string to process
     */
    public IotHubMessageResult onFeedbackReceived(String feedbackJson)
    {
        try
        {
            FileUploadNotificationParser notificationParser = new FileUploadNotificationParser(feedbackJson);

            FileUploadNotification fileUploadNotification = new FileUploadNotification(notificationParser.getDeviceId(),
                notificationParser.getBlobUri(), notificationParser.getBlobName(), notificationParser.getLastUpdatedTime(),
                notificationParser.getBlobSizeInBytesTag(), notificationParser.getEnqueuedTimeUtc());

            return notificationReceivedCallback.onFileUploadNotificationReceived(fileUploadNotification);
        }
        catch (Exception e)
        {
            // this should never happen. However if it does, proton can't handle it. So guard against throwing it at proton.
            log.warn("Encountered an exception while handling file upload notification", e);
            return IotHubMessageResult.ABANDON;
        }
    }
}
