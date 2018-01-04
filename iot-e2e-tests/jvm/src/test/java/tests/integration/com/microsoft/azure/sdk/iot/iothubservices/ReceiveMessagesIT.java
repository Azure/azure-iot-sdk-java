/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.DeviceConnectionString;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.junit.Assert.assertEquals;

public class ReceiveMessagesIT
{
    public static Map<String, String> messageProperties = new HashMap<>(3);

    private final static String SET_MINIMUM_POLLING_INTERVAL = "SetMinimumPollingInterval";
    private final static Long ONE_SECOND_POLLING_INTERVAL = 1000L;
    
    // variables used in E2E test for sending back to back messages using C2D sendAsync method
    private static final int MAX_COMMANDS_TO_SEND = 5; // maximum commands to be sent in a loop
    private static final List messageIdListStoredOnC2DSend = new ArrayList(); // store the message id list on sending C2D commands using service client
    private static final List messageIdListStoredOnReceive = new ArrayList(); // store the message id list on receiving C2D commands using device client

    private static final String PUBLIC_KEY_CERTIFICATE_BASE64_ENCODED_ENV_VAR_NAME = "IOTHUB_E2E_X509_CERT_BASE64";
    private static final String PRIVATE_KEY_BASE64_ENCODED_ENV_VAR_NAME = "IOTHUB_E2E_X509_PRIVATE_KEY_BASE64";
    private static final String X509_THUMBPRINT_ENV_VAR_NAME = "IOTHUB_E2E_X509_THUMBPRINT";

    private static String publicKeyCert;
    private static String privateKey;
    private static String x509Thumbprint;

    private static String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static RegistryManager registryManager;
    private static Device deviceHttps;
    private static Device deviceAmqps;
    private static Device deviceMqtt;
    private static Device deviceMqttWs;
    private static Device deviceAmqpsWS;
    private static Device deviceMqttX509;

    private static ServiceClient serviceClient;

    // How much to wait until receiving a message from the server, in milliseconds
    private Integer receiveTimeout = 180000;

    private static String expectedCorrelationId = "1234";
    private static String expectedMessageId = "5678";

    @BeforeClass
    public static void setUp() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        String privateKeyBase64Encoded = Tools.retrieveEnvironmentVariableValue(PRIVATE_KEY_BASE64_ENCODED_ENV_VAR_NAME);
        String publicKeyCertBase64Encoded = Tools.retrieveEnvironmentVariableValue(PUBLIC_KEY_CERTIFICATE_BASE64_ENCODED_ENV_VAR_NAME);
        x509Thumbprint = Tools.retrieveEnvironmentVariableValue(X509_THUMBPRINT_ENV_VAR_NAME);

        byte[] publicCertBytes = Base64.decodeBase64Local(publicKeyCertBase64Encoded.getBytes());
        publicKeyCert = new String(publicCertBytes);

        byte[] privateKeyBytes = Base64.decodeBase64Local(privateKeyBase64Encoded.getBytes());
        privateKey = new String(privateKeyBytes);

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        String uuid = UUID.randomUUID().toString();
        String deviceIdHttps = "java-device-client-e2e-test-https".concat("-" + uuid);
        String deviceIdAmqps = "java-device-client-e2e-test-amqps".concat("-" + uuid);
        String deviceIdMqtt = "java-device-client-e2e-test-mqtt".concat("-" + uuid);
        String deviceIdMqttWs = "java-device-client-e2e-test-mqttws".concat("-" + uuid);
        String deviceIdAmqpsWS = "java-device-client-e2e-test-amqpsws".concat("-" + uuid);
        String deviceIdMqttX509 = "java-device-client-e2e-test-mqtt-x509".concat("-" + uuid);

        deviceHttps = Device.createFromId(deviceIdHttps, null, null);
        deviceAmqps = Device.createFromId(deviceIdAmqps, null, null);
        deviceMqtt = Device.createFromId(deviceIdMqtt, null, null);
        deviceMqttWs = Device.createFromId(deviceIdMqttWs, null, null);
        deviceAmqpsWS = Device.createFromId(deviceIdAmqpsWS, null, null);
        deviceMqttX509 = Device.createDevice(deviceIdMqttX509, AuthenticationType.SELF_SIGNED);

        deviceMqttX509.setThumbprint(x509Thumbprint, x509Thumbprint);

