# QueryCollectionResponse Requirements

## Overview

A QueryCollectionResponse holds a query's page of response objects within an exposed Collection as well as the continuation token needed to get the next page of results.

## References

## Exposed API

```java
public class QueryCollectionResponse<E>
{
    QueryCollectionResponse(String jsonString, String continuationToken);
    QueryCollectionResponse(Collection<E> responseElementsCollection, String continuationToken);
    public String getContinuationToken();
    public Collection<E> getCollection();
}
```

### QueryCollectionResponse

```java
QueryCollectionResponse(String jsonString, String continuationToken);
```

**SRS_QUERY_COLLECTION_RESPONSE_34_001: [**If the provided jsonString is null or empty, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERY_COLLECTION_RESPONSE_34_002: [**This constructor shall parse the provided jsonString using the QueryResponseParser class into a Collection and save it.**]**

**SRS_QUERY_COLLECTION_RESPONSE_34_003: [**This constructor shall save the provided continuation token.**]**


```java
QueryCollectionResponse(Collection<E> responseElementsCollection, String continuationToken);
```

**SRS_QUERY_COLLECTION_RESPONSE_34_004: [**This constructor shall save the provided continuation token and Collection.**]**

**SRS_QUERY_COLLECTION_RESPONSE_34_007: [**If the provided Collection is null or empty, this function shall throw an IllegalArgumentException.**]**


### getContinuationToken

```java
public String getContinuationToken();
```

**SRS_QUERY_COLLECTION_RESPONSE_34_005: [**This function shall return the saved continuation token.**]**


### getCollection
```java
public Collection<E> getCollection();
```

**SRS_QUERY_COLLECTION_RESPONSE_34_006: [**This function shall return the saved Collection.**]**
