// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

/**
 * The callback interface for handling file upload notifications received by {@link FileUploadNotificationReceiver} instances.
 */
public interface FileUploadNotificationReceivedCallback
{
    /**
     * This callback is executed each time a file upload notification is received by the {@link FileUploadNotificationReceiver}.
     * Each file upload notification must be completed or abandoned. See {@link IotHubMessageResult} for more details on what
     * completed and abandoned mean.
     * @param notification The received file upload notification.
     * @return The way to acknowledge the received file upload notification.
     */
    public IotHubMessageResult onFileUploadNotificationReceived(FileUploadNotification notification);
}
