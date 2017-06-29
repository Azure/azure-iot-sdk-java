# Query Requirements

## Overview

A Query is used to send query request to IotHub for twins, jobs, raw request or device jobs. This class also parses a response to query and helps iterate over responses.

## References

## Exposed API

```java
public class Query
{    
    Query(String query, int pageSize, QueryType requestQueryType) throws IllegalArgumentException;

    void continueQuery(String continuationToken);
    void continueQuery(String continuationToken, int pageSize) throws IllegalArgumentException;

    QueryResponse sendQueryRequest(IotHubConnectionString iotHubConnectionString, URL url, HttpMethod method, Long timeoutInMs) throws IOException, IotHubException;
    String getContinuationToken();    
    boolean hasNext();
    Object next();
}
```

### Query

```java
Query(String query, int pageSize, QueryType requestQueryType) throws IllegalArgumentException;
```

**SRS_QUERY_25_001: [**The constructor shall validate query and save query, pagesize and request type**]**

**SRS_QUERY_25_002: [**If the query is null or empty or is not a valid sql query (containing select and from), the constructor shall throw an IllegalArgumentException.**]**

**SRS_QUERY_25_003: [**If the pagesize is zero or negative the constructor shall throw an IllegalArgumentException.**]**

**SRS_QUERY_25_004: [**If the QueryType is null or unknown then the constructor shall throw an IllegalArgumentException.**]**


### continueQuery

```java
void continueQuery(String continuationToken);
void continueQuery(String continuationToken, int pageSize) throws IllegalArgumentException;   
```

**SRS_QUERY_25_005: [**The method shall update the request continuation token and request pagesize which shall be used for processing subsequent query request.**]**

**SRS_QUERY_25_006: [**If the pagesize is zero or negative the constructor shall throw an IllegalArgumentException.**]**


### sendQueryRequest

```java
QueryResponse sendQueryRequest(IotHubConnectionString iotHubConnectionString, URL url, HttpMethod method, Long timeoutInMs) throws IOException, IotHubException;  
```

**SRS_QUERY_25_007: [**The method shall set the http headers  `x-ms-continuation` and `x-ms-max-item-count` with request continuation token and page size if they were not null.**]**

**SRS_QUERY_25_008: [**The method shall obtain the serilaized query by using `QueryRequestParser`.**]**

**SRS_QUERY_25_009: [**The method shall use the provided HTTP Method and send request to IotHub with the serialized body over the provided URL.**]**

**SRS_QUERY_25_010: [**The method shall read the continuation token (`x-ms-continuation`) and reponse type (`x-ms-item-type`) from the HTTP Headers and save it.**]**

**SRS_QUERY_25_011: [**If the request type and response does not match then the method shall throw IOException.**]**

**SRS_QUERY_25_012: [**If the response type is Unknown or not found then this method shall throw IOException.**]**

**SRS_QUERY_25_013: [**The method shall create a `QueryResponse` object with the contents from the response body and save it.**]**

### getContinuationToken

```java
String getContinuationToken();   
```

**SRS_QUERY_25_014: [**The method shall return the continuation token found in response to a query (which can be null).**]**

### hasNext

```java
public boolean hasNext();   
```

**SRS_QUERY_25_015: [**The method shall return true if next element from QueryResponse is available and false otherwise.**]**


### next

```java
public Object next(); 
```

**SRS_QUERY_25_016: [**The method shall return the next element for this QueryResponse.**]**
