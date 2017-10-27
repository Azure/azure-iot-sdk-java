/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security;

import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityClientException;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.UUID;

public abstract class SecurityClientX509 extends SecurityClient
{
    private static final String ALIAS_CERT_ALIAS = "ALIAS_CERT";
    private HsmType HsmType;

    abstract public String getDeviceCommonName();
    abstract public Certificate getAliasCert();
    abstract public Key getAliasKey();
    abstract public Certificate getDeviceSignerCert();

    @Override
    public String getRegistrationId() throws SecurityClientException
    {
        return this.getDeviceCommonName();
    }

    @Override
    public SSLContext getSSLContext() throws SecurityClientException
    {
        try
        {
            return this.generateSSLContext(this.getAliasCert(), this.getAliasKey(), this.getDeviceSignerCert());
        }
        catch (NoSuchProviderException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException | CertificateException e)
        {
            throw new SecurityClientException(e);
        }
    }

    private TrustManager getDefaultX509TrustManager(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException, SecurityClientException
    {
        // obtain X509 trust manager
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        for (TrustManager trustManager : trustManagerFactory.getTrustManagers())
        {
            if (trustManager instanceof X509TrustManager)
            {
                return trustManager;
            }
        }

        throw new SecurityClientException("Could not retrieve X509 trust manager");
    }

    private KeyManager getDefaultX509KeyManager(KeyStore keyStore, String password) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, SecurityClientException
    {
        // create key manager factory and obtain x509 key manager

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());

        for (KeyManager keyManager : keyManagerFactory.getKeyManagers())
        {
            if (keyManager instanceof X509KeyManager)
            {
                return keyManager;
            }
        }

        throw new SecurityClientException("Could not retrieve X509 Key Manager");
    }

    private SSLContext generateSSLContext(Certificate aliasCertificate, Key privateKey, Certificate signerCertificate) throws NoSuchProviderException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException, SecurityClientException
    {
        if (aliasCertificate == null || privateKey == null || signerCertificate == null)
        {
            throw new IllegalArgumentException("cert or private key cannot be null");
        }

        String password = UUID.randomUUID().toString();
        SSLContext sslContext = SSLContext.getInstance(DEFAULT_TLS_PROTOCOL);
        // Load Trusted certs to keystore and retrieve it.

        KeyStore keyStore = this.getKeyStoreWithTrustedCerts();

        // Load Alias cert and private key to key store
        keyStore.setKeyEntry(ALIAS_CERT_ALIAS, privateKey, password.toCharArray(), new Certificate[] {aliasCertificate});

        //TODO : determine if signer cert is also suppose to be set on SSL context
        //keyStore.setCertificateEntry("DPSSignerCert", signerCert);

        sslContext.init(new KeyManager[] {this.getDefaultX509KeyManager(keyStore, password)}, new TrustManager[] {this.getDefaultX509TrustManager(keyStore)}, new SecureRandom());
        return sslContext;
    }
}
