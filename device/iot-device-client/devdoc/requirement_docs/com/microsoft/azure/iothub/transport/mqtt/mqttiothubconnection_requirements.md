# MqttIotHubConnection Requirements

## Overview

An MQTT connection between a device and an IoT Hub. The connection talks to various messaging clients to exchange messages between mqtt lower layers and upper layers of the SDK.

## References

## Exposed API

```java
public final class MqttIotHubConnection
{
    public MqttIotHubConnection(DeviceClientConfig config);

    public void open() throws IOException;
    public void close();
    public IotHubStatusCode sendEvent(Message msg) throws IllegalStateException;
    public Message receiveMessage() throws IllegalStateException;

}
```


### MqttIotHubConnection

```java
public MqttIotHubConnection(DeviceClientConfig config)
```

**SRS_MQTTIOTHUBCONNECTION_15_001: [**The constructor shall save the configuration.**]**

**SRS_MQTTIOTHUBCONNECTION_15_003: [**The constructor shall throw a new IllegalArgumentException if any of the parameters of the configuration is null or empty.**]**


### open

```java
public void open() throws IOException, IllegalArgumentException;
```

**SRS_MQTTIOTHUBCONNECTION_15_004: [**The function shall establish an MQTT connection with an IoT Hub using the provided host name, user name, device ID, and sas token.**]**

**SRS_MQTTIOTHUBCONNECTION_25_018: [**The function shall establish an MQTT WS connection with a server uri as `wss://<hostName>/$iothub/websocket?iothub-no-client-cert=true` if websocket was enabled.**]**

**SRS_MQTTIOTHUBCONNECTION_25_019: [**The function shall establish an MQTT connection with a server uri as `ssl://<hostName>:8883` if websocket was not enabled.**]**

**SRS_MQTTIOTHUBCONNECTION_15_005: [**If an MQTT connection is unable to be established for any reason, the function shall throw an IOException.**]**

**SRS_MQTTIOTHUBCONNECTION_15_006: [**If the MQTT connection is already open, the function shall do nothing.**]**

**SRS_MQTTIOTHUBCONNECTION_99_017: [**The function shall set DeviceClientConfig object needed for SAS token renewal.**]**

### close

```java
public void close() throws IOException;
```

**SRS_MQTTIOTHUBCONNECTION_15_006: [**The function shall close the MQTT connection.**]**

**SRS_MQTTIOTHUBCONNECTION_15_007: [**If the MQTT connection is closed, the function shall do nothing.**]**



### sendEvent

```java
public IotHubStatusCode sendEvent(Message msg) throws IllegalStateException
```

**SRS_MQTTIOTHUBCONNECTION_15_008: [**The function shall send an event message to the IoT Hub given in the configuration.**]**

**SRS_MQTTIOTHUBCONNECTION_15_009: [**The function shall send the message payload.**]**

**SRS_MQTTIOTHUBCONNECTION_15_010: [**If the message is null, the function shall return status code BAD_FORMAT.**]**

**SRS_MQTTIOTHUBCONNECTION_15_011: [**If the message was successfully received by the service, the function shall return status code OK_EMPTY.**]**

**SRS_MQTTIOTHUBCONNECTION_15_012: [**If the message was not successfully received by the service, the function shall return status code ERROR.**]**

**SRS_MQTTIOTHUBCONNECTION_15_013: [**If the MQTT connection is closed, the function shall throw an IllegalStateException.**]**


### receiveMessage

```java
public Message receiveMessage() throws IllegalStateException, IOException;
```

**SRS_MQTTIOTHUBCONNECTION_15_014: [**The function shall attempt to consume a message from various messaging clients.**]**

**SRS_MQTTIOTHUBCONNECTION_15_015: [**If the MQTT connection is closed, the function shall throw an IllegalStateException.**]**

**SRS_MQTTIOTHUBCONNECTION_34_016: [**If any of the messaging clients throw an exception, The associated message will be removed from the queue and the exception will be propagated up to the receive task.**]**

**SRS_MQTTIOTHUBCONNECTION_34_017: [**If all of the messaging clients fail to receive, the function shall throw an UnsupportedOperationException.**]**
