# JobStatistics Requirements

## Overview

Collection of jobs statistics.

## References

[Schedule jobs on multiple devices](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-jobs)


## Exposed API

```java
public class JobStatistics 
{
    JobStatistics(JobsStatisticsParser jobsStatisticsParser) throws IllegalArgumentException;
    public int getDeviceCount();
    public int getFailedCount();
    public int getSucceededCount();
    public int getRunningCount();
    public int getPendingCount();
}
```

### JobStatistics
```java
JobStatistics(JobsStatisticsParser jobsStatisticsParser) throws IllegalArgumentException;
```
**SRS_JOBSTATISTICS_21_001: [**The constructor shall throw IllegalArgumentException if the input jobsStatisticsParser is null.**]**  
**SRS_JOBSTATISTICS_21_002: [**The constructor shall locally store all statistics information in jobsStatisticsParser.**]**  

### getDeviceCount
```java
public int getDeviceCount()
```
**SRS_JOBSTATISTICS_21_003: [**The getDeviceCount shall return the stored device count.**]**  

### getDeviceCount
```java
public int getFailedCount()
```
**SRS_JOBSTATISTICS_21_004: [**The getFailedCount shall return the stored failed count.**]**  

### getDeviceCount
```java
public int getSucceededCount()
```
**SRS_JOBSTATISTICS_21_005: [**The getSucceededCount shall return the stored succeeded count.**]**  

### getDeviceCount
```java
public int getRunningCount()
```
**SRS_JOBSTATISTICS_21_006: [**The getRunningCount shall return the stored running count.**]**  

### getDeviceCount
```java
public int getPendingCount()
```
**SRS_JOBSTATISTICS_21_007: [**The getPendingCount shall return the stored pending count.**]**  
