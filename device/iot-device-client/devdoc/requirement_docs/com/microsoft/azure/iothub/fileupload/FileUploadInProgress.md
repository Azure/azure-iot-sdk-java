# FileUploadInProgress Requirements

## Overview

This is an auxiliary class to store the artifacts of the file uploads in progress.

## References

## Exposed API

```java
public final class FileUploadInProgress
{
    FileUploadInProgress(IotHubEventCallback statusCallback, Object statusCallbackContext) throws IllegalArgumentException;
    void setTask(Future task);
 
    void triggerCallback(IotHubStatusCode iotHubStatusCode);
    boolean isCancelled() throws IOException;
}
```


### FileUploadInProgress
```java
FileUploadInProgress(IotHubEventCallback statusCallback, Object statusCallbackContext) throws IllegalArgumentException;
```
**SRS_FILEUPLOADINPROGRESS_21_001: [**The constructor shall sore the content of the `statusCallback`, and `statusCallbackContext`.**]**  
**SRS_FILEUPLOADINPROGRESS_21_002: [**If the `statusCallback` is null, the constructor shall throws IllegalArgumentException.**]**  

### setTask
```java
void setTask(Future task);
```
**SRS_FILEUPLOADINPROGRESS_21_003: [**The setTask shall sore the content of the `task`.**]**  
**SRS_FILEUPLOADINPROGRESS_21_004: [**If the `task` is null, the setTask shall throws IllegalArgumentException.**]**  

### triggerCallback
```java
void triggerCallback(IotHubStatusCode iotHubStatusCode)
```
**SRS_FILEUPLOADINPROGRESS_21_005: [**The triggerCallback shall call the execute in `statusCallback` with the provided `iotHubStatusCode` and `statusCallbackContext`.**]**  

### isCancelled
```java
boolean isCancelled() throws IOException;
```
**SRS_FILEUPLOADINPROGRESS_21_006: [**The isCancelled shall return the value of isCancelled on the `task`.**]**  
**SRS_FILEUPLOADINPROGRESS_21_007: [**If the `task` is null, the isCancelled shall throws IOException.**]**  
