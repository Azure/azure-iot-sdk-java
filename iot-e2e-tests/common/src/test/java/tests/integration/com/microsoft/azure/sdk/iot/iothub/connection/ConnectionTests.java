package tests.integration.com.microsoft.azure.sdk.iot.iothub.connection;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.Module;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.HttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.impl.DefaultHttpProxyServer;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

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
    protected static String iotHubConnectionString;

    @Parameterized.Parameters(name = "{0}_{1}_{2}_{3}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

        return Arrays.asList(
            new Object[][]
                {
                    {HTTPS, SAS, ClientType.DEVICE_CLIENT, false},
                    {AMQPS, SAS, ClientType.DEVICE_CLIENT, false},
                    {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT, false},
                    {MQTT, SAS, ClientType.DEVICE_CLIENT, false},
                    {MQTT_WS, SAS, ClientType.DEVICE_CLIENT, false},

                    {HTTPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, false},
                    {AMQPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, false},
                    {MQTT, SELF_SIGNED, ClientType.DEVICE_CLIENT, false},
                    {MQTT_WS, SELF_SIGNED, ClientType.DEVICE_CLIENT, false},

                    {AMQPS, SAS, ClientType.MODULE_CLIENT, false},
                    {AMQPS_WS, SAS, ClientType.MODULE_CLIENT, false},
                    {MQTT, SAS, ClientType.MODULE_CLIENT, false},
                    {MQTT_WS, SAS, ClientType.MODULE_CLIENT, false},

                    {AMQPS, SELF_SIGNED, ClientType.MODULE_CLIENT, false},
                    {MQTT, SELF_SIGNED, ClientType.MODULE_CLIENT, false},
                    {MQTT_WS, SELF_SIGNED, ClientType.MODULE_CLIENT, false},

                    {HTTPS, SAS, ClientType.DEVICE_CLIENT, true},
                    {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT, true},
                    {MQTT_WS, SAS, ClientType.DEVICE_CLIENT, true},
                    {MQTT_WS, SELF_SIGNED, ClientType.DEVICE_CLIENT, true},
                    {AMQPS_WS, SAS, ClientType.MODULE_CLIENT, true},
                    {MQTT_WS, SAS, ClientType.MODULE_CLIENT, true},
                    {MQTT_WS, SELF_SIGNED, ClientType.MODULE_CLIENT, true},
                });
    }

    public ConnectionTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, boolean withProxy)
    {
        this.testInstance = new ConnectionTestInstance(protocol, authenticationType, clientType, withProxy);
    }

    public class ConnectionTestInstance
    {
        public IotHubClientProtocol protocol;
        public TestIdentity identity;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public boolean useHttpProxy;

        public ConnectionTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, boolean useHttpProxy)
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.useHttpProxy = useHttpProxy;
        }

        public void setup() throws Exception
        {
            ClientOptions.ClientOptionsBuilder optionsBuilder = ClientOptions.builder();
            if (this.useHttpProxy)
            {
                Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
                optionsBuilder.proxySettings(new ProxySettings(testProxy, testProxyUser, testProxyPass));
            }

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                this.identity = Tools.getTestDevice(iotHubConnectionString, this.protocol, this.authenticationType, false, optionsBuilder);
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                this.identity = Tools.getTestModule(iotHubConnectionString, this.protocol, this.authenticationType , false, optionsBuilder);
            }
        }

        public void setupEccDevice() throws Exception
        {
            ClientOptions.ClientOptionsBuilder optionsBuilder = ClientOptions.builder();
            if (this.useHttpProxy)
            {
                Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
                optionsBuilder.proxySettings(new ProxySettings(testProxy, testProxyUser, testProxyPass));
            }

            X509CertificateGenerator certificateGenerator = new X509CertificateGenerator(X509CertificateGenerator.CertificateAlgorithm.ECC);
            SSLContext sslContext = SSLContextBuilder.buildSSLContext(certificateGenerator.getX509Certificate(), certificateGenerator.getPrivateKey());
            optionsBuilder.sslContext(sslContext);

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                Device eccDevice = new Device("ecc-test-device-" + UUID.randomUUID(), SELF_SIGNED);
                eccDevice.setThumbprint(certificateGenerator.getX509Thumbprint(), certificateGenerator.getX509Thumbprint());

                Tools.addDeviceWithRetry(new RegistryClient(iotHubConnectionString), eccDevice);

                String deviceConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, eccDevice);
                this.identity = new TestDeviceIdentity(
                    new DeviceClient(deviceConnectionString, testInstance.protocol, optionsBuilder.build()),
                    eccDevice);
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                Device eccDevice = new Device("ecc-test-device-" + UUID.randomUUID(), SELF_SIGNED);
                Module eccModule = new Module(eccDevice.getDeviceId(), "ecc-test-module-" + UUID.randomUUID(), SELF_SIGNED);
                eccDevice.setThumbprint(certificateGenerator.getX509Thumbprint(), certificateGenerator.getX509Thumbprint());
                eccModule.setThumbprint(certificateGenerator.getX509Thumbprint(), certificateGenerator.getX509Thumbprint());

                Tools.addDeviceWithRetry(new RegistryClient(iotHubConnectionString), eccDevice);
                Tools.addModuleWithRetry(new RegistryClient(iotHubConnectionString), eccModule);

                String moduleConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, eccDevice) + ";ModuleId=" + eccModule.getId();
                this.identity = new TestModuleIdentity(
                    new ModuleClient(moduleConnectionString, testInstance.protocol, optionsBuilder.build()),
                    eccDevice,
                    eccModule);
            }
        }

        public void dispose()
        {
            if (this.identity != null && this.identity.getClient() != null)
            {
                this.identity.getClient().close();
            }

            Tools.disposeTestIdentity(this.identity, iotHubConnectionString);
        }
    }

    private final ConnectionTestInstance testInstance;
    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8899;

    // Semmle flags this as a security issue, but this is a test username so the warning can be suppressed
    protected static final String testProxyUser = "proxyUsername"; // lgtm

    // Semmle flags this as a security issue, but this is a test password so the warning can be suppressed
    protected static final char[] testProxyPass = "1234".toCharArray(); // lgtm

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

    @Test(timeout = 60000) // 1 minute
    public void CanOpenConnection() throws Exception
    {
        testInstance.setup();
        testInstance.identity.getClient().open(true);

        // deviceClient.open() is a no-op on HTTP, so a message needs to be sent to actually test opening the connection
        if (testInstance.protocol == HTTPS)
        {
            testInstance.identity.getClient().sendEvent(new Message("some message"));
        }

        testInstance.identity.getClient().close();
    }

    @Test(timeout = 60000) // 1 minute
    public void CanOpenConnectionWithECCCertificates() throws Exception
    {
        // SAS token authenticated devices/modules don't use RSA or ECC certificates
        assumeTrue(testInstance.authenticationType == SELF_SIGNED);

        // ECC cert generation is broken for Android. "ECDSA KeyPairGenerator is not available"
        assumeFalse(Tools.isAndroid());

        testInstance.setupEccDevice();

        testInstance.identity.getClient().open(true);

        // deviceClient.open() is a no-op on HTTP, so a message needs to be sent to actually test opening the connection
        if (testInstance.protocol == HTTPS)
        {
            testInstance.identity.getClient().sendEvent(new Message("some message"));
        }

        testInstance.identity.getClient().close();
    }
}
