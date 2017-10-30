# DeviceMethodData Requirements

## Overview

DeviceMethodData is exposed to user as a utility to help perform DeviceMethod operations like setting status, response message in response to device method invocation.

## References

## Exposed API

```java
public class DeviceMethodData
{    
    public DeviceMethodData(int status, String responseMessage) throws IllegalArgumentException;

    public int getStatus();
    public String getResponseMessage();

    public void setResponseMessage(String responseMessage);
    public void setStatus(int status);    
}
```


### DeviceMethodData

```java
public DeviceMethodData(int status, String responseMessage) throws IllegalArgumentException;
```

**SRS_DEVICEMETHODDATA_25_001: [**The constructor shall save the status and response message provided by user.**]**


### getStatus

```java
public int getStatus();
```

**SRS_DEVICEMETHODDATA_25_003: [**This method shall return the status previously set.**]**


### getResponseMessage

```java
public String getResponseMessage(); 
```

**SRS_DEVICEMETHODDATA_25_004: [**This method shall return the response message previously set.**]**


### setResponseMessage

```java
public void setResponseMessage(String responseMessage);
```

**SRS_DEVICEMETHODDATA_25_005: [**This method shall save the response message provided by the user.**]**


### setStatus

```java
public void setStatus(int status);
```

**SRS_DEVICEMETHODDATA_25_007: [**The method shall set the status.**]**

