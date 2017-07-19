# DeviceTwin Requirements

## Overview

DeviceTwin enables service client to manage the tags and desired properties for various devices.

## References

[IoTHub DeviceTwin.doc](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins)

[Schedule jobs on multiple devices](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-jobs)

## Exposed API


```java
public class DeviceTwin 
{
    public static DeviceTwin createFromConnectionString(String connectionString) throws Exception;

    public void getTwin(DeviceTwinDevice device) throws IotHubException, IOException;

    public void updateTwin(DeviceTwinDevice device) throws IotHubException, IOException;
    public void updateDesiredProperties(DeviceTwinDevice device) throws IotHubException, IOException;
    
    public void replaceDesired(DeviceTwinDevice device) throws IotHubException, IOException;
    public void replaceTags(DeviceTwinDevice device) throws IotHubException, IOException;
    
    public synchronized Query queryTwin(String sqlQuery, Integer pageSize) throws IotHubException, IOException;
    public synchronized Query queryTwin(String sqlQuery) throws IotHubException, IOException;

    public synchronized boolean hasNextDeviceTwin(Query query) throws IotHubException, IOException;
    public synchronized String getNextDeviceTwin(Query query) throws IOException, IotHubException, NoSuchElementException;

    public Job scheduleUpdateTwin(String queryCondition,
                                  DeviceTwinDevice updateTwin,
                                  Date startTimeUtc,
                                  long maxExecutionTimeInSeconds) throws IOException, IotHubException;
}
```

### createFromConnectionString

```java
public static DeviceTwin createFromConnectionString(String connectionString) throws Exception;
```
**SRS_DEVICETWIN_25_001: [** The constructor shall throw IllegalArgumentException if the input string is null or empty **]**

**SRS_DEVICETWIN_25_002: [** The constructor shall create an IotHubConnectionStringBuilder object from the given connection string **]**

**SRS_DEVICETWIN_25_003: [** The constructor shall create a new DeviceTwin instance and return it **]**

### getTwin

```java
public void getTwin(DeviceTwinDevice device) throws IotHubException, IOException
```
**SRS_DEVICETWIN_25_004: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**

**SRS_DEVICETWIN_25_005: [** The function shall build the URL for this operation by calling getUrlTwin **]**

**SRS_DEVICETWIN_25_006: [** The function shall create a new SAS token **]**

**SRS_DEVICETWIN_25_007: [** The function shall create a new HttpRequest with http method as Get **]**

**SRS_DEVICETWIN_25_008: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

**SRS_DEVICETWIN_25_009: [** The function shall send the created request and get the response **]**

**SRS_DEVICETWIN_25_010: [** The function shall verify the response status and throw proper Exception **]**

**SRS_DEVICETWIN_25_011: [** The function shall deserialize the payload by calling updateTwin Api on the twinParser object **]**

**SRS_DEVICETWIN_25_012: [** The function shall set tags, desired property map, reported property map on the user device **]**

### updateTwin

```java
public void updateTwin(DeviceTwinDevice device) throws IotHubException, IOException;
```
**SRS_DEVICETWIN_25_013: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**

**SRS_DEVICETWIN_25_045: [** The function shall throw IllegalArgumentException if the both desired and tags maps are either empty or null **]**

**SRS_DEVICETWIN_25_014: [** The function shall build the URL for this operation by calling getUrlTwin **]**

**SRS_DEVICETWIN_25_015: [** The function shall serialize the twin map by calling updateTwin Api on the twinParser object for the device provided by the user**]**

**SRS_DEVICETWIN_25_046: [** The function shall throw IOException if updateTwin Api call returned an empty or null json**]**

**SRS_DEVICETWIN_25_016: [** The function shall create a new SAS token **]**

**SRS_DEVICETWIN_25_017: [** The function shall create a new HttpRequest with http method as Patch **]**

**SRS_DEVICETWIN_25_018: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

**SRS_DEVICETWIN_25_019: [** The function shall send the created request and get the response **]**

**SRS_DEVICETWIN_25_020: [** The function shall verify the response status and throw proper Exception **]**

### updateDesiredProperties

```java
public void updateDesiredProperties(DeviceTwinDevice device) throws IotHubException, IOException
```
**SRS_DEVICETWIN_25_021: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**

**SRS_DEVICETWIN_25_022: [** The function shall build the URL for this operation by calling getUrlTwinDesired **]**

**SRS_DEVICETWIN_25_023: [** The function shall serialize the desired properties map by calling updateDesiredProperty Api on the twinParser object for the device provided by the user**]**

**SRS_DEVICETWIN_25_024: [** The function shall create a new SAS token **]**

**SRS_DEVICETWIN_25_025: [** The function shall create a new HttpRequest with http method as Patch **]**

**SRS_DEVICETWIN_25_026: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

**SRS_DEVICETWIN_25_027: [** The function shall send the created request and get the response **]**

**SRS_DEVICETWIN_25_028: [** The function shall verify the response status and throw proper Exception **]**


### replaceDesired

```java
public void replaceDesired(DeviceTwinDevice device) throws IotHubException, IOException;
```
**SRS_DEVICETWIN_25_029: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**

**SRS_DEVICETWIN_25_030: [** The function shall build the URL for this operation by calling getUrlTwinDesired **]**

