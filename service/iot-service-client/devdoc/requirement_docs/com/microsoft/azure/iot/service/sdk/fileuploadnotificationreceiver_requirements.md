# FileUploadNotificationReceiver Requirements

## Overview

FileUploadNotificationReceiver is a specialized receiver whose ReceiveAsync method returns a FileUploadNotification.

## References

([IoT Hub SDK.doc](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload)

## Exposed API

```java
public class FileUploadNotificationReceiver extends Receiver
{
    public FileUploadNotificationReceiver(String hostname, String username, String sasToken);
    public void open();
    public void close();
    public FileUploadNotification receive();
    public FileUploadNotification receive(long timeoutMs);
    public CompletableFuture openAsync();
    public CompletableFuture closeAsync();
    public CompletableFuture receiveAsync();
    public CompletableFuture receiveAsync(long timeoutMs);
}
```
### FileUploadNotificationReceiver

```java
public FileUploadNotificationReceiver (String hostname, String username, String sasToken);
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_001: [** The constructor shall throw IllegalArgumentException if any the input string is null or empty **]**

**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_002: [** The constructor shall create a new instance of AmqpFileUploadNotificationReceive object **]**

### open

```java
public void open();
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_004: [** The function shall call open() on the member AmqpFileUploadNotificationReceive object **]**

### close

```java
public void close();
```

**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_006: [** The function shall call close() on the member AmqpFileUploadNotificationReceive object **]**

### receive

```java
public FileUploadNotification receive();
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_007: [** The function shall call receive(long timeoutMs) function with the default timeout **]**

### receive

```java
public FileUploadNotification receive(long timeoutMs);
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_008: [** The function shall throw IOException if the member AmqpFileUploadNotificationReceive object has not been initialized **]**

**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_009: [** The function shall call receive() on the member AmqpFileUploadNotificationReceive object and return with the result **]**

### openAsync

```java
public CompletableFuture openAsync();
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_010: [** The function shall create an async wrapper around the open() function call **]**

### closeAsync

```java
public CompletableFuture closeAsync();
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_011: [** The function shall create an async wrapper around the close() function call **]**

### receiveAsync

```java
public CompletableFuture receiveAsync();
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_012: [** The function shall create an async wrapper around the receive() function call using the default timeout **]**

### receiveAsync

```java
public CompletableFuture receiveAsync(long timeoutMs);
```
**SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATIONRECEIVER_25_013: [** The function shall create an async wrapper around the receive(long timeoutMs) function call **]**
