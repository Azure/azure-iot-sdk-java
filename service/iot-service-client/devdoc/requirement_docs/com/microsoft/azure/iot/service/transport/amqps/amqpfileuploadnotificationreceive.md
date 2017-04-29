# AmqpFileUploadNotificationReceive Requirements

## Overview

Instance of the QPID-Proton-J BaseHandler class overriding the events what are needed to handle high level open, close methods and feedback received event.

## References

## Exposed API

```java
public class AmqpFileUploadNotificationReceive
{
    AmqpFileUploadNotificationReceive(String hostName, String userName, String sasToken);
    public void onReactorInit(Event event);
    public void open();
    public void close();
    public FeedbackBatch receive(String deviceId, long timeoutMs);
    public onFeedbackReceived(String feedbackJson);
}
```
### AmqpFileUploadNotificationReceive

```java
AmqpFileUploadNotificationReceive(String hostName, String userName, String sasToken);
```
**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_001: [** The constructor shall copy all input parameters to private member variables for event processing **]**

### onReactorInit

```java
public void onReactorInit(Event event);
```
**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_002: [** The event handler shall set the member AmqpsReceiveHandler object to handle the given connection events **]**

### open

```java
public void open();
```
**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_003: [** The function shall create an AmqpsReceiveHandler object to handle reactor events **]**

### close

```java
public void close();
```
**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_004: [** The function shall invalidate the member AmqpsReceiveHandler object **]**

### receive

```java
public FeedbackBatch receive(String deviceId, long timeoutMs);
```
**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_005: [** The function shall initialize the Proton reactor object **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_006: [** The function shall start the Proton reactor object **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_007: [** The function shall wait for specified timeout to check for any feedback message **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_008: [** The function shall stop and free the Proton reactor object **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_009: [** The function shall throw IOException if the send handler object is not initialized **]**

### onFeedbackReceived

```java
public void onFeedbackReceived(String feedbackJson);
```
**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVE_25_010: [** The function shall parse the received Json string to FileUploadNotification object **]**

