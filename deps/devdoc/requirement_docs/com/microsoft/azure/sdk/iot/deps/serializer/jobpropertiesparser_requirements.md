# JobPropertiesParser Requirements

## Overview

Representation of a JobProperties object with a Json deserializer and serializer.

## References


## Exposed API

```java
public class JobPropertiesParser
{
    public JobPropertiesParser(String json)
    public String toJson()

    public String getType()
    public void setType(String type)
    public String getInputBlobContainerUri()
    public void setInputBlobContainerUri(String inputBlobContainerUri)
    public String getOutputBlobContainerUri()
    public void setOutputBlobContainerUri(String outputBlobContainerUri)
    public String getJobId()
    public void setJobId(String jobId)
    public Date getStartTimeUtc()
    public void setStartTimeUtc(Date startTimeUtc)
    public Date getEndTimeUtc()
    public void setEndTimeUtc(Date endTimeUtc)
    public String getStatus()
    public void setStatus(String status)
    public int getProgress()
    public void setProgress(int progress)
    public boolean isExcludeKeysInExport()
    public void setExcludeKeysInExport(boolean excludeKeysInExport)
    public String getFailureReason()
    public void setFailureReason(String failureReason)
}
```

### JobPropertiesParser
```java
public JobPropertiesParser(String json)
```
**SRS_JOB_PROPERTIES_PARSER_34_001: [**The constructor shall create and return an instance of a JobPropertiesParser object based off the provided json.**]**

**SRS_JOB_PROPERTIES_PARSER_34_007: [**If the provided json is null or empty, an IllegalArgumentException shall be thrown.**]**

**SRS_JOB_PROPERTIES_PARSER_34_008: [**If the provided json cannot be parsed into a JobPropertiesParser object, an IllegalArgumentException shall be thrown.**]**

**SRS_JOB_PROPERTIES_PARSER_34_009: [**If the provided json is missing the field for jobId, or if its value is null or empty, an IllegalArgumentException shall be thrown.**]**


### toJson
```java
public String toJson()
```
**SRS_JOB_PROPERTIES_PARSER_34_002: [**This method shall return a json representation of this.**]**


### setJobId

```java
public void setJobId(String jobId)
```
**SRS_JOB_PROPERTIES_PARSER_34_005: [**If the provided jobId is null, an IllegalArgumentException shall be thrown.**]**
**SRS_JOB_PROPERTIES_PARSER_34_010: [**This method shall set the value of this object's JobId equal to the provided value.**]**


### setType
```java
public void setType(String type)
```
**SRS_JOB_PROPERTIES_PARSER_34_011: [**This method shall set the value of this object's type equal to the provided value.**]**


### getType
```java
public String getType()
```
**SRS_JOB_PROPERTIES_PARSER_34_012: [**This method shall return the value of this object's type.**]**


### setInputBlobContainerUri
```java
public void setInputBlobContainerUri(String inputBlobContainerUri)
```
**SRS_JOB_PROPERTIES_PARSER_34_013: [**This method shall set the value of this object's inputBlobContainerUri equal to the provided value.**]**


### getInputBlobContainerUri
```java
public String getInputBlobContainerUri()
```
**SRS_JOB_PROPERTIES_PARSER_34_014: [**This method shall return the value of this object's inputBlobContainerUri.**]**


### setOutputBlobContainerUri
```java
public void setOutputBlobContainerUri(String outputBlobContainerUri)
```
**SRS_JOB_PROPERTIES_PARSER_34_015: [**This method shall set the value of this object's outputBlobContainerUri equal to the provided value.**]**


### getOutputBlobContainerUri
```java
public String getOutputBlobContainerUri()
```
**SRS_JOB_PROPERTIES_PARSER_34_016: [**This method shall return the value of this object's outputBlobContainerUri.**]**


### getJobId
```java
public String getJobId()
```
**SRS_JOB_PROPERTIES_PARSER_34_018: [**This method shall return the value of this object's jobId.**]**


### setStartTimeUtc
```java
public void setStartTimeUtc(Date startTimeUtc)
```
**SRS_JOB_PROPERTIES_PARSER_34_019: [**This method shall set the value of this object's startTimeUtc equal to the provided value.**]**


### getStartTimeUtc
```java
public Date getStartTimeUtc()
```
**SRS_JOB_PROPERTIES_PARSER_34_020: [**This method shall return the value of this object's startTimeUtc.**]**


### setEndTimeUtc
```java
public void setEndTimeUtc(Date endTimeUtc)
```
**SRS_JOB_PROPERTIES_PARSER_34_021: [**This method shall set the value of this object's endTimeUtc equal to the provided value.**]**


### getEndTimeUtc
```java
public Date getEndTimeUtc()
```
**SRS_JOB_PROPERTIES_PARSER_34_022: [**This method shall return the value of this object's endTimeUtc.**]**


### setStatus
```java
public void setStatus(String status)
```
**SRS_JOB_PROPERTIES_PARSER_34_023: [**This method shall set the value of this object's status equal to the provided value.**]**


### getStatus
```java
public String getStatus()
```
**SRS_JOB_PROPERTIES_PARSER_34_024: [**This method shall return the value of this object's status.**]**


### setProgress
```java
public void setProgress(int progress)
```
**SRS_JOB_PROPERTIES_PARSER_34_025: [**This method shall set the value of this object's progress equal to the provided value.**]**


### getProgress
```java
public int getProgress()
```
**SRS_JOB_PROPERTIES_PARSER_34_026: [**This method shall return the value of this object's progress.**]**


### setExcludeKeysInExport
```java
public void setExcludeKeysInExport(boolean excludeKeysInExport)
```
**SRS_JOB_PROPERTIES_PARSER_34_027: [**This method shall set the value of this object's excludeKeysInExport equal to the provided value.**]**


### isExcludeKeysInExport
```java
public boolean isExcludeKeysInExport()
```
**SRS_JOB_PROPERTIES_PARSER_34_028: [**This method shall return the value of this object's excludeKeysInExport.**]**


### setFailureReason
```java
public void setFailureReason(String failureReason)
```
**SRS_JOB_PROPERTIES_PARSER_34_029: [**This method shall set the value of this object's failureReason equal to the provided value.**]**


### getFailureReason
```java
public String getFailureReason()
```
**SRS_JOB_PROPERTIES_PARSER_34_030: [**This method shall return the value of this object's failureReason.**]**