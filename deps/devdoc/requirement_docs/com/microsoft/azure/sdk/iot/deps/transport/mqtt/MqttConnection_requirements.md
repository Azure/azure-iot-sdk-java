# MqttMessage Requirements

## Overview

## References

## Exposed API

```java
public class MqttMessage
{
    public MqttMessage(String topic);
    public MqttMessage(String topic, org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage);
    public MqttMessage(String topic, byte[] payload)
    public static int retrieveQosValue(MqttQos qos);
    public void setTopic(String topic);
    public String getTopic();
    public byte[] getPayload();
    public void setPayload(byte[] payload);
    public void setQos(MqttQos qos);
    public MqttQos getQos();
    public org.eclipse.paho.client.mqttv3.MqttMessage getMqttMessage();
}
```

### MqttMessage

```java
public MqttMessage(String topic)
```

### MqttMessage Paho

```java
public MqttMessage(String topic, org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage)
```

### MqttMessage payload

```java
public MqttMessage(String topic, byte[] payload)
```

### retrieveQosValue

```java
public static int retrieveQosValue(MqttQos qos);
```

### setTopic

```java
public void setTopic(String topic);
```

### getTopic

```java
public String getTopic();
```

### getPayload

```java
public byte[] getPayload();
```

### setPayload

```java
public void setPayload(byte[] payload);
```

### getQos

```java
public MqttQos getQos();
```

### getMqttMessage

```java
public org.eclipse.paho.client.mqttv3.MqttMessage getMqttMessage();
```
