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
    private ProvisioningDeviceClientRegistrationCallback registrationCallback;

    @Getter
    private Object registrationCallbackContext;

    @Getter
    @Setter
    private String payload;

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
}
