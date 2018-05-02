/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.ErrorInjectionHelper;
import com.microsoft.azure.sdk.iot.common.EventCallback;
import com.microsoft.azure.sdk.iot.common.MessageAndResult;
import com.microsoft.azure.sdk.iot.common.Success;
import com.microsoft.azure.sdk.iot.common.iothubservices.IotHubServicesCommon;
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

import static com.microsoft.azure.sdk.iot.common.iothubservices.IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate;
import static com.microsoft.azure.sdk.iot.common.iothubservices.IotHubServicesCommon.sendMessagesExpectingUnrecoverableConnectionLossAndTimeout;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.*;
import static junit.framework.TestCase.fail;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenGenerator.generateSasTokenForIotDevice;

@RunWith(Parameterized.class)
public class SendMessagesIT
{
    //How much devices the multithreaded test will create in parallel.
    private static final Integer MAX_DEVICE_PARALLEL = 3;

    //Huw much sequential connections each device will open and close in the multithreaded test.
    private static final Integer NUM_CONNECTIONS_PER_DEVICE = 5;

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
    private static final List<MessageAndResult> AMQP_METHOD_REQ_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    private static final List<MessageAndResult> AMQP_METHOD_RESP_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    private static final List<MessageAndResult> AMQP_TWIN_REQ_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();
    private static final List<MessageAndResult> AMQP_TWIN_RESP_LINK_DROP_MESSAGES_TO_SEND = new ArrayList<>();

    //How many keys each message will cary.
    private static final Integer NUM_KEYS_PER_MESSAGE = 3;

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

    private static Device device;
    private static Device deviceX509;

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
        String deviceId = "java-device-client-e2e-test-send-messages".concat("-" + uuid);
        String deviceIdX509 = "java-device-client-e2e-test-send-messages-X509".concat("-" + uuid);

        device = Device.createFromId(deviceId, null, null);
        deviceX509 = Device.createDevice(deviceIdX509, SELF_SIGNED);

        deviceX509.setThumbprint(x509Thumbprint, x509Thumbprint);

        registryManager.addDevice(device);
        registryManager.addDevice(deviceX509);

        for (int i = 0; i < MAX_DEVICE_PARALLEL; i++)
        {
            String deviceIdAmqps = "java-device-client-e2e-test-amqps".concat(i + "-" + uuid);
            deviceListAmqps[i] = Device.createFromId(deviceIdAmqps, null, null);
            registryManager.addDevice(deviceListAmqps[i]);
        }

