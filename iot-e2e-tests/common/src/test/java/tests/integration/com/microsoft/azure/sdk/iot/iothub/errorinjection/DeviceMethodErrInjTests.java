/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.errorinjection;


import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.DeviceMethodCommon;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;

/**
 * Test class containing all error injection tests to be run on JVM and android pertaining to Device methods.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class DeviceMethodErrInjTests extends DeviceMethodCommon
{
    public DeviceMethodErrInjTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws Exception
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodRecoveredFromTcpConnectionDrop() throws Exception
    {
        super.cleanToStart();
        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsConnectionDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        super.cleanToStart();
        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsSessionDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        super.cleanToStart();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsCBSReqLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        super.cleanToStart();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsCBSRespLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        super.cleanToStart();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsD2CLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        super.cleanToStart();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsC2DLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. C2D link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        super.cleanToStart();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsMethodReqLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Method Req is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        super.cleanToStart();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsMethodRespLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Method Resp is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        super.cleanToStart();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodRecoveredFromGracefulShutdownAmqp() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        super.cleanToStart();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsGracefulShutdownErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodRecoveredFromGracefulShutdownMqtt() throws Exception
    {
        if (!(testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS))
        {
            return;
        }

        super.cleanToStart();
        this.errorInjectionTestFlow(ErrorInjectionHelper.mqttGracefulShutdownErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    public void errorInjectionTestFlow(Message errorInjectionMessage) throws Exception
    {
        // Arrange
        List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates = new ArrayList<>();
        setConnectionStatusCallBack(actualStatusUpdates);
        invokeMethodSucceed();

        // Act
        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(errorInjectionMessage, IotHubStatusCode.OK_EMPTY);
        this.testInstance.deviceTestManager.sendErrorInjectionMessageAndWaitForResponse(
                errorInjectionMsgAndRet,
                RETRY_MILLISECONDS,
                SEND_TIMEOUT_MILLISECONDS,
                this.testInstance.protocol);

        // Assert
        IotHubServicesCommon.waitForStabilizedConnection(actualStatusUpdates, ERROR_INJECTION_WAIT_TIMEOUT_MILLISECONDS, this.testInstance.deviceTestManager.client);
        invokeMethodSucceed();
    }
}
