// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
final class FileUpload
{
    private static final Charset DEFAULT_IOTHUB_MESSAGE_CHARSET = StandardCharsets.UTF_8;

    private final HttpsTransportManager httpsTransportManager;

    FileUpload(HttpsTransportManager httpsTransportManager)
    {
        this.httpsTransportManager = httpsTransportManager;
        httpsTransportManager.open();
    }

    FileUploadSasUriResponse getFileUploadSasUri(FileUploadSasUriRequest request) throws IotHubClientException
    {
        IotHubTransportMessage message = new IotHubTransportMessage(request.toJson());
        message.setIotHubMethod(HttpsMethod.POST);

        try
        {
            HttpsResponse responseMessage = httpsTransportManager.getFileUploadSasUri(message);
            String responseMessagePayload = validateServiceStatusCode(responseMessage);

            if (responseMessagePayload == null || responseMessagePayload.isEmpty())
            {
                throw new IotHubClientException(IotHubStatusCode.ERROR, "SAS URI response message had no payload");
            }

            return new FileUploadSasUriResponse(responseMessagePayload);
        }
        catch (IOException e)
        {
            throw new IotHubClientException(IotHubStatusCode.IO_ERROR, "Failed to get file upload SAS URI", e);
        }
    }

    void sendNotification(FileUploadCompletionNotification fileUploadStatusParser) throws IotHubClientException
    {
        IotHubTransportMessage message = new IotHubTransportMessage(fileUploadStatusParser.toJson());
        message.setIotHubMethod(HttpsMethod.POST);

        try
        {
            HttpsResponse responseMessage = httpsTransportManager.sendFileUploadNotification(message);
            validateServiceStatusCode(responseMessage);
        }
        catch (IOException e)
        {
            throw new IotHubClientException(IotHubStatusCode.IO_ERROR, "Failed to send file upload completion notification", e);
        }
    }

    private String validateServiceStatusCode(HttpsResponse responseMessage) throws IotHubClientException
    {
        String responseMessagePayload = null;
        if (responseMessage.getBody() != null && responseMessage.getBody().length > 0)
        {
            responseMessagePayload = new String(responseMessage.getBody(), DEFAULT_IOTHUB_MESSAGE_CHARSET);
        }

        IotHubStatusCode statusCode = IotHubStatusCode.getIotHubStatusCode(responseMessage.getStatus());

        // serviceException is only not null if the provided status code was a non-successful status code like 400, 429, 500, etc.
        if (!IotHubStatusCode.isSuccessful(statusCode))
        {
            throw new IotHubClientException(statusCode, responseMessagePayload);
        }

        return responseMessagePayload;
    }

    void close()
    {
        httpsTransportManager.close();
    }
}
