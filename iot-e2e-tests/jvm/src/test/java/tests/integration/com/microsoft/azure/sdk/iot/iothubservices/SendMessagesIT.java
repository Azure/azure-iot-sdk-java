/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.ErrorInjectionHelper;
import com.microsoft.azure.sdk.iot.common.EventCallback;
import com.microsoft.azure.sdk.iot.common.MessageAndResult;
import com.microsoft.azure.sdk.iot.common.Success;
import com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.ExponentialBackoffWithJitter;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.transport.NoRetry;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.DeviceConnectionString;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.X509Cert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon.sendMessagesExpectingConnectionStatusChangeUpdate;
import static com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon.sendMessagesExpectingUnrecoverableConnectionLossAndTimeout;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static junit.framework.TestCase.fail;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenGenerator.generateSasTokenForIotDevice;

@RunWith(Parameterized.class)
public class SendMessagesIT
{
    //How much devices the multithreaded test will create in parallel.
    private static final Integer MAX_DEVICE_PARALLEL = 5;

    //Huw much sequential connections each device will open and close in the multithreaded test.
    private static final Integer NUM_CONNECTIONS_PER_DEVICE = 10;

    //How much messages each device will send to the hub for each connection.
    private static final Integer NUM_MESSAGES_PER_CONNECTION = 6;

    //The messages to be sent in these tests. Some contain error injection messages surrounded by normal messages
    private static final List<MessageAndResult> NORMAL_MESSAGES_TO_SEND = new ArrayList<>();
    private static final List<MessageAndResult> TCP_CONNECTION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    private static final List<MessageAndResult> AMQP_CONNECTION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    private static final List<MessageAndResult> AMQP_SESSION_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    private static final List<MessageAndResult> AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    private static final List<MessageAndResult> AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    private static final List<MessageAndResult> AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    private static final List<MessageAndResult> AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();

    //How many keys each message will cary.
    private static final Integer NUM_KEYS_PER_MESSAGE = 10;

    // How much to wait until a message makes it to the server, in milliseconds
    private static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    //How many milliseconds between retry
    private static final Integer RETRY_MILLISECONDS = 100;

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;
    private static String publicKeyCert;
    private static String privateKey;
    private static String x509Thumbprint;

    private static RegistryManager registryManager;

    private static String hostName;

    private static Device deviceHttps;
    private static Device deviceAmqps;
    private static Device deviceAmqpsWs;
    private static Device deviceMqtt;
    private static Device deviceMqttWs;
    private static Device deviceMqttX509;
    private static Device deviceHttpsX509;
    private static Device deviceAmqpsX509;
    private static Device deviceMqttWsX509;
    private static Device deviceAmqpsWsX509;

    private static Device[] deviceListAmqps = new Device[MAX_DEVICE_PARALLEL];
    private static final AtomicBoolean succeed = new AtomicBoolean();

    //Some tests below involve creating a short-lived sas token to test how expired tokens are handled
    private static final long SECONDS_FOR_SAS_TOKEN_TO_LIVE = 3;
    private static final long MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE = 5000;
    private static final long SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL = 1;

    private SendMessagesITRunner testInstance;

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{1} with {3} auth")
    public static Collection inputs() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        X509Cert cert = new X509Cert(0,false, "TestLeaf", "TestRoot");
        privateKey =  cert.getPrivateKeyLeafPem();
        publicKeyCert = cert.getPublicCertLeafPem();
        x509Thumbprint = cert.getThumbPrintLeaf();

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        String uuid = UUID.randomUUID().toString();
        String deviceIdHttps = "java-device-client-e2e-test-https".concat("-" + uuid);
        String deviceIdAmqps = "java-device-client-e2e-test-amqps".concat("-" + uuid);
        String deviceIdAmqpsWs = "java-device-client-e2e-test-amqpsws".concat("-" + uuid);
        String deviceIdMqtt = "java-device-client-e2e-test-mqtt".concat("-" + uuid);
        String deviceIdMqttWs = "java-device-client-e2e-test-mqttws".concat("-" + uuid);
        String deviceIdMqttX509 = "java-device-client-e2e-test-mqtt-X509".concat("-" + uuid);
        String deviceIdHttpsX509 = "java-device-client-e2e-test-https-X509".concat("-" + uuid);
        String deviceIdAmqpsX509 = "java-device-client-e2e-test-amqps-X509".concat("-" + uuid);
        String deviceIdMqttWsX509 = "java-device-client-e2e-test-mqttws-X509".concat("-" + uuid);
        String deviceIdAmqpsWsX509 = "java-device-client-e2e-test-amqpsws-X509".concat("-" + uuid);

