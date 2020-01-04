/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

public interface FileUploadNotificationCallback
{
    /**
     * Called each time a file upload notification was received. The listener must return a delivery outcome so that
     * the amqp process can acknowledge the message accordingly
     * @param fileUploadNotification The received file upload notification
     * @return Whether to Complete the message, Abandon it, or Reject it
     */
    public DeliveryOutcome onFileUploadNotificationReceived(FileUploadNotification fileUploadNotification);
}
