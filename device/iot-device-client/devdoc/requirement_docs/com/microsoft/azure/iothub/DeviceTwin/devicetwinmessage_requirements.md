# DeviceTwinMessage Requirements

## Overview

A Device Twin Message to or from an IoT Hub.

## References

## Exposed API

```java
public final class DeviceTwinMessage extends Message
{    
    public DeviceTwinMessage(byte[] body);

    public void setVersion(String version);
    public String getVersion();
    
    public void setRequestId(String id);
    public String getRequestId();
    
    public void setStatus(String status);
    public String getStatus();
    
    public void setDeviceTwinOperationType(DeviceTwinOperations type);
    public DeviceTwinOperations getDeviceTwinOperationType();
    
}
```


### DeviceTwinMessage

```java
public DeviceTwinMessage(byte[] body);
```

**SRS_DEVICETWINMESSAGE_25_001: [**The constructor shall save the message body by calling super with the body as parameter.**]**

**SRS_DEVICETWINMESSAGE_25_002: [**If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.**]**


### setVersion

```java
public void setVersion(String version);
```

**SRS_DEVICETWINMESSAGE_25_003: [**The function shall set the version.**]**


### getVersion

```java
public String getVersion();
```

**SRS_DEVICETWINMESSAGE_25_004: [**The function shall return the value of the version either set by the setter or the default (null) if unset so far.**]**


### setRequestId

```java
public void setRequestId(String id);
```

**SRS_DEVICETWINMESSAGE_25_005: [**The function shall save the request id.**]**


### getRequestId

```java
public String getRequestId();
```

**SRS_DEVICETWINMESSAGE_25_006: [**The function shall return the value of the request id either set by the setter or the default (null) if unset so far.**]**


### setStatus

```java
public void setStatus(String id);
```

**SRS_DEVICETWINMESSAGE_25_007: [**The function shall save the status.**]**


### getStatus

```java
public String getStatus();
```

**SRS_DEVICETWINMESSAGE_25_008: [**The function shall return the value of the status either set by the setter or the default (null) if unset so far.**]**

### setDeviceTwinOperationType

```java
public void setDeviceTwinOperationType(DeviceTwinOperations type);
```

**SRS_DEVICETWINMESSAGE_25_009: [**The function shall save the device twin operation type.**]**


### getDeviceTwinOperationType

```java
public DeviceTwinOperations getDeviceTwinOperationType();
```

**SRS_DEVICETWINMESSAGE_25_010: [**The function shall return the operation type either set by the setter or the default (DEVICE_TWIN_OPERATION_UNKNOWN) if unset so far.**]**
