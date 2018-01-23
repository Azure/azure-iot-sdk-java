# JobsResponseParser Requirements

## Overview

Representation of a single Jobs response collection with a Json deserializer.

## References

[Schedule jobs on multiple devices](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-jobs)

## Exposed API

```java
public class JobsResponseParser
{
    public static JobsResponseParser createFromJson(String json) throws IllegalArgumentException, JsonParseException, ParseException;

    public String getJobId();
    public String getQueryCondition();
    public Date getCreateTime();
    public Date getStartTime();
    public Date getEndTime();
    public Date getLastUpdatedTime();
    public Long getMaxExecutionTimeInSeconds();
    public String getJobType();
    public String getJobStatus();
    public MethodParser getCloudToDeviceMethod();
    public TwinState getUpdateTwin();
    public String getFailureReason();
    public String getStatusMessage();
    public JobsStatisticsParser getJobStatistics();
    public String getDeviceId();
    public String getParentJobId();
    public JobQueryResponseError getError();
    public MethodParser getOutcome();

}
```

### createFromJson
```java
public static JobsResponseParser createFromJson(String json) throws IllegalArgumentException, JsonParseException, ParseException
```
**SRS_JOBSRESPONSEPARSER_21_001: [**The createFromJson shall create a new instance of JobsResponseParser class.**]**  
**SRS_JOBSRESPONSEPARSER_21_002: [**The createFromJson shall parse the provided string for JobsResponseParser class.**]**  
**SRS_JOBSRESPONSEPARSER_21_003: [**If the json contains `updateTwin`, the createFromJson shall parse the content of it for TwinState class.**]**
**SRS_JOBSRESPONSEPARSER_21_004: [**If the json contains `cloudToDeviceMethod`, the createFromJson shall parse the content of it for MethodParser class.**]** 
**SRS_JOBSRESPONSEPARSER_25_028: [**If the json contains `outcome`, the createFromJson shall parse the value of the key `deviceMethodResponse` for MethodParser class.**]** 
**SRS_JOBSRESPONSEPARSER_25_029: [**If the json contains `outcome`, and the key `deviceMethodResponse` does not exist then this method shall create empty method parser for MethodParser class.**]** 
**SRS_JOBSRESPONSEPARSER_21_030: [**If the json contains `error`, the createFromJson shall parse the content of it for JobQueryResponseError class.**]**  
**SRS_JOBSRESPONSEPARSER_21_005: [**If the json contains `deviceJobStatistics`, the createFromJson shall parse the content of it for JobsStatisticsParser class.**]**  
**SRS_JOBSRESPONSEPARSER_21_006: [**If the json is null or empty, the createFromJson shall throws IllegalArgumentException.**]**  
**SRS_JOBSRESPONSEPARSER_21_007: [**If the json is not valid, the createFromJson shall throws JsonParseException.**]**  
**SRS_JOBSRESPONSEPARSER_21_008: [**If the json do not contains `jobId`, the createFromJson shall throws IllegalArgumentException.**]**  
**SRS_JOBSRESPONSEPARSER_21_009: [**If the json do not contains `type` or `jobType`, or the `type` or `jobType` is invalid or contains both at the same time, the createFromJson shall throws IllegalArgumentException.**]**  
**SRS_JOBSRESPONSEPARSER_21_010: [**If the json do not contains `status`, or the `status` is invalid, the createFromJson shall throws IllegalArgumentException.**]**  
**SRS_JOBSRESPONSEPARSER_21_011: [**If the json contains any of the dates `createdTime` or `createdDateTimeUtc`, `startTime` or `startTimeUtc`, `lastUpdatedDateTimeUtc` or `endTime` or `endTimeUtc`, the createFromJson shall parser it as ISO_8601.**]**  
**SRS_JOBSRESPONSEPARSER_25_034: [**If the json contains both of the dates `createdTime` and `createdDateTimeUtc` or `startTime` and `startTimeUtc` or `endTime` and `endTimeUtc`, the createFromJson shall throw IllegalArgumentException.**]**  
**SRS_JOBSRESPONSEPARSER_21_012: [**If the createFromJson cannot properly parse the date in json, it shall ignore this value.**]**  

### getJobId
```java
public String getJobId()
```
**SRS_JOBSRESPONSEPARSER_21_013: [**The getJobId shall return the jobId value.**]**  

### getQueryCondition
```java
public String getQueryCondition()
```
**SRS_JOBSRESPONSEPARSER_21_014: [**The getQueryCondition shall return the queryCondition value.**]**  

### getCreateTime
```java
public Date getCreateTime()
```
**SRS_JOBSRESPONSEPARSER_21_015: [**The getCreateTime shall return the createTime value.**]**  

### getStartTime
```java
public Date getStartTime()
```
**SRS_JOBSRESPONSEPARSER_21_016: [**The getStartTime shall return the startTime value.**]**  

### getEndTime
```java
public Date getEndTime()
```
**SRS_JOBSRESPONSEPARSER_21_017: [**The getEndTime shall return the endTime value.**]**  

### getLastUpdatedTimeUTCDate
```java
public Date getLastUpdatedTime()
```
**SRS_JOBSRESPONSEPARSER_25_031: [**The getLastUpdatedTimeUTCDate shall return the LastUpdatedTime value.**]** 

### getMaxExecutionTimeInSeconds
```java
public Long getMaxExecutionTimeInSeconds()
```
**SRS_JOBSRESPONSEPARSER_21_018: [**The getMaxExecutionTimeInSeconds shall return the maxExecutionTimeInSeconds value.**]**  

### getJobType
```java
public String getJobType()
```
**SRS_JOBSRESPONSEPARSER_21_019: [**The getJobType shall return a String with the job type value.**]**  

### getJobStatus
```java
public String getJobStatus()
```
**SRS_JOBSRESPONSEPARSER_21_020: [**The getJobsStatus shall return a String with the job status value.**]**  

### getCloudToDeviceMethod
```java
public MethodParser getCloudToDeviceMethod()
```
**SRS_JOBSRESPONSEPARSER_21_021: [**The getCloudToDeviceMethod shall return the cloudToDeviceMethod value.**]**  

### getUpdateTwin
```java
public TwinState getUpdateTwin()
```
**SRS_JOBSRESPONSEPARSER_21_022: [**The getUpdateTwin shall return the updateTwin value.**]**  

### getFailureReason
```java
public String getFailureReason()
```
**SRS_JOBSRESPONSEPARSER_21_023: [**The getFailureReason shall return the failureReason value.**]**  

### getStatusMessage
```java
public String getStatusMessage()
```
**SRS_JOBSRESPONSEPARSER_21_024: [**The getStatusMessage shall return the statusMessage value.**]**  

### getJobStatistics
```java
public JobsStatisticsParser getJobStatistics()
```
**SRS_JOBSRESPONSEPARSER_21_025: [**The getJobStatistics shall return the jobStatistics value.**]**  

### getDeviceId
```java
public String getDeviceId()
```
**SRS_JOBSRESPONSEPARSER_21_026: [**The getDeviceId shall return the deviceId value.**]**  

### getParentJobId
```java
public String getParentJobId()
```
**SRS_JOBSRESPONSEPARSER_21_027: [**The getParentJobId shall return the parentJobId value.**]**  

### getError
```java
public JobQueryResponseError getError()
```
**SRS_JOBSRESPONSEPARSER_25_032: [**The getError shall return the error value.**]** 

### getOutcome
```java
public String getOutcome()
```
**SRS_JOBSRESPONSEPARSER_25_033: [**The getOutcome shall return the outcome value.**]** 