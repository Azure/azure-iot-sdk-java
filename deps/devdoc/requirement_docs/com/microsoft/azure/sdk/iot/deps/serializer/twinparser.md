# TwinParser Requirements

## Overview

TwinParser is a representation of the device twinParser collection.

## References

[Understand device twins](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins)

## Exposed API

```java
/**
 * TwinParser Representation including the twinParser collection and Json serializer and deserializer.
 */
public class TwinParser
{
    public TwinParser();
    public TwinParser(TwinChangedCallback onDesiredCallback);
    public TwinParser(TwinChangedCallback onDesiredCallback, 
                TwinChangedCallback onReportedCallback);
    
    public void setDesiredCallback(TwinChangedCallback onDesiredCallback);
    public void setReportedCallback(TwinChangedCallback onReportedCallback);
    public void setTagsCallback(TwinChangedCallback onTagsCallback);
    public void enableTags();
    public void enableMetadata();

    public String toJson();
    public JsonElement toJsonElement();

    public void updateTwin(String json);
    public void updateDesiredProperty(String json);
    public void updateReportedProperty(String json);

    public String updateDesiredProperty(Map<String, Object> propertyMap);
    public String updateReportedProperty(Map<String, Object> propertyMap);
    public String updateTags(Map<String, Object> tagsMap);
    public String updateTwin(Map<String, Object> desiredPropertyMap,
                             Map<String, Object> reportedPropertyMap,
                             Map<String, Object> tagsMap);
    public String updateDeviceManager(String deviceId, 
                                      TwinStatus status, 
                                      String statusReason);


    public String resetDesiredProperty(Map<String, Object> propertyMap);
    public String resetReportedProperty(Map<String, Object> propertyMap);
    public String resetTags(Map<String, Object> tagsMap);

    public Integer getDesiredPropertyVersion();
    public Integer getReportedPropertyVersion();
    
    public Map<String, Object> getDesiredPropertyMap();
    public Map<String, Object> getReportedPropertyMap();
    public Map<String, Object> getTagsMap();

    public void setDeviceId(String deviceId) throws IllegalArgumentException;
    public void setETag(String eTag) throws IllegalArgumentException;

    public String getDeviceId();
    public String getGenerationId();
    public String getETag();
    public Integer getVersion();
    public TwinStatus getStatus();
    public String getStatusReason();
    public String getStatusUpdatedTime();
    public DeviceConnectionState getConnectionState();
    public String getConnectionStateUpdatedTime();
    public String getLastActivityTime();
}

/**
 * An interface for an IoT Hub Device TwinParser callback.
 *
 * Developers are expected to create an implementation of this interface,
 * and the transport will call {@link TwinChangedCallback#execute(Map)}
 * upon receiving a property changes from an IoT Hub Device Twin.
 */
public interface TwinChangedCallback {
    /**
     * Executes the callback.
     *
     * @param propertyMap is a sub-collection of properties that had its values changed.
     */
    void execute(Map<String , Object> propertyMap);
}

/**
 * INNER TWIN CLASS
 *
 * Enum for device status
 */
public enum TwinStatus
{
    @SerializedName("enabled")
    enabled,

    @SerializedName("disabled")
    disabled
}

/**
 * INNER TWIN CLASS
 *
 * Enum for device connection state
 */
public enum TwinConnectionState 
{
    @SerializedName("disconnected")
    disconnected,

    @SerializedName("connected")
    connected
}
```


### Validation
**SRS_TWINPARSER_21_152: [**A valid `key` shall not be null.**]**  
**SRS_TWINPARSER_21_153: [**A valid `key` shall not be empty.**]**  
**SRS_TWINPARSER_21_154: [**A valid `key` shall be less than 128 characters long.**]**  
**SRS_TWINPARSER_21_155: [**A valid `key` shall not have an illegal character (`$`,`.`, space).**]**  
**SRS_TWINPARSER_21_156: [**A valid `value` shall contains types of boolean, number, string, or object.**]**  
**SRS_TWINPARSER_21_157: [**A valid `value` can contains sub-maps.**]**  
**SRS_TWINPARSER_21_158: [**A valid `value` shall contains less than 5 levels of sub-maps.**]**  


### TwinParser

```java
/**
 * CONSTRUCTOR
 * Create a TwinParser instance with default values.
 *      set OnDesiredCallback as null
 *      set OnReportedCallback as null
 *      set Tags as null
 *
 */
public TwinParser()
```

