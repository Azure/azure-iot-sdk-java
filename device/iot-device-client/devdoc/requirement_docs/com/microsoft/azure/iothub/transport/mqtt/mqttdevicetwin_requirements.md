# MqttDeviceTwin Requirements

## Overview

MqttDeviceTwin is a concrete class extending Mqtt. This class implements all the abstract methods from MQTT and overrides
the receive, onReconnect, and onReconnectComplete events.

## References

## Exposed API

```java
public final class MqttDeviceTwin extends Mqtt
{
    public MqttDeviceTwin() throws IOException;

    public Message receive() throws IOException;
    public void send(DeviceTwinMessage message) throws IOException;

    public void start() throws IOException;
    public void stop() throws IOException;

}
```

### MqttDeviceTwin

```java
public MqttDeviceTwin();
```

**SRS_MQTTDEVICETWIN_25_001: [**The constructor shall instantiate super class without any parameters.**]**


### start

```java
public void start() throws IOException;
```

**SRS_MQTTDEVICETWIN_25_019: [**start method shall subscribe to twin response topic ($iothub/twin/res/#) if connected and throw IoException otherwise.**]**


### stop

```java
public void stop() throws IOException;
```


### send

```java
 public void send(final DeviceTwinMessage message) throws IOException;
```

**SRS_MQTTDEVICETWIN_25_021: [**send method shall throw an exception if the message is null.**]**

**SRS_MQTTDEVICETWIN_25_022: [**send method shall return if the message is not of Type DeviceTwin.**]**

**SRS_MQTTDEVICETWIN_25_023: [**send method shall throw an exception if the getDeviceTwinOperationType() returns DEVICE_TWIN_OPERATION_UNKNOWN.**]**

**SRS_MQTTDEVICETWIN_25_024: [**send method shall build the get request topic of the format mentioned in spec ($iothub/twin/GET/?$rid={request id}) if the operation is of type DEVICE_TWIN_OPERATION_GET_REQUEST.**]**

**SRS_MQTTDEVICETWIN_25_025: [**send method shall throw an exception if message contains a null or empty request id if the operation is of type DEVICE_TWIN_OPERATION_GET_REQUEST.**]**

**SRS_MQTTDEVICETWIN_25_026: [**send method shall build the update reported properties request topic of the format mentioned in spec ($iothub/twin/PATCH/properties/reported/?$rid={request id}&$version={base version}) if the operation is of type DEVICE_TWIN_OPERATION_UPDATE_REPORTED_PROPERTIES_REQUEST.**]**

**SRS_MQTTDEVICETWIN_25_027: [**send method shall throw an exception if message contains a null or empty request id if the operation is of type DEVICE_TWIN_OPERATION_UPDATE_REPORTED_PROPERTIES_REQUEST.**]**

**SRS_MQTTDEVICETWIN_25_028: [**send method shall not throw an exception if message contains a null or empty version if the operation is of type DEVICE_TWIN_OPERATION_UPDATE_REPORTED_PROPERTIES_REQUEST as version is optional**]**

**SRS_MQTTDEVICETWIN_25_029: [**send method shall build the subscribe to desired properties request topic of the format mentioned in spec ($iothub/twin/PATCH/properties/desired/?$version={new version}) if the operation is of type DEVICE_TWIN_OPERATION_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**

**SRS_MQTTDEVICETWIN_25_030: [**send method shall not throw an exception if message contains a null or empty version if the operation is of type DEVICE_TWIN_OPERATION_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST as version is optional**]**

**SRS_MQTTDEVICETWIN_25_031: [**send method shall publish a message to the IOT Hub on the respective publish topic by calling method publish().**]**

**SRS_MQTTDEVICETWIN_25_032: [**send method shall subscribe to desired properties by calling method subscribe() on topic "$iothub/twin/PATCH/properties/desired/#" specified in spec if the operation is DEVICE_TWIN_OPERATION_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**


### receive

```java
 public Message receive() throws IOException;
```

**SRS_MQTTDEVICETWIN_34_034: [**If the call peekMessage returns null or empty string then this method shall do nothing and return null**]**

**SRS_MQTTDEVICETWIN_25_035: [**This method shall call peekMessage to get the message payload from the received Messages queue corresponding to the messaging client's operation.**]**

**SRS_MQTTDEVICETWIN_25_035: [**This method shall call peekMessage to get the message payload from the received Messages queue corresponding to the messaging client's operation.**]**

**SRS_MQTTDEVICETWIN_25_037: [**This method shall parse topic to look for only either twin response topic or twin patch topic and thorw unsupportedoperation exception other wise.**]**

**SRS_MQTTDEVICETWIN_25_038: [**If the topic is of type response topic then this method shall parse further for status and set it for the message by calling setStatus for the message**]**

**SRS_MQTTDEVICETWIN_25_039: [**If the topic is of type response topic and if status is either a non 3 digit number or not found then receive shall throw IOException **]**

**SRS_MQTTDEVICETWIN_25_040: [**If the topic is of type response topic then this method shall parse further to look for request id which if found is set by calling setRequestId**]**

**SRS_MQTTDEVICETWIN_25_041: [**If the topic is of type response topic then this method shall parse further to look for version which if found is set by calling setVersion**]**

**SRS_MQTTDEVICETWIN_25_042: [**If the topic is of type patch for desired properties then this method shall parse further to look for version which if found is set by calling setVersion**]**

**SRS_MQTTDEVICETWIN_25_043: [**If the topic is not of type response for desired properties then this method shall throw unsupportedoperation exception**]**

**SRS_MQTTDEVICETWIN_25_044: [**If the topic is of type response then this method shall set data and operation type as DEVICE_TWIN_OPERATION_GET_RESPONSE if data is not null**]**

**SRS_MQTTDEVICETWIN_25_045: [**If the topic is of type response then this method shall set empty data and operation type as DEVICE_TWIN_OPERATION_UPDATE_REPORTED_PROPERTIES_RESPONSE if data is null or empty**]**

**SRS_MQTTDEVICETWIN_25_046: [**If the topic is of type patch for desired properties then this method shall set the data and operation type as DEVICE_TWIN_OPERATION_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE if data is not null or empty**]**

**SRS_MQTTDEVICETWIN_25_047: [**If the topic is of type patch for desired properties then this method shall throw unsupportedoperation exception if data is null or empty**]**

























    
    
   