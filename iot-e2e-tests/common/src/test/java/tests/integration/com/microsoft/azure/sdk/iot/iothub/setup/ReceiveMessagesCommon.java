/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry.ReceiveMessagesTests;

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
@Slf4j
public class ReceiveMessagesCommon extends IntegrationTest
{
    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs()
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        IntegrationTest.isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        IntegrationTest.isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        messageProperties = new HashMap<>(3);
        messageProperties.put("name1", "value1");
        messageProperties.put("name2", "value2");
        messageProperties.put("name3", "value3");

        List inputs = new ArrayList(Arrays.asList(
                new Object[][]
                        {
                                //sas token module client
                                {MQTT, SAS, ClientType.DEVICE_CLIENT},
                                {AMQPS, SAS, ClientType.DEVICE_CLIENT},
                                {MQTT_WS, SAS, ClientType.DEVICE_CLIENT},
                                {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT},

                                //x509 module client
                                {HTTPS, SELF_SIGNED, ClientType.DEVICE_CLIENT},
                                {MQTT, SELF_SIGNED, ClientType.DEVICE_CLIENT},
                                {AMQPS, SELF_SIGNED, ClientType.DEVICE_CLIENT}
                        }
        ));

        if (!IntegrationTest.isBasicTierHub)
        {
            inputs.addAll(Arrays.asList(
                    new Object[][]
                            {
                                    //sas token module client
                                    {MQTT, SAS, ClientType.MODULE_CLIENT},
                                    {AMQPS, SAS, ClientType.MODULE_CLIENT},
                                    {MQTT_WS, SAS, ClientType.MODULE_CLIENT},
                                    {AMQPS_WS, SAS, ClientType.MODULE_CLIENT},

                                    //x509 module client
                                    {MQTT, SELF_SIGNED, ClientType.MODULE_CLIENT},
                                    {AMQPS, SELF_SIGNED, ClientType.MODULE_CLIENT}
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

    public ReceiveMessagesCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        this.testInstance = new ReceiveMessagesTestInstance(protocol, authenticationType, clientType);
    }

    public class ReceiveMessagesTestInstance
    {
        public IotHubClientProtocol protocol;
        public TestIdentity identity;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public RegistryManager registryManager;
        public ServiceClient serviceClient;

        public ReceiveMessagesTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.publicKeyCert = x509CertificateGenerator.getPublicCertificate();
            this.privateKey = x509CertificateGenerator.getPrivateKey();
            this.x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();
            this.registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
            this.serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        }

        public void setup() throws Exception
        {
            if (clientType == ClientType.DEVICE_CLIENT)
            {
                this.identity = Tools.getTestDevice(iotHubConnectionString, this.protocol, this.authenticationType, false);
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                this.identity = Tools.getTestModule(iotHubConnectionString, this.protocol, this.authenticationType, false);
            }

            if ((this.protocol == AMQPS || this.protocol == AMQPS_WS) && this.authenticationType == SAS)
            {
                this.identity.getClient().setOption("SetAmqpOpenAuthenticationSessionTimeout", AMQP_AUTHENTICATION_SESSION_TIMEOUT_SECONDS);
                this.identity.getClient().setOption("SetAmqpOpenDeviceSessionsTimeout", AMQP_DEVICE_SESSION_TIMEOUT_SECONDS);
            }

            testInstance.serviceClient.open();
        }

        public void dispose()
        {
            try
            {
                if (this.identity != null && this.identity.getClient() != null)
                {
                    this.identity.getClient().closeNow();
                }

                this.serviceClient.close();
            }
            catch (IOException e)
            {
                log.error("Failed to close clients during cleanup", e);
            }

            Tools.disposeTestIdentity(this.identity, iotHubConnectionString);
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
                    Assert.fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Timed out waiting to receive message", testInstance.identity.getClient()));
                }
            }

            if (!messageReceived.getResult())
            {
                Assert.fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving message over " + protocolName + " protocol failed. Received message was missing expected properties", testInstance.identity.getClient()));
            }
        }
        catch (InterruptedException e)
        {
            Assert.fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving message over " + protocolName + " protocol failed. Unexpected interrupted exception occurred", testInstance.identity.getClient()));
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
                    Assert.fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving messages timed out.", testInstance.identity.getClient()));
                }
            }
        }
        catch (InterruptedException e)
        {
            Assert.fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving message failed. Unexpected interrupted exception occurred.", testInstance.identity.getClient()));
        }
    }
}
