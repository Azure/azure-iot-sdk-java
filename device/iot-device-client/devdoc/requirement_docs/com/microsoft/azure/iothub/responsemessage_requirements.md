# ResponseMessage Requirements

## Overview

Extend Message to support status response.

## References

## Exposed API

```java
public final class ResponseMessage extends Message
{    
    public ResponseMessage(byte[] body, IotHubStatusCode status);
    public IotHubStatusCode getStatus();
}
```

### ResponseMessage
```java
public ResponseMessage(byte[] body, IotHubStatusCode status);
```
**SRS_RESPONSEMESSAGE_21_001: [**The constructor shall save the message body by calling super with the body as parameter.**]**  
**SRS_RESPONSEMESSAGE_21_002: [**If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.**]**  
**SRS_RESPONSEMESSAGE_21_003: [**The constructor shall save the status.**]**  
**SRS_RESPONSEMESSAGE_21_004: [**If the message status is null, the constructor shall throw an IllegalArgumentException.**]**  

### getStatus
```java
public IotHubStatusCode getStatus();
```
**SRS_RESPONSEMESSAGE_21_005: [**The getStatus shall return the stored status.**]**  
