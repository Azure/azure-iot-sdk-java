/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.cert.CertificateException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class IotHubAuthenticationProviderTest
{
    @Mocked
    IotHubSSLContext mockedIotHubSSLContext;

    @Mocked
    SSLContext mockedSSLContext;
    
    private static final String expectedHostname = "hostname";
    private static final String expectedGatewayHostname = "gatewayhostname";
    private static final String expectedDeviceId = "deviceId";
    private static final String expectedModuleId = "moduleId";
    
    private class IotHubAuthenticationProviderMock extends IotHubAuthenticationProvider
    {
        public IotHubAuthenticationProviderMock(String hostname, String gatewayHostname, String deviceId, String moduleId)
        {
            super(hostname, gatewayHostname, deviceId, moduleId);
        }

        public IotHubAuthenticationProviderMock(String hostname, String gatewayHostname, String deviceId, String moduleId, SSLContext sslContext)
        {
            super(hostname, gatewayHostname, deviceId, moduleId, sslContext);
        }
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_001: [The constructor shall save the provided hostname, gatewayhostname, deviceid and moduleid.]
    @Test
    public void constructorSavesArguments()
    {
        //act
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId);
        
        //assert
        assertEquals(expectedHostname, Deencapsulation.getField(iotHubAuthenticationProvider, "hostname"));
        assertEquals(expectedGatewayHostname, Deencapsulation.getField(iotHubAuthenticationProvider, "gatewayHostname"));
        assertEquals(expectedDeviceId, Deencapsulation.getField(iotHubAuthenticationProvider, "deviceId"));
        assertEquals(expectedModuleId, Deencapsulation.getField(iotHubAuthenticationProvider, "moduleId"));
    }


    @Test
    public void constructorSuccessWithSSLContext()
    {
        //arrange
        new Expectations()
        {
            {
                new IotHubSSLContext(mockedSSLContext);
                result = mockedIotHubSSLContext;
            }
        };

        //act
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockedSSLContext);

        //assert
        assertFalse((boolean) Deencapsulation.getField(iotHubAuthenticationProvider, "sslContextNeedsUpdate"));
        assertEquals(expectedHostname, Deencapsulation.getField(iotHubAuthenticationProvider, "hostname"));
        assertEquals(expectedGatewayHostname, Deencapsulation.getField(iotHubAuthenticationProvider, "gatewayHostname"));
        assertEquals(expectedDeviceId, Deencapsulation.getField(iotHubAuthenticationProvider, "deviceId"));
        assertEquals(expectedModuleId, Deencapsulation.getField(iotHubAuthenticationProvider, "moduleId"));
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_002: [This function shall return the saved hostname.]
    // Tests_SRS_AUTHENTICATIONPROVIDER_34_003: [This function shall return the saved gatewayHostname.]
    // Tests_SRS_AUTHENTICATIONPROVIDER_34_004: [This function shall return the saved deviceId.]
    // Tests_SRS_AUTHENTICATIONPROVIDER_34_005: [This function shall return the saved moduleId.]
    // Tests_SRS_AUTHENTICATIONPROVIDER_34_008: [This function shall return the saved iotHubTrustedCert.]
    // Tests_SRS_AUTHENTICATIONPROVIDER_34_009: [This function shall return the saved pathToIotHubTrustedCert.]
    @Test
    public void gettersWork()
    {
        //arrange
        String expectedCert = "some cert";
        String expectedCertPath = "some/path";
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId);
        Deencapsulation.setField(iotHubAuthenticationProvider, "iotHubTrustedCert", expectedCert);
        Deencapsulation.setField(iotHubAuthenticationProvider, "pathToIotHubTrustedCert", expectedCertPath);

        //act
        String actualHostName = iotHubAuthenticationProvider.getHostname();
        String actualGatewayHostname = iotHubAuthenticationProvider.getGatewayHostname();
        String actualDeviceId = iotHubAuthenticationProvider.getDeviceId();
        String actualModuleId = iotHubAuthenticationProvider.getModuleId();
        String actualCert = iotHubAuthenticationProvider.getIotHubTrustedCert();
        String actualCertPath = iotHubAuthenticationProvider.getPathToIotHubTrustedCert();

        //assert
        assertEquals(expectedHostname, actualHostName);
        assertEquals(expectedGatewayHostname, actualGatewayHostname);
        assertEquals(expectedDeviceId, actualDeviceId);
        assertEquals(expectedModuleId, actualModuleId);
        assertEquals(expectedCert, actualCert);
        assertEquals(expectedCertPath, actualCertPath);
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_006: [If the provided hostname is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullHostname()
    {
        //act
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(null, expectedGatewayHostname, expectedDeviceId, expectedModuleId);
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_007: [If the provided device id is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullDeviceId()
    {
        //act
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, null, expectedModuleId);
    }

    //Codes_SRS_AUTHENTICATIONPROVIDER_34_012: [If a CertificateException, NoSuchAlgorithmException, KeyManagementException, or KeyStoreException is thrown during this function, this function shall throw an IOException.]
    //Codes_SRS_AUTHENTICATIONPROVIDER_34_010: [If this object's ssl context has not been generated yet or if it needs to be re-generated, this function shall regenerate the ssl context.]
    @Test (expected = IOException.class)
    public void getSSLContextWrapsExceptions() throws IOException
    {
        //arrange
        IotHubAuthenticationProvider sasAuth = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = new CertificateException();
            }
        };

        //act
        sasAuth.getSSLContext();
    }

    //Codes_SRS_AUTHENTICATIONPROVIDER_34_011: [This function shall return the generated IotHubSSLContext.]
    @Test
    public void getSSLContextSuccess() throws IOException
    {
        //arrange
        IotHubAuthenticationProvider sasAuth = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = mockedIotHubSSLContext;

                Deencapsulation.invoke(mockedIotHubSSLContext, "getSSLContext");
                result = mockedSSLContext;
            }
        };

        //act
        SSLContext actualSSLContext = sasAuth.getSSLContext();

        //assert
        assertEquals(mockedSSLContext, actualSSLContext);
    }

    //Tests_SRS_AUTHENTICATIONPROVIDER_34_059: [This function shall save the provided pathToCertificate.]
    //Tests_SRS_AUTHENTICATIONPROVIDER_34_030: [If the provided pathToCertificate is different than the saved path, this function shall set sslContextNeedsRenewal to true.]
    @Test
    public void setPathToCertificateWorks() throws IOException
    {
        //arrange
        IotHubAuthenticationProvider auth = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId);
        String pathToCert = "somePath";

        //act
        auth.setPathToIotHubTrustedCert(pathToCert);

        //assert
        String actualPathToCert = auth.getPathToIotHubTrustedCert();
        assertEquals(pathToCert, actualPathToCert);
        boolean sslContextNeedsRenewal = Deencapsulation.getField(auth, "sslContextNeedsUpdate");
        assertTrue(sslContextNeedsRenewal);
    }

    //Tests_SRS_AUTHENTICATIONPROVIDER_34_064: [This function shall save the provided userCertificateString.]
    //Tests_SRS_AUTHENTICATIONPROVIDER_34_031: [If the provided certificate is different than the saved certificate, this function shall set sslContextNeedsRenewal to true.]
    @Test
    public void setCertificateWorks() throws IOException
    {
        //arrange
        IotHubAuthenticationProvider auth = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId);
        String cert = "somePath";

        //act
        auth.setIotHubTrustedCert(cert);

        //assert
        String actualCert = auth.getIotHubTrustedCert();
        assertEquals(cert, actualCert);
        boolean sslContextNeedsRenewal = Deencapsulation.getField(auth, "sslContextNeedsUpdate");
        assertTrue(sslContextNeedsRenewal);
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_019: [If this has a saved iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert as the trusted cert.]
    @Test
    public void generateSSLContextUsesSavedTrustedCert()
    {
        //arrange
        final String expectedCert = "someTrustedCert";
        IotHubAuthenticationProvider auth = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId);
        auth.setIotHubTrustedCert(expectedCert);

        //act
        Deencapsulation.invoke(auth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, boolean.class}, expectedCert, false);
                times = 1;
            }
        };
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_020: [If this has a saved path to a iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert path as the trusted cert.]
    @Test
    public void generateSSLContextUsesSavedTrustedCertPath()
    {
        //arrange
        final String expectedCertPath = "someTrustedCertPath";
        IotHubAuthenticationProvider auth = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId);
        auth.setPathToIotHubTrustedCert(expectedCertPath);

        //act
        Deencapsulation.invoke(auth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, boolean.class}, expectedCertPath, true);
                times = 1;
            }
        };
    }
    
    // Tests_SRS_AUTHENTICATIONPROVIDER_34_021: [If this has no saved iotHubTrustedCert or path, This function shall create and save a new default IotHubSSLContext object.]
    @Test
    public void generateSSLContextGeneratesDefaultIotHubSSLContext()
    {
        //arrange
        IotHubAuthenticationProvider auth = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId);

        //act
        Deencapsulation.invoke(auth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class);
                times = 1;
            }
        };
    }
}
