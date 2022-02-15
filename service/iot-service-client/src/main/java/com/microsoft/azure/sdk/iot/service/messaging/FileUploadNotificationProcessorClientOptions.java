// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import lombok.Builder;
import lombok.Getter;

import javax.net.ssl.SSLContext;
import java.util.function.Consumer;

@Builder
public class FileUploadNotificationProcessorClientOptions
{
    @Getter
    private final ProxyOptions proxyOptions;

    @Getter
    private final SSLContext sslContext;

    @Getter
    private final Consumer<ErrorContext> errorProcessor;
}
