/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;

import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityClientException;
import org.apache.commons.codec.binary.Base32;

import java.security.*;
import java.security.cert.CertificateException;

public abstract class SecurityClientTpm extends SecurityClient
{
    private static final String SHA_256 = "SHA-256";
    abstract public byte[] decryptAndStoreKey(byte[] key) throws SecurityClientException;
    abstract public byte[] getDeviceEnrollmentKey() throws SecurityClientException;
    abstract public byte[] getDeviceStorageRootKey() throws SecurityClientException;
    abstract public byte[] signData(byte[] data) throws SecurityClientException;

    @Override
    public String getRegistrationId() throws SecurityClientException
    {
        /*
        1. get ek
        2. sha256
        3. base 32
        4. remove "="
         */
        String registrationId = null;
        try
        {
            byte[] ek = this.getDeviceEnrollmentKey();
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(ek);
            Base32 base32 = new Base32();
            byte[] base32Encoded = base32.encode(hash);
            String base32EncodedString = new String(base32Encoded);
            if (base32EncodedString.contains("="))
            {
                registrationId = base32EncodedString.replace("=", "").toLowerCase();
            }
            return registrationId;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new SecurityClientException(e);
        }
    }

    @Override
    public SSLContext getSSLContext() throws SecurityClientException
    {
        try
        {
            return this.generateSSLContext();
        }
        catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException | KeyManagementException e)
        {
            throw new SecurityClientException(e);
        }
    }

    private SSLContext generateSSLContext() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, KeyManagementException
    {
        SSLContext sslContext = SSLContext.getInstance(DEFAULT_TLS_PROTOCOL);

        // create keystore
        KeyStore keyStore = this.getKeyStoreWithTrustedCerts();

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }
}
