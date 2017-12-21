# MqttConnection Requirements

## Overview

## References

## Exposed API

```java
public class MqttConnection
{
    public MqttConnection(String hostname, String clientId, String userName, String password, SSLContext sslContext, boolean useWebSockets) throws IOException;
    public void setListener(MqttListener listener);
    public boolean isMqttConnected();
    public void updateConnectionOptions(String userName, String userPassword, SSLContext sslContext);
    public void connect() throws IOException;
    public void disconnect() throws IOException;
    public void publishMessage(String topic, MqttQos qos, byte[] message) throws IOException;
    public void publishMessage(MqttMessage message) throws IOException;
    public void subscribe(String topic, MqttQos qos) throws IOException;
    public void unsubscribe(String topic) throws IOException;
    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage);
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken);
    public void connectionLost(Throwable throwable);
}
```

### MqttConnection

```java
public MqttConnection(String hostname, String clientId, String userName, String password, SSLContext sslContext, boolean useWebSockets) throws IOException;
```

**SRS_MQTTCONNECTION_07_001: [** If the hostname, clientId, username or sslContext is NULL, `MqttConnection` shall throw an IllegalArgumentException. **]**

**SRS_MQTTCONNECTION_07_002: [** If the hostname, clientId, or username is empty, `MqttConnection` shall throw an IllegalArgumentException. **]**

**SRS_MQTTCONNECTION_07_003: [** If `useWebSockets` is true `MqttConnection` shall construct a `WS_SSL_URL` otherwise it shall construct a `SSL_URL`. **]**

**SRS_MQTTCONNECTION_07_004: [** The constructor shall create an MqttAsync client and update the connection options using the provided serverUri, clientId, userName, password and sslContext. **]**

### setListener

```java
public void setListener(MqttListener listener);
```

**SRS_MQTTCONNECTION_07_005: [** If `listener` is NULL `setListener` shall throw IllegalArgumentException. **]**

**SRS_MQTTCONNECTION_07_006: [** `setListener` shall store the listener. **]**

### isMqttConnected

```java
public boolean isMqttConnected();
```

**SRS_MQTTCONNECTION_07_007: [** If the `mqttAsyncClient` is NULL `isMqttConnected` shall return false. **]**

**SRS_MQTTCONNECTION_07_008: [** `isMqttConnected` shall return mqttAsyncClient.isConnected(). **]**

### connect

```java
public void connect() throws IOException;
```

**SRS_MQTTCONNECTION_07_009: [** If mqttAsyncClient is not connected `connect` shall call mqttAsyncClient.connected(). **]**

**SRS_MQTTCONNECTION_07_010: [** If mqttAsyncClient is connected `connect` shall do nothing. **]**

### disconnect

```java
public void disconnect() throws IOException;
```

**SRS_MQTTCONNECTION_07_011: [** If mqttAsyncClient is connected `disconnect` shall call mqttAsyncClient.disconnect(). **]**

**SRS_MQTTCONNECTION_07_012: [** If mqttAsyncClient is not connected `disconnect` shall do nothing. **]**

### publishMessage Topic

```java
public void publishMessage(String topic, MqttQos qos, byte[] message) throws IOException
```

**SRS_MQTTCONNECTION_07_013: [** `publishMessage` shall construct a MqttMessage and call PublishMessage with the message. **]**

### publishMessage

```java
public void publishMessage(MqttMessage message) throws IOException
```

**SRS_MQTTCONNECTION_07_014: [** If mqttAsyncClient is not connected or null or message is null, `publish **]**Message` shall throw IOException.

**SRS_MQTTCONNECTION_07_015: [** `publishMessage` shall call `mqttAsyncClient` publish method. **]**

### subscribe

```java
public void subscribe(String topic, MqttQos qos) throws IOException
```

**SRS_MQTTCONNECTION_07_016: [** If mqttAsyncClient is not connected or null or message is null, `subscribe` shall throw IOException. **]**

**SRS_MQTTCONNECTION_07_017: [** `subscribe` shall call `mqttAsyncClient` subscribe method. **]**

### unsubscribe

```java
public void unsubscribe(String topic) throws IOException
```

**SRS_MQTTCONNECTION_07_018: [** If mqttAsyncClient is not connected or null or message is null, `unsu bscribe` shall throw IOException.**]**

**SRS_MQTTCONNECTION_07_019: [** `unsubscribe` shall call `mqttAsyncClient` unsubscribe method. **]**

### messageArrived

```java
public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage)
```

**SRS_MQTTCONNECTION_07_020: [** If `mqttListener` is not null, mqttListener shall call `mqttListener` messageReceived. **]**

### connectionLost

```java
public void connectionLost(Throwable throwable)
```

**SRS_MQTTCONNECTION_07_021: [** If `mqttListener` is not null, mqttListener shall call `mqttListener` connectionLost. **]**