**SRS_TWINPARSER_21_001: [**The constructor shall create an instance of the properties.**]**  
**SRS_TWINPARSER_21_002: [**The constructor shall set OnDesiredCallback as null.**]**  
**SRS_TWINPARSER_21_003: [**The constructor shall set OnReportedCallback as null.**]**  
**SRS_TWINPARSER_21_004: [**The constructor shall set Tags as null.**]**  


### TwinParser

```java
/**
 * CONSTRUCTOR
 * Create a TwinParser instance with default values.
 *      set OnReportedCallback as null
 *      set Tags as null
 *
 * @param onDesiredCallback - Callback function to report changes on the `Desired` collection.
 */
public TwinParser(TwinChangedCallback onDesiredCallback)
```

**SRS_TWINPARSER_21_005: [**The constructor shall call the standard constructor.**]**  
**SRS_TWINPARSER_21_006: [**The constructor shall set OnDesiredCallback with the provided Callback function.**]**  
**SRS_TWINPARSER_21_007: [**The constructor shall set OnReportedCallback as null.**]**  
**SRS_TWINPARSER_21_008: [**The constructor shall set Tags as null.**]**  


### TwinParser

```java
/**
 * CONSTRUCTOR
 * Create a TwinParser instance with default values.
 *      set Tags as null
 *
 * @param onDesiredCallback - Callback function to report changes on the `Desired` collection.
 * @param onReportedCallback - Callback function to report changes on the `Reported` collection.
 */
public TwinParser(TwinChangedCallback onDesiredCallback, 
            TwinChangedCallback onReportedCallback)
```

**SRS_TWINPARSER_21_009: [**The constructor shall call the standard constructor.**]**  
**SRS_TWINPARSER_21_010: [**The constructor shall set OnDesiredCallback with the provided Callback function.**]**  
**SRS_TWINPARSER_21_011: [**The constructor shall set OnReportedCallback with the provided Callback function.**]**  
**SRS_TWINPARSER_21_012: [**The constructor shall set Tags as null.**]**  


### setDesiredCallback

```java
/**
 *TwinParser
 * receives a new json.
 *
 * @param onDesiredCallback - Callback function to report changes on the `Desired` collection.
 */
public void setDesiredCallback(TwinChangedCallback onDesiredCallback)
```

**SRS_TWINPARSER_21_013: [**The setDesiredCallback shall set OnDesiredCallback with the provided callback function.**]**  
**SRS_TWINPARSER_21_053: [**The setDesiredCallback shall keep only one instance of the callback.**]**  
**SRS_TWINPARSER_21_054: [**If the OnDesiredCallback is already set, the setDesiredCallback shall replace the first one.**]**  
**SRS_TWINPARSER_21_055: [**If callback is null, the setDesiredCallback will set the OnDesiredCallback as null.**]**  


### setReportedCallback

```java
/**
 * Set the callback function to report changes on the `Reported` collection when `TwinParser`
 * receives a new json.
 *
 * @param onReportedCallback - Callback function to report changes on the `Reported` collection.
 */
public void setReportedCallback(TwinChangedCallback onReportedCallback)
```

**SRS_TWINPARSER_21_014: [**The setReportedCallback shall set OnReportedCallback with the provided callback function.**]**  
**SRS_TWINPARSER_21_056: [**The setReportedCallback shall keep only one instance of the callback.**]**  
**SRS_TWINPARSER_21_057: [**If the OnReportedCallback is already set, the setReportedCallback shall replace the first one.**]**  
**SRS_TWINPARSER_21_058: [**If callback is null, the setReportedCallback will set the OnReportedCallback as null.**]**  


### setTagsCallback

```java
/**
 * Set the callback function to report changes on the `tags` collection when `TwinParser`
 * receives a new json.
 *
 * @param onTagsCallback - Callback function to report changes on the `Reported` collection.
 */
public void setTagsCallback(TwinChangedCallback onTagsCallback)
```

**SRS_TWINPARSER_21_099: [**The setTagsCallback shall set onTagsCallback with the provided callback function.**]**  
**SRS_TWINPARSER_21_100: [**The setTagsCallback shall keep only one instance of the callback.**]**  
**SRS_TWINPARSER_21_101: [**If the onTagsCallback is already set, the setTagsCallback shall replace the first one.**]**  
**SRS_TWINPARSER_21_102: [**If callback is null, the setTagsCallback will set the onTagsCallback as null.**]**  


### toJson

```java
/**
 * Create a String with a json content that represents all the information in the TwinParser class and innerClasses.
 *
 * @return String with the json content.
 */
public String toJson()
```

