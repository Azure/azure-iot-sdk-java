/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.errorinjection;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.ExponentialBackoffWithJitter;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.transport.NoRetry;
import com.microsoft.azure.sdk.iot.device.twin.Pair;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ErrInjTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.SendMessagesCommon;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.IotHubServicesCommon.actualStatusUpdatesContainsStatus;

/**
 * Test class containing all error injection tests to be run on JVM and android pertaining to sending messages to the cloud.
 */
@ErrInjTest
@IotHubTest
@RunWith(Parameterized.class)
public class SendMessagesErrInjTests extends SendMessagesCommon
{
    public SendMessagesErrInjTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        super(protocol, authenticationType, clientType);
    }

    @Test
    public void sendMessagesWithTcpConnectionDrop() throws Exception
    {
        // This error injection test only applies for no connection can be dropped for HTTP since it is a stateless connection
        assumeTrue(this.testInstance.protocol != HTTPS);

        errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithConnectionDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        errorInjectionTestFlow(ErrorInjectionHelper.amqpsConnectionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithSessionDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        errorInjectionTestFlow(ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithCbsRequestLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //CBS links are only established when using sas authentication
        assumeTrue(testInstance.authenticationType == SAS);

        errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithCbsResponseLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //CBS links are only established when using sas authentication
        assumeTrue(testInstance.authenticationType == SAS);

        errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithD2CLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        errorInjectionTestFlow(ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithC2DLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //TODO error injection seems to fail when using x509 auth here. C2D link is never dropped even if waiting a long time
        // Need to talk to service folks about this strange behavior
        assumeTrue(testInstance.authenticationType == SAS);

        errorInjectionTestFlow(ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Ignore
    @Test
    public void sendMessagesWithThrottling() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        this.testInstance.setup();

        errorInjectionTestFlowNoDisconnect(
                ErrorInjectionHelper.throttledConnectionErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec),
                IotHubStatusCode.OK,
                false);
    }

    @Ignore
    @Test
    @ContinuousIntegrationTest
    public void sendMessagesWithThrottlingNoRetry() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        this.testInstance.setup();

        errorInjectionTestFlowNoDisconnect(
                ErrorInjectionHelper.throttledConnectionErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec),
                IotHubStatusCode.THROTTLED,
                true);
    }

    @Ignore
    @Test
    public void sendMessagesWithAuthenticationError() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        this.testInstance.setup();

        errorInjectionTestFlowNoDisconnect(
                ErrorInjectionHelper.authErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec),
                IotHubStatusCode.ERROR,
                false);
    }

    @Ignore
    @Test
    @ContinuousIntegrationTest
    public void sendMessagesWithQuotaExceeded() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        this.testInstance.setup();

        errorInjectionTestFlowNoDisconnect(
                ErrorInjectionHelper.quotaExceededErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec),
                IotHubStatusCode.ERROR,
                false);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesWithTcpConnectionDropNotifiesUserIfRetryExpires() throws Exception
    {
        // This error injection test only applies for no connection can be dropped for HTTP since it is a stateless connection
        assumeTrue(this.testInstance.protocol != HTTPS);

        this.testInstance.setup();

        testInstance.identity.getClient().setRetryPolicy(new NoRetry());

        Message errorInjectionMessage = ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(1, 100000);
        final List<Pair<IotHubConnectionStatus, Throwable>> statusUpdates = new ArrayList<>();
        testInstance.identity.getClient().setConnectionStatusChangeCallback((context) -> statusUpdates.add(new Pair<>(context.getNewStatus(), context.getCause())), new Object());

        testInstance.identity.getClient().open(false);

        testInstance.identity.getClient().sendEventAsync(errorInjectionMessage, new EventCallback(null), new Success());

        long startTime = System.currentTimeMillis();
        while (!(actualStatusUpdatesContainsStatus(statusUpdates, IotHubConnectionStatus.DISCONNECTED_RETRYING) && actualStatusUpdatesContainsStatus(statusUpdates, IotHubConnectionStatus.DISCONNECTED)))
        {
            Thread.sleep(500);

            if (System.currentTimeMillis() - startTime > 30 * 1000)
            {
                break;
            }
        }

        Assert.assertTrue(buildExceptionMessage("Expected notification about disconnected but retrying.", testInstance.identity.getClient()), actualStatusUpdatesContainsStatus(statusUpdates, IotHubConnectionStatus.DISCONNECTED_RETRYING));
        Assert.assertTrue(buildExceptionMessage("Expected notification about disconnected.", testInstance.identity.getClient()), actualStatusUpdatesContainsStatus(statusUpdates, IotHubConnectionStatus.DISCONNECTED));

        testInstance.identity.getClient().close();
    }

    private void errorInjectionTestFlow(Message faultInjectionMessage) throws Exception
    {
        testInstance.setup();

        List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates = new ArrayList<>();
        IotHubConnectionStatusChangeCallback connectionStatusUpdateCallback = (context) -> actualStatusUpdates.add(new Pair<>(context.getNewStatus(), context.getCause()));
        testInstance.identity.getClient().setConnectionStatusChangeCallback(connectionStatusUpdateCallback, null);

        testInstance.identity.getClient().open(true);

        testInstance.identity.getClient().sendEvent(new Message("some normal message"));

        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(faultInjectionMessage, IotHubStatusCode.OK);
        IotHubServicesCommon.sendErrorInjectionMessageAndWaitForResponse(testInstance.identity.getClient(), errorInjectionMsgAndRet, testInstance.protocol);

        IotHubServicesCommon.waitForStabilizedConnection(actualStatusUpdates, testInstance.identity.getClient());

        testInstance.identity.getClient().sendEvent(new Message("some normal message"));

        testInstance.identity.getClient().close();
    }

    private void errorInjectionTestFlowNoDisconnect(Message errorInjectionMessage, IotHubStatusCode expectedStatus, boolean noRetry) throws IOException, IotHubException, URISyntaxException, InterruptedException, GeneralSecurityException, IotHubClientException
    {
        // Arrange
        if (noRetry)
        {
            testInstance.identity.getClient().setRetryPolicy(new NoRetry());
        }

        testInstance.identity.getClient().open(false);

        // Act
        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(errorInjectionMessage,IotHubStatusCode.OK);
        IotHubServicesCommon.sendErrorInjectionMessageAndWaitForResponse(testInstance.identity.getClient(), errorInjectionMsgAndRet, testInstance.protocol);

        // time for the error injection to take effect on the service side
        Thread.sleep(2000);

        try
        {
            testInstance.identity.getClient().sendEvent(new Message("some message sent after the fault"));

            if (expectedStatus != IotHubStatusCode.OK)
            {
                fail("Expected " + expectedStatus + " but was OK");
            }
        }
        catch (IotHubClientException e)
        {
            assertEquals(expectedStatus, e.getStatusCode());
        }

        testInstance.identity.getClient().close();
    }
}
