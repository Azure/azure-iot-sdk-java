/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.auth;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    public IotHubSSLContext()
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
    public IotHubSSLContext(SSLContext sslContext)
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
     * @param trustedCert the certificate to be trusted
     * @param isPath if the trustedCert is a path to the trusted cert, or if it is the certificate itself
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
    public IotHubSSLContext(String trustedCert, boolean isPath)
            throws KeyStoreException, KeyManagementException, IOException, CertificateException, NoSuchAlgorithmException
    {
        IotHubCertificateManager defaultCert = new IotHubCertificateManager();

        if (isPath)
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_34_025: [If the provided cert is a path, this function shall set the path of the default cert to the provided cert path.]
            defaultCert.setCertificatesPath(trustedCert);
        }
        else
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_34_026: [If the provided cert is not a path, this function shall set the default cert to the provided cert.]
            defaultCert.setCertificates(trustedCert);
        }

        generateDefaultSSLContext(defaultCert);
    }

    /**
     * Creates a default SSLContext for the IotHub with the specified certificate.
     * @param publicKeyCertificateString the public key for x509 authentication
     * @param privateKeyString the private key for x509 authentication
     * @param cert the trusted certificate
     * @param isPath If the provided cert is a path, or the actual certificate itself
     *
     * @throws KeyStoreException  if no Provider supports a KeyStoreSpi implementation for the specified type or
     *                            if the keystore has not been initialized,
     *                            or the given alias already exists and does not identify an entry containing a trusted certificate,
     *                            or this operation fails for some other reason.
     * @throws KeyManagementException As per https://docs.oracle.com/javase/7/docs/api/java/security/KeyManagementException.html
     * @throws IOException If the certificate provided was null or invalid
     * @throws CertificateException As per https://docs.oracle.com/javase/7/docs/api/java/security/cert/CertificateException.html
     * @throws NoSuchAlgorithmException if the default SSL Context cannot be created
     * @throws UnrecoverableKeyException if accessing the protected keystore fails
     */
    public IotHubSSLContext(String publicKeyCertificateString, String privateKeyString, String cert, boolean isPath)
            throws KeyStoreException, KeyManagementException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException
    {
        IotHubCertificateManager defaultCert = new IotHubCertificateManager();

        if (isPath)
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_34_040: [If the provided cert is a path, this function shall set the path of the default cert to the provided cert path.]
            defaultCert.setCertificatesPath(cert);
        }
        else
        {
            //Codes_SRS_IOTHUBSSLCONTEXT_34_041: [If the provided cert is not a path, this function shall set the default cert to the provided cert.]
            defaultCert.setCertificates(cert);
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
    public IotHubSSLContext(String publicKeyCertificateString, String privateKeyString)
            throws KeyManagementException, IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException
    {
        generateSSLContextWithKeys(publicKeyCertificateString, privateKeyString, new IotHubCertificateManager());
    }

    /**
     * Getter for the IotHubSSLContext
     * @return SSLContext defined for the IotHub.
     */
    public SSLContext getSSLContext()
    {
        //Codes_SRS_IOTHUBSSLCONTEXT_25_017: [*This method shall return the value of sslContext.**]**
        return this.sslContext;
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
        Key privateKey = IotHubSSLContext.parsePrivateKey(privateKeyString);
        Collection<X509Certificate> certChain = IotHubSSLContext.parsePublicKeyCertificate(publicKeyCertificateString);

        X509Certificate[] certs = certChain.toArray(new X509Certificate[certChain.size()]);

        //Codes_SRS_IOTHUBSSLCONTEXT_34_018: [This constructor shall generate a temporary password to protect the created keystore holding the private key.]
        char[] temporaryPassword = generateTemporaryPassword();

        //Codes_SRS_IOTHUBSSLCONTEXT_34_020: [The constructor shall create a keystore containing the public key certificate and the private key.]
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null);
        keystore.setCertificateEntry(CERTIFICATE_ALIAS, certs[0]);
        keystore.setKeyEntry(PRIVATE_KEY_ALIAS, privateKey, temporaryPassword, certs);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, temporaryPassword);

        //wipe password from stack memory after done using it
        Arrays.fill(temporaryPassword, 0, temporaryPassword.length, '0');

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

    private char[] generateTemporaryPassword()
    {
        byte[] randomBytes = new byte[256];
        char[] randomChars = new char[256];
        new SecureRandom().nextBytes(randomBytes);

        for (int i = 0; i < 256; i++)
        {
            randomChars[i] = (char) randomBytes[i];
        }

        return randomChars;
    }

    private static Key parsePrivateKey(String privateKeyString) throws CertificateException
    {
        try
        {
            // Codes_SRS_IOTHUBSSLCONTEXT_34_031: [This function shall return a Private Key instance created by the provided PEM formatted privateKeyString.]
            Security.addProvider(new BouncyCastleProvider());
            PEMParser privateKeyParser = new PEMParser(new StringReader(privateKeyString));
            Object possiblePrivateKey = privateKeyParser.readObject();
            return IotHubSSLContext.getPrivateKey(possiblePrivateKey);
        }
        catch (Exception e)
        {
            // Codes_SRS_IOTHUBSSLCONTEXT_34_032: [If any exception is encountered while attempting to create the private key instance, this function shall throw a CertificateException.]
            throw new CertificateException(e);
        }
    }

    private static Collection<X509Certificate> parsePublicKeyCertificate(String publicKeyCertificateString) throws CertificateException
    {
        try
        {
            Collection<X509Certificate> certChain = new ArrayList<>();

            // Codes_SRS_IOTHUBSSLCONTEXT_34_033: [This function shall return the X509Certificate cert chain specified by the PEM formatted publicKeyCertificateString.]
            Security.addProvider(new BouncyCastleProvider());

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            final PemReader publicKeyCertificateReader = new PemReader(new StringReader(publicKeyCertificateString));

            try
            {
                PemObject possiblePublicKeyCertificate;
                while (((possiblePublicKeyCertificate = publicKeyCertificateReader.readPemObject()) != null))
                {
                    byte[] content = possiblePublicKeyCertificate.getContent();
                    if (content.length > 0)
                    {
                        final ByteArrayInputStream bais = new ByteArrayInputStream(content);

                        while (bais.available() > 0)
                        {
                            final Certificate cert = certFactory.generateCertificate(bais);
                            if (cert instanceof X509Certificate)
                            {
                                certChain.add((X509Certificate) cert);
                            }
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }
            finally
            {
                publicKeyCertificateReader.close();
            }

            return certChain;
        }
        catch (Exception e)
        {
            // Codes_SRS_IOTHUBSSLCONTEXT_34_034: [If any exception is encountered while attempting to create the public key certificate instance, this function shall throw a CertificateException.]
            throw new CertificateException(e);
        }
    }

    private static Key getPrivateKey(Object possiblePrivateKey) throws IOException
    {
        if (possiblePrivateKey instanceof PEMKeyPair)
        {
            return new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) possiblePrivateKey)
                    .getPrivate();
        }
        else if (possiblePrivateKey instanceof PrivateKeyInfo)
        {
            return new JcaPEMKeyConverter().getPrivateKey((PrivateKeyInfo) possiblePrivateKey);
        }
        else
        {
            throw new IOException("Unable to parse private key, type unknown");
        }
    }
}