        buildMessageLists();

        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();
        return Arrays.asList(
                new Object[][]
                {
                    //sas token
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), HTTPS), HTTPS, device, SAS},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), MQTT), MQTT, device, SAS},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), MQTT_WS), MQTT_WS, device, SAS},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS), AMQPS, device, SAS},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS_WS), AMQPS_WS, device, SAS},

                    //x509
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), HTTPS, publicKeyCert, false, privateKey, false), HTTPS, deviceX509, SELF_SIGNED},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), MQTT, publicKeyCert, false, privateKey, false), MQTT, deviceX509, SELF_SIGNED},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), AMQPS, publicKeyCert, false, privateKey, false), AMQPS, deviceX509, SELF_SIGNED}
                }
        );
    }

    /**
     * Each error injection test will take a list of messages to send. In that list, there will be an error injection message in the middle
     */
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

                Message amqpMethodReqLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsMethodReqLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_METHOD_REQ_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpMethodReqLinkDropErrorInjectionMessage, null));

                Message amqpMethodRespLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_METHOD_RESP_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpMethodRespLinkDropErrorInjectionMessage, null));

                Message amqpTwinReqLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsTwinReqLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_TWIN_REQ_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpTwinReqLinkDropErrorInjectionMessage, null));

                Message amqpTwinRespLinkDropErrorInjectionMessage = ErrorInjectionHelper.amqpsTwinRespLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec);
                AMQP_TWIN_RESP_LINK_DROP_MESSAGES_TO_SEND.add(new MessageAndResult(amqpTwinRespLinkDropErrorInjectionMessage, null));
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
            }

            NORMAL_MESSAGES_TO_SEND.add(new MessageAndResult(new Message("test message"), IotHubStatusCode.OK_EMPTY));
        }
    }

    public SendMessagesIT(DeviceClient deviceClient, IotHubClientProtocol protocol, Device device, AuthenticationType authenticationType)
    {
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
            for (int i = 0; i < this.numConnectionsPerDevice; i++)
            {
                try
                {
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
            IotHubServicesCommon.openDeviceClientWithRetry(client);
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
            registryManager.removeDevice(device.getDeviceId());
            registryManager.removeDevice(deviceX509.getDeviceId());

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
        if (testInstance.protocol == MQTT_WS && (testInstance.authenticationType == SELF_SIGNED || testInstance.authenticationType == CERTIFICATE_AUTHORITY))
        {
            //mqtt_ws does not support x509 auth currently
            return;
        }

        IotHubServicesCommon.sendMessages(testInstance.deviceClient, testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
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
        IotHubServicesCommon.openDeviceClientWithRetry(testInstance.deviceClient);

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
        if (testInstance.protocol != HTTPS || testInstance.authenticationType != SAS)
        {
            //This scenario only applies to HTTP since MQTT and AMQP allow expired sas tokens for 30 minutes after open
            // as long as token did not expire before open. X509 doesn't apply either
            return;
        }

        String soonToBeExpiredSASToken = generateSasTokenForIotDevice(hostName, testInstance.device.getDeviceId(), testInstance.device.getPrimaryKey(), SECONDS_FOR_SAS_TOKEN_TO_LIVE);
        DeviceClient client = new DeviceClient(soonToBeExpiredSASToken, testInstance.protocol);
        IotHubServicesCommon.openDeviceClientWithRetry(client);

        //Force the SAS token to expire before sending messages
        Thread.sleep(MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE);
        IotHubServicesCommon.sendMessagesExpectingSASTokenExpiration(client, testInstance.protocol.toString(), 1, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.authenticationType);
        client.closeNow();
    }

    @Test
    public void expiredMessagesAreNotSent() throws IOException
    {
        IotHubServicesCommon.sendExpiredMessageExpectingMessageExpiredCallback(testInstance.deviceClient, testInstance.protocol, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesWithTcpConnectionDrop() throws IOException, InterruptedException
    {
        if (testInstance.protocol == HTTPS || (testInstance.protocol == MQTT_WS && testInstance.authenticationType != SAS))
        {
            //TCP connection is not maintained between device and service when using HTTPS, so this test case isn't applicable
            //MQTT_WS + x509 is not supported for sending messages
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, TCP_CONNECTION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithConnectionDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_CONNECTION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithSessionDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_SESSION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithCbsRequestLinkDrop() throws IOException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithCbsResponseLinkDrop() throws IOException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithD2CLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithC2DLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)))
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

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithMethodReqLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Method link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_METHOD_REQ_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithMethodRespLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
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

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_METHOD_RESP_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithTwinReqLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Twin link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_TWIN_REQ_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithTwinRespLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Twin is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.deviceClient, testInstance.protocol, AMQP_TWIN_RESP_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesWithTcpConnectionDropNotifiesUserIfRetryExpires() throws IOException, InterruptedException
    {
        if (testInstance.protocol == HTTPS || (testInstance.protocol == MQTT_WS && testInstance.authenticationType != SAS))
        {
            //TCP connection is not maintained between device and service when using HTTPS, so this test case isn't applicable
            //MQTT_WS + x509 is not supported for sending messages
            return;
        }

        testInstance.deviceClient.setRetryPolicy(new NoRetry());

        Message tcpConnectionDropErrorInjectionMessageUnrecoverable = ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(1, 100000);
        sendMessagesExpectingUnrecoverableConnectionLossAndTimeout(testInstance.deviceClient, testInstance.protocol, tcpConnectionDropErrorInjectionMessageUnrecoverable, testInstance.authenticationType);

        //reset back to default
        testInstance.deviceClient.setRetryPolicy(new ExponentialBackoffWithJitter());
    }
}
