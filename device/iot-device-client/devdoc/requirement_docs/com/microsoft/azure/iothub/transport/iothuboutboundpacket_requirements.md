# IotHubOutboundPacket Requirements

## Overview

A packet containing the data needed for an IoT Hub transport to send a message.

## References

## Exposed API

```java
public final class IotHubOutboundPacket
{
    public IotHubOutboundPacket(Message message, IotHubEventCallback callback, Object callbackContext);
    public IotHubOutboundPacket(Message message, IotHubResponseCallback callback, Object callbackContext);

    public Message getMessage();
    public IotHubEventCallback getCallback();
    public IotHubResponseCallback getResponseCallback();
    public Object getCallbackContext();
}
```

### IotHubOutboundPacket

```java
public IotHubOutboundPacket(Message message, IotHubEventCallback callback, Object callbackContext);
```

**SRS_IOTHUBOUTBOUNDPACKET_11_001: [**The constructor shall save the message, callback, and callback context.**]**
**SRS_IOTHUBOUTBOUNDPACKET_21_007: [**The constructor shall set the response callback as null.**]**


```java
public IotHubOutboundPacket(Message message, IotHubResponseCallback callback, Object callbackContext);
```

**SRS_IOTHUBOUTBOUNDPACKET_21_005: [**The constructor shall save the message, callback, and callback context.**]**
**SRS_IOTHUBOUTBOUNDPACKET_21_008: [**The constructor shall set the event callback as null.**]**


### getMessage

```java
public Message getMessage();
```

**SRS_IOTHUBOUTBOUNDPACKET_11_002: [**The function shall return the message given in the constructor.**]**


### getCallback

```java
public IotHubEventCallback getCallback();
```

**SRS_IOTHUBOUTBOUNDPACKET_11_003: [**The function shall return the event callback given in the constructor.**]**


### getResponseCallback

```java
public IotHubResponseCallback getResponseCallback();
```

**SRS_IOTHUBOUTBOUNDPACKET_21_006: [**The function shall return the response callback given in the constructor.**]**


### getCallbackContext

```java
public Object getCallbackContext();
```

**SRS_IOTHUBOUTBOUNDPACKET_11_004: [**The function shall return the callback context given in the constructor.**]**
