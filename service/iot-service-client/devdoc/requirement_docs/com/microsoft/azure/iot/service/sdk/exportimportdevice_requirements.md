# ImportExportDevice Requirements

## Overview

The ImportExportDevice class stores device parameters and it is used by the bulk import/export operations.

## References

## Exposed API

```java
public class ExportImportDevice
{
    public ExportImportDevice()
    public ExportImportDevice(String deviceId, AuthenticationType authenticationType)

    public void setId(String id)
    public String getId()
    public String getETag()
    public void setETag(String eTag)
    public ImportMode getImportMode()
    public void setImportMode(ImportMode importMode)
    public DeviceStatus getStatus()
    public void setStatus(DeviceStatus status)
    public String getStatusReason()
    public void setStatusReason(String statusReason)
    public Authentication getAuthentication()
    public void setAuthentication(Authentication authentication)
}
```


**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_IMPORT_EXPORT_DEVICE_15_001: [** The ExportImportDevice class shall have the following properties: Id,
Etag, ImportMode, Status, StatusReason, Authentication **]**

### ExportImportDevice
```java
public ExportImportDevice()
```
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_050: [**This constructor shall automatically set the authentication type of this object to be sas, and shall generate a deviceId and symmetric key.**]**


```java
public ExportImportDevice(String deviceId, AuthenticationType authenticationType)
```
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_051: [**This constructor shall save the provided deviceId and authenticationType to itself.**]**
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_057: [**If either the provided deviceId or authenticationType is null or empty, an IllegalArgumentException shall be thrown.**]**


```java
ExportImportDevice(ExportImportDeviceParser parser)
```
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_052: [**This constructor shall use the properties of the provided parser object to set the new ExportImportDevice's properties.**]**
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_053: [**If the provided parser does not have values for the properties deviceId or authentication, an IllegalArgumentException shall be thrown.**]**
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_058: [**If the provided parser uses SAS authentication and is missing one or both symmetric keys, two new keys will be generated.**]**
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_059: [**If the provided parser uses self signed authentication and is missing one or both thumbprints, two new thumbprints will be generated.**]**


### toExportImportDeviceParser
```java
ExportImportDeviceParser toExportImportDeviceParser()
```
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_054: [**This method shall convert this into an ExportImportDeviceParser object and return it.**]**
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_060: [**If this device uses sas authentication, but does not have a primary and secondary symmetric key saved, an IllegalStateException shall be thrown.**]**
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_061: [**If this device uses self signed authentication, but does not have a primary and secondary thumbprint saved, an IllegalStateException shall be thrown.**]**


```java
public void setId(String id)
```
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_055: [**If the provided id is null, an IllegalArgumentException shall be thrown.**]**


```java
public void setAuthentication(Authentication authentication)
```
**SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_056: [**If the provided authentication is null, an IllegalArgumentException shall be thrown.**]**