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
    private SecurityProviderX509 securityProviderX509;

    public IotHubX509HardwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, SecurityProvider securityProvider)
    {
        super(hostname, gatewayHostname, deviceId, moduleId);

        if (!(securityProvider instanceof SecurityProviderX509))
        {
            //Codes_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_002: [If the provided security provider is not an instance of SecurityProviderX509, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided security provider must be of type SecurityProviderX509");
        }

        //Codes_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_001: [This function shall save the provided security provider.]
        this.securityProviderX509 = (SecurityProviderX509) securityProvider;
        this.iotHubSSLContext = null;
    }

    /**
     * Getter for IotHubSSLContext
     * @throws IOException if an error occurs when generating the SSLContext
     * @return The value of IotHubSSLContext
     */
    @Override
    public SSLContext getSSLContext() throws IOException
    {
        if (this.iotHubSSLContext == null)
        {
            try
            {
                //Codes_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_003: [If this object's ssl context has not been generated yet, this function shall generate it from the saved security provider.]
                this.iotHubSSLContext = new IotHubSSLContext(securityProviderX509.getSSLContext());
            }
            catch (SecurityProviderException e)
            {
                //Codes_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_004: [If the security provider throws a SecurityProviderException while generating an SSLContext, this function shall throw an IOException.]
                throw new IOException("Failed to get the SSLContext from the security provider", e);
            }
        }

        //Codes_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_005: [This function shall return the saved IotHubSSLContext.]
        return this.iotHubSSLContext.getSSLContext();
    }
}
