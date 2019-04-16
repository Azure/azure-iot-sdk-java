/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.twin;

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
public class DeviceTwinWithVersionTests extends IntegrationTest
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
    private static com.microsoft.azure.sdk.iot.service.Device deviceForRegistryManager;

    private static DeviceTwin sCDeviceTwin;
    private static TestDevice testDevice;

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
        assertEquals(buildExceptionMessage("Expected size " + expected.size() + " but was size " + actual.size(), testDevice.deviceClient), expected.size(), actual.size());
        for(Pair actualProperty: actual)
        {
            Property expectedProperty = fetchProperty(expected, actualProperty.getKey());
            assertNotNull(buildExceptionMessage("Expected Set of Properties to not contain " + actualProperty.getKey(), testDevice.deviceClient), expectedProperty);
            assertEquals(buildExceptionMessage("Expected value " + expectedProperty.getValue() + " but got " + actualProperty.getValue(), testDevice.deviceClient), expectedProperty.getValue(), actualProperty.getValue());
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

        // Create the service client
        sCDeviceTwin = DeviceTwin.createFromConnectionString(iotHubConnectionString);

        List inputs =  Arrays.asList(
                    new Object[][]
                            {
                                    //sas token, device client
                                    {AMQPS},
                                    {AMQPS_WS},
                                    {MQTT},
                                    {MQTT_WS},
                            }
            );

        return inputs;
    }

    public DeviceTwinWithVersionTestInstance testInstance;


    public DeviceTwinWithVersionTests(IotHubClientProtocol protocol)
    {
        this.testInstance = new DeviceTwinWithVersionTestInstance(protocol);
    }

    public class DeviceTwinWithVersionTestInstance
    {
        public IotHubClientProtocol protocol;

        public DeviceTwinWithVersionTestInstance(IotHubClientProtocol protocol)
        {
            this.protocol = protocol;
        }
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        // Destroy the service client
        sCDeviceTwin = null;

        if (registryManager != null)
        {
            registryManager.close();
            registryManager = null;
        }
    }

    private void createDevice(IotHubClientProtocol protocol) throws IOException, URISyntaxException, InterruptedException
    {
        testDevice.deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceForRegistryManager), protocol);
        IotHubServicesCommon.openClientWithRetry(testDevice.deviceClient);
        testDevice.deviceClient.startDeviceTwin(new DeviceTwinStatusCallBack(), testDevice, new DeviceTwinPropertyCallback(), testDevice);
    }

    @Before
    public void createDevice() throws Exception
    {
        testDevice = new TestDevice();
        testDevice.deviceId = "java-twin-version-e2e-test-".concat(UUID.randomUUID().toString());
        testDevice.receivedProperties = new HashSet<>();

        deviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(testDevice.deviceId, null, null);
        deviceForRegistryManager = Tools.addDeviceWithRetry(registryManager, deviceForRegistryManager);

    }

    @After
    public void destroyDevice() throws Exception
    {
        testDevice.deviceClient.closeNow();
        testDevice.deviceClient = null;
        testDevice.expectedProperties = null;
        testDevice.reportedPropertyVersion = null;
        testDevice.receivedProperties = null;
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;

        registryManager.removeDevice(testDevice.deviceId);
        testDevice = null;
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testSendReportedPropertiesWithoutVersionSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;

        // act
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while(testDevice.deviceTwinStatus == STATUS.UNKNOWN)
        {
            Thread.sleep(BREATHE_TIME);
        }

        testDevice.deviceClient.getDeviceTwin();
        long startTime = System.currentTimeMillis();
        while(!testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testDevice.reportedPropertyVersion, testDevice.deviceClient), 2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(buildExceptionMessage("Expected reported properties version 2 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testDevice.deviceClient), 2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testUpdateReportedPropertiesWithVersionSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        long startTime = System.currentTimeMillis();
        while(!testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.IOTHUB_FAILURE)
            {
                throw new IOException("IoTHub send Http error code");
            }
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testDevice.reportedPropertyVersion, testDevice.deviceClient), 2, (int)testDevice.reportedPropertyVersion);

        // New values for the reported properties
        final Set<Property> newValues = new HashSet<Property>()
        {
            {
                add(new Property(PROPERTY_KEY_1, "newValue1"));
                add(new Property(PROPERTY_KEY_2, "newValue2"));
            }
        };
        testDevice.expectedProperties = new HashSet<>(newValues);
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.reportedPropertyVersion = null;
        testDevice.receivedProperties = new HashSet<>();

        // act
        testDevice.deviceClient.sendReportedProperties(newValues, 2);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while(testDevice.deviceTwinStatus == STATUS.UNKNOWN)
        {
            Thread.sleep(BREATHE_TIME);
        }

        do {
            Thread.sleep(BREATHE_TIME);
            testDevice.expectedProperties = new HashSet<>(newValues);
            testDevice.deviceTwinStatus = STATUS.UNKNOWN;
            testDevice.reportedPropertyVersion = null;
            testDevice.receivedProperties = new HashSet<>();
            testDevice.deviceClient.getDeviceTwin();
            startTime = System.currentTimeMillis();
            while(!testDevice.expectedProperties.isEmpty())
            {
                if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
                {
                    fail(buildExceptionMessage("Timed out waiting for expected property change", testDevice.deviceClient));
                }

                Thread.sleep(BREATHE_TIME);
                if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
                {
                    throw new IOException(testDevice.exception);
                }
            }
        }while (testDevice.reportedPropertyVersion != 3);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(buildExceptionMessage("Expected reported properties version 3 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testDevice.deviceClient), 3, (int)deviceOnServiceClient.getReportedPropertiesVersion());        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(newValues, reported);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testUpdateReportedPropertiesWithLowerVersionFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        long startTime = System.currentTimeMillis();
        while(!testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.IOTHUB_FAILURE)
            {
                throw new IOException("IoTHub send Http error code");
            }
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testDevice.reportedPropertyVersion, testDevice.deviceClient), 2, (int)testDevice.reportedPropertyVersion);

        // New values for the reported properties
        final Set<Property> newValues = new HashSet<Property>()
        {
            {
                add(new Property(PROPERTY_KEY_1, "newValue1"));
                add(new Property(PROPERTY_KEY_2, "newValue2"));
            }
        };
        testDevice.expectedProperties = new HashSet<>(newValues);
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.reportedPropertyVersion = null;
        testDevice.receivedProperties = new HashSet<>();

        // act
        testDevice.deviceClient.sendReportedProperties(newValues, 1);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while((testDevice.deviceTwinStatus != STATUS.BAD_ANSWER) && (testDevice.deviceTwinStatus != STATUS.IOTHUB_FAILURE))
        {
            Thread.sleep(BREATHE_TIME);
        }
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.reportedPropertyVersion = null;
        testDevice.receivedProperties = new HashSet<>();
        testDevice.deviceClient.getDeviceTwin();
        startTime = System.currentTimeMillis();
        while(!testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testDevice.reportedPropertyVersion, testDevice.deviceClient), 2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(buildExceptionMessage("Expected reported properties version 2 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testDevice.deviceClient), 2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testUpdateReportedPropertiesWithHigherVersionFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        long startTime = System.currentTimeMillis();
        while(!testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.IOTHUB_FAILURE)
            {
                throw new IOException("IoTHub send Http error code");
            }
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testDevice.reportedPropertyVersion, testDevice.deviceClient), 2, (int)testDevice.reportedPropertyVersion);

        // New values for the reported properties
        final Set<Property> newValues = new HashSet<Property>()
        {
            {
                add(new Property(PROPERTY_KEY_1, "newValue1"));
                add(new Property(PROPERTY_KEY_2, "newValue2"));
            }
        };
        testDevice.expectedProperties = new HashSet<>(newValues);
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.reportedPropertyVersion = null;
        testDevice.receivedProperties = new HashSet<>();

        // act
        testDevice.deviceClient.sendReportedProperties(newValues, 3);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while((testDevice.deviceTwinStatus != STATUS.BAD_ANSWER) && (testDevice.deviceTwinStatus != STATUS.IOTHUB_FAILURE))
        {
            Thread.sleep(BREATHE_TIME);
        }
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.reportedPropertyVersion = null;
        testDevice.receivedProperties = new HashSet<>();
        testDevice.deviceClient.getDeviceTwin();
        startTime = System.currentTimeMillis();
        while(!testDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testDevice.reportedPropertyVersion, testDevice.deviceClient), 2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(buildExceptionMessage("Expected reported properties version 2 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testDevice.deviceClient), 2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }
}
