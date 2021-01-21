/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.twin;

import com.google.gson.JsonParser;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertiesCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to DesiredProperties.
 */
@Slf4j
@IotHubTest
@RunWith(Parameterized.class)
public class DesiredPropertiesTests extends DeviceTwinCommon
{
    private final JsonParser jsonParser;

    public DesiredPropertiesTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws IOException
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
        jsonParser = new JsonParser();
    }

    @Test
    @StandardTierHubOnlyTest
    public void testSubscribeToDesiredProperties() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
        subscribeToDesiredPropertiesAndVerify(
                MAX_PROPERTIES_TO_TEST,
                PROPERTY_VALUE,
                PROPERTY_VALUE_UPDATE,
                PROPERTY_VALUE_UPDATE);
    }

    @Test
    @StandardTierHubOnlyTest
    public void testSubscribeToDesiredArrayProperties() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
        subscribeToDesiredPropertiesAndVerify(
                MAX_PROPERTIES_TO_TEST,
                jsonParser.parse(PROPERTY_VALUE_ARRAY),
                jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
                PROPERTY_VALUE_UPDATE_ARRAY_PREFIX);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void testSubscribeToDesiredArrayPropertiesWithVersion() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
        testSubscribeToDesiredPropertiesWithVersionFlow(
                jsonParser.parse(PROPERTY_VALUE_ARRAY),
                jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
                PROPERTY_VALUE_UPDATE_ARRAY_PREFIX);
    }

    @Test
    @StandardTierHubOnlyTest
    public void testSubscribeToDesiredPropertiesWithVersion() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
        testSubscribeToDesiredPropertiesWithVersionFlow(
                PROPERTY_VALUE,
                PROPERTY_VALUE_UPDATE,
                PROPERTY_VALUE_UPDATE);
    }

    public void testSubscribeToDesiredPropertiesWithVersionFlow(Object propertyValue, Object updatePropertyValue, String updatePropertyPrefix) throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
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
            desiredPropertiesCB.put(propertyState.property, new com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair<>(deviceUnderTest.dCOnProperty, propertyState));
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
        testInstance.twinServiceClient.updateTwin(deviceUnderTest.sCDeviceForTwin);

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(updatePropertyPrefix, true);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void testSubscribeToDesiredPropertiesMultiThreaded() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
        testSubscribeToDesiredPropertiesMultiThreadedFlow(PROPERTY_VALUE, PROPERTY_VALUE_UPDATE, PROPERTY_VALUE_UPDATE);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void testSubscribeToDesiredArrayPropertiesMultiThreaded() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
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
                            testInstance.twinServiceClient.updateTwin(deviceUnderTest.sCDeviceForTwin);
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
    @StandardTierHubOnlyTest
    public void testSubscribeToDesiredPropertiesSequentially() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
        testSubscribeToDesiredPropertiesSequentiallyFlow(PROPERTY_VALUE, PROPERTY_VALUE_UPDATE, PROPERTY_VALUE_UPDATE);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void testSubscribeToDesiredArrayPropertiesSequentially() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
        testSubscribeToDesiredPropertiesSequentiallyFlow(
                jsonParser.parse(PROPERTY_VALUE_ARRAY),
                jsonParser.parse(PROPERTY_VALUE_UPDATE_ARRAY),
                PROPERTY_VALUE_UPDATE_ARRAY_PREFIX);
    }

    public void testSubscribeToDesiredPropertiesSequentiallyFlow(
            Object propertyValue,
            Object updatePropertyValue,
            String updatePropertyPrefix) throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
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
            testInstance.twinServiceClient.updateTwin(deviceUnderTest.sCDeviceForTwin);
            Thread.sleep(DELAY_BETWEEN_OPERATIONS);
        }

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(updatePropertyPrefix, false);
    }

    @Test
    @StandardTierHubOnlyTest
    public void testUpdateDesiredProperties() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        super.setUpNewDeviceAndModule();
        addMultipleDevices(MAX_DEVICES);

        // Add desired properties for multiple devices
        deviceUnderTest.sCDeviceForTwin.clearTwin();
        deviceUnderTest.dCDeviceForTwin.getDesiredProp().clear();
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(PROPERTY_KEY + i, PROPERTY_VALUE + i));
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            testInstance.twinServiceClient.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Update desired properties on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties();
            for (com.microsoft.azure.sdk.iot.service.devicetwin.Pair dp : desiredProperties)
            {
                dp.setValue(PROPERTY_VALUE_UPDATE + i);
            }
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            testInstance.twinServiceClient.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(devicesUnderTest[i].sCDeviceForTwin);

            for (com.microsoft.azure.sdk.iot.service.devicetwin.Pair dp : devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties())
            {
                Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Unexpected desired property key, expected " + PROPERTY_KEY + i + " but was " + dp.getKey(), internalClient), PROPERTY_KEY + i, dp.getKey());
                Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Unexpected desired property value, expected " + PROPERTY_VALUE_UPDATE + i + " but was " + dp.getValue(), internalClient), PROPERTY_VALUE_UPDATE + i, dp.getValue());
            }
            Integer version = devicesUnderTest[i].sCDeviceForTwin.getDesiredPropertiesVersion();
            assertNotNull(CorrelationDetailsLoggingAssert.buildExceptionMessage("Version was null", internalClient), version);
        }

        // Remove desired properties
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties();
            for (com.microsoft.azure.sdk.iot.service.devicetwin.Pair dp : desiredProperties)
            {
                dp.setValue(null);
            }
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            testInstance.twinServiceClient.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(devicesUnderTest[i].sCDeviceForTwin);

            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Desired properties were not deleted by setting to null", internalClient), 0, devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties().size());
        }

        removeMultipleDevices(MAX_DEVICES);
    }

    @AllArgsConstructor
    class TwinPropertiesCallbackImpl implements TwinPropertiesCallback
    {
        TwinCollection expectedProperties;

        @Override
        public void TwinPropertiesCallBack(TwinCollection actualProperties, Object context) {
            Success desiredPropertiesCallbackState = (Success) context;
            desiredPropertiesCallbackState.callbackWasFired();

            if (actualProperties.size() != expectedProperties.size())
            {
                log.error("Batch twin properties callback was fired, but had an unexpected number of properties");
                desiredPropertiesCallbackState.setResult(false);
                return;
            }

            for (String actualPropertyKey : actualProperties.keySet())
            {
                if (!expectedProperties.containsKey(actualPropertyKey))
                {
                    log.error("Batch twin properties callback was fired, but had an unexpected key");
                    desiredPropertiesCallbackState.setResult(false);
                    return;
                }
                else if (!actualProperties.get(actualPropertyKey).equals(expectedProperties.get(actualPropertyKey)))
                {
                    log.error("Batch twin properties callback was fired, but had an unexpected value");
                    desiredPropertiesCallbackState.setResult(false);
                    return;
                }
            }

            // If the twins have the same number of properties, and the same key/value pairs, then the expected
            // twin matches the actually received twin
            desiredPropertiesCallbackState.setResult(true);
        }
    }

    // This test is for the startDeviceTwin/startTwin API that takes the TwinPropertiesCallback rather than the TwinPropertyCallback
    // This callback should receive the full twin update in one callback, rather than one callback per updated
    // desired property
    @Test
    @StandardTierHubOnlyTest
    public void testSubscribeToDesiredPropertiesBatch() throws Exception
    {
        super.setUpNewDeviceAndModule(false);

        String expectedKey1 = UUID.randomUUID().toString();
        String expectedValue1 = UUID.randomUUID().toString();
        String expectedKey2 = UUID.randomUUID().toString();
        String expectedValue2 = UUID.randomUUID().toString();

        TwinCollection expectedDesiredProperties = new TwinCollection();
        expectedDesiredProperties.putFinal(expectedKey1, expectedValue1);
        expectedDesiredProperties.putFinal(expectedKey2, expectedValue2);
        TwinPropertiesCallback twinPropertiesCallback = new TwinPropertiesCallbackImpl(expectedDesiredProperties);

        Success desiredPropertiesCallbackState = new Success();

        internalClient.open();
        if (internalClient instanceof DeviceClient)
        {
            ((DeviceClient) internalClient).startDeviceTwin(new DeviceTwinStatusCallBack(), deviceUnderTest, twinPropertiesCallback, desiredPropertiesCallbackState);
        }
        else
        {
            ((ModuleClient) internalClient).startTwin(new DeviceTwinStatusCallBack(), deviceUnderTest, twinPropertiesCallback, desiredPropertiesCallbackState);
        }

        long startTime = System.currentTimeMillis();
        while (deviceUnderTest.deviceTwinStatus != IotHubStatusCode.OK)
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > START_TWIN_TIMEOUT_MILLISECONDS)
            {
                fail("Timed out waiting for twin to start");
            }
        }

        DeviceTwinDevice serviceClientTwin;
        if (testInstance.clientType == ClientType.DEVICE_CLIENT)
        {
            serviceClientTwin = new DeviceTwinDevice(internalClient.getConfig().getDeviceId());
        }
        else
        {
            serviceClientTwin = new DeviceTwinDevice(internalClient.getConfig().getDeviceId(), internalClient.getConfig().getModuleId());
        }

        Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(expectedKey1, expectedValue1));
        desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(expectedKey2, expectedValue2));
        serviceClientTwin.setDesiredProperties(desiredProperties);

        testInstance.twinServiceClient.updateTwin(serviceClientTwin);

        startTime = System.currentTimeMillis();
        while (!desiredPropertiesCallbackState.wasCallbackFired())
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > DESIRED_PROPERTIES_PROPAGATION_TIME_MILLISECONDS)
            {
                fail("Timed out waiting for desired properties callback to execute");
            }
        }

        assertTrue("Desired properties callback executed, but with unexpected properties", desiredPropertiesCallbackState.getResult());
    }
}
