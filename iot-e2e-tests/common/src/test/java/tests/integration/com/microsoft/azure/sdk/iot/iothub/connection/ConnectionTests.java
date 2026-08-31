package tests.integration.com.microsoft.azure.sdk.iot.iothub.connection;

import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.server.auth.BasicHttpProxyAuthenticationProvider;
import com.github.monkeywie.proxyee.server.auth.model.BasicHttpToken;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.Module;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.FlakeyTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@Slf4j
@IotHubTest
@RunWith(Parameterized.class)
public class ConnectionTests extends IntegrationTest
{
    private static String iotHubConnectionString;
    private static String hostName;

    @Parameterized.Parameters(name = "{0}_{1}_{2}_{3}_{4}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString).getHostName();

        return Arrays.asList(
            new Object[][]
                {
                    {HTTPS, SAS, ClientType.DEVICE_CLIENT, false, false},
                    {AMQPS, SAS, ClientType.DEVICE_CLIENT, false, false},
                    {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT, false, false},
                    {MQTT, SAS, ClientType.DEVICE_CLIENT, false, false},
                    {MQTT_WS, SAS, ClientType.DEVICE_CLIENT, false, false},

                    {HTTPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, false, false},
                    {AMQPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, false, false},
                    {MQTT, SELF_SIGNED, ClientType.DEVICE_CLIENT, false, false},
                    {MQTT_WS, SELF_SIGNED, ClientType.DEVICE_CLIENT, false, false},

                    {AMQPS, SAS, ClientType.MODULE_CLIENT, false, false},
                    {AMQPS_WS, SAS, ClientType.MODULE_CLIENT, false, false},
                    {MQTT, SAS, ClientType.MODULE_CLIENT, false, false},
                    {MQTT_WS, SAS, ClientType.MODULE_CLIENT, false, false},

                    {AMQPS, SELF_SIGNED, ClientType.MODULE_CLIENT, false, false},
                    {MQTT, SELF_SIGNED, ClientType.MODULE_CLIENT, false, false},
                    {MQTT_WS, SELF_SIGNED, ClientType.MODULE_CLIENT, false, false},

                    {HTTPS, SAS, ClientType.DEVICE_CLIENT, true, false},
                    {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT, true, false},
                    {MQTT_WS, SAS, ClientType.DEVICE_CLIENT, true, false},
                    {MQTT_WS, SELF_SIGNED, ClientType.DEVICE_CLIENT, true, false},
                    {AMQPS_WS, SAS, ClientType.MODULE_CLIENT, true, false},
                    {MQTT_WS, SAS, ClientType.MODULE_CLIENT, true, false},
                    {MQTT_WS, SELF_SIGNED, ClientType.MODULE_CLIENT, true, false},

                    // TODO AMQP_WS with proxy with auth doesn't work against our test proxy. It does work against
                    // other proxies, so this is likely just a bug in our current test proxy
                    {HTTPS, SAS, ClientType.DEVICE_CLIENT, true, true},
                    {MQTT_WS, SAS, ClientType.DEVICE_CLIENT, true, true},
                    {MQTT_WS, SELF_SIGNED, ClientType.DEVICE_CLIENT, true, true},
                    {MQTT_WS, SAS, ClientType.MODULE_CLIENT, true, true},
                    {MQTT_WS, SELF_SIGNED, ClientType.MODULE_CLIENT, true, true},
                });
    }

