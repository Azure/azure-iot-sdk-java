/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.commons.codec.binary.Base32;

import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.UUID;

public abstract class SecurityClientKey implements SecurityClient
{
    private HsmType HsmType;

    abstract public byte[] importKey(byte[] key) throws IOException;
    abstract public byte[] getDeviceEk();
    abstract public byte[] getDeviceSRK();
    abstract public byte[] signData(byte[] data);

    @Override
    public String getRegistrationId() throws SecurityException
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
            byte[] ek = this.getDeviceEk();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ek);
            Base32 base32 = new Base32();
            byte[] base32Encoded = base32.encode(hash);
            String base32EncodedString = new String(base32Encoded);
            if (base32EncodedString.contains("="))
            {
                registrationId = base32EncodedString.replace("=", "").toLowerCase();
            }

            System.out.println("registration id \n" + registrationId);
            return registrationId;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new SecurityException(e.getMessage());
        }
    }

    private SSLContext generateSSLContext() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, KeyManagementException
    {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        // create keystore
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        Collection<? extends Certificate> trustedCert;

        try (InputStream certStreamArray = new ByteArrayInputStream(this.DEFAULT_TRUSTED_CERT.getBytes()))
        {
            trustedCert =  certFactory.generateCertificates(certStreamArray);
        }
        for (Certificate c : trustedCert)
        {
            keyStore.setCertificateEntry("trustedDPSCert-" + UUID.randomUUID(), c);
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    @Override
    public SSLContext getSSLContext() throws SecurityException
    {
        try
        {
            return this.generateSSLContext();
        }
        catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException | KeyManagementException e)
        {
            throw new SecurityException(e.getMessage());
        }
    }
}
