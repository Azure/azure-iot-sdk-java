# provisioningtask Requirements

## Overview

A task which implements callable. It handles register and status task and also implements state machine.

## References

## Exposed API

```java
public class provisioningtask 
{
    public ProvisioningTask(ProvisioningDeviceClientConfig provisioningDeviceClientConfig,
                            DPSSecurityClient dpsSecurityClient,
                            ProvisioningDeviceClientContract provisioningDeviceClientContract,
                            ProvisioningDeviceClientStatusCallback provisioningDeviceClientStatusCallback,
                            Object dpsStatusCallbackContext) throws ProvisioningDeviceClientException;

    public void setRegistrationCallback(ProvisioningDeviceClientRegistrationCallback provisioningDeviceClientRegistrationCallback,
                                        Object dpsRegistrationCallbackContext) throws ProvisioningDeviceClientException;
    @Override
    public Object call() throws Exception;

    public void close();
}
```

### ProvisioningTask

```java
    public ProvisioningTask(ProvisioningDeviceClientConfig provisioningDeviceClientConfig,
                            DPSSecurityClient dpsSecurityClient,
                            ProvisioningDeviceClientContract provisioningDeviceClientContract,
                            ProvisioningDeviceClientStatusCallback provisioningDeviceClientStatusCallback,
                            Object dpsStatusCallbackContext) throws ProvisioningDeviceClientException;
```
**SRS_provisioningtask_25_001: [** Constructor shall save `provisioningDeviceClientConfig` , `dpsSecurityClient`, `provisioningDeviceClientContract`, `provisioningDeviceClientStatusCallback`, `dpsStatusCallbackContext`.**]**

**SRS_provisioningtask_25_002: [** Constructor throw ProvisioningDeviceClientException if `provisioningDeviceClientConfig` , `dpsSecurityClient` or `provisioningDeviceClientContract` is null.**]**

**SRS_provisioningtask_25_003: [** Constructor shall trigger status callback if provided with status `DPS_DEVICE_STATUS_UNAUTHENTICATED`.**]**

### setRegistrationCallback

```java
    public void setRegistrationCallback(ProvisioningDeviceClientRegistrationCallback provisioningDeviceClientRegistrationCallback,
                                        Object dpsRegistrationCallbackContext) throws ProvisioningDeviceClientException;
```
**SRS_provisioningtask_25_004: [** This method shall throw ProvisioningDeviceClientException if the `provisioningDeviceClientRegistrationCallback` is null. **]**

**SRS_provisioningtask_25_005: [** This method shall save the registration callback. **]**

### call

```java
    @Override
    public Object call() throws Exception;
```
**SRS_provisioningtask_25_006: [** This method shall invoke the status callback, if any of the task fail or throw any exception. **]**

**SRS_provisioningtask_25_007: [** This method shall invoke Register task and status task to execute the state machine of the service as per below rules.**]**

### Service State Machine Rules

**SRS_provisioningtask_25_008: [** This method shall invoke register task and wait for it to complete.**]**
**SRS_provisioningtask_25_009: [** This method shall invoke status callback with status `DPS_DEVICE_STATUS_AUTHENTICATED` if register task completes successfully.**]**
**SRS_provisioningtask_25_010: [** This method shall invoke status task to get the current state of the device registration and wait until a terminal state is reached.**]**
**SRS_provisioningtask_25_011: [** Upon reaching one of the terminal state i.e ASSIGNED, this method shall invoke registration callback with the information retrieved from service for IotHub Uri and DeviceId. Also if status callback is defined then it shall be invoked with status `DPS_DEVICE_STATUS_ASSIGNED`.**]**
**SRS_provisioningtask_25_012: [** Upon reaching one of the terminal states i.e FAILED or DISABLED, this method shall invoke registration callback with error message received from service. Also if status callback is defined then it shall be invoked with status `DPS_DEVICE_STATUS_ERROR`.**]**
**SRS_provisioningtask_25_013: [** Upon reaching intermediate state i.e UNASSIGNED or ASSIGNING, this method shall continue to query for status until a terminal state is reached. 
Also if status callback is defined then it shall be invoked with status `DPS_DEVICE_STATUS_ASSIGNING`.**]**

State diagram :

One of the following states can be reached from register or status task -
(A) Unassigned
(B) Assigning 
(C) Assigned 
(D) Fail
(E) Disable

Return-State  | A | B | C | D | E
------------- | ------------- | ------------- | ------------- | ------------- | -------------
Register-State  | B, C, D, E | C, D, E | terminal | terminal | terminal
Status-State | B, C, D, E | C, D, E | terminal | terminal | terminal

### close

```java
public void close();
```
**SRS_provisioningtask_25_014: [** This method shall shutdown the executors if they have not already shutdown. **]**
