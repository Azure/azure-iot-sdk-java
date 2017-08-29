# AmqpsConvertToProtonReturnValue Requirements

## Overview

Class to provide placeholder for return value for convert to proton device operations. 

## References

## Exposed API

```java
class AmqpsConvertToProtonReturnValue
{
    AmqpsConvertToProtonReturnValue(MessageImpl messageImpl, MessageType messageType);
    MessageImpl getMessageImpl();
    MessageType getMessageType();
}
```

### AmqpsConvertToProtonReturnValue

```java
AmqpsConvertToProtonReturnValue(MessageImpl messageImpl, MessageType messageType);
```

**SRS_AMQPSCONVERTTOPROTONRETURNVALUE_12_001: [**The constructor shall initialize messageImpl and messageType private member variables with the given arguments.**]**


### getMessageImpl

```java
MessageImpl getMessageImpl();
```

**SRS_AMQPSCONVERTTOPROTONRETURNVALUE_12_002: [**The function shall return the current value of messageImpl private member.**]**


### getMessageType

```java
MessageType getMessageType();
```

**SRS_AMQPSCONVERTTOPROTONRETURNVALUE_12_003: [**The function shall return the current value of messageType private member.**]**
