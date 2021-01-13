// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Provide means to upload file in the Azure Storage using the IoTHub.
 */
@Slf4j
public final class FileUpload
{
    private static final int MAX_UPLOAD_PARALLEL = 10;

    private final HttpsTransportManager httpsTransportManager;
    private final ScheduledExecutorService taskScheduler;
    private final FileUploadStatusCallBack fileUploadStatusCallBack;
    private static Queue<FileUploadInProgress> fileUploadInProgressesSet;

    /**
     * CONSTRUCTOR
     *
     * @param config is the set of device client configurations.
     * @throws IllegalArgumentException if one of the parameters is null.
     * @throws IOException if cannot create the artifacts to control the file upload.
     */
    public FileUpload(DeviceClientConfig config) throws IllegalArgumentException, IOException
    {
        /* Codes_SRS_FILEUPLOAD_21_001: [If the provided `config` is null, the constructor shall throw IllegalArgumentException.] */
        if(config == null)
        {
            throw new IllegalArgumentException("config is null");
        }

        // File upload will directly use the HttpsTransportManager, avoiding
        //  all extra async controls.
        // We can do that because File upload have its own async mechanism.
        /* Codes_SRS_FILEUPLOAD_21_002: [The constructor shall create a new instance of `HttpsTransportManager` with the provided `config`.] */
        /* Codes_SRS_FILEUPLOAD_21_003: [If the constructor fail to create the new instance of the `HttpsTransportManager`, it shall throw IllegalArgumentException, threw by the HttpsTransportManager constructor.] */
        this.httpsTransportManager = new HttpsTransportManager(config);

        try
        {
            /* Codes_SRS_FILEUPLOAD_21_012: [The constructor shall create an pool of 10 threads to execute the uploads in parallel.] */
            taskScheduler = Executors.newScheduledThreadPool(MAX_UPLOAD_PARALLEL);
        }
        catch (IllegalArgumentException | NullPointerException e)
        {
            /* Codes_SRS_FILEUPLOAD_21_015: [If create the executor failed, the constructor shall throws IOException.] */
            throw new IOException("Cannot create a pool of threads to manager uploads: " + e);
        }
        /* Codes_SRS_FILEUPLOAD_21_013: [The constructor shall create a list `fileUploadInProgressesSet` to control the pending uploads.] */
        fileUploadInProgressesSet = new LinkedBlockingDeque<>();
        /* Codes_SRS_FILEUPLOAD_21_014: [The constructor shall create an Event callback `fileUploadStatusCallBack` to receive the upload status.] */
        fileUploadStatusCallBack = new FileUploadStatusCallBack();

        log.info("FileUpload object is created successfully");
    }

