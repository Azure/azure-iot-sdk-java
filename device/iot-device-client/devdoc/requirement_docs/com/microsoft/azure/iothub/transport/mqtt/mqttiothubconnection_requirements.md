# MqttIotHubConnection Requirements

## Overview

An MQTT connection between a device and an IoT Hub. The connection talks to various messaging clients to exchange messages between mqtt lower layers and upper layers of the SDK.

## References

## Exposed API

```java
public final class MqttIotHubConnection implements MqttConnectionStateListener, IotHubTransportConnection
{
    public MqttIotHubConnection(DeviceClientConfig config) throws TransportException;

    public void open(Queue<DeviceClientConfig> deviceClientConfigs) throws TransportException;
    public void close() throws TransportException;

    private IotHubTransportMessage receiveMessage() throws TransportException;

    @Override
    public void setListener(IotHubListener listener) throws TransportException;

    @Override
    public IotHubStatusCode sendMessage(Message message) throws TransportException;

    @Override
    public boolean sendMessageResult(Message message, IotHubMessageResult result) throws TransportException;

    @Override
    public void onMessageArrived(int messageId);
}
```


### MqttIotHubConnection

```java
public MqttIotHubConnection(DeviceClientConfig config) throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_15_001: [**The constructor shall save the configuration.**]**

**SRS_MQTTIOTHUBCONNECTION_15_003: [**The constructor shall throw a new TransportException if any of the parameters of the configuration is null or empty.**]**

**SRS_MQTTIOTHUBCONNECTION_34_020: [**If the config has no shared access token, device key, or x509 certificates, this constructor shall throw an IllegalArgumentException.**]**


### open

```java
public void open(Queue<DeviceClientConfig> deviceClientConfigs) throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_15_004: [**The function shall establish an MQTT connection with an IoT Hub using the provided host name, user name, device ID, and sas token.**]**

**SRS_MQTTIOTHUBCONNECTION_25_018: [**The function shall establish an MQTT WS connection with a server uri as `wss://<hostName>/$iothub/websocket?iothub-no-client-cert=true` if websocket was enabled.**]**

**SRS_MQTTIOTHUBCONNECTION_25_019: [**The function shall establish an MQTT connection with a server uri as `ssl://<hostName>:8883` if websocket was not enabled.**]**

**SRS_MQTTIOTHUBCONNECTION_15_005: [**If an MQTT connection is unable to be established for any reason, the function shall throw a TransportException.**]**

**SRS_MQTTIOTHUBCONNECTION_15_006: [**If the MQTT connection is already open, the function shall do nothing.**]**

**SRS_MQTTIOTHUBCONNECTION_99_017: [**The function shall set DeviceClientConfig object needed for SAS token renewal.**]**

**SRS_MQTTIOTHUBCONNECTION_34_027: [**If this function is called while using websockets and x509 authentication, an UnsupportedOperation shall be thrown.**]**

