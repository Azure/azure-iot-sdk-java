# IotHubSasTokenHardwareAuthenticationProvider Requirements

## Overview

This class holds all the authentication information needed for a device to connect to a Iot Hub using sas tokens generated from a SecurityProvider instance

## References

## Exposed API

```java
public class IotHubSasTokenHardwareAuthenticationProvider
{
    public IotHubSasTokenHardwareAuthenticationProvider(String hostname, String deviceId, SecurityProvider securityProvider) throws IOException;

    public String getRenewedSasToken() throws IOException;

    public SSLContext getSSLContext() throws IOException;

    public void setPathToIotHubTrustedCert(String pathToCertificate);
    public void setIotHubTrustedCert(String certificate);
}
```

### IotHubSasTokenHardwareAuthenticationProvider
```java
public IotHubSasTokenHardwareAuthenticationProvider(String hostname, String deviceId, SecurityProvider securityProvider) throws IOException;
```

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_003: [**If the provided security provider is not an instance of SecurityProviderTpm, this function shall throw an IllegalArgumentException.**]**

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_032: [**This constructor shall save the provided security provider, hostname, and device id.**]**

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_033: [**This constructor shall generate and save a sas token from the security provider with the default time to live.**]**

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_034: [**This constructor shall retrieve and save the ssl context from the security provider.**]**

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_023: [**If the security provider throws an exception while retrieving a sas token or ssl context from it, this function shall throw an IOException.**]**

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_009: [**If the token scope cannot be encoded, this function shall throw an IOException.**]**

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_010: [**If the call for the saved security provider to sign with identity returns null or empty bytes, this function shall throw an IOException.**]**

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_011: [**When generating the sas token signature from the security provider, if an UnsupportedEncodingException or SecurityClientException is thrown, this function shall throw an IOException.**]**


### getRenewedSasToken
```java
public String getRenewedSasToken() throws IOException;
```

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_035: [**If the saved sas token has expired and there is a security provider, the saved sas token shall be refreshed with a new token from the security provider.**]**

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_005: [**This function shall return the saved sas token.**]**


### getSSLContext
```java
public SSLContext getSSLContext();
```

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_008: [**This function shall return the generated IotHubSSLContext.**]**


### setPathToIotHubTrustedCert
```java
public void setPathToIotHubTrustedCert(String pathToCertificate);
```

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_001: [**This function shall throw an UnsupportedOperationException.**]**


### setIotHubTrustedCert
```java
public void setIotHubTrustedCert(String certificate);
```

**SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_002: [**This function shall throw an UnsupportedOperationException.**]**
