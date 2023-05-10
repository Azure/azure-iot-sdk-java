// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.twin.Pair;
import com.microsoft.azure.sdk.iot.device.auth.*;
import com.microsoft.azure.sdk.iot.device.transport.ExponentialBackoffWithJitter;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit tests for deviceClientConfig.
 * Methods: 97%
 * Lines: 91%
 * (Untested lines and method are the unused default constructor)
 */
public class ClientConfigurationTest
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
    @Mocked
    IotHubConnectionString mockIotHubConnectionString;
    @Mocked
    SecurityProvider mockSecurityProvider;
    @Mocked
    SecurityProviderX509 mockSecurityProviderX509;
    @Mocked
    SecurityProviderTpm mockSecurityProviderTpm;
    @Mocked
    SecurityProviderSymmetricKey mockSecurityProviderSymKey;
    @Mocked
    SSLContext mockSSLContext;
    @Mocked
    RetryPolicy mockRetryPolicy;
    @Mocked
    ProductInfo mockedProductInfo;
    @Mocked
    MessageCallback mockedMessageCallback;

    private static final String expectedDeviceId = "deviceId";
    private static final String expectedModuleId = "moduleId";
    private static final String expectedHostname = "hostname";

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

        ClientConfiguration config = new ClientConfiguration(mockSasTokenSoftwareAuthentication, IotHubClientProtocol.AMQPS);

        String testIotHubHostname = config.getIotHubHostname();

        assertThat(testIotHubHostname, is(iotHubHostname));
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

        ClientConfiguration config = new ClientConfiguration(mockSasTokenSoftwareAuthentication, IotHubClientProtocol.AMQPS);

        //act
        String testIotHubHostname = config.getGatewayHostname();

        //assert
        assertThat(testIotHubHostname, is(iotHubHostname));
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

        ClientConfiguration config = new ClientConfiguration(mockSasTokenSoftwareAuthentication, IotHubClientProtocol.AMQPS);

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

        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);

        String testDeviceId = config.getDeviceId();

        assertThat(testDeviceId, is(deviceId));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_047: [This function shall return the saved protocol.]
    @Test
    public void getProtocolReturnsProtocol()
    {
        //arrange
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);

        //act
        IotHubClientProtocol actualProtocol = config.getProtocol();

        //assert
        assertThat(actualProtocol, is(IotHubClientProtocol.AMQPS));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_049: [This function return the saved authentication provider.]
    @Test
    public void getAuthenticationProviderGetsAuthenticationProvider()
    {
        //arrange
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
        Deencapsulation.setField(config, "authenticationProvider", mockSasTokenSoftwareAuthentication);

        //act
        ClientConfiguration.AuthType actualAuthType = config.getAuthenticationType();

        //assert
        assertEquals(ClientConfiguration.AuthType.SAS_TOKEN, actualAuthType);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_052: [If the saved authentication provider uses x509, this function return AuthType.X509_CERTIFICATE.]
    @Test
    public void getAuthTypeReturnsX509Auth()
    {
        //arrange
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
        Deencapsulation.setField(config, "authenticationProvider", mockAuthentication);

        //act
        ClientConfiguration.AuthType actualAuthType = config.getAuthenticationType();

        //assert
        assertEquals(ClientConfiguration.AuthType.X509_CERTIFICATE, actualAuthType);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_055: [If the saved authentication provider uses sas tokens, this function return the saved authentication provider.]
    @Test
    public void getSasAuthProviderReturnsSasAuth()
    {
        //arrange
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
        Object context = new Object();
        config.setMessageCallback(mockCallback, context);
        MessageCallback testCallback = config.getDeviceTelemetryMessageCallback(null);

        assertThat(testCallback, is(mockCallback));
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
        Object context = new Object();
        config.setMessageCallback(mockCallback, context);
        Object testContext = config.getDeviceTelemetryMessageContext(null);

        assertThat(testContext, is(context));
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
        Object context = new Object();
        config.setMessageCallback(inputName, mockCallback, context);

        //act
        Object testContext = config.getDeviceTelemetryMessageContext(inputName);

        //assert
        assertThat(testContext, is(context));
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
        Object context = new Object();
        config.setDeviceTwinMessageCallback(mockCallback, context);
        Object testContext = config.getDeviceTwinMessageContext();

        assertThat(testContext, is(context));
        assertEquals(config.getDeviceTwinMessageCallback(), mockCallback);
    }

    /*
    **Tests_SRS_DEVICECLIENTCONFIG_25_022: [**The function shall return the current DirectMethod message context.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_023: [**The function shall set the DeviceTwin message callback.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_021: [**The function shall return the current DirectMethod message callback.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_022: [**The function shall return the current DirectMethod message context.**] **
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
        Object context = new Object();
        config.setDirectMethodsMessageCallback(mockCallback, context);
        Object testContext = config.getDirectMethodsMessageContext();


        assertThat(testContext, is(context));
        assertEquals(config.getDirectMethodsMessageCallback(), mockCallback);
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
        Object dMContext = new Object();
        config.setDirectMethodsMessageCallback(mockCallback, dMContext);
        Object testContextDM = config.getDirectMethodsMessageContext();

        Object dTcontext = new Object();
        config.setDeviceTwinMessageCallback(mockCallback, dTcontext);
        Object testContextDT = config.getDeviceTwinMessageContext();

        assertThat(testContextDT, is(dTcontext));
        assertEquals(config.getDeviceTwinMessageCallback(), mockCallback);


        assertThat(testContextDM, is(dMContext));
        assertEquals(config.getDirectMethodsMessageCallback(), mockCallback);
    }

    @Test
    public void getReadTimeoutMillis()
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
        int testReadTimeoutMillis = config.getHttpsReadTimeout();

        final int expectedDefaultReadTimeoutMillis = 240000;
        assertThat(testReadTimeoutMillis, is(expectedDefaultReadTimeoutMillis));

        int newValue = 444;
        config.setHttpsReadTimeout(newValue);

        assertThat(config.getHttpsReadTimeout(), is(newValue));
    }

    @Test
    public void getConnectTimeoutMillis()
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
        int testConnectTimeoutMillis = config.getHttpsConnectTimeout();

        final int expectedDefaultConnectTimeoutMillis = 0;
        assertThat(testConnectTimeoutMillis, is(expectedDefaultConnectTimeoutMillis));

        int newValue = 444;
        config.setHttpsConnectTimeout(newValue);

        assertThat(config.getHttpsConnectTimeout(), is(newValue));
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
        config.setUseWebsocket(true);
        assertTrue(config.isUsingWebsocket());
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
        config.setUseWebsocket(true);
        assertTrue(config.isUsingWebsocket());
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

        ClientConfiguration config = new ClientConfiguration(iotHubConnectionString, IotHubClientProtocol.AMQPS);
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
        ClientConfiguration config = new ClientConfiguration((IotHubConnectionString) null, IotHubClientProtocol.AMQPS);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_039: [This function shall return the type of authentication that the config is set up to use.]
    @Test
    public void getAuthenticationTypeWorks() throws IOException
    {
        //arrange
        ClientConfiguration config = new ClientConfiguration(mockSasTokenSoftwareAuthentication, IotHubClientProtocol.AMQPS);

        ClientConfiguration.AuthType expectedAuthType = ClientConfiguration.AuthType.SAS_TOKEN;

        //act
        ClientConfiguration.AuthType actualAuthType = config.getAuthenticationType();

        //assert
        assertEquals(expectedAuthType, actualAuthType);
    }

    @Test
    public void constructorWithSSLContextBuildsX509SoftwareAuthenticationProvider(@Mocked final SSLContext mockSSLContext)
    {
        final String hostname = "hostname";
        final String gatewayhostname = "gatewayhostname";
        final String deviceId = "deviceId";
        final String moduleId = "moduleId";
        new Expectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = true;

                mockIotHubConnectionString.getHostName();
                result = hostname;

                mockIotHubConnectionString.getGatewayHostName();
                result = gatewayhostname;

                mockIotHubConnectionString.getDeviceId();
                result = deviceId;

                mockIotHubConnectionString.getModuleId();
                result = moduleId;

                new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayhostname, deviceId, moduleId, mockSSLContext);
            }
        };

        new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS, mockSSLContext);
    }

    @Test
    public void constructorWithSSLContextBuildsSasSoftwareAuthenticationProvider(@Mocked final SSLContext mockSSLContext)
    {
        final String hostname = "hostname";
        final String gatewayhostname = "gatewayhostname";
        final String deviceId = "deviceId";
        final String moduleId = "moduleId";
        final String sharedAccessKey = "sharedAccessKey";
        final String sharedAccessToken = "sharedAccessToken";
        new Expectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = false;

                mockIotHubConnectionString.getHostName();
                result = hostname;

                mockIotHubConnectionString.getGatewayHostName();
                result = gatewayhostname;

                mockIotHubConnectionString.getDeviceId();
                result = deviceId;

                mockIotHubConnectionString.getModuleId();
                result = moduleId;

                mockIotHubConnectionString.getSharedAccessKey();
                result = sharedAccessKey;

                mockIotHubConnectionString.getSharedAccessToken();
                result = sharedAccessToken;

                new IotHubSasTokenSoftwareAuthenticationProvider(hostname, gatewayhostname, deviceId, moduleId, sharedAccessKey, sharedAccessToken, mockSSLContext);
            }
        };

        new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS, mockSSLContext);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_12_002: [If the authentication type is X509 the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509AuthThrows(@Mocked final IotHubConnectionString mockIotHubConnectionString) throws IOException
    {
        //act
        new ClientConfiguration((IotHubConnectionString) null, IotHubClientProtocol.AMQPS);
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
        new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
        Deencapsulation.setField(config, "authenticationProvider", mockSasTokenAuthentication);

        //act
        IotHubSasTokenAuthenticationProvider auth = config.getSasTokenAuthentication();

        //assert
        assertEquals(mockSasTokenAuthentication, auth);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_080: [If the provided connectionString or security provider is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void securityProviderConstructorThrowsForNullConnectionString() throws IOException
    {
        //act
        new ClientConfiguration(null, mockSecurityProvider, IotHubClientProtocol.AMQPS);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_080: [If the provided connectionString or security provider is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void securityProviderConstructorThrowsForNullSecurityProvider() throws IOException
    {
        //act
        new ClientConfiguration(mockIotHubConnectionString, null, IotHubClientProtocol.AMQPS);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_082: [If the provided security provider is a SecurityProviderX509 instance, this function shall set its auth type to X509 and create its IotHubX509AuthenticationProvider instance using the security provider's ssl context.]
    @Test
    public void securityProviderConstructorWithX509Success() throws SecurityProviderException, IOException
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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, mockSecurityProviderX509, IotHubClientProtocol.AMQPS);

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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, mockSecurityProviderTpm, IotHubClientProtocol.AMQPS);

        //assert
        new Verifications()
        {
            {
                new IotHubSasTokenHardwareAuthenticationProvider(expectedHostname, null, expectedDeviceId, expectedModuleId, mockSecurityProviderTpm);
                times = 1;
            }
        };
    }

    @Test
    public void securityClientConstructorWithSASSuccess() throws SecurityProviderException, IOException
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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, mockSecurityProviderSymKey, IotHubClientProtocol.AMQPS);

        //assert
        new Verifications()
        {
            {
                new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, null, expectedDeviceId, expectedModuleId, new String(mockSecurityProviderSymKey.getSymmetricKey(), StandardCharsets.UTF_8), null);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_084: [If the provided security provider is neither a SecurityProviderX509 instance nor a SecurityProviderTpm instance, this function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void securityProviderConstructorThrowsForUnknownSecurityProviderImplementation() throws SecurityProviderException, IOException
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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, mockSecurityProvider, IotHubClientProtocol.AMQPS);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_28_001: [The class shall have ExponentialBackOff as the default retryPolicy.]
    @Test
    public void constructorWithExponentialBackOffAsDefaultPolicy()
    {
        //act
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);

        //assert
        assertEquals(Deencapsulation.getField(config, "retryPolicy").getClass(), ExponentialBackoffWithJitter.class);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_28_002: [This function shall throw IllegalArgumentException retryPolicy is null.]
    @Test (expected = NullPointerException.class)
    public void setRetryPolicyThrowsIfNull()
    {
        //arrange
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);

        //act
        config.setRetryPolicy(null);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_28_003: [This function shall set retryPolicy.]
    @Test
    public void setRetryPolicySetPolicy()
    {
        //arrange
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);

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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);

        //act
        Deencapsulation.invoke(config, "setOperationTimeout", new Class[] {long.class}, -1);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_030: [If the provided timeout is 0 or negative, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setOperationTimeoutThrowsForZeroTimeout()
    {
        //arrange
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);

        //act
        Deencapsulation.invoke(config, "setOperationTimeout", new Class[] {long.class}, 0);
    }


    //Tests_SRS_DEVICECLIENTCONFIG_34_031: [This function shall save the provided operation timeout.]
    @Test
    public void setOperationTimeoutSavesTimeout()
    {
        //arrange
        final long expectedOperationTimeout = 1234;
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);

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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);

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
    public void ConstructorSecurityProviderSavesNewProductInfo() throws IOException
    {
        //act
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, mockSecurityProviderTpm, IotHubClientProtocol.AMQPS);

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
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);

        //act
        config.setMessageCallback(inputName, mockedMessageCallback, context);

        //assert
        Map<String, Pair<MessageCallback, Object>> actualMap = Deencapsulation.getField(config, "inputChannelMessageCallbacks");
        assertNotNull(actualMap);
        assertTrue(actualMap.containsKey(inputName));
        assertEquals(actualMap.get(inputName).getKey(), mockedMessageCallback);
        assertEquals(actualMap.get(inputName).getValue(), context);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_058: [If the provided inputName is already saved in the message callbacks map, and the provided callback is null, this function
    // shall remove the inputName from the message callbacks map.]
    @Test
    public void setMessageCallbackWithInputCanRemoveInput()
    {
        //arrange
        final String inputName = "someInputName";
        final Object context = new Object();
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
        config.setMessageCallback(inputName, mockedMessageCallback, context);

        //act
        config.setMessageCallback(inputName, null, null);

        //assert
        Map<String, Pair<MessageCallback, Object>> actualMap = Deencapsulation.getField(config, "inputChannelMessageCallbacks");
        assertNotNull(actualMap);
        assertFalse(actualMap.containsKey(inputName));
    }

    @Test
    public void setProxySettingsSets(@Mocked final ProxySettings mockedProxySettings)
    {
        //arrange
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);

        //act
        config.setProxySettings(mockedProxySettings);

        //assert
        ProxySettings savedProxySettings = Deencapsulation.getField(config, "proxySettings");
        assertEquals(mockedProxySettings, savedProxySettings);
    }

    @Test
    public void getProxyHostnameGets(@Mocked final ProxySettings mockedProxySettings)
    {
        //arrange
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.AMQPS);
        Deencapsulation.setField(config, "proxySettings", mockedProxySettings);

        //act
        ProxySettings actualProxySettings = config.getProxySettings();

        //assert
        assertEquals(mockedProxySettings, actualProxySettings);
    }

    @Test
    public void ConstructorWithValidGatewayHostNameAndE4KGatewayType()
    {
        //arrange - act
        new Expectations()
        {
            {
                mockIotHubConnectionString.getGatewayHostName();
                result = "testGatewayHostName";
            }
        };

        ClientOptions options = ClientOptions.builder().gatewayType(GatewayType.E4K).build();
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.MQTT, options);

        //assert
        assertEquals(true, config.isConnectingToMqttGateway());
    }

    @Test
    public void ConstructorWithNullGatewayHostNameAndE4KGatewayType()
    {
        //arrange - act
        new Expectations()
        {
            {
                mockIotHubConnectionString.getGatewayHostName();
                result = null;
            }
        };

        ClientOptions options = ClientOptions.builder().gatewayType(GatewayType.E4K).build();
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.MQTT, options);

        //assert
        assertEquals(false, config.isConnectingToMqttGateway());
    }

    @Test
    public void ConstructorWithValidGatewayHostNameAndEdgeGatewayType()
    {
        //arrange - act
        new Expectations()
        {
            {
                mockIotHubConnectionString.getGatewayHostName();
                result = "testGatewayHostName";
            }
        };

        ClientOptions options = ClientOptions.builder().gatewayType(GatewayType.EDGE).build();
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.MQTT, options);

        //assert
        assertEquals(false, config.isConnectingToMqttGateway());
    }

    @Test
    public void ConstructorWithNullGatewayHostNameAndEdgeGatewayType()
    {
        //arrange - act
        new Expectations()
        {
            {
                mockIotHubConnectionString.getGatewayHostName();
                result = null;
            }
        };

        ClientOptions options = ClientOptions.builder().gatewayType(GatewayType.EDGE).build();
        ClientConfiguration config = new ClientConfiguration(mockIotHubConnectionString, IotHubClientProtocol.MQTT, options);

        //assert
        assertEquals(false, config.isConnectingToMqttGateway());
    }
}
