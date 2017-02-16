# Twin Requirements

## Overview

Twin is a representation of the device twin database.

## References

[Understand device twins](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins)

## Exposed API

```java
/**
 * Twin Representation including the twin database and Json serializer and deserializer.
 */
public class Twin
{
    public Twin();
    public Twin(TwinPropertiesChangeCallback onDesiredCallback);
    public Twin(TwinPropertiesChangeCallback onDesiredCallback, 
                TwinPropertiesChangeCallback onReportedCallback);
    
    public void setDesiredCallback(TwinPropertiesChangeCallback onDesiredCallback);
    public void setReportedCallback(TwinPropertiesChangeCallback onReportedCallback);
    public void enableMetadata();

    public String toJson();
    public JsonElement toJsonElement();


    public void updateTwin(String json);
    public void updateDesiredProperty(String json);
    public void updateReportedProperty(String json);

    public String updateDesiredProperty(Map<String, String> property);
    public String updateReportedProperty(Map<String, String> property);

    public Integer getDesiredPropertyVersion();
    public Integer getReportedPropertyVersion();
    
    public HashMap<String, String> getDesiredPropertyMap();
    public HashMap<String, String> getReportedPropertyMap();
}

/**
 * An interface for an IoT Hub Device Twin callback.
 *
 * Developers are expected to create an implementation of this interface,
 * and the transport will call {@link TwinPropertiesChangeCallback#execute(Map<String , String>)}
 * upon receiving a property changes from an IoT Hub Device Twin.
 */
public interface TwinPropertiesChangeCallback {
    /**
     * Executes the callback.
     *
     * @param propertyMap is a collection of properties that had its values changed.
     */
    void execute(Map<String , String> propertyMap);
}
```


### Twin

```java
/**
 * CONSTRUCTOR
 * Create a Twin instance with default values.
 *      set OnDesiredCallback as null
 *      set OnReportedCallback as null
 *
 */
public Twin()
```

**SRS_TWIN_21_001: [**The constructor shall create an instance of the properties.**]**  
**SRS_TWIN_21_002: [**The constructor shall set OnDesiredCallback as null.**]**  
**SRS_TWIN_21_003: [**The constructor shall set OnReportedCallback as null.**]**  


### Twin

```java
/**
 * CONSTRUCTOR
 * Create a Twin instance with default values.
 *      set OnReportedCallback as null
 *
 * @param onDesiredCallback - Callback function to report changes on the `Desired` database.
 */
public Twin(TwinPropertiesChangeCallback onDesiredCallback)
```

**SRS_TWIN_21_005: [**The constructor shall call the standard constructor.**]**  
**SRS_TWIN_21_006: [**The constructor shall set OnDesiredCallback with the provided Callback function.**]**  
**SRS_TWIN_21_007: [**The constructor shall set OnReportedCallback as null.**]**  


### Twin

```java
/**
 * CONSTRUCTOR
 * Create a Twin instance with default values.
 *
 * @param onDesiredCallback - Callback function to report changes on the `Desired` database.
 * @param onReportedCallback - Callback function to report changes on the `Reported` database.
 */
public Twin(TwinPropertiesChangeCallback onDesiredCallback, 
            TwinPropertiesChangeCallback onReportedCallback)
```

**SRS_TWIN_21_009: [**The constructor shall call the standard constructor.**]**  
**SRS_TWIN_21_010: [**The constructor shall set OnDesiredCallback with the provided Callback function.**]**  
**SRS_TWIN_21_011: [**The constructor shall set OnReportedCallback with the provided Callback function.**]**  


### setDesiredCallback

```java
/**
 * Set the callback function to report changes on the `Desired` database when `Twin`
 * receives a new json.
 *
 * @param onDesiredCallback - Callback function to report changes on the `Desired` database.
 */
public void setDesiredCallback(TwinPropertiesChangeCallback onDesiredCallback)
```

**SRS_TWIN_21_013: [**The setDesiredCallback shall set OnDesiredCallback with the provided callback function.**]**  
**SRS_TWIN_21_053: [**The setDesiredCallback shall keep only one instance of the callback.**]**  
**SRS_TWIN_21_054: [**If the OnDesiredCallback is already set, the setDesiredCallback shall replace the first one.**]**  
**SRS_TWIN_21_055: [**If callback is null, the setDesiredCallback will set the OnDesiredCallback as null.**]**  


### setReportedCallback

