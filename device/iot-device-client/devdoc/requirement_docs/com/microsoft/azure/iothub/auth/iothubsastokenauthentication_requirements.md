# IotHubSasTokenAuthentication Requirements

## Overview

This class holds all the authentication information needed for a device to connect to a Iot Hub using sas tokens or a device key

## References

## Exposed API

```java
public class IotHubSasTokenAuthentication
{
    public abstract void setPathToIotHubTrustedCert(String pathToCertificate);
    public abstract void setIotHubTrustedCert(String certificate);
    public abstract SSLContext getSSLContext() throws IOException;
    public abstract String getRenewedSasToken() throws IOException;

    public String getCurrentSasToken();
    public void setTokenValidSecs(long tokenValidSecs);
    Long getExpiryTimeInSeconds();
    public boolean isRenewalNecessary();
    public long getTokenValidSecs();
}
```

### getCurrentSasToken
```java
public String getCurrentSasToken();
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_018: [**This function shall return the current sas token without renewing it.**]**


### getTokenValidSecs

```java
public long getTokenValidSecs();
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_12_001: [**This function shall return the tokenValidSecs as the number of seconds the current sas token valid for.**]**


### setTokenValidSecs
```java
public void setTokenValidSecs(long tokenValidSecs);
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_012: [**This function shall save the provided tokenValidSecs as the number of seconds that created sas tokens are valid for.**]**


### isRenewalNecessary
```java
public boolean isRenewalNecessary();
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [**If the saved sas token has expired, this function shall return true.**]**


### getExpiryTimeInSeconds
```java
Long getExpiryTimeInSeconds();
```

**SRS_IOTHUBSASTOKENAUTHENTICATION_34_001: [**This function shall return the number of seconds from the UNIX Epoch that a sas token constructed now would expire.**]**

