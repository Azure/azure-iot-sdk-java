/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;


import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.ProxySettings;
import com.microsoft.azure.sdk.iot.device.SasTokenProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.HttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.impl.DefaultHttpProxyServer;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;

/**
 * Utility functions, setup and teardown for all D2C telemetry integration tests. This class should not contain any tests,
 * but any child class should.
 */
@Slf4j
public class SendMessagesCommon extends IntegrationTest
{
    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        registryClient = new RegistryClient(iotHubConnectionString, RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
        hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString).getHostName();

        return Arrays.asList(
            new Object[][]
                {
                    {HTTPS, SAS, ClientType.DEVICE_CLIENT},
                    {AMQPS, SAS, ClientType.DEVICE_CLIENT},
                    {MQTT, SAS, ClientType.DEVICE_CLIENT},
                    {AMQPS, SAS, ClientType.MODULE_CLIENT},
                    {MQTT, SAS, ClientType.MODULE_CLIENT},
                });
    }

    // Max IoT Hub message size is 256 kb, but that includes headers, not just payload
    protected static final int MAX_MESSAGE_PAYLOAD_SIZE = 255*1024;

    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 60 * 1000;

    protected static String iotHubConnectionString = "";

    protected static String hostName;

    public SendMessagesTestInstance testInstance;

    protected static RegistryClient registryClient;

    public SendMessagesCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType)
    {
        this.testInstance = new SendMessagesTestInstance(protocol, authenticationType, clientType);
    }

    @BeforeClass
    public static void classSetup()
    {
        registryClient = new RegistryClient(iotHubConnectionString, RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
    }

    public class SendMessagesTestInstance
    {
        public IotHubClientProtocol protocol;
        public TestIdentity identity;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public String x509Thumbprint;

        public SendMessagesTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType)
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();
        }

        public void setup() throws Exception
        {
            SSLContext sslContext = SSLContextBuilder.buildSSLContext(x509CertificateGenerator.getX509Certificate(), x509CertificateGenerator.getPrivateKey());
            setup(sslContext);
        }

        public void setup(SSLContext customSSLContext) throws Exception {
            setup(customSSLContext, false);
        }

        public void setup(boolean useCustomSasTokenProvider) throws Exception {
            setup(null, useCustomSasTokenProvider);
        }

        public void setup(SSLContext customSSLContext, boolean useCustomSasTokenProvider) throws Exception
        {
            ClientOptions.ClientOptionsBuilder optionsBuilder = ClientOptions.builder();

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                this.identity = Tools.getTestDevice(iotHubConnectionString, this.protocol, this.authenticationType, false, optionsBuilder);

                if (customSSLContext != null)
                {
                    ClientOptions options = optionsBuilder.sslContext(customSSLContext).build();
                    DeviceClient clientWithCustomSSLContext = new DeviceClient(Tools.getDeviceConnectionString(iotHubConnectionString, testInstance.identity.getDevice()), protocol, options);
                    ((TestDeviceIdentity)this.identity).setDeviceClient(clientWithCustomSSLContext);
                }
                else if (useCustomSasTokenProvider)
                {
                    SasTokenProvider sasTokenProvider = new SasTokenProviderImpl(Tools.getDeviceConnectionString(iotHubConnectionString, this.identity.getDevice()));
                    DeviceClient clientWithCustomSasTokenProvider = new DeviceClient(hostName, testInstance.identity.getDeviceId(), sasTokenProvider, protocol, null);
                    ((TestDeviceIdentity)this.identity).setDeviceClient(clientWithCustomSasTokenProvider);
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

    @After
    public void tearDownTest()
    {
        this.testInstance.dispose();
    }
}
