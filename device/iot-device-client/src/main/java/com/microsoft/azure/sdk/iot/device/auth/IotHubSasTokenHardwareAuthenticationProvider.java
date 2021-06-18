/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

public class IotHubSasTokenHardwareAuthenticationProvider extends IotHubSasTokenAuthenticationProvider
{
    private static final String TOKEN_SCOPE_FORMAT = "%s/devices/%s";
    private static final String SASTOKEN_FORMAT = "SharedAccessSignature sr=%s&sig=%s&se=%s";

    protected SecurityProviderTpm securityProvider;

    /**
     * Creates a Sas Token based authentication object that uses the provided security provider to produce sas tokens.
     *
     * @param hostname The host name of the hub to authenticate against
     * @param gatewayHostname The gateway hostname to use, or null if connecting to an IotHub
     * @param deviceId The unique id of the device to authenticate
     * @param moduleId the module id. May be null if not using a module
     * @param securityProvider the security provider to use for authentication
     * @throws SecurityProviderException if the provided securityProvider throws while retrieving a sas token or ssl context instance
     */
    public IotHubSasTokenHardwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, SecurityProvider securityProvider) throws SecurityProviderException
    {
        super(hostname, gatewayHostname, deviceId, moduleId);

        if (!(securityProvider instanceof SecurityProviderTpm))
        {
            throw new IllegalArgumentException("The provided security provided must be an instance of SecurityProviderTpm");
        }

        this.securityProvider = (SecurityProviderTpm) securityProvider;

        this.sasToken = new IotHubSasToken(hostname, deviceId, null, this.generateSasTokenSignatureFromSecurityProvider(this.tokenValidSecs), moduleId, 0);

        this.iotHubSSLContext = new IotHubSSLContext(securityProvider.getSSLContext());
    }

    /**
     * Getter for SasToken. If the saved token has expired, this method shall renew it if possible
     *
     * @return The value of SasToken
     */
    public char[] getSasToken()
    {
        String sasTokenString = this.generateSasTokenSignatureFromSecurityProvider(this.tokenValidSecs);
        this.sasToken = new IotHubSasToken(this.hostname, this.deviceId, null, sasTokenString, this.moduleId, 0);
        return this.sasToken.toString().toCharArray();
    }

    @Override
    public boolean canRefreshToken()
    {
        return true;
    }

    /**
     * Getter for SSLContext
     * @throws SecurityProviderException if an error occurs when generating the SSLContext
     * @return The value of SSLContext
     */
    @Override
    public SSLContext getSSLContext() throws SecurityProviderException
    {
        return this.iotHubSSLContext.getSSLContext();
    }

    /**
     * @return always returns false as the hardware authentication mechanism will never need to be updated with a new key or token
     */
    @Override
    public boolean isAuthenticationProviderRenewalNecessary()
    {
        //Hardware will always be able to generate new sas tokens
        return false;
    }

    private String generateSasTokenSignatureFromSecurityProvider(long secondsToLive)
    {
        try
        {
            //token scope is formatted as "<hostName>/devices/<deviceId>"
            String tokenScope = String.format(TOKEN_SCOPE_FORMAT, this.hostname, this.deviceId);
            String encodedTokenScope = URLEncoder.encode(tokenScope, ENCODING_FORMAT_NAME);
            if (encodedTokenScope == null || encodedTokenScope.isEmpty())
            {
                throw new IllegalArgumentException("Could not construct token scope");
            }

            Long expiryTimeUTC = (System.currentTimeMillis() / 1000) + secondsToLive;
            byte[] token = this.securityProvider.signWithIdentity(encodedTokenScope.concat("\n" + expiryTimeUTC).getBytes());
            if (token == null || token.length == 0)
            {
                throw new IllegalArgumentException("Security provider could not sign data successfully");
            }

            byte[] base64Signature = encodeBase64(token);
            String base64UrlEncodedSignature = URLEncoder.encode(new String(base64Signature), ENCODING_FORMAT_NAME);
            return String.format(SASTOKEN_FORMAT, encodedTokenScope, base64UrlEncodedSignature, expiryTimeUTC);
        }
        catch (UnsupportedEncodingException | SecurityProviderException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
