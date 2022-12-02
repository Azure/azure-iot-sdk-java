package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.provisioning.device.TpmRegistrationResult;

public class Tpm extends TpmRegistrationResult
{
    void setAuthenticationKey(String authenticationKey)
    {
        this.authenticationKey = authenticationKey;
    }
}
