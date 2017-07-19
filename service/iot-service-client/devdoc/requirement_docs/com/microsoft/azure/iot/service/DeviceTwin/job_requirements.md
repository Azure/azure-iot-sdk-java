# Job Requirements

## Overview

Representation of a single Job scheduled on the Iothub.

## References

[Schedule jobs on multiple devices](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-jobs)

## Exposed API

```java
public class Job 
{
    Job(String connectionString) throws IOException;
    Job(String jobId, String connectionString) throws IOException;
    
    void scheduleUpdateTwin(String queryCondition,
                            DeviceTwinDevice updateTwin,
                            Date startTimeUtc, long maxExecutionTimeInSeconds) throws IOException, IotHubException;
    void scheduleDeviceMethod(String queryCondition,
                              String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload,
                              Date startTimeUtc, long maxExecutionTimeInSeconds) throws IOException, IotHubException;
    
    public JobResult get() throws IOException, IotHubException;
    public JobResult cancel() throws IOException, IotHubException;
    public String getJobId();
}
```

### CONSTRUCTOR
```java
Job(String connectionString) throws IOException;
Job(String jobId, String connectionString) throws IOException;
```
**SRS_JOB_21_001: [**The constructor shall throw IllegalArgumentException if the input string is null or empty.**]**  
**SRS_JOB_21_002: [**If a jobId is provided, the constructor shall use this jobId to identify the Job in the Iothub.**]**  
**SRS_JOB_21_003: [**If no jobId is provided, the constructor shall generate a unique jobId to identify the Job in the Iothub.**]**  
**SRS_JOB_21_004: [**The constructor shall create a new instance of JobClient to manage the Job.**]**  
**SRS_JOB_21_005: [**The constructor shall throw IOException if it failed to create a new instance of the JobClient. Threw by the JobClient constructor.**]**  

### scheduleUpdateTwin
```java
public Job scheduleUpdateTwin(String queryCondition,
                              DeviceTwinDevice updateTwin,
                              Date startTimeUtc,
                              long maxExecutionTimeInSeconds) throws IOException, IotHubException;
```
**SRS_JOB_21_006: [**If the updateTwin is null, the scheduleUpdateTwin shall throws IllegalArgumentException.**]**  
**SRS_JOB_21_007: [**If the startTimeUtc is null, the scheduleUpdateTwin shall throws IllegalArgumentException.**]**  
**SRS_JOB_21_008: [**If the maxExecutionTimeInSeconds is negative, the scheduleUpdateTwin shall throws IllegalArgumentException.**]**  
**SRS_JOB_21_009: [**The scheduleUpdateTwin shall invoke the scheduleUpdateTwin in the JobClient class with the received parameters.**]**  
**SRS_JOB_21_010: [**If scheduleUpdateTwin failed, the scheduleUpdateTwin shall throws IotHubException. Threw by the scheduleUpdateTwin.**]**  
**SRS_JOB_21_011: [**If the Iothub reported fail as result of the scheduleUpdateTwin, the scheduleUpdateTwin shall throws IotHubException.**]**  

### scheduleDeviceMethod
```java
void scheduleDeviceMethod(String queryCondition,
                          String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload,
                          Date startTimeUtc, long maxExecutionTimeInSeconds) throws IOException, IotHubException;
```
**SRS_JOB_21_012: [**If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.**]**  
**SRS_JOB_21_013: [**If the startTimeUtc is null, the scheduleDeviceMethod shall throws IllegalArgumentException.**]**  
**SRS_JOB_21_014: [**If the maxExecutionTimeInSeconds is negative, the scheduleDeviceMethod shall throws IllegalArgumentException.**]**  
**SRS_JOB_21_015: [**The scheduleDeviceMethod shall invoke the scheduleDeviceMethod in the JobClient class with the received parameters.**]**  
**SRS_JOB_21_016: [**If scheduleDeviceMethod failed, the scheduleDeviceMethod shall throws IotHubException. Threw by the scheduleUpdateTwin.**]**  
**SRS_JOB_21_017: [**If the Iothub reported fail as result of the scheduleDeviceMethod, the scheduleDeviceMethod shall throws IotHubException.**]**  

### get
```java
public JobResult get() throws IOException, IotHubException;
```
**SRS_JOB_21_018: [**The get shall invoke getJob on JobClient with the current jobId and return its result.**]**  
**SRS_JOB_21_019: [**If getJob failed, the get shall throws IOException. Threw by the getJob.**]**  

### cancel
```java
public JobResult cancel() throws IOException, IotHubException;
```
**SRS_JOB_21_020: [**The cancel shall invoke cancelJob on JobClient with the current jobId and return its result.**]**  
**SRS_JOB_21_021: [**If cancelJob failed, the cancel shall throws IOException. Threw by the cancelJob.**]**  

### getJobId
```java
public String getJobId();
```
**SRS_JOB_21_022: [**The getJobId shall return the store value of jobId.**]**  
