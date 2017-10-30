# AmqpsDeviceAuthentication Requirements

## Overview

Child class of AmqpsDeviceOperations to provide common domain handling and prototype for the specific authentication classes.

## References

## Exposed API


```java
class AmqpsDeviceAuthentication extends AmqpsDeviceOperations
{
    AmqpsDeviceAuthentication();
    protected SslDomain makeDomain(SSLContext sslContext);
    protected void setSslDomain(Transport transport, SSLContext sslContext) {}
    protected void authenticate(DeviceClientConfig deviceClientConfig) throws IOException {}
    protected Boolean authenticationMessageReceived(AmqpsMessage amqpsMessage, UUID authenticationCorrelationId);
```


### AmqpsDeviceAuthentication

```java
AmqpsDeviceAuthentication();
```


### makeDomain

```java
protected SslDomain makeDomain(SSLContext sslContext)
```

**SRS_AMQPSDEVICEAUTHENTICATION_12_001: [**The function shall get the sslDomain oject from the Proton reactor.**]**

**SRS_AMQPSDEVICEAUTHENTICATION_12_002: [**The function shall set the sslContext on the domain.**]**

**SRS_AMQPSDEVICEAUTHENTICATION_12_003: [**The function shall set the peer authentication mode to VERIFY_PEER.**]**

**SRS_AMQPSDEVICEAUTHENTICATION_12_004: [**The function shall initialize the sslDomain.**]**

**SRS_AMQPSDEVICEAUTHENTICATION_12_005: [**The function shall return with the sslDomain.**]**


### setSslDomain

```java
    protected void setSslDomain(Transport transport, SSLContext sslContext) {}
```

**SRS_AMQPSDEVICEAUTHENTICATION_12_006: [**The prototype function does nothing.**]**

### authenticate

```java
protected void authenticate(DeviceClientConfig deviceClientConfig) throws IOException {}
```

**SRS_AMQPSDEVICEAUTHENTICATION_12_007: [**The prototype function does nothing.**]**


### authenticationMessageReceived

```java
protected Boolean authenticationMessageReceived(AmqpsMessage amqpsMessage, UUID authenticationCorrelationId);
```

**SRS_AMQPSDEVICEAUTHENTICATION_12_008: [**The prototype function shall return false.**]**

