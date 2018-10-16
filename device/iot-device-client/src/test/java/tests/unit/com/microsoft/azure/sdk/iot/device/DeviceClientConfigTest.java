// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.auth.*;
import com.microsoft.azure.sdk.iot.device.transport.ExponentialBackoffWithJitter;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit tests for deviceClientConfig.
 * Methods: 97%
 * Lines: 91%
 * (Untested lines and method are the unused default constructor)
 */
public class DeviceClientConfigTest
{
    @Mocked
    IotHubAuthenticationProvider mockedIotHubAuthenticationProvider;
    @Mocked
    IotHubSasTokenAuthenticationProvider mockSasTokenAuthentication;
    @Mocked
    IotHubSasTokenSoftwareAuthenticationProvider mockSasTokenSoftwareAuthentication;
    @Mocked
    IotHubSasTokenHardwareAuthenticationProvider mockSasTokenHardwareAuthentication;
    @Mocked
    IotHubAuthenticationProvider mockAuthentication;
    @Mocked
    IotHubX509HardwareAuthenticationProvider mockX509HardwareAuthentication;
    @Mocked
    IotHubX509SoftwareAuthenticationProvider mockX509SoftwareAuthentication;
    @Mocked IotHubConnectionString mockIotHubConnectionString;
    @Mocked SecurityProvider mockSecurityProvider;
    @Mocked SecurityProviderX509 mockSecurityProviderX509;
    @Mocked SecurityProviderTpm mockSecurityProviderSAS;
    @Mocked SSLContext mockSSLContext;
    @Mocked RetryPolicy mockRetryPolicy;
    @Mocked ProductInfo mockedProductInfo;
    @Mocked MessageCallback mockedMessageCallback;

    private static String expectedDeviceId = "deviceId";
    private static String expectedModuleId = "moduleId";
    private static String expectedHostname = "hostname";

    // Tests_SRS_DEVICECLIENTCONFIG_11_002: [The function shall return the IoT Hub hostname given in the constructor.]
    @Test
    public void getIotHubHostnameReturnsIotHubHostname()
    {
        final String iotHubHostname = "test.iothubhostname";
        new NonStrictExpectations()
        {
            {
                mockSasTokenSoftwareAuthentication.getHostname();
                result = iotHubHostname;
            }
        };

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockSasTokenSoftwareAuthentication);

        String testIotHubHostname = config.getIotHubHostname();

        final String expectedIotHubHostname = iotHubHostname;
        assertThat(testIotHubHostname, is(expectedIotHubHostname));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_057: [The function shall return the gateway hostname, or null if this connection string does not contain a gateway hostname.]
    @Test
    public void getGatewayHostnameReturnsGatewayHostname()
    {
        final String iotHubHostname = "test.iothubhostname";
        new NonStrictExpectations()
        {
            {
                mockSasTokenSoftwareAuthentication.getGatewayHostname();
                result = iotHubHostname;
            }
        };

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockSasTokenSoftwareAuthentication);

        //act
        String testIotHubHostname = config.getGatewayHostname();

        //assert
        final String expectedIotHubHostname = iotHubHostname;
        assertThat(testIotHubHostname, is(expectedIotHubHostname));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_007: [The function shall return the IoT Hub name given in the constructor,
    // where the IoT Hub name is embedded in the IoT Hub hostname as follows: [IoT Hub name].[valid HTML chars]+.]
    @Test
    public void getIotHubNameReturnsIotHubName()
    {
        final String expectedIotHubName = "test";
        new NonStrictExpectations()
        {
            {
                mockSasTokenSoftwareAuthentication.getHostname();
                result = expectedHostname;

                Deencapsulation.invoke(IotHubConnectionString.class, "parseHubName", expectedHostname);
                result = expectedIotHubName;
            }
        };

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockSasTokenSoftwareAuthentication);

        final String testIotHubName = config.getIotHubName();

        assertThat(testIotHubName, is(expectedIotHubName));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_003: [The function shall return the device ID given in the constructor.]
    @Test
    public void getDeviceIdReturnsDeviceId()
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        new NonStrictExpectations()
        {
            {
                new IotHubSasTokenSoftwareAuthenticationProvider(anyString, anyString, anyString, anyString, anyString, anyString);
                result = mockSasTokenSoftwareAuthentication;

                mockSasTokenSoftwareAuthentication.getDeviceId();
                result = deviceId;
            }
        };

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);

        String testDeviceId = config.getDeviceId();

