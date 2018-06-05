/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.AuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import mockit.Deencapsulation;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class AuthenticationProviderTest
{

    private static String expectedHostname = "hostname";
    private static String expectedGatewayHostname = "gatewayhostname";
    private static String expectedDeviceId = "deviceId";
    private static String expectedModuleId = "moduleId";
    
    private class AuthenticationProviderMock extends AuthenticationProvider
    {
        public AuthenticationProviderMock(String hostname, String gatewayHostname, String deviceId, String moduleId)
        {
            super(hostname, gatewayHostname, deviceId, moduleId);
        }
        
        @Override
        public SSLContext getSSLContext() throws IOException, TransportException 
        {
            return null;
        }
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_001: [The constructor shall save the provided hostname, gatewayhostname, deviceid and moduleid.]
    @Test
    public void constructorSavesArguments()
    {
        //act
        AuthenticationProvider authenticationProvider = new AuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId);
        
        //assert
        assertEquals(expectedHostname, Deencapsulation.getField(authenticationProvider, "hostname"));
        assertEquals(expectedGatewayHostname, Deencapsulation.getField(authenticationProvider, "gatewayHostname"));
        assertEquals(expectedDeviceId, Deencapsulation.getField(authenticationProvider, "deviceId"));
        assertEquals(expectedModuleId, Deencapsulation.getField(authenticationProvider, "moduleId"));
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_002: [This function shall return the saved hostname.]
    // Tests_SRS_AUTHENTICATIONPROVIDER_34_003: [This function shall return the saved gatewayHostname.]
    // Tests_SRS_AUTHENTICATIONPROVIDER_34_004: [This function shall return the saved deviceId.]
    // Tests_SRS_AUTHENTICATIONPROVIDER_34_005: [This function shall return the saved moduleId.]
    @Test
    public void gettersWork()
    {
        //arrange
        AuthenticationProvider authenticationProvider = new AuthenticationProviderMock(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId);

        //act
        String actualHostName = authenticationProvider.getHostname();
        String actualGatewayHostname = authenticationProvider.getGatewayHostname();
        String actualDeviceId = authenticationProvider.getDeviceId();
        String actualModuleId = authenticationProvider.getModuleId();
        
        //assert
        assertEquals(expectedHostname, actualHostName);
        assertEquals(expectedGatewayHostname, actualGatewayHostname);
        assertEquals(expectedDeviceId, actualDeviceId);
        assertEquals(expectedModuleId, actualModuleId);
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_006: [If the provided hostname is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullHostname()
    {
        //act
        AuthenticationProvider authenticationProvider = new AuthenticationProviderMock(null, expectedGatewayHostname, expectedDeviceId, expectedModuleId);
    }

    // Tests_SRS_AUTHENTICATIONPROVIDER_34_007: [If the provided device id is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullDeviceId()
    {
        //act
        AuthenticationProvider authenticationProvider = new AuthenticationProviderMock(expectedHostname, expectedGatewayHostname, null, expectedModuleId);
    }
}
