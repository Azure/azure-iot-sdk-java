# FileUploadNotification Requirements

## Overview

FileUploadNotification represents the data received from IotHub upon receiving a notification on uploading a file.

Ex of JSON format:
            {
                "deviceId":"mydevice",
                "blobUri":"https://{storage account}.blob.core.windows.net/{container name}/mydevice/myfile.jpg",
                "blobName":"mydevice/myfile.jpg",
                "lastUpdatedTime":"2016-06-01T21:22:41+00:00",
                "blobSizeInBytes":1234,
                "enqueuedTimeUtc":"2016-06-01T21:22:43.7996883Z"
            }

## References

([IoT Hub SDK File Upload.doc](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload))

## Exposed API

```java
public class FileUploadNotification
{
    public FileUploadNotification(String deviceId, String blobUri, String blobName, Date lastUpdatedTimeDate, Long blobSizeInBytes, Date enqueuedTimeUtcDate) throws IOException;
    
    public String getDeviceId();

    public String getBlobUri();

    public String getBlobName();

    public Date getLastUpdatedTimeDate();

    public Long getBlobSizeInBytes();

    public Date getEnqueuedTimeUtcDate();
}

### FileUploadNotification
```java
public FileUploadNotification(String deviceId, String blobUri, String blobName, Date lastUpdatedTimeDate, Long blobSizeInBytes, Date enqueuedTimeUtcDate) throws IOException;
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_001: [** The constructor shall save all the parameters only if they are valid **]**

**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_002: [** If any of the parameters are null or empty then this method shall throw IllegalArgumentException.**]**

```
### getDeviceId
```java
public String getDeviceId();
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_003: [** The getter for device ID **]**

### getBlobUri
```java
public String getBlobUri();
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_004: [** The getter for Blob Uri **]**

### getBlobName
```java
public String getBlobName();
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_005: [** The getter for blobName **]**

### getLastUpdatedTimeDate
```java
 public Date getLastUpdatedTimeDate();
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_006: [** The getter for lastUpdatedTimeDate **]**

### getBlobSizeInBytes
```java
public Long getBlobSizeInBytes();
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_007: [** The getter for blobSizeInBytes **]**

## getEnqueuedTimeUtcDate
```java
 public Date getEnqueuedTimeUtcDate();
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_008: [** The getter for enqueuedTimeUtcDate **]**
