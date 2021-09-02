/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm.parser;

import com.microsoft.azure.sdk.iot.device.hsm.parser.TrustBundleResponse;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class TrustBundleResponseTest
{
    final String testCertificates =                     /*D-TRUST Root Class 3 CA 2 2009*/
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
                    "-----END CERTIFICATE-----\r\n";

    final String jsonWithTrustedCertificates = "{\"certificate\":\"" + testCertificates + "\"}";
    final String jsonWithoutTrustedCertificates = "{}";

    //Tests_SRS_TRUSTBUNDLERESPONSE_34_001: [This function shall return the saved certificates string.]
    @Test
    public void constructorSavesCertificates()
    {
        //act
        TrustBundleResponse response = TrustBundleResponse.fromJson(jsonWithTrustedCertificates);

        String certificates = Deencapsulation.getField(response, "certificates");

        //assert
        assertEquals(testCertificates, certificates);
    }

    //Tests_SRS_TRUSTBUNDLERESPONSE_34_002: [If the provided json does not contain any certificates, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfJsonMissingCertificates()
    {
        //act
        TrustBundleResponse response = TrustBundleResponse.fromJson(jsonWithoutTrustedCertificates);
    }

    //Tests_SRS_TRUSTBUNDLERESPONSE_34_003: [This constructor shall create a new TrustBundleResponse from json.]
    @Test
    public void getCertificatesReturnsCertificates()
    {
        //arrange
        TrustBundleResponse response = TrustBundleResponse.fromJson(jsonWithTrustedCertificates);

        //act
        String certificates = response.getCertificates();

        //assert
        assertEquals(testCertificates, certificates);
    }
}
