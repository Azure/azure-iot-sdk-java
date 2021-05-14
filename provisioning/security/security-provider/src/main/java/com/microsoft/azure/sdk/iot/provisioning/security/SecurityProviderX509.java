/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security;

import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.UUID;

public abstract class SecurityProviderX509 extends SecurityProvider
{
    private static final String ALIAS_CERT_ALIAS = "ALIAS_CERT";

    abstract public String getClientCertificateCommonName();
    abstract public X509Certificate getClientCertificate();
    abstract public Key getClientPrivateKey();
    abstract public Collection<X509Certificate> getIntermediateCertificatesChain();

    @Override
    public String getRegistrationId()
    {
        //SRS_SecurityClientX509_25_001: [ This method shall retrieve the commonName of the client certificate and return as registration Id. ]
        return this.getClientCertificateCommonName();
    }

    @Override
    public SSLContext getSSLContext() throws SecurityProviderException
    {
        try
        {
            //SRS_SecurityClientX509_25_002: [ This method shall generate the SSL context. ]
            return this.generateSSLContext(this.getClientCertificate(), this.getClientPrivateKey(), this.getIntermediateCertificatesChain());
        }
        catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException | CertificateException e)
        {
            //SRS_SecurityClientX509_25_003: [ This method shall throw SecurityProviderException chained with the exception thrown from underlying API calls to SSL library. ]
            throw new SecurityProviderException(e);
        }
    }

    private TrustManager getDefaultX509TrustManager(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException, SecurityProviderException
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

        //SRS_SecurityClientX509_25_004: [ This method shall throw SecurityProviderException if X509 Trust Manager is not found. ]
        throw new SecurityProviderException("Could not retrieve X509 trust manager");
    }

    private KeyManager getDefaultX509KeyManager(KeyStore keyStore, String password) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, SecurityProviderException
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

        //SRS_SecurityClientX509_25_005: [ This method shall throw SecurityProviderException if X509 Key Manager is not found. ]
        throw new SecurityProviderException("Could not retrieve X509 Key Manager");
    }

    private SSLContext generateSSLContext(X509Certificate leafCertificate, Key leafPrivateKey, Collection<X509Certificate> signerCertificates) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException, SecurityProviderException
    {
        if (leafCertificate == null || leafPrivateKey == null || signerCertificates == null)
        {
            //SRS_SecurityClientX509_25_006: [ This method shall throw IllegalArgumentException if input parameters are null. ]
            throw new IllegalArgumentException("cert or private key cannot be null");
        }

        //SRS_SecurityClientX509_25_007: [ This method shall use random UUID as a password for keystore. ]
        String password = UUID.randomUUID().toString();
        //SRS_SecurityClientX509_25_008: [ This method shall create a TLSv1.2 instance. ]
        SSLContext sslContext = SSLContext.getInstance(DEFAULT_TLS_PROTOCOL);
        // Load Trusted certs to keystore and retrieve it.

        //SRS_SecurityClientX509_25_009: [ This method shall retrieve the keystore loaded with trusted certs. ]
        KeyStore keyStore = this.getKeyStoreWithTrustedCerts();

        if (keyStore == null)
        {
            throw new SecurityProviderException("Key store with trusted certs cannot be null");
        }

        // Load Alias cert and private key to key store
        int noOfCerts = signerCertificates.size() + 1;
        X509Certificate[] certs = new X509Certificate[noOfCerts];
        int i = 0;
        certs[i++] = leafCertificate;

        // Load the chain of signer cert to keystore
        for (X509Certificate c : signerCertificates)
        {
            certs[i++] = c;
        }
        //SRS_SecurityClientX509_25_010: [ This method shall load all the provided X509 certs (leaf with both public certificate and private key,
        // intermediate certificates(if any) to the Key store. ]
        keyStore.setKeyEntry(ALIAS_CERT_ALIAS, leafPrivateKey, password.toCharArray(), certs);

        //SRS_SecurityClientX509_25_011: [ This method shall initialize the ssl context with X509KeyManager and X509TrustManager for the keystore. ]
        sslContext.init(new KeyManager[] {this.getDefaultX509KeyManager(keyStore, password)}, new TrustManager[] {this.getDefaultX509TrustManager(keyStore)}, new SecureRandom());
        //SRS_SecurityClientX509_25_012: [ This method shall return the ssl context created as above to the caller. ]
        return sslContext;
    }
}
