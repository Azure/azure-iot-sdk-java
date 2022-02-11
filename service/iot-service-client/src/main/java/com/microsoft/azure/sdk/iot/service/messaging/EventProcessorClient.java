// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFeedbackReceivedHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFileUploadNotificationReceivedHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class EventProcessorClient
{
    final Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor;
    final Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor;
    final Consumer<ErrorContext> errorProcessor;
    final String hostName;
    final TokenCredential credential;
    final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    final ProxyOptions proxyOptions;
    final SSLContext sslContext;
    final AzureSasCredential sasTokenProvider;

    final AmqpFeedbackReceivedHandler amqpFeedbackReceivedHandler;
    final AmqpFileUploadNotificationReceivedHandler amqpFileUploadNotificationReceivedHandler;

    ReactorRunner reactorRunner;

    public static EventProcessorClientBuilder builder()
    {
        return new EventProcessorClientBuilder();
    }

    EventProcessorClient(
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor,
        Consumer<ErrorContext> errorProcessor,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        this.fileUploadNotificationProcessor = fileUploadNotificationProcessor;
        this.feedbackMessageProcessor = feedbackMessageProcessor;
        this.errorProcessor = errorProcessor;
        this.hostName = hostName;
        this.credential = credential;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
        this.sasTokenProvider = null;

        if (fileUploadNotificationProcessor != null)
        {
            this.amqpFileUploadNotificationReceivedHandler =
                new AmqpFileUploadNotificationReceivedHandler(
                    hostName,
                    credential,
                    iotHubServiceClientProtocol,
                    fileUploadNotificationProcessor,
                    proxyOptions,
                    sslContext);
        }
        else
        {
            this.amqpFileUploadNotificationReceivedHandler = null;
        }

        if (feedbackMessageProcessor != null)
        {
            this.amqpFeedbackReceivedHandler =
                new AmqpFeedbackReceivedHandler(
                    hostName,
                    credential,
                    iotHubServiceClientProtocol,
                    feedbackMessageProcessor,
                    proxyOptions,
                    sslContext);
        }
        else
        {
            this.amqpFeedbackReceivedHandler = null;
        }

        if (fileUploadNotificationProcessor == null && feedbackMessageProcessor == null)
        {
            // TODO no processors attached
            throw new IllegalArgumentException();
        }
    }

    EventProcessorClient(
        String hostName,
        AzureSasCredential sasTokenProvider,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor,
        Consumer<ErrorContext> errorProcessor,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        this.fileUploadNotificationProcessor = fileUploadNotificationProcessor;
        this.feedbackMessageProcessor = feedbackMessageProcessor;
        this.errorProcessor = errorProcessor;
        this.hostName = hostName;
        this.credential = null;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
        this.sasTokenProvider = sasTokenProvider;

        if (fileUploadNotificationProcessor != null)
        {
            this.amqpFileUploadNotificationReceivedHandler =
                new AmqpFileUploadNotificationReceivedHandler(
                    hostName,
                    sasTokenProvider,
                    iotHubServiceClientProtocol,
                    fileUploadNotificationProcessor,
                    proxyOptions,
                    sslContext);
        }
        else
        {
            this.amqpFileUploadNotificationReceivedHandler = null;
        }

        if (feedbackMessageProcessor != null)
        {
            this.amqpFeedbackReceivedHandler =
                new AmqpFeedbackReceivedHandler(
                    hostName,
                    sasTokenProvider,
                    iotHubServiceClientProtocol,
                    feedbackMessageProcessor,
                    proxyOptions,
                    sslContext);
        }
        else
        {
            this.amqpFeedbackReceivedHandler = null;
        }

        if (fileUploadNotificationProcessor == null && feedbackMessageProcessor == null)
        {
            // TODO no processors attached
            throw new IllegalArgumentException();
        }
    }

    EventProcessorClient(
        String connectionString,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor,
        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor,
        Consumer<ErrorContext> errorProcessor,
        ProxyOptions proxyOptions,
        SSLContext sslContext)
    {
        this.fileUploadNotificationProcessor = fileUploadNotificationProcessor;
        this.feedbackMessageProcessor = feedbackMessageProcessor;
        this.errorProcessor = errorProcessor;
        this.hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString).getHostName();
        this.credential = null;
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.proxyOptions = proxyOptions;
        this.sslContext = sslContext;
        this.sasTokenProvider = null;

        if (fileUploadNotificationProcessor != null)
        {
            this.amqpFileUploadNotificationReceivedHandler =
                new AmqpFileUploadNotificationReceivedHandler(
                    connectionString,
                    iotHubServiceClientProtocol,
                    fileUploadNotificationProcessor,
                    proxyOptions,
                    sslContext);
        }
        else
        {
            this.amqpFileUploadNotificationReceivedHandler = null;
        }

        if (feedbackMessageProcessor != null)
        {
            this.amqpFeedbackReceivedHandler =
                new AmqpFeedbackReceivedHandler(
                    connectionString,
                    iotHubServiceClientProtocol,
                    feedbackMessageProcessor,
                    proxyOptions,
                    sslContext);
        }
        else
        {
            this.amqpFeedbackReceivedHandler = null;
        }

        if (fileUploadNotificationProcessor == null && feedbackMessageProcessor == null)
        {
            // TODO no processors attached
            throw new IllegalArgumentException();
        }
    }

    public void start() throws IOException
    {
        log.debug("Opening file upload notification receiver");

        if (this.amqpFileUploadNotificationReceivedHandler != null && this.amqpFeedbackReceivedHandler != null)
        {
            this.reactorRunner = new ReactorRunner(
                this.hostName,
                "AmqpFileUploadNotificationAndCloudToDeviceFeedbackReceiver",
                this.amqpFileUploadNotificationReceivedHandler,
                this.amqpFeedbackReceivedHandler);
        }
        else if (this.amqpFeedbackReceivedHandler != null)
        {
            this.reactorRunner = new ReactorRunner(
                this.hostName,
                "AmqpCloudToDeviceFeedbackReceiver",
                this.amqpFeedbackReceivedHandler);
        }
        else if (this.amqpFileUploadNotificationReceivedHandler != null)
        {
            this.reactorRunner = new ReactorRunner(
                this.hostName,
                "AmqpFileUploadNotificationReceiver",
                this.amqpFileUploadNotificationReceivedHandler);
        }

        new Thread(() ->
        {
            try
            {
                reactorRunner.run();

                log.trace("Amqp receive reactor stopped, checking that the connection was opened");
                if (this.amqpFileUploadNotificationReceivedHandler != null)
                {
                    this.amqpFileUploadNotificationReceivedHandler.verifyConnectionWasOpened();
                }
                else
                {
                    this.amqpFeedbackReceivedHandler.verifyConnectionWasOpened();
                }

                log.trace("Amqp receive reactor did successfully open the connection, returning without exception");
            }
            catch (IOException e)
            {
                log.warn("Amqp connection thread encountered an exception", e);

                if (this.errorProcessor != null)
                {
                    this.errorProcessor.accept(new ErrorContext(e));
                }
            }
        }).start();

        log.debug("Opened file upload notification receiver");
    }

    public void stop()
    {
        log.debug("Closing file upload notification receiver");

        this.reactorRunner.stop();

        log.debug("Closed file upload notification receiver");
    }
}
