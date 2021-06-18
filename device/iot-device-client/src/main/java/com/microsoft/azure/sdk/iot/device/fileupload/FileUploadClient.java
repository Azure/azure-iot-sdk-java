// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadCompletionNotification;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriRequest;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriResponse;
import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMethod;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Provide means to upload file in the Azure Storage using the IoTHub.
 */
@Slf4j
public final class FileUploadClient
{
    private static final Charset DEFAULT_IOTHUB_MESSAGE_CHARSET = StandardCharsets.UTF_8;
    private final HttpsTransportManager httpsTransportManager;

    /**
     * CONSTRUCTOR
     *
     * @param config is the set of device client configurations.
     * @throws IllegalArgumentException if one of the parameters is null.
     */
    public FileUploadClient(DeviceClientConfig config) throws IllegalArgumentException
    {
        if(config == null)
        {
            throw new IllegalArgumentException("config is null");
        }

        // File upload will directly use the HttpsTransportManager, avoiding
        // all extra async controls and different protocols since this feature only exists on HTTPS.
        this.httpsTransportManager = new HttpsTransportManager(config);

        log.info("FileUploadClient object is created successfully");
    }

    public FileUploadSasUriResponse getFileUploadSasUri(FileUploadSasUriRequest request) throws DeviceClientException
    {
        IotHubTransportMessage message = new IotHubTransportMessage(request.toJson());
        message.setIotHubMethod(IotHubMethod.POST);

        ResponseMessage responseMessage;
        try
        {
            httpsTransportManager.open();
            responseMessage = httpsTransportManager.getFileUploadSasUri(message);
            httpsTransportManager.close();

            String responseMessagePayload = validateServiceStatusCode(responseMessage, "Failed to get the file upload SAS URI");

            if (responseMessagePayload == null || responseMessagePayload.isEmpty())
            {
                throw new IOException("Sas URI response message had no payload");
            }

            return new FileUploadSasUriResponse(responseMessagePayload);
        }
        catch (IOException e)
        {
            throw new DeviceClientException("Failed to send the file upload notification", e);
        }
    }

    @SuppressWarnings("UnusedReturnValue") // Public method
    public IotHubStatusCode sendNotification(FileUploadCompletionNotification fileUploadStatusParser) throws DeviceClientException
    {
        IotHubTransportMessage message = new IotHubTransportMessage(fileUploadStatusParser.toJson());
        message.setIotHubMethod(IotHubMethod.POST);
        try
        {
            httpsTransportManager.open();
            ResponseMessage responseMessage = httpsTransportManager.sendFileUploadNotification(message);
            httpsTransportManager.close();
            validateServiceStatusCode(responseMessage, "Failed to complete the file upload notification");
            return responseMessage.getStatus();
        }
        catch (IOException e)
        {
            throw new DeviceClientException("Failed to send the file upload notification", e);
        }
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

    public void close()
    {
        this.httpsTransportManager.close();
    }
}
