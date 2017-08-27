# DeviceClientConfig Requirements

## Overview

Configuration settings for an IoT Hub device client. Validates all user-defined settings.

## References

## Exposed API

```java
public final class DeviceClientConfig
{
    private long tokenValidSecs = 3600;
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 240000;
    public static final int DEFAULT_MESSAGE_LOCK_TIMEOUT_SECS = 180;

    public DeviceClientConfig(IotHubConnectionString iotHubConnectionString) throws URISyntaxException;
    public DeviceClientConfig(String iotHubHostname, String deviceId, String deviceKey, String sharedAccessToken) throws URISyntaxException;
    public String getIotHubName();
    public String getDeviceId();
    public String getDeviceKey();
    public boolean isUseWebsocket();
    public void setUseWebsocket(boolean useWebsocket);
    public String getSharedAccessToken();
    public long getTokenValidSecs();
    public int getReadTimeoutMillis();

    public String getPathToCertificate();
    public void setPathToCert(String pathToCertificate);

    public void setUserCertificateString(String userCertificateString);
    public String getUserCertificateString();

    public void setIotHubSSLContext(IotHubSSLContext iotHubSSLContext);
    public IotHubSSLContext getIotHubSSLContext();

    public void setMessageCallback(MessageCallback callback, Object context);

    public MessageCallback getMessageCallback();
    public Object getMessageContext();
    public int getMessageLockTimeoutSecs();

    public void setDeviceMethodMessageCallback(MessageCallback callback, Object context);
    public MessageCallback getDeviceMethodMessageCallback();
    public Object getDeviceMethodMessageContext();

    public void setDeviceTwinMessageCallback(MessageCallback callback, Object context);
    public MessageCallback getDeviceTwinMessageCallback();
    public Object getDeviceTwinMessageContext();

}
```

### DeviceClientConfig

```java
public DeviceClientConfig(IotHubConnectionString iotHubConnectionString) throws URISyntaxException;
```

** SRS_DEVICECLIENTCONFIG_21_033: [**The constructor shall save the IoT Hub hostname, hubname, device ID, device key, and device token, provided in the `iotHubConnectionString`.**] **

** SRS_DEVICECLIENTCONFIG_21_034: [**If the provided `iotHubConnectionString` is null, the constructor shall throw IllegalArgumentException.**] **


### getIotHubHostname

```java
public String getIotHubHostname();
```

** SRS_DEVICECLIENTCONFIG_11_002: [**The function shall return the IoT Hub hostname given in the constructor.**] **


### getIotHubName

```java
public String getIotHubName();
```

** SRS_DEVICECLIENTCONFIG_11_007: [**The function shall return the IoT Hub name given in the constructor, where the IoT Hub name is embedded in the IoT Hub hostname as follows: [IoT Hub name].[valid HTML chars]+.**] ** 


### getDeviceId

```java
public String getDeviceId();
```

** SRS_DEVICECLIENTCONFIG_11_003: [**The function shall return the device ID given in the constructor.**] **


### getDeviceKey

```java
public String getDeviceKey();
```

** SRS_DEVICECLIENTCONFIG_11_004: [**The function shall return the device key given in the constructor.**] **


### getSharedAccessToken

```java
public String getSharedAccessToken();
```

** SRS_DEVICECLIENTCONFIG_25_018: [**The function shall return the SharedAccessToken given in the constructor.**] **

### isUseWebsocket

```java
public boolean isUseWebsocket();
```

**SRS_DEVICECLIENTCONFIG_25_033: [**The function shall return true if websocket is enabled, false otherwise.**]**

### setUseWebsocket

```java
public void setUseWebsocket(boolean useWebsocket);
```

**SRS_DEVICECLIENTCONFIG_25_034: [**The function shall save `useWebsocket`.**]**

### getMessageValidSecs

```java
public long getMessageValidSecs();
```

** SRS_DEVICECLIENTCONFIG_11_005: [**The function shall return the value of tokenValidSecs.**] **

