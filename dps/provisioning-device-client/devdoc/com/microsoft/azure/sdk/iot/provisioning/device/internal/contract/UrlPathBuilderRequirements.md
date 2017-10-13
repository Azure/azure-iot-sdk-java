# UrlPathBuilder Requirements

## Overview

Generates a url/path for different operations that can be performed on the service.

## References

## Exposed API

```java
public class UrlPathBuilder 
{
    public UrlPathBuilder(String scopeId) throws IllegalArgumentException;
    public UrlPathBuilder(String hostName, String scopeId, ProvisioningDeviceClientTransportProtocol protocol) throws IllegalArgumentException;
    public String generateSasTokenUrl(String registrationId) throws UnsupportedEncodingException;
    public String generateRegisterUrl(String registrationId) throws IOException;
    public String generateRequestUrl(String registrationId, String operationsId) throws IOException;
}
```

### UrlPathBuilder

```java
    public UrlPathBuilder(String scopeId) throws IllegalArgumentException;
```
**SRS_UrlPathBuilder_25_001: [** Constructor shall save `scopeId`.**]**

**SRS_UrlPathBuilder_25_002: [** Constructor throw IllegalArgumentException if `scopeId` is null or empty.**]**

### UrlPathBuilder

```java
    public UrlPathBuilder(String hostName, String scopeId, ProvisioningDeviceClientTransportProtocol protocol) throws IllegalArgumentException;
```
**SRS_UrlPathBuilder_25_003: [** The constructor shall throw IllegalArgumentException if the `scopeId` or `hostName` string is empty or null or if `protocol` is null.**]**

**SRS_UrlPathBuilder_25_004: [** The constructor shall save the `scopeId` or `hostName` string and  `protocol`. **]**

### generateSasTokenUrl

```java
    public String generateSasTokenUrl(String registrationId) throws UnsupportedEncodingException;
```
**SRS_UrlPathBuilder_25_005: [** This method shall throw IllegalArgumentException if the registration id is null or empty. **]**

**SRS_UrlPathBuilder_25_006: [** This method shall create a String using the following format after Url Encoding: 
`<scopeid>/registrations/<registrationId>` **]**



### generateRegisterUrl

```java
public String generateRegisterUrl(String registrationId) throws IOException;
```
**SRS_UrlPathBuilder_25_007: [** This method shall throw IllegalArgumentException if the registration id is null or empty. **]**

**SRS_UrlPathBuilder_25_008: [** This method shall create a String using the following format: 
HTTP - `https://<HostName>/<Scope>/registrations/<Registration ID>/register?api-version=<Service API Version>`
MQTT - TBD
AMQP - TBD **]**

### generateRequestUrl

```java
public String generateRequestUrl(String registrationId, String operationsId) throws IOException;
```
**SRS_UrlPathBuilder_25_009: [** This method shall throw IllegalArgumentException if the registration id or operation id is null or empty. **]**

**SRS_UrlPathBuilder_25_010: [** This method shall create a String using the following format: 
HTTP - `https://<HostName>/<Scope>/registrations/<Registration ID>/operations/<operationId>?api-version=<Service API Version>`
MQTT - TBD
AMQP - TBD **]**