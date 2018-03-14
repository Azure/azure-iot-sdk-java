# IotHubTransportMessage Requirements

## Overview

Extends Message, adding transport artifacts.

## References

## Exposed API

```java
public class IotHubTransportMessage extends Message
{
    public IotHubTransportMessage(String body);
    public IotHubTransportMessage(Message message);

    public void setIotHubMethod(IotHubMethod iotHubMethod);
    public void setUriPath(String uriPath);

    public IotHubMethod getIotHubMethod();
    public String getUriPath();
}
```

### IotHubTransportMessage

```java
public IotHubTransportMessage(byte[] data, MessageType messageType);
```

**SRS_IOTHUBTRANSPORTMESSAGE_12_001: [**If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.**]**

**SRS_IOTHUBTRANSPORTMESSAGE_12_002: [**The constructor shall save the message body by calling super with the body as parameter.**]**

**SRS_IOTHUBTRANSPORTMESSAGE_12_003: [**The constructor shall set the messageType to the given value by calling the super with the given value.**]**

**SRS_IOTHUBTRANSPORTMESSAGE_12_015: [**The constructor shall initialize version, requestId and status to null.**]**

**SRS_IOTHUBTRANSPORTMESSAGE_12_016: [**The constructor shall initialize operationType to UNKNOWN**]**


### IotHubTransportMessage

```java
public IotHubTransportMessage(String body);
```

**SRS_IOTHUBTRANSPORTMESSAGE_21_002: [**This method shall throw IllegalArgumentException if the body argument is null.**]**


### IotHubTransportMessage

```java
public IotHubTransportMessage(Message message);
```

**SRS_IOTHUBTRANSPORTMESSAGE_34_017: [**This constructor shall return an instance of IotHubTransportMessage with provided bytes, messagetype, correlationid, messageid, and application properties.**]**


### setVersion

```java
public void setVersion(String version);
```

**SRS_IOTHUBTRANSPORTMESSAGE_12_004: [**The function shall set the version.**]**


### getVersion

```java
public String getVersion();
```

**SRS_IOTHUBTRANSPORTMESSAGE_12_005: [**The function shall return the value of the version either set by the setter or the default (null) if unset so far.**]**


### setRequestId

```java
public void setRequestId(String id);
```

**SRS_IOTHUBTRANSPORTMESSAGE_12_006: [**The function shall save the request id.**]**


### getRequestId

```java
public String getRequestId();
```

**SRS_IOTHUBTRANSPORTMESSAGE_12_007: [**The function shall return the value of the request id either set by the setter or the default (null) if unset so far.**]**


### setStatus

```java
public void setStatus(String status);
```

**SRS_IOTHUBTRANSPORTMESSAGE_12_008: [**The function shall save the status.**]**


### getStatus

```java
public String getStatus();
```

**SRS_IOTHUBTRANSPORTMESSAGE_12_009: [**The function shall return the value of the status either set by the setter or the default (null) if unset so far.**]**


### setDeviceOperationType

```java
public void setDeviceOperationType(DeviceOperations deviceOperationType);
```

**SRS_IOTHUBTRANSPORTMESSAGE_12_010: [**The function shall save the device twin operation type.**]**


### getDeviceOperationType

```java
public DeviceOperations getDeviceOperationType();
```

**SRS_IOTHUBTRANSPORTMESSAGE_12_011: [**The function shall return the operation type either set by the setter or the default if unset so far.**]**


### setMethodName

```java
public void setMethodName(String methodName);
```

**SRS_IOTHUBTRANSPORTMESSAGE_12_012: [**The function shall throw IllegalArgumentException if the methodName is null.**]**

**SRS_IOTHUBTRANSPORTMESSAGE_12_013: [**The function shall set the methodName.**]**


### getMethodName

```java
public String getMethodName();
```

**SRS_IOTHUBTRANSPORTMESSAGE_12_014: [**The function shall return the methodName either set by the setter or the default (null) if unset so far.**]**


### setIotHubMethod

```java
public void setIotHubMethod(IotHubMethod iotHubMethod);
```

**SRS_IOTHUBTRANSPORTMESSAGE_21_002: [**The setIotHubMethod shall store the iotHubMethod. This function do not evaluates this parameter.**]**  


### setUriPath

```java
public void setUriPath(String uriPath);
```

**SRS_IOTHUBTRANSPORTMESSAGE_21_003: [**The setUriPath shall store the uriPath. This function do not evaluates this parameter.**]**  


### getIotHubMethod

```java
public IotHubMethod getIotHubMethod();
```

**SRS_IOTHUBTRANSPORTMESSAGE_21_004: [**The getIotHubMethod shall return the stored iotHubMethod.**]**  


### getUriPath

```java
public String getUriPath();
```

**SRS_IOTHUBTRANSPORTMESSAGE_21_005: [**The getUriPath shall return the stored uriPath.**]**  