```java
/**
 * Set the callback function to report changes on the `Reported` database when `Twin`
 * receives a new json.
 *
 * @param onReportedCallback - Callback function to report changes on the `Reported` database.
 */
public void setReportedCallback(TwinPropertiesChangeCallback onReportedCallback)
```

**SRS_TWIN_21_014: [**The setReportedCallback shall set OnReportedCallback with the provided callback function.**]**  
**SRS_TWIN_21_056: [**The setReportedCallback shall keep only one instance of the callback.**]**  
**SRS_TWIN_21_057: [**If the OnReportedCallback is already set, the setReportedCallback shall replace the first one.**]**  
**SRS_TWIN_21_058: [**If callback is null, the setReportedCallback will set the OnReportedCallback as null.**]**  


### toJson

```java
/**
 * Create a String with a json content that represents all the information in the Twin class and innerClasses.
 *
 * @return String with the json content.
 */
public String toJson()
```

**SRS_TWIN_21_015: [**The toJson shall create a String with information in the Twin using json format.**]**  
**SRS_TWIN_21_016: [**The toJson shall not include null fields.**]**  


### toJsonElement

```java
/**
 * Create a JsonElement that represents all the information in the Twin class and innerClasses.
 *
 * @return JsonElement with the Twin information.
 */
public JsonElement toJsonElement()
```

**SRS_TWIN_21_017: [**The toJsonElement shall return a JsonElement with information in the Twin using json format.**]**  
**SRS_TWIN_21_018: [**The toJsonElement shall not include null fields.**]**  
**SRS_TWIN_21_086: [**The toJsonElement shall include the `properties` in the json even if it has no content.**]**  
**SRS_TWIN_21_087: [**The toJsonElement shall include the `desired` property in the json even if it has no content.**]**  
**SRS_TWIN_21_088: [**The toJsonElement shall include the `reported` property in the json even if it has no content.**]**  


### enableMetadata

```java
/**
 * Enable metadata report in the Json.
 *
 */
public void enableMetadata();
```

**SRS_TWIN_21_020: [**The enableMetadata shall enable report metadata in Json for the Desired and for the Reported Properties.**]**  


### updateDesiredProperty

```java
/**
 * Update the `desired` properties information in the database, and return a string with a json that contains a
 * collection of added properties, or properties with new value.
 *
 * @param propertyMap - Map of `desired` property to change the database.
 * @return Json with added or changed properties
 * @throws IllegalArgumentException This exception is thrown if the properties in the map do not fits the requirements.
 */
public String updateDesiredProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
```

