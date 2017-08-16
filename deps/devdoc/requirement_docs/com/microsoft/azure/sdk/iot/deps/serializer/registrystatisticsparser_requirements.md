# RegistryStatisticsParser Requirements

## Overview

Representation of a RegistryStatistics object with a Json deserializer and serializer.

## References


## Exposed API

```java
public class RegistryStatisticsParser
{
    public RegistryStatisticsParser(String json);
    public String toJson();

    public long getTotalDeviceCount()
    public void setTotalDeviceCount(long totalDeviceCount)
    public long getEnabledDeviceCount()
    public void setEnabledDeviceCount(long enabledDeviceCount)
    public long getDisabledDeviceCount()
    public void setDisabledDeviceCount(long disabledDeviceCount)
}
```

### toJson
```java
public String toJson();
```
**SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_001: [**This method shall return a json representation of this.**]**


### fromJson
```java
public RegistryStatisticsParser(String json);
```
**SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_002: [**This constructor shall create and return an instance of a RegistryStatisticsParser object based off the provided json.**]**

**SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_003: [**If the provided json is null, empty, or cannot be parsed into a RegistryStatisticsParser object, an IllegalArgumentException shall be thrown.**]**


### setTotalDeviceCount
```java
public void setTotalDeviceCount(long totalDeviceCount)
```
**SRS_JOB_PROPERTIES_PARSER_34_004: [**This method shall set the value of this object's totalDeviceCount equal to the provided value.**]**


### getTotalDeviceCount
```java
public long getTotalDeviceCount()
```
**SRS_JOB_PROPERTIES_PARSER_34_005: [**This method shall return the value of this object's totalDeviceCount.**]**


### setEnabledDeviceCount
```java
public void setEnabledDeviceCount(long enabledDeviceCount)
```
**SRS_JOB_PROPERTIES_PARSER_34_006: [**This method shall set the value of this object's enabledDeviceCount equal to the provided value.**]**


### getEnabledDeviceCount
```java
public long getEnabledDeviceCount()
```
**SRS_JOB_PROPERTIES_PARSER_34_007: [**This method shall return the value of this object's enabledDeviceCount.**]**


### setDisabledDeviceCount
```java
public void setDisabledDeviceCount(long disabledDeviceCount)
```
**SRS_JOB_PROPERTIES_PARSER_34_008: [**This method shall set the value of this object's disabledDeviceCount equal to the provided value.**]**


### getDisabledDeviceCount
```java
public long getDisabledDeviceCount()
```
**SRS_JOB_PROPERTIES_PARSER_34_009: [**This method shall return the value of this object's disabledDeviceCount.**]**