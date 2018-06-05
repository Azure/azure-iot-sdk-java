/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Authentication method that uses HSM to get a SAS token.
 */
public class ModuleAuthenticationWithHsm extends ModuleAuthenticationWithTokenRefresh
{
    SignatureProvider signatureProvider;

    public static ModuleAuthenticationWithHsm create(SignatureProvider signatureProvider, String deviceId, String moduleId, String hostname, String gatewayHostname, int suggestedTimeToLiveSeconds, int timeBufferPercentage) throws IOException, TransportException
    {
        if (signatureProvider == null)
        {
            // Codes_SRS_MODULEAUTHENTICATIONWITHHSM_34_002: [If the provided signature provider is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("signatureProvider cannot be null");
        }

        // Codes_SRS_MODULEAUTHENTICATIONWITHHSM_34_001: [This function shall construct a sas token from the provided arguments and then return a ModuleAuthenticationWithHsm instance that uses that sas token.]
        IotHubSasToken sasToken = createNewSasToken(hostname, gatewayHostname, deviceId, moduleId, signatureProvider, suggestedTimeToLiveSeconds);
        return new ModuleAuthenticationWithHsm(hostname, gatewayHostname, deviceId, moduleId, sasToken.getSasToken(), signatureProvider, suggestedTimeToLiveSeconds, timeBufferPercentage);
    }

    private ModuleAuthenticationWithHsm(String hostname, String gatewayHostName, String deviceId, String moduleId, String sharedAccessToken, SignatureProvider signatureProvider, int suggestedTimeToLiveSeconds, int timeBufferPercentage)
    {
        super(hostname, gatewayHostName, deviceId, moduleId, sharedAccessToken, suggestedTimeToLiveSeconds, timeBufferPercentage);
        this.signatureProvider = signatureProvider;
    }

    static IotHubSasToken createNewSasToken(String hostname, String gatewayHostName, String deviceId, String moduleId, SignatureProvider signatureProvider, long suggestedTimeToLive) throws IOException, TransportException
    {
        try
        {
            String audience = buildAudience(hostname, deviceId, moduleId);

            long expiresOn = (System.currentTimeMillis() / 1000) + suggestedTimeToLive;
            String data = audience + "\n" + expiresOn;
            String signature = signatureProvider.sign(moduleId, data);

            if (gatewayHostName != null && !gatewayHostName.isEmpty())
            {
                // Codes_SRS_MODULEAUTHENTICATIONWITHHSM_34_003: [If the gatewayHostname is not null or empty, this function shall construct the sas token using the gateway hostname instead of the hostname.]
                return new IotHubSasToken(gatewayHostName, deviceId, null, signature, moduleId, expiresOn);
            }
            else
            {
                // Codes_SRS_MODULEAUTHENTICATIONWITHHSM_34_004: [If the gatewayHostname is null or empty, this function shall construct the sas token using the hostname instead of the gateway hostname.]
                return new IotHubSasToken(hostname, deviceId, null, signature, moduleId, expiresOn);
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IOException(e);
        }
    }

    public void refreshSasToken() throws IOException, TransportException
    {
        // Codes_SRS_MODULEAUTHENTICATIONWITHHSM_34_005: [This function shall create a new sas token and save it locally.]
        this.sasToken = createNewSasToken(this.hostname, this.gatewayHostname, this.deviceId, this.moduleId, this.signatureProvider, this.tokenValidSecs);
    }
}
