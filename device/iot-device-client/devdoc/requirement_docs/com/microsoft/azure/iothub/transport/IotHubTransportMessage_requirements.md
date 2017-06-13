# IotHubTransportMessage Requirements

## Overview

Extends Message, adding transport artifacts.

## References

## Exposed API

```java
public class IotHubTransportMessage extends Message
{
    public IotHubTransportMessage(String body);

    public void setIotHubMethod(IotHubMethod iotHubMethod);
    public void setUriPath(String uriPath);

    public IotHubMethod getIotHubMethod();
    public String getUriPath();
}
```


### IotHubTransportMessage
```java
public IotHubTransportMessage(String body);
```
**SRS_IOTHUBTRANSPORTMESSAGE_21_001: [**The constructor shall call the supper class with the body. This function do not evaluates this parameter.**]**  


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
