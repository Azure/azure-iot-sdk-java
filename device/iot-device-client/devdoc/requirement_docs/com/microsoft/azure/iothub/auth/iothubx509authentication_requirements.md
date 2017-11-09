# IotHubX509Authentication Requirements

## Overview

This class holds all the authentication information needed for a device to connect to a Iot Hub using x509 authentication

## References

## Exposed API

```java
public class IotHubX509Authentication
{
    public abstract SSLContext getSSLContext() throws IOException;
    public abstract void setPathToIotHubTrustedCert(String pathToCertificate) throws IOException;
    public abstract void setIotHubTrustedCert(String certificate) throws IOException;
}
```

