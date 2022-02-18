/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.twin;


import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.twin.Property;
import com.microsoft.azure.sdk.iot.device.twin.TwinPropertyCallback;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.twin.TwinClientOptions;
import com.microsoft.azure.sdk.iot.service.twin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceConnectionString;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestDeviceIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static org.junit.Assert.*;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/**
 * Test class containing all tests to be run on JVM and android pertaining to twin with version.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class TwinWithVersionTests extends IntegrationTest
{
    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs()
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        return Arrays.asList(
                new Object[][]
                        {
                                {AMQPS},
                                {AMQPS_WS},
                                {MQTT},
                                {MQTT_WS},
                        }
        );
    }

    private static final long BREATHE_TIME = 100; // 0.1 sec
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 1000; // 1 sec
    private static final long EXPECTED_PROPERTIES_MAX_WAIT_MILLISECONDS = 60 * 1000; //1 minute
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

    private enum STATUS
    {
        SUCCESS,
        IOTHUB_FAILURE,
        BAD_ANSWER,
        UNKNOWN
    }

    private static class DeviceTwinWithVersionTestDevice
    {
        String deviceId;
        DeviceClient deviceClient;
        STATUS deviceTwinStatus;
        Throwable exception;
        Set<Property> expectedProperties;
        Integer reportedPropertyVersion;
    }

    private void assertSetEquals(Set<Property> expected, Set<Pair> actual)
    {
        assertEquals(buildExceptionMessage("Expected size " + expected.size() + " but was size " + actual.size(), testInstance.deviceTwinWithVersionTestDevice.deviceClient), expected.size(), actual.size());
        for(Pair actualProperty: actual)
        {
            Property expectedProperty = fetchProperty(expected, actualProperty.getKey());
            assertNotNull(buildExceptionMessage("Expected Set of Properties to not contain " + actualProperty.getKey(), testInstance.deviceTwinWithVersionTestDevice.deviceClient), expectedProperty);
            assertEquals(buildExceptionMessage("Expected value " + expectedProperty.getValue() + " but got " + actualProperty.getValue(), testInstance.deviceTwinWithVersionTestDevice.deviceClient), expectedProperty.getValue(), actualProperty.getValue());
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

    private static class DeviceTwinPropertyCallback implements TwinPropertyCallback
    {
        @Override
        public void onPropertyChanged(Property property, Object context)
        {
            DeviceTwinWithVersionTestDevice state = (DeviceTwinWithVersionTestDevice) context;
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

    protected static class DeviceTwinStatusCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            DeviceTwinWithVersionTestDevice state = (DeviceTwinWithVersionTestDevice) context;

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

    public DeviceTwinWithVersionTestInstance testInstance;

    public TwinWithVersionTests(IotHubClientProtocol protocol) throws IOException
    {
        this.testInstance = new DeviceTwinWithVersionTestInstance(protocol);
    }

    public static class DeviceTwinWithVersionTestInstance
    {
        public IotHubClientProtocol protocol;
        private Device deviceForRegistryManager;
        public TestDeviceIdentity testDeviceIdentity;

        private final TwinClient twinServiceClient;
        private DeviceTwinWithVersionTestDevice deviceTwinWithVersionTestDevice;
        public RegistryClient registryClient;

        public DeviceTwinWithVersionTestInstance(IotHubClientProtocol protocol) throws IOException
        {
            this.protocol = protocol;

            this.twinServiceClient = new TwinClient(iotHubConnectionString, TwinClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
            this.registryClient = new RegistryClient(iotHubConnectionString, RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
            this.deviceTwinWithVersionTestDevice = new DeviceTwinWithVersionTestDevice();
        }
    }

    private void createDevice(IotHubClientProtocol protocol) throws IOException, URISyntaxException
    {
        testInstance.deviceTwinWithVersionTestDevice.deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, testInstance.deviceForRegistryManager), protocol);
        testInstance.deviceTwinWithVersionTestDevice.deviceClient.open(false);
        testInstance.deviceTwinWithVersionTestDevice.deviceClient.startTwinAsync(new DeviceTwinStatusCallback(), testInstance.deviceTwinWithVersionTestDevice, new DeviceTwinPropertyCallback(), testInstance.deviceTwinWithVersionTestDevice);
    }

    @Before
    public void createDevice() throws Exception
    {
        testInstance.deviceTwinWithVersionTestDevice = new DeviceTwinWithVersionTestDevice();

        testInstance.testDeviceIdentity = Tools.getTestDevice(iotHubConnectionString, testInstance.protocol, AuthenticationType.SAS, true);
        testInstance.deviceForRegistryManager = testInstance.testDeviceIdentity.getDevice();
        testInstance.deviceTwinWithVersionTestDevice.deviceId = testInstance.testDeviceIdentity.getDeviceId();
    }

    @After
    public void destroyDevice() throws Exception
    {
        if (testInstance.deviceTwinWithVersionTestDevice.deviceClient != null)
        {
            testInstance.deviceTwinWithVersionTestDevice.deviceClient.close();
            testInstance.deviceTwinWithVersionTestDevice.deviceClient = null;
        }

        Tools.disposeTestIdentity(testInstance.testDeviceIdentity, iotHubConnectionString);
    }

    @Test
    @StandardTierHubOnlyTest
    public void testSendReportedPropertiesWithoutVersionSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testInstance.deviceTwinWithVersionTestDevice.expectedProperties = new HashSet<>(PROPERTIES);
        testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus = STATUS.UNKNOWN;

        // act
        testInstance.deviceTwinWithVersionTestDevice.deviceClient.sendReportedPropertiesAsync(PROPERTIES);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.UNKNOWN)
        {
            Thread.sleep(BREATHE_TIME);
        }

        testInstance.deviceTwinWithVersionTestDevice.deviceClient.getTwinAsync();
        long startTime = System.currentTimeMillis();
        while(!testInstance.deviceTwinWithVersionTestDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MILLISECONDS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.deviceTwinWithVersionTestDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.deviceTwinWithVersionTestDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion, testInstance.deviceTwinWithVersionTestDevice.deviceClient), 2, (int)testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion);

        // test service client
        Twin deviceOnServiceClient = testInstance.twinServiceClient.get(testInstance.deviceTwinWithVersionTestDevice.deviceId);
        assertEquals(buildExceptionMessage("Expected reported properties version 2 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testInstance.deviceTwinWithVersionTestDevice.deviceClient), 2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test
    @StandardTierHubOnlyTest
    public void testUpdateReportedPropertiesWithVersionSucceed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testInstance.deviceTwinWithVersionTestDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.deviceTwinWithVersionTestDevice.deviceClient.sendReportedPropertiesAsync(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testInstance.deviceTwinWithVersionTestDevice.deviceClient.getTwinAsync();
        long startTime = System.currentTimeMillis();
        while(!testInstance.deviceTwinWithVersionTestDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MILLISECONDS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.deviceTwinWithVersionTestDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.IOTHUB_FAILURE)
            {
                throw new IOException("IoTHub send Http error code");
            }
            if(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.deviceTwinWithVersionTestDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion, testInstance.deviceTwinWithVersionTestDevice.deviceClient), 2, (int)testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion);

        // New values for the reported properties
        final Set<Property> newValues = new HashSet<Property>()
        {
            {
                add(new Property(PROPERTY_KEY_1, "newValue1"));
                add(new Property(PROPERTY_KEY_2, "newValue2"));
            }
        };
        testInstance.deviceTwinWithVersionTestDevice.expectedProperties = new HashSet<>(newValues);
        testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion = null;

        // act
        testInstance.deviceTwinWithVersionTestDevice.deviceClient.sendReportedPropertiesAsync(newValues, 2);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.UNKNOWN)
        {
            Thread.sleep(BREATHE_TIME);
        }

        do {
            Thread.sleep(BREATHE_TIME);
            testInstance.deviceTwinWithVersionTestDevice.expectedProperties = new HashSet<>(newValues);
            testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus = STATUS.UNKNOWN;
            testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion = null;
            testInstance.deviceTwinWithVersionTestDevice.deviceClient.getTwinAsync();
            startTime = System.currentTimeMillis();
            while(!testInstance.deviceTwinWithVersionTestDevice.expectedProperties.isEmpty())
            {
                if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MILLISECONDS)
                {
                    fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.deviceTwinWithVersionTestDevice.deviceClient));
                }

                Thread.sleep(BREATHE_TIME);
                if(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
                {
                    throw new IOException(testInstance.deviceTwinWithVersionTestDevice.exception);
                }
            }
        }while (testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion != 3);

        // test service client
        Twin deviceOnServiceClient = testInstance.twinServiceClient.get(testInstance.deviceTwinWithVersionTestDevice.deviceId);
        assertEquals(buildExceptionMessage("Expected reported properties version 3 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testInstance.deviceTwinWithVersionTestDevice.deviceClient), 3, (int)deviceOnServiceClient.getReportedPropertiesVersion());        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(newValues, reported);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void testUpdateReportedPropertiesWithLowerVersionFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testInstance.deviceTwinWithVersionTestDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.deviceTwinWithVersionTestDevice.deviceClient.sendReportedPropertiesAsync(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testInstance.deviceTwinWithVersionTestDevice.deviceClient.getTwinAsync();
        long startTime = System.currentTimeMillis();
        while(!testInstance.deviceTwinWithVersionTestDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MILLISECONDS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.deviceTwinWithVersionTestDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.IOTHUB_FAILURE)
            {
                throw new IOException("IoTHub send Http error code");
            }
            if(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.deviceTwinWithVersionTestDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion, testInstance.deviceTwinWithVersionTestDevice.deviceClient), 2, (int)testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion);

        // New values for the reported properties
        final Set<Property> newValues = new HashSet<Property>()
        {
            {
                add(new Property(PROPERTY_KEY_1, "newValue1"));
                add(new Property(PROPERTY_KEY_2, "newValue2"));
            }
        };
        testInstance.deviceTwinWithVersionTestDevice.expectedProperties = new HashSet<>(newValues);
        testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion = null;

        // act
        testInstance.deviceTwinWithVersionTestDevice.deviceClient.sendReportedPropertiesAsync(newValues, 1);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while((testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus != STATUS.BAD_ANSWER) && (testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus != STATUS.IOTHUB_FAILURE))
        {
            Thread.sleep(BREATHE_TIME);
        }
        testInstance.deviceTwinWithVersionTestDevice.expectedProperties = new HashSet<>(PROPERTIES);
        testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion = null;
        testInstance.deviceTwinWithVersionTestDevice.deviceClient.getTwinAsync();
        startTime = System.currentTimeMillis();
        while(!testInstance.deviceTwinWithVersionTestDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MILLISECONDS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.deviceTwinWithVersionTestDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.deviceTwinWithVersionTestDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion, testInstance.deviceTwinWithVersionTestDevice.deviceClient), 2, (int)testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion);

        // test service client
        Twin deviceOnServiceClient = testInstance.twinServiceClient.get(testInstance.deviceTwinWithVersionTestDevice.deviceId);
        assertEquals(buildExceptionMessage("Expected reported properties version 2 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testInstance.deviceTwinWithVersionTestDevice.deviceClient), 2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void testUpdateReportedPropertiesWithHigherVersionFailed() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        createDevice(testInstance.protocol);
        testInstance.deviceTwinWithVersionTestDevice.expectedProperties = new HashSet<>(PROPERTIES);

        // Create the first version of the reported properties.
        testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.deviceTwinWithVersionTestDevice.deviceClient.sendReportedPropertiesAsync(PROPERTIES);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        testInstance.deviceTwinWithVersionTestDevice.deviceClient.getTwinAsync();
        long startTime = System.currentTimeMillis();
        while(!testInstance.deviceTwinWithVersionTestDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MILLISECONDS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.deviceTwinWithVersionTestDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.IOTHUB_FAILURE)
            {
                throw new IOException("IoTHub send Http error code");
            }
            if(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.deviceTwinWithVersionTestDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion, testInstance.deviceTwinWithVersionTestDevice.deviceClient), 2, (int)testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion);

        // New values for the reported properties
        final Set<Property> newValues = new HashSet<Property>()
        {
            {
                add(new Property(PROPERTY_KEY_1, "newValue1"));
                add(new Property(PROPERTY_KEY_2, "newValue2"));
            }
        };
        testInstance.deviceTwinWithVersionTestDevice.expectedProperties = new HashSet<>(newValues);
        testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion = null;

        // act
        testInstance.deviceTwinWithVersionTestDevice.deviceClient.sendReportedPropertiesAsync(newValues, 3);

        // assert
        // test device client
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        while((testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus != STATUS.BAD_ANSWER) && (testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus != STATUS.IOTHUB_FAILURE))
        {
            Thread.sleep(BREATHE_TIME);
        }
        testInstance.deviceTwinWithVersionTestDevice.expectedProperties = new HashSet<>(PROPERTIES);
        testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus = STATUS.UNKNOWN;
        testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion = null;
        testInstance.deviceTwinWithVersionTestDevice.deviceClient.getTwinAsync();
        startTime = System.currentTimeMillis();
        while(!testInstance.deviceTwinWithVersionTestDevice.expectedProperties.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > EXPECTED_PROPERTIES_MAX_WAIT_MILLISECONDS)
            {
                fail(buildExceptionMessage("Timed out waiting for expected property change", testInstance.deviceTwinWithVersionTestDevice.deviceClient));
            }

            Thread.sleep(BREATHE_TIME);
            if(testInstance.deviceTwinWithVersionTestDevice.deviceTwinStatus == STATUS.BAD_ANSWER)
            {
                throw new IOException(testInstance.deviceTwinWithVersionTestDevice.exception);
            }
        }
        assertEquals(buildExceptionMessage("Expected 2, but reported properties version was " + testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion, testInstance.deviceTwinWithVersionTestDevice.deviceClient), 2, (int)testInstance.deviceTwinWithVersionTestDevice.reportedPropertyVersion);

        // test service client
        Twin deviceOnServiceClient = testInstance.twinServiceClient.get(testInstance.deviceTwinWithVersionTestDevice.deviceId);
        assertEquals(buildExceptionMessage("Expected reported properties version 2 but was " + deviceOnServiceClient.getReportedPropertiesVersion(), testInstance.deviceTwinWithVersionTestDevice.deviceClient), 2, (int)deviceOnServiceClient.getReportedPropertiesVersion());
        Set<Pair> reported = deviceOnServiceClient.getReportedProperties();
        assertSetEquals(PROPERTIES, reported);
    }
}