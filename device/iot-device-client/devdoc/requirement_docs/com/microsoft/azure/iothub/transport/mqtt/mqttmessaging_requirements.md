# MqttMessaging Requirements

## Overview

MqttMessaging is a concrete class extending Mqtt. This class implements all the abstract methods from MQTT and overrides the parseTopic, 
parsePayload, onReconnect, and onReconnectComplete events.

## References

## Exposed API

```java
public final class MqttMessaging extends Mqtt
{
    public MqttMessaging(String serverURI, String clientId, String userName, String password, IotHubSSLContext iotHubSSLContext) throws IOException;
        
    public Message receive() throws IOException;
    String parseTopic() throws IOException;
    byte[] parsePayload(String topic) throws IOException;
    void onReconnect() throws IOException;
    void onReconnectComplete(boolean status) throws IOException;

    public void start() throws IOException;
    public void stop() throws IOException;
    public void send(Message message) throws IOException;

}
```

### MqttMessaging

```java
public MqttMessaging(String serverURI, String clientId, String userName, String password, IotHubSSLContext iotHubSSLContext);
```

**SRS_MqttMessaging_25_001: [**The constructor shall throw InvalidParameter Exception if any of the parameters are null or empty .**]**

**SRS_MqttMessaging_25_002: [**The constructor shall use the configuration to instantiate super class and passing the parameters.**]**

**SRS_MqttMessaging_25_003: [**The constructor construct publishTopic and subscribeTopic from deviceId.**]**


### parseTopic

```java
String parseTopic() throws IOException;
```

**SRS_MqttMessaging_25_004: [**parseTopic concrete method shall be implemeted by MqttMessaging concrete class.**]**

**SRS_MqttMessaging_25_005: [**parseTopic shall look for the subscribe topic prefix from received message queue.**]**

**SRS_MqttMessaging_25_006: [**If none of the topics from the received queue match the subscribe topic prefix then this method shall return null string .**]**

**SRS_MqttMessaging_25_007: [**If received messages queue is empty then parseTopic shall return null string.**]**

**SRS_MqttMessaging_25_008: [**If receiveMessage queue is null then parseTopic shall throw IOException.**]**


### parsePayload

```java
byte[] parsePayload(String topic) throws IOException;
```

**SRS_MqttMessaging_25_009: [**parsePayload concrete method shall be implemeted by MqttMessaging concrete class.**]**

**SRS_MqttMessaging_25_010: [**This parsePayload method look for payload for the corresponding topic from the received messagesqueue.**]**

**SRS_MqttMessaging_25_011: [**If the topic is null then parsePayload shall stop parsing for payload and return.**]**

**SRS_MqttMessaging_25_012: [**If the topic is non-null and received messagesqueue could not locate the payload then this method shall throw IOException**]**

**SRS_MqttMessaging_25_013: [**If receiveMessage queue is null then this method shall throw IOException.**]**

**SRS_MqttMessaging_25_014: [**If the topic is found in the message queue then parsePayload shall delete it from the queue.**]**


### onReconnect

```java
abstract void onReconnect() throws IOException;
```

**SRS_MqttMessaging_25_015: [**onReconnect method shall be implemeted by MqttMessaging class.**]**

**SRS_MqttMessaging_25_016: [**This onReconnect method shall put the entire operation of the MqttMessaging class on hold by waiting on the lock.**]**


### onReconnectComplete

```java
abstract void onReconnectComplete(boolean status) throws IOException;
```

**SRS_MqttMessaging_25_017: [**This onReconnectComplete method shall be implemeted by MqttMessaging class.**]**

**SRS_MqttMessaging_25_018: [**If the status is true, onReconnectComplete method shall release all the operation of the MqttMessaging class put on hold by notifying the users of the lock.**]**

**SRS_MqttMessaging_25_019: [**If the status is false, onReconnectComplete method shall throw IOException**]**


### start

```java
public void start() throws IOException;
```

**SRS_MqttMessaging_25_020: [**start method shall be call connect to establish a connection to IOT Hub with the given configuration.**]**

**SRS_MqttMessaging_25_021: [**start method shall subscribe to messaging subscribe topic once connected.**]**


### stop

```java
public void stop() throws IOException;
```

**SRS_MqttMessaging_25_022: [**stop method shall be call disconnect to tear down a connection to IOT Hub with the given configuration.**]**

**SRS_MqttMessaging_25_023: [**stop method shall be call restartBaseMqtt to tear down a the base class even if disconnect fails.**]**

### send

```java
 public void send(Message message) throws IOException;
```

**SRS_MqttMessaging_25_024: [**send method shall publish a message to the IOT Hub on the publish topic by calling method publish().**]**

**SRS_MqttMessaging_25_026: [**send method shall append the message properties to publishTopic before publishing.**]**

**SRS_MqttMessaging_25_025: [**send method shall throw an exception if the message is null.**]**

**SRS_MqttMessaging_21_027: [**send method shall append the messageid to publishTopic before publishing using the key name `$.mid`.**]**


    
    
   