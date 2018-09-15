/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.ErrorInjectionHelper;
import com.microsoft.azure.sdk.iot.common.EventCallback;
import com.microsoft.azure.sdk.iot.common.Success;
import com.microsoft.azure.sdk.iot.common.iothubservices.IotHubServicesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.microsoft.azure.sdk.iot.common.DeviceConnectionString;
import com.microsoft.azure.sdk.iot.common.iothubservices.MethodNameLoggingIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.X509Cert;

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
public class ReceiveMessagesIT extends MethodNameLoggingIntegrationTest
{
    private static final long DEFAULT_TEST_TIMEOUT = 3 * 60 * 1000;
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

    private static Device device;
    private static Device deviceX509;

    private static Module module;
    private static Module moduleX509;

    private static ServiceClient serviceClient;

    // How much to wait until receiving a message from the server, in milliseconds
    private static final int RECEIVE_TIMEOUT = 2 * 60 * 1000; // 2 minutes

    private static String expectedCorrelationId = "1234";
    private static String expectedMessageId = "5678";
    private static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;
    private static final long ERROR_INJECTION_RECOVERY_TIMEOUT = 1 * 60 * 1000; // 1 minute

    private ReceiveMessagesITRunner testInstance;

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{1} with {4} auth using {5}")
    public static Collection inputs() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        X509Cert cert = new X509Cert(0, false, "TestLeaf", "TestRoot");
        privateKey =  cert.getPrivateKeyLeafPem();
        publicKeyCert = cert.getPublicCertLeafPem();
        x509Thumbprint = cert.getThumbPrintLeaf();
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        String uuid = UUID.randomUUID().toString();

        String deviceId = "java-device-client-e2e-test-receive-messages".concat("-" + uuid);
        String deviceIdX509 = "java-device-client-e2e-test-receive-messages-x509".concat("-" + uuid);
        String moduleId = "java-module-client-e2e-test-send-messages".concat("-" + uuid);
        String moduleIdX509 = "java-module-client-e2e-test-send-messages-X509".concat("-" + uuid);

        device = Device.createFromId(deviceId, null, null);
        deviceX509 = Device.createDevice(deviceIdX509, SELF_SIGNED);

        module = Module.createFromId(deviceId, moduleId, null);
        moduleX509 = Module.createModule(deviceIdX509, moduleIdX509, SELF_SIGNED);

        deviceX509.setThumbprint(x509Thumbprint, x509Thumbprint);
        moduleX509.setThumbprint(x509Thumbprint, x509Thumbprint);

        module = Module.createFromId(deviceId, moduleId, null);
        moduleX509 = Module.createModule(deviceIdX509, moduleIdX509, SELF_SIGNED);

        deviceX509.setThumbprint(x509Thumbprint, x509Thumbprint);
        moduleX509.setThumbprint(x509Thumbprint, x509Thumbprint);

        registryManager.addDevice(device);
        registryManager.addDevice(deviceX509);

