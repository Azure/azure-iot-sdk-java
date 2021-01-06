/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.auth.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static org.junit.Assert.*;

/**
 * Unit tests for IotHubX509SoftwareAuthenticationProvider.java
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubX509SoftwareIotHubAuthenticationProviderTest
{
    private static final String publicKeyCertificate = "someCert";
    private static final String privateKey = "someKey";

    private static final String hostname = "hostname";
    private static final String gatewayHostname = "gateway";
    private static final String deviceId = "deviceId";
    private static final String moduleId = "moduleId";


    private void commonExpectations() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException
    {
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockIotHubX509, "getPublicKeyCertificate");
                result = publicKeyCertificate;
                Deencapsulation.invoke(mockIotHubX509, "getPrivateKey");
                result = privateKey;
            }
        };
    }

    @Mocked IotHubSSLContext mockIotHubSSLContext;
    @Mocked IotHubX509 mockIotHubX509;
    @Mocked SSLContext mockSSLContext;

    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_002: [This constructor will create and save an IotHubX509 object using the provided public key certificate and private key.]
    @Test
    public void constructorSuccessCertStringKeyString() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException
    {
        //act
        new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, false, privateKey, false);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, publicKeyCertificate, false, privateKey, false);
                times = 1;
            }
        };
    }

    @Test
    public void constructorSuccessWithSSLContext()
    {
        //arrange
        new Expectations()
        {
            {
                new IotHubSSLContext(mockSSLContext);
                result = mockIotHubSSLContext;
            }
        };

        //act
        IotHubX509SoftwareAuthenticationProvider provider = new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, mockSSLContext);

        //assert
        assertNull(Deencapsulation.getField(provider, "iotHubX509"));
    }

    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_002: [This constructor will create and save an IotHubX509 object using the provided public key certificate and private key.]
    @Test
    public void constructorSuccessCertPathKeyString() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException
    {
        //act
        new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, true, privateKey, false);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, publicKeyCertificate, true, privateKey, false);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_002: [This constructor will create and save an IotHubX509 object using the provided public key certificate and private key.]
    @Test
    public void constructorSuccessCertStringKeyPath() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException
    {
        //act
        new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, false, privateKey, true);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, publicKeyCertificate, false, privateKey, true);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_002: [This constructor will create and save an IotHubX509 object using the provided public key certificate and private key.]
    @Test
    public void constructorSuccessCertPathKeyPath() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException
    {
        //act
        new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, true, privateKey, true);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, publicKeyCertificate, true, privateKey, true);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_005: [This function shall return the saved IotHubSSLContext.]
    @Test
    public void getSSLContextGets() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException, TransportException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockIotHubSSLContext, "getSSLContext");
                result = mockSSLContext;
            }
        };
        IotHubAuthenticationProvider x509Auth = new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, false, privateKey, false);
        Deencapsulation.setField(x509Auth, "iotHubSSLContext", mockIotHubSSLContext);

        //act
        SSLContext actualSSLContext = x509Auth.getSSLContext();

        //assert
        assertEquals(mockSSLContext, actualSSLContext);
    }

    // Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_019: [If this has a saved iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert as the trusted cert.]
    @Test
    public void generateSSLContextUsesSavedTrustedCert() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, UnrecoverableKeyException
    {
        //arrange
        commonExpectations();
        final String expectedCert = "someTrustedCert";
        IotHubAuthenticationProvider x509Auth = new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, false, privateKey, false);
        x509Auth.setIotHubTrustedCert(expectedCert);

        //act
        Deencapsulation.invoke(x509Auth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, String.class, String.class, boolean.class}, publicKeyCertificate, privateKey, expectedCert, false);
                times = 1;
            }
        };
    }

    // Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_020: [If this has a saved path to a iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert path as the trusted cert.]
    @Test
    public void generateSSLContextUsesSavedTrustedCertPath() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, UnrecoverableKeyException
    {
        //arrange
        commonExpectations();
        final String expectedCertPath = "someTrustedCertPath";
        IotHubAuthenticationProvider x509Auth = new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, false, privateKey, false);
        x509Auth.setPathToIotHubTrustedCert(expectedCertPath);

        //act
        Deencapsulation.invoke(x509Auth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, String.class, String.class, boolean.class}, publicKeyCertificate, privateKey, expectedCertPath, true);
                times = 1;
            }
        };
    }

    // Tests_SRS_IOTHUBX509AUTHENTICATION_34_021: [If this has no saved iotHubTrustedCert or path, This function shall create and save a new IotHubSSLContext object with the saved public and private key combo.]
    @Test
    public void generateSSLContextUsesSavedKeys() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyManagementException, KeyStoreException
    {
        //arrange
        commonExpectations();
        IotHubAuthenticationProvider x509Auth = new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, false, privateKey, false);

        //act
        Deencapsulation.invoke(x509Auth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, String.class}, publicKeyCertificate, privateKey);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_059: [This function shall save the provided pathToCertificate.]
    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_030: [If the provided pathToCertificate is different than the saved path, this function shall set sslContextNeedsRenewal to true.]
    @Test
    public void setPathToCertificateWorks() throws IOException
    {
        //arrange
        IotHubAuthenticationProvider auth = new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, false, privateKey, false);
        String pathToCert = "somePath";

        //act
        auth.setPathToIotHubTrustedCert(pathToCert);

        //assert
        String actualPathToCert = Deencapsulation.getField(auth, "pathToIotHubTrustedCert");
        assertEquals(pathToCert, actualPathToCert);
        boolean sslContextNeedsRenewal = Deencapsulation.getField(auth, "sslContextNeedsUpdate");
        assertTrue(sslContextNeedsRenewal);
    }

    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_031: [If the provided certificate is different than the saved certificate, this function shall set sslContextNeedsRenewal to true.]
    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_064: [This function shall save the provided userCertificateString.]
    @Test
    public void setCertificateWorks() throws IOException
    {
        //arrange
        IotHubAuthenticationProvider auth = new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, false, privateKey, false);
        String cert = "somePath";

        //act
        auth.setIotHubTrustedCert(cert);

        //assert
        String actualCert = Deencapsulation.getField(auth, "iotHubTrustedCert");
        assertEquals(cert, actualCert);
        boolean sslContextNeedsRenewal = Deencapsulation.getField(auth, "sslContextNeedsUpdate");
        assertTrue(sslContextNeedsRenewal);
    }

    //Tests_SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_004: [If the security provider throws a SecurityProviderException while generating an SSLContext, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void getSSLContextThrowsIOExceptionIfExceptionEncountered() throws SecurityProviderException, IOException, TransportException
    {
        //arrange
        IotHubAuthenticationProvider authentication = new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, false, privateKey, false);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, String.class});
                result = mockIotHubSSLContext;

                Deencapsulation.invoke(mockIotHubSSLContext, "getSSLContext");
                result = new CertificateException();
            }
        };

        //act
        authentication.getSSLContext();
    }

    //Tests_SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_003: [If this object's ssl context has not been generated yet, this function shall generate it from the saved security provider.]
    //Tests_SRS_IOTHUBX509SOFTWAREAUTHENTICATION_34_005: [This function shall return the saved IotHubSSLContext.]
    @Test
    public void getSSLContextSuccess() throws IOException, TransportException
    {
        //arrange
        IotHubAuthenticationProvider authentication = new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, publicKeyCertificate, false, privateKey, false);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, String.class});
                result = mockIotHubSSLContext;

                Deencapsulation.invoke(mockIotHubSSLContext, "getSSLContext");
                result = mockSSLContext;
            }
        };

        Deencapsulation.setField(authentication, "iotHubSSLContext", null);

        //act
        SSLContext actualSSLContext = authentication.getSSLContext();
        assertEquals(mockSSLContext, actualSSLContext);
    }

}

