/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.twin;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.DeviceTwinCommon;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to Reported properties.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class ReportedPropertiesTests extends DeviceTwinCommon
{
    public ReportedPropertiesTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, TestClientType testClientType) throws IOException
    {
        super(protocol, authenticationType, testClientType);
    }

    @Before
    @SuppressWarnings("EmptyMethod")
    public void setUpNewDeviceAndModule() throws IOException, IotHubException, URISyntaxException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        super.setUpNewDeviceAndModule();
    }

    @Test
    @StandardTierHubOnlyTest
    public void testSendReportedProperties() throws IOException, IotHubException, InterruptedException
    {
        sendReportedPropertiesAndVerify(MAX_PROPERTIES_TO_TEST);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void testSendReportedArrayProperties() throws IOException, IotHubException, InterruptedException
    {
        sendReportedArrayPropertiesAndVerify(MAX_PROPERTIES_TO_TEST);
    }

    @Test
    @StandardTierHubOnlyTest
    public void testSendReportedPropertiesSequentially() throws IOException, InterruptedException, IotHubException
    {
        // arrange

        // send max_prop RP one at a time sequentially
        // verify if they are updated by SC
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            Set<Property> createdProperties = testInstance.deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(1);
            testInstance.testIdentity.getClient().sendReportedPropertiesAsync(createdProperties);
            waitAndVerifyTwinStatusBecomesSuccess();
        }

        readReportedPropertiesAndVerify(testInstance.deviceUnderTest, PROPERTY_VALUE, MAX_PROPERTIES_TO_TEST);
    }

    @Test
    @StandardTierHubOnlyTest
    public void testUpdateReportedProperties() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        // send max_prop RP all at once
        testInstance.deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        testInstance.testIdentity.getClient().sendReportedPropertiesAsync(testInstance.deviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(REPORTED_PROPERTIES_PROPAGATION_DELAY_MILLISECONDS);

        // act
        // Update RP
        testInstance.deviceUnderTest.dCDeviceForTwin.updateAllExistingReportedProperties();
        testInstance.testIdentity.getClient().sendReportedPropertiesAsync(testInstance.deviceUnderTest.dCDeviceForTwin.getReportedProp());

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();

        // verify if they are received by SC
        readReportedPropertiesAndVerify(testInstance.deviceUnderTest, PROPERTY_VALUE_UPDATE, MAX_PROPERTIES_TO_TEST);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void testUpdateReportedPropertiesSequentially() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        // send max_prop RP all at once
        testInstance.deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        testInstance.testIdentity.getClient().sendReportedPropertiesAsync(testInstance.deviceUnderTest.dCDeviceForTwin.getReportedProp());

        Thread.sleep(REPORTED_PROPERTIES_PROPAGATION_DELAY_MILLISECONDS);

        // act
        // Update RP
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            testInstance.deviceUnderTest.dCDeviceForTwin.updateExistingReportedProperty(i);
        }

        testInstance.testIdentity.getClient().sendReportedPropertiesAsync(testInstance.deviceUnderTest.dCDeviceForTwin.getReportedProp());

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();

        // verify if they are received by SC
        readReportedPropertiesAndVerify(testInstance.deviceUnderTest, PROPERTY_VALUE_UPDATE, MAX_PROPERTIES_TO_TEST);
    }
}
