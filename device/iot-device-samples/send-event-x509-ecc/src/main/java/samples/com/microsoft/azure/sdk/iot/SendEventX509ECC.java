// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.UUID;

public class SendEventX509ECC
{
    static final String DEFAULT_TLS_PROTOCOL = "TLSv1.2";
    private static final String ALIAS_CERT_ALIAS = "cert-alias";
    private static final String PRIVATE_KEY_ALIAS = "key-alias";
    private static final String DEFAULT_CERT_INSTANCE = "X.509";

    // Set VM options to "-Djdk.tls.client.cipherSuites="TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256, TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"
    // in order to force the SDK to only use ECC certificate cipher suites
    public static void main(String[] args) throws IOException, URISyntaxException, GeneralSecurityException, SecurityProviderException, OperatorCreationException, InterruptedException, com.microsoft.azure.sdk.iot.service.exceptions.IotHubException
    {
        if (args.length == 0)
        {
            throw new IllegalArgumentException("Pass in hub level connection string as the only argument");
        }

        String connectionString = args[0];

        // HTTPS - Works as expected
        // MQTT - Works as expected
        // MQTT_WS - Service fails to send "sec-websocket-protocol" websocket response header, but this fails in non ECC cert authenticated connections, too
        // AMQPS - Worker links never open, but this fails in non ECC cert authenticated connections, too
        // AMQPS_WS - Worker links never open, but this fails in non ECC cert authenticated connections, too
        IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        // Call method that returns generatedcert and create a collection of these certs
        final ECCX509Certificate rootCert = ECCX509CertificateGenerator.generateCertificate("rootCert", null);
        final ECCX509Certificate cert1 = ECCX509CertificateGenerator.generateCertificate("cert1", rootCert);
        final ECCX509Certificate cert2 = ECCX509CertificateGenerator.generateCertificate("cert2", cert1);
        final ECCX509Certificate deviceCert = ECCX509CertificateGenerator.generateCertificate("deviceCert", cert2);

        // This is the thumbprint used for device created on portal
        String thumbprint = deviceCert.x509ThumbPrint;

        X509Certificate[] signerCertificates = new X509Certificate[3];
        signerCertificates[0] = rootCert.certificate;
        signerCertificates[1] = cert1.certificate;
        signerCertificates[2] = cert2.certificate;

        // For simplicity, this sample will register the new device to your IoT Hub.
        RegistryManager registryManager = new RegistryManager(connectionString);
        Device device = Device.createDevice(UUID.randomUUID().toString(), AuthenticationType.SELF_SIGNED);
        device.setThumbprintFinal(thumbprint, thumbprint);
        registryManager.addDevice(device);

        // Create DeviceClient with the ECC certificates loaded into its SSLContext
        SSLContext sslContext = buildSSLContext(deviceCert.certificate, deviceCert.privateKey, signerCertificates);
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setSslContext(sslContext);
        DeviceClient client = new DeviceClient(registryManager.getDeviceConnectionString(device), protocol, clientOptions);

        System.out.println("Successfully created an IoT Hub client.");

        client.open();

        client.sendEventAsync(new Message("some message"), new IotHubEventCallback()
        {
            @Override
            public void execute(IotHubStatusCode responseStatus, Object callbackContext)
            {
                System.out.println("IoT Hub responded to the device to cloud message with status " + responseStatus);
            }
        }, null);

        // Wait for message to send
        Thread.sleep(2000);

        client.closeNow();
    }

