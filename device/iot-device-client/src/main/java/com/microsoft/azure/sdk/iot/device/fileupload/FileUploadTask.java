// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.fileupload;

import com.google.gson.JsonIOException;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadRequestParser;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadResponseParser;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadStatusParser;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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
public final class FileUploadTask implements Runnable
{
    private static final Charset DEFAULT_IOTHUB_MESSAGE_CHARSET = StandardCharsets.UTF_8;
    private static final String PATH_NOTIFICATIONS_STRING = "/files/notifications";
    private static final String PATH_FILES_STRING = "/files";
    private static final String HTTPS_URL_STRING = "https://";
    private static final String PATH_SEPARATOR_STRING = "/";
    private static final String UTF_8_STRING = "UTF-8";

    private HttpsTransportManager httpsTransportManager;
    private static CustomLogger logger;

    private String blobName;
    private InputStream inputStream;
    private long streamLength;
    private IotHubEventCallback userCallback;
    private Object userCallbackContext;

    private String correlationId;
    private URI blobURI;

    private static final ObjectLock FILE_UPLOAD_LOCK = new ObjectLock();

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
        /* Codes_SRS_FILEUPLOADTASK_21_001: [If the `blobName` is null or empty, the constructor shall throw IllegalArgumentException.] */
        if((blobName == null) || blobName.isEmpty())
        {
            throw new IllegalArgumentException("blobName is null or empty");
        }

        /* Codes_SRS_FILEUPLOADTASK_21_002: [If the `inputStream` is null, the constructor shall throw IllegalArgumentException.] */
        if(inputStream == null)
        {
            throw new IllegalArgumentException("inputStream is null or empty");
        }

        /* Codes_SRS_FILEUPLOADTASK_21_003: [If the `streamLength` is negative, the constructor shall throw IllegalArgumentException.] */
        if(streamLength < 0)
        {
            throw new IllegalArgumentException("streamLength is negative");
        }

        /* Codes_SRS_FILEUPLOADTASK_21_004: [If the `httpsTransportManager` is null, the constructor shall throw IllegalArgumentException.] */
        if(httpsTransportManager == null)
        {
            throw new IllegalArgumentException("httpsTransportManager is null");
        }

        /* Codes_SRS_FILEUPLOADTASK_21_005: [If the `userCallback` is null, the constructor shall throw IllegalArgumentException.] */
        if(userCallback == null)
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

