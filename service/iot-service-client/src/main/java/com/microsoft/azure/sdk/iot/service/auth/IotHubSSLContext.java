/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A simple wrapper for holding the SSLContext used by all connections from clients in this library to the service.
 */
public class IotHubSSLContext
{
    private final SSLContext sslContext;
    private static final String SSL_CONTEXT_PROTOCOL = "TLSv1.2";

    private static final String CERTIFICATE_TYPE = "X.509";

    private static final String TRUSTED_IOT_HUB_CERT_PREFIX = "trustedIotHubCert-";

    /**
     * Create a default IotHubSSLContext that trusts the certificates stored in your physical device's
     * Trusted Root Certification Authorities certificate store. This IotHubSSLContext can be used for connections
     * that use symmetric key authentication, but cannot be used for connections that use x509 authentication.
     */
    public IotHubSSLContext()
    {
        try
        {
            // Only loads public certs. Private keys are in password protected keystores,
            // so they can't be retrieved in this constructor. Because no private keys are loaded,
            // this SSLContext can only be used in connections that are authenticated via symmetric keys.
            this.sslContext = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);

            // Initializing the SSLContext with null keyManagers and null trustManagers makes it so the device's default
            // trusted certificates are loaded, and no private keys are loaded.
            this.sslContext.init(null, null, new SecureRandom());
        }
        catch (NoSuchAlgorithmException | KeyManagementException e)
        {
            throw new IllegalStateException("Failed to build the default SSLContext instance", e);
        }
    }

    /**
     * Create an IotHubSSLContext that will use the provided sslContext rather than create one based on your device's
     * Trusted Root Certification Authorities certificate store.
     * @param sslContext the SSLContext that will be used during the TLS handshake when establishing a connection to
     * the service.
     */
    public IotHubSSLContext(SSLContext sslContext)
    {
        Objects.requireNonNull(sslContext);
        this.sslContext = sslContext;
    }

    /**
     * Get the SSLContext that will be used during the TLS handshake when establishing a connection to the service.
     * @return the SSLContext that will be used during the TLS handshake when establishing a connection to the service.
     */
    public SSLContext getSSLContext()
    {
        return this.sslContext;
    }

    /**
     * Create an IotHubSSLContext that trusts the PEM formatted certificates stored in the provided trustedCertificates.
     * @param trustedCertificates the PEM formatted certificates that this IotHubSSLContext will trust.
     * @return the created IotHubSSLContext.
     * @throws CertificateException if the provided trustedCertificates are not PEM formatted and cannot be parsed.
     * @throws IOException if the provided trustedCertificates cannot be read as a stream.
     * @throws KeyStoreException if a key store cannot be created.
     * @throws NoSuchAlgorithmException if your device cannot use x509 certificates or TLS SSLContexts.
     * @throws KeyManagementException if the created SSLContext cannot be initialized.
     */
    public static SSLContext getSSLContextFromString(String trustedCertificates) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException
    {
        if (trustedCertificates == null || trustedCertificates.isEmpty())
        {
            throw new IllegalArgumentException("The provided certificate string cannot be null or empty");
        }

        try (InputStream inputStream = new ByteArrayInputStream(trustedCertificates.getBytes(StandardCharsets.UTF_8)))
        {
            return getSSLContextFromStream(inputStream);
        }
    }

    /**
     * Create an IotHubSSLContext that trusts the PEM formatted certificates stored in a file with the provided path.
     * @param trustedCertificatesFilePath the absolute file path of the file that contains the PEM formatted
     * certificates that this IotHubSSLContext will trust.
     * @return the created IotHubSSLContext.
     * @throws CertificateException if the provided trustedCertificates are not PEM formatted and cannot be parsed.
     * @throws IOException if the provided trustedCertificates cannot be read as a stream.
     * @throws KeyStoreException if a key store cannot be created.
     * @throws NoSuchAlgorithmException if your device cannot use x509 certificates or TLS SSLContexts.
     * @throws KeyManagementException if the created SSLContext cannot be initialized.
     */
    public static SSLContext getSSLContextFromFile(String trustedCertificatesFilePath) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException
    {
        if (trustedCertificatesFilePath == null || trustedCertificatesFilePath.isEmpty())
        {
            throw new IllegalArgumentException("The provided certificate path string cannot be null or empty");
        }

        try (FileInputStream fis = new FileInputStream(trustedCertificatesFilePath))
        {
            return getSSLContextFromStream(fis);
        }
    }

    private static SSLContext getSSLContextFromStream(InputStream inputStream) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException
    {
        final CertificateFactory certificateFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE);

        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(inputStream);

        TrustManagerFactory trustManagerFactory = generateTrustManagerFactory(certificates);

        SSLContext sslContext = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    private static TrustManagerFactory generateTrustManagerFactory(Collection<? extends Certificate> certificates)
        throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException
    {
        KeyStore trustKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustKeyStore.load(null);

        for (Certificate c : certificates)
        {
            trustKeyStore.setCertificateEntry(TRUSTED_IOT_HUB_CERT_PREFIX + UUID.randomUUID(), c);
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustKeyStore);

        return trustManagerFactory;
    }
}
