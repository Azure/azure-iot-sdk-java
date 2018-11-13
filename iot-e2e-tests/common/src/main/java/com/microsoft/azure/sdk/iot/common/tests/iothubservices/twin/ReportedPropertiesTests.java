/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.twin;

import com.microsoft.azure.sdk.iot.common.setup.DeviceTwinCommon;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to Reported properties. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class ReportedPropertiesTests extends DeviceTwinCommon
{
    public ReportedPropertiesTests(String deviceId, String moduleId, IotHubClientProtocol protocol, AuthenticationType authenticationType, String clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceId, moduleId, protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testSendReportedProperties() throws IOException, IotHubException, InterruptedException
    {
        sendReportedPropertiesAndVerify(MAX_PROPERTIES_TO_TEST);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
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
                    assertEquals(deviceUnderTest.deviceTwinStatus, DeviceTwinCommon.STATUS.SUCCESS);
                }
            });
        }
        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        // verify if they are received by SC
        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE, MAX_PROPERTIES_TO_TEST.intValue());
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
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

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
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

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
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
                        fail(e.getMessage());
                    }
                }
            });
        }
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);
        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        // assert
        assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);

        // verify if they are received by SC
        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE_UPDATE, MAX_PROPERTIES_TO_TEST.intValue());
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
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
