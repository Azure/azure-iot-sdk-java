// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.IotHubCertificateManager;
import com.microsoft.azure.sdk.iot.device.IotHubSSLContext;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertNotNull;

public class IotHubSSLContextTest
{
    @Mocked
    private SSLContext mockedSSLContext;

    @Mocked
    private TrustManagerFactory mockedTrustManagerFactory;

    @Mocked
    private KeyStore mockedKeyStore;

    @Mocked
    private IotHubCertificateManager mockedDefaultCert;

    @Mocked
    private Certificate mockedCertificate;

    @Mocked
    private TrustManager[] mockedTrustManager;

    @Mocked
    private SecureRandom mockedSecureRandom;

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
                Deencapsulation.invoke(mockedDefaultCert, "getCertificateCollection");
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
        assertNotNull(Deencapsulation.invoke(testContext, "getIotHubSSlContext"));
        testCollection.remove(mockedCertificate);

    }

    //Tests_SRS_IOTHUBSSLCONTEXT_25_010: [**The constructor shall create a certificate with 'cert' if it were a not a path by calling setValidCert.**]**
    @Test
    public void constructorWithParamsCreatesSSLContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //arrange
        testCollection.add(mockedCertificate);
        generateSSLContextExpectations();

        //act
        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class, "TestCertString", false);

        //assert
        generateSSLContextVerifications();
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedDefaultCert, "setValidCert", anyString);
                times = 1;
            }
        };
        assertNotNull(Deencapsulation.invoke(testContext, "getIotHubSSlContext"));
        testCollection.remove(mockedCertificate);
    }

    //Tests_SRS_IOTHUBSSLCONTEXT_25_009: [**The constructor shall create a certificate to be used with IotHub with cert only if it were a path by calling setValidCertPath**]**
    @Test
    public void constructorWithParamsAsPathCreatesSSLContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //arrange
        testCollection.add(mockedCertificate);
        generateSSLContextExpectations();

        //act
        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class, "Test/Cert/Path", true);

        //assert
        generateSSLContextVerifications();
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedDefaultCert, "setValidCertPath", anyString);
                times = 1;
            }
        };
        assertNotNull(Deencapsulation.invoke(testContext, "getIotHubSSlContext"));
        testCollection.remove(mockedCertificate);

    }

    //Tests_SRS_IOTHUBSSLCONTEXT_25_008: [**The constructor shall throw IllegalArgumentException if any of the parameters are null.**]**
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParamsThrowsOnNullCert() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //act
        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class, String.class, true);
    }

    @Test (expected = IOException.class)
    public void constructorWithInvalidCertPathThrows() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        new Expectations()
        {
            {
                Deencapsulation.invoke(mockedDefaultCert, "setValidCertPath", anyString);
                result = new FileNotFoundException();
            }
        };

        //act
        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class, "/Invalid/Test/Cert/Path", true);

    }

    //Tests_SRS_IOTHUBSSLCONTEXT_25_017: [*This method shall return the value of sslContext.**]**
    @Test
    public void getterGetsContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //arrange
        testCollection.add(mockedCertificate);
        generateSSLContextExpectations();

        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class, "Test/Cert/Path", true);

        //act
        SSLContext testSSLContext = Deencapsulation.invoke(testContext, "getIotHubSSlContext");

        //assert
        generateSSLContextVerifications();
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedDefaultCert, "setValidCertPath", anyString);
                times = 1;
            }
        };
        assertNotNull(testSSLContext);
        testCollection.remove(mockedCertificate);
    }

    // Tests_SRS_IOTHUBSSLCONTEXT_21_018: [If the pathToCertificate is not null, the constructor shall create a certificate to be used with IotHub with cert by calling setValidCertPath]
    @Test
    public void constructorWithValidPathAndNullString() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //arrange
        testCollection.add(mockedCertificate);
        generateSSLContextExpectations();
        final String path = "Test/Cert/Path";
        final String cert = null;

        //act
        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, String.class},path, cert);

        //assert
        generateSSLContextVerifications();
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedDefaultCert, "setValidCertPath", new Class[] {String.class}, path);
                times = 1;
                Deencapsulation.invoke(mockedDefaultCert, "setValidCert", new Class[] {String.class}, cert);
                times = 0;
            }
        };
        assertNotNull(Deencapsulation.invoke(testContext, "getIotHubSSlContext"));
        testCollection.remove(mockedCertificate);
    }

    // Tests_SRS_IOTHUBSSLCONTEXT_21_018: [If the pathToCertificate is not null, the constructor shall create a certificate to be used with IotHub with cert by calling setValidCertPath]
    @Test
    public void constructorWithValidPathAndValidString() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //arrange
        testCollection.add(mockedCertificate);
        generateSSLContextExpectations();
        final String path = "Test/Cert/Path";
        final String cert = "validCert";

        //act
        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, String.class},path, cert);

        //assert
        generateSSLContextVerifications();
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedDefaultCert, "setValidCertPath", new Class[] {String.class}, path);
                times = 1;
                Deencapsulation.invoke(mockedDefaultCert, "setValidCert", new Class[] {String.class}, cert);
                times = 0;
            }
        };
        assertNotNull(Deencapsulation.invoke(testContext, "getIotHubSSlContext"));
        testCollection.remove(mockedCertificate);
    }

    // Tests_SRS_IOTHUBSSLCONTEXT_21_019: [If the userCertificateString is not null, and pathToCertificate is null, the constructor shall create a certificate with 'cert' by calling setValidCert.]
    @Test
    public void constructorWithNullPathAndValidString() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //arrange
        testCollection.add(mockedCertificate);
        generateSSLContextExpectations();
        final String path = null;
        final String cert = "validCert";

        //act
        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, String.class},path, cert);

        //assert
        generateSSLContextVerifications();
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedDefaultCert, "setValidCertPath", new Class[] {String.class}, path);
                times = 0;
                Deencapsulation.invoke(mockedDefaultCert, "setValidCert", new Class[] {String.class}, cert);
                times = 1;
            }
        };
        assertNotNull(Deencapsulation.invoke(testContext, "getIotHubSSlContext"));
        testCollection.remove(mockedCertificate);
    }

    // Tests_SRS_IOTHUBSSLCONTEXT_21_020: [If both userCertificateString, and pathToCertificate are null, the constructor shall create a default certificate.]
    @Test
    public void constructorWithNullPathAndNullString() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, CertificateException
    {
        //arrange
        testCollection.add(mockedCertificate);
        generateSSLContextExpectations();
        final String path = null;
        final String cert = null;

        //act
        IotHubSSLContext testContext = Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, String.class},path, cert);

        //assert
        generateSSLContextVerifications();
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedDefaultCert, "setValidCertPath", new Class[] {String.class}, path);
                times = 0;
                Deencapsulation.invoke(mockedDefaultCert, "setValidCert", new Class[] {String.class}, cert);
                times = 0;
            }
        };
        assertNotNull(Deencapsulation.invoke(testContext, "getIotHubSSlContext"));
        testCollection.remove(mockedCertificate);
    }
}
