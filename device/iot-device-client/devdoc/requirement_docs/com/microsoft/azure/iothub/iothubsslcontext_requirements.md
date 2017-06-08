# IotHubSSLContext Requirements

## Overview

This class creates ssl context to be used to secure all the underlying transport.

## References

## Exposed API

```java
public final class IotHubSSLContext
{
    protected IotHubSSLContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException;

    protected IotHubSSLContext(String cert, boolean isPath) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException;

    IotHubSSLContext(String pathToCertificate, String userCertificateString)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException

    public SSLContext getIotHubSSLContext();
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


### IotHubSSLContext

```java
protected IotHubSSLContext(String cert, boolean isPath) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException;
```

**SRS_IOTHUBSSLCONTEXT_25_008: [**The constructor shall throw IllegalArgumentException if any of the parameters are null.**]**

**SRS_IOTHUBSSLCONTEXT_25_009: [**The constructor shall create a certificate to be used with IotHub with cert only if it were a path by calling setValidCertPath**]**

**SRS_IOTHUBSSLCONTEXT_25_010: [**The constructor shall create a certificate with 'cert' if it were a not a path by calling setValidCert.**]**

**SRS_IOTHUBSSLCONTEXT_25_011: [**The constructor shall create default SSL context for TLSv1.2.**]**

**SRS_IOTHUBSSLCONTEXT_25_012: [**The constructor shall create default TrustManagerFactory with the default algorithm.**]**

**SRS_IOTHUBSSLCONTEXT_25_013: [**The constructor shall create default KeyStore instance with the default type and initialize it.**]**

**SRS_IOTHUBSSLCONTEXT_25_014: [**The constructor shall set the above created certificate into a keystore.**]**

**SRS_IOTHUBSSLCONTEXT_25_015: [**The constructor shall initialize TrustManagerFactory with the above initialized keystore.**]**

**SRS_IOTHUBSSLCONTEXT_25_016: [**The constructor shall initialize SSL context with the above initialized TrustManagerFactory and a new secure random.**]**


### getIotHubSSLContext

```java
IotHubSSLContext(String pathToCertificate, String userCertificateString)
        throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
```

**SRS_IOTHUBSSLCONTEXT_21_018: [**If the pathToCertificate is not null, the constructor shall create a certificate to be used with IotHub with cert by calling setValidCertPath**]**

**SRS_IOTHUBSSLCONTEXT_21_019: [**If the userCertificateString is not null, and pathToCertificate is null, the constructor shall create a certificate with 'cert' by calling setValidCert.**]**

**SRS_IOTHUBSSLCONTEXT_21_020: [**If both userCertificateString, and pathToCertificate are null, the constructor shall create a default certificate.**]**


### getIotHubSSLContext

```java
public SSLContext getIotHubSSLContext();
```

**SRS_IOTHUBSSLCONTEXT_25_017: [*This method shall return the value of sslContext.**]**