# TwinTags Requirements

## Overview

TwinTags is a representation of the device twin tags database.


## References

session 7.3.1 of Azure IoT Hub - Device Twin.


## Exposed API

```java
public class TwinTags extends HashMap<String, HashMap<String, Object>> {

    public TwinTags()
    public TwinTags(String tag, HashMap<String, Object> tagProperties) throws IllegalArgumentException
    
    public void addTag(String tag, HashMap<String, Object> tagProperties) throws IllegalArgumentException
    public void addTag(String tag, String Key, Object value) throws IllegalArgumentException

    public String GetTagProperty(String tag, String property) throws IllegalArgumentException

    public String toJson()
    public void fromJson(String json)
}
```


### TwinTags

```java
    public TwinTags()
```

**SRS_TWIN_TAGS_21_001: [**The constructor shall initialize an empty HashMap of tags calling the superClass constructor.**]**  


### TwinTags

```java
    public TwinTags(String tag, HashMap<String, Object> tagProperties) throws IllegalArgumentException
```

**SRS_TWIN_TAGS_21_002: [**The constructor shall initialize an empty HashMap of tags calling the superClass constructor.**]**  
**SRS_TWIN_TAGS_21_003: [**The constructor shall add the provided `tag` and its properties into the superClass.**]**  
**SRS_TWIN_TAGS_21_004: [**If the `tag` is null, the constructor shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_005: [**If the `tag` is empty, the constructor shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_006: [**If the `tag` is more than 128 characters long, the constructor shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_007: [**If the `tag` has a illegal  character, the constructor shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_008: [**If the any `tagProperty` key is null, the constructor shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_009: [**If the any `tagProperty` key is empty, the constructor shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_010: [**If the any `tagProperty` key is more than 128 characters long, the constructor shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_011: [**If the any `tagProperty` key has a illegal  character, the constructor shall throw IllegalArgumentException.**]**  


### addTag

```java
    public void addTag(String tag, HashMap<String, Object> tagProperties) throws IllegalArgumentException
```

**SRS_TWIN_TAGS_21_012: [**The addTag shall add the provided `tag` and its properties into the superClass.**]**  
**SRS_TWIN_TAGS_21_013: [**If the `tag` already exists, the addTag shall add the properties to the existed tag.**]**  
**SRS_TWIN_TAGS_21_014: [**If the `tag` is null, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_015: [**If the `tag` is empty, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_016: [**If the `tag` is more than 128 characters long, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_017: [**If the `tag` has a illegal  character, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_018: [**If the any `tagProperty` key is null, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_019: [**If the any `tagProperty` key is empty, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_020: [**If the any `tagProperty` key is more than 128 characters long, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_021: [**If the any `tagProperty` key has a illegal  character, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_022: [**If the any `tagProperty` key already exists, the addTag shall replace the existed value by the new one.**]**  


### addTag

```java
    public void addTag(String tag, String Key, Object value) throws IllegalArgumentException
```

**SRS_TWIN_TAGS_21_023: [**The addTag shall add the provided `tag` and the property into the superClass.**]**  
**SRS_TWIN_TAGS_21_024: [**If the `tag` already exists, the addTag shall add the property to the existed tag.**]**  
**SRS_TWIN_TAGS_21_025: [**If the `tag` is null, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_026: [**If the `tag` is empty, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_027: [**If the `tag` is more than 128 characters long, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_028: [**If the `tag` has a illegal  character, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_029: [**If the `key` is null, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_030: [**If the `key` is empty, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_031: [**If the `key` is more than 128 characters long, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_032: [**If the `key` has a illegal  character, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_033: [**If the `key` already exists, the addTag shall replace the existed value by the new one.**]**  


### GetTagProperty

```java
    public String GetTagProperty(String tag, String key) throws IllegalArgumentException
```

**SRS_TWIN_TAGS_21_034: [**The GetTagProperty shall return the string that correspond to the value of the provided key in the provided tag.**]**  
**SRS_TWIN_TAGS_21_035: [**If the `tag` is null, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_036: [**If the `tag` is empty, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_037: [**If the `tag` is more than 128 characters long, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_038: [**If the `tag` has a illegal  character, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_039: [**If the `tag` do not exists, the addTag shall return null.**]**  
**SRS_TWIN_TAGS_21_040: [**If the `key` is null, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_041: [**If the `key` is empty, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_042: [**If the `key` is more than 128 characters long, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_043: [**If the `key` has a illegal  character, the addTag shall throw IllegalArgumentException.**]**  
**SRS_TWIN_TAGS_21_044: [**If the `key` do not exists, the addTag shall return null.**]**  


### toJson

```java
    public String toJson();
```

**SRS_TWIN_TAGS_21_045: [**The toJson shall create a String with information in the TwinTags using json format.**]**  
**SRS_TWIN_TAGS_21_046: [**The toJson shall not include null fields.**]**  


### fromJson

```java
    public void fromJson(String json);
```

**SRS_TWIN_TAGS_21_047: [**The fromJson shall fill the fields in TwinTags with the values provided in the json string.**]**  
**SRS_TWIN_TAGS_21_048: [**The fromJson shall not change fields that is not reported in the json string.**]**  
