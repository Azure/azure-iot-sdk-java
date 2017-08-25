# Device Requirements

## Overview

The Device class is the main DTO for the RegistryManager class. It implements static constructors and stores device parameters.

## References

([IoT Hub SDK.doc](https://microsoft.sharepoint.com/teams/Azure_IoT/_layouts/15/WopiFrame.aspx?sourcedoc={9A552E4B-EC00-408F-AE9A-D8C2C37E904F}&file=IoT%20Hub%20SDK.docx&action=default))

## Exposed API

```java
public class Device
{
    public static Device createFromId(String deviceId, DeviceStatus status, SymmetricKey symmetricKey)
    public static Device createDevice(String deviceId, AuthenticationType authenticationType)
    protected Device(String deviceId, DeviceStatus status, SymmetricKey symmetricKey)
}
```
**SRS_SERVICE_SDK_JAVA_DEVICE_12_001: [** The Device class shall have the following properties: Id, Etag, SymmetricKey, State, StateReason, StateUpdatedTime, ConnectionState, ConnectionStateUpdatedTime, LastActivityTime, symmetricKey, thumbprint, status, authentication**]**

### createFromId

```java
public static Device createFromId(String deviceId, DeviceStatus status, SymmetricKey symmetricKey) throws IllegalArgumentException, NoSuchAlgorithmException;
```
**SRS_SERVICE_SDK_JAVA_DEVICE_12_002: [** The function shall throw IllegalArgumentException if the input string is empty or null **]**

**SRS_SERVICE_SDK_JAVA_DEVICE_12_003: [** The function shall create a new instance of Device using the given id and return with it **]**

### createDevice

```java
public static Device createDevice(String deviceId, AuthenticationType authenticationType)
```
**SRS_SERVICE_SDK_JAVA_DEVICE_34_009: [**The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.**]**

**SRS_SERVICE_SDK_JAVA_DEVICE_34_010: [**The function shall create a new instance of Device using the given id and return it.**]**


### Device

```java
private Device(String deviceId, DeviceStatus status, SymmetricKey symmetricKey) throws NoSuchAlgorithmException, IllegalArgumentException;
```
**SRS_SERVICE_SDK_JAVA_DEVICE_12_004: [**The constructor shall throw IllegalArgumentException if the input string is empty or null.**]**

**SRS_SERVICE_SDK_JAVA_DEVICE_12_005: [**If the input symmetric key is empty, the constructor shall create a new SymmetricKey instance using AES encryption and store it into a member variable.**]**

**SRS_SERVICE_SDK_JAVA_DEVICE_12_006: [**The constructor shall initialize all properties to default values.**]**

**SRS_SERVICE_SDK_JAVA_DEVICE_15_007: [**The constructor shall store the input device status and symmetric key into a member variable.**]**


```java
private Device(String deviceId, AuthenticationType authenticationType)
```
**SRS_SERVICE_SDK_JAVA_DEVICE_34_011: [**If the provided authenticationType is certificate authority, no symmetric key shall be generated and no thumbprint shall be generated**]**

**SRS_SERVICE_SDK_JAVA_DEVICE_34_012: [**If the provided authenticationType is sas, a symmetric key shall be generated but no thumbprint shall be generated**]**

**SRS_SERVICE_SDK_JAVA_DEVICE_34_013: [**If the provided authenticationType is self signed, a thumbprint shall be generated but no symmetric key shall be generated**]**


```java
Device(DeviceParser parser)
```
**SRS_SERVICE_SDK_JAVA_DEVICE_34_014: [**This constructor shall create a new Device object using the values within the provided parser.**]**

**SRS_SERVICE_SDK_JAVA_DEVICE_34_015: [**If the provided parser is missing a value for its authentication or its device Id, an IllegalArgumentException shall be thrown.**]**

**SRS_SERVICE_SDK_JAVA_DEVICE_34_016: [**If the provided parser uses SAS authentication and is missing one or both symmetric keys, an IllegalArgumentException shall be thrown.**]**

**SRS_SERVICE_SDK_JAVA_DEVICE_34_017: [**If the provided parser uses selfSigned authentication and is missing one or both thumbprint, an IllegalArgumentException shall be thrown.**]**


### toDeviceParser
```java
DeviceParser toDeviceParser()
```
**SRS_SERVICE_SDK_JAVA_DEVICE_34_018: [**This method shall return a new instance of a DeviceParser object that is populated using the properties of this.**]**
**SRS_SERVICE_SDK_JAVA_DEVICE_34_019: [**If this device uses sas authentication, but does not have a primary and secondary symmetric key saved, an IllegalStateException shall be thrown.**]**
**SRS_SERVICE_SDK_JAVA_DEVICE_34_020: [**If this device uses self signed authentication, but does not have a primary and secondary thumbprint saved, an IllegalStateException shall be thrown.**]**
