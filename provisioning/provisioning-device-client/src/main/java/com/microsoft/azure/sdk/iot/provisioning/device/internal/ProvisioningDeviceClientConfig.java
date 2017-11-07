/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityClient;

public final class ProvisioningDeviceClientConfig
{
    private String provisioningServiceGlobalEndpoint;
    private String scopeId;
    private ProvisioningDeviceClientTransportProtocol protocol;
    private SecurityClient securityClient;

    private ProvisioningDeviceClientRegistrationCallback registrationCallback;
    private Object registrationCallbackContext;

    public void setRegistrationCallback(ProvisioningDeviceClientRegistrationCallback registrationCallback, Object registrationCallbackContext)
    {
        //SRS_ProvisioningDeviceClientConfig_25_001: [ This method shall save registrationCallback and registrationCallbackContext. ]
        this.registrationCallback = registrationCallback;
        this.registrationCallbackContext = registrationCallbackContext;
    }

    public ProvisioningDeviceClientRegistrationCallback getRegistrationCallback()
    {
        //SRS_ProvisioningDeviceClientConfig_25_002: [ This method shall retrieve registrationCallback. ]
        return registrationCallback;
    }

    public Object getRegistrationCallbackContext()
    {
        //SRS_ProvisioningDeviceClientConfig_25_003: [ This method shall retrieve registrationCallbackContext. ]
        return registrationCallbackContext;
    }

    public String getProvisioningServiceGlobalEndpoint()
    {
        //SRS_ProvisioningDeviceClientConfig_25_004: [ This method shall retrieve provisioningServiceGlobalEndpoint. ]
        return provisioningServiceGlobalEndpoint;
    }

    public void setProvisioningServiceGlobalEndpoint(String provisioningServiceGlobalEndpoint)
    {
        //SRS_ProvisioningDeviceClientConfig_25_005: [ This method shall set provisioningServiceGlobalEndpoint. ]
        this.provisioningServiceGlobalEndpoint = provisioningServiceGlobalEndpoint;
    }

    public String getScopeId()
    {
        //SRS_ProvisioningDeviceClientConfig_25_006: [ This method shall retrieve scopeId. ]
        return scopeId;
    }

    public void setScopeId(String scopeId)
    {
        //SRS_ProvisioningDeviceClientConfig_25_007: [ This method shall set scopeId. ]
        this.scopeId = scopeId;
    }

    public ProvisioningDeviceClientTransportProtocol getProtocol()
    {
        //SRS_ProvisioningDeviceClientConfig_25_008: [ This method shall retrieve ProvisioningDeviceClientTransportProtocol. ]
        return protocol;
    }

    public void setProtocol(ProvisioningDeviceClientTransportProtocol protocol)
    {
        //SRS_ProvisioningDeviceClientConfig_25_009: [ This method shall set ProvisioningDeviceClientTransportProtocol. ]
        this.protocol = protocol;
    }

    public SecurityClient getSecurityClient()
    {
        //SRS_ProvisioningDeviceClientConfig_25_010: [ This method shall retrieve securityClient. ]
        return securityClient;
    }

    public void setSecurityClient(SecurityClient securityClient)
    {
        //SRS_ProvisioningDeviceClientConfig_25_011: [ This method shall set securityClient. ]
        this.securityClient = securityClient;
    }
}
