/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

public interface FileUploadNotificationCallback
{
    /**
     * Called each time a file upload notification was received. The listener must return a delivery outcome so that
     * the amqp connection can acknowledge the message accordingly
     * @param fileUploadNotification The file upload notification that was received
     * @return Whether to Complete the message, Abandon it, or Reject it. See {@link DeliveryOutcome} for details
     */
    public DeliveryOutcome onFileUploadNotificationReceived(FileUploadNotification fileUploadNotification);
}
