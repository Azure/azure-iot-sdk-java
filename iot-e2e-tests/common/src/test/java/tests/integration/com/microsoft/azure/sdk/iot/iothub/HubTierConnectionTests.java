package tests.integration.com.microsoft.azure.sdk.iot.iothub;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.BasicTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS_WS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static junit.framework.TestCase.fail;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

@IotHubTest
@RunWith(Parameterized.class)
public class HubTierConnectionTests extends IntegrationTest
{
    protected static final long WAIT_FOR_DISCONNECT_TIMEOUT_MILLISECONDS = 1 * 60 * 1000; // 1 minute

    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 180000;

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    protected static String iotHubConnectionString = "";
    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8897;
    protected static final String testProxyUser = "proxyUsername";
    protected static final char[] testProxyPass = "1234".toCharArray();

    protected static String hostName;

    public HubTierConnectionTestInstance testInstance;

    protected static RegistryManager registryManager;

    public HubTierConnectionTests(DeviceClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, String publicKeyCert, String privateKey, String x509Thumbprint, boolean useHttpProxy)
    {
        this.testInstance = new HubTierConnectionTestInstance(client, protocol, identity, authenticationType, publicKeyCert, privateKey, x509Thumbprint, useHttpProxy);
    }

    @Parameterized.Parameters(name = "{1}_{3}_{7}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        String publicKeyCert = certificateGenerator.getPublicCertificate();
        String privateKey = certificateGenerator.getPrivateKey();
        String x509Thumbprint = certificateGenerator.getX509Thumbprint();

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        String uuid = UUID.randomUUID().toString();
        String deviceId = "java-tier-connection-e2e-test".concat("-" + uuid);
        String deviceIdX509 = "java-tier-connection-e2e-test-X509".concat("-" + uuid);

        Device device = Device.createFromId(deviceId, null, null);
        Device deviceX509 = Device.createDevice(deviceIdX509, SELF_SIGNED);

        deviceX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);

        Tools.addDeviceWithRetry(registryManager, device);
        Tools.addDeviceWithRetry(registryManager, deviceX509);

        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();
        SSLContext sslContext = SSLContextBuilder.buildSSLContext(publicKeyCert, privateKey);

        List inputs = new ArrayList(Arrays.asList(
                new Object[][]
                        {
                                //sas token device client
                                {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS), AMQPS, device, SAS, publicKeyCert, privateKey, x509Thumbprint, false},
                                {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS_WS), AMQPS_WS, device, SAS, publicKeyCert, privateKey, x509Thumbprint, false},

                                //x509 device client
                                {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), AMQPS, sslContext), AMQPS, deviceX509, SELF_SIGNED, publicKeyCert, privateKey, x509Thumbprint, false},

                                //sas token device client, with proxy
                                {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS_WS), AMQPS_WS, device, SAS, publicKeyCert, privateKey, x509Thumbprint, true}
                        }
        ));

        Thread.sleep(2000);

        return inputs;
    }

    @BeforeClass
    public static void classSetup()
    {
        try
        {
            registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Unexpected exception encountered");
        }
    }

    @BeforeClass
    public static void startProxy()
    {
        proxyServer = DefaultHttpProxyServer.bootstrap()
                .withPort(testProxyPort)
                .withProxyAuthenticator(new BasicProxyAuthenticator(testProxyUser, testProxyPass))
                .start();
    }

    @AfterClass
    public static void stopProxy()
    {
        proxyServer.stop();
    }

    @Before
    public void SetProxyIfApplicable()
    {
        if (testInstance.useHttpProxy)
        {
            Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
            testInstance.client.setProxySettings(new ProxySettings(testProxy, testProxyUser, testProxyPass));
        }
    }

    public class HubTierConnectionTestInstance
    {
        public DeviceClient client;
        public IotHubClientProtocol protocol;
        public BaseDevice identity;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public CorrelationDetailsLoggingAssert correlationDetailsLoggingAssert;
        public boolean useHttpProxy;

        public HubTierConnectionTestInstance(DeviceClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, String publicKeyCert, String privateKey, String x509Thumbprint, boolean useHttpProxy)
        {
            this.client = client;
            this.protocol = protocol;
            this.identity = identity;
            this.authenticationType = authenticationType;
            this.publicKeyCert = publicKeyCert;
            this.privateKey = privateKey;
            this.x509Thumbprint = x509Thumbprint;
            String deviceId = identity.getDeviceId();
            this.useHttpProxy = useHttpProxy;

            this.correlationDetailsLoggingAssert = new CorrelationDetailsLoggingAssert(this.client.getConfig().getIotHubHostname(), deviceId, protocol.toString(), null);
        }
    }

    protected static class DeviceMethodCallback implements com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback
    {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            return new DeviceMethodData(200, "payload");
        }
    }

    protected static class DeviceMethodStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("Device Client: IoT Hub responded to device method operation with status " + status.name());
        }
    }

    @Test
    @BasicTierHubOnlyTest
    public void enableMethodFailedWithBasicTier() throws IOException, InterruptedException
    {
        //arrange
        List<Pair<IotHubConnectionStatus, Throwable>> connectionStatusUpdates = new ArrayList<>();
        testInstance.client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
            {
                connectionStatusUpdates.add(new Pair<>(status, throwable));
            }
        }, null);

        testInstance.client.open();

        //act
        testInstance.client.subscribeToDeviceMethod(new DeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);

        //assert
        waitForDisconnect(connectionStatusUpdates, WAIT_FOR_DISCONNECT_TIMEOUT_MILLISECONDS, testInstance.client);
        testInstance.client.closeNow();
    }

    public static void waitForDisconnect(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, long timeout, InternalClient client) throws InterruptedException
    {
        //Wait for DISCONNECTED
        long startTime = System.currentTimeMillis();
        while (!actualStatusUpdatesContainsStatus(actualStatusUpdates, IotHubConnectionStatus.DISCONNECTED))
        {
            Thread.sleep(2000);
            long timeElapsed = System.currentTimeMillis() - startTime;
            if (timeElapsed > timeout)
            {
                Assert.fail(buildExceptionMessage("Timed out waiting for disconnection to take effect", client));
            }
        }
    }

    public static boolean actualStatusUpdatesContainsStatus(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, IotHubConnectionStatus status)
    {
        for (int i = 0; i < actualStatusUpdates.size(); i++)
        {
            if (actualStatusUpdates.get(i).getKey() == status)
            {
                return true;
            }
        }

        return false;
    }
}
