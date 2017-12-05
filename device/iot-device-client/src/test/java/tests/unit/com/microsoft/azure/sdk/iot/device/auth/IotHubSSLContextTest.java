// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.IotHubCertificateManager;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        final String temporaryPassword = "00000000-0000-0000-0000-000000000000";

        new NonStrictExpectations(IotHubSSLContext.class)
        {
            {
                Deencapsulation.invoke(IotHubSSLContext.class, "parsePrivateKey", privateKey);
                returns(mockedPrivateKey);

                Deencapsulation.invoke(IotHubSSLContext.class, "parsePublicKeyCertificate", publicKeyCert);
                returns(mockedX509Certificate);

                KeyManagerFactory.getInstance("SunX509");
                result = mockKeyManagerFactory;

                mockUUID.randomUUID();
                result = UUID.fromString(temporaryPassword);

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
                times = 1;

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
                Deencapsulation.invoke(mockedCertificateManager, "setValidCertPath", new Class[] {String.class}, defaultCertPath);
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
                Deencapsulation.invoke(mockedCertificateManager, "setValidCert", new Class[] {String.class}, defaultCert);
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
        final String temporaryPassword = "00000000-0000-0000-0000-000000000000";
        final String iotHubTrustedCert = "some trusted cert";

        new NonStrictExpectations(IotHubSSLContext.class)
        {
            {
                Deencapsulation.newInstance(IotHubCertificateManager.class);
                result = mockedCertificateManager;

                Deencapsulation.invoke(IotHubSSLContext.class, "parsePrivateKey", privateKey);
                returns(mockedPrivateKey);

                Deencapsulation.invoke(IotHubSSLContext.class, "parsePublicKeyCertificate", publicKeyCert);
                returns(mockedX509Certificate);

                KeyManagerFactory.getInstance("SunX509");
                result = mockKeyManagerFactory;

                mockUUID.randomUUID();
                result = UUID.fromString(temporaryPassword);

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
                Deencapsulation.invoke(mockedCertificateManager, "setValidCert", new Class[] {String.class}, iotHubTrustedCert);
                times = 1;

                SSLContext.getInstance("TLSv1.2");
                times = 1;

                Deencapsulation.invoke(iotHubSSLContext, "generateTemporaryPassword");
                times = 1;

                mockedKeyStore.setCertificateEntry(anyString, mockedX509Certificate);
                times = 1;

                new SecureRandom();
                times = 1;

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
        final String temporaryPassword = "00000000-0000-0000-0000-000000000000";
        final String iotHubTrustedCertPath = "some trusted cert path";

        new NonStrictExpectations(IotHubSSLContext.class)
        {
            {
                Deencapsulation.newInstance(IotHubCertificateManager.class);
                result = mockedCertificateManager;

                Deencapsulation.invoke(IotHubSSLContext.class, "parsePrivateKey", privateKey);
                returns(mockedPrivateKey);

                Deencapsulation.invoke(IotHubSSLContext.class, "parsePublicKeyCertificate", publicKeyCert);
                returns(mockedX509Certificate);

                KeyManagerFactory.getInstance("SunX509");
                result = mockKeyManagerFactory;

                mockUUID.randomUUID();
                result = UUID.fromString(temporaryPassword);

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
                Deencapsulation.invoke(mockedCertificateManager, "setValidCertPath", new Class[] {String.class}, iotHubTrustedCertPath);
                times = 1;

                SSLContext.getInstance("TLSv1.2");
                times = 1;

                Deencapsulation.invoke(iotHubSSLContext, "generateTemporaryPassword");
                times = 1;

                mockedKeyStore.setCertificateEntry(anyString, mockedX509Certificate);
                times = 1;

                new SecureRandom();
                times = 1;

                Deencapsulation.invoke(iotHubSSLContext, "generateTrustManagerFactory", new Class[] { IotHubCertificateManager.class, KeyStore.class }, mockedCertificateManager, mockedKeyStore);
                times = 1;

                mockedSSLContext.init(mockKeyManagers, mockedTrustManager, new SecureRandom());
                times = 1;
            }
        };
    }

    //----------- PEM UTILITIES TESTS -----------

    private static final String expectedPrivateKeyString = "some private key string";
    private static final String expectedPublicKeyCertificateString = "some public key certificate string";

    @Mocked PEMKeyPair mockedPEMKeyPair;
    @Mocked PEMParser mockedPEMParser;
    @Mocked PemObject mockedPemObject;
    @Mocked PemReader mockedPemReader;
    @Mocked StringReader mockedStringReader;
    @Mocked KeyPair mockedKeyPair;
    @Mocked CertificateFactory mockedCertificateFactory;

    // Tests_SRS_IOTHUBSSLCONTEXT_34_031: [This function shall return a Private Key instance created by the provided PEM formatted privateKeyString.]
    @Test
    public void parsePrivateKeySuccess() throws CertificateException, IOException
    {
        //arrange
        new NonStrictExpectations(IotHubSSLContext.class)
        {
            {
                new StringReader(expectedPrivateKeyString);
                result = mockedStringReader;

                new PEMParser(mockedStringReader);
                result = mockedPEMParser;

                mockedPEMParser.readObject();
                result = mockedPEMKeyPair;

                //Doing this instead of just mocking JCA converter because trying to mock the JCA converter causes strange errors to be thrown.
                Deencapsulation.invoke(IotHubSSLContext.class, "getPrivateKeyFromPEMKeyPair", new Class[] {PEMKeyPair.class}, mockedPEMKeyPair);
                result = mockedPrivateKey;
            }
        };

        //act
        Key actualPrivateKey = Deencapsulation.invoke(IotHubSSLContext.class, "parsePrivateKey", new Class[] {String.class}, expectedPrivateKeyString);

        //assert
        assertEquals(mockedPrivateKey, actualPrivateKey);
    }

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

    // Tests_SRS_IOTHUBSSLCONTEXT_34_033: [This function shall return an X509Certificate instance created by the provided PEM formatted publicKeyCertificateString.]
    @Test
    public void parsePublicKeyCertificateSuccess() throws CertificateException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                new PemReader(new StringReader(expectedPublicKeyCertificateString));
                result = mockedPemReader;

                mockedPemReader.readPemObject();
                result = mockedPemObject;

                CertificateFactory.getInstance("X.509");
                result = mockedCertificateFactory;

                mockedCertificateFactory.generateCertificate(new ByteArrayInputStream(mockedPemObject.getContent()));
                result = mockedX509Certificate;
            }
        };

        //act
        X509Certificate actualPublicKeyCertificate = Deencapsulation.invoke(IotHubSSLContext.class, "parsePublicKeyCertificate", new Class[] {String.class}, expectedPublicKeyCertificateString);

        //assert
        assertEquals(mockedX509Certificate, actualPublicKeyCertificate);
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
}
