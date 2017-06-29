# SqlQuery Requirements

## Overview

SqlQuery is used to create Sql Style query for IotHub for twins and Jobs.

## References

[IoTHub Query.doc](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language)

## Exposed API


```java
public class SqlQuery 
{
    public static SqlQuery createSqlQuery(String selection, FromType fromType, String where, String groupby) throws IOException;

    public String getQuery();
}
```

### createSqlQuery

```java
public static SqlQuery createSqlQuery(String selection, FromType fromType, String where, String groupby) throws IOException;
```
**SRS_SQL_QUERY_25_001: [** The constructor shall throw IllegalArgumentException if either input string `selection` or `fromType` is null or empty **]**

**SRS_SQL_QUERY_25_002: [** The constructor shall build the sql query string from the given Input **]**

**SRS_SQL_QUERY_25_003: [** The constructor shall append `where` to the sql query string only when provided **]**

**SRS_SQL_QUERY_25_004: [** The constructor shall append `groupby` to the sql query string only when provided **]**

**SRS_SQL_QUERY_25_005: [** The constructor shall create a new SqlQuery instance and return it **]**

### getQuery

```java
 public String getQuery();
```
**SRS_SQL_QUERY_25_006: [** The method shall return the sql query string built **]**