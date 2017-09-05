# HttpResponse Requirements

## Overview

An HTTP response. Contains the status code, body, header fields, and error reason (if any).

## References

## Exposed API

```java
public class HttpResponse
{
    public static int NOT_RECEIVED = -1;
    public HttpResponse(int status, String body, Map<String, List<String>> headerFields, String errorReason);
    public int getStatus();
    public String getBody();
    public String getHeaderField(String field);
    public String getErrorReason();
}
```

### HttpResponse

```java
public HttpResponse(int status, String body, Map<String, List<String>> headerFields, String errorReason);
```
**SRS_HTTPRESPONSE_25_001: [** The constructor shall store the input arguments so that the getters can return them later. **]**

### getStatus

```java
public int getStatus();
```
**SRS_HTTPRESPONSE_25_002: [** The function shall return the status code given in the constructor. **]**

### getBody

```java
public String getBody();
```
**SRS_HTTPRESPONSE_25_003: [** The function shall return a copy of the body given in the constructor. **]**

### getHeaderField

```java
public String getHeaderField(String field);
```
**SRS_HTTPRESPONSE_25_004: [** The function shall return a comma-separated list of the values associated with the header field name. **]**

**SRS_HTTPRESPONSE_25_005: [** The function shall match the header field name in a case-insensitive manner. **]**

**SRS_HTTPRESPONSE_25_006: [** If a value could not be found for the given header field name, the function shall throw an IllegalArgumentException. **]**

### getErrorReason

```java
public byte[] getErrorReason();
```
**SRS_HTTPRESPONSE_25_007: [** The function shall return the error reason given in the constructor. **]**

