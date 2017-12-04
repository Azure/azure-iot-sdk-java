# Mqtt Requirements

## Overview

An MQTT is an abtract class defining all the operations that can be performed over MQTT between a device and an IoT Hub. This class implements the Eclipse Paho MqttCallback interface and overrides the connectionLost and messageArrived events.

## References

## Exposed API

```java
public final class Mqtt implements MqttCallback
{
    public Mqtt(MqttConnection mqttConnection) throws IllegalArgumentException;

    Pair<String, byte[]> peekMessage() throws IOException;

    private class MqttConnectionInfo
    {
        MqttConnectionInfo(String serverURI, String clientId, String userName, String password, IotHubSSLContext iotHubSSLContext) throws IOException;
        private void updateConnectionOptions(String userName, String userPassword, IotHubSSLContext iotHubSSLContext);
    }

    protected void connect() throws IOException;
    protected void disconnect() throws IOException;
    protected void publish(String publishTopic, byte[] payload) throws IOException;
    protected void subscribe(String topic) throws IOException;
    public Message receive() throws IOException;
    public void restartBaseMqtt();

    public void connectionLost(Throwable throwable);
    public void messageArrived(String topic, MqttMessage mqttMessage);
}
```


### Mqtt

```java
public Mqtt(MqttConnection mqttConnection) throws IllegalArgumentException
```

**SRS_Mqtt_25_002: [**The constructor shall throw InvalidParameter Exception if mqttConnection is null .**]**

**SRS_Mqtt_25_003: [**The constructor shall retrieve lock, queue from the provided connection information and save the connection.**]**

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
**SRS_Mqtt_99_049: [**If the user supplied SAS token has expired, the function shall throw an IOException.**]**

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

**SRS_Mqtt_99_049: [**If the user supplied SAS token has expired, the function shall throw an IOException.**]**

**SRS_Mqtt_25_048: [**If the Mqtt Client Async throws MqttException for any reason, the function shall throw an IOException with the message.**]**

**SRS_Mqtt_25_017: [**The function shall subscribe to subscribeTopic specified to the IoT Hub given in the configuration.**]**


### receive

```java
public Message receive() throws IOException;
```

**SRS_Mqtt_34_021: [**If the call peekMessage returns null then this method shall do nothing and return null**]**

**SRS_Mqtt_34_022: [**If the call peekMessage returns a null or empty string then this method shall do nothing and return null**]**

**SRS_Mqtt_34_023: [**This method shall call peekMessage to get the message payload from the received Messages queue corresponding to the messaging client's operation.**]**

**SRS_Mqtt_34_024: [**This method shall construct new Message with the bytes obtained from peekMessage and return the message.**]**

**SRS_Mqtt_34_025: [**If the call to peekMessage returns null when topic is non-null then this method will throw IOException**]**


### connectionLost

```java
public void connectionLost(Throwable throwable);
```
**SRS_Mqtt_25_026: [**The function shall notify all its concrete classes by calling abstract method onReconnect at the entry of the function**]**

**SRS_Mqtt_99_050: [**The function shall check if SAS token has already expired.**]**

**SRS_Mqtt_99_051: [**The function shall check if SAS token in based on user supplied SharedAccessKey.**]**

**SRS_Mqtt_99_052: [**The function shall generate a new SAS token.**]**

**SRS_Mqtt_99_053: [**The function shall set user supplied SAS token expiration flag to true.**]**

**SRS_Mqtt_25_027: [**The function shall attempt to reconnect to the IoTHub in a loop with exponential backoff until it succeeds**]**

**SRS_Mqtt_25_028: [**The maximum wait interval until a reconnect is attempted shall be 60 seconds.**]**

**SRS_Mqtt_25_029: [**The function shall notify all its concrete classes by calling abstract method onReconnectComplete at the exit of the function**]**


### messageArrived

```java
public void messageArrived(String topic, MqttMessage mqttMessage);
```

**SRS_Mqtt_25_030: [**The payload of the message and the topic is added to the received messages queue .**]**


### constructMessage

```java
private Message constructMessage(byte[] data, String topic) throws IllegalArgumentException
```

**SRS_Mqtt_25_024: [**This method shall construct new Message with the bytes obtained from parsePayload and return the message.**]**

**SRS_Mqtt_34_041: [**This method shall call assignPropertiesToMessage so that all properties from the topic string can be assigned to the message**]**


### assignPropertiesToMessage

```java
private void assignPropertiesToMessage(Message message, String propertiesString) throws IllegalArgumentException, NumberFormatException
```

**SRS_Mqtt_34_051: [**If a topic string's property's key and value are not separated by the '=' symbol, an IllegalArgumentException shall be thrown**]**

**SRS_Mqtt_34_053: [**A property's key and value may include unusual characters such as &, %, $**]**

**SRS_Mqtt_34_054: [**A message may have 0 to many custom properties**]**



### peekMessage

```java
Pair<String, byte[]> peekMessage() throws IOException;
```

**SRS_Mqtt_34_040: [**If allReceivedMessages queue is null then this method shall throw IOException.**]**
