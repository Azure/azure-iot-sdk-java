/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import java.io.IOException;
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

    private String deviceId;
    private String blobUri;
    private String blobName;
    private Date lastUpdatedTimeDate;
    private Long blobSizeInBytes;
    private Date enqueuedTimeUtcDate;

    public FileUploadNotification(String deviceId, String blobUri, String blobName, Date lastUpdatedTimeDate, Long blobSizeInBytes, Date enqueuedTimeUtcDate) throws IOException
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

    /**
     * Getter for Device ID
     * @return device id
     */
    public String getDeviceId()
    {

        //Codes_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_001: [ The getter for device ID ]
        return deviceId;
    }

    /**
     * Getter for BlobUri
     * @return BlobUri String
     */
    public String getBlobUri()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_003: [ The getter for Blob Uri ]
        return blobUri;
    }

    /**
     * Getter for BlobName
     * @return BlobName
     */
    public String getBlobName()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_005: [ The getter for blobName ]
        return blobName;
    }

    /**
     * Getter for LastUpdatedTimeDate
     * @return LastUpdatedTimeDate
     */
    public Date getLastUpdatedTimeDate()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_007: [ The getter for lastUpdatedTimeDate ]
        return lastUpdatedTimeDate;
    }

    /**
     * Getter for BlobSize in Bytes
     * @return BlobSize in Bytes
     */
    public Long getBlobSizeInBytes()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_009: [ The getter for blobSizeInBytes ]
        return blobSizeInBytes;
    }

    /**
     * Getter for EnqueuedTimeUtcDate
     * @return EnqueuedTimeUtcDate
     */
    public Date getEnqueuedTimeUtcDate()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_011: [ The getter for enqueuedTimeUtcDate ]
        return enqueuedTimeUtcDate;
    }

}
