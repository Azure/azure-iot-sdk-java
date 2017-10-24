# Util helpers for the serializer

## Overview

Set of static functions to help the serializer.

## References

[Azure IoT Hub developer guide](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide)

## Exposed API

```java
/**
 * Set of static functions to help the serializer.
 */
class ParserUtility
{
    public static void validateStringUTF8(String str) throws IllegalArgumentException;
    public static void validateObject(object val) throws IllegalArgumentException;
    public static void validateBoolean(Boolean condition) throws IllegalArgumentException;
    public static void validateKey(String key, boolean isMetadata) throws IllegalArgumentException;
    public static void validateMap(Map<String, Object> map, int maxLevel, boolean allowMetadata) throws IllegalArgumentException;
    public static void validateId(String id) throws IllegalArgumentException;
    public static void validateHostName(String hostName) throws IllegalArgumentException;
    
    public static void validateBlobName(String blobName) throws IllegalArgumentException;
    protected static void validateQuery(String query) throws IllegalArgumentException;
    
    public static Date getDateTimeUtc(String dataTime) throws IllegalArgumentException;
    public static Date stringToDateTimeOffset(String dataTime) throws IllegalArgumentException;
    public static String dateTimeUtcToString(Date date);
    protected static JsonElement mapToJsonElement(Map<String, Object> map);
}
```

### validateStringUTF8
```java
/**
 * Helper to validate if the provided string is not null, empty, and all characters are UTF-8.
 *
 * @param str is the string to be validated.
 * @throws IllegalArgumentException if the string do not fit the criteria.
 */
protected static void validateStringUTF8(String str) throws IllegalArgumentException
```
**SRS_PARSER_UTILITY_21_001: [**The validateStringUTF8 shall do nothing if the string is valid.**]**  
**SRS_PARSER_UTILITY_21_002: [**The validateStringUTF8 shall throw IllegalArgumentException if the provided string is null or empty.**]**  
**SRS_PARSER_UTILITY_21_003: [**The validateStringUTF8 shall throw IllegalArgumentException if the provided string contains at least one not UTF-8 character.**]**  

### validateBlobName
```java
/**
 * Helper to validate if the provided blob name is not null, empty, and valid.
 *
 * @param blobName is the blob name to be validated.
 * @throws IllegalArgumentException if the blob name do not fit the criteria.
 */
public static void validateBlobName(String blobName) throws IllegalArgumentException
```
**SRS_PARSER_UTILITY_21_004: [**The validateBlobName shall do nothing if the string is valid.**]**  
**SRS_PARSER_UTILITY_21_005: [**The validateBlobName shall throw IllegalArgumentException if the provided blob name is null or empty.**]**  
**SRS_PARSER_UTILITY_21_006: [**The validateBlobName shall throw IllegalArgumentException if the provided blob name contains at least one not UTF-8 character.**]**  
**SRS_PARSER_UTILITY_21_007: [**The validateBlobName shall throw IllegalArgumentException if the provided blob name contains more than 1024 characters.**]**  
**SRS_PARSER_UTILITY_21_008: [**The validateBlobName shall throw IllegalArgumentException if the provided blob name contains more than 254 path segments.**]**  

### validateQuery
```java
/**
 * Helper to validate if the provided blob name is not null, empty, and valid.
 *
 * @param blobName is the blob name to be validated.
 * @throws IllegalArgumentException if the blob name do not fit the criteria.
 */
protected static void validateQuery(String query) throws IllegalArgumentException;
```
**SRS_PARSER_UTILITY_25_031: [**The validateQuery shall do nothing if the string is valid.**]**  
**SRS_PARSER_UTILITY_25_032: [**The validateQuery shall throw IllegalArgumentException is the provided query is null or empty.**]**  
**SRS_PARSER_UTILITY_25_033: [**The validateQuery shall throw IllegalArgumentException is the provided query contains non UTF-8 character.**]**  
**SRS_PARSER_UTILITY_25_034: [**The validateQuery shall throw IllegalArgumentException is the provided query does not contain SELECT and FROM.**]**  

### validateObject
```java
/**
 * Helper to validate if the provided object is not null.
 *
 * @param val is the object to be validated.
 * @throws IllegalArgumentException if the object do not fit the criteria.
 */
protected static void validateObject(object val) throws IllegalArgumentException
```
**SRS_PARSER_UTILITY_21_009: [**The validateObject shall do nothing if the object is valid.**]**  
**SRS_PARSER_UTILITY_21_010: [**The validateObject shall throw IllegalArgumentException if the provided object is null.**]**  

