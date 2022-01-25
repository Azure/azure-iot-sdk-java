/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry;


import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
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

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;

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
        testInstance.identity.getClient().open();

        Message serviceMessage = createCloudToDeviceMessage(messageSize);

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
            testInstance.serviceClient.send(testInstance.identity.getDeviceId(), serviceMessage);
        }
        else if (testInstance.identity.getClient() instanceof ModuleClient)
        {
            testInstance.serviceClient.send(testInstance.identity.getDeviceId(), ((TestModuleIdentity) testInstance.identity).getModuleId(), serviceMessage);
        }

        waitForMessageToBeReceived(messageReceived, testInstance.protocol.toString());

        Thread.sleep(200);
        testInstance.identity.getClient().close();
    }
}
