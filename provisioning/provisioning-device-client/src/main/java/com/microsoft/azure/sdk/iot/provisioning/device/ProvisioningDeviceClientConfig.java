/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

import com.microsoft.azure.sdk.iot.dps.security.HsmType;
import com.microsoft.azure.sdk.iot.dps.security.SecurityClient;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;

public class ProvisioningDeviceClientConfig
{
    private String dpsURI;
    private String dpsScopeId;
    private ProvisioningDeviceClientTransportProtocol protocol;
    private HsmType hsmType;
    private SecurityClient securityClient;

    private ProvisioningDeviceClientConfig(String dpsURI, String dpsScopeId, ProvisioningDeviceClientTransportProtocol protocol)
    {
        this.dpsURI = dpsURI;
        this.dpsScopeId = dpsScopeId;
        this.protocol = protocol;
    }

    public ProvisioningDeviceClientConfig(String dpsURI, String dpsScopeId, ProvisioningDeviceClientTransportProtocol protocol, HsmType hsmType) throws ProvisioningDeviceClientException
    {
        this(dpsURI, dpsScopeId, protocol);

        this.hsmType = hsmType;
        verifyDpsConfig();
    }

    public ProvisioningDeviceClientConfig(String dpsURI, String dpsScopeId, ProvisioningDeviceClientTransportProtocol protocol, SecurityClient securityClient) throws ProvisioningDeviceClientException
    {
        this(dpsURI, dpsScopeId, protocol);
        this.securityClient = securityClient;
        verifyDpsConfig();
    }

    public SecurityClient getSecurityClient()
    {
        return this.securityClient;
    }

    public String getDpsURI()
    {
        return dpsURI;
    }

    public void setDpsURI(String dpsURI)
    {
        this.dpsURI = dpsURI;
    }

    public String getDpsScopeId()
    {
        return dpsScopeId;
    }

    public void setDpsScopeId(String dpsScopeId)
    {
        this.dpsScopeId = dpsScopeId;
    }

    public ProvisioningDeviceClientTransportProtocol getProtocol()
    {
        return protocol;
    }

    public void setProtocol(ProvisioningDeviceClientTransportProtocol protocol)
    {
        this.protocol = protocol;
    }

    public HsmType getHsmType()
    {
        return hsmType;
    }

    public void setHsmType(HsmType hsmType)
    {
        this.hsmType = hsmType;
    }

    private void verifyDpsConfig() throws ProvisioningDeviceClientException
    {
        if (this.dpsScopeId == null)
        {
            throw new ProvisioningDeviceClientException("DpsScopeID cannot be null");
        }

        if (this.dpsURI == null)
        {
            throw new ProvisioningDeviceClientException("DpsUri cannot be null");
        }

        if (this.protocol == null)
        {
            throw new ProvisioningDeviceClientException("protocol cannot be null");
        }

        if (this.hsmType == hsmType.TPM_EMULATOR)
        {
            throw new ProvisioningDeviceClientException("HSM type and Security type should match to TPM and Key");
        }

        if (this.hsmType == hsmType.DICE_EMULATOR)
        {
            throw new ProvisioningDeviceClientException("HSM type and Security type should match to DICE and X509");
        }

        //todo rem to verify instances as well

    }
}