### validateKey
```java
/**
 * Helper to validate if the provided string is a valid json key.
 *
 * @param key is the string to be validated.
 * @param isMetadata defines if the key belongs to a metadata, which allows character `$`.
 * @throws IllegalArgumentException if the string do not fit the criteria.
 */
protected static void validateKey(String key, boolean isMetadata) throws IllegalArgumentException
```
**SRS_PARSER_UTILITY_21_013: [**The validateKey shall do nothing if the string is a valid key.**]**  
**SRS_PARSER_UTILITY_21_014: [**The validateKey shall throw IllegalArgumentException if the provided string is null or empty.**]**  
**SRS_PARSER_UTILITY_21_015: [**The validateKey shall throw IllegalArgumentException if the provided string contains at least one not UTF-8 character.**]**  
**SRS_PARSER_UTILITY_21_016: [**The validateKey shall throw IllegalArgumentException if the provided string contains more than 128 characters.**]**  
**SRS_PARSER_UTILITY_21_017: [**The validateKey shall throw IllegalArgumentException if the provided string contains an illegal character (`$`,`.`, space).**]**  
**SRS_PARSER_UTILITY_21_018: [**If `isMetadata` is `true`, the validateKey shall accept the character `$` as valid. **]**  
**SRS_PARSER_UTILITY_21_019: [**If `isMetadata` is `false`, the validateKey shall not accept the character `$` as valid. **]**  

### validateMap
```java
/**
 * Helper to validate if the provided map in terms of maximum 
 * levels and optionally if the keys ar not metadata.
 * 
 * @param map the {@code Map} to be validate. It can be {@code null}, and it will succeed in this case.
 * @param maxLevel the max number of level allowed in the map.
 * @param allowMetadata the {@code boolean} that indicates if the key can contain metadata `$` or not.
 * @throws IllegalArgumentException If the Map contains more than maxLevel levels or do not allow metadata 
 *                                  but contains metadata key.
 */
public static void validateMap(Map<String, Object> map, int maxLevel, boolean allowMetadata) throws IllegalArgumentException
```
**SRS_PARSER_UTILITY_21_046: [**The validateMap shall throws IllegalArgumentException if the maxLevel is `0` or negative.**]**  
**SRS_PARSER_UTILITY_21_047: [**The validateMap shall do nothing if the map is a valid Map.**]**  
**SRS_PARSER_UTILITY_21_048: [**The validateMap shall do nothing if the map is null.**]**  
**SRS_PARSER_UTILITY_21_049: [**The validateMap shall throws IllegalArgumentException if any key in the map is null, empty, contains more than 128 characters, or illegal characters (`$`,`.`, space).**]**  
**SRS_PARSER_UTILITY_21_050: [**If `isMetadata` is `true`, the validateMap shall accept the character `$` in the key.**]**  
**SRS_PARSER_UTILITY_21_051: [**The validateMap shall throws IllegalArgumentException if any value is null, or contains illegal type (array or invalid class).**]**  
**SRS_PARSER_UTILITY_21_052: [**The validateMap shall throws IllegalArgumentException if the provided map contains more than maxLevel levels.**]**  

### validateId
```java
/**
 * Validate if a provided ID is valid using the follow criteria.
 * A case-sensitive string (up to 128 char long)
 * of ASCII 7-bit alphanumeric chars
 * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
 *
 * @param id is the ID to test
 * @throws IllegalArgumentException if the ID do not fits the criteria
 */
protected static void validateId(String id) throws IllegalArgumentException
```
**SRS_PARSER_UTILITY_21_026: [**The validateKey shall throw IllegalArgumentException if the provided string is null or empty.**]**  
**SRS_PARSER_UTILITY_21_027: [**The validateKey shall throw IllegalArgumentException if the provided string contains at least one not UTF-8 character.**]**  
**SRS_PARSER_UTILITY_21_028: [**The validateId shall throw IllegalArgumentException if the provided string contains more than 128 characters.**]**  
**SRS_PARSER_UTILITY_21_029: [**The validateId shall throw IllegalArgumentException if the provided string contains an illegal character.**]**  
**SRS_PARSER_UTILITY_21_030: [**The validateId shall do nothing if the string is a valid ID.**]**  

### validateHostName
```java
/**
 * Validate if a provided host name is valid using the follow criteria.
 * A case-sensitive string (up to 128 char long)
 *   of ASCII 7-bit alphanumeric chars
 *   + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
 * Contains at least one separator '.'
 *
 * @param hostName is the host name to test
 * @throws IllegalArgumentException if the provided host name do not fits the criteria
 */
public static void validateHostName(String hostName) throws IllegalArgumentException
```
**SRS_PARSER_UTILITY_21_044: [**The validateHostName shall throw IllegalArgumentException if the provided string is not a valid host name.**]**  
**SRS_PARSER_UTILITY_21_045: [**The validateHostName shall do nothing if the string is a valid host name.**]**  

