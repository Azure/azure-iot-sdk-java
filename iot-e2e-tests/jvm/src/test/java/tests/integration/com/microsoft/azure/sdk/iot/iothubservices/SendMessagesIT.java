/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.EventCallback;
import com.microsoft.azure.sdk.iot.common.Success;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.ConnectionStatusCallback;
import tests.integration.com.microsoft.azure.sdk.iot.DeviceConnectionString;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.fail;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenGenerator.generateSasTokenForIotDevice;

public class SendMessagesIT
{
    //How much devices the multithreaded test will create in parallel.
    private static final Integer MAX_DEVICE_PARALLEL = 5;

    //Huw much sequential connections each device will open and close in the multithreaded test.
    private static final Integer NUM_CONNECTIONS_PER_DEVICE = 10;

    //How much messages each device will send to the hub for each connection.
    private static final Integer NUM_MESSAGES_PER_CONNECTION = 10;

    //How many keys each message will cary.
    private static final Integer NUM_KEYS_PER_MESSAGE = 10;

    // How much to wait until a message makes it to the server, in milliseconds
    private static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    //How many milliseconds between retry
    private static final Integer RETRY_MILLISECONDS = 100;

    private static String iotHubonnectionStringEnvVarName = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static RegistryManager registryManager;

    private static String hostName;

    private static Device deviceHttps;
    private static Device deviceAmqps;
    private static Device deviceAmqpsWs;
    private static Device deviceMqtt;
    private static Device deviceMqttWs;

    private static Device[] deviceListAmqps = new Device[MAX_DEVICE_PARALLEL];
    private static final AtomicBoolean succeed = new AtomicBoolean();

    //Some tests below involve creating a short-lived sas token to test how expired tokens are handled
    private static final long SECONDS_FOR_SAS_TOKEN_TO_LIVE = 4;
    private static final long MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE = 6000;
    private static final long SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL = 1;

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

        private void openConnection() throws URISyntaxException, IOException
        {
            client = new DeviceClient(connString, protocol);
            client.open();
        }

        public void sendMessages() {
            for (int i = 0; i < numMessagesPerConnection; ++i)
            {
                try
                {
                    Message msgSend = new Message(messageString);
                    msgSend.setProperty("messageCount", Integer.toString(i));
                    for (int j = 0; j < numKeys; j++) {
                        msgSend.setProperty("key"+j, "value"+j);
                    }
                    msgSend.setExpiryTime(5000);

                    Success messageSent = new Success();
                    EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
                    client.sendEventAsync(msgSend, callback, messageSent);

                    Integer waitDuration = 0;
                    while(!messageSent.getResult())
                    {
                        Thread.sleep(RETRY_MILLISECONDS);
                        if ((waitDuration += RETRY_MILLISECONDS) > sendTimeout)
                        {
                            break;
                        }
                    }

                    if (!messageSent.getResult())
                    {
                        Assert.fail("Sending message over AMQPS protocol failed");
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

    @BeforeClass
    public static void setUp() throws Exception
    {
        Map<String, String> env = System.getenv();
        if (env.keySet().contains(iotHubonnectionStringEnvVarName))
        {
            iotHubConnectionString = env.get(iotHubonnectionStringEnvVarName);
        }

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        String uuid = UUID.randomUUID().toString();
        String deviceIdHttps = "java-device-client-e2e-test-https".concat("-" + uuid);
        String deviceIdAmqps = "java-device-client-e2e-test-amqps".concat("-" + uuid);
        String deviceIdAmqpsWs = "java-device-client-e2e-test-amqpsws".concat("-" + uuid);
        String deviceIdMqtt = "java-device-client-e2e-test-mqtt".concat("-" + uuid);
        String deviceIdMqttWs = "java-device-client-e2e-test-mqttws".concat("-" + uuid);

        deviceHttps = Device.createFromId(deviceIdHttps, null, null);
        deviceAmqps = Device.createFromId(deviceIdAmqps, null, null);
        deviceAmqpsWs = Device.createFromId(deviceIdAmqpsWs, null, null);
        deviceMqtt = Device.createFromId(deviceIdMqtt, null, null);
        deviceMqttWs = Device.createFromId(deviceIdMqttWs, null, null);

        registryManager.addDevice(deviceHttps);
        registryManager.addDevice(deviceAmqps);
        registryManager.addDevice(deviceAmqpsWs);
        registryManager.addDevice(deviceMqtt);
        registryManager.addDevice(deviceMqttWs);

        for (int i = 0; i < MAX_DEVICE_PARALLEL; i++) {
            deviceIdAmqps = "java-device-client-e2e-test-amqps".concat(i + "-" + uuid);
            deviceListAmqps[i] = Device.createFromId(deviceIdAmqps, null, null);
            registryManager.addDevice(deviceListAmqps[i]);
        }

        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();
    }

    @AfterClass
    public static void tearDown() throws IOException, IotHubException
    {
        registryManager.removeDevice(deviceHttps.getDeviceId());
        registryManager.removeDevice(deviceAmqps.getDeviceId());
        registryManager.removeDevice(deviceAmqpsWs.getDeviceId());
        registryManager.removeDevice(deviceMqtt.getDeviceId());
        registryManager.removeDevice(deviceMqttWs.getDeviceId());

        for (int i = 0; i < MAX_DEVICE_PARALLEL; i++) {
            registryManager.removeDevice(deviceListAmqps[i].getDeviceId());
        }
    }

    @Test
    public void sendMessagesOverHttps() throws URISyntaxException, IOException
    {
        String messageString = "Java client e2e test message over Https protocol";
        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceHttps), IotHubClientProtocol.HTTPS);
        client.open();

        for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; ++i)
        {
            try
            {
                Success messageSent = new Success();
                EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);

                client.sendEventAsync(msg, callback, messageSent);

                Integer waitDuration = 0;
                while(!messageSent.getResult())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                    {
                        break;
                    }
                }

                if (!messageSent.getResult())
                {
                    Assert.fail("Sending message over HTTPS protocol failed");
                }
            }
            catch (Exception e)
            {
                Assert.fail("Sending message over HTTPS protocol failed");
            }
        }

