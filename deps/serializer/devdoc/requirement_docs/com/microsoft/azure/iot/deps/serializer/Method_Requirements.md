# Method Requirements

## Overview

Method is representation of a single Direct Method access collection with a Json serializer and deserializer.

## References

[Direct methods](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-direct-methods)

## Exposed API

```java
/**
 * Representation of a single Direct Method Access collection with a Json serializer and deserializer.
 */
public class Method
{
    public Method();
    public Method(String name, Long responseTimeout, Long connectTimeout, Object payload) throws IllegalArgumentException;
    public Method(Object payload);

    public synchronized void fromJson(String json) throws IllegalArgumentException;
    
    public Integer getStatus() throws IllegalArgumentException;
    public Object getPayload();
    
    public String toJson();
}
```

```java
/**
 * CONSTRUCTOR
 * Create a Method instance with provided values.
 */
public Method();
```
**SRS_METHOD_21_029: [**The constructor shall create an instance of the method.**]**  
**SRS_METHOD_21_030: [**The constructor shall initialize all data in the collection as null.**]**  
**SRS_METHOD_21_022: [**The constructor shall initialize the method operation as `none`.**]**  

### Method
```java
/**
 * CONSTRUCTOR
 * Create a Method instance with provided values.
 *
 * @param name - method name [required].
 * @param responseTimeout - maximum interval of time, in seconds, that the Direct Method will wait for answer. It can be {@code null}.
 * @param connectTimeout - maximum interval of time, in seconds, that the Direct Method will wait for the connection. It can be {@code null}.
 * @param payload - Object that contains the payload defined by the user. It can be {@code null}.
 * @throws IllegalArgumentException This exception is thrown if the one of the provided information do not fits the requirements.
 */
public Method(String name, Long responseTimeout, Long connectTimeout, Object payload) throws IllegalArgumentException;
```
**SRS_METHOD_21_001: [**The constructor shall create an instance of the method.**]**  
**SRS_METHOD_21_002: [**The constructor shall update the method collection using the provided information.**]**  
**SRS_METHOD_21_023: [**The constructor shall initialize the method operation as `invoke`.**]**  
**SRS_METHOD_21_003: [**All Strings are case sensitive.**]**  
**SRS_METHOD_21_004: [**If the `name` is null, empty, contains more than 128 chars, or illegal char (`$`, `.`, space), the constructor shall throw IllegalArgumentException.**]**  
**SRS_METHOD_21_005: [**If the responseTimeout is a negative number, the constructor shall throw IllegalArgumentException.**]**  
**SRS_METHOD_21_033: [**If the connectTimeout is a negative number, the constructor shall throw IllegalArgumentException.**]**  


```java
/**
 * CONSTRUCTOR
 * Create a Method instance with provided values.
 *
 * @param payload - Object that contains the payload defined by the user. It can be {@code null}.
 */
public Method(Object payload);
```
**SRS_METHOD_21_020: [**The constructor shall create an instance of the method.**]**  
**SRS_METHOD_21_021: [**The constructor shall update the method collection using the provided information.**]**  
**SRS_METHOD_21_034: [**The constructor shall set the method operation as `payload`.**]**  


### fromJson
```java
/**
 * Set the Method collection with the provided information in the json.
 *
 * @param json - Json with the information to change the collection.
 *                  - If contains `methodName`, it is a full method including `methodName`, `responseTimeoutInSeconds`, `connectTimeoutInSeconds`, and `payload`.
 *                  - If contains `status`, it is a response with `status` and `payload`.
 *                  - Otherwise, it is only `payload`.
 * @throws IllegalArgumentException This exception is thrown if the one of the provided information do not fits the requirements.
 */
public synchronized void fromJson(String json) throws IllegalArgumentException;
```
**SRS_METHOD_21_006: [**The fromJson shall parse the json and fill the method collection.**]**  
**SRS_METHOD_21_007: [**The json can contain values `null`, `"null"`, and `""`, which represents null, the string null, and empty string respectively.**]**  
**SRS_METHOD_21_008: [**If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.**]**  
**SRS_METHOD_21_009: [**If the json contains the `methodName` identification, the fromJson shall parse the full method, and set the operation as `invoke`.**]**  
Ex:
```json
{
    "methodName": "reboot",
    "responseTimeoutInSeconds": 200,
    "connectTimeoutInSeconds": 5,
    "payload": 
    {
        "input1": "someInput",
        "input2": "anotherInput"
    }
}
```
**SRS_METHOD_21_010: [**If the json contains any payload without `methodName` or `status` identification, the fromJson shall parse only the payload, and set the operation as `payload`**]**  
Ex:
```json
{
    "input1": "someInput",
    "input2": "anotherInput"
}
```
**SRS_METHOD_21_011: [**If the json contains the `status` identification, the fromJson shall parse both status and payload, and set the operation as `response`.**]**  
Ex:
```json
{
    "status": 201,
    "payload": {"AnyValidPayload" : "" }
}
```