    private static SSLContext buildSSLContext(X509Certificate leafCertificate, Key leafPrivateKey, X509Certificate[] signerCertificates) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException, SecurityProviderException
    {
        if (leafCertificate == null || leafPrivateKey == null || signerCertificates == null)
        {
            throw new IllegalArgumentException("cert or private key cannot be null");
        }

        // This password is for sample purposes only. Generally a more secure password should be used.
        char[] password = new char[]{'t', 'e', 's', 't', ' ', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
        SSLContext sslContext = SSLContext.getInstance(DEFAULT_TLS_PROTOCOL);

        // Load Trusted certs to keystore and retrieve it.
        KeyStore keyStore = getKeyStoreWithTrustedCerts();

        if (keyStore == null)
        {
            throw new SecurityProviderException("Key store with trusted certs cannot be null");
        }

        // Load Alias cert and private key to key store
        int noOfCerts = signerCertificates.length + 1;
        X509Certificate[] certs = new X509Certificate[noOfCerts];
        int i = 0;
        certs[i++] = leafCertificate;

        // Load the chain of signer cert to keystore
        for (X509Certificate c : signerCertificates)
        {
            certs[i++] = c;
        }

        keyStore.setKeyEntry(PRIVATE_KEY_ALIAS, leafPrivateKey, password, certs);

        sslContext.init(getDefaultX509KeyManager(keyStore, password), getDefaultX509TrustManager(keyStore), new SecureRandom());
        return sslContext;
    }

    private static KeyStore getKeyStoreWithTrustedCerts() throws NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException
    {
        // create keystore
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);

        CertificateFactory certFactory = CertificateFactory.getInstance(DEFAULT_CERT_INSTANCE);
        Collection<? extends Certificate> trustedCert;
        try (InputStream certStreamArray = new ByteArrayInputStream(DEFAULT_CERT.getBytes()))
        {
            trustedCert =  certFactory.generateCertificates(certStreamArray);
        }

        for (java.security.cert.Certificate c : trustedCert)
        {
            keyStore.setCertificateEntry(ALIAS_CERT_ALIAS + UUID.randomUUID(), c);
        }

        return keyStore;
    }

    private static TrustManager[] getDefaultX509TrustManager(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException
    {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return trustManagerFactory.getTrustManagers();
    }

    private static KeyManager[] getDefaultX509KeyManager(KeyStore keyStore, char[] password) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException
    {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        return keyManagerFactory.getKeyManagers();
    }

    // The trusted certs for IoT Hub, including the "DigiCert Global CA G3" certificate that is required to trust for ECC
    // certificate authenticated connections
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
            /* DigiCert Global CA G3 */
            "-----BEGIN CERTIFICATE-----\r\n" +
            "MIIDAzCCAomgAwIBAgIQBYjADIzAxXdmfmTLqIurSTAKBggqhkjOPQQDAzBhMQsw\r\n" +
            "CQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3d3cu\r\n" +
            "ZGlnaWNlcnQuY29tMSAwHgYDVQQDExdEaWdpQ2VydCBHbG9iYWwgUm9vdCBHMzAe\r\n" +
            "Fw0xMzA4MDExMjAwMDBaFw0yODA4MDExMjAwMDBaMEQxCzAJBgNVBAYTAlVTMRUw\r\n" +
            "EwYDVQQKEwxEaWdpQ2VydCBJbmMxHjAcBgNVBAMTFURpZ2lDZXJ0IEdsb2JhbCBD\r\n" +
            "QSBHMzB2MBAGByqGSM49AgEGBSuBBAAiA2IABGY9nqbRBXwQO+2i5tz0bMPnXF/p\r\n" +
            "aT+G2x/Cfk22Ga8rAP8LlrJaUK8Wguo1B5cq/eQY83hwiTsQgCFz+uSjKbU9TrVt\r\n" +
            "nD+hAsQdmWUlMpzpd1ZwPnCR1+mg4jTsONHa8KOCASEwggEdMBIGA1UdEwEB/wQI\r\n" +
            "MAYBAf8CAQAwDgYDVR0PAQH/BAQDAgGGMDQGCCsGAQUFBwEBBCgwJjAkBggrBgEF\r\n" +
            "BQcwAYYYaHR0cDovL29jc3AuZGlnaWNlcnQuY29tMEIGA1UdHwQ7MDkwN6A1oDOG\r\n" +
            "MWh0dHA6Ly9jcmw0LmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydEdsb2JhbFJvb3RHMy5j\r\n" +
            "cmwwPQYDVR0gBDYwNDAyBgRVHSAAMCowKAYIKwYBBQUHAgEWHGh0dHBzOi8vd3d3\r\n" +
            "LmRpZ2ljZXJ0LmNvbS9DUFMwHQYDVR0OBBYEFBsAey6NpdNhIQxdVjUDP9LNcZgK\r\n" +
            "MB8GA1UdIwQYMBaAFLPbSKT5ocXYrjZBzBFjaWIpvEvGMAoGCCqGSM49BAMDA2gA\r\n" +
            "MGUCMQDuo99HhMCfVcynjSsjBZR9FDFHzMxl/wEZSg2vdjHyiKYBa52Ok9LWR2WG\r\n" +
            "Ss3h4jkCME8wZqBwYpkfJh9ziZrgEkbUmpsLg6E2DLlOyLfeS/znes2wDjQYaTyh\r\n" +
            "EbyxjtCg/w==\r\n" +
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
