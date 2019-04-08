/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Authentication method that uses a shared access signature token and allows for token refresh.
 */
public abstract class IotHubSasTokenWithRefreshAuthenticationProvider extends IotHubSasTokenAuthenticationProvider
{
    /**
     * Constructor for IotHubSasTokenWithRefreshAuthenticationProvider
     * @param hostname the hostname
     * @param gatewayHostName the gateway hostname
     * @param deviceId the device id
     * @param moduleId the module id
     * @param sharedAccessToken the shared access token
     * @param suggestedTimeToLiveSeconds the time to live for generated tokens
     * @param timeBufferPercentage the percent of a sas token's life to live before renewing
     */
    protected IotHubSasTokenWithRefreshAuthenticationProvider(String hostname, String gatewayHostName, String deviceId, String moduleId, String sharedAccessToken, int suggestedTimeToLiveSeconds, int timeBufferPercentage)
    {
        super(hostname, gatewayHostName, deviceId, moduleId, suggestedTimeToLiveSeconds, timeBufferPercentage);
        this.sasToken = new IotHubSasToken(hostname, deviceId, null, sharedAccessToken, moduleId, getExpiryTimeInSeconds());
    }

    /**
     * Constructs the audience string to be used in a sas token
     * @param hostname the hostname
     * @param deviceId the device id
     * @param moduleId the module id
     * @return the audience
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported
     */
    protected static String buildAudience(String hostname, String deviceId, String moduleId) throws UnsupportedEncodingException
    {
        if (hostname == null || deviceId == null || moduleId == null || hostname.isEmpty() || deviceId.isEmpty() || moduleId.isEmpty())
        {
            // Codes_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_001: [If any of the provided arguments are null or empty, this
            // function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("No argument can be null or empty");
        }

        // Codes_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_002: [This function shall return the path
        // "<hostname>/devices/<device id>/modules/<module id> url encoded with utf-8.]
        return URLEncoder.encode(String.format("%s/devices/%s/modules/%s", hostname, deviceId, moduleId), "UTF-8");
    }

    /**
     * Returns false as instances of this class will always be able to renew their own sas token
     * @return false
     */
    @Override
    public boolean isRenewalNecessary()
    {
        // Codes_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_003: [This function shall always return false.]
        return false;
    }

    /**
     * Renew the saved sas token
     * @throws IOException If an IOException is encountered while refreshing the sas token
     * @throws TransportException If a TransportException is encountered while refreshing the sas token
     */
    public abstract void refreshSasToken() throws IOException, TransportException;

    /**
     * Check if sas token should be renewed at all, and then renew it if necessary
     * @return the renewed token, or the old token if it did not need to be renewed
     * @throws IOException If an IOException is encountered while refreshing the sas token
     * @throws TransportException If a TransportException is encountered while refreshing the sas token
     */
    @Override
    public String getRenewedSasToken(boolean proactivelyRenew, boolean forceRenewal) throws IOException, TransportException
    {
        if (this.shouldRefreshToken(proactivelyRenew) || forceRenewal)
        {
            // Codes_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_004: [This function shall invoke shouldRefreshSasToken, and if it should refresh, this function shall refresh the sas token.]
            this.refreshSasToken();
        }

        // Codes_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_005: [This function shall return the saved sas token's string representation.]
        return this.sasToken.toString();
    }
}
