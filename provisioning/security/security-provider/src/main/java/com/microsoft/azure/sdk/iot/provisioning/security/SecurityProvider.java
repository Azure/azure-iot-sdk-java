/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security;

import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public abstract class SecurityProvider
{
    static final String SSL_CONTEXT_PROTOCOL = "TLSv1.2";

    /**
     * Unique id required for registration
     * @return Returns the registration Id used needed for the service
     * @throws SecurityProviderException If registration id with the underlying implementation could not be retrieved
     */
    abstract public String getRegistrationId() throws SecurityProviderException;

    /**
     * Retrieves the SSL context loaded with trusted certs. This default implementation loads the trusted certificates
     * from your device's trusted root certification authorities certificate store. Implementations of {@link SecurityProviderX509}
     * must override this function so that it returns an SSLContext instance with the required private key and public certificates
     * loaded into it as well as the default trusted certificates saved in your device's trusted root certification authorities certificate store.
     * @return The SSLContext instance.
     * @throws SecurityProviderException If ssl context could not be generated for any of the reason.
     */
    public SSLContext getSSLContext() throws SecurityProviderException
    {
        try
        {
            SSLContext sslContext = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
            sslContext.init(null, null, new SecureRandom());
            return sslContext;
        }
        catch (NoSuchAlgorithmException | KeyManagementException e)
        {
            throw new SecurityProviderException("Failed to create the default SSLContext instance", e);
        }
    }
}
