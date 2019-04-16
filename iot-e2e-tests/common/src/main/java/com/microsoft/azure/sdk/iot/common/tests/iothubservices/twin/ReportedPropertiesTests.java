/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.twin;

import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.helpers.ConditionalIgnoreRule;
import com.microsoft.azure.sdk.iot.common.helpers.StandardTierOnlyRule;
import com.microsoft.azure.sdk.iot.common.setup.DeviceTwinCommon;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to Reported properties. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class ReportedPropertiesTests extends DeviceTwinCommon
{
    public ReportedPropertiesTests(String deviceId, String moduleId, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceId, moduleId, protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSendReportedProperties() throws IOException, IotHubException, InterruptedException
    {
        sendReportedPropertiesAndVerify(MAX_PROPERTIES_TO_TEST);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
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
                        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(1);
                        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
                    }
                    catch (IOException e)
                    {
                        fail(e.getMessage());
                    }
                    assertEquals(buildExceptionMessage("Expected SUCCESS but twin status was " + deviceUnderTest.deviceTwinStatus, internalClient), DeviceTwinCommon.STATUS.SUCCESS, deviceUnderTest.deviceTwinStatus);
                }
            });
        }
        executor.shutdown();
        if (!executor.awaitTermination(MULTITHREADED_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        // verify if they are received by SC
        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE, MAX_PROPERTIES_TO_TEST.intValue());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSendReportedPropertiesSequentially() throws IOException, InterruptedException, IotHubException
    {
        // arrange

        // send max_prop RP one at a time sequentially
        // verify if they are updated by SC
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(1);
            internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
            waitAndVerifyTwinStatusBecomesSuccess();
        }

        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE, MAX_PROPERTIES_TO_TEST.intValue());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testUpdateReportedProperties() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        // act
        // Update RP
        deviceUnderTest.dCDeviceForTwin.updateAllExistingReportedProperties();
        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();

        // verify if they are received by SC
        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE_UPDATE, MAX_PROPERTIES_TO_TEST.intValue());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testUpdateReportedPropertiesMultiThreaded() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_PROPERTIES_TO_TEST);

        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

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
                        deviceUnderTest.dCDeviceForTwin.updateExistingReportedProperty(index);
                        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
                        waitAndVerifyTwinStatusBecomesSuccess();
                    }
                    catch (IOException | InterruptedException e)
                    {
                        fail(buildExceptionMessage("Unexpected exception occurred during sending reported properties: " + e.getMessage(), internalClient));
                    }
                }
            });
        }
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);
        executor.shutdown();
        if (!executor.awaitTermination(MULTITHREADED_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        // assert
        assertEquals(buildExceptionMessage("Expected SUCCESS but twin status was " + deviceUnderTest.deviceTwinStatus, internalClient), DeviceTwinCommon.STATUS.SUCCESS, deviceUnderTest.deviceTwinStatus);

        // verify if they are received by SC
        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE_UPDATE, MAX_PROPERTIES_TO_TEST.intValue());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testUpdateReportedPropertiesSequential() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        // act
        // Update RP
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            deviceUnderTest.dCDeviceForTwin.updateExistingReportedProperty(i);
            internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        }

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();

        // verify if they are received by SC
        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE_UPDATE, MAX_PROPERTIES_TO_TEST.intValue());
    }
}
