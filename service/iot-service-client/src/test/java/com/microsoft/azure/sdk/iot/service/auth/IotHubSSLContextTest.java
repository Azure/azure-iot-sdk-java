// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.auth;

import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class IotHubSSLContextTest
{
    @Mocked
    SSLContext mockSSLContext;

    @Mocked
    CertificateFactory mockCertificateFactory;

    @Mocked
    KeyStore mockKeyStore;

    @Mocked
    X509Certificate mockCertificate;

    @Mocked
    TrustManagerFactory mockTrustManagerFactory;

    private String DIGICERT_GLOBAL_ROOT_G2_PUBLIC_CERTIFICATE =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIDjjCCAnagAwIBAgIQAzrx5qcRqaC7KGSxHQn65TANBgkqhkiG9w0BAQsFADBh\n" +
        "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3\n" +
        "d3cuZGlnaWNlcnQuY29tMSAwHgYDVQQDExdEaWdpQ2VydCBHbG9iYWwgUm9vdCBH\n" +
        "MjAeFw0xMzA4MDExMjAwMDBaFw0zODAxMTUxMjAwMDBaMGExCzAJBgNVBAYTAlVT\n" +
        "MRUwEwYDVQQKEwxEaWdpQ2VydCBJbmMxGTAXBgNVBAsTEHd3dy5kaWdpY2VydC5j\n" +
        "b20xIDAeBgNVBAMTF0RpZ2lDZXJ0IEdsb2JhbCBSb290IEcyMIIBIjANBgkqhkiG\n" +
        "9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuzfNNNx7a8myaJCtSnX/RrohCgiN9RlUyfuI\n" +
        "2/Ou8jqJkTx65qsGGmvPrC3oXgkkRLpimn7Wo6h+4FR1IAWsULecYxpsMNzaHxmx\n" +
        "1x7e/dfgy5SDN67sH0NO3Xss0r0upS/kqbitOtSZpLYl6ZtrAGCSYP9PIUkY92eQ\n" +
        "q2EGnI/yuum06ZIya7XzV+hdG82MHauVBJVJ8zUtluNJbd134/tJS7SsVQepj5Wz\n" +
        "tCO7TG1F8PapspUwtP1MVYwnSlcUfIKdzXOS0xZKBgyMUNGPHgm+F6HmIcr9g+UQ\n" +
        "vIOlCsRnKPZzFBQ9RnbDhxSJITRNrw9FDKZJobq7nMWxM4MphQIDAQABo0IwQDAP\n" +
        "BgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBhjAdBgNVHQ4EFgQUTiJUIBiV\n" +
        "5uNu5g/6+rkS7QYXjzkwDQYJKoZIhvcNAQELBQADggEBAGBnKJRvDkhj6zHd6mcY\n" +
        "1Yl9PMWLSn/pvtsrF9+wX3N3KjITOYFnQoQj8kVnNeyIv/iPsGEMNKSuIEyExtv4\n" +
        "NeF22d+mQrvHRAiGfzZ0JFrabA0UWTW98kndth/Jsw1HKj2ZL7tcu7XUIOGZX1NG\n" +
        "Fdtom/DzMNU+MeKNhJ7jitralj41E6Vf8PlwUHBHQRFXGU7Aj64GxJUTFy8bJZ91\n" +
        "8rGOmaFvE7FBcf6IKshPECBV1/MUReXgRPTqh5Uykw7+U0b6LJ3/iyK5S9kJRaTe\n" +
        "pLiaWN0bfVKfjllDiIGknibVb63dDcY3fe0Dkhvld1927jyNxF1WW6LZZm6zNTfl\n" +
        "MrY=\n" +
        "-----END CERTIFICATE-----\n";

    @Test
    public void defaultConstructor() throws NoSuchAlgorithmException, KeyManagementException
    {
        new Expectations()
        {
            {
                SSLContext.getInstance("TLSv1.2");
                result = mockSSLContext;

                mockSSLContext.init(null, null, (SecureRandom) any);
            }
        };

        new IotHubSSLContext();
    }

    @Test
    public void constructorWithSSLContext() throws NoSuchAlgorithmException
    {
        new Expectations()
        {
            {
                SSLContext.getInstance("TLSv1.2");
                times = 0;
            }
        };

        IotHubSSLContext testIotHubSSLContext = new IotHubSSLContext(mockSSLContext);

        assertEquals(mockSSLContext, testIotHubSSLContext.getSSLContext());
    }

    @Test
    public void getCertificateFromString(@Mocked final ByteArrayInputStream mockByteArrayInputStream) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        final Collection<Certificate> mockCertificates = new ArrayList<>();
        mockCertificates.add(mockCertificate);

        new Expectations()
        {
            {
                new ByteArrayInputStream(DIGICERT_GLOBAL_ROOT_G2_PUBLIC_CERTIFICATE.getBytes());
                result = mockByteArrayInputStream;

                mockCertificateFactory = CertificateFactory.getInstance("X.509");
                result = mockCertificateFactory;

                mockCertificateFactory.generateCertificates(mockByteArrayInputStream);
                result = mockCertificates;

                KeyStore.getInstance(KeyStore.getDefaultType());
                result = mockKeyStore;

                mockKeyStore.load(null);

                mockKeyStore.setCertificateEntry(anyString, mockCertificate);
                times = 1;

                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                result = mockTrustManagerFactory;

                mockTrustManagerFactory.init(mockKeyStore);
            }
        };

        IotHubSSLContext.getSSLContextFromString(DIGICERT_GLOBAL_ROOT_G2_PUBLIC_CERTIFICATE);
    }

    @Test
    public void getCertificateFromStringMultipleCertificates(@Mocked final ByteArrayInputStream mockByteArrayInputStream) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        final Collection<Certificate> mockCertificates = new ArrayList<>();
        mockCertificates.add(mockCertificate);
        mockCertificates.add(mockCertificate);
        mockCertificates.add(mockCertificate);

        new Expectations()
        {
            {
                new ByteArrayInputStream(DIGICERT_GLOBAL_ROOT_G2_PUBLIC_CERTIFICATE.getBytes());
                result = mockByteArrayInputStream;

                mockCertificateFactory = CertificateFactory.getInstance("X.509");
                result = mockCertificateFactory;

                mockCertificateFactory.generateCertificates(mockByteArrayInputStream);
                result = mockCertificates;

                KeyStore.getInstance(KeyStore.getDefaultType());
                result = mockKeyStore;

                mockKeyStore.load(null);

                mockKeyStore.setCertificateEntry(anyString, mockCertificate);
                times = 3; // set 3 certificate entries since the mockCertificates collection has 3 certificates

                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                result = mockTrustManagerFactory;

                mockTrustManagerFactory.init(mockKeyStore);
            }
        };

        IotHubSSLContext.getSSLContextFromString(DIGICERT_GLOBAL_ROOT_G2_PUBLIC_CERTIFICATE);
    }

    @Test
    public void getCertificateFromStringWithoutMocking() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        assertNotNull(IotHubSSLContext.getSSLContextFromString(DIGICERT_GLOBAL_ROOT_G2_PUBLIC_CERTIFICATE));
    }
}
