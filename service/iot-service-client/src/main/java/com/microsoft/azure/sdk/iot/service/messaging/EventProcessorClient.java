// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpEventProcessorHandler;
import com.microsoft.azure.sdk.iot.service.transport.amqps.ReactorRunner;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class EventProcessorClient
{
    private static final int STOP_REACTOR_TIMEOUT_MILLISECONDS = 10 * 1000; // 10 seconds

    final Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor;
    final Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor;
    final Consumer<ErrorContext> errorProcessor;
    final String hostName;
    final TokenCredential credential;
    final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    final ProxyOptions proxyOptions;
    final SSLContext sslContext;
    final AzureSasCredential sasTokenProvider;

    final AmqpEventProcessorHandler amqpEventProcessorHandler;

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

        if (fileUploadNotificationProcessor == null && feedbackMessageProcessor == null)
        {
            // TODO no processors attached
            throw new IllegalArgumentException();
        }

        this.amqpEventProcessorHandler =
            new AmqpEventProcessorHandler(
                hostName,
                credential,
                iotHubServiceClientProtocol,
                fileUploadNotificationProcessor,
                feedbackMessageProcessor,
                proxyOptions,
                sslContext);
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

        if (fileUploadNotificationProcessor == null && feedbackMessageProcessor == null)
        {
            // TODO no processors attached
            throw new IllegalArgumentException();
        }

        this.amqpEventProcessorHandler =
            new AmqpEventProcessorHandler(
                hostName,
                sasTokenProvider,
                iotHubServiceClientProtocol,
                fileUploadNotificationProcessor,
                feedbackMessageProcessor,
                proxyOptions,
                sslContext);
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

        if (fileUploadNotificationProcessor == null && feedbackMessageProcessor == null)
        {
            // TODO no processors attached
            throw new IllegalArgumentException();
        }

        this.amqpEventProcessorHandler =
            new AmqpEventProcessorHandler(
                connectionString,
                iotHubServiceClientProtocol,
                fileUploadNotificationProcessor,
                feedbackMessageProcessor,
                proxyOptions,
                sslContext);
    }

    public void start() throws IOException
    {
        log.debug("Opening file upload notification receiver");

        this.reactorRunner = new ReactorRunner(
            this.hostName,
            "AmqpFileUploadNotificationAndCloudToDeviceFeedbackReceiver",
            this.amqpEventProcessorHandler);

        new Thread(() ->
        {
            try
            {
                reactorRunner.run();

                log.trace("EventProcessorClient Amqp reactor stopped, checking that the connection was opened");
                this.amqpEventProcessorHandler.verifyConnectionWasOpened();

                log.trace("EventProcessorClient  reactor did successfully open the connection, returning without exception");
            }
            catch (IOException e)
            {
                log.warn("EventProcessorClient Amqp connection encountered an exception", e);

                if (this.errorProcessor != null)
                {
                    this.errorProcessor.accept(new ErrorContext(e));
                }
            }
        }).start();

        log.debug("Opened EventProcessorClient");
    }

    public void stop() throws InterruptedException
    {
        this.stop(STOP_REACTOR_TIMEOUT_MILLISECONDS);
    }

    public void stop(int timeoutMilliseconds) throws InterruptedException
    {
        log.debug("Closing EventProcessorClient");

        this.reactorRunner.stop(timeoutMilliseconds);

        log.debug("Closed EventProcessorClient ");
    }
}
