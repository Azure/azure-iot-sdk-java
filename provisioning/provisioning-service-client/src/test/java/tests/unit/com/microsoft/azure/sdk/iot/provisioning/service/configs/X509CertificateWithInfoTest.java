// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.microsoft.azure.sdk.iot.provisioning.service.configs.X509CertificateInfo;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.X509CertificateWithInfo;
import mockit.Deencapsulation;
import mockit.Mocked;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service X509 certificates
 * 100% methods, 100% lines covered
 */
public class X509CertificateWithInfoTest
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

    /* SRS_X509_CERTIFICATE_WITH_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided certificate is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnCertificateNull()
    {
        // arrange
        // act
        Deencapsulation.newInstance(X509CertificateWithInfo.class, new Class[] {String.class},(String)null);
        // assert
    }

    /* SRS_X509_CERTIFICATE_WITH_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided certificate is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnCertificateEmpty()
    {
        // arrange
        // act
        Deencapsulation.newInstance(X509CertificateWithInfo.class, new Class[] {String.class},"");
        // assert
    }

    /* SRS_X509_CERTIFICATE_WITH_INFO_21_002: [The constructor shall store the provided certificate and set info as null.] */
    @Test
    public void constructorStoresCertificate()
    {
        // arrange
        // act
        X509CertificateWithInfo x509CertificateWithInfo = Deencapsulation.newInstance(X509CertificateWithInfo.class, new Class[] {String.class}, PUBLIC_CERTIFICATE_STRING);

        // assert
        assertEquals(PUBLIC_CERTIFICATE_STRING, Deencapsulation.getField(x509CertificateWithInfo, "certificate"));
        assertNull(Deencapsulation.getField(x509CertificateWithInfo, "info"));
    }

    /* SRS_X509_CERTIFICATE_WITH_INFO_21_003: [The constructor shall throw IllegalArgumentException if the provided x509CertificateWithInfo is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopyThrowsOnNull()
    {
        // arrange
        // act
        new X509CertificateWithInfo(null);
        // assert
    }

    /* SRS_X509_CERTIFICATE_WITH_INFO_21_004: [The constructor shall copy the certificate form the provided x509CertificateWithInfo.] */
    @Test
    public void constructorCopyCertificate()
    {
        // arrange
        X509CertificateWithInfo x509CertificateWithInfo = Deencapsulation.newInstance(X509CertificateWithInfo.class, new Class[] {String.class}, PUBLIC_CERTIFICATE_STRING);

        // act
        X509CertificateWithInfo x509CertificateWithInfoCopy = new X509CertificateWithInfo(x509CertificateWithInfo);

        // assert
        assertEquals(PUBLIC_CERTIFICATE_STRING, Deencapsulation.getField(x509CertificateWithInfoCopy, "certificate"));
        assertNull(Deencapsulation.getField(x509CertificateWithInfoCopy, "info"));
    }

    /* SRS_X509_CERTIFICATE_WITH_INFO_21_004: [The constructor shall copy the certificate form the provided x509CertificateWithInfo.] */
    /* SRS_X509_CERTIFICATE_WITH_INFO_21_005: [If the provide x509CertificateWithInfo contains `info`, the constructor shall create a new instance of the X509CertificateInfo with the provided `info`.] */
    @Test
    public void constructorCopyCertificateAndInfo(
            @Mocked final X509CertificateInfo mockedX509CertificateInfo)
            throws IllegalArgumentException
    {
        // arrange
        X509CertificateWithInfo x509CertificateWithInfo = Deencapsulation.newInstance(X509CertificateWithInfo.class, new Class[] {String.class}, PUBLIC_CERTIFICATE_STRING);
        Deencapsulation.setField(x509CertificateWithInfo, "info", mockedX509CertificateInfo);

        // act
        X509CertificateWithInfo x509CertificateWithInfoCopy = new X509CertificateWithInfo(x509CertificateWithInfo);

        // assert
        assertEquals(PUBLIC_CERTIFICATE_STRING, Deencapsulation.getField(x509CertificateWithInfoCopy, "certificate"));
        assertNotNull(Deencapsulation.getField(x509CertificateWithInfoCopy, "info"));
    }

    /* SRS_X509_CERTIFICATE_WITH_INFO_21_005: [If the provide x509CertificateWithInfo contains `info`, the constructor shall create a new instance of the X509CertificateInfo with the provided `info`.] */
    @Test
    public void constructorCopyInfo(
            @Mocked final X509CertificateInfo mockedX509CertificateInfo)
            throws IllegalArgumentException
    {
        // arrange
        X509CertificateWithInfo x509CertificateWithInfo = Deencapsulation.newInstance(X509CertificateWithInfo.class);
        Deencapsulation.setField(x509CertificateWithInfo, "info", mockedX509CertificateInfo);

        // act
        X509CertificateWithInfo x509CertificateWithInfoCopy = new X509CertificateWithInfo(x509CertificateWithInfo);

        // assert
        assertNull(Deencapsulation.getField(x509CertificateWithInfoCopy, "certificate"));
        assertNotNull(Deencapsulation.getField(x509CertificateWithInfoCopy, "info"));
    }

    /* SRS_X509_CERTIFICATE_WITH_INFO_21_006: [The getCertificate shall return the stored certificate.] */
    /* SRS_X509_CERTIFICATE_WITH_INFO_21_007: [The getInfo shall return the stored info.] */
    @Test
    public void gettersSucceed(
            @Mocked final X509CertificateInfo mockedX509CertificateInfo)
            throws IllegalArgumentException
    {
        // arrange
        X509CertificateWithInfo x509CertificateWithInfo = Deencapsulation.newInstance(X509CertificateWithInfo.class, new Class[] {String.class}, PUBLIC_CERTIFICATE_STRING);
        Deencapsulation.setField(x509CertificateWithInfo, "info", mockedX509CertificateInfo);

        // act
        X509CertificateWithInfo x509CertificateWithInfoCopy = new X509CertificateWithInfo(x509CertificateWithInfo);

        // assert
        assertEquals(PUBLIC_CERTIFICATE_STRING, x509CertificateWithInfoCopy.getCertificate());
        assertNotNull(x509CertificateWithInfoCopy.getInfo());
    }

    /* SRS_X509_CERTIFICATE_WITH_INFO_21_008: [The X509CertificateWithInfo shall provide an empty constructor to make GSON happy.] */
    @Test
    public void emptyConstructorSucceed()
    {
        // arrange
        // act
        X509CertificateWithInfo x509CertificateWithInfo = Deencapsulation.newInstance(X509CertificateWithInfo.class);

        // assert
        assertNotNull(x509CertificateWithInfo);
    }
}
