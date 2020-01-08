/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothub.twin;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static org.junit.Assert.*;

/**
 * Test class containing all tests to be run on JVM and android pertaining to twin with version. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class DeviceTwinWithVersionTests extends IotHubIntegrationTest
{
    private static final long BREATHE_TIME = 100; // 0.1 sec
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 1000; // 1 sec
    private static final long EXPECTED_PROPERTIES_MAX_WAIT_MS = 60 * 1000; //1 minute
    protected static String iotHubConnectionString = "";

    private static final String PROPERTY_KEY_1 = "Key1";
    private static final String PROPERTY_VALUE_1 = "Value1";
    private static final String PROPERTY_KEY_2 = "Key2";
    private static final String PROPERTY_VALUE_2 = "Value2";

    private static final Set<Property> PROPERTIES = new HashSet<Property>()
    {
        {
            add(new Property(PROPERTY_KEY_1, PROPERTY_VALUE_1));
            add(new Property(PROPERTY_KEY_2, PROPERTY_VALUE_2));
        }
    };

    private static RegistryManager registryManager;

    private enum STATUS
    {
        SUCCESS,
        IOTHUB_FAILURE,
        BAD_ANSWER,
        UNKNOWN
    }

    private static class TestDevice
    {
        String deviceId;
        DeviceClient deviceClient;
        STATUS deviceTwinStatus;
        Throwable exception;
        Set<Property> expectedProperties;
        Set<Property> receivedProperties;
        Integer reportedPropertyVersion;
    }

    private void assertSetEquals(Set<Property> expected, Set<Pair> actual)
    {
        assertEquals(buildExceptionMessage("Expected size " + expected.size() + " but was size " + actual.size(), testInstance.testDevice.deviceClient), expected.size(), actual.size());
        for(Pair actualProperty: actual)
        {
            Property expectedProperty = fetchProperty(expected, actualProperty.getKey());
            assertNotNull(buildExceptionMessage("Expected Set of Properties to not contain " + actualProperty.getKey(), testInstance.testDevice.deviceClient), expectedProperty);
            assertEquals(buildExceptionMessage("Expected value " + expectedProperty.getValue() + " but got " + actualProperty.getValue(), testInstance.testDevice.deviceClient), expectedProperty.getValue(), actualProperty.getValue());
        }
    }

    private static Property fetchProperty(Set<Property> expected, String key)
    {
        for(Property property: expected)
        {
            if(property.getKey().equals(key))
            {
                return property;
            }
        }
        return null;
    }

    private static class DeviceTwinPropertyCallback implements TwinPropertyCallBack
    {
        @Override
        public void TwinPropertyCallBack(Property property, Object context)
        {
            TestDevice state = (TestDevice) context;
            state.receivedProperties.add(property);
            try
            {
                if(property.getIsReported())
                {
                    state.reportedPropertyVersion = property.getVersion();
                    Property toRemove = null;
                    for (Property entry: state.expectedProperties)
                    {
                        if(entry.getKey().equals(property.getKey()) && entry.getValue().equals(property.getValue()))
                        {
                            toRemove = entry;
                            break;
                        }
                    }
                    state.expectedProperties.remove(toRemove);
                }
            }
            catch (Exception e)
            {
                state.exception = e;
                state.deviceTwinStatus = STATUS.BAD_ANSWER;
            }
        }
    }

    protected class DeviceTwinStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            TestDevice state = (TestDevice) context;

            //On failure, Don't update status any further
            if ((status == OK || status == OK_EMPTY) && state.deviceTwinStatus != STATUS.IOTHUB_FAILURE)
            {
                state.deviceTwinStatus = STATUS.SUCCESS;
            }
            else
            {
                state.deviceTwinStatus = STATUS.IOTHUB_FAILURE;
            }
        }
    }

    public static Collection inputsCommon() throws IOException
    {
        // Create a register manager
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        List inputs =  Arrays.asList(
                    new Object[][]
                            {
                                    {AMQPS},
                                    {AMQPS_WS},
                                    {MQTT},
                                    {MQTT_WS},
                            }
            );

        return inputs;
    }

    public DeviceTwinWithVersionTestInstance testInstance;


    public DeviceTwinWithVersionTests(IotHubClientProtocol protocol) throws IOException
    {
        this.testInstance = new DeviceTwinWithVersionTestInstance(protocol);
    }

    public class DeviceTwinWithVersionTestInstance
    {
        public IotHubClientProtocol protocol;
        private com.microsoft.azure.sdk.iot.service.Device deviceForRegistryManager;

        private DeviceTwin sCDeviceTwin;
        private TestDevice testDevice;

        public DeviceTwinWithVersionTestInstance(IotHubClientProtocol protocol) throws IOException
        {
            this.protocol = protocol;

            this.sCDeviceTwin = DeviceTwin.createFromConnectionString(iotHubConnectionString);
            this.testDevice = new TestDevice();
        }
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        if (registryManager != null)
        {
            registryManager.close();
            registryManager = null;
        }
    }

    private void createDevice(IotHubClientProtocol protocol) throws IOException, URISyntaxException, InterruptedException
    {
        testInstance.testDevice.deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, testInstance.deviceForRegistryManager), protocol);
        testInstance.testDevice.deviceClient.open();
        testInstance.testDevice.deviceClient.startDeviceTwin(new DeviceTwinStatusCallBack(), testInstance.testDevice, new DeviceTwinPropertyCallback(), testInstance.testDevice);
    }

    @Before
    public void createDevice() throws Exception
    {
        testInstance.testDevice = new TestDevice();
        testInstance.testDevice.deviceId = "java-twin-version-e2e-test-".concat(UUID.randomUUID().toString());
        testInstance.testDevice.receivedProperties = new HashSet<>();

        testInstance.deviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(testInstance.testDevice.deviceId, null, null);
        testInstance.deviceForRegistryManager = Tools.addDeviceWithRetry(registryManager, testInstance.deviceForRegistryManager);

    }

    @After
    public void destroyDevice() throws Exception
    {
        if (testInstance.testDevice.deviceClient != null)
        {
            testInstance.testDevice.deviceClient.closeNow();
            testInstance.testDevice.deviceClient = null;
        }

        if (testInstance != null && testInstance.testDevice != null)
        {
            testInstance.testDevice.expectedProperties = null;
            testInstance.testDevice.reportedPropertyVersion = null;
            testInstance.testDevice.receivedProperties = null;
            testInstance.testDevice.deviceTwinStatus = STATUS.UNKNOWN;

            if (registryManager != null && testInstance.testDevice.deviceId != null)
            {
                registryManager.removeDevice(testInstance.testDevice.deviceId);
            }
        }
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSendReportedPropertiesWithoutVersionSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testInstance.testDevice.expectedProperties = new HashSet<>(PROPERTIES);
        testInstance.testDevice.deviceTwinStatus = STATUS.UNKNOWN;

        // act
        testInstance.testDevice.deviceClient.sendReportedProperties(PROPERTIES);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while(testInstance.testDevice.deviceTwinStatus == STATUS.UNKNOWN)
        {
            Thread.sleep(BREATHE_TIME);
        }

        testInstance.testDevice.deviceClient.getDeviceTwin();
        long startTime = System.currentTimeMillis();
        while(!testInstance.testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.testDevice.reportedPropertyVersion, testInstance.testDevice.deviceClient), 2, (int)testInstance.testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testInstance.testDevice.deviceId);
        testInstance.sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(buildExceptionMessage("Expected reported properties version 2 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testInstance.testDevice.deviceClient), 2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testUpdateReportedPropertiesWithVersionSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testInstance.testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testInstance.testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testInstance.testDevice.deviceClient.getDeviceTwin();
        long startTime = System.currentTimeMillis();
        while(!testInstance.testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.testDevice.deviceTwinStatus == STATUS.IOTHUB_FAILURE)
            {
                throw new IOException("IoTHub send Http error code");
            }
            if(testInstance.testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.testDevice.reportedPropertyVersion, testInstance.testDevice.deviceClient), 2, (int)testInstance.testDevice.reportedPropertyVersion);

        // New values for the reported properties
        final Set<Property> newValues = new HashSet<Property>()
        {
            {
                add(new Property(PROPERTY_KEY_1, "newValue1"));
                add(new Property(PROPERTY_KEY_2, "newValue2"));
            }
        };
        testInstance.testDevice.expectedProperties = new HashSet<>(newValues);
        testInstance.testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.testDevice.reportedPropertyVersion = null;
        testInstance.testDevice.receivedProperties = new HashSet<>();

        // act
        testInstance.testDevice.deviceClient.sendReportedProperties(newValues, 2);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while(testInstance.testDevice.deviceTwinStatus == STATUS.UNKNOWN)
        {
            Thread.sleep(BREATHE_TIME);
        }

        do {
            Thread.sleep(BREATHE_TIME);
            testInstance.testDevice.expectedProperties = new HashSet<>(newValues);
            testInstance.testDevice.deviceTwinStatus = STATUS.UNKNOWN;
            testInstance.testDevice.reportedPropertyVersion = null;
            testInstance.testDevice.receivedProperties = new HashSet<>();
            testInstance.testDevice.deviceClient.getDeviceTwin();
            startTime = System.currentTimeMillis();
            while(!testInstance.testDevice.expectedProperties.isEmpty())
            {
                if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
                {
                    fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.testDevice.deviceClient));
                }

                Thread.sleep(BREATHE_TIME);
                if(testInstance.testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
                {
                    throw new IOException(testInstance.testDevice.exception);
                }
            }
        }while (testInstance.testDevice.reportedPropertyVersion != 3);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testInstance.testDevice.deviceId);
        testInstance.sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(buildExceptionMessage("Expected reported properties version 3 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testInstance.testDevice.deviceClient), 3, (int)deviceOnServiceClient.getReportedPropertiesVersion());        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(newValues, reported);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testUpdateReportedPropertiesWithLowerVersionFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testInstance.testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testInstance.testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testInstance.testDevice.deviceClient.getDeviceTwin();
        long startTime = System.currentTimeMillis();
        while(!testInstance.testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.testDevice.deviceTwinStatus == STATUS.IOTHUB_FAILURE)
            {
                throw new IOException("IoTHub send Http error code");
            }
            if(testInstance.testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.testDevice.reportedPropertyVersion, testInstance.testDevice.deviceClient), 2, (int)testInstance.testDevice.reportedPropertyVersion);

        // New values for the reported properties
        final Set<Property> newValues = new HashSet<Property>()
        {
            {
                add(new Property(PROPERTY_KEY_1, "newValue1"));
                add(new Property(PROPERTY_KEY_2, "newValue2"));
            }
        };
        testInstance.testDevice.expectedProperties = new HashSet<>(newValues);
        testInstance.testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.testDevice.reportedPropertyVersion = null;
        testInstance.testDevice.receivedProperties = new HashSet<>();

        // act
        testInstance.testDevice.deviceClient.sendReportedProperties(newValues, 1);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while((testInstance.testDevice.deviceTwinStatus != STATUS.BAD_ANSWER) && (testInstance.testDevice.deviceTwinStatus != STATUS.IOTHUB_FAILURE))
        {
            Thread.sleep(BREATHE_TIME);
        }
        testInstance.testDevice.expectedProperties = new HashSet<>(PROPERTIES);
        testInstance.testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.testDevice.reportedPropertyVersion = null;
        testInstance.testDevice.receivedProperties = new HashSet<>();
        testInstance.testDevice.deviceClient.getDeviceTwin();
        startTime = System.currentTimeMillis();
        while(!testInstance.testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.testDevice.reportedPropertyVersion, testInstance.testDevice.deviceClient), 2, (int)testInstance.testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testInstance.testDevice.deviceId);
        testInstance.sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(buildExceptionMessage("Expected reported properties version 2 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testInstance.testDevice.deviceClient), 2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testUpdateReportedPropertiesWithHigherVersionFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testInstance.testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testInstance.testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testInstance.testDevice.deviceClient.getDeviceTwin();
        long startTime = System.currentTimeMillis();
        while(!testInstance.testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.testDevice.deviceTwinStatus == STATUS.IOTHUB_FAILURE)
            {
                throw new IOException("IoTHub send Http error code");
            }
            if(testInstance.testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.testDevice.reportedPropertyVersion, testInstance.testDevice.deviceClient), 2, (int)testInstance.testDevice.reportedPropertyVersion);

        // New values for the reported properties
        final Set<Property> newValues = new HashSet<Property>()
        {
            {
                add(new Property(PROPERTY_KEY_1, "newValue1"));
                add(new Property(PROPERTY_KEY_2, "newValue2"));
            }
        };
        testInstance.testDevice.expectedProperties = new HashSet<>(newValues);
        testInstance.testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.testDevice.reportedPropertyVersion = null;
        testInstance.testDevice.receivedProperties = new HashSet<>();

        // act
        testInstance.testDevice.deviceClient.sendReportedProperties(newValues, 3);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while((testInstance.testDevice.deviceTwinStatus != STATUS.BAD_ANSWER) && (testInstance.testDevice.deviceTwinStatus != STATUS.IOTHUB_FAILURE))
        {
            Thread.sleep(BREATHE_TIME);
        }
        testInstance.testDevice.expectedProperties = new HashSet<>(PROPERTIES);
        testInstance.testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.testDevice.reportedPropertyVersion = null;
        testInstance.testDevice.receivedProperties = new HashSet<>();
        testInstance.testDevice.deviceClient.getDeviceTwin();
        startTime = System.currentTimeMillis();
        while(!testInstance.testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.testDevice.reportedPropertyVersion, testInstance.testDevice.deviceClient), 2, (int)testInstance.testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testInstance.testDevice.deviceId);
        testInstance.sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(buildExceptionMessage("Expected reported properties version 2 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testInstance.testDevice.deviceClient), 2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }
}
