/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import javax.net.ssl.SSLContext;

public class IotHubSasTokenSoftwareAuthenticationProvider extends IotHubSasTokenAuthenticationProvider
{
    private final String deviceKey;

    /**
     * Constructor that takes a connection string containing a sas token or a device key
     *
     * @param hostname the IotHub host name
     * @param gatewayHostname The gateway hostname to use, or null if connecting to an IotHub
     * @param deviceId the IotHub device id
     * @param moduleId the module id. May be null if not using a module
     * @param deviceKey the device key for the device. Must be null if the provided sharedAccessToken is not
     * @param sharedAccessToken the sas token string for accessing the device. Must be null if the provided deviceKey is not.
     * @param tokenValidSecs the number of seconds that the token will be valid for
     * @param timeBufferPercentage the percent of the sas token's life that will be exhausted before renewal is attempted
     */
    public IotHubSasTokenSoftwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, String deviceKey, String sharedAccessToken, int tokenValidSecs, int timeBufferPercentage)
    {
        super(hostname, gatewayHostname, deviceId, moduleId, tokenValidSecs, timeBufferPercentage);
        this.deviceKey = deviceKey;
        this.sasToken = new IotHubSasToken(hostname, deviceId, deviceKey, sharedAccessToken, moduleId, getExpiryTimeInSeconds());
    }

    /**
     * Constructor that takes a connection string containing a sas token or a device key and uses the default token valid seconds and timeBufferPercentage
     * @param hostname the IotHub host name
     * @param gatewayHostname The gateway hostname to use, or null if connecting to an IotHub
     * @param deviceId the IotHub device id
     * @param moduleId the module id. May be null if not using a module
     * @param deviceKey the device key for the device. Must be null if the provided sharedAccessToken is not
     * @param sharedAccessToken the sas token string for accessing the device. Must be null if the provided deviceKey is not.
     */
    public IotHubSasTokenSoftwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, String deviceKey, String sharedAccessToken)
    {
        super(hostname, gatewayHostname, deviceId, moduleId);
        this.deviceKey = deviceKey;
        this.sasToken = new IotHubSasToken(hostname, deviceId, deviceKey, sharedAccessToken, moduleId, getExpiryTimeInSeconds());
    }

    /**
     * Constructor that takes a connection string containing a sas token or a device key and uses the default token valid seconds and timeBufferPercentage
     * @param hostname the IotHub host name
     * @param gatewayHostname The gateway hostname to use, or null if connecting to an IotHub
     * @param deviceId the IotHub device id
     * @param moduleId the module id. May be null if not using a module
     * @param deviceKey the device key for the device. Must be null if the provided sharedAccessToken is not
     * @param sharedAccessToken the sas token string for accessing the device. Must be null if the provided deviceKey is not.
     * @param sslContext the sslContext to use for SSL negotiation
     */
    public IotHubSasTokenSoftwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, String deviceKey, String sharedAccessToken, SSLContext sslContext)
    {
        super(hostname, gatewayHostname, deviceId, moduleId, sslContext);
        this.deviceKey = deviceKey;
        this.sasToken = new IotHubSasToken(hostname, deviceId, deviceKey, sharedAccessToken, moduleId, getExpiryTimeInSeconds());
    }

    /**
     * Returns true if the saved sas token has expired and cannot be auto-renewed through the device key
     * @return if the sas token needs manual renewal
     */
    @Override
    public boolean isAuthenticationProviderRenewalNecessary()
    {
        return (super.isAuthenticationProviderRenewalNecessary() && this.deviceKey == null);
    }

    @Override
    public void setTokenValidSecs(long tokenValidSecs)
    {
        super.setTokenValidSecs(tokenValidSecs);

        if (this.deviceKey != null)
        {
            this.sasToken = new IotHubSasToken(this.hostname, this.deviceId, this.deviceKey, null, this.moduleId, getExpiryTimeInSeconds());
        }
    }

    @Override
    public boolean canRefreshToken()
    {
        return this.deviceKey != null;
    }

    /**
     * Getter for SasToken. If the saved token has expired, this method shall renew it if possible
     *
     * @return The value of SasToken
     */
    @Override
    public char[] getSasToken()
    {
        if (this.deviceKey != null)
        {
            this.sasToken = new IotHubSasToken(this.hostname, this.deviceId, this.deviceKey, null, this.moduleId, getExpiryTimeInSeconds());
        }

        return this.sasToken.toString().toCharArray();
    }
}
