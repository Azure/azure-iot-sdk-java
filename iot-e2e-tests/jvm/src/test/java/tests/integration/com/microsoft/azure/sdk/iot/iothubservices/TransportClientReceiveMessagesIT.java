package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;

public class TransportClientReceiveMessagesIT
{
    private static final Integer MAX_DEVICE_MULTIPLEX = 3;
    public static Map<String, String> messageProperties = new HashMap<>(3);

    private static String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static RegistryManager registryManager;
    private static Device deviceAmqps;
    private static Device deviceAmqpsWS;

    private static ServiceClient serviceClient;
    private static Device[] deviceListAmqps = new Device[MAX_DEVICE_MULTIPLEX];

    // How much to wait until receiving a message from the server, in milliseconds
    private Integer receiveTimeout = 60000;

    private static String expectedCorrelationId = "1234";
    private static String expectedMessageId = "5678";

    private static ArrayList<String> clientConnectionStringArrayList = new ArrayList<>();
    private ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

    @BeforeClass
    public static void setUp() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            String uuid = UUID.randomUUID().toString();
            String deviceId = "java-device-client-e2e-test-multiplexing-receive-amqps".concat(i + "-" + uuid);

            deviceListAmqps[i] = Device.createFromId(deviceId, null, null);
            registryManager.addDevice(deviceListAmqps[i]);
            clientConnectionStringArrayList.add(registryManager.getDeviceConnectionString(deviceListAmqps[i]));
        }

        messageProperties = new HashMap<>(3);
        messageProperties.put("name1", "value1");
        messageProperties.put("name2", "value2");
        messageProperties.put("name3", "value3");

        serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        serviceClient.open();
    }

    @AfterClass
    public static void tearDown() throws IOException, IotHubException
    {
        if (serviceClient != null)
        {
            serviceClient.close();
        }

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
    public void receiveMessagesOverAmqpsIncludingProperties() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        transportClient.open();

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            TransportClientReceiveMessagesIT.Success messageReceived = new TransportClientReceiveMessagesIT.Success();
            com.microsoft.azure.sdk.iot.device.MessageCallback callback = new TransportClientReceiveMessagesIT.MessageCallback();
            clientArrayList.get(i).setMessageCallback(callback, messageReceived);

            sendMessageToDevice(deviceListAmqps[i].getDeviceId(), "AMQPS");
            waitForMessageToBeReceived(messageReceived, "AMQPS");

            Thread.sleep(200);
        }

        transportClient.closeNow();
    }

    @Test
    public void receiveMessagesOverAmqpWSIncludingProperties() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        transportClient.open();

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            TransportClientReceiveMessagesIT.Success messageReceived = new TransportClientReceiveMessagesIT.Success();
            com.microsoft.azure.sdk.iot.device.MessageCallback callback = new TransportClientReceiveMessagesIT.MessageCallback();
            clientArrayList.get(i).setMessageCallback(callback, messageReceived);

            sendMessageToDevice(deviceListAmqps[i].getDeviceId(), "AMQPS_WS");
            waitForMessageToBeReceived(messageReceived, "AMQPS_WS");

            Thread.sleep(200);
        }

        transportClient.closeNow();
    }

    private static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            Boolean resultValue = true;
            HashMap<String, String> messageProperties = (HashMap<String, String>) TransportClientReceiveMessagesIT.messageProperties;
            TransportClientReceiveMessagesIT.Success messageReceived = (TransportClientReceiveMessagesIT.Success)context;
            if (!hasExpectedProperties(msg, messageProperties) || !hasExpectedSystemProperties(msg))
            {
                resultValue = false;
            }

            messageReceived.setResult(resultValue);
            return IotHubMessageResult.COMPLETE;
        }
    }

    private class Success
    {
        public Boolean result = false;

        public void setResult(Boolean result)
        {
            this.result = result;
        }

        public Boolean getResult()
        {
            return this.result;
        }
    }

    private static boolean hasExpectedProperties(Message msg, Map<String, String> messageProperties)
    {
        for (String key : messageProperties.keySet())
        {
            if (msg.getProperty(key) == null || !msg.getProperty(key).equals(messageProperties.get(key)))
            {
                return false;
            }
        }

        return true;
    }

    private static boolean hasExpectedSystemProperties(Message msg)
    {
        if (msg.getCorrelationId() == null || !msg.getCorrelationId().equals(expectedCorrelationId))
        {
            return false;
        }

        if (msg.getMessageId() == null || !msg.getMessageId().equals(expectedMessageId))
        {
            return false;
        }

        //all system properties are as expected
        return true;
    }

    private void sendMessageToDevice(String deviceId, String protocolName) throws IotHubException, IOException
    {
        String messageString = "Java service e2e test message to be received over " + protocolName + " protocol";
        com.microsoft.azure.sdk.iot.service.Message serviceMessage = new com.microsoft.azure.sdk.iot.service.Message(messageString);
        serviceMessage.setCorrelationId(expectedCorrelationId);
        serviceMessage.setMessageId(expectedMessageId);
        serviceMessage.setProperties(messageProperties);
        serviceClient.send(deviceId, serviceMessage);
    }

    private void waitForMessageToBeReceived(TransportClientReceiveMessagesIT.Success messageReceived, String protocolName)
    {
        try
        {
            int waitDuration = 0;
            while (!messageReceived.getResult() && waitDuration <= receiveTimeout)
            {
                Thread.sleep(100);
                waitDuration += 100;
            }

            if (waitDuration > receiveTimeout)
            {
                Assert.fail("Receiving messages over " + protocolName + " protocol timed out.");
            }

            if (!messageReceived.getResult())
            {
                Assert.fail("Receiving message over " + protocolName + " protocol failed");
            }
        }
        catch (InterruptedException e)
        {
            Assert.fail("Receiving message over " + protocolName + " protocol failed");
        }
    }
}
