/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.auth.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.Assert.*;

/**
 * Unit tests for IotHubSasTokenHardwareAuthenticationProvider.java
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubSasTokenHardwareAuthenticationProviderTest
{
    @Mocked SecurityProvider mockSecurityProvider;
    @Mocked SecurityProviderTpm mockSecurityProviderTpm;
    @Mocked IotHubSSLContext mockIotHubSSLContext;
    @Mocked SSLContext mockSSLContext;
    @Mocked IotHubSasToken mockSasToken;
    @Mocked URLEncoder mockURLEncoder;
    @Mocked Base64 mockBase64;

    private static final String encodingName = StandardCharsets.UTF_8.displayName();

    private static String expectedDeviceId = "deviceId";
    private static String expectedModuleId = "moduleId";
    private static String expectedHostname = "hostname";
    private static String expectedGatewayHostname = "gateway";
    private static String expectedSasToken = "sasToken";

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_033: [This constructor shall generate and save a sas token from the security provider with the default time to live.]
    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_034: [This constructor shall retrieve and save the ssl context from the security provider.]
    @Test
    public void securityProviderConstructorSavesNeededInfo() throws IOException, InvalidKeyException, SecurityProviderException
    {
        //arrange
        final String someToken = "someToken";
        final byte[] tokenBytes= someToken.getBytes();
        new Expectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = tokenBytes;

                Base64.encodeBase64Local((byte[]) any);
                result = tokenBytes;

                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                mockSecurityProviderTpm.getSSLContext();
                result = mockSSLContext;

                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {SSLContext.class}, mockSSLContext);
                result = mockIotHubSSLContext;
            }
        };

        //act
        IotHubSasTokenAuthenticationProvider sasTokenAuthentication = new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);

        //assert
        String actualHostname = sasTokenAuthentication.getHostname();
        String actualDeviceId = sasTokenAuthentication.getDeviceId();
        String actualModuleId = sasTokenAuthentication.getModuleId();
        SecurityProviderTpm actualSecurityProvider = Deencapsulation.getField(sasTokenAuthentication, "securityProvider");
        assertEquals(expectedHostname, actualHostname);
        assertEquals(expectedDeviceId, actualDeviceId);
        assertEquals(expectedModuleId, actualModuleId);
        assertEquals(mockSecurityProviderTpm, actualSecurityProvider);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_003: [If the provided security provider is not an instance of SecurityProviderTpm, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void securityProviderConstructorThrowsForInvalidSecurityProviderInstance() throws IOException, InvalidKeyException, SecurityProviderException
    {
        //act
        new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProvider);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_023: [If the security provider throws an exception while retrieving a sas token or ssl context from it, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void securityProviderConstructorThrowsIfRetrievingSSLContextFromSecurityProviderThrows() throws IOException, InvalidKeyException, SecurityProviderException
    {
        //arrange
        final String someToken = "someToken";
        final byte[] tokenBytes= someToken.getBytes();
        new NonStrictExpectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = tokenBytes;

                Base64.encodeBase64Local((byte[]) any);
                result = tokenBytes;

                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                mockSecurityProviderTpm.getSSLContext();
                result = new SecurityProviderException("");
            }
        };

        //act
        new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_035: [If the saved sas token has expired and there is a security provider, the saved sas token shall be refreshed with a new token from the security provider.]
    @Test
    public void getRenewedSasTokenAutoRenewsFromSecurityProvider() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, SecurityProviderException, InvalidKeyException, TransportException
    {
        //arrange
        final String someToken = "someToken";
        final byte[] tokenBytes= someToken.getBytes();

        //assert
        new Expectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                System.currentTimeMillis();
                result = 0;

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = tokenBytes;
                times = 2;

                Base64.encodeBase64Local((byte[]) any);
                result = tokenBytes;

                URLEncoder.encode(anyString, encodingName);
                result = someToken;
                
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);

        //act
        sasAuth.getRenewedSasToken(false, false);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_036: [If the saved sas token has not expired and there is a security provider, but the sas token should be proactively renewed, the saved sas token shall be refreshed with a new token from the security provider.]
    @Test
    public void getRenewedSasTokenProactivelyRenewsFromSecurityProvider() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, SecurityProviderException, InvalidKeyException, TransportException
    {
        //arrange
        final String someToken = "someToken";
        final byte[] tokenBytes= someToken.getBytes();

        //assert
        new Expectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                System.currentTimeMillis();
                result = 0;

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = tokenBytes;
                times = 2;

                Base64.encodeBase64Local((byte[]) any);
                result = tokenBytes;

                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);

        //act
        sasAuth.getRenewedSasToken(true, false);
    }

    @Test
    public void getRenewedSasTokenForciblyRenewsFromSecurityProvider() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, SecurityProviderException, InvalidKeyException, TransportException
    {
        //arrange
        final String someToken = "someToken";
        final byte[] tokenBytes= someToken.getBytes();

        //assert
        new Expectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                System.currentTimeMillis();
                result = 0;

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = tokenBytes;
                times = 2;

                Base64.encodeBase64Local((byte[]) any);
                result = tokenBytes;

                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;

                new IotHubSasToken(anyString, anyString, anyString, null, anyString, anyLong);
                result = mockSasToken;
                times = 2; //initial creation during constructor, then again during getRenewedSasToken
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);

        //act
        sasAuth.getRenewedSasToken(true, true);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_005: [This function shall return the saved sas token.]
    @Test
    public void getSasTokenReturnsSavedValue() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, InvalidKeyException, SecurityProviderException, TransportException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockSasToken.toString();
                result  = expectedSasToken;

                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, String.class, long.class}, anyString, anyString, anyString, anyString, anyString, anyLong);
                result = mockSasToken;

                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;

                URLEncoder.encode(anyString, encodingName);
                result = "some token scope";

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = "some token".getBytes();
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);

        //act
        String actualSasToken = sasAuth.getRenewedSasToken(true, false);

        //assert
        assertEquals(mockSasToken.toString(), actualSasToken);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_001: [This function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void setPathToCertificateThrows() throws IOException, InvalidKeyException, SecurityProviderException
    {
        //arrange
        securityProviderExpectations();
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);

        //act
        sasAuth.setPathToIotHubTrustedCert("any string");
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_002: [This function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void setCertificateThrows() throws IOException, InvalidKeyException, SecurityProviderException
    {
        //arrange
        securityProviderExpectations();
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);

        //act
        sasAuth.setIotHubTrustedCert("any string");
    }

    private void securityProviderExpectations() throws UnsupportedEncodingException, InvalidKeyException, SecurityProviderException
    {
        new NonStrictExpectations()
        {
            {
                mockSasToken.toString();
                result  = expectedSasToken;

                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, String.class, long.class}, anyString, anyString, anyString, anyString, anyString, anyLong);
                result = mockSasToken;

                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;

                URLEncoder.encode(anyString, encodingName);
                result = "some token scope";

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = "some token".getBytes();
            }
        };
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_008: [This function shall return the generated IotHubSSLContext.]
    @Test
    public void getSSLContextSuccess() throws IOException, InvalidKeyException, SecurityProviderException
    {
        //arrange
        final String someToken = "someToken";
        final byte[] tokenBytes= someToken.getBytes();
        new NonStrictExpectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = tokenBytes;

                Base64.encodeBase64Local((byte[]) any);
                result = tokenBytes;

                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                mockSecurityProviderTpm.getSSLContext();
                result = mockSSLContext;

                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {SSLContext.class}, mockSSLContext);
                result = mockIotHubSSLContext;

                mockSecurityProviderTpm.getSSLContext();
                result = mockSSLContext;

                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {SSLContext.class}, mockSSLContext);
                result = mockIotHubSSLContext;

                Deencapsulation.invoke(mockIotHubSSLContext, "getSSLContext");
                result = mockSSLContext;
            }
        };

        IotHubSasTokenAuthenticationProvider sasTokenAuthentication = new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);

        //act
        SSLContext actualSSLContext = sasTokenAuthentication.getSSLContext();

        //assert
        assertEquals(mockSSLContext, actualSSLContext);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_009: [If the token scope cannot be encoded, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void generateSasTokenSignatureFromSecurityProviderFailsToEncodeTokenScopeThrowsIOException() throws IOException, InvalidKeyException, SecurityProviderException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = null;
            }
        };

        //act
        new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_010: [If the call for the saved security provider to sign with identity returns null or empty bytes, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void generateSasTokenSignatureFromSecurityProviderFailsToSignWithIdentityAndReturnsNullThrowsIOException() throws IOException, InvalidKeyException, SecurityProviderException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = "some token";

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = null;
            }
        };

        //act
        new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_010: [If the call for the saved security provider to sign with identity returns null or empty bytes, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void generateSasTokenSignatureFromSecurityProviderFailsToSignWithIdentityAndReturnsEmptyBytesThrowsIOException() throws IOException, InvalidKeyException, SecurityProviderException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = "some token";

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = new byte[] {};
            }
        };

        //act
        new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_011: [When generating the sas token signature from the security provider, if an UnsupportedEncodingException or SecurityProviderException is thrown, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void generateSasTokenSignatureThrowsUnsupportedEncodingWrappedInIOException() throws IOException, InvalidKeyException, SecurityProviderException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = new UnsupportedEncodingException();
            }
        };

        //act
        new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_011: [When generating the sas token signature from the security provider, if an UnsupportedEncodingException or SecurityProviderException is thrown, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void generateSasTokenSignatureFromSecurityProviderThrowsDuringSignWithIdentityThrowsIOException() throws IOException, InvalidKeyException, SecurityProviderException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = "some token";

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = new SecurityProviderException("");
            }
        };

        //act
        new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_012: [This function shall return false.]
    @Test
    public void isRenewalNecessaryReturnsFalse() throws IOException, SecurityProviderException
    {
        //arrange
        final String someToken = "someToken";
        final byte[] tokenBytes= someToken.getBytes();
        new NonStrictExpectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = tokenBytes;

                Base64.encodeBase64Local((byte[]) any);
                result = tokenBytes;

                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                mockSecurityProviderTpm.getSSLContext();
                result = mockSSLContext;

                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {SSLContext.class}, mockSSLContext);
                result = mockIotHubSSLContext;
            }
        };

        IotHubSasTokenAuthenticationProvider sasTokenAuthentication = new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);

        //act
        boolean result = sasTokenAuthentication.isRenewalNecessary();

        //assert
        assertFalse(result);
    }

    //Tests_SRS_IOTHUBSASTOKENHARDWAREAUTHENTICATION_34_013: [This function shall return true.]
    @Test
    public void canRefreshTokenReturnsTrue() throws IOException, SecurityProviderException
    {
        //arrange
        final String someToken = "someToken";
        final byte[] tokenBytes= someToken.getBytes();
        new NonStrictExpectations()
        {
            {
                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                mockSecurityProviderTpm.signWithIdentity((byte[]) any);
                result = tokenBytes;

                Base64.encodeBase64Local((byte[]) any);
                result = tokenBytes;

                URLEncoder.encode(anyString, encodingName);
                result = someToken;

                mockSecurityProviderTpm.getSSLContext();
                result = mockSSLContext;

                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {SSLContext.class}, mockSSLContext);
                result = mockIotHubSSLContext;
            }
        };

        IotHubSasTokenAuthenticationProvider sasTokenAuthentication = new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedGatewayHostname, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);

        //act
        boolean result = sasTokenAuthentication.canRefreshToken();

        //assert
        assertTrue(result);
    }
}
