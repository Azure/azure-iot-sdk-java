// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthentication;
import com.microsoft.azure.sdk.iot.device.auth.IotHubX509Authentication;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.StrictExpectations;
import org.junit.Test;

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
 * Lines: 98%
 */
public class DeviceClientConfigTest
{
    @Mocked IotHubSasTokenAuthentication mockSasTokenAuthentication;
    @Mocked IotHubX509Authentication mockX509Authentication;

    private static final String publicKeyCert = "someCert";
    private static final String privateKey = "someKey";

    private static String expectedDeviceId = "deviceId";
    private static String expectedHostname = "hostname";
    private static String expectedDeviceKey = "deviceKey";
    private static String expectedSasToken = "sasToken";

    // Tests_SRS_DEVICECLIENTCONFIG_11_002: [The function shall return the IoT Hub hostname given in the constructor.]
    @Test
    public void getIotHubHostnameReturnsIotHubHostname()
            throws URISyntaxException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
        String testIotHubHostname = config.getIotHubHostname();

        final String expectedIotHubHostname = iotHubHostname;
        assertThat(testIotHubHostname, is(expectedIotHubHostname));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_007: [The function shall return the IoT Hub name given in the constructor,
    // where the IoT Hub name is embedded in the IoT Hub hostname as follows: [IoT Hub name].[valid HTML chars]+.]
    @Test
    public void getIotHubNameReturnsIotHubName() throws URISyntaxException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
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
        final String expectedIotHubName = "test";

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
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
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
        String testDeviceId = config.getDeviceId();

        final String expectedDeviceId = deviceId;
        assertThat(testDeviceId, is(expectedDeviceId));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_004: [If this is using Sas token authentication, the function shall return the device key given in the constructor.]
    @Test
    public void getDeviceKeyReturnsDeviceKey() throws URISyntaxException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
        int testMessageLockTimeoutSecs = config.getMessageLockTimeoutSecs();

        final int expectedMessageLockTimeoutSecs = 180;
        assertThat(testMessageLockTimeoutSecs,
                is(expectedMessageLockTimeoutSecs));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_21_034: [If the provided `iotHubConnectionString` is null,
    // the constructor shall throw IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorFailsNullConnectionString()
            throws URISyntaxException, IOException
    {
        new DeviceClientConfig(null);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_039: [This function shall return the type of authentication that the config is set up to use.]
    @Test
    public void getAuthenticationTypeWorks(@Mocked final IotHubConnectionString mockIotHubConnectionString) throws IOException
    {
        //arrange
        DeviceClientConfig config = new DeviceClientConfig(mockIotHubConnectionString);

        DeviceClientConfig.AuthType expectedAuthType = DeviceClientConfig.AuthType.X509_CERTIFICATE;
        Deencapsulation.setField(config, "authenticationType", expectedAuthType);

        //act
        DeviceClientConfig.AuthType actualAuthType = config.getAuthenticationType();

        //assert
        assertEquals(expectedAuthType, actualAuthType);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_046: [If the provided `iotHubConnectionString` does not use x509 authentication, it shall be saved to a new IotHubSasTokenAuthentication object and the authentication type of this shall be set to SASToken.]
    @Test
    public void constructorUsingSASTokenSetsTypeCorrectly(@Mocked final IotHubConnectionString mockIotHubConnectionString) throws URISyntaxException, IOException
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
        DeviceClientConfig config = new DeviceClientConfig(mockIotHubConnectionString);

        //assert
        DeviceClientConfig.AuthType actualAuthType = Deencapsulation.getField(config, "authenticationType");
        assertEquals(DeviceClientConfig.AuthType.SAS_TOKEN, actualAuthType);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_069: [If the provided connection string is null or does not use x509 auth, and IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithNullConnStringThrows(@Mocked final IotHubConnectionString mockIotHubConnectionString) throws IOException
    {
        //act
        new DeviceClientConfig(null, "", false, "", false);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_069: [If the provided connection string is null or does not use x509 auth, and IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithWrongAuthTypeConnStringThrows(@Mocked final IotHubConnectionString mockIotHubConnectionString) throws IOException
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

    //Tests_SRS_DEVICECLIENTCONFIG_34_076: [If the provided `iotHubConnectionString` uses x509 authentication, the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorForSasTokenRejectsX509ConnectionStrings(@Mocked final IotHubConnectionString mockIotHubConnectionString)
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
        new DeviceClientConfig(mockIotHubConnectionString);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_077: [This function shall return the saved IotHubX509Authentication object.]
    @Test
    public void getIotHubX509AuthenticationWorks(@Mocked final IotHubConnectionString mockIotHubConnectionString)
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = true;
            }
        };
        DeviceClientConfig config = new DeviceClientConfig(mockIotHubConnectionString, "", false, "", false);
        Deencapsulation.setField(config, "x509Authentication", mockX509Authentication);

        //act
        IotHubX509Authentication auth = config.getX509Authentication();

        //assert
        assertEquals(mockX509Authentication, auth);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_078: [This function shall return the saved IotHubSasTokenAuthentication object.]
    @Test
    public void getIotHubSasTokenAuthenticationWorks(@Mocked final IotHubConnectionString mockIotHubConnectionString)
    {
        //arrange

        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = false;
            }
        };
        DeviceClientConfig config = new DeviceClientConfig(mockIotHubConnectionString);
        Deencapsulation.setField(config, "sasTokenAuthentication", mockSasTokenAuthentication);

        //act
        IotHubSasTokenAuthentication auth = config.getSasTokenAuthentication();

        //assert
        assertEquals(mockSasTokenAuthentication, auth);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_34_079: [This function shall return the saved IotHubConnectionString object.]
    public void getIotHubConnectionStringWorks(@Mocked final IotHubConnectionString mockIotHubConnectionString)
    {
        //arrange

        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = false;
            }
        };
        DeviceClientConfig config = new DeviceClientConfig(mockIotHubConnectionString);

        //act
        IotHubConnectionString actualConnString = config.getIotHubConnectionString();

        //assert
        assertEquals(mockIotHubConnectionString, actualConnString);
    }
}
