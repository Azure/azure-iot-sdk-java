/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.errorinjection;

import com.microsoft.azure.sdk.iot.common.helpers.ErrorInjectionHelper;
import com.microsoft.azure.sdk.iot.common.helpers.IotHubServicesCommon;
import com.microsoft.azure.sdk.iot.common.helpers.MessageAndResult;
import com.microsoft.azure.sdk.iot.common.setup.DeviceTwinCommon;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;

/**
 * Test class containing all error injection tests to be run on JVM and android pertaining to GetDeviceTwin/GetTwin. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class GetTwinErrInjTests extends DeviceTwinCommon
{
    public GetTwinErrInjTests(String deviceId, String moduleId, IotHubClientProtocol protocol, AuthenticationType authenticationType, String clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceId, moduleId, protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);

        System.out.print(clientType + " GetTwinErrInjTests UUID: " + (moduleId != null && !moduleId.isEmpty() ? moduleId : deviceId));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromTcpConnectionDrop() throws Exception
    {
        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromAmqpsConnectionDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromAmqpsSessionDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromAmqpsCBSReqLinkDrop() throws Exception
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

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromAmqpsCBSRespLinkDrop() throws Exception
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

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromAmqpsD2CLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromAmqpsC2DLinkDrop() throws Exception
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


        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromAmqpsTwinReqLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Twin Req link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsTwinReqLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromAmqpsTwinRespLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Twin Resp link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsTwinRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromAmqpsMethodReqLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Method Req link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsMethodReqLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromAmqpsMethodRespLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Method Resp link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromGracefulShutdownAmqp() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsGracefulShutdownErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void getDeviceTwinRecoveredFromGracefulShutdownMqtt() throws Exception
    {
        if (!(testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS))
        {
            return;
        }

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.mqttGracefulShutdownErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    public void errorInjectionGetDeviceTwinFlow(Message errorInjectionMessage) throws Exception
    {
        // Arrange
        final List<IotHubConnectionStatus> actualStatusUpdates = new ArrayList<>();
        setConnectionStatusCallBack(actualStatusUpdates);
        testGetDeviceTwin();

        // Act
        errorInjectionMessage.setExpiryTime(100);
        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(errorInjectionMessage, null);
        IotHubServicesCommon.sendMessageAndWaitForResponse(internalClient,
                errorInjectionMsgAndRet,
                RETRY_MILLISECONDS,
                SEND_TIMEOUT_MILLISECONDS,
                this.testInstance.protocol);

        // Assert
        IotHubServicesCommon.waitForStabilizedConnection(actualStatusUpdates, ERROR_INJECTION_WAIT_TIMEOUT, internalClient);
        for (PropertyState propertyState : deviceUnderTest.dCDeviceForTwin.propertyStateList)
        {
            propertyState.callBackTriggered = false;
            propertyState.propertyNewVersion = -1;
        }

        if (internalClient instanceof DeviceClient)
        {
            ((DeviceClient)internalClient).getDeviceTwin();
        }
        else
        {
            ((ModuleClient)internalClient).getTwin();
        }

        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(PROPERTY_VALUE_UPDATE, true);
    }
}
