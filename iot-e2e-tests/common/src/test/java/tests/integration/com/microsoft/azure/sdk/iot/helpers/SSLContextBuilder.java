/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class SSLContextBuilder
{
    private static final String SSL_CONTEXT_INSTANCE = "TLSv1.2";

    private static final String CERTIFICATE_ALIAS = "cert-alias";
    private static final String PRIVATE_KEY_ALIAS = "key-alias";

    /**
     * Create an SSLContext instance with the provided public certificate and private key that also trusts the public
     * certificates loaded in your device's trusted root certification authorities certificate store.
     * @param publicKeyCertificate the public key to use for x509 authentication. Does not need to include the
     *                                   Iot Hub trusted certificate as it will be added automatically as long as it is
     *                                   in your device's trusted root certification authorities certificate store.
     * @param privateKey The private key to use for x509 authentication
     * @return The created SSLContext that uses the provided public key and private key
     * @throws GeneralSecurityException If the certificate creation fails, or if the SSLContext creation using those certificates fails.
     * @throws IOException If the certificates cannot be read.
     */
    public static SSLContext buildSSLContext(X509Certificate publicKeyCertificate, PrivateKey privateKey) throws GeneralSecurityException, IOException
    {
        char[] temporaryPassword = generateTemporaryPassword();

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null);
        keystore.setCertificateEntry(CERTIFICATE_ALIAS, publicKeyCertificate);
        keystore.setKeyEntry(PRIVATE_KEY_ALIAS, privateKey, temporaryPassword, new Certificate[] {publicKeyCertificate});

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
