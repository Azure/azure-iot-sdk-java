# MqttIotHubConnection Requirements

## Overview

An MQTT connection between a device and an IoT Hub. The connection talks to various messaging clients to exchange messages between mqtt lower layers and upper layers of the SDK.

## References

## Exposed API

```java
public final class MqttIotHubConnection implements MqttConnectionStateListener, IotHubTransportConnection
{
    public MqttIotHubConnection(DeviceClientConfig config) throws TransportException;

    public void open() throws TransportException;
    public void close() throws TransportException;
    public IotHubStatusCode sendEvent(Message msg) throws TransportException;
    public Message receiveMessage() throws TransportException;
    
    void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext) throws TransportException;
    
    public void onConnectionEstablished();
    public void onConnectionLost(Throwable throwable);

    public void addListener(IotHubListener listener) throws TransportException
}
```


### MqttIotHubConnection

```java
public MqttIotHubConnection(DeviceClientConfig config) throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_15_001: [**The constructor shall save the configuration.**]**

**SRS_MQTTIOTHUBCONNECTION_15_003: [**The constructor shall throw a new TransportException if any of the parameters of the configuration is null or empty.**]**

**SRS_MQTTIOTHUBCONNECTION_34_020: [**If the config has no shared access token, device key, or x509 certificates, this constructor shall throw a TransportException.**]**


### open

```java
public void open() throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_15_004: [**The function shall establish an MQTT connection with an IoT Hub using the provided host name, user name, device ID, and sas token.**]**

**SRS_MQTTIOTHUBCONNECTION_25_018: [**The function shall establish an MQTT WS connection with a server uri as `wss://<hostName>/$iothub/websocket?iothub-no-client-cert=true` if websocket was enabled.**]**

**SRS_MQTTIOTHUBCONNECTION_25_019: [**The function shall establish an MQTT connection with a server uri as `ssl://<hostName>:8883` if websocket was not enabled.**]**

**SRS_MQTTIOTHUBCONNECTION_15_005: [**If an MQTT connection is unable to be established for any reason, the function shall throw a TransportException.**]**

**SRS_MQTTIOTHUBCONNECTION_15_006: [**If the MQTT connection is already open, the function shall do nothing.**]**

**SRS_MQTTIOTHUBCONNECTION_99_017: [**The function shall set DeviceClientConfig object needed for SAS token renewal.**]**

**SRS_MQTTIOTHUBCONNECTION_34_027: [**If this function is called while using websockets and x509 authentication, an UnsupportedOperation shall be thrown.**]**

**SRS_MQTTIOTHUBCONNECTION_34_030: [**This function shall instantiate this object's MqttMessaging object with this object as the listener.**]**


### close

```java
public void close() throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_15_006: [**The function shall close the MQTT connection.**]**

**SRS_MQTTIOTHUBCONNECTION_15_007: [**If the MQTT connection is closed, the function shall do nothing.**]**

**SRS_MQTTIOTHUBCONNECTION_34_037: [**If an IOException is encountered while closing the mqtt connection, this function shall set this object's state to CLOSED and rethrow that exception.**]**



### sendEvent

```java
public IotHubStatusCode sendEvent(Message msg) throws TransportException
```

**SRS_MQTTIOTHUBCONNECTION_15_008: [**The function shall send an event message to the IoT Hub given in the configuration.**]**

**SRS_MQTTIOTHUBCONNECTION_15_009: [**The function shall send the message payload.**]**

**SRS_MQTTIOTHUBCONNECTION_15_010: [**If the message is null, the function shall return status code BAD_FORMAT.**]**

**SRS_MQTTIOTHUBCONNECTION_15_011: [**If the message was successfully received by the service, the function shall return status code OK_EMPTY.**]**

**SRS_MQTTIOTHUBCONNECTION_15_013: [**If the MQTT connection is closed, the function shall throw a TransportException.**]**

**SRS_MQTTIOTHUBCONNECTION_34_035: [**If the sas token saved in the config has expired and needs to be renewed, this function shall return UNAUTHORIZED.**]**


### receiveMessage

```java
public Message receiveMessage() throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_15_014: [**The function shall attempt to consume a message from various messaging clients.**]**

**SRS_MQTTIOTHUBCONNECTION_15_015: [**If the MQTT connection is closed, the function shall throw a TransportException.**]**

**SRS_MQTTIOTHUBCONNECTION_34_017: [**If all of the messaging clients fail to receive, the function shall throw a TransportException.**]**


### registerConnectionStateCallback
```java
void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext) throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_34_033: [**If the provided callback object is null, this function shall throw a TransportException.**]**


### onConnectionLost

```java
public void onConnectionLost(Throwable throwable);
```

**SRS_MQTTIOTHUBCONNECTION_34_038: [**If the provided throwable is not an instance of MqttException, this function shall notify the listeners of that throwable.**]**


### onConnectionEstablished

```java
public void onConnectionEstablished();
```

**SRS_MQTTIOTHUBCONNECTION_34_036: [**This function shall notify its listeners that connection was established successfully.**]**

### addListener
```java
public void addListener(IotHubListener listener) throws TransportException
```

**SRS_MQTTIOTHUBCONNECTION_34_049: [**If the provided listener object is null, this function shall throw a TransportException.**]**

**SRS_MQTTIOTHUBCONNECTION_34_050: [**This function shall save the provided listener object.**]**
