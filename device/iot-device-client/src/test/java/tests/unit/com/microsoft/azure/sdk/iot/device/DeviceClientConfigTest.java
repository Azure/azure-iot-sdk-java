// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.device.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit tests for deviceClientConfig.
 * Methods: 96%
 * Lines: 90%
 */
public class DeviceClientConfigTest
{
    // Tests_SRS_DEVICECLIENTCONFIG_11_002: [The function shall return the IoT Hub hostname given in the constructor.]
    @Test
    public void getIotHubHostnameReturnsIotHubHostname()
            throws URISyntaxException
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
    public void getIotHubNameReturnsIotHubName() throws URISyntaxException
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
        String testIotHubName = config.getIotHubName();

        final String expectedIotHubName = "test";
        assertThat(testIotHubName, is(expectedIotHubName));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_003: [The function shall return the device ID given in the constructor.]
    @Test
    public void getDeviceIdReturnsDeviceId() throws URISyntaxException
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

    // Tests_SRS_DEVICECLIENTCONFIG_11_004: [The function shall return the device key given in the constructor.]
    @Test
    public void getDeviceKeyReturnsDeviceKey() throws URISyntaxException
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
        String testDeviceKey = config.getDeviceKey();

        final String expectedDeviceKey = deviceKey;
        assertThat(testDeviceKey, is(expectedDeviceKey));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_25_018: [**The function shall return the SharedAccessToken given in the constructor.**] **
    @Test
    public void getSasTokenReturnsSasToken() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = null;
        final String sharedAccessToken = "SharedAccessSignature sr=sample-iothub-hostname.net%2fdevices%2fsample-device-ID&sig=S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d&se=" + Long.MAX_VALUE;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);
        String testSasToken = config.getSharedAccessToken();

        final String expectedSasToken = sharedAccessToken;
        assertThat(testSasToken, is(expectedSasToken));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_005: [The function shall return the value of tokenValidSecs.]
    @Test
    public void getMessageValidSecsReturnsConstant() throws URISyntaxException
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
        long testMessageValidSecs = config.getTokenValidSecs();

        final long expectedMessageValidSecs = 3600;
        assertThat(testMessageValidSecs, is(expectedMessageValidSecs));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_25_008: [The function shall set the value of tokenValidSecs.]
    @Test
    public void getandsetMessageValidSecsReturnsConstant() throws URISyntaxException
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

        long testsetMessageValidSecs = 60;
        config.setTokenValidSecs(testsetMessageValidSecs);
        long testgetMessageValidSecs= config.getTokenValidSecs();

        final long expectedMessageValidSecs = testsetMessageValidSecs;
        assertThat(testgetMessageValidSecs, is(expectedMessageValidSecs));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_006: [The function shall set the message callback, with its associated context.]
    // Tests_SRS_DEVICECLIENTCONFIG_11_010: [The function shall return the current message callback.]
    @Test
    public void getAndSetMessageCallbackMatch(
            @Mocked final MessageCallback mockCallback)
            throws URISyntaxException
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
            throws URISyntaxException
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
            throws URISyntaxException
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
            throws URISyntaxException
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
            throws URISyntaxException
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
    public void getReadTimeoutMillisReturnsConstant() throws URISyntaxException
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
    public void setWebsocketEnabledSets() throws URISyntaxException
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
    public void getWebsocketEnabledGets() throws URISyntaxException
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
            throws URISyntaxException
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

    //Tests_SRS_DEVICECLIENTCONFIG_25_027: [**The function shall return the value of the path to the certificate.**] **
    @Test
    public void getPathToCertificateGets() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final String certPath = "/test/path/to/certificate";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);

        config.setPathToCert(certPath);

        String testCertPath = config.getPathToCertificate();

        assertNotNull(certPath);
        assertEquals(testCertPath, certPath);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_25_028: [**The function shall set the path to the certificate**] **
    @Test
    public void setPathToCertificateSets() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final String certPath = "/test/path/to/certificate";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);

        config.setPathToCert(certPath);

        String testCertPath = config.getPathToCertificate();

        assertNotNull(certPath);
        assertEquals(testCertPath, certPath);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_25_030: [**The function shall return the value of the user certificate string.**] **
    @Test
    public void getUserCertificateGets() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final String cert = "ValidCertString";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);

        config.setUserCertificateString(cert);

        String testCert = config.getUserCertificateString();

        assertNotNull(cert);
        assertEquals(testCert, cert);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_25_029: [**The function shall set user certificate String**] **
    @Test
    public void setUserCertificateSets() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final String cert = "ValidCertString";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);

        config.setUserCertificateString(cert);

        String testCert = config.getUserCertificateString();

        assertNotNull(cert);
        assertEquals(testCert, cert);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_25_032: [**The function shall return the IotHubSSLContext.**] **
    @Test
    public void getIotHubSSLContextGets(@Mocked final IotHubSSLContext mockedContext) throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final String cert = "ValidCertString";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);

        config.setIotHubSSLContext(mockedContext);

        IotHubSSLContext testContext = config.getIotHubSSLContext();

        assertNotNull(testContext);
        assertEquals(testContext, mockedContext);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_25_031: [**The function shall set IotHub SSL Context**] **
    @Test
    public void setIotHubSSLContextSets(@Mocked final IotHubSSLContext mockedContext) throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;
        final String cert = "ValidCertString";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class, String.class, String.class, String.class},
                        iotHubHostname,
                        deviceId,
                        deviceKey,
                        sharedAccessToken);

        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);

        config.setIotHubSSLContext(mockedContext);

        IotHubSSLContext testContext = config.getIotHubSSLContext();

        assertNotNull(testContext);
        assertEquals(testContext, mockedContext);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_21_033: [The constructor shall save the IoT Hub hostname, hubname,
    // device ID, device key, and device token, provided in the `iotHubConnectionString`.]
    @Test
    public void constructorWithConnectionStringKeySucceed() throws URISyntaxException
    {
        // arrange
        final String iotHubname = "iotHubName";
        final String iotHubHostname = iotHubname + ".iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class},
                        "HostName=" + iotHubHostname + ";CredentialType=SharedAccessKey;CredentialScope=Device;" +
                        "DeviceId=" + deviceId + ";SharedAccessKey=" + deviceKey + ";");

        // act
        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);

        // assert
        assertNotNull(config);
        IotHubConnectionString actualConnectionString = Deencapsulation.getField(config, "iotHubConnectionString");
        assertEquals(iotHubHostname, actualConnectionString.getHostName());
        assertEquals(iotHubname, actualConnectionString.getHubName());
        assertEquals(deviceId, actualConnectionString.getDeviceId());
        assertEquals(deviceKey, actualConnectionString.getSharedAccessKey());
    }

    // Tests_SRS_DEVICECLIENTCONFIG_21_033: [The constructor shall save the IoT Hub hostname, hubname,
    // device ID, device key, and device token, provided in the `iotHubConnectionString`.]
    @Test
    public void constructorWithConnectionStringTokenSucceed() throws URISyntaxException
    {
        // arrange
        final String iotHubname = "iotHubName";
        final String iotHubHostname = iotHubname + ".iothubhostname";
        final String deviceId = "test-deviceid";
        final String sharedAccessToken = "SharedAccessSignature sr=sample-iothub-hostname.net%2fdevices%2fsample-device-ID&sig=S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d&se=" + Long.MAX_VALUE;;
        final IotHubConnectionString iotHubConnectionString =
                Deencapsulation.newInstance(IotHubConnectionString.class,
                        new Class[] {String.class},
                        "HostName=" + iotHubHostname + ";CredentialType=SharedAccessKey;CredentialScope=Device;" +
                        "DeviceId=" + deviceId + ";SharedAccessSignature=" + sharedAccessToken + ";");

        // act
        DeviceClientConfig config = new DeviceClientConfig(iotHubConnectionString);

        // assert
        assertNotNull(config);
        IotHubConnectionString actualConnectionString = Deencapsulation.getField(config, "iotHubConnectionString");
        assertEquals(iotHubHostname, actualConnectionString.getHostName());
        assertEquals(iotHubname, actualConnectionString.getHubName());
        assertEquals(deviceId, actualConnectionString.getDeviceId());
        assertEquals(sharedAccessToken, actualConnectionString.getSharedAccessToken());
    }

    // Tests_SRS_DEVICECLIENTCONFIG_21_034: [If the provided `iotHubConnectionString` is null,
    // the constructor shall throw IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorFailsNullConnectionString()
            throws URISyntaxException
    {
        new DeviceClientConfig(null);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_035: [If the saved sas token has expired and there is no device key present, this function shall return true.]
    @Test
    public void needsToRenewSasTokenReturnsTrueWhenSASTokenExpired(@Mocked final IotHubConnectionString mockIotHubConnectionString)
    {
        //arrange
        final String expiredSasToken = "SharedAccessSignature sr=sample-iothub-hostname.net%2fdevices%2fsample-device-ID&sig=S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d&se=" + 0L;;

        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getSharedAccessToken();
                result = expiredSasToken;
            }
        };

        DeviceClientConfig config = new DeviceClientConfig(mockIotHubConnectionString);

        //act
        boolean needsToRenewToken = config.needsToRenewSasToken();

        //assert
        assertTrue(needsToRenewToken);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_035: [If the saved sas token has expired and there is no device key present, this function shall return true.]
    @Test
    public void needsToRenewSasTokenReturnsFalseWhenSASTokenNotExpired(@Mocked final IotHubConnectionString mockIotHubConnectionString)
    {
        //arrange
        final String nonExpiredSasToken = "SharedAccessSignature sr=sample-iothub-hostname.net%2fdevices%2fsample-device-ID&sig=S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d&se=" + Long.MAX_VALUE;

        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getSharedAccessToken();
                result = nonExpiredSasToken;
            }
        };

        DeviceClientConfig config = new DeviceClientConfig(mockIotHubConnectionString);

        //act
        boolean needsToRenewToken = config.needsToRenewSasToken();

        //assert
        assertFalse(needsToRenewToken);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_035: [If the saved sas token has expired and there is no device key present, this function shall return true.]
    @Test
    public void needsToRenewSasTokenReturnsFalseWhenSASTokenExpiredButDeviceKeyPresent(
            @Mocked final IotHubConnectionString mockIotHubConnectionString,
            @Mocked final IotHubSasToken mockSasToken)
    {
        //arrange
        final String expiredSasToken = "SharedAccessSignature sr=sample-iothub-hostname.net%2fdevices%2fsample-device-ID&sig=S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d&se=" + 0L;
        final DeviceClientConfig config = new DeviceClientConfig(mockIotHubConnectionString);

        Deencapsulation.setField(mockSasToken, "sasToken", expiredSasToken);

        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getSharedAccessKey();
                result = "some key";
                mockIotHubConnectionString.getSharedAccessToken();
                result = expiredSasToken;
                mockIotHubConnectionString.getHostName();
                result = "someHost";
                new IotHubSasToken(config, anyLong);
                result = mockSasToken;
                IotHubSasToken.isSasTokenExpired(expiredSasToken);
                result = true;
            }
        };

        //act
        boolean needsToRenewToken = config.needsToRenewSasToken();

        //assert
        assertFalse(needsToRenewToken);
    }

    //Tests_SRS_DEVICECLIENTCONFIG_34_036: [If this function generates the returned SharedAccessToken from a device key, the previous SharedAccessToken shall be overwritten with the generated value.]
    @Test
    public void getSharedAccessTokenOverwritesOldTokenIfGeneratedByDeviceKey(
            @Mocked final IotHubConnectionString mockIotHubConnectionString,
            @Mocked final IotHubSasToken mockSasToken)
    {
        //arrange
        final String expiredSasToken = "SharedAccessSignature sr=sample-iothub-hostname.net%2fdevices%2fsample-device-ID&sig=S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d&se=" + 0L;
        final DeviceClientConfig config = new DeviceClientConfig(mockIotHubConnectionString);
        final String expectedSasTokenString = "some new sas token";
        final String deviceKey = "some device key";

        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getSharedAccessKey();
                result = deviceKey;
                mockSasToken.toString();
                result = expectedSasTokenString;
                new IotHubSasToken(config, anyLong);
                result = mockSasToken;
            }
        };

        //act
        String actualSharedAccessToken = config.getSharedAccessToken();

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockIotHubConnectionString, "setSharedAccessToken", new Class[] {String.class}, expectedSasTokenString);
                times = 1;
            }
        };

        assertNotEquals(expiredSasToken, actualSharedAccessToken);
        assertEquals(expectedSasTokenString, actualSharedAccessToken);
    }
}
