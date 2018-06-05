/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public abstract class AuthenticationProvider
{
    protected String hostname;
    protected String gatewayHostname;
    protected String deviceId;
    protected String moduleId;

    public abstract SSLContext getSSLContext() throws IOException, TransportException;

    public AuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId)
    {
        if (hostname == null || hostname.isEmpty())
        {
            // Codes_SRS_AUTHENTICATIONPROVIDER_34_006: [If the provided hostname is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("hostname cannot be null");
        }

        if (deviceId == null || deviceId.isEmpty())
        {
            // Codes_SRS_AUTHENTICATIONPROVIDER_34_007: [If the provided device id is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("deviceId cannot be null");
        }

        // Codes_SRS_AUTHENTICATIONPROVIDER_34_001: [The constructor shall save the provided hostname, gatewayhostname, deviceid and moduleid.]
        this.hostname = hostname;
        this.gatewayHostname = gatewayHostname;
        this.deviceId = deviceId;
        this.moduleId = moduleId;
    }

    public String getHostname()
    {
        // Codes_SRS_AUTHENTICATIONPROVIDER_34_002: [This function shall return the saved hostname.]
        return this.hostname;
    }

    public String getGatewayHostname()
    {
        // Codes_SRS_AUTHENTICATIONPROVIDER_34_003: [This function shall return the saved gatewayHostname.]
        return this.gatewayHostname;
    }

    public String getDeviceId()
    {
        // Codes_SRS_AUTHENTICATIONPROVIDER_34_004: [This function shall return the saved deviceId.]
        return this.deviceId;
    }

    public String getModuleId()
    {
        // Codes_SRS_AUTHENTICATIONPROVIDER_34_005: [This function shall return the saved moduleId.]
        return this.moduleId;
    }
}
