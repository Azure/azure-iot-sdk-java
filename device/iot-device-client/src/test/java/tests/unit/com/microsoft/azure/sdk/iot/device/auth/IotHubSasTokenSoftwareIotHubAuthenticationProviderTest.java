/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.auth.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenSoftwareAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for IotHubSasTokenSoftwareAuthenticationProvider.java
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubSasTokenSoftwareIotHubAuthenticationProviderTest
{
    private static String expectedDeviceId = "deviceId";
    private static String expectedHostname = "hostname";
    private static String expectedGatewayHostname = "gateway";
    private static String expectedModuleId = "moduleId";
    private static String expectedDeviceKey = "deviceKey";
    private static String expectedSasToken = "sasToken";
    private static long expectedExpiryTime = 3601;
    private static long expectedBufferPercent = 20;

    @Mocked IotHubSasToken mockSasToken;
    @Mocked IotHubSSLContext mockIotHubSSLContext;
    @Mocked SSLContext mockSSLContext;

    //Tests_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_002: [This constructor shall save the provided hostname, device id, module id, deviceKey, and sharedAccessToken.]
    @Test
    public void constructorSavesArguments()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, String.class, long.class}, anyString, anyString, anyString, anyString, anyString, anyLong);
                result = mockSasToken;

                mockSasToken.toString();
                result = expectedSasToken;
            }
        };

        //act
        IotHubSasTokenSoftwareAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedDeviceKey, expectedSasToken);

        //assert
        String actualDeviceId = sasAuth.getDeviceId();
        String actualModuleId = sasAuth.getModuleId();
        String actualHostName = sasAuth.getHostname();
        String actualDeviceKey = Deencapsulation.getField(sasAuth, "deviceKey");
        IotHubSasToken actualSasToken = Deencapsulation.getField(sasAuth, "sasToken");

        assertEquals(expectedDeviceId, actualDeviceId);
        assertEquals(expectedModuleId, actualModuleId);
        assertEquals(expectedHostname, actualHostName);
        assertEquals(expectedDeviceKey, actualDeviceKey);
        assertEquals(expectedSasToken, actualSasToken.toString());
    }

    //Tests_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_003: [This constructor shall save the provided hostname, device id, module id, deviceKey, and sharedAccessToken.]
    @Test
    public void overloadedConstructorSavesArguments()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, String.class, long.class}, anyString, anyString, anyString, anyString, anyString, anyLong);
                result = mockSasToken;

                mockSasToken.toString();
                result = expectedSasToken;
            }
        };

        //act
        IotHubSasTokenSoftwareAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedDeviceKey, expectedSasToken, 10, 10);

        //assert
        String actualDeviceId = sasAuth.getDeviceId();
        String actualModuleId = sasAuth.getModuleId();
        String actualHostName = sasAuth.getHostname();
        String actualDeviceKey = Deencapsulation.getField(sasAuth, "deviceKey");
        IotHubSasToken actualSasToken = Deencapsulation.getField(sasAuth, "sasToken");

        assertEquals(expectedDeviceId, actualDeviceId);
        assertEquals(expectedModuleId, actualModuleId);
        assertEquals(expectedHostname, actualHostName);
        assertEquals(expectedDeviceKey, actualDeviceKey);
        assertEquals(expectedSasToken, actualSasToken.toString());
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_004: [If the saved sas token has expired and there is a device key present, the saved sas token shall be renewed.]
    @Test
    public void getRenewedSasTokenAutoRenews(@Mocked final System mockSystem) throws IOException, TransportException
    {
        //assert
        new Expectations()
        {
            {
                System.currentTimeMillis();
                result = 0;
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
                System.currentTimeMillis();
                result = 0;
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, String.class, long.class}, expectedHostname, expectedDeviceId, expectedDeviceKey, null, expectedModuleId, expectedExpiryTime);
                result = mockSasToken;
            }
        };

        //arrange
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedDeviceKey, expectedSasToken);

        //act
        sasAuth.getRenewedSasToken(false, false);

    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_006: [If the saved sas token has not expired and there is a device key present, but this method is called to proactively renew and the token should renew, the saved sas token shall be renewed.]
    @Test
    public void getRenewedSasTokenProactivelyRenews(@Mocked final System mockSystem) throws IOException, TransportException
    {
        new MockUp<IotHubSasTokenAuthenticationProvider>()
        {
            @Mock public boolean shouldRefreshToken(boolean proactivelyRenew)
            {
                return true;
            }
        };

        //assert
        new Expectations()
        {
            {
                System.currentTimeMillis();
                result = 0;
                System.currentTimeMillis();
                result = 0;
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, String.class, long.class}, expectedHostname, expectedDeviceId, expectedDeviceKey, null, expectedModuleId, expectedExpiryTime);
                result = mockSasToken;
            }
        };

        //arrange
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedDeviceKey, expectedSasToken);

        //act
        sasAuth.getRenewedSasToken(true, false);
    }

    @Test
    public void getRenewedSasTokenForciblyRenews(@Mocked final System mockSystem) throws IOException, TransportException
    {
        new MockUp<IotHubSasTokenAuthenticationProvider>()
        {
            @Mock public boolean shouldRefreshToken(boolean proactivelyRenew)
            {
                return false;
            }
        };

        //assert
        new Expectations()
        {
            {
                System.currentTimeMillis();
                result = 0;
                times = 2;
                new IotHubSasToken(expectedHostname, expectedDeviceId, expectedDeviceKey, null, expectedModuleId, anyLong);
                result = mockSasToken;
                times = 2;
            }
        };

        //arrange
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedDeviceKey, expectedSasToken);

        //act
        sasAuth.getRenewedSasToken(true, true);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_006: [If the saved sas token has not expired and there is a device key present, but this method is called to proactively renew and the token should renew, the saved sas token shall be renewed.]
    @Test
    public void getRenewedSasTokenDoesntProactivelyRenewIfShouldntRefreshToken(@Mocked final System mockSystem) throws IOException, TransportException
    {
        new MockUp<IotHubSasTokenAuthenticationProvider>()
        {
            @Mock public boolean shouldRefreshToken(boolean proactivelyRenew)
            {
                return false;
            }
        };

        //assert
        new Expectations()
        {
            {
                System.currentTimeMillis();
                result = 0;
                System.currentTimeMillis();
                result = 0;
            }
        };

        //arrange
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedDeviceKey, expectedSasToken);

        //act
        sasAuth.getRenewedSasToken(true, false);
    }

    //Tests_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_005: [This function shall return the saved sas token.]
    @Test
    public void getSasTokenReturnsSavedValue() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new Expectations()
        {
            {
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, String.class, long.class}, anyString, anyString, anyString, anyString, anyString, anyLong);
                result = mockSasToken;
                mockSasToken.toString();
                result = "some token";
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedDeviceKey, expectedSasToken);

        //act
        String actualSasToken = Deencapsulation.invoke(sasAuth, "getRenewedSasToken", false, false);

        //assert
        assertEquals(mockSasToken.toString(), actualSasToken);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired and cannot be renewed, this function shall return true.]
    @Test
    public void isRenewalNecessaryReturnsTrueWhenTokenHasExpiredAndNoDeviceKeyIsPresent() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, null, expectedSasToken);

        //act
        boolean needsToRenew = Deencapsulation.invoke(sasAuth, "isRenewalNecessary");

        //assert
        assertTrue(needsToRenew);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired and cannot be renewed, this function shall return true.]
    @Test
    public void isRenewalNecessaryReturnsFalseDeviceKeyPresent() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new StrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, String.class, long.class}, anyString, anyString, anyString, anyString, anyString, anyLong);
                result = mockSasToken;
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedDeviceKey, expectedSasToken);

        //act
        boolean needsToRenew = Deencapsulation.invoke(sasAuth, "isRenewalNecessary");

        //assert
        assertFalse(needsToRenew);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired and cannot be renewed, this function shall return true.]
    @Test
    public void isRenewalNecessaryReturnsFalseWhenTokenHasNotExpired() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedDeviceKey, expectedSasToken);

        //act
        boolean needsToRenew = Deencapsulation.invoke(sasAuth, "isRenewalNecessary");

        //assert
        assertFalse(needsToRenew);
    }

    // Tests_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_018: [This function shall return true if a deviceKey is present and if super.isRenewalNecessary returns true.]
    @Test
    public void isRenewalNecessaryReturnsTrueIfDeviceKeyPresent()
    {
        //arrange
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedDeviceKey, expectedSasToken);

        //act
        boolean result = sasAuth.isRenewalNecessary();

        //assert
        assertFalse(result);
    }

    // Tests_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_017: [This function shall return true if a deviceKey is present.]
    @Test
    public void canRefreshTokenReturnsTrueIfDeviceKeyPresent()
    {
        //arrange
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, expectedDeviceKey, expectedSasToken);

        //act
        boolean result = sasAuth.canRefreshToken();

        //assert
        assertTrue(result);
    }

    // Tests_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_017: [This function shall return true if a deviceKey is present.]
    @Test
    public void canRefreshTokenReturnsFalseIfDeviceKeyAbsent()
    {
        //arrange
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, null, expectedSasToken);

        //act
        boolean result = sasAuth.canRefreshToken();

        //assert
        assertFalse(result);
    }

}
