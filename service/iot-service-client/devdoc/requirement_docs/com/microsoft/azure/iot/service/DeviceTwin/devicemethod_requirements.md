# DeviceMethod Requirements

## Overview

DeviceMethod enables service client to directly invoke methods on various devices from service client.

## References

[Understand and invoke direct methods from IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-direct-methods)

## Exposed API


```java
/**
 * DeviceMethod enables service client to directly invoke methods on various devices from service client.
 */
public class DeviceMethod 
{
    public static DeviceMethod createFromConnectionString(String connectionString) throws Exception;
    public MethodResult Invoke(String deviceId, String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload) throws IotHubException, IOException;
}
```

### createFromConnectionString
```java
/**
 * Create a DeviceMethod instance from the information in the connection string.
 *      
 * @param connectionString is a Azure IoTHub connection string.
 * @return an instance of the DeviceMethod.
 * @throws Exception This exception is thrown if the object creation failed
 */
public static DeviceMethod createFromConnectionString(String connectionString) throws Exception;
```
**SRS_DEVICEMETHOD_21_001: [**The constructor shall throw IllegalArgumentException if the input string is null or empty.**]**  
**SRS_DEVICEMETHOD_21_002: [**The constructor shall create an IotHubConnectionStringBuilder object from the given connection string.**]**  
**SRS_DEVICEMETHOD_21_003: [**The constructor shall create a new DeviceMethod instance and return it.**]**  

### invoke
```java
/**
 * Directly invokes a method on the device and return its result.
 * 
 * @param deviceId is the device identification.
 * @param methodName is the name of the method that shall be invoked on the device.
 * @param responseTimeoutInSeconds is the maximum waiting time for a response from the device in seconds.
 * @param connectTimeoutInSeconds is the maximum waiting time for a response from the connection in seconds.
 * @param payload is the the method parameter
 * @return the status and payload resulted from the method invoke
 * @throws IotHubException This exception is thrown if the response verification failed
 * @throws IOException This exception is thrown if the IO operation failed
 */
public MethodResult invoke(String deviceId, String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload) throws IotHubException, IOException;
```
**SRS_DEVICEMETHOD_21_004: [**The invoke shall throw IllegalArgumentException if the provided deviceId is null or empty.**]**  
**SRS_DEVICEMETHOD_21_005: [**The invoke shall throw IllegalArgumentException if the provided methodName is null, empty, or not valid.**]**  
**SRS_DEVICEMETHOD_21_006: [**The invoke shall throw IllegalArgumentException if the provided responseTimeoutInSeconds is negative.**]**  
**SRS_DEVICEMETHOD_21_007: [**The invoke shall throw IllegalArgumentException if the provided connectTimeoutInSeconds is negative.**]**  
**SRS_DEVICEMETHOD_21_008: [**The invoke shall build the Method URL `{iot hub}/twins/{device id}/methods/` by calling getUrlMethod.**]**  
**SRS_DEVICEMETHOD_21_009: [**The invoke shall send the created request and get the response using the HttpRequester.**]**  
**SRS_DEVICEMETHOD_21_010: [**The invoke shall create a new HttpRequest with http method as `POST`.**]**  
**SRS_DEVICEMETHOD_21_011: [**The invoke shall add a HTTP body with Json created by the `serializer.Method`.**]**  
**SRS_DEVICEMETHOD_21_012: [**If `Method` return a null Json, the invoke shall throw IllegalArgumentException.**]**    
**SRS_DEVICEMETHOD_21_013: [**The invoke shall deserialize the payload using the `serializer.Method`.**]**  
**SRS_DEVICEMETHOD_21_014: [**The invoke shall bypass the Exception if one of the functions called by invoke failed.**]**  
**SRS_DEVICEMETHOD_21_015: [**If the HttpStatus represents success, the invoke shall return the status and payload using the `MethodResult` class.**]**  
