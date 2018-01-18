# ContractAPIAmqp Requirements

## Overview

An AMQP connection between a device and an IoT Hub for exercising the contract with the Provisioning Service.

## References

## Exposed API

```java
public class ContractAPIAmqp extends ProvisioningDeviceClientContract
{
    public ContractAPIAmqp(ProvisioningDeviceClientConfig provisioningDeviceClientConfig) throws ProvisioningDeviceClientException;

    public synchronized void open(RequestData requestData) throws ProvisioningDeviceConnectionException;

    public synchronized void close() throws ProvisioningDeviceConnectionException;

    public synchronized void authenticateWithProvisioningService(RequestData requestData, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException;

    public synchronized void requestNonceForTPM(RequestData requestData, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException;

    public synchronized void getRegistrationStatus(RequestData requestData, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException;
}
```

### ContractAPIAmqp

```java
public ContractAPIAmqp(ProvisioningDeviceClientConfig provisioningDeviceClientConfig) throws ProvisioningDeviceClientException;
```

**SRS_ContractAPIAmqp_07_001: [**The constructor shall save the scope id and hostname.**]**

**SRS_ContractAPIAmqp_07_002: [**The constructor shall throw ProvisioningDeviceClientException if either idScope and hostName are null or empty.**]**

### authenticateWithDPS

```Java
public synchronized void authenticateWithProvisioningService(RequestData requestData, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException;
```

**SRS_ContractAPIAmqp_07_003: [**If `responseCallback` is null, this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIAmqp_07_024: [** If `provisioningDeviceClientConfig` is null, this method shall throw ProvisioningDeviceClientException. **]**

**SRS_ContractAPIAmqp_07_005: [**This method shall send an AMQP message with the property of `iotdps-register`.**]**

**SRS_ContractAPIAmqp_07_006: [**This method shall wait `MAX_WAIT_TO_SEND_MSG` for a reply from the service.**]**

**SRS_ContractAPIAmqp_07_007: [**If the service fails to reply in the alloted time this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIAmqp_07_008: [**This method shall responds to the responseCallback with amqp response data and the status `DPS_REGISTRATION_RECEIVED`.**]**

### requestNonceWithDPSTPM

```Java
public synchronized void requestNonceForTPM(RequestData requestData, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
```

TBD

### getRegistrationStatus

```Java
public synchronized void getRegistrationStatus(RequestData requestData, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
```

**SRS_ContractAPIAmqp_07_009: [**If `requestData` is null this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIAmqp_07_010: [**If `requestData.getOperationId()` is null or empty, this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIAmqp_07_011: [**If `responseCallback` is null, this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIAmqp_07_012: [**If amqpConnection is null or not connected, this method shall throw ProvisioningDeviceConnectionException.**]**

**SRS_ContractAPIAmqp_07_013: [**This method shall send an AMQP message with the property of `iotdps-get-operationstatus` and the OperationId.**]**

**SRS_ContractAPIAmqp_07_014: [**This method shall wait `MAX_WAIT_TO_SEND_MSG` for a reply from the service.**]**

**SRS_ContractAPIAmqp_07_015: [**If the service fails to reply in the alloted time this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIAmqp_07_016: [**This method shall responds to the responseCallback with amqp response data and the status `DPS_REGISTRATION_RECEIVED`.**]**

### open

```Java
public synchronized void open(RequestData requestData) throws ProvisioningDeviceConnectionException
```

**SRS_ContractAPIAmqp_07_017: [**If amqpConnection is not null and is connect this method shall do nothing.**]**

**SRS_ContractAPIAmqp_07_018: [**If `requestData` is null this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIAmqp_07_019: [**If `requestData.getRegistrationId()` is null or empty, this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIAmqp_07_020: [**If `requestData.getSslContext()` is null, this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIAmqp_07_021: [**This method shall connect to the amqpConnectoins with the link address of `amqps://<hostname>/<idScope>/registrations/<registrationId>`.**]**

```Java
public synchronized void close() throws ProvisioningDeviceConnectionException
```

**SRS_ContractAPIAmqp_07_022: [**If the amqpConnection is NULL or Is not open this method will do nothing.**]**

**SRS_ContractAPIAmqp_07_023: [**This method will close the amqpConnection connection.**]**
