/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.After;
import org.junit.Assert;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry.ReceiveMessagesTests;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/**
 * Utility functions, setup and teardown for all C2D telemetry integration tests. This class should not contain any tests,
 * but any child class should.
 */
public class ReceiveMessagesCommon extends IntegrationTest
{
    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        IntegrationTest.isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        IntegrationTest.isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        String publicKeyCert = certificateGenerator.getPublicCertificate();
        String privateKey = certificateGenerator.getPrivateKey();
        String x509Thumbprint = certificateGenerator.getX509Thumbprint();

        messageProperties = new HashMap<>(3);
        messageProperties.put("name1", "value1");
        messageProperties.put("name2", "value2");
        messageProperties.put("name3", "value3");

        List inputs = new ArrayList(Arrays.asList(
                new Object[][]
                        {
                                //sas token module client
                                {MQTT, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                {AMQPS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                {MQTT_WS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},

                                //x509 module client
                                {HTTPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                {MQTT, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                {AMQPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint}
                        }
        ));

        if (!IntegrationTest.isBasicTierHub)
        {
            inputs.addAll(Arrays.asList(
                    new Object[][]
                            {
                                    //sas token module client
                                    {MQTT, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                    {AMQPS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                    {MQTT_WS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                    {AMQPS_WS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},

                                    //x509 module client
                                    {MQTT, SELF_SIGNED, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                    {AMQPS, SELF_SIGNED, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint}
                            }
            ));
        }

        return inputs;
    }

    protected static Map<String, String> messageProperties = new HashMap<>(3);

    protected final static String SET_MINIMUM_POLLING_INTERVAL = "SetMinimumPollingInterval";
    protected final static Long ONE_SECOND_POLLING_INTERVAL = 1000L;

    // variables used in E2E test for sending back to back messages using C2D sendAsync method
    protected static final int MAX_COMMANDS_TO_SEND = 5; // maximum commands to be sent in a loop

    protected static String iotHubConnectionString = "";

    // How much to wait until receiving a message from the server, in milliseconds
    protected static final int RECEIVE_TIMEOUT_MILLISECONDS = 3 * 60 * 1000; // 3 minutes

    protected static String expectedCorrelationId = "1234";
    protected static String expectedMessageId = "5678";
    protected static final long ERROR_INJECTION_RECOVERY_TIMEOUT_MILLISECONDS = 60 * 1000; // 1 minute

    public ReceiveMessagesTestInstance testInstance;

    public ReceiveMessagesCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws Exception
    {
        this.testInstance = new ReceiveMessagesTestInstance(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    public class ReceiveMessagesTestInstance
    {
        public InternalClient client;
        public IotHubClientProtocol protocol;
        public BaseDevice identity;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public RegistryManager registryManager;
        public ServiceClient serviceClient;

        public ReceiveMessagesTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws Exception
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.publicKeyCert = publicKeyCert;
            this.privateKey = privateKey;
            this.x509Thumbprint = x509Thumbprint;
            this.registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
            this.serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        }

        public void setup() throws Exception
        {
            String TEST_UUID = UUID.randomUUID().toString();
            SSLContext sslContext = SSLContextBuilder.buildSSLContext(publicKeyCert, privateKey);
            if (clientType == ClientType.DEVICE_CLIENT)
            {
                if (authenticationType == SAS)
                {
                    //sas device client
                    String deviceId = "java-method-e2e-test-device".concat("-" + TEST_UUID);
                    Device device = Device.createFromId(deviceId, null, null);
                    device = Tools.addDeviceWithRetry(registryManager, device);
                    this.client = new DeviceClient(registryManager.getDeviceConnectionString(device), protocol);
                    this.identity = device;
                }
                else if (authenticationType == SELF_SIGNED)
                {
                    //x509 device client
                    String deviceX509Id = "java-method-e2e-test-device-x509".concat("-" + TEST_UUID);
                    Device deviceX509 = Device.createDevice(deviceX509Id, AuthenticationType.SELF_SIGNED);
                    deviceX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
                    deviceX509 = Tools.addDeviceWithRetry(registryManager, deviceX509);
                    this.client = new DeviceClient(registryManager.getDeviceConnectionString(deviceX509), protocol, sslContext);
                    this.identity = deviceX509;
                }
                else
                {
                    throw new Exception("Test code has not been written for this path yet");
                }
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                if (authenticationType == SAS)
                {
                    //sas device client to house the module under test
                    String deviceId = "java-receive-message-e2e-test-device".concat("-" + TEST_UUID);
                    Device device = Device.createFromId(deviceId, null, null);
                    device = Tools.addDeviceWithRetry(registryManager, device);

                    //sas module client
                    String moduleId = "java-receive-message-e2e-test-module".concat("-" + TEST_UUID);
                    Module module = Module.createFromId(deviceId, moduleId, null);
                    module = Tools.addModuleWithRetry(registryManager, module);
                    this.client = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), protocol);
                    this.identity = module;
                }
                else if (authenticationType == SELF_SIGNED)
                {
                    //x509 device client to house the module under test
                    String deviceX509Id = "java-receive-message-e2e-test-device-x509".concat("-" + TEST_UUID);
                    Device deviceX509 = Device.createDevice(deviceX509Id, AuthenticationType.SELF_SIGNED);
                    deviceX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
                    deviceX509 = Tools.addDeviceWithRetry(registryManager, deviceX509);

                    //x509 module client
                    String moduleX509Id = "java-receive-message-e2e-test-module-x509".concat("-" + TEST_UUID);
                    Module moduleX509 = Module.createModule(deviceX509Id, moduleX509Id, AuthenticationType.SELF_SIGNED);
                    moduleX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
                    moduleX509 = Tools.addModuleWithRetry(registryManager, moduleX509);
                    this.client = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), protocol, sslContext);
                    this.identity = moduleX509;
                }
                else
                {
                    throw new Exception("Test code has not been written for this path yet");
                }

                if ((this.protocol == AMQPS || this.protocol == AMQPS_WS) && this.authenticationType == SAS)
                {
                    this.client.setOption("SetAmqpOpenAuthenticationSessionTimeout", AMQP_AUTHENTICATION_SESSION_TIMEOUT_SECONDS);
                    this.client.setOption("SetAmqpOpenDeviceSessionsTimeout", AMQP_DEVICE_SESSION_TIMEOUT_SECONDS);
                }
            }

            testInstance.serviceClient.open();
        }

        public void dispose()
        {
            try
            {
                this.client.closeNow();
                this.registryManager.removeDevice(this.identity.getDeviceId()); //removes all modules associated with this device, too
                this.serviceClient.close();
            }
            catch (Exception e)
            {
                //not a big deal if dispose fails. This test suite is not testing the functions in this cleanup.
                // If identities are left registered, they will be deleted my nightly cleanup job anyways
            }
        }
    }

    public void setupTest() throws Exception
    {
        testInstance.setup();
    }

    @After
    public void tearDownTest()
    {
        testInstance.dispose();
    }

    public static class MessageCallbackForBackToBackC2DMessages implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        private final List messageIdListStoredOnReceive;

        public MessageCallbackForBackToBackC2DMessages(List messageIdListStoredOnReceive)
        {
            this.messageIdListStoredOnReceive = messageIdListStoredOnReceive;
        }

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

    public static class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
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

        return msg.getMessageId() != null && msg.getMessageId().equals(expectedMessageId);//all system properties are as expected
    }

    protected void sendMessageToDevice(String deviceId, String protocolName) throws IotHubException, IOException
    {
        String messageString = "Java service e2e test message to be received over " + protocolName + " protocol";
        com.microsoft.azure.sdk.iot.service.Message serviceMessage = new com.microsoft.azure.sdk.iot.service.Message(messageString);
        serviceMessage.setCorrelationId(expectedCorrelationId);
        serviceMessage.setMessageId(expectedMessageId);
        serviceMessage.setProperties(messageProperties);
        testInstance.serviceClient.send(deviceId, serviceMessage);
    }

    protected void sendMessageToModule(String deviceId, String moduleId, String protocolName) throws IotHubException, IOException
    {
        String messageString = "Java service e2e test message to be received over " + protocolName + " protocol";
        com.microsoft.azure.sdk.iot.service.Message serviceMessage = new com.microsoft.azure.sdk.iot.service.Message(messageString);
        serviceMessage.setCorrelationId(expectedCorrelationId);
        serviceMessage.setMessageId(expectedMessageId);
        serviceMessage.setProperties(messageProperties);
        testInstance.serviceClient.send(deviceId, moduleId, serviceMessage);
    }

    protected void waitForMessageToBeReceived(Success messageReceived, String protocolName)
    {
        try
        {
            long startTime = System.currentTimeMillis();
            while (!messageReceived.wasCallbackFired())
            {
                Thread.sleep(300);

                if (System.currentTimeMillis() - startTime > RECEIVE_TIMEOUT_MILLISECONDS)
                {
                    Assert.fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Timed out waiting to receive message", testInstance.client));
                }
            }

            if (!messageReceived.getResult())
            {
                Assert.fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving message over " + protocolName + " protocol failed. Received message was missing expected properties", testInstance.client));
            }
        }
        catch (InterruptedException e)
        {
            Assert.fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving message over " + protocolName + " protocol failed. Unexpected interrupted exception occurred", testInstance.client));
        }
    }

    protected void waitForBackToBackC2DMessagesToBeReceived(List messageIdListStoredOnReceive)
    {
        try
        {
            long startTime = System.currentTimeMillis();

            // check if all messages are received.
            while (messageIdListStoredOnReceive.size() != MAX_COMMANDS_TO_SEND)
            {
                Thread.sleep(100);

                if (System.currentTimeMillis() - startTime > RECEIVE_TIMEOUT_MILLISECONDS)
                {
                    Assert.fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving messages timed out.", testInstance.client));
                }
            }
        }
        catch (InterruptedException e)
        {
            Assert.fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving message failed. Unexpected interrupted exception occurred.", testInstance.client));
        }
    }
}
