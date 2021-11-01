// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.auth;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.UUID;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;

// This test class is a continuation of the unit tests defined in IotHubSSLContextTest, but without any mocked security
// objects. This allows us to actually attempt to parse valid certificates.
public class IotHubSSLContextParsingTest
{
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

    private String DIGICERT_BALTIMORE_CYBERTRUST_ROOT_PUBLIC_CERTIFICATE =
        "-----BEGIN CERTIFICATE-----\n" +
            "MIIDdzCCAl+gAwIBAgIEAgAAuTANBgkqhkiG9w0BAQUFADBaMQswCQYDVQQGEwJJ\n" +
            "RTESMBAGA1UEChMJQmFsdGltb3JlMRMwEQYDVQQLEwpDeWJlclRydXN0MSIwIAYD\n" +
            "VQQDExlCYWx0aW1vcmUgQ3liZXJUcnVzdCBSb290MB4XDTAwMDUxMjE4NDYwMFoX\n" +
            "DTI1MDUxMjIzNTkwMFowWjELMAkGA1UEBhMCSUUxEjAQBgNVBAoTCUJhbHRpbW9y\n" +
            "ZTETMBEGA1UECxMKQ3liZXJUcnVzdDEiMCAGA1UEAxMZQmFsdGltb3JlIEN5YmVy\n" +
            "VHJ1c3QgUm9vdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKMEuyKr\n" +
            "mD1X6CZymrV51Cni4eiVgLGw41uOKymaZN+hXe2wCQVt2yguzmKiYv60iNoS6zjr\n" +
            "IZ3AQSsBUnuId9Mcj8e6uYi1agnnc+gRQKfRzMpijS3ljwumUNKoUMMo6vWrJYeK\n" +
            "mpYcqWe4PwzV9/lSEy/CG9VwcPCPwBLKBsua4dnKM3p31vjsufFoREJIE9LAwqSu\n" +
            "XmD+tqYF/LTdB1kC1FkYmGP1pWPgkAx9XbIGevOF6uvUA65ehD5f/xXtabz5OTZy\n" +
            "dc93Uk3zyZAsuT3lySNTPx8kmCFcB5kpvcY67Oduhjprl3RjM71oGDHweI12v/ye\n" +
            "jl0qhqdNkNwnGjkCAwEAAaNFMEMwHQYDVR0OBBYEFOWdWTCCR1jMrPoIVDaGezq1\n" +
            "BE3wMBIGA1UdEwEB/wQIMAYBAf8CAQMwDgYDVR0PAQH/BAQDAgEGMA0GCSqGSIb3\n" +
            "DQEBBQUAA4IBAQCFDF2O5G9RaEIFoN27TyclhAO992T9Ldcw46QQF+vaKSm2eT92\n" +
            "9hkTI7gQCvlYpNRhcL0EYWoSihfVCr3FvDB81ukMJY2GQE/szKN+OMY3EU/t3Wgx\n" +
            "jkzSswF07r51XgdIGn9w/xZchMB5hbgF/X++ZRGjD8ACtPhSNzkE1akxehi/oCr0\n" +
            "Epn3o0WC4zxe9Z2etciefC7IpJ5OCBRLbf1wbWsaY71k5h+3zvDyny67G7fyUIhz\n" +
            "ksLi4xaNmjICq44Y3ekQEe5+NauQrz4wlHrQMz2nZQ/1/I6eYs9HRCwBXbsdtTLS\n" +
            "R9I4LtD+gdwyah617jzV/OeBHRnDJELqYzmp\n" +
            "-----END CERTIFICATE-----\n";

    @Test
    public void getCertificateFromStringWithoutMocking() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        assertNotNull(IotHubSSLContext.getSSLContextFromString(DIGICERT_GLOBAL_ROOT_G2_PUBLIC_CERTIFICATE));
    }

    @Test
    public void getCertificatesFromStringWithoutMocking() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        assertNotNull(IotHubSSLContext.getSSLContextFromString(DIGICERT_GLOBAL_ROOT_G2_PUBLIC_CERTIFICATE + DIGICERT_BALTIMORE_CYBERTRUST_ROOT_PUBLIC_CERTIFICATE));
    }

    @Test
    public void getCertificateFromFileWithoutMocking() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        // create a file with a valid PEM in it
        File testCertificateFile = new File(UUID.randomUUID() + "-cert.pem");
        try
        {
            try
            {
                testCertificateFile.createNewFile();

                FileWriter testCertificateFileWriter = new FileWriter(testCertificateFile);

                // load 1 certificate into the pem file
                testCertificateFileWriter.write(DIGICERT_GLOBAL_ROOT_G2_PUBLIC_CERTIFICATE);
                testCertificateFileWriter.close();
            }
            catch (IOException e)
            {
                fail("Failed to create a test file to read from " + e.getMessage());
            }

            assertNotNull(IotHubSSLContext.getSSLContextFromFile(testCertificateFile.getAbsolutePath()));
        }
        finally
        {
            //always delete the test file after the test finishes
            testCertificateFile.delete();
        }
    }

    @Test
    public void getCertificatesFromFileWithoutMocking() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        // create a file with a valid PEM in it
        File testCertificateFile = new File(UUID.randomUUID() + "-cert.pem");
        try
        {
            try
            {
                testCertificateFile.createNewFile();

                FileWriter testCertificateFileWriter = new FileWriter(testCertificateFile);

                // load 2 certificates into the pem file
                testCertificateFileWriter.write(DIGICERT_GLOBAL_ROOT_G2_PUBLIC_CERTIFICATE);
                testCertificateFileWriter.write(DIGICERT_BALTIMORE_CYBERTRUST_ROOT_PUBLIC_CERTIFICATE);
                testCertificateFileWriter.close();
            }
            catch (IOException e)
            {
                fail("Failed to create a test file to read from " + e.getMessage());
            }

            assertNotNull(IotHubSSLContext.getSSLContextFromFile(testCertificateFile.getAbsolutePath()));
        }
        finally
        {
            //always delete the test file after the test finishes
            testCertificateFile.delete();
        }
    }
}
