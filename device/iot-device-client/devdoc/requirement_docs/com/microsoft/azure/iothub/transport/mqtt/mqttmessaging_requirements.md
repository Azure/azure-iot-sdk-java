# MqttMessaging Requirements

## Overview

MqttMessaging is a concrete class extending Mqtt. This class implements all the abstract methods from MQTT and
overrides the onReconnect and onReconnectComplete events.

## References

## Exposed API

```java
public final class MqttMessaging extends Mqtt
{
    public MqttMessaging(String serverURI, String clientId, String userName, String password, IotHubSSLContext iotHubSSLContext) throws IOException;
        
    public Message receive() throws IOException;
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


    
    
   