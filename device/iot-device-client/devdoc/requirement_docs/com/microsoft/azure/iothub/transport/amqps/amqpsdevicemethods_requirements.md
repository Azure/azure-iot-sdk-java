# AmqpsDeviceMethods Requirements

## Overview

Child class of DeviceOperations to provide device method specific attributes and objects for Device Client. 

## References

## Exposed API

```java
public final class AmqpsDeviceMethods
{
    AmqpsDeviceMethods(String deviceId) throws IllegalArgumentException;
    protected AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException;
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig);
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message);
    protected Message protonMessageToIoTHubMessage(MessageImpl protonMsg);
    protected Message protonMessageToIoTHubMessage(MessageImpl protonMsg);
```

### AmqpsDeviceMethods

```java
AmqpsDeviceMethods(String deviceId) throws IllegalArgumentException
```

**SRS_AMQPSDEVICEMETHODS_12_001: [**The constructor shall throw IllegalArgumentException if the deviceClientConfig argument is null.**]**

**SRS_AMQPSDEVICEMETHODS_12_002: [**The constructor shall set the sender and receiver endpoint path to IoTHub specific values.**]**

**SRS_AMQPSDEVICEMETHODS_12_003: [**The constructor shall concatenate a sender specific prefix to the sender link tag's current value.**]**

**SRS_AMQPSDEVICEMETHODS_12_004: [**The constructor shall concatenate a receiver specific prefix to the receiver link tag's current value.**]**

**SRS_AMQPSDEVICEMETHODS_12_005: [**The constructor shall insert the given deviceId argument to the sender and receiver link address.**]**

**SRS_AMQPSDEVICEMETHODS_12_006: [**The constructor shall add API version key and API version value to the amqpProperties.**]**

**SRS_AMQPSDEVICEMETHODS_12_007: [**The constructor shall add correlation ID key and deviceId value to the amqpProperties.**]**


### isLinkFound
```java
protected Boolean isLinkFound(String linkName);
```

**SRS_AMQPSDEVICEMETHODS_12_047: [**The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.**]**

**SRS_AMQPSDEVICEMETHODS_12_048: [**The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.**]**

**SRS_AMQPSDEVICEMETHODS_12_049: [**The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.**]**


### sendMessageAndGetDeliveryHash

```java
protected AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException;
```

**SRS_AMQPSDEVICEMETHODS_12_010: [**The function shall call the super function if the MessageType is DeviceMethods, and return with it's return value.**]**

**SRS_AMQPSDEVICEMETHODS_12_011: [**The function shall return with AmqpsSendReturnValue with false success and -1 delivery hash.**]**


### getMessageFromReceiverLink

```java
protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
```

**SRS_AMQPSDEVICEMETHODS_12_012: [**The function shall call the super function.**]**

**SRS_AMQPSDEVICEMETHODS_12_013: [**The function shall set the MessageType to DeviceMethods if the super function returned not null.**]**

**SRS_AMQPSDEVICEMETHODS_12_014: [**The function shall return the super function return value.**]**


### convertFromProton

```java
protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig);
```

**SRS_AMQPSDEVICEMETHODS_12_015: [**The function shall return null if the message type is not DeviceMethods.**]**

**SRS_AMQPSDEVICEMETHODS_12_016: [**The function shall convert the amqpsMessage to IoTHubTransportMessage.**]**

**SRS_AMQPSDEVICEMETHODS_12_017: [**The function shall create a new empty buffer for message body if the proton message body is null.**]**

**SRS_AMQPSDEVICEMETHODS_12_018: [**The function shall shall create a new buffer for message body and copy the proton message body to it.**]**

**SRS_AMQPSDEVICEMETHODS_12_019: [**The function shall create a new IotHubTransportMessage using the Proton message body and set the message type to DeviceMethods.**]**

**SRS_AMQPSDEVICEMETHODS_12_025: [**The function shall copy the method name from Proton application properties and set IotHubTransportMessage method name with it.**]**

**SRS_AMQPSDEVICEMETHODS_12_026: [**The function shall copy the Proton application properties to IotHubTransportMessage properties excluding the reserved property names.**]**

**SRS_AMQPSDEVICEMETHODS_12_046: [**The function shall set the device operation type to DEVICE_OPERATION_METHOD_RECEIVE_REQUEST on IotHubTransportMessage.**]**

**SRS_AMQPSDEVICEMETHODS_12_027: [**The function shall create a AmqpsConvertFromProtonReturnValue and set the message field to the new IotHubTransportMessage.**]**

**SRS_AMQPSDEVICEMETHODS_12_028: [**The function shall create a AmqpsConvertFromProtonReturnValue and copy the DeviceClientConfig callback and context to it.**]**


### convertToProton

```java
protected AmqpsConvertToProtonReturnValue convertToProton(Message message);
```

**SRS_AMQPSDEVICEMETHODS_12_029: [**The function shall return null if the message type is not DeviceMethods.**]**

**SRS_AMQPSDEVICEMETHODS_12_030: [**The function shall convert the IoTHubTransportMessage to a proton message.**]**

**SRS_AMQPSDEVICEMETHODS_12_031: [**The function shall copy the correlationId, messageId properties to the Proton message properties.**]**

**SRS_AMQPSDEVICEMETHODS_12_032: [**The function shall copy the user properties to Proton message application properties excluding the reserved property names.**]**

**SRS_AMQPSDEVICEMETHODS_12_033: [**The function shall set the proton message status field to the value of IoTHubTransportMessage status field.**]**

**SRS_AMQPSDEVICEMETHODS_12_040: [**The function shall set the proton message body using the IotHubTransportMessage body.**]**

**SRS_AMQPSDEVICEMETHODS_12_041: [**The function shall create a AmqpsConvertToProtonReturnValue and set the message field to the new proton message.**]**

**SRS_AMQPSDEVICEMETHODS_12_042: [**The function shall create a AmqpsConvertToProtonReturnValue and set the message type to DeviceMethods.**]**
