/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry;


import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Success;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestModuleIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.ReceiveMessagesCommon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

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
    public void receiveMessagesOverIncludingProperties() throws Exception
    {
        if (testInstance.protocol == HTTPS)
        {
            testInstance.identity.getClient().setOption(SET_MINIMUM_POLLING_INTERVAL, ONE_SECOND_POLLING_INTERVAL);
        }

        testInstance.identity.getClient().open();

        com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();

        if (testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS)
        {
            callback = new MessageCallbackMqtt();
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
            sendMessageToDevice(testInstance.identity.getDeviceId(), testInstance.protocol.toString());
        }
        else if (testInstance.identity.getClient() instanceof ModuleClient)
        {
            sendMessageToModule(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), testInstance.protocol.toString());
        }

        waitForMessageToBeReceived(messageReceived, testInstance.protocol.toString());

        Thread.sleep(200);
        testInstance.identity.getClient().closeNow();
    }

    @Test
    @StandardTierHubOnlyTest
    public void receiveBackToBackUniqueC2DCommandsOverAmqpsUsingSendAsync() throws Exception
    {
        List messageIdListStoredOnC2DSend = new ArrayList(); // store the message id list on sending C2D commands using service client
        List messageIdListStoredOnReceive = new ArrayList(); // store the message id list on receiving C2D commands using device client

        if (this.testInstance.protocol != AMQPS)
        {
            //only want to test for AMQPS
            return;
        }

        // This E2E test is for testing multiple C2D sends and make sure buffers are not getting overwritten
        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        // set identity to receive back to back different commands using AMQPS protocol
        testInstance.identity.getClient().open();

        // set call back for device client for receiving message
        com.microsoft.azure.sdk.iot.device.MessageCallback callBackOnRx = new MessageCallbackForBackToBackC2DMessages(messageIdListStoredOnReceive);

        if (testInstance.identity.getClient() instanceof DeviceClient)
        {
            ((DeviceClient) testInstance.identity.getClient()).setMessageCallback(callBackOnRx, null);
        }
        else if (testInstance.identity.getClient() instanceof ModuleClient)
        {
            ((ModuleClient) testInstance.identity.getClient()).setMessageCallback(callBackOnRx, null);
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
            if (testInstance.identity.getClient() instanceof DeviceClient)
            {
                future = testInstance.serviceClient.sendAsync(testInstance.identity.getDeviceId(), serviceMessage);
                futureList.add(future);
            }
            else if (testInstance.identity.getClient() instanceof ModuleClient)
            {
                testInstance.serviceClient.send(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), serviceMessage);
            }
        }

        int futureTimeout = 3;

        for (CompletableFuture<Void> future : futureList)
        {
            try
            {
                future.get(futureTimeout, TimeUnit.MINUTES);
            }
            catch (ExecutionException e)
            {
                Assert.fail(buildExceptionMessage("Exception : " + e.getMessage(), testInstance.identity.getClient()));
            }
        }

        // Now wait for messages to be received in the device client
        waitForBackToBackC2DMessagesToBeReceived(messageIdListStoredOnReceive);
        testInstance.identity.getClient().closeNow(); //close the device client connection
        Assert.assertTrue(buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Received messages don't match up with sent messages", testInstance.identity.getClient()), messageIdListStoredOnReceive.containsAll(messageIdListStoredOnC2DSend)); // check if the received list is same as the actual list that was created on sending the messages
        messageIdListStoredOnReceive.clear();
    }
}
