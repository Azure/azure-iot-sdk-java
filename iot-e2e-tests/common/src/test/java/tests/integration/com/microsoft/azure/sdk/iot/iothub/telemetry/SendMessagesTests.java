/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry;


import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SSLContextBuilder;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Success;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.SendMessagesCommon;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static junit.framework.TestCase.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to sending messages from a device/module.
 */
@IotHubTest
@Slf4j
@RunWith(Parameterized.class)
public class SendMessagesTests extends SendMessagesCommon
{
    public SendMessagesTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        super(protocol, authenticationType, clientType);
    }

    @Test
    public void sendMessages() throws Exception
    {
        this.testInstance.setup();
        testInstance.identity.getClient().open(true);
        testInstance.identity.getClient().sendEvent(new Message("some message"));
        testInstance.identity.getClient().close();
    }

    // Ensure that a user can use a device/module client as soon as the connection status callback executes with status CONNECTED
    // even from the callback itself
    @Test
    public void sendMessagesFromConnectionStatusChangeCallback() throws Exception
    {
        Success messageSent = new Success();
        messageSent.setResult(false);
        testInstance.setup();
        testInstance.identity.getClient().setConnectionStatusChangeCallback((context) ->
        {
            if (context.getNewStatus() == IotHubConnectionStatus.CONNECTED)
            {
                try
                {
                    testInstance.identity.getClient().sendEventAsync(
                        new Message("test message"),
                        (responseStatus, exception, callbackContext1) ->
                        {
                            ((Success) callbackContext1).setResult(true);
                            ((Success) callbackContext1).setCallbackStatusCode(exception == null ? IotHubStatusCode.OK : exception.getStatusCode());
                            ((Success) callbackContext1).callbackWasFired();
                        },
                        messageSent);
                }
                catch (Exception e)
                {
                    log.error("Encountered an error when sending the message", e);
                    messageSent.setResult(false);
                    messageSent.setCallbackStatusCode(IotHubStatusCode.ERROR);
                    messageSent.callbackWasFired();
                }
            }
        }, null);

        testInstance.identity.getClient().open(false);

        long startTime = System.currentTimeMillis();
        while (!messageSent.wasCallbackFired())
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > SEND_TIMEOUT_MILLISECONDS)
            {
                fail("Timed out waiting for sent message to be acknowledged");
            }
        }

        assertTrue(messageSent.getResult());
        testInstance.identity.getClient().close();
    }

    @Test
    public void sendMessagesWithCustomSasTokenProvider() throws Exception
    {
        assumeTrue(testInstance.authenticationType == SAS);

        this.testInstance.setup(true);

        testInstance.identity.getClient().open(true);
        testInstance.identity.getClient().sendEvent(new Message("Custom sas token provider client message"));
        testInstance.identity.getClient().close();
    }

    @Test
    @ContinuousIntegrationTest
    public void sendManySmallMessagesAsBatch() throws Exception
    {
        // Only HTTP supports batching messages
        assumeTrue(this.testInstance.protocol == HTTPS);

        this.testInstance.setup();

        int count = 5;
        List<Message> messages = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
        {
            messages.add(new Message("Bulk message " + i));
        }

        testInstance.identity.getClient().open(true);
        testInstance.identity.getClient().sendEvents(messages);
        testInstance.identity.getClient().close();
    }

    @Test
    @ContinuousIntegrationTest
    public void sendLargestMessages() throws Exception
    {
        testInstance.setup();
        testInstance.identity.getClient().open(true);
        testInstance.identity.getClient().sendEvent(new Message(new byte[MAX_MESSAGE_PAYLOAD_SIZE]));
        testInstance.identity.getClient().close();
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesWithUnusualApplicationProperties() throws Exception
    {
        this.testInstance.setup();
        this.testInstance.identity.getClient().open(false);
        Message msg = new Message("asdf");

        //All of these characters should be allowed within application properties
        msg.setProperty("TestKey1234!#$%&'*+-^_`|~", "TestValue1234!#$%&'*+-^_`|~()<>@,;:\\\"[]?={} \t");
        this.testInstance.identity.getClient().sendEvent(msg);
        this.testInstance.identity.getClient().close();
    }

    @Test
    @ContinuousIntegrationTest
    public void expiredMessagesAreNotSent() throws Exception
    {
        this.testInstance.setup();

        this.testInstance.identity.getClient().open(true);

        try
        {
            Message expiredMessage = new Message("some pre-expired message");
            expiredMessage.setAbsoluteExpiryTime(1);
            this.testInstance.identity.getClient().sendEvent(expiredMessage);
            fail("Expected a MESSAGE_EXPIRED error but did not trigger one");
        }
        catch (IotHubClientException e)
        {
            assertEquals(IotHubStatusCode.MESSAGE_EXPIRED, e.getStatusCode());
        }
    }

    @Test
    public void sendMessagesWithCustomSSLContextAndSasAuth() throws Exception
    {
        assumeTrue(testInstance.authenticationType == SAS);

        this.testInstance.setup(SSLContextBuilder.buildSSLContext());

        testInstance.identity.getClient().open(true);
        testInstance.identity.getClient().sendEvent(new Message("some message"));
        testInstance.identity.getClient().close();
    }

    @Test
    @ContinuousIntegrationTest
    public void sendTooLargeMessage() throws Exception
    {
        // The service responds to a excessively large message by killing the TCP connection when connecting over MQTT
        // and doesn't have a mechanism for providing an error code, so this scenario can't be tested over MQTT
        assumeFalse(testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS);

        this.testInstance.setup();

        this.testInstance.identity.getClient().open(true);

        try
        {
            this.testInstance.identity.getClient().sendEvent(new Message(new byte[MAX_MESSAGE_PAYLOAD_SIZE + 10000]));
            fail("Expected client to throw an exception for sending a message that was too large");
        }
        catch (IotHubClientException e)
        {
            assertEquals(IotHubStatusCode.REQUEST_ENTITY_TOO_LARGE, e.getStatusCode());
        }
    }
}