    public ConnectionTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, boolean withProxy, boolean withProxyAuth)
    {
        this.testInstance = new ConnectionTestInstance(protocol, authenticationType, clientType, withProxy, withProxyAuth);
    }

    public class ConnectionTestInstance
    {
        public IotHubClientProtocol protocol;
        public TestIdentity identity;

        // ECC identities are created by this test rather than taken from the shared pool, and they carry a
        // certificate that only this test knows about, so they must never be recycled back into that pool. This is
        // recorded as soon as the device is registered, rather than being derived from the identity, because the
        // rest of setupEccDevice can fail after that registration has already happened. Deleting the device also
        // deletes any module underneath it, so the module does not need to be tracked separately.
        private String eccDeviceIdToDelete;

        // What teardown still owns, kept separate from the public identity field above. dispose() clears these but
        // deliberately leaves identity set, because a test thread abandoned by the JUnit timeout may still read it.
        // Clearing them is what makes dispose() safe to run more than once: without it a second run would close the
        // same client twice and, worse, requeue the same identity into the shared pool twice.
        private TestIdentity identityToDispose;

        // Set once teardown has run. Guarded by lifecycleLock along with the two fields above.
        private boolean disposed;

        private final Object lifecycleLock = new Object();
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public boolean useHttpProxy;
        public boolean useHttpProxyAuth;

        public ConnectionTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, boolean useHttpProxy, boolean useHttpProxyAuth)
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.useHttpProxy = useHttpProxy;
            this.useHttpProxyAuth = useHttpProxyAuth;
        }

        /**
         * Configure this test's proxy settings on the given builder, if this variant uses a proxy at all.
         *
         * <p>Both setup paths go through here so that they cannot drift apart. They previously had separate copies of
         * this logic, and the copy in setupEccDevice was missing the unauthenticated branch entirely.</p>
         *
         * @param optionsBuilder The builder to apply the proxy settings to
         */
        private void applyProxySettings(ClientOptions.ClientOptionsBuilder optionsBuilder)
        {
            if (!this.useHttpProxy)
            {
                return;
            }

            if (this.useHttpProxyAuth)
            {
                Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
                optionsBuilder.proxySettings(new ProxySettings(testProxy, testProxyUser, testProxyPass.toCharArray()));
            }
            else
            {
                Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostnameWithoutAuth, testProxyPortWithoutAuth));
                optionsBuilder.proxySettings(new ProxySettings(testProxy));
            }
        }

        public void setup() throws Exception
        {
            ClientOptions.ClientOptionsBuilder optionsBuilder = ClientOptions.builder();
            applyProxySettings(optionsBuilder);

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                trackForCleanup(Tools.getTestDevice(iotHubConnectionString, this.protocol, this.authenticationType, false, optionsBuilder));
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                trackForCleanup(Tools.getTestModule(iotHubConnectionString, this.protocol, this.authenticationType , false, optionsBuilder));
            }

            disposeIfTeardownAlreadyRan();
        }

        public void setupEccDevice() throws Exception
        {
            ClientOptions.ClientOptionsBuilder optionsBuilder = ClientOptions.builder();
            applyProxySettings(optionsBuilder);

            X509CertificateGenerator certificateGenerator = new X509CertificateGenerator(X509CertificateGenerator.CertificateAlgorithm.ECC);
            SSLContext sslContext = SSLContextBuilder.buildSSLContext(certificateGenerator.getX509Certificate(), certificateGenerator.getPrivateKey());
            optionsBuilder.sslContext(sslContext);

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                Device eccDevice = new Device("ecc-test-device-" + UUID.randomUUID(), SELF_SIGNED);
                eccDevice.setThumbprint(certificateGenerator.getX509Thumbprint(), certificateGenerator.getX509Thumbprint());

                Tools.addDeviceWithRetry(new RegistryClient(iotHubConnectionString), eccDevice);
                trackEccDeviceForCleanup(eccDevice.getDeviceId());

                String deviceConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, eccDevice);
                trackForCleanup(new TestDeviceIdentity(
                    new DeviceClient(deviceConnectionString, testInstance.protocol, optionsBuilder.build()),
                    eccDevice));
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                Device eccDevice = new Device("ecc-test-device-" + UUID.randomUUID(), SELF_SIGNED);
                Module eccModule = new Module(eccDevice.getDeviceId(), "ecc-test-module-" + UUID.randomUUID(), SELF_SIGNED);
                eccDevice.setThumbprint(certificateGenerator.getX509Thumbprint(), certificateGenerator.getX509Thumbprint());
                eccModule.setThumbprint(certificateGenerator.getX509Thumbprint(), certificateGenerator.getX509Thumbprint());

                Tools.addDeviceWithRetry(new RegistryClient(iotHubConnectionString), eccDevice);
                trackEccDeviceForCleanup(eccDevice.getDeviceId());

                Tools.addModuleWithRetry(new RegistryClient(iotHubConnectionString), eccModule);

                String moduleConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, eccDevice) + ";ModuleId=" + eccModule.getId();
                trackForCleanup(new TestModuleIdentity(
                    new ModuleClient(moduleConnectionString, testInstance.protocol, optionsBuilder.build()),
                    eccDevice,
                    eccModule));
            }

            disposeIfTeardownAlreadyRan();
        }

        /**
         * Hand an identity to teardown, and publish it for the test body to use.
         *
         * @param newIdentity The identity this test just acquired or created
         */
        private void trackForCleanup(TestIdentity newIdentity)
        {
            // Published for the test body. Never cleared, so a thread the JUnit timeout abandoned can keep reading it.
            this.identity = newIdentity;

            synchronized (lifecycleLock)
            {
                this.identityToDispose = newIdentity;
            }
        }

        /**
         * Hand a freshly registered ECC device to teardown, so it is removed from the registry even if the rest of
         * setupEccDevice never completes.
         *
         * @param deviceId The device id that was just added to the registry
         */
        private void trackEccDeviceForCleanup(String deviceId)
        {
            synchronized (lifecycleLock)
            {
                this.eccDeviceIdToDelete = deviceId;
            }
        }

        /**
         * Dispose anything registered after teardown already ran.
         *
         * <p>Every test in this class is bounded by {@code @Test(timeout = 60000)}. JUnit runs the test body on a
         * separate thread and, when the timeout fires, abandons that thread while it is still running. {@code @After}
         * is outside the timeout, so teardown can execute while setup on the abandoned thread has not finished
         * acquiring its identity. Without this, the identity that setup goes on to produce would have no owner and
         * would leak, which is precisely the leak this class is trying to stop.</p>
         *
         * <p>This does not wait for setup, in either direction. Blocking teardown on a setup that is itself hung -
         * which is how these tests have actually timed out - would stall the rest of the run.</p>
         */
        private void disposeIfTeardownAlreadyRan()
        {
            boolean teardownAlreadyRan;
            synchronized (lifecycleLock)
            {
                teardownAlreadyRan = this.disposed;
            }

            if (teardownAlreadyRan)
            {
                dispose();
            }
        }

        /**
         * Close and dispose whatever this instance currently owns.
         *
         * <p>Safe to call more than once, and safe to call concurrently with setup: it takes ownership of the tracked
         * fields and clears them, so a second call finds only what was registered since the first.</p>
         */
        public void dispose()
        {
            TestIdentity identityToClean;
            String eccDeviceIdToClean;

            synchronized (lifecycleLock)
            {
                this.disposed = true;

                identityToClean = this.identityToDispose;
                eccDeviceIdToClean = this.eccDeviceIdToDelete;

                this.identityToDispose = null;
                this.eccDeviceIdToDelete = null;
            }

            if (identityToClean != null && identityToClean.getClient() != null)
            {
                identityToClean.getClient().close();
            }

            if (eccDeviceIdToClean != null)
            {
                // Recycling this identity would hand a device carrying a certificate that no other test knows about to
                // the next test that takes an x509 identity from the shared pool, so delete it instead. This runs even
                // when the identity was never finished being built, because the device is in the registry from the
                // moment it is registered, whether or not the rest of the setup succeeded.
                try
                {
                    Tools.getRegistyManager(iotHubConnectionString).removeDevice(eccDeviceIdToClean);
                }
                catch (IOException | IotHubException e)
                {
                    log.error("Failed to clean up ECC test device {}", eccDeviceIdToClean, e);
                }
            }
            else if (identityToClean != null)
            {
                Tools.disposeTestIdentity(identityToClean, iotHubConnectionString);
            }
        }
    }

    private final ConnectionTestInstance testInstance;
    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8899;

    protected static HttpProxyServer proxyServerWithoutAuth;
    protected static String testProxyHostnameWithoutAuth = "127.0.0.1";
    protected static int testProxyPortWithoutAuth = 9000;

    // Semmle flags this as a security issue, but this is a test username so the warning can be suppressed
    protected static final String testProxyUser = "proxyUsername"; // lgtm

    // Semmle flags this as a security issue, but this is a test password so the warning can be suppressed
    protected static final String testProxyPass = "1234"; // lgtm

    @BeforeClass
    public static void startProxy() throws Exception
    {
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setAuthenticationProvider(new BasicProxyAuthenticator(testProxyUser, testProxyPass));
        config.setHandleSsl(false);
        proxyServer = new HttpProxyServer().serverConfig(config);
        ProxyServerTools.startProxyServer(proxyServer, testProxyPort);

        HttpProxyServerConfig configWithoutAuth = new HttpProxyServerConfig();
        configWithoutAuth.setHandleSsl(false);
        proxyServerWithoutAuth = new HttpProxyServer().serverConfig(configWithoutAuth);
        ProxyServerTools.startProxyServer(proxyServerWithoutAuth, testProxyPortWithoutAuth);
    }

    // Without this, every test in this class leaks the client it opened. Those clients keep retrying their
    // connections for the rest of the JVM's life, and the ones configured with proxy settings keep retrying through
    // the proxies this class runs locally, which competes with the tests that are still running. Once stopProxy has
    // closed those proxies they retry against a dead port instead, for the remainder of the job.
    @After
    public void disposeTestInstance()
    {
        testInstance.dispose();
    }

    @AfterClass
    public static void stopProxy()
    {
        if (proxyServer != null)
        {
            proxyServer.close();
        }

        if (proxyServerWithoutAuth != null)
        {
            proxyServerWithoutAuth.close();
        }
    }

    /**
     * Log every connection status change for a client so that a test which times out while opening leaves behind
     * something explaining why.
     *
     * <p>These tests open with retry, and the default retry policy retries an unlimited number of times, so a
     * connection that never succeeds is never abandoned by the client. That means the client never throws, and the
     * only thing that ends the test is the JUnit timeout, which reports nothing beyond the line it was stuck on. The
     * status callback is the one place the underlying reason is visible, so it gets written to the log as it
     * arrives.</p>
     *
     * @param client The client to report status changes for
     */
    private static void logConnectionStatusChanges(InternalClient client)
    {
        client.setConnectionStatusChangeCallback(
            (context) ->
            {
                Throwable cause = context.getCause();
                log.info("Connection status changed from {} to {} because of {}{}",
                    context.getPreviousStatus(),
                    context.getNewStatus(),
                    context.getNewStatusReason(),
                    cause == null ? "" : ", cause: " + cause);

                if (cause != null)
                {
                    log.info("Connection status change cause", cause);
                }
            },
            null);
    }

    @Test(timeout = 60000) // 1 minute
    @IotHubTest
    public void CanOpenConnection() throws Exception
    {
        testInstance.setup();

        // Held locally rather than read back off testInstance for each call. The @After that disposes this instance
        // runs outside this method's timeout, so it can execute while this thread is still here after a timeout.
        InternalClient client = testInstance.identity.getClient();

        logConnectionStatusChanges(client);
        client.open(true);

        // deviceClient.open() is a no-op on HTTP, so a message needs to be sent to actually test opening the connection
        if (testInstance.protocol == HTTPS)
        {
            client.sendEvent(new Message("some message"));
        }

        client.close();
    }

    @IotHubTest
    @StandardTierHubOnlyTest
    @Test(timeout = 60000) // 1 minute
    @FlakeyTest
    public void CanOpenConnectionWithECCCertificates() throws Exception
    {
        // SAS token authenticated devices/modules don't use RSA or ECC certificates
        assumeTrue(testInstance.authenticationType == SELF_SIGNED);
        int javaVersion = Integer.parseInt(System.getenv("JAVA_VERSION"));

        // Windows env fails this test consistently for Java 17 and 21. TODO to investigate
        assumeTrue(javaVersion == 8 || javaVersion == 11 || Tools.isLinux());

        // ECC cert generation is broken for Android. "ECDSA KeyPairGenerator is not available"
        assumeFalse(Tools.isAndroid());

        testInstance.setupEccDevice();

        InternalClient client = testInstance.identity.getClient();

        logConnectionStatusChanges(client);
        client.open(true);

        // deviceClient.open() is a no-op on HTTP, so a message needs to be sent to actually test opening the connection
        if (testInstance.protocol == HTTPS)
        {
            client.sendEvent(new Message("some message"));
        }

        client.close();
    }

    @Test(timeout = 60000) // 1 minute
    @IotHubTest
    public void CanOpenMultiplexingConnection() throws Exception
    {
        // MQTT/HTTP don't support multiplexing
        assumeTrue(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS);

        // IoT hub does not support x509 authenticated multiplexed connections
        assumeTrue(testInstance.authenticationType == SAS);

        // IoT hub does not support module multiplexing
        assumeTrue(testInstance.clientType == ClientType.DEVICE_CLIENT);

        MultiplexingClientOptions.MultiplexingClientOptionsBuilder optionsBuilder = MultiplexingClientOptions.builder();
        if (testInstance.useHttpProxy)
        {
            if (testInstance.useHttpProxyAuth)
            {
                Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
                optionsBuilder.proxySettings(new ProxySettings(testProxy, testProxyUser, testProxyPass.toCharArray()));
            }
            else
            {
                Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostnameWithoutAuth, testProxyPortWithoutAuth));
                optionsBuilder.proxySettings(new ProxySettings(testProxy));
            }
        }

        MultiplexingClient multiplexingClient = new MultiplexingClient(hostName, testInstance.protocol, optionsBuilder.build());

        int multiplexCount = 3;
        List<TestIdentity> testIdentities = new ArrayList<>(multiplexCount);
        List<DeviceClient> testClients = new ArrayList<>(multiplexCount);
        for (int i = 0; i < multiplexCount; i++)
        {
            TestIdentity testIdentity = Tools.getTestDevice(iotHubConnectionString, testInstance.protocol, testInstance.authenticationType, false);
            testIdentities.add(testIdentity);
            testClients.add((DeviceClient) testIdentity.getClient());
        }

        try
        {
            multiplexingClient.registerDeviceClients(testClients);

            multiplexingClient.open(true);
        }
        finally
        {
            // Closing here rather than after open() so that a failed or timed out open still gives the client and the
            // three device clients registered to it back. Otherwise they keep retrying for the life of the JVM, and
            // the proxied variants keep retrying through the proxies this class runs locally.
            try
            {
                multiplexingClient.close();
            }
            catch (Exception e)
            {
                // Swallowed so it cannot mask whatever the test itself threw.
                log.error("Failed to close the multiplexing client", e);
            }

            Tools.disposeTestIdentities(testIdentities, iotHubConnectionString);
        }
    }
}
