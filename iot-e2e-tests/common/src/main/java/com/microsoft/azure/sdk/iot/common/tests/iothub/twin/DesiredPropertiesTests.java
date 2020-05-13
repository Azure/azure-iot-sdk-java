/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothub.twin;

import com.google.gson.JsonParser;
import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.helpers.ConditionalIgnoreRule;
import com.microsoft.azure.sdk.iot.common.helpers.StandardTierOnlyRule;
import com.microsoft.azure.sdk.iot.common.setup.iothub.DeviceTwinCommon;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static org.junit.Assert.*;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to DesiredProperties. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class DesiredPropertiesTests extends DeviceTwinCommon
{
    private JsonParser jsonParser = new JsonParser();

    public DesiredPropertiesTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
        jsonParser = new JsonParser();
    }

    @Before
    public void setUpNewDeviceAndModule() throws IOException, IotHubException, URISyntaxException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        super.setUpNewDeviceAndModule();
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSubscribeToDesiredProperties() throws IOException, InterruptedException, IotHubException
    {
        subscribeToDesiredPropertiesAndVerify(
                MAX_PROPERTIES_TO_TEST,
                PROPERTY_VALUE,
                PROPERTY_VALUE_UPDATE,
                PROPERTY_VALUE_UPDATE);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSubscribeToDesiredArrayProperties() throws IOException, InterruptedException, IotHubException
    {
        subscribeToDesiredPropertiesAndVerify(
                MAX_PROPERTIES_TO_TEST,
                jsonParser.parse(PROPERTY_VALUE_ARRAY),
                jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
                PROPERTY_VALUE_UPDATE_ARRAY_PREFIX);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSubscribeToDesiredArrayPropertiesWithVersion() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        testSubscribeToDesiredPropertiesWithVersionFlow(
                jsonParser.parse(PROPERTY_VALUE_ARRAY),
                jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
                PROPERTY_VALUE_UPDATE_ARRAY_PREFIX);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSubscribeToDesiredPropertiesWithVersion() throws IOException, InterruptedException, IotHubException
    {
        testSubscribeToDesiredPropertiesWithVersionFlow(
                PROPERTY_VALUE,
                PROPERTY_VALUE_UPDATE,
                PROPERTY_VALUE_UPDATE);
    }

    public void testSubscribeToDesiredPropertiesWithVersionFlow(Object propertyValue, Object updatePropertyValue, String updatePropertyPrefix) throws IOException, InterruptedException, IotHubException
    {
        // arrange
        deviceUnderTest.sCDeviceForTwin.clearTwin();
        deviceUnderTest.dCDeviceForTwin.getDesiredProp().clear();
        Map<Property, Pair<TwinPropertyCallBack, Object>> desiredPropertiesCB = new HashMap<>();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            PropertyState propertyState = new PropertyState();
            propertyState.callBackTriggered = false;
            propertyState.propertyNewVersion = -1;
            propertyState.property = new Property(PROPERTY_KEY + i, propertyValue);
            deviceUnderTest.dCDeviceForTwin.propertyStateList[i] = propertyState;
            desiredPropertiesCB.put(propertyState.property, new com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair<TwinPropertyCallBack, Object>(deviceUnderTest.dCOnProperty, propertyState));
        }

        // act
        internalClient.subscribeToTwinDesiredProperties(desiredPropertiesCB);
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = new HashSet<>();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(PROPERTY_KEY + i, updatePropertyValue));
        }
        deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
        sCDeviceTwin.updateTwin(deviceUnderTest.sCDeviceForTwin);

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(updatePropertyPrefix, true);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSubscribeToDesiredPropertiesMultiThreaded() throws IOException, InterruptedException, IotHubException
    {
        testSubscribeToDesiredPropertiesMultiThreadedFlow(PROPERTY_VALUE, PROPERTY_VALUE_UPDATE, PROPERTY_VALUE_UPDATE);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSubscribeToDesiredArrayPropertiesMultiThreaded() throws IOException, InterruptedException, IotHubException
    {
        testSubscribeToDesiredPropertiesMultiThreadedFlow(jsonParser.parse(PROPERTY_VALUE_ARRAY), jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY), PROPERTY_VALUE_UPDATE_ARRAY_PREFIX);
    }

    public void testSubscribeToDesiredPropertiesMultiThreadedFlow(Object propertyValue, Object updatePropertyValue, String updatePropertyPrefix) throws IOException, InterruptedException, IotHubException
    {
        // arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_PROPERTIES_TO_TEST);

        deviceUnderTest.dCDeviceForTwin.getDesiredProp().clear();
        deviceUnderTest.sCDeviceForTwin.clearTwin();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            PropertyState propertyState = new PropertyState();
            propertyState.callBackTriggered = false;
            propertyState.property = new Property(PROPERTY_KEY + i, propertyValue);
            deviceUnderTest.dCDeviceForTwin.propertyStateList[i] = propertyState;
            deviceUnderTest.dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, deviceUnderTest.dCDeviceForTwin, propertyState);
        }

        // act
        internalClient.subscribeToDesiredProperties(deviceUnderTest.dCDeviceForTwin.getDesiredProp());
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        //Setting desired properties in different threads leads to a race condition
        Object desiredPropertiesUpdateLock = new Object();

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
                        Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = new HashSet<>();
                        desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(PROPERTY_KEY + index, updatePropertyValue));
                        synchronized (desiredPropertiesUpdateLock)
                        {
                            Set currentDesiredProperties = deviceUnderTest.sCDeviceForTwin.getDesiredProperties();
                            desiredProperties.addAll(currentDesiredProperties);
                            deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
                            sCDeviceTwin.updateTwin(deviceUnderTest.sCDeviceForTwin);
                        }
                    }
                    catch (IotHubException | IOException e)
                    {
                        fail(e.getMessage());
                    }
                }
            });
            Thread.sleep(DELAY_BETWEEN_OPERATIONS);
        }

        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.MINUTES))
        {
            executor.shutdownNow();
        }

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(updatePropertyPrefix, false);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSubscribeToDesiredPropertiesSequentially() throws IOException, InterruptedException, IotHubException
    {
        testSubscribeToDesiredPropertiesSequentiallyFlow(PROPERTY_VALUE, PROPERTY_VALUE_UPDATE, PROPERTY_VALUE_UPDATE);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSubscribeToDesiredArrayPropertiesSequentially() throws IOException, InterruptedException, IotHubException
    {
        testSubscribeToDesiredPropertiesSequentiallyFlow(
                jsonParser.parse(PROPERTY_VALUE_ARRAY),
                jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
                PROPERTY_VALUE_UPDATE_ARRAY_PREFIX);
    }

    public void testSubscribeToDesiredPropertiesSequentiallyFlow(
            Object propertyValue,
            Object updatePropertyValue,
            String updatePropertyPrefix) throws IOException, InterruptedException, IotHubException
    {
        // arrange
        deviceUnderTest.sCDeviceForTwin.clearTwin();
        deviceUnderTest.dCDeviceForTwin.getDesiredProp().clear();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            PropertyState propertyState = new PropertyState();
            propertyState.callBackTriggered = false;
            propertyState.property = new Property(PROPERTY_KEY + i, propertyValue);
            deviceUnderTest.dCDeviceForTwin.propertyStateList[i] = propertyState;
            deviceUnderTest.dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, deviceUnderTest.dCDeviceForTwin, propertyState);
        }

        // act
        internalClient.subscribeToDesiredProperties(deviceUnderTest.dCDeviceForTwin.getDesiredProp());
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(PROPERTY_KEY + i, updatePropertyValue));
            deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
            sCDeviceTwin.updateTwin(deviceUnderTest.sCDeviceForTwin);
            Thread.sleep(DELAY_BETWEEN_OPERATIONS);
        }

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(updatePropertyPrefix, false);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testUpdateDesiredProperties() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(MAX_DEVICES);

        // Add desired properties for multiple devices
        deviceUnderTest.sCDeviceForTwin.clearTwin();
        deviceUnderTest.dCDeviceForTwin.getDesiredProp().clear();
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(PROPERTY_KEY + i, PROPERTY_VALUE + i));
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Update desired properties on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties();
            for (com.microsoft.azure.sdk.iot.service.devicetwin.Pair dp : desiredProperties)
            {
                dp.setValue(PROPERTY_VALUE_UPDATE + i);
            }
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);

            for (com.microsoft.azure.sdk.iot.service.devicetwin.Pair dp : devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties())
            {
                assertEquals(buildExceptionMessage("Unexpected desired property key, expected " + PROPERTY_KEY + i + " but was " + dp.getKey(), internalClient), PROPERTY_KEY + i, dp.getKey());
                assertEquals(buildExceptionMessage("Unexpected desired property value, expected " + PROPERTY_VALUE_UPDATE + i + " but was " + dp.getValue(), internalClient), PROPERTY_VALUE_UPDATE + i, dp.getValue());
            }
            Integer version = devicesUnderTest[i].sCDeviceForTwin.getDesiredPropertiesVersion();
            assertNotNull(buildExceptionMessage("Version was null", internalClient), version);
        }

        // Remove desired properties
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties();
            for (com.microsoft.azure.sdk.iot.service.devicetwin.Pair dp : desiredProperties)
            {
                dp.setValue(null);
            }
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);

            assertEquals(buildExceptionMessage("Desired properties were not deleted by setting to null", internalClient), 0, devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties().size());
        }

        removeMultipleDevices(MAX_DEVICES);
    }
}
