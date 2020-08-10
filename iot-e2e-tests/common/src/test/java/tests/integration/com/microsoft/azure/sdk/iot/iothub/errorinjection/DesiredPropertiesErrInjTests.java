/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.errorinjection;

import com.google.gson.JsonParser;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.DeviceTwinCommon;

import java.io.IOException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS_WS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;

/**
 * Test class containing all error injection tests to be run on JVM and android pertaining to DesiredProperties.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class DesiredPropertiesErrInjTests extends DeviceTwinCommon
{
    private JsonParser jsonParser = new JsonParser();

    public DesiredPropertiesErrInjTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws IOException
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
        jsonParser = new JsonParser();
    }

    @Test
    @StandardTierHubOnlyTest
    public void subscribeToDesiredPropertiesRecoveredFromTcpConnectionDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromTcpConnectionDropFlow(
            PROPERTY_VALUE,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE2,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE
        );
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredArrayPropertiesRecoveredFromTcpConnectionDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromTcpConnectionDropFlow(
            jsonParser.parse(PROPERTY_VALUE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE2_ARRAY),
            PROPERTY_VALUE_UPDATE_ARRAY_PREFIX,
            PROPERTY_VALUE_UPDATE2_ARRAY_PREFIX
        );
    }

    public void subscribeToDesiredPropertiesRecoveredFromTcpConnectionDropFlow(
        Object propertyValue,
        Object updatePropertyValue,
        Object updatePropertyValue2,
        String update1Prefix,
        String update2Prefix) throws Exception
    {
        super.setUpNewDeviceAndModule();
        this.errorInjectionSubscribeToDesiredPropertiesFlow(
            ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec),
            propertyValue,
            updatePropertyValue,
            updatePropertyValue2,
            update1Prefix,
            update2Prefix);
    }

    @Test
    @StandardTierHubOnlyTest
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsConnectionDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsConnectionDropFlow(
            PROPERTY_VALUE,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE2,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE);
    }

    @Test
    @StandardTierHubOnlyTest
    public void subscribeToDesiredArrayPropertiesRecoveredFromAmqpsConnectionDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsConnectionDropFlow(
            jsonParser.parse(PROPERTY_VALUE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE2_ARRAY),
            PROPERTY_VALUE_UPDATE_ARRAY_PREFIX,
            PROPERTY_VALUE_UPDATE2_ARRAY_PREFIX);
    }

    public void subscribeToDesiredPropertiesRecoveredFromAmqpsConnectionDropFlow(
        Object propertyValue,
        Object updatePropertyValue,
        Object updatePropertyValue2,
        String update1Prefix,
        String update2Prefix) throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        super.setUpNewDeviceAndModule();

        this.errorInjectionSubscribeToDesiredPropertiesFlow(
            ErrorInjectionHelper.amqpsConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec),
            propertyValue,
            updatePropertyValue,
            updatePropertyValue2,
            update1Prefix,
            update2Prefix);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsSessionDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsSessionDropFlow(
            PROPERTY_VALUE,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE2,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredArrayPropertiesRecoveredFromAmqpsSessionDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsSessionDropFlow(
            jsonParser.parse(PROPERTY_VALUE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE2_ARRAY),
            PROPERTY_VALUE_UPDATE_ARRAY_PREFIX,
            PROPERTY_VALUE_UPDATE2_ARRAY_PREFIX);
    }

    public void subscribeToDesiredPropertiesRecoveredFromAmqpsSessionDropFlow(
            Object propertyValue,
            Object updatePropertyValue,
            Object updatePropertyValue2,
            String update1Prefix,
            String update2Prefix) throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        super.setUpNewDeviceAndModule();

        this.errorInjectionSubscribeToDesiredPropertiesFlow(
            ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec),
            propertyValue,
            updatePropertyValue,
            updatePropertyValue2,
            update1Prefix,
            update2Prefix);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsCBSReqLinkrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsCBSReqLinkropFlow(
            PROPERTY_VALUE,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE2,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsCBSReqLinkropFlow() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsCBSReqLinkropFlow(
            jsonParser.parse(PROPERTY_VALUE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE2_ARRAY),
            PROPERTY_VALUE_UPDATE_ARRAY_PREFIX,
            PROPERTY_VALUE_UPDATE2_ARRAY_PREFIX);
    }

    public void subscribeToDesiredPropertiesRecoveredFromAmqpsCBSReqLinkropFlow(
        Object propertyValue,
        Object updatePropertyValue,
        Object updatePropertyValue2,
        String update1Prefix,
        String update2Prefix) throws Exception
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

        super.setUpNewDeviceAndModule();

        this.errorInjectionSubscribeToDesiredPropertiesFlow(
            ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec),
            propertyValue,
            updatePropertyValue,
            updatePropertyValue2,
            update1Prefix,
            update2Prefix);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsCBSRespLinkDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsCBSRespLinkDropFlow(
            PROPERTY_VALUE,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE2,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredArrayPropertiesRecoveredFromAmqpsCBSRespLinkDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsCBSRespLinkDropFlow(
            jsonParser.parse(PROPERTY_VALUE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE2_ARRAY),
            PROPERTY_VALUE_UPDATE_ARRAY_PREFIX,
            PROPERTY_VALUE_UPDATE2_ARRAY_PREFIX);
    }

    public void subscribeToDesiredPropertiesRecoveredFromAmqpsCBSRespLinkDropFlow(
        Object propertyValue,
        Object updatePropertyValue,
        Object updatePropertyValue2,
        String update1Prefix,
        String update2Prefix) throws Exception
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

        super.setUpNewDeviceAndModule();

        this.errorInjectionSubscribeToDesiredPropertiesFlow(
            ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec),
            propertyValue,
            updatePropertyValue,
            updatePropertyValue2,
            update1Prefix,
            update2Prefix);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsD2CDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsD2CDropFlow(
            PROPERTY_VALUE,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE2,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredArrayPropertiesRecoveredFromAmqpsD2CDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsD2CDropFlow(
            jsonParser.parse(PROPERTY_VALUE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE2_ARRAY),
            PROPERTY_VALUE_UPDATE_ARRAY_PREFIX,
            PROPERTY_VALUE_UPDATE2_ARRAY_PREFIX);
    }

    public void subscribeToDesiredPropertiesRecoveredFromAmqpsD2CDropFlow(
        Object propertyValue,
        Object updatePropertyValue,
        Object updatePropertyValue2,
        String update1Prefix,
        String update2Prefix
    ) throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        super.setUpNewDeviceAndModule();

        this.errorInjectionSubscribeToDesiredPropertiesFlow(
            ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec),
            propertyValue,
            updatePropertyValue,
            updatePropertyValue2,
            update1Prefix,
            update2Prefix);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredPropertiesRecoveredFromC2DDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromC2DDropFlow(
            PROPERTY_VALUE,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE2,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredArrayPropertiesRecoveredFromC2DDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromC2DDropFlow(
            jsonParser.parse(PROPERTY_VALUE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE2_ARRAY),
            PROPERTY_VALUE_UPDATE_ARRAY_PREFIX,
            PROPERTY_VALUE_UPDATE2_ARRAY_PREFIX);
    }

    public void subscribeToDesiredPropertiesRecoveredFromC2DDropFlow(
        Object propertyValue,
        Object updatePropertyValue,
        Object updatePropertyValue2,
        String update1Prefix,
        String update2Prefix) throws Exception
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

        super.setUpNewDeviceAndModule();

        this.errorInjectionSubscribeToDesiredPropertiesFlow(
            ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec),
            propertyValue,
            updatePropertyValue,
            updatePropertyValue2,
            update1Prefix,
            update2Prefix);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsTwinReqLinkDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsTwinReqLinkDropFlow(
            PROPERTY_VALUE,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE2,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredArrayPropertiesRecoveredFromAmqpsTwinReqLinkDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsTwinReqLinkDropFlow(
            jsonParser.parse(PROPERTY_VALUE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE2_ARRAY),
            PROPERTY_VALUE_UPDATE_ARRAY_PREFIX,
            PROPERTY_VALUE_UPDATE2_ARRAY_PREFIX);
    }

    public void subscribeToDesiredPropertiesRecoveredFromAmqpsTwinReqLinkDropFlow(
            Object propertyValue,
            Object updatePropertyValue,
            Object updatePropertyValue2,
            String update1Prefix,
            String update2Prefix) throws Exception
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

        super.setUpNewDeviceAndModule();

        this.errorInjectionSubscribeToDesiredPropertiesFlow(
            ErrorInjectionHelper.amqpsTwinReqLinkDropErrorInjectionMessage(
                    ErrorInjectionHelper.DefaultDelayInSec,
                    ErrorInjectionHelper.DefaultDurationInSec),
            propertyValue,
            updatePropertyValue,
            updatePropertyValue2,
            update1Prefix,
            update2Prefix);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsTwinRespLinkDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsTwinRespLinkDropFlow(
            PROPERTY_VALUE,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE2,
            PROPERTY_VALUE_UPDATE,
            PROPERTY_VALUE_UPDATE);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void subscribeToDesiredArrayPropertiesRecoveredFromAmqpsTwinRespLinkDrop() throws Exception
    {
        subscribeToDesiredPropertiesRecoveredFromAmqpsTwinRespLinkDropFlow(
            jsonParser.parse(PROPERTY_VALUE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
            jsonParser.parse(PROPERTY_VALUE_UPDATE2_ARRAY),
            PROPERTY_VALUE_UPDATE_ARRAY_PREFIX,
            PROPERTY_VALUE_UPDATE2_ARRAY_PREFIX);
    }

    public void subscribeToDesiredPropertiesRecoveredFromAmqpsTwinRespLinkDropFlow(
        Object propertyValue,
        Object updatePropertyValue,
        Object updatePropertyValue2,
        String update1Prefix,
        String update2Prefix) throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        super.setUpNewDeviceAndModule();

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Twin Resp link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionSubscribeToDesiredPropertiesFlow(
            ErrorInjectionHelper.amqpsTwinRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec),
            propertyValue,
            updatePropertyValue,
            updatePropertyValue2,
            update1Prefix,
            update2Prefix);
    }

    public void errorInjectionSubscribeToDesiredPropertiesFlow(
        Message errorInjectionMessage,
        Object propertyValue,
        Object updatePropertyValue,
        Object updatePropertyValue2,
        String update1Prefix,
        String update2Prefix) throws Exception
    {
        // Arrange
        List<com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates = new ArrayList<>();
        setConnectionStatusCallBack(actualStatusUpdates);
        subscribeToDesiredPropertiesAndVerify(1, propertyValue, updatePropertyValue, update1Prefix);

        // Act
        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(errorInjectionMessage, IotHubStatusCode.OK_EMPTY);
        IotHubServicesCommon.sendErrorInjectionMessageAndWaitForResponse(
            internalClient,
            errorInjectionMsgAndRet,
            RETRY_MILLISECONDS,
            SEND_TIMEOUT_MILLISECONDS,
            this.testInstance.protocol);

        // Assert
        IotHubServicesCommon.waitForStabilizedConnection(actualStatusUpdates, ERROR_INJECTION_WAIT_TIMEOUT_MILLISECONDS, internalClient);
        deviceUnderTest.dCDeviceForTwin.propertyStateList[0].callBackTriggered = false;
        Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Expected desired properties to be size 1, but was size " + deviceUnderTest.sCDeviceForTwin.getDesiredProperties().size(), internalClient), 1, deviceUnderTest.sCDeviceForTwin.getDesiredProperties().size());
        Set<Pair> dp = new HashSet<>();
        Pair p = deviceUnderTest.sCDeviceForTwin.getDesiredProperties().iterator().next();
        p.setValue(updatePropertyValue2);
        dp.add(p);
        deviceUnderTest.sCDeviceForTwin.setDesiredProperties(dp);

        testInstance.twinServiceClient.updateTwin(deviceUnderTest.sCDeviceForTwin);

        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(update2Prefix, false);
    }
}