        deviceHttps = Device.createFromId(deviceIdHttps, null, null);
        deviceAmqps = Device.createFromId(deviceIdAmqps, null, null);
        deviceAmqpsWs = Device.createFromId(deviceIdAmqpsWs, null, null);
        deviceMqtt = Device.createFromId(deviceIdMqtt, null, null);
        deviceMqttWs = Device.createFromId(deviceIdMqttWs, null, null);

        deviceMqttX509 = Device.createDevice(deviceIdMqttX509, AuthenticationType.SELF_SIGNED);
        deviceHttpsX509 = Device.createDevice(deviceIdHttpsX509, AuthenticationType.SELF_SIGNED);
        deviceAmqpsX509 = Device.createDevice(deviceIdAmqpsX509, AuthenticationType.SELF_SIGNED);
        deviceMqttWsX509 = Device.createDevice(deviceIdMqttWsX509, AuthenticationType.SELF_SIGNED);
        deviceAmqpsWsX509 = Device.createDevice(deviceIdAmqpsWsX509, AuthenticationType.SELF_SIGNED);

        deviceMqttX509.setThumbprint(x509Thumbprint, x509Thumbprint);
        deviceHttpsX509.setThumbprint(x509Thumbprint, x509Thumbprint);
        deviceAmqpsX509.setThumbprint(x509Thumbprint,x509Thumbprint);
        deviceMqttWsX509.setThumbprint(x509Thumbprint, x509Thumbprint);
        deviceAmqpsWsX509.setThumbprint(x509Thumbprint,x509Thumbprint);

        registryManager.addDevice(deviceHttps);
        registryManager.addDevice(deviceAmqps);
        registryManager.addDevice(deviceAmqpsWs);
        registryManager.addDevice(deviceMqtt);
        registryManager.addDevice(deviceMqttWs);

        registryManager.addDevice(deviceMqttX509);
        registryManager.addDevice(deviceHttpsX509);
        registryManager.addDevice(deviceAmqpsX509);

        for (int i = 0; i < MAX_DEVICE_PARALLEL; i++)
        {
            deviceIdAmqps = "java-device-client-e2e-test-amqps".concat(i + "-" + uuid);
            deviceListAmqps[i] = Device.createFromId(deviceIdAmqps, null, null);
            registryManager.addDevice(deviceListAmqps[i]);
        }

