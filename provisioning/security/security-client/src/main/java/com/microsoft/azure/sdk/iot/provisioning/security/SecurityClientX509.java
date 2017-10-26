/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.UUID;

public abstract class SecurityClientX509 implements SecurityClient
{
    private HsmType HsmType;

    abstract public String getDeviceCommonName();
    abstract public Certificate getAliasCert();
    abstract public Key getAliasKey();
    abstract public Certificate getDeviceSignerCert();

    @Override
    public String getRegistrationId() throws SecurityException
    {
        return this.getDeviceCommonName();
    }

    @Override
    public SSLContext getSSLContext() throws SecurityException
    {
        SSLContext sslContext = null;
        try
        {
            sslContext = this.generateSSLContext(this.getAliasCert(), this.getAliasKey(), this.getDeviceSignerCert());
        }
        catch (NoSuchProviderException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException | CertificateException e)
        {
            throw new SecurityException(e.getMessage());
        }

        return sslContext;
    }

    private SSLContext generateSSLContext(Certificate aliasCertificate, Key privateKey, Certificate rootCert) throws NoSuchProviderException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        String password = UUID.randomUUID().toString();
        if (aliasCertificate == null || privateKey == null)
        {
            throw new IOException("cert cannot be null");
        }

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        // create keystore
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);

        // load cert and private key to key store
        keyStore.setKeyEntry("DPSAlias", privateKey, password.toCharArray(), new Certificate[] {aliasCertificate});
        /*if (keyStore.containsAlias("DPSSignerCert"));
        {
            keyStore.deleteEntry("DPSSignerCert");
        }*/
        //keyStore.setCertificateEntry("DPSSignerCert", signerCert);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> serverCert;
        try (InputStream certStreamArray = new ByteArrayInputStream(this.DEFAULT_TRUSTED_CERT.getBytes()))
        {
            serverCert =  certFactory.generateCertificates(certStreamArray);
        }
        for (Certificate c : serverCert)
        {
            keyStore.setCertificateEntry("trustedDPSCert-" + UUID.randomUUID(), c);
        }

        // obtain X509 trust manager
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        X509TrustManager x509TrustManager = null;
        for (TrustManager trustManager : trustManagerFactory.getTrustManagers())
        {
            if (trustManager instanceof X509TrustManager)
            {
                x509TrustManager = (X509TrustManager) trustManager;
                break;
            }
        }

        if (x509TrustManager == null)
        {
            throw new NullPointerException();
        }

       // x509TrustManager.checkClientTrusted(new X509Certificate[] { (X509Certificate) serverCert}, "DHE_DSS");

        // create key manager factory and obtain x509 key manager
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());

        X509KeyManager x509KeyManager = null;
        for (KeyManager keyManager : keyManagerFactory.getKeyManagers())
        {
            if (keyManager instanceof X509KeyManager)
            {
                x509KeyManager = (X509KeyManager) keyManager;
                break;
            }
        }

        if (x509KeyManager == null)
        {
            throw new NullPointerException();
        }

        sslContext.init(new KeyManager[] {x509KeyManager}, new TrustManager[] {x509TrustManager}, new SecureRandom());
        return sslContext;
    }
}
