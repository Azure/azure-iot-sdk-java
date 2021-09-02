/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.IotHubX509HardwareAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for IotHubX509HardwareAuthenticationProvider.java
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubX509HardwareIotHubAuthenticationProviderTest
{
    @Mocked SecurityProviderX509 mockSecurityProviderX509;
    @Mocked SecurityProvider mockSecurityProvider;
    @Mocked IotHubSSLContext mockIotHubSSLContext;
    @Mocked SSLContext mockSSLContext;

    private static final String hostname = "hostname";
    private static final String gatewayHostname = "gateway";
    private static final String deviceId = "deviceId";
    private static final String moduleId = "moduleId";

    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_001: [This function shall save the provided security provider.]
    @Test
    public void constructorSuccess()
    {
        //act
        IotHubAuthenticationProvider authentication = new IotHubX509HardwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, mockSecurityProviderX509);

        //assert
        SecurityProvider actualSecurityProvider = Deencapsulation.getField(authentication, "securityProviderX509");
        assertEquals(mockSecurityProviderX509, actualSecurityProvider);
    }

    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_002: [If the provided security provider is not an instance of SecurityProviderX509, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidSecurityProviderInstance()
    {
        //act
        IotHubAuthenticationProvider authentication = new IotHubX509HardwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, mockSecurityProvider);
    }

    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_032: [If this object was created using a constructor that takes an SSLContext, this function shall throw an UnsupportedOperationException.]
    @Test(expected = UnsupportedOperationException.class)
    public void cannotSetNewDefaultCertPathIfConstructedWithSSLContext()
    {
        //arrange
        IotHubAuthenticationProvider authentication = new IotHubX509HardwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, mockSecurityProviderX509);

        //act
        authentication.setPathToIotHubTrustedCert("any path");
    }

    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_033: [If this object was created using a constructor that takes an SSLContext, this function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void cannotSetNewDefaultCertIfConstructedWithSSLContext()
    {
        //arrange
        IotHubAuthenticationProvider authentication = new IotHubX509HardwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, mockSecurityProviderX509);

        //act
        authentication.setIotHubTrustedCert("any string");
    }

    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_004: [If the security provider throws a SecurityProviderException while generating an SSLContext, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void getSSLContextThrowsIOExceptionIfExceptionEncountered() throws SecurityProviderException, IOException, TransportException
    {
        //arrange
        IotHubAuthenticationProvider authentication = new IotHubX509HardwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, mockSecurityProviderX509);

        new NonStrictExpectations()
        {
            {
                mockSecurityProviderX509.getSSLContext();
                result = new SecurityProviderException("");
            }
        };

        //act
        authentication.getSSLContext();
    }

    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_003: [If this object's ssl context has not been generated yet, this function shall generate it from the saved security provider.]
    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_005: [This function shall return the saved IotHubSSLContext.]
    @Test
    public void getSSLContextSuccess() throws SecurityProviderException, IOException, TransportException
    {
        //arrange
        IotHubAuthenticationProvider authentication = new IotHubX509HardwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, mockSecurityProviderX509);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockIotHubSSLContext, "getSSLContext");
                result = mockSSLContext;

                mockSecurityProviderX509.getSSLContext();
                result = mockSSLContext;

                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] { SSLContext.class }, mockSSLContext);
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

    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_006: [This function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void setPathToCertificateThrows()
    {
        //arrange
        IotHubAuthenticationProvider auth = new IotHubX509HardwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, mockSecurityProviderX509);

        //act
        auth.setPathToIotHubTrustedCert("any string");
    }

    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_007: [This function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void setCertificateThrows()
    {
        //arrange
        IotHubAuthenticationProvider auth = new IotHubX509HardwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, mockSecurityProviderX509);

        //act
        auth.setIotHubTrustedCert("any string");
    }
}
