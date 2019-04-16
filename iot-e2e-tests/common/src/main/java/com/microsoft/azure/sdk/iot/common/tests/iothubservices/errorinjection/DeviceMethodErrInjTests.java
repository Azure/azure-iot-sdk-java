/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.errorinjection;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.common.setup.DeviceMethodCommon;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;

/**
 * Test class containing all error injection tests to be run on JVM and android pertaining to Device methods. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class DeviceMethodErrInjTests extends DeviceMethodCommon
{
    public DeviceMethodErrInjTests(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, BaseDevice identity, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceTestManager, protocol, authenticationType, clientType, identity, publicKeyCert, privateKey, x509Thumbprint);

        System.out.println(clientType + " DeviceMethodErrInjTests UUID: " + (identity instanceof Module ? ((Module) identity).getId() : identity.getDeviceId()));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodRecoveredFromTcpConnectionDrop() throws Exception
    {
        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodRecoveredFromAmqpsConnectionDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodRecoveredFromAmqpsSessionDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
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

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
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

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodRecoveredFromAmqpsD2CLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
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

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
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

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
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

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodRecoveredFromGracefulShutdownAmqp() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsGracefulShutdownErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodRecoveredFromGracefulShutdownMqtt() throws Exception
    {
        if (!(testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS))
        {
            return;
        }

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
        errorInjectionMessage.setExpiryTime(200);
        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(errorInjectionMessage, null);
        this.testInstance.deviceTestManager.sendMessageAndWaitForResponse(
                errorInjectionMsgAndRet,
                RETRY_MILLISECONDS,
                SEND_TIMEOUT_MILLISECONDS,
                this.testInstance.protocol);

        // Assert
        IotHubServicesCommon.waitForStabilizedConnection(actualStatusUpdates, ERROR_INJECTION_WAIT_TIMEOUT, this.testInstance.deviceTestManager.client);
        invokeMethodSucceed();
    }
}
