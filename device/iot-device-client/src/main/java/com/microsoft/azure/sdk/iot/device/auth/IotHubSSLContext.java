// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.auth;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

public class IotHubSSLContext
{
    private SSLContext sslContext = null;

    private static final String SSL_CONTEXT_INSTANCE = "TLSv1.2";

    private static final String CERTIFICATE_ALIAS = "cert-alias";
    private static final String PRIVATE_KEY_ALIAS = "key-alias";

    private static final String TRUSTED_IOT_HUB_CERT_PREFIX = "trustedIotHubCert-";

    /**
     * Creates a SSLContext for the IotHub.
     *
     * @throws KeyStoreException  if no Provider supports a KeyStoreSpi implementation for the specified type or
     *                            if the keystore has not been initialized,
     *                            or the given alias already exists and does not identify an entry containing a trusted certificate,
     *                            or this operation fails for some other reason.
     * @throws KeyManagementException As per https://docs.oracle.com/javase/7/docs/api/java/security/KeyManagementException.html
     * @throws IOException If the certificate provided was null or invalid
     * @throws CertificateException As per https://docs.oracle.com/javase/7/docs/api/java/security/cert/CertificateException.html
     * @throws NoSuchAlgorithmException if the default SSL Context cannot be created
     */
    IotHubSSLContext()
            throws KeyStoreException, KeyManagementException, IOException, CertificateException, NoSuchAlgorithmException
    {
        //Codes_SRS_IOTHUBSSLCONTEXT_25_001: [**The constructor shall create a default certificate to be used with IotHub.**]**
        IotHubCertificateManager defaultCert = new IotHubCertificateManager();
        generateDefaultSSLContext(defaultCert);
    }

    /**
     * Constructor that takes and saves an SSLContext object
     * @param sslContext the ssl context to save
     */
    IotHubSSLContext(SSLContext sslContext)
    {
        if (sslContext == null)
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_34_028: [If the provided sslContext is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("sslContext cannot be null");
        }

        //Codes_SRS_IOTHUBSSLCONTEXT_34_027: [This constructor shall save the provided ssl context.]
        this.sslContext = sslContext;
    }

