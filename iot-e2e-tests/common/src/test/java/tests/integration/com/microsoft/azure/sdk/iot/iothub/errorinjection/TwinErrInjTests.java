/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.errorinjection;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.twin.Pair;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ErrInjTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.TwinCommon;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assume.assumeTrue;

/**
 * Test class containing all error injection tests to be run on JVM and android pertaining to GetDeviceTwin/GetTwin.
 */
@ErrInjTest
@IotHubTest
@RunWith(Parameterized.class)
public class TwinErrInjTests extends TwinCommon
{
    public TwinErrInjTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException, InterruptedException, IotHubException, URISyntaxException, GeneralSecurityException
    {
        super(protocol, authenticationType, clientType);
    }

    @Test
    @StandardTierHubOnlyTest
    public void getDeviceTwinRecoveredFromTcpConnectionDrop() throws Exception
    {
        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void getDeviceTwinRecoveredFromAmqpsConnectionDrop() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void getDeviceTwinRecoveredFromAmqpsSessionDrop() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void getDeviceTwinRecoveredFromAmqpsCBSReqLinkDrop() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //CBS links are only established when using sas authentication
        assumeTrue(this.testInstance.authenticationType == SAS);

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void getDeviceTwinRecoveredFromAmqpsCBSRespLinkDrop() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //CBS links are only established when using sas authentication
        assumeTrue(this.testInstance.authenticationType == SAS);

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void getDeviceTwinRecoveredFromAmqpsD2CLinkDrop() throws Exception
    {
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void getDeviceTwinRecoveredFromAmqpsC2DLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //TODO error injection seems to fail when using x509 auth here. C2D link is never dropped even if waiting a long time
        // Need to talk to service folks about this strange behavior
        assumeTrue(testInstance.authenticationType == SAS);

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void getDeviceTwinRecoveredFromAmqpsTwinReqLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //TODO error injection seems to fail when using x509 auth here. C2D link is never dropped even if waiting a long time
        // Need to talk to service folks about this strange behavior
        assumeTrue(testInstance.authenticationType == SAS);

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsTwinReqLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void getDeviceTwinRecoveredFromAmqpsTwinRespLinkDrop() throws Exception
    {
        // This error injection test only applies for AMQPS and AMQPS_WS
        assumeTrue(this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS);

        //TODO error injection seems to fail when using x509 auth here. C2D link is never dropped even if waiting a long time
        // Need to talk to service folks about this strange behavior
        assumeTrue(testInstance.authenticationType == SAS);

        this.errorInjectionGetDeviceTwinFlow(ErrorInjectionHelper.amqpsTwinRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    public void errorInjectionGetDeviceTwinFlow(Message errorInjectionMessage) throws Exception
    {
        // Arrange
        this.testInstance.setup();

        List<com.microsoft.azure.sdk.iot.device.twin.Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates = new ArrayList<>();

        IotHubConnectionStatusChangeCallback connectionStatusUpdateCallback = (context) -> actualStatusUpdates.add(new Pair<>(context.getNewStatus(), context.getCause()));

        this.testInstance.testIdentity.getClient().setConnectionStatusChangeCallback(connectionStatusUpdateCallback, null);

        super.testBasicTwinFlow(true);

        // Act
        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(errorInjectionMessage, IotHubStatusCode.OK);
        IotHubServicesCommon.sendErrorInjectionMessageAndWaitForResponse(testInstance.testIdentity.getClient(), errorInjectionMsgAndRet, this.testInstance.protocol);

        // Assert
        IotHubServicesCommon.waitForStabilizedConnection(actualStatusUpdates, testInstance.testIdentity.getClient());

        // user isn't expected to call client.subscribeToDesiredProperties again since the connection was only temporarily lost
        super.testBasicTwinFlow(false);
    }
}
