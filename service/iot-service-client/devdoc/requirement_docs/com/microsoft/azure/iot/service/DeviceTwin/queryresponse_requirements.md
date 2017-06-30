# QueryResponse Requirements

## Overview

A QueryResponse implements iterator. This class parses a response to query and helps iterate over responses.

## References

## Exposed API

```java
public final class QueryResponse implements Iterator<Object>
{    
    QueryResponse(String jsonString) throws IOException;  
    @Override
    public boolean hasNext();    
    @Override
    public Object next();    
}
```

### QueryResponse

```java
QueryResponse(String jsonString) throws IOException;
```

**SRS_QUERY_RESPONSE_25_001: [**The constructor shall parse the json response using `QueryResponseParser` and set the iterator.**]**

**SRS_QUERY_RESPONSE_25_002: [**If the jsonString is null or empty, the constructor shall throw an IllegalArgumentException.**]**


### hasNext

```java
public boolean hasNext();   
```

**SRS_QUERY_RESPONSE_25_003: [**The method shall return true if next element from QueryResponse is available and false otherwise.**]**


### next

```java
public Object next(); 
```

**SRS_QUERY_RESPONSE_25_004: [**The method shall return the next element for this QueryResponse.**]**
