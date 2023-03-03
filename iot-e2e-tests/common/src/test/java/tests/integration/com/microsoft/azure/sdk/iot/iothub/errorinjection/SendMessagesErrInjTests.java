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
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
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

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assume.assumeTrue;

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

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, TCP_CONNECTION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithConnectionDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_CONNECTION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithSessionDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_SESSION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithCbsRequestLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //CBS links are only established when using sas authentication
        assumeTrue(testInstance.authenticationType == SAS);

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithCbsResponseLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //CBS links are only established when using sas authentication
        assumeTrue(testInstance.authenticationType == SAS);

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithD2CLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
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

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
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
    public void sendMessagesOverAmqpWithGracefulShutdown() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverMqttWithGracefulShutdown() throws Exception
    {
        // This error injection test only applies for MQTT and MQTT_WS
        assumeTrue(this.testInstance.protocol == MQTT || this.testInstance.protocol == MQTT_WS);

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, MQTT_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesWithTcpConnectionDropNotifiesUserIfRetryExpires() throws Exception
    {
        // This error injection test only applies for no connection can be dropped for HTTP since it is a stateless connection
        assumeTrue(this.testInstance.protocol != HTTPS);

        this.testInstance.setup();

        testInstance.identity.getClient().setRetryPolicy(new NoRetry());

        Message tcpConnectionDropErrorInjectionMessageUnrecoverable = ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(1, 100000);
        IotHubServicesCommon.sendMessagesExpectingUnrecoverableConnectionLossAndTimeout(testInstance.identity.getClient(), testInstance.protocol, tcpConnectionDropErrorInjectionMessageUnrecoverable, testInstance.authenticationType);

        //reset back to default
        testInstance.identity.getClient().setRetryPolicy(new ExponentialBackoffWithJitter());
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
        IotHubServicesCommon.sendMessageAndWaitForResponse(
                testInstance.identity.getClient(),
                errorInjectionMsgAndRet,
                this.testInstance.protocol);

        // time for the error injection to take effect on the service side
        Thread.sleep(2000);

        MessageAndResult normalMessageAndExpectedResult = new MessageAndResult(new Message("test message"), expectedStatus);
        IotHubServicesCommon.sendMessageAndWaitForResponse(
                testInstance.identity.getClient(),
                normalMessageAndExpectedResult,
                this.testInstance.protocol);

        testInstance.identity.getClient().close();
    }
}
