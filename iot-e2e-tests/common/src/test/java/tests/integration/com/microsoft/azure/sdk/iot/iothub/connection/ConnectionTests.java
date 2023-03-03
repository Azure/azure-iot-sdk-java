package tests.integration.com.microsoft.azure.sdk.iot.iothub.connection;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
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
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Collection;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;

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
        public String x509Thumbprint;
        public boolean useHttpProxy;

        public ConnectionTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, boolean useHttpProxy)
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();
            this.useHttpProxy = useHttpProxy;
        }

        public void setup() throws Exception
        {
            SSLContext sslContext = SSLContextBuilder.buildSSLContext(x509CertificateGenerator.getX509Certificate(), x509CertificateGenerator.getPrivateKey());
            setup(sslContext);
        }

        public void setup(SSLContext customSSLContext) throws Exception
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

                if (customSSLContext != null)
                {
                    ClientOptions options = optionsBuilder.sslContext(customSSLContext).build();
                    DeviceClient clientWithCustomSSLContext = new DeviceClient(Tools.getDeviceConnectionString(iotHubConnectionString, testInstance.identity.getDevice()), protocol, options);
                    ((TestDeviceIdentity)this.identity).setDeviceClient(clientWithCustomSSLContext);
                }
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                this.identity = Tools.getTestModule(iotHubConnectionString, this.protocol, this.authenticationType , false, optionsBuilder);
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

    @Test(timeout = 10000) // 10 seconds
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
}