### getStatus
```java
/**
 * Return an Integer with the response status.
 *
 * @return An integer with the status of the response. It can be {@code null}. 
 * @throws IllegalArgumentException This exception is thrown if the operation is not type of `response`.
 */
public Integer getStatus() throws IllegalArgumentException
```
**SRS_METHOD_21_012: [**The getStatus shall return an Integer with the status in the parsed json.**]**  
**SRS_METHOD_21_035: [**If the operation is not `response`, the getStatus shall throws IllegalArgumentException.**]**  


### getPayload
```java
/**
 * Return an Object with the payload.
 *
 * @return An Object with the payload. It can be {@code null}. 
 */
public Object getPayload()
```
**SRS_METHOD_21_013: [**The getPayload shall return an Object with the Payload in the parsed json.**]**  


### toJson
```java
/**
 * Create a String with a json content that represents all the information in the method collection.
 *
 * @return String with the json content.
 * @throws IllegalArgumentException This exception is thrown if the one of the provided information do not fits the requirements.
 */
public String toJson()
```
**SRS_METHOD_21_014: [**The toJson shall create a String with the full information in the method collection using json format.**]**  
**SRS_METHOD_21_015: [**The toJson shall include name as `methodName` in the json.**]**  
**SRS_METHOD_21_016: [**The toJson shall include responseTimeout in seconds as `responseTimeoutInSeconds` in the json.**]**  
**SRS_METHOD_21_017: [**If the responseTimeout is null, the toJson shall not include the `responseTimeoutInSeconds` in the json.**]**  
**SRS_METHOD_21_031: [**The toJson shall include connectTimeout in seconds as `connectTimeoutInSeconds` in the json.**]**  
**SRS_METHOD_21_032: [**If the connectTimeout is null, the toJson shall not include the `connectTimeoutInSeconds` in the json.**]**  
**SRS_METHOD_21_018: [**The class toJson include payload as `payload` in the json.**]**  
**SRS_METHOD_21_019: [**If the payload is null, the toJson shall include `payload` with value `null`.**]**  
**SRS_METHOD_21_024: [**The class toJson include status as `status` in the json.**]**  
**SRS_METHOD_21_025: [**If the status is null, the toJson shall include `status` as `null`.**]**  
**SRS_METHOD_21_026: [**If the method operation is `invoke`, the toJson shall include the full method information in the json.**]**  
Ex:
```json
{
    "methodName": "reboot",
    "responseTimeoutInSeconds": 200,
    "connectTimeoutInSeconds": 5,
    "payload": {
        "input1": "someInput",
        "input2": "anotherInput"
    }
}
```
**SRS_METHOD_21_027: [**If the method operation is `response`, the toJson shall parse both status and payload.**]**  
Ex:
```json
{
    "status": 201,
    "payload": {"AnyValidPayload" : "" }
}
```
**SRS_METHOD_21_028: [**If the method operation is `payload`, the toJson shall parse only the payload.**]**  
Ex:
```json
{
    "input1": "someInput",
    "input2": "anotherInput"
}
```
**SRS_METHOD_21_036: [**If the method operation is `none`, the toJson shall throw IllegalArgumentException.**]**  
