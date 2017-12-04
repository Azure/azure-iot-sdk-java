# MqttDeviceMethod Requirements

## Overview

MqttDeviceMethod is a concrete class extending Mqtt. This class implements all the abstract methods from MQTT and overrides the receive for Mqtt Methods execution on the device.

## References

([IoTHub DeviceMethod.doc](to https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-direct-methods)

## Exposed API

```java
public final class MqttDeviceMethod extends Mqtt
{
    public MqttDeviceMethod() throws IOException;

    public Message receive() throws IOException;
    public void send(DeviceMethodMessage message) throws IOException;

    public void start() throws IOException;
    public void stop() throws IOException;

}
```

### MqttDeviceMethod

```java
public MqttDeviceMethod() throws IOException;
```

**SRS_MQTTDEVICEMETHOD_25_001: [**The constructor shall instantiate super class without any parameters.**]**

**SRS_MQTTDEVICEMETHOD_25_002: [**The constructor shall create subscribe and response topics strings for device methods as per the spec.**]**


### start

```java
public void start() throws IOException;
```

**SRS_MQTTDEVICEMETHOD_25_014: [**start method shall just mark that this class is ready to start.**]**


### stop

```java
public void stop() throws IOException;
```


### send

```java
 public void send(final DeviceMethodMessage message) throws IOException;
```

**SRS_MQTTDEVICEMETHOD_25_016: [**send method shall throw an IllegalArgumentException if the message is null.**]**

**SRS_MQTTDEVICEMETHOD_25_017: [**send method shall do nothing and return if the message is not of Type DeviceMethod.**]**

**SRS_MQTTDEVICEMETHOD_25_018: [**send method shall throw an IOException if device method has not been started yet.**]**

**SRS_MQTTDEVICEMETHOD_25_019: [**send method shall throw an IOException if the getDeviceOperationType() is not of type DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST or DEVICE_OPERATION_METHOD_SEND_RESPONSE .**]**

**SRS_MQTTDEVICEMETHOD_25_020: [**send method shall subscribe to topic from spec ($iothub/methods/POST/#) if the operation is of type DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST.**]**

**SRS_MQTTDEVICEMETHOD_25_021: [**send method shall throw an IOException if message contains a null or empty request id and if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.**]**

**SRS_MQTTDEVICEMETHOD_25_022: [**send method shall build the publish topic of the format mentioned in spec ($iothub/methods/res/{status}/?$rid={request id}) and publish if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.**]**

**SRS_MQTTDEVICEMETHOD_25_023: [**send method shall throw an IOException if a response is sent without having a method invoke on the request id and if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.**]**


### receive

```java
 public Message receive() throws IOException;
```

**SRS_MQTTDEVICEMETHOD_25_026: [**This method shall call peekMessage to get the message payload from the received Messages queue corresponding to the messaging client's operation.**]**

**SRS_MQTTDEVICEMETHOD_34_027: [**This method shall parse message to look for Post topic ($iothub/methods/POST/) and return null other wise.**]**

**SRS_MQTTDEVICEMETHOD_25_028: [**If the topic is of type post topic then this method shall parse further for method name and set it for the message by calling setMethodName for the message**]**

**SRS_MQTTDEVICEMETHOD_25_029: [**If method name not found or is null then receive shall throw IOException **]**

**SRS_MQTTDEVICEMETHOD_25_030: [**If the topic is of type post topic then this method shall parse further to look for request id which if found is set by calling setRequestId**]**

**SRS_MQTTDEVICEMETHOD_25_031: [**If request id is not found or is null then receive shall throw IOException **]**

**SRS_MQTTDEVICEMETHOD_25_032: [**If the topic is of type post topic and if method name and request id has been successfully parsed then this method shall set operation type as DEVICE_OPERATION_METHOD_RECEIVE_REQUEST **]**






















    
    
   