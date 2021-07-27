// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadCompletionNotification;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriRequest;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriResponse;
import com.microsoft.azure.sdk.iot.device.IotHubMethod;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public final class FileUpload
{
    private static final Charset DEFAULT_IOTHUB_MESSAGE_CHARSET = StandardCharsets.UTF_8;

    private final HttpsTransportManager httpsTransportManager;

    FileUpload(HttpsTransportManager httpsTransportManager)
    {
        this.httpsTransportManager = httpsTransportManager;
        httpsTransportManager.open();
    }

    FileUploadSasUriResponse getFileUploadSasUri(FileUploadSasUriRequest request) throws IOException
    {
        IotHubTransportMessage message = new IotHubTransportMessage(request.toJson());
        message.setIotHubMethod(IotHubMethod.POST);

        ResponseMessage responseMessage = httpsTransportManager.getFileUploadSasUri(message);

        String responseMessagePayload = validateServiceStatusCode(responseMessage, "Failed to get the file upload SAS URI");

        if (responseMessagePayload == null || responseMessagePayload.isEmpty())
        {
            throw new IOException("Sas URI response message had no payload");
        }

        return new FileUploadSasUriResponse(responseMessagePayload);
    }

    @SuppressWarnings("UnusedReturnValue") // Public method
    IotHubStatusCode sendNotification(FileUploadCompletionNotification fileUploadStatusParser) throws IOException
    {
        IotHubTransportMessage message = new IotHubTransportMessage(fileUploadStatusParser.toJson());
        message.setIotHubMethod(IotHubMethod.POST);

        ResponseMessage responseMessage = httpsTransportManager.sendFileUploadNotification(message);
        httpsTransportManager.close();

        validateServiceStatusCode(responseMessage, "Failed to complete the file upload notification");

        return responseMessage.getStatus();
    }

    private String validateServiceStatusCode(ResponseMessage responseMessage, String errorMessage) throws IOException
    {
        String responseMessagePayload = null;
        if (responseMessage.getBytes() != null && responseMessage.getBytes().length > 0)
        {
            responseMessagePayload = new String(responseMessage.getBytes(), DEFAULT_IOTHUB_MESSAGE_CHARSET);
        }

        IotHubServiceException serviceException =
            IotHubStatusCode.getConnectionStatusException(
                responseMessage.getStatus(),
                responseMessagePayload);

        // serviceException is only not null if the provided status code was a non-successful status code like 400, 429, 500, etc.
        if (serviceException != null)
        {
            throw new IOException(errorMessage, serviceException);
        }

        return responseMessagePayload;
    }

    void close()
    {
        this.httpsTransportManager.close();
    }
}
