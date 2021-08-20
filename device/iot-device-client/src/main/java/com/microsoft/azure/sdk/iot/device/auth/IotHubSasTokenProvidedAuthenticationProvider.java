/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.SasTokenProvider;

import javax.net.ssl.SSLContext;

/**
 * {@link IotHubSasTokenAuthenticationProvider} implementation where the tokens are provided by an instance of {@link SasTokenProvider}.
 * This is used in cases like when the user creates a device client with {@link com.microsoft.azure.sdk.iot.device.DeviceClient#DeviceClient(String, String, SasTokenProvider, IotHubClientProtocol, ClientOptions)}
 */
public class IotHubSasTokenProvidedAuthenticationProvider extends IotHubSasTokenAuthenticationProvider
{
    private SasTokenProvider sasTokenProvider;
    private char[] lastSasToken;

    public IotHubSasTokenProvidedAuthenticationProvider(String hostName, String deviceId, String moduleId, SasTokenProvider sasTokenProvider, SSLContext sslContext) {
        super(hostName, null, deviceId, moduleId, sslContext);

        if (sasTokenProvider == null)
        {
            throw new IllegalArgumentException("SAS token provider cannot be null");
        }

        this.sasTokenProvider = sasTokenProvider;
    }

    @Override
    public boolean isAuthenticationProviderRenewalNecessary()
    {
        // Renewal of the authentication provider itself is never needed since the SAS token provider is responsible
        // for providing SAS tokens indefinitely.
        return false;
    }

    @Override
    public void setTokenValidSecs(long tokenValidSecs)
    {
        throw new UnsupportedOperationException("Cannot configure SAS token time to live when custom SAS token provider is in use");
    }

    @Override
    public boolean canRefreshToken()
    {
        // User is always capable of providing a new SAS token when using this authentication provider.
        return true;
    }

    @Override
    public char[] getSasToken()
    {
        lastSasToken = sasTokenProvider.getSasToken();
        return lastSasToken;
    }

    @Override
    public int getMillisecondsBeforeProactiveRenewal()
    {
        // Seconds since UNIX epoch when this sas token will expire
        long expiryTimeSeconds = IotHubSasToken.getExpiryTimeFromToken(new String(lastSasToken));

        // Assuming that the token's life "starts" now for the sake of figuring out when it needs to be renewed. Users
        // could theoretically give us a SAS token that started a while ago, but since we have no way of figuring that out,
        // we will conservatively just renew at timeBufferPercentage% of the remaining time on the token, rather than timeBufferPercentage% of the time the token
        // has existed for.
        long tokenValidSeconds = expiryTimeSeconds - (System.currentTimeMillis() / 1000);

        double timeBufferMultiplier = this.timeBufferPercentage / 100.0; //Convert 85 to .85, for example. Percentage multipliers are in decimal
        return (int) (tokenValidSeconds * 1000 * timeBufferMultiplier);
    }
}
