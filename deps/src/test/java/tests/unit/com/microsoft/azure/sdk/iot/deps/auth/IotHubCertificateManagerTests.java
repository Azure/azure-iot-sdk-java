// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubCertificateManager;
import mockit.Deencapsulation;
import org.junit.Test;

import java.io.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit tests for IotHubCertificateManager
 * Code Coverage:
 * Methods: 100%
 * Lines: 100%
 */
// Unsure if this is somehthing needed to run locally. Leaving for now.
@SuppressWarnings("CommentedOutCode")
public class IotHubCertificateManagerTests
{
/*
    @Mocked
    Certificate mockedCertificate;

    @Mocked
    CertificateFactory mockedCertificateFactory;

    class MockCertificateType extends Certificate
    {
        public MockCertificateType()
        {
            //do nothing
            super("");
        }

        @Override
        public byte[] getEncoded() throws CertificateEncodingException
        {
            return new byte[0];
        }

        @Override
        public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
        {

        }

        @Override
        public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
        {

        }

        @Override
        public String toString()
        {
            return null;
        }

        @Override
        public PublicKey getPublicKey()
        {
            return null;
        }
    }
*/
    private final String someSingleValidCertificate =
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

    private final String someValidCertificates =
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
                    "-----END CERTIFICATE-----\r\n";

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_001: [**This function shall get the x509 instance of the certificate factory.**]**
    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_002: [**This function shall generate the default certificates.**]**
    @Test
    public void constructorSucceeds() throws CertificateException
    {
        //act
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);

        //assert
        Collection<? extends Certificate> defaultCerts = Deencapsulation.getField(testCertManager, "certificates");
        assertNotNull(defaultCerts);
        assertTrue(defaultCerts.size() > 0);

        CertificateFactory certificateFactory = Deencapsulation.getField(testCertManager, "certificateFactory");
        assertNotNull(certificateFactory);
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_003: [**This function shall return the saved certificates.**]**
    @Test
    public void getCertificatesReturnsSavedCertificates()
    {
        //arrange
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);

        //act
        Collection<? extends Certificate> defaultCerts = Deencapsulation.invoke(testCertManager, "getCertificateCollection");

        //assert
        assertNotNull(defaultCerts);
        assertTrue(defaultCerts.size() > 0);
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_004: [**This function shall read the certificates from the provided
    // path and save them into a certificate collection.**]**
    @Test
    public void setCertificatePathSavesCerts() throws IOException, CertificateException
    {
        //arrange
        File testCertFile = null;
        try
        {
            IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
            String validCertPath = "TestCertFile.crt";
            testCertFile = new File(validCertPath);
            testCertFile.setWritable(true);
            if (!testCertFile.exists())
            {
                testCertFile.createNewFile();
            }
            try (FileWriter testFileWriter = new FileWriter(testCertFile))
            {
                testFileWriter.write(someSingleValidCertificate);
            }
            testCertFile.setReadOnly();

            //act
            Deencapsulation.invoke(testCertManager, "setCertificatesPath", validCertPath);

            //assert

            Collection<? extends Certificate> certificates = Deencapsulation.getField(testCertManager, "certificates");
            assertEquals(1, certificates.size());
        }
        finally
        {
            if (testCertFile != null)
            {
                testCertFile.delete();
            }
        }
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_004: [**This function shall read the certificates from the provided
    // path and save them into a certificate collection.**]**
    @Test
    public void setCertificatePathSavesMultipleCerts() throws IOException, CertificateException
    {
        //arrange
        File testCertFile = null;
        try
        {
            IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
            String validCertPath = "TestCertFile.crt";
            testCertFile = new File(validCertPath);
            testCertFile.setWritable(true);
            if (!testCertFile.exists())
            {
                testCertFile.createNewFile();
            }
            try (FileWriter testFileWriter = new FileWriter(testCertFile))
            {
                testFileWriter.write(someValidCertificates);
            }
            testCertFile.setReadOnly();

            //act
            Deencapsulation.invoke(testCertManager, "setCertificatesPath", validCertPath);

            //assert

            Collection<? extends Certificate> certificates = Deencapsulation.getField(testCertManager, "certificates");
            assertEquals(2, certificates.size());
        }
        finally
        {
            if (testCertFile != null)
            {
                testCertFile.delete();
            }
        }
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_005: [**This function shall read the certificates from the provided
    // string and save them into a certificate collection.**]**
    @Test
    public void setCertificateSavesCerts() throws IOException, CertificateException
    {
        //arrange
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);

        //act
        Deencapsulation.invoke(testCertManager, "setCertificates", someSingleValidCertificate);

        //assert
        Collection<? extends Certificate> certificates = Deencapsulation.getField(testCertManager, "certificates");
        assertEquals(1, certificates.size());
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_005: [**This function shall read the certificates from the provided
    // string and save them into a certificate collection.**]**
    @Test
    public void setCertificateSavesMultipleCerts() throws IOException, CertificateException
    {
        //arrange
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);

        //act
        Deencapsulation.invoke(testCertManager, "setCertificates", someValidCertificates);

        //assert
        Collection<? extends Certificate> certificates = Deencapsulation.getField(testCertManager, "certificates");
        assertEquals(2, certificates.size());
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_006: [**This method shall throw IllegalArgumentException if parameter is null or empty.**]**
    @Test (expected = IllegalArgumentException.class)
    public void setValidCertThrowsIfCertIsEmpty()
    {
        //arrange
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
        String invalidCertString = "";

        //act
        Deencapsulation.invoke(testCertManager, "setCertificates", invalidCertString);
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_006: [**This method shall throw IllegalArgumentException if parameter is null or empty.**]**
    @Test (expected = IllegalArgumentException.class)
    public void setValidCertThrowsIfCertIsNull()
    {
        //arrange
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
        String invalidCertString = null;

        //act
        Deencapsulation.invoke(testCertManager, "setCertificates", invalidCertString);
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_007: [**This method shall throw IllegalArgumentException if the cert path is null or empty.**]**
    @Test (expected = IllegalArgumentException.class)
    public void setValidPathThrowsIfNull() throws IOException
    {
        //arrange
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
        String invalidCertPath = "";

        //act
        Deencapsulation.invoke(testCertManager, "setCertificatesPath", invalidCertPath);
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_007: [**This method shall throw IllegalArgumentException if the cert path is null or empty.**]**
    @Test (expected = IllegalArgumentException.class)
    public void setValidPathThrowsIfEmpty() throws IOException
    {
        //arrange
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
        String invalidCertPath = null;

        //act
        Deencapsulation.invoke(testCertManager, "setCertificatesPath", invalidCertPath);
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_34_008: [**If no certificates were parsed from the provided certificate file path, this function shall throw an IllegalArgumentException.**]**
    @Test (expected = IllegalArgumentException.class)
    public void setValidPathThrowsIfNoCertificatesParsed() throws IOException
    {
        //arrange
        File testCertFile = null;
        try
        {
            IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
            String validCertPath = "TestCertFile.crt";
            testCertFile = new File(validCertPath);
            testCertFile.setWritable(true);
            if (!testCertFile.exists())
            {
                //create empty file with no certificates
                testCertFile.createNewFile();
            }
            testCertFile.setReadOnly();

            //act
            Deencapsulation.invoke(testCertManager, "setCertificatesPath", validCertPath);
        }
        finally
        {
            if (testCertFile != null)
            {
                testCertFile.delete();
            }
        }
    }
}
