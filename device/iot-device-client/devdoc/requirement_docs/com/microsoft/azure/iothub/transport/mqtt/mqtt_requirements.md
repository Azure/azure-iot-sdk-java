# Mqtt Requirements

## Overview

An MQTT is an abtract class defining all the operations that can be performed over MQTT between a device and an IoT Hub. This class implements the Eclipse Paho MqttCallback interface and overrides the connectionLost and messageArrived events.

## References

## Exposed API

```java
public final class Mqtt implements MqttCallback
{
    public Mqtt(String serverURI, String clientId, String userName, String password, IotHubSSLContext iotHubSSLContext) throws IOException;
    public Mqtt() throws IOException;

    abstract String parseTopic() throws IOException;
    abstract byte[] parsePayload(String topic) throws IOException;

    private class MqttConnectionInfo
    {
        MqttConnectionInfo(String serverURI, String clientId, String userName, String password, IotHubSSLContext iotHubSSLContext) throws IOException
        private void updateConnectionOptions(String userName, String userPassword, IotHubSSLContext iotHubSSLContext)
    }

    protected void connect() throws IOException
    protected void disconnect() throws IOException
    protected void publish(String publishTopic, byte[] payload) throws IOException
    protected void subscribe(String topic) throws IOException
    protected void unsubscribe(String topic) throws IOException
    public Message receive() throws IOException;
    public void restartBaseMqtt();

    public void connectionLost(Throwable throwable);
    public void messageArrived(String topic, MqttMessage mqttMessage);
}
```


### Mqtt

```java
public Mqtt();
```
**SRS_Mqtt_25_001: [**The constructor shall instantiate MQTT lock for using base class.**]**

### Mqtt

```java
public Mqtt(String serverURI, String clientId, String userName, String password, IotHubSSLContext iotHubSSLContext);
```

**SRS_Mqtt_25_002: [**The constructor shall throw InvalidParameter Exception if any of the parameters are null or empty .**]**

**SRS_Mqtt_25_003: [**The constructor shall use the configuration to instantiate an instance of the inner class MqttConnectionInfo if not already created.**]**

**SRS_Mqtt_25_004: [**If an instance of the inner class MqttConnectionInfo is already created than it shall return doing nothing.**]**

