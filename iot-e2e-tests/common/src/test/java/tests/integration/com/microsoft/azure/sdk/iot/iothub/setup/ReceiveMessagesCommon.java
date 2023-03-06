/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.messaging.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry.ReceiveMessagesTests;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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

        return Arrays.asList(
            new Object[][]
                {
                    {HTTPS, SAS, ClientType.DEVICE_CLIENT},
                    {AMQPS, SAS, ClientType.DEVICE_CLIENT},
                    {MQTT, SAS, ClientType.DEVICE_CLIENT},
                    {AMQPS, SAS, ClientType.MODULE_CLIENT},
                    {MQTT, SAS, ClientType.MODULE_CLIENT},
                });
    }

    protected static Map<String, String> messageProperties = new HashMap<>(3);

    protected static int MESSAGE_SIZE_IN_BYTES = 1000;
    protected static int LARGE_MESSAGE_SIZE_IN_BYTES = 65000; // Max C2D message size is 65535

    protected static String iotHubConnectionString = "";

    // How much to wait until receiving a message from the server, in milliseconds
    protected static final int RECEIVE_TIMEOUT_MILLISECONDS = 3 * 60 * 1000; // 3 minutes

    protected static final int FEEDBACK_TIMEOUT_MILLIS = 60 * 1000; // 1 minute

    public ReceiveMessagesTestInstance testInstance;

    public ReceiveMessagesCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        this.testInstance = new ReceiveMessagesTestInstance(protocol, authenticationType, clientType);
    }

    public static class ReceiveMessagesTestInstance
    {
        public IotHubClientProtocol protocol;
        public TestIdentity identity;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public RegistryClient registryClient;
        public MessagingClient messagingClient;

        public ReceiveMessagesTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.publicKeyCert = x509CertificateGenerator.getPublicCertificatePEM();
            this.privateKey = x509CertificateGenerator.getPrivateKeyPEM();
            this.x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();
            this.registryClient = new RegistryClient(iotHubConnectionString, RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
            this.messagingClient = new MessagingClient(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
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

            this.messagingClient.open();
        }

        public void dispose()
        {
            if (this.identity != null && this.identity.getClient() != null)
            {
                this.identity.getClient().close();
            }

            Tools.disposeTestIdentity(this.identity, iotHubConnectionString);

            try
            {
                this.messagingClient.close();
            }
            catch (InterruptedException e)
            {
                log.warn("Failed to close messagingClient", e);
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
        private final List<String> messageIdListStoredOnReceive;

        public MessageCallbackForBackToBackC2DMessages(List<String> messageIdListStoredOnReceive)
        {
            this.messageIdListStoredOnReceive = messageIdListStoredOnReceive;
        }

        public IotHubMessageResult onCloudToDeviceMessageReceived(com.microsoft.azure.sdk.iot.device.Message msg, Object context)
        {
            messageIdListStoredOnReceive.add(msg.getMessageId()); // add received messsage id to messageList
            return IotHubMessageResult.COMPLETE;
        }
    }

    public static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        private final com.microsoft.azure.sdk.iot.service.messaging.Message expectedMessage;

        public MessageCallback()
        {
            this.expectedMessage = null;
        }

        public MessageCallback(com.microsoft.azure.sdk.iot.service.messaging.Message expectedMessage)
        {
            this.expectedMessage = expectedMessage;
        }

        public IotHubMessageResult onCloudToDeviceMessageReceived(com.microsoft.azure.sdk.iot.device.Message msg, Object context)
        {
            boolean resultValue = true;
            HashMap<String, String> messageProperties = (HashMap<String, String>) ReceiveMessagesTests.messageProperties;
            Success messageReceived = (Success) context;

            if (expectedMessage != null)
            {
                if (!hasExpectedProperties(msg, messageProperties) || !hasExpectedSystemProperties(msg, expectedMessage.getCorrelationId(), expectedMessage.getMessageId()))
                {
                    log.warn("Unexpected properties in the received message");
                    resultValue = false;
                }

                if (!ArrayUtils.isEquals(this.expectedMessage.getBytes(), msg.getBytes()))
                {
                    log.warn("Unexpected payload in the received message");
                    resultValue = false;
                }
            }

            messageReceived.callbackWasFired();
            messageReceived.setResult(resultValue);
            return IotHubMessageResult.COMPLETE;
        }
    }

    public static class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        private final com.microsoft.azure.sdk.iot.service.messaging.Message expectedMessage;

        public MessageCallbackMqtt()
        {
            this.expectedMessage = null;
        }

        public MessageCallbackMqtt(com.microsoft.azure.sdk.iot.service.messaging.Message expectedMessage)
        {
            this.expectedMessage = expectedMessage;
        }

        public IotHubMessageResult onCloudToDeviceMessageReceived(com.microsoft.azure.sdk.iot.device.Message msg, Object context)
        {
            HashMap<String, String> messageProperties = (HashMap<String, String>) ReceiveMessagesTests.messageProperties;
            Success messageReceived = (Success)context;
            boolean resultValue = true;

            if (this.expectedMessage != null)
            {
                if (!hasExpectedProperties(msg, messageProperties) || !hasExpectedSystemProperties(msg, expectedMessage.getCorrelationId(), expectedMessage.getMessageId()))
                {
                    log.warn("Unexpected properties in the received message");
                    resultValue = false;
                }

                if (!ArrayUtils.isEquals(this.expectedMessage.getBytes(), msg.getBytes()))
                {
                    log.warn("Unexpected payload in the received message");
                    resultValue = false;
                }
            }

            messageReceived.callbackWasFired();
            messageReceived.setResult(resultValue);

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

    public void receiveMessage(int messageSize) throws Exception
    {
        testInstance.identity.getClient().open(false);

        com.microsoft.azure.sdk.iot.service.messaging.Message serviceMessage = createCloudToDeviceMessage(messageSize);
        serviceMessage.setMessageId(UUID.randomUUID().toString());

        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback(serviceMessage);

        if (testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS)
        {
            callback = new MessageCallbackMqtt(serviceMessage);
        }

        Success messageReceived = new Success();

        if (testInstance.identity.getClient() instanceof DeviceClient)
        {
            ((DeviceClient) testInstance.identity.getClient()).setMessageCallback(callback, messageReceived);
        }
        else if (testInstance.identity.getClient() instanceof ModuleClient)
        {
            ((ModuleClient) testInstance.identity.getClient()).setMessageCallback(callback, messageReceived);
        }

        if (testInstance.identity.getClient() instanceof DeviceClient)
        {
            testInstance.messagingClient.send(testInstance.identity.getDeviceId(), serviceMessage);
        }
        else if (testInstance.identity.getClient() instanceof ModuleClient)
        {
            testInstance.messagingClient.send(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), serviceMessage);
        }

        waitForMessageToBeReceived(messageReceived, testInstance.protocol.toString());

        // flakey feature
        //waitForFeedbackMessage(serviceMessage.getMessageId());

        Thread.sleep(200);
        testInstance.identity.getClient().close();
    }

    protected static boolean hasExpectedSystemProperties(com.microsoft.azure.sdk.iot.device.Message msg, String expectedCorrelationId, String expectedMessageId)
    {
        if (msg.getCorrelationId() == null || !msg.getCorrelationId().equals(expectedCorrelationId))
        {
            return false;
        }

        return msg.getMessageId() != null && msg.getMessageId().equals(expectedMessageId);//all system properties are as expected
    }

    protected com.microsoft.azure.sdk.iot.service.messaging.Message createCloudToDeviceMessage(int messageSize) throws IotHubException, IOException
    {
        byte[] payload = new byte[messageSize];
        new Random().nextBytes(payload);
        com.microsoft.azure.sdk.iot.service.messaging.Message serviceMessage = new com.microsoft.azure.sdk.iot.service.messaging.Message(payload);
        serviceMessage.setCorrelationId(UUID.randomUUID().toString());
        serviceMessage.setMessageId(UUID.randomUUID().toString());
        serviceMessage.setProperties(messageProperties);
        serviceMessage.setDeliveryAcknowledgement(DeliveryAcknowledgement.Full);
        return serviceMessage;
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
                    fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Timed out waiting to receive message", testInstance.identity.getClient()));
                }
            }

            if (!messageReceived.getResult())
            {
                fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving message over " + protocolName + " protocol failed. Received message was missing expected properties", testInstance.identity.getClient()));
            }
        }
        catch (InterruptedException e)
        {
            fail(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Receiving message over " + protocolName + " protocol failed. Unexpected interrupted exception occurred", testInstance.identity.getClient()));
        }
    }

    private void waitForFeedbackMessage(String expectedMessageId) throws InterruptedException, IOException, IotHubException, TimeoutException
    {
        final Success feedbackReceived = new Success();

        Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor = feedbackBatch ->
        {
            for (FeedbackRecord feedbackRecord : feedbackBatch.getRecords())
            {
                if (feedbackRecord.getDeviceId().equals(testInstance.identity.getDeviceId())
                    && feedbackRecord.getOriginalMessageId().equals(expectedMessageId))
                {
                    feedbackReceived.setResult(true);
                    feedbackReceived.callbackWasFired();
                }
            }

            return AcknowledgementType.ABANDON;
        };

        MessageFeedbackProcessorClientOptions messageFeedbackProcessorClientOptions =
            MessageFeedbackProcessorClientOptions.builder()
                .build();

        MessageFeedbackProcessorClient messageFeedbackProcessorClient =
            new MessageFeedbackProcessorClient(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS_WS, feedbackMessageProcessor, messageFeedbackProcessorClientOptions);

        messageFeedbackProcessorClient.start();

        long startTime = System.currentTimeMillis();
        while (!feedbackReceived.wasCallbackFired())
        {
            Thread.sleep(1000);

            if (System.currentTimeMillis() - startTime > FEEDBACK_TIMEOUT_MILLIS)
            {
                fail("Timed out waiting on notification for device " + testInstance.identity.getDeviceId());
            }
        }

        messageFeedbackProcessorClient.stop();
        assertTrue(feedbackReceived.getResult());
    }
}