        registryManager.addModule(module);
        registryManager.addModule(moduleX509);

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
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), HTTPS), HTTPS, device, null, SAS, "DeviceClient"},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), MQTT), MQTT, device, null, SAS, "DeviceClient"},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), MQTT_WS), MQTT_WS, device, null, SAS, "DeviceClient"},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS), AMQPS, device, null, SAS, "DeviceClient"},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS_WS), AMQPS_WS, device, null, SAS, "DeviceClient"},

                    //x509
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), HTTPS, publicKeyCert, false, privateKey, false), HTTPS, deviceX509, null, SELF_SIGNED, "DeviceClient"},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), MQTT, publicKeyCert, false, privateKey, false), MQTT, deviceX509, null, SELF_SIGNED, "DeviceClient"},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), AMQPS, publicKeyCert, false, privateKey, false), AMQPS, deviceX509, null, SELF_SIGNED, "DeviceClient"},

                    //sas token module client
                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), MQTT), MQTT, device, module, SAS, "ModuleClient"},
                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), MQTT_WS), MQTT_WS, device, module, SAS, "ModuleClient"},
                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), AMQPS), AMQPS, device, module, SAS, "ModuleClient"},
                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), AMQPS_WS), AMQPS_WS, device, module, SAS, "ModuleClient"},

                    //x509 module client
                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), MQTT, publicKeyCert, false, privateKey, false), MQTT, deviceX509, moduleX509, SELF_SIGNED, "ModuleClient"},
                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), AMQPS, publicKeyCert, false, privateKey, false), AMQPS, deviceX509, moduleX509, SELF_SIGNED, "ModuleClient"},
                }
        );
    }

    public ReceiveMessagesIT(InternalClient client, IotHubClientProtocol protocol, Device device, Module module, AuthenticationType authenticationType, String clientType)
    {
        this.testInstance = new ReceiveMessagesITRunner(client, protocol, device, module, authenticationType, clientType);
    }

    private class ReceiveMessagesITRunner
    {
        private InternalClient client;
        private IotHubClientProtocol protocol;
        private Device device;
        private Module module;
        private AuthenticationType authenticationType;
        private String clientType;

        public ReceiveMessagesITRunner(InternalClient client, IotHubClientProtocol protocol, Device device, Module module, AuthenticationType authenticationType, String clientType)
        {
            this.client = client;
            this.protocol = protocol;
            this.device = device;
            this.module = module;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
        }
    }

    @AfterClass
    public static void tearDown() throws IOException, IotHubException
    {
        serviceClient.close();
        if (registryManager != null)
        {
            registryManager.removeDevice(device.getDeviceId());
            registryManager.removeDevice(deviceX509.getDeviceId());
            registryManager.close();
            registryManager = null;
        }
    }

    @After
    public void delayTests()
    {
        //since these lists are recycled between multiple tests, they need to be cleared between each test
        messageIdListStoredOnC2DSend.clear();
        messageIdListStoredOnReceive.clear();

        try
        {
            Thread.sleep(INTERTEST_GUARDIAN_DELAY_MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void receiveMessagesOverIncludingProperties() throws Exception
    {
        if (testInstance.protocol == HTTPS)
        {
            testInstance.client.setOption(SET_MINIMUM_POLLING_INTERVAL, ONE_SECOND_POLLING_INTERVAL);
        }

        IotHubServicesCommon.openClientWithRetry(testInstance.client);

        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();

        if (testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS)
        {
            callback = new MessageCallbackMqtt();
        }

        Success messageReceived = new Success();

        if (testInstance.client instanceof DeviceClient)
        {
            ((DeviceClient) testInstance.client).setMessageCallback(callback, messageReceived);
        }
        else if (testInstance.client instanceof ModuleClient)
        {
            ((ModuleClient) testInstance.client).setMessageCallback(callback, messageReceived);
        }

        if (testInstance.client instanceof DeviceClient)
        {
            sendMessageToDevice(testInstance.device.getDeviceId(), testInstance.protocol.toString());
        }
        else if (testInstance.client instanceof ModuleClient)
        {
            sendMessageToModule(testInstance.device.getDeviceId(), testInstance.module.getId(), testInstance.protocol.toString());
        }

        waitForMessageToBeReceived(messageReceived, testInstance.protocol.toString());

        Thread.sleep(200);
        testInstance.client.closeNow();
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void receiveMessagesWithTCPConnectionDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol == HTTPS)
        {
            //test case not applicable
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void receiveMessagesWithAmqpsConnectionDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsConnectionDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void receiveMessagesWithAmqpsSessionDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
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

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
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

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void receiveMessagesWithAmqpsD2CLinkDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
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

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void receiveMessagesWithAmqpsMethodReqLinkDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Method Req link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsMethodReqLinkDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void receiveMessagesWithAmqpsMethodRespLinkDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Method Resp link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void receiveMessagesWithAmqpsTwinReqLinkDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Twin Req link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsTwinReqLinkDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void receiveMessagesWithAmqpsTwinRespLinkDrop() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Twin Req link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsTwinRespLinkDropErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void receiveMessagesWithGracefulShutdownAmqp() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsGracefulShutdownErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void receiveMessagesWithGracefulShutdownMqtt() throws IOException, IotHubException, InterruptedException
    {
        if (testInstance.protocol != MQTT && testInstance.protocol != MQTT_WS)
        {
            //test case not applicable
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.mqttGracefulShutdownErrorInjectionMessage(DefaultDelayInSec, DefaultDurationInSec));
    }

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
        IotHubServicesCommon.openClientWithRetry(testInstance.client);

        // set call back for device client for receiving message
        com.microsoft.azure.sdk.iot.device.MessageCallback callBackOnRx = new MessageCallbackForBackToBackC2DMessages();

        if (testInstance.client instanceof DeviceClient)
        {
            ((DeviceClient) testInstance.client).setMessageCallback(callBackOnRx, null);
        }
        else if (testInstance.client instanceof ModuleClient)
        {
            ((ModuleClient) testInstance.client).setMessageCallback(callBackOnRx, null);
        }

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
            CompletableFuture<Void> future;
            if (testInstance.client instanceof DeviceClient)
            {
                future = serviceClient.sendAsync(testInstance.device.getDeviceId(), serviceMessage);
                futureList.add(future);
            }
            else if (testInstance.client instanceof ModuleClient)
            {
                serviceClient.send(testInstance.device.getDeviceId(), testInstance.module.getId(), serviceMessage);
            }
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
        testInstance.client.closeNow(); //close the device client connection
        assertTrue(testInstance.protocol + ", " + testInstance.authenticationType + ": Received messages don't match up with sent messages", messageIdListStoredOnReceive.containsAll(messageIdListStoredOnC2DSend)); // check if the received list is same as the actual list that was created on sending the messages
        messageIdListStoredOnReceive.clear();
    }

    private void errorInjectionTestFlow(Message errorInjectionMessage) throws IOException, IotHubException, InterruptedException
    {
        final ArrayList<IotHubConnectionStatus> connectionStatusUpdates = new ArrayList<>();
        testInstance.client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
            {
                connectionStatusUpdates.add(status);
            }
        }, null);

        IotHubServicesCommon.openClientWithRetry(testInstance.client);

        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();

        if (testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS)
        {
            callback = new MessageCallbackMqtt();
        }

        Success messageReceived = new Success();
        if (testInstance.client instanceof DeviceClient)
        {
            ((DeviceClient) testInstance.client).setMessageCallback(callback, messageReceived);
        }
        else if (testInstance.client instanceof ModuleClient)
        {
            ((ModuleClient) testInstance.client).setMessageCallback(callback, messageReceived);
        }

        //error injection message is not guaranteed to be ack'd by service so it may be re-sent. By setting expiry time,
        // we ensure that error injection message isn't resent to service too many times. The message will still likely
        // be sent 3 or 4 times causing 3 or 4 disconnections, but the test should recover anyways.
        errorInjectionMessage.setExpiryTime(200);
        testInstance.client.sendEventAsync(errorInjectionMessage, new EventCallback(null), null);

        //wait to send the message because we want to ensure that the tcp connection drop happens beforehand and we
        // want the connection to be re-established before sending anything from service client
        IotHubServicesCommon.waitForStabilizedConnection(connectionStatusUpdates, ERROR_INJECTION_RECOVERY_TIMEOUT);

        if (testInstance.client instanceof DeviceClient)
        {
            sendMessageToDevice(testInstance.device.getDeviceId(), testInstance.protocol.toString());
        }
        else if (testInstance.client instanceof ModuleClient)
        {
            sendMessageToModule(testInstance.device.getDeviceId(), testInstance.module.getId(), testInstance.protocol.toString());
        }

        waitForMessageToBeReceived(messageReceived, testInstance.protocol.toString());

        Thread.sleep(200);
        testInstance.client.closeNow();

        assertTrue(testInstance.protocol + ", " + testInstance.authenticationType + ": Error Injection message did not cause service to drop TCP connection", connectionStatusUpdates.contains(IotHubConnectionStatus.DISCONNECTED_RETRYING));
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
        serviceClient.open();
        serviceClient.send(deviceId, serviceMessage);
    }

    private void sendMessageToModule(String deviceId, String moduleId, String protocolName) throws IotHubException, IOException
    {
        String messageString = "Java service e2e test message to be received over " + protocolName + " protocol";
        com.microsoft.azure.sdk.iot.service.Message serviceMessage = new com.microsoft.azure.sdk.iot.service.Message(messageString);
        serviceMessage.setCorrelationId(expectedCorrelationId);
        serviceMessage.setMessageId(expectedMessageId);
        serviceMessage.setProperties(messageProperties);
        serviceClient.open();
        serviceClient.send(deviceId, moduleId, serviceMessage);
    }

    private void waitForMessageToBeReceived(Success messageReceived, String protocolName)
    {
        try
        {
            long startTime = System.currentTimeMillis();
            while (!messageReceived.wasCallbackFired())
            {
                Thread.sleep(300);

                if (System.currentTimeMillis() - startTime > RECEIVE_TIMEOUT)
                {
                    fail(testInstance.protocol + ", " + testInstance.authenticationType + ": Timed out waiting to receive message");
                }
            }

            if (!messageReceived.getResult())
            {
                Assert.fail(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving message over " + protocolName + " protocol failed. Received message was missing expected properties");
            }
        }
        catch (InterruptedException e)
        {
            Assert.fail(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving message over " + protocolName + " protocol failed. Unexpected interrupted exception occurred");
        }
    }

    private void waitForBackToBackC2DMessagesToBeReceived()
    {
        try
        {
            long startTime = System.currentTimeMillis();
        
            // check if all messages are received.
            while (messageIdListStoredOnReceive.size() != MAX_COMMANDS_TO_SEND)
            {
                Thread.sleep(100);

                System.out.println(messageIdListStoredOnReceive.size());

                if (System.currentTimeMillis() - startTime > RECEIVE_TIMEOUT)
                {
                    Assert.fail(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving messages timed out.");
                }
            }
        }
        catch (InterruptedException e)
        {
            Assert.fail(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving message failed. Unexpected interrupted exception occurred.");
        }
    }
}