**SRS_TWINPARSER_21_015: [**The toJson shall create a String with information in the TwinParser using json format.**]**  
**SRS_TWINPARSER_21_016: [**The toJson shall not include null fields.**]**  


### toJsonElement

```java
/**
 * Create a JsonElement that represents all the information in the TwinParser class and innerClasses.
 *
 * @return JsonElement with the Twin information.
 */
public JsonElement toJsonElement()
```

**SRS_TWINPARSER_21_017: [**The toJsonElement shall return a JsonElement with information in the Twin using json format.**]**  
**SRS_TWINPARSER_21_018: [**The toJsonElement shall not include null fields.**]**  
**SRS_TWINPARSER_21_085: [**If `tags` is enable, the toJsonElement shall include the tags in the json even if it has no content.**]**  
**SRS_TWINPARSER_21_086: [**The toJsonElement shall include the `properties` in the json even if it has no content.**]**  
**SRS_TWINPARSER_21_087: [**The toJsonElement shall include the `desired` property in the json even if it has no content.**]**  
**SRS_TWINPARSER_21_088: [**The toJsonElement shall include the `reported` property in the json even if it has no content.**]**  


### enableTags

```java
/**
 * Enable tags in the Twin collection and in the Json.
 *
 */
public void enableTags();
```
**SRS_TWINPARSER_21_019: [**The enableTags shall enable tags in the twin collection.**]**  
**SRS_TWINPARSER_21_161: [**It tags is already enabled, twinParserenableTags shall not do anything.**]**  


### enableMetadata

```java
/**
 * Enable metadata report in the Json.
 *
 */
public void enableMetadata();
```

**SRS_TWINPARSER_21_020: [**The enableMetadata shall enable report metadata in Json for the Desired and for the Reported Properties.**]**  


### updateDesiredProperty

```java
/**
 * Update the `desired` properties information in the collection, and return a string with a json that contains a
 * sub-collection of added properties, or properties with new value.
 *
 * @param propertyMap - Map of `desired` property to change the collection.
 * @return Json with added or changed properties
 * @throws IllegalArgumentException This exception is thrown if the properties in the map do not fits the requirements.
 */
public String updateDesiredProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
```

