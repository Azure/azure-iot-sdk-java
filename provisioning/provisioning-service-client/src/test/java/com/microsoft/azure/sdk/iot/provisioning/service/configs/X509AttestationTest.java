// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
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
    private static final String CA_REFERENCES_STRING = "validCertificateName";

    /* SRS_X509_ATTESTATION_21_001: [The constructor shall throw IllegalArgumentException if `clientCertificates`, `rootCertificates`, and `caReferences` are null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnClientAndRootCertificatesNull()
    {
        // arrange
        X509Certificates clientCertificates = null;
        X509Certificates rootCertificates = null;
        X509CAReferences caReferences = null;

        // act
        Deencapsulation.newInstance(X509Attestation.class,
                new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                clientCertificates, rootCertificates, caReferences);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_002: [The constructor shall throw IllegalArgumentException if more than one certificate type are not null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnClientAndRootCertificatesNotNull(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509CAReferences caReferences = null;

        // act
        Deencapsulation.newInstance(X509Attestation.class,
                new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                mockedX509Certificates, mockedX509Certificates, caReferences);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_002: [The constructor shall throw IllegalArgumentException if more than one certificate type are not null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnRootCertificatesAndCAReferenceNotNull(
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final X509CAReferences mockedX509CAReferences)
    {
        // arrange
        X509Certificates clientCertificates = null;

        // act
        Deencapsulation.newInstance(X509Attestation.class,
                new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                clientCertificates, mockedX509Certificates, mockedX509CAReferences);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_003: [The constructor shall store the provided `clientCertificates`, `rootCertificates`, and `caReferences`.] */
    @Test
    public void constructorStoresClientCertificateSucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Certificates rootCertificates = null;
        X509CAReferences caReferences = null;

        // act
        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        mockedX509Certificates, rootCertificates, caReferences);

        // assert
        assertEquals(mockedX509Certificates, Deencapsulation.getField(x509Attestation, "clientCertificates"));
        assertNull(Deencapsulation.getField(x509Attestation, "rootCertificates"));
        assertNull(Deencapsulation.getField(x509Attestation, "caReferences"));
    }

    /* SRS_X509_ATTESTATION_21_003: [The constructor shall store the provided `clientCertificates`, `rootCertificates`, and `caReferences`.] */
    @Test
    public void constructorStoresRootCertificateSucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Certificates clientCertificates = null;
        X509CAReferences caReferences = null;

        // act
        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        clientCertificates, mockedX509Certificates, caReferences);

        // assert
        assertEquals(mockedX509Certificates, Deencapsulation.getField(x509Attestation, "rootCertificates"));
        assertNull(Deencapsulation.getField(x509Attestation, "clientCertificates"));
        assertNull(Deencapsulation.getField(x509Attestation, "caReferences"));
    }

    /* SRS_X509_ATTESTATION_21_003: [The constructor shall store the provided `clientCertificates`, `rootCertificates`, and `caReferences`.] */
    @Test
    public void constructorStoresCAReferenceSucceed(
            @Mocked final X509CAReferences mockedX509CAReferences)
    {
        // arrange
        X509Certificates clientCertificates = null;
        X509Certificates rootCertificates = null;

        // act
        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        clientCertificates, rootCertificates, mockedX509CAReferences);

        // assert
        assertEquals(mockedX509CAReferences, Deencapsulation.getField(x509Attestation, "caReferences"));
        assertNull(Deencapsulation.getField(x509Attestation, "clientCertificates"));
        assertNull(Deencapsulation.getField(x509Attestation, "rootCertificates"));
    }

    /* SRS_X509_ATTESTATION_21_004: [The constructor shall throw IllegalArgumentException if the provided x509Attestation is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopiesClientCertificateThrowsOnNull()
            throws IllegalArgumentException
    {
        // arrange
        // act
        new X509Attestation(null);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_005: [The constructor shall throw IllegalArgumentException if `clientCertificates`, `rootCertificates`, and `caReferences` are null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopiesClientCertificateThrowsOnAllCertsNull()
    {
        // arrange
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class);

        // act
        new X509Attestation(x509Attestation);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_006: [The constructor shall throw IllegalArgumentException if more than one certificate type are not null.] */
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

    /* SRS_X509_ATTESTATION_21_006: [The constructor shall throw IllegalArgumentException if more than one certificate type are not null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopiesClientCertificateThrowsOnBothRootCertificateAndCAReferenceAreNotNull(
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final X509CAReferences mockedX509CAReferences)
    {
        // arrange
        X509Attestation x509Attestation = Deencapsulation.newInstance(X509Attestation.class);
        Deencapsulation.setField(x509Attestation, "caReferences", mockedX509CAReferences);
        Deencapsulation.setField(x509Attestation, "rootCertificates", mockedX509Certificates);

        // act
        new X509Attestation(x509Attestation);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_007: [The constructor shall copy `clientCertificates`, `rootCertificates`, and `caReferences` from the provided X509Attestation.] */
    @Test
    public void constructorCopiesClientCertificateSucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Certificates rootCertificates = null;
        X509CAReferences caReferences = null;
        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        mockedX509Certificates, rootCertificates, caReferences);

        // act
        X509Attestation x509AttestationCopy = new X509Attestation(x509Attestation);

        // assert
        assertNotNull(Deencapsulation.getField(x509AttestationCopy, "clientCertificates"));
        assertNull(Deencapsulation.getField(x509AttestationCopy, "rootCertificates"));
        assertNull(Deencapsulation.getField(x509AttestationCopy, "caReferences"));
    }

    /* SRS_X509_ATTESTATION_21_007: [The constructor shall copy `clientCertificates`, `rootCertificates`, and `caReferences` from the provided X509Attestation.] */
    @Test
    public void constructorCopiesRootCertificateSucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Certificates clientCertificates = null;
        X509CAReferences caReferences = null;
        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        clientCertificates, mockedX509Certificates, caReferences);

        // act
        X509Attestation x509AttestationCopy = new X509Attestation(x509Attestation);

        // assert
        assertNotNull(Deencapsulation.getField(x509AttestationCopy, "rootCertificates"));
        assertNull(Deencapsulation.getField(x509AttestationCopy, "clientCertificates"));
        assertNull(Deencapsulation.getField(x509AttestationCopy, "caReferences"));
    }

    /* SRS_X509_ATTESTATION_21_007: [The constructor shall copy `clientCertificates`, `rootCertificates`, and `caReferences` from the provided X509Attestation.] */
    @Test
    public void constructorCopiesCAReferencesSucceed(
            @Mocked final X509CAReferences mockedX509CAReferences)
    {
        // arrange
        X509Certificates clientCertificates = null;
        X509Certificates rootCertificates = null;
        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        clientCertificates, rootCertificates, mockedX509CAReferences);

        // act
        X509Attestation x509AttestationCopy = new X509Attestation(x509Attestation);

        // assert
        assertNotNull(Deencapsulation.getField(x509AttestationCopy, "caReferences"));
        assertNull(Deencapsulation.getField(x509AttestationCopy, "rootCertificates"));
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
        assertNull(Deencapsulation.getField(x509Attestation, "caReferences"));
    }

    /* SRS_X509_ATTESTATION_21_009: [The factory shall throw IllegalArgumentException if the primary certificate is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryCreatesX509AttestationForClientCertificateOnlyPrimaryThrowsOnNull()
    {
        // arrange
        // act
        X509Attestation.createFromClientCertificates(null);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_09: [The factory shall throw IllegalArgumentException if the primary certificate is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryCreatesX509AttestationForClientCertificateThrowsOnNull()
    {
        // arrange
        // act
       X509Attestation.createFromClientCertificates(null, null);

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
        assertNull(Deencapsulation.getField(x509Attestation, "caReferences"));
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
        assertNull(Deencapsulation.getField(x509Attestation, "caReferences"));
    }

    /* SRS_X509_ATTESTATION_21_013: [The factory shall throw IllegalArgumentException if the primary certificate is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryCreatesX509AttestationForSigningCertificateThrowsOnNull()
    {
        // arrange
        // act
        X509Attestation.createFromRootCertificates(null, null);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_014: [The factory shall create a new instance of the X509Certificates with the provided primary and secondary certificates.] */
    /* SRS_X509_ATTESTATION_21_015: [The factory shall create a new instance of the X509Attestation with the created X509Certificates as the RootCertificates.] */
    @Test
    public void factoryCreatesX509AttestationForRootCertificatesucceed(
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
        assertNull(Deencapsulation.getField(x509Attestation, "caReferences"));
    }

    /* SRS_X509_ATTESTATION_21_025: [The factory shall create a new instance of the X509Attestation for CA reference receiving only the primary certificate.] */
    @Test
    public void factoryCreatesX509AttestationForCAReferenceOnlyPrimarySucceed(
            @Mocked final X509CAReferences mockedX509CAReferences)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(X509CAReferences.class, new Class[] {String.class, String.class}, CA_REFERENCES_STRING, null);
                result = mockedX509CAReferences;
                times = 1;
            }
        };

        // act
        X509Attestation x509Attestation = X509Attestation.createFromCAReferences(CA_REFERENCES_STRING);

        // assert
        assertNull(Deencapsulation.getField(x509Attestation, "clientCertificates"));
        assertNull(Deencapsulation.getField(x509Attestation, "rootCertificates"));
        assertNotNull(Deencapsulation.getField(x509Attestation, "caReferences"));
    }

    /* SRS_X509_ATTESTATION_21_026: [The factory shall throw IllegalArgumentException if the primary CA reference is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryCreatesX509AttestationForCAReferencesThrowsOnNull()
    {
        // arrange
        // act
        X509Attestation.createFromCAReferences(null, null);

        // assert
    }

    /* SRS_X509_ATTESTATION_21_027: [The factory shall create a new instance of the X509CAReferences with the provided primary and secondary CA references.] */
    /* SRS_X509_ATTESTATION_21_028: [The factory shall create a new instance of the X509Attestation with the created X509CAReferences as the caReferences.] */
    @Test
    public void factoryCreatesX509AttestationForCAReferencesSucceed(
            @Mocked final X509CAReferences mockedX509CAReferences)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(X509CAReferences.class, new Class[] {String.class, String.class}, PUBLIC_CERTIFICATE_STRING, PUBLIC_CERTIFICATE_STRING);
                result = mockedX509CAReferences;
                times = 1;
            }
        };

        // act
        X509Attestation x509Attestation = X509Attestation.createFromCAReferences(PUBLIC_CERTIFICATE_STRING, PUBLIC_CERTIFICATE_STRING);

        // assert
        assertNull(Deencapsulation.getField(x509Attestation, "clientCertificates"));
        assertNull(Deencapsulation.getField(x509Attestation, "rootCertificates"));
        assertNotNull(Deencapsulation.getField(x509Attestation, "caReferences"));
    }

    /* SRS_X509_ATTESTATION_21_016: [The getClientCertificates shall return the stored clientCertificates.] */
    @Test
    public void getterForClientCertificateSucceed()
    {
        // arrange
        X509Attestation x509Attestation = X509Attestation.createFromClientCertificates(PUBLIC_CERTIFICATE_STRING, PUBLIC_CERTIFICATE_STRING);

        // act - assert
        assertNotNull(x509Attestation.getClientCertificatesFinal());
    }

    /* SRS_X509_ATTESTATION_21_017: [The getRootCertificates shall return the stored rootCertificates.] */
    @Test
    public void getterForRootCertificateSucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Attestation x509Attestation = X509Attestation.createFromRootCertificates(PUBLIC_CERTIFICATE_STRING, PUBLIC_CERTIFICATE_STRING);

        // act - assert
        assertNotNull(x509Attestation.getRootCertificatesFinal());
    }

    /* SRS_X509_ATTESTATION_21_024: [The getCAReferences shall return the stored caReferences.] */
    @Test
    public void getterForCAReferencesSucceed(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        X509Attestation x509Attestation = X509Attestation.createFromCAReferences(CA_REFERENCES_STRING, CA_REFERENCES_STRING);

        // act - assert
        assertNotNull(x509Attestation.getCAReferencesFinal());
    }


    /* SRS_X509_ATTESTATION_21_018: [If the clientCertificates is not null, the getPrimaryX509CertificateInfo shall return the info in the primary key of the clientCertificates.] */
    @Test
    public void getterForClientCertificatesPrimaryInfoSucceed(
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo,
            @Mocked final X509CertificateInfo mockedX509CertificateInfo)
    {
        // arrange
        final X509Certificates rootCertificates = null;
        final X509CAReferences caReferences = null;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getPrimaryFinal();
                result = mockedX509CertificateWithInfo;
                mockedX509CertificateWithInfo.getInfo();
                result = mockedX509CertificateInfo;
            }
        };

        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        mockedX509Certificates, rootCertificates, caReferences);

        // act - assert
        assertEquals(mockedX509CertificateInfo, x509Attestation.getPrimaryX509CertificateInfo());
    }

    /* SRS_X509_ATTESTATION_21_019: [If the rootCertificates is not null, the getPrimaryX509CertificateInfo shall return the info in the primary key of the rootCertificates.] */
    @Test
    public void getterForRootCertificatesPrimaryInfoSucceed(
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo,
            @Mocked final X509CertificateInfo mockedX509CertificateInfo)
    {
        // arrange
        final X509Certificates clientCertificates = null;
        final X509CAReferences caReferences = null;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getPrimaryFinal();
                result = mockedX509CertificateWithInfo;
                mockedX509CertificateWithInfo.getInfo();
                result = mockedX509CertificateInfo;
            }
        };

        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        clientCertificates, mockedX509Certificates, caReferences);

        // act - assert
        assertEquals(mockedX509CertificateInfo, x509Attestation.getPrimaryX509CertificateInfo());
    }

    /* SRS_X509_ATTESTATION_21_020: [If both clientCertificates and rootCertificates are null, the getPrimaryX509CertificateInfo shall throw IllegalArgumentException.] */
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
        final X509Certificates rootCertificates = null;
        final X509CAReferences caReferences = null;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getSecondaryFinal();
                result = mockedX509CertificateWithInfo;
                mockedX509CertificateWithInfo.getInfo();
                result = mockedX509CertificateInfo;
            }
        };

        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        mockedX509Certificates, rootCertificates, caReferences);

        // act - assert
        assertEquals(mockedX509CertificateInfo, x509Attestation.getSecondaryX509CertificateInfo());
    }

    /* SRS_X509_ATTESTATION_21_021: [If the clientCertificates is not null, and it contains secondary key, the getSecondaryX509CertificateInfo shall return the info in the secondary key of the rootCertificates.] */
    @Test
    public void getterForClientCertificatesSecondaryInfoSucceedOnNullSecondaryCertificate(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        final X509Certificates rootCertificates = null;
        final X509CAReferences caReferences = null;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getSecondaryFinal();
                result = null;
            }
        };

        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        mockedX509Certificates, rootCertificates, caReferences);

        // act - assert
        assertNull(x509Attestation.getSecondaryX509CertificateInfo());
    }

    /* SRS_X509_ATTESTATION_21_022: [If the rootCertificates is not null, and it contains secondary key, the getSecondaryX509CertificateInfo shall return the info in the secondary key of the rootCertificates.] */
    @Test
    public void getterForRootCertificatesSecondaryInfoSucceed(
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final X509CertificateWithInfo mockedX509CertificateWithInfo,
            @Mocked final X509CertificateInfo mockedX509CertificateInfo)
    {
        // arrange
        final X509Certificates clientCertificates = null;
        final X509CAReferences caReferences = null;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getSecondaryFinal();
                result = mockedX509CertificateWithInfo;
                mockedX509CertificateWithInfo.getInfo();
                result = mockedX509CertificateInfo;
            }
        };

        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        clientCertificates, mockedX509Certificates, caReferences);

        // act - assert
        assertEquals(mockedX509CertificateInfo, x509Attestation.getSecondaryX509CertificateInfo());
    }

    /* SRS_X509_ATTESTATION_21_022: [If the rootCertificates is not null, and it contains secondary key, the getSecondaryX509CertificateInfo shall return the info in the secondary key of the rootCertificates.] */
    @Test
    public void getterForRootCertificatesSecondaryInfoSucceedOnNullSecondaryCertificate(
            @Mocked final X509Certificates mockedX509Certificates)
    {
        // arrange
        final X509Certificates clientCertificates = null;
        final X509CAReferences caReferences = null;

        new NonStrictExpectations()
        {
            {
                mockedX509Certificates.getSecondaryFinal();
                result = null;
            }
        };

        X509Attestation x509Attestation =
                Deencapsulation.newInstance(X509Attestation.class,
                        new Class[]{X509Certificates.class, X509Certificates.class, X509CAReferences.class},
                        clientCertificates, mockedX509Certificates, caReferences);

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
