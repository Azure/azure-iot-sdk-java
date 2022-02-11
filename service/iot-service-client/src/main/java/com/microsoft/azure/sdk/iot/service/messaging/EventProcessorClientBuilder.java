// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;

import javax.net.ssl.SSLContext;
import java.util.function.Consumer;
import java.util.function.Function;

public class EventProcessorClientBuilder
{
    Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor;
    Function<FeedbackBatch, AcknowledgementType> cloudToDeviceFeedbackMessageProcessor;
    Consumer<ErrorContext> errorProcessor;
    String hostName;
    TokenCredential credential;
    IotHubServiceClientProtocol protocol;
    ProxyOptions proxyOptions;
    SSLContext sslContext;
    AzureSasCredential sasTokenProvider;
    String connectionString;

    EventProcessorClientBuilder()
    {
        // accessible only by EventProcessorClient.builder()
    }

    public EventProcessorClientBuilder setFileUploadNotificationProcessor(Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor)
    {
        this.fileUploadNotificationProcessor = fileUploadNotificationProcessor;
        return this;
    }

    public EventProcessorClientBuilder setCloudToDeviceFeedbackMessageProcessor(Function<FeedbackBatch, AcknowledgementType> cloudToDeviceFeedbackMessageProcessor)
    {
        this.cloudToDeviceFeedbackMessageProcessor = cloudToDeviceFeedbackMessageProcessor;
        return this;
    }

    public EventProcessorClientBuilder setHostName(String hostName)
    {
        this.hostName = hostName;
        return this;
    }

    public EventProcessorClientBuilder setCredential(TokenCredential credential)
    {
        this.credential = credential;
        return this;
    }

    public EventProcessorClientBuilder setProtocol(IotHubServiceClientProtocol protocol)
    {
        this.protocol = protocol;
        return this;
    }

    public EventProcessorClientBuilder setProxyOptions(ProxyOptions proxyOptions)
    {
        this.proxyOptions = proxyOptions;
        return this;
    }

    public EventProcessorClientBuilder setSslContext(SSLContext sslContext)
    {
        this.sslContext = sslContext;
        return this;
    }

    public EventProcessorClientBuilder setSasTokenProvider(AzureSasCredential sasTokenProvider)
    {
        this.sasTokenProvider = sasTokenProvider;
        return this;
    }

    public EventProcessorClientBuilder setConnectionString(String connectionString)
    {
        this.connectionString = connectionString;
        return this;
    }

    public EventProcessorClientBuilder setErrorProcessor(Consumer<ErrorContext> errorProcessor)
    {
        this.errorProcessor = errorProcessor;
        return this;
    }

    public EventProcessorClient build()
    {
        if (protocol == null)
        {
            //TODO throw
            throw new IllegalArgumentException();
        }

        if (hostName != null && credential != null)
        {
            return new EventProcessorClient(
                hostName,
                credential,
                protocol,
                fileUploadNotificationProcessor,
                cloudToDeviceFeedbackMessageProcessor,
                errorProcessor,
                proxyOptions,
                sslContext);
        }
        else if (hostName != null && sasTokenProvider != null)
        {
            return new EventProcessorClient(
                hostName,
                sasTokenProvider,
                protocol,
                fileUploadNotificationProcessor,
                cloudToDeviceFeedbackMessageProcessor,
                errorProcessor,
                proxyOptions,
                sslContext);
        }
        else if (connectionString != null)
        {
            return new EventProcessorClient(
                connectionString,
                protocol,
                fileUploadNotificationProcessor,
                cloudToDeviceFeedbackMessageProcessor,
                errorProcessor,
                proxyOptions,
                sslContext);
        }
        else
        {
            // TODO throw
            throw new IllegalArgumentException();
        }
    }
}