        final String expectedDeviceId = deviceId;
        assertThat(testDeviceId, is(expectedDeviceId));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_047: [This function shall return the saved protocol.]
    @Test
    public void getProtocolReturnsProtocol()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
        Deencapsulation.invoke(config, "setProtocol", IotHubClientProtocol.AMQPS);

        //act
        IotHubClientProtocol actualProtocol = config.getProtocol();

        //assert
        assertThat(actualProtocol, is(IotHubClientProtocol.AMQPS));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_048: [This function shall save the provided protocol.]
    @Test
    public void setProtocolSetsProtocol()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);

        //act
        Deencapsulation.invoke(config, "setProtocol", IotHubClientProtocol.AMQPS);

        //assert
        IotHubClientProtocol savedProtocol = Deencapsulation.getField(config, "protocol");
        assertEquals(IotHubClientProtocol.AMQPS, savedProtocol);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_049: [This function return the saved authentication provider.]
    @Test
    public void getAuthenticationProviderGetsAuthenticationProvider()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
        Deencapsulation.setField(config, "authenticationProvider", mockedIotHubAuthenticationProvider);

        //act
        IotHubAuthenticationProvider iotHubAuthenticationProvider = config.getAuthenticationProvider();

        //assert
        assertEquals(mockedIotHubAuthenticationProvider, iotHubAuthenticationProvider);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_050: [This function return the saved moduleId.]
    @Test
    public void getModuleIdGetsModuleId()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
        Deencapsulation.setField(config, "authenticationProvider", mockedIotHubAuthenticationProvider);
        new NonStrictExpectations()
        {
            {
                mockedIotHubAuthenticationProvider.getModuleId();
                result = expectedModuleId;
            }
        };

        //act
        String actualModuleId = config.getModuleId();

        //assert
        assertEquals(expectedModuleId, actualModuleId);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_051: [If the saved authentication provider uses sas tokens, this function return AuthType.SAS_TOKEN.]
    @Test
    public void getAuthTypeReturnsSasTokenAuth()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
        Deencapsulation.setField(config, "authenticationProvider", mockSasTokenSoftwareAuthentication);

        //act
        DeviceClientConfig.AuthType actualAuthType = config.getAuthenticationType();

        //assert
        assertEquals(DeviceClientConfig.AuthType.SAS_TOKEN, actualAuthType);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_052: [If the saved authentication provider uses x509, this function return AuthType.X509_CERTIFICATE.]
    @Test
    public void getAuthTypeReturnsX509Auth()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
        Deencapsulation.setField(config, "authenticationProvider", mockAuthentication);

        //act
        DeviceClientConfig.AuthType actualAuthType = config.getAuthenticationType();

        //assert
        assertEquals(DeviceClientConfig.AuthType.X509_CERTIFICATE, actualAuthType);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_055: [If the saved authentication provider uses sas tokens, this function return the saved authentication provider.]
    @Test
    public void getSasAuthProviderReturnsSasAuth()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
        Deencapsulation.setField(config, "authenticationProvider", mockSasTokenSoftwareAuthentication);

        //act
        IotHubAuthenticationProvider actualAuthProvider = config.getSasTokenAuthentication();

        //assert
        assertEquals(mockSasTokenSoftwareAuthentication, actualAuthProvider);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_056: [If the saved authentication provider doesn't use sas tokens, this function return null.]
    @Test
    public void getSasAuthProviderReturnsNull()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
        Deencapsulation.setField(config, "authenticationProvider", mockAuthentication);

        //act
        IotHubAuthenticationProvider actualAuthProvider = config.getSasTokenAuthentication();

        //assert
        assertNull(actualAuthProvider);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_006: [The function shall set the message callback, with its associated context.]
    // Tests_SRS_DEVICECLIENTCONFIG_11_010: [If the inputName is null, or the message callbacks map does not contain the provided inputName, this function shall return the default message callback.]
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

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString);
        Object context = new Object();
        config.setMessageCallback(mockCallback, context);
        MessageCallback testCallback = config.getDeviceTelemetryMessageCallback(null);

        final MessageCallback expectedCallback = mockCallback;
        assertThat(testCallback, is(expectedCallback));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_006: [The function shall set the message callback, with its associated context.]
    // Tests_SRS_DEVICECLIENTCONFIG_11_011: [If the inputName is null, or the message callbacks map does not contain the provided inputName, this function shall return the default message callback context.]
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

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString);
        Object context = new Object();
        config.setMessageCallback(mockCallback, context);
        Object testContext = config.getDeviceTelemetryMessageContext(null);

        final Object expectedContext = context;
        assertThat(testContext, is(expectedContext));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_045: [If the message callbacks map contains the provided inputName, this function
    // shall return the callback associated with that inputName.]
    @Test
    public void getMessageCallbackWithSavedInput(@Mocked final MessageCallback mockCallback)
            throws URISyntaxException, IOException
    {
        //arrange
        String inputName = "someValidInputName";
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

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString);
        Object context = new Object();
        config.setMessageCallback(inputName, mockedMessageCallback, context);

        //act
        MessageCallback actualMessageCallback = config.getDeviceTelemetryMessageCallback(inputName);

        //assert
        assertEquals(mockedMessageCallback, actualMessageCallback);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_046: [If the message callbacks map contains the provided inputName, this function
    // shall return the context associated with that inputName.]
    @Test
    public void getMessageCallbackContextWithSavedInput(
            @Mocked final MessageCallback mockCallback)
            throws URISyntaxException, IOException
    {
        //arrange
        String inputName = "someValidInputName";
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

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString);
        Object context = new Object();
        config.setMessageCallback(inputName, mockCallback, context);

        //act
        Object testContext = config.getDeviceTelemetryMessageContext(inputName);

        //assert
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

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString);
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

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString);
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

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString);
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

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString);
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

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString);
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

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString);
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

        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, iotHubConnectionString);
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
        DeviceClientConfig config = new DeviceClientConfig((IotHubConnectionString) null);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_039: [This function shall return the type of authentication that the config is set up to use.]
    @Test
    public void getAuthenticationTypeWorks() throws IOException
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockSasTokenSoftwareAuthentication);

        DeviceClientConfig.AuthType expectedAuthType = DeviceClientConfig.AuthType.SAS_TOKEN;

        //act
        DeviceClientConfig.AuthType actualAuthType = config.getAuthenticationType();

        //assert
        assertEquals(expectedAuthType, actualAuthType);
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
        new DeviceClientConfig((IotHubConnectionString) null);
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
        Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
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
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
        Deencapsulation.setField(config, "authenticationProvider", mockSasTokenAuthentication);

        //act
        IotHubSasTokenAuthenticationProvider auth = config.getSasTokenAuthentication();

        //assert
        assertEquals(mockSasTokenAuthentication, auth);
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

                mockIotHubConnectionString.getHostName();
                result = "hostname";

                mockIotHubConnectionString.getGatewayHostName();
                result = "gatewayHostname";

                mockIotHubConnectionString.getDeviceId();
                result = "deviceId";

                mockIotHubConnectionString.getModuleId();
                result = "moduleId";
            }
        };

        //act
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, new Class[] {IotHubConnectionString.class, SecurityProvider.class}, mockIotHubConnectionString, mockSecurityProviderX509);

        //assert
        new Verifications()
        {
            {
                new IotHubX509HardwareAuthenticationProvider("hostname", "gatewayHostname", "deviceId", "moduleId", mockSecurityProviderX509);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_083: [If the provided security provider is a SecurityProviderTpm instance, this function shall set its auth type to SAS and create its IotHubSasTokenAuthenticationProvider instance using the security provider.]
    @Test
    public void securityClientConstructorWithTPMSuccess() throws SecurityProviderException, IOException
    {
        //arrange
        new Expectations()
        {
            {
                mockIotHubConnectionString.getHostName();
                result = expectedHostname;

                mockIotHubConnectionString.getDeviceId();
                result = expectedDeviceId;

                mockIotHubConnectionString.getModuleId();
                result = expectedModuleId;
            }
        };

        //act
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, new Class[] {IotHubConnectionString.class, SecurityProvider.class}, mockIotHubConnectionString, mockSecurityProviderSAS);

        //assert
        new Verifications()
        {
            {
                new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, null, expectedDeviceId, expectedModuleId, mockSecurityProviderSAS);
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

    //Tests_SRS_DEVICECLIENTCONFIG_28_001: [The class shall have ExponentialBackOff as the default retryPolicy.]
    @Test
    public void constructorWithExponentialBackOffAsDefaultPolicy()
    {
        //act
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);

        //assert
        assertEquals(Deencapsulation.getField(config, "retryPolicy").getClass(), new ExponentialBackoffWithJitter().getClass());
    }

    //Tests_SRS_DEVICECLIENTCONFIG_28_002: [This function shall throw IllegalArgumentException retryPolicy is null.]
    @Test (expected = IllegalArgumentException.class)
    public void setRetryPolicyThrowsIfNull()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);

        //act
        config.setRetryPolicy(null);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_28_003: [This function shall set retryPolicy.]
    @Test
    public void setRetryPolicySetPolicy()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);

        //act
        config.setRetryPolicy(mockRetryPolicy);

        //assert
        assertEquals(Deencapsulation.getField(config, "retryPolicy"), mockRetryPolicy);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_28_004: [This function shall return the saved RetryPolicy object.]
    @Test
    public void getRetryPolicyReturnsSavedPolicy()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
        Deencapsulation.setField(config, "retryPolicy", mockRetryPolicy);

        //act
        RetryPolicy actual = config.getRetryPolicy();

        //assert
        assertEquals(mockRetryPolicy, actual);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_030: [If the provided timeout is 0 or negative, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setOperationTimeoutThrowsForNegativeTimeout()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);

        //act
        Deencapsulation.invoke(config, "setOperationTimeout", new Class[] {long.class}, -1);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_030: [If the provided timeout is 0 or negative, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setOperationTimeoutThrowsForZeroTimeout()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);

        //act
        Deencapsulation.invoke(config, "setOperationTimeout", new Class[] {long.class}, 0);
    }


    //Tests_SRS_DEVICECLIENTCONFIG_34_031: [This function shall save the provided operation timeout.]
    @Test
    public void setOperationTimeoutSavesTimeout()
    {
        //arrange
        final long expectedOperationTimeout = 1234;
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);

        //act
        Deencapsulation.invoke(config, "setOperationTimeout", new Class[] {long.class}, expectedOperationTimeout);

        //assert
        long actualTimeout = Deencapsulation.getField(config, "operationTimeout");
        assertEquals(expectedOperationTimeout, actualTimeout);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_032: [This function shall return the saved operation timeout.]
    @Test
    public void getDeviceOperationTimeoutReturnsTimeout()
    {
        //arrange
        final long expectedOperationTimeout = 1234;
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
        Deencapsulation.setField(config, "operationTimeout", expectedOperationTimeout);

        //act
        long actual = config.getOperationTimeout();

        //assert
        assertEquals(expectedOperationTimeout, actual);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_040: [This function shall return the saved product info.]
    @Test
    public void getProductInfoReturnsSavedProductInfo()
    {
        //arrange
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
        Deencapsulation.setField(config, "productInfo", mockedProductInfo);

        //act
        ProductInfo actualProductInfo = config.getProductInfo();

        //assert
        assertEquals(mockedProductInfo, actualProductInfo);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_041: [This function shall save a new default product info.]
    @Test
    public void ConstructorSavesNewProductInfo()
    {
        //act
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);

        //assert
        assertNotNull(Deencapsulation.getField(config, "productInfo"));
        new Verifications()
        {
            {
                new ProductInfo();
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_042: [This function shall save a new default product info.]
    @Test
    public void ConstructorX509SavesNewProductInfo()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = true;
            }
        };

        //act
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString, "", true, "", true);

        //assert
        assertNotNull(Deencapsulation.getField(config, "productInfo"));
        new Verifications()
        {
            {
                new ProductInfo();
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_043: [This function shall save a new default product info.]
    @Test
    public void ConstructorSecurityProviderSavesNewProductInfo()
    {
        //act
        DeviceClientConfig config = Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString, mockSecurityProviderSAS);

        //assert
        assertNotNull(Deencapsulation.getField(config, "productInfo"));
        new Verifications()
        {
            {
                new ProductInfo();
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_044: [The function shall map the provided inputName to the callback and context in the saved inputChannelMessageCallbacks map.]
    @Test
    public void setMessageCallbackWithInput()
    {
        //arrange
        final String inputName = "someInputName";
        final Object context = new Object();
        DeviceClientConfig config = new DeviceClientConfig(mockIotHubConnectionString);

        //act
        config.setMessageCallback(inputName, mockedMessageCallback, context);

        //assert
        Map<String, Pair<MessageCallback, Object>> actualMap = Deencapsulation.getField(config, "inputChannelMessageCallbacks");
        assertNotNull(actualMap);
        assertTrue(actualMap.containsKey(inputName));
        assertTrue(actualMap.get(inputName).getKey().equals(mockedMessageCallback));
        assertTrue(actualMap.get(inputName).getValue().equals(context));
    }
}