        buildMessageLists();

        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();
        return Arrays.asList(
                new Object[][]
                {
                    //sas token
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceHttps), IotHubClientProtocol.HTTPS), IotHubClientProtocol.HTTPS, deviceHttps, SAS},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqtt), IotHubClientProtocol.MQTT), IotHubClientProtocol.MQTT, deviceMqtt, SAS},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqttWs), IotHubClientProtocol.MQTT_WS), IotHubClientProtocol.MQTT_WS, deviceMqttWs, SAS},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceAmqps), AMQPS), AMQPS, deviceAmqps, SAS},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceAmqpsWs), IotHubClientProtocol.AMQPS_WS), IotHubClientProtocol.AMQPS_WS, deviceAmqpsWs, SAS},

                    //x509
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceHttpsX509), IotHubClientProtocol.HTTPS, publicKeyCert, false, privateKey, false), IotHubClientProtocol.HTTPS, deviceHttpsX509, AuthenticationType.SELF_SIGNED},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqttX509), IotHubClientProtocol.MQTT, publicKeyCert, false, privateKey, false), IotHubClientProtocol.MQTT, deviceMqttX509, AuthenticationType.SELF_SIGNED},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceAmqpsX509), AMQPS, publicKeyCert, false, privateKey, false), AMQPS, deviceAmqpsX509, AuthenticationType.SELF_SIGNED}

                    //not supported yet
                                //{new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceAmqpsWsX509), IotHubClientProtocol.AMQPS_WS, publicKeyCert, false, privateKey, false), IotHubClientProtocol.AMQPS_WS, deviceAmqpsWsX509, AuthenticationType.SELF_SIGNED},
                                //{new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqttWsX509), IotHubClientProtocol.MQTT_WS, publicKeyCert, false, privateKey, false), IotHubClientProtocol.MQTT_WS, deviceMqttWsX509, AuthenticationType.SELF_SIGNED}
                }
        );
    }

    private static void buildMessageLists()
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
            }

            NORMAL_MESSAGES_TO_SEND.add(new MessageAndResult(new Message("test message"), IotHubStatusCode.OK_EMPTY));
        }
    }

    public SendMessagesIT(DeviceClient deviceClient, IotHubClientProtocol protocol, Device device, AuthenticationType authenticationType)
    {
        super();
        this.testInstance = new SendMessagesITRunner(deviceClient, protocol, device, authenticationType);
    }

    private class SendMessagesITRunner
    {
        private DeviceClient deviceClient;
        private IotHubClientProtocol protocol;
        private Device device;
        private AuthenticationType authenticationType;

        public SendMessagesITRunner(DeviceClient deviceClient, IotHubClientProtocol protocol, Device device, AuthenticationType authenticationType)
        {
            this.deviceClient = deviceClient;
            this.protocol = protocol;
            this.device = device;
            this.authenticationType = authenticationType;
        }
    }

    protected static class testDevice implements Runnable
    {
        private DeviceClient client;
        private String messageString;
        private String connString;

        private IotHubClientProtocol protocol;
        private Integer numMessagesPerConnection;
        private Integer numConnectionsPerDevice;
        private Integer sendTimeout;
        private Integer numKeys;
        private CountDownLatch latch;

        @Override
        public void run()
        {
            for (int i = 0; i < this.numConnectionsPerDevice; i++) {
                try {
                    this.openConnection();
                    this.sendMessages();
                    this.closeConnection();
                } catch (Exception e)
                {
                    succeed.set(false);
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

        private void openConnection() throws IOException, URISyntaxException
        {
            client = new DeviceClient(connString, protocol);
            SendMessagesCommon.openDeviceClientWithRetry(client);
        }

        public void sendMessages()
        {
            for (int i = 0; i < numMessagesPerConnection; ++i)
            {
                try
                {
                    Message msgSend = new Message(messageString);
                    msgSend.setProperty("messageCount", Integer.toString(i));
                    for (int j = 0; j < numKeys; j++) {
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
                            fail("Timed out waiting for event callback");
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

    @AfterClass
    public static void tearDown() throws IOException, IotHubException
    {
        if (registryManager != null)
        {
            registryManager.removeDevice(deviceHttps.getDeviceId());
            registryManager.removeDevice(deviceAmqps.getDeviceId());
            registryManager.removeDevice(deviceMqtt.getDeviceId());
            registryManager.removeDevice(deviceMqttWs.getDeviceId());
            registryManager.removeDevice(deviceAmqpsWs.getDeviceId());

            registryManager.removeDevice(deviceMqttX509.getDeviceId());
            registryManager.removeDevice(deviceAmqpsX509.getDeviceId());
            registryManager.removeDevice(deviceHttpsX509.getDeviceId());

            //devices not needed for these tests as neither amqps_ws nor mqtt_ws supports sending messages when using x509
            //registryManager.removeDevice(deviceAmqpsWsX509.getDeviceId());
            //registryManager.removeDevice(deviceMqttWsX509.getDeviceId());

            for (int i = 0; i < MAX_DEVICE_PARALLEL; i++)
            {
                registryManager.removeDevice(deviceListAmqps[i].getDeviceId());
            }

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
            throw new RuntimeException(e);
        }
    }

    @Test
    public void sendMessages() throws IOException, InterruptedException
    {
        if (testInstance.protocol == IotHubClientProtocol.MQTT_WS &&(testInstance.authenticationType == AuthenticationType.SELF_SIGNED || testInstance.authenticationType == AuthenticationType.CERTIFICATE_AUTHORITY))
        {
            //mqtt_ws does not support x509 auth currently
            return;
        }

        SendMessagesCommon.sendMessages(testInstance.deviceClient, testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0);
    }

    @Test
    public void sendMessagesOverAmqpsMultithreaded() throws InterruptedException
    {
        if (!(testInstance.protocol == AMQPS && testInstance.authenticationType == SAS))
        {
            //this test only applicable for AMQPS with SAS auth
            return;
        }

        List<Thread> threads = new ArrayList<>(deviceListAmqps.length);
        CountDownLatch cdl = new CountDownLatch(deviceListAmqps.length);

        for(Device deviceAmqps: deviceListAmqps)
        {
            Thread thread = new Thread(
                    new testDevice(
                            deviceAmqps,
                            AMQPS,
                            NUM_CONNECTIONS_PER_DEVICE,
                            NUM_MESSAGES_PER_CONNECTION,
                            NUM_KEYS_PER_MESSAGE,
                            SEND_TIMEOUT_MILLISECONDS,
                            cdl));
            thread.start();
            threads.add(thread);
        }

        cdl.await(1, TimeUnit.MINUTES);

        if(!succeed.get())
        {
            Assert.fail("Sending message over AMQP protocol in parallel failed");
        }
    }

    @Test
    public void tokenRenewalWorks() throws InterruptedException
    {
        if (testInstance.authenticationType != SAS)
        {
            //this scenario is not applicable for x509 auth
            return;
        }

        //set it so a newly generated sas token only lasts for a small amount of time
        testInstance.deviceClient.setOption("SetSASTokenExpiryTime", SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL);
        SendMessagesCommon.openDeviceClientWithRetry(testInstance.deviceClient);

        for (int messageAttempt = 0; messageAttempt < NUM_MESSAGES_PER_CONNECTION; messageAttempt++)
        {
            //wait until old sas token has expired, this should force the config to generate a new one from the device key
            Thread.sleep(SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL * 1000);

            Success messageSent = new Success();
            EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
            testInstance.deviceClient.sendEventAsync(new Message("some message body"), callback, messageSent);

            long startTime = System.currentTimeMillis();
            while(!messageSent.wasCallbackFired())
            {
                Thread.sleep(RETRY_MILLISECONDS);
                if (System.currentTimeMillis() - startTime > SEND_TIMEOUT_MILLISECONDS)
                {
                    fail("Timed out waiting for successful message callback");
                }
            }

            if (messageSent.getCallbackStatusCode() != IotHubStatusCode.OK_EMPTY)
            {
                fail("Sending messages over " + testInstance.protocol + " failed: expected OK_EMPTY message callback but received " + messageSent.getCallbackStatusCode());
            }
        }
    }

    @Test
    public void tokenExpiredAfterOpenButBeforeSendHttp() throws InvalidKeyException, IOException, InterruptedException, URISyntaxException
    {
        if (testInstance.protocol != IotHubClientProtocol.HTTPS || testInstance.authenticationType != SAS)
        {
            //This scenario only applies to HTTP since MQTT and AMQP allow expired sas tokens for 30 minutes after open
            // as long as token did not expire before open. X509 doesn't apply either
            return;
        }

        String soonToBeExpiredSASToken = generateSasTokenForIotDevice(hostName, deviceHttps.getDeviceId(), testInstance.device.getPrimaryKey(), SECONDS_FOR_SAS_TOKEN_TO_LIVE);
        DeviceClient client = new DeviceClient(soonToBeExpiredSASToken, testInstance.protocol);
        SendMessagesCommon.openDeviceClientWithRetry(client);

        //Force the SAS token to expire before sending messages
        Thread.sleep(MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE);
        SendMessagesCommon.sendMessagesExpectingSASTokenExpiration(client, testInstance.protocol.toString(), 1, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
        client.closeNow();
    }

    @Test
    public void expiredMessagesAreNotSent() throws URISyntaxException, IOException
    {
        SendMessagesCommon.sendExpiredMessageExpectingMessageExpiredCallback(testInstance.deviceClient, testInstance.protocol, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
    }

    @Test
    public void sendMessagesWithTcpConnectionDrop() throws IOException, InterruptedException
    {
        if (testInstance.protocol == IotHubClientProtocol.HTTPS || (testInstance.protocol == IotHubClientProtocol.MQTT_WS && testInstance.authenticationType != SAS))
        {
            //TCP connection is not maintained between device and service when using HTTPS, so this test case isn't applicable
            //MQTT_WS + x509 is not supported for sending messages
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, TCP_CONNECTION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 400);
    }

    @Test
    public void sendMessagesOverAmqpWithConnectionDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == IotHubClientProtocol.AMQPS_WS && testInstance.authenticationType == SAS)))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        SendMessagesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_CONNECTION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 400);
    }

    @Test
    public void sendMessagesOverAmqpWithSessionDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == IotHubClientProtocol.AMQPS_WS && testInstance.authenticationType == SAS)))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        SendMessagesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_SESSION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 400);
    }

    @Test
    public void sendMessagesOverAmqpWithCbsRequestLinkDrop() throws IOException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != IotHubClientProtocol.AMQPS_WS)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        SendMessagesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 600);
    }

    @Test
    public void sendMessagesOverAmqpWithCbsResponseLinkDrop() throws IOException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != IotHubClientProtocol.AMQPS_WS)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        SendMessagesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 600);
    }

    @Test
    public void sendMessagesOverAmqpWithD2CLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == IotHubClientProtocol.AMQPS_WS && testInstance.authenticationType == SAS)))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        SendMessagesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 600);
    }

    @Test
    public void sendMessagesOverAmqpWithC2DLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == IotHubClientProtocol.AMQPS_WS && testInstance.authenticationType == SAS)))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. C2D link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        SendMessagesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 600);
    }

    @Test
    public void sendMessagesWithTcpConnectionDropNotifiesUserIfRetryExpires() throws IOException, InterruptedException
    {
        if (testInstance.protocol == IotHubClientProtocol.HTTPS || (testInstance.protocol == IotHubClientProtocol.MQTT_WS && testInstance.authenticationType != SAS))
        {
            //TCP connection is not maintained between device and service when using HTTPS, so this test case isn't applicable
            //MQTT_WS + x509 is not supported for sending messages
            return;
        }

        testInstance.deviceClient.setRetryPolicy(new NoRetry());

        Message tcpConnectionDropErrorInjectionMessageUnrecoverable = ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(1, 100000);
        sendMessagesExpectingUnrecoverableConnectionLossAndTimeout(testInstance.deviceClient, testInstance.protocol, tcpConnectionDropErrorInjectionMessageUnrecoverable);

        //reset back to default
        testInstance.deviceClient.setRetryPolicy(new ExponentialBackoffWithJitter());
    }
}
