/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthentication;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.Assert.*;

/**
 * Unit tests for IotHubSasTokenAuthentication.java
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubSasTokenAuthenticationTest
{
    @Mocked
    SSLContext mockSSLContext;
    @Mocked
    IotHubSSLContext mockIotHubSSLContext;
    @Mocked
    IotHubSasToken mockSasToken;
    @Mocked
    System mockSystem;

    private static String expectedDeviceId = "deviceId";
    private static String expectedHostname = "hostname";
    private static String expectedDeviceKey = "deviceKey";
    private static String expectedSasToken = "sasToken";
    private static long expectedExpiryTime = 3600;
    private static final long MILLISECONDS_PER_SECOND = 1000L;
    private static final long MINIMUM_EXPIRATION_TIME_OFFSET = 1L;

    private class mockIotHubSasTokenAuthenticationImplementation extends IotHubSasTokenAuthentication
    {
        public mockIotHubSasTokenAuthenticationImplementation()
        {
            this.sasToken = mockSasToken;
            this.deviceId = expectedDeviceId;
            this.hostname = expectedHostname;
        }

        @Override
        public void setPathToIotHubTrustedCert(String pathToCertificate)
        {
        }

        @Override
        public void setIotHubTrustedCert(String certificate)
        {
        }

        @Override
        public SSLContext getSSLContext() throws IOException
        {
            return null;
        }

        @Override
        public String getRenewedSasToken() throws IOException
        {
            return null;
        }
    }

    // Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_12_001: [This function shall return the tokenValidSecs as the number of seconds the current sas token valid for.]
    @Test
    public void getTokenValidSecs()
    {
        //arrange
        long newTokenValidSecs = 5000L;
        IotHubSasTokenAuthentication sasAuth = new mockIotHubSasTokenAuthenticationImplementation();
        sasAuth.setTokenValidSecs(newTokenValidSecs);

        //act
        long actualTokenValidSecs = sasAuth.getTokenValidSecs();

        //assert
        assertEquals(newTokenValidSecs, actualTokenValidSecs);
    }


    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_012: [This function shall save the provided tokenValidSecs as the number of seconds that created sas tokens are valid for.]
    @Test
    public void setTokenValidSecsSets() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        long newTokenValidSecs = 5000L;
        IotHubSasTokenAuthentication sasAuth = new mockIotHubSasTokenAuthenticationImplementation();

        //act
        sasAuth.setTokenValidSecs(newTokenValidSecs);

        //assert
        long actualTokenValidSecs = Deencapsulation.getField(sasAuth, "tokenValidSecs");
        assertEquals(newTokenValidSecs, actualTokenValidSecs);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_018: [This function shall return the current sas token without renewing it.]
    @Test
    public void getCurrentSasTokenGetsCurrentToken()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockSasToken.toString();
                result = expectedSasToken;
            }
        };

        IotHubSasTokenAuthentication sasAuth = new mockIotHubSasTokenAuthenticationImplementation();

        //act
        String actualCurrentSasToken = sasAuth.getCurrentSasToken();

        //assert
        assertEquals(expectedSasToken, actualCurrentSasToken);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired, this function shall return true.]
    @Test
    public void isRenewalNecessaryWithExpiredToken()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
            }
        };

        IotHubSasTokenAuthentication sasAuth = new mockIotHubSasTokenAuthenticationImplementation();
        Deencapsulation.setField(sasAuth, "sasToken", mockSasToken);

        //act
        boolean isRenewalNecessary = Deencapsulation.invoke(sasAuth, "isRenewalNecessary");

        //assert
        assertTrue(isRenewalNecessary);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired, this function shall return true.]
    @Test
    public void isRenewalNecessaryWithNonExpiredToken()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;
            }
        };

        IotHubSasTokenAuthentication sasAuth = new mockIotHubSasTokenAuthenticationImplementation();
        Deencapsulation.setField(sasAuth, "sasToken", mockSasToken);

        //act
        boolean isRenewalNecessary = Deencapsulation.invoke(sasAuth, "isRenewalNecessary");

        //assert
        assertFalse(isRenewalNecessary);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_001: [This function shall return the number of seconds from the UNIX Epoch that a sas token constructed now would expire.]
    @Test
    public void getExpiryTimeInSecondsSuccess()
    {
        //arrange
        long expectedExpiryTime = 3601;
        new NonStrictExpectations()
        {
            {
                System.currentTimeMillis();
                result = 0;
            }
        };

        IotHubSasTokenAuthentication sasAuth = new mockIotHubSasTokenAuthenticationImplementation();

        //act
        long actualExpiryTime =  Deencapsulation.invoke(sasAuth, "getExpiryTimeInSeconds");

        //assert
        assertEquals(expectedExpiryTime, actualExpiryTime);
    }
}
