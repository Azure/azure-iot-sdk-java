// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.auth.*;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit tests for deviceClientConfig.
 * Methods: 96%
 * Lines: 83%
 * (Untested lines and method are the unused default constructor)
 */
public class DeviceClientConfigTest
{
    @Mocked
    IotHubSasTokenAuthenticationProvider mockSasTokenAuthentication;
    @Mocked
    IotHubSasTokenSoftwareAuthenticationProvider mockSasTokenSoftwareAuthentication;
    @Mocked
    IotHubSasTokenHardwareAuthenticationProvider mockSasTokenHardwareAuthentication;
    @Mocked
    IotHubX509AuthenticationProvider mockX509Authentication;
    @Mocked
    IotHubX509HardwareAuthenticationProvider mockX509HardwareAuthentication;
    @Mocked
    IotHubX509SoftwareAuthenticationProvider mockX509SoftwareAuthentication;
    @Mocked IotHubConnectionString mockIotHubConnectionString;
    @Mocked SecurityProvider mockSecurityProvider;
    @Mocked SecurityProviderX509 mockSecurityProviderX509;
    @Mocked SecurityProviderTpm mockSecurityProviderSAS;
    @Mocked SSLContext mockSSLContext;

    private static String expectedDeviceId = "deviceId";
    private static String expectedHostname = "hostname";

