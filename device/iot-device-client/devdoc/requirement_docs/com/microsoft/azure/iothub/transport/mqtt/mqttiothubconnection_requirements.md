# MqttIotHubConnection Requirements

## Overview

An MQTT connection between a device and an IoT Hub. The connection talks to various messaging clients to exchange messages between mqtt lower layers and upper layers of the SDK.

## References

## Exposed API

```java
public final class MqttIotHubConnection implements MqttConnectionStateListener
{
    public MqttIotHubConnection(DeviceClientConfig config) throws IllegalArgumentException;

    public void open() throws IOException;
    public void close();
    public IotHubStatusCode sendEvent(Message msg) throws IllegalStateException;
    public Message receiveMessage() throws IllegalStateException;
    
    void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext);
    
    @Override
    public void connectionEstablished();

    @Override
    public void connectionLost();

}
```


### MqttIotHubConnection

```java
public MqttIotHubConnection(DeviceClientConfig config) throws IllegalArgumentException;
```

**SRS_MQTTIOTHUBCONNECTION_15_001: [**The constructor shall save the configuration.**]**

**SRS_MQTTIOTHUBCONNECTION_15_003: [**The constructor shall throw a new IllegalArgumentException if any of the parameters of the configuration is null or empty.**]**

**SRS_MQTTIOTHUBCONNECTION_34_020: [**If the config has no shared access token, device key, or x509 certificates, this constructor shall throw an IllegalArgumentException.**]**


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

**SRS_MQTTIOTHUBCONNECTION_34_027: [**If this function is called while using websockets and x509 authentication, an UnsupportedOperation shall be thrown.**]**

**SRS_MQTTIOTHUBCONNECTION_34_030: [**This function shall instantiate this object's MqttMessaging object with this object as the listener.**]**


### close

```java
public void close() throws IOException;
```

**SRS_MQTTIOTHUBCONNECTION_15_006: [**The function shall close the MQTT connection.**]**

**SRS_MQTTIOTHUBCONNECTION_15_007: [**If the MQTT connection is closed, the function shall do nothing.**]**

**SRS_MQTTIOTHUBCONNECTION_34_037: [**If an IOException is encountered while closing the mqtt connection, this function shall set this object's state to CLOSED and rethrow that exception.**]**



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

**SRS_MQTTIOTHUBCONNECTION_34_035: [**If the sas token saved in the config has expired and needs to be renewed, this function shall return UNAUTHORIZED.**]**

**SRS_MQTTIOTHUBCONNECTION_34_036: [**If the sas token saved in the config has expired and needs to be renewed and if there is a connection state callback saved, this function shall invoke that callback with Status SAS_TOKEN_EXPIRED.**]**


### receiveMessage

```java
public Message receiveMessage() throws IllegalStateException, IOException;
```

**SRS_MQTTIOTHUBCONNECTION_15_014: [**The function shall attempt to consume a message from various messaging clients.**]**

**SRS_MQTTIOTHUBCONNECTION_15_015: [**If the MQTT connection is closed, the function shall throw an IllegalStateException.**]**

**SRS_MQTTIOTHUBCONNECTION_34_016: [**If any of the messaging clients throw an exception, The associated message will be removed from the queue and the exception will be propagated up to the receive task.**]**

**SRS_MQTTIOTHUBCONNECTION_34_017: [**If all of the messaging clients fail to receive, the function shall throw an UnsupportedOperationException.**]**


### registerConnectionStateCallback
```java
void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext);
```

**SRS_MQTTIOTHUBCONNECTION_34_033: [**If the provided callback object is null, this function shall throw an IllegalArgumentException.**]**

**SRS_MQTTIOTHUBCONNECTION_34_034: [**This function shall save the provided callback and callback context.**]**


### connectionLost

```java
public void connectionLost();
```

**SRS_MQTTIOTHUBCONNECTION_34_028: [**If this object's connection state callback is not null, this function shall fire that callback with the saved context and status CONNECTION_DROP.**]**


### connectionEstablished

```java
public void connectionEstablished();
```

**SRS_MQTTIOTHUBCONNECTION_34_029: [**If this object's connection state callback is not null, this function shall fire that callback with the saved context and status CONNECTION_SUCCESS.**]**
