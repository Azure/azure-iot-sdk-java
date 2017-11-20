# AmqpsDeviceAuthenticationSAS Requirements

## Overview

Child class of AmqpsDeviceAuthentication to provide SAS authentication specific functionality.

## References

## Exposed API


```java
class AmqpsDeviceAuthenticationSAS extends AmqpsDeviceAuthentication
{
    AmqpsDeviceAuthenticationSAS(DeviceClientConfig deviceClientConfig);
    protected AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException;
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
    protected void setSslDomain(Transport transport, SSLContext sslContext) {};
    protected Boolean isLinkFound(String linkName);
```


### AmqpsDeviceAuthenticationSAS

```java
AmqpsDeviceAuthenticationSAS(DeviceClientConfig deviceClientConfig);
```

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_001: [**The constructor shall throw IllegalArgumentException if the deviceClientConfig parameter is null.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_002: [**The constructor shall save the deviceClientConfig parameter value to a member variable.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_003: [**The constructor shall set both the sender and the receiver link state to OPENED.**]**


### sendMessageAndGetDeliveryHash

```java
    protected AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException;
```

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_004: [**The function shall override the default behaviour and return null.**]**


### getMessageFromReceiverLink

```java
protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
```

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_005: [**The function shall override the default behaviour and return null.**]**


### setSslDomain

```java
protected void setSslDomain(Transport transport, SSLContext sslContext) {};
```

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_006: [**The function shall throw IllegalArgumentException if any of the input parameter is null.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_007: [**The function shall get the sasl object from the transport.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_008: [**The function shall construct the userName.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_009: [**The function shall set SASL PLAIN authentication mode with the usrName and SAS token.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_010: [**The function shall call the prototype class makeDomain function with the sslContext.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_011: [**The function shall set the domain on the transport.**]**


### isLinkFound

```java
protected Boolean isLinkFound(String linkName);
```

**SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_012: [**The function shall override the default behaviour and return true.**]**