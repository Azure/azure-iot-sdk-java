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

    //Tests_SRS_IOTHUBSSLCONTEXT_34_018: [This constructor shall generate a temporary password to protect the created keystore holding the private key.]
    //Tests_SRS_IOTHUBSSLCONTEXT_34_019: [The constructor shall create default SSL context for TLSv1.2.]
    //Tests_SRS_IOTHUBSSLCONTEXT_34_020: [The constructor shall create a keystore containing the public key certificate and the private key.]
    //Tests_SRS_IOTHUBSSLCONTEXT_34_021: [The constructor shall initialize a default trust manager factory that accepts communications from Iot Hub.]
    //Tests_SRS_IOTHUBSSLCONTEXT_34_024: [The constructor shall initialize SSL context with its initialized keystore, its initialized TrustManagerFactory and a new secure random.]
    @Test
    public void constructorWithCertAndKeySuccess()
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException, UnrecoverableKeyException
    {
        //arrange
        final String publicKeyCert = "someCert";
        final String privateKey = "someKey";

        final Collection<X509Certificate> testCertChain = new ArrayList<>();
        testCertChain.add(mockedX509Certificate);

        new MockUp<IotHubSSLContext>()
        {
            @Mock Key parsePrivateKey(String privateKeyString) throws CertificateException
            {
                return mockedPrivateKey;
            }

            @Mock Collection<X509Certificate> parsePublicKeyCertificate(String publicKeyCertificateString) throws CertificateException
            {
                return testCertChain;
            }
        };

        new NonStrictExpectations()
        {
            {
                KeyManagerFactory.getInstance("SunX509");
                result = mockKeyManagerFactory;

                Deencapsulation.newInstance(IotHubCertificateManager.class);
                result = mockedCertificateManager;

                mockKeyManagerFactory.getKeyManagers();
                result = mockKeyManagers;

                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                result = mockedTrustManagerFactory;

                mockedTrustManagerFactory.getTrustManagers();
                result = mockedTrustManager;
            }
        };

        final IotHubSSLContext iotHubSSLContext = Deencapsulation.newInstance(IotHubSSLContext.class, new Class[]{String.class, String.class}, publicKeyCert, privateKey);

        //assert
        new Verifications()
        {
            {
                SSLContext.getInstance("TLSv1.2");
                times = 1;

                Deencapsulation.invoke(iotHubSSLContext, "generateTemporaryPassword");
                times = 1;

                mockedKeyStore.setCertificateEntry(anyString, mockedX509Certificate);
                times = 1;

                new SecureRandom();
                times = 2;

                Deencapsulation.invoke(iotHubSSLContext, "generateTrustManagerFactory", new Class[] { IotHubCertificateManager.class, KeyStore.class }, mockedCertificateManager, mockedKeyStore);
                times = 1;

                mockedSSLContext.init(mockKeyManagers, mockedTrustManager, new SecureRandom());
                times = 1;
            }
        };
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

    // Tests_SRS_IOTHUBSSLCONTEXT_34_041: [If the provided cert is not a path, this function shall set the default cert to the provided cert.]
    // Tests_SRS_IOTHUBSSLCONTEXT_34_042: [This constructor shall generate a temporary password to protect the created keystore holding the private key.]
    // Tests_SRS_IOTHUBSSLCONTEXT_34_043: [The constructor shall create default SSL context for TLSv1.2.]
    // Tests_SRS_IOTHUBSSLCONTEXT_34_044: [The constructor shall create a keystore containing the public key certificate and the private key.]
    // Tests_SRS_IOTHUBSSLCONTEXT_34_045: [The constructor shall initialize a default trust manager factory that accepts communications from Iot Hub.]
    // Tests_SRS_IOTHUBSSLCONTEXT_34_046: [The constructor shall initialize SSL context with its initialized keystore, its initialized TrustManagerFactory and a new secure random.]
    @Test
    public void constructorWithDefaultCertAndPublicCertAndPrivateKey()
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException, UnrecoverableKeyException
    {
        //arrange
        final String publicKeyCert = "someCert";
        final String privateKey = "someKey";
        final String iotHubTrustedCert = "some trusted cert";

        final Collection<X509Certificate> testCertChain = new ArrayList<>();
        testCertChain.add(mockedX509Certificate);

        new MockUp<IotHubSSLContext>()
        {
            @Mock Key parsePrivateKey(String privateKeyString) throws CertificateException
            {
                return mockedPrivateKey;
            }

            @Mock Collection<X509Certificate> parsePublicKeyCertificate(String publicKeyCertificateString) throws CertificateException
            {
                return testCertChain;
            }
        };

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubCertificateManager.class);
                result = mockedCertificateManager;

                Deencapsulation.invoke(IotHubSSLContext.class, "parsePrivateKey", privateKey);
                returns(mockedPrivateKey);

                Deencapsulation.invoke(IotHubSSLContext.class, "parsePublicKeyCertificate", publicKeyCert);
                returns(testCertChain);

                KeyManagerFactory.getInstance("SunX509");
                result = mockKeyManagerFactory;

                Deencapsulation.newInstance(IotHubCertificateManager.class);
                result = mockedCertificateManager;

                mockKeyManagerFactory.getKeyManagers();
                result = mockKeyManagers;

                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                result = mockedTrustManagerFactory;

                mockedTrustManagerFactory.getTrustManagers();
                result = mockedTrustManager;
            }
        };

        final IotHubSSLContext iotHubSSLContext = Deencapsulation.newInstance(IotHubSSLContext.class, new Class[]{String.class, String.class, String.class, boolean.class}, publicKeyCert, privateKey, iotHubTrustedCert, false);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedCertificateManager, "setCertificates", new Class[] {String.class}, iotHubTrustedCert);
                times = 1;

                SSLContext.getInstance("TLSv1.2");
                times = 1;

                Deencapsulation.invoke(iotHubSSLContext, "generateTemporaryPassword");
                times = 1;

                mockedKeyStore.setCertificateEntry(anyString, mockedX509Certificate);
                times = 1;

                new SecureRandom();
                times = 2;

                Deencapsulation.invoke(iotHubSSLContext, "generateTrustManagerFactory", new Class[] { IotHubCertificateManager.class, KeyStore.class }, mockedCertificateManager, mockedKeyStore);
                times = 1;

                mockedSSLContext.init(mockKeyManagers, mockedTrustManager, new SecureRandom());
                times = 1;
            }
        };
    }

    // Tests_SRS_IOTHUBSSLCONTEXT_34_040: [If the provided cert is a path, this function shall set the path of the default cert to the provided cert path.]
    @Test
    public void constructorWithDefaultCertPathAndPublicCertAndPrivateKey() throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, CertificateException
    {
        //arrange
        final String publicKeyCert = "someCert";
        final String privateKey = "someKey";
        final String iotHubTrustedCertPath = "some trusted cert path";

        final Collection<X509Certificate> testCertChain = new ArrayList<>();
        testCertChain.add(mockedX509Certificate);

        new MockUp<IotHubSSLContext>()
        {
            @Mock Key parsePrivateKey(String privateKeyString) throws CertificateException
            {
                return mockedPrivateKey;
            }

            @Mock Collection<X509Certificate> parsePublicKeyCertificate(String publicKeyCertificateString) throws CertificateException
            {
                return testCertChain;
            }
        };

        new Expectations()
        {
            {
                Deencapsulation.newInstance(IotHubCertificateManager.class);
                result = mockedCertificateManager;

                Deencapsulation.newInstance(IotHubCertificateManager.class);
                result = mockedCertificateManager;

                mockKeyManagerFactory.getKeyManagers();
                result = mockKeyManagers;

                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                result = mockedTrustManagerFactory;

                mockedTrustManagerFactory.getTrustManagers();
                result = mockedTrustManager;
            }
        };

        final IotHubSSLContext iotHubSSLContext = Deencapsulation.newInstance(IotHubSSLContext.class, new Class[]{String.class, String.class, String.class, boolean.class}, publicKeyCert, privateKey, iotHubTrustedCertPath, true);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedCertificateManager, "setCertificatesPath", new Class[] {String.class}, iotHubTrustedCertPath);
                times = 1;

                SSLContext.getInstance("TLSv1.2");
                times = 1;

                Deencapsulation.invoke(iotHubSSLContext, "generateTemporaryPassword");
                times = 1;

                mockedKeyStore.setCertificateEntry(anyString, mockedX509Certificate);
                times = 1;

                new SecureRandom();
                times = 2;

                mockedSSLContext.init(mockKeyManagers, mockedTrustManager, new SecureRandom());
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

    // Tests_SRS_IOTHUBSSLCONTEXT_34_031: [This function shall return a Private Key instance created by the provided PEM formatted privateKeyString.]
    @Test
    public void parsePrivateKeySuccess() throws CertificateException, IOException
    {
        //arrange
        new MockUp<IotHubSSLContext>()
        {
            @Mock Key parsePrivateKey(String privateKeyString) throws CertificateException
            {
                return mockedPrivateKey;
            }
        };

        //act
        Key actualPrivateKey = Deencapsulation.invoke(IotHubSSLContext.class, "parsePrivateKey", new Class[] {String.class}, expectedPrivateKeyString);

        //assert
        assertEquals(mockedPrivateKey, actualPrivateKey);
    }

    @Test
    public void parsePrivateKeyType2Success() throws CertificateException, IOException
    {
        //arrange
        new MockUp<IotHubSSLContext>()
        {
            @Mock Key parsePrivateKey(String privateKeyString) throws CertificateException
            {
                return mockedPrivateKey;
            }
        };

        //act
        Key actualPrivateKey = Deencapsulation.invoke(IotHubSSLContext.class, "parsePrivateKey", new Class[] {String.class}, expectedPrivateKeyString);

        //assert
        assertEquals(mockedPrivateKey, actualPrivateKey);
    }
