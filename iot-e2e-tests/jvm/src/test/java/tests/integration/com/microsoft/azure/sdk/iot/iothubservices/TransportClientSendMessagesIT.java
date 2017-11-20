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
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon.sendMessagesMultiplex;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS_WS;

public class TransportClientSendMessagesIT
{
    //Huw much sequential connections each device will open and close in the multithreaded test.

    //How many keys each message will cary.
    private static final Integer NUM_KEYS_PER_MESSAGE = 10;

    private static final Integer MAX_DEVICE_MULTIPLEX = 3;
    private static final Integer NUM_MESSAGES_PER_CONNECTION = 5;
    private static final Integer RETRY_MILLISECONDS = 100;
    private static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    private static Device[] deviceListAmqps = new Device[MAX_DEVICE_MULTIPLEX];
    private static final AtomicBoolean succeed = new AtomicBoolean();

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";

    private static RegistryManager registryManager;

    private static String hostName;

    private static ArrayList<String> clientConnectionStringArrayList = new ArrayList<>();
    private ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

    protected static class multiplexRunnable implements Runnable
    {
        private DeviceClient client;
        private String messageString;

        private Integer numMessagesPerDevice;
        private Integer sendTimeout;
        private Integer numKeys;
        private CountDownLatch latch;

        @Override
        public void run()
        {
            try
            {
                this.sendMessages();
            }
            catch (Exception e)
            {
                succeed.set(false);
            }
            latch.countDown();
        }

        public multiplexRunnable(Device deviceAmqps, DeviceClient deviceClient,
                          Integer numMessagesPerConnection,
                          Integer numKeys, Integer sendTimeout, CountDownLatch latch)
        {
            this.client = deviceClient;
            this.numMessagesPerDevice = numMessagesPerConnection;
            this.sendTimeout = sendTimeout;
            this.numKeys = numKeys;
            this.latch = latch;

            succeed.set(true);

            messageString = "Java client " + deviceAmqps.getDeviceId() + " test e2e message over AMQP protocol";
        }

        public void sendMessages() {
            for (int i = 0; i < numMessagesPerDevice; ++i)
            {
                try
                {
                    Message msgSend = new Message(messageString);
                    msgSend.setProperty("messageCount", Integer.toString(i));
                    for (int j = 0; j < numKeys; j++)
                    {
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
                }
                catch (Exception e)
                {
                    Assert.fail("Sending message multithreaded throws " + e);
                }
            }
        }
    }

    @BeforeClass
    public static void setUp() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            String uuid = UUID.randomUUID().toString();
            String deviceId = "java-device-client-e2e-test-multiplexing-send-amqps".concat(i + "-" + uuid);

            deviceListAmqps[i] = Device.createFromId(deviceId, null, null);
            registryManager.addDevice(deviceListAmqps[i]);
            clientConnectionStringArrayList.add(registryManager.getDeviceConnectionString(deviceListAmqps[i]));
        }

        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();
    }

    @AfterClass
    public static void tearDown() throws IOException, IotHubException
    {
        if (registryManager != null)
        {
            for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
            {
                registryManager.removeDevice(deviceListAmqps[i].getDeviceId());
            }
            registryManager.close();
        }
    }

    @Test
    public void sendMessagesOverAmqps() throws URISyntaxException, IOException, InterruptedException
    {
        TransportClient transportClient = new TransportClient(AMQPS);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        transportClient.open();

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            sendMessagesMultiplex(clientArrayList.get(i), IotHubClientProtocol.AMQPS, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
        }

        transportClient.closeNow();
    }

    @Test
    public void sendMessagesOverAmqpsMultithreaded() throws IOException, InterruptedException, URISyntaxException
    {
        List<Thread> threads = new ArrayList<>(clientArrayList.size());
        CountDownLatch cdl = new CountDownLatch(clientArrayList.size());

        TransportClient transportClient = new TransportClient(AMQPS);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        transportClient.open();

        Integer count = 0;
        for (int i = 0; i < clientArrayList.size(); i++)
        {
            Thread thread = new Thread(new TransportClientSendMessagesIT.multiplexRunnable(deviceListAmqps[i], clientArrayList.get(i), NUM_MESSAGES_PER_CONNECTION, NUM_KEYS_PER_MESSAGE, SEND_TIMEOUT_MILLISECONDS, cdl));
            thread.start();
            threads.add(thread);
            count++;

            cdl.await();
        }

        if(!succeed.get())
        {
            Assert.fail("Sending message over AMQP protocol in parallel failed");
        }
    }

    @Test
    public void sendMessagesOverAmqpsWs() throws URISyntaxException, IOException, InterruptedException
    {
        TransportClient transportClient = new TransportClient(AMQPS_WS);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        transportClient.open();

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            sendMessagesMultiplex(clientArrayList.get(i), IotHubClientProtocol.AMQPS_WS, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
        }

        transportClient.closeNow();
    }

    @Test
    public void sendMessagesOverAmqpsWsMultithreaded() throws IOException, InterruptedException, URISyntaxException
    {
        List<Thread> threads = new ArrayList<>(clientArrayList.size());
        CountDownLatch cdl = new CountDownLatch(clientArrayList.size());

        TransportClient transportClient = new TransportClient(AMQPS_WS);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        transportClient.open();

        Integer count = 0;
        for (int i = 0; i < clientArrayList.size(); i++)
        {
            Thread thread = new Thread(new TransportClientSendMessagesIT.multiplexRunnable(deviceListAmqps[i], clientArrayList.get(i), NUM_MESSAGES_PER_CONNECTION, NUM_KEYS_PER_MESSAGE, SEND_TIMEOUT_MILLISECONDS, cdl));
            thread.start();
            threads.add(thread);
            count++;

            cdl.await();
        }

        if(!succeed.get())
        {
            Assert.fail("Sending message over AMQP protocol in parallel failed");
        }
    }
}