        logger = new CustomLogger(this.getClass());
        logger.LogInfo("HttpsFileUpload object is created successfully, method name is %s ", logger.getMethodName());
    }

    /**
     * Runnable
     */
    @Override
    public void run()
    {
        Thread.currentThread().setName(THREAD_NAME);

        FileUploadStatusParser fileUploadStatusParser = null;
        IotHubStatusCode resultStatus = IotHubStatusCode.OK;

        try
        {
            resultStatus = getContainer();
        }
        catch (IOException | IllegalArgumentException | URISyntaxException | NullPointerException e) //Nobody will handel exception from this thread, so, convert it to an failed code in the user callback.
        {
            /* Codes_SRS_FILEUPLOADTASK_21_031: [If run failed to send the request, it shall call the userCallback with the status `ERROR`, and abort the upload.] */
            logger.LogError("File upload failed to upload the stream to the blob. " + e.toString());
            resultStatus = IotHubStatusCode.ERROR;
        }

        if(resultStatus == IotHubStatusCode.OK)
        {
            try
            {
            /* Codes_SRS_FILEUPLOADTASK_21_019: [The run shall create a `CloudBlockBlob` using the `blobUri`.] */
                CloudBlockBlob blob = new CloudBlockBlob(blobURI);
            /* Codes_SRS_FILEUPLOADTASK_21_020: [The run shall upload the `inputStream` with the `streamLength` to the created `CloudBlockBlob`.] */
                blob.upload(inputStream, streamLength);
            /* Codes_SRS_FILEUPLOADTASK_21_021: [If the upload to blob succeed, the run shall create a notification the IoT Hub with `isSuccess` equals true, `statusCode` equals 0.] */
                fileUploadStatusParser = new FileUploadStatusParser(correlationId, true, 0, "Succeed to upload to storage.");
                resultStatus = IotHubStatusCode.OK;
            }
            catch (StorageException | IOException | IllegalArgumentException e) //Nobody will handel exception from this thread, so, convert it to an failed code in the user callback.
            {
                logger.LogError("File upload failed to upload the stream to the blob. " + e.toString());
            /* Codes_SRS_FILEUPLOADTASK_21_030: [If the upload to blob failed, the run shall call the `userCallback` reporting an error status `ERROR`.] */
                resultStatus = IotHubStatusCode.ERROR;
            /* Codes_SRS_FILEUPLOADTASK_21_022: [If the upload to blob failed, the run shall create a notification the IoT Hub with `isSuccess` equals false, `statusCode` equals -1.] */
                fileUploadStatusParser = new FileUploadStatusParser(correlationId, false, -1, "Failed to upload to storage.");
            }
            finally
            {
                IotHubStatusCode notificationResultStatus = sendNotification(fileUploadStatusParser);
                if (resultStatus == IotHubStatusCode.OK)
                {
                    resultStatus = notificationResultStatus;
                }
            }
        }

        /* Codes_SRS_FILEUPLOADTASK_21_029: [The run shall call the `userCallback` with the final response status.] */
        userCallback.execute(resultStatus, userCallbackContext);
    }

    private void addBlobInformation(Message responseMessage) throws IllegalArgumentException, URISyntaxException, UnsupportedEncodingException
    {
        /* Codes_SRS_FILEUPLOADTASK_21_015: [If the iothub accepts the request, it shall provide a `responseMessage` with the blob information with a correlationId.] */
        /* Codes_SRS_FILEUPLOADTASK_21_016: [If the `responseMessage` is null, empty, do not contains a valid json, or if the information in json is not correct, the run shall call the `userCallback` reporting the error, and abort the upload.] */
        String json = new String(responseMessage.getBytes(), DEFAULT_IOTHUB_MESSAGE_CHARSET);
        FileUploadResponseParser fileUploadResponseParser = new FileUploadResponseParser(json);

        /* Codes_SRS_FILEUPLOADTASK_21_017: [The run shall parse and store the blobName and correlationId in the response, by use the FileUploadResponseParser.] */
        this.correlationId = fileUploadResponseParser.getCorrelationId();
        this.blobName = fileUploadResponseParser.getBlobName();
        String hostName = fileUploadResponseParser.getHostName();
        String containerName = fileUploadResponseParser.getContainerName();
        String sasToken = fileUploadResponseParser.getSasToken();

        /* Codes_SRS_FILEUPLOADTASK_21_018: [The run shall create a blob URI `blobUri` with the format `https://[hostName]/[containerName]/[blobName,UTF-8][sasToken]`.] */
        /* Codes_SRS_FILEUPLOADTASK_21_032: [If create the blob URI failed, the run shall call the `userCallback` reporting the error, and abort the upload.] */
        String putString = HTTPS_URL_STRING +
                hostName + PATH_SEPARATOR_STRING +
                containerName + PATH_SEPARATOR_STRING +
                URLEncoder.encode(blobName, UTF_8_STRING) + // Pass URL encoded blob name to support special characters
                sasToken;
        this.blobURI = new URI(putString);
    }

    private IotHubStatusCode getContainer() throws IOException, IllegalArgumentException, URISyntaxException
    {
        /* Codes_SRS_FILEUPLOADTASK_21_007: [The run shall create a FileUpload request message, by using the FileUploadRequestParser.] */
        FileUploadRequestParser fileUploadRequestParser = new FileUploadRequestParser(blobName);

        IotHubTransportMessage message = new IotHubTransportMessage(fileUploadRequestParser.toJson());
        /* Codes_SRS_FILEUPLOADTASK_21_008: [The run shall set the message method as `POST`.] */
        message.setIotHubMethod(IotHubMethod.POST);
        /* Codes_SRS_FILEUPLOADTASK_21_009: [The run shall set the message URI path as `/files`.] */
        message.setUriPath(PATH_FILES_STRING);

        ResponseMessage responseMessage;
        synchronized (FILE_UPLOAD_LOCK)
        {
            /* Codes_SRS_FILEUPLOADTASK_21_010: [The run shall open the connection with the iothub, using the httpsTransportManager.] */
            httpsTransportManager.open();
            /* Codes_SRS_FILEUPLOADTASK_21_011: [The run shall send the blob request message to the iothub, using the httpsTransportManager.] */
            responseMessage = httpsTransportManager.send(message);
            /* Codes_SRS_FILEUPLOADTASK_21_012: [The run shall close the connection with the iothub, using the httpsTransportManager.] */
            httpsTransportManager.close();
        }

        IotHubStatusCode resultStatus = responseMessage.getStatus();

        /* Codes_SRS_FILEUPLOADTASK_21_013: [If result status for the blob request is not `OK`, or `OK_EMPTY`, the run shall call the userCallback bypassing the received status, and abort the upload.] */
        if(resultStatus == IotHubStatusCode.OK)
        {
            addBlobInformation(responseMessage);
        }
        else if(resultStatus == IotHubStatusCode.OK_EMPTY)
        {
            /* Codes_SRS_FILEUPLOADTASK_21_014: [If result status for the blob request is `OK_EMPTY`, the run shall call the userCallback with the stratus `ERROR`, and abort the upload.] */
            resultStatus = IotHubStatusCode.ERROR;
        }

        return resultStatus;
    }

    private IotHubStatusCode sendNotification(FileUploadStatusParser fileUploadStatusParser)
    {
        IotHubStatusCode responseStatus;
        try
        {
            /* Codes_SRS_FILEUPLOADTASK_21_023: [The run shall create a FileUpload status notification message, by using the FileUploadStatusParser.] */
            IotHubTransportMessage message = new IotHubTransportMessage(fileUploadStatusParser.toJson());
            /* Codes_SRS_FILEUPLOADTASK_21_024: [The run shall set the message method as `POST`.] */
            message.setIotHubMethod(IotHubMethod.POST);
            /* Codes_SRS_FILEUPLOADTASK_21_025: [The run shall set the message URI path as `/files/notifications`.] */
            message.setUriPath(PATH_NOTIFICATIONS_STRING);

            ResponseMessage responseMessage;
            synchronized (FILE_UPLOAD_LOCK)
            {
            /* Codes_SRS_FILEUPLOADTASK_21_026: [The run shall open the connection with the iothub, using the httpsTransportManager.] */
                httpsTransportManager.open();
            /* Codes_SRS_FILEUPLOADTASK_21_027: [The run shall send the blob request message to the iothub, using the httpsTransportManager.] */
                responseMessage = httpsTransportManager.send(message);
            /* Codes_SRS_FILEUPLOADTASK_21_028: [The run shall close the connection with the iothub, using the httpsTransportManager.] */
                httpsTransportManager.close();
            }

            responseStatus = responseMessage.getStatus();
        }
        catch (IllegalArgumentException |IOException | JsonIOException e) //Nobody will handel exception from this thread, so, convert it to an failed code in the user callback.
        {
            /* Codes_SRS_FILEUPLOADTASK_21_033: [If run failed to send the notification, it shall call the userCallback with the stratus `ERROR`, and abort the upload.] */
            logger.LogError("File upload failed to report status to the iothub. " + e.toString());
            responseStatus = IotHubStatusCode.ERROR;
        }

        return responseStatus;
    }
}
