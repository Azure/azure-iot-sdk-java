/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry;


import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
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
        testInstance.identity.getClient().close();
    }
}
