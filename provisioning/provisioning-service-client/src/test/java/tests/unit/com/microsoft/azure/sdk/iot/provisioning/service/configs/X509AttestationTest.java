// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.microsoft.azure.sdk.iot.provisioning.service.configs.X509Attestation;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.X509CertificateInfo;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.X509CertificateWithInfo;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.X509Certificates;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service X509 attestation
 * 100% methods, 100% lines covered
 */
public class X509AttestationTest
{
    //PEM encoded representation of the public key certificate
    private static String PUBLIC_CERTIFICATE_STRING =
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

    /* SRS_X509_ATTESTATION_21_001: [The constructor shall throws IllegalArgumentException if both `clientCertificates` and `rootCertificates` are null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnClientAndIntermediateCertificatesChainNull()
    {
        // arrange
        X509Certificates clientCertificates = null;
        X509Certificates rootCertificates = null;

        // act
        Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_002: [The constructor shall throws IllegalArgumentException if both `clientCertificates` and `rootCertificates` are not null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnClientAndIntermediateCertificatesChainNotNull(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Certificates clientCertificates = mockedX509Certificates;
        X509Certificates rootCertificates = mockedX509Certificates;

        // act
        Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_003: [The constructor shall store the provided `clientCertificates` and `rootCertificates`.] */
    @Test
    public void constructorStoresClientCertificateSucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Certificates clientCertificates = mockedX509Certificates;
        X509Certificates rootCertificates = null;

