# ExportImportDeviceParser Requirements

## Overview

Representation of an ExportImportDevice with a Json deserializer and serializer.

## References


## Exposed API

```java
public class ExportImportDeviceParser
{
    public static ExportImportDeviceParser fromJson(String json);
    public static String toJson(ExportImportDeviceParser device);

    public String getId()
    public void setId(String id)
    public String getETag()
    public void setETag(String eTag)
    public String getImportMode()
    public void setImportMode(String importMode)
    public String getStatus()
    public void setStatus(String status)
    public String getStatusReason()
    public void setStatusReason(String statusReason)
    public AuthenticationParser getAuthentication()
    public void setAuthentication(AuthenticationParser authentication)
}
```

### toJson
```java
public static String toJson(ExportImportDeviceParser device);
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_001: [**The parser shall save the ExportImportDeviceParser's authentication to the returned json representation**]**


### ExportImportDeviceParser
```java
public ExportImportDeviceParser(String json);
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_002: [**The parser shall look for the authentication of the serialized export import device and save it to the returned ExportImportDeviceParser instance**]**

**SRS_EXPORTIMPORTDEVICE_PARSER_34_005: [**This constructor shall take the provided json and convert it into a new ExportImportDeviceParser and return it.**]**

**SRS_EXPORTIMPORTDEVICE_PARSER_34_008: [**If the provided json is missing the Id field, or its value is empty, an IllegalArgumentException shall be thrown**]**

**SRS_EXPORTIMPORTDEVICE_PARSER_34_009: [**If the provided json is missing the Authentication field, or its value is empty, an IllegalArgumentException shall be thrown.**]**

**SRS_EXPORTIMPORTDEVICE_PARSER_34_011: [**If the provided json is null, empty, or cannot be parsed into an ExportImportDeviceParser object, an IllegalArgumentException shall be thrown.**]**


### setAuthentication
```java
public void setAuthentication(AuthenticationParser authentication)
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_006: [**If the provided authentication is null, an IllegalArgumentException shall be thrown.**]**
**SRS_EXPORTIMPORTDEVICE_PARSER_34_023: [**This method shall set the value of this object's AuthenticationParser equal to the provided value.**]**


### getAuthentication
```java
public AuthenticationParser getAuthentication()
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_012: [**This method shall return the value of this object's AuthenticationParser.**]**


### setId
```java
public void setId(String id)
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_007: [**If the provided id is null, an IllegalArgumentException shall be thrown.**]**
**SRS_EXPORTIMPORTDEVICE_PARSER_34_022: [**This method shall set the value of this object's Id equal to the provided value.**]**


### getId
```java
public String getId()
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_013: [**This method shall return the value of this object's Id.**]**


### setETag
```java
public void setETag(String eTag)
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_014: [**This method shall set the value of this object's eTag equal to the provided value.**]**


### getETag
```java
public String getETag()
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_015: [**This method shall return the value of this object's eTag.**]**


### setImportMode
```java
public void setImportMode(String importMode)
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_016: [**This method shall set the value of this object's importMode equal to the provided value.**]**


### getImportMode
```java
public String getImportMode()
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_017: [**This method shall return the value of this object's importMode.**]**


### setStatus
```java
public void setStatus(String status)
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_018: [**This method shall set the value of this object's status equal to the provided value.**]**


### getStatus
```java
public String getStatus()
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_019: [**This method shall return the value of this object's status.**]**


### setStatusReason
```java
public void setStatusReason(String statusReason)
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_020: [**This method shall set the value of this object's statusReason equal to the provided value.**]**


### getStatusReason
```java
public String getStatusReason()
```
**SRS_EXPORTIMPORTDEVICE_PARSER_34_021: [**This method shall return the value of this object's statusReason.**]**