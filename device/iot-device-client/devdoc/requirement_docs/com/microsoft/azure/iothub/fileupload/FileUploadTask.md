# FileUploadTask Requirements

## Overview

Runnable that provide means to upload file in the Azure Storage using the IoTHub.

## References

[File uploads with IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload)  

## Exposed API

```java
public final class FileUploadTask implements Runnable
{
    FileUploadTask(String blobName, InputStream inputStream, long streamLength, HttpsTransportManager httpsTransportManager,
                    IotHubEventCallback userCallback, Object userCallbackContext);
    
    public void run();    
}
```


### FileUploadTask
```java
FileUploadTask(String blobName, InputStream inputStream, long streamLength, HttpsTransportManager httpsTransportManager,
                IotHubEventCallback userCallback, Object userCallbackContext);
```
**SRS_FILEUPLOADTASK_21_001: [**If the `blobName` is null or empty, the constructor shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOADTASK_21_002: [**If the `inputStream` is null, the constructor shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOADTASK_21_003: [**If the `streamLength` is negative, the constructor shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOADTASK_21_004: [**If the `httpsTransportManager` is null, the constructor shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOADTASK_21_005: [**If the `userCallback` is null, the constructor shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOADTASK_21_006: [**The constructor shall store all the provided parameters.**]**  
 
 
### run
```java
public void run();
```
**REQUEST:** Start the file upload requesting the sasToken from the IoT Hub.  
**SRS_FILEUPLOADTASK_21_007: [**The run shall create a FileUpload request message, by using the FileUploadRequestParser.**]**  
Json request example:
```json
{ 
    "blobName": "[name of the file for which a SAS URI will be generated]" 
} 
```
**SRS_FILEUPLOADTASK_21_008: [**The run shall set the message method as `POST`.**]**  
**SRS_FILEUPLOADTASK_21_009: [**The run shall set the message URI path as `/files`.**]**  
**SRS_FILEUPLOADTASK_21_010: [**The run shall open the connection with the iothub, using the httpsTransportManager.**]**  
**SRS_FILEUPLOADTASK_21_011: [**The run shall send the blob request message to the iothub, using the httpsTransportManager.**]**  
**SRS_FILEUPLOADTASK_21_012: [**The run shall close the connection with the iothub, using the httpsTransportManager.**]**  
**SRS_FILEUPLOADTASK_21_013: [**If result status for the blob request is not `OK`, or `OK_EMPTY`, the run shall call the userCallback bypassing the received status, and abort the upload.**]**  
**SRS_FILEUPLOADTASK_21_014: [**If result status for the blob request is `OK_EMPTY`, the run shall call the userCallback with the stratus `ERROR`, and abort the upload.**]**  
**SRS_FILEUPLOADTASK_21_031: [**If run failed to send the request, it shall call the userCallback with the status `ERROR`, and abort the upload.**]**  

**PARSE REQUEST RESPONSE:** Parse the response message from the IoT Hub to extract the blob information and the correlation ID.  
**SRS_FILEUPLOADTASK_21_015: [**If the iothub accepts the request, it shall provide a `responseMessage` with the blob information with a correlationId.**]**  
Json response example:
```json
{ 
    "correlationId": "somecorrelationid", 
    "hostname": "contoso.azure-devices.net", 
    "containerName": "testcontainer", 
    "blobName": "test-device1/image.jpg", 
    "sasToken": "1234asdfSAStoken" 
} 
```
**SRS_FILEUPLOADTASK_21_016: [**If the `responseMessage` is null, empty, do not contains a valid json, or if the information in json is not correct, the run shall call the `userCallback` reporting the error, and abort the upload.**]**  
**SRS_FILEUPLOADTASK_21_017: [**The run shall parse and store the blobName and correlationId in the response, by use the FileUploadResponseParser.**]**  
**SRS_FILEUPLOADTASK_21_018: [**The run shall create a blob URI `blobUri` with the format `https://[hostName]/[containerName]/[blobName,UTF-8][sasToken]`.**]**  
**SRS_FILEUPLOADTASK_21_032: [**If create the blob URI failed, the run shall call the `userCallback` reporting the error, and abort the upload.**]**  

**UPLOAD TO BLOB:** Using the Azure Storage APIs, upload the inputStream to the blob.  
**SRS_FILEUPLOADTASK_21_019: [**The run shall create a `CloudBlockBlob` using the `blobUri`.**]**  
**SRS_FILEUPLOADTASK_21_020: [**The run shall upload the `inputStream` with the `streamLength` to the created `CloudBlockBlob`.**]**  

**NOTIFY:** Notify the IoT Hub and the user about the result of the upload.
**SRS_FILEUPLOADTASK_21_021: [**If the upload to blob succeed, the run shall create a notification the IoT Hub with `isSuccess` equals true, `statusCode` equals 0.**]**  
**SRS_FILEUPLOADTASK_21_022: [**If the upload to blob failed, the run shall create a notification the IoT Hub with `isSuccess` equals false, `statusCode` equals -1.**]**  
**SRS_FILEUPLOADTASK_21_023: [**The run shall create a FileUpload status notification message, by using the FileUploadStatusParser.**]**  
Json notification example:
```json
{ 
    "correlationId": "[correlation ID returned by the initial request]", 
    "isSuccess": true, 
    "statusCode": 1234, 
    "statusDescription": "Description of the status" 
} 
```
**SRS_FILEUPLOADTASK_21_024: [**The run shall set the message method as `POST`.**]**  
**SRS_FILEUPLOADTASK_21_025: [**The run shall set the message URI path as `/files/notifications`.**]**  
**SRS_FILEUPLOADTASK_21_026: [**The run shall open the connection with the iothub, using the httpsTransportManager.**]**  
**SRS_FILEUPLOADTASK_21_027: [**The run shall send the blob request message to the iothub, using the httpsTransportManager.**]**  
**SRS_FILEUPLOADTASK_21_028: [**The run shall close the connection with the iothub, using the httpsTransportManager.**]**  
**SRS_FILEUPLOADTASK_21_029: [**The run shall call the `userCallback` with the final response status.**]**  
**SRS_FILEUPLOADTASK_21_030: [**If the upload to blob failed, the run shall call the `userCallback` reporting an error status `ERROR`.**]**  
**SRS_FILEUPLOADTASK_21_033: [**If run failed to send the notification, it shall call the userCallback with the stratus `ERROR`, and abort the upload.**]**  