    /**
     * Upload the file to container, which was associated to the iothub.
     * This function will start the upload process, and back the execution
     * to the caller. The upload process will be executed in background.
     * When it is completed, the background thread will trigger the
     * callback with the upload status.
     *
     * @param blobName is the name of the file in the container.
     * @param inputStream is the input stream.
     * @param streamLength is the stream length.
     * @param statusCallback is the callback to notify that the upload is completed (with status).
     * @param statusCallbackContext is the context of the callback, allowing multiple uploads in parallel.
     * @throws IllegalArgumentException if one of the parameters is invalid.
     *              blobName is {@code null} or empty,
     *              inputStream is {@code null} or not available,
     *              streamLength is negative,
     *              statusCallback is {@code null}
     * @throws IOException if an I/O error occurs in the inputStream.
     */
    public synchronized void uploadToBlobAsync(
            String blobName, InputStream inputStream, long streamLength,
            IotHubEventCallback statusCallback, Object statusCallbackContext)
            throws IllegalArgumentException, IOException
    {
        /* Codes_SRS_FILEUPLOAD_21_005: [If the `blobName` is null or empty, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        if((blobName == null) || blobName.isEmpty())
        {
            throw new IllegalArgumentException("blobName is null or empty");
        }

        /* Codes_SRS_FILEUPLOAD_21_006: [If the `inputStream` is null or not available, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        /* Codes_SRS_FILEUPLOAD_21_011: [If the `inputStream` failed to do I/O, the uploadToBlobAsync shall throw IOException, threw by the InputStream class.] */
        if((inputStream == null))
        {
            throw new IllegalArgumentException("inputStream is null");
        }

        /* Codes_SRS_FILEUPLOAD_21_007: [If the `streamLength` is negative, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        if(streamLength < 0)
        {
            throw new IllegalArgumentException("streamLength is negative");
        }

        /* Codes_SRS_FILEUPLOAD_21_008: [If the `userCallback` is null, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        if(statusCallback == null)
        {
            throw new IllegalArgumentException("statusCallback is null");
        }

        /* Codes_SRS_FILEUPLOAD_21_016: [The uploadToBlobAsync shall create a `FileUploadInProgress` to store the fileUpload context.] */
        FileUploadInProgress newUpload = new FileUploadInProgress(statusCallback, statusCallbackContext);
        fileUploadInProgressesSet.add(newUpload);

        /* Codes_SRS_FILEUPLOAD_21_004: [The uploadToBlobAsync shall asynchronously upload the InputStream `inputStream` to the blob in `blobName`.] */
        /* Codes_SRS_FILEUPLOAD_21_009: [The uploadToBlobAsync shall create a `FileUploadTask` to control this file upload.] */
        FileUploadTask fileUploadTask = new FileUploadTask(blobName, inputStream, streamLength, httpsTransportManager, fileUploadStatusCallBack, newUpload);

        /* Codes_SRS_FILEUPLOAD_21_010: [The uploadToBlobAsync shall schedule the task `FileUploadTask` to immediately start.] */
        newUpload.setTask(taskScheduler.submit(fileUploadTask));
    }

    private final class FileUploadStatusCallBack implements IotHubEventCallback
    {
        @Override
        public synchronized void execute(IotHubStatusCode status, Object context)
        {
            /* Codes_SRS_FILEUPLOAD_21_019: [The FileUploadStatusCallBack shall implements the `IotHubEventCallback` as result of the FileUploadTask.] */
            if(context instanceof FileUploadInProgress)
            {
                FileUploadInProgress uploadInProgress = (FileUploadInProgress) context;
                /* Codes_SRS_FILEUPLOAD_21_020: [The FileUploadStatusCallBack shall call the `statusCallback` reporting the received status.] */
                uploadInProgress.triggerCallback(status);
                /* Codes_SRS_FILEUPLOAD_21_021: [The FileUploadStatusCallBack shall delete the `FileUploadInProgress` that store this file upload context.] */
                try
                {
                    fileUploadInProgressesSet.remove(context);
                }
                catch (ClassCastException | NullPointerException | UnsupportedOperationException e)
                {
                    /* Codes_SRS_FILEUPLOAD_21_023: [If the FileUploadStatusCallBack failed to delete the `FileUploadInProgress`, it shall log a error.] */
                    log.error("FileUploadStatusCallBack received callback for unknown FileUpload", e);
                }
            }
            else
            {
                /* Codes_SRS_FILEUPLOAD_21_022: [If the received context is not type of `FileUploadInProgress`, the FileUploadStatusCallBack shall log a error and ignore the message.] */
                log.error("FileUploadStatusCallBack received callback for unknown FileUpload");
            }
        }
    }

    /**
     * Close the file upload cancelling all existing uploads and shutting down the thread pool.
     *
     * @throws IOException if an I/O error occurs in the inputStream.
     */
    public void closeNow() throws IOException
    {
        /* Codes_SRS_FILEUPLOAD_21_017: [The closeNow shall shutdown the thread pool by calling `shutdownNow`.] */
        taskScheduler.shutdownNow();

        /* Codes_SRS_FILEUPLOAD_21_018: [If there is pending file uploads, the closeNow shall cancel the upload, and call the `statusCallback` reporting ERROR.] */
        for (FileUploadInProgress uploadInProgress : fileUploadInProgressesSet)
        {
            if(uploadInProgress.isCancelled())
            {
                uploadInProgress.triggerCallback(IotHubStatusCode.ERROR);
            }
        }
    }
}
