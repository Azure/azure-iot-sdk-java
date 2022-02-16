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
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class FileUploadNotificationProcessorClient
{
    private static final int STOP_REACTOR_TIMEOUT_MILLISECONDS = 10 * 1000; // 10 seconds

    private final AmqpEventProcessorHandler amqpEventProcessorHandler;
    private final Consumer<ErrorContext> errorProcessor; // may be null if user doesn't provide one

    private ReactorRunner reactorRunner;

    public FileUploadNotificationProcessorClient(
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol iotHubServiceClientProtocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor)
    {
        this(hostName, credential, iotHubServiceClientProtocol, fileUploadNotificationProcessor, FileUploadNotificationProcessorClientOptions.builder().build());
    }

    public FileUploadNotificationProcessorClient(
        String hostName,
        TokenCredential credential,
        IotHubServiceClientProtocol protocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor,
        FileUploadNotificationProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");

        this.errorProcessor = options.getErrorProcessor();
        this.amqpEventProcessorHandler =
            new AmqpEventProcessorHandler(
                hostName,
                credential,
                protocol,
                fileUploadNotificationProcessor,
                null,
                options.getProxyOptions(),
                options.getSslContext());
    }

    public FileUploadNotificationProcessorClient(
        String hostName,
        AzureSasCredential sasTokenProvider,
        IotHubServiceClientProtocol protocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor)
    {
        this(hostName, sasTokenProvider, protocol, fileUploadNotificationProcessor, FileUploadNotificationProcessorClientOptions.builder().build());
    }

    public FileUploadNotificationProcessorClient(
        String hostName,
        AzureSasCredential sasTokenProvider,
        IotHubServiceClientProtocol protocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor,
        FileUploadNotificationProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");

        this.errorProcessor = options.getErrorProcessor();
        this.amqpEventProcessorHandler =
            new AmqpEventProcessorHandler(
                hostName,
                sasTokenProvider,
                protocol,
                fileUploadNotificationProcessor,
                null,
                options.getProxyOptions(),
                options.getSslContext());
    }

    public FileUploadNotificationProcessorClient(
        String connectionString,
        IotHubServiceClientProtocol protocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor)
    {
        this(connectionString, protocol, fileUploadNotificationProcessor, FileUploadNotificationProcessorClientOptions.builder().build());
    }

    public FileUploadNotificationProcessorClient(
        String connectionString,
        IotHubServiceClientProtocol protocol,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor,
        FileUploadNotificationProcessorClientOptions options)
    {
        Objects.requireNonNull(options, "Options cannot be null");

        this.errorProcessor = options.getErrorProcessor();
        this.amqpEventProcessorHandler =
            new AmqpEventProcessorHandler(
                connectionString,
                protocol,
                fileUploadNotificationProcessor,
                null,
                options.getProxyOptions(),
                options.getSslContext());
    }

    public void start() throws IOException, InterruptedException
    {
        log.debug("Opening file upload notification receiver");

        this.reactorRunner = new ReactorRunner(
            this.amqpEventProcessorHandler.getHostName(),
            "AmqpFileUploadNotificationAndCloudToDeviceFeedbackReceiver",
            this.amqpEventProcessorHandler);

        final CountDownLatch openLatch = new CountDownLatch(1);
        this.amqpEventProcessorHandler.setOnConnectionOpenedCallback(e ->
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

        boolean timedOut = !openLatch.await(10 * 1000, TimeUnit.MILLISECONDS);

        if (timedOut)
        {
            //TODO
        }

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
