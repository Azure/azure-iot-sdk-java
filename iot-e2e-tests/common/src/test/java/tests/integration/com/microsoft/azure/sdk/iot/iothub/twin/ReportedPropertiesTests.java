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
import org.junit.Assert;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static org.junit.Assert.fail;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to Reported properties.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class ReportedPropertiesTests extends DeviceTwinCommon
{
    public ReportedPropertiesTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws IOException
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
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
    @ContinuousIntegrationTest
    public void testSendReportedPropertiesMultiThreaded() throws IOException, IotHubException, InterruptedException
    {
        // arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_PROPERTIES_TO_TEST);

        // act
        // send max_prop RP one at a time in parallel
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Set<Property> newReportedProperties = deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(1);
                        internalClient.sendReportedProperties(newReportedProperties);
                    }
                    catch (IOException e)
                    {
                        fail(e.getMessage());
                    }
                    Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Expected OK but twin status was " + deviceUnderTest.deviceTwinStatus, internalClient), OK, deviceUnderTest.deviceTwinStatus);
                }
            });
        }
        executor.shutdown();
        if (!executor.awaitTermination(MULTITHREADED_WAIT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        // verify if they are received by SC
        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE, MAX_PROPERTIES_TO_TEST);
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
            Set<Property> createdProperties = deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(1);
            internalClient.sendReportedProperties(createdProperties);
            waitAndVerifyTwinStatusBecomesSuccess();
        }

        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE, MAX_PROPERTIES_TO_TEST);
    }

    @Test
    @StandardTierHubOnlyTest
    public void testUpdateReportedProperties() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(REPORTED_PROPERTIES_PROPAGATION_DELAY_MILLISECONDS);

        // act
        // Update RP
        deviceUnderTest.dCDeviceForTwin.updateAllExistingReportedProperties();
        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();

        // verify if they are received by SC
        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE_UPDATE, MAX_PROPERTIES_TO_TEST);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void testUpdateReportedPropertiesMultiThreaded() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_PROPERTIES_TO_TEST);

        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(REPORTED_PROPERTIES_PROPAGATION_DELAY_MILLISECONDS);

        // act
        // Update RP
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            final int index = i;
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Set<Property> updatedProperties = deviceUnderTest.dCDeviceForTwin.updateExistingReportedProperty(index);
                        internalClient.sendReportedProperties(updatedProperties);
                        waitAndVerifyTwinStatusBecomesSuccess();
                    }
                    catch (IOException | InterruptedException e)
                    {
                        fail(CorrelationDetailsLoggingAssert.buildExceptionMessage("Unexpected exception occurred during sending reported properties: " + Tools.getStackTraceFromThrowable(e), internalClient));
                    }
                }
            });
        }
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);
        executor.shutdown();
        if (!executor.awaitTermination(MULTITHREADED_WAIT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        // assert
        Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Expected OK but twin status was " + deviceUnderTest.deviceTwinStatus, internalClient), OK, deviceUnderTest.deviceTwinStatus);

        // verify if they are received by SC
        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE_UPDATE, MAX_PROPERTIES_TO_TEST);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void testUpdateReportedPropertiesSequential() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());

        Thread.sleep(REPORTED_PROPERTIES_PROPAGATION_DELAY_MILLISECONDS);

        // act
        // Update RP
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            deviceUnderTest.dCDeviceForTwin.updateExistingReportedProperty(i);
        }

        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();

        // verify if they are received by SC
        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE_UPDATE, MAX_PROPERTIES_TO_TEST);
    }
}