**SRS_TWINPARSER_21_021: [**The updateDesiredProperty shall add all provided properties to the Desired property.**]**  
**SRS_TWINPARSER_21_022: [**The updateDesiredProperty shall return a string with json representing the desired properties with changes.**]**  
**SRS_TWINPARSER_21_023: [**If the provided `property` map is null, the updateDesiredProperty shall not change the collection and throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_024: [**If no Desired property changed its value, the updateDesiredProperty shall return null.**]**  
**SRS_TWINPARSER_21_059: [**The updateDesiredProperty shall only change properties in the map, keep the others as is.**]**  
**SRS_TWINPARSER_21_061: [**The `key` and `value` in property shall be case sensitive.**]**  
**SRS_TWINPARSER_21_063: [**If the provided `property` map is empty, the updateDesiredProperty shall not change the collection and return null.**]**  
**SRS_TWINPARSER_21_073: [**If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_077: [**If any `key` already exists, the updateDesiredProperty shall replace the existed value by the new one.**]**  
**SRS_TWINPARSER_21_078: [**If any `value` is null, the updateDesiredProperty shall delete it from the collection and report on Json.**]**  


### updateReportedProperty

```java
/**
 * Update the `reported` properties information in the collection, and return a string with a json that contains a
 * sub-collection of added properties, or properties with new value.
 *
 * @param propertyMap - Map of `reported` property to change the collection.
 * @return Json with added or changed properties
 * @throws IllegalArgumentException This exception is thrown if the properties in the map do not fits the requirements.
 */
public String updateReportedProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
```

**SRS_TWINPARSER_21_025: [**The updateReportedProperty shall add all provided properties to the Reported property.**]**  
**SRS_TWINPARSER_21_026: [**The updateReportedProperty shall return a string with json representing the Reported properties with changes.**]**  
**SRS_TWINPARSER_21_027: [**If the provided `property` map is null, the updateReportedProperty shall not change the collection and throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_028: [**If no Reported property changed its value, the updateReportedProperty shall return null.**]**  
**SRS_TWINPARSER_21_060: [**The updateReportedProperty shall only change properties in the map, keep the others as is.**]**  
**SRS_TWINPARSER_21_062: [**All `key` and `value` in property shall be case sensitive.**]**  
**SRS_TWINPARSER_21_064: [**If the provided `property` map is empty, the updateReportedProperty shall not change the collection and return null.**]**  
**SRS_TWINPARSER_21_079: [**If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_083: [**If any `key` already exists, the updateReportedProperty shall replace the existed value by the new one.**]**  
**SRS_TWINPARSER_21_084: [**If any `value` is null, the updateReportedProperty shall delete it from the collection and report on Json.**]**  


### updateTags

```java
/**
 * Update the `tags` information in the collection, and return a string with a json that contains a
 * sub-collection of added tags, or tags with new value.
 *
 * @param tagsMap - Map of `tags` to change the collection.
 * @return Json with added or changed tags
 * @throws IllegalArgumentException This exception is thrown if the tags in the map do not fits the requirements.
 * @throws IOException This exception is thrown if tags the is not enabled.
 */
public String updateTags(Map<String, Object> tagsMap) throws IllegalArgumentException, IOException
```

**SRS_TWINPARSER_21_103: [**The updateTags shall add all provided tags to the collection.**]**  
**SRS_TWINPARSER_21_104: [**The updateTags shall return a string with json representing the tags with changes.**]**  
**SRS_TWINPARSER_21_105: [**If the provided `tagsMap` is null, the updateTags shall not change the collection and throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_106: [**If no tags changed its value, the updateTags shall return null.**]**  
**SRS_TWINPARSER_21_107: [**The updateTags shall only change tags in the map, keep the others as is.**]**  
**SRS_TWINPARSER_21_108: [**All `key` and `value` in tags shall be case sensitive.**]**  
**SRS_TWINPARSER_21_109: [**If the provided `tagsMap` is empty, the updateTags shall not change the collection and return null.**]**  
**SRS_TWINPARSER_21_110: [**If the map is invalid, the updateTags shall throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_111: [**If Tags is not enable, the updateTags shall throw IOException.**]**  
**SRS_TWINPARSER_21_114: [**If any `key` already exists, the updateTags shall replace the existed value by the new one.**]**  
**SRS_TWINPARSER_21_115: [**If any `value` is null, the updateTags shall delete it from the collection and report on Json.**]**  


### updateTwin

```java
/**
 * Update properties and tags information in the collection, and return a string with a json that contains a
 * sub-collection of added properties, properties with new value, added tags, and tags with new values.
 *
 * @param desiredPropertyMap - Map of `desired` property to change the collection.
 * @param reportedPropertyMap - Map of `reported` property to change the collection.
 * @param tagsMap - Map of `tags` to change the collection.
 * @return Json with added or changed properties and tags
 * @throws IllegalArgumentException This exception is thrown if the properties or tags in the maps do not fits the requirements.
 * @throws IOException This exception is thrown if tags the is not enabled and tagsMap is different than null.
 */
public String updateTwin(Map<String, Object> desiredPropertyMap,
                         Map<String, Object> reportedPropertyMap,
                         Map<String, Object> tagsMap)
        throws IllegalArgumentException, IOException
```

**SRS_TWINPARSER_21_116: [**The updateTwin shall add all provided properties and tags to the collection.**]**  
**SRS_TWINPARSER_21_117: [**The updateTwin shall return a string with json representing the properties and tags with changes.**]**  
**SRS_TWINPARSER_21_118: [**If one of the provided map is null, the updateTwin shall not change that part of the collection.**]**  
**SRS_TWINPARSER_21_160: [**If all of the provided map is null, the updateTwin shall not change the collection and throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_119: [**If no property or tags changed its value, the updateTwin shall return null.**]**  
**SRS_TWINPARSER_21_126: [**The updateTwin shall only change properties and tags in the map, keep the others as is.**]**  
**SRS_TWINPARSER_21_127: [**The `key` and `value` in the maps shall be case sensitive.**]**  
**SRS_TWINPARSER_21_128: [**If one of the provided map is empty, the updateTwin shall not change its the collection.**]**  
**SRS_TWINPARSER_21_080: [**If one of the map is invalid, the updateTwin shall not change the collection and throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_081: [**If any `key` already exists, the updateTwin shall replace the existed value by the new one.**]**  
**SRS_TWINPARSER_21_082: [**If any `value` is null, the updateTwin shall delete it from the collection and report on Json.**]**  
**SRS_TWINPARSER_21_075: [**If Tags is not enable and `tagsMap` is not null, the updateTwin shall throw IOException.**]**  
**SRS_TWINPARSER_21_076: [**If `deviceId`, `generationId`, `etag`, `status`, `statusReason`, `statusUpdatedTime`, `connectionState`, `connectionStateUpdatedTime`, `lastActivityTime`, and `lastAcceptingIpFilterRule` are not null, the updateTwin shall include its value in the json.**]**  


### updateDeviceManager

```java
/**
 * Update the device manager information in the collection, and return a string with a json that contains a
 * the new device manager description, including new and old values.
 *
 * @param deviceId - Device name
 * @param status - Device status("enabled", "disabled")
 * @param statusReason - A 128 char long string storing the reason of suspension (for status="disabled").
 * @return Json with the manager description. Null if nothing change.
 * @throws IllegalArgumentException This exception is thrown if there are any inconsistence in the provided description.
 */
public String updateDeviceManager(String deviceId, TwinStatus status, String statusReason) throws IllegalArgumentException
```

**SRS_TWINPARSER_21_159: [**The updateDeviceManager shall replace the `deviceId` by the provided one.**]**  
**SRS_TWINPARSER_21_162: [**The updateDeviceManager shall replace the `status` by the provided one.**]**  
**SRS_TWINPARSER_21_163: [**If the provided `status` is different than the previous one, The updateDeviceManager shall replace the `statusReason` by the provided one.**]**  
**SRS_TWINPARSER_21_165: [**If the provided `status` is different than the previous one, and the `statusReason` is null, The updateDeviceManager shall throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_166: [**The updateDeviceManager shall return a json with the new device management information.**]**  
**SRS_TWINPARSER_21_167: [**If nothing change in the management collection, The updateDeviceManager shall return null.**]**  


### resetDesiredProperty

```java
/**
 * Reset the `desired` properties information in the collection, deleting all old properties and add all the new provided ones.
 * Return a string with a json that contains a sub-collection of added properties.
 *
 * @param propertyMap - Map of `desired` property to change the collection.
 * @return Json with added properties
 * @throws IllegalArgumentException. This exception is thrown if the properties in the map do not fits the requirements.
 */
public String resetDesiredProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
```

**SRS_TWINPARSER_21_120: [**The resetDesiredProperty shall cleanup the desired collection and add all provided properties to the Desired property.**]**  
**SRS_TWINPARSER_21_121: [**The resetDesiredProperty shall return a string with json representing the added desired properties.**]**  
**SRS_TWINPARSER_21_122: [**If the provided `propertyMap` is null, the resetDesiredProperty shall not change the collection and throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_123: [**The `key` and `value` in property shall be case sensitive.**]**  
**SRS_TWINPARSER_21_124: [**If the provided `propertyMap` is empty, the resetDesiredProperty shall cleanup the desired collection and return `{}`.**]**  
**SRS_TWINPARSER_21_125: [**If the map is invalid, the resetDesiredProperty shall not change the collection and throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_129: [**If any `value` is null, the resetDesiredProperty shall delete it from the collection and report on Json.**]**  


### resetReportedProperty

```java
/**
 * Reset the `reported` properties information in the collection, deleting all old properties and add all the new provided ones.
 * Return a string with a json that contains a sub-collection of added properties.
 *
 * @param propertyMap - Map of `reported` property to change the collection.
 * @return Json with added properties
 * @throws IllegalArgumentException This exception is thrown if the properties in the map do not fits the requirements.
 */
public String resetReportedProperty(Map<String, Object> propertyMap) throws IllegalArgumentException
```

**SRS_TWINPARSER_21_130: [**The resetReportedProperty shall cleanup the reported collection and add all provided properties to the Reported property.**]**  
**SRS_TWINPARSER_21_131: [**The resetReportedProperty shall return a string with json representing the added reported properties.**]**  
**SRS_TWINPARSER_21_132: [**If the provided `propertyMap` is null, the resetReportedProperty shall not change the collection and throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_133: [**The `key` and `value` in property shall be case sensitive.**]**  
**SRS_TWINPARSER_21_134: [**If the provided `propertyMap` is empty, the resetReportedProperty shall cleanup the reported collection and return `{}`.**]**  
**SRS_TWINPARSER_21_135: [**If the map is invalid, the resetReportedProperty shall not change the collection and throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_139: [**If any `value` is null, the resetReportedProperty shall delete it from the collection and report on Json.**]**  


### resetTags

```java
/**
 * Reset the `tags` information in the collection, deleting all old tags and add all the new provided ones.
 * Return a string with a json that contains a sub-collection of added tags.
 *
 * @param tagsMap - Map of `tags` to change the collection.
 * @return Json with added tags
 * @throws IllegalArgumentException This exception is thrown if the tags in the map do not fits the requirements.
 * @throws IOException This exception is thrown if tags the is not enabled.
 */
public String resetTags(Map<String, Object> tagsMap) throws IllegalArgumentException, IOException
```

**SRS_TWINPARSER_21_140: [**The resetTags shall cleanup the tags collection and add all provided tags to the tags.**]**  
**SRS_TWINPARSER_21_141: [**The resetTags shall return a string with json representing the added tags.**]**  
**SRS_TWINPARSER_21_142: [**If the provided `tagsMap` is null, the resetTags shall not change the collection and throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_143: [**The `key` and `value` in tags shall be case sensitive.**]**  
**SRS_TWINPARSER_21_144: [**If the provided `tagsMap` is empty, the resetTags shall cleanup the tags collection and return `{}`.**]**  
**SRS_TWINPARSER_21_145: [**If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_146: [**If Tags is not enable, the resetTags shall throw IOException.**]**  
**SRS_TWINPARSER_21_149: [**If any `value` is null, the resetTags shall delete it from the collection and report on Json.**]**  


### updateDesiredProperty

```java
/**
 * Update the `desired` properties information in the collection, using the information parsed from the provided json.
 * It will fire a callback if any property was added, excluded, or had its value updated.
 *
 * @param json - Json with `desired` property to change the collection.
 * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
 */
public void updateDesiredProperty(String json) throws IllegalArgumentException
```

**SRS_TWINPARSER_21_029: [**The updateDesiredProperty shall update the Desired property using the information provided in the json.**]**  
**SRS_TWINPARSER_21_030: [**The updateDesiredProperty shall generate a map with all pairs key value that had its content changed.**]**  
**SRS_TWINPARSER_21_031: [**The updateDesiredProperty shall send the map with all changed pairs to the upper layer calling onDesiredCallback (TwinChangedCallback).**]**  
**SRS_TWINPARSER_21_032: [**If the OnDesiredCallback is set as null, the updateDesiredProperty shall discard the map with the changed pairs.**]**  
**SRS_TWINPARSER_21_033: [**If there is no change in the Desired property, the updateDesiredProperty shall not change the collection and not call the OnDesiredCallback.**]**  
**SRS_TWINPARSER_21_065: [**If the provided json is empty, the updateDesiredProperty shall not change the collection and not call the OnDesiredCallback.**]**  
**SRS_TWINPARSER_21_066: [**If the provided json is null, the updateDesiredProperty shall not change the collection, not call the OnDesiredCallback, and  throws IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_092: [**If the provided json is not valid, the updateDesiredProperty shall throws IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_096: [**If the provided json have any duplicated `key`, the updateDesiredProperty shall throws IllegalArgumentException.**]**  


### updateReportedProperty

```java
/**
 * Update the `reported` properties information in the collection, using the information parsed from the provided json.
 * It will fire a callback if any property was added, excluded, or had its value updated.
 *
 * @param json - Json with `reported` property to change the collection.
 * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
 */
public void updateReportedProperty(String json) throws IllegalArgumentException
```

**SRS_TWINPARSER_21_034: [**The updateReportedProperty shall update the Reported property using the information provided in the json.**]**  
**SRS_TWINPARSER_21_035: [**The updateReportedProperty shall generate a map with all pairs key value that had its content changed.**]**  
**SRS_TWINPARSER_21_036: [**The updateReportedProperty shall send the map with all changed pairs to the upper layer calling onReportedCallback (TwinChangedCallback).**]**  
**SRS_TWINPARSER_21_037: [**If the OnReportedCallback is set as null, the updateReportedProperty shall discard the map with the changed pairs.**]**  
**SRS_TWINPARSER_21_038: [**If there is no change in the Reported property, the updateReportedProperty shall not change the collection and not call the OnReportedCallback.**]**  
**SRS_TWINPARSER_21_067: [**If the provided json is empty, the updateReportedProperty shall not change the collection and not call the OnReportedCallback.**]**  
**SRS_TWINPARSER_21_068: [**If the provided json is null, the updateReportedProperty shall not change the collection, not call the OnReportedCallback, and throws IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_093: [**If the provided json is not valid, the updateReportedProperty shall throws IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_095: [**If the provided json have any duplicated `key`, the updateReportedProperty shall throws IllegalArgumentException.**]**  


### updateTwin

```java
/**
 * Update the properties information in the collection, using the information parsed from the provided json.
 * It will fire a callback if any property was added, excluded, or had its value updated.
 *
 * @param json - Json with property to change the collection.
 * @throws IllegalArgumentException This exception is thrown if the Json is not well formed.
 */
public void updateTwin(String json) throws IllegalArgumentException
```

**SRS_TWINPARSER_21_039: [**The updateTwin shall fill the fields the properties in the TwinParser class with the keys and values provided in the json string.**]**  
**SRS_TWINPARSER_21_040: [**The updateTwin shall not change fields that is not reported in the json string.**]**  
**SRS_TWINPARSER_21_041: [**The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.**]**  
**SRS_TWINPARSER_21_042: [**If a valid key has a null value, the updateTwin shall delete this property.**]**  
**SRS_TWINPARSER_21_043: [**If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_044: [**If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.**]**  
**SRS_TWINPARSER_21_045: [**If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.**]**  
**SRS_TWINPARSER_21_046: [**If OnDesiredCallback was not provided, the updateTwin shall not do anything with the list of updated desired properties.**]**  
**SRS_TWINPARSER_21_047: [**If OnReportedCallback was not provided, the updateTwin shall not do anything with the list of updated reported properties.**]**  
**SRS_TWINPARSER_21_069: [**If there is no change in the Desired property, the updateTwin shall not change the reported collection and not call the OnReportedCallback.**]**  
**SRS_TWINPARSER_21_070: [**If there is no change in the Reported property, the updateTwin shall not change the reported collection and not call the OnReportedCallback.**]**  
**SRS_TWINPARSER_21_071: [**If the provided json is empty, the updateTwin shall not change the collection and not call the OnDesiredCallback or the OnReportedCallback.**]**  
**SRS_TWINPARSER_21_072: [**If the provided json is null, the updateTwin shall not change the collection, not call the OnDesiredCallback or the OnReportedCallback, and throws IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_089: [**If the provided json contains `desired` or `reported` in its first level, the updateTwin shall parser the json as properties only.**]**  
**SRS_TWINPARSER_21_090: [**If the provided json is properties only and contains other tag different than `desired` or `reported`, the updateTwin shall throws IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_091: [**If the provided json is NOT properties only and contains `desired` or `reported` in its first level, the updateTwin shall throws IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_094: [**If the provided json have any duplicated `key`, the updateTwin shall use the content of the last one in the String.**]**  
**SRS_TWINPARSER_21_097: [**If the provided json have any duplicated `properties` or `tags`, the updateTwin shall throw IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_098: [**If the provided json is properties only and contains duplicated `desired` or `reported`, the updateTwin shall throws IllegalArgumentException.**]**  
**SRS_TWINPARSER_21_172: [**If the provided json contains `deviceId`, `generationId`, `etag`, `status`, `statusReason`, `statusUpdatedTime`, `connectionState`, `connectionStateUpdatedTime`, `lastActivityTime`, and `lastAcceptingIpFilterRule`, the updateTwin shall store its value.**]**  


### getDesiredPropertyVersion
```java
/**
 * Return the `desired` property version.
 *
 * @return Integer that contains the `desired` property version (it can be null).
 */
public Integer getDesiredPropertyVersion()
```
**SRS_TWINPARSER_21_048: [**The getDesiredPropertyVersion shall return the desired property version.**]**  

### getReportedPropertyVersion
```java
/**
 * Return the `reported` property version.
 *
 * @return Integer that contains the `reported` property version (it can be null).
 */
public Integer getReportedPropertyVersion()
```
**SRS_TWINPARSER_21_049: [**The getReportedPropertyVersion shall return the reported property version.**]**  

### getDesiredPropertyMap
```java
/**
 * Return a map with all `desired` properties in the collection.
 *
 * @return A map with all `desired` properties in the collection (it can be null).
 */
public Map<String, Object> getDesiredPropertyMap()
```
**SRS_TWINPARSER_21_050: [**The getDesiredPropertyMap shall return a map with all desired property key value pairs.**]**  

### getReportedPropertyMap
```java
/**
 * Return a map with all `reported` properties in the collection.
 *
 * @return A map with all `reported` properties in the collection (it can be null).
 */
public Map<String, Object> getReportedPropertyMap()
```
**SRS_TWINPARSER_21_051: [**The getReportedPropertyMap shall return a map with all reported property key value pairs.**]**  

### getTagsMap
```java
/**
 * Return a map with all `tags` in the collection.
 *
 * @return A map with all `tags` in the collection (it can be null).
 * @throws IOException This exception is thrown if tags the is not enabled.
 */
public Map<String, Object> getTagsMap() throws IOException
```
**SRS_TWINPARSER_21_052: [**The getTagsMap shall return a map with all tags in the collection.**]**  
**SRS_TWINPARSER_21_074: [**If Tags is not enable, the getTagsMap shall throw IOException.**]**  

### getDeviceId
```java
/**
 * Getter for device name
 *
 * @return Device name
 * A case-sensitive string (up to 128 char long)
 * of ASCII 7-bit alphanumeric chars
 * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
 */
public String getDeviceId()
```
**SRS_TWINPARSER_21_112: [**The `getDeviceId` shall return the device name.**]**  


### getGenerationId
```java
/**
 * Getter for device generation name
 *
 * @return Device generation Id
 */
public String getGenerationId()
```
**SRS_TWINPARSER_21_150: [**The `getGenerationId` shall return the device generation name.**]**  


### getETag
```java
/**
 * Getter for ETag
 *
 * @return A string representing a weak ETAG version
 * of this JSON description. This is a hash.
 */
public String getETag()
```
**SRS_TWINPARSER_21_113: [**The `getETag` shall return the string representing a weak ETAG version.**]**  


### getVersion
```java
/**
 * Getter for Twin version
 *
 * @return A Integer representing a twin tags and properties version in the JSON. It can be {@code null}
 */
public Integer getVersion()
```
**SRS_TWINPARSER_21_173: [**The `getVersion` shall return the Integer representing a twin version.**]**  


### getStatus
```java
/**
 * Getter for device status
 *
 * @return "enabled", "disabled".
 * If "enabled", this device is authorized to connect.
 * If "disabled" this device cannot receive or send messages, and statusReason must be set.
 */
public TwinStatus getStatus()
```
**SRS_TWINPARSER_21_136: [**The `getStatus` shall return the device status.**]**  


### getStatusReason
```java
/**
 * Getter for status reason
 *
 * @return A 128 char long string storing the reason of suspension.
 * (all UTF-8 chars allowed).
 */
public String getStatusReason()
```
**SRS_TWINPARSER_21_137: [**The `getStatusReason` shall return the device status reason.**]**  


### getStatusUpdatedTime
```java
/**
 * Getter for status updated date and time
 *
 * @return Datetime of last time the state was updated.
 */
public String getStatusUpdatedTime()
```
**SRS_TWINPARSER_21_138: [**The `getStatusUpdatedTime` shall return the device status update date and time.**]**  


### getConnectionState
```java
/**
 * Getter for connection state
 *
 * @return The connectionState string
 */
public DeviceConnectionState getConnectionState()
```
**SRS_TWINPARSER_21_147: [**The `getConnectionState` shall return the connection state.**]**  


### getConnectionStateUpdatedTime
```java
/**
 * Getter for connection state updated date and time
 *
 * @return The string containing the time when the connectionState parameter was updated
 */
public String getConnectionStateUpdatedTime()
```
**SRS_TWINPARSER_21_148: [**The `getConnectionStateUpdatedTime` shall return the connection state update date and time.**]**  


### getLastActivityTime
```java
/**
 * Getter for last activity time
 *
 * @return The string containing the time when the lastActivity parameter was updated
 */
public String getLastActivityTime()
```
**SRS_TWINPARSER_21_151: [**The `getLastActivityTime` shall return the last activity date and time.**]**  


### setDeviceId
```java
/**
 * Setter for device name
 *
 * @param deviceId device id
 * A case-sensitive string (up to 128 char long)
 * of ASCII 7-bit alphanumeric chars
 * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}
 * @throws IllegalArgumentException if the provided device Id is not valid.
 */
public void setDeviceId(String deviceId) throws IllegalArgumentException;
```
**SRS_TWINPARSER_21_168: [**The `setDeviceId` shall set the deviceId in the twin collection.**]**  
**SRS_TWINPARSER_21_169: [**If the deviceId is empty, null, or not valid, the `setDeviceId` shall throw IllegalArgumentException.**]**  

### setETag
```java
/**
 * Setter for ETag.
 * 
 * @param eTag is a string representing a weak ETAG version
 * of this JSON description. This is a hash.
 * @throws IllegalArgumentException if the provided eTag Id is not valid.
 */
public void setETag(String eTag) throws IllegalArgumentException;
```
**SRS_TWINPARSER_21_170: [**The `setETag` shall set the ETag in the twin collection.**]**  
**SRS_TWINPARSER_21_171: [**If the ETag is empty, null, or not valid, the `setETag` shall throw IllegalArgumentException.**]**  

