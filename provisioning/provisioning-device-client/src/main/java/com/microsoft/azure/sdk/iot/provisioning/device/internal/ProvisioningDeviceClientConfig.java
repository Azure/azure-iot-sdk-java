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
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public final class ProvisioningDeviceClientConfig
{
    @Getter
    @Setter
    private String provisioningServiceGlobalEndpoint;

    @Getter
    @Setter
    private String idScope;

    @Getter
    @Setter
    private ProvisioningDeviceClientTransportProtocol protocol;

    @Getter
    @Setter
    private SecurityProvider securityProvider;

    @Getter
    @Setter
    private boolean usingWebSocket = false;

    @Getter
    private final String provisioningDeviceClientUniqueIdentifier = UUID.randomUUID().toString().substring(0, 8);

    @Getter
    private ProvisioningDeviceClientRegistrationCallback registrationCallback;

    @Getter
    private Object registrationCallbackContext;

    @Getter
    @Setter
    private String payload;

    /*
     * Certificate request that DPS sends to its CA to sign and return an X509 certificate that is then linked to a device.  
     */
    @Getter
    @Setter
    private String operationalCertificateRequest;

    /**
     * Setter for the Registration Callback.
     * @param registrationCallback  Registration Callback to be triggered.
     * @param registrationCallbackContext Registration Callback Context to be passed.
     */
    public void setRegistrationCallback(ProvisioningDeviceClientRegistrationCallback registrationCallback, Object registrationCallbackContext)
    {
        this.registrationCallback = registrationCallback;
        this.registrationCallbackContext = registrationCallbackContext;
    }

    public String getUniqueIdentifier() {
        return this.provisioningDeviceClientUniqueIdentifier;
    }
}
