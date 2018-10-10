/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.iothubservices;

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
import com.microsoft.azure.sdk.iot.common.DeviceConnectionString;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeviceTwinWithVersionCommon extends MethodNameLoggingIntegrationTest
{
    private static final long BREATHE_TIME = 100; // 0.1 sec
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 1000; // 1 sec
    private static final long MAX_MILLISECS_TIMEOUT_KILL_TEST = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB + 50000; // 50 secs
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

    private static void assertSetEquals(Set<Property> expected, Set<Pair> actual)
    {
        assertEquals(expected.size(), actual.size());
        for(Pair actualProperty: actual)
        {
            Property expectedProperty = fetchProperty(expected, actualProperty.getKey());
            assertNotNull("Expected Set of Properties do no contain " + actualProperty.getKey(), expectedProperty);
            assertEquals(expectedProperty.getValue(), actualProperty.getValue());
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

    public static void setUp() throws IOException
    {
        // Create a register manager
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        // Create the service client
        sCDeviceTwin = DeviceTwin.createFromConnectionString(iotHubConnectionString);
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        // Destroy the service client
        sCDeviceTwin = null;

        // Remove device from IoTHub
        if (registryManager != null)
        {
            registryManager.close();
            registryManager = null;
        }
    }

    private void createDevice(IotHubClientProtocol protocol) throws IOException, URISyntaxException
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
        deviceForRegistryManager = registryManager.addDevice(deviceForRegistryManager);

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

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testSendReportedPropertiesWithoutVersionMqttSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.MQTT);
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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithVersionMqttSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.MQTT);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
            while(!testDevice.expectedProperties.isEmpty())
            {
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
        assertEquals(3, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(newValues, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithLowerVersionMqttFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.MQTT);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithHigherVersionMqttFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.MQTT);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testSendReportedPropertiesWithoutVersionMqttWSSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.MQTT_WS);
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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithVersionMqttWSSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.MQTT_WS);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
            while(!testDevice.expectedProperties.isEmpty())
            {
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
        assertEquals(3, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(newValues, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithLowerVersionMqttWSFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.MQTT_WS);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithHigherVersionMqttWSFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.MQTT_WS);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testSendReportedPropertiesWithoutVersionAmqpSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.AMQPS);
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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithVersionAmqpSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.AMQPS);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
            while(!testDevice.expectedProperties.isEmpty())
            {
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
        assertEquals(3, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(newValues, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithLowerVersionAmqpFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.AMQPS);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithHigherVersionAmqpFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.AMQPS);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testSendReportedPropertiesWithoutVersionAmqpWSSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.AMQPS_WS);
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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithVersionAmqpWSSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.AMQPS_WS);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
            while(!testDevice.expectedProperties.isEmpty())
            {
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
        assertEquals(3, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(newValues, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithLowerVersionAmqpWSFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.AMQPS_WS);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesWithHigherVersionAmqpWSFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(IotHubClientProtocol.AMQPS_WS);
        testDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testDevice.deviceClient.sendReportedProperties(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testDevice.deviceClient.getDeviceTwin();
        while(!testDevice.expectedProperties.isEmpty())
        {
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
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

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
        while(!testDevice.expectedProperties.isEmpty())
        {
            Thread.sleep(BREATHE_TIME);
            if(testDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testDevice.exception);
            }
        }
        assertEquals(2, (int)testDevice.reportedPropertyVersion);

        // test service client
        DeviceTwinDevice deviceOnServiceClient = new DeviceTwinDevice(testDevice.deviceId);
        sCDeviceTwin.getTwin(deviceOnServiceClient);
        assertEquals(2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }
}
