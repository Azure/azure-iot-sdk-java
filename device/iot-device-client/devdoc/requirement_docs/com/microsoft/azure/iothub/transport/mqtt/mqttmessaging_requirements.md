# MqttMessaging Requirements

## Overview

MqttMessaging is a concrete class extending Mqtt.

## References

## Exposed API

```java
public final class MqttMessaging extends Mqtt
{
    public MqttMessaging(MqttConnection mqttConnection, String deviceId, MqttConnectionStateListener listener) throws IOException;

    public void start() throws TransportException;
    public void stop() throws TransportException;
    public void send(Message message) throws TransportException;
}
```

### MqttMessaging

```java
public MqttMessaging(MqttConnection mqttConnection, String deviceId, MqttConnectionStateListener listener) throws TransportException
```

**SRS_MqttMessaging_25_001: [**The constructor shall throw IllegalArgumentException if any of the parameters are null or empty .**]**

**SRS_MqttMessaging_25_002: [**The constructor shall use the configuration to instantiate super class and passing the parameters.**]**

**SRS_MqttMessaging_25_003: [**The constructor construct publishTopic and subscribeTopic from deviceId.**]**

**SRS_MqttMessaging_25_004: [**The constructor shall save the provided listener.**]**


### start

```java
public void start() throws TransportException;
```

**SRS_MqttMessaging_25_020: [**start method shall be call connect to establish a connection to IOT Hub with the given configuration.**]**

**SRS_MqttMessaging_25_021: [**start method shall subscribe to messaging subscribe topic once connected.**]**


### stop

```java
public void stop() throws TransportException;
```

**SRS_MqttMessaging_25_022: [**stop method shall be call disconnect to tear down a connection to IOT Hub with the given configuration.**]**

**SRS_MqttMessaging_25_023: [**stop method shall be call restartBaseMqtt to tear down a the base class even if disconnect fails.**]**

### send

```java
 public void send(Message message) throws TransportException;
```

**SRS_MqttMessaging_25_024: [**send method shall publish a message to the IOT Hub on the publish topic by calling method publish().**]**

**SRS_MqttMessaging_25_025: [**send method shall throw an IllegalArgumentException if the message is null.**]**

**SRS_MqttMessaging_34_026: [**This method shall append each custom property's name and value to the publishTopic before publishing.**]**

**SRS_MqttMessaging_21_027: [**send method shall append the messageid to publishTopic before publishing using the key name `$.mid`.**]**

**SRS_MqttMessaging_34_028: [**If the message has a correlationId, this method shall append that correlationid to publishTopic before publishing using the key name `$.cid`.**]**

**SRS_MqttMessaging_34_029: [**If the message has a To, this method shall append that To to publishTopic before publishing using the key name `$.to`.**]**

**SRS_MqttMessaging_34_030: [**If the message has a UserId, this method shall append that userId to publishTopic before publishing using the key name `$.uid`.**]**
