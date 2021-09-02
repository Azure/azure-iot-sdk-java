// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.microsoft.azure.sdk.iot.provisioning.service.configs.X509CertificateWithInfo;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.X509Certificates;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service X509 certificates
 * 100% methods, 100% lines covered
 */
public class X509CertificatesTest
{
    //PEM encoded representation of the public key certificate
    private static final String PUBLIC_CERTIFICATE_STRING =
            "-----BEGIN CERTIFICATE-----\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "-----END CERTIFICATE-----\n";

    /* SRS_X509_CERTIFICATES_21_001: [The constructor shall throw IllegalArgumentException if the primary certificate is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnPrimaryNull()
    {
        // arrange
        // act
        Deencapsulation.newInstance(X509Certificates.class, new Class[] {String.class, String.class},null, null);
        // assert
    }

    /* SRS_X509_CERTIFICATES_21_002: [The constructor shall create a new instance of the X509CertificateWithInfo using the provided primary certificate, and store is as the primary Certificate.] */
    /* SRS_X509_CERTIFICATES_21_003: [If the secondary certificate is not null or empty, the constructor shall create a new instance of the X509CertificateWithInfo using the provided secondary certificate, and store it as the secondary Certificate.] */
    @Test
    public void constructorStorePrimaryCertSucceed(
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo)
            throws IllegalArgumentException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(X509CertificateWithInfo.class, new Class[] {String.class}, PUBLIC_CERTIFICATE_STRING);
                result = mockedX509CertificateWithInfo;
                times = 1;
            }
        };

        // act
        X509Certificates x509Certificates = Deencapsulation.newInstance(X509Certificates.class, new Class[] {String.class, String.class},PUBLIC_CERTIFICATE_STRING, null);

        // assert
        assertNotNull(Deencapsulation.getField(x509Certificates, "primary"));
        assertNull(Deencapsulation.getField(x509Certificates, "secondary"));
    }

    /* SRS_X509_CERTIFICATES_21_003: [If the secondary certificate is not null or empty, the constructor shall create a new instance of the X509CertificateWithInfo using the provided secondary certificate, and store it as the secondary Certificate.] */
    @Test
    public void constructorStorePrimaryAndSecondaryCertsSucceed(
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo)
            throws IllegalArgumentException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(X509CertificateWithInfo.class, new Class[] {String.class}, PUBLIC_CERTIFICATE_STRING);
                result = mockedX509CertificateWithInfo;
                times = 2;
            }
        };

        // act
        X509Certificates x509Certificates = Deencapsulation.newInstance(X509Certificates.class, new Class[] {String.class, String.class},PUBLIC_CERTIFICATE_STRING, PUBLIC_CERTIFICATE_STRING);

        // assert
        assertNotNull(Deencapsulation.getField(x509Certificates, "primary"));
        assertNotNull(Deencapsulation.getField(x509Certificates, "secondary"));
    }

    /* SRS_X509_CERTIFICATES_21_004: [The constructor shall throw IllegalArgumentException if the provide X509Certificates is null or if its primary certificate is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopyThrowsOnNull()
            throws IllegalArgumentException
    {
        // arrange
        // act
        new X509Certificates(null);

        // assert
    }

    /* SRS_X509_CERTIFICATES_21_004: [The constructor shall throw IllegalArgumentException if the provide X509Certificates is null or if its primary certificate is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopyThrowsOnPrimaryCertNull()
            throws IllegalArgumentException
    {
        // arrange
        X509Certificates x509Certificates = Deencapsulation.newInstance(X509Certificates.class);

        // act
        new X509Certificates(x509Certificates);

        // assert
    }

    /* SRS_X509_CERTIFICATES_21_005: [The constructor shall create a new instance of X509CertificateWithInfo using the primary certificate on the provided x509Certificates.] */
    /* SRS_X509_CERTIFICATES_21_006: [If the secondary certificate is not null, the constructor shall create a new instance of the X509CertificateWithInfo using the provided secondary certificate, and store it as the secondary Certificate.] */
    @Test
    public void constructorCopiesPrimaryCertSucceed(
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo)
            throws IllegalArgumentException
    {
        // arrange
        X509Certificates x509Certificates = Deencapsulation.newInstance(X509Certificates.class, new Class[] {String.class, String.class},PUBLIC_CERTIFICATE_STRING, null);

        // act
        X509Certificates x509CertificatesCopy = new X509Certificates(x509Certificates);

        // assert
        assertNotNull(Deencapsulation.getField(x509CertificatesCopy, "primary"));
        assertNull(Deencapsulation.getField(x509CertificatesCopy, "secondary"));
        new Verifications()
        {
            {
                new X509CertificateWithInfo((X509CertificateWithInfo)any);
                times = 1;
            }
        };
    }

    /* SRS_X509_CERTIFICATES_21_006: [If the secondary certificate is not null, the constructor shall create a new instance of the X509CertificateWithInfo using the provided secondary certificate, and store it as the secondary Certificate.] */
    @Test
    public void constructorCopiesPrimaryAndSecondaryCertsSucceed(
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo)
            throws IllegalArgumentException
    {
        // arrange
        X509Certificates x509Certificates = Deencapsulation.newInstance(X509Certificates.class, new Class[] {String.class, String.class},PUBLIC_CERTIFICATE_STRING, PUBLIC_CERTIFICATE_STRING);

        // act
        X509Certificates x509CertificatesCopy = new X509Certificates(x509Certificates);

        // assert
        assertNotNull(Deencapsulation.getField(x509CertificatesCopy, "primary"));
        assertNotNull(Deencapsulation.getField(x509CertificatesCopy, "secondary"));
        new Verifications()
        {
            {
                new X509CertificateWithInfo((X509CertificateWithInfo)any);
                times = 2;
            }
        };
    }

    /* SRS_X509_CERTIFICATES_21_007: [The getPrimary shall return the stored primary.] */
    /* SRS_X509_CERTIFICATES_21_008: [The getSecondary shall return the stored secondary.] */
    @Test
    public void gettersSucceed(
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo)
            throws IllegalArgumentException
    {
        // arrange
        X509Certificates x509Certificates = Deencapsulation.newInstance(X509Certificates.class, new Class[] {String.class, String.class},PUBLIC_CERTIFICATE_STRING, PUBLIC_CERTIFICATE_STRING);

        // act - assert
        assertNotNull(x509Certificates.getPrimaryFinal());
        assertNotNull(x509Certificates.getSecondaryFinal());
    }

    /* SRS_X509_CERTIFICATES_21_009: [The X509Certificates shall provide an empty constructor to make GSON happy.] */
    @Test
    public void emptyConstructorSucceed()
    {
        // arrange
        // act
        X509Certificates x509Certificates = Deencapsulation.newInstance(X509Certificates.class);

        // assert
        assertNotNull(x509Certificates);
    }
}