**SRS_DEVICETWIN_25_031: [** The function shall serialize the desired properties map by calling resetDesiredProperty Api on the twinParser object for the device provided by the user**]**

**SRS_DEVICETWIN_25_045: [** If resetDesiredProperty call returns null or empty string then this method shall throw IOException**]**

**SRS_DEVICETWIN_25_032: [** The function shall create a new SAS token **]**

**SRS_DEVICETWIN_25_033: [** The function shall create a new HttpRequest with http method as PUT **]**

**SRS_DEVICETWIN_25_034: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

**SRS_DEVICETWIN_25_035: [** The function shall send the created request and get the response **]**

**SRS_DEVICETWIN_25_036: [** The function shall verify the response status and throw proper Exception **]**

### replaceTags

```java
public void replaceTags(DeviceTwinDevice device) throws IotHubException, IOException;
```
**SRS_DEVICETWIN_25_037: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**

**SRS_DEVICETWIN_25_038: [** The function shall build the URL for this operation by calling getUrlTwinTags **]**

**SRS_DEVICETWIN_25_039: [** The function shall serialize the tags map by calling resetTags Api on the twinParser object for the device provided by the user**]**

**SRS_DEVICETWIN_25_046: [** If resetTags call returns null or empty string then this method shall throw IOException**]**

**SRS_DEVICETWIN_25_040: [** The function shall create a new SAS token **]**

**SRS_DEVICETWIN_25_041: [** The function shall create a new HttpRequest with http method as PUT **]**

**SRS_DEVICETWIN_25_042: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

**SRS_DEVICETWIN_25_043: [** The function shall send the created request and get the response **]**

**SRS_DEVICETWIN_25_044: [** The function shall verify the response status and throw proper Exception **]**


### queryTwin

```java
 public synchronized Query queryTwin(String sqlQuery, Integer pageSize) throws IotHubException, IOException;
 public synchronized Query queryTwin(String sqlQuery) throws IotHubException, IOException;
```
**SRS_DEVICETWIN_25_047: [** The method shall throw IllegalArgumentException if the query is null or empty.**]**

**SRS_DEVICETWIN_25_048: [** The method shall throw IllegalArgumentException if the page size is zero or negative.**]**

**SRS_DEVICETWIN_25_049: [** The method shall build the URL for this operation by calling getUrlTwinQuery **]**

**SRS_DEVICETWIN_25_050: [** The method shall create a new Query Object of Type TWIN. **]**

**SRS_DEVICETWIN_25_051: [** The method shall send a Query Request to IotHub as HTTP Method Post on the query Object by calling `sendQueryRequest`.**]**

**SRS_DEVICETWIN_25_052: [** If the pageSize if not provided then a default pageSize of 100 is used for the query.**]**

### hasNextDeviceTwin

```java
public synchronized boolean hasNextDeviceTwin(Query deviceTwinQuery) throws IotHubException, IOException;
```
**SRS_DEVICETWIN_25_053: [** The method shall throw IllegalArgumentException if deviceTwinQuery is null **]**

**SRS_DEVICETWIN_25_055: [** If a queryResponse is available, this method shall return true as is to the user, and false otherwise. **]**

### getNextDeviceTwin

```java
public synchronized DeviceTwinDevice getNextDeviceTwin(Query deviceTwinQuery) throws IOException, IotHubException, NoSuchElementException;
```
**SRS_DEVICETWIN_25_058: [** The method shall throw NoSuchElementException if no element is found.**]**

**SRS_DEVICETWIN_25_054: [** The method shall throw IllegalArgumentException if deviceTwinQuery is null **]**

**SRS_DEVICETWIN_25_059: [** The method shall parse the next element from the query response as Twin Document using `TwinParser` and provide the response on DeviceTwinDevice.**]**

**SRS_DEVICETWIN_25_060: [** If the next element from the query response is an object other than String, then this method shall throw IOException **]**


### scheduleUpdateTwin

```java
public Job scheduleUpdateTwin(String queryCondition,
                              DeviceTwinDevice updateTwin,
                              Date startTimeUtc,
                              long maxExecutionTimeInSeconds) throws IOException, IotHubException
```
**SRS_DEVICETWIN_21_061: [** If the updateTwin is null, the scheduleUpdateTwin shall throws IllegalArgumentException **]**

**SRS_DEVICETWIN_21_062: [** If the startTimeUtc is null, the scheduleUpdateTwin shall throws IllegalArgumentException **]**

**SRS_DEVICETWIN_21_063: [** If the maxExecutionTimeInSeconds is negative, the scheduleUpdateTwin shall throws IllegalArgumentException **]**

**SRS_DEVICETWIN_21_064: [** The scheduleUpdateTwin shall create a new instance of the Job class **]**

**SRS_DEVICETWIN_21_065: [** If the scheduleUpdateTwin failed to create a new instance of the Job class, it shall throws IOException. Threw by the Jobs constructor **]**

**SRS_DEVICETWIN_21_066: [** The scheduleUpdateTwin shall invoke the scheduleUpdateTwin in the Job class with the received parameters **]**

**SRS_DEVICETWIN_21_067: [** If scheduleUpdateTwin failed, the scheduleUpdateTwin shall throws IotHubException. Threw by the scheduleUpdateTwin **]**

**SRS_DEVICETWIN_21_068: [** The scheduleUpdateTwin shall return the created instance of the Job class **]**

