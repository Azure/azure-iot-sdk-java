### HttpsRequest Requirements

## Overview

An HTTPS request.

## References

## Exposed API

```java
public class HttpsRequest
{
    public HttpsRequest(URL url, HttpsMethod method, byte[] body) throws TransportException;

    public HttpsResponse send() throws IOException;

    public HttpsRequest setHeaderField(String field, String value);
    public HttpsRequest setReadTimeoutMillis(int timeout);
    public HttpsRequest setSSLContext(IotHubSSLContext iotHubSSLContext) throws TransportException;
}
```


### HttpsRequest

```java
public HttpsRequest(URL url, HttpsMethod method, byte[] body) throws IOException;
```

**SRS_HTTPSREQUEST_11_001: [**The function shall open a connection with the given URL as the endpoint.**]**

**SRS_HTTPSREQUEST_11_002: [**The function shall write the body to the connection.**]**

**SRS_HTTPSREQUEST_11_004: [**The function shall use the given HTTPS method (i.e. GET) as the request method.**]**

**SRS_HTTPSREQUEST_11_005: [**If an IOException occurs in setting up the HTTPS connection, the function shall throw a TransportException.**]**


### HttpsResponse

```java
public HttpsResponse send() throws IOException;
```

**SRS_HTTPSREQUEST_11_008: [**The function shall send an HTTPS request as formatted in the constructor.**]**

**SRS_HTTPSREQUEST_11_009: [**The function shall return the HTTPS response received, including the status code, body, header fields, and error reason (if any).**]**

**SRS_HTTPSREQUEST_11_012: [**If an I/O exception occurs because of a bad response status code, the function shall attempt to flush or read the error stream so that the underlying HTTPS connection can be reused.**]**

**SRS_HTTPSREQUEST_34_017: [**If a NoRouteToHostException or UnknownHostException is thrown while connecting, this function shall throw a retryable TransportException.**]**

**SRS_HTTPSREQUEST_34_017: [**If an I/O exception occurs while calling getResponseStatus, this function shall wrap that exception into a TransportException and throw it.**]**


### setHeaderField

```java
public HttpsRequest setHeaderField(String field, String value);
```

**SRS_HTTPSREQUEST_11_013: [**The function shall set the header field with the given name to the given value.**]**


### setReadTimeoutMillis

```java
public HttpsRequest setReadTimeoutMillis(int timeout);
```

**SRS_HTTPSREQUEST_11_014: [**The function shall set the read timeout for the request to the given value.**]**


### setSSLContext

```java
public HttpsRequest setSSLContext(IotHubSSLContext iotHubSSLContext) throws TransportException;
```

**SRS_HTTPSREQUEST_25_015: [**The function shall throw SRS_HTTPSREQUEST_25_015 if argument is null.**]**

**SRS_HTTPSREQUEST_25_016: [**The function shall set the SSL context for the IotHub.**]**
