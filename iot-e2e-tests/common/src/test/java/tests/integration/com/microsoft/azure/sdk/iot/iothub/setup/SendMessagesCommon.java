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
    @Parameterized.Parameters(name = "{0}_{1}_{2}_{3}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        registryClient = new RegistryClient(iotHubConnectionString, RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
        hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString).getHostName();

        List inputs = new ArrayList(Arrays.asList(
                new Object[][]
                        {
                                //sas token device client, no proxy
                                {HTTPS, SAS, ClientType.DEVICE_CLIENT, false},
                                {MQTT, SAS, ClientType.DEVICE_CLIENT, false},
                                {AMQPS, SAS, ClientType.DEVICE_CLIENT, false},
                                {MQTT_WS, SAS, ClientType.DEVICE_CLIENT, false},
                                {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT, false},

                                //x509 device client, no proxy
                                {HTTPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, false},
                                {MQTT, SELF_SIGNED, ClientType.DEVICE_CLIENT, false},
                                {AMQPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, false},

                                //sas token device client, with proxy
                                {MQTT_WS, SAS, ClientType.DEVICE_CLIENT, true},
                                {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT, true},

                                //x509 device client, with proxy
                                {HTTPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, true}
                        }
        ));

        if (!isBasicTierHub)
        {
            inputs.addAll(Arrays.asList(
                    new Object[][]
                            {
                                    //sas token module client, no proxy
                                    {MQTT, SAS, ClientType.MODULE_CLIENT, false},
                                    {AMQPS, SAS, ClientType.MODULE_CLIENT, false},
                                    {MQTT_WS, SAS, ClientType.MODULE_CLIENT, false},
                                    {AMQPS_WS, SAS, ClientType.MODULE_CLIENT, false},

                                    //x509 module client, no proxy
                                    {MQTT, SELF_SIGNED, ClientType.MODULE_CLIENT, false},
                                    {AMQPS, SELF_SIGNED, ClientType.MODULE_CLIENT, false},

                                    //sas token module client, with proxy
                                    {MQTT_WS, SAS, ClientType.MODULE_CLIENT, true},
                                    {AMQPS_WS, SAS, ClientType.MODULE_CLIENT, true}
                            }
            ));
        }

        return inputs;
    }

    protected static final Integer NUM_SMALL_MESSAGES = 50;

    // Max IoT Hub message size is 256 kb, but that includes headers, not just payload
    protected static final int MAX_MESSAGE_PAYLOAD_SIZE = 255*1024;

    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 60 * 1000;

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    protected static String iotHubConnectionString = "";

    protected static String hostName;

    //The messages to be sent in these tests. Some contain error injection messages surrounded by normal messages
    protected List<MessageAndResult> NORMAL_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> LARGE_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> MULTIPLE_SMALL_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> TCP_CONNECTION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> AMQP_CONNECTION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> AMQP_SESSION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> AMQP_METHOD_REQ_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> AMQP_METHOD_RESP_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> AMQP_TWIN_REQ_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> AMQP_TWIN_RESP_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> AMQP_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND = new ArrayList<>();
    protected List<MessageAndResult> MQTT_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND = new ArrayList<>();

    public SendMessagesTestInstance testInstance;

    //How much messages each device will send to the hub for each connection.
    protected static final Integer NUM_MESSAGES_PER_CONNECTION = 6;

    protected static RegistryClient registryClient;
    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8899;

    // Semmle flags this as a security issue, but this is a test username so the warning can be suppressed
    protected static final String testProxyUser = "proxyUsername"; // lgtm

    // Semmle flags this as a security issue, but this is a test password so the warning can be suppressed
    protected static final char[] testProxyPass = "1234".toCharArray(); // lgtm


    public SendMessagesCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, boolean withProxy)
    {
        this.testInstance = new SendMessagesTestInstance(protocol, authenticationType, clientType, withProxy);
    }

    @BeforeClass
    public static void classSetup()
    {
        registryClient = new RegistryClient(iotHubConnectionString, RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
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

    public class SendMessagesTestInstance
    {
        public IotHubClientProtocol protocol;
        public TestIdentity identity;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public String x509Thumbprint;
        public boolean useHttpProxy;

        public SendMessagesTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, boolean useHttpProxy)
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

        public void setup(SSLContext customSSLContext) throws Exception {
            setup(customSSLContext, false);
        }

        public void setup(boolean useCustomSasTokenProvider) throws Exception {
            setup(null, useCustomSasTokenProvider);
        }

        public void setup(SSLContext customSSLContext, boolean useCustomSasTokenProvider) throws Exception
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

            buildMessageLists();
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

    public static class testDevice implements Runnable
    {
        public DeviceClient client;
        public String messageString;
        public String connString;

        public IotHubClientProtocol protocol;
        public Integer numMessagesPerConnection;
        public Integer numConnectionsPerDevice;
        public Integer sendTimeout;
        public Integer numKeys;
        public CountDownLatch latch;
        public AtomicBoolean succeed;

        @Override
        public void run()
        {
            for (int i = 0; i < this.numConnectionsPerDevice; i++)
            {
                try
                {
                    this.openConnection();
                    this.sendMessages();
                    this.closeConnection();
                }
                catch (Exception | AssertionError x)
                {
                    succeed.set(false);
                    System.out.print("testDevice thread: " + x.getMessage());
                }
            }
            latch.countDown();
        }

        public testDevice(Device deviceAmqps, IotHubClientProtocol protocol,
                          Integer numConnectionsPerDevice, Integer numMessagesPerConnection,
                          Integer numKeys, Integer sendTimeout, CountDownLatch latch, AtomicBoolean succeed)
        {
            this.protocol = protocol;
            this.numConnectionsPerDevice = numConnectionsPerDevice;
            this.numMessagesPerConnection = numMessagesPerConnection;
            this.sendTimeout = sendTimeout;
            this.numKeys = numKeys;
            this.latch = latch;

            this.succeed = succeed;
            this.succeed.set(true);

            this.connString = DeviceConnectionString.get(iotHubConnectionString, deviceAmqps);

            messageString = "Java client " + deviceAmqps.getDeviceId() + " test e2e message over AMQP protocol";
        }

        public void openConnection() throws IOException, URISyntaxException, IotHubClientException
        {
            client = new DeviceClient(connString, protocol);
            client.open(false);
        }

        public void sendMessages()
        {
            for (int i = 0; i < numMessagesPerConnection; ++i)
            {
                try
                {
                    Message msgSend = new Message(messageString);
                    msgSend.setProperty("messageCount", Integer.toString(i));
                    for (int j = 0; j < numKeys; j++)
                    {
                        msgSend.setProperty("key"+j, "value"+j);
                    }

                    Success messageSent = new Success();
                    EventCallback callback = new EventCallback(IotHubStatusCode.OK);
                    client.sendEventAsync(msgSend, callback, messageSent);

                    long startTime = System.currentTimeMillis();
                    while(!messageSent.wasCallbackFired())
                    {
                        Thread.sleep(RETRY_MILLISECONDS);
                        if (System.currentTimeMillis() - startTime > sendTimeout)
                        {
                            Assert.fail("Timed out waiting for event callback");
                        }
                    }

                    if (messageSent.getCallbackStatusCode() != IotHubStatusCode.OK)
                    {
                        Assert.fail("Sending message over AMQPS protocol failed: expected OK but received " + messageSent.getCallbackStatusCode());
                    }
                }
                catch (Exception e)
                {
                    Assert.fail("Sending message over AMQPS protocol throws " + e);
                }
            }
        }

        public void closeConnection() throws IOException
        {
            client.close();
        }
    }

    /**
     * Each error injection test will take a list of messages to send. In that list, there will be an error injection message in the middle
     */
    protected void buildMessageLists()
    {
         NORMAL_MESSAGES_TO_SEND = new ArrayList<>();
         TCP_CONNECTION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
         AMQP_CONNECTION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
         AMQP_SESSION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
         AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
         AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
         AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
         AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
         AMQP_METHOD_REQ_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
         AMQP_METHOD_RESP_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
         AMQP_TWIN_REQ_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
         AMQP_TWIN_RESP_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
        AMQP_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND = new ArrayList<>();
        MQTT_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND = new ArrayList<>();
        LARGE_MESSAGES_TO_SEND = new ArrayList<>();
        MULTIPLE_SMALL_MESSAGES_TO_SEND = new ArrayList<>();

        MessageAndResult normalMessageAndExpectedResult = new MessageAndResult(new Message("test message"), IotHubStatusCode.OK);
        for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; i++)
        {
            //error injection should take place in the middle of normal communications
            if (i == (NUM_MESSAGES_PER_CONNECTION / 2))
            {
                //messages that tests should recover from
                Message tcpConnectionDropErrorInjectionMessage = ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                TCP_CONNECTION_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(tcpConnectionDropErrorInjectionMessage, IotHubStatusCode.OK));

                Message amqpConnectionDropErrorInjectionMessage = ErrorInjectionHelper.amqpsConnectionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_CONNECTION_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpConnectionDropErrorInjectionMessage, IotHubStatusCode.OK));

                Message amqpSessionDropErrorInjectionMessage = ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_SESSION_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpSessionDropErrorInjectionMessage, IotHubStatusCode.OK));

                Message amqpCbsRequestLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpCbsRequestLinkDropErrorInjectionMessage, IotHubStatusCode.OK));

                Message amqpCbsResponseLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpCbsResponseLinkDropErrorInjectionMessage, IotHubStatusCode.OK));

                Message amqpC2DLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpC2DLinkDropErrorInjectionMessage, IotHubStatusCode.OK));

                Message amqpD2CLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpD2CLinkDropErrorInjectionMessage, IotHubStatusCode.OK));

                Message amqpMethodReqLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsMethodReqLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_METHOD_REQ_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpMethodReqLinkDropErrorInjectionMessage, IotHubStatusCode.OK));

                Message amqpMethodRespLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_METHOD_RESP_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpMethodRespLinkDropErrorInjectionMessage, IotHubStatusCode.OK));

                Message amqpTwinReqLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsTwinReqLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_TWIN_REQ_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpTwinReqLinkDropErrorInjectionMessage, IotHubStatusCode.OK));

                Message amqpTwinRespLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsTwinRespLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_TWIN_RESP_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpTwinRespLinkDropErrorInjectionMessage, IotHubStatusCode.OK));

                Message amqpGracefulShutdownErrorInjectionMessage = ErrorInjectionHelper.amqpsGracefulShutdownErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND.add(new MessageAndResult(amqpGracefulShutdownErrorInjectionMessage, IotHubStatusCode.OK));

                Message mqttGracefulShutdownErrorInjectionMessage = ErrorInjectionHelper.mqttGracefulShutdownErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                MQTT_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND.add(new MessageAndResult(mqttGracefulShutdownErrorInjectionMessage, IotHubStatusCode.OK));
            }
            else
            {
                TCP_CONNECTION_DROP_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                AMQP_CONNECTION_DROP_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                AMQP_SESSION_DROP_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                AMQP_METHOD_REQ_LINK_DROP_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                AMQP_METHOD_RESP_LINK_DROP_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                AMQP_TWIN_REQ_LINK_DROP_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                AMQP_TWIN_RESP_LINK_DROP_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                AMQP_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
                MQTT_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND.add(normalMessageAndExpectedResult);
            }

            NORMAL_MESSAGES_TO_SEND.add(new MessageAndResult(new Message("test message" + UUID.randomUUID() ), IotHubStatusCode.OK));
            LARGE_MESSAGES_TO_SEND.add(new MessageAndResult(new Message(new byte[MAX_MESSAGE_PAYLOAD_SIZE]), IotHubStatusCode.OK));
        }

        for (int i = 0 ; i < NUM_SMALL_MESSAGES; i++){
            MULTIPLE_SMALL_MESSAGES_TO_SEND.add(new MessageAndResult(new Message("test message" + UUID.randomUUID() ), IotHubStatusCode.OK));
        }
    }

    @After
    public void tearDownTest()
    {
        this.testInstance.dispose();
    }
}
