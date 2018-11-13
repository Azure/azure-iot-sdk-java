/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.setup;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.telemetry.ReceiveMessagesTests;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assert.fail;

/**
 * Utility functions, setup and teardown for all C2D telemetry integration tests. This class should not contain any tests,
 * but any child class should.
 */
public class ReceiveMessagesCommon extends MethodNameLoggingIntegrationTest
{
    protected static final long DEFAULT_TEST_TIMEOUT = 3 * 60 * 1000;
    protected static Map<String, String> messageProperties = new HashMap<>(3);

    protected final static String SET_MINIMUM_POLLING_INTERVAL = "SetMinimumPollingInterval";
    protected final static Long ONE_SECOND_POLLING_INTERVAL = 1000L;

    // variables used in E2E test for sending back to back messages using C2D sendAsync method
    protected static final int MAX_COMMANDS_TO_SEND = 5; // maximum commands to be sent in a loop
    protected static final List messageIdListStoredOnC2DSend = new ArrayList(); // store the message id list on sending C2D commands using service client
    protected static final List messageIdListStoredOnReceive = new ArrayList(); // store the message id list on receiving C2D commands using device client

    protected static String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    protected static String iotHubConnectionString = "";
    protected static RegistryManager registryManager;

    protected static Device device;
    protected static Device deviceX509;

    protected static Module module;
    protected static Module moduleX509;

    protected static ServiceClient serviceClient;

    // How much to wait until receiving a message from the server, in milliseconds
    protected static final int RECEIVE_TIMEOUT = 3 * 60 * 1000; // 3 minutes

    protected static String expectedCorrelationId = "1234";
    protected static String expectedMessageId = "5678";
    protected static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;
    protected static final long ERROR_INJECTION_RECOVERY_TIMEOUT = 1 * 60 * 1000; // 1 minute

    public ReceiveMessagesTestInstance testInstance;

