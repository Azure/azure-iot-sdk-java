/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.Assert.*;

/**
 * Unit tests for IotHubSasTokenAuthenticationProvider.java
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubSasTokenAuthenticationProviderTest
{
    @Mocked
    SSLContext mockSSLContext;
    @Mocked
    IotHubSSLContext mockIotHubSSLContext;
    @Mocked
    IotHubSasToken mockSasToken;

    private static String expectedDeviceId = "deviceId";
    private static String expectedHostname = "hostname";
    private static String expectedDeviceKey = "deviceKey";
    private static String expectedSasToken = "sasToken";
    private static long expectedExpiryTime = 3600;
    private static final long MILLISECONDS_PER_SECOND = 1000L;
    private static final long MINIMUM_EXPIRATION_TIME_OFFSET = 1L;

    private class mockIotHubSasTokenAuthenticationImplementation extends IotHubSasTokenAuthenticationProvider
    {
        public mockIotHubSasTokenAuthenticationImplementation()
        {
            super(expectedHostname, null, expectedDeviceId, null);
            this.sasToken = mockSasToken;
            this.deviceId = expectedDeviceId;
            this.hostname = expectedHostname;
        }

        public mockIotHubSasTokenAuthenticationImplementation(long expectedTimeToLive, int expectedBufferPercentage)
        {
            super(expectedHostname, null, expectedDeviceId, null, expectedTimeToLive, expectedBufferPercentage);
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
        public String getRenewedSasToken(boolean proactivelyRenew, boolean forceRenewal) throws IOException
        {
            return null;
        }

        @Override
        public boolean canRefreshToken()
        {
            return false;
        }
    }

    // Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_12_001: [This function shall return the tokenValidSecs as the number of seconds the current sas token valid for.]
    @Test
    public void getTokenValidSecs()
    {
        //arrange
        long newTokenValidSecs = 5000L;
        IotHubSasTokenAuthenticationProvider sasAuth = new mockIotHubSasTokenAuthenticationImplementation();
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
        IotHubSasTokenAuthenticationProvider sasAuth = new mockIotHubSasTokenAuthenticationImplementation();

        //act
        sasAuth.setTokenValidSecs(newTokenValidSecs);

        //assert
        long actualTokenValidSecs = Deencapsulation.getField(sasAuth, "tokenValidSecs");
        assertEquals(newTokenValidSecs, actualTokenValidSecs);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired, this function shall return true.]
    @Test
    public void isRenewalNecessaryWithExpiredToken()
    {
        //arrange
        new Expectations()
        {
            {
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new mockIotHubSasTokenAuthenticationImplementation();
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
        new Expectations()
        {
            {
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new mockIotHubSasTokenAuthenticationImplementation();
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
        long expectedBaseExpiryTime = 3601;

        IotHubSasTokenAuthenticationProvider sasAuth = new mockIotHubSasTokenAuthenticationImplementation();

        //act
        long actualExpiryTime =  Deencapsulation.invoke(sasAuth, "getExpiryTimeInSeconds");

        //assert
        //expect that the actual expiry time is at least expectedBaseExpiryTime since it should be in the future
        assertTrue(actualExpiryTime > expectedBaseExpiryTime);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_015: [This function shall save the provided tokenValidSecs as the number of seconds that created sas tokens are valid for.]
    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_018: [This function shall save the provided timeBufferPercentage.]
    @Test
    public void constructorSavesArguments(@Mocked IotHubAuthenticationProvider mockedIotHubAuthenticationProvider)
    {
        //arrange
        long expectedTokenValidSecs = 1234;
        int expectedTimeBufferPercentage = 34;

        //act
        IotHubSasTokenAuthenticationProvider authenticationProvider = new mockIotHubSasTokenAuthenticationImplementation(expectedTokenValidSecs, expectedTimeBufferPercentage);

        //assert
        assertEquals(expectedTokenValidSecs, Deencapsulation.getField(authenticationProvider, "tokenValidSecs"));
        assertEquals(expectedTimeBufferPercentage, Deencapsulation.getField(authenticationProvider, "timeBufferPercentage"));
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_016: [If the provided tokenValidSecs is less than 1, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForTTLBelowOneSecond(@Mocked IotHubAuthenticationProvider mockedIotHubAuthenticationProvider)
    {
        //arrange
        long expectedTokenValidSecs = 0;
        int expectedTimeBufferPercentage = 34;

        //act
        new mockIotHubSasTokenAuthenticationImplementation(expectedTokenValidSecs, expectedTimeBufferPercentage);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the provided timeBufferPercentage is less than 1 or greater than 100, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForBufferBelowOnePercent(@Mocked IotHubAuthenticationProvider mockedIotHubAuthenticationProvider)
    {
        //arrange
        long expectedTokenValidSecs = 2;
        int expectedTimeBufferPercentage = 0;

        //act
        new mockIotHubSasTokenAuthenticationImplementation(expectedTokenValidSecs, expectedTimeBufferPercentage);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the provided timeBufferPercentage is less than 1 or greater than 100, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForBufferAboveOneHundred(@Mocked IotHubAuthenticationProvider mockedIotHubAuthenticationProvider)
    {
        //arrange
        long expectedTokenValidSecs = 2;
        int expectedTimeBufferPercentage = 101;

        //act
        new mockIotHubSasTokenAuthenticationImplementation(expectedTokenValidSecs, expectedTimeBufferPercentage);
    }

    //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_019: [This function shall return true if the saved token has lived for longer
    // than its buffered threshold.]
    @Test
    public void shouldRefreshTokenReturnsTrueIfBeyondBuffer()
    {
        //arrange
        IotHubSasTokenAuthenticationProvider authenticationProvider = new mockIotHubSasTokenAuthenticationImplementation(10, 1);

        //act
        boolean result = authenticationProvider.shouldRefreshToken(true);

        //assert
        assertTrue(result);
    }

    //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_020: [This function shall return false if the saved token has not lived for longer
    // than its buffered threshold.]
    @Test
    public void shouldRefreshTokenReturnsFalseIfNotBeyondBuffer(@Mocked final System mockSystem)
    {
        //arrange
        IotHubSasTokenAuthenticationProvider authenticationProvider = new mockIotHubSasTokenAuthenticationImplementation(100000, 100);

        //act
        boolean result = authenticationProvider.shouldRefreshToken(true);

        //assert
        assertFalse(result);
    }
}
