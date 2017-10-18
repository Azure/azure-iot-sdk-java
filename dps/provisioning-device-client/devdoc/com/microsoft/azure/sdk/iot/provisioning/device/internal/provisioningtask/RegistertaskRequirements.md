# RegisterTask Requirements

## Overview

A task which implements callable. It handles register operations talking to underneath transport contracts.

## References

## Exposed API

```java
public class RegisterTask 
{
    RegisterTask(ProvisioningDeviceClientConfig provisioningDeviceClientConfig, DPSSecurityClient dpsSecurityClient,
                 ProvisioningDeviceClientContract provisioningDeviceClientContract, Authorization authorization)
            throws ProvisioningDeviceClientException;

    @Override
    public Object call() throws Exception;
}
```

### RegisterTask

```java
    RegisterTask(ProvisioningDeviceClientConfig provisioningDeviceClientConfig, DPSSecurityClient dpsSecurityClient,
                 ProvisioningDeviceClientContract provisioningDeviceClientContract, Authorization authorization)
            throws ProvisioningDeviceClientException;
```
**SRS_RegisterTask_25_001: [** Constructor shall save `provisioningDeviceClientConfig` , `dpsSecurityClient`, `provisioningDeviceClientContract`
and `authorization`.**]**

**SRS_RegisterTask_25_002: [** Constructor throw ProvisioningDeviceClientException if `provisioningDeviceClientConfig` , `dpsSecurityClient`, `authorization` or `provisioningDeviceClientContract` is null.**]**

### call

```java
    @Override
    public Object call() throws Exception;
```

**SRS_RegisterTask_25_003: [** If the provided security client is for X509 then, this method shall throw ProvisioningDeviceClientException if registration id is null. **]**

**SRS_RegisterTask_25_004: [** If the provided security client is for X509 then, this method shall save the SSL context to Authorization if it is not null and throw ProvisioningDeviceClientException otherwise. **]**

**SRS_RegisterTask_25_005: [** If the provided security client is for X509 then, this method shall build the required Json input using parser and throw the exception back to the user. **]**

**SRS_RegisterTask_25_006: [** If the provided security client is for X509 then, this method shall trigger `authenticateWithDPS` on the contract API and wait for response and return it. **]**

**SRS_RegisterTask_25_007: [** If the provided security client is for X509 then, this method shall throw ProvisioningDeviceClientException if null response is received. **]**

**SRS_RegisterTask_25_008: [** If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if registration id or endorsement key or storage root key are null. **]**

**SRS_RegisterTask_25_009: [** If the provided security client is for Key then, this method shall save the SSL context to Authorization if it is not null and throw ProvisioningDeviceClientException otherwise. **]**

**SRS_RegisterTask_25_010: [** If the provided security client is for Key then, this method shall build the required Json input with base 64 encoded endorsement key, storage root key and on failure pass the exception back to the user. **]**

**SRS_RegisterTask_25_011: [** If the provided security client is for Key then, this method shall trigger `requestNonceWithDPSTPM` on the contract API and wait for Authentication Key and decode it from Base64. Also this method shall pass the exception back to the user if it fails. **]**

**SRS_RegisterTask_25_012: [** If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if null response is received. **]**

**SRS_RegisterTask_25_013: [** If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if Authentication Key received is null. **]**

**SRS_RegisterTask_25_018: [** If the provided security client is for Key then, this method shall import the Base 64 encoded Authentication Key into the HSM using the security client and pass the exception to the user on failure. **]**

**SRS_RegisterTask_25_014: [** If the provided security client is for Key then, this method shall construct SasToken by doing the following
1. Build a `tokenScope` of format `<scopeid>/registrations/<registrationId>`
2. Sign the HSM with the string of format `<tokenScope>/n<expiryTime>` and receive a `token`
3. Encode the `token` to Base64 format and UrlEncode it to generate the signature. **]**

**SRS_RegisterTask_25_015: [** If the provided security client is for Key then, this method shall build the SasToken of the format
`SharedAccessSignature sr=<tokenScope>&sig=<signature>&se=<expiryTime>&skn=` and save it to `authorization`**]**

**SRS_RegisterTask_25_016: [** If the provided security client is for Key then, this method shall trigger `authenticateWithDPS` on the contract API using the sasToken generated and wait for response and return it. **]**

**SRS_RegisterTask_25_017: [** If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if null response to `authenticateWithDPS` is received. **]**