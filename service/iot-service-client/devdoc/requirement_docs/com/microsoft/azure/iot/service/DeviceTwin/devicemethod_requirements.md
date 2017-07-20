# DeviceMethod Requirements

## Overview

DeviceMethod enables service client to directly invoke methods on various devices from service client.

## References

[Understand and invoke direct methods from IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-direct-methods)  
[Schedule jobs on multiple devices](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-jobs)

## Exposed API


```java
/**
 * DeviceMethod enables service client to directly invoke methods on various devices from service client.
 */
public class DeviceMethod 
{
    public static DeviceMethod createFromConnectionString(String connectionString) throws Exception;
    public MethodResult invoke(String deviceId, 
                               String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload)
                            throws IotHubException, IOException;
    public Job scheduleDeviceMethod(String queryCondition,
                                    String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload,
                                    Date startTimeUtc, long maxExecutionTimeInSeconds) 
                                throws IOException, IotHubException;
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
public MethodResult invoke(String deviceId, 
                           String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload) 
                       throws IotHubException, IOException;
```
**SRS_DEVICEMETHOD_21_004: [**The invoke shall throw IllegalArgumentException if the provided deviceId is null or empty.**]**  
**SRS_DEVICEMETHOD_21_005: [**The invoke shall throw IllegalArgumentException if the provided methodName is null, empty, or not valid.**]**  
**SRS_DEVICEMETHOD_21_006: [**The invoke shall throw IllegalArgumentException if the provided responseTimeoutInSeconds is negative.**]**  
**SRS_DEVICEMETHOD_21_007: [**The invoke shall throw IllegalArgumentException if the provided connectTimeoutInSeconds is negative.**]**  
**SRS_DEVICEMETHOD_21_008: [**The invoke shall build the Method URL `{iot hub}/twins/{device id}/methods/` by calling getUrlMethod.**]**  
**SRS_DEVICEMETHOD_21_009: [**The invoke shall send the created request and get the response using the HttpRequester.**]**  
**SRS_DEVICEMETHOD_21_010: [**The invoke shall create a new HttpRequest with http method as `POST`.**]**  
**SRS_DEVICEMETHOD_21_011: [**The invoke shall add a HTTP body with Json created by the `serializer.MethodParser`.**]**  
**SRS_DEVICEMETHOD_21_012: [**If `MethodParser` return a null Json, the invoke shall throw IllegalArgumentException.**]**    
**SRS_DEVICEMETHOD_21_013: [**The invoke shall deserialize the payload using the `serializer.MethodParser`.**]**  
**SRS_DEVICEMETHOD_21_014: [**The invoke shall bypass the Exception if one of the functions called by invoke failed.**]**  
**SRS_DEVICEMETHOD_21_015: [**If the HttpStatus represents success, the invoke shall return the status and payload using the `MethodResult` class.**]**  

### scheduleDeviceMethod
```java
/**
 * Creates a new Job to invoke method on one or multiple devices
 *
 * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
 * @param methodName Method name to be invoked
 * @param responseTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for answer. It can be {@code null}.
 * @param connectTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for the connection. It can be {@code null}.
 * @param payload Object that contains the payload defined by the user. It can be {@code null}.
 * @param startTimeUtc Date time in Utc to start the job
 * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run
 * @return a Job class that represent this job on IotHub
 * @throws IOException if the function contains invalid parameters
 * @throws IotHubException if the http request failed
 */
public Job scheduleDeviceMethod(String queryCondition,
                                String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload,
                                Date startTimeUtc, long maxExecutionTimeInSeconds)
                            throws IOException, IotHubException;
```
**SRS_DEVICEMETHOD_21_016: [**If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.**]**  
**SRS_DEVICEMETHOD_21_017: [**If the startTimeUtc is null, the scheduleDeviceMethod shall throws IllegalArgumentException.**]**  
**SRS_DEVICEMETHOD_21_018: [**If the maxExecutionTimeInSeconds is negative, the scheduleDeviceMethod shall throws IllegalArgumentException.**]**  
**SRS_DEVICEMETHOD_21_019: [**The scheduleDeviceMethod shall create a new instance of the Job class.**]**  
**SRS_DEVICEMETHOD_21_020: [**If the scheduleDeviceMethod failed to create a new instance of the Job class, it shall throws IOException. Threw by the Jobs constructor.**]**  
**SRS_DEVICEMETHOD_21_021: [**The scheduleDeviceMethod shall invoke the scheduleDeviceMethod in the Job class with the received parameters.**]**  
**SRS_DEVICEMETHOD_21_022: [**If scheduleDeviceMethod failed, the scheduleDeviceMethod shall throws IotHubException. Threw by the scheduleUpdateTwin.**]**  
**SRS_DEVICEMETHOD_21_023: [**The scheduleDeviceMethod shall return the created instance of the Job class.**]**  
