// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubCertificateManager;
import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/*
 * Unit tests for IotHubSSLContext
 * Code Coverage:
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubSSLContextTest
{
    @Mocked UUID mockUUID;
    @Mocked SecureRandom mockedSecureRandom;

    @Mocked X509Certificate mockedX509Certificate;
    @Mocked Key mockedPrivateKey;

    @Mocked KeyStore mockedKeyStore;
    @Mocked KeyManagerFactory mockKeyManagerFactory;
    @Mocked KeyManager[] mockKeyManagers;

    @Mocked SSLContext mockedSSLContext;

    @Mocked TrustManagerFactory mockedTrustManagerFactory;
    @Mocked TrustManager[] mockedTrustManager;
    @Mocked IotHubCertificateManager mockedCertificateManager;

    @Mocked byte[] mockedByteArray;
    @Mocked ByteArrayInputStream mockedByteArrayInputStream;
    @Mocked Security mockedSecurity;

    private final static Collection<Certificate> testCollection = new LinkedHashSet<Certificate>();

    private void generateSSLContextExpectations() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        new NonStrictExpectations()
        {
            {
                mockedSSLContext.getInstance(anyString);
                result = mockedSSLContext;
                mockedTrustManagerFactory.getInstance(anyString);
                result = mockedTrustManagerFactory;
                mockedKeyStore.getInstance(anyString);
                result = mockedKeyStore;
                Deencapsulation.invoke(mockedCertificateManager, "getCertificateCollection");
                result = testCollection;
                mockedTrustManagerFactory.getTrustManagers();
                result = mockedTrustManager;
                new SecureRandom();
                result = mockedSecureRandom;
            }
        };
    }

    private void generateSSLContextVerifications() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        new Verifications()
        {
            {
                mockedKeyStore.load(null);
                times = 1;
                mockedKeyStore.setCertificateEntry(anyString, mockedX509Certificate);
                minTimes = 1;
                mockedTrustManagerFactory.init(mockedKeyStore);
                times = 1;
                mockedSSLContext.init(null, mockedTrustManager, mockedSecureRandom);
                times = 1;
            }
        };
    }


    //Tests_SRS_IOTHUBSSLCONTEXT_25_001: [**The constructor shall create a default certificate to be used with IotHub.**]**
    //Tests_SRS_IOTHUBSSLCONTEXT_25_002: [**The constructor shall create default SSL context for TLSv1.2.**]**
    //Tests_SRS_IOTHUBSSLCONTEXT_25_003: [**The constructor shall create default TrustManagerFactory with the default algorithm.**]**
    //Tests_SRS_IOTHUBSSLCONTEXT_25_004: [**The constructor shall create default KeyStore instance with the default type and initialize it.**]**
    //Tests_SRS_IOTHUBSSLCONTEXT_25_005: [**The constructor shall set the above created certificate into a keystore.**]**
    //Tests_SRS_IOTHUBSSLCONTEXT_25_006: [**The constructor shall initialize TrustManagerFactory with the above initialized keystore.**]**
    //Tests_SRS_IOTHUBSSLCONTEXT_25_007: [**The constructor shall initialize SSL context with the above initialized TrustManagerFactory and a new secure random.**]**
    @Test
    public void constructorCreatesSSLContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //arrange
        testCollection.add(mockedX509Certificate);
        generateSSLContextExpectations();

        //act

        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class);

        //assert
        generateSSLContextVerifications();
        assertNotNull(Deencapsulation.invoke(testContext, "getSSLContext"));
        testCollection.remove(mockedX509Certificate);

    }

    //Tests_SRS_IOTHUBSSLCONTEXT_25_017: [*This method shall return the value of sslContext.**]**
    @Test
    public void getterGetsContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //arrange
        testCollection.add(mockedX509Certificate);
        generateSSLContextExpectations();

        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {});

        //act
        SSLContext testSSLContext = Deencapsulation.invoke(testContext, "getSSLContext");

        //assert
        generateSSLContextVerifications();
        assertNotNull(testSSLContext);
        testCollection.remove(mockedX509Certificate);
    }

    //Tests_SRS_IOTHUBSSLCONTEXT_34_025: [If the provided cert is a path, this function shall set the path of the default cert to the provided cert path.]
    @Test
    public void constructorUpdatesDefaultCertPath() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException
    {
        //arrange
        final String defaultCertPath = "somePath";
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubCertificateManager.class);
                result = mockedCertificateManager;
            }
        };

        //act
        Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, boolean.class}, defaultCertPath, true);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedCertificateManager, "setCertificatesPath", new Class[] {String.class}, defaultCertPath);
                times = 1;

            }
        };
    }

    //Tests_SRS_IOTHUBSSLCONTEXT_34_026: [If the provided cert is not a path, this function shall set the default cert to the provided cert.]
    @Test
    public void constructorUpdatesDefaultCert() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException
    {
        //arrange
        final String defaultCert = "someCert";
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubCertificateManager.class);
                result = mockedCertificateManager;
            }
        };

        //act
        Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, boolean.class}, defaultCert, false);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedCertificateManager, "setCertificates", new Class[] {String.class}, defaultCert);
                times = 1;
            }
        };
    }

    //----------- PEM UTILITIES TESTS -----------

    private static final String expectedPrivateKeyString = "some private key string";
    private static final String expectedPublicKeyCertificateString = "some public key certificate string";
    private static final String expectedPublicKeyCertificateString1 =
            "-----BEGIN CERTIFICATE-----\n" +
            "jdafjoadjojaofjajfijafijoiajfdoijafiojo\n" +
            "-----END CERTIFICATE-----\n";

    @Mocked StringReader mockedStringReader;
    @Mocked KeyPair mockedKeyPair;
    @Mocked CertificateFactory mockedCertificateFactory;

    //Tests_SRS_IOTHUBSSLCONTEXT_34_027: [This constructor shall save the provided ssl context.]
    @Test
    public void constructorWithSSLContextSavesSSLContext()
    {
        //act
        IotHubSSLContext iotHubSSLContext = new IotHubSSLContext(mockedSSLContext);

        //assert
        SSLContext savedSSLContext = Deencapsulation.getField(iotHubSSLContext, "sslContext");
        assertEquals(mockedSSLContext, savedSSLContext);
    }

    //Tests_SRS_IOTHUBSSLCONTEXT_34_028: [If the provided sslContext is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullSSLContext()
    {
        //act
        new IotHubSSLContext(null);
    }
}
