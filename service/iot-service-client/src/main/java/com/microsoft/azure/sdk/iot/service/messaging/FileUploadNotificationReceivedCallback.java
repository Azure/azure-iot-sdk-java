// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

public interface FileUploadNotificationReceivedCallback
{
    public IotHubMessageResult onFileUploadNotificationReceived(FileUploadNotification notification);
}
