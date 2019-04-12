/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;

public final class ProvisioningDeviceClientConfig
{
    private String provisioningServiceGlobalEndpoint;
    private String idScope;
    private ProvisioningDeviceClientTransportProtocol protocol;
    private SecurityProvider securityProvider;
    private boolean useWebSockets = false;

    private ProvisioningDeviceClientRegistrationCallback registrationCallback;
    private Object registrationCallbackContext;
    private String jsonPayload;

    /**
     * Setter for the Registration Callback.
     * @param registrationCallback  Registration Callback to be triggered.
     * @param registrationCallbackContext Registration Callback Context to be passed.
     */
    public void setRegistrationCallback(ProvisioningDeviceClientRegistrationCallback registrationCallback, Object registrationCallbackContext)
    {
        //SRS_ProvisioningDeviceClientConfig_25_001: [ This method shall save registrationCallback and registrationCallbackContext. ]
        this.registrationCallback = registrationCallback;
        this.registrationCallbackContext = registrationCallbackContext;
    }

    /**
     * Setter for the Registration Callback.
     * @return Registration Callback set by the user.
     */
    public ProvisioningDeviceClientRegistrationCallback getRegistrationCallback()
    {
        //SRS_ProvisioningDeviceClientConfig_25_002: [ This method shall retrieve registrationCallback. ]
        return registrationCallback;
    }

    /**
     * Getter for the Context
     * @return returns the context set by the user.
     */
    public Object getRegistrationCallbackContext()
    {
        //SRS_ProvisioningDeviceClientConfig_25_003: [ This method shall retrieve registrationCallbackContext. ]
        return registrationCallbackContext;
    }

    /**
     * Getter for the Provisioning Service Global Endpoint.
     * @return returns the end point.
     */
    public String getProvisioningServiceGlobalEndpoint()
    {
        //SRS_ProvisioningDeviceClientConfig_25_004: [ This method shall retrieve provisioningServiceGlobalEndpoint. ]
        return provisioningServiceGlobalEndpoint;
    }

    /**
     * Setter for the Provisioning Service Global Endpoint.
     * @param provisioningServiceGlobalEndpoint The end point to be set.
     */
    public void setProvisioningServiceGlobalEndpoint(String provisioningServiceGlobalEndpoint)
    {
        //SRS_ProvisioningDeviceClientConfig_25_005: [ This method shall set provisioningServiceGlobalEndpoint. ]
        this.provisioningServiceGlobalEndpoint = provisioningServiceGlobalEndpoint;
    }

    /**
     * Getter for the Scope Id.
     * @return returns the scope Id.
     */
    public String getIdScope()
    {
        //SRS_ProvisioningDeviceClientConfig_25_006: [ This method shall retrieve idScope. ]
        return idScope;
    }

    /**
     * Setter for the Scope Id.
     * @param idScope Scope to be set.
     */
    public void setIdScope(String idScope)
    {
        //SRS_ProvisioningDeviceClientConfig_25_007: [ This method shall set idScope. ]
        this.idScope = idScope;
    }

    /**
     * Getter for the protocol.
     * @return The protocol set by the user.
     */
    public ProvisioningDeviceClientTransportProtocol getProtocol()
    {
        //SRS_ProvisioningDeviceClientConfig_25_008: [ This method shall retrieve ProvisioningDeviceClientTransportProtocol. ]
        return protocol;
    }

    /**
     * Setter for the protocol.
     * @param protocol protocol set by the user.
     */
    public void setProtocol(ProvisioningDeviceClientTransportProtocol protocol)
    {
        //SRS_ProvisioningDeviceClientConfig_25_009: [ This method shall set ProvisioningDeviceClientTransportProtocol. ]
        this.protocol = protocol;
    }

    /**
     * Getter for the Security Provider.
     * @return security provider set by the user
     */
    public SecurityProvider getSecurityProvider()
    {
        //SRS_ProvisioningDeviceClientConfig_25_010: [ This method shall retrieve securityProvider. ]
        return securityProvider;
    }

    /**
     * Setter for the Security provider.
     * @param securityProvider security provider to be set.
     */
    public void setSecurityProvider(SecurityProvider securityProvider)
    {
        //SRS_ProvisioningDeviceClientConfig_25_011: [ This method shall set securityProvider. ]
        this.securityProvider = securityProvider;
    }

    /**
     * Setter for Using Web Sockets
     * @param useWebSocket flag to determine to use web sockets
     */
    public void setUseWebSockets(boolean useWebSocket)
    {
        this.useWebSockets = useWebSocket;
    }

    /**
     * Getter for Using Web Sockets
     * @return flag to determine to use web sockets
     */
    public boolean getUseWebSockets()
    {
        return this.useWebSockets;
    }

    /**
     * Setter data for custom payload
     * @param json payload data
     */
    public void setCustomPayload(String jsonPayload)
    {
        this.jsonPayload = jsonPayload;
    }

    /**
     * Getter for retreiving Custom Payload
     * @return value of the custom payload
     */
    public String getCustomPayload()
    {
        return this.jsonPayload;
    }
}