### setTokenValidSecs

```java
public setTokenValidSecs(long expiryTime);
```

** SRS_DEVICECLIENTCONFIG_25_008: [**The function shall set the value of tokenValidSecs.**] **

### getPathToCertificate

```java
public String getPathToCertificate();
```

** SRS_DEVICECLIENTCONFIG_25_027: [**The function shall return the value of the path to the certificate.**] **

### setPathToCert

```java
public void setPathToCert(String pathToCertificate);
```

** SRS_DEVICECLIENTCONFIG_25_028: [**The function shall set the path to the certificate**] **

### setUserCertificateString

```java
public void setUserCertificateString(String userCertificateString);
```

** SRS_DEVICECLIENTCONFIG_25_029: [**The function shall set user certificate String**] **

### getUserCertificateString

```java
public String getUserCertificateString();
```

** SRS_DEVICECLIENTCONFIG_25_030: [**The function shall return the value of the user certificate string.**] **

### setIotHubSSLContext

```java
public void setIotHubSSLContext(IotHubSSLContext iotHubSSLContext);
```

** SRS_DEVICECLIENTCONFIG_25_031: [**The function shall set IotHub SSL Context**] **

### getIotHubSSLContext

```java
public IotHubSSLContext getIotHubSSLContext();
```

** SRS_DEVICECLIENTCONFIG_25_032: [**The function shall return the IotHubSSLContext.**] **

### setMessageCallback

```java
public void setMessageCallback(MessageCallback  callback, Object context);
```

** SRS_DEVICECLIENTCONFIG_11_006: [**The function shall set the message callback, with its associated context.**] ** 


### getReadTimeoutMillis

```java
public int getReadTimeoutMillis();
```

** SRS_DEVICECLIENTCONFIG_11_012: [**The function shall return 240000ms.**] **


### getMessageCallback

```java
public MessageCallback getMessageCallback();
```

** SRS_DEVICECLIENTCONFIG_11_010: [**The function shall return the current message callback.**] ** 


### getMessageContext

```java
public Object getMessageContext();
```

** SRS_DEVICECLIENTCONFIG_11_011: [**The function shall return the current message context.**] **


### getMessageLockTimeoutSecs

```java
public int getMessageLockTimeoutSecs();
```

** SRS_DEVICECLIENTCONFIG_11_013: [**The function shall return 180s.**] **


### setDeviceMethodMessageCallback

```java
public void setDeviceMethodMessageCallback(MessageCallback  callback, Object context);
```

** SRS_DEVICECLIENTCONFIG_25_019: [**The function shall set the DeviceMethod message callback.**] ** 


** SRS_DEVICECLIENTCONFIG_25_020: [**The function shall set the DeviceMethod message context.**] **


### getDeviceMethodMessageCallback

```java
public MessageCallback getDeviceMethodMessageCallback();
```

** SRS_DEVICECLIENTCONFIG_25_021: [**The function shall return the current DeviceMethod message callback.**] ** 


### getDeviceMethodMessageContext

```java
public Object getDeviceMethodMessageContext();
```

** SRS_DEVICECLIENTCONFIG_25_022: [**The function shall return the current DeviceMethod message context.**] **

### setDeviceTwinMessageCallback

```java
public void setDeviceTwinMessageCallback(MessageCallback  callback, Object context);
```

** SRS_DEVICECLIENTCONFIG_25_023: [**The function shall set the DeviceTwin message callback.**] ** 

** SRS_DEVICECLIENTCONFIG_25_024: [**The function shall set the DeviceTwin message context.**] **


### getDeviceTwinMessageCallback

```java
public MessageCallback getDeviceTwinMessageCallback();
```

** SRS_DEVICECLIENTCONFIG_25_025: [**The function shall return the current DeviceTwin message callback.**] ** 


### getDeviceTwinMessageContext

```java
public Object getDeviceTwinMessageContext();
```

** SRS_DEVICECLIENTCONFIG_25_026: [**The function shall return the current DeviceTwin message context.**] **
