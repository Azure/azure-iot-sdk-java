# Mqtt Requirements

## Overview

An MQTT is an abtract class defining all the operations that can be performed over MQTT between a device and an IoT Hub. This class implements the Eclipse Paho MqttCallback interface and overrides the connectionLost and messageArrived events.

## References

## Exposed API

```java
public final class Mqtt implements MqttCallback
{
    public Mqtt(MqttConnection mqttConnection) throws TransportException;

    Pair<String, byte[]> peekMessage() throws TransportException;

    protected void connect() throws TransportException;
    protected void disconnect() throws TransportException;
    protected int publish(String publishTopic, byte[] payload) throws TransportException;
    protected void subscribe(String topic) throws TransportException;
    public Message receive() throws TransportException;
    public void restartBaseMqtt();

    public void connectionLost(Throwable throwable);
    public void messageArrived(String topic, MqttMessage mqttMessage);

    public void deliveryComplete(IMqttDeliveryToken imqttDeliveryToken);
    protected boolean sendMessageAcknowledgement(int messageId) throws TransportException
}
```


### Mqtt

```java
public Mqtt(MqttConnection mqttConnection) throws TransportException
```

**SRS_Mqtt_25_002: [**The constructor shall throw an IllegalArgumentException if mqttConnection is null.**]**

**SRS_Mqtt_25_003: [**The constructor shall retrieve lock, queue from the provided connection information and save the connection.**]**

### connect

```java
protected void connect() throws TransportException;
```

**SRS_Mqtt_25_005: [**The function shall establish an MQTT connection with an IoT Hub using the provided host name, user name, device ID, and sas token.**]**

**SRS_Mqtt_25_008: [**If the MQTT connection is already open, the function shall do nothing.**]**

**SRS_Mqtt_34_020: [**If the MQTT connection is established successfully, this function shall notify its listener that connection was established.**]**

**SRS_Mqtt_34_044: [**If an MqttException is encountered while connecting, this function shall throw the associated ProtocolException.**]**


### disconnect

```java
protected void disconnect() throws TransportException;
```

**SRS_Mqtt_25_010: [**If the MQTT connection is closed, the function shall do nothing.**]**

**SRS_Mqtt_25_011: [**If an MQTT connection is unable to be closed for any reason, the function shall throw a TransportException.**]**



### publish

```java
protected void publish(String publishTopic, byte[] payload) throws TransportException;
```
**SRS_Mqtt_99_049: [**If the user supplied SAS token has expired, the function shall throw a TransportException.**]**

**SRS_Mqtt_25_012: [**If the MQTT connection is closed, the function shall throw a TransportException.**]**

**SRS_Mqtt_25_013: [**If the either publishTopic or payload is null or empty, the function shall throw an IllegalArgumentException.**]**

**SRS_Mqtt_25_047: [**If the MqttClientAsync client throws MqttException on call to publish or getPendingDeliveryTokens, the function shall throw a ProtocolConnectionException with the message.**]**

**SRS_Mqtt_25_048: [**publish shall check for pending publish tokens by calling getPendingDeliveryTokens. And if there are pending tokens publish shall sleep until the number of pending tokens are less than 10 as per paho limitations**]**

**SRS_Mqtt_25_014: [**The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.**]**

**SRS_Mqtt_34_026: [**If this function publishes the message on the mqtt async client, this function shall return the message id of the returned mqtt delivery token.**]**


### subscribe

```java
protected void subscribe(String topic) throws TransportException;
```

**SRS_Mqtt_25_015: [**If the MQTT connection is closed, the function shall throw a TranpsortException with message.**]**

**SRS_Mqtt_25_016: [**If the subscribeTopic is null or empty, the function shall throw an IllegalArgumentException.**]**

**SRS_Mqtt_99_049: [**If the user supplied SAS token has expired, the function shall throw a TransportException.**]**

**SRS_Mqtt_25_048: [**If the Mqtt Client Async throws MqttException for any reason, the function shall throw a ProtocolException with the message.**]**

**SRS_Mqtt_25_017: [**The function shall subscribe to subscribeTopic specified to the IoT Hub given in the configuration.**]**


### receive

```java
public Message receive() throws TransportException;
```

**SRS_Mqtt_34_021: [**If the call peekMessage returns null then this method shall do nothing and return null**]**

**SRS_Mqtt_34_022: [**If the call peekMessage returns a null or empty string then this method shall do nothing and return null**]**

**SRS_Mqtt_34_023: [**This method shall call peekMessage to get the message payload from the received Messages queue corresponding to the messaging client's operation.**]**

**SRS_Mqtt_34_024: [**This method shall construct new Message with the bytes obtained from peekMessage and return the message.**]**

**SRS_Mqtt_34_025: [**If the call to peekMessage returns null when topic is non-null then this method will throw a TransportException**]**


### connectionLost

```java
public void connectionLost(Throwable throwable);
```

**SRS_Mqtt_34_045: [**If this object has a saved listener, this function shall notify the listener that connection was lost.**]**

**SRS_Mqtt_34_055: [**If the provided throwable is an instance of MqttException, this function shall derive the associated ConnectionStatusException and notify the listener of that derived exception.**]**


### messageArrived

```java
public void messageArrived(String topic, MqttMessage mqttMessage);
```

**SRS_Mqtt_25_030: [**The payload of the message and the topic is added to the received messages queue .**]**

**SRS_Mqtt_34_045: [**If there is a saved listener, this function shall notify that listener that a message arrived.**]**


### constructMessage

```java
private Message constructMessage(byte[] data, String topic) throws TransportException
```

**SRS_Mqtt_25_024: [**This method shall construct new Message with the bytes obtained from parsePayload and return the message.**]**

**SRS_Mqtt_34_041: [**This method shall call assignPropertiesToMessage so that all properties from the topic string can be assigned to the message**]**


### assignPropertiesToMessage

```java
private void assignPropertiesToMessage(Message message, String propertiesString) throws TransportException
```

**SRS_Mqtt_34_051: [**If a topic string's property's key and value are not separated by the '=' symbol, a TransportException shall be thrown**]**

**SRS_Mqtt_34_053: [**A property's key and value may include unusual characters such as &, %, $**]**

**SRS_Mqtt_34_054: [**A message may have 0 to many custom properties**]**


### deliveryComplete
```java
public void deliveryComplete(IMqttDeliveryToken imqttDeliveryToken);
```

**SRS_Mqtt_34_042: [**If this object has a saved listener, that listener shall be notified of the successfully delivered message.**]**


### sendMessageAcknowledgement
```java
protected boolean sendMessageAcknowledgement(int messageId) throws TransportException
```

**SRS_Mqtt_34_043: [**This function shall invoke the saved mqttConnection object to send the message acknowledgement for the provided messageId and return that result.**]**
