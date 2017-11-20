# AmqpsDeviceTwin Requirements

## Overview

Child class of DeviceOperations to provide device method specific attributes and objects for Device Client. 

## References

## Exposed API

```java
class AmqpsDeviceTwin
{
    AmqpsDeviceTwin(String deviceId) throws IllegalArgumentException;
    protected AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException;
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException;
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message) throws IOException;
    protected Message protonMessageToIoTHubMessage(MessageImpl protonMsg) throws IOException;
    protected MessageImpl iotHubMessageToProtonMessage(com.microsoft.azure.sdk.iot.device.Message message) throws IOException;
```

### AmqpsDeviceTwin

```java
public AmqpsDeviceTwin(String deviceId) throws IllegalArgumentException;
```

**SRS_AMQPSDEVICETWIN_12_001: [**The constructor shall throw IllegalArgumentException if the deviceId argument is null or empty.**]**

**SRS_AMQPSDEVICETWIN_12_002: [**The constructor shall set the sender and receiver endpoint path to IoTHub specific values.**]**

**SRS_AMQPSDEVICETWIN_12_003: [**The constructor shall concatenate a sender specific prefix to the sender link tag's current value.**]**

**SRS_AMQPSDEVICETWIN_12_004: [**The constructor shall concatenate a receiver specific prefix to the receiver link tag's current value.**]**

**SRS_AMQPSDEVICETWIN_12_005: [**The constructor shall insert the given deviceId argument to the sender and receiver link address.**]**

**SRS_AMQPSDEVICETWIN_12_006: [**The constructor shall add the API version key to the amqpProperties.**]**

**SRS_AMQPSDEVICETWIN_12_007: [**The constructor shall generate a UUID amd add it as a correlation ID to the amqpProperties.**]**

**SRS_AMQPSDEVICETWIN_12_009: [**The constructor shall create a HashMap for correlationId list.**]**


### isLinkFound
```java
protected Boolean isLinkFound(String linkName);
```
**SRS_AMQPSDEVICETWIN_12_046: [**The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.**]**

**SRS_AMQPSDEVICETWIN_12_047: [**The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.**]**

**SRS_AMQPSDEVICETWIN_12_048: [**The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.**]**


### sendMessageAndGetDeliveryHash

```java
public AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException;
```

**SRS_AMQPSDEVICETWIN_12_010: [**The function shall call the super function if the MessageType is DeviceTwin, and return with it's return value.**]**

**SRS_AMQPSDEVICETWIN_12_011: [**The function shall return with AmqpsSendReturnValue with false success and -1 delivery hash.**]**


### getMessageFromReceiverLink

```java
public AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
```

**SRS_AMQPSDEVICETWIN_12_012: [**The function shall call the super function.**]**

**SRS_AMQPSDEVICETWIN_12_013: [**The function shall set the MessageType to DeviceTwin if the super function returned not null.**]**

**SRS_AMQPSDEVICETWIN_12_014: [**The function shall return the super function return value.**]**


### convertFromProton

```java
public AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException;
```

**SRS_AMQPSDEVICETWIN_12_015: [**The function shall return null if the message type is not DeviceTwin.**]**

**SRS_AMQPSDEVICETWIN_12_016: [**The function shall convert the amqpsMessage to IoTHubTransportMessage.**]**

**SRS_AMQPSDEVICETWIN_12_017: [**The function shall create a new empty buffer for message body if the proton message body is null.**]**

**SRS_AMQPSDEVICETWIN_12_018: [**The function shall shall create a new buffer for message body and copy the proton message body to it.**]**

**SRS_AMQPSDEVICETWIN_12_019: [**The function shall create a new IotHubTransportMessage using the Proton message body and set the message type to DeviceTwin.**]**

**SRS_AMQPSDEVICETWIN_12_020: [**The function shall read the proton message annotations and set the status to the value of STATUS key.**]**

**SRS_AMQPSDEVICETWIN_12_021: [**The function shall read the proton message annotations and set the version to the value of VERSION key.**]**

**SRS_AMQPSDEVICETWIN_12_022: [**The function shall read the proton message annotations and set the operation type to SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE if the PROPERTIES_DESIRED resource exist.**]**

**SRS_AMQPSDEVICETWIN_12_044: [**The function shall set the IotHubTransportMessage correlationID to the proton correlationId.**]**

**SRS_AMQPSDEVICETWIN_12_023: [**The function shall find the proton correlation ID in the correlationIdList and if it is found, set the operation type to the related response.**]**

**SRS_AMQPSDEVICETWIN_12_043: [**The function shall remove the correlation from the correlationId list.**]**

**SRS_AMQPSDEVICETWIN_12_024: [**THe function shall set the operation type to SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE if the proton correlation ID is null.**]**

**SRS_AMQPSDEVICETWIN_12_025: [**The function shall copy the correlationId, messageId, To and userId properties to the IotHubTransportMessage properties.**]**

**SRS_AMQPSDEVICETWIN_12_026: [**The function shall copy the Proton application properties to IotHubTransportMessage properties excluding the reserved property names.**]**

**SRS_AMQPSDEVICETWIN_12_027: [**The function shall create a AmqpsConvertFromProtonReturnValue and set the message field to the new IotHubTransportMessage.**]**

**SRS_AMQPSDEVICETWIN_12_028: [**The function shall create a AmqpsConvertFromProtonReturnValue and copy the DeviceClientConfig callback and context to it.**]**


### convertToProton

```java
public AmqpsConvertToProtonReturnValue convertToProton(Message message) throws IOException;
```

**SRS_AMQPSDEVICETWIN_12_029: [**The function shall return null if the message type is not DeviceTwin.**]**

**SRS_AMQPSDEVICETWIN_12_030: [**The function shall convert the IoTHubTransportMessage to a proton message.**]**

**SRS_AMQPSDEVICETWIN_12_031: [**The function shall copy the correlationId, messageId properties to the Proton message properties.**]**

**SRS_AMQPSDEVICETWIN_12_045: [**The function shall add the correlationId to the correlationIdList if it is not null.**]**

**SRS_AMQPSDEVICETWIN_12_032: [**The function shall copy the user properties to Proton message application properties excluding the reserved property names.**]**

**SRS_AMQPSDEVICETWIN_12_033: [**The function shall set the proton message annotation operation field to GET if the IotHubTransportMessage operation type is GET_REQUEST.**]**

**SRS_AMQPSDEVICETWIN_12_034: [**The function shall set the proton message annotation operation field to PATCH if the IotHubTransportMessage operation type is UPDATE_REPORTED_PROPERTIES_REQUEST.**]**

**SRS_AMQPSDEVICETWIN_12_035: [**The function shall set the proton message annotation resource field to "/properties/reported" if the IotHubTransportMessage operation type is UPDATE_REPORTED_PROPERTIES_REQUEST.**]**

**SRS_AMQPSDEVICETWIN_12_036: [**The function shall set the proton message annotation operation field to PUT if the IotHubTransportMessage operation type is SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**

**SRS_AMQPSDEVICETWIN_12_037: [**The function shall set the proton message annotation resource field to "/notifications/twin/properties/desired" if the IotHubTransportMessage operation type is SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**

**SRS_AMQPSDEVICETWIN_12_038: [**The function shall set the proton message annotation operation field to DELETE if the IotHubTransportMessage operation type is UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**

**SRS_AMQPSDEVICETWIN_12_039: [**The function shall set the proton message annotation resource field to "/notifications/twin/properties/desired" if the IotHubTransportMessage operation type is UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**

**SRS_AMQPSDEVICETWIN_12_040: [**The function shall set the proton message body using the IotHubTransportMessage body.**]**

**SRS_AMQPSDEVICETWIN_12_041: [**The function shall create a AmqpsConvertToProtonReturnValue and set the message field to the new proton message.**]**

**SRS_AMQPSDEVICETWIN_12_042: [**The function shall create a AmqpsConvertToProtonReturnValue and set the message type to DeviceTwin.**]**
