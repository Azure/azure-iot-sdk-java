# File Upload Request Requirements

## Overview

FileUploadRequest is a representation of a single File Upload request with a Json serializer.

## References

[File uploads with IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload)

## Exposed API

```java
/**
 * Representation of a single File Upload request with a Json serializer.
 * Ex of JSON format:
 *  {
 *      "blobName": "{name of the file for which a SAS URI will be generated}"
 *  }
 */
public class FileUploadRequest
{
    public static FileUploadRequest(String blobName) throws IllegalArgumentException;
    public String toJson();
}
```


### CONSTRUCTOR
```java
/**
 * CONSTRUCTOR
 *FileUploadRequestParser
 *
 * @param blobName is the name of the blob (file name in the blob)
 * @throws IllegalArgumentException if the blobName is null, empty, or not valid.
 */
public FileUploadRequest(String blobName) throws IllegalArgumentException
```
**SRS_FILE_UPLOAD_REQUEST_21_001: [**The constructor shall create an instance of the FileUploadRequest.**]**  
**SRS_FILE_UPLOAD_REQUEST_21_002: [**The constructor shall set the `blobName` in the new class with the provided blob name.**]**  
**SRS_FILE_UPLOAD_REQUEST_21_003: [**If the provided blob name is null, empty, or not valid, the constructor shall throws IllegalArgumentException.**]**  

### toJson
```java
/**
 * Convert this class in a valid json.
 * 
 * @return a valid json that represents the content of this class.
 */
public String toJson()
```
**SRS_FILE_UPLOAD_REQUEST_21_004: [**The toJson shall return a string with a json that represents the contend of the FileUploadRequest.**]**  
