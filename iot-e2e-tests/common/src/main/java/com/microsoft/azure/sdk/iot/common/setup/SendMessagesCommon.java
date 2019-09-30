/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.common.setup;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.jproxy.ProxyServer;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static junit.framework.TestCase.fail;

/**
 * Utility functions, setup and teardown for all D2C telemetry integration tests. This class should not contain any tests,
 * but any child class should.
 */
public class SendMessagesCommon extends IntegrationTest
{
    //How much sequential connections each device will open and close in the multithreaded test.
    protected static final Integer NUM_CONNECTIONS_PER_DEVICE = 5;

    //How much devices the multithreaded test will create in parallel.
    protected static final Integer MAX_DEVICE_PARALLEL = 3;

    //How many keys each message will cary.
    protected static final Integer NUM_KEYS_PER_MESSAGE = 3;

    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 60 * 1000;

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    protected static String iotHubConnectionString = "";

    protected static String hostName;

    //The messages to be sent in these tests. Some contain error injection messages surrounded by normal messages
    protected List<MessageAndResult> NORMAL_MESSAGES_TO_SEND = new ArrayList<>();
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

    protected static RegistryManager registryManager;
    protected static ProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8899;

    public SendMessagesCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint, boolean withProxy) throws Exception
    {
        this.testInstance = new SendMessagesTestInstance(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint, withProxy);
    }

    @BeforeClass
    public static void classSetup()
    {
        try
        {
            registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
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
        proxyServer = ProxyServer.create(testProxyHostname, testProxyPort);
        try
        {
            proxyServer.start(ex -> {});
        }
        catch (IOException e)
        {
            fail("Failed to start the test proxy");
        }
    }

    @AfterClass
    public static void stopProxy()
    {
        try
        {
            proxyServer.stop();
        }
        catch (IOException e)
        {
            fail("Failed to stop the test proxy");
        }
    }

    protected static Collection inputsCommon() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException, InterruptedException
    {
        return inputsCommon(ClientType.DEVICE_CLIENT, ClientType.MODULE_CLIENT);
    }

    protected static Collection inputsCommon(ClientType... clientTypes) throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException, InterruptedException
    {
        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        return inputsCommon(certificateGenerator.getPublicCertificate(), certificateGenerator.getPrivateKey(), certificateGenerator.getX509Thumbprint(), clientTypes);
    }

    protected static Collection inputsCommon(String publicKeyCert, String privateKey, String x509Thumbprint, ClientType... clientTypes) throws IOException, InterruptedException
    {
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();

        List inputs = new ArrayList();
        for (ClientType clientType : clientTypes)
        {
            if (clientType == ClientType.DEVICE_CLIENT)
            {
                inputs.addAll(Arrays.asList(
                        new Object[][]
                                {
                                    //sas token device client, no proxy
                                    {HTTPS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},
                                    {MQTT, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},
                                    {AMQPS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},
                                    {MQTT_WS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},
                                    {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},

                                    //x509 device client, no proxy
                                    {HTTPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},
                                    {MQTT, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},
                                    {AMQPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},

                                    //sas token device client, with proxy
                                    {MQTT_WS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, true},
                                    {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, true},

                                    //x509 device client, with proxy
                                    {HTTPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, true}
                            }
                ));
            }
            else
            {
                inputs.addAll(Arrays.asList(
                        new Object[][]
                                {
                                        //sas token module client, no proxy
                                        {MQTT, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},
                                        {AMQPS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},
                                        {MQTT_WS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},
                                        {AMQPS_WS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},

                                        //x509 module client, no proxy
                                        {MQTT, SELF_SIGNED, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},
                                        {AMQPS, SELF_SIGNED, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, false},

                                        //sas token module client, with proxy
                                        {MQTT_WS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, true},
                                        {AMQPS_WS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint, true}
                                }
                ));
            }
        }


        Thread.sleep(2000);

        return inputs;
    }

    public class SendMessagesTestInstance
    {
        public InternalClient client;
        public IotHubClientProtocol protocol;
        public BaseDevice identity;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public boolean useHttpProxy;

        public SendMessagesTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint, boolean useHttpProxy) throws Exception
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.publicKeyCert = publicKeyCert;
            this.privateKey = privateKey;
            this.x509Thumbprint = x509Thumbprint;
            this.useHttpProxy = useHttpProxy;
        }

        public void setup() throws Exception {
            String TEST_UUID = UUID.randomUUID().toString();

            /* Create unique device names */
            String deviceId = "java-method-e2e-test-device".concat("-" + TEST_UUID);
            String moduleId = "java-method-e2e-test-module".concat("-" + TEST_UUID);
            String deviceX509Id = "java-method-e2e-test-device-x509".concat("-" + TEST_UUID);
            String moduleX509Id = "java-method-e2e-test-module-x509".concat("-" + TEST_UUID);

            /* Create device on the service */
            Device device = Device.createFromId(deviceId, null, null);
            Module module = Module.createFromId(deviceId, moduleId, null);

            Device deviceX509 = Device.createDevice(deviceX509Id, AuthenticationType.SELF_SIGNED);
            deviceX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
            Module moduleX509 = Module.createModule(deviceX509Id, moduleX509Id, AuthenticationType.SELF_SIGNED);
            moduleX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
            device = Tools.addDeviceWithRetry(registryManager, device);
            deviceX509 = Tools.addDeviceWithRetry(registryManager, deviceX509);

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                if (authenticationType == SAS)
                {
                    //sas device client
                    this.client = new DeviceClient(registryManager.getDeviceConnectionString(device), protocol);
                    this.identity = device;
                }
                else if (authenticationType == SELF_SIGNED)
                {
                    //x509 device client
                    this.client = new DeviceClient(registryManager.getDeviceConnectionString(deviceX509), protocol, publicKeyCert, false, privateKey, false);
                    this.identity = deviceX509;
                }
                else
                {
                    throw new Exception("Test code has not been written for this path yet");
                }
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                if (authenticationType == SAS)
                {
                    //sas module client
                    module = Tools.addModuleWithRetry(registryManager, module);
                    this.client = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), protocol);
                    this.identity = module;
                }
                else if (authenticationType == SELF_SIGNED)
                {
                    //x509 module client
                    moduleX509 = Tools.addModuleWithRetry(registryManager, moduleX509);
                    this.client = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), protocol, publicKeyCert, false, privateKey, false);
                    this.identity = moduleX509;
                }
                else
                {
                    throw new Exception("Test code has not been written for this path yet");
                }
            }

            if (this.useHttpProxy)
            {
                Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
                ProxySettings proxySettings = new ProxySettings(testProxy);
                this.client.setProxySettings(proxySettings);
            }

            Thread.sleep(2000);

            buildMessageLists();
        }

        public void dispose()
        {
            try
            {
                this.client.closeNow();
                registryManager.removeDevice(this.identity.getDeviceId()); //removes all modules associated with this device, too
            }
            catch (Exception e)
            {
                //not a big deal if dispose fails. This test suite is not testing the functions in this cleanup.
                // If identities are left registered, they will be deleted my nightly cleanup job anyways
            }
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

        public void openConnection() throws IOException, URISyntaxException, InterruptedException
        {
            client = new DeviceClient(connString, protocol);
            client.open();
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
                    EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
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

                    if (messageSent.getCallbackStatusCode() != IotHubStatusCode.OK_EMPTY)
                    {
                        Assert.fail("Sending message over AMQPS protocol failed: expected OK_EMPTY but received " + messageSent.getCallbackStatusCode());
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
            client.closeNow();
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

        MessageAndResult normalMessageAndExpectedResult = new MessageAndResult(new Message("test message"), IotHubStatusCode.OK_EMPTY);
        for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; i++)
        {
            //error injection should take place in the middle of normal communications
            if (i == (NUM_MESSAGES_PER_CONNECTION / 2))
            {
                //messages that tests should recover from
                Message tcpConnectionDropErrorInjectionMessage = ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                TCP_CONNECTION_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(tcpConnectionDropErrorInjectionMessage, null));

                Message amqpConnectionDropErrorInjectionMessage = ErrorInjectionHelper.amqpsConnectionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_CONNECTION_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpConnectionDropErrorInjectionMessage, null));

                Message amqpSessionDropErrorInjectionMessage = ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_SESSION_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpSessionDropErrorInjectionMessage, null));

                Message amqpCbsRequestLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpCbsRequestLinkDropErrorInjectionMessage, null));

                Message amqpCbsResponseLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpCbsResponseLinkDropErrorInjectionMessage, null));

                Message amqpC2DLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpC2DLinkDropErrorInjectionMessage, null));

                Message amqpD2CLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpD2CLinkDropErrorInjectionMessage, null));

                Message amqpMethodReqLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsMethodReqLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_METHOD_REQ_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpMethodReqLinkDropErrorInjectionMessage, null));

                Message amqpMethodRespLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_METHOD_RESP_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpMethodRespLinkDropErrorInjectionMessage, null));

                Message amqpTwinReqLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsTwinReqLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_TWIN_REQ_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpTwinReqLinkDropErrorInjectionMessage, null));

                Message amqpTwinRespLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsTwinRespLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_TWIN_RESP_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpTwinRespLinkDropErrorInjectionMessage, null));

                Message amqpGracefulShutdownErrorInjectionMessage = ErrorInjectionHelper.amqpsGracefulShutdownErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND.add(new MessageAndResult(amqpGracefulShutdownErrorInjectionMessage, null));

                Message mqttGracefulShutdownErrorInjectionMessage = ErrorInjectionHelper.mqttGracefulShutdownErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                MQTT_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND.add(new MessageAndResult(mqttGracefulShutdownErrorInjectionMessage, null));
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

            NORMAL_MESSAGES_TO_SEND.add(new MessageAndResult(new Message("test message"), IotHubStatusCode.OK_EMPTY));
        }
    }

    @After
    public void tearDownTest()
    {
        this.testInstance.dispose();
    }
}
