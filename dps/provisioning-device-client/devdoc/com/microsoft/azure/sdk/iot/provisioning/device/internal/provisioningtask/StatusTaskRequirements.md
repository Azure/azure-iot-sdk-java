# StatusTask Requirements

## Overview

A task which implements callable. It handles Status query operations talking to underneath transport contracts.

## References

## Exposed API

```java
public class StatusTask 
{
    
    StatusTask(DPSSecurityClient dpsSecurityClient, ProvisioningDeviceClientContract provisioningDeviceClientContract,
               String operationId, Authorization authorization) throws ProvisioningDeviceClientException;

    @Override
    public Object call() throws Exception;
}
```

### StatusTask

```java
    
    StatusTask(DPSSecurityClient dpsSecurityClient, ProvisioningDeviceClientContract provisioningDeviceClientContract,
               String operationId, Authorization authorization) throws ProvisioningDeviceClientException;
```
**SRS_StatusTask_25_001: [** Constructor shall save `operationId` , `dpsSecurityClient`, `provisioningDeviceClientContract`
and `authorization`. **]**

**SRS_StatusTask_25_002: [** Constructor shall throw ProvisioningDeviceClientException if `operationId` , `dpsSecurityClient`, `authorization` or `provisioningDeviceClientContract` is null. **]**

### call

```java
    @Override
    public Object call() throws Exception;
```

**SRS_StatusTask_25_003: [** This method shall throw ProvisioningDeviceClientException if registration id is null or empty. **]**

**SRS_StatusTask_25_004: [** This method shall retrieve the SSL context from Authorization and throw ProvisioningDeviceClientException if it is null. **]**

**SRS_StatusTask_25_005: [** This method shall trigger `getRegistrationStatus` on the contract API and wait for response and return it. **]**

**SRS_StatusTask_25_006: [** This method shall throw ProvisioningDeviceClientException if null response or no response is received in maximum time of 90 seconds. **]**