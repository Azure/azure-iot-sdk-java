/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;

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
    private static final String expectedMqttGatewayHostname = "mqttGatewayHostname";
    private static final String expectedDeviceId = "deviceId";
    private static final String expectedModuleId = "moduleId";
    
    private static class IotHubAuthenticationProviderMock extends IotHubAuthenticationProvider
    {
        public IotHubAuthenticationProviderMock(String hostname, String gatewayHostname, String mqttGatewayHostname, String deviceId, String moduleId)
        {
            super(hostname, gatewayHostname, mqttGatewayHostname, deviceId, moduleId);
        }

        public IotHubAuthenticationProviderMock(String hostname, String gatewayHostname, String mqttGatewayHostname, String deviceId, String moduleId, SSLContext sslContext)
        {
            super(hostname, gatewayHostname, mqttGatewayHostname, deviceId, moduleId, sslContext);
        }
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_001: [The constructor shall save the provided hostname, gatewayhostname, deviceid and moduleid.]
    @Test
    public void constructorSavesArgumentsWithGatewayHostname()
    {
        //act
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, null, expectedDeviceId, expectedModuleId);
        
        //assert
        assertEquals(expectedHostname, Deencapsulation.getField(iotHubAuthenticationProvider, "hostname"));
        assertEquals(expectedGatewayHostname, Deencapsulation.getField(iotHubAuthenticationProvider, "gatewayHostname"));
        assertEquals(null, Deencapsulation.getField(iotHubAuthenticationProvider, "mqttGatewayHostname"));
        assertEquals(expectedDeviceId, Deencapsulation.getField(iotHubAuthenticationProvider, "deviceId"));
        assertEquals(expectedModuleId, Deencapsulation.getField(iotHubAuthenticationProvider, "moduleId"));
    }

    @Test
    public void constructorSavesArgumentsWithMqttGatewayHostname()
    {
        //act
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(expectedHostname, null, expectedMqttGatewayHostname, expectedDeviceId, expectedModuleId);

        //assert
        assertEquals(expectedHostname, Deencapsulation.getField(iotHubAuthenticationProvider, "hostname"));
        assertEquals(null, Deencapsulation.getField(iotHubAuthenticationProvider, "gatewayHostname"));
        assertEquals(expectedMqttGatewayHostname, Deencapsulation.getField(iotHubAuthenticationProvider, "mqttGatewayHostname"));
        assertEquals(expectedDeviceId, Deencapsulation.getField(iotHubAuthenticationProvider, "deviceId"));
        assertEquals(expectedModuleId, Deencapsulation.getField(iotHubAuthenticationProvider, "moduleId"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithGatewayHostnameAndMqttGatewayHostnameThrows()
    {
        //act
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedMqttGatewayHostname, expectedDeviceId, expectedModuleId);
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
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, null, expectedDeviceId, expectedModuleId, mockedSSLContext);

        //assert
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
    public void gettersWorkWithGatewayHostname()
    {
        //arrange
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, null, expectedDeviceId, expectedModuleId);

        //act
        String actualHostName = iotHubAuthenticationProvider.getHostname();
        String actualGatewayHostname = iotHubAuthenticationProvider.getGatewayHostname();
        String actualDeviceId = iotHubAuthenticationProvider.getDeviceId();
        String actualModuleId = iotHubAuthenticationProvider.getModuleId();

        //assert
        assertEquals(expectedHostname, actualHostName);
        assertEquals(expectedGatewayHostname, actualGatewayHostname);
        assertEquals(expectedDeviceId, actualDeviceId);
        assertEquals(expectedModuleId, actualModuleId);
    }

    @Test
    public void gettersWorkWithMqttGatewayHostname()
    {
        //arrange
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(expectedHostname, null, expectedMqttGatewayHostname, expectedDeviceId, expectedModuleId);

        //act
        String actualHostName = iotHubAuthenticationProvider.getHostname();
        String actualMqttGatewayHostname = iotHubAuthenticationProvider.getMqttGatewayHostname();
        String actualDeviceId = iotHubAuthenticationProvider.getDeviceId();
        String actualModuleId = iotHubAuthenticationProvider.getModuleId();

        //assert
        assertEquals(expectedHostname, actualHostName);
        assertEquals(expectedMqttGatewayHostname, actualMqttGatewayHostname);
        assertEquals(expectedDeviceId, actualDeviceId);
        assertEquals(expectedModuleId, actualModuleId);
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_006: [If the provided hostname is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = NullPointerException.class)
    public void constructorThrowsForNullHostname()
    {
        //act
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(null, expectedGatewayHostname, expectedMqttGatewayHostname, expectedDeviceId, expectedModuleId);
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_007: [If the provided device id is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = NullPointerException.class)
    public void constructorThrowsForNullDeviceId()
    {
        //act
        IotHubAuthenticationProvider iotHubAuthenticationProvider = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedMqttGatewayHostname, null, expectedModuleId);
    }

    //Codes_SRS_AUTHENTICATIONPROVIDER_34_012: [If a CertificateException, NoSuchAlgorithmException, KeyManagementException, or KeyStoreException is thrown during this function, this function shall throw an IOException.]
    //Codes_SRS_AUTHENTICATIONPROVIDER_34_010: [If this object's ssl context has not been generated yet or if it needs to be re-generated, this function shall regenerate the ssl context.]
    @Test (expected = SecurityProviderException.class)
    public void getSSLContextWrapsExceptions() throws IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = new SecurityProviderException("");
            }
        };

        IotHubAuthenticationProvider sasAuth = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, null, expectedDeviceId, expectedModuleId);

        //act
        sasAuth.getSSLContext();
    }

    //Codes_SRS_AUTHENTICATIONPROVIDER_34_011: [This function shall return the generated IotHubSSLContext.]
    @Test
    public void getSSLContextSuccess() throws IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = mockedIotHubSSLContext;

                Deencapsulation.invoke(mockedIotHubSSLContext, "getSSLContext");
                result = mockedSSLContext;
            }
        };

        IotHubAuthenticationProvider sasAuth = new IotHubAuthenticationProviderMock(expectedHostname, expectedGatewayHostname, null, expectedDeviceId, expectedModuleId);

        //act
        SSLContext actualSSLContext = sasAuth.getSSLContext();

        //assert
        assertEquals(mockedSSLContext, actualSSLContext);
    }
}