**SRS_TWIN_21_021: [**The updateDesiredProperty shall add all provided properties to the Desired property.**]**  
**SRS_TWIN_21_022: [**The updateDesiredProperty shall return a string with json representing the desired properties with changes.**]**  
**SRS_TWIN_21_023: [**If the provided `property` map is null, the updateDesiredProperty shall not change the database and return null.**]**  
**SRS_TWIN_21_024: [**If no Desired property changed its value, the updateDesiredProperty shall return null.**]**  
**SRS_TWIN_21_059: [**The updateDesiredProperty shall only change properties in the map, keep the others as is.**]**  
**SRS_TWIN_21_061: [**The key and value in property shall be case sensitive.**]**  
**SRS_TWIN_21_063: [**If the provided `property` map is empty, the updateDesiredProperty shall not change the database and return null.**]**  
**SRS_TWIN_21_061: [**The key and value in property shall be case sensitive.**]**  
**SRS_TWIN_21_073: [**If any `key` is null, the updateDesiredProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_21_074: [**If any `key` is empty, the updateDesiredProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_21_075: [**If any `key` is more than 128 characters long, the updateDesiredProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_21_076: [**If any `key` has an illegal character (`$`,`.`, space), the updateDesiredProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_21_077: [**If any `key` already exists, the updateDesiredProperty shall replace the existed value by the new one.**]**  
**SRS_TWIN_21_078: [**If any `value` is null, the updateDesiredProperty shall store it but do not report on Json.**]**  


### updateReportedProperty

```java
/**
 * Update the `reported` properties information in the database, and return a string with a json that contains a
 * collection of added properties, or properties with new value.
 *
 * @param propertyMap - Map of `reported` property to change the database.
 * @return Json with added or changed properties
 * @throws IllegalArgumentException This exception is thrown if the properties in the map do not fits the requirements.
 */
public String updateReportedProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
```

**SRS_TWIN_21_025: [**The updateReportedProperty shall add all provided properties to the Reported property.**]**  
**SRS_TWIN_21_026: [**The updateReportedProperty shall return a string with json representing the Reported properties with changes.**]**  
**SRS_TWIN_21_027: [**If the provided `property` map is null, the updateReportedProperty shall not change the database and return null.**]**  
**SRS_TWIN_21_028: [**If no Reported property changed its value, the updateReportedProperty shall return null.**]**  
**SRS_TWIN_21_060: [**The updateReportedProperty shall only change properties in the map, keep the others as is.**]**  
**SRS_TWIN_21_062: [**All `key` and `value` in property shall be case sensitive.**]**  
**SRS_TWIN_21_064: [**If the provided `property` map is empty, the updateReportedProperty shall not change the database and return null.**]**  
**SRS_TWIN_21_079: [**If any `key` is null, the updateReportedProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_21_080: [**If any `key` is empty, the updateReportedProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_21_081: [**If any `key` is more than 128 characters long, the updateReportedProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_21_082: [**If any `key` has an illegal character (`$`,`.`, space), the updateReportedProperty shall throw IllegalArgumentException.**]**  
**SRS_TWIN_21_083: [**If any `key` already exists, the updateReportedProperty shall replace the existed value by the new one.**]**  
**SRS_TWIN_21_084: [**If any `value` is null, the updateReportedProperty shall store it but do not report on Json.**]**  


### updateDesiredProperty

```java
/**
 * Update the `desired` properties information in the database, using the information parsed from the provided json.
 * It will fire a callback if any property was added, excluded, or had its value updated.
 *
 * @param json - Json with `desired` property to change the database.
 * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
 */
public void updateDesiredProperty(String json) throws IllegalArgumentException
```

**SRS_TWIN_21_029: [**The updateDesiredProperty shall update the Desired property using the information provided in the json.**]**  
**SRS_TWIN_21_030: [**The updateDesiredProperty shall generate a map with all pairs key value that had its content changed.**]**  
**SRS_TWIN_21_031: [**The updateDesiredProperty shall send the map with all changed pairs to the upper layer calling onDesiredCallback (TwinPropertiesChangeCallback).**]**  
**SRS_TWIN_21_032: [**If the OnDesiredCallback is set as null, the updateDesiredProperty shall discard the map with the changed pairs.**]**  
**SRS_TWIN_21_033: [**If there is no change in the Desired property, the updateDesiredProperty shall not change the database and not call the OnDesiredCallback.**]**  
**SRS_TWIN_21_065: [**If the provided json is empty, the updateDesiredProperty shall not change the database and not call the OnDesiredCallback.**]**  
**SRS_TWIN_21_066: [**If the provided json is null, the updateDesiredProperty shall not change the database and not call the OnDesiredCallback.**]**  
**SRS_TWIN_21_092: [**If the provided json is not valid, the updateDesiredProperty shall throws IllegalArgumentException.**]**  
**SRS_TWIN_21_096: [**If the provided json have any duplicated `key`, the updateDesiredProperty shall throws IllegalArgumentException.**]**  


### updateReportedProperty

```java
/**
 * Update the `reported` properties information in the database, using the information parsed from the provided json.
 * It will fire a callback if any property was added, excluded, or had its value updated.
 *
 * @param json - Json with `reported` property to change the database.
 * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
 */
public void updateReportedProperty(String json) throws IllegalArgumentException
```

**SRS_TWIN_21_034: [**The updateReportedProperty shall update the Reported property using the information provided in the json.**]**  
**SRS_TWIN_21_035: [**The updateReportedProperty shall generate a map with all pairs key value that had its content changed.**]**  
**SRS_TWIN_21_036: [**The updateReportedProperty shall send the map with all changed pairs to the upper layer calling onReportedCallback (TwinPropertiesChangeCallback).**]**  
**SRS_TWIN_21_037: [**If the OnReportedCallback is set as null, the updateReportedProperty shall discard the map with the changed pairs.**]**  
**SRS_TWIN_21_038: [**If there is no change in the Reported property, the updateReportedProperty shall not change the database and not call the OnReportedCallback.**]**  
**SRS_TWIN_21_067: [**If the provided json is empty, the updateReportedProperty shall not change the database and not call the OnReportedCallback.**]**  
**SRS_TWIN_21_068: [**If the provided json is null, the updateReportedProperty shall not change the database and not call the OnReportedCallback.**]**  
**SRS_TWIN_21_093: [**If the provided json is not valid, the updateReportedProperty shall throws IllegalArgumentException.**]**  
**SRS_TWIN_21_095: [**If the provided json have any duplicated `key`, the updateReportedProperty shall throws IllegalArgumentException.**]**  


### updateTwin

```java
/**
 * Update the properties information in the database, using the information parsed from the provided json.
 * It will fire a callback if any property was added, excluded, or had its value updated.
 *
 * @param json - Json with property to change the database.
 * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
 */
public void updateTwin(String json) throws IllegalArgumentException
```

**SRS_TWIN_21_039: [**The updateTwin shall fill the fields the properties in the Twin class with the keys and values provided in the json string.**]**  
**SRS_TWIN_21_040: [**The updateTwin shall not change fields that is not reported in the json string.**]**  
**SRS_TWIN_21_041: [**The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.**]**  
**SRS_TWIN_21_042: [**If a valid key has a null value, the updateTwin shall delete this property.**]**  
**SRS_TWIN_21_043: [**If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.**]**  
**SRS_TWIN_21_044: [**If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.**]**  
**SRS_TWIN_21_045: [**If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.**]**  
**SRS_TWIN_21_046: [**If OnDesiredCallback was not provided, the updateTwin shall not do anything with the list of updated desired properties.**]**  
**SRS_TWIN_21_047: [**If OnReportedCallback was not provided, the updateTwin shall not do anything with the list of updated reported properties.**]**  
**SRS_TWIN_21_069: [**If there is no change in the Desired property, the updateTwin shall not change the reported database and not call the OnReportedCallback.**]**  
**SRS_TWIN_21_070: [**If there is no change in the Reported property, the updateTwin shall not change the reported database and not call the OnReportedCallback.**]**  
**SRS_TWIN_21_071: [**If the provided json is empty, the updateTwin shall not change the database and not call the OnDesiredCallback or the OnReportedCallback.**]**  
**SRS_TWIN_21_072: [**If the provided json is null, the updateTwin shall not change the database and not call the OnDesiredCallback or the OnReportedCallback.**]**  
**SRS_TWIN_21_089: [**If the provided json contains `desired` or `reported` in its first level, the updateTwin shall parser the json as properties only.**]**  
**SRS_TWIN_21_090: [**If the provided json is properties only and contains other tag different than `desired` or `reported`, the updateTwin shall throws IllegalArgumentException.**]**  
**SRS_TWIN_21_091: [**If the provided json is NOT properties only and contains `desired` or `reported` in its first level, the updateTwin shall throws IllegalArgumentException.**]**  
**SRS_TWIN_21_094: [**If the provided json have any duplicated `key`, the updateTwin shall use the content of the last one in the String.**]**  
**SRS_TWIN_21_097: [**If the provided json have any duplicated `properties`, the updateTwin shall throw IllegalArgumentException.**]**  
**SRS_TWIN_21_098: [**If the provided json is properties only and contains duplicated `desired` or `reported`, the updateTwin shall throws IllegalArgumentException.**]**  


### getDesiredPropertyVersion

```java
/**
 * Return the `desired` property version.
 *
 * @return Integer that contains the `desired` property version (it can be null).
 */
public Integer getDesiredPropertyVersion()
```

**SRS_TWIN_21_048: [**The getDesiredPropertyVersion shall return the desired property version.**]**  


### getReportedPropertyVersion

```java
/**
 * Return the `reported` property version.
 *
 * @return Integer that contains the `reported` property version (it can be null).
 */
public Integer getReportedPropertyVersion()
```

**SRS_TWIN_21_049: [**The getReportedPropertyVersion shall return the reported property version.**]**  


### getDesiredPropertyMap

```java
/**
 * Return a map with all `desired` properties in the database.
 *
 * @return A map with all `desired` properties in the database (it can be null).
 */
public Map<String, String> getDesiredPropertyMap()
```

**SRS_TWIN_21_050: [**The getDesiredPropertyMap shall return a map with all desired property key value pairs.**]**  

### getReportedPropertyMap

```java
/**
 * Return a map with all `reported` properties in the database.
 *
 * @return A map with all `reported` properties in the database (it can be null).
 */
public Map<String, String> getReportedPropertyMap()
```

**SRS_TWIN_21_051: [**The getReportedPropertyMap shall return a map with all reported property key value pairs.**]**  
