// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.IotHubCertificateManager;
import mockit.Deencapsulation;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for IotHubCertificateManager
 * Code Coverage:
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubCertificateManagerTests
{

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_25_001: [**The constructor shall set the valid certificate to be default certificate unless changed by user.**]**
    @Test
    public void constructorSucceeds()
    {
        //act
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);

        //assert
        String testValidCert = Deencapsulation.getField(testCertManager, "validCert");
        assertNotNull(testValidCert);

    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_25_003: [**This method shall attempt to read the contents of the certificate file from the path provided and save it as valid certificate.**]**
    @Test
    public void setValidPathSucceeds() throws IOException
    {
        //arrange
        File testCertFile = null;
        try
        {
            IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
            String validCertString = "-----BEGIN CERTIFICATE-----\r\n" +
                    "SomeRandomCertValue\r\n" +
                    "-----END CERTIFICATE-----\r\n";
            String validCertPath = "TestCertFile.crt";
            testCertFile = new File(validCertPath);
            testCertFile.setWritable(true);
            if (!testCertFile.exists())
            {
                testCertFile.createNewFile();
            }
            try (FileWriter testFileWriter = new FileWriter(testCertFile))
            {
                testFileWriter.write(validCertString);
            }
            testCertFile.setReadOnly();

            //act
            Deencapsulation.invoke(testCertManager, "setValidCertPath", validCertPath);

            //assert

            String actualCert = Deencapsulation.getField(testCertManager, "validCert");
            assertEquals(validCertString, actualCert);
        }
        finally
        {
            if (testCertFile != null)
            {
                testCertFile.delete();
            }
        }
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_25_002: [**This method shall throw IllegalArgumentException if parameter is null.**]**
    @Test (expected = IllegalArgumentException.class)
    public void setValidPathThrowsIfNull() throws IOException
    {
        //arrange
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
        //act
        Deencapsulation.invoke(testCertManager, "setValidCertPath", String.class);
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_25_005: [**This method shall throw FileNotFoundException if it could not be found or does not exist.**]**
    @Test (expected = FileNotFoundException.class)
    public void setValidPathThrowsIfInvalidPath()
    {
        //arrange
        String invalidPath = "/invalid/path";
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
        //act
        Deencapsulation.invoke(testCertManager, "setValidCertPath", invalidPath);
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_25_005: [**This method shall throw FileNotFoundException if it could not be found or does not exist.**]**
    @Test (expected = FileNotFoundException.class)
    public void setValidPathThrowsIfCertIsDir() throws IOException
    {
        //arrange
        File testCertFile = null;
        try
        {
            IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
            String validCertString = "-----BEGIN CERTIFICATE-----\r\n" +
                    "SomeRandomCertValue\r\n" +
                    "-----END CERTIFICATE-----\r\n";
            String validCertPath = "TestCertFile.crt";
            testCertFile = new File(validCertPath);
            testCertFile.setWritable(true);
            if (!testCertFile.exists())
            {
                testCertFile.mkdir();
            }

            //act
            Deencapsulation.invoke(testCertManager, "setValidCertPath", validCertPath);

        }
        finally
        {
            if (testCertFile != null)
            {
                testCertFile.delete();
            }
        }

    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_25_004: [**This method shall throw IllegalArgumentException if certificate contents were empty.**]**
    @Test (expected = IllegalArgumentException.class)
    public void setValidPathThrowsIfCertIsEmpty() throws IOException
    {
        //arrange
        File testCertFile = null;
        try
        {
            IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
            String validCertString = "";
            String validCertPath = "TestCertFile.crt";
            testCertFile = new File(validCertPath);
            testCertFile.setWritable(true);
            if (!testCertFile.exists())
            {
                testCertFile.createNewFile();
            }
            try (FileWriter testFileWriter = new FileWriter(testCertFile))
            {
                testFileWriter.write(validCertString);
            }
            testCertFile.setReadOnly();

            //act
            Deencapsulation.invoke(testCertManager, "setValidCertPath", validCertPath);

            //assert

            String actualCert = Deencapsulation.getField(testCertManager, "validCert");
            assertEquals(validCertString, actualCert);
        }
        finally
        {
            if (testCertFile != null)
            {
                testCertFile.delete();
            }
        }

    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_25_007: [**This method shall throw IllegalArgumentException if parameter is null.**]**
    @Test (expected = IllegalArgumentException.class)
    public void setValidCertThrowsIfCertIsEmpty()
    {
        //arrange
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
        String validCertString = "";

        //act
        Deencapsulation.invoke(testCertManager, "setValidCert", validCertString);
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_25_007: [**This method shall throw IllegalArgumentException if parameter is null.**]**
    @Test (expected = IllegalArgumentException.class)
    public void setValidCertThrowsIfCertIsNull()
    {
        //arrange
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
        String validCertString = null;

        //act
        Deencapsulation.invoke(testCertManager, "setValidCert", validCertString);
    }


    @Test
    public void getCertificateCollectionSucceedsAsDefault()
    {
        //arrange
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);

        //act
        Collection<Certificate> testCerts = Deencapsulation.invoke(testCertManager, "getCertificateCollection");

        //assert
        assertNotNull(testCerts);
        assertEquals(testCerts.size(), 4);
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_25_011: [*This method shall create a collection of all the certificates defined as valid using CertificateFactory instance for "X.509".**]**
    @Test
    public void getCertificateCollectionSucceedsWithValidCert()
    {
        //arrange
        String validCertString = "-----BEGIN CERTIFICATE-----\r\n" +
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
                "-----END CERTIFICATE-----\r\n";
        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);

        //act
        Deencapsulation.invoke(testCertManager, "setValidCert", validCertString);
        Collection<Certificate> testCerts = Deencapsulation.invoke(testCertManager, "getCertificateCollection");

        //assert
        assertNotNull(testCerts);
        assertEquals(testCerts.size(), 1);
    }

    @Test
    public void getCertificateCollectionSucceedsWithValidCertPath() throws IOException
    {
        //arrange
        File testCertFile = null;
        try
        {
            IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);
            String validCertString = "-----BEGIN CERTIFICATE-----\r\n" +
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
                    "-----END CERTIFICATE-----\r\n";
            String validCertPath = "TestCertFile.crt";
            testCertFile = new File(validCertPath);
            testCertFile.setWritable(true);
            if (!testCertFile.exists())
            {
                testCertFile.createNewFile();
            }
            try (FileWriter testFileWriter = new FileWriter(testCertFile))
            {
                testFileWriter.write(validCertString);
            }
            testCertFile.setReadOnly();

            //act
            Deencapsulation.invoke(testCertManager, "setValidCertPath", validCertPath);
            Collection<Certificate> testCerts = Deencapsulation.invoke(testCertManager, "getCertificateCollection");

            //assert
            assertNotNull(testCerts);
            assertEquals(testCerts.size(), 1);
            String actualCert = Deencapsulation.getField(testCertManager, "validCert");
            assertEquals(validCertString, actualCert);
        }
        finally
        {
            if (testCertFile != null)
            {
                testCertFile.delete();
            }
        }
    }

    @Test
    public void getCertificateCollectionSucceedsWithMultipleRootCerts()
    {
        String validCertString =
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

                        "-----BEGIN CERTIFICATE-----\r\n" +
                        "MIIFdjCCA16gAwIBAgIQXmjWEXGUY1BWAGjzPsnFkTANBgkqhkiG9w0BAQUFADBV\r\n" +
                        "MQswCQYDVQQGEwJDTjEaMBgGA1UEChMRV29TaWduIENBIExpbWl0ZWQxKjAoBgNV\r\n" +
                        "BAMTIUNlcnRpZmljYXRpb24gQXV0aG9yaXR5IG9mIFdvU2lnbjAeFw0wOTA4MDgw\r\n" +
                        "MTAwMDFaFw0zOTA4MDgwMTAwMDFaMFUxCzAJBgNVBAYTAkNOMRowGAYDVQQKExFX\r\n" +
                        "b1NpZ24gQ0EgTGltaXRlZDEqMCgGA1UEAxMhQ2VydGlmaWNhdGlvbiBBdXRob3Jp\r\n" +
                        "dHkgb2YgV29TaWduMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAvcqN\r\n" +
                        "rLiRFVaXe2tcesLea9mhsMMQI/qnobLMMfo+2aYpbxY94Gv4uEBf2zmoAHqLoE1U\r\n" +
                        "fcIiePyOCbiohdfMlZdLdNiefvAA5A6JrkkoRBoQmTIPJYhTpA2zDxIIFgsDcScc\r\n" +
                        "f+Hb0v1naMQFXQoOXXDX2JegvFNBmpGN9J42Znp+VsGQX+axaCA2pIwkLCxHC1l2\r\n" +
                        "ZjC1vt7tj/id07sBMOby8w7gLJKA84X5KIq0VC6a7fd2/BVoFutKbOsuEo/Uz/4M\r\n" +
                        "x1wdC34FMr5esAkqQtXJTpCzWQ27en7N1QhatH/YHGkR+ScPewavVIMYe+HdVHpR\r\n" +
                        "aG53/Ma/UkpmRqGyZxq7o093oL5d//xWC0Nyd5DKnvnyOfUNqfTq1+ezEC8wQjch\r\n" +
                        "zDBwyYaYD8xYTYO7feUapTeNtqwylwA6Y3EkHp43xP901DfA4v6IRmAR3Qg/UDar\r\n" +
                        "uHqklWJqbrDKaiFaafPz+x1wOZXzp26mgYmhiMU7ccqjUu6Du/2gd/Tkb+dC221K\r\n" +
                        "mYo0SLwX3OSACCK28jHAPwQ+658geda4BmRkAjHXqc1S+4RFaQkAKtxVi8QGRkvA\r\n" +
                        "Sh0JWzko/amrzgD5LkhLJuYwTKVYyrREgk/nkR4zw7CT/xH8gdLKH3Ep3XZPkiWv\r\n" +
                        "HYG3Dy+MwwbMLyejSuQOmbp8HkUff6oZRZb9/D0CAwEAAaNCMEAwDgYDVR0PAQH/\r\n" +
                        "BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFOFmzw7R8bNLtwYgFP6H\r\n" +
                        "EtX2/vs+MA0GCSqGSIb3DQEBBQUAA4ICAQCoy3JAsnbBfnv8rWTjMnvMPLZdRtP1\r\n" +
                        "LOJwXcgu2AZ9mNELIaCJWSQBnfmvCX0KI4I01fx8cpm5o9dU9OpScA7F9dY74ToJ\r\n" +
                        "MuYhOZO9sxXqT2r09Ys/L3yNWC7F4TmgPsc9SnOeQHrAK2GpZ8nzJLmzbVUsWh2e\r\n" +
                        "JXLOC62qx1ViC777Y7NhRCOjy+EaDveaBk3e1CNOIZZbOVtXHS9dCF4Jef98l7VN\r\n" +
                        "g64N1uajeeAz0JmWAjCnPv/So0M/BVoG6kQC2nz4SNAzqfkHx5Xh9T71XXG68pWp\r\n" +
                        "dIhhWeO/yloTunK0jF02h+mmxTwTv97QRCbut+wucPrXnbes5cVAWubXbHssw1ab\r\n" +
                        "R80LzvobtCHXt2a49CUwi1wNuepnsvRtrtWhnk/Yn+knArAdBtaP4/tIEp9/EaEQ\r\n" +
                        "PkxROpaw0RPxx9gmrjrKkcRpnd8BKWRRb2jaFOwIQZeQjdCygPLPwj2/kWjFgGce\r\n" +
                        "xGATVdVhmVd8upUPYUk6ynW8yQqTP2cOEvIo4jEbwFcW3wh8GcF+Dx+FHgo2fFt+\r\n" +
                        "J7x6v+Db9NpSvd4MVHAxkUOVyLzwPt0JfjBkUO1/AaQzZ01oT74V77D2AhGiGxMl\r\n" +
                        "OtzCWfHjXEa7ZywCRuoeSKbmW9m1vFGikpbbqsY3Iqb+zCB0oy2pLmvLwIIRIbWT\r\n" +
                        "ee5Ehr7XHuQe+w==\r\n" +
                        "-----END CERTIFICATE-----\r\n";

                        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);

        //act
        Deencapsulation.invoke(testCertManager, "setValidCert", validCertString);
        Collection<Certificate> testCerts = Deencapsulation.invoke(testCertManager, "getCertificateCollection");

        //assert
        assertNotNull(testCerts);
        assertEquals(testCerts.size(), 2);
    }

    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_25_006: [*If a user attempted to set the certificate and for somereason could not succeed then this method shall not use default certificate by setting valid certificate as null.**]**
    //Tests_SRS_IOTHUBCERTIFICATEMANAGER_25_009: [*If a user attempted to set the certificate and for somereason could not succeed then this method shall not use default certificate by setting valid certificate as null.**]**
    @Test (expected = IOException.class)
    public void getCertificateCollectionFailsIfSetCertFailed()
    {
        String validCertString = "";

        IotHubCertificateManager testCertManager = Deencapsulation.newInstance(IotHubCertificateManager.class);

        //act
        try
        {
            Deencapsulation.invoke(testCertManager, "setValidCert", validCertString);
        }
        catch (IllegalArgumentException e)
        {
            //Ignoring the exception and moving on is dangerous. This test proves that.
        }
        Collection<Certificate> testCerts = Deencapsulation.invoke(testCertManager, "getCertificateCollection");
    }

}
