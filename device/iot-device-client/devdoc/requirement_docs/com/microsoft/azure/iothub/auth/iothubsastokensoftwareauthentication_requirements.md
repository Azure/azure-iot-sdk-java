# IotHubSasTokenSoftwareAuthentication Requirements

## Overview

This class holds all the authentication information needed for a device to connect to a Iot Hub using sas tokens or a device key

## References

## Exposed API

```java
public class IotHubSasTokenAuthentication
{
    public IotHubSasTokenSoftwareAuthentication(String hostname, String deviceId, String deviceKey, String sharedAccessToken) throws SecurityException;

    public boolean isRenewalNecessary();
    public String getRenewedSasToken() throws IOException;

    public SSLContext getSSLContext() throws IOException;

    public void setPathToIotHubTrustedCert(String pathToCertificate);
    public void setIotHubTrustedCert(String certificate);
}
```

### IotHubSasTokenAuthentication
```java
public IotHubSasTokenSoftwareAuthentication(String hostname, String deviceId, String deviceKey, String sharedAccessToken) throws SecurityException;
```

**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_002: [**This constructor shall save the provided hostname, device id, deviceKey, and sharedAccessToken.**]**


### getRenewedSasToken
```java
public String getRenewedSasToken() throws IOException;
```

**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_004: [**If the saved sas token has expired and there is a device key present, the saved sas token shall be renewed.**]**

**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_005: [**This function shall return the saved sas token.**]**


### isRenewalNecessary
```java
public boolean isRenewalNecessary();
```

**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_017: [**If the saved sas token has expired and cannot be renewed, this function shall return true.**]**


### setPathToCert
```java
public void setPathToIotHubTrustedCert(String pathToCertificate);
```

**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_059: [**This function shall save the provided pathToCertificate.**]**

**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_030: [**If the provided pathToCertificate is different than the saved path, this function shall set sslContextNeedsRenewal to true.**]**


### setUserCertificateString
```java
public void setIotHubTrustedCert(String certificate);
```

**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_064: [**This function shall save the provided userCertificateString.**]**

**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_031: [**If the provided certificate is different than the saved certificate, this function shall set sslContextNeedsRenewal to true.**]**


### getSSLContext
```java
public SSLContext getSSLContext() throws IOException
```
**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_006: [**If a CertificateException, NoSuchAlgorithmException, KeyManagementException, or KeyStoreException is thrown during this function, this function shall throw an IOException.**]**
**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_007: [**If this object's ssl context has not been generated yet or if it needs to be re-generated, this function shall regenerate the ssl context.**]**
**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_008: [**This function shall return the generated IotHubSSLContext.**]**



### generateSSLContext
```java
private void generateSSLContext();
```

**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_019: [**If this has a saved iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert as the trusted cert.**]**

**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_020: [**If this has a saved path to a iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert path as the trusted cert.**]**

**SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_021: [**If this has no saved iotHubTrustedCert or path, This function shall create and save a new default IotHubSSLContext object.**]**


