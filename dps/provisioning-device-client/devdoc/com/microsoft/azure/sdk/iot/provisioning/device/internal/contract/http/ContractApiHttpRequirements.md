# ContractAPIHttp Requirements

## Overview

An HTTPS connection between a device and an IoT Hub for exercising the contract with the Provisioning Service. 

## References

## Exposed API

```java
public class ContractAPIHttp extends ProvisioningDeviceClientContract
{
    public ContractAPIHttp(String scopeId, String hostName) throws ProvisioningDeviceClientException;

    public synchronized void requestNonceWithDPSTPM(byte[] payload, String registrationId, SSLContext sslContext, RestResponseCallback restResponseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException;

    public synchronized void authenticateWithDPS(byte[] payload, String registrationId, SSLContext sslContext, String authorization, RestResponseCallback restResponseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException, ProvisioningDeviceTransportException, ProvisioningDeviceHubException;

    public synchronized void getRegistrationStatus(String operationId, String registrationId, String dpsAuthorization, SSLContext sslContext, RestResponseCallback restResponseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException, ProvisioningDeviceTransportException, ProvisioningDeviceHubException;
}
```

### ContractAPIHttp

```java
public ContractAPIHttp(String scopeId, String hostName) throws ProvisioningDeviceClientException;
```

**SRS_ContractAPIHttp_25_001: [**The constructor shall save the scope id and hostname.**]**

**SRS_ContractAPIHttp_25_002: [**The constructor shall throw ProvisioningDeviceClientException if either scopeId and hostName are null or empty.**]**


### requestNonceWithDPSTPM

```Java
    public synchronized void requestNonceWithDPSTPM(byte[] payload, String registrationId, SSLContext sslContext, RestResponseCallback restResponseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException;
```

**SRS_ContractAPIHttp_25_003: [**If either registrationId, sslcontext or restResponseCallback is null or if registrationId is empty then this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIHttp_25_004: [**This method shall retrieve the Url by calling 'generateRegisterUrl' on an object for ProvisioningDeviceClientGenerateUrl.**]**

**SRS_ContractAPIHttp_25_005: [**This method shall prepare the `PUT` request by setting following headers on a HttpRequest
                                1. User-Agent : User Agent String for the SDK
                                2. Accept : "application/json"
                                3. Content-Type: "application/json; charset=utf-8".**]**

**SRS_ContractAPIHttp_25_006: [**This method shall set the SSLContext for the Http Request.**]**

**SRS_ContractAPIHttp_25_007: [**This method shall send http request and verify the status by calling 'ProvisioningDeviceClientExceptionManager.verifyHttpResponse'.**]**

**SRS_ContractAPIHttp_25_008: [**If service return a status as `404` then this method shall trigger the callback to the user with the response message.**]**

**SRS_ContractAPIHttp_25_009: [**If service return any other status other than `404` then this method shall throw ProvisioningDeviceTransportException in case of `< 300 ` or ProvisioningDeviceHubException on any other status.**]**

### authenticateWithDPS

```Java
    public synchronized void authenticateWithDPS(byte[] payload, String registrationId, SSLContext sslContext, String authorization, RestResponseCallback restResponseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException;
```

**SRS_ContractAPIHttp_25_011: [**If either registrationId, sslcontext or restResponseCallback is null or if registrationId is empty then this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIHttp_25_012: [**This method shall retrieve the Url by calling 'generateRegisterUrl' on an object for ProvisioningDeviceClientGenerateUrl.**]**

**SRS_ContractAPIHttp_25_013: [**This method shall prepare the `PUT` request by setting following headers on a HttpRequest
                                1. User-Agent : User Agent String for the SDK
                                2. Accept : "application/json"
                                3. Content-Type: "application/json; charset=utf-8"
                                4. Authorization: specified sas token as `authorization` if a non null value is given.**]**

**SRS_ContractAPIHttp_25_014: [**This method shall set the SSLContext for the Http Request.**]**

**SRS_ContractAPIHttp_25_015: [**This method shall send http request and verify the status by calling 'ProvisioningDeviceClientExceptionManager.verifyHttpResponse'.**]**

**SRS_ContractAPIHttp_25_016: [**If service return a status as `< 300` then this method shall trigger the callback to the user with the response message.**]**

**SRS_ContractAPIHttp_25_017: [**If service return any other status other than `<300` then this method shall throw ProvisioningDeviceHubException.**]**

### getRegistrationStatus

```Java
    public synchronized void getRegistrationStatus(String operationId, String registrationId, String dpsAuthorization, SSLContext sslContext, RestResponseCallback restResponseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException, ProvisioningDeviceTransportException, ProvisioningDeviceHubException;
```

**SRS_ContractAPIHttp_25_018: [**If either operationId, registrationId, sslcontext or restResponseCallback is null or if operationId, registrationId is empty then this method shall throw ProvisioningDeviceClientException.**]**

**SRS_ContractAPIHttp_25_019: [**This method shall retrieve the Url by calling `generateRequestUrl` on an object for ProvisioningDeviceClientGenerateUrl.**]**

**SRS_ContractAPIHttp_25_020: [**This method shall prepare the `GET` request by setting following headers on a HttpRequest
                                1. User-Agent : User Agent String for the SDK
                                2. Accept : "application/json"
                                3. Content-Type: "application/json; charset=utf-8"
                                4. Authorization: specified sas token as `authorization` if a non null value is given.**]**

**SRS_ContractAPIHttp_25_021: [**This method shall set the SSLContext for the Http Request.**]**

**SRS_ContractAPIHttp_25_022: [**This method shall send http request and verify the status by calling 'ProvisioningDeviceClientExceptionManager.verifyHttpResponse'.**]**

**SRS_ContractAPIHttp_25_023: [**If service return a status as `< 300` then this method shall trigger the callback to the user with the response message.**]**

**SRS_ContractAPIHttp_25_024: [**If service return any other status other than `< 300` then this method shall throw ProvisioningDeviceHubException.**]**