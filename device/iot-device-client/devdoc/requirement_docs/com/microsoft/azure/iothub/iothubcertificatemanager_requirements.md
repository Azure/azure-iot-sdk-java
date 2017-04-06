# IotHubCertificateManager Requirements

## Overview

This class creates ssl context to be used to secure all the underlying transport.

## References

## Exposed API

```java
public final class IotHubCertificateManager
{
    IotHubCertificateManager();

    void setValidCertPath(String certPath) throws IOException;
    void setValidCert(String cert) throws IOException;

    Collection<? extends Certificate> getCertificateCollection() throws CertificateException, IOException;
}
```


### IotHubCertificateManager

```java
IotHubCertificateManager();
```

**SRS_IOTHUBCERTIFICATEMANAGER_25_001: [**The constructor shall set the valid certificate to be default certificate unless changed by user.**]**

### setValidCertPath

```java
void setValidCertPath(String certPath) throws IOException;
```

**SRS_IOTHUBCERTIFICATEMANAGER_25_002: [**This method shall throw IllegalArgumentException if parameter is null.**]**

**SRS_IOTHUBCERTIFICATEMANAGER_25_003: [**This method shall attempt to read the contents of the certificate file from the path provided and save it as valid certificate.**]**

**SRS_IOTHUBCERTIFICATEMANAGER_25_004: [**This method shall throw IllegalArgumentException if certificate contents were empty.**]**

**SRS_IOTHUBCERTIFICATEMANAGER_25_005: [**This method shall throw FileNotFoundException if it could not be found or does not exist.**]**

**SRS_IOTHUBCERTIFICATEMANAGER_25_006: [*If a user attempted to set the certificate and for some reason could not succeed then this method shall not use default certificate by setting valid certificate as null.**]**

### setValidCert

```java
void setValidCert(String cert) throws IOException
```
**SRS_IOTHUBCERTIFICATEMANAGER_25_007: [**This method shall throw IllegalArgumentException if parameter is null.**]**

**SRS_IOTHUBCERTIFICATEMANAGER_25_008: [*This method shall save the cert provided by the user as valid cert to be used to communicate with IotHub.**]**

**SRS_IOTHUBCERTIFICATEMANAGER_25_009: [*If a user attempted to set the certificate and for some reason could not succeed then this method shall not use default certificate by setting valid certificate as null.**]**

### getCertificateCollection

```java
Collection<? extends Certificate> getCertificateCollection() throws CertificateException, IOException;
```
**SRS_IOTHUBCERTIFICATEMANAGER_25_010: [**This method shall throw IOException if valid certificate was not defined.**]**

**SRS_IOTHUBCERTIFICATEMANAGER_25_011: [*This method shall create a collection of all the certificates defined as valid using CertificateFactory instance for "X.509".**]**
