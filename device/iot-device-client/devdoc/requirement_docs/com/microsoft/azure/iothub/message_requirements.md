# Message Requirements

## Overview

A message to or from an IoT Hub.

## References

## Exposed API

```java
public final class Message
{
    public static final Charset DEFAULT_IOTHUB_MESSAGE_CHARSET = StandardCharsets.UTF_8;
    
    public Message(byte[] body);

    public Message setProperty(String name, String value);
    public String getProperty(String name);
    public MessageProperty[] getProperties();
    public boolean isExpired();

    public byte[] getBytes();
}
```


### Message

```java
public Message(byte[] body);
```

**SRS_MESSAGE_11_024: [**The constructor shall save the message body.**]**

**SRS_MESSAGE_11_025: [**If the message body is null, the constructor shall throw an IllegalArgumentException.**]**


### setProperty

```java
public Message setProperty(String name, String value);
```

**SRS_MESSAGE_11_026: [**The function shall set the message property to the given value.**]**

**SRS_MESSAGE_11_028: [**If name is null, the function shall throw an IllegalArgumentException.**]**

**SRS_MESSAGE_11_029: [**If value is null, the function shall throw an IllegalArgumentException.**]**

**SRS_MESSAGE_11_030: [**If name contains a character not specified in RFC 2047, the function shall throw an IllegalArgumentException.**]**

**SRS_MESSAGE_11_031: [**If value name contains a character not specified in RFC 2047, the function shall throw an IllegalArgumentException.**]**


### getProperty

```java
public String getProperty(String name);
```

**SRS_MESSAGE_11_032: [**The function shall return the value associated with the message property name, where the name can be either the HTTPS or AMQPS property name.**]**

**SRS_MESSAGE_11_034: [**If no value associated with the property name is found, the function shall return null.**]**


### getProperties

```java
public MessageProperty[] getProperties();
```

**SRS_MESSAGE_11_033: [**The function shall return a copy of the message properties.**]**


### isExpired()

```java
public boolean isExpired();
```

**SRS_MESSAGE_15_035: [**The function shall return false if the expiry time is 0.**]**

**SRS_MESSAGE_15_036: [**The function shall return true if the current time is greater than the expiry time and false otherwise.**]**


### getBytes

```java
public byte[] getBytes();
```

**SRS_MESSAGE_11_002: [**The function shall return the message body.**]**

### setAbsoluteExpiryTime()
```java
public void setAbsoluteExpiryTime(long absoluteTimeout);
```

**SRS_MESSAGE_34_037: [**The function shall set the message's expiry time to be the number of milliseconds since the epoch provided in absoluteTimeout.**]**

**SRS_MESSAGE_34_038: [**If the provided absolute expiry time is negative, an IllegalArgumentException shall be thrown.**]**


### getUserId

```java
public String getUserId();
```

**SRS_MESSAGE_34_037: [**The function shall return the message's user ID.**]**


### getDeliveryAcknowledgement

```java
public String getDeliveryAcknowledgement();
```

**SRS_MESSAGE_34_039: [**The function shall return the message's DeliveryAcknowledgement.**]**


### getTo

```java
public String getTo();
```

**SRS_MESSAGE_34_041: [**The function shall return the message's To value.**]**


### getMessageId

```java
public String getMessageId();
```

**SRS_MESSAGE_34_043: [**The function shall return the message's message Id.**]**


### setMessageId

```java
public void setMessageId(String messageId);
```

**SRS_MESSAGE_34_044: [**The function shall set the message's message ID to the provided value.**]**


### getCorrelationId

```java
public String getCorrelationId();
```

**SRS_MESSAGE_34_045: [**The function shall return the message's correlation ID.**]**


### setCorrelationId

```java
public void setCorrelationId(String correlationId);
```

**SRS_MESSAGE_34_046: [**The function shall set the message's correlation ID to the provided value.**]**


### setExpiryTime

```java
public void setExpiryTime(String expiryTime);
```

**SRS_MESSAGE_34_047: [**The function shall set the message's expiry time.**]**


### getMessageType
```java
public MessageType getMessageType();
```

**SRS_MESSAGE_34_049: [**The function shall return the message's message type.**]**


**SRS_MESSAGE_12_001: [**The function shall return the message's iotHubConnectionString object.**]**
```java
public IotHubConnectionString getIotHubConnectionString();
```


**SRS_MESSAGE_12_002: [**The function shall set the message's iotHubConnectionString object to the provided value.**]**
```java
public void setIotHubConnectionString(IotHubConnectionString iotHubConnectionString);
```
