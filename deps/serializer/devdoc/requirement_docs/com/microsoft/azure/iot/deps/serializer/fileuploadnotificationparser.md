# File Upload Notification Requirements

## Overview

FileUploadNotification is a representation of a notification for the File Upload, with a Json deserializer.

## References

[File uploads with IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload)

## Exposed API

```java
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
public class FileUploadNotification
{
    public FileUploadNotification(String json) throws IllegalArgumentException;
    public String getDeviceId();
    public String getBlobUri();
    public String getBlobName();
    public String getLastUpdatedTime();
    public String getEnqueuedTimeUtc();
    public Integer getBlobSizeInBytesTag();
}
```

### FileUploadNotification
```java
/**
 * CONSTRUCTOR
 * Create an instance of the FileUploadNotification using the information in the provided json.
 *
 * @param json is the string that contains a valid json with the FileUpload notification.
 * @throws IllegalArgumentException if the json is null, empty, or not valid.
 */
public FileUploadNotification(String json) throws IllegalArgumentException
```
**SRS_FILE_UPLOAD_NOTIFICATION_21_001: [**The constructor shall create an instance of the FileUploadNotification.**]**  
**SRS_FILE_UPLOAD_NOTIFICATION_21_002: [**The constructor shall parse the provided json and initialize `correlationId`, `hostName`, `containerName`, `blobName`, and `sasToken` using the information in the json.**]**  
**SRS_FILE_UPLOAD_NOTIFICATION_21_003: [**If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.**]**  
**SRS_FILE_UPLOAD_NOTIFICATION_21_004: [**If the provided json do not contains a valid `deviceId`, `blobUri`, `blobName`, `lastUpdatedTime`, `enqueuedTimeUtc`, and `blobSizeInBytes`, the constructor shall throws IllegalArgumentException.**]**  
**SRS_FILE_UPLOAD_NOTIFICATION_21_005: [**If the provided json do not contains one of the keys `deviceId`, `blobUri`, `blobName`, `lastUpdatedTime`, `enqueuedTimeUtc`, and `blobSizeInBytes`, the constructor shall throws IllegalArgumentException.**]**  

### getDeviceId
```java
/**
 * Getter for the device identification.
 *
 * @return string with the device identification.
 */
public String getDeviceId()
```
**SRS_FILE_UPLOAD_NOTIFICATION_21_006: [**The getDeviceId shall return the string stored in `deviceId`.**]**  

### getBlobUri
```java
/**
 * Getter for the file uri.
 *
 * @return string with the blob URI.
 */
public String getBlobUri()
```
**SRS_FILE_UPLOAD_NOTIFICATION_21_007: [**The getBlobUri shall return the string stored in `blobUri`.**]**  

### getBlobName
```java
/**
 * Getter for the file name.
 *
 * @return string with the blob name.
 */
public String getBlobName()
```
**SRS_FILE_UPLOAD_NOTIFICATION_21_008: [**The getBlobName shall return the string stored in `blobName`.**]**  

### getLastUpdatedTime
```java
/**
 * Getter for the last update time.
 *
 * @return string with the last update time.
 */
public String getLastUpdatedTime()
```
**SRS_FILE_UPLOAD_NOTIFICATION_21_009: [**The getLastUpdatedTime shall return the string stored in `lastUpdatedTime`.**]**  

### getEnqueuedTimeUtc
```java
/**
 * Getter for the enqueue time UTC.
 *
 * @return string with the enqueue time UTC.
 */
public String getEnqueuedTimeUtc()
```
**SRS_FILE_UPLOAD_NOTIFICATION_21_010: [**The getEnqueuedTimeUtc shall return the string stored in `enqueuedTimeUtc`.**]**  

### getBlobSizeInBytesTag
```java
/**
 * Getter for the file size.
 *
 * @return integer with the blob size in bytes.
 */
public Integer getBlobSizeInBytesTag()
```
**SRS_FILE_UPLOAD_NOTIFICATION_21_011: [**The getBlobSizeInBytesTag shall return the string stored in `blobSizeInBytesTag`.**]**  

