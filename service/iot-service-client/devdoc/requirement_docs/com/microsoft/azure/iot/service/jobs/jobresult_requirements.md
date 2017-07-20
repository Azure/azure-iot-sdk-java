# JobResult Requirements

## Overview

Collection with the result of a job operation.

## References

[Schedule jobs on multiple devices](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-jobs)


## Exposed API

```java
public class JobResult 
{
    JobResult(byte[] body) throws JsonParseException, IllegalArgumentException;

    public String getJobId();
    public String getQueryCondition();
    public Date getCreatedTime();
    public Date getStartTime();
    public Date getEndTime();
    public Long getMaxExecutionTimeInSeconds();
    public JobType getJobType();
    public JobStatus getJobStatus();
    public String getCloudToDeviceMethod();
    public DeviceTwinDevice getUpdateTwin();
    public String getFailureReason();
    public String getStatusMessage();
    public JobStatistics getJobStatistics();
    public String getDeviceId();
    public String getParentJobId();
    public getLastUpdatedDateTime();
    public String getOutcomeResult();
    public String getError();
    
    @Override public String toString();
}
```

### JobResult
```java
JobResult(byte[] body) throws JsonParseException, IllegalArgumentException;
```
**SRS_JOBRESULT_21_001: [**The constructor shall throw IllegalArgumentException if the input body is null.**]**  
**SRS_JOBRESULT_21_002: [**The constructor shall parse the body using the JobsResponseParser.**]**  
**SRS_JOBRESULT_21_003: [**The constructor shall throw JsonParseException if the input body contains a invalid json.**]**  
**SRS_JOBRESULT_21_004: [**The constructor shall locally store all results information in the provided body.**]**  

### getJobId
```java
public String getJobId();
```
**SRS_JOBRESULT_21_005: [**The getJobId shall return the stored jobId.**]**  

### getQueryCondition
```java
public String getQueryCondition();
```
**SRS_JOBRESULT_21_006: [**The getQueryCondition shall return the stored queryCondition.**]**  

### getCreatedTime
```java
public Date getCreatedTime();
```
**SRS_JOBRESULT_21_007: [**The getCreatedTime shall return the stored createdTime.**]**  

### getStartTime
```java
public Date getStartTime();
```
**SRS_JOBRESULT_21_008: [**The getStartTime shall return the stored startTime.**]**  

### getEndTime
```java
public Date getEndTime();
```
**SRS_JOBRESULT_21_009: [**The getEndTime shall return the stored endTime.**]**  

### getMaxExecutionTimeInSeconds
```java
public Long getMaxExecutionTimeInSeconds();
```
**SRS_JOBRESULT_21_010: [**The getMaxExecutionTimeInSeconds shall return the stored maxExecutionTimeInSeconds.**]**  

### getJobType
```java
public JobType getJobType();
```
**SRS_JOBRESULT_21_011: [**The getJobType shall return the stored jobType.**]**  

### getJobStatus
```java
public JobStatus getJobStatus();
```
**SRS_JOBRESULT_21_012: [**The getJobStatus shall return the stored jobStatus.**]**  

### getCloudToDeviceMethod
```java
public String getCloudToDeviceMethod();
```
**SRS_JOBRESULT_21_013: [**The getCloudToDeviceMethod shall return the stored cloudToDeviceMethod.**]**  

### getUpdateTwin
```java
public DeviceTwinDevice getUpdateTwin();
```
**SRS_JOBRESULT_21_014: [**The getUpdateTwin shall return the stored updateTwin.**]**  

### getFailureReason
```java
public String getFailureReason();
```
**SRS_JOBRESULT_21_015: [**The getFailureReason shall return the stored failureReason.**]**  

### getStatusMessage
```java
public String getStatusMessage();
```
**SRS_JOBRESULT_21_016: [**The getStatusMessage shall return the stored statusMessage.**]**  

### getJobStatistics
```java
public JobStatistics getJobStatistics();
```
**SRS_JOBRESULT_21_017: [**The getJobStatistics shall return the stored jobStatistics.**]**  

### getDeviceId
```java
public String getDeviceId();
```
**SRS_JOBRESULT_21_018: [**The getDeviceId shall return the stored deviceId.**]**  

### getParentJobId
```java
public String getParentJobId();
```
**SRS_JOBRESULT_21_019: [**The getParentJobId shall return the stored parentJobId.**]**  

### getLastUpdatedDateTime
```java
public Date getLastUpdatedDateTime();
```
**SRS_JOBRESULT_25_023: [**The getLastUpdatedDateTime shall return the stored LastUpdatedDateTime.**]**  

### getOutcomeResult
```java
public String getOutcomeResult();
```
**SRS_JOBRESULT_25_021: [**The getOutcomeResult shall return the stored outcome.**]**  

### getError
```java
public String getError();
```
**SRS_JOBRESULT_25_022: [**The getError shall return the stored error message.**]**  

### toString
```java
@Override public String toString()
```
**SRS_JOBRESULT_21_020: [**The toString shall return a String with a pretty print json that represents this class.**]**  

