# RawTwinQuery Requirements

## Overview

RawTwinQuery is used to request Sql Style query to IotHub for twins. The response of the raw query is a json document as per the query request.

## References

[IoTHub Query.doc](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language)

## Exposed API


```java
public class RawTwinQuery 
{
    public static RawTwinQuery createFromConnectionString(String connectionString) throws IOException;

    public synchronized Query query(String sqlQuery, Integer pageSize) throws IotHubException, IOException;
    public synchronized Query query(String sqlQuery) throws IotHubException, IOException;

    public synchronized boolean hasNext(Query query) throws IotHubException, IOException;
    public synchronized String next(Query query) throws IOException, IotHubException, NoSuchElementException;
}
```

### createFromConnectionString

```java
public static RawTwinQuery createFromConnectionString(String connectionString) throws IOException;
```
**SRS_RAW_QUERY_25_001: [** The constructor shall throw IllegalArgumentException if the input string is null or empty **]**

**SRS_RAW_QUERY_25_002: [** The constructor shall create an IotHubConnectionStringBuilder object from the given connection string **]**

**SRS_RAW_QUERY_25_003: [** The constructor shall create a new RawTwinQuery instance and return it **]**

### query

```java
 public synchronized Query query(String sqlQuery, Integer pageSize) throws IotHubException, IOException;
 public synchronized Query query(String sqlQuery) throws IotHubException, IOException;
```
**SRS_RAW_QUERY_25_004: [** The method shall throw IllegalArgumentException if the query is null or empty.**]**

**SRS_RAW_QUERY_25_005: [** The method shall throw IllegalArgumentException if the page size is zero or negative.**]**

**SRS_RAW_QUERY_25_006: [** The method shall build the URL for this operation by calling getUrlTwinQuery **]**

**SRS_RAW_QUERY_25_007: [** The method shall create a new Query Object of Type Raw. **]**

**SRS_RAW_QUERY_25_008: [** The method shall send a Query Request to IotHub as HTTP Method Post on the query Object by calling `sendQueryRequest`.**]**

**SRS_RAW_QUERY_25_009: [** If the pagesize if not provided then a default pagesize of 100 is used for the query.**]**

### hasNext

```java
public synchronized boolean hasNext(Query query) throws IotHubException, IOException;
```
**SRS_RAW_QUERY_25_010: [** The method shall throw IllegalArgumentException if query is null **]**

**SRS_RAW_QUERY_25_012: [** If a queryResponse is available, this method shall return true as is to the user, and false otherwise. **]**

### next

```java
public synchronized String next(Query query) throws IOException, IotHubException, NoSuchElementException;
```

**SRS_RAW_QUERY_25_015: [** The method shall check if hasNext returns true and throw NoSuchElementException otherwise **]**

**SRS_RAW_QUERY_25_016: [** The method shall return the next element from the query response.**]**

**SRS_RAW_QUERY_25_017: [** If the next element from the query response is an object other than String, then this method shall throw IOException **]**

**SRS_RAW_QUERY_25_018: [** If the input query is null, then this method shall throw IllegalArgumentException **]**

