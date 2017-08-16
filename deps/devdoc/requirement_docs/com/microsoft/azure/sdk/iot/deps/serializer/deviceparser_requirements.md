# DeviceParser Requirements

## Overview

Representation of a Device with a Json deserializer and serializer.

## References


## Exposed API

```java
public class DeviceParser
{
    public DeviceParser(String json);
    public String toJson();

    public String getETag()
    public void setETag(String eTag)
    public String getDeviceId()
    public void setDeviceId(String deviceId) throws IllegalArgumentException
    public String getGenerationId()
    public void setGenerationId(String generationId)
    public String getStatus()
    public void setStatus(String status)
    public String getStatusReason()
    public void setStatusReason(String statusReason)
    public Date getStatusUpdatedTime()
    public void setStatusUpdatedTime(Date statusUpdatedTime)
    public String getConnectionState()
    public void setConnectionState(String connectionState)
    public Date getConnectionStateUpdatedTime()
    public void setConnectionStateUpdatedTime(Date connectionStateUpdatedTime)
    public Date getLastActivityTime()
    public void setLastActivityTime(Date lastActivityTime)
    public long getCloudToDeviceMessageCount()
    public void setCloudToDeviceMessageCount(long cloudToDeviceMessageCount)
    public AuthenticationParser getAuthenticationParser()
    public void setAuthenticationParser(AuthenticationParser authenticationParser) throws IllegalArgumentException
}
```

### toJson
```java
public String toJson();
```
**SRS_DEVICE_PARSER_34_001: [**This method shall return a json representation of this.**]**


### DeviceParser
```java
public DeviceParser(String json);
```
**SRS_DEVICE_PARSER_34_002: [**This constructor shall create a DeviceParser object based off of the provided json.**]**

**SRS_DEVICE_PARSER_34_005: [**If the provided json is null or empty, an IllegalArgumentException shall be thrown.**]**

**SRS_DEVICE_PARSER_34_006: [**If the provided json cannot be parsed into a DeviceParser object, an IllegalArgumentException shall be thrown.**]**

**SRS_DEVICE_PARSER_34_011: [**If the provided json is missing the DeviceId field or its value is empty, an IllegalArgumentException shall be thrown.**]**

**SRS_DEVICE_PARSER_34_012: [**If the provided json is missing the authentication field or its value is empty, an IllegalArgumentException shall be thrown.**]**


### setDeviceId
```java
public void setDeviceId(String deviceId)
````
**SRS_DEVICE_PARSER_34_009: [**This method shall set the value of deviceId to the provided value.**]**

**SRS_DEVICE_PARSER_34_010: [**If the provided deviceId value is null, an IllegalArgumentException shall be thrown.**]**


### setAuthenticationParser
```java
public void setAuthenticationParser(AuthenticationParser authenticationParser)
```
**SRS_DEVICE_PARSER_34_007: [**This method shall set the value of authenticationParser to the provided value.**]**

**SRS_DEVICE_PARSER_34_008: [**If the provided authenticationParser value is null, an IllegalArgumentException shall be thrown.**]**


### setETag
```java
public void setETag(String eTag)
```
**SRS_DEVICE_PARSER_34_013: [**This method shall set the value of this object's ETag equal to the provided value.**]**


### getETag
```java
public String getETag()
```
**SRS_DEVICE_PARSER_34_014: [**This method shall return the value of this object's ETag.**]**


### setGenerationId
```java
public void setGenerationId(String generationId)
```
**SRS_DEVICE_PARSER_34_015: [**This method shall set the value of this object's Generation Id equal to the provided value.**]**


### getGenerationId
```java
public String getGenerationId()
```
**SRS_DEVICE_PARSER_34_016: [**This method shall return the value of this object's Generation Id.**]**


### setStatus
```java
public void setStatus(String status)
```
**SRS_DEVICE_PARSER_34_017: [**This method shall set the value of this object's Status equal to the provided value.**]**


### getStatus
```java
public String getStatus()
```
**SRS_DEVICE_PARSER_34_018: [**This method shall return the value of this object's Status.**]**


### setStatusReason
```java
public void setStatusReason(String statusReason)
```
**SRS_DEVICE_PARSER_34_019: [**This method shall set the value of this object's Status Reason equal to the provided value.**]**


### getStatusReason
```java
public String getStatusReason()
```
**SRS_DEVICE_PARSER_34_020: [**This method shall return the value of this object's Status Reason.**]**


### setStatusUpdatedTime
```java
public void setStatusUpdatedTime(Date statusUpdatedTime)
```
**SRS_DEVICE_PARSER_34_021: [**This method shall set the value of this object's statusUpdatedTime equal to the provided value.**]**


### getStatusUpdatedTime
```java
public Date getStatusUpdatedTime()
```
**SRS_DEVICE_PARSER_34_022: [**This method shall return the value of this object's statusUpdatedTime.**]**


### setConnectionState
```java
public void setConnectionState(String connectionState)
```
**SRS_DEVICE_PARSER_34_023: [**This method shall set the value of this object's connectionState equal to the provided value.**]**


### getConnectionState
```java
public String getConnectionState()
```
**SRS_DEVICE_PARSER_34_024: [**This method shall return the value of this object's connectionState.**]**


### setConnectionStateUpdatedTime
```java
public void setConnectionStateUpdatedTime(Date connectionStateUpdatedTime)
```
**SRS_DEVICE_PARSER_34_025: [**This method shall set the value of this object's connectionStateUpdatedTime equal to the provided value.**]**


### getConnectionStateUpdatedTime
```java
public Date getConnectionStateUpdatedTime()
```
**SRS_DEVICE_PARSER_34_026: [**This method shall return the value of this object's connectionStateUpdatedTime.**]**


### setLastActivityTime
```java
public void setLastActivityTime(Date lastActivityTime)
```
**SRS_DEVICE_PARSER_34_027: [**This method shall set the value of this object's lastActivityTime equal to the provided value.**]**


### getLastActivityTime
```java
public Date getLastActivityTime()
```
**SRS_DEVICE_PARSER_34_028: [**This method shall return the value of this object's lastActivityTime.**]**


### setCloudToDeviceMessageCount
```java
public void setCloudToDeviceMessageCount(long cloudToDeviceMessageCount)
```
**SRS_DEVICE_PARSER_34_029: [**This method shall set the value of this object's cloudToDeviceMessageCount equal to the provided value.**]**


### getCloudToDeviceMessageCount
```java
public long getCloudToDeviceMessageCount()
```
**SRS_DEVICE_PARSER_34_030: [**This method shall return the value of this object's cloudToDeviceMessageCount.**]**


### getAuthenticationParser
```java
public AuthenticationParser getAuthenticationParser()
```
**SRS_DEVICE_PARSER_34_031: [**This method shall return the value of this object's AuthenticationParser.**]**


### getDeviceId
```java
public String getDeviceId()
```
**SRS_DEVICE_PARSER_34_032: [**This method shall return the value of this object's DeviceId.**]**