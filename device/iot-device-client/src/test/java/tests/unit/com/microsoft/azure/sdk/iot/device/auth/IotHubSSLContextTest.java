// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.IotHubCertificateManager;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.bouncycastle.openssl.PEMReader;
import org.junit.Test;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;

/*
 * Unit tests for IotHubSSLContext
 * Code Coverage:
 * Methods: 100%
 * Lines: 96%
 */
public class IotHubSSLContextTest
{
    @Mocked UUID mockUUID;
    @Mocked SecureRandom mockedSecureRandom;

    @Mocked PEMReader mockedPemReaderForPublic;
    @Mocked PEMReader mockedPemReaderForPrivate;
    @Mocked StringReader mockStringReaderPublic;
    @Mocked StringReader mockStringReaderPrivate;

    @Mocked KeyPair mockedKeyPair;
    @Mocked X509Certificate mockedX509Certificate;
    @Mocked PrivateKey mockedPrivateKey;

    @Mocked KeyStore mockedKeyStore;
    @Mocked KeyManagerFactory mockKeyManagerFactory;
    @Mocked KeyManager[] mockKeyManagers;

    @Mocked SSLContext mockedSSLContext;

    @Mocked TrustManagerFactory mockedTrustManagerFactory;
    @Mocked TrustManager[] mockedTrustManager;
    @Mocked IotHubCertificateManager mockedCertificateManager;
    @Mocked Certificate mockedCertificate;

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
                mockedKeyStore.setCertificateEntry(anyString, mockedCertificate);
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
        testCollection.add(mockedCertificate);
        generateSSLContextExpectations();

        //act

        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class);

        //assert
        generateSSLContextVerifications();
        assertNotNull(Deencapsulation.invoke(testContext, "getSSLContext"));
        testCollection.remove(mockedCertificate);

    }

    //Tests_SRS_IOTHUBSSLCONTEXT_25_017: [*This method shall return the value of sslContext.**]**
    @Test
    public void getterGetsContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //arrange
        testCollection.add(mockedCertificate);
        generateSSLContextExpectations();

        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {});

        //act
        SSLContext testSSLContext = Deencapsulation.invoke(testContext, "getSSLContext");

        //assert
        generateSSLContextVerifications();
        assertNotNull(testSSLContext);
        testCollection.remove(mockedCertificate);
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

        new NonStrictExpectations()
        {
            {
                new StringReader(privateKey);
                result = mockStringReaderPrivate;

                new StringReader(publicKeyCert);
                result = mockStringReaderPublic;

                new PEMReader(mockStringReaderPrivate);
                result = mockedPemReaderForPrivate;

                new PEMReader(mockStringReaderPublic);
                result = mockedPemReaderForPublic;

                mockedPemReaderForPrivate.readObject();
                result = mockedKeyPair;

                mockedPemReaderForPublic.readObject();
                result = mockedX509Certificate;

                KeyStore.getInstance(KeyStore.getDefaultType());
                result = mockedKeyStore;

                mockedKeyPair.getPrivate();
                result = mockedPrivateKey;

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

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubCertificateManager.class);
                result = mockedCertificateManager;

                new StringReader(privateKey);
                result = mockStringReaderPrivate;

                new StringReader(publicKeyCert);
                result = mockStringReaderPublic;

                new PEMReader(mockStringReaderPrivate);
                result = mockedPemReaderForPrivate;

                new PEMReader(mockStringReaderPublic);
                result = mockedPemReaderForPublic;

                mockedPemReaderForPrivate.readObject();
                result = mockedKeyPair;

                mockedPemReaderForPublic.readObject();
                result = mockedX509Certificate;

                KeyStore.getInstance(KeyStore.getDefaultType());
                result = mockedKeyStore;

                mockedKeyPair.getPrivate();
                result = mockedPrivateKey;

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
    public void constructorWithDefaultCertPathAndPublicCertAndPrivateKey() throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException
    {
        //arrange
        final String publicKeyCert = "someCert";
        final String privateKey = "someKey";
        final String temporaryPassword = "00000000-0000-0000-0000-000000000000";
        final String iotHubTrustedCertPath = "some trusted cert path";

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubCertificateManager.class);
                result = mockedCertificateManager;

                new StringReader(privateKey);
                result = mockStringReaderPrivate;

                new StringReader(publicKeyCert);
                result = mockStringReaderPublic;

                new PEMReader(mockStringReaderPrivate);
                result = mockedPemReaderForPrivate;

                new PEMReader(mockStringReaderPublic);
                result = mockedPemReaderForPublic;

                mockedPemReaderForPrivate.readObject();
                result = mockedKeyPair;

                mockedPemReaderForPublic.readObject();
                result = mockedX509Certificate;

                KeyStore.getInstance(KeyStore.getDefaultType());
                result = mockedKeyStore;

                mockedKeyPair.getPrivate();
                result = mockedPrivateKey;

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
}
