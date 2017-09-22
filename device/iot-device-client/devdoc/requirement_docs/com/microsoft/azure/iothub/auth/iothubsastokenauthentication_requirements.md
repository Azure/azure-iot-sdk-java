# IotHubSasTokenAuthentication Requirements

## Overview

This class holds all the authentication information needed for a device to connect to a Iot Hub using sas tokens or a device key

## References

## Exposed API

```java
public class IotHubSasTokenAuthentication
{
    public IotHubSasTokenAuthentication(String hostname, String deviceId, String deviceKey, String sharedAccessToken) throws SecurityException;
    public String getRenewedSasToken();
    public String getCurrentSasToken();
    public SSLContext getSSLContext();
    public void setTokenValidSecs(long tokenValidSecs) throws IllegalArgumentException;
    public boolean isRenewalNecessary();

    public void setPathToIotHubTrustedCert(String pathToCertificate) throws IOException;
    public void setIotHubTrustedCert(String certificate) throws IOException;
    public String getIotHubTrustedCert();
    public String getPathToIotHubTrustedCert();
}
```

### IotHubSasTokenAuthentication
```java
public IotHubSasTokenAuthentication(IotHubConnectionString connectionString);
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_002: [**This constructor shall save the provided connection string.**]**

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_003: [**This constructor shall generate a default IotHubSSLContext.**]**


### getRenewedSasToken
```java
public String getRenewedSasToken();
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_004: [**If the saved sas token has expired and there is a device key present, the saved sas token shall be renewed.**]**

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_005: [**This function shall return the saved sas token.**]**


### getCurrentSasToken
```java
public String getCurrentSasToken();
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_018: [**This function shall return the current sas token without renewing it.**]**


### getSSLContext
```java
public SSLContext getSSLContext();
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_008: [**This function shall return the generated IotHubSSLContext.**]**


### setTokenValidSecs
```java
public void setTokenValidSecs(long tokenValidSecs);
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_012: [**This function shall save the provided tokenValidSecs as the number of seconds that created sas tokens are valid for.**]**


### isRenewalNecessary
```java
public boolean isRenewalNecessary();
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [**If the saved sas token has expired and cannot be renewed, this function shall return true.**]**


### generateSSLContext
```java
private void generateSSLContext();
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_019: [**If this has a saved iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert as the trusted cert.**]**

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_020: [**If this has a saved path to a iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert path as the trusted cert.**]**

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_021: [**If this has no saved iotHubTrustedCert or path, This function shall create and save a new default IotHubSSLContext object.**]**


### setPathToCert
```java
public void setPathToIotHubTrustedCert(String pathToCertificate);
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_059: [**This function shall save the provided pathToCertificate.**]**

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_030: [**If the provided pathToCertificate is different than the saved path, this function shall set sslContextNeedsRenewal to true.**]**


### setUserCertificateString
```java
public void setIotHubTrustedCert(String certificate);
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_064: [**This function shall save the provided userCertificateString.**]**

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_031: [**If the provided certificate is different than the saved certificate, this function shall set sslContextNeedsRenewal to true.**]**

