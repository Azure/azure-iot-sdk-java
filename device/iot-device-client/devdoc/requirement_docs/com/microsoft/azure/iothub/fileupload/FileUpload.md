# FileUpload Requirements

## Overview

Provide means to upload file in the Azure Storage using the IoTHub.

## References

[File uploads with IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload)  
[FileUploadTask](./FileUploadTask.md)

## Exposed API

```java
public final class FileUpload
{
    public FileUpload(DeviceClientConfig config) throws IllegalArgumentException;
    
    public synchronized void uploadToBlobAsync(
            String blobName, InputStream inputStream, long streamLength,
            IotHubEventCallback statusCallback, Object statusCallbackContext)
            throws IllegalArgumentException;    
    
    public void closeNow();

    protected static class FileUploadStatusCallBack implements IotHubEventCallback {}   
}
```


### FileUpload
```java
public FileUpload(DeviceClientConfig config) throws IllegalArgumentException;
```
**SRS_FILEUPLOAD_21_001: [**If the provided `config` is null, the constructor shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_002: [**The constructor shall create a new instance of `HttpsTransportManager` with the provided `config`.**]**  
**SRS_FILEUPLOAD_21_003: [**If the constructor fail to create the new instance of the `HttpsTransportManager`, it shall throw IllegalArgumentException, threw by the HttpsTransportManager constructor.**]**  
**SRS_FILEUPLOAD_21_012: [**The constructor shall create an pool of 10 threads to execute the uploads in parallel.**]**  
**SRS_FILEUPLOAD_21_013: [**The constructor shall create a list `fileUploadInProgressesSet` to control the pending uploads.**]**  
**SRS_FILEUPLOAD_21_014: [**The constructor shall create an Event callback `fileUploadStatusCallBack` to receive the upload status.**]**  
**SRS_FILEUPLOAD_21_015: [**If create the executor failed, the constructor shall throws IOException.**]**  

 
### uploadToBlobAsync
```java
public synchronized void uploadToBlobAsync(
        String blobName, InputStream inputStream, long streamLength,
        IotHubEventCallback statusCallback, Object statusCallbackContext)
        throws IllegalArgumentException;
```
**SRS_FILEUPLOAD_21_004: [**The uploadToBlobAsync shall asynchronously upload the InputStream `inputStream` to the blob in `blobName`.**]**  
**SRS_FILEUPLOAD_21_005: [**If the `blobName` is null or empty, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_006: [**If the `inputStream` is null or not available, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_011: [**If the `inputStream` failed to do I/O, the uploadToBlobAsync shall throw IOException, threw by the InputStream class.**]**  
**SRS_FILEUPLOAD_21_007: [**If the `streamLength` is negative, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_008: [**If the `statusCallback` is null, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_016: [**The uploadToBlobAsync shall create a `FileUploadInProgress` to store the fileUpload context.**]**  
**SRS_FILEUPLOAD_21_009: [**The uploadToBlobAsync shall create a `FileUploadTask` to control this file upload.**]**  
**SRS_FILEUPLOAD_21_010: [**The uploadToBlobAsync shall schedule the task `FileUploadTask` to immediately start.**]**  

### closeNow
```java
public void closeNow();
```
**SRS_FILEUPLOAD_21_017: [**The closeNow shall shutdown the thread pool by calling `shutdownNow`.**]**  
**SRS_FILEUPLOAD_21_018: [**If there is pending file uploads, the closeNow shall cancel the upload, and call the `statusCallback` reporting ERROR.**]**  

### FileUploadStatusCallBack
```java
protected static class FileUploadStatusCallBack implements IotHubEventCallback {}   
```
**SRS_FILEUPLOAD_21_019: [**The FileUploadStatusCallBack shall implements the `IotHubEventCallback` as result of the FileUploadTask.**]**  
**SRS_FILEUPLOAD_21_020: [**The FileUploadStatusCallBack shall call the `statusCallback` reporting the received status.**]**  
**SRS_FILEUPLOAD_21_021: [**The FileUploadStatusCallBack shall delete the `FileUploadInProgress` that store this file upload context.**]**  
**SRS_FILEUPLOAD_21_022: [**If the received context is not type of `FileUploadInProgress`, the FileUploadStatusCallBack shall log a error and ignore the message.**]**  
**SRS_FILEUPLOAD_21_023: [**If the FileUploadStatusCallBack failed to delete the `FileUploadInProgress`, it shall log a error.**]**  
