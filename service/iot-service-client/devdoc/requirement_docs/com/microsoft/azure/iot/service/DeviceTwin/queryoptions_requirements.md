# QueryOptions Requirements

## Overview

QueryOptions represents the options allowed to a user when constructing a query. This includes the page size, and if 
they have a continuation token.

## References

## Exposed API

```java
public class QueryOptions
{
    public QueryOptions();
    public String getContinuationToken();
    public void setContinuationToken(String continuationToken) throws IllegalArgumentException;
    public Integer getPageSize();
    public void setPageSize(Integer pageSize) throws IllegalArgumentException;
}
```

### QueryOptions

```java
public QueryOptions();
```

**SRS_QUERYOPTIONS_34_001: [**This constructor shall initialize a QueryOptions object with a default page size of 100 and no continuation token.**]**


### getContinuationToken
```java
public String getContinuationToken();
```

**SRS_QUERYOPTIONS_34_002: [**This function shall return the saved continuation token.**]**


### setContinuationToken

```java
public void setContinuationToken(String continuationToken) throws IllegalArgumentException;
```

**SRS_QUERYOPTIONS_34_004: [**If the provided continuation token is null or empty, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYOPTIONS_34_006: [**This function shall save the provided continuation token string.**]**


### getPageSize

```java
public Integer getPageSize();
```

**SRS_QUERYOPTIONS_34_003: [**This function shall return the saved page size.**]**


### setPageSize

```java
public void setPageSize(Integer pageSize) throws IllegalArgumentException;
```

**SRS_QUERYOPTIONS_34_005: [**If the provided page size is null or is not a positive integer, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYOPTIONS_34_007: [**This function shall save the provided page size.**]**
