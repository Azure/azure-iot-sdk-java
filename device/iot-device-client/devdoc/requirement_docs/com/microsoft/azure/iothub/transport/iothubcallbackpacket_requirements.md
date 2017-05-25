# IotHubCallbackPacket Requirements

## Overview

A packet containing the data needed for an IoT Hub transport to invoke a callback on a completed request.

## References

## Exposed API
```java
public final class IotHubCallbackPacket
{
    public IotHubCallbackPacket(IotHubStatusCode status, 
                                IotHubEventCallback callback, 
                                Object callbackContext);
    public IotHubCallbackPacket(ResponseMessage responseMessage, 
                                IotHubResponseCallback callback,
                                Object callbackContext);

    public IotHubStatusCode getStatus();
    public Message getMessage();
    public IotHubEventCallback getCallback();
    public IotHubResponseCallback getResponseCallback();
    public Object getContext();
}
```

### IotHubCallbackPacket

```java
public IotHubCallbackPacket(IotHubStatusCode status, 
                            IotHubEventCallback callback, 
                            Object callbackContext);
```

**SRS_IOTHUBCALLBACKPACKET_11_001: [**The constructor shall save the status, callback, and callback context.**]**  

**SRS_IOTHUBCALLBACKPACKET_21_007: [**The constructor shall set message and responseCallback as null.**]**  


### IotHubCallbackPacket

```java
public IotHubCallbackPacket(ResponseMessage responseMessage, 
                            IotHubResponseCallback callback,
                                Object callbackContext);
```

**SRS_IOTHUBCALLBACKPACKET_21_006: [**The constructor shall save the responseMessage, responseCallback, and callback context.**]**

**SRS_IOTHUBCALLBACKPACKET_21_009: [**The constructor shall set status and eventCallback as null.**]**  


### getStatus

```java
public IotHubStatusCode getStatus();
```

**SRS_IOTHUBCALLBACKPACKET_11_002: [**The function shall return the status given in the constructor.**]**


### getMessage

```java
public Message getMessage();
```

**SRS_IOTHUBCALLBACKPACKET_21_008: [**The function shall return the response message given in the constructor.**]**


### getCallback

```java
public IotHubEventCallback getCallback();
```

**SRS_IOTHUBCALLBACKPACKET_11_003: [**The function shall return the eventCallback given in the constructor.**]**


### getResponseCallback

```java
public IotHubResponseCallback getResponseCallback()
```

**SRS_IOTHUBCALLBACKPACKET_21_005: [**The getResponseCallback shall return the responseCallback given in the constructor.**]**


### getContext

```java
public Object getContext();
```

**SRS_IOTHUBCALLBACKPACKET_11_004: [**The function shall return the callback context given in the constructor.**]**
