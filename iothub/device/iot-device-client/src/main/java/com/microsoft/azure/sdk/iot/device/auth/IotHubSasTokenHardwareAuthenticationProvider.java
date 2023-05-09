/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

public class IotHubSasTokenHardwareAuthenticationProvider extends IotHubSasTokenAuthenticationProvider
{
    private static final String TOKEN_SCOPE_FORMAT = "%s/devices/%s";
    private static final String SASTOKEN_FORMAT = "SharedAccessSignature sr=%s&sig=%s&se=%s";

    private final SecurityProviderTpm securityProvider;

    /**
     * Creates a Sas Token based authentication object that uses the provided security provider to produce sas tokens.
     *
     * @param hostname The host name of the hub to authenticate against
     * @param gatewayHostname The gateway hostname to use, or null if connecting to an IotHub
     * @param mqttGatewayHostname The mqttGatewayHostname to use in E4K context, or null if connecting to an IotHub
     * @param deviceId The unique id of the device to authenticate
     * @param moduleId the module id. May be null if not using a module
     * @param securityProvider the security provider to use for authentication
     * @throws IOException if the provided securityProvider throws while retrieving a sas token or ssl context instance
     */
    public IotHubSasTokenHardwareAuthenticationProvider(String hostname, String gatewayHostname, String mqttGatewayHostname, String deviceId, String moduleId, SecurityProvider securityProvider) throws IOException
    {
        super(hostname, gatewayHostname, mqttGatewayHostname, deviceId, moduleId);

        try
        {
            if (!(securityProvider instanceof SecurityProviderTpm))
            {
                //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_003: [If the provided security provider is not an instance of SecurityProviderTpm, this function shall throw an IllegalArgumentException.]
                throw new IllegalArgumentException("The provided security provided must be an instance of SecurityProviderTpm");
            }

            this.securityProvider = (SecurityProviderTpm) securityProvider;
            this.sasToken = new IotHubSasToken(hostname, deviceId, null, this.generateSasTokenSignatureFromSecurityProvider(this.tokenValidSecs), moduleId, 0);
            this.iotHubSSLContext = new IotHubSSLContext(securityProvider.getSSLContext());
        }
        catch (SecurityProviderException e)
        {
            throw new IOException(e);
        }
    }

    /**
     * Getter for SasToken. If the saved token has expired, this method shall renew it if possible
     *
     * @throws IOException if generating the sas token from the TPM fails
     * @return The value of SasToken
     */
    public char[] getSasToken() throws IOException
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
     * @return always returns false as the hardware authentication mechanism will never need to be updated with a new key or token
     */
    @Override
    public boolean isAuthenticationProviderRenewalNecessary()
    {
        //Hardware will always be able to generate new sas tokens
        return false;
    }

    private String generateSasTokenSignatureFromSecurityProvider(long secondsToLive) throws IOException
    {
        try
        {
            //token scope is formatted as "<hostName>/devices/<deviceId>"
            String tokenScope = String.format(TOKEN_SCOPE_FORMAT, this.hostname, this.deviceId);
            String encodedTokenScope = URLEncoder.encode(tokenScope, ENCODING_FORMAT_NAME);
            if (encodedTokenScope == null || encodedTokenScope.isEmpty())
            {
                throw new IOException("Could not construct token scope");
            }

            Long expiryTimeUTC = (System.currentTimeMillis() / 1000) + secondsToLive;
            byte[] token = this.securityProvider.signWithIdentity(encodedTokenScope.concat("\n" + expiryTimeUTC).getBytes(StandardCharsets.UTF_8));
            if (token == null || token.length == 0)
            {
                throw new IOException("Security provider could not sign data successfully");
            }

            byte[] base64Signature = encodeBase64(token);
            String base64UrlEncodedSignature = URLEncoder.encode(new String(base64Signature, StandardCharsets.UTF_8), ENCODING_FORMAT_NAME);
            return String.format(SASTOKEN_FORMAT, encodedTokenScope, base64UrlEncodedSignature, expiryTimeUTC);
        }
        catch (UnsupportedEncodingException | SecurityProviderException e)
        {
            throw new IOException(e);
        }
    }
}
