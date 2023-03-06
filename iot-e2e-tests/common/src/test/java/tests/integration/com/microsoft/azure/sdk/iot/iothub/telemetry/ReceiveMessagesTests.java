/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry;


import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.ReceiveMessagesCommon;

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
        receiveMessage(MESSAGE_SIZE_IN_BYTES, true);
    }

    // Test out receiving a near-maximum sized cloud to device message both for testing the sending of it from the
    // service client, but also to test how MQTT/HTTPS/AMQPS handle it on the receiving side. AMQP in particular
    // has some "partial delivery" scenarios that are worth having an e2e test around.
    @Test
    @ContinuousIntegrationTest
    @StandardTierHubOnlyTest
    public void receiveLargeMessage() throws Exception
    {
        receiveMessage(LARGE_MESSAGE_SIZE_IN_BYTES, true);
    }
}
