# DeviceMethodMessage Requirements

## Overview

A Device Method Message to or from an IoT Hub.

## References

## Exposed API

```java
public class DeviceMethodMessage extends DeviceTwinMessage
{    
    public DeviceMethodMessage(byte[] data);

    public void setMethodName(String methodName);
    public String getMethodName();
    
}
```

### DeviceMethodMessage

```java
public DeviceMethodMessage(byte[] body);
```

**SRS_DEVICEMETHODMESSAGE_25_001: [**The constructor shall save the message body by calling super with the body as parameter.**]**

**SRS_DEVICEMETHODMESSAGE_25_002: [**If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.**]**


### setMethodName

```java
public void setMethodName(String methodName);
```

**SRS_DEVICEMETHODMESSAGE_25_003: [**This method shall set the methodName.**]**

**SRS_DEVICEMETHODMESSAGE_25_004: [**This method shall throw IllegalArgumentException if the methodName is null.**]**


### getMethodName

```java
public String getMethodName();
```

**SRS_DEVICEMETHODMESSAGE_25_005: [**The method shall return the methodName either set by the setter or the default (null) if unset so far.**]**
