/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Objects;

/**
 * Base class for providing authentication for a Device Client or Module Client, including x509 and SAS based authentication.
 */
public abstract class IotHubAuthenticationProvider
{
    protected String hostname;
    protected final String gatewayHostname;
    protected final String mqttGatewayHostname;
    protected String deviceId;
    protected final String moduleId;

    IotHubSSLContext iotHubSSLContext;

    public IotHubAuthenticationProvider(String hostname, String gatewayHostname, String mqttGatewayHostname, String deviceId, String moduleId)
    {
        this(hostname, gatewayHostname, mqttGatewayHostname, deviceId, moduleId, null);
    }

    public IotHubAuthenticationProvider(String hostname, String gatewayHostname, String mqttGatewayHostname, String deviceId, String moduleId, SSLContext sslContext)
    {
        Objects.requireNonNull(hostname);
        Objects.requireNonNull(deviceId);

        this.hostname = hostname;
        this.gatewayHostname = gatewayHostname;
        this.mqttGatewayHostname = mqttGatewayHostname;
        this.deviceId = deviceId;
        this.moduleId = moduleId;

        if (sslContext == null)
        {
            this.iotHubSSLContext = new IotHubSSLContext();
        }
        else
        {
            this.iotHubSSLContext = new IotHubSSLContext(sslContext);
        }
    }

    public SSLContext getSSLContext() throws IOException
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
     * Get the mqttGatewayHostname
     * @return the saved mqttGatewayHostname
     */
    public String getMqttGatewayHostname()
    {
        return mqttGatewayHostname;
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
