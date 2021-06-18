/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Objects;

/**
 * Base class for providing authentication for a Device Client or Module Client, including x509 and SAS based authentication.
 */
public abstract class IotHubAuthenticationProvider
{
    protected String hostname;
    protected String gatewayHostname;
    protected String deviceId;
    protected String moduleId;

    protected IotHubSSLContext iotHubSSLContext;

    public IotHubAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId)
    {
        if (hostname == null || hostname.isEmpty())
        {
            throw new IllegalArgumentException("hostname cannot be null");
        }

        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId cannot be null");
        }

        this.hostname = hostname;
        this.gatewayHostname = gatewayHostname;
        this.deviceId = deviceId;
        this.moduleId = moduleId;
        this.iotHubSSLContext = new IotHubSSLContext();
    }

    public IotHubAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, SSLContext sslContext)
    {
        this(hostname, gatewayHostname, deviceId, moduleId);

        Objects.requireNonNull(sslContext);

        this.iotHubSSLContext = new IotHubSSLContext(sslContext);
    }

    //TODO it would be nice if this base level abstract class didn't throw this exception since only some implementations throw this exception
    public SSLContext getSSLContext() throws SecurityProviderException
    {
        return iotHubSSLContext.getSSLContext();
    }

    /**
     * Get the hostname
     * @return the saved hostname
     */
    public String getHostname()
    {
        return this.hostname;
    }

    /**
     * Get the gatewayHostname
     * @return the saved gatewayHostname
     */
    public String getGatewayHostname()
    {
        return this.gatewayHostname;
    }

    /**
     * Get the deviceId
     * @return the saved deviceId
     */
    public String getDeviceId()
    {
        return this.deviceId;
    }

    /**
     * Get the module id
     * @return the saved module id
     */
    public String getModuleId()
    {
        return this.moduleId;
    }
}
