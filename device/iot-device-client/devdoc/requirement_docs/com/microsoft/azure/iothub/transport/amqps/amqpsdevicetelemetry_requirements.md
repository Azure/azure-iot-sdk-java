# AmqpsDeviceTelemetry Requirements

## Overview

Child class of AmqpsDeviceOperations to provide telemetry specific attributes and objects for Device Client. 

## References

## Exposed API

```java
class AmqpsDeviceTelemetry
{
    AmqpsDeviceTelemetry(DeviceClientConfig deviceClientConfig) throws IllegalArgumentException;
    protected Boolean isLinkFound(String linkName);
    protected AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException;    
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig);
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message);
    protected Message protonMessageToIoTHubMessage(MessageImpl protonMsg);
    protected MessageImpl iotHubMessageToProtonMessage(com.microsoft.azure.sdk.iot.device.Message message);
```

### AmqpsDeviceTelemetry

```java
public AmqpsDeviceTelemetry(DeviceClientConfig deviceClientConfig) throws IllegalArgumentException;
```

**SRS_AMQPSDEVICETELEMETRY_12_001: [**The constructor shall throw IllegalArgumentException if the deviceClientConfig argument is null.**]**

**SRS_AMQPSDEVICETELEMETRY_12_002: [**The constructor shall set the sender and receiver endpoint path to IoTHub specific values.**]**

**SRS_AMQPSDEVICETELEMETRY_12_003: [**The constructor shall concatenate a sender specific prefix to the sender link tag's current value.**]**

**SRS_AMQPSDEVICETELEMETRY_12_004: [**The constructor shall concatenate a receiver specific prefix to the receiver link tag's current value.**]**

**SRS_AMQPSDEVICETELEMETRY_12_005: [**The constructor shall insert the given deviceId argument to the sender and receiver link address.**]**


### isLinkFound

```java
protected Boolean isLinkFound(String linkName);
```

**SRS_AMQPSDEVICETELEMETRY_12_026: [**The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.**]**

**SRS_AMQPSDEVICETELEMETRY_12_027: [**The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.**]**

**SRS_AMQPSDEVICETELEMETRY_12_028: [**The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.**]**


### sendMessageAndGetDeliveryHash

```java
public AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException;
```

**SRS_AMQPSDEVICETELEMETRY_12_006: [**The function shall return an AmqpsSendReturnValue object with false and -1 if the message type is not DeviceTelemetry.**]**

**SRS_AMQPSDEVICETELEMETRY_12_007: [**The function shall call the super function with the arguments and return with it's return value.**]**


### getMessageFromReceiverLink

```java
public AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
```

**SRS_AMQPSDEVICETELEMETRY_12_020: [**The function shall call the super function.**]**

**SRS_AMQPSDEVICETELEMETRY_12_021: [**The function shall set the MessageType to DeviceTelemetry if the super function returned not null.**]**

**SRS_AMQPSDEVICETELEMETRY_12_022: [**The function shall return the super function return value.**]**


### convertFromProton

```java
public AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig)
```

**SRS_AMQPSDEVICETELEMETRY_12_008: [**The function shall return null if the Proton message type is not null or DeviceTelemetry.**]**

**SRS_AMQPSDEVICETELEMETRY_12_024: [**The function shall shall create a new buffer for message body and copy the proton message body to it.**]**

**SRS_AMQPSDEVICETELEMETRY_12_025: [**The function shall create a new empty buffer for message body if the proton message body is null.**]**

**SRS_AMQPSDEVICETELEMETRY_12_009: [**The function shall create a new IoTHubMessage using the Proton message body.**]**

**SRS_AMQPSDEVICETELEMETRY_12_010: [**The function shall copy the correlationId, messageId, To and userId properties to the IotHubMessage properties.**]**

**SRS_AMQPSDEVICETELEMETRY_12_011: [**The function shall copy the Proton application properties to IoTHubMessage properties excluding the reserved property names.**]**

**SRS_AMQPSDEVICETELEMETRY_12_012: [**The function shall create a new AmqpsConvertFromProtonReturnValue object and fill it with the converted message and the user callback and user context values from the deviceClientConfig.**]**

**SRS_AMQPSDEVICETELEMETRY_12_013: [**The function shall return with the new AmqpsConvertFromProtonReturnValue object.**]**


### convertToProton

```java
public AmqpsConvertToProtonReturnValue convertToProton(Message message);
```

**SRS_AMQPSDEVICETELEMETRY_12_014: [**The function shall return null if the Proton message type is not null or DeviceTelemetry.**]**

**SRS_AMQPSDEVICETELEMETRY_12_015: [**The function shall create a new Proton message using the IoTHubMessage body.**]**

**SRS_AMQPSDEVICETELEMETRY_12_016: [**The function shall copy the correlationId, messageId properties to the Proton message properties.**]**

**SRS_AMQPSDEVICETELEMETRY_12_017: [**The function shall copy the user properties to Proton message application properties excluding the reserved property names.**]**

**SRS_AMQPSDEVICETELEMETRY_12_023: [**The function shall set the proton message body using the IotHubTransportMessage body.**]**

**SRS_AMQPSDEVICETELEMETRY_12_018: [**The function shall create a new AmqpsConvertToProtonReturnValue object and fill it with the Proton message and the message type.**]**

**SRS_AMQPSDEVICETELEMETRY_12_019: [**The function shall return with the new AmqpsConvertToProtonReturnValue object.**]**


