# JobClient Requirements

## Overview

JobClient enables service client to schedule and cancel jobs for a group of devices using the IoTHub.

## References

[Schedule jobs on multiple devices](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-jobs)


## Exposed API

```java
public class JobClient 
{
    public static JobClient createFromConnectionString(String connectionString) throws IOException, IllegalArgumentException;
    
    public synchronized JobResult scheduleUpdateTwin(
            String jobId,
            String queryCondition,
            DeviceTwinDevice updateTwin,
            Date startTimeUtc,
            long maxExecutionTimeInSeconds)
            throws IllegalArgumentException, IOException, IotHubException;
    
    public synchronized JobResult scheduleDeviceMethod(
            String jobId,
            String queryCondition,
            String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload,
            Date startTimeUtc,
            long maxExecutionTimeInSeconds)
            throws IllegalArgumentException, IOException, IotHubException;

    public synchronized JobResult getJob(String jobId)
            throws IllegalArgumentException, IOException, IotHubException;

    public synchronized JobResult cancelJob(String jobId)
            throws IllegalArgumentException, IOException, IotHubException;    
}
```

### createFromConnectionString
```java
public static JobClient createFromConnectionString(String connectionString) throws IOException, IllegalArgumentException;
```
**SRS_JOBCLIENT_21_001: [**The constructor shall throw IllegalArgumentException if the input string is null or empty.**]**  
**SRS_JOBCLIENT_21_002: [**The constructor shall create an IotHubConnectionStringBuilder object from the given connection string.**]**  
**SRS_JOBCLIENT_21_003: [**The constructor shall create a new JobClient instance and return it.**]**  


### scheduleUpdateTwin
```java
public synchronized JobResult scheduleUpdateTwin(
        String jobId,
        String queryCondition,
        DeviceTwinDevice updateTwin,
        Date startTimeUtc,
        long maxExecutionTimeInSeconds)
        throws IllegalArgumentException, IOException, IotHubException
```
**SRS_JOBCLIENT_21_004: [**The scheduleUpdateTwin shall create a json String that represent the twin job using the JobsParser class.**]**  
**SRS_JOBCLIENT_21_005: [**If the JobId is null, empty, or invalid, the scheduleUpdateTwin shall throws IllegalArgumentException.**]**  
**SRS_JOBCLIENT_21_006: [**If the updateTwin is null, the scheduleUpdateTwin shall throws IllegalArgumentException.**]**  
**SRS_JOBCLIENT_21_007: [**If the startTimeUtc is null, the scheduleUpdateTwin shall throws IllegalArgumentException.**]**  
**SRS_JOBCLIENT_21_008: [**If the maxExecutionTimeInSeconds is negative, the scheduleUpdateTwin shall throws IllegalArgumentException.**]**  
**SRS_JOBCLIENT_21_009: [**The scheduleUpdateTwin shall create a URL for Jobs using the iotHubConnectionString.**]**  
**SRS_JOBCLIENT_21_010: [**The scheduleUpdateTwin shall send a PUT request to the iothub using the created uri and json.**]**  
**SRS_JOBCLIENT_21_011: [**If the scheduleUpdateTwin failed to send a PUT request, it shall throw IOException.**]**  
**SRS_JOBCLIENT_21_012: [**If the scheduleUpdateTwin failed to verify the iothub response, it shall throw IotHubException.**]**  
**SRS_JOBCLIENT_21_013: [**The scheduleUpdateTwin shall parse the iothub response and return it as JobResult.**]**  


### scheduleDeviceMethod
```java
public synchronized JobResult scheduleDeviceMethod(
        String jobId,
        String queryCondition,
        String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload,
        Date startTimeUtc,
        long maxExecutionTimeInSeconds)
        throws IllegalArgumentException, IOException, IotHubException
```
**SRS_JOBCLIENT_21_014: [**If the JobId is null, empty, or invalid, the scheduleDeviceMethod shall throws IllegalArgumentException.**]**  
**SRS_JOBCLIENT_21_015: [**If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.**]**  
**SRS_JOBCLIENT_21_016: [**If the startTimeUtc is null, the scheduleDeviceMethod shall throws IllegalArgumentException.**]**  
**SRS_JOBCLIENT_21_017: [**If the maxExecutionTimeInSeconds is negative, the scheduleDeviceMethod shall throws IllegalArgumentException.**]**  
**SRS_JOBCLIENT_21_018: [**The scheduleDeviceMethod shall create a json String that represent the twin job using the JobsParser class.**]**  
**SRS_JOBCLIENT_21_019: [**The scheduleDeviceMethod shall create a URL for Jobs using the iotHubConnectionString.**]**  
**SRS_JOBCLIENT_21_020: [**The scheduleDeviceMethod shall send a PUT request to the iothub using the created url and json.**]**  
**SRS_JOBCLIENT_21_021: [**If the scheduleDeviceMethod failed to send a PUT request, it shall throw IOException.**]**  
**SRS_JOBCLIENT_21_022: [**If the scheduleDeviceMethod failed to verify the iothub response, it shall throw IotHubException.**]**  
**SRS_JOBCLIENT_21_023: [**The scheduleDeviceMethod shall parse the iothub response and return it as JobResult.**]**  


### getJob
```java
public synchronized JobResult getJob(String jobId)
        throws IllegalArgumentException, IOException, IotHubException
```
**SRS_JOBCLIENT_21_024: [**If the JobId is null, empty, or invalid, the scheduleDeviceMethod shall throws IllegalArgumentException.**]**  
**SRS_JOBCLIENT_21_025: [**The getJob shall create a URL for Jobs using the iotHubConnectionString.**]**  
**SRS_JOBCLIENT_21_026: [**The getJob shall send a GET request to the iothub using the created url.**]**  
**SRS_JOBCLIENT_21_027: [**If the getJob failed to send a GET request, it shall throw IOException.**]**  
**SRS_JOBCLIENT_21_028: [**If the getJob failed to verify the iothub response, it shall throw IotHubException.**]**  
**SRS_JOBCLIENT_21_029: [**The getJob shall parse the iothub response and return it as JobResult.**]**  


### cancelJob
```java
public synchronized JobResult cancelJob(String jobId)
        throws IllegalArgumentException, IOException, IotHubException
```
**SRS_JOBCLIENT_21_030: [**If the JobId is null, empty, or invalid, the cancelJob shall throws IllegalArgumentException.**]**  
**SRS_JOBCLIENT_21_031: [**The cancelJob shall create a cancel URL for Jobs using the iotHubConnectionString.**]**  
**SRS_JOBCLIENT_21_032: [**The cancelJob shall send a POST request to the iothub using the created url.**]**  
**SRS_JOBCLIENT_21_033: [**If the cancelJob failed to send a POST request, it shall throw IOException.**]**  
**SRS_JOBCLIENT_21_034: [**If the cancelJob failed to verify the iothub response, it shall throw IotHubException.**]**  
**SRS_JOBCLIENT_21_035: [**The cancelJob shall parse the iothub response and return it as JobResult.**]**  
