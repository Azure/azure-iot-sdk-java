/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenWithRefreshAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.SignatureProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

/**
 * Authentication method that uses HSM to get a SAS token.
 */
public class IotHubSasTokenHsmAuthenticationProvider extends IotHubSasTokenWithRefreshAuthenticationProvider
{
    SignatureProvider signatureProvider;
    String generationId;

    /**
     * Constructor for a IotHubSasTokenHsmAuthenticationProvider instance
     * @param signatureProvider the signature provider to be used when generating sas tokens
     * @param deviceId the id of the device the module belongs to
     * @param moduleId the id of the module to be authenticated for
     * @param hostname the hostname of the iothub to be authenticated for. May be null if gatewayHostname is not
     * @param gatewayHostname the gatewayHostname of the edge hub to be authenticated for. May be null if hostname is not
     * @param generationId the generation id
     * @param suggestedTimeToLiveSeconds the time for the generated sas tokens to live for
     * @param timeBufferPercentage the percent of the life a sas token will live before attempting to be renewed. (100 means don't renew until end of life)
     * @return the created IotHubSasTokenHsmAuthenticationProvider instance
     * @throws IOException If the Hsm unit cannot be reached
     * @throws TransportException If the Hsm unit cannot be reached
     */
    public static IotHubSasTokenHsmAuthenticationProvider create(SignatureProvider signatureProvider, String deviceId, String moduleId, String hostname, String gatewayHostname, String generationId, int suggestedTimeToLiveSeconds, int timeBufferPercentage) throws IOException, TransportException
    {
        if (signatureProvider == null)
        {
            // Codes_SRS_MODULEAUTHENTICATIONWITHHSM_34_002: [If the provided signature provider is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("signatureProvider cannot be null");
        }

        // Codes_SRS_MODULEAUTHENTICATIONWITHHSM_34_001: [This function shall construct a sas token from the provided arguments and then return a IotHubSasTokenHsmAuthenticationProvider instance that uses that sas token.]
        IotHubSasToken sasToken = createNewSasToken(hostname, gatewayHostname, deviceId, moduleId, generationId, signatureProvider, suggestedTimeToLiveSeconds);
        return new IotHubSasTokenHsmAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, generationId, sasToken.getSasToken(), signatureProvider, suggestedTimeToLiveSeconds, timeBufferPercentage);
    }

    /**
     * Renew the saved sas token using the HSM unit
     * @throws IOException If the Hsm unit cannot be reached
     * @throws TransportException If the Hsm unit cannot be reached
     */
    public void refreshSasToken() throws IOException, TransportException
    {
        // Codes_SRS_MODULEAUTHENTICATIONWITHHSM_34_005: [This function shall create a new sas token and save it locally.]
        this.sasToken = createNewSasToken(this.hostname, this.gatewayHostname, this.deviceId, this.moduleId, this.generationId, this.signatureProvider, this.tokenValidSecs);
    }

    /**
     * Returns true as the Hsm can always refresh the token
     * @return true
     */
    public boolean canRefreshToken()
    {
        return true;
    }

    static IotHubSasToken createNewSasToken(String hostname, String gatewayHostName, String deviceId, String moduleId, String generationId, SignatureProvider signatureProvider, long suggestedTimeToLive) throws IOException, TransportException
    {
        try
        {
            String audience = buildAudience(hostname, deviceId, moduleId);

            long expiresOn = (System.currentTimeMillis() / 1000) + suggestedTimeToLive;
            String data = audience + "\n" + expiresOn;
            String signature = signatureProvider.sign(moduleId, data, generationId);

            String host = gatewayHostName != null && !gatewayHostName.isEmpty() ? gatewayHostName : hostname;
            String sharedAccessToken = IotHubSasToken.buildSharedAccessToken(audience, signature, expiresOn);

            // Codes_SRS_MODULEAUTHENTICATIONWITHHSM_34_004: [If the gatewayHostname is null or empty, this function shall construct the sas token using the hostname instead of the gateway hostname.]
            // Codes_SRS_MODULEAUTHENTICATIONWITHHSM_34_006: [If the gatewayHostname is present, this function shall construct the sas token using the gateway hostname instead of the hostname.]
            return new IotHubSasToken(host, deviceId, null, sharedAccessToken, moduleId, expiresOn);
        }
        catch (UnsupportedEncodingException | URISyntaxException | HsmException e)
        {
            throw new IOException(e);
        }
    }

    private IotHubSasTokenHsmAuthenticationProvider(String hostname, String gatewayHostName, String deviceId, String moduleId, String generationId, String sharedAccessToken, SignatureProvider signatureProvider, int suggestedTimeToLiveSeconds, int timeBufferPercentage)
    {
        super(hostname, gatewayHostName, deviceId, moduleId, sharedAccessToken, suggestedTimeToLiveSeconds, timeBufferPercentage);
        this.signatureProvider = signatureProvider;
        this.generationId = generationId;
    }
}
