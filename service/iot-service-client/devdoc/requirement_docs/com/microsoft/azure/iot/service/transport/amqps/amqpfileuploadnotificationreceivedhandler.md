# AmqpFileUploadNotificationReceivedHandler Requirements

## Overview

Instance of the QPID-Proton-J BaseHandler class overriding the events what are needed to handle the receive  operation for fileuploadnotification. Contains and sets connection parameters (path, port, endpoint). Maintains the layers of AMQP protocol (Link, Session, Connection, Transport). Creates and sets SASL authentication for transport. 

## References

## Exposed API

```java
public class AmqpFileUploadNotificationReceivedHandler 
{
    AmqpFileUploadNotificationReceivedHandler(String hostName, String userName, String sasToken, AmqpFeedbackReceivedEvent amqpFeedbackReceivedEvent);
    public void onDellivery(Event event);
    public void onConnectionBound(Event event);
    public void onConnectionInit(Event event);
    public void onLinkInit(Event event);
}
```

### AmqpFileUploadNotificationReceivedHandler

```java
AmqpFileUploadNotificationReceivedHandler(String hostName, String userName, String sasToken, AmqpFeedbackReceivedEvent amqpFeedbackReceivedEvent);
```
**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_001: [** The constructor shall copy all input parameters to private member variables for event processing **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_021: [** The constructor shall throw IllegalArgumentException if any of the parameters are null or empty **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_002: [** The constructor shall initialize a new Handshaker (Proton) object to handle communication handshake **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_003: [** The constructor shall initialize a new FlowController (Proton) object to handle communication handshake **]**

### onDelivery

```java
public void onDelivery (Event event);
```
**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_004: [** The event handler shall get the Link, Receiver and Delivery (Proton) objects from the event **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_005: [** The event handler shall read the received buffer **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_006: [** The event handler shall create a Message (Proton) object from the decoded buffer **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_007: [** The event handler shall settle the Delivery with the Accepted outcome **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_008: [** The event handler shall close the Session and Connection (Proton) **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_009: [** The event handler shall call the FeedbackReceived callback if it has been initialized **]**

### onConnectionBound

```java
public void onConnectionBound(Event event);
```
**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_019: [** The event handler shall set the SASL PLAIN authentication on the Transport using the given user name and sas token **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_010: [** The event handler shall set ANONYMUS_PEER authentication mode on the domain of the Transport **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_020: [** The event handler shall not initialize WebSocket if the protocol is AMQP **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_018: [** The event handler shall initialize WebSocket if the protocol is AMQP_WS **]**


### onConnectionInit

```java
public void onConnectionInit(Event event);
```
**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_011: [** The event handler shall set the host name on the connection **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_012: [** The event handler shall create a Session (Proton) object from the connection **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_013: [** The event handler shall create a Receiver (Proton) object and set the protocol tag on it to a predefined constant **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_014: [** The event handler shall open the Connection, the Session and the Receiver object **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_017: [** The Receiver object shall have the properties set to service client version identifier.**]**

### onLinkInit

```java
public void onLinkInit(Event event);
```
**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_015: [** The event handler shall create a new Target (Proton) object using the given endpoint address **]**

**SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_016: [** The event handler shall get the Link (Proton) object and set its target to the created Target (Proton) object **]**
