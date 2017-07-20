# JobQueryResponseError Requirements

## Overview

Representation of a JobQueryResponseError for a Json deserializer.

## References

## Exposed API

```java
public class JobQueryResponseError
{
    public String getCode();
    public String getDescription();
    public String toJson();
    public JobQueryResponseError fromJson(String json) throws IOException;
}
```

### getCode
```java
public String getCode();
```
**SRS_JOB_QUERY_RESPONSE_ERROR_25_001: [**The getCode shall return the value of the code.**]**  

### getDescription
```java
public String getDescription();
```
**SRS_JOB_QUERY_RESPONSE_ERROR_25_002: [**The getDescription shall return the value of the Description.**]**  

### toJson
```java
public String toJson();
```
**SRS_JOB_QUERY_RESPONSE_ERROR_25_003: [**The method shall build the json with the values provided to this object.**]**  

### fromJson
```java
public JobQueryResponseError fromJson(String json) throws IOException;
```
**SRS_JOB_QUERY_RESPONSE_ERROR_25_004: [**This method shall save the values of `code` and `description` to this object.**]**  
**SRS_JOB_QUERY_RESPONSE_ERROR_25_005: [**This method shall throw IOException if either `code` and `description` is not present in the json.**]** 
**SRS_JOB_QUERY_RESPONSE_ERROR_25_006: [**This method shall throw IOException if parsing of json fails for any reason.**]** 
**SRS_JOB_QUERY_RESPONSE_ERROR_25_007: [**If the input json is null or empty then this method shall throw IllegalArgumentException.**]** 