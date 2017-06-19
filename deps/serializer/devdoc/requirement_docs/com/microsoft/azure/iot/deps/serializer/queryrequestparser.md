# Query Request Request Requirements

## Overview

QueryRequestParser is a representation of a single Query Request with a Json serializer.

## References

[Query Request with IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language)

## Exposed API

```java
/**
 * Representation of a single Query Request request with a Json serializer.
 * Ex of JSON format:
 *  {
 *      "query": "sql query"
 *  }
 */
public class QueryRequestParser
{
    public QueryRequestParser(String query) throws IllegalArgumentException;
    public String toJson();
}
```


### CONSTRUCTOR
```java
public QueryRequestParser(String query) throws IllegalArgumentException;
```
**SRS_QUERY_REQUEST_PARSER_25_001: [**The constructor shall create an instance of the QueryRequestParser.**]**  
**SRS_QUERY_REQUEST_PARSER_25_002: [**The constructor shall set the `query` value with the provided query.**]**  
**SRS_QUERY_REQUEST_PARSER_25_003: [**If the provided query is null, empty, or not valid, the constructor shall throws IllegalArgumentException.**]**  

### toJson
```java
public String toJson()
```
**SRS_QUERY_REQUEST_PARSER_25_004: [**The toJson shall return a string with a json that represents the contents of the QueryRequestParser.**]**  
