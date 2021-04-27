// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Control the file uploads that are in progress
 */
public final class FileUploadInProgress
{
    private final IotHubEventCallback statusCallback;
    private final Object statusCallbackContext;
    @SuppressWarnings("rawtypes")
    private Future task;

    /**
     * CONSTRUCTOR
     *
     * @param statusCallback is the callback to notify that the upload is completed (with status).
     * @param statusCallbackContext is the context of the callback, allowing multiple uploads in parallel.
     * @throws IllegalArgumentException if the stratusCallback is null.
     */
    FileUploadInProgress(IotHubEventCallback statusCallback, Object statusCallbackContext) throws IllegalArgumentException
    {
        /* Codes_SRS_FILEUPLOADINPROGRESS_21_002: [If the `statusCallback` is null, the constructor shall throws IllegalArgumentException.] */
        if(statusCallback == null)
        {
            throw new IllegalArgumentException("status callback is null");
        }

        /* Codes_SRS_FILEUPLOADINPROGRESS_21_001: [The constructor shall sore the content of the `statusCallback`, and `statusCallbackContext`.] */
        this.statusCallback = statusCallback;
        this.statusCallbackContext = statusCallbackContext;
    }

    /**
     * Setter for the future task.
     *
     * @param task is the Future task.
     * @throws IllegalArgumentException if the task is null.
     */
    @SuppressWarnings("rawtypes")
    void setTask(Future task) throws IllegalArgumentException
    {
        /* Codes_SRS_FILEUPLOADINPROGRESS_21_004: [If the `task` is null, the setTask shall throws IllegalArgumentException.] */
        if(task == null)
        {
            throw new IllegalArgumentException("future task is null");
        }

        /* Codes_SRS_FILEUPLOADINPROGRESS_21_003: [The setTask shall sore the content of the `task`.] */
        this.task = task;
    }

    /**
     * Call the execute function on the statusCallback with the provided status and context.
     *
     * @param iotHubStatusCode is the status to report.
     */
    void triggerCallback(IotHubStatusCode iotHubStatusCode)
    {
        /* Codes_SRS_FILEUPLOADINPROGRESS_21_005: [The triggerCallback shall call the execute in `statusCallback` with the provided `iotHubStatusCode` and `statusCallbackContext`.] */
        statusCallback.execute(iotHubStatusCode, statusCallbackContext);
    }

    /**
     * Getter for the task cancellation.
     * 
     * @return boolean true if the Future task was cancelled or false if it is not.
     * @throws IOException is the task is null.
     */
    boolean isCancelled() throws IOException
    {
        /* Codes_SRS_FILEUPLOADINPROGRESS_21_007: [If the `task` is null, the isCancelled shall throws IOException.] */
        if(task == null)
        {
            throw new IOException("future task is null");
        }

        /* Codes_SRS_FILEUPLOADINPROGRESS_21_006: [The isCancelled shall return the value of isCancelled on the `task`.] */
        return this.task.isCancelled();
    }
}
