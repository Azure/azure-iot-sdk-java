/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.dps.security.DPSHsmType;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClient;
import com.microsoft.azure.sdk.iot.dps.security.SecurityType;

public class ProvisioningDeviceClientConfig
{
    private String dpsURI;
    private String dpsScopeId;
    private ProvisioningDeviceClientTransportProtocol protocol;
    private SecurityType securityType;
    private com.microsoft.azure.sdk.iot.dps.security.DPSHsmType DPSHsmType;
    private DPSSecurityClient thirdPartySecurityClient;

    private ProvisioningDeviceClientConfig(String dpsURI, String dpsScopeId, ProvisioningDeviceClientTransportProtocol protocol)
    {
        this.dpsURI = dpsURI;
        this.dpsScopeId = dpsScopeId;
        this.protocol = protocol;
    }

    public ProvisioningDeviceClientConfig(String dpsURI, String dpsScopeId, ProvisioningDeviceClientTransportProtocol protocol, DPSHsmType DPSHsmType) throws ProvisioningDeviceClientException
    {
        this(dpsURI, dpsScopeId, protocol);

        this.DPSHsmType = DPSHsmType;

        if (this.DPSHsmType == DPSHsmType.TPM_EMULATOR)
        {
            this.securityType = SecurityType.Key;
        }

        if (this.DPSHsmType == DPSHsmType.DICE_EMULATOR)
        {
            this.securityType = SecurityType.X509;
        }

        verifyDpsConfig();
    }

    public ProvisioningDeviceClientConfig(String dpsURI, String dpsScopeId, ProvisioningDeviceClientTransportProtocol protocol, SecurityType securityType, DPSHsmType DPSHsmType) throws ProvisioningDeviceClientException
    {
        this(dpsURI, dpsScopeId, protocol);
        this.securityType = securityType;
        this.DPSHsmType = DPSHsmType;
        verifyDpsConfig();
    }

    public ProvisioningDeviceClientConfig(String dpsURI, String dpsScopeId, ProvisioningDeviceClientTransportProtocol protocol, SecurityType securityType, DPSSecurityClient thirdPartySecurityClient) throws ProvisioningDeviceClientException
    {
        this(dpsURI, dpsScopeId, protocol);
        this.securityType = securityType;
        this.thirdPartySecurityClient = thirdPartySecurityClient;
        this.DPSHsmType = DPSHsmType.THIRD_PARTY;
        verifyDpsConfig();
    }

    public DPSSecurityClient getThirdPartySecurityType()
    {
        return this.thirdPartySecurityClient;
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

    public SecurityType getSecurityType()
    {
        return securityType;
    }

    public void setSecurityType(SecurityType securityType)
    {
        this.securityType = securityType;
    }

    public DPSHsmType getDPSHsmType()
    {
        return DPSHsmType;
    }

    public void setDPSHsmType(DPSHsmType DPSHsmType)
    {
        this.DPSHsmType = DPSHsmType;
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

        if (this.securityType == null)
        {
            throw new ProvisioningDeviceClientException("Security Type cannot be null");
        }

        if (this.DPSHsmType == null)
        {
            throw new ProvisioningDeviceClientException("HSM type cannot be null");
        }

        if (this.DPSHsmType == DPSHsmType.TPM_EMULATOR && this.securityType != SecurityType.Key)
        {
            throw new ProvisioningDeviceClientException("HSM type and Security type should match to TPM and Key");
        }

        if (this.DPSHsmType == DPSHsmType.DICE_EMULATOR && this.securityType != SecurityType.X509)
        {
            throw new ProvisioningDeviceClientException("HSM type and Security type should match to DICE and X509");
        }

        //todo rem to verify instances as well

    }
}
