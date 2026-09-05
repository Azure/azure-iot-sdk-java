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

        // Set once setupEccDevice starts creating an identity, and cleared only when a new attempt begins.
        // Identity classification has to outlive eccDeviceIdToDelete: teardown claims and clears that id, so if
        // teardown runs between the device being registered and the identity being published, the late
        // disposeIfTeardownAlreadyRan would see an identity with no ecc id and hand a self signed identity to the
        // shared x509 pool.
        private boolean identityIsEcc;

        // Which setup attempt this instance is currently serving, and how far teardown has run.
        //
        // RerunFailedTestRule reuses this instance for every attempt at a test, so lifecycle state cannot be a plain
        // "teardown has happened" flag. It was, and the result was that once the first attempt's @After had run,
        // every later attempt disposed its own freshly acquired client the moment setup published it, and open()
        // then failed with "Client was closed while attempting to open the connection". One flaky timeout became a
        // guaranteed failure of every remaining attempt.
        //
        // Each setup takes a generation number, and teardown records the generation it covered. A setup only cleans
        // up after itself if teardown has already run for its own generation, so an earlier attempt's teardown can
        // no longer reach into a later attempt's.
        private long setupGeneration;
        private long disposedThrough;

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
            long generation = beginSetup();

            ClientOptions.ClientOptionsBuilder optionsBuilder = ClientOptions.builder();
            applyProxySettings(optionsBuilder);

            log.info("Acquiring test identity");

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                trackForCleanup(generation, Tools.getTestDevice(iotHubConnectionString, this.protocol, this.authenticationType, false, optionsBuilder), false);
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                trackForCleanup(generation, Tools.getTestModule(iotHubConnectionString, this.protocol, this.authenticationType , false, optionsBuilder), false);
            }

            log.info("Test identity acquired");

            disposeIfTeardownAlreadyRan(generation);
        }

        public void setupEccDevice() throws Exception
        {
            long generation = beginSetup();

            // Marked before anything is created, so that every identity this method goes on to publish is classified
            // as ECC even if teardown runs partway through.
            synchronized (lifecycleLock)
            {
                this.identityIsEcc = true;
            }

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
                trackEccDeviceForCleanup(generation, eccDevice.getDeviceId());

                String deviceConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, eccDevice);
                trackForCleanup(generation, new TestDeviceIdentity(
                    new DeviceClient(deviceConnectionString, testInstance.protocol, optionsBuilder.build()),
                    eccDevice), true);
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                Device eccDevice = new Device("ecc-test-device-" + UUID.randomUUID(), SELF_SIGNED);
                Module eccModule = new Module(eccDevice.getDeviceId(), "ecc-test-module-" + UUID.randomUUID(), SELF_SIGNED);
                eccDevice.setThumbprint(certificateGenerator.getX509Thumbprint(), certificateGenerator.getX509Thumbprint());
                eccModule.setThumbprint(certificateGenerator.getX509Thumbprint(), certificateGenerator.getX509Thumbprint());

                Tools.addDeviceWithRetry(new RegistryClient(iotHubConnectionString), eccDevice);
                trackEccDeviceForCleanup(generation, eccDevice.getDeviceId());

                Tools.addModuleWithRetry(new RegistryClient(iotHubConnectionString), eccModule);

                String moduleConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, eccDevice) + ";ModuleId=" + eccModule.getId();
                trackForCleanup(generation, new TestModuleIdentity(
                    new ModuleClient(moduleConnectionString, testInstance.protocol, optionsBuilder.build()),
                    eccDevice,
                    eccModule), true);
            }

            disposeIfTeardownAlreadyRan(generation);
        }

        /**
         * Begin a new setup attempt and take its generation number.
         *
         * <p>Reclaims anything a previous attempt left behind first. Normally there is nothing: that attempt's
         * {@code @After} already claimed and cleared what it owned. Anything still present is residue from a setup
         * abandoned by the timeout, and disposing it here is the last chance to reclaim it.</p>
         *
         * @return The generation number this setup attempt owns
         */
        private long beginSetup()
        {
            dispose();

            synchronized (lifecycleLock)
            {
                return ++this.setupGeneration;
            }
        }

        /**
         * Hand an identity to teardown, and publish it for the test body to use.
         *
         * @param generation The generation of the setup attempt that produced this identity
         * @param newIdentity The identity this test just acquired or created
         * @param isEcc Whether this identity was created by setupEccDevice
         */
        private void trackForCleanup(long generation, TestIdentity newIdentity, boolean isEcc)
        {
            boolean superseded;
            synchronized (lifecycleLock)
            {
                superseded = generation != this.setupGeneration;

                if (!superseded)
                {
                    this.identityToDispose = newIdentity;

                    if (isEcc)
                    {
                        this.identityIsEcc = true;
                    }
                }
            }

            if (superseded)
            {
                // A setup the timeout abandoned finished after a later attempt had already started. Publishing now
                // would give this instance an identity the running attempt is not using, and lose the one it is.
                disposeSupersededIdentity(newIdentity, isEcc);
                return;
            }

            // Published for the test body. Never cleared, so a thread the JUnit timeout abandoned can keep reading it.
            this.identity = newIdentity;
        }

        /**
         * Hand a freshly registered ECC device to teardown, so it is removed from the registry even if the rest of
         * setupEccDevice never completes.
         *
         * @param generation The generation of the setup attempt that registered this device
         * @param deviceId The device id that was just added to the registry
         */
        private void trackEccDeviceForCleanup(long generation, String deviceId)
        {
            boolean superseded;
            synchronized (lifecycleLock)
            {
                superseded = generation != this.setupGeneration;

                if (!superseded)
                {
                    this.eccDeviceIdToDelete = deviceId;
                }
            }

            if (superseded)
            {
                removeEccDevice(deviceId);
            }
        }

        /**
         * Close and discard an identity produced by a setup attempt that has already been superseded.
         *
         * @param supersededIdentity The identity to reclaim
         * @param isEcc Whether it was created by setupEccDevice, and so must never be recycled
         */
        private void disposeSupersededIdentity(TestIdentity supersededIdentity, boolean isEcc)
        {
            log.debug("Reclaiming identity {} from a superseded setup attempt", supersededIdentity.getDeviceId());

            if (supersededIdentity.getClient() != null)
            {
                supersededIdentity.getClient().close();
            }

            if (isEcc)
            {
                removeEccDevice(supersededIdentity.getDeviceId());
            }
            else
            {
                Tools.disposeTestIdentity(supersededIdentity, iotHubConnectionString);
            }
        }

        /**
         * Remove an ECC device from the registry. These are never recycled: they are self signed with a certificate
         * no other test knows about, so returning one to the shared x509 pool would fail a later test.
         *
         * @param deviceId The device to remove
         */
        private void removeEccDevice(String deviceId)
        {
            try
            {
                Tools.getRegistyManager(iotHubConnectionString).removeDevice(deviceId);
            }
            catch (IOException | IotHubException e)
            {
                log.error("Failed to clean up ECC test device {}", deviceId, e);
            }
        }

        /**
         * Dispose anything registered after teardown already ran for this setup's generation.
         *
         * <p>Every test in this class is bounded by a timeout, the two minute one that {@link IntegrationTest}
         * applies. JUnit runs the test body on a
         * separate thread and, when the timeout fires, abandons that thread while it is still running. {@code @After}
         * is outside the timeout, so teardown can execute while setup on the abandoned thread has not finished
         * acquiring its identity. Without this, the identity that setup goes on to produce would have no owner and
         * would leak, which is precisely the leak this class is trying to stop.</p>
         *
         * <p>The generation matters. A rerun reuses this instance, so an unqualified "teardown has run" would still
         * be set when the next attempt started, and that attempt would dispose its own client the moment it
         * published it.</p>
         *
         * <p>This does not wait for setup, in either direction. Blocking teardown on a setup that is itself hung -
         * which is how these tests have actually timed out - would stall the rest of the run.</p>
         *
         * @param generation The generation of the setup attempt that is finishing
         */
        private void disposeIfTeardownAlreadyRan(long generation)
        {
            boolean teardownAlreadyRan;
            synchronized (lifecycleLock)
            {
                teardownAlreadyRan = this.disposedThrough >= generation;
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
            boolean wasEcc;

            synchronized (lifecycleLock)
            {
                // Teardown covers every setup that has begun so far, and nothing later. A setup that starts after
                // this takes a higher generation and is unaffected.
                this.disposedThrough = this.setupGeneration;

                identityToClean = this.identityToDispose;
                eccDeviceIdToClean = this.eccDeviceIdToDelete;
                wasEcc = this.identityIsEcc;

                this.identityToDispose = null;
                this.eccDeviceIdToDelete = null;
                this.identityIsEcc = false;
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
                removeEccDevice(eccDeviceIdToClean);
            }
            else if (identityToClean != null && wasEcc)
            {
                // An earlier dispose already claimed and deleted the device id, and this identity was published after
                // that. The device is gone from the registry, so there is nothing left to delete, but it must still
                // not be recycled: it is self signed with a certificate no other test knows about.
                log.debug("Discarding late ECC identity {} rather than recycling it", identityToClean.getDeviceId());
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

    // These tests deliberately do not set their own timeout, and take the two minute one from IntegrationTest instead.
    //
    // They used to declare @Test(timeout = 60000). That is exactly Mqtt.CONNECTION_TIMEOUT, the budget the client gives
    // a single MQTT CONNECT round trip, so the two expired at the same instant. When a connect attempt stalled, the
    // client sat in connectToken.waitForCompletion for the full minute, and JUnit killed the test on the same tick that
    // the client would have thrown and let its retry policy try again. The failure that came out was a bare
    // TestTimedOutException with no connection status transition logged at all, because nothing had been allowed to
    // happen yet.
    //
    // A test bounded by the same number as the component it exercises cannot observe that component retrying. Two
    // minutes leaves room for one stalled attempt to expire and a retry to follow it.
    @Test
    @IotHubTest
    public void CanOpenConnection() throws Exception
    {
        testInstance.setup();

        // Held locally rather than read back off testInstance for each call. The @After that disposes this instance
        // runs outside this method's timeout, so it can execute while this thread is still here after a timeout.
        InternalClient client = testInstance.identity.getClient();

        logConnectionStatusChanges(client);

        // Bracketing the open so a timeout can be attributed. Silence after "Acquiring test identity" and before this
        // line means setup was stuck getting an identity; silence after this line means the connect itself stalled.
        log.info("Opening client");
        client.open(true);
        log.info("Client opened");

        // deviceClient.open() is a no-op on HTTP, so a message needs to be sent to actually test opening the connection
        if (testInstance.protocol == HTTPS)
        {
            client.sendEvent(new Message("some message"));
        }

        client.close();
    }

    @IotHubTest
    @StandardTierHubOnlyTest
    @Test
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

        // Bracketing the open so a timeout can be attributed. Silence after "Acquiring test identity" and before this
        // line means setup was stuck getting an identity; silence after this line means the connect itself stalled.
        log.info("Opening client");
        client.open(true);
        log.info("Client opened");

        // deviceClient.open() is a no-op on HTTP, so a message needs to be sent to actually test opening the connection
        if (testInstance.protocol == HTTPS)
        {
            client.sendEvent(new Message("some message"));
        }

        client.close();
    }

    @Test
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

        // The acquisition loop is inside the protected scope because it can throw partway through. Acquiring the
        // second or third identity can fail after the first is already in the list, and this method never assigns
        // testInstance.identity, so the @After cannot reclaim them. Everything acquired so far is disposed instead.
        try
        {
            for (int i = 0; i < multiplexCount; i++)
            {
                TestIdentity testIdentity = Tools.getTestDevice(iotHubConnectionString, testInstance.protocol, testInstance.authenticationType, false);
                testIdentities.add(testIdentity);
                testClients.add((DeviceClient) testIdentity.getClient());
            }

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
