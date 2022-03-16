/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry;


import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackRecord;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.messaging.MessageFeedbackProcessorClient;
import com.microsoft.azure.sdk.iot.service.messaging.MessageFeedbackProcessorClientOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Success;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestModuleIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.ReceiveMessagesCommon;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to receiving messages on a device/module.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class ReceiveMessagesTests extends ReceiveMessagesCommon
{
    public ReceiveMessagesTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        super(protocol, authenticationType, clientType);
    }

    @Before
    @SuppressWarnings("EmptyMethod")
    public void setupTest() throws Exception
    {
        super.setupTest();
    }

    @Test
    @StandardTierHubOnlyTest
    public void receiveMessage() throws Exception
    {
        receiveMessage(MESSAGE_SIZE_IN_BYTES);
    }

    // Test out receiving a near-maximum sized cloud to device message both for testing the sending of it from the
    // service client, but also to test how MQTT/HTTPS/AMQPS handle it on the receiving side. AMQP in particular
    // has some "partial delivery" scenarios that are worth having an e2e test around.
    @Test
    @ContinuousIntegrationTest
    @StandardTierHubOnlyTest
    public void receiveLargeMessage() throws Exception
    {
        receiveMessage(LARGE_MESSAGE_SIZE_IN_BYTES);
    }

    public void receiveMessage(int messageSize) throws Exception
    {
        testInstance.identity.getClient().open(false);

        Message serviceMessage = createCloudToDeviceMessage(messageSize);
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