### getDateTimeUtc
```java
/**
 * Helper to convert the provided string in a UTC Date.
 * Expected format:
 *      "2016-06-01T21:22:43.7996883Z"
 *
 * @param dataTime is the string with the date and time
 * @return Date parsed from the string
 * @throws IllegalArgumentException if the date and time in the string is not in the correct format.
 */
protected static Date getDateTimeUtc(String dataTime) throws IllegalArgumentException
```
**SRS_PARSER_UTILITY_21_020: [**The getDateTimeUtc shall parse the provide string using `UTC` timezone.**]**  
**SRS_PARSER_UTILITY_21_021: [**The getDateTimeUtc shall parse the provide string using the data format `yyyy-MM-dd'T'HH:mm:ss'`.**]**
**SRS_PARSER_UTILITY_21_022: [**If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.**]**  
**SRS_PARSER_UTILITY_21_040: [**If the provide string contains more than 3 digits for milliseconds, the getDateTimeUtc shall reduce the milliseconds to 3 digits.**]**  
**SRS_PARSER_UTILITY_21_041: [**The getDateTimeUtc shall accept date without milliseconds.**]**  

### stringToDateTimeOffset
```java
/**
 * Helper to convert the provided string in a offset Date.
 * Expected format:
 *      "2016-06-01T21:22:41+00:00"
 *
 * @param dataTime is the string with the date and time
 * @return Date parsed from the string
 * @throws IllegalArgumentException if the date and time in the string is not in the correct format.
 */
protected static Date stringToDateTimeOffset(String dataTime) throws IllegalArgumentException
```
**SRS_PARSER_UTILITY_21_023: [**The stringToDateTimeOffset shall parse the provide string using `UTC` timezone.**]**  
**SRS_PARSER_UTILITY_21_024: [**The stringToDateTimeOffset shall parse the provide string using the data format `2016-06-01T21:22:41+00:00`.**]**  
**SRS_PARSER_UTILITY_21_025: [**If the provide string is null, empty or contains an invalid data format, the stringToDateTimeOffset shall throw IllegalArgumentException.**]**

### dateTimeUtcToString
```java
/**
 * Helper to convert the provided Date UTC into String.
 * Expected result:
 *      "2016-06-01T21:22:43.799Z"
 *
 * @param date is the {@code Date} with the date and time
 * @return the {@code String} with the date and time using the UTC format.
 */
public static String dateTimeUtcToString(Date date)
```
**SRS_PARSER_UTILITY_21_053: [**The dateTimeUtcToString shall throws IllegalArgumentException if the provided Date is null.**]**  
**SRS_PARSER_UTILITY_21_054: [**The dateTimeUtcToString shall serialize the provide Date using `UTC` timezone.**]**  

```java
/**
 * Convert from a date object back into a string representation
 * Expected format of returned string:
 *      "2016-01-21T11:05:21"
 *
 * @param date the date to convert into a string
 * @return the date represented as a string
 */
public static String getSimpleDateStringFromDate(Date date) throws IllegalArgumentException
```
**SRS_PARSER_UTILITY_34_042: [**If the provided date is null, an IllegalArgumentException shall be thrown.**]**
**SRS_PARSER_UTILITY_34_043: [**The provided date shall be converted into this format: "yyyy-MM-dd'T'HH:mm:ss".**]**


### mapToJsonElement
```java
/**
 * Helper to convert a provided map in to a JsonElement, including sub-maps.
 *
 * @param map is the map to serialize
 * @return a JsonElement that represents the content of the map.
 * @throws IllegalArgumentException if the provided map is null.
 */
protected static JsonElement mapToJsonElement(Map<String, Object> map) throws IllegalArgumentException
```
**SRS_PARSER_UTILITY_21_035: [**The mapToJsonElement shall serialize the provided map into a JsonElement.**]**  
**SRS_PARSER_UTILITY_21_036: [**The mapToJsonElement shall include keys with null values in the JsonElement.**]**  
**SRS_PARSER_UTILITY_21_037: [**If the value is a map, the mapToJsonElement shall include it as a submap in the JsonElement.**]**  
**SRS_PARSER_UTILITY_21_038: [**If the map is empty, the mapToJsonElement shall return a empty JsonElement.**]**  
**SRS_PARSER_UTILITY_21_039: [**If the map is null, the mapToJsonElement shall throw IllegalArgumentException.**]**  
