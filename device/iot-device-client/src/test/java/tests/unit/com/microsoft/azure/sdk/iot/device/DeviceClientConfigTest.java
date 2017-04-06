// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import mockit.Mocked;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

/** Unit tests for IoTHubClientConfig. */
public class DeviceClientConfigTest
{
    // Tests_SRS_DEVICECLIENTCONFIG_11_014: [If the IoT Hub hostname is not a valid URI,
    // the constructor shall throw a URISyntaxException.]
    @Test(expected = URISyntaxException.class)
    public void constructorFailsForInvalidHostname() throws URISyntaxException
    {
        final String illegalIotHubHostname = "test.iothubhostname}{";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessSig = null;

        new DeviceClientConfig(illegalIotHubHostname, deviceId, deviceKey, sharedAccessSig);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_015: [If the IoT Hub hostname does not contain a '.',
    // the function shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorFailsForHostnameWithoutIotHubName()
            throws URISyntaxException
    {
        final String illegalIotHubHostname = "badiothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessSig = null;

        new DeviceClientConfig(illegalIotHubHostname, deviceId, deviceKey, sharedAccessSig);
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_001: [The constructor shall save the IoT Hub hostname,
    // device ID, and device key.]
    // Tests_SRS_DEVICECLIENTCONFIG_11_002: [The function shall return the IoT Hub hostname given in the constructor.]
    @Test
    public void getIotHubHostnameReturnsIotHubHostname()
            throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessSig = null;

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessSig);
        String testIotHubHostname = config.getIotHubHostname();

        final String expectedIotHubHostname = iotHubHostname;
        assertThat(testIotHubHostname, is(expectedIotHubHostname));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_11_002: [The function shall return the IoT Hub name given in the constructor,
    // where the IoT Hub name is embedded in the IoT Hub hostname as follows: [IoT Hub name].[valid HTML chars]+.]
    @Test
    public void getIotHubNameReturnsIotHubName() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessSig = null;

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessSig);
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
        final String sharedAccessSig = null;

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessSig);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);
        String testDeviceKey = config.getDeviceKey();

        final String expectedDeviceKey = deviceKey;
        assertThat(testDeviceKey, is(expectedDeviceKey));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_25_017: [**The constructor shall save sharedAccessToken.**] **
    // Tests_SRS_DEVICECLIENTCONFIG_25_018: [**The function shall return the SharedAccessToken given in the constructor.**] **
    @Test
    public void getSasTokenReturnsSasToken() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = "SharedAccessSignature sr=sample-iothub-hostname.net%2fdevices%2fsample-device-ID&sig=S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d&se=1469813873";

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);
        long testMessageValidSecs = config.getTokenValidSecs();

        final long expectedMessageValidSecs = 3600;
        assertThat(testMessageValidSecs, is(expectedMessageValidSecs));
    }

    // Tests_SRS_DEVICECLIENTCONFIG_25_016: [The function shall set the value of tokenValidSecs.]
    @Test
    public void getandsetMessageValidSecsReturnsConstant() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);

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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);
        Object context = new Object();
        config.setMessageCallback(mockCallback, context);
        MessageCallback testCallback = config.getMessageCallback();

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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);
        Object context = new Object();
        config.setMessageCallback(mockCallback, context);
        Object testContext = config.getMessageContext();

        final Object expectedContext = context;
        assertThat(testContext, is(expectedContext));
    }

    /*
    **Tests_SRS_DEVICECLIENTCONFIG_25_023: [**The function shall set the DeviceTwin message callback.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_024: [**The function shall set the DeviceTwin message context.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_025: [**The function shall return the current DeviceTwin message callback.**] **
    **Tests_SRS_DEVICECLIENTCONFIG_25_026: [**The function shall return the current DeviceTwin message context.**] **
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);
        Object context = new Object();
        config.setDeviceMethodMessageCallback(mockCallback, context);
        Object testContext = config.getDeviceMethodMessageContext();


        final Object expectedContext = context;
        assertThat(testContext, is(expectedContext));
        assertEquals(config.getDeviceMethodMessageCallback(), mockCallback);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);
        Object dMContext = new Object();
        config.setDeviceMethodMessageCallback(mockCallback, dMContext);
        Object testContextDM = config.getDeviceMethodMessageContext();

        Object dTcontext = new Object();
        config.setDeviceTwinMessageCallback(mockCallback, dTcontext);
        Object testContextDT = config.getDeviceTwinMessageContext();

        final Object expectedDTContext = dTcontext;
        assertThat(testContextDT, is(expectedDTContext));
        assertEquals(config.getDeviceTwinMessageCallback(), mockCallback);


        final Object expectedDMContext = dMContext;
        assertThat(testContextDM, is(expectedDMContext));
        assertEquals(config.getDeviceMethodMessageCallback(), mockCallback);
    }
    // Tests_SRS_DEVICECLIENTCONFIG_11_012: [The function shall return 240000ms.]
    @Test
    public void getReadTimeoutMillisReturnsConstant() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothubhostname";
        final String deviceId = "test-deviceid";
        final String deviceKey = "test-devicekey";
        final String sharedAccessToken = null;

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);
        int testReadTimeoutMillis = config.getReadTimeoutMillis();

        final int expectedReadTimeoutMillis = 240000;
        assertThat(testReadTimeoutMillis, is(expectedReadTimeoutMillis));
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);
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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);

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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);

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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);

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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);

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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);

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

        DeviceClientConfig config = new DeviceClientConfig(iotHubHostname, deviceId, deviceKey, sharedAccessToken);

        config.setIotHubSSLContext(mockedContext);

        IotHubSSLContext testContext = config.getIotHubSSLContext();

        assertNotNull(testContext);
        assertEquals(testContext, mockedContext);
    }

}
