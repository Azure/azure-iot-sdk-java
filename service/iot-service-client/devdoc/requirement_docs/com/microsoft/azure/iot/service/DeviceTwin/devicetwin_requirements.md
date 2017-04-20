# DeviceTwin Requirements

## Overview

DeviceTwin enables service client to manage the tags and desired properties for various devices.

## References

([IoTHub DeviceTwin.doc](to https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins)

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

**SRS_DEVICETWIN_25_011: [** The function shall deserialize the payload by calling updateTwin Api on the twin object **]**

**SRS_DEVICETWIN_25_012: [** The function shall set tags, desired property map, reported property map on the user device **]**

### updateTwin

```java
public void updateTwin(DeviceTwinDevice device) throws IotHubException, IOException;
```
**SRS_DEVICETWIN_25_013: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**

**SRS_DEVICETWIN_25_045: [** The function shall throw IllegalArgumentException if the both desired and tags maps are either empty or null **]**

**SRS_DEVICETWIN_25_014: [** The function shall build the URL for this operation by calling getUrlTwin **]**

**SRS_DEVICETWIN_25_015: [** The function shall serialize the twin map by calling updateTwin Api on the twin object for the device provided by the user**]**

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

**SRS_DEVICETWIN_25_023: [** The function shall serialize the desired properties map by calling updateDesiredProperty Api on the twin object for the device provided by the user**]**

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

**SRS_DEVICETWIN_25_031: [** The function shall serialize the desired properties map by calling resetDesiredProperty Api on the twin object for the device provided by the user**]**

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

**SRS_DEVICETWIN_25_039: [** The function shall serialize the tags map by calling resetTags Api on the twin object for the device provided by the user**]**

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
