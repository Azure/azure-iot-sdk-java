/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.ErrorInjectionHelper;
import com.microsoft.azure.sdk.iot.common.EventCallback;
import com.microsoft.azure.sdk.iot.common.Success;
import com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.DeviceConnectionString;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.X509Cert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.microsoft.azure.sdk.iot.common.ErrorInjectionHelper.DefaultDelayInSec;
import static com.microsoft.azure.sdk.iot.common.ErrorInjectionHelper.DefaultDurationInSec;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class ReceiveMessagesIT
{
    public static Map<String, String> messageProperties = new HashMap<>(3);

    private final static String SET_MINIMUM_POLLING_INTERVAL = "SetMinimumPollingInterval";
    private final static Long ONE_SECOND_POLLING_INTERVAL = 1000L;
    
    // variables used in E2E test for sending back to back messages using C2D sendAsync method
    private static final int MAX_COMMANDS_TO_SEND = 5; // maximum commands to be sent in a loop
    private static final List messageIdListStoredOnC2DSend = new ArrayList(); // store the message id list on sending C2D commands using service client
    private static final List messageIdListStoredOnReceive = new ArrayList(); // store the message id list on receiving C2D commands using device client

    private static String publicKeyCert;
    private static String privateKey;
    private static String x509Thumbprint;

    private static String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static RegistryManager registryManager;
    private static Device deviceHttps;
    private static Device deviceHttpsX509;
    private static Device deviceAmqps;
    private static Device deviceAmqpsX509;
    private static Device deviceMqtt;
    private static Device deviceMqttWs;
    private static Device deviceAmqpsWs;
    private static Device deviceAmqpsWsX509;
    private static Device deviceMqttX509;

    private static ServiceClient serviceClient;

    // How much to wait until receiving a message from the server, in milliseconds
    private Integer receiveTimeout = 240000; // 4 minutes

    private static String expectedCorrelationId = "1234";
    private static String expectedMessageId = "5678";
    private static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;
    private static final long ERROR_INJECTION_WAIT_TIMEOUT = 1 * 60 * 1000; // 1 minute

    private ReceiveMessagesITRunner testInstance;

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{1} with {3} auth")
    public static Collection inputs() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        X509Cert cert = new X509Cert(0, false, "TestLeaf", "TestRoot");
        privateKey =  cert.getPrivateKeyLeafPem();
        publicKeyCert = cert.getPublicCertLeafPem();
        x509Thumbprint = cert.getThumbPrintLeaf();
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        String uuid = UUID.randomUUID().toString();
        String deviceIdHttps = "java-device-client-e2e-test-https".concat("-" + uuid);
        String deviceIdAmqps = "java-device-client-e2e-test-amqps".concat("-" + uuid);
        String deviceIdMqtt = "java-device-client-e2e-test-mqtt".concat("-" + uuid);
        String deviceIdMqttWs = "java-device-client-e2e-test-mqttws".concat("-" + uuid);
        String deviceIdAmqpsWS = "java-device-client-e2e-test-amqpsws".concat("-" + uuid);
        String deviceIdMqttX509 = "java-device-client-e2e-test-mqtt-x509".concat("-" + uuid);
        String deviceIdHttpsX509 = "java-device-client-e2e-test-https-x509".concat("-" + uuid);
        String deviceIdAmqpsX509 = "java-device-client-e2e-test-amqps-x509".concat("-" + uuid);
        String deviceIdAmqpsWsX509 = "java-device-client-e2e-test-amqpsws-x509".concat("-" + uuid);

        deviceHttps = Device.createFromId(deviceIdHttps, null, null);
        deviceAmqps = Device.createFromId(deviceIdAmqps, null, null);
        deviceMqtt = Device.createFromId(deviceIdMqtt, null, null);
        deviceMqttWs = Device.createFromId(deviceIdMqttWs, null, null);
        deviceAmqpsWs = Device.createFromId(deviceIdAmqpsWS, null, null);
        deviceMqttX509 = Device.createDevice(deviceIdMqttX509, AuthenticationType.SELF_SIGNED);
        deviceHttpsX509 = Device.createDevice(deviceIdHttpsX509, AuthenticationType.SELF_SIGNED);
        deviceAmqpsX509 = Device.createDevice(deviceIdAmqpsX509, AuthenticationType.SELF_SIGNED);
        deviceAmqpsWsX509 = Device.createDevice(deviceIdAmqpsWsX509, AuthenticationType.SELF_SIGNED);

        deviceMqttX509.setThumbprint(x509Thumbprint, x509Thumbprint);
        deviceHttpsX509.setThumbprint(x509Thumbprint, x509Thumbprint);
        deviceAmqpsX509.setThumbprint(x509Thumbprint, x509Thumbprint);
        deviceAmqpsWsX509.setThumbprint(x509Thumbprint, x509Thumbprint);

        registryManager.addDevice(deviceHttps);
        registryManager.addDevice(deviceAmqps);
        registryManager.addDevice(deviceMqtt);
        registryManager.addDevice(deviceMqttWs);
        registryManager.addDevice(deviceAmqpsWs);
        registryManager.addDevice(deviceMqttX509);
        registryManager.addDevice(deviceAmqpsWsX509);
        registryManager.addDevice(deviceHttpsX509);
        registryManager.addDevice(deviceAmqpsX509);

        messageProperties = new HashMap<>(3);
        messageProperties.put("name1", "value1");
        messageProperties.put("name2", "value2");
        messageProperties.put("name3", "value3");

        serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        serviceClient.open();

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

    public ReceiveMessagesIT(DeviceClient deviceClient, IotHubClientProtocol protocol, Device device, AuthenticationType authenticationType)
    {
        super();
        this.testInstance = new ReceiveMessagesITRunner(deviceClient, protocol, device, authenticationType);
    }

    private class ReceiveMessagesITRunner
    {
        private DeviceClient deviceClient;
        private IotHubClientProtocol protocol;
        private Device device;
        private AuthenticationType authenticationType;

        public ReceiveMessagesITRunner(DeviceClient deviceClient, IotHubClientProtocol protocol, Device device, AuthenticationType authenticationType)
        {
            this.deviceClient = deviceClient;
            this.protocol = protocol;
            this.device = device;
            this.authenticationType = authenticationType;
        }
    }

    @AfterClass
    public static void tearDown() throws IOException, IotHubException
    {
        serviceClient.close();
        if (registryManager != null)
        {
            registryManager.removeDevice(deviceHttps.getDeviceId());
            registryManager.removeDevice(deviceAmqps.getDeviceId());
            registryManager.removeDevice(deviceMqtt.getDeviceId());
            registryManager.removeDevice(deviceMqttWs.getDeviceId());
            registryManager.removeDevice(deviceAmqpsWs.getDeviceId());
            registryManager.removeDevice(deviceMqttX509.getDeviceId());
            registryManager.removeDevice(deviceAmqpsX509.getDeviceId());
            registryManager.removeDevice(deviceAmqpsWsX509.getDeviceId());
            registryManager.removeDevice(deviceHttpsX509.getDeviceId());
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
    public void receiveMessagesOverIncludingProperties() throws Exception
    {
        if (testInstance.protocol == HTTPS)
        {
            testInstance.deviceClient.setOption(SET_MINIMUM_POLLING_INTERVAL, ONE_SECOND_POLLING_INTERVAL);
        }

        SendMessagesCommon.openDeviceClientWithRetry(testInstance.deviceClient);

        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();

        if (testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS)
        {
            callback = new MessageCallbackMqtt();
        }

        Success messageReceived = new Success();
        testInstance.deviceClient.setMessageCallback(callback, messageReceived);

        sendMessageToDevice(testInstance.device.getDeviceId(), testInstance.protocol.toString());
        waitForMessageToBeReceived(messageReceived, testInstance.protocol.toString());

        Thread.sleep(200);
        testInstance.deviceClient.closeNow();
    }

    @Test
    public void receiveMessagesWithTCPConnectionDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol == HTTPS)
        {
            //test case not applicable
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test
    public void receiveMessagesWithAmqpsConnectionDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsConnectionDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test
    public void receiveMessagesWithAmqpsSessionDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test
    public void receiveMessagesWithAmqpsCBSReqLinkDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        if (testInstance.authenticationType == SELF_SIGNED || testInstance.authenticationType == CERTIFICATE_AUTHORITY)
        {
            //cbs links aren't established in these scenarios, so it would be impossible/irrelevant if a cbs link dropped
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test
    public void receiveMessagesWithAmqpsCBSRespLinkDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        if (testInstance.authenticationType == SELF_SIGNED || testInstance.authenticationType == CERTIFICATE_AUTHORITY)
        {
            //cbs links aren't established in these scenarios, so it would be impossible/irrelevant if a cbs link dropped
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test
    public void receiveMessagesWithAmqpsD2CLinkDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test
    public void receiveMessagesWithAmqpsC2DLinkDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //TODO X509 case never seems to get callback about the connection dying. Needs investigation because this should pass, but doesn't
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    //Test times out very frequently
    @Ignore
    @Test
    public void receiveBackToBackUniqueC2DCommandsOverAmqpsUsingSendAsync() throws Exception
    {
        if (this.testInstance.protocol != AMQPS)
        {
            //only want to test for AMQPS
            return;
        }

        // This E2E test is for testing multiple C2D sends and make sure buffers are not getting overwritten
        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        // set device to receive back to back different commands using AMQPS protocol
        SendMessagesCommon.openDeviceClientWithRetry(testInstance.deviceClient);

        // set call back for device client for receiving message
        com.microsoft.azure.sdk.iot.device.MessageCallback callBackOnRx = new MessageCallbackForBackToBackC2DMessages();
        testInstance.deviceClient.setMessageCallback(callBackOnRx, null);

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
            CompletableFuture<Void> future = serviceClient.sendAsync(deviceAmqps.getDeviceId(), serviceMessage);
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
        testInstance.deviceClient.closeNow(); //close the device client connection
        assertTrue("Received messages don't match up with sent messages", messageIdListStoredOnReceive.containsAll(messageIdListStoredOnC2DSend)); // check if the received list is same as the actual list that was created on sending the messages
    }

    private void errorInjectionTestFlow(Message errorInjectionMessage) throws IOException, IotHubException, InterruptedException
    {
        final ArrayList<IotHubConnectionStatus> connectionStatusUpdates = new ArrayList<>();
        testInstance.deviceClient.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
            {
                connectionStatusUpdates.add(status);
            }
        }, null);

        SendMessagesCommon.openDeviceClientWithRetry(testInstance.deviceClient);

        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();

        if (testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS)
        {
            callback = new MessageCallbackMqtt();
        }

        Success messageReceived = new Success();
        testInstance.deviceClient.setMessageCallback(callback, messageReceived);

        //error injection message is not guaranteed to be ack'd by service so it may be re-sent. By setting expiry time,
        // we ensure that error injection message isn't resent to service too many times. The message will still likely
        // be sent 3 or 4 times causing 3 or 4 disconnections, but the test should recover anyways.
        errorInjectionMessage.setExpiryTime(200);
        testInstance.deviceClient.sendEventAsync(errorInjectionMessage, new EventCallback(null), null);

        //wait to send the message because we want to ensure that the tcp connection drop happens before the message is received
        long startTime = System.currentTimeMillis();
        long timeElapsed = 0;
        while (!connectionStatusUpdates.contains(IotHubConnectionStatus.DISCONNECTED_RETRYING))
        {
            Thread.sleep(200);
            timeElapsed = System.currentTimeMillis() - startTime;

            //2 minute timeout waiting for error injection to occur
            if (timeElapsed > ERROR_INJECTION_WAIT_TIMEOUT)
            {
                fail("Timed out waiting for error injection message to take effect");
            }
        }

        sendMessageToDevice(testInstance.device.getDeviceId(), testInstance.protocol.toString());
        waitForMessageToBeReceived(messageReceived, testInstance.protocol.toString());

        Thread.sleep(200);
        testInstance.deviceClient.closeNow();

        assertTrue("Error Injection message did not cause service to drop TCP connection", connectionStatusUpdates.contains(IotHubConnectionStatus.DISCONNECTED_RETRYING));
    }

    private static class MessageCallbackForBackToBackC2DMessages implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            messageIdListStoredOnReceive.add(msg.getMessageId()); // add received messsage id to messageList
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

            messageReceived.callbackWasFired();
            messageReceived.setResult(resultValue);
            return IotHubMessageResult.COMPLETE;
        }
    }

    private class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            HashMap<String, String> messageProperties = (HashMap<String, String>) ReceiveMessagesIT.messageProperties;
            Success messageReceived = (Success)context;
            if (hasExpectedProperties(msg, messageProperties) && hasExpectedSystemProperties(msg))
            {
                messageReceived.setResult(true);
            }

            messageReceived.callbackWasFired();

            return IotHubMessageResult.COMPLETE;
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
            long startTime = System.currentTimeMillis();
            while (!messageReceived.wasCallbackFired())
            {
                Thread.sleep(100);

                if (System.currentTimeMillis() - startTime > receiveTimeout)
                {
                    fail("Timed out waiting to receive message");
                }
            }

            if (!messageReceived.getResult())
            {
                Assert.fail("Receiving message over " + protocolName + " protocol failed. Received message was missing expected properties");
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
            long startTime = 0;
        
            // check if all messages are received.
            while (messageIdListStoredOnReceive.size() != MAX_COMMANDS_TO_SEND)
            {
                Thread.sleep(100);

                if (System.currentTimeMillis() - startTime > receiveTimeout)
                {
                    Assert.fail("Receiving messages timed out.");
                }
            }
        }
        catch (InterruptedException e)
        {
            Assert.fail("Receiving message failed");
        }
    }
}
