/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security;

import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.UUID;

public abstract class SecurityProvider
{
    static final String DEFAULT_TLS_PROTOCOL = "TLSv1.2";
    private static final String DEFAULT_CERT_INSTANCE = "X.509";
    private static final String TRUSTED_CERT_ALIAS = "TRUSTED_CERT_";
    private static final String DEFAULT_TRUSTED_CERT =
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
            "-----END CERTIFICATE-----\r\n" +
            /* DigiCert Global Root G2 */
            "-----BEGIN CERTIFICATE-----\r\n" +
            "MIIDljCCAn6gAwIBAgIQC5McOtY5Z+pnI7/Dr5r0SzANBgkqhkiG9w0BAQsFADBl\r\n" +
            "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3\r\n" +
            "d3cuZGlnaWNlcnQuY29tMSQwIgYDVQQDExtEaWdpQ2VydCBBc3N1cmVkIElEIFJv\r\n" +
            "b3QgRzIwHhcNMTMwODAxMTIwMDAwWhcNMzgwMTE1MTIwMDAwWjBlMQswCQYDVQQG\r\n" +
            "EwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMRkwFwYDVQQLExB3d3cuZGlnaWNl\r\n" +
            "cnQuY29tMSQwIgYDVQQDExtEaWdpQ2VydCBBc3N1cmVkIElEIFJvb3QgRzIwggEi\r\n" +
            "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZ5ygvUj82ckmIkzTz+GoeMVSA\r\n" +
            "n61UQbVH35ao1K+ALbkKz3X9iaV9JPrjIgwrvJUXCzO/GU1BBpAAvQxNEP4Htecc\r\n" +
            "biJVMWWXvdMX0h5i89vqbFCMP4QMls+3ywPgym2hFEwbid3tALBSfK+RbLE4E9Hp\r\n" +
            "EgjAALAcKxHad3A2m67OeYfcgnDmCXRwVWmvo2ifv922ebPynXApVfSr/5Vh88lA\r\n" +
            "bx3RvpO704gqu52/clpWcTs/1PPRCv4o76Pu2ZmvA9OPYLfykqGxvYmJHzDNw6Yu\r\n" +
            "YjOuFgJ3RFrngQo8p0Quebg/BLxcoIfhG69Rjs3sLPr4/m3wOnyqi+RnlTGNAgMB\r\n" +
            "AAGjQjBAMA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0PAQH/BAQDAgGGMB0GA1UdDgQW\r\n" +
            "BBTOw0q5mVXyuNtgv6l+vVa1lzan1jANBgkqhkiG9w0BAQsFAAOCAQEAyqVVjOPI\r\n" +
            "QW5pJ6d1Ee88hjZv0p3GeDgdaZaikmkuOGybfQTUiaWxMTeKySHMq2zNixya1r9I\r\n" +
            "0jJmwYrA8y8678Dj1JGG0VDjA9tzd29KOVPt3ibHtX2vK0LRdWLjSisCx1BL4Gni\r\n" +
            "lmwORGYQRI+tBev4eaymG+g3NJ1TyWGqolKvSnAWhsI6yLETcDbYz+70CjTVW0z9\r\n" +
            "B5yiutkBclzzTcHdDrEcDcRjvq30FPuJ7KJBDkzMyFdA0G4Dqs0MjomZmWzwPDCv\r\n" +
            "ON9vvKO+KSAnq3T/EyJ43pdSVR6DtVQgA+6uwE9W3jfMw3+qBCe703e4YtsXfJwo\r\n" +
            "IhNzbM8m9Yop5w==\r\n" +
            "-----END CERTIFICATE-----\r\n";

    /**
     * Unique id required for registration
     * @return Returns the registration Id used needed for the service
     * @throws SecurityProviderException If registration id with the underlying implementation could not be retrieved
     */
    abstract public String getRegistrationId() throws SecurityProviderException;

    /**
     * Retrieves the SSL context loaded with trusted certs. In case of X509 SSL context shall be loaded with complete chain
     * all the way till the leaf along with its private key.
     * @return The SSLContext relevant to the flow
     * @throws SecurityProviderException If ssl context could not be generated for any of the reason
     */
    abstract public SSLContext getSSLContext() throws SecurityProviderException;

    KeyStore getKeyStoreWithTrustedCerts() throws NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException
    {
        // create keystore
        //SRS_SecurityClient_25_001: [ This method shall retrieve the default instance of keystore using default algorithm type. ]
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);

        //SRS_SecurityClient_25_002: [ This method shall retrieve the default CertificateFactory instance. ]
        CertificateFactory certFactory = CertificateFactory.getInstance(DEFAULT_CERT_INSTANCE);
        Collection<? extends Certificate> trustedCert;
        try (InputStream certStreamArray = new ByteArrayInputStream(DEFAULT_TRUSTED_CERT.getBytes()))
        {
            trustedCert =  certFactory.generateCertificates(certStreamArray);
        }

        for (Certificate c : trustedCert)
        {
            //SRS_SecurityClient_25_003: [ This method shall load all the trusted certificates to the keystore. ]
            keyStore.setCertificateEntry(TRUSTED_CERT_ALIAS + UUID.randomUUID(), c);
        }

        return keyStore;
    }
}
