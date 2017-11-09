# IotHubSSLContext Requirements

## Overview

This class creates ssl context to be used to secure all the underlying transport.

## References

## Exposed API

```java
public final class IotHubSSLContext
{
    IotHubSSLContext() throws KeyManagementException, IOException, CertificateException;

    IotHubSSLContext(SSLContext sslContext);

    IotHubSSLContext(String cert, boolean isPath)
            throws KeyStoreException, KeyManagementException, IOException, CertificateException, NoSuchAlgorithmException;

    IotHubSSLContext(String publicKeyCertificateString, String privateKeyString)
            throws KeyManagementException, IOException, CertificateException, KeyStoreException;

    IotHubSSLContext(String publicKeyCertificateString, String privateKeyString, String cert, boolean isPath)
            throws KeyStoreException, KeyManagementException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException;
    
    SSLContext getSSLContext();
}
```


### IotHubSSLContext

```java
protected IotHubSSLContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException;
```

**SRS_IOTHUBSSLCONTEXT_25_001: [**The constructor shall create a default certificate to be used with IotHub.**]**

**SRS_IOTHUBSSLCONTEXT_25_002: [**The constructor shall create default SSL context for TLSv1.2.**]**

**SRS_IOTHUBSSLCONTEXT_25_003: [**The constructor shall create default TrustManagerFactory with the default algorithm.**]**

**SRS_IOTHUBSSLCONTEXT_25_004: [**The constructor shall create default KeyStore instance with the default type and initialize it.**]**

**SRS_IOTHUBSSLCONTEXT_25_005: [**The constructor shall set the above created certificate into a keystore.**]**

**SRS_IOTHUBSSLCONTEXT_25_006: [**The constructor shall initialize TrustManagerFactory with the above initialized keystore.**]**

**SRS_IOTHUBSSLCONTEXT_25_007: [**The constructor shall initialize SSL context with the above initialized TrustManagerFactory and a new secure random.**]**


```java
IotHubSSLContext(String cert, boolean isPath) throws KeyStoreException, KeyManagementException, IOException, CertificateException, NoSuchAlgorithmException;
```

**SRS_IOTHUBSSLCONTEXT_34_025: [**If the provided cert is a path, this function shall set the path of the default cert to the provided cert path.**]**

**SRS_IOTHUBSSLCONTEXT_34_026: [**If the provided cert is not a path, this function shall set the default cert to the provided cert.**]**


```java
IotHubSSLContext(SSLContext sslContext);
```

**SRS_IOTHUBSSLCONTEXT_34_027: [**This constructor shall save the provided ssl context.**]**

**SRS_IOTHUBSSLCONTEXT_34_028: [**If the provided sslContext is null, this function shall throw an IllegalArgumentException.**]**


```java
IotHubSSLContext(String publicKeyCertificateString, String privateKeyString);
```

**SRS_IOTHUBSSLCONTEXT_34_018: [**This constructor shall generate a temporary password to protect the created keystore holding the private key.**]**

**SRS_IOTHUBSSLCONTEXT_34_019: [**The constructor shall create default SSL context for TLSv1.2.**]**

**SRS_IOTHUBSSLCONTEXT_34_020: [**The constructor shall create a keystore containing the public key certificate and the private key.**]**

**SRS_IOTHUBSSLCONTEXT_34_021: [**The constructor shall initialize a default trust manager factory that accepts communications from Iot Hub.**]**

**SRS_IOTHUBSSLCONTEXT_34_024: [**The constructor shall initialize SSL context with its initialized keystore, its initialized TrustManagerFactory and a new secure random.**]**


```java
IotHubSSLContext(String publicKeyCertificateString, String privateKeyString, String cert, boolean isPath)
```

**SRS_IOTHUBSSLCONTEXT_34_040: [**If the provided cert is a path, this function shall set the path of the default cert to the provided cert path.**]**

**SRS_IOTHUBSSLCONTEXT_34_041: [**If the provided cert is not a path, this function shall set the default cert to the provided cert.**]**

**SRS_IOTHUBSSLCONTEXT_34_042: [**This constructor shall generate a temporary password to protect the created keystore holding the private key.**]**

**SRS_IOTHUBSSLCONTEXT_34_043: [**The constructor shall create default SSL context for TLSv1.2.**]**

**SRS_IOTHUBSSLCONTEXT_34_044: [**The constructor shall create a keystore containing the public key certificate and the private key.**]**

**SRS_IOTHUBSSLCONTEXT_34_045: [**The constructor shall initialize a default trust manager factory that accepts communications from Iot Hub.**]**

**SRS_IOTHUBSSLCONTEXT_34_046: [**The constructor shall initialize SSL context with its initialized keystore, its initialized TrustManagerFactory and a new secure random.**]**


### getSSLContext

```java
public SSLContext getSSLContext();
```

**SRS_IOTHUBSSLCONTEXT_25_017: [*This method shall return the value of sslContext.**]**