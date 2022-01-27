package tests.integration.com.microsoft.azure.sdk.iot.iothub;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.twin.MethodData;
import com.microsoft.azure.sdk.iot.device.twin.Pair;
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
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

@IotHubTest
@RunWith(Parameterized.class)
public class HubTierConnectionTests extends IntegrationTest
{
    protected static final long WAIT_FOR_DISCONNECT_TIMEOUT_MILLISECONDS = 60 * 1000; // 1 minute

    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 180000;

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    protected static String iotHubConnectionString = "";
    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8897;

    // Semmle flags this as a security issue, but this is a test username so the warning can be suppressed
    protected static final String testProxyUser = "proxyUsername"; // lgtm

    // Semmle flags this as a security issue, but this is a test password so the warning can be suppressed
    protected static final char[] testProxyPass = "1234".toCharArray(); // lgtm

    protected static String hostName;

    public HubTierConnectionTestInstance testInstance;

    protected static RegistryManager registryManager;

    public HubTierConnectionTests(DeviceClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, boolean useHttpProxy)
    {
        this.testInstance = new HubTierConnectionTestInstance(client, protocol, identity, authenticationType, useHttpProxy);
    }

    @Parameterized.Parameters(name = "{1}_{3}_{4}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        String publicKeyCert = x509CertificateGenerator.getPublicCertificate();
        String privateKey = x509CertificateGenerator.getPrivateKey();
        String x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();

        registryManager = new RegistryManager(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        String uuid = UUID.randomUUID().toString();
        String deviceId = "java-tier-connection-e2e-test".concat("-" + uuid);
        String deviceIdX509 = "java-tier-connection-e2e-test-X509".concat("-" + uuid);

        Device device = Device.createFromId(deviceId, null, null);
        Device deviceX509 = Device.createDevice(deviceIdX509, SELF_SIGNED);

        deviceX509.setThumbprint(x509Thumbprint, x509Thumbprint);

        Tools.addDeviceWithRetry(registryManager, device);
        Tools.addDeviceWithRetry(registryManager, deviceX509);

        hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString).getHostName();
        SSLContext sslContext = SSLContextBuilder.buildSSLContext(publicKeyCert, privateKey);

        ClientOptions options = ClientOptions.builder().sslContext(sslContext).build();

        Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
        ClientOptions optionsWithProxy = ClientOptions.builder().proxySettings((new ProxySettings(testProxy, testProxyUser, testProxyPass))).build();

        return new ArrayList(Arrays.asList(
                new Object[][]
                        {
                                //sas token device client
                                {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS), AMQPS, device, SAS, false},
                                {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS_WS), AMQPS_WS, device, SAS, false},

                                //x509 device client
                                {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), AMQPS, options), AMQPS, deviceX509, SELF_SIGNED, false},

                                //sas token device client, with proxy
                                {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS_WS, options), AMQPS_WS, device, SAS, true}
                        }
        ));
    }

    @BeforeClass
    public static void classSetup()
    {
        registryManager = new RegistryManager(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
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

    }

    public static class HubTierConnectionTestInstance
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

        public HubTierConnectionTestInstance(DeviceClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, boolean useHttpProxy)
        {
            this.client = client;
            this.protocol = protocol;
            this.identity = identity;
            this.authenticationType = authenticationType;
            this.publicKeyCert = x509CertificateGenerator.getPublicCertificate();
            this.privateKey = x509CertificateGenerator.getPrivateKey();
            this.x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();
            String deviceId = identity.getDeviceId();
            this.useHttpProxy = useHttpProxy;

            this.correlationDetailsLoggingAssert = new CorrelationDetailsLoggingAssert(this.client.getConfig().getIotHubHostname(), deviceId, protocol.toString(), null);
        }
    }

    protected static class MethodCallback implements com.microsoft.azure.sdk.iot.device.twin.MethodCallback
    {
        @Override
        public MethodData call(String methodName, Object methodData, Object context)
        {
            return new MethodData(200, "payload");
        }
    }

    protected static class DeviceMethodStatusCallback implements IotHubEventCallback
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
        testInstance.client.setConnectionStatusChangeCallback((status, statusChangeReason, throwable, callbackContext) -> connectionStatusUpdates.add(new Pair<>(status, throwable)), null);

        testInstance.client.open(false);

        //act
        testInstance.client.subscribeToMethodsAsync(new MethodCallback(), null, new DeviceMethodStatusCallback(), null);

        //assert
        waitForDisconnect(connectionStatusUpdates, WAIT_FOR_DISCONNECT_TIMEOUT_MILLISECONDS, testInstance.client);
        testInstance.client.close();
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
        for (Pair<IotHubConnectionStatus, Throwable> actualStatusUpdate : actualStatusUpdates)
        {
            if (actualStatusUpdate.getKey() == status)
            {
                return true;
            }
        }

        return false;
    }
}
