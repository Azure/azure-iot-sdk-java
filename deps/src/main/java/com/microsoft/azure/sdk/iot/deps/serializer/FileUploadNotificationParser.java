// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Representation of the notification of a single File Upload, with a Json deserializer.
 * Ex of JSON format:
 *  {
 *      "deviceId":"mydevice",
 *      "blobUri":"https://{storage account}.blob.core.windows.net/{container name}/mydevice/myfile.jpg",
 *      "blobName":"mydevice/myfile.jpg",
 *      "lastUpdatedTime":"2016-06-01T21:22:41+00:00",
 *      "blobSizeInBytes":1234,
 *      "enqueuedTimeUtc":"2016-06-01T21:22:43.7996883Z"
 *  }
 */
public class FileUploadNotificationParser
{
    private static final String DEVICE_ID_TAG = "deviceId";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(DEVICE_ID_TAG)
    private String deviceId = null;

    private static final String BLOB_URI_TAG = "blobUri";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(BLOB_URI_TAG)
    private String blobUri = null;

    private static final String BLOB_NAME_TAG = "blobName";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(BLOB_NAME_TAG)
    private String blobName = null;

    private static final String LAST_UPDATED_TIME_TAG = "lastUpdatedTime";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(LAST_UPDATED_TIME_TAG)
    private String lastUpdatedTime = null;

    private Date lastUpdatedTimeDate;

    private static final String BLOB_SIZE_IN_BYTES_TAG = "blobSizeInBytes";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(BLOB_SIZE_IN_BYTES_TAG)
    private Long blobSizeInBytes = null;

    private static final String ENQUEUED_TIME_UTC_TAG = "enqueuedTimeUtc";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(ENQUEUED_TIME_UTC_TAG)
    private String enqueuedTimeUtc = null;

    private Date enqueuedTimeUtcDate;

    /**
     * CONSTRUCTOR
     * Create an instance of the FileUploadNotification using the information in the provided json.
     *
     * @param json is the string that contains a valid json with the FileUpload notification.
     * @throws IllegalArgumentException if the json is null, empty, or not valid.
     */
    public FileUploadNotificationParser(String json) throws IllegalArgumentException
    {
        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_001: [The constructor shall create an instance of the FileUploadNotification.] */
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        FileUploadNotificationParser fileUploadNotificationParser;

        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_003: [If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.] */
        ParserUtility.validateStringUTF8(json);
        try
        {
            fileUploadNotificationParser = gson.fromJson(json, FileUploadNotificationParser.class);
        }
        catch (Exception malformed)
        {
            throw new IllegalArgumentException("Malformed json:" + malformed);
        }

        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_004: [If the provided json do not contains a valid `deviceId`, `blobUri`, `blobName`, `lastUpdatedTime`, `enqueuedTimeUtc`, and `blobSizeInBytes`, the constructor shall throws IllegalArgumentException.] */
        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_005: [If the provided json do not contains one of the keys `deviceId`, `blobUri`, `blobName`, `lastUpdatedTime`, and `enqueuedTimeUtc`, the constructor shall throws IllegalArgumentException.] */
        ParserUtility.validateStringUTF8(fileUploadNotificationParser.deviceId);
        ParserUtility.validateStringUTF8(fileUploadNotificationParser.blobUri);
        ParserUtility.validateBlobName(fileUploadNotificationParser.blobName);
        ParserUtility.validateStringUTF8(fileUploadNotificationParser.enqueuedTimeUtc);
        ParserUtility.validateStringUTF8(fileUploadNotificationParser.lastUpdatedTime);

        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_012: [If the provided json do not the keys `blobSizeInBytes`, the constructor shall assume the default value 0 for the blob size.] */
        if(fileUploadNotificationParser.blobSizeInBytes == null)
        {
            fileUploadNotificationParser.blobSizeInBytes = 0L;
        }
        else if(fileUploadNotificationParser.blobSizeInBytes < 0)
        {
            throw new IllegalArgumentException("negative size");
        }

        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_002: [The constructor shall parse the provided json and initialize `correlationId`, `hostName`, `containerName`, `blobName`, and `sasToken` using the information in the json.] */
        this.deviceId = fileUploadNotificationParser.deviceId;
        this.blobUri = fileUploadNotificationParser.blobUri;
        this.blobName = fileUploadNotificationParser.blobName;
        this.lastUpdatedTime = fileUploadNotificationParser.lastUpdatedTime;
        this.enqueuedTimeUtc = fileUploadNotificationParser.enqueuedTimeUtc;
        this.blobSizeInBytes = fileUploadNotificationParser.blobSizeInBytes;
        this.enqueuedTimeUtcDate = ParserUtility.getDateTimeUtc(this.enqueuedTimeUtc);
        this.lastUpdatedTimeDate = ParserUtility.stringToDateTimeOffset(this.lastUpdatedTime);
    }

    /**
     * Getter for the device identification.
     *
     * @return string with the device identification.
     */
    public String getDeviceId()
    {
        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_006: [The getDeviceId shall return the string stored in `deviceId`.] */
        return this.deviceId;
    }

    /**
     * Getter for the file uri.
     *
     * @return string with the blob URI.
     */
    public String getBlobUri()
    {
        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_007: [The getBlobUri shall return the string stored in `blobUri`.] */
        return this.blobUri;
    }

    /**
     * Getter for the file name.
     *
     * @return string with the blob name.
     */
    public String getBlobName()
    {
        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_008: [The getBlobName shall return the string stored in `blobName`.] */
        return this.blobName;
    }

    /**
     * Getter for the last update time.
     *
     * @return string with the last update time.
     */
    public Date getLastUpdatedTime()
    {
        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_009: [The getLastUpdateTime shall return the string stored in `lastUpdateTime`.] */
        return this.lastUpdatedTimeDate;
    }

    /**
     * Getter for the enqueued time UTC.
     *
     * @return string with the enqueued time UTC.
     */
    public Date getEnqueuedTimeUtc()
    {
        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_010: [The getEnqueuedTimeUtc shall return the string stored in `enqueuedTimeUtcDate`.] */

        return this.enqueuedTimeUtcDate;
    }

    /**
     * Getter for the file size.
     *
     * @return long with the blob size in bytes.
     */
    public Long getBlobSizeInBytesTag()
    {
        /* Codes_SRS_FILE_UPLOAD_NOTIFICATION_21_011: [The getBlobSizeInBytesTag shall return the integer stored in `blobSizeInBytes`.] */
        return this.blobSizeInBytes;
    }

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    @SuppressWarnings("unused")
    protected FileUploadNotificationParser()
    {
    }
}