    public ReceiveMessagesCommon(InternalClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, String clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        this.testInstance = new ReceiveMessagesTestInstance(client, protocol, identity, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    protected static Collection<BaseDevice> getIdentities(Collection inputs)
    {
        Set<BaseDevice> identities = new HashSet<>();

        Object[] inputArray = inputs.toArray();
        for (int i = 0; i < inputs.size(); i++)
        {
            Object[] inputsInstance = (Object[]) inputArray[i];
            identities.add((BaseDevice) inputsInstance[2]);
        }

        return identities;
    }

    @BeforeClass
    public static void classSetup()
    {
        try
        {
            registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
            serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
            serviceClient.open();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Unexpected exception occurred");
        }
    }

    protected static Collection inputsCommon(ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException, InterruptedException
    {
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

        if (clientType == ClientType.MODULE_CLIENT)
        {
            registryManager.addModule(module);
            registryManager.addModule(moduleX509);
        }

        Thread.sleep(2000);

        messageProperties = new HashMap<>(3);
        messageProperties.put("name1", "value1");
        messageProperties.put("name2", "value2");
        messageProperties.put("name3", "value3");

        serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        serviceClient.open();

        List inputs;
        if (clientType == ClientType.DEVICE_CLIENT)
        {
            inputs = Arrays.asList(
                    new Object[][]
                            {
                                    //sas token
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), HTTPS), HTTPS, device, SAS, "DeviceClient", publicKeyCert, privateKey, x509Thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), MQTT), MQTT, device, SAS, "DeviceClient", publicKeyCert, privateKey, x509Thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), MQTT_WS), MQTT_WS, device, SAS, "DeviceClient", publicKeyCert, privateKey, x509Thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS), AMQPS, device, SAS, "DeviceClient", publicKeyCert, privateKey, x509Thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS_WS), AMQPS_WS, device, SAS, "DeviceClient", publicKeyCert, privateKey, x509Thumbprint},

                                    //x509
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), HTTPS, publicKeyCert, false, privateKey, false), HTTPS, deviceX509, SELF_SIGNED, "DeviceClient", publicKeyCert, privateKey, x509Thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), MQTT, publicKeyCert, false, privateKey, false), MQTT, deviceX509, SELF_SIGNED, "DeviceClient", publicKeyCert, privateKey, x509Thumbprint},
                                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), AMQPS, publicKeyCert, false, privateKey, false), AMQPS, deviceX509, SELF_SIGNED, "DeviceClient", publicKeyCert, privateKey, x509Thumbprint}
                            }
            );
        }
        else
        {
            inputs = Arrays.asList(
                    new Object[][]
                            {
                                    //sas token module client
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), MQTT), MQTT, module, SAS, "ModuleClient", publicKeyCert, privateKey, x509Thumbprint},
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), MQTT_WS), MQTT_WS, module, SAS, "ModuleClient", publicKeyCert, privateKey, x509Thumbprint},
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), AMQPS), AMQPS, module, SAS, "ModuleClient", publicKeyCert, privateKey, x509Thumbprint},
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), AMQPS_WS), AMQPS_WS, module, SAS, "ModuleClient", publicKeyCert, privateKey, x509Thumbprint},

                                    //x509 module client
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), MQTT, publicKeyCert, false, privateKey, false), MQTT, moduleX509, SELF_SIGNED, "ModuleClient", publicKeyCert, privateKey, x509Thumbprint},
                                    {new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), AMQPS, publicKeyCert, false, privateKey, false), AMQPS, moduleX509, SELF_SIGNED, "ModuleClient", publicKeyCert, privateKey, x509Thumbprint}                           }
            );
        }

        return inputs;
    }

    public class ReceiveMessagesTestInstance
    {
        public InternalClient client;
        public IotHubClientProtocol protocol;
        public BaseDevice identity;
        public AuthenticationType authenticationType;
        public String clientType;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;

        public ReceiveMessagesTestInstance(InternalClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, String clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
        {
            this.client = client;
            this.protocol = protocol;
            this.identity = identity;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.publicKeyCert = publicKeyCert;
            this.privateKey = privateKey;
            this.x509Thumbprint = x509Thumbprint;
        }
    }

    protected static void tearDown(Collection<BaseDevice> identitiesToDispose)
    {
        if (registryManager != null)
        {
            Tools.removeDevicesAndModules(registryManager, identitiesToDispose);
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
            e.printStackTrace();
            TestCase.fail("Unexpected exception encountered");
        }
    }

    public static class MessageCallbackForBackToBackC2DMessages implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(com.microsoft.azure.sdk.iot.device.Message msg, Object context)
        {
            messageIdListStoredOnReceive.add(msg.getMessageId()); // add received messsage id to messageList
            return IotHubMessageResult.COMPLETE;
        }
    }

    public static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(com.microsoft.azure.sdk.iot.device.Message msg, Object context)
        {
            Boolean resultValue = true;
            HashMap<String, String> messageProperties = (HashMap<String, String>) ReceiveMessagesTests.messageProperties;
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

    public class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(com.microsoft.azure.sdk.iot.device.Message msg, Object context)
        {
            HashMap<String, String> messageProperties = (HashMap<String, String>) ReceiveMessagesTests.messageProperties;
            Success messageReceived = (Success)context;
            if (hasExpectedProperties(msg, messageProperties) && hasExpectedSystemProperties(msg))
            {
                messageReceived.setResult(true);
            }

            messageReceived.callbackWasFired();

            return IotHubMessageResult.COMPLETE;
        }
    }

    protected static boolean hasExpectedProperties(com.microsoft.azure.sdk.iot.device.Message msg, Map<String, String> messageProperties)
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

    protected static boolean hasExpectedSystemProperties(com.microsoft.azure.sdk.iot.device.Message msg)
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

    protected void sendMessageToDevice(String deviceId, String protocolName) throws IotHubException, IOException
    {
        String messageString = "Java service e2e test message to be received over " + protocolName + " protocol";
        com.microsoft.azure.sdk.iot.service.Message serviceMessage = new com.microsoft.azure.sdk.iot.service.Message(messageString);
        serviceMessage.setCorrelationId(expectedCorrelationId);
        serviceMessage.setMessageId(expectedMessageId);
        serviceMessage.setProperties(messageProperties);
        serviceClient.send(deviceId, serviceMessage);
    }

    protected void sendMessageToModule(String deviceId, String moduleId, String protocolName) throws IotHubException, IOException
    {
        String messageString = "Java service e2e test message to be received over " + protocolName + " protocol";
        com.microsoft.azure.sdk.iot.service.Message serviceMessage = new com.microsoft.azure.sdk.iot.service.Message(messageString);
        serviceMessage.setCorrelationId(expectedCorrelationId);
        serviceMessage.setMessageId(expectedMessageId);
        serviceMessage.setProperties(messageProperties);
        serviceClient.send(deviceId, moduleId, serviceMessage);
    }

    protected void waitForMessageToBeReceived(Success messageReceived, String protocolName)
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

    protected void waitForBackToBackC2DMessagesToBeReceived()
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
