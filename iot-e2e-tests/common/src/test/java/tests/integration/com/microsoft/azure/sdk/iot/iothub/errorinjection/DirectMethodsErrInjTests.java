/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.errorinjection;


import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.twin.Pair;
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
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ErrInjTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.DirectMethodsCommon;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assume.assumeTrue;

/**
 * Test class containing all error injection tests to be run on JVM and android pertaining to Device methods.
 */
@ErrInjTest
@IotHubTest
@RunWith(Parameterized.class)
public class DirectMethodsErrInjTests extends DirectMethodsCommon
{
    public DirectMethodsErrInjTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        super(protocol, authenticationType, clientType);
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodRecoveredFromTcpConnectionDrop() throws Exception
    {
        super.openDeviceClientAndSubscribeToMethods();
        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsConnectionDrop() throws Exception
    {
        assumeTrue(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS);

        super.openDeviceClientAndSubscribeToMethods();
        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsSessionDrop() throws Exception
    {
        assumeTrue(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS);

        super.openDeviceClientAndSubscribeToMethods();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsCBSReqLinkDrop() throws Exception
    {
        assumeTrue(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS);

        //CBS links are only established when using sas authentication
        assumeTrue(testInstance.authenticationType == SAS);

        super.openDeviceClientAndSubscribeToMethods();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsCBSRespLinkDrop() throws Exception
    {
        assumeTrue(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS);

        //CBS links are only established when using sas authentication
        assumeTrue(testInstance.authenticationType == SAS);

        super.openDeviceClientAndSubscribeToMethods();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsD2CLinkDrop() throws Exception
    {
        assumeTrue(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS);

        super.openDeviceClientAndSubscribeToMethods();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsC2DLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //TODO error injection seems to fail when using x509 auth here. C2D link is never dropped even if waiting a long time
        // Need to talk to service folks about this strange behavior
        assumeTrue(testInstance.authenticationType == SAS);

        super.openDeviceClientAndSubscribeToMethods();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsMethodReqLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //TODO error injection seems to fail when using x509 auth here. C2D link is never dropped even if waiting a long time
        // Need to talk to service folks about this strange behavior
        assumeTrue(testInstance.authenticationType == SAS);

        super.openDeviceClientAndSubscribeToMethods();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodRecoveredFromAmqpsMethodRespLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //TODO error injection seems to fail when using x509 auth here. C2D link is never dropped even if waiting a long time
        // Need to talk to service folks about this strange behavior
        assumeTrue(testInstance.authenticationType == SAS);

        super.openDeviceClientAndSubscribeToMethods();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodRecoveredFromGracefulShutdownAmqp() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        super.openDeviceClientAndSubscribeToMethods();
        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsGracefulShutdownErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodRecoveredFromGracefulShutdownMqtt() throws Exception
    {
        assumeTrue(this.testInstance.protocol == MQTT || this.testInstance.protocol == MQTT_WS);

        super.openDeviceClientAndSubscribeToMethods();
        this.errorInjectionTestFlow(ErrorInjectionHelper.mqttGracefulShutdownErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    public void errorInjectionTestFlow(Message errorInjectionMessage) throws Exception
    {
        // Arrange
        List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates = new ArrayList<>();
        IotHubConnectionStatusChangeCallback connectionStatusUpdateCallback = (context) -> actualStatusUpdates.add(new Pair<>(context.getNewStatus(), context.getCause()));
        this.testInstance.identity.getClient().setConnectionStatusChangeCallback(connectionStatusUpdateCallback, null);
        invokeMethodSucceed();

        // Act
        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(errorInjectionMessage, IotHubStatusCode.OK);
        IotHubServicesCommon.sendErrorInjectionMessageAndWaitForResponse(this.testInstance.identity.getClient(), errorInjectionMsgAndRet, this.testInstance.protocol);

        // Assert
        IotHubServicesCommon.waitForStabilizedConnection(actualStatusUpdates, this.testInstance.identity.getClient());
        invokeMethodSucceed();
    }
}
