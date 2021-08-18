/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.twin;

import com.google.gson.JsonParser;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertiesCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallback;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
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

    public DesiredPropertiesTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException
    {
        super(protocol, authenticationType, clientType);
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
    public void testReplaceTwin() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        // arrange
        if (testInstance.protocol != IotHubClientProtocol.AMQPS || testInstance.authenticationType != AuthenticationType.SAS)
        {
            //Test is for service client operations, so no need to parameterize on device client protocols or authentication types
            return;
        }

        super.setUpNewDeviceAndModule(false);

        String propertyKey = "someKey";
        String propertyValue = "someValue";
        String propertyUpdateKey = "someUpdatedKey";
        String propertyUpdateValue = "someUpdatedValue";
        String tagKey = "someKey";
        String tagValue = "someValue";
        String tagUpdateKey = "someUpdatedKey";
        String tagUpdateValue = "someUpdatedValue";

        testInstance.twinServiceClient.getTwin(testInstance.deviceUnderTest.sCDeviceForTwin);

        Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = new HashSet<>();
        Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> tags = new HashSet<>();
        desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(propertyKey, propertyValue));
        tags.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(tagKey, tagValue));
        testInstance.deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
        testInstance.deviceUnderTest.sCDeviceForTwin.setTags(tags);
        testInstance.deviceUnderTest.sCDeviceForTwin = testInstance.twinServiceClient.replaceTwin(testInstance.deviceUnderTest.sCDeviceForTwin);

        // Check that the twin has the expected desired properties and tags
        testInstance.twinServiceClient.getTwin(testInstance.deviceUnderTest.sCDeviceForTwin);
        assertEquals(1, testInstance.deviceUnderTest.sCDeviceForTwin.getDesiredProperties().size());
        com.microsoft.azure.sdk.iot.service.devicetwin.Pair actualDesiredProperty =
            testInstance.deviceUnderTest.sCDeviceForTwin.getDesiredProperties().iterator().next();

        assertEquals(propertyKey, actualDesiredProperty.getKey());
        assertEquals(propertyValue, actualDesiredProperty.getValue());

        assertEquals(1, testInstance.deviceUnderTest.sCDeviceForTwin.getTags().size());
        com.microsoft.azure.sdk.iot.service.devicetwin.Pair actualTags =
            testInstance.deviceUnderTest.sCDeviceForTwin.getTags().iterator().next();

        assertEquals(tagKey, actualTags.getKey());
        assertEquals(tagValue, actualTags.getValue());

        // Test replacing the old desired properties and tags with a new set of desired properties and tags
        desiredProperties.clear();
        tags.clear();

        desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(propertyUpdateKey, propertyUpdateValue));
        tags.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(tagUpdateKey, tagUpdateValue));
        testInstance.deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
        testInstance.deviceUnderTest.sCDeviceForTwin.setTags(tags);
        testInstance.deviceUnderTest.sCDeviceForTwin = testInstance.twinServiceClient.replaceTwin(testInstance.deviceUnderTest.sCDeviceForTwin);

        // Check that the twin's desired properties consist only of the updated values. If replace works as expected, then the old values
        // should be gone entirely
        testInstance.twinServiceClient.getTwin(testInstance.deviceUnderTest.sCDeviceForTwin);
        assertEquals(1, testInstance.deviceUnderTest.sCDeviceForTwin.getDesiredProperties().size());
        actualDesiredProperty = testInstance.deviceUnderTest.sCDeviceForTwin.getDesiredProperties().iterator().next();

        assertEquals(propertyUpdateKey, actualDesiredProperty.getKey());
        assertEquals(propertyUpdateValue, actualDesiredProperty.getValue());

        // Check that the twin's tags consist only of the updated values. If replace works as expected, then the old values
        // should be gone entirely
        assertEquals(1, testInstance.deviceUnderTest.sCDeviceForTwin.getTags().size());
        actualTags = testInstance.deviceUnderTest.sCDeviceForTwin.getTags().iterator().next();

        assertEquals(tagUpdateKey, actualTags.getKey());
        assertEquals(tagUpdateValue, actualTags.getValue());
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

    public void testSubscribeToDesiredPropertiesWithVersionFlow(Object propertyValue, Object updatePropertyValue, String updatePropertyPrefix) throws IOException, InterruptedException, IotHubException
    {
        // arrange
        testInstance.deviceUnderTest.sCDeviceForTwin.clearTwin();
        testInstance.deviceUnderTest.dCDeviceForTwin.getDesiredProp().clear();
        Map<Property, Pair<TwinPropertyCallback, Object>> desiredPropertiesCB = new HashMap<>();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            PropertyState propertyState = new PropertyState();
            propertyState.callBackTriggered = false;
            propertyState.propertyNewVersion = -1;
            propertyState.property = new Property(PROPERTY_KEY + i, propertyValue);
            testInstance.deviceUnderTest.dCDeviceForTwin.propertyStateList[i] = propertyState;
            desiredPropertiesCB.put(propertyState.property, new com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair<>(testInstance.deviceUnderTest.dCOnProperty, propertyState));
        }

        // act
        testInstance.testIdentity.getClient().subscribeToTwinDesiredPropertiesAsync(desiredPropertiesCB);
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = new HashSet<>();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(PROPERTY_KEY + i, updatePropertyValue));
        }
        testInstance.deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
        testInstance.twinServiceClient.updateTwin(testInstance.deviceUnderTest.sCDeviceForTwin);

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(updatePropertyPrefix, true);
    }

    @Test
    @StandardTierHubOnlyTest
    public void testSubscribeToDesiredPropertiesSequentially() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        super.setUpNewDeviceAndModule();
        testSubscribeToDesiredPropertiesSequentiallyFlow(PROPERTY_VALUE, PROPERTY_VALUE_UPDATE, PROPERTY_VALUE_UPDATE);
    }

    public void testSubscribeToDesiredPropertiesSequentiallyFlow(
            Object propertyValue,
            Object updatePropertyValue,
            String updatePropertyPrefix) throws IOException, InterruptedException, IotHubException
    {
        // arrange
        testInstance.deviceUnderTest.sCDeviceForTwin.clearTwin();
        testInstance.deviceUnderTest.dCDeviceForTwin.getDesiredProp().clear();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            PropertyState propertyState = new PropertyState();
            propertyState.callBackTriggered = false;
            propertyState.property = new Property(PROPERTY_KEY + i, propertyValue);
            testInstance.deviceUnderTest.dCDeviceForTwin.propertyStateList[i] = propertyState;
            testInstance.deviceUnderTest.dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, testInstance.deviceUnderTest.dCDeviceForTwin, propertyState);
        }

        // act
        testInstance.testIdentity.getClient().subscribeToDesiredPropertiesAsync(testInstance.deviceUnderTest.dCDeviceForTwin.getDesiredProp());
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(PROPERTY_KEY + i, updatePropertyValue));
            testInstance.deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
            testInstance.twinServiceClient.updateTwin(testInstance.deviceUnderTest.sCDeviceForTwin);
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
        testInstance.deviceUnderTest.sCDeviceForTwin.clearTwin();
        testInstance.deviceUnderTest.dCDeviceForTwin.getDesiredProp().clear();
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(PROPERTY_KEY + i, PROPERTY_VALUE + i));
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            testInstance.twinServiceClient.updateTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            testInstance.devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Update desired properties on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = testInstance.devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties();
            for (com.microsoft.azure.sdk.iot.service.devicetwin.Pair dp : desiredProperties)
            {
                dp.setValue(PROPERTY_VALUE_UPDATE + i);
            }
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            testInstance.twinServiceClient.updateTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            testInstance.devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);

            for (com.microsoft.azure.sdk.iot.service.devicetwin.Pair dp : testInstance.devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties())
            {
                Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Unexpected desired property key, expected " + PROPERTY_KEY + i + " but was " + dp.getKey(), testInstance.testIdentity.getClient()), PROPERTY_KEY + i, dp.getKey());
                Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Unexpected desired property value, expected " + PROPERTY_VALUE_UPDATE + i + " but was " + dp.getValue(), testInstance.testIdentity.getClient()), PROPERTY_VALUE_UPDATE + i, dp.getValue());
            }
            Integer version = testInstance.devicesUnderTest[i].sCDeviceForTwin.getDesiredPropertiesVersion();
            assertNotNull(CorrelationDetailsLoggingAssert.buildExceptionMessage("Version was null", testInstance.testIdentity.getClient()), version);
        }

        // Remove desired properties
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = testInstance.devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties();
            for (com.microsoft.azure.sdk.iot.service.devicetwin.Pair dp : desiredProperties)
            {
                dp.setValue(null);
            }
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            testInstance.twinServiceClient.updateTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            testInstance.devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);

            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Desired properties were not deleted by setting to null", testInstance.testIdentity.getClient()), 0, testInstance.devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties().size());
        }
    }

    @AllArgsConstructor
    static class TwinPropertiesCallbackImpl implements TwinPropertiesCallback
    {
        TwinCollection expectedProperties;

        @Override
        public void onTwinPropertiesChanged(TwinCollection actualProperties, Object context) {
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

    // This test is for the startTwinAsync/startTwinAsync API that takes the onTwinPropertyChanged rather than the TwinPropertyCallback
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
        expectedDesiredProperties.put(expectedKey1, expectedValue1);
        expectedDesiredProperties.put(expectedKey2, expectedValue2);
        TwinPropertiesCallback twinPropertiesCallback = new TwinPropertiesCallbackImpl(expectedDesiredProperties);

        Success desiredPropertiesCallbackState = new Success();

        testInstance.testIdentity.getClient().open();
        testInstance.testIdentity.getClient().startTwinAsync(new DeviceTwinStatusCallBack(), testInstance.deviceUnderTest, twinPropertiesCallback, desiredPropertiesCallbackState);

        long startTime = System.currentTimeMillis();
        while (testInstance.deviceUnderTest.deviceTwinStatus != IotHubStatusCode.OK)
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
            serviceClientTwin = new DeviceTwinDevice(testInstance.testIdentity.getClient().getConfig().getDeviceId());
        }
        else
        {
            serviceClientTwin = new DeviceTwinDevice(testInstance.testIdentity.getClient().getConfig().getDeviceId(), testInstance.testIdentity.getClient().getConfig().getModuleId());
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
