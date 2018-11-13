/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.errorinjection;

import com.microsoft.azure.sdk.iot.common.helpers.DeviceConnectionString;
import com.microsoft.azure.sdk.iot.common.helpers.ErrorInjectionHelper;
import com.microsoft.azure.sdk.iot.common.helpers.IotHubServicesCommon;
import com.microsoft.azure.sdk.iot.common.helpers.MessageAndResult;
import com.microsoft.azure.sdk.iot.common.setup.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.ExponentialBackoffWithJitter;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.transport.NoRetry;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.common.helpers.IotHubServicesCommon.sendMessagesExpectingConnectionStatusChangeUpdate;
import static com.microsoft.azure.sdk.iot.common.helpers.IotHubServicesCommon.sendMessagesExpectingUnrecoverableConnectionLossAndTimeout;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;

/**
 * Test class containing all error injection tests to be run on JVM and android pertaining to sending messages to the cloud. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class SendMessagesErrInjTests extends SendMessagesCommon
{
    public SendMessagesErrInjTests(InternalClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, String clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(client, protocol, identity, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    @Test
    public void sendMessagesWithTcpConnectionDrop() throws IOException, InterruptedException
    {
        if (testInstance.protocol == HTTPS || (testInstance.protocol == MQTT_WS && testInstance.authenticationType != SAS))
        {
            //TCP connection is not maintained between device and service when using HTTPS, so this test case isn't applicable
            //MQTT_WS + x509 is not supported for sending messages
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, TCP_CONNECTION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithConnectionDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, AMQP_CONNECTION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithSessionDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, AMQP_SESSION_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithCbsRequestLinkDrop() throws IOException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, AMQP_CBS_REQUEST_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithCbsResponseLinkDrop() throws IOException, InterruptedException
    {
        if (testInstance.protocol != AMQPS && testInstance.protocol != AMQPS_WS)
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, AMQP_CBS_RESPONSE_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithD2CLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, AMQP_D2C_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithC2DLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || (testInstance.protocol == AMQPS_WS && testInstance.authenticationType == SAS)))
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

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, AMQP_C2D_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithMethodReqLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Method link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, AMQP_METHOD_REQ_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithMethodRespLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
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

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, AMQP_METHOD_RESP_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithTwinReqLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Twin link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, AMQP_TWIN_REQ_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverAmqpWithTwinRespLinkDrop() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            //This error injection test only applies for AMQPS with SAS and X509 and for AMQPS_WS with SAS
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Twin is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, AMQP_TWIN_RESP_LINK_DROP_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesWithThrottling() throws URISyntaxException, IOException, IotHubException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        errorInjectionTestFlowNoDisconnect(
                ErrorInjectionHelper.throttledConnectionErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec),
                IotHubStatusCode.OK_EMPTY,
                false);

    }

    @Ignore
    @Test
    public void sendMessagesWithThrottlingNoRetry() throws URISyntaxException, IOException, IotHubException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        errorInjectionTestFlowNoDisconnect(
                ErrorInjectionHelper.throttledConnectionErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec),
                IotHubStatusCode.THROTTLED,
                true);

    }

    @Test
    public void sendMessagesWithAuthenticationError() throws URISyntaxException, IOException, IotHubException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        errorInjectionTestFlowNoDisconnect(
                ErrorInjectionHelper.authErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec),
                IotHubStatusCode.ERROR,
                false);
    }

    @Test
    public void sendMessagesWithQuotaExceeded() throws URISyntaxException, IOException, IotHubException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        errorInjectionTestFlowNoDisconnect(
                ErrorInjectionHelper.quotaExceededErrorInjectionMessage(ErrorInjectionHelper.DefaultDelayInSec, ErrorInjectionHelper.DefaultDurationInSec),
                IotHubStatusCode.ERROR,
                false);
    }

    @Test
    public void sendMessagesOverAmqpWithGracefulShutdown() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            //This error injection test only applies for AMQPS and AMQPS_WS
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, AMQP_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesOverMqttWithGracefulShutdown() throws IOException, InterruptedException
    {
        if (!(testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS))
        {
            //This error injection test only applies for MQTT and MQTT_WS
            return;
        }

        sendMessagesExpectingConnectionStatusChangeUpdate(testInstance.client, testInstance.protocol, MQTT_GRACEFUL_SHUTDOWN_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, IotHubConnectionStatus.DISCONNECTED_RETRYING, 100, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesWithTcpConnectionDropNotifiesUserIfRetryExpires() throws IOException, InterruptedException
    {
        if (testInstance.protocol == HTTPS || (testInstance.protocol == MQTT_WS && testInstance.authenticationType != SAS))
        {
            //TCP connection is not maintained between device and service when using HTTPS, so this test case isn't applicable
            //MQTT_WS + x509 is not supported for sending messages
            return;
        }

        testInstance.client.setRetryPolicy(new NoRetry());

        Message tcpConnectionDropErrorInjectionMessageUnrecoverable = ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(1, 100000);
        sendMessagesExpectingUnrecoverableConnectionLossAndTimeout(testInstance.client, testInstance.protocol, tcpConnectionDropErrorInjectionMessageUnrecoverable, testInstance.authenticationType);

        //reset back to default
        testInstance.client.setRetryPolicy(new ExponentialBackoffWithJitter());
    }

    private void errorInjectionTestFlowNoDisconnect(Message errorInjectionMessage, IotHubStatusCode expectedStatus, boolean noRetry) throws IOException, IotHubException, URISyntaxException, InterruptedException
    {
        // Arrange
        // This test case creates a device instead of re-using the one in this.testInstance due to state changes
        // introduced by injected errors
        String uuid = UUID.randomUUID().toString();
        String deviceId = "java-device-client-e2e-test-send-messages".concat("-" + uuid);

        Device target;
        DeviceClient dc;
        if (this.testInstance.authenticationType == SELF_SIGNED)
        {
            target = Device.createDevice(deviceId, SELF_SIGNED);
            target.setThumbprint(testInstance.x509Thumbprint, testInstance.x509Thumbprint);
            registryManager.addDevice(target);
            dc = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, target), this.testInstance.protocol, testInstance.publicKeyCert, false, testInstance.privateKey, false);
        }
        else
        {
            target = Device.createFromId(deviceId, null, null);
            registryManager.addDevice(target);
            dc = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, target), this.testInstance.protocol);
        }

        if (noRetry)
        {
            dc.setRetryPolicy(new NoRetry());
        }
        IotHubServicesCommon.openClientWithRetry(dc);

        // Act
        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(errorInjectionMessage,null);
        IotHubServicesCommon.sendMessageAndWaitForResponse(
                dc,
                errorInjectionMsgAndRet,
                RETRY_MILLISECONDS,
                SEND_TIMEOUT_MILLISECONDS,
                this.testInstance.protocol);

        // time for the error injection to take effect on the service side
        Thread.sleep(2000);

        MessageAndResult normalMessageAndExpectedResult = new MessageAndResult(new Message("test message"), expectedStatus);
        IotHubServicesCommon.sendMessageAndWaitForResponse(
                dc,
                normalMessageAndExpectedResult,
                RETRY_MILLISECONDS,
                SEND_TIMEOUT_MILLISECONDS,
                this.testInstance.protocol);

        dc.closeNow();

        //cleanup
        registryManager.removeDevice(target.getDeviceId());
    }
}
