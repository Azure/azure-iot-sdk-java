# IotHubX509 Requirements
 
## Overview

Holds a public key certificate and a private key that are used for x509 authentication.

## References

## Exposed API


```java
public final class IotHubX509
{
    IotHubX509(String publicKeyCertificate, boolean isPathForPublic, String privateKey, boolean isPathForPrivate) throws IllegalArgumentException;
    String getPublicKeyCertificate() throws IOException;
    String getPrivateKey() throws IOException;
}
```


### IotHubX509

```java
IotHubX509(String publicKeyCertificate, boolean isPathForPublic, String privateKey, boolean isPathForPrivate) throws IOException, IllegalArgumentException;
```
    
**SRS_IOTHUBX509_34_001: [**If the provided public key certificate or private key is null or empty, an IllegalArgumentException shall be thrown.**]**

**SRS_IOTHUBX509_34_013: [**If a path is provided for the public key certificate, the path will be saved and the contents of the file shall be read and saved as a string.**]**

**SRS_IOTHUBX509_34_014: [**If the public key certificate is not provided as a path, no path will be saved and the value of the public key certificate will be saved as a string.**]**

**SRS_IOTHUBX509_34_015: [**If a path is provided for the private key, the path will be saved and the contents of the file shall be read and saved as a string.**]**

**SRS_IOTHUBX509_34_016: [**If the private key is not provided as a path, no path will be saved and the value of the private key will be saved as a string.**]**


### getPublicKeyCertificate
```java
String getPublicKeyCertificate() throws IOException;
```

**SRS_IOTHUBX509_34_017: [**If the public key certificate was provided as a path in the constructor, this function shall read the public key certificate from its file.**]**

**SRS_IOTHUBX509_34_018: [**This function shall return the saved public key certificate string.**]**

    
### getPrivateKey
```java
String getPrivateKey() throws IOException;
```

**SRS_IOTHUBX509_34_019: [**If the private key was provided as a path in the constructor, this function shall read the private key from its file.**]**

**SRS_IOTHUBX509_34_020: [**This function shall return the saved private key string.**]**

