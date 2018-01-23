# JobsParser Requirements

## Overview

Representation of a single Jobs collection with a Json serializer.

## References

[Schedule jobs on multiple devices](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-jobs)

## Exposed API

```java
public class JobsParser
{
    public JobsParser(
            String jobId, MethodParser cloudToDeviceMethod,
            String queryCondition, Date startTime, long maxExecutionTimeInSeconds)
            throws IllegalArgumentException;
    public JobsParser(
            String jobId, TwinState updateTwin,
            String queryCondition, Date startTime, long maxExecutionTimeInSeconds)
            throws IllegalArgumentException;

    public String toJson();

    private void commonFields(
            String jobId, String queryCondition, Date startTime, long maxExecutionTimeInSeconds)
            throws IllegalArgumentException;
}
```

### JobsParser
```java
public JobsParser(
        String jobId, MethodParser cloudToDeviceMethod,
        String queryCondition, Date startTime, long maxExecutionTimeInSeconds)
        throws IllegalArgumentException;
```
**SRS_JOBSPARSER_21_001: [**The constructor shall evaluate and store the commons parameters using the internal function commonFields.**]**  
**SRS_JOBSPARSER_21_002: [**If any common parameter is invalid, the constructor shall throws IllegalArgumentException.**]**  
**SRS_JOBSPARSER_21_003: [**The constructor shall store the JsonElement for the cloudToDeviceMethod.**]**  
**SRS_JOBSPARSER_21_004: [**If the cloudToDeviceMethod is null, the constructor shall throws IllegalArgumentException.**]**  
**SRS_JOBSPARSER_21_005: [**The constructor shall set the jobType as scheduleDeviceMethod.**]**  
**SRS_JOBSPARSER_21_006: [**The constructor shall set the updateTwin as null.**]**  

### JobsParser
```java
public JobsParser(
        String jobId, TwinState updateTwin,
        String queryCondition, Date startTime, long maxExecutionTimeInSeconds)
        throws IllegalArgumentException;
```
**SRS_JOBSPARSER_21_007: [**The constructor shall evaluate and store the commons parameters using the internal function commonFields.**]**  
**SRS_JOBSPARSER_21_008: [**If any common parameter is invalid, the constructor shall throws IllegalArgumentException.**]**  
**SRS_JOBSPARSER_21_009: [**The constructor shall store the JsonElement for the updateTwin.**]**  
**SRS_JOBSPARSER_21_010: [**If the updateTwin is null, the constructor shall throws IllegalArgumentException.**]**  
**SRS_JOBSPARSER_21_011: [**The constructor shall set the jobType as scheduleUpdateTwin.**]**  
**SRS_JOBSPARSER_21_012: [**The constructor shall set the cloudToDeviceMethod as null.**]**  

### toJson
```java
public String toJson();
```
**SRS_JOBSPARSER_21_013: [**The toJson shall return a String with a json that represents the content of this class.**]**  

### commonFields
```java
private void commonFields(
        String jobId, String queryCondition, Date startTime, long maxExecutionTimeInSeconds)
        throws IllegalArgumentException;
```
**SRS_JOBSPARSER_21_014: [**The commonFields shall store the jobId, queryCondition, and maxExecutionTimeInSeconds.**]**  
**SRS_JOBSPARSER_21_015: [**If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.**]**  
**SRS_JOBSPARSER_21_017: [**If the maxExecutionTimeInSeconds is negative, the commonFields shall throws IllegalArgumentException.**]**  
**SRS_JOBSPARSER_21_018: [**The commonFields shall format startTime as a String and store it.**]**  
**SRS_JOBSPARSER_21_019: [**If the startTime is null, the commonFields shall throws IllegalArgumentException.**]**  
