# IoTHubDeviceProperties Requirements

## Overview

IoT Hub device properties representation as described on session 7.4.1 of IoTGatewayPMspecs

## References

## Exposed API

```java
public class IoTHubDeviceProperties
{
    public enum DeviceStatusEnum {
        enabled,
        disabled
    }

    protected static String deviceId;
    protected static String generationId;
    protected static Integer etag;
    protected static DeviceStatusEnum status;
    protected static String statusReason;
    protected static String statusUpdateTime;

    public IoTHubDeviceProperties(String deviceId, String generationId) throws IllegalArgumentException
    public void SetDevice(String deviceId, String generationId) throws IllegalArgumentException
    public void EnableDevice()
    public void DisableDevice(String reason) throws IllegalArgumentException
    public String toJson()
    public void fromJson(String json)
    
    public String GetDeviceId()
    public String GetGenerationId()
    public String GetETag()
    public DeviceStatusEnum GetStatus()
    public String GetStatusReason()
    public String GetStatusUpdateTime()    
}
```

## General requirements

**SRS_IOTHUB_DEVICEPROPERTIES_21_038: [**All data and time shall use ISO8601 UTC format.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_039: [**All strings shall be up to 128 char long.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_040: [**All strings shall not be null.**]**  


### Constructor

```java
    public IoTHubDeviceProperties(String deviceId, String generationId) throws IllegalArgumentException
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_001: [**The constructor shall receive the deviceId and store it using the SetDevice.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_002: [**The constructor shall receive the generationId and store it using the SetGeneration.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_003: [**The constructor shall set the etag as `1`.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_004: [**The constructor shall set the device status as enabled.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_005: [**The constructor shall store the string `provisioned` in the statusReason.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_006: [**The constructor shall store the current date and time in statusUpdateTime.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_007: [**If one of the parameters do not fit the criteria, the constructor shall throw IllegalArgumentException.**]**  


### SetDevice

Set the device name and generation following the rulers:
deviceId: A case-sensitive string (up to 128 char long) of ASCII 7-bit alphanumeric chars + {'-', ':', '/', '\', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}. Non-alphanumeric characters are from URN RFC.
generationId: A case-sensitive string (up to 128 char long). This is used to identify devices with the same deviceId when they have been deleted and recreated. Currently implemented as GUID. Note: no ordering guaranteed. 

```java
    public void SetDevice(String deviceId, String generationId) throws IllegalArgumentException
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_008: [**The SetDevice shall receive the device name and store copy it into the deviceId.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_009: [**The SetDevice shall receive the device generation and store copy it into the generationId.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_010: [**The SetDevice shall increment the etag by `1`.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_011: [**If the provided name is null, the SetDevice not change the deviceId.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_012: [**If the provided name do not fits the json criteria, the SetDevice shall throw IllegalArgumentException.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_014: [**If the provided generation is null, the SetDevice shall not change the generationId.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_015: [**If the provided generation do not fits the json criteria, the SetDevice shall throw IllegalArgumentException.**]**  


### EnableDevice

If `Enabled`, this device is authorized to connect. 

```java
    public void EnableDevice()
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_017: [**The EnableDevice shall set the device as `enabled`.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_018: [**The EnableDevice shall store the string `provisioned` in the statusReason.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_019: [**The EnableDevice shall increment the etag by `1`.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_020: [**The EnableDevice shall store the current date and time in statusUpdateTime.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_013: [**If the device is already enable, the EnableDevice shall not do anything.**]**  


### DisableDevice

If `Disabled` this device cannot receive or send messages, and statusReason has to be set. Note: Service can still send C2D msgs to the device. 

```java
    public void DisableDevice(String reason) throws IllegalArgumentException
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_021: [**The DisableDevice shall set the device as `disabled`.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_022: [**The DisableDevice shall store the provided reason in the statusReason.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_023: [**The DisableDevice shall increment the etag by `1`.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_024: [**The DisableDevice shall store the current date and time in statusUpdateTime.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_025: [**If the provided reason is null, the DisableDevice shall throw IllegalArgumentException.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_026: [**If the provided reason do not fits the json criteria, the DisableDevice shall throw IllegalArgumentException.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_027: [**If the device is already disabled, the DisableDevice shall not do anything.**]**  


### toJson

Serialize the IoTHubDeviceProperties in a json format and return it as a String.

```java
    public String toJson()
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_028: [**The toJson shall create a String with information in the IoTHubDeviceProperties using json format.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_029: [**The toJson shall not include null fields.**]**  


### fromJson

Deserialize a string that contains a json data in to the IoTHubDeviceProperties.

```java
    public void fromJson(String json)
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_030: [**The fromJson shall fill the fields in IoTHubDeviceProperties with the values provided in the json string.**]**  
**SRS_IOTHUB_DEVICEPROPERTIES_21_031: [**The fromJson shall not change fields that is not reported in the json string.**]**  


### GetDeviceId

```java
    public String GetDeviceId()
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_032: [**The GetDeviceId shall return a string with the device name stored in the deviceId.**]**  


### GetGenerationId

```java
    public String GetGenerationId()
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_033: [**The GetGenerationId shall return a string with the device generation stored in the generationId.**]**  


### GetETag

```java
    public String GetETag()
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_034: [**The GetETag shall return a string with the last message ETag stored in the etag.**]**  


### GetStatus

```java
    public DeviceStatusEnum GetStatus()
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_035: [**The GetStatus shall return the device status stored in the status.**]**  


### GetStatusReason

```java
    public String GetStatusReason()
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_036: [**The GetStatusReason shall return a string with the status reason stored in the statusReason.**]**  


### GetStatusUpdateTime

```java
    public String GetStatusUpdateTime()
```

**SRS_IOTHUB_DEVICEPROPERTIES_21_037: [**The GetStatusUpdateTime shall return a string with the last status update time stored in the statusUpdateTime.**]**  


