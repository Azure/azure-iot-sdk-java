/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.errorinjection;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.twin.Pair;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ErrInjTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.ReceiveMessagesCommon;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Test class containing all error injection tests to be run on JVM and android pertaining to receiving messages.
 */
@ErrInjTest
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
        // This error injection test only applies for no connection can be dropped for HTTP since it is a stateless connection
        assumeTrue(this.testInstance.protocol != HTTPS);

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsConnectionDrop() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsConnectionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsSessionDrop() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsCBSReqLinkDrop() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);
        assumeTrue(this.testInstance.authenticationType == SAS);

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsCBSRespLinkDrop() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);
        assumeTrue(this.testInstance.authenticationType == SAS);

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsD2CLinkDrop() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void receiveMessagesWithAmqpsC2DLinkDrop() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //TODO error injection seems to fail when using x509 auth here. C2D link is never dropped even if waiting a long time
        // Need to talk to service folks about this strange behavior
        assumeTrue(testInstance.authenticationType == SAS);

        super.setupTest();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    public void errorInjectionTestFlow(com.microsoft.azure.sdk.iot.device.Message faultInjectionMessage) throws Exception
    {
        List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates = new ArrayList<>();
        IotHubConnectionStatusChangeCallback connectionStatusUpdateCallback = (context) -> actualStatusUpdates.add(new Pair<>(context.getNewStatus(), context.getCause()));
        testInstance.identity.getClient().setConnectionStatusChangeCallback(connectionStatusUpdateCallback, null);

        testInstance.identity.getClient().open(true);

        // Test that the device/module can receive c2d messages
        receiveMessage(MESSAGE_SIZE_IN_BYTES, false);

        // Inject the error
        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(faultInjectionMessage, IotHubStatusCode.OK);
        IotHubServicesCommon.sendErrorInjectionMessageAndWaitForResponse(testInstance.identity.getClient(), errorInjectionMsgAndRet, testInstance.protocol);

        IotHubServicesCommon.waitForStabilizedConnection(actualStatusUpdates, testInstance.identity.getClient());

        // Test that the device/module can still receive c2d messages
        receiveMessage(MESSAGE_SIZE_IN_BYTES, false);

        testInstance.identity.getClient().close();

        assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessage(testInstance.protocol + ", " + testInstance.authenticationType + ": Error Injection message did not cause service to drop TCP connection", testInstance.identity.getClient()), IotHubServicesCommon.actualStatusUpdatesContainsStatus(actualStatusUpdates, IotHubConnectionStatus.DISCONNECTED_RETRYING));
    }
}
