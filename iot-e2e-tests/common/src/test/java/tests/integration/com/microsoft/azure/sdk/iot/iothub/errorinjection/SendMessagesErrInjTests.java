/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.errorinjection;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.transport.ExponentialBackoffWithJitter;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.transport.NoRetry;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.SendMessagesCommon;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;

/**
 * Test class containing all error injection tests to be run on JVM and android pertaining to sending messages to the cloud.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class SendMessagesErrInjTests extends SendMessagesCommon
{
    public SendMessagesErrInjTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, boolean withProxy) throws Exception
    {
        super(protocol, authenticationType, clientType, withProxy);
    }

    @Test
    public void sendMessagesWithTcpConnectionDrop() throws Exception
    {
        if (testInstance.protocol == HTTPS || (testInstance.protocol == MQTT_WS && testInstance.authenticationType != SAS) || testInstance.useHttpProxy)
        {
            //TCP connection is not maintained between device and service when using HTTPS, so this test case isn't applicable
            //MQTT_WS + x509 is not supported for sending messages
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, TCP_CONNECTION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithConnectionDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)) || testInstance.useHttpProxy)
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_CONNECTION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithSessionDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)) || testInstance.useHttpProxy)
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_SESSION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithCbsRequestLinkDrop() throws Exception
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS || testInstance.useHttpProxy)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithCbsResponseLinkDrop() throws Exception
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS || testInstance.useHttpProxy)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithD2CLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)) || testInstance.useHttpProxy)
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpWithC2DLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)) || testInstance.useHttpProxy)
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. C2D link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Ignore
    @Test
    public void sendMessagesWithThrottling() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS) || testInstance.useHttpProxy)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        this.testInstance.setup();

        errorInjectionTestFlowNoDisconnect(
                ErrorInjectionHelper.throttledConnectionErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec),
                IotHubStatusCode.OK_EMPTY,
                false);

    }

    @Ignore
    @Test
    @ContinuousIntegrationTest
    public void sendMessagesWithThrottlingNoRetry() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS) || testInstance.useHttpProxy)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

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
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS) || testInstance.useHttpProxy)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

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
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS) || testInstance.useHttpProxy)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        this.testInstance.setup();

        errorInjectionTestFlowNoDisconnect(
                ErrorInjectionHelper.quotaExceededErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec),
                IotHubStatusCode.ERROR,
                false);
    }

    @Test
    public void sendMessagesOverAmqpWithGracefulShutdown() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS) || testInstance.useHttpProxy)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, AMQP_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverMqttWithGracefulShutdown() throws Exception
    {
        if (!(testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS) || testInstance.useHttpProxy)
        {
            //This error injection test only applies for MQTT and MQTT_WS
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.identity.getClient(), testInstance.protocol, MQTT_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesWithTcpConnectionDropNotifiesUserIfRetryExpires() throws Exception
    {
        if (testInstance.protocol == HTTPS || (testInstance.protocol == MQTT_WS && testInstance.authenticationType != SAS) || testInstance.useHttpProxy)
        {
            //TCP connection is not maintained between device and service when using HTTPS, so this test case isn't applicable
            //MQTT_WS + x509 is not supported for sending messages
            return;
        }

        this.testInstance.setup();

        testInstance.identity.getClient().setRetryPolicy(new NoRetry());

        Message tcpConnectionDropErrorInjectionMessageUnrecoverable = ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(1, 100000);
        IotHubServicesCommon.sendMessagesExpectingUnrecoverableConnectionLossAndTimeout(testInstance.identity.getClient(), testInstance.protocol, tcpConnectionDropErrorInjectionMessageUnrecoverable, testInstance.authenticationType);

        //reset back to default
        testInstance.identity.getClient().setRetryPolicy(new ExponentialBackoffWithJitter());
    }

    private void errorInjectionTestFlowNoDisconnect(Message errorInjectionMessage, IotHubStatusCode expectedStatus, boolean noRetry) throws IOException, IotHubException, URISyntaxException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        // Arrange
        Device targetDevice = null;
        InternalClient client = null;

        if (testInstance.clientType == ClientType.DEVICE_CLIENT)
        {
            TestDeviceIdentity testDeviceIdentity = Tools.getTestDevice(iotHubConnectionString, testInstance.protocol, testInstance.authenticationType);
            targetDevice = testDeviceIdentity.getDevice();
            client = testDeviceIdentity.getDeviceClient();
        }
        else if (testInstance.clientType == ClientType.MODULE_CLIENT)
        {
            TestModuleIdentity testModuleIdentity = Tools.getTestModule(iotHubConnectionString, testInstance.protocol, testInstance.authenticationType);
            targetDevice = testModuleIdentity.getDevice();
            client = testModuleIdentity.getModuleClient();
        }

        if (noRetry)
        {
            client.setRetryPolicy(new NoRetry());
        }
        client.open();

        // Act
        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(errorInjectionMessage,IotHubStatusCode.OK_EMPTY);
        IotHubServicesCommon.sendMessageAndWaitForResponse(
                client,
                errorInjectionMsgAndRet,
                RETRY_MILLISECONDS,
                SEND_TIMEOUT_MILLISECONDS,
                this.testInstance.protocol);

        // time for the error injection to take effect on the service side
        Thread.sleep(2000);

        MessageAndResult normalMessageAndExpectedResult = new MessageAndResult(new Message("test message"), expectedStatus);
        IotHubServicesCommon.sendMessageAndWaitForResponse(
                client,
                normalMessageAndExpectedResult,
                RETRY_MILLISECONDS,
                SEND_TIMEOUT_MILLISECONDS,
                this.testInstance.protocol);

        client.closeNow();

        //cleanup
        Tools.disposeTestIdentity(new TestDeviceIdentity(null, targetDevice), iotHubConnectionString);
    }
}