        registryManager.addDevice(deviceHttps);
        registryManager.addDevice(deviceAmqps);
        registryManager.addDevice(deviceMqtt);
        registryManager.addDevice(deviceMqttWs);
        registryManager.addDevice(deviceAmqpsWS);
        registryManager.addDevice(deviceMqttX509);

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
        registryManager.removeDevice(deviceMqttX509.getDeviceId());
        registryManager.close();
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

    @Test
    public void receiveMessagesOverMQTTIncludingPropertiesUsingX509Auth() throws Exception
    {
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqttX509), IotHubClientProtocol.MQTT, publicKeyCert, false, privateKey, false);
        client.open();

        Success messageReceived = new Success();
        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallbackMqtt();
        client.setMessageCallback(callback, messageReceived);

        sendMessageToDevice(deviceMqttX509.getDeviceId(),"MQTT");
        waitForMessageToBeReceived(messageReceived, "MQTT");

        Thread.sleep(200);
        client.closeNow();
    }

    @Test
    public void receiveMessagesOverAMQPSIncludingPropertiesUsingX509Auth() throws Exception
    {
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqttX509), IotHubClientProtocol.AMQPS, publicKeyCert, false, privateKey, false);
        client.open();

        Success messageReceived = new Success();
        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallbackMqtt();
        client.setMessageCallback(callback, messageReceived);

        sendMessageToDevice(deviceMqttX509.getDeviceId(),"AMQPS");
        waitForMessageToBeReceived(messageReceived, "AMQPS");

        Thread.sleep(200);
        client.closeNow();
    }

    @Test
    public void receiveBackToBackUniqueC2DCommandsOverAmqpsUsingSendAsync() throws Exception
    {
        // This E2E test is for testing multiple C2D sends and make sure buffers are not getting overwritten
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        
        // set device to receive back to back different commands using AMQPS protocol
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceAmqps), IotHubClientProtocol.AMQPS);
        client.open();
        
        // set call back for device client for receiving message
        com.microsoft.azure.sdk.iot.device.MessageCallback callBackOnRx = new MessageCallbackForBackToBackC2DMessages();
        client.setMessageCallback(callBackOnRx, null);
       
        // send back to back unique commands from service client using sendAsync operation.
        for (int i = 0; i < MAX_COMMANDS_TO_SEND; i++)
        {
            String messageString = Integer.toString(i);
            com.microsoft.azure.sdk.iot.service.Message serviceMessage = new com.microsoft.azure.sdk.iot.service.Message(messageString);
            
            // set message id
            serviceMessage.setMessageId(Integer.toString(i));
            
            // set expected list of messaged id's
            messageIdListStoredOnC2DSend.add(Integer.toString(i));
            
            // send the message. Service client uses AMQPS protocol
            CompletableFuture<Void> future = serviceClient.sendAsync(deviceAmqps.getDeviceId(),serviceMessage);
            futureList.add(future);

        }
        
        for (CompletableFuture<Void> future : futureList)
        {
            try
            {
                future.get();
            }
            catch (ExecutionException e)
            {
                Assert.fail("Exception : " + e.getMessage());
            }
        }
        
        // Now wait for messages to be received in the device client
        waitForBackToBackC2DMessagesToBeReceived();
        client.closeNow(); //close the device client connection
        assertEquals(true,messageIdListStoredOnReceive.containsAll(messageIdListStoredOnC2DSend)); // check if the received list is same as the actual list that was created on sending the messages
    }

    private static class MessageCallbackForBackToBackC2DMessages implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            Success messageReceived = (Success)context;
            messageIdListStoredOnReceive.add(msg.getMessageId()); // add received messsage id to messageList
            messageReceived.setResult(true);
            return IotHubMessageResult.COMPLETE;
        }
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

    private void waitForBackToBackC2DMessagesToBeReceived()
    {
        try
        {
            int waitDuration = 0;
        
            // check if all messages are received.
            while ( (messageIdListStoredOnReceive.size() != MAX_COMMANDS_TO_SEND) && (waitDuration <= receiveTimeout))
            {
                Thread.sleep(100);
                waitDuration += 100;
            }

            if (waitDuration > receiveTimeout)
            {
                Assert.fail("Receiving messages timed out.");
            }

        }
  
        catch (InterruptedException e)
        {
            Assert.fail("Receiving message failed");
        }
    }
}
