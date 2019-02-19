/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.common.setup;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.IOException;
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
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 180000;

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    protected static String iotHubConnectionString = "";
    protected static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;

    protected static String hostName;

    //The messages to be sent in these tests. Some contain error injection messages surrounded by normal messages
    protected static final List<MessageAndResult> NORMAL_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> TCP_CONNECTION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> AMQP_CONNECTION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> AMQP_SESSION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> AMQP_METHOD_REQ_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> AMQP_METHOD_RESP_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> AMQP_TWIN_REQ_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> AMQP_TWIN_RESP_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> AMQP_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND = new ArrayList<>();
    protected static final List<MessageAndResult> MQTT_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND = new ArrayList<>();

    public SendMessagesTestInstance testInstance;

    //How much messages each device will send to the hub for each connection.
    protected static final Integer NUM_MESSAGES_PER_CONNECTION = 6;

    protected static final AtomicBoolean succeed = new AtomicBoolean();

    protected static RegistryManager registryManager;

    public SendMessagesCommon(InternalClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        this.testInstance = new SendMessagesTestInstance(client, protocol, identity, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
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

    protected static Collection inputsCommon(ClientType clientType) throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException, InterruptedException, OperatorCreationException
    {
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        String uuid = UUID.randomUUID().toString();
        String deviceId = "java-device-client-e2e-test-send-messages".concat("-" + uuid);
        String deviceIdX509 = "java-device-client-e2e-test-send-messages-X509".concat("-" + uuid);
        String moduleId = "java-module-client-e2e-test-send-messages".concat("-" + uuid);
        String moduleIdX509 = "java-module-client-e2e-test-send-messages-X509".concat("-" + uuid);

        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        String publicCertificate = certificateGenerator.getPublicCertificate();
        String privateKey = certificateGenerator.getPrivateKey();
        String thumbprint = certificateGenerator.getX509Thumbprint();

        Device device = Device.createFromId(deviceId, null, null);
        Device deviceX509 = Device.createDevice(deviceIdX509, SELF_SIGNED);

        Module module = Module.createFromId(deviceId, moduleId, null);
        Module moduleX509 = Module.createModule(deviceIdX509, moduleIdX509, SELF_SIGNED);

        deviceX509.setThumbprintFinal(thumbprint, thumbprint);
        moduleX509.setThumbprintFinal(thumbprint, thumbprint);

        Tools.addDeviceWithRetry(registryManager, device);
        Tools.addDeviceWithRetry(registryManager, deviceX509);

        if (clientType == ClientType.MODULE_CLIENT)
        {
            Tools.addModuleWithRetry(registryManager, module);
            Tools.addModuleWithRetry(registryManager, moduleX509);
        }

        buildMessageLists();

        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();

        List inputs;
        if (clientType == ClientType.DEVICE_CLIENT)
        {
            inputs = Arrays.asList(
                    new Object[][]
                            {
                                    //sas token device client
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), HTTPS), HTTPS, device, SAS, ClientType.DEVICE_CLIENT, publicCertificate, privateKey, thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), MQTT), MQTT, device, SAS, ClientType.DEVICE_CLIENT, publicCertificate, privateKey, thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), MQTT_WS), MQTT_WS, device, SAS, ClientType.DEVICE_CLIENT, publicCertificate, privateKey, thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS), AMQPS, device, SAS, ClientType.DEVICE_CLIENT, publicCertificate, privateKey, thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS_WS), AMQPS_WS, device, SAS, ClientType.DEVICE_CLIENT, publicCertificate, privateKey, thumbprint},

                                    //x509 device client
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), HTTPS, publicCertificate, false, privateKey, false), HTTPS, deviceX509, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicCertificate, privateKey, thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), MQTT, publicCertificate, false, privateKey, false), MQTT, deviceX509, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicCertificate, privateKey, thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), AMQPS, publicCertificate, false, privateKey, false), AMQPS, deviceX509, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicCertificate, privateKey, thumbprint}
                            }
            );
        }
        else
        {
            inputs = Arrays.asList(
                    new Object[][]
                            {
                                    //sas token module client
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), MQTT), MQTT, module, SAS, ClientType.MODULE_CLIENT, publicCertificate, privateKey, thumbprint},
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), MQTT_WS), MQTT_WS, module, SAS, ClientType.MODULE_CLIENT, publicCertificate, privateKey, thumbprint},
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), AMQPS), AMQPS, module, SAS, ClientType.MODULE_CLIENT, publicCertificate, privateKey, thumbprint},
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), AMQPS_WS), AMQPS_WS, module, SAS, ClientType.MODULE_CLIENT, publicCertificate, privateKey, thumbprint},

                                    //x509 module client
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), MQTT, publicCertificate, false, privateKey, false), MQTT, moduleX509, SELF_SIGNED, ClientType.MODULE_CLIENT, publicCertificate, privateKey, thumbprint},
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), AMQPS, publicCertificate, false, privateKey, false), AMQPS, moduleX509, SELF_SIGNED, ClientType.MODULE_CLIENT, publicCertificate, privateKey, thumbprint}
                    }
            );
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
        public CorrelationDetailsLoggingAssert correlationDetailsLoggingAssert;

        public SendMessagesTestInstance(InternalClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
        {
            this.client = client;
            this.protocol = protocol;
            this.identity = identity;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.publicKeyCert = publicKeyCert;
            this.privateKey = privateKey;
            this.x509Thumbprint = x509Thumbprint;

            String deviceId = "";
            String moduleId = "";
            if (identity instanceof Module)
            {
                deviceId = identity.getDeviceId();
                moduleId = ((Module) identity).getId();
            }
            else if (identity instanceof Device)
            {
                deviceId = identity.getDeviceId();
            }

            this.correlationDetailsLoggingAssert = new CorrelationDetailsLoggingAssert(this.client.getConfig().getIotHubHostname(), deviceId, protocol.toString(), moduleId);
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
                          Integer numKeys, Integer sendTimeout, CountDownLatch latch)
        {
            this.protocol = protocol;
            this.numConnectionsPerDevice = numConnectionsPerDevice;
            this.numMessagesPerConnection = numMessagesPerConnection;
            this.sendTimeout = sendTimeout;
            this.numKeys = numKeys;
            this.latch = latch;

            succeed.set(true);

            this.connString = DeviceConnectionString.get(iotHubConnectionString, deviceAmqps);

            messageString = "Java client " + deviceAmqps.getDeviceId() + " test e2e message over AMQP protocol";
        }

        public void openConnection() throws IOException, URISyntaxException, InterruptedException
        {
            client = new DeviceClient(connString, protocol);
            IotHubServicesCommon.openClientWithRetry(client);
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

    public static Collection<BaseDevice> getIdentities(Collection inputs)
    {
        Set<BaseDevice> identities = new HashSet<>();

        Object[] inputArray = inputs.toArray();
        for (int i = 0; i < inputs.size(); i++)
        {
            Object[] inputsInstance = (Object[]) inputArray[i];
            identities.add((BaseDevice) inputsInstance[2]);
        }

        return identities;
    }

    /**
     * Each error injection test will take a list of messages to send. In that list, there will be an error injection message in the middle
     */
    protected static void buildMessageLists()
    {
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

    protected static void tearDown(Collection<BaseDevice> identitiesToDispose)
    {
        if (registryManager != null)
        {
            Tools.removeDevicesAndModules(registryManager, identitiesToDispose);
            registryManager.close();
            registryManager = null;
        }
    }

    @After
    public void delayTests()
    {
        try
        {
            Thread.sleep(INTERTEST_GUARDIAN_DELAY_MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            fail("Unexpected exception encountered");
        }
    }
}
