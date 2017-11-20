# IotHubX509Authentication Requirements

## Overview

This class holds all the authentication information needed for a device to connect to a Iot Hub using x509 authentication

## References

## Exposed API

```java
public class IotHubX509Authentication
{
    public IotHubX509Authentication(String publicKeyCertificate, boolean isPathForPublic, String privateKey, boolean isPathForPrivate) throws IllegalArgumentException;
    public SSLContext getSSLContext() throws IOException;
    
    @Override
    public void setIotHubTrustedCert(String certificate);
        
    @Override
    public void setPathToIotHubTrustedCert(String pathToCertificate);
}
```

### IotHubX509Authentication
```java
public IotHubX509Authentication(IotHubConnectionString iotHubConnectionString, String publicKeyCertificate, boolean isPathForPublic, String privateKey, boolean isPathForPrivate);
```

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_002: [**This constructor will create and save an IotHubX509 object using the provided public key certificate and private key.**]**


### getSSLContext
```java
public SSLContext getSSLContext();
```

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_003: [**If this object's ssl context has not been generated yet, this function shall generate it from the saved security client.**]**

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_004: [**If the security client throws a SecurityClientException while generating an SSLContext, this function shall throw an IOException.**]**

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_005: [**This function shall return the saved IotHubSSLContext.**]**


### setPathToCert
```java
public void setPathToIotHubTrustedCert(String pathToCertificate);
```

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_059: [**This function shall save the provided pathToCertificate.**]**

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_030: [**If the provided pathToCertificate is different than the saved path, this function shall set sslContextNeedsRenewal to true.**]**

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_033: [**If this object was created using a constructor that takes an SSLContext, this function shall throw an UnsupportedOperationException.**]**


### setUserCertificateString
```java
public void setIotHubTrustedCert(String certificate);
```

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_064: [**This function shall save the provided userCertificateString.**]**

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_031: [**If the provided certificate is different than the saved certificate, this function shall set sslContextNeedsRenewal to true.**]**

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_032: [**If this object was created using a constructor that takes an SSLContext, this function shall throw an UnsupportedOperationException.**]**


### generateSSLContext
```java
public void generateSSLContext() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, UnrecoverableKeyException;
```

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_019: [**If this has a saved iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert as the trusted cert and with the saved public and private key combo.**]**

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_020: [**If this has a saved path to a iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert path as the trusted cert and with the saved public and private key combo.**]**

**SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_021: [**If this has no saved iotHubTrustedCert or path, This function shall create and save a new IotHubSSLContext object with the saved public and private key combo.**]**

