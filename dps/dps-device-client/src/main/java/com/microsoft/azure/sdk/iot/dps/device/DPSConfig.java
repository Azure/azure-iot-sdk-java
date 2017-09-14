package com.microsoft.azure.sdk.iot.dps.device;

import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSClientException;
import com.microsoft.azure.sdk.iot.dps.security.DPSHsmType;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClient;
import com.microsoft.azure.sdk.iot.dps.security.SecurityType;

public class DPSConfig
{
    private String dpsURI;
    private String dpsScopeId;
    private DPSTransportProtocol protocol;
    private SecurityType securityType;
    private com.microsoft.azure.sdk.iot.dps.security.DPSHsmType DPSHsmType;
    private DPSSecurityClient thirdPartySecurityClient;

    private DPSConfig(String dpsURI, String dpsScopeId, DPSTransportProtocol protocol)
    {
        this.dpsURI = dpsURI;
        this.dpsScopeId = dpsScopeId;
        this.protocol = protocol;
    }

    public DPSConfig(String dpsURI, String dpsScopeId, DPSTransportProtocol protocol, DPSHsmType DPSHsmType) throws DPSClientException
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

    public DPSConfig(String dpsURI, String dpsScopeId, DPSTransportProtocol protocol, SecurityType securityType, DPSHsmType DPSHsmType) throws DPSClientException
    {
        this(dpsURI, dpsScopeId, protocol);
        this.securityType = securityType;
        this.DPSHsmType = DPSHsmType;
        verifyDpsConfig();
    }

    public DPSConfig(String dpsURI, String dpsScopeId, DPSTransportProtocol protocol, SecurityType securityType, DPSSecurityClient thirdPartySecurityClient) throws DPSClientException
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

    public DPSTransportProtocol getProtocol()
    {
        return protocol;
    }

    public void setProtocol(DPSTransportProtocol protocol)
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

    private void verifyDpsConfig() throws DPSClientException
    {
        if (this.dpsScopeId == null)
        {
            throw new DPSClientException("DpsScopeID cannot be null");
        }

        if (this.dpsURI == null)
        {
            throw new DPSClientException("DpsUri cannot be null");
        }

        if (this.protocol == null)
        {
            throw new DPSClientException("protocol cannot be null");
        }

        if (this.securityType == null)
        {
            throw new DPSClientException("Security Type cannot be null");
        }

        if (this.DPSHsmType == null)
        {
            throw new DPSClientException("HSM type cannot be null");
        }

        if (this.DPSHsmType == DPSHsmType.TPM_EMULATOR && this.securityType != SecurityType.Key)
        {
            throw new DPSClientException("HSM type and Security type should match to TPM and Key");
        }

        if (this.DPSHsmType == DPSHsmType.DICE_EMULATOR && this.securityType != SecurityType.X509)
        {
            throw new DPSClientException("HSM type and Security type should match to DICE and X509");
        }

        //todo rem to verify instances as well

    }
}
