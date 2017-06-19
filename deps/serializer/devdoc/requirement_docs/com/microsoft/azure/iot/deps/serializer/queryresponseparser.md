# File Upload Response Requirements

## Overview

QueryResponseParser deserializes the response received for a query request as json.

## References

[Query for IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language)

## Exposed API

```java
/**
 * Ex of JSON format:
 *  {
       type: <unknown|twin|deviceJob|jobResponse|raw>,
       items: [Array of objects, can be deserialized to concrete types based on the ‘type’ above],
       continuationToken: <for pagination>
 *  }
 */
public class QueryResponseParser
{
    public QueryResponseParser(String json) throws IllegalArgumentException;

    public String getType();
    public String getJsonItemsArray();
    public String getContinuationToken();
    public List<TwinParser> getTwins() throws IllegalStateException, IllegalArgumentException;
    public List getDeviceJobs() throws IllegalStateException, IllegalArgumentException;
    public List getJobs() throws IllegalStateException, IllegalArgumentException;
    public List<String> getRawData() throws IllegalStateException;        
}
```

### QueryResponseParser
```java
public QueryResponseParser(String json) throws IllegalArgumentException;
```
**SRS_QUERY_RESPONSE_PARSER_25_001: [**The constructor shall create an instance of the QueryResponseParser.**]**  
**SRS_QUERY_RESPONSE_PARSER_25_002: [**The constructor shall parse the provided json and initialize `type`, `continuationToken` and `jsonItems` using the information in the json.**]**  
**SRS_QUERY_RESPONSE_PARSER_25_003: [**If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.**]**  
**SRS_QUERY_RESPONSE_PARSER_25_004: [**If the provided json do not contains a valid `type`, `continuationToken` and `jsonItems`, the constructor shall throws IllegalArgumentException.**]**  
**SRS_QUERY_RESPONSE_PARSER_25_005: [**If the provided json do not contains one of the keys `type`, `continuationToken` and `jsonItems`, the constructor shall throws IllegalArgumentException.**]**  
**SRS_QUERY_RESPONSE_PARSER_25_006: [**If the provided json is of `type` other than twin, raw, deviceJob or jobResponse, the constructor shall throws IllegalArgumentException.**]**  

### getType
```java
public String getType();
```
**SRS_QUERY_RESPONSE_PARSER_25_007: [**The getType shall return the string stored in `type` enum.**]**  

### getJsonItemsArray
```java
public String getJsonItemsArray();
```
**SRS_QUERY_RESPONSE_PARSER_25_008: [**The getJsonItemsArray shall return the array of json items as string .**]**  

### getContinuationToken
```java
public String getContinuationToken();
```
**SRS_QUERY_RESPONSE_PARSER_25_009: [**The getContinuationToken shall return the string stored in `continuationToken`.**]**  

### getTwins
```java
public List<TwinParser> getTwins() throws IllegalStateException, IllegalArgumentException;
```
**SRS_QUERY_RESPONSE_PARSER_25_010: [**The getTwins shall return the collection of twin parsers as retrieved and parsed from json.**]**  
**SRS_QUERY_RESPONSE_PARSER_25_011: [**The getTwins shall throw IllegalStateException if the type represented by json is not "twin"**]**  
**SRS_QUERY_RESPONSE_PARSER_25_012: [**The getTwins shall throw IllegalArgumentException if the twin array from the json cannot be parsed**]**  

### getDeviceJobs
```java
public List getDeviceJobs() throws IllegalStateException, IllegalArgumentException;
```
**SRS_QUERY_RESPONSE_PARSER_25_013: [**The getDeviceJobs shall return the collection of device jobs parsers as retrieved and parsed from json.**]**  
**SRS_QUERY_RESPONSE_PARSER_25_014: [**The getDeviceJobs shall throw IllegalStateException if the type represented by json is not "deviceJobs"**]**  
**SRS_QUERY_RESPONSE_PARSER_25_015: [**The getDeviceJobs shall throw IllegalArgumentException if the items array from the json cannot be parsed**]**  

### getJobs
```java
public List getJobs() throws IllegalStateException, IllegalArgumentException;
```
**SRS_QUERY_RESPONSE_PARSER_25_016: [**The getJobs shall return the collection of jobs parsers as retrieved and parsed from json.**]**  
**SRS_QUERY_RESPONSE_PARSER_25_017: [**The getJobs shall throw IllegalStateException if the type represented by json is not "jobResponse"**]**  
**SRS_QUERY_RESPONSE_PARSER_25_018: [**The getJobs shall throw IllegalArgumentException if the jobs array from the json cannot be parsed**]**  

### getRawData
```java
public List<String> getRawData() throws IllegalStateException;
```
**SRS_QUERY_RESPONSE_PARSER_25_019: [**The getRawData shall return the collection of raw data json as string as retrieved and parsed from json.**]**  
**SRS_QUERY_RESPONSE_PARSER_25_020: [**The getRawData shall throw IllegalStateException if the type represented by json is not "raw"**]**  
**SRS_QUERY_RESPONSE_PARSER_25_021: [**The getRawData shall throw IllegalArgumentException if the raw data array from the json cannot be parsed**]**  
