# TwinProperty Requirements

## Overview

TwinProperty is a representation of the device twin property database. It can represent `Desired` property as well `Reported` property.


## References

session 7.3.1 of Azure IoT Hub - Device Twin.


## Exposed API

```java
public class TwinProperty extends HashMap<String, Object> {

    @SerializedName("$metadata")
    protected HashMap<String, TwinMetadata> metadata = new HashMap<>();

    @SerializedName("$version")
    protected Integer version;

    public TwinProperty(Integer version) throws IllegalArgumentException
    public void addProperty(String key, Object value, Integer version) throws IllegalArgumentException

    public Integer GetVersion();
    public TwinMetadata GetMetadata(String key)
    public String toJson()
    public void fromJson(String json)
}
```


### TwinProperty

```java
    public TwinProperty(Integer version) throws IllegalArgumentException
```

**SRS_TWIN_PROPERTY_21_001: [**The constructor shall call the constructor for the superClass.**]**  
**SRS_TWIN_PROPERTY_21_002: [**The constructor shall store the provided version value in the version.**]**  
**SRS_TWIN_PROPERTY_21_003: [**If the provided version is null, the constructor shall throw IllegalArgumentException.**]**  


### addProperty

```java
    public Object addProperty(String key, Object value, Integer version) throws IllegalArgumentException
```

**SRS_TWIN_PROPERTY_21_004: [**The addProperty shall create an instance of the metadata related to the provided key and version.**]**  
**SRS_TWIN_PROPERTY_21_005: [**The addProperty shall add the created metadata to the `metadata`.**]**  
**SRS_TWIN_PROPERTY_21_006: [**The addProperty shall call the put in the superClass.**]**  
**SRS_TWIN_PROPERTY_21_007: [**The addProperty shall return the same `Object` returned by the superClass.**]**  
**SRS_TWIN_PROPERTY_21_008: [**If the `key` is null, the addProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_PROPERTY_21_009: [**If the `key` is empty, the addProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_PROPERTY_21_010: [**If the `key` is more than 128 characters long, the addProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_PROPERTY_21_011: [**If the `key` has an illegal character, the addProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_PROPERTY_21_012: [**If the `key` already exists, the addProperty shall replace the existed value by the new one.**]**  


### GetVersion

```java
    public Integer GetVersion();
```

**SRS_TWIN_PROPERTY_21_013: [**The GetVersion shall return an Integer with the property version stored in the `version`.**]**  


### GetMetadata

```java
    public TwinMetadata GetMetadata(String key)
```

**SRS_TWIN_PROPERTY_21_014: [**The GetMetadata shall return the TwinMetadata for the provided key.**]**  
**SRS_TWIN_PROPERTY_21_015: [**If the key do not exists, the GetMetadata shall return null.**]**  


### toJson

```java
    public String toJson();
```

**SRS_TWIN_PROPERTY_21_016: [**The toJson shall create a String with information in the TwinProperty using json format.**]**  
**SRS_TWIN_PROPERTY_21_017: [**The toJson shall not include null fields.**]**  


### fromJson

```java
    public void fromJson(String json);
```

**SRS_TWIN_PROPERTY_21_018: [**The fromJson shall fill the fields in TwinProperty with the values provided in the json string.**]**  
**SRS_TWIN_PROPERTY_21_019: [**The fromJson shall not change fields that is not reported in the json string.**]**  
