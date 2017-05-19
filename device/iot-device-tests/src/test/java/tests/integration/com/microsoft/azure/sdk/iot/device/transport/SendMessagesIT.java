// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.integration.com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.device.DeviceConnectionString;
import tests.integration.com.microsoft.azure.sdk.iot.device.EventCallback;
import tests.integration.com.microsoft.azure.sdk.iot.device.Success;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendMessagesIT
{
    //How much devices the multithreaded test will create in parallel.
    private static final Integer MAX_DEVICE_PARALLEL = 10;

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

    private static Device deviceHttps;
    private static Device deviceAmqps;
    private static Device deviceMqtt;

    private static Device[] deviceListAmqps = new Device[MAX_DEVICE_PARALLEL];
    private static final AtomicBoolean succeed = new AtomicBoolean();

    protected static class TestDevice implements Runnable
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
                    this.OpenConnection();
                    this.SendMessages();
                    this.CloseConnection();
                } catch (Exception e)
                {
                    succeed.set(false);
                }
            }
            latch.countDown();
        }

        public TestDevice(Device deviceAmqps, IotHubClientProtocol protocol,
                          Integer numConnectionsPerDevice, Integer numMessagesPerConnection,
                          Integer numKeys, Integer sendTimeout, CountDownLatch latch)
        {
            this.protocol = protocol;
            this .numConnectionsPerDevice = numConnectionsPerDevice;
            this.numMessagesPerConnection = numMessagesPerConnection;
            this.sendTimeout = sendTimeout;
            this.numKeys = numKeys;
            this.latch = latch;

            succeed.set(true);

            this.connString = DeviceConnectionString.get(iotHubConnectionString, deviceAmqps);

            messageString = "Java client " + deviceAmqps.getDeviceId() + " test e2e message over AMQP protocol";
        }

        private void OpenConnection() throws URISyntaxException, IOException
        {
            client = new DeviceClient(connString, protocol);
            client.open();
        }

        public void SendMessages() {
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
                    EventCallback callback = new EventCallback();
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

        public void CloseConnection() throws IOException
        {
            client.closeNow();
        }
    }

    @BeforeClass
    public static void setUp() throws Exception
    {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet())
        {
            if (envName.equals(iotHubonnectionStringEnvVarName))
            {
                iotHubConnectionString = env.get(envName);
            }
        }

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        String uuid = UUID.randomUUID().toString();
        String deviceIdHttps = "java-device-client-e2e-test-https".concat("-" + uuid);
        String deviceIdAmqps = "java-device-client-e2e-test-amqps".concat("-" + uuid);
        String deviceIdMqtt = "java-device-client-e2e-test-mqtt".concat("-" + uuid);

        deviceHttps = Device.createFromId(deviceIdHttps, null, null);
        deviceAmqps = Device.createFromId(deviceIdAmqps, null, null);
        deviceMqtt = Device.createFromId(deviceIdMqtt, null, null);

        registryManager.addDevice(deviceHttps);
        registryManager.addDevice(deviceAmqps);
        registryManager.addDevice(deviceMqtt);

        for (int i = 0; i < MAX_DEVICE_PARALLEL; i++) {
            deviceIdAmqps = "java-device-client-e2e-test-amqps".concat(i + "-" + uuid);
            deviceListAmqps[i] = Device.createFromId(deviceIdAmqps, null, null);
            registryManager.addDevice(deviceListAmqps[i]);
        }
    }

    @AfterClass
    public static void TearDown() throws IOException, IotHubException
    {
        registryManager.removeDevice(deviceHttps.getDeviceId());
        registryManager.removeDevice(deviceAmqps.getDeviceId());
        registryManager.removeDevice(deviceMqtt.getDeviceId());

        for (int i = 0; i < MAX_DEVICE_PARALLEL; i++) {
            registryManager.removeDevice(deviceListAmqps[i].getDeviceId());
        }
    }

    @Test
    public void SendMessagesOverHttps() throws URISyntaxException, IOException
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
                EventCallback callback = new EventCallback();

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
    public void SendMessagesOverAmqps() throws URISyntaxException, IOException, InterruptedException
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
                EventCallback callback = new EventCallback();
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
    public void SendMessagesOverAmqps_multithreaded() throws URISyntaxException, IOException, InterruptedException
    {
        List<Thread> threads = new ArrayList<>(deviceListAmqps.length);
        CountDownLatch cdl = new CountDownLatch(deviceListAmqps.length);

        Integer count = 0;
        for(Device deviceAmqps: deviceListAmqps)
        {
            Thread thread = new Thread(
                    new TestDevice(
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
    public void SendMessagesOverMqtt() throws URISyntaxException, IOException
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
                EventCallback callback = new EventCallback();
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
}