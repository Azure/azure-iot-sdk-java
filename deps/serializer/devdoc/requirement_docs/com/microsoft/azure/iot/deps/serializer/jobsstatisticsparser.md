# JobsStatisticsParser Requirements

## Overview

Representation of a single Jobs statistics collection for a Json deserializer.

## References

[Schedule jobs on multiple devices](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-jobs)

## Exposed API

```java
public class JobsStatisticsParser
{
    public int getDeviceCount();
    public int getFailedCount();
    public int getSucceededCount();
    public int getRunningCount();
    public int getPendingCount();
}
```

### getDeviceCount
```java
public int getDeviceCount();
```
**SRS_JOBSSTATISTICSPARSER_21_001: [**The getDeviceCount shall return the value of the deviceCount counter.**]**  

### getFailedCount
```java
public int getFailedCount();
```
**SRS_JOBSSTATISTICSPARSER_21_002: [**The getFailedCount shall return the value of the failedCount counter.**]**  

### getSucceededCount
```java
public int getSucceededCount();
```
**SRS_JOBSSTATISTICSPARSER_21_003: [**The getSucceededCount shall return the value of the succeededCount counter.**]**  

### getRunningCount
```java
public int getRunningCount();
```
**SRS_JOBSSTATISTICSPARSER_21_004: [**The getRunningCount shall return the value of the runningCount counter.**]**  

### getPendingCount
```java
public int getPendingCount();
```
**SRS_JOBSSTATISTICSPARSER_21_005: [**The getPendingCount shall return the value of the pendingCount counter.**]**  
