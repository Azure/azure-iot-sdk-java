# DEVICE_OPERATIONS Requirements

## Overview

Set of common operations for Twin and Method.

## References

[IoTHub device twin guide](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins)
[Understand and request direct methods from IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-direct-methods)

## Exposed API


```java
/**
 * Set of common operations for Twin and Method.
 */
public class DeviceOperations
{
    public static HttpResponse request(IotHubConnectionString iotHubConnectionString, URL url, HttpMethod method, byte[] payload, String requestId) throws IOException, IotHubException;

    public static void setHeaders(Map<String, String> httpHeaders) throws IllegalArgumentException;
}
```

### request
```java
/**
 * Send a http request to the IoTHub using the Twin/Method standard, and return its response.
 * 
 * @param iotHubConnectionString is the connection string for the IoTHub
 * @param url is the Twin URL for the device ID.
 * @param method is the HTTP method (GET, POST, DELETE, PATCH, PUT).
 * @param payload is the array of bytes that contains the payload.
 * @param requestId is an unique number that identify the request.
 * @return the result of the request.
 * @throws IotHubException This exception is thrown if the response verification failed
 * @throws IOException This exception is thrown if the IO operation failed
 */
public static HttpResponse request(
        IotHubConnectionString iotHubConnectionString, 
        URL url, 
        HttpMethod method, 
        byte[] payload, 
        String requestId) 
        throws IOException, IotHubException, IllegalArgumentException
```
**SRS_DEVICE_OPERATIONS_21_001: [**The request shall throw IllegalArgumentException if the provided `iotHubConnectionString` is null.**]**  
**SRS_DEVICE_OPERATIONS_21_002: [**The request shall throw IllegalArgumentException if the provided `url` is null.**]**  
**SRS_DEVICE_OPERATIONS_21_003: [**The request shall throw IllegalArgumentException if the provided `method` is null.**]**  
**SRS_DEVICE_OPERATIONS_21_004: [**The request shall throw IllegalArgumentException if the provided `payload` is null.**]**  
**SRS_DEVICE_OPERATIONS_21_005: [**If the `requestId` is not null or empty, it shall be set as header.**]**  
**SRS_DEVICE_OPERATIONS_21_006: [**The request shall create a new SASToken with the ServiceConnect rights.**]**  
**SRS_DEVICE_OPERATIONS_21_007: [**If the SASToken is null or empty, the request shall throw IOException.**]**  
**SRS_DEVICE_OPERATIONS_21_008: [**The request shall create a new HttpRequest with the provided `url`, http `method`, and `payload`.**]**  
**SRS_DEVICE_OPERATIONS_21_009: [**The request shall add to the HTTP header the sum of timeout and default timeout in milliseconds.**]**  
**SRS_DEVICE_OPERATIONS_21_010: [**The request shall add to the HTTP header an `authorization` key with the SASToken.**]**    
**SRS_DEVICE_OPERATIONS_21_011: [**If the `requestId` is not null or empty, the request shall add to the HTTP header a `Request-Id` key with a new unique string value for every request.**]**  
**SRS_DEVICE_OPERATIONS_21_012: [**The request shall add to the HTTP header a `User-Agent` key with the client Id and service version.**]**  
**SRS_DEVICE_OPERATIONS_21_013: [**The request shall add to the HTTP header a `Accept` key with `application/json`.**]**  
**SRS_DEVICE_OPERATIONS_21_014: [**The request shall add to the HTTP header a `Content-Type` key with `application/json; charset=utf-8`.**]**  
**SRS_DEVICE_OPERATIONS_25_019: [**The request shall add to the HTTP header all the additional custom headers set for this request.**]** 
**SRS_DEVICE_OPERATIONS_21_015: [**The request shall send the created request and get the response.**]**  
**SRS_DEVICE_OPERATIONS_21_016: [**If the resulted HttpResponseStatus represents fail, the request shall throw proper Exception by calling httpResponseVerification.**]**  
**SRS_DEVICE_OPERATIONS_21_017: [**If the resulted status represents success, the request shall return the http response.**]**  
**SRS_DEVICE_OPERATIONS_99_018: [**The request shall throw IllegalArgumentException if the provided `timeoutInMs` plus DEFAULT_HTTP_TIMEOUT_MS exceed Integer.MAX_VALUE.**]**  

### setHeaders
```java
static void setHeaders(Map<String, String> httpHeaders) throws IllegalArgumentException;
```
**SRS_DEVICE_OPERATIONS_25_020: [**This method shall set the headers map to be used for next request only.**]** 

**SRS_DEVICE_OPERATIONS_25_021: [**If the headers map is null or empty then this method shall throw IllegalArgumentException.**]** 