**SRS_Mqtt_25_045: [**The constructor throws IOException if MqttException is thrown and doesn't instantiate this instance.**]**


### restartBaseMqtt

```java
public restartBaseMqtt();
```
**SRS_Mqtt_25_046: [**restartBaseMqtt shall unset all the static variables.**]**


### connect

```java
protected void connect() throws IOException;
```

**SRS_Mqtt_25_005: [**The function shall establish an MQTT connection with an IoT Hub using the provided host name, user name, device ID, and sas token.**]**

**SRS_Mqtt_25_006: [**If the inner class MqttConnectionInfo has not been instantiated then the function shall throw IOException.**]**

**SRS_Mqtt_25_007: [**If an MQTT connection is unable to be established for any reason, the function shall throw an IOException.**]**

**SRS_Mqtt_25_008: [**If the MQTT connection is already open, the function shall do nothing.**]**


### disconnect

```java
protected void disconnect() throws IOException;
```

**SRS_Mqtt_25_009: [**The function shall close the MQTT connection.**]**

**SRS_Mqtt_25_010: [**If the MQTT connection is closed, the function shall do nothing.**]**

**SRS_Mqtt_25_011: [**If an MQTT connection is unable to be closed for any reason, the function shall throw an IOException.**]**


### publish

```java
protected void publish(String publishTopic, byte[] payload) throws IOException;
```
**SRS_Mqtt_25_012: [**If the MQTT connection is closed, the function shall throw an IOException.**]**

**SRS_Mqtt_25_013: [**If the either publishTopic or payload is null or empty, the function shall throw an IOException.**]**

**SRS_Mqtt_25_047: [**If the MqttClientAsync client throws MqttException on call to publish or getPendingDeliveryTokens, the function shall throw an IOException with the message.**]**

**SRS_Mqtt_25_048: [**publish shall check for pending publish tokens by calling getPendingDeliveryTokens. And if there are pending tokens publish shall sleep until the number of pending tokens are less than 10 as per paho limitations**]**

**SRS_Mqtt_25_014: [**The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.**]**


### subscribe

```java
protected void subscribe(String topic) throws IOException;
```

**SRS_Mqtt_25_015: [**If the MQTT connection is closed, the function shall throw an IOexception with message.**]**

**SRS_Mqtt_25_016: [**If the subscribeTopic is null or empty, the function shall throw an InvalidParameter Exception.**]**

**SRS_Mqtt_25_048: [**If the Mqtt Client Async throws MqttException for any reason, the function shall throw an IOException with the message.**]**

**SRS_Mqtt_25_017: [**The function shall subscribe to subscribeTopic specified to the IoT Hub given in the configuration.**]**


### unsubscribe

```java
protected void unsubscribe(String topic) throws IOException;
```

**SRS_Mqtt_25_018: [**If the MQTT connection is closed, the function shall throw an IOException with message.**]**

**SRS_Mqtt_25_019: [**If the unsubscribeTopic is null or empty, the function shall throw an IOException.**]**

**SRS_Mqtt_25_020: [**The function shall unsubscribe from subscribeTopic specified to the IoT Hub given in the configuration.**]**


### receive

```java
public Message receive() throws IOException;
```

**SRS_Mqtt_25_021: [**This method shall call parseTopic to parse the topic from the recevived Messages queue corresponding to the messaging client's operation.**]**

**SRS_Mqtt_25_022: [**If the call parseTopic returns null or empty string then this method shall do nothing and return null**]**

**SRS_Mqtt_25_023: [**This method shall call parsePayload to get the message payload from the recevived Messages queue corresponding to the messaging client's operation.**]**

**SRS_Mqtt_25_024: [**This method shall construct new Message with the bytes obtained from parsePayload and return the message.**]**

**SRS_Mqtt_25_025: [**If the call to parsePayload returns null when topic is non-null then this method will throw IOException**]**


### connectionLost

```java
public void connectionLost(Throwable throwable);
```
**SRS_Mqtt_25_026: [**The function shall notify all its concrete classes by calling abstract method onReconnect at the entry of the function**]**

**SRS_Mqtt_25_027: [**The function shall attempt to reconnect to the IoTHub in a loop with exponential backoff until it succeeds**]**

**SRS_Mqtt_25_028: [**The maximum wait interval until a reconnect is attempted shall be 60 seconds.**]**

**SRS_Mqtt_25_029: [**The function shall notify all its concrete classes by calling abstract method onReconnectComplete at the exit of the function**]**


### messageArrived

```java
public void messageArrived(String topic, MqttMessage mqttMessage);
```

**SRS_Mqtt_25_030: [**The payload of the message and the topic is added to the received messages queue .**]**


### parseTopic

```java
abstract String parseTopic() throws IOException;
```

**SRS_Mqtt_25_031: [**This abstract method shall be implemeted by the concrete classes.**]**

**SRS_Mqtt_25_032: [**This abstract method shall parse the topic from received message queue corresponding to the concrete classes operation.**]**

**SRS_Mqtt_25_033: [**If none of the topics from the received queue match the concrete classes operation then this method shall return null string .**]**

**SRS_Mqtt_25_034: [**If received messages queue is empty then this method shall return null string in its concrete implementation.**]**

**SRS_Mqtt_25_035: [**If receiveMessage queue is null then this method shall throw IOException.**]**


### parsePayload

```java
abstract byte[] parsePayload(String topic) throws IOException;
```

**SRS_Mqtt_25_036: [**This abstract method shall be implemeted by the concrete classes.**]**

**SRS_Mqtt_25_037: [**This abstract method look for payload for the corresponding topic from the received messagesqueue in the concrete classes implementation.**]**

**SRS_Mqtt_25_038: [**If the topic is null then this method stop parsing for payload and return.**]**

**SRS_Mqtt_25_039: [**If the topic is non-null and received messagesqueue could not locate the payload then this method shall throw IOException**]**

**SRS_Mqtt_25_040: [**If receiveMessage queue is null then this method shall throw IOException.**]**
