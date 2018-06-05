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
public abstract class ModuleAuthenticationWithTokenRefresh extends IotHubSasTokenSoftwareAuthenticationProvider
{
    protected ModuleAuthenticationWithTokenRefresh(String hostname, String gatewayHostName, String deviceId, String moduleId, String sharedAccessToken, int suggestedTimeToLiveSeconds, int timeBufferPercentage)
    {
        super(hostname, gatewayHostName, deviceId, moduleId, null, sharedAccessToken, suggestedTimeToLiveSeconds, timeBufferPercentage);
    }

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

    @Override
    public boolean isRenewalNecessary()
    {
        //HSM will always be able to renew token for the user

        // Codes_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_003: [This function shall always return false.]
        return false;
    }

    public abstract void refreshSasToken() throws IOException, TransportException;

    @Override
    public String getRenewedSasToken() throws IOException, TransportException
    {
        if (this.shouldRefreshToken())
        {
            // Codes_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_004: [This function shall invoke shouldRefreshSasToken, and if it should refresh, this function shall refresh the sas token.]
            this.refreshSasToken();
        }

        // Codes_SRS_MODULEAUTHENTICATIONWITHTOKENREFRESH_34_005: [This function shall return the saved sas token's string representation.]
        return this.sasToken.toString();
    }
}
