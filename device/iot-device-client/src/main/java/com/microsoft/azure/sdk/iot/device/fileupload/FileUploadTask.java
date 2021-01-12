// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadCompletionNotification;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriRequest;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriResponse;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMethod;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Provide means to  asynchronous upload file in the Azure Storage using the IoTHub.
 *
 * <p>
 *     The file upload process is composed by 3 steps represented in the follow diagram.
 *   +--------------+      +---------------+    +---------------+    +---------------+
 *   |    Device    |      |    Iot Hub    |    |    Storage    |    |    Service    |
 *   +--------------+      +---------------+    +---------------+    +---------------+
 *           |                     |                    |                    |
 *           |                     |                    |                    |
 *       REQUEST_BLOB              |                    |                    |
 *           +--- request blob ---&gt;|                    |                    |
 *           |&lt;-- blob SAS token --+                    |                    |
 *           |                     |                    |                    |
 *       UPLOAD_FILE               |                    |                    |
 *           +---- upload file to the provided blob ---&gt;|                    |
 *           +&lt;------ end of upload with `status` ------+                    |
 *           |                     |                    |                    |
 *       NOTIFY_IOTHUB             |                    |                    |
 *           +--- notify status --&gt;|                    |                    |
 *           |                     +------ notify new file available -------&gt;|
 *           |                     |                    |                    |
 * </p>
 */
@Slf4j
public final class FileUploadTask implements Runnable
{
    private static final Charset DEFAULT_IOTHUB_MESSAGE_CHARSET = StandardCharsets.UTF_8;

    private HttpsTransportManager httpsTransportManager;

    private String blobName;
    private InputStream inputStream;
    private long streamLength;
    private IotHubEventCallback userCallback;
    private Object userCallbackContext;

    private static final String THREAD_NAME = "azure-iot-sdk-FileUploadTask";

    /**
     * Constructor
     *
     * @param blobName is the destination blob name in the storage. Cannot be {@code null}, or empty.
     * @param inputStream is the byte stream with the information to store in the blob. Cannot be {@code null}.
     * @param streamLength is the number of bytes to upload. Cannot be negative.
     * @param httpsTransportManager is the https transport to connect to the IoT Hub. Cannot be {@code null}.
     * @param userCallback is the callback to call when the upload is completed. Cannot be {@code null}.
     * @param userCallbackContext is the context for the callback. Can be any value.
     * @throws IllegalArgumentException if one of the parameters is not valid.
     */
    FileUploadTask(String blobName, InputStream inputStream, long streamLength, HttpsTransportManager httpsTransportManager,
                    IotHubEventCallback userCallback, Object userCallbackContext) throws IllegalArgumentException
    {
        if ((blobName == null) || blobName.isEmpty())
        {
            throw new IllegalArgumentException("blobName is null or empty");
        }

        if (inputStream == null)
        {
            throw new IllegalArgumentException("inputStream is null or empty");
        }

        if (streamLength < 0)
        {
            throw new IllegalArgumentException("streamLength is negative");
        }

        if (httpsTransportManager == null)
        {
            throw new IllegalArgumentException("httpsTransportManager is null");
        }

        if (userCallback == null)
        {
            throw new IllegalArgumentException("statusCallback is null");
        }

        /* Codes_SRS_FILEUPLOADTASK_21_006: [The constructor shall store all the provided parameters.] */
        this.blobName = blobName;
        this.inputStream = inputStream;
        this.streamLength = streamLength;
        this.userCallback = userCallback;
        this.userCallbackContext = userCallbackContext;
        this.httpsTransportManager = httpsTransportManager;

        log.trace("HttpsFileUpload object is created successfully");
    }

    public FileUploadTask(HttpsTransportManager httpsTransportManager)
    {
        this.httpsTransportManager = httpsTransportManager;
    }

    @Override
    public void run()
    {
        Thread.currentThread().setName(THREAD_NAME);

        FileUploadCompletionNotification fileUploadCompletionNotification = null;
        FileUploadSasUriResponse sasUriResponse;

        try
        {
            sasUriResponse = getFileUploadSasUri(new FileUploadSasUriRequest(this.blobName));
        }
        catch (IOException | IllegalArgumentException e)
        {
            log.error("File upload failed to get a SAS URI from Iot Hub", e);
            userCallback.execute(IotHubStatusCode.ERROR, userCallbackContext);
            return;
        }

        try
        {
            CloudBlockBlob blob = new CloudBlockBlob(sasUriResponse.getBlobUri());
            blob.upload(inputStream, streamLength);
            fileUploadCompletionNotification = new FileUploadCompletionNotification(sasUriResponse.getCorrelationId(), true, 0, "Succeed to upload to storage.");
        }
        catch (StorageException | IOException | IllegalArgumentException | URISyntaxException e)
        {
            log.error("File upload failed to upload the stream to the blob", e);
            fileUploadCompletionNotification = new FileUploadCompletionNotification(sasUriResponse.getCorrelationId(), false, -1, "Failed to upload to storage.");
        }
        finally
        {
            try
            {
                sendNotification(fileUploadCompletionNotification);
            }
            catch (IOException e)
            {
                log.error("Failed to send file upload status", e);
            }
        }

        if (fileUploadCompletionNotification.getSuccess())
        {
            userCallback.execute(IotHubStatusCode.OK, userCallbackContext);
        }
        else
        {
            userCallback.execute(IotHubStatusCode.ERROR, userCallbackContext);
        }
    }

    public FileUploadSasUriResponse getFileUploadSasUri(FileUploadSasUriRequest request) throws IOException
    {
        IotHubTransportMessage message = new IotHubTransportMessage(request.toJson());
        message.setIotHubMethod(IotHubMethod.POST);

        ResponseMessage responseMessage;
        httpsTransportManager.open();
        responseMessage = httpsTransportManager.getFileUploadSasUri(message);
        httpsTransportManager.close();

        if (responseMessage.getBytes() == null || responseMessage.getBytes().length == 0)
        {
            throw new IOException("Sas URI response message had no payload");
        }

        return new FileUploadSasUriResponse(new String(responseMessage.getBytes(), DEFAULT_IOTHUB_MESSAGE_CHARSET));
    }

    public IotHubStatusCode sendNotification(FileUploadCompletionNotification fileUploadStatusParser) throws IOException
    {
        IotHubTransportMessage message = new IotHubTransportMessage(fileUploadStatusParser.toJson());
        message.setIotHubMethod(IotHubMethod.POST);

        httpsTransportManager.open();
        ResponseMessage responseMessage = httpsTransportManager.sendFileUploadNotification(message);
        httpsTransportManager.close();

        return responseMessage.getStatus();
    }

    public void close()
    {
        this.httpsTransportManager.close();
    }
}
