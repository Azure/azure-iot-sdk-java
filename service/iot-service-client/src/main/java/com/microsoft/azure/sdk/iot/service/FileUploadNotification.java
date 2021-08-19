/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import lombok.Getter;

import java.util.Date;

public class FileUploadNotification
{

   /* Ex of FileUploadNotification format:
         *      deviceId : "mydevice",
         *      blobUri: "https://{storage account}.blob.core.windows.net/{container name}/mydevice/myfile.jpg",
         *      blobName: "mydevice/myfile.jpg",
         *      lastUpdatedTime: 2016-06-01T21:22:41+00:00,
         *      blobSizeInBytes: 1234,
         *      enqueuedTimeUtc: 2016-06-01T21:22:43.7996883Z
   */

    @Getter
    private final String deviceId;

    @Getter
    private final String blobUri;

    @Getter
    private final String blobName;

    @Getter
    private final Date lastUpdatedTimeDate;

    @Getter
    private final Long blobSizeInBytes;

    @Getter
    private final Date enqueuedTimeUtcDate;

    public FileUploadNotification(String deviceId, String blobUri, String blobName, Date lastUpdatedTimeDate, Long blobSizeInBytes, Date enqueuedTimeUtcDate)
    {
        if (deviceId == null || blobUri == null || blobName == null || lastUpdatedTimeDate == null || blobSizeInBytes == null || enqueuedTimeUtcDate == null ||
                deviceId.isEmpty() || blobName.isEmpty() || blobUri.isEmpty())
        {
            // Codes_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_002: [** If any of the parameters are null or empty then this method shall throw IllegalArgumentException.**]
            throw new IllegalArgumentException("Null data for notification is not accepted");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_001: [** The constructor shall save all the parameters only if they are valid **]
        this.deviceId = deviceId;
        this.blobUri = blobUri;
        this.blobName = blobName;
        this.lastUpdatedTimeDate = lastUpdatedTimeDate;
        this.blobSizeInBytes = blobSizeInBytes;
        this.enqueuedTimeUtcDate = enqueuedTimeUtcDate;
    }
}
