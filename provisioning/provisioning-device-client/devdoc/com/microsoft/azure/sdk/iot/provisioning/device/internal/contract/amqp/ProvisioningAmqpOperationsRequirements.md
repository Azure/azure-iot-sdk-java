# ProvisioningAmqpOperations Requirements

## Overview

An Provisioning AMQP Operation module.

## References

## Exposed API

```java
public class ProvisioningAmqpOperations extends AmqpDeviceOperations implements AmqpListener
{
    public ProvisioningAmqpOperations(String scopeId, String hostName) throws ProvisioningDeviceClientException;

    public boolean isAmqpConnected();

    public void open(String registrationId, SSLContext sslContext, boolean isX509Cert) throws ProvisioningDeviceConnectionException;

    public void close() throws IOException;

    public void sendStatusMessage(String operationId, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException;

    public void sendRegisterMessage(ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException;

    public void ConnectionEstablished();

    public void ConnectionLost();

    public void MessageSent();

    public void MessageReceived(AmqpMessage message);
}
```

### ProvisioningAmqpOperations

```java
public ProvisioningAmqpOperations(String scopeId, String hostName) throws ProvisioningDeviceClientException
```

**SRS_ProvisioningAmqpOperations_07_001: [**The constructor shall save the `scopeId` and `hostname`.**]**

**SRS_ProvisioningAmqpOperations_07_002: [**The constructor shall throw ProvisioningDeviceClientException if either `scopeId` and `hostName` are null or empty.**]**

### open

```Java
public void open(String registrationId, SSLContext sslContext, boolean isX509Cert) throws ProvisioningDeviceConnectionException;
```

**SRS_ProvisioningAmqpOperations_07_003: [**If `amqpConnection` is not null and is connected, open shall do nothing.**]**

**SRS_ProvisioningAmqpOperations_07_004: [**open shall throw ProvisioningDeviceClientException if either `registrationId` or `sslContext` are null or empty.**]**

**SRS_ProvisioningAmqpOperations_07_005: [**This method shall construct the Link Address with `/<scopeId>/registrations/<registrationId>`.**]**

**SRS_ProvisioningAmqpOperations_07_006: [**This method shall connect to the amqp connection and throw ProvisioningDeviceConnectionException on error.**]**

### close

```Java
public void close() throws IOException;
```

**SRS_ProvisioningAmqpOperations_07_007: [**If `amqpConnection` is null, this method shall do nothing.**]**

**SRS_ProvisioningAmqpOperations_07_008: [**This method shall call `close` on amqpConnection.**]**

### sendStatusMessage

```Java
public void sendStatusMessage(String operationId, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
```

**SRS_ProvisioningAmqpOperations_07_015: [**sendStatusMessage shall throw ProvisioningDeviceClientException if either `operationId` or `responseCallback` are null or empty.**]**

**SRS_ProvisioningAmqpOperations_07_016: [**This method shall send the Operation Status AMQP Provisioning message.**]**

**SRS_ProvisioningAmqpOperations_07_017: [**This method shall wait for the response of this message for `MAX_WAIT_TO_SEND_MSG` and call the responseCallback with the reply.**]**

**SRS_ProvisioningAmqpOperations_07_018: [**This method shall throw ProvisioningDeviceClientException if any failure is encountered.**]**

### sendRegisterMessage

```Java
public void sendRegisterMessage(ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
```

**SRS_ProvisioningAmqpOperations_07_009: [**sendRegisterMessage shall throw ProvisioningDeviceClientException if either `responseCallback` is null.**]**

**SRS_ProvisioningAmqpOperations_07_010: [**This method shall send the Register AMQP Provisioning message.**]**

**SRS_ProvisioningAmqpOperations_07_011: [**This method shall wait for the response of this message for `MAX_WAIT_TO_SEND_MSG` and call the responseCallback with the reply.**]**

**SRS_ProvisioningAmqpOperations_07_012: [**This method shall throw ProvisioningDeviceClientException if any failure is encountered.**]**

### MessageReceived

```Java
public void MessageReceived(AmqpMessage message)
```

**SRS_ProvisioningAmqpOperations_07_013: [**This method shall add the message to a message queue.**]**

**SRS_ProvisioningAmqpOperations_07_014: [**This method shall then Notify the receiveLock.**]**
