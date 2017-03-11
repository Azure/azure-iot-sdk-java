# DeviceMethod Requirements

## Overview

DeviceMethod enables service client to directly invoke methods from various devices.

## References

[Understand and invoke direct methods from IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-direct-methods)

## Exposed API


```java
/**
 * DeviceMethod enables service client to directly invoke methods from various devices.
 */
public class DeviceMethod 
{
    public static DeviceMethod createFromConnectionString(String connectionString) throws Exception;
    public Object Invoke(String deviceId, String methodName, Long timeout, Object payload) throws IotHubException, IOException;
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
 * @param timeout is the maximum waiting time for a response from the device.
 * @param payload is the the method parameter
 * @return the returned values from the method in the device.
 * @throws IotHubException This exception is thrown if the response verification failed
 * @throws IOException This exception is thrown if the IO operation failed
 */
public Object invoke(String deviceId, String methodName, Long timeout, Object payload) throws IotHubException, IOException;
```
**SRS_DEVICEMETHOD_21_004: [**The invoke shall throw IllegalArgumentException if the provided deviceId is null or empty.**]**  
**SRS_DEVICEMETHOD_21_005: [**The invoke shall throw IllegalArgumentException if the provided methodName is null, empty, or not valid.**]**  
**SRS_DEVICEMETHOD_21_006: [**The invoke shall throw IllegalArgumentException if the provided timeout is negative.**]**  
**SRS_DEVICEMETHOD_21_007: [**The invoke shall build the Method URL `{iot hub}/twins/{device id}/methods/`.**]**  
**SRS_DEVICEMETHOD_21_008: [**The invoke shall create a new SASToken with the ServiceConnect rights.**]**  
**SRS_DEVICEMETHOD_21_009: [**The invoke shall create a new HttpRequest with http method as `POST`.**]**  
**SRS_DEVICEMETHOD_21_010: [**The invoke shall add to the HTTP header an default timeout in milliseconds.**]**  
**SRS_DEVICEMETHOD_21_011: [**The invoke shall add to the HTTP header an `authorization` key with the SASToken.**]**    
**SRS_DEVICEMETHOD_21_012: [**The invoke shall add to the HTTP header a `request-id` key with a new unique string value for every request.**]**  
**SRS_DEVICEMETHOD_21_013: [**The invoke shall add a HTTP body with Json created by the `serializer.Method`.**]**  
**SRS_DEVICEMETHOD_21_014: [**The invoke shall send the created request and get the response.**]**  
**SRS_DEVICEMETHOD_21_015: [**The invoke shall deserialize the payload using the `serializer.Method`.**]**  
**SRS_DEVICEMETHOD_21_016: [**If the resulted status represents fail, the invoke shall throw proper Exception.**]**  
**SRS_DEVICEMETHOD_21_017: [**If the resulted status represents success, the invoke shall return the result payload.**]**  
**SRS_DEVICEMETHOD_21_018: [**The invoke shall bypass the Exception if one of the functions called by invoke failed.**]**  
