# File Upload Response Requirements

## Overview

FileUploadResponse is a representation of a single container for the File Upload response, with a Json deserializer.

## References

[File uploads with IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload)

## Exposed API

```java
/**
 * Representation of a single container for the File Upload response with a Json deserializer.
 * Ex of JSON format:
 *  {
 *      "correlationId": "somecorrelationid",
 *      "hostname": "contoso.azure-devices.net",
 *      "containerName": "testcontainer",
 *      "blobName": "test-device1/image.jpg",
 *      "sasToken": "1234asdfSAStoken"
 *  }
 */
public class FileUploadResponse
{
    public FileUploadResponse(String json) throws IllegalArgumentException;

    public String getCorrelationId();
    public String getHostName();
    public String getContainerName();
    public String getBlobName();
    public String getSasToken();
}
```

### FileUploadResponse
```java
/**
 * CONSTRUCTOR
 *FileUploadResponseParser
 *
 * @param json is the string that contains a valid json with the FileUpload response.
 * @throws IllegalArgumentException if the json is null, empty, or not valid.
 */
public FileUploadResponse(String json) throws IllegalArgumentException
```
**SRS_FILE_UPLOAD_RESPONSE_21_001: [**The constructor shall create an instance of the FileUploadResponse.**]**  
**SRS_FILE_UPLOAD_RESPONSE_21_002: [**The constructor shall parse the provided json and initialize `correlationId`, `hostName`, `containerName`, `blobName`, and `sasToken` using the information in the json.**]**  
**SRS_FILE_UPLOAD_RESPONSE_21_003: [**If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.**]**  
**SRS_FILE_UPLOAD_RESPONSE_21_004: [**If the provided json do not contains a valid `correlationId`, `hostName`, `containerName`, `blobName`, and `sasToken`, the constructor shall throws IllegalArgumentException.**]**  
**SRS_FILE_UPLOAD_RESPONSE_21_005: [**If the provided json do not contains one of the keys `correlationId`, `hostName`, `containerName`, `blobName`, and `sasToken`, the constructor shall throws IllegalArgumentException.**]**  

### getCorrelationId
```java
/**
 * Getter for the Azure storage correlation identification.
 *
 * @return string with the correlation identification.
 */
public String getCorrelationId()
```
**SRS_FILE_UPLOAD_RESPONSE_21_006: [**The getCorrelationId shall return the string stored in `correlationId`.**]**  

### getHostName
```java
/**
 * Getter for the Azure storage host name.
 *
 * @return string with the host name.
 */
public String getHostName()
```
**SRS_FILE_UPLOAD_RESPONSE_21_007: [**The getHostName shall return the string stored in `hostName`.**]**  

### getContainerName
```java
/**
 * Getter for the container name in the Azure storage.
 *
 * @return string with the container name.
 */
public String getContainerName()
```
**SRS_FILE_UPLOAD_RESPONSE_21_008: [**The getContainerName shall return the string stored in `containerName`.**]**  

### getBlobName
```java
/**
 * Getter for the file name (blob name).
 *
 * @return string with the file name.
 */
public String getBlobName()
```
**SRS_FILE_UPLOAD_RESPONSE_21_009: [**The getBlobName shall return the string stored in `blobName`.**]**  

### getSasToken
```java
/**
 * Getter for the file sasToken.
 *
 * @return String with the file sasToken.
 */
public String getSasToken()
```
**SRS_FILE_UPLOAD_RESPONSE_21_010: [**The getSasToken shall return the string stored in `sasToken`.**]**  
