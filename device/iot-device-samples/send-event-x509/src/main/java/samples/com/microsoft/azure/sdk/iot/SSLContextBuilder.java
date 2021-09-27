/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Helper class that demonstrates how to build an SSLContext for x509 authentication from your public and private certificates,
 * or how to build an SSLContext for SAS authentication from the default IoT Hub public certificates
 */
public class SSLContextBuilder
{
    private static final String SSL_CONTEXT_INSTANCE = "TLSv1.2";
    private static final String CERTIFICATE_TYPE = "X.509";
    private static final String PRIVATE_KEY_ALGORITHM = "RSA";

    private static final String CERTIFICATE_ALIAS = "cert-alias";
    private static final String PRIVATE_KEY_ALIAS = "key-alias";

    /**
     * Create an SSLContext instance with the provided public certificate and private key that also trusts the public
     * certificates loaded in your device's trusted root certification authorities certificate store.
     * @param publicKeyCertificateString the public key to use for x509 authentication. Does not need to include the
     *                                   Iot Hub trusted certificate as it will be added automatically as long as it is
     *                                   in your device's trusted root certification authorities certificate store.
     * @param privateKeyString The private key to use for x509 authentication
     * @return The created SSLContext that uses the provided public key and private key
     * @throws GeneralSecurityException If the certificate creation fails, or if the SSLContext creation using those certificates fails.
     * @throws IOException If the certificates cannot be read.
     */
    public static SSLContext buildSSLContext(String publicKeyCertificateString, String privateKeyString) throws GeneralSecurityException, IOException
    {
        Key privateKey = parsePrivateKeyString(privateKeyString);
        Certificate[] publicKeyCertificates = parsePublicCertificateString(publicKeyCertificateString);

        char[] temporaryPassword = generateTemporaryPassword();

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null);
        keystore.setCertificateEntry(CERTIFICATE_ALIAS, publicKeyCertificates[0]);
        keystore.setKeyEntry(PRIVATE_KEY_ALIAS, privateKey, temporaryPassword, publicKeyCertificates);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, temporaryPassword);

        SSLContext sslContext = SSLContext.getInstance(SSL_CONTEXT_INSTANCE);

        // By leaving the TrustManager array null, the SSLContext will trust the certificates stored on your device's
        // trusted root certification authorities certificate store.
        //
        // This must include the Baltimore CyberTrust Root public certificate: https://baltimore-cybertrust-root.chain-demos.digicert.com/info/index.html
        // and eventually it will need to include the DigiCert Global Root G2 public certificate: https://global-root-g2.chain-demos.digicert.com/info/index.html
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

        return sslContext;
    }

    /**
     * Build the default SSLContext. Trusts the certificates stored in your device's trusted root certification
     * authorities certificate store.
     * @return the default SSLContext
     * @throws NoSuchAlgorithmException If the SSLContext cannot be created because of a missing algorithm.
     * @throws KeyManagementException If the SSLContext cannot be initiated.
     */
    public static SSLContext buildSSLContext() throws NoSuchAlgorithmException, KeyManagementException
    {
        SSLContext sslContext = SSLContext.getInstance(SSL_CONTEXT_INSTANCE);

        // By leaving the KeyManager array null, the SSLContext will not present any private keys during the TLS
        // handshake. This means that the connection will need to be authenticated via a SAS token or similar mechanism.
        //
        // By leaving the TrustManager array null, the SSLContext will trust the certificates stored on your device's
        // trusted root certification authorities certificate store.
        //
        // This must include the Baltimore CyberTrust Root public certificate: https://baltimore-cybertrust-root.chain-demos.digicert.com/info/index.html
        // and eventually it will need to include the DigiCert Global Root G2 public certificate: https://global-root-g2.chain-demos.digicert.com/info/index.html
        sslContext.init(null, null, new SecureRandom());

        return sslContext;
    }

    private static RSAPrivateKey parsePrivateKeyString(String privateKeyPEM) throws GeneralSecurityException
    {
        if (privateKeyPEM == null || privateKeyPEM.isEmpty())
        {
            throw new IllegalArgumentException("Public key certificate cannot be null or empty");
        }

        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
        byte[] encoded = Base64.decodeBase64(privateKeyPEM.getBytes(StandardCharsets.UTF_8));
        KeyFactory kf = KeyFactory.getInstance(PRIVATE_KEY_ALGORITHM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) kf.generatePrivate(keySpec);
    }

    private static X509Certificate[] parsePublicCertificateString(String pemString) throws GeneralSecurityException, IOException
    {
        if (pemString == null || pemString.isEmpty())
        {
            throw new IllegalArgumentException("Public key certificate cannot be null or empty");
        }

        try (InputStream pemInputStream = new ByteArrayInputStream(pemString.getBytes(StandardCharsets.UTF_8)))
        {
            CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
            Collection<X509Certificate> collection = new ArrayList<>();
            X509Certificate x509Cert;

            while (pemInputStream.available() > 0)
            {
                x509Cert = (X509Certificate) cf.generateCertificate(pemInputStream);
                collection.add(x509Cert);
            }

            return collection.toArray(new X509Certificate[0]);
        }
    }

    private static char[] generateTemporaryPassword()
    {
        char[] randomChars = new char[256];
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < 256; i++)
        {
            // character will be between 97 and 122 on the ASCII table. This forces it to be a lower case character.
            // that ensures that the password, as a whole, is alphanumeric
            randomChars[i] = (char) (97 + secureRandom.nextInt(26));
        }

        return randomChars;
    }
}