**SRS_MQTTIOTHUBCONNECTION_34_030: [**This function shall instantiate this object's MqttMessaging object with this object as the listener.**]**

**SRS_MQTTIOTHUBCONNECTION_34_022: [**If the list of device client configuration objects is larger than 1, this function shall throw an UnsupportedOperationException.**]**


### close

```java
public void close() throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_15_006: [**The function shall close the MQTT connection.**]**

**SRS_MQTTIOTHUBCONNECTION_15_007: [**If the MQTT connection is closed, the function shall do nothing.**]**

**SRS_MQTTIOTHUBCONNECTION_34_037: [**If an IOException is encountered while closing the mqtt connection, this function shall set this object's state to CLOSED and rethrow that exception.**]**

**SRS_MQTTIOTHUBCONNECTION_34_021: [**If a TransportException is encountered while closing the three clients, this function shall set this object's state to closed and then rethrow the exception.**]**



### sendMessage

```java
public IotHubStatusCode sendMessage(Message message) throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_15_008: [**The function shall send an event message to the IoT Hub given in the configuration.**]**

**SRS_MQTTIOTHUBCONNECTION_15_009: [**The function shall send the message payload.**]**

**SRS_MQTTIOTHUBCONNECTION_15_010: [**If the message is null, the function shall return status code BAD_FORMAT.**]**

**SRS_MQTTIOTHUBCONNECTION_15_011: [**If the message was successfully received by the service, the function shall return status code OK_EMPTY.**]**

**SRS_MQTTIOTHUBCONNECTION_15_013: [**If the MQTT connection is closed, the function shall throw an IllegalStateException.**]**

**SRS_MQTTIOTHUBCONNECTION_34_035: [**If the sas token saved in the config has expired and needs to be renewed, this function shall return UNAUTHORIZED.**]**


### receiveMessage
```java
private IotHubTransportMessage receiveMessage() throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_15_014: [**The function shall attempt to consume a message from various messaging clients.**]**

**SRS_MQTTIOTHUBCONNECTION_15_015: [**If the MQTT connection is closed, the function shall throw a TransportException.**]**

**SRS_MQTTIOTHUBCONNECTION_34_017: [**If all of the messaging clients fail to receive, the function shall throw a TransportException.**]**


### setListener
```java
public void setListener(IotHubListener listener) throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_34_049: [**If the provided listener object is null, this function shall throw an IllegalArgumentException.**]**

**SRS_MQTTIOTHUBCONNECTION_34_050: [**This function shall save the provided listener object.**]**


### sendMessageResult
```java
public boolean sendMessageResult(Message message, IotHubMessageResult result) throws TransportException;
```

**SRS_MQTTIOTHUBCONNECTION_34_051: [**If this object has not received the provided message from the service, this function shall throw a TransportException.**]**

**SRS_MQTTIOTHUBCONNECTION_34_052: [**If this object has received the provided message from the service, this function shall retrieve the Mqtt messageId for that message.**]**

**SRS_MQTTIOTHUBCONNECTION_34_053: [**If the provided message has message type DEVICE_METHODS, this function shall invoke the methods client to send the ack and return the result.**]**

**SRS_MQTTIOTHUBCONNECTION_34_054: [**If the provided message has message type DEVICE_TWIN, this function shall invoke the twin client to send the ack and return the result.**]**

**SRS_MQTTIOTHUBCONNECTION_34_055: [**If the provided message has message type other than DEVICE_METHODS and DEVICE_TWIN, this function shall invoke the telemetry client to send the ack and return the result.**]**

**SRS_MQTTIOTHUBCONNECTION_34_056: [**If the ack was sent successfully, this function shall remove the provided message from the saved map of messages to acknowledge.**]**

**SRS_MQTTIOTHUBCONNECTION_34_057: [**If the provided message or result is null, this function shall throw a TransportException.**]**


###
```java
public void onMessageArrived(int messageId);
```

**SRS_MQTTIOTHUBCONNECTION_34_058: [**This function shall attempt to receive a message.**]**

**SRS_MQTTIOTHUBCONNECTION_34_059: [**If a transport message is successfully received, this function shall save it in this object's map of messages to be acknowledged along with the provided messageId.**]**

**SRS_MQTTIOTHUBCONNECTION_34_060: [**If a transport message is successfully received, and the message has a type of DEVICE_TWIN, this function shall set the callback and callback context of this object from the saved values in config for methods.**]**

**SRS_MQTTIOTHUBCONNECTION_34_061: [**If a transport message is successfully received, and the message has a type of DEVICE_METHODS, this function shall set the callback and callback context of this object from the saved values in config for twin.**]**

**SRS_MQTTIOTHUBCONNECTION_34_062: [**If a transport message is successfully received, and the message has a type of DEVICE_TELEMETRY, this function shall set the callback and callback context of this object from the saved values in config for telemetry.**]**

**SRS_MQTTIOTHUBCONNECTION_34_063: [**If a transport message is successfully received, this function shall notify its listener that a message was received and provide the received message.**]**
