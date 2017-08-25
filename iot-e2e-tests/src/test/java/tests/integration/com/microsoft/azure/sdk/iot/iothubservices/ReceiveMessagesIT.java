/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.DeviceConnectionString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReceiveMessagesIT
{
    public static Map<String, String> messageProperties = new HashMap<>(3);

    private final static String SET_MINIMUM_POLLING_INTERVAL = "SetMinimumPollingInterval";
    private final static Long ONE_SECOND_POLLING_INTERVAL = 1000L;

    private static String iotHubonnectionStringEnvVarName = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static RegistryManager registryManager;
    private static Device deviceHttps;
    private static Device deviceAmqps;
    private static Device deviceMqtt;
    private static Device deviceMqttWs;
    private static Device deviceAmqpsWS;

    private static ServiceClient serviceClient;

    // How much to wait until receiving a message from the server, in milliseconds
    private Integer receiveTimeout = 60000;

    private static String expectedCorrelationId = "1234";
    private static String expectedMessageId = "5678";

    @BeforeClass
    public static void setUp() throws Exception
    {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet())
        {
            if (envName.equals(iotHubonnectionStringEnvVarName.toString()))
            {
                iotHubConnectionString = env.get(envName);
            }
        }
        
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        String uuid = UUID.randomUUID().toString();
        String deviceIdHttps = "java-device-client-e2e-test-https".concat("-" + uuid);
        String deviceIdAmqps = "java-device-client-e2e-test-amqps".concat("-" + uuid);
        String deviceIdMqtt = "java-device-client-e2e-test-mqtt".concat("-" + uuid);
        String deviceIdMqttWs = "java-device-client-e2e-test-mqttws".concat("-" + uuid);
        String deviceIdAmqpsWS = "java-device-client-e2e-test-amqpsws".concat("-" + uuid);

        deviceHttps = Device.createFromId(deviceIdHttps, null, null);
        deviceAmqps = Device.createFromId(deviceIdAmqps, null, null);
        deviceMqtt = Device.createFromId(deviceIdMqtt, null, null);
        deviceMqttWs = Device.createFromId(deviceIdMqttWs, null, null);
        deviceAmqpsWS = Device.createFromId(deviceIdAmqpsWS, null, null);

        registryManager.addDevice(deviceHttps);
        registryManager.addDevice(deviceAmqps);
        registryManager.addDevice(deviceMqtt);
        registryManager.addDevice(deviceMqttWs);
        registryManager.addDevice(deviceAmqpsWS);

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
        serviceClient.close();
        registryManager.removeDevice(deviceHttps.getDeviceId());
        registryManager.removeDevice(deviceAmqps.getDeviceId());
        registryManager.removeDevice(deviceMqtt.getDeviceId());
        registryManager.removeDevice(deviceMqttWs.getDeviceId());
        registryManager.removeDevice(deviceAmqpsWS.getDeviceId());
    }

    @Test
    public void receiveMessagesOverHttpsIncludingProperties() throws Exception
    {
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceHttps), IotHubClientProtocol.HTTPS);
        client.setOption(SET_MINIMUM_POLLING_INTERVAL, ONE_SECOND_POLLING_INTERVAL);
        client.open();

        Success messageReceived = new Success();
        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();
        client.setMessageCallback(callback, messageReceived);

        sendMessageToDevice(deviceHttps.getDeviceId(), "HTTPS");
        waitForMessageToBeReceived(messageReceived, "HTTPS");

        Thread.sleep(200);
        client.closeNow();
    }

    @Test
    public void receiveMessagesOverAmqpsIncludingProperties() throws Exception
    {
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceAmqps), IotHubClientProtocol.AMQPS);
        client.open();

        Success messageReceived = new Success();
        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();
        client.setMessageCallback(callback, messageReceived);

        sendMessageToDevice(deviceAmqps.getDeviceId(), "AMQPS");
        waitForMessageToBeReceived(messageReceived, "AMQPS");

        Thread.sleep(200);
        client.closeNow();
    }

    @Test
    public void receiveMessagesOverMqttWithProperties() throws Exception
    {
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqtt), IotHubClientProtocol.MQTT);
        client.open();

        Success messageReceived = new Success();
        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallbackMqtt();
        client.setMessageCallback(callback, messageReceived);

        sendMessageToDevice(deviceMqtt.getDeviceId(), "MQTT");
        waitForMessageToBeReceived(messageReceived, "MQTT");

        Thread.sleep(200);
        client.closeNow();
    }

    @Test
    public void receiveMessagesOverMqttWsWithProperties() throws Exception
    {
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqttWs), IotHubClientProtocol.MQTT_WS);
        client.open();

        Success messageReceived = new Success();
        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallbackMqtt();
        client.setMessageCallback(callback, messageReceived);

        sendMessageToDevice(deviceMqttWs.getDeviceId(), "MQTT_WS");
        waitForMessageToBeReceived(messageReceived, "MQTT_WS");

        Thread.sleep(200);
        client.closeNow();
    }

    @Test
    public void receiveMessagesOverAmqpWSIncludingProperties() throws Exception
    {
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceAmqpsWS), IotHubClientProtocol.AMQPS_WS);
        client.open();

        Success messageReceived = new Success();
        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();
        client.setMessageCallback(callback, messageReceived);

        sendMessageToDevice(deviceAmqpsWS.getDeviceId(), "AMQPS_WS");
        waitForMessageToBeReceived(messageReceived, "AMQPS_WS");

        Thread.sleep(200);
        client.closeNow();
    }

    private static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            Boolean resultValue = true;
            HashMap<String, String> messageProperties = (HashMap<String, String>) ReceiveMessagesIT.messageProperties;
            Success messageReceived = (Success)context;
            if (!hasExpectedProperties(msg, messageProperties) || !hasExpectedSystemProperties(msg))
            {
                resultValue = false;
            }

            messageReceived.setResult(resultValue);
            return IotHubMessageResult.COMPLETE;
        }
    }

    private class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            HashMap<String, String> messageProperties = (HashMap<String, String>) ReceiveMessagesIT.messageProperties;
            if (hasExpectedProperties(msg, messageProperties) && hasExpectedSystemProperties(msg))
            {
                Success messageReceived = (Success)context;
                messageReceived.setResult(true);
            }

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

    private void waitForMessageToBeReceived(Success messageReceived, String protocolName)
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
