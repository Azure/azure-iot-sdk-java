# ContractAPIMqtt Requirements

## Overview

An MQTT connection between a device and an IoT Hub for exercising the contract with the Provisioning Service.

## References

## Exposed API

```java
public class ContractAPIMqtt extends ProvisioningDeviceClientContract
{
    public ContractAPIMqtt(String idScope, String hostName) throws ProvisioningDeviceClientException;

    public synchronized void requestNonceWithDPSTPM(byte[] payload, String registrationId, SSLContext sslContext, RestResponseCallback restResponseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException;

    public synchronized void authenticateWithDPS(byte[] payload, String registrationId, SSLContext sslContext, String authorization, RestResponseCallback restResponseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException, ProvisioningDeviceTransportException, ProvisioningDeviceHubException;

    public synchronized void getRegistrationStatus(String operationId, String registrationId, String dpsAuthorization, SSLContext sslContext, RestResponseCallback restResponseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException, ProvisioningDeviceTransportException, ProvisioningDeviceHubException;
}
```

### ContractAPIMqtt

```java
public ContractAPIMqtt(String idScope, String hostName) throws ProvisioningDeviceClientException;
```

**SRS_ContractAPIMqtt_25_001: [**The constructor shall save the scope id and hostname.**]**

**SRS_ContractAPIMqtt_25_002: [**The constructor shall throw ProvisioningDeviceClientException if either idScope and hostName are null or empty.**]**

### requestNonceWithTPM

```Java
    public synchronized void requestNonceWithTPM(RequestData requestData, ResponseCallback responseCallback, Object authorizationCallbackContext) throws ProvisioningDeviceClientException;
```

**SRS_ContractAPIMqtt_25_003: [**`requestNonceWithTPM` is not supported with MQTT and shall throw `ProvisioningDeviceClientException`.**]**

### authenticateWithDPS

```Java
    public synchronized void authenticateWithProvisioningService(RequestData requestData, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException;
```

**SRS_ContractAPIMqtt_07_003: [**If `responseCallback` is null, this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIMqtt_07_024: [** If `provisioningDeviceClientConfig` is null, this method shall throw  **]**ProvisioningDeviceClientException.

**SRS_ContractAPIMqtt_07_004: [**If mqttConnection is null or not connected, this method shall throw ProvisioningDeviceConnectionException.**]**

**SRS_ContractAPIMqtt_07_005: [**This method shall send an MQTT message with the payload of `$dps/registrations/PUT/iotdps-register/?$rid=<ID>`.**]**

**SRS_ContractAPIMqtt_07_006: [**This method shall wait `MAX_WAIT_TO_SEND_MSG` for a reply from the service.**]**

**SRS_ContractAPIMqtt_07_007: [**If the service fails to reply in the alloted time this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIMqtt_07_008: [**This method shall responds to the responseCallback with mqtt response data and the status `DPS_REGISTRATION_RECEIVED`.**]**

### getRegistrationStatus

```Java
    public synchronized void getRegistrationStatus(RequestData requestData, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException;
```

**SRS_ContractAPIMqtt_07_009: [**If `requestData` is null this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIMqtt_07_010: [**If `requestData.getOperationId()` is null or empty, this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIMqtt_07_011: [**If `responseCallback` is null, this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIMqtt_07_012: [**If mqttConnection is null or not connected, this method shall throw ProvisioningDeviceConnectionException.**]**

**SRS_ContractAPIMqtt_07_013: [**This method shall send an MQTT message with the property of `$dps/registrations/GET/iotdps-get-operationstatus/?$rid=<Id>&operationId=<op_id>`.**]**

**SRS_ContractAPIMqtt_07_014: [**This method shall wait `MAX_WAIT_TO_SEND_MSG` for a reply from the service.**]**

**SRS_ContractAPIMqtt_07_015: [**If the service fails to reply in the alloted time this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIMqtt_07_016: [**This method shall responds to the responseCallback with MQTT response data and the status `DPS_REGISTRATION_RECEIVED`.**]**

### open

```Java
public synchronized void open(RequestData requestData) throws ProvisioningDeviceConnectionException
```

**SRS_ContractAPIMqtt_07_017: [**If mqttConnection is not null and is connect this method shall do nothing.**]**

**SRS_ContractAPIMqtt_07_018: [**If `requestData` is null this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIMqtt_07_019: [**If `requestData.getRegistrationId()` is null or empty, this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIMqtt_07_020: [**If `requestData.getSslContext()` is null, this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIMqtt_07_021: [**This method shall connect to the mqttConnectoins with the username of `<idScope>/registrations/<Registration_ID>/api-version=<API_VERSION>&ClientVersion=<client_version>`.**]**

```Java
public synchronized void close() throws ProvisioningDeviceConnectionException
```

**SRS_ContractAPIMqtt_07_022: [**If the mqttConnection is NULL or Is not open this method will do nothing.**]**

**SRS_ContractAPIMqtt_07_023: [**This method will close the mqttConnection connection.**]**