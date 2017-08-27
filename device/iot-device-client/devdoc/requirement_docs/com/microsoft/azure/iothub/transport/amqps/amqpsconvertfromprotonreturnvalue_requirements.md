# AmqpsConvertFromProtonReturnValue Requirements

## Overview

Class to provide placeholder for return value for convert from proton device operations. 

## References

## Exposed API

```java
class AmqpsConvertFromProtonReturnValue
{
    AmqpsConvertFromProtonReturnValue(Message message, MessageCallback messageCallback, Object messageContext);
    Message getMessage()
    MessageCallback getMessageCallback()
    Object getMessageContext()
```

### AmqpsConvertFromProtonReturnValue

```java
AmqpsConvertFromProtonReturnValue(Message message, MessageCallback messageCallback, Object messageContext);
```

**SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_001: [**The constructor shall initialize message, messageCallback and messageContext private member variables with the given arguments.**]**


### getMessage

```java
Message getMessage()
```

**SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_002: [**The function shall return the current value of message private member.**]**


### getMessageCallback

```java
MessageType getMessageCallback();
```

**SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_003: [**The function shall return the current value of messageCallback private member.**]**


### getMessageContext

```java
MessageType getMessageContext();
```

**SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_004: [**The function shall return the current value of messageContext private member.**]**