        // act
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // assert
        assertEquals(mockedX509Certificates, Deencapsulation.getField(x509Attestation, "clientCertificates"));
        assertNull(Deencapsulation.getField(x509Attestation, "rootCertificates"));
    }

    /* SRS_X509_ATTESTATION_21_003: [The constructor shall store the provided `clientCertificates` and `rootCertificates`.] */
    @Test
    public void constructorStoresIntermediateCertificatesChainucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Certificates clientCertificates = null;
        X509Certificates rootCertificates = mockedX509Certificates;

        // act
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // assert
        assertEquals(mockedX509Certificates, Deencapsulation.getField(x509Attestation, "rootCertificates"));
        assertNull(Deencapsulation.getField(x509Attestation, "clientCertificates"));
    }

    /* SRS_X509_ATTESTATION_21_004: [The constructor shall throws IllegalArgumentException if the provided x509Attestation is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopiesClientCertificateThrowsOnNull()
            throws IllegalArgumentException
    {
        // arrange
        // act
        new X509Attestation(null);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_005: [The constructor shall throws IllegalArgumentException if both `clientCertificates` and `rootCertificates` are null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopiesClientCertificateThrowsOnBothCertsNull()
    {
        // arrange
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class);

        // act
        new X509Attestation(x509Attestation);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_006: [The constructor shall throws IllegalArgumentException if both `clientCertificates` and `rootCertificates` are not null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopiesClientCertificateThrowsOnBothCertsNotNull(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class);
        Deencapsulation.setField(x509Attestation, "clientCertificates", mockedX509Certificates);
        Deencapsulation.setField(x509Attestation, "rootCertificates", mockedX509Certificates);

        // act
        new X509Attestation(x509Attestation);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_007: [The constructor shall copy `clientCertificates` and `rootCertificates` from the provided X509Attestation.] */
    @Test
    public void constructorCopiesClientCertificateSucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Certificates clientCertificates = mockedX509Certificates;
        X509Certificates rootCertificates = null;
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // act
        X509Attestation x509AttestationCopy = new X509Attestation(x509Attestation);

        // assert
        assertNotNull(Deencapsulation.getField(x509AttestationCopy, "clientCertificates"));
        assertNull(Deencapsulation.getField(x509AttestationCopy, "rootCertificates"));
    }

    /* SRS_X509_ATTESTATION_21_007: [The constructor shall copy `clientCertificates` and `rootCertificates` from the provided X509Attestation.] */
    @Test
    public void constructorCopiesIntermediateCertificatesChainucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Certificates clientCertificates = null;
        X509Certificates rootCertificates = mockedX509Certificates;
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // act
        X509Attestation x509AttestationCopy = new X509Attestation(x509Attestation);

        // assert
        assertNotNull(Deencapsulation.getField(x509AttestationCopy, "rootCertificates"));
        assertNull(Deencapsulation.getField(x509AttestationCopy, "clientCertificates"));
    }

    /* SRS_X509_ATTESTATION_21_008: [The factory shall create a new instance of the X509Attestation for clientCertificates receiving only the primary certificate.] */
    @Test
    public void factoryCreatesX509AttestationForClientCertificateOnlyPrimarySucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(X509Certificates.class, new Class[] {String.class, String.class}, PUBLIC_CERTIFICATE_STRING, null);
                result = mockedX509Certificates;
                times = 1;
            }
        };

        // act
        X509Attestation x509Attestation = X509Attestation.createFromClientCertificates(PUBLIC_CERTIFICATE_STRING);

        // assert
        assertNotNull(Deencapsulation.getField(x509Attestation, "clientCertificates"));
        assertNull(Deencapsulation.getField(x509Attestation, "rootCertificates"));
    }

    /* SRS_X509_ATTESTATION_21_009: [The factory shall throws IllegalArgumentException if the primary certificate is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryCreatesX509AttestationForClientCertificateOnlyPrimaryThrowsOnNull()
    {
        // arrange
        // act
        X509Attestation x509Attestation = X509Attestation.createFromClientCertificates(null);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_09: [The factory shall throws IllegalArgumentException if the primary certificate is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryCreatesX509AttestationForClientCertificateThrowsOnNull()
    {
        // arrange
        // act
        X509Attestation x509Attestation = X509Attestation.createFromClientCertificates(null, null);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_010: [The factory shall create a new instance of the X509Certificates with the provided primary and secondary certificates.] */
    /* SRS_X509_ATTESTATION_21_011: [The factory shall create a new instance of the X509Attestation with the created X509Certificates as the ClientCertificates.] */
    @Test
    public void factoryCreatesX509AttestationForClientCertificateSucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(X509Certificates.class, new Class[] {String.class, String.class}, PUBLIC_CERTIFICATE_STRING, PUBLIC_CERTIFICATE_STRING);
                result = mockedX509Certificates;
                times = 1;
            }
        };

        // act
        X509Attestation x509Attestation = X509Attestation.createFromClientCertificates(PUBLIC_CERTIFICATE_STRING, PUBLIC_CERTIFICATE_STRING);

        // assert
        assertNotNull(Deencapsulation.getField(x509Attestation, "clientCertificates"));
        assertNull(Deencapsulation.getField(x509Attestation, "rootCertificates"));
    }

    /* SRS_X509_ATTESTATION_21_012: [The factory shall create a new instance of the X509Attestation for rootCertificates receiving only the primary certificate.] */
    @Test
    public void factoryCreatesX509AttestationForSigningCertificateOnlyPrimarySucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(X509Certificates.class, new Class[] {String.class, String.class}, PUBLIC_CERTIFICATE_STRING, null);
                result = mockedX509Certificates;
                times = 1;
            }
        };

        // act
        X509Attestation x509Attestation = X509Attestation.createFromRootCertificates(PUBLIC_CERTIFICATE_STRING);

        // assert
        assertNull(Deencapsulation.getField(x509Attestation, "clientCertificates"));
        assertNotNull(Deencapsulation.getField(x509Attestation, "rootCertificates"));
    }

    /* SRS_X509_ATTESTATION_21_013: [The factory shall throws IllegalArgumentException if the primary certificate is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryCreatesX509AttestationForSigningCertificateThrowsOnNull()
    {
        // arrange
        // act
        X509Attestation x509Attestation = X509Attestation.createFromRootCertificates(null, null);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_014: [The factory shall create a new instance of the X509Certificates with the provided primary and secondary certificates.] */
    /* SRS_X509_ATTESTATION_21_015: [The factory shall create a new instance of the X509Attestation with the created X509Certificates as the IntermediateCertificatesChain.] */
    @Test
    public void factoryCreatesX509AttestationForIntermediateCertificatesChainucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(X509Certificates.class, new Class[] {String.class, String.class}, PUBLIC_CERTIFICATE_STRING, PUBLIC_CERTIFICATE_STRING);
                result = mockedX509Certificates;
                times = 1;
            }
        };

        // act
        X509Attestation x509Attestation = X509Attestation.createFromRootCertificates(PUBLIC_CERTIFICATE_STRING, PUBLIC_CERTIFICATE_STRING);

        // assert
        assertNull(Deencapsulation.getField(x509Attestation, "clientCertificates"));
        assertNotNull(Deencapsulation.getField(x509Attestation, "rootCertificates"));
    }

    /* SRS_X509_ATTESTATION_21_016: [The getClientCertificates shall return the stored clientCertificates.] */
    @Test
    public void getterForClientCertificateSucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Certificates clientCertificates = mockedX509Certificates;
        X509Certificates rootCertificates = null;
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // act - assert
        assertEquals(mockedX509Certificates, x509Attestation.getClientCertificates());
    }

    /* SRS_X509_ATTESTATION_21_017: [The getIntermediateCertificatesChain shall return the stored rootCertificates.] */
    @Test
    public void getterForIntermediateCertificatesChainucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Certificates clientCertificates = null;
        X509Certificates rootCertificates = mockedX509Certificates;
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // act - assert
        assertEquals(mockedX509Certificates, x509Attestation.getIntermediateCertificatesChain());
    }

    /* SRS_X509_ATTESTATION_21_018: [If the clientCertificates is not null, the getPrimaryX509CertificateInfo shall return the info in the primary key of the clientCertificates.] */
    @Test
    public void getterForClientCertificatesPrimaryInfoSucceed(
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo,
            @Mocked final X509CertificateInfo mockedX509CertificateInfo)
    {
        // arrange
        final X509Certificates clientCertificates = mockedX509Certificates;
        final X509Certificates rootCertificates = null;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getPrimary();
                result = mockedX509CertificateWithInfo;
                mockedX509CertificateWithInfo.getInfo();
                result = mockedX509CertificateInfo;
            }
        };

        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // act - assert
        assertEquals(mockedX509CertificateInfo, x509Attestation.getPrimaryX509CertificateInfo());
    }

    /* SRS_X509_ATTESTATION_21_019: [If the rootCertificates is not null, the getPrimaryX509CertificateInfo shall return the info in the primary key of the rootCertificates.] */
    @Test
    public void getterForIntermediateCertificatesChainPrimaryInfoSucceed(
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo,
            @Mocked final X509CertificateInfo mockedX509CertificateInfo)
    {
        // arrange
        final X509Certificates clientCertificates = null;
        final X509Certificates rootCertificates = mockedX509Certificates;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getPrimary();
                result = mockedX509CertificateWithInfo;
                mockedX509CertificateWithInfo.getInfo();
                result = mockedX509CertificateInfo;
            }
        };

        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // act - assert
        assertEquals(mockedX509CertificateInfo, x509Attestation.getPrimaryX509CertificateInfo());
    }

    /* SRS_X509_ATTESTATION_21_020: [If both clientCertificates and rootCertificates are null, the getPrimaryX509CertificateInfo shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getterForCertificatesPrimaryInfoThrowsOnNonCertificate()
    {
        // arrange
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class);

        // act
        x509Attestation.getPrimaryX509CertificateInfo();

        // assert
    }

    /* SRS_X509_ATTESTATION_21_021: [If the clientCertificates is not null, and it contains secondary key, the getSecondaryX509CertificateInfo shall return the info in the secondary key of the rootCertificates.] */
    @Test
    public void getterForClientCertificatesSecondaryInfoSucceed(
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo,
            @Mocked final X509CertificateInfo mockedX509CertificateInfo)
    {
        // arrange
        final X509Certificates clientCertificates = mockedX509Certificates;
        final X509Certificates rootCertificates = null;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getSecondary();
                result = mockedX509CertificateWithInfo;
                mockedX509CertificateWithInfo.getInfo();
                result = mockedX509CertificateInfo;
            }
        };

        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // act - assert
        assertEquals(mockedX509CertificateInfo, x509Attestation.getSecondaryX509CertificateInfo());
    }

    /* SRS_X509_ATTESTATION_21_021: [If the clientCertificates is not null, and it contains secondary key, the getSecondaryX509CertificateInfo shall return the info in the secondary key of the rootCertificates.] */
    @Test
    public void getterForClientCertificatesSecondaryInfoSucceedOnNullSecondaryCertificate(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        final X509Certificates clientCertificates = mockedX509Certificates;
        final X509Certificates rootCertificates = null;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getSecondary();
                result = null;
            }
        };

        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // act - assert
        assertNull(x509Attestation.getSecondaryX509CertificateInfo());
    }

    /* SRS_X509_ATTESTATION_21_022: [If the rootCertificates is not null, and it contains secondary key, the getSecondaryX509CertificateInfo shall return the info in the secondary key of the rootCertificates.] */
    @Test
    public void getterForIntermediateCertificatesChainSecondaryInfoSucceed(
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo,
            @Mocked final X509CertificateInfo mockedX509CertificateInfo)
    {
        // arrange
        final X509Certificates clientCertificates = null;
        final X509Certificates rootCertificates = mockedX509Certificates;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getSecondary();
                result = mockedX509CertificateWithInfo;
                mockedX509CertificateWithInfo.getInfo();
                result = mockedX509CertificateInfo;
            }
        };

        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // act - assert
        assertEquals(mockedX509CertificateInfo, x509Attestation.getSecondaryX509CertificateInfo());
    }

    /* SRS_X509_ATTESTATION_21_022: [If the rootCertificates is not null, and it contains secondary key, the getSecondaryX509CertificateInfo shall return the info in the secondary key of the rootCertificates.] */
    @Test
    public void getterForIntermediateCertificatesChainSecondaryInfoSucceedOnNullSecondaryCertificate(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        final X509Certificates clientCertificates = null;
        final X509Certificates rootCertificates = mockedX509Certificates;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getSecondary();
                result = null;
            }
        };

        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class, new Class[]{X509Certificates.class, X509Certificates.class}, clientCertificates, rootCertificates);

        // act - assert
        assertNull(x509Attestation.getSecondaryX509CertificateInfo());
    }

    /* SRS_X509_ATTESTATION_21_023: [The X509Attestation shall provide an empty constructor to make GSON happy.] */
    @Test
    public void emptyConstructorSucceed()
    {
        // arrange
        // act
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class);

        // assert
        assertNotNull(x509Attestation);
    }
}
