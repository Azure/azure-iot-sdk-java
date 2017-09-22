# DeviceClientConfig Requirements

## Overview

Configuration settings for an IoT Hub device client. Validates all user-defined settings.

## References

## Exposed API

```java
public final class DeviceClientConfig
{
    public DeviceClientConfig(IotHubConnectionString iotHubConnectionString) throws IllegalArgumentException;
    public DeviceClientConfig(IotHubConnectionString iotHubConnectionString, String publicKeyCertificate, boolean isPathForPublic, String privateKey, boolean isPathForPrivate);
    
    public IotHubX509Authentication getX509Authentication();
    public IotHubSasTokenAuthentication getSasTokenAuthentication();
    public IotHubConnectionString getIotHubConnectionString();
    
    public boolean isUseWebsocket();
    public void setUseWebsocket(boolean useWebsocket);
    
    public String getIotHubHostname();
    public String getIotHubName();
    public String getDeviceId();
    public int getReadTimeoutMillis();
    
    public void setMessageCallback(MessageCallback callback, Object context);
    public MessageCallback getDeviceTelemetryMessageCallback();
    public Object getDeviceTelemetryMessageContext();
    public void setDeviceMethodsMessageCallback(MessageCallback callback, Object context);
    public MessageCallback getDeviceMethodsMessageCallback();
    public Object getDeviceMethodsMessageContext();
    public void setDeviceTwinMessageCallback(MessageCallback callback, Object context);
    public MessageCallback getDeviceTwinMessageCallback();
    public Object getDeviceTwinMessageContext();
    
    public int getMessageLockTimeoutSecs();
    public AuthType getAuthenticationType();
    public void generateSSLContext() throws IOException;
    
    @Deprecated
    public void setPathToCert(String pathToCertificate) throws IOException;
    @Deprecated
    public String getPathToCertificate();
    @Deprecated
    public void setUserCertificateString(String userCertificateString) throws IOException;
    @Deprecated
    public String getUserCertificateString();
}
```

### DeviceClientConfig

```java
public DeviceClientConfig(IotHubConnectionString iotHubConnectionString) throws URISyntaxException;
```

** SRS_DEVICECLIENTCONFIG_34_046: [**If the provided `iotHubConnectionString` does not use x509 authentication, it shall be saved to a new IotHubSasTokenAuthentication object and the authentication type of this shall be set to SASToken.**]**

** SRS_DEVICECLIENTCONFIG_34_048: [**If an exception is thrown when creating the appropriate Authentication object, an IOException shall be thrown containing the details of that exception.**]**

** SRS_DEVICECLIENTCONFIG_21_034: [**If the provided `iotHubConnectionString` is null, the constructor shall throw IllegalArgumentException.**] **

** SRS_DEVICECLIENTCONFIG_34_076: [**If the provided `iotHubConnectionString` uses x509 authentication, the constructor shall throw an IllegalArgumentException.**] **


```java
public DeviceClientConfig(IotHubConnectionString iotHubConnectionString, String publicKeyCertificate, boolean isPathForPublic, String privateKey, boolean isPathForPrivate) throws IOException
```

** SRS_DEVICECLIENTCONFIG_34_069: [**If the provided connection string is null or does not use x509 auth, and IllegalArgumentException shall be thrown.**] **

** SRS_DEVICECLIENTCONFIG_34_069: [**This function shall generate a new SSLContext and set this to using X509 authentication.**] **

** SRS_DEVICECLIENTCONFIG_34_070: [**If any exceptions are encountered while generating the new SSLContext, an IOException shall be thrown.**] **


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


### getMessageValidSecs

```java
public long getTokenValidSecs();
```

** SRS_DEVICECLIENTCONFIG_11_005: [**If this is using Sas token authentication, then this function shall return the value of tokenValidSecs saved in it and 0 otherwise.**] **


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


### isUseWebsocket

```java
public boolean isUseWebsocket();
```

**SRS_DEVICECLIENTCONFIG_25_037: [**The function shall return true if websocket is enabled, false otherwise.**]**

### setUseWebsocket

```java
public void setUseWebsocket(boolean useWebsocket);
```

**SRS_DEVICECLIENTCONFIG_25_038: [**The function shall save `useWebsocket`.**]**


### getAuthenticationType
```java
public AuthType getAuthenticationType();
```

**SRS_DEVICECLIENTCONFIG_34_039: [**This function shall return the type of authentication that the config is set up to use.**]**

** SRS_DEVICECLIENTCONFIG_25_020: [**The function shall set the DeviceMethod message context.**] **

**SRS_DEVICECLIENTCONFIG_34_059: [**This function shall save the provided pathToCertificate.**]**


### getPathToCertificate
```java
public String getPathToCertificate();
```

**SRS_DEVICECLIENTCONFIG_34_063: [**This function shall return the saved path to certificate.**]**


### setUserCertificateString
```java
public void setUserCertificateString(String userCertificateString);
```

**SRS_DEVICECLIENTCONFIG_34_064: [**This function shall save the provided userCertificateString.**]**


### getUserCertificateString
```java
public String getUserCertificateString();
```

**SRS_DEVICECLIENTCONFIG_34_067: [**This function shall return the saved user certificate string.**]**


### generateSSLContext
```java
public void generateSSLContext() throws IOException;
```

**SRS_DEVICECLIENTCONFIG_34_070: [**If this is using SAS token authentication and there is a user certificate saved, this function shall call its sas token authentication object to generate SSLContext with the saved user certificate.**]**

**SRS_DEVICECLIENTCONFIG_34_071: [**If this is using SAS token authentication and there is a path to user certificate saved, this function shall call its sas token authentication object to generate SSLContext with the saved user certificate path.**]**

**SRS_DEVICECLIENTCONFIG_34_072: [**If this is using SAS token authentication and there is no path to user certificate saved nor a user certificate saved, this function shall call its sas token authentication object to generate a default SSLContext.**]**

**SRS_DEVICECLIENTCONFIG_34_073: [**If this is using X509 authentication and there is a user certificate saved, this function shall call its X509 authentication object to generate SSLContext with the saved user certificate.**]**

**SRS_DEVICECLIENTCONFIG_34_074: [**If this is using X509 authentication and there is a path to user certificate saved, this function shall call its X509 authentication object to generate SSLContext with the saved user certificate path.**]**

**SRS_DEVICECLIENTCONFIG_34_075: [**If this is using X509 authentication and there is no path to user certificate saved nor a user certificate saved, this function shall call its X509 authentication object to generate a default SSLContext.**]**

**SRS_DEVICECLIENTCONFIG_34_068: [**If any exceptions are thrown while generating SSLContext, this method shall throw an IOException.**]**


### getAuthenticationType
```java
public AuthType getAuthenticationType();
```

**SRS_DEVICECLIENTCONFIG_34_039: [**This function shall return the type of authentication that the config is set up to use.**]**


### getX509Authentication
```java
public IotHubX509Authentication getX509Authentication();
```

**SRS_DEVICECLIENTCONFIG_34_077: [**This function shall return the saved IotHubX509Authentication object.**]**


### getSasTokenAuthentication
```java
public IotHubSasTokenAuthentication getSasTokenAuthentication();
```

**SRS_DEVICECLIENTCONFIG_34_078: [**This function shall return the saved IotHubSasTokenAuthentication object.**]**


### getIotHubConnectionString
```java
public IotHubConnectionString getIotHubConnectionString();
```

**SRS_DEVICECLIENTCONFIG_34_079: [**This function shall return the saved IotHubConnectionString object.**]**
