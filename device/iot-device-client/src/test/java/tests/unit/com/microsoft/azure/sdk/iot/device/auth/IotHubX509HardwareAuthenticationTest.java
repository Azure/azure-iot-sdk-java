/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.auth.IotHubX509Authentication;
import com.microsoft.azure.sdk.iot.device.auth.IotHubX509HardwareAuthentication;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityClient;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityClientX509;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityClientException;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for IotHubX509HardwareAuthentication.java
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubX509HardwareAuthenticationTest
{
    @Mocked SecurityClientX509 mockSecurityClientX509;
    @Mocked SecurityClient mockSecurityClient;
    @Mocked IotHubSSLContext mockIotHubSSLContext;
    @Mocked SSLContext mockSSLContext;

    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_001: [This function shall save the provided security client.]
    @Test
    public void constructorSuccess()
    {
        //act
        IotHubX509Authentication authentication = new IotHubX509HardwareAuthentication(mockSecurityClientX509);

        //assert
        SecurityClient actualSecurityClient = Deencapsulation.getField(authentication, "securityClientX509");
        assertEquals(mockSecurityClientX509, actualSecurityClient);
    }

    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_002: [If the provided security client is not an instance of SecurityClientX509, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidSecurityClientInstance()
    {
        //act
        IotHubX509Authentication authentication = new IotHubX509HardwareAuthentication(mockSecurityClient);
    }

    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_032: [If this object was created using a constructor that takes an SSLContext, this function shall throw an UnsupportedOperationException.]
    @Test(expected = UnsupportedOperationException.class)
    public void cannotSetNewDefaultCertPathIfConstructedWithSSLContext()
    {
        //arrange
        IotHubX509Authentication authentication = new IotHubX509HardwareAuthentication(mockSecurityClientX509);

        //act
        authentication.setPathToIotHubTrustedCert("any path");
    }

    //Tests_SRS_IOTHUBX509AUTHENTICATION_34_033: [If this object was created using a constructor that takes an SSLContext, this function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void cannotSetNewDefaultCertIfConstructedWithSSLContext()
    {
        //arrange
        IotHubX509Authentication authentication = new IotHubX509HardwareAuthentication(mockSecurityClientX509);

        //act
        authentication.setIotHubTrustedCert("any string");
    }

    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_004: [If the security client throws a SecurityClientException while generating an SSLContext, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void getSSLContextThrowsIOExceptionIfExceptionEncountered() throws SecurityClientException, IOException
    {
        //arrange
        IotHubX509Authentication authentication = new IotHubX509HardwareAuthentication(mockSecurityClientX509);

        new NonStrictExpectations()
        {
            {
                mockSecurityClientX509.getSSLContext();
                result = new SecurityClientException("");
            }
        };

        //act
        authentication.getSSLContext();
    }

    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_003: [If this object's ssl context has not been generated yet, this function shall generate it from the saved security client.]
    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_005: [This function shall return the saved IotHubSSLContext.]
    @Test
    public void getSSLContextSuccess() throws SecurityClientException, IOException
    {
        //arrange
        IotHubX509Authentication authentication = new IotHubX509HardwareAuthentication(mockSecurityClientX509);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockIotHubSSLContext, "getSSLContext");
                result = mockSSLContext;

                mockSecurityClientX509.getSSLContext();
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
        IotHubX509Authentication auth = new IotHubX509HardwareAuthentication(mockSecurityClientX509);

        //act
        auth.setPathToIotHubTrustedCert("any string");
    }

    //Tests_SRS_IOTHUBX509HARDWAREAUTHENTICATION_34_007: [This function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void setCertificateThrows()
    {
        //arrange
        IotHubX509Authentication auth = new IotHubX509HardwareAuthentication(mockSecurityClientX509);

        //act
        auth.setIotHubTrustedCert("any string");
    }
}
