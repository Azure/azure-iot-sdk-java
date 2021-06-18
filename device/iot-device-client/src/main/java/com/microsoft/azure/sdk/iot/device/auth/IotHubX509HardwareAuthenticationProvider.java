/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public class IotHubX509HardwareAuthenticationProvider extends IotHubAuthenticationProvider
{
    protected SecurityProviderX509 securityProviderX509;

    public IotHubX509HardwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, SecurityProvider securityProvider)
    {
        super(hostname, gatewayHostname, deviceId, moduleId);

        if (!(securityProvider instanceof SecurityProviderX509))
        {
            throw new IllegalArgumentException("The provided security provider must be of type SecurityProviderX509");
        }

        this.securityProviderX509 = (SecurityProviderX509) securityProvider;
    }

    /**
     * Getter for IotHubSSLContext
     * @throws IOException if an error occurs when generating the SSLContext
     * @return The value of IotHubSSLContext
     */
    @Override
    public SSLContext getSSLContext() throws SecurityProviderException
    {
        return securityProviderX509.getSSLContext();
    }
}
