/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class IotHubSasTokenSoftwareAuthenticationProvider extends IotHubSasTokenAuthenticationProvider
{
    protected String deviceKey;

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
     * @throws SecurityException if the provided sas token has expired
     */
    public IotHubSasTokenSoftwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, String deviceKey, String sharedAccessToken, int tokenValidSecs, int timeBufferPercentage) throws SecurityException
    {
        super(hostname, gatewayHostname, deviceId, moduleId, tokenValidSecs, timeBufferPercentage);

        this.deviceKey = deviceKey;

        this.sslContextNeedsUpdate = true;

        //Codes_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_002: [This constructor shall save the provided hostname, device id, module id, deviceKey, and sharedAccessToken.]
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
     * @throws SecurityException if the provided sas token has expired
     */
    public IotHubSasTokenSoftwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, String deviceKey, String sharedAccessToken) throws SecurityException
    {
        super(hostname, gatewayHostname, deviceId, moduleId);

        this.deviceKey = deviceKey;
        this.sslContextNeedsUpdate = true;

        //Codes_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_003: [This constructor shall save the provided hostname, device id, module id, deviceKey, and sharedAccessToken.]
        this.sasToken = new IotHubSasToken(hostname, deviceId, deviceKey, sharedAccessToken, moduleId, getExpiryTimeInSeconds());
    }

    /**
     * Returns true if the saved sas token has expired and cannot be auto-renewed through the device key
     * @return if the sas token needs manual renewal
     */
    @Override
    public boolean isRenewalNecessary()
    {
        // Codes_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_018: [This function shall return true if a deviceKey is present and if super.isRenewalNecessary returns true.]
        return (super.isRenewalNecessary() && this.deviceKey == null);
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
        // Codes_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_017: [This function shall return true if a deviceKey is present.]
        return this.deviceKey != null;
    }

    /**
     * Getter for SasToken. If the saved token has expired, this method shall renew it if possible
     *
     * @param proactivelyRenew if true, this method will generate a fresh sas token even if the previously saved token
     *                                 has not expired yet as long as the current token has lived beyond its buffer.
     *                                 Use this for pre-emptively renewing sas tokens.
     *
     * @return The value of SasToken
     */
    @Override
    public String getRenewedSasToken(boolean proactivelyRenew, boolean forceRenewal) throws IOException, TransportException
    {
        if (this.shouldRefreshToken(proactivelyRenew) || forceRenewal)
        {
            if (this.deviceKey != null)
            {
                //Codes_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_004: [If the saved sas token has expired and there is a device key present, the saved sas token shall be renewed.]
                //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_006: [If the saved sas token has not expired and there is a device key present, but this method is called to proactively renew and the token should renew, the saved sas token shall be renewed.]
                this.sasToken = new IotHubSasToken(this.hostname, this.deviceId, this.deviceKey, null, this.moduleId, getExpiryTimeInSeconds());
            }
        }

        //Codes_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_005: [This function shall return the saved sas token.]
        return this.sasToken.toString();
    }
}
