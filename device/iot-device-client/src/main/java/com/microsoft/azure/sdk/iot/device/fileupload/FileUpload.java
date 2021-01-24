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
    static Queue<FileUploadInProgress> fileUploadInProgressesSet;

    /**
     * CONSTRUCTOR
     *
     * @param config is the set of device client configurations.
     * @throws IllegalArgumentException if one of the parameters is null.
     * @throws IOException if cannot create the artifacts to control the file upload.
     */
    public FileUpload(DeviceClientConfig config) throws IllegalArgumentException, IOException
    {
        if(config == null)
        {
            throw new IllegalArgumentException("config is null");
        }

        // File upload will directly use the HttpsTransportManager, avoiding
        //  all extra async controls.
        // We can do that because File upload have its own async mechanism.
        this.httpsTransportManager = new HttpsTransportManager(config);

        try
        {
            taskScheduler = Executors.newScheduledThreadPool(MAX_UPLOAD_PARALLEL);
        }
        catch (IllegalArgumentException | NullPointerException e)
        {
            throw new IOException("Cannot create a pool of threads to manager uploads: " + e);
        }
        fileUploadInProgressesSet = new LinkedBlockingDeque<>();
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
        if((blobName == null) || blobName.isEmpty())
        {
            throw new IllegalArgumentException("blobName is null or empty");
        }

        if((inputStream == null))
        {
            throw new IllegalArgumentException("inputStream is null");
        }

        if(streamLength < 0)
        {
            throw new IllegalArgumentException("streamLength is negative");
        }

        if(statusCallback == null)
        {
            throw new IllegalArgumentException("statusCallback is null");
        }

        FileUploadInProgress newUpload = new FileUploadInProgress(statusCallback, statusCallbackContext);
        fileUploadInProgressesSet.add(newUpload);

        FileUploadTask fileUploadTask = new FileUploadTask(blobName, inputStream, streamLength, httpsTransportManager, fileUploadStatusCallBack, newUpload);

        newUpload.setTask(taskScheduler.submit(fileUploadTask));
    }

    /**
     * Close the file upload cancelling all existing uploads and shutting down the thread pool.
     *
     * @throws IOException if an I/O error occurs in the inputStream.
     */
    public void closeNow() throws IOException
    {
        taskScheduler.shutdownNow();

        for (FileUploadInProgress uploadInProgress : fileUploadInProgressesSet)
        {
            if(uploadInProgress.isCancelled())
            {
                uploadInProgress.triggerCallback(IotHubStatusCode.ERROR);
            }
        }
    }
}
