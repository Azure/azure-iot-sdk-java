/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class SSLContextBuilder
{
    private static final String SSL_CONTEXT_INSTANCE = "TLSv1.2";

    private static final String CERTIFICATE_ALIAS = "cert-alias";
    private static final String PRIVATE_KEY_ALIAS = "key-alias";

    /**
     * Create an SSLContext instance with the provided public and private keys that also trusts the base iot hub certificates
     * @param publicKeyCertificateString the public key to use for x509 authentication. Does not need to include the default
     *                                   Iot Hub trusted certificate as it will be added automatically
     * @param privateKeyString The private key to use for x509 authentication
     * @return The created SSLContext that uses the provided public key and private key
     * @throws GeneralSecurityException If the certificate creation fails, or if the SSLContext creation using those certificates fails
     * @throws IOException If the certificates cannot be read
     */
    public static SSLContext buildSSLContext(String publicKeyCertificateString, String privateKeyString) throws GeneralSecurityException, IOException
    {
        Key privateKey = parsePrivateKeyString(privateKeyString);
        Certificate[] publicKeyCertificates = parsePublicKeyCertificateString(publicKeyCertificateString);

        char[] temporaryPassword = generateTemporaryPassword();

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null);
        keystore.setCertificateEntry(CERTIFICATE_ALIAS, publicKeyCertificates[0]);
        keystore.setKeyEntry(PRIVATE_KEY_ALIAS, privateKey, temporaryPassword, publicKeyCertificates);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, temporaryPassword);

        TrustManagerFactory trustManagerFactory = generateTrustManagerFactory(keystore);

        SSLContext sslContext = SSLContext.getInstance(SSL_CONTEXT_INSTANCE);

        sslContext.init(kmf.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

        return sslContext;
    }

    /**
     * Build the default SSLContext. Trusts the iot hub base certificates, but can only be used for sas auth
     * @return the default SSLContext
     * @throws NoSuchAlgorithmException If the SSLContext cannot be created because of a missing algorithm
     * @throws KeyManagementException If the SSLContext cannot be initiated
     * @throws CertificateException If certificate creation fails
     * @throws KeyStoreException If the keystore operations fail
     * @throws IOException If the default certificate fails to be read
     */
    public static SSLContext buildSSLContext() throws NoSuchAlgorithmException, KeyManagementException, CertificateException, KeyStoreException, IOException
    {
        //Codes_SRS_IOTHUBSSLCONTEXT_25_002: [The constructor shall create default SSL context for TLSv1.2.]
        SSLContext sslContext = SSLContext.getInstance(SSL_CONTEXT_INSTANCE);

        //Codes_SRS_IOTHUBSSLCONTEXT_25_003: [The constructor shall create default TrustManagerFactory with the default algorithm.]
        //Codes_SRS_IOTHUBSSLCONTEXT_25_004: [The constructor shall create default KeyStore instance with the default type and initialize it.]
        //Codes_SRS_IOTHUBSSLCONTEXT_25_005: [The constructor shall set the above created certificateManager into a keystore.]
        //Codes_SRS_IOTHUBSSLCONTEXT_25_006: [The constructor shall initialize TrustManagerFactory with the above initialized keystore.]
        //Codes_SRS_IOTHUBSSLCONTEXT_25_007: [The constructor shall initialize SSL context with the above initialized TrustManagerFactory and a new secure random.]
        TrustManagerFactory trustManagerFactory = generateTrustManagerFactory(null);

        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

        return sslContext;
    }

    public static RSAPrivateKey parsePrivateKeyString(String privateKeyPEM) throws IOException, GeneralSecurityException
    {
        if (privateKeyPEM == null || privateKeyPEM.isEmpty())
        {
            throw new GeneralSecurityException("Public key certificate cannot be null or empty");
        }

        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
        byte[] encoded = Base64.decodeBase64(privateKeyPEM.getBytes("UTF-8"));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
        return privKey;
    }

    public static X509Certificate[] parsePublicKeyCertificateString(String pemString) throws GeneralSecurityException
    {
        if (pemString == null || pemString.isEmpty())
        {
            throw new GeneralSecurityException("Public key certificate cannot be null or empty");
        }

        InputStream pemInputStream = new ByteArrayInputStream(pemString.getBytes());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection<X509Certificate> collection = new ArrayList<>();
        X509Certificate x509Cert;

        try
        {
            while (pemInputStream.available() > 0)
            {
                x509Cert = (X509Certificate) cf.generateCertificate(pemInputStream);
                collection.add(x509Cert);
            }
        }
        catch (IOException e)
        {
            throw new GeneralSecurityException("Unable to generate x509 certificate from public key certificate string", e);
        }

        return collection.toArray(new X509Certificate[collection.size()]);
    }

    private static char[] generateTemporaryPassword()
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

    private static TrustManagerFactory generateTrustManagerFactory(KeyStore trustKeyStore)
            throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException
    {
        if (trustKeyStore == null)
        {
            trustKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustKeyStore.load(null);
        }

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        try (InputStream inputStream = new ByteArrayInputStream(DEFAULT_CERT.getBytes()))
        {
            Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(inputStream);

            for (Certificate c : certificates)
            {
                trustKeyStore.setCertificateEntry(TRUSTED_IOT_HUB_CERT_PREFIX + UUID.randomUUID(), c);
            }
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustKeyStore);

        return trustManagerFactory;
    }

    private static final String TRUSTED_IOT_HUB_CERT_PREFIX = "trustedIotHubCert-";
    private static final String DEFAULT_CERT =
            /*D-TRUST Root Class 3 CA 2 2009*/
            "-----BEGIN CERTIFICATE-----\r\n" +
                    "MIIEMzCCAxugAwIBAgIDCYPzMA0GCSqGSIb3DQEBCwUAME0xCzAJBgNVBAYTAkRF\r\n" +
                    "MRUwEwYDVQQKDAxELVRydXN0IEdtYkgxJzAlBgNVBAMMHkQtVFJVU1QgUm9vdCBD\r\n" +
                    "bGFzcyAzIENBIDIgMjAwOTAeFw0wOTExMDUwODM1NThaFw0yOTExMDUwODM1NTha\r\n" +
                    "ME0xCzAJBgNVBAYTAkRFMRUwEwYDVQQKDAxELVRydXN0IEdtYkgxJzAlBgNVBAMM\r\n" +
                    "HkQtVFJVU1QgUm9vdCBDbGFzcyAzIENBIDIgMjAwOTCCASIwDQYJKoZIhvcNAQEB\r\n" +
                    "BQADggEPADCCAQoCggEBANOySs96R+91myP6Oi/WUEWJNTrGa9v+2wBoqOADER03\r\n" +
                    "UAifTUpolDWzU9GUY6cgVq/eUXjsKj3zSEhQPgrfRlWLJ23DEE0NkVJD2IfgXU42\r\n" +
                    "tSHKXzlABF9bfsyjxiupQB7ZNoTWSPOSHjRGICTBpFGOShrvUD9pXRl/RcPHAY9R\r\n" +
                    "ySPocq60vFYJfxLLHLGvKZAKyVXMD9O0Gu1HNVpK7ZxzBCHQqr0ME7UAyiZsxGsM\r\n" +
                    "lFqVlNpQmvH/pStmMaTJOKDfHR+4CS7zp+hnUquVH+BGPtikw8paxTGA6Eian5Rp\r\n" +
                    "/hnd2HN8gcqW3o7tszIFZYQ05ub9VxC1X3a/L7AQDcUCAwEAAaOCARowggEWMA8G\r\n" +
                    "A1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFP3aFMSfMN4hvR5COfyrYyNJ4PGEMA4G\r\n" +
                    "A1UdDwEB/wQEAwIBBjCB0wYDVR0fBIHLMIHIMIGAoH6gfIZ6bGRhcDovL2RpcmVj\r\n" +
                    "dG9yeS5kLXRydXN0Lm5ldC9DTj1ELVRSVVNUJTIwUm9vdCUyMENsYXNzJTIwMyUy\r\n" +
                    "MENBJTIwMiUyMDIwMDksTz1ELVRydXN0JTIwR21iSCxDPURFP2NlcnRpZmljYXRl\r\n" +
                    "cmV2b2NhdGlvbmxpc3QwQ6BBoD+GPWh0dHA6Ly93d3cuZC10cnVzdC5uZXQvY3Js\r\n" +
                    "L2QtdHJ1c3Rfcm9vdF9jbGFzc18zX2NhXzJfMjAwOS5jcmwwDQYJKoZIhvcNAQEL\r\n" +
                    "BQADggEBAH+X2zDI36ScfSF6gHDOFBJpiBSVYEQBrLLpME+bUMJm2H6NMLVwMeni\r\n" +
                    "acfzcNsgFYbQDfC+rAF1hM5+n02/t2A7nPPKHeJeaNijnZflQGDSNiH+0LS4F9p0\r\n" +
                    "o3/U37CYAqxva2ssJSRyoWXuJVrl5jLn8t+rSfrzkGkj2wTZ51xY/GXUl77M/C4K\r\n" +
                    "zCUqNQT4YJEVdT1B/yMfGchs64JTBKbkTCJNjYy6zltz7GRUUG3RnFX7acM2w4y8\r\n" +
                    "PIWmawomDeCTmGCufsYkl4phX5GOZpIJhzbNi5stPvZR1FDUWSi9g/LMKHtThm3Y\r\n" +
                    "Johw1+qRzT65ysCQblrGXnRl11z+o+I=\r\n" +
                    "-----END CERTIFICATE-----\r\n" +
                    /*DigiCert Global Root CA*/
                    "-----BEGIN CERTIFICATE-----\r\n" +
                    "MIIDrzCCApegAwIBAgIQCDvgVpBCRrGhdWrJWZHHSjANBgkqhkiG9w0BAQUFADBh\r\n" +
                    "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3\r\n" +
                    "d3cuZGlnaWNlcnQuY29tMSAwHgYDVQQDExdEaWdpQ2VydCBHbG9iYWwgUm9vdCBD\r\n" +
                    "QTAeFw0wNjExMTAwMDAwMDBaFw0zMTExMTAwMDAwMDBaMGExCzAJBgNVBAYTAlVT\r\n" +
                    "MRUwEwYDVQQKEwxEaWdpQ2VydCBJbmMxGTAXBgNVBAsTEHd3dy5kaWdpY2VydC5j\r\n" +
                    "b20xIDAeBgNVBAMTF0RpZ2lDZXJ0IEdsb2JhbCBSb290IENBMIIBIjANBgkqhkiG\r\n" +
                    "9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4jvhEXLeqKTTo1eqUKKPC3eQyaKl7hLOllsB\r\n" +
                    "CSDMAZOnTjC3U/dDxGkAV53ijSLdhwZAAIEJzs4bg7/fzTtxRuLWZscFs3YnFo97\r\n" +
                    "nh6Vfe63SKMI2tavegw5BmV/Sl0fvBf4q77uKNd0f3p4mVmFaG5cIzJLv07A6Fpt\r\n" +
                    "43C/dxC//AH2hdmoRBBYMql1GNXRor5H4idq9Joz+EkIYIvUX7Q6hL+hqkpMfT7P\r\n" +
                    "T19sdl6gSzeRntwi5m3OFBqOasv+zbMUZBfHWymeMr/y7vrTC0LUq7dBMtoM1O/4\r\n" +
                    "gdW7jVg/tRvoSSiicNoxBN33shbyTApOB6jtSj1etX+jkMOvJwIDAQABo2MwYTAO\r\n" +
                    "BgNVHQ8BAf8EBAMCAYYwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUA95QNVbR\r\n" +
                    "TLtm8KPiGxvDl7I90VUwHwYDVR0jBBgwFoAUA95QNVbRTLtm8KPiGxvDl7I90VUw\r\n" +
                    "DQYJKoZIhvcNAQEFBQADggEBAMucN6pIExIK+t1EnE9SsPTfrgT1eXkIoyQY/Esr\r\n" +
                    "hMAtudXH/vTBH1jLuG2cenTnmCmrEbXjcKChzUyImZOMkXDiqw8cvpOp/2PV5Adg\r\n" +
                    "06O/nVsJ8dWO41P0jmP6P6fbtGbfYmbW0W5BjfIttep3Sp+dWOIrWcBAI+0tKIJF\r\n" +
                    "PnlUkiaY4IBIqDfv8NZ5YBberOgOzW6sRBc4L0na4UU+Krk2U886UAb3LujEV0ls\r\n" +
                    "YSEY1QSteDwsOoBrp+uvFRTp2InBuThs4pFsiv9kuXclVzDAGySj4dzp30d8tbQk\r\n" +
                    "CAUw7C29C79Fv1C5qfPrmAESrciIxpg0X40KPMbp1ZWVbd4=\r\n" +
                    "-----END CERTIFICATE-----\r\n" +
                    /* Baltimore */
                    "-----BEGIN CERTIFICATE-----\r\n" +
                    "MIIDdzCCAl+gAwIBAgIEAgAAuTANBgkqhkiG9w0BAQUFADBaMQswCQYDVQQGEwJJ\r\n" +
                    "RTESMBAGA1UEChMJQmFsdGltb3JlMRMwEQYDVQQLEwpDeWJlclRydXN0MSIwIAYD\r\n" +
                    "VQQDExlCYWx0aW1vcmUgQ3liZXJUcnVzdCBSb290MB4XDTAwMDUxMjE4NDYwMFoX\r\n" +
                    "DTI1MDUxMjIzNTkwMFowWjELMAkGA1UEBhMCSUUxEjAQBgNVBAoTCUJhbHRpbW9y\r\n" +
                    "ZTETMBEGA1UECxMKQ3liZXJUcnVzdDEiMCAGA1UEAxMZQmFsdGltb3JlIEN5YmVy\r\n" +
                    "VHJ1c3QgUm9vdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKMEuyKr\r\n" +
                    "mD1X6CZymrV51Cni4eiVgLGw41uOKymaZN+hXe2wCQVt2yguzmKiYv60iNoS6zjr\r\n" +
                    "IZ3AQSsBUnuId9Mcj8e6uYi1agnnc+gRQKfRzMpijS3ljwumUNKoUMMo6vWrJYeK\r\n" +
                    "mpYcqWe4PwzV9/lSEy/CG9VwcPCPwBLKBsua4dnKM3p31vjsufFoREJIE9LAwqSu\r\n" +
                    "XmD+tqYF/LTdB1kC1FkYmGP1pWPgkAx9XbIGevOF6uvUA65ehD5f/xXtabz5OTZy\r\n" +
                    "dc93Uk3zyZAsuT3lySNTPx8kmCFcB5kpvcY67Oduhjprl3RjM71oGDHweI12v/ye\r\n" +
                    "jl0qhqdNkNwnGjkCAwEAAaNFMEMwHQYDVR0OBBYEFOWdWTCCR1jMrPoIVDaGezq1\r\n" +
                    "BE3wMBIGA1UdEwEB/wQIMAYBAf8CAQMwDgYDVR0PAQH/BAQDAgEGMA0GCSqGSIb3\r\n" +
                    "DQEBBQUAA4IBAQCFDF2O5G9RaEIFoN27TyclhAO992T9Ldcw46QQF+vaKSm2eT92\r\n" +
                    "9hkTI7gQCvlYpNRhcL0EYWoSihfVCr3FvDB81ukMJY2GQE/szKN+OMY3EU/t3Wgx\r\n" +
                    "jkzSswF07r51XgdIGn9w/xZchMB5hbgF/X++ZRGjD8ACtPhSNzkE1akxehi/oCr0\r\n" +
                    "Epn3o0WC4zxe9Z2etciefC7IpJ5OCBRLbf1wbWsaY71k5h+3zvDyny67G7fyUIhz\r\n" +
                    "ksLi4xaNmjICq44Y3ekQEe5+NauQrz4wlHrQMz2nZQ/1/I6eYs9HRCwBXbsdtTLS\r\n" +
                    "R9I4LtD+gdwyah617jzV/OeBHRnDJELqYzmp\r\n" +
                    "-----END CERTIFICATE-----\r\n";
}
