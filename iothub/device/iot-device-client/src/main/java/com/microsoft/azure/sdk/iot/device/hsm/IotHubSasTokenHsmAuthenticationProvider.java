/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenWithRefreshAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.SignatureProvider;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Authentication method that uses HSM to get a SAS token.
 */
public class IotHubSasTokenHsmAuthenticationProvider extends IotHubSasTokenWithRefreshAuthenticationProvider
{
    private final SignatureProvider signatureProvider;
    private final String generationId;

    /**
     * Constructor for a IotHubSasTokenHsmAuthenticationProvider instance
     * @param signatureProvider the signature provider to be used when generating sas tokens
     * @param deviceId the id of the device the module belongs to
     * @param moduleId the id of the module to be authenticated for
     * @param hostname the hostname of the iothub to be authenticated for.
     * @param gatewayHostname the gatewayHostname of the edge hub to be authenticated for. May be null
     * @param generationId the generation id
     * @param suggestedTimeToLiveSeconds the time for the generated sas tokens to live for
     * @param timeBufferPercentage the percent of the life a sas token will live before attempting to be renewed. (100 means don't renew until end of life)
     * @return the created IotHubSasTokenHsmAuthenticationProvider instance
     * @throws IOException If the Hsm unit cannot be reached
     * @throws TransportException If the Hsm unit cannot be reached
     */
    public static IotHubSasTokenHsmAuthenticationProvider create(
        SignatureProvider signatureProvider,
        String deviceId,
        String moduleId,
        String hostname,
        String gatewayHostname,
        String generationId,
        int suggestedTimeToLiveSeconds,
        int timeBufferPercentage) throws IOException, TransportException
    {
        if (signatureProvider == null)
        {
            throw new IllegalArgumentException("signatureProvider cannot be null");
        }

        IotHubSasToken sasToken = createNewSasToken(
            hostname,
            gatewayHostname,
            deviceId,
            moduleId,
            generationId,
            signatureProvider,
            suggestedTimeToLiveSeconds);

        return new IotHubSasTokenHsmAuthenticationProvider(
            hostname,
            gatewayHostname,
            deviceId,
            moduleId,
            generationId,
            sasToken.getSasToken(),
            signatureProvider,
            suggestedTimeToLiveSeconds,
            timeBufferPercentage);
    }

    /**
     * Constructor for a IotHubSasTokenHsmAuthenticationProvider instance
     * @param signatureProvider the signature provider to be used when generating sas tokens
     * @param deviceId the id of the device the module belongs to
     * @param moduleId the id of the module to be authenticated for
     * @param hostname the hostname of the iothub to be authenticated for.
     * @param gatewayHostname the gatewayHostname of the edge hub to be authenticated for. May be null
     * @param generationId the generation id
     * @param suggestedTimeToLiveSeconds the time for the generated sas tokens to live for
     * @param timeBufferPercentage the percent of the life a sas token will live before attempting to be renewed. (100 means don't renew until end of life)
     * @param sslContext the SSLContext to open connections with
     * @return the created IotHubSasTokenHsmAuthenticationProvider instance
     * @throws IOException If the Hsm unit cannot be reached
     * @throws TransportException If the Hsm unit cannot be reached
     */
    public static IotHubSasTokenHsmAuthenticationProvider create(
        SignatureProvider signatureProvider,
        String deviceId,
        String moduleId,
        String hostname,
        String gatewayHostname,
        String generationId,
        int suggestedTimeToLiveSeconds,
        int timeBufferPercentage,
        SSLContext sslContext) throws IOException, TransportException
    {
        if (signatureProvider == null)
        {
            throw new IllegalArgumentException("signatureProvider cannot be null");
        }

        IotHubSasToken sasToken =
            createNewSasToken(
                hostname,
                gatewayHostname,
                deviceId,
                moduleId,
                generationId,
                signatureProvider,
                suggestedTimeToLiveSeconds);

        return new IotHubSasTokenHsmAuthenticationProvider(
            hostname,
            gatewayHostname,
            deviceId,
            moduleId,
            generationId,
            sasToken.getSasToken(),
            signatureProvider,
            suggestedTimeToLiveSeconds,
            timeBufferPercentage,
            sslContext);
    }

    /**
     * Renew the saved sas token using the HSM unit
     * @throws IOException If the Hsm unit cannot be reached
     * @throws TransportException If the Hsm unit cannot be reached
     */
    public void refreshSasToken() throws IOException, TransportException
    {
        this.sasToken = createNewSasToken(
            this.hostname,
            this.gatewayHostname,
            this.deviceId,
            this.moduleId,
            this.generationId,
            this.signatureProvider,
            this.tokenValidSecs);
    }

    /**
     * Returns true as the Hsm can always refresh the token
     * @return true
     */
    public boolean canRefreshToken()
    {
        return true;
    }

    private static IotHubSasToken createNewSasToken(
        String hostname,
        String gatewayHostName,
        String deviceId,
        String moduleId,
        String generationId,
        SignatureProvider signatureProvider,
        long suggestedTimeToLive) throws IOException, TransportException
    {
        try
        {
            String audience = buildAudience(hostname, deviceId, moduleId);

            long expiresOn = (System.currentTimeMillis() / 1000) + suggestedTimeToLive;
            String data = audience + "\n" + expiresOn;
            String signature = signatureProvider.sign(moduleId, data, generationId);

            String host = gatewayHostName != null && !gatewayHostName.isEmpty() ? gatewayHostName : hostname;
            String sharedAccessToken = IotHubSasToken.buildSharedAccessToken(audience, signature, expiresOn);

            return new IotHubSasToken(host, deviceId, null, sharedAccessToken, moduleId, expiresOn);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IOException(e);
        }
    }

    private IotHubSasTokenHsmAuthenticationProvider(
        String hostname,
        String gatewayHostName,
        String deviceId,
        String moduleId,
        String generationId,
        String sharedAccessToken,
        SignatureProvider signatureProvider,
        int suggestedTimeToLiveSeconds,
        int timeBufferPercentage)
    {
        super(hostname, gatewayHostName, deviceId, moduleId, sharedAccessToken, suggestedTimeToLiveSeconds, timeBufferPercentage);
        this.signatureProvider = signatureProvider;
        this.generationId = generationId;
    }

    private IotHubSasTokenHsmAuthenticationProvider(
        String hostname,
        String gatewayHostName,
        String deviceId,
        String moduleId,
        String generationId,
        String sharedAccessToken,
        SignatureProvider signatureProvider,
        int suggestedTimeToLiveSeconds,
        int timeBufferPercentage,
        SSLContext sslContext)
    {
        super(hostname, gatewayHostName, deviceId, moduleId, sharedAccessToken, suggestedTimeToLiveSeconds, timeBufferPercentage, sslContext);
        this.signatureProvider = signatureProvider;
        this.generationId = generationId;
    }
}