    /**
     * Creates a default SSLContext for the IotHub with the specified certificate.
     *
     * @throws KeyStoreException  if no Provider supports a KeyStoreSpi implementation for the specified type or
     *                            if the keystore has not been initialized,
     *                            or the given alias already exists and does not identify an entry containing a trusted certificate,
     *                            or this operation fails for some other reason.
     * @throws KeyManagementException As per https://docs.oracle.com/javase/7/docs/api/java/security/KeyManagementException.html
     * @throws IOException If the certificate provided was null or invalid
     * @throws CertificateException As per https://docs.oracle.com/javase/7/docs/api/java/security/cert/CertificateException.html
     * @throws NoSuchAlgorithmException if the default SSL Context cannot be created
     */
    IotHubSSLContext(String trustedCert, boolean isTrustedCert)
            throws KeyStoreException, KeyManagementException, IOException, CertificateException, NoSuchAlgorithmException
    {
        IotHubCertificateManager defaultCert = new IotHubCertificateManager();

        if (isTrustedCert)
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_34_025: [If the provided cert is a path, this function shall set the path of the default cert to the provided cert path.]
            defaultCert.setValidCertPath(trustedCert);
        }
        else
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_34_026: [If the provided cert is not a path, this function shall set the default cert to the provided cert.]
            defaultCert.setValidCert(trustedCert);
        }

        generateDefaultSSLContext(defaultCert);
    }

    /**
     * Creates a default SSLContext for the IotHub with the specified certificate.
     *
     * @throws KeyStoreException  if no Provider supports a KeyStoreSpi implementation for the specified type or
     *                            if the keystore has not been initialized,
     *                            or the given alias already exists and does not identify an entry containing a trusted certificate,
     *                            or this operation fails for some other reason.
     * @throws KeyManagementException As per https://docs.oracle.com/javase/7/docs/api/java/security/KeyManagementException.html
     * @throws IOException If the certificate provided was null or invalid
     * @throws CertificateException As per https://docs.oracle.com/javase/7/docs/api/java/security/cert/CertificateException.html
     * @throws NoSuchAlgorithmException if the default SSL Context cannot be created
     */
    IotHubSSLContext(String publicKeyCertificateString, String privateKeyString, String cert, boolean isPath)
            throws KeyStoreException, KeyManagementException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException
    {
        IotHubCertificateManager defaultCert = new IotHubCertificateManager();

        if (isPath)
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_34_040: [If the provided cert is a path, this function shall set the path of the default cert to the provided cert path.]
            defaultCert.setValidCertPath(cert);
        }
        else
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_34_041: [If the provided cert is not a path, this function shall set the default cert to the provided cert.]
            defaultCert.setValidCert(cert);
        }

        // Codes_SRS_IOTHUBSSLCONTEXT_34_042: [This constructor shall generate a temporary password to protect the created keystore holding the private key.]
        // Codes_SRS_IOTHUBSSLCONTEXT_34_043: [The constructor shall create default SSL context for TLSv1.2.]
        // Codes_SRS_IOTHUBSSLCONTEXT_34_044: [The constructor shall create a keystore containing the public key certificate and the private key.]
        // Codes_SRS_IOTHUBSSLCONTEXT_34_045: [The constructor shall initialize a default trust manager factory that accepts communications from Iot Hub.]
        // Codes_SRS_IOTHUBSSLCONTEXT_34_046: [The constructor shall initialize SSL context with its initialized keystore, its initialized TrustManagerFactory and a new secure random.]
        generateSSLContextWithKeys(publicKeyCertificateString, privateKeyString, defaultCert);
    }

    /**
     * Constructor that takes a public key certificate and private key pair.
     *
     * @param publicKeyCertificateString The PEM formatted public key certificate string
     * @param privateKeyString The PEM formatted private key string
     * @throws KeyManagementException If the SSLContext could not be initialized
     * @throws IOException If an IO exception occurs
     * @throws CertificateException If a certificate cannot be loaded
     * @throws KeyStoreException If the provided certificates cannot be loaded into the JVM keystore
     * @throws UnrecoverableKeyException if accessing the passphrase protected keystore fails due to the key
     * @throws NoSuchAlgorithmException if the default SSLContext cannot be generated
     */
    IotHubSSLContext(String publicKeyCertificateString, String privateKeyString)
            throws KeyManagementException, IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException
    {
        generateSSLContextWithKeys(publicKeyCertificateString, privateKeyString, new IotHubCertificateManager());
    }

    /**
     * Creates an SSLContext from a public key certificate, private key, and certificate manager.
     *
     * @param publicKeyCertificateString The PEM formatted public key certificate string
     * @param privateKeyString The PEM formatted private key string
     * @throws KeyManagementException If the SSLContext could not be initialized
     * @throws IOException If an IO exception occurs
     * @throws CertificateException If a certificate cannot be loaded
     * @throws KeyStoreException If the provided certificates cannot be loaded into the JVM keystore
     * @throws UnrecoverableKeyException if accessing the passphrase protected keystore fails due to the key
     * @throws NoSuchAlgorithmException if the default SSLContext cannot be generated
     */
    private void generateSSLContextWithKeys(String publicKeyCertificateString, String privateKeyString, IotHubCertificateManager certificateManager)
            throws KeyManagementException, IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException
    {
        Security.addProvider(new BouncyCastleProvider());

        PEMReader privateKeyReader = new PEMReader(new StringReader(privateKeyString));
        PEMReader publicKeyReader = new PEMReader(new StringReader(publicKeyCertificateString));

        Object possiblePrivateKey = privateKeyReader.readObject();
        if (!(possiblePrivateKey instanceof KeyPair))
        {
            throw new CertificateException("Unexpected format for private key");
        }
        KeyPair certPairWithPrivate = (KeyPair) possiblePrivateKey;

        Object possiblePublicKey = publicKeyReader.readObject();
        if (!(possiblePublicKey instanceof X509Certificate))
        {
            throw new CertificateException("Unexpected format for public key certificate");
        }
        X509Certificate certPairWithPublic = (X509Certificate) possiblePublicKey;


        //Codes_SRS_IOTHUBSSLCONTEXT_34_018: [This constructor shall generate a temporary password to protect the created keystore holding the private key.]
        char[] temporaryPassword = generateTemporaryPassword();

        //Codes_SRS_IOTHUBSSLCONTEXT_34_020: [The constructor shall create a keystore containing the public key certificate and the private key.]
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null);
        keystore.setCertificateEntry(CERTIFICATE_ALIAS, certPairWithPublic);
        keystore.setKeyEntry(PRIVATE_KEY_ALIAS, certPairWithPrivate.getPrivate(), temporaryPassword, new Certificate[] {certPairWithPublic});

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, temporaryPassword);

        //Codes_SRS_IOTHUBSSLCONTEXT_34_021: [The constructor shall initialize a default trust manager factory that accepts communications from Iot Hub.]
        TrustManagerFactory trustManagerFactory = generateTrustManagerFactory(certificateManager, keystore);

        //Codes_SRS_IOTHUBSSLCONTEXT_34_019: [The constructor shall create default SSL context for TLSv1.2.]
        this.sslContext = SSLContext.getInstance(SSL_CONTEXT_INSTANCE);

        //Codes_SRS_IOTHUBSSLCONTEXT_34_024: [The constructor shall initialize SSL context with its initialized keystore, its initialized TrustManagerFactory and a new secure random.]
        this.sslContext.init(kmf.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
    }

    /**
     * Generates the default SSL Context and saves it to this object's SSLContext object
     *
     * @throws KeyStoreException If the provided certificateManager's certificates cannot be loaded into the trust manager used in creating the SSLContext
     * @throws IOException If a valid certificate could not be retrieved from the provided certificateManager
     * @throws CertificateException If the provided certificateManager cannot retrieve any certificates for any of a variety of reasons
     * @throws KeyManagementException If the generated SSLContext cannot be initialized given the provided certificateManager's certificates
     * @throws NoSuchAlgorithmException if default ssl context cannot be created or the trust manager cannot be created
     */
    private void generateDefaultSSLContext(IotHubCertificateManager certificateManager)
            throws KeyStoreException, IOException, CertificateException, KeyManagementException, NoSuchAlgorithmException
    {
        //Codes_SRS_IOTHUBSSLCONTEXT_25_002: [The constructor shall create default SSL context for TLSv1.2.]
        this.sslContext = SSLContext.getInstance(SSL_CONTEXT_INSTANCE);

        //Codes_SRS_IOTHUBSSLCONTEXT_25_003: [The constructor shall create default TrustManagerFactory with the default algorithm.]
        //Codes_SRS_IOTHUBSSLCONTEXT_25_004: [The constructor shall create default KeyStore instance with the default type and initialize it.]
        //Codes_SRS_IOTHUBSSLCONTEXT_25_005: [The constructor shall set the above created certificateManager into a keystore.]
        //Codes_SRS_IOTHUBSSLCONTEXT_25_006: [The constructor shall initialize TrustManagerFactory with the above initialized keystore.]
        //Codes_SRS_IOTHUBSSLCONTEXT_25_007: [The constructor shall initialize SSL context with the above initialized TrustManagerFactory and a new secure random.]
        TrustManagerFactory trustManagerFactory = generateTrustManagerFactory(certificateManager, null);

        this.sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
    }

    /**
     * Generate a trust key store that has the public key needed to trust all messages from Iot Hub
     * @param certificateManager the certificate manager to build the trust manager factory from
     * @param trustKeyStore the trust key store to load. If this is null, a default trust key store shall be generated
     * @return The default trust manager factory
     * @throws NoSuchAlgorithmException if the default sslcontext cannot be created
     * @throws KeyStoreException if the created key store cannot be created
     * @throws IOException If a valid certificate could not be defined.
     * @throws CertificateException If a certificate cannot be created by a certificate factory
     */
    private TrustManagerFactory generateTrustManagerFactory(IotHubCertificateManager certificateManager, KeyStore trustKeyStore)
            throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException
    {
        if (trustKeyStore == null)
        {
            trustKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustKeyStore.load(null);
        }

        for (Certificate c : certificateManager.getCertificateCollection())
        {
            trustKeyStore.setCertificateEntry(TRUSTED_IOT_HUB_CERT_PREFIX + UUID.randomUUID(), c);
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustKeyStore);

        return trustManagerFactory;
    }

    /**
     * Getter for the IotHubSSLContext
     * @return SSLContext defined for the IotHub.
     */
    SSLContext getSSLContext()
    {
        //Codes_SRS_IOTHUBSSLCONTEXT_25_017: [*This method shall return the value of sslContext.**]**
        return this.sslContext;
    }

    private char[] generateTemporaryPassword()
    {
        return UUID.randomUUID().toString().toCharArray();
    }
}