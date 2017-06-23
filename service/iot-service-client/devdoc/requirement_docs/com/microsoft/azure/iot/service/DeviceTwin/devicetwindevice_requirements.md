# DeviceTwinDevice  Requirements

## Overview

The DeviceTwinDevice is the Object representing the device tags and properties for a particular device. This object is used to communicate with DeviceTwin class for various operations.

## References

([IoTHub DeviceTwin.doc](to https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins)

## Exposed API

```java
public class DeviceTwinDevice 
{
    public DeviceTwinDevice(String deviceId) throws IllegalArgumentException;

    public String getDeviceId();
    public void setETag(String eTag) throws IllegalArgumentException;
    public String getETag();

    public Set<Pair> getReportedProperties();

    public void setTags(Set<Pair> tags) throws IllegalArgumentException;
    public Set<Pair> getTags();    

    public void setDesiredProperties(Set<Pair> desiredProperties) throws IllegalArgumentException;
    public Set<Pair> getDesiredProperties();  

    public String toString();
    public String tagsToString();
    public String desiredPropertiesToString();
    public String reportedPropertiesToString();

    protected void setReportedProperties(Map<String, Object> reportedProperties);
    protected void setDesiredProperties(Map<String, Object> desiredProperties);
    protected void setTags(Map<String, Object> tag);

    protected Map<String, Object> getTagMap();
    protected Map<String, Object> getDesiredMap();
    protected Map<String, Object> getReportedMap();
    protected Twin getTwinObject();    
}
```
**SRS_DEVICETWINDEVICE_25_001: [** The DeviceTwinDevice class has the following properties: deviceId, a container for tags, desired and reported properties, and a twin object. **]**

### DeviceTwinDevice

```java
public DeviceTwinDevice(String deviceId) throws IllegalArgumentException;
```
**SRS_DEVICETWINDEVICE_25_002: [** The constructor shall throw IllegalArgumentException if the input string is empty or null.**]**

**SRS_DEVICETWINDEVICE_25_003: [** The constructor shall create a new instance of twin object for this device and store the device id.**]**

### getDeviceId

```java
public String getDeviceId();
```
**SRS_DEVICETWINDEVICE_25_004: [** This method shall return the device id **]**

### getReportedProperties

```java
public Set<Pair> getReportedProperties();
```
**SRS_DEVICETWINDEVICE_25_005: [** This method shall convert the reported properties map to a set of pairs and return with it. **]**

**SRS_DEVICETWINDEVICE_25_006: [** If the reported properties map is null then this method shall return empty set of pairs.**]**

### setTags

```java
public void setTags(Set<Pair> tags) throws IllegalArgumentException;
```
**SRS_DEVICETWINDEVICE_25_007: [** This method shall convert the set of pairs of tags to a map and save it. **]**

**SRS_DEVICETWINDEVICE_25_008: [** If the tags Set is null then this method shall throw IllegalArgumentException.**]**

### getTags

```java
public Set<Pair> getTags();
```
**SRS_DEVICETWINDEVICE_25_009: [** This method shall convert the tags map to a set of pairs and return with it. **]**

**SRS_DEVICETWINDEVICE_25_010: [** If the tags map is null then this method shall return empty set of pairs.**]**

### setDesiredProperties

```java
public void setDesiredProperties(Set<Pair> desiredProperties) throws IllegalArgumentException; 
```
**SRS_DEVICETWINDEVICE_25_011: [** This method shall convert the set of pairs of desiredProperties to a map and save it. **]**

**SRS_DEVICETWINDEVICE_25_012: [** If the desiredProperties Set is null then this method shall throw IllegalArgumentException.**]**

### getDesiredProperties

```java
public Set<Pair> getDesiredProperties();
```
**SRS_DEVICETWINDEVICE_25_013: [** This method shall convert the desiredProperties map to a set of pairs and return with it. **]**

**SRS_DEVICETWINDEVICE_25_014: [** If the desiredProperties map is null then this method shall return empty set of pairs.**]**

### toString

```java
public String toString();
```
**SRS_DEVICETWINDEVICE_25_015: [** This method shall append device id, etag, tags, desired and reported properties to string (if present) and return **]**

### tagsToString

```java
public String tagsToString();
```

**SRS_DEVICETWINDEVICE_25_016: [** This method shall convert the tags map to string (if present) and return **]**

**SRS_DEVICETWINDEVICE_25_017: [** This method shall return an empty string if tags map is empty or null and return **]**

### desiredPropertiesToString

```java
public String desiredPropertiesToString();
```

**SRS_DEVICETWINDEVICE_25_018: [** This method shall convert the desiredProperties map to string (if present) and return **]**

**SRS_DEVICETWINDEVICE_25_019: [** This method shall return an empty string if desiredProperties map is empty or null and return **]**

### reportedPropertiesToString

```java
public String reportedPropertiesToString();
```

**SRS_DEVICETWINDEVICE_25_020: [** This method shall convert the reportedProperties map to string (if present) and return **]**

**SRS_DEVICETWINDEVICE_25_021: [** This method shall return an empty string if reportedProperties map is empty or null and return **]**

### setReportedProperties

```java
protected void setReportedProperties(Map<String, Object> reportedProperties);
```

**SRS_DEVICETWINDEVICE_25_022: [** This method shall save the reportedProperties map**]**

### setDesiredProperties

```java
protected void setDesiredProperties(Map<String, Object> desiredProperties);
```

**SRS_DEVICETWINDEVICE_25_023: [** This method shall save the desiredProperties map**]**

### setTags

```java
protected void setTags(Map<String, Object> tag);
```

**SRS_DEVICETWINDEVICE_25_024: [** This method shall save the tags map**]**

### getTagMap

```java
protected Map<String, Object> getTagMap();
```

**SRS_DEVICETWINDEVICE_25_025: [** This method shall return the tags map**]**

### getReportedMap

```java
protected Map<String, Object> getReportedMap();
```

**SRS_DEVICETWINDEVICE_25_026: [** This method shall return the reportedProperties map**]**

### getDesiredMap

```java
protected Map<String, Object> getDesiredMap();
```

**SRS_DEVICETWINDEVICE_25_027: [** This method shall return the desiredProperties map**]**

### getTwinObject

```java
protected Twin getTwinObject();    
```

**SRS_DEVICETWINDEVICE_25_028: [** This method shall return the twinObject for this device**]**

    
### setETag

```java
public void setETag(String eTag) throws IllegalArgumentException
```

**SRS_DEVICETWINDEVICE_21_029: [** The setETag shall throw IllegalArgumentException if the input string is empty or null.**]**
    
**SRS_DEVICETWINDEVICE_21_030: [** The setETag shall store the eTag.**]**


### setETag

```java
public String getETag()
```

**SRS_DEVICETWINDEVICE_21_031: [** The getETag shall return the stored eTag.**]**