        client.closeNow();
    }

    @Test
    public void sendMessagesOverAmqps() throws URISyntaxException, IOException, InterruptedException
    {
        String messageString = "Java client e2e test message over Amqps protocol";
        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceAmqps), IotHubClientProtocol.AMQPS);
        client.open();

        for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; ++i)
        {
            try
            {
                Success messageSent = new Success();
                EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
                client.sendEventAsync(msg, callback, messageSent);

                Integer waitDuration = 0;
                while(!messageSent.getResult())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                    {
                        break;
                    }
                }


                if (!messageSent.getResult())
                {
                    Assert.fail("Sending message over AMQPS protocol failed");
                }
            }
            catch (Exception e)
            {
                Assert.fail("Sending message over AMQPS protocol failed");
            }
        }

        client.closeNow();
    }

    @Test
    public void sendMessagesOverAmqpsWs() throws URISyntaxException, IOException, InterruptedException
    {
        String messageString = "Java client e2e test message over Amqps WS protocol";
        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceAmqps), IotHubClientProtocol.AMQPS_WS);
        client.open();

        for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; ++i)
        {
            try
            {
                Success messageSent = new Success();
                EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
                client.sendEventAsync(msg, callback, messageSent);

                Integer waitDuration = 0;
                while(!messageSent.getResult())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                    {
                        break;
                    }
                }


                if (!messageSent.getResult())
                {
                    Assert.fail("Sending message over AMQPS WS protocol failed");
                }
            }
            catch (Exception e)
            {
                Assert.fail("Sending message over AMQPS WS protocol failed");
            }
        }

        client.closeNow();
    }

    @Test
    public void sendMessagesOverAmqpsMultithreaded() throws URISyntaxException, IOException, InterruptedException
    {
        List<Thread> threads = new ArrayList<>(deviceListAmqps.length);
        CountDownLatch cdl = new CountDownLatch(deviceListAmqps.length);

        Integer count = 0;
        for(Device deviceAmqps: deviceListAmqps)
        {
            Thread thread = new Thread(
                    new testDevice(
                            deviceAmqps,
                            IotHubClientProtocol.AMQPS,
                            NUM_CONNECTIONS_PER_DEVICE,
                            NUM_MESSAGES_PER_CONNECTION,
                            NUM_KEYS_PER_MESSAGE,
                            SEND_TIMEOUT_MILLISECONDS,
                            cdl));
            thread.start();
            threads.add(thread);
            count++;
        }

        cdl.await();

        if(!succeed.get())
        {
            Assert.fail("Sending message over AMQP protocol in parallel failed");
        }
    }

    @Test
    public void sendMessagesOverMqtt() throws URISyntaxException, IOException
    {
        String messageString = "Java client e2e test message over Mqtt protocol";
        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqtt), IotHubClientProtocol.MQTT);
        client.open();

        for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; ++i)
        {
            try
            {
                Success messageSent = new Success();
                EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
                client.sendEventAsync(msg, callback, messageSent);

                Integer waitDuration = 0;
                while(!messageSent.getResult())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                    {
                        break;
                    }
                }

                if (!messageSent.getResult())
                {
                    Assert.fail("Sending message over MQTT protocol failed");
                }
            }
            catch (Exception e)
            {
                Assert.fail("Sending message over MQTT protocol failed");
            }
        }

        client.closeNow();
    }

    @Test
    public void sendMessagesOverMqttWS() throws URISyntaxException, IOException
    {
        String messageString = "Java client e2e test message over Mqtt WS protocol";
        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqtt), IotHubClientProtocol.MQTT_WS);
        client.open();

        for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; ++i)
        {
            try
            {
                Success messageSent = new Success();
                EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
                client.sendEventAsync(msg, callback, messageSent);

                Integer waitDuration = 0;
                while(!messageSent.getResult())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                    {
                        break;
                    }
                }


                if (!messageSent.getResult())
                {
                    Assert.fail("Sending message over MQTT WS protocol failed");
                }
            }
            catch (Exception e)
            {
                Assert.fail("Sending message over MQTT WS protocol failed");
            }
        }

        client.closeNow();
    }

    @Test
    public void tokenRenewalWorksForHTTPS() throws URISyntaxException, IOException, InterruptedException
    {
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceHttps), IotHubClientProtocol.HTTPS);

        //set it so a newly generated sas token only lasts for a small amount of time
        client.setOption("SetSASTokenExpiryTime", SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL);
        client.open();

        for (int messageAttempt = 0; messageAttempt < NUM_MESSAGES_PER_CONNECTION; messageAttempt++)
        {
            //wait until old sas token has expired, this should force the config to generate a new one from the device key
            Thread.sleep(SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL * 1000);

            Success messageSent = new Success();
            EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
            client.sendEventAsync(new Message("some message body"), callback, messageSent);

            Integer waitDuration = 0;
            while(!messageSent.getResult())
            {
                Thread.sleep(RETRY_MILLISECONDS);
                if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                {
                    fail("Timed out waiting for successful message callback");
                    break;
                }
            }
        }
    }

    @Test
    public void tokenExpiredAfterOpenButBeforeSendHTTPS() throws InvalidKeyException, URISyntaxException, IOException, InterruptedException
    {
        String soonToBeExpiredSASToken = generateSasTokenForIotDevice(hostName, deviceHttps.getDeviceId(), deviceHttps.getPrimaryKey(), SECONDS_FOR_SAS_TOKEN_TO_LIVE);

        DeviceClient client = new DeviceClient(soonToBeExpiredSASToken, IotHubClientProtocol.HTTPS);
        client.open();

        //Force the SAS token to expire before sending messages
        Thread.sleep(MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE);

        sendMessagesExpectingSASTokenExpiration(client, IotHubClientProtocol.HTTPS.toString());

        client.closeNow();
    }

    /**
     * Send some messages that wait for callbacks to signify that the SAS token in the client config has expired.
     *
     * @param client the client to send the messages from
     * @param protocol the protocol the client is using
     */
    private void sendMessagesExpectingSASTokenExpiration(DeviceClient client, String protocol)
    {
        for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; ++i)
        {
            try
            {
                Message messageToSend = new Message("Test message expecting SAS Token Expired callback for protocol: " + protocol);
                Success messageSent = new Success();
                Success statusUpdated = new Success();

                ConnectionStatusCallback stateCallback = new ConnectionStatusCallback(IotHubConnectionState.SAS_TOKEN_EXPIRED);
                EventCallback messageCallback = new EventCallback(IotHubStatusCode.UNAUTHORIZED);

                client.registerConnectionStateCallback(stateCallback, statusUpdated);
                client.sendEventAsync(messageToSend, messageCallback, messageSent);

                Integer waitDuration = 0;
                while(!messageSent.getResult() || !statusUpdated.getResult())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                    {
                        break;
                    }
                }

                if (!messageSent.getResult() || !statusUpdated.getResult())
                {
                    Assert.fail("Sending message over " + protocol + " protocol failed");
                }
            }
            catch (Exception e)
            {
                Assert.fail("Sending message over " + protocol + " protocol failed");
            }
        }
    }
}