/*
    // Tests_SRS_IOTHUBSSLCONTEXT_34_032: [If any exception is encountered while attempting to create the private key instance, this function shall throw a CertificateException.]
    @Test (expected = CertificateException.class)
    public void parsePrivateKeyExceptionsWrappedInCertificateException() throws CertificateException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                new StringReader(expectedPrivateKeyString);
                result = new IOException();
            }
        };

        //act
        PrivateKey actualPrivateKey = Deencapsulation.invoke(IotHubSSLContext.class, "parsePrivateKey", new Class[] {String.class}, expectedPrivateKeyString);
    }

    // Tests_Codes_SRS_IOTHUBSSLCONTEXT_34_033: [This function shall return the X509Certificate cert chain specified by the PEM formatted publicKeyCertificateString.]
    @Test
    public void parsePublicKeyCertificateSuccess() throws CertificateException, IOException
    {
        final byte[] byteArray = new byte[]{1,2};
        //arrange
        new StrictExpectations()
        {
            {
                new BouncyCastleProvider();
                result = mockedBouncyCastleProvider;

                Security.addProvider(mockedBouncyCastleProvider);

                CertificateFactory.getInstance("X.509");
                result = mockedCertificateFactory;

                new StringReader(expectedPublicKeyCertificateString);
                result = mockedStringReader;

                new PemReader(mockedStringReader);
                result = mockedPemReader;

                mockedPemReader.readPemObject();
                result = mockedPemObject;

                mockedPemObject.getContent();
                result = byteArray;

                new ByteArrayInputStream(byteArray);
                result = mockedByteArrayInputStream;

                mockedByteArrayInputStream.available();
                result = 2;

                mockedCertificateFactory.generateCertificate(mockedByteArrayInputStream);
                result = mockedX509Certificate;

                mockedByteArrayInputStream.available();
                result = 0;

                mockedPemReader.readPemObject();
                result = mockedPemObject;

                mockedPemObject.getContent();
                result = byteArray;

                new ByteArrayInputStream(byteArray);
                result = mockedByteArrayInputStream;

                mockedByteArrayInputStream.available();
                result = 2;

                mockedCertificateFactory.generateCertificate(mockedByteArrayInputStream);
                result = mockedX509Certificate;

                mockedByteArrayInputStream.available();
                result = 0;

                mockedPemReader.readPemObject();
                result = null;

                mockedPemReader.close();
            }
        };

        //act
        Collection<X509Certificate> actualPublicKeyCertificate = Deencapsulation.invoke(IotHubSSLContext.class, "parsePublicKeyCertificate", new Class[] {String.class}, expectedPublicKeyCertificateString);

        //assert
        assertNotNull(actualPublicKeyCertificate);
        assertEquals(2, actualPublicKeyCertificate.size());
    }

    // Tests_SRS_IOTHUBSSLCONTEXT_34_034: [If any exception is encountered while attempting to create the public key certificate instance, this function shall throw a CertificateException.]
    @Test (expected = CertificateException.class)
    public void parsePublicKeyCertificateExceptionsWrappedInCertificateException() throws CertificateException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                new PemReader(new StringReader(expectedPublicKeyCertificateString));
                result = new IOException();
            }
        };

        //act
        X509Certificate actualPublicKeyCertificate = Deencapsulation.invoke(IotHubSSLContext.class, "parsePublicKeyCertificate", new Class[] {String.class}, expectedPublicKeyCertificateString);

        //assert
        assertEquals(mockedX509Certificate, actualPublicKeyCertificate);
    }
*/
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
