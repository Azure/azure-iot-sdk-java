# QueryCollection Requirements

## Overview

A QueryCollection is used to send query request to IotHub for twins, jobs, raw request or device jobs. 
This class also parses a response to query. It returns the full page of results for a query.

## References

## Exposed API

```java
public class QueryCollection
{
    protected QueryCollection(String query, int pageSize, QueryType requestQueryType, IotHubConnectionString iotHubConnectionString, URL url, HttpMethod httpMethod, long timeout);
    protected QueryCollection(int pageSize, QueryType requestQueryType, IotHubConnectionString iotHubConnectionString, URL url, HttpMethod httpMethod, long timeout);
    
    protected boolean hasNext();
    protected QueryCollectionResponse<String> next() throws IOException, IotHubException;
    protected QueryCollectionResponse<String> next(QueryOptions options) throws IOException, IotHubException;
    
    protected Integer getPageSize();
}
```

### QueryCollection

```java
protected QueryCollection(String query, int pageSize, QueryType requestQueryType, IotHubConnectionString iotHubConnectionString, URL url, HttpMethod httpMethod, long timeout);
```

**SRS_QUERYCOLLECTION_34_001: [**If the provided query string is invalid or does not contain both SELECT and FROM, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_002: [**If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_004: [**If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_006: [**This function shall save the provided query, pageSize, requestQueryType, iotHubConnectionString, url, httpMethod and timeout.**]**

**SRS_QUERYCOLLECTION_34_008: [**The constructed QueryCollection shall be a sql query type.**]**

**SRS_QUERYCOLLECTION_34_037: [**If the provided connection string, url, or http method is null, this function shall throw an IllegalArgumentException.**]**


```java
protected QueryCollection(int pageSize, QueryType requestQueryType, IotHubConnectionString iotHubConnectionString, URL url, HttpMethod httpMethod, long timeout);
```

**SRS_QUERYCOLLECTION_34_003: [**If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_005: [**If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_007: [**This function shall save the provided pageSize, requestQueryType, iotHubConnectionString, url, httpMethod and timeout.**]**

**SRS_QUERYCOLLECTION_34_009: [**The constructed QueryCollection shall not be a sql query type.**]**

**SRS_QUERYCOLLECTION_34_038: [**If the provided connection string, url, or http method is null, this function shall throw an IllegalArgumentException.**]**


### sendQueryRequest

```java
private QueryCollectionResponse<String> sendQueryRequest(QueryOptions options)
```

**SRS_QUERYCOLLECTION_34_011: [**If the provided query options is not null and contains a continuation token, it shall be put in the query headers to continue the query.**]**

**SRS_QUERYCOLLECTION_34_012: [**If a continuation token is not provided from the passed in query options, but there is a continuation token saved in the latest queryCollectionResponse, that token shall be put in the query headers to continue the query.**]**

**SRS_QUERYCOLLECTION_34_013: [**If the provided query options is not null, the query option's page size shall be included in the query headers.**]**

**SRS_QUERYCOLLECTION_34_014: [**If the provided query options is null, this object's page size shall be included in the query headers.**]**

**SRS_QUERYCOLLECTION_34_015: [**If this is a sql query, the payload of the query message shall be set to the json bytes representation of this object's query string.**]**

**SRS_QUERYCOLLECTION_34_016: [**If this is not a sql query, the payload of the query message shall be set to empty bytes.**]**

**SRS_QUERYCOLLECTION_34_017: [**This function shall send an HTTPS request using DeviceOperations.**]**

**SRS_QUERYCOLLECTION_34_018: [**The method shall read the continuation token (x-ms-continuation) and response type (x-ms-item-type) from the HTTP Headers and save it.**]**

**SRS_QUERYCOLLECTION_34_019: [**If the response type is Unknown or not found then this method shall throw IOException.**]**

**SRS_QUERYCOLLECTION_34_020: [**If the request type and response does not match then the method shall throw IOException.**]**

**SRS_QUERYCOLLECTION_34_021: [**The method shall create a QueryResponse object with the contents from the response body and its continuation token and save it.**]**


### hasNext

```java
protected boolean hasNext();
```

**SRS_QUERYCOLLECTION_34_025: [**If this query is the initial query, this function shall return true.**]**

**SRS_QUERYCOLLECTION_34_026: [**If this query is not the initial query, this function shall return true if there is a continuation token and false otherwise.**]**


### next

```java
protected QueryCollectionResponse<String> next() throws IOException, IotHubException;
```

**SRS_QUERYCOLLECTION_34_032: [**If this object has a next set to return, this function shall return it.**]**

**SRS_QUERYCOLLECTION_34_033: [**If this object does not have a next set to return, this function shall return null.**]**


```java
protected QueryCollectionResponse<String> next(QueryOptions options) throws IOException, IotHubException;
```

**SRS_QUERYCOLLECTION_34_034: [**If this object has a next set to return using the provided query options, this function shall return it.**]**

**SRS_QUERYCOLLECTION_34_035: [**If this object does not have a next set to return, this function shall return null.**]**


### getPageSize
```java
protected Integer getPageSize();
```

**SRS_QUERYCOLLECTION_34_036: [**This function shall return the saved page size.**]**
