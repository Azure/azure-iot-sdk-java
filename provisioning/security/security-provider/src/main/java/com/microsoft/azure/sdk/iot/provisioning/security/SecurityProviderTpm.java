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

public abstract class SecurityProviderTpm extends SecurityProvider
{
    private static final String SHA_256 = "SHA-256";
    private static final String EQUALS = "=";
    abstract public byte[] activateIdentityKey(byte[] key) throws SecurityClientException;
    abstract public byte[] getEndorsementKey() throws SecurityClientException;
    abstract public byte[] getStorageRootKey() throws SecurityClientException;
    abstract public byte[] signWithIdentity(byte[] data) throws SecurityClientException;

    @Override
    public String getRegistrationId() throws SecurityClientException
    {
        try
        {
            //SRS_SecurityClientTpm_25_001: [ This method shall retrieve the EnrollmentKey from the implementation of this abstract class. ]
            byte[] enrollmentKey = this.getEndorsementKey();

            //SRS_SecurityClientTpm_25_002: [ This method shall hash the EnrollmentKey using SHA-256. ]
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(enrollmentKey);

            //SRS_SecurityClientTpm_25_003: [ This method shall convert the resultant hash to Base32 to convert all the data to be case agnostic and remove "=" from the string. ]
            Base32 base32 = new Base32();
            byte[] base32Encoded = base32.encode(hash);

            String registrationId = new String(base32Encoded).toLowerCase();
            if (registrationId.contains(EQUALS))
            {
                registrationId = registrationId.replace(EQUALS, "").toLowerCase();
            }
            return registrationId;
        }
        catch (NoSuchAlgorithmException e)
        {
            //SRS_SecurityClientTpm_25_008: [ This method shall throw SecurityClientException if any of the underlying API's in generating registration Id. ]
            throw new SecurityClientException(e);
        }
    }

    @Override
    public SSLContext getSSLContext() throws SecurityClientException
    {
        try
        {
            //SRS_SecurityClientTpm_25_004: [ This method shall generate SSLContext for this flow. ]
            return this.generateSSLContext();
        }
        catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException | KeyManagementException e)
        {
            //SRS_SecurityClientTpm_25_005: [ This method shall throw SecurityClientException if any of the underlying API's in generating SSL context fails. ]
            throw new SecurityClientException(e);
        }
    }

    private SSLContext generateSSLContext() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, KeyManagementException
    {
        SSLContext sslContext = SSLContext.getInstance(DEFAULT_TLS_PROTOCOL);

        // create keystore
        //SRS_SecurityClientTpm_25_006: [ This method shall load the keystore with TrustedCerts. ]
        KeyStore keyStore = this.getKeyStoreWithTrustedCerts();

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        //SRS_SecurityClientTpm_25_007: [ This method shall initialize SSLContext with the default trustManager loaded with keystore. ]
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }
}
