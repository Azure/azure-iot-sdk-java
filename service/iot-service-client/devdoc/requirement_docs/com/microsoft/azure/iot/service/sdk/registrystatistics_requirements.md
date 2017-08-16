# RegistryStatistics Requirements

## Overview

The RegistryStatistics class stores statistics regarding the device registry, such as device count.

## References

## Exposed API
public class RegistryStatistics()
{
    public long getDisabledDeviceCount()
    public long getEnabledDeviceCount()
    public long getTotalDeviceCount()
}


### RegistryStatistics
```java
RegistryStatistics(RegistryStatisticsParser parser)
```
**SRS_SERVICE_SDK_JAVA_REGISTRY_STATISTICS_34_001: [**This method shall convert the provided parser into a RegistryStatistics object and return it.**]**
**SRS_SERVICE_SDK_JAVA_REGISTRY_STATISTICS_34_003: [**If the provided RegistryStatisticsParser object is null, an IllegalArgumentException shall be thrown.**]**


### toRegistryStatisticsParser
```java
RegistryStatisticsParser toRegistryStatisticsParser()
```
**SRS_SERVICE_SDK_JAVA_REGISTRY_STATISTICS_34_002: [**This method shall convert this into a RegistryStatisticsParser object and return it.**]**