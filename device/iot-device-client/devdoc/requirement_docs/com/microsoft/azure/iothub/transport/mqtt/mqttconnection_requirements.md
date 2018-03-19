# MqttConnection Requirements

## Overview

An MqttConnection is class defining the connection options that can be performed over MQTT between a device and an IoT Hub.

## References

## Exposed API

```java
public class MqttConnection
{
    MqttConnection(String serverURI, String clientId, String userName, String password, SSLContext iotHubSSLContext) throws IOException;

    void setMqttCallback(MqttCallback mqttCallback) throws TransportException;
    MqttAsyncClient getMqttAsyncClient();
    ConcurrentLinkedQueue<Pair<String, byte[]>> getAllReceivedMessages();
    Object getMqttLock();
    MqttConnectOptions getConnectionOptions();
    void setMqttAsyncClient(MqttAsyncClient mqttAsyncClient);
    boolean sendMessageAcknowledgement(int messageId) throws TransportException;
}
```

### MqttConnection

```java
MqttConnection(String serverURI, String clientId, String userName, String password, SSLContext iotHubSSLContext) throws IOException
```

**SRS_MQTTCONNECTION_25_001: [**The constructor shall throw IllegalArgumentException if any of the input parameters are null other than password.**]**

**SRS_MQTTCONNECTION_25_002: [**The constructor shall throw IllegalArgumentException if serverUri, clientId, userName, password are empty.**]**

**SRS_MQTTCONNECTION_25_003: [**The constructor shall create lock, queue for this MqttConnection.**]**

**SRS_MQTTCONNECTION_25_004: [**The constructor shall create an MqttAsync client and update the connection options using the provided serverUri, clientId, userName, password and sslContext.**]**

### setMqttCallback

```java
void setMqttCallback(MqttCallback mqttCallback) throws TransportException;
```

**SRS_MQTTCONNECTION_25_005: [**This method shall set the callback for Mqtt.**]**

**SRS_MQTTCONNECTION_25_006: [**This method shall throw IllegalArgumentException if callback is null.**]**

### getMqttAsyncClient

```java
 MqttAsyncClient getMqttAsyncClient();
```

**SRS_MQTTCONNECTION_25_007: [**Getter for the MqttAsyncClient.**]**

### getAllReceivedMessages

```java
 ConcurrentLinkedQueue<Pair<String, byte[]>> getAllReceivedMessages()
```

**SRS_MQTTCONNECTION_25_008: [**Getter for the Message Queue.**]**

### getMqttLock

```java
 Object getMqttLock()
```

**SRS_MQTTCONNECTION_25_009: [**Getter for the Mqtt Lock on this connection.**]**

### getConnectionOptions

```java
 MqttConnectOptions getConnectionOptions()
```

**SRS_MQTTCONNECTION_25_010: [**Getter for the MqttConnectionOptions.**]**

### setMqttAsyncClient

```java
 void setMqttAsyncClient(MqttAsyncClient mqttAsyncClient);
```

**SRS_MQTTCONNECTION_25_011: [**Setter for the MqttAsyncClient which can be null.**]**


### sendMessageAcknowledgement
```java
boolean sendMessageAcknowledgement(int messageId) throws TransportException
```

**SRS_MQTTCONNECTION_25_012: [**This function shall invoke the saved mqttAsyncClient to send the message ack for the provided messageId and then return true.**]**

**SRS_MQTTCONNECTION_25_013: [**If this function encounters an MqttException when sending the message ack over the mqtt async client, this function shall translate that exception and throw it.**]**