    // Tests_SRS_DEVICECLIENTCONFIG_11_002: [The function shall return the IoT Hub hostname given in the constructor.]
    @Test
    public void getIotHubHostnameReturnsIotHubHostname()
            throws URISyntaxException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
    {
        final String iotHubHostname = "test.iothubhostname";
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getHostName();
                result = iotHubHostname;
            }
        };

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);

        String testIotHubHostname = config.getIotHubHostname();

        final String expectedIotHubHostname = iotHubHostname;
        assertThat(testIotHubHostname, is(expectedIotHubHostname));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_007: [The function shall return the IoT Hub name given in the constructor,
    // where the IoT Hub name is embedded in the IoT Hub hostname as follows: [IoT Hub name].[valid HTML chars]+.]
    @Test
    public void getIotHubNameReturnsIotHubName() throws URISyntaxException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
    {
        final String expectedIotHubName = "test";
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getHubName();
                result = expectedIotHubName;
            }
        };

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);

        final String testIotHubName = config.getIotHubName();

        assertThat(testIotHubName, is(expectedIotHubName));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_003: [The function shall return the device ID given in the constructor.]
    @Test
    public void getDeviceIdReturnsDeviceId() throws URISyntaxException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getDeviceId();
                result = deviceId;
            }
        };

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);

        String testDeviceId = config.getDeviceId();

        final String expectedDeviceId = deviceId;
        assertThat(testDeviceId, is(expectedDeviceId));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_004: [If this is using Sas token authentication, the function shall return the device key given in the constructor.]
    @Test
    public void getDeviceKeyReturnsDeviceKey() throws URISyntaxException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
    {
        final String deviceKey = "test-devicekey";
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getSharedAccessKey();
                result = deviceKey;
            }
        };

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);

        String testDeviceKey = config.getIotHubConnectionString().getSharedAccessKey();

        final String expectedDeviceKey = deviceKey;
        assertThat(testDeviceKey, is(expectedDeviceKey));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_006: [The function shall set the message callback, with its associated context.]
    // Tests_SRS_DEVICECLIENTCONFIG_11_010: [The function shall return the current message callback.]
    @Test
    public void getAndSetMessageCallbackMatch(
            @Mocked final MessageCallback mockCallback)
            throws URISyntaxException, IOException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        Object context = new Object();
        config.setMessageCallback(mockCallback, context);
        MessageCallback testCallback = config.getDeviceTelemetryMessageCallback();

        final MessageCallback expectedCallback = mockCallback;
        assertThat(testCallback, is(expectedCallback));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_006: [The function shall set the message callback, with its associated context.]
    // Tests_SRS_DEVICECLIENTCONFIG_11_011: [The function shall return the current message context.]
    @Test
    public void getAndSetMessageCallbackContextsMatch(
            @Mocked final MessageCallback mockCallback)
            throws URISyntaxException, IOException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        Object context = new Object();
        config.setMessageCallback(mockCallback, context);
        Object testContext = config.getDeviceTelemetryMessageContext();

        final Object expectedContext = context;
        assertThat(testContext, is(expectedContext));
    }

    /*
    **Tests_SRS_DEVICECLIENTCONFIG_25_023: [**The function shall set the DEVICE_TWIN message callback.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_024: [**The function shall set the DEVICE_TWIN message context.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_025: [**The function shall return the current DEVICE_TWIN message callback.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_026: [**The function shall return the current DEVICE_TWIN message context.**] **
     */
    @Test
    public void getAndSetDeviceTwinMessageCallbackAndContextsMatch(
            @Mocked final MessageCallback mockCallback)
            throws URISyntaxException, IOException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        Object context = new Object();
        config.setDeviceTwinMessageCallback(mockCallback, context);
        Object testContext = config.getDeviceTwinMessageContext();

        final Object expectedContext = context;
        assertThat(testContext, is(expectedContext));
        assertEquals(config.getDeviceTwinMessageCallback(), mockCallback);
    }

    /*
    **Tests_SRS_DEVICECLIENTCONFIG_25_022: [**The function shall return the current DeviceMethod message context.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_023: [**The function shall set the DeviceTwin message callback.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_021: [**The function shall return the current DeviceMethod message callback.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_022: [**The function shall return the current DeviceMethod message context.**] **
     */
    @Test
    public void getAndSetDeviceMethodMessageCallbackAndContextsMatch(
            @Mocked final MessageCallback mockCallback)
            throws URISyntaxException, IOException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        Object context = new Object();
        config.setDeviceMethodsMessageCallback(mockCallback, context);
        Object testContext = config.getDeviceMethodsMessageContext();


        final Object expectedContext = context;
        assertThat(testContext, is(expectedContext));
        assertEquals(config.getDeviceMethodsMessageCallback(), mockCallback);
    }

    @Test
    public void getAndSetDeviceMethodAndTwinMessageCallbackAndContextsMatch(
            @Mocked final MessageCallback mockCallback)
            throws URISyntaxException, IOException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        Object dMContext = new Object();
        config.setDeviceMethodsMessageCallback(mockCallback, dMContext);
        Object testContextDM = config.getDeviceMethodsMessageContext();

        Object dTcontext = new Object();
        config.setDeviceTwinMessageCallback(mockCallback, dTcontext);
        Object testContextDT = config.getDeviceTwinMessageContext();

        final Object expectedDTContext = dTcontext;
        assertThat(testContextDT, is(expectedDTContext));
        assertEquals(config.getDeviceTwinMessageCallback(), mockCallback);


        final Object expectedDMContext = dMContext;
        assertThat(testContextDM, is(expectedDMContext));
        assertEquals(config.getDeviceMethodsMessageCallback(), mockCallback);
    }
    // Tests_SRS_DEVICECLIENTCONFIG_11_012: [The function shall return 240000ms.]
    @Test
    public void getReadTimeoutMillisReturnsConstant() throws URISyntaxException, IOException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        int testReadTimeoutMillis = config.getReadTimeoutMillis();

        final int expectedReadTimeoutMillis = 240000;
        assertThat(testReadTimeoutMillis, is(expectedReadTimeoutMillis));
    }

    //Tests_SRS_DEVICECLIENTCONFIG_25_038: [The function shall save useWebsocket.]
    @Test
    public void setWebsocketEnabledSets() throws URISyntaxException, IOException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        config.setUseWebsocket(true);
        assertTrue(config.isUseWebsocket());
    }

    //Tests_SRS_DEVICECLIENTCONFIG_25_037: [The function shall return the true if websocket is enabled, false otherwise.]
    @Test
    public void getWebsocketEnabledGets() throws URISyntaxException, IOException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        config.setUseWebsocket(true);
        assertTrue(config.isUseWebsocket());
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_013: [The function shall return 180s.]
    @Test
    public void getMessageLockTimeoutSecsReturnsConstant()
            throws URISyntaxException, IOException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        int testMessageLockTimeoutSecs = config.getMessageLockTimeoutSecs();

        final int expectedMessageLockTimeoutSecs = 180;
        assertThat(testMessageLockTimeoutSecs,
                is(expectedMessageLockTimeoutSecs));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_21_034: [If the provided `iotHubConnectionString` is null,
    // the constructor shall throw IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorFailsNullConnectionString()
    {
        DeviceClientConfig config = new DeviceClientConfig(null, DeviceClientConfig.AuthType.SAS_TOKEN);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_039: [This function shall return the type of authentication that the config is set up to use.]
    @Test
    public void getAuthenticationTypeWorks() throws IOException
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);

        DeviceClientConfig.AuthType expectedAuthType = DeviceClientConfig.AuthType.X509_CERTIFICATE;
        Deencapsulation.setField(config, "authenticationType", expectedAuthType);

        //act
        DeviceClientConfig.AuthType actualAuthType = config.getAuthenticationType();

        //assert
        assertEquals(expectedAuthType, actualAuthType);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_12_001: [The constructor shall set the authentication type to the given authType value.]

    //Tests_SRS_DEVICECLIENTCONFIG_34_046: [If the provided `iotHubConnectionString` does not use x509 authentication, it shall be saved to a new IotHubSasTokenAuthenticationProvider object and the authentication type of this shall be set to SASToken.]
    @Test
    public void constructorSetsAuthType(@Mocked final IotHubConnectionString mockIotHubConnectionString) throws URISyntaxException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = false;
            }
        };

        //act
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);

        //assert
        DeviceClientConfig.AuthType actualAuthType = Deencapsulation.getField(config, "authenticationType");
        assertEquals(DeviceClientConfig.AuthType.SAS_TOKEN, actualAuthType);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_069: [If the provided connection string is null or does not use x509 auth, and IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithNullConnStringThrows() throws IOException
    {
        //act
        new DeviceClientConfig(null, "", false, "", false);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_069: [If the provided connection string is null or does not use x509 auth, and IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithWrongAuthTypeConnStringThrows() throws IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = false;
            }
        };

        //act
        new DeviceClientConfig(mockIotHubConnectionString, "", false, "", false);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_12_002: [If the authentication type is X509 the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509AuthThrows(@Mocked final IotHubConnectionString mockIotHubConnectionString) throws IOException
    {
        //act
        new DeviceClientConfig(null, DeviceClientConfig.AuthType.X509_CERTIFICATE);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_076: [If the provided `iotHubConnectionString` uses x509 authentication, the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorForSasTokenRejectsX509ConnectionStrings()
    {
        //arrange
        new StrictExpectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = true;
            }
        };

        //act
        Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString, DeviceClientConfig.AuthType.X509_CERTIFICATE);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_077: [This function shall return the saved IotHubX509AuthenticationProvider object.]
    @Test
    public void getIotHubX509AuthenticationWorks()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = true;

                new IotHubX509SoftwareAuthenticationProvider("", false, "", false);
                result = mockX509SoftwareAuthentication;
            }
        };

        DeviceClientConfig config = new DeviceClientConfig(mockIotHubConnectionString, "", false, "", false);
        Deencapsulation.setField(config, "x509Authentication", mockX509Authentication);

        //act
        IotHubX509AuthenticationProvider auth = config.getX509Authentication();

        //assert
        assertEquals(mockX509Authentication, auth);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_078: [This function shall return the saved IotHubSasTokenAuthenticationProvider object.]
    @Test
    public void getIotHubSasTokenAuthenticationWorks()
    {
        //arrange

        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = false;
            }
        };
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        Deencapsulation.setField(config, "sasTokenAuthentication", mockSasTokenAuthentication);

        //act
        IotHubSasTokenAuthenticationProvider auth = config.getSasTokenAuthentication();

        //assert
        assertEquals(mockSasTokenAuthentication, auth);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_079: [This function shall return the saved IotHubConnectionString object.]
    public void getIotHubConnectionStringWorks()
    {
        //arrange

        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = false;
            }
        };
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString, DeviceClientConfig.AuthType.X509_CERTIFICATE);

        //act
        IotHubConnectionString actualConnString = config.getIotHubConnectionString();

        //assert
        assertEquals(mockIotHubConnectionString, actualConnString);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_080: [If the provided connectionString or security provider is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void securityProviderConstructorThrowsForNullConnectionString()
    {
        //act
        Deencapsulation.newInstance(DeviceClientConfig.class, new Class[] {IotHubConnectionString.class, SecurityProvider.class}, null, mockSecurityProvider);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_080: [If the provided connectionString or security provider is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void securityProviderConstructorThrowsForNullSecurityProvider()
    {
        //act
        Deencapsulation.newInstance(DeviceClientConfig.class, new Class[] {IotHubConnectionString.class, SecurityProvider.class}, mockIotHubConnectionString, null);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_082: [If the provided security provider is a SecurityProviderX509 instance, this function shall set its auth type to X509 and create its IotHubX509AuthenticationProvider instance using the security provider's ssl context.]
    @Test
    public void securityProviderConstructorWithX509Success() throws SecurityProviderException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockSecurityProviderX509.getSSLContext();
                result = mockSSLContext;

                new IotHubX509HardwareAuthenticationProvider(mockSecurityProviderX509);
                result = mockX509HardwareAuthentication;
            }
        };

        //act
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, new Class[] {IotHubConnectionString.class, SecurityProvider.class}, mockIotHubConnectionString, mockSecurityProviderX509);

        //assert
        DeviceClientConfig.AuthType actualAuthType = Deencapsulation.getField(config, "authenticationType");
        assertEquals(DeviceClientConfig.AuthType.X509_CERTIFICATE, actualAuthType);
        new Verifications()
        {
            {
                new IotHubX509HardwareAuthenticationProvider(mockSecurityProviderX509);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_083: [If the provided security provider is a SecurityProviderTpm instance, this function shall set its auth type to SAS and create its IotHubSasTokenAuthenticationProvider instance using the security provider.]
    @Test
    public void securityClientConstructorWithTPMSuccess() throws SecurityProviderException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockSecurityProviderSAS.getSSLContext();
                result = mockSSLContext;

                mockIotHubConnectionString.getHostName();
                result = expectedHostname;

                mockIotHubConnectionString.getDeviceId();
                result = expectedDeviceId;

                new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedDeviceId, mockSecurityProviderSAS);
                result = mockSasTokenHardwareAuthentication;
            }
        };

        //act
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, new Class[] {IotHubConnectionString.class, SecurityProvider.class}, mockIotHubConnectionString, mockSecurityProviderSAS);

        //assert
        DeviceClientConfig.AuthType actualAuthType = Deencapsulation.getField(config, "authenticationType");
        assertEquals(DeviceClientConfig.AuthType.SAS_TOKEN, actualAuthType);
        new Verifications()
        {
            {
                new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, expectedDeviceId, mockSecurityProviderSAS);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_084: [If the provided security provider is neither a SecurityProviderX509 instance nor a SecurityProviderTpm instance, this function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void securityProviderConstructorThrowsForUnknownSecurityProviderImplementation() throws SecurityProviderException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getHostName();
                result = expectedHostname;

                mockIotHubConnectionString.getDeviceId();
                result = expectedDeviceId;
            }
        };

        //act
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, new Class[] {IotHubConnectionString.class, SecurityProvider.class}, mockIotHubConnectionString, mockSecurityProvider);
    }
}
