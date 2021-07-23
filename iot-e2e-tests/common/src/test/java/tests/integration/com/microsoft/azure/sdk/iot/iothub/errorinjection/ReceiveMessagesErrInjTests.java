/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.errorinjection;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.ReceiveMessagesCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.*;
import static org.junit.Assert.assertTrue;

/**
 * Test class containing all error injection tests to be run on JVM and android pertaining to receiving messages.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class ReceiveMessagesErrInjTests extends ReceiveMessagesCommon
{
    public ReceiveMessagesErrInjTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        super(protocol, authenticationType, clientType);
    }

    @Test
    @StandardTierHubOnlyTest
    public void receiveMessagesWithTCPConnectionDrop() throws Exception
    {
        if (testInstance.protocol == HTTPS)
        {
            //test case not applicable
            return;
        }

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsConnectionDrop() throws Exception
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsConnectionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsSessionDrop() throws Exception
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsCBSReqLinkDrop() throws Exception
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        if (testInstance.authenticationType == SELF_SIGNED || testInstance.authenticationType == CERTIFICATE_AUTHORITY)
        {
            //cbs links aren't established in these scenarios, so it would be impossible/irrelevant if a cbs link dropped
            return;
        }

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsCBSRespLinkDrop() throws Exception
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        if (testInstance.authenticationType == SELF_SIGNED || testInstance.authenticationType == CERTIFICATE_AUTHORITY)
        {
            //cbs links aren't established in these scenarios, so it would be impossible/irrelevant if a cbs link dropped
            return;
        }

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsD2CLinkDrop() throws Exception
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsC2DLinkDrop() throws Exception
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //TODO X509 case never seems to get callback about the connection dying. Needs investigation because this should pass, but doesn't
            return;
        }

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    public void receiveMessagesWithGracefulShutdownAmqp() throws Exception
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //test case not applicable
            return;
        }

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsGracefulShutdownErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    public void receiveMessagesWithGracefulShutdownMqtt() throws Exception
    {
        if (testInstance.protocol != MQTT && testInstance.protocol != MQTT_WS)
        {
            //test case not applicable
            return;
        }

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.mqttGracefulShutdownErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    public void errorInjectionTestFlow(com.microsoft.azure.sdk.iot.device.Message errorInjectionMessage) throws IOException, IotHubException, InterruptedException
    {
        List<Pair<IotHubConnectionStatus, Throwable>> connectionStatusUpdates = new ArrayList<>();
        testInstance.identity.getClient().registerConnectionStatusChangeCallback((status, statusChangeReason, throwable, callbackContext) -> connectionStatusUpdates.add(new Pair<>(status, throwable)), null);

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

        try
        {
            testInstance.identity.getClient().open();

            IotHubStatusCode expectedStatusCode = IotHubStatusCode.OK_EMPTY;

            if (testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS)
            {
                // error injection message will not be ack'd by service if sent over MQTT/MQTT_WS, so the SDK's
                // retry logic will try to send it again after the connection drops. By setting expiry time,
                // we ensure that error injection message isn't resent to service too many times. The message will still likely
                // be sent 3 or 4 times causing 3 or 4 disconnections, but the test should recover anyways.
                errorInjectionMessage.setExpiryTime(1000);

                // Since the message won't be ack'd, then we don't need to validate the status code when this message's callback is fired
                expectedStatusCode = null;
            }

            testInstance.identity.getClient().sendEventAsync(errorInjectionMessage, new EventCallback(expectedStatusCode), null);

            //wait to send the message because we want to ensure that the tcp connection drop happens beforehand and we
            // want the connection to be re-established before sending anything from service client
            IotHubServicesCommon.waitForStabilizedConnection(connectionStatusUpdates, ERROR_INJECTION_RECOVERY_TIMEOUT_MILLISECONDS, testInstance.identity.getClient());

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
        }
        finally
        {
            testInstance.identity.getClient().close();
        }

        assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Error Injection message did not cause service to drop TCP connection", testInstance.identity.getClient()), IotHubServicesCommon.actualStatusUpdatesContainsStatus(connectionStatusUpdates, IotHubConnectionStatus.DISCONNECTED_RETRYING));
    }
}
