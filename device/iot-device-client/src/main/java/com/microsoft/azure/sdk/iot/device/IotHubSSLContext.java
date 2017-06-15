// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.UUID;

public class IotHubSSLContext
{
    private SSLContext iotHubSslContext = null;

    /**
     * Creates a SSLContext for the IotHub.
     *
     * @throws NoSuchAlgorithmException   if no Provider supports a TrustManagerFactorySpi implementation for the specified protocol.
     * @throws KeyStoreException  if no Provider supports a KeyStoreSpi implementation for the specified type or
     *                            if the keystore has not been initialized,
     *                            or the given alias already exists and does not identify an entry containing a trusted certificate,
     *                            or this operation fails for some other reason.
     * @throws KeyManagementException As per https://docs.oracle.com/javase/7/docs/api/java/security/KeyManagementException.html
     * @throws IOException If the certificate provided was null or invalid
     * @throws CertificateException As per https://docs.oracle.com/javase/7/docs/api/java/security/cert/CertificateException.html
     */
    IotHubSSLContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //Codes_SRS_IOTHUBSSLCONTEXT_25_001: [**The constructor shall create a default certificate to be used with IotHub.**]**
        IotHubCertificateManager defaultCert = new IotHubCertificateManager();
        generateSSLContext(defaultCert);
    }

    /**
     * Creates a SSLContext for the IotHub with the specified certificate.
     * @param cert Certificate to be used to communicate with IotHub
     * @param isPath If the certificate is a path or not
     * @throws NoSuchAlgorithmException   if no Provider supports a TrustManagerFactorySpi implementation for the specified protocol.
     * @throws KeyStoreException  if no Provider supports a KeyStoreSpi implementation for the specified type or
     *                            if the keystore has not been initialized,
     *                            or the given alias already exists and does not identify an entry containing a trusted certificate,
     *                            or this operation fails for some other reason.
     * @throws KeyManagementException As per https://docs.oracle.com/javase/7/docs/api/java/security/KeyManagementException.html
     * @throws IOException If the certificate provided was null or invalid
     * @throws CertificateException As per https://docs.oracle.com/javase/7/docs/api/java/security/cert/CertificateException.html
     */
    IotHubSSLContext(String cert, boolean isPath) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        if (cert == null)
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_25_008: [**The constructor shall throw IllegalArgumentException if any of the parameters are null.**]**
            throw new IllegalArgumentException("Cert cannot be null");
        }

        IotHubCertificateManager certManager = new IotHubCertificateManager();

        if (isPath)
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_25_009: [**The constructor shall create a certificate to be used with IotHub with cert only if it were a path by calling setValidCertPath**]**
            certManager.setValidCertPath(cert);
        }
        else
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_25_010: [**The constructor shall create a certificate with 'cert' if it were a not a path by calling setValidCert.**]**
            certManager.setValidCert(cert);
        }

        generateSSLContext(certManager);
    }

    /**
     * Creates a SSLContext for the IotHub with the specified certificate.
     *      If the pathToCertificate is not {@code null}, a certificate will be read from the file.
     *      If the userCertificateString is not {@code null}, it will be set as the certificate.
     *      If no certificate is provided, a new default certificate will be generated.
     * @param pathToCertificate is the path to certificate. It can be {@code null}.
     * @param userCertificateString is the string with the certificate. It can be {@code null}.
     * @throws NoSuchAlgorithmException   if no Provider supports a TrustManagerFactorySpi implementation for the specified protocol.
     * @throws KeyStoreException  if no Provider supports a KeyStoreSpi implementation for the specified type or
     *                            if the keystore has not been initialized,
     *                            or the given alias already exists and does not identify an entry containing a trusted certificate,
     *                            or this operation fails for some other reason.
     * @throws KeyManagementException As per https://docs.oracle.com/javase/7/docs/api/java/security/KeyManagementException.html
     * @throws IOException If the certificate provided was null or invalid
     * @throws CertificateException As per https://docs.oracle.com/javase/7/docs/api/java/security/cert/CertificateException.html
     */
    IotHubSSLContext(String pathToCertificate, String userCertificateString)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        IotHubCertificateManager certManager = new IotHubCertificateManager();
        if (pathToCertificate != null)
        {
            // Codes_SRS_IOTHUBSSLCONTEXT_21_018: [If the pathToCertificate is not null, the constructor shall create a certificate to be used with IotHub with cert by calling setValidCertPath]
            certManager.setValidCertPath(pathToCertificate);
        }
        else if (userCertificateString != null)
        {
            // Codes_SRS_IOTHUBSSLCONTEXT_21_019: [If the userCertificateString is not null, and pathToCertificate is null, the constructor shall create a certificate with 'cert' by calling setValidCert.]
            certManager.setValidCert(userCertificateString);
        }
        // Codes_SRS_IOTHUBSSLCONTEXT_21_020: [If both userCertificateString, and pathToCertificate are null, the constructor shall create a default certificate.]
        generateSSLContext(certManager);
    }

    private void generateSSLContext(IotHubCertificateManager certificate) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //Codes_SRS_IOTHUBSSLCONTEXT_25_002: [**The constructor shall create default SSL context for TLSv1.2.**]**
        //Codes_SRS_IOTHUBSSLCONTEXT_25_003: [**The constructor shall create default TrustManagerFactory with the default algorithm.**]**
        //Codes_SRS_IOTHUBSSLCONTEXT_25_004: [**The constructor shall create default KeyStore instance with the default type and initialize it.**]**
        //Codes_SRS_IOTHUBSSLCONTEXT_25_005: [**The constructor shall set the above created certificate into a keystore.**]**
        //Codes_SRS_IOTHUBSSLCONTEXT_25_006: [**The constructor shall initialize TrustManagerFactory with the above initialized keystore.**]**
        //Codes_SRS_IOTHUBSSLCONTEXT_25_007: [**The constructor shall initialize SSL context with the above initialized TrustManagerFactory and a new secure random.**]**
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        for (Certificate c : certificate.getCertificateCollection())
        {
            keyStore.setCertificateEntry("trustedIotHubCert-" + UUID.randomUUID(), c);
        }
        trustManagerFactory.init(keyStore);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        this.iotHubSslContext = sslContext;
    }

    /**
     * Getter for the IotHubSSLContext
     * @return SSLContext defined for the IotHub.
     */
    public SSLContext getIotHubSSlContext()
    {
        //Codes_SRS_IOTHUBSSLCONTEXT_25_017: [*This method shall return the value of sslContext.**]**
        return this.iotHubSslContext;
    }
}
