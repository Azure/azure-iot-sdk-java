/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
     * @throws IOException if the provided securityProvider throws while retrieving a sas token or ssl context instance
     */
    public IotHubSasTokenHardwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, SecurityProvider securityProvider) throws IOException
    {
        super(hostname, gatewayHostname, deviceId, moduleId);

        try
        {
            if (!(securityProvider instanceof SecurityProviderTpm))
            {
                //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_003: [If the provided security provider is not an instance of SecurityProviderTpm, this function shall throw an IllegalArgumentException.]
                throw new IllegalArgumentException("The provided security provided must be an instance of SecurityProviderTpm");
            }

            this.securityProvider = (SecurityProviderTpm) securityProvider;

            // Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_033: [This constructor shall generate and save a sas token from the security provider with the default time to live.]
            this.sasToken = new IotHubSasToken(hostname, deviceId, null, this.generateSasTokenSignatureFromSecurityProvider(this.tokenValidSecs), moduleId, 0);

            // Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_034: [This constructor shall retrieve and save the ssl context from the security provider.]
            this.iotHubSSLContext = new IotHubSSLContext(securityProvider.getSSLContext());

            this.sslContextNeedsUpdate = false;
        }
        catch (SecurityProviderException e)
        {
            //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_023: [If the security provider throws an exception while retrieving a sas token or ssl context from it, this function shall throw an IOException.]
            throw new IOException(e);
        }
    }

    /**
     * Getter for SasToken. If the saved token has expired, this method shall renew it if possible
     *
     * @param proactivelyRenew if true, this method will generate a fresh sas token even if the previously saved token
     *                                 has not expired yet as long as the current token has lived beyond its buffer.
     *                                 Use this for pre-emptively renewing sas tokens.

     * @throws IOException if generating the sas token from the TPM fails
     * @return The value of SasToken
     */
    public String getRenewedSasToken(boolean proactivelyRenew, boolean forceRenewal) throws IOException
    {
        if (this.shouldRefreshToken(proactivelyRenew) || forceRenewal)
        {
            //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_035: [If the saved sas token has expired and there is a security provider, the saved sas token shall be refreshed with a new token from the security provider.]
            //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_036: [If the saved sas token has not expired and there is a security provider, but the sas token should be proactively renewed, the saved sas token shall be refreshed with a new token from the security provider.]
            String sasTokenString = this.generateSasTokenSignatureFromSecurityProvider(this.tokenValidSecs);
            this.sasToken = new IotHubSasToken(this.hostname, this.deviceId, null, sasTokenString, this.moduleId, 0);
        }

        //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_005: [This function shall return the saved sas token.]
        return this.sasToken.toString();
    }

    @Override
    public boolean canRefreshToken()
    {
        //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_013: [This function shall return true.]
        return true;
    }

    /**
     * Getter for SSLContext
     * @throws IOException if an error occurs when generating the SSLContext
     * @return The value of SSLContext
     */
    @Override
    public SSLContext getSSLContext() throws IOException
    {
        //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_008: [This function shall return the generated IotHubSSLContext.]
        return this.iotHubSSLContext.getSSLContext();
    }

    /**
     * Setter for the providing trusted certificate.
     * @param pathToCertificate path to the certificate for one way authentication.
     */
    @Override
    public void setPathToIotHubTrustedCert(String pathToCertificate)
    {
        //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_001: [This function shall throw an UnsupportedOperationException.]
        throw new UnsupportedOperationException("Cannot change the trusted certificate when using security provider for authentication.");
    }

    /**
     * Setter for the user trusted certificate
     * @param certificate valid user trusted certificate string
     */
    @Override
    public void setIotHubTrustedCert(String certificate)
    {
        //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_002: [This function shall throw an UnsupportedOperationException.]
        throw new UnsupportedOperationException("Cannot change the trusted certificate when using security provider for authentication.");
    }

    /**
     * @return always returns false as the hardware authentication mechanism will never need to be updated with a new key or token
     */
    @Override
    public boolean isRenewalNecessary()
    {
        //Hardware will always be able to generate new sas tokens
        //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_012: [This function shall return false.]
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
                //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_009: [If the token scope cannot be encoded, this function shall throw an IOException.]
                throw new IOException("Could not construct token scope");
            }

            Long expiryTimeUTC = (System.currentTimeMillis() / 1000) + secondsToLive;
            byte[] token = this.securityProvider.signWithIdentity(encodedTokenScope.concat("\n" + String.valueOf(expiryTimeUTC)).getBytes());
            if (token == null || token.length == 0)
            {
                //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_010: [If the call for the saved security provider to sign with identity returns null or empty bytes, this function shall throw an IOException.]
                throw new IOException("Security provider could not sign data successfully");
            }

            byte[] base64Signature = Base64.encodeBase64Local(token);
            String base64UrlEncodedSignature = URLEncoder.encode(new String(base64Signature), ENCODING_FORMAT_NAME);
            return String.format(SASTOKEN_FORMAT, encodedTokenScope, base64UrlEncodedSignature, expiryTimeUTC);
        }
        catch (UnsupportedEncodingException | SecurityProviderException e)
        {
            //Codes_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_011: [When generating the sas token signature from the security provider, if an UnsupportedEncodingException or SecurityProviderException is thrown, this function shall throw an IOException.]
            throw new IOException(e);
        }
    }
}
