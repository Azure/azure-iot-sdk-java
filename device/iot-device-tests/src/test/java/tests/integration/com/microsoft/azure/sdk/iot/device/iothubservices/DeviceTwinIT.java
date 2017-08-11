/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.device.iothubservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Device;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.*;
import tests.integration.com.microsoft.azure.sdk.iot.device.DeviceConnectionString;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static org.junit.Assert.*;

public class DeviceTwinIT
{
    // Max time to wait to see it on Hub
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 200; // 0.2 sec

    //Max time to wait before timing out test
    private static final long MAX_MILLISECS_TIMEOUT_KILL_TEST = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB + 50000; // 50 secs

    // Max reported properties to be tested
    private static final Integer MAX_PROPERTIES_TO_TEST = 5;

    //Max devices to test
    private static final Integer MAX_DEVICES = 5;

    //Default Page Size for Query
    private static final Integer PAGE_SIZE = 2;

    private static final String iotHubonnectionStringEnvVarName = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";

    // Constants used in for Testing
    private static final String PROPERTY_KEY = "Key";
    private static final String PROPERTY_KEY_QUERY = "KeyQuery";
    private static final String PROPERTY_VALUE = "Value";
    private static final String PROPERTY_VALUE_QUERY = "ValueQuery";
    private static final String PROPERTY_VALUE_UPDATE = "Update";
    private static final String TAG_KEY = "Tag_Key";
    private static final String TAG_VALUE = "Tag_Value";
    private static final String TAG_VALUE_UPDATE = "Tag_Value_Update";

    // States of SDK
    private static RegistryManager registryManager;
    private static DeviceClient deviceClient;
    private static RawTwinQuery scRawTwinQueryClient;
    private static DeviceTwin sCDeviceTwin;
    private static DeviceState deviceUnderTest = null;
    private static DeviceState[] devicesUnderTest;

    private enum STATUS
    {
        SUCCESS, FAILURE
    }

    protected class DeviceTwinStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            DeviceState state = (DeviceState) context;

            //On failure, Don't update status any further
            if ((status == OK || status == OK_EMPTY ) && state.deviceTwinStatus != STATUS.FAILURE)
            {
                state.deviceTwinStatus = STATUS.SUCCESS;
            }
            else
            {
                state.deviceTwinStatus = STATUS.FAILURE;
            }
        }
    }

    class DeviceState
    {
        com.microsoft.azure.sdk.iot.service.Device sCDeviceForRegistryManager;
        DeviceTwinDevice sCDeviceForTwin;
        DeviceExtension dCDeviceForTwin;
        STATUS deviceTwinStatus;
    }

    class PropertyState
    {
        boolean callBackTriggered;
        Property property;
        Object propertyNewValue;
    }

    class DeviceExtension extends Device
    {
        List<PropertyState> propertyStateList = new LinkedList<>();

        @Override
        public void PropertyCall(String propertyKey, Object propertyValue, Object context)
        {
            PropertyState propertyState = (PropertyState) context;
            if (propertyKey.equals(propertyState.property.getKey()))
            {
                propertyState.callBackTriggered = true;
                propertyState.propertyNewValue = propertyValue;
            }
        }

        synchronized void createNewReportedProperties(int maximumPropertiesToCreate)
        {
            for( int i = 0; i < maximumPropertiesToCreate; i++)
            {
                UUID randomUUID = UUID.randomUUID();
                this.setReportedProp(new Property(PROPERTY_KEY + randomUUID, PROPERTY_VALUE + randomUUID));
            }
        }

        synchronized void updateAllExistingReportedProperties()
        {
            Set<Property> reportedProp = this.getReportedProp();

            for (Property p : reportedProp)
            {
                UUID randomUUID = UUID.randomUUID();
                p.setValue(PROPERTY_VALUE_UPDATE + randomUUID);
            }
        }

        synchronized void updateExistingReportedProperty(int index)
        {
            Set<Property> reportedProp = this.getReportedProp();
            int i = 0;
            for (Property p : reportedProp)
            {
                if (i == index)
                {
                    UUID randomUUID = UUID.randomUUID();
                    p.setValue(PROPERTY_VALUE_UPDATE + randomUUID);
                    break;
                }
                i++;
            }
        }
    }

    private void addMultipleDevices() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException
    {
        devicesUnderTest = new DeviceState[MAX_DEVICES];

        for (int i = 0 ; i < MAX_DEVICES; i++)
        {
            devicesUnderTest[i] = new DeviceState();
            String deviceIdMqtt = "java-device-twin-e2e-test-mqtt".concat(UUID.randomUUID().toString());
            devicesUnderTest[i].sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceIdMqtt, null, null);
            devicesUnderTest[i].sCDeviceForRegistryManager = registryManager.addDevice(devicesUnderTest[i].sCDeviceForRegistryManager);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
            setUpTwin(devicesUnderTest[i]);
        }
    }

    private void removeMultipleDevices() throws IOException, IotHubException, InterruptedException
    {
        for (int i = 0 ; i < MAX_DEVICES; i++)
        {
            tearDownTwin(devicesUnderTest[i]);
            registryManager.removeDevice(devicesUnderTest[i].sCDeviceForRegistryManager.getDeviceId());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        }
    }

    private void setUpTwin(DeviceState deviceState) throws IOException, URISyntaxException, IotHubException, InterruptedException
    {
        // set up twin on DeviceClient
        /*
            Because of the bug in device client on MQTT, we cannot have multiple device client open
            in the main thread at the same time. Hence restricting to use single device client.
         */
        if (deviceClient == null)
        {
            deviceState.dCDeviceForTwin = new DeviceExtension();
            deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceState.sCDeviceForRegistryManager), IotHubClientProtocol.MQTT);
            deviceClient.open();
            deviceClient.startDeviceTwin(new DeviceTwinStatusCallBack(), deviceState, deviceState.dCDeviceForTwin, deviceState);
            deviceState.deviceTwinStatus = STATUS.SUCCESS;
        }
        // set up twin on ServiceClient
        if (sCDeviceTwin != null)
        {
            deviceState.sCDeviceForTwin = new DeviceTwinDevice(deviceState.sCDeviceForRegistryManager.getDeviceId());
            sCDeviceTwin.getTwin(deviceState.sCDeviceForTwin);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        }
    }

    private void tearDownTwin(DeviceState deviceState) throws IOException
    {
        // tear down twin on device client
        if (deviceState.sCDeviceForTwin != null)
        {
            deviceState.sCDeviceForTwin.clearTwin();
        }
        if (deviceState.dCDeviceForTwin != null)
        {
            deviceState.dCDeviceForTwin.clean();
        }
        if (deviceClient != null)
        {
            deviceClient.closeNow();
            deviceClient = null;
        }
    }

    @BeforeClass
    public static void setUp() throws IOException
    {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet())
        {
            if (envName.equals(iotHubonnectionStringEnvVarName))
            {
                iotHubConnectionString = env.get(envName);
            }
        }

        assertNotNull(iotHubConnectionString);
        assertFalse(iotHubConnectionString.isEmpty());

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        sCDeviceTwin = DeviceTwin.createFromConnectionString(iotHubConnectionString);
        scRawTwinQueryClient = RawTwinQuery.createFromConnectionString(iotHubConnectionString);
    }

    @AfterClass
    public static void tearDown()
    {
        registryManager = null;
        sCDeviceTwin = null;
        deviceClient = null;
    }

    @Before
    public void setUpNewDevice() throws IOException , IotHubException, NoSuchAlgorithmException, URISyntaxException, InterruptedException
    {
        deviceUnderTest = new DeviceState();
        String deviceIdMqtt = "java-device-twin-e2e-test-mqtt".concat(UUID.randomUUID().toString());
        deviceUnderTest.sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceIdMqtt, null, null);
        deviceUnderTest.sCDeviceForRegistryManager = registryManager.addDevice(deviceUnderTest.sCDeviceForRegistryManager);
        setUpTwin(deviceUnderTest);
    }

    @After
    public void tearDownNewDevice() throws IOException , IotHubException
    {
        tearDownTwin(deviceUnderTest);
        registryManager.removeDevice(deviceUnderTest.sCDeviceForRegistryManager.getDeviceId());
    }

    private int readReportedProperties(DeviceState deviceState, String startsWithKey, String startsWithValue) throws IOException , IotHubException, InterruptedException
    {
        int totalCount = 0;
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        sCDeviceTwin.getTwin(deviceState.sCDeviceForTwin);
        Set<Pair> repProperties = deviceState.sCDeviceForTwin.getReportedProperties();

        for (Pair p : repProperties)
        {
            String val = (String) p.getValue();
            if (p.getKey().startsWith(startsWithKey) && val.startsWith(startsWithValue))
            {
                totalCount++;
            }
        }
        return totalCount;
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testSendReportedProperties() throws IOException, IotHubException, InterruptedException
    {
        // arrange

        // act
        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        deviceClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        // assert
        assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);

        // verify if they are received by SC
        int actualReportedPropFound = readReportedProperties(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE);
        assertEquals(MAX_PROPERTIES_TO_TEST.intValue(), actualReportedPropFound);
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
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
                        deviceClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
                    }
                    catch (IOException e)
                    {
                        assertTrue(e.getMessage(), true);
                    }
                    assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);
                }
            });
        }
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        // verify if they are received by SC
        int actualReportedPropFound = readReportedProperties(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE);
        assertEquals(MAX_PROPERTIES_TO_TEST.intValue(), actualReportedPropFound);
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testSendReportedPropertiesSequentially() throws IOException, InterruptedException, IotHubException
    {
        // arrange

        // send max_prop RP one at a time sequentially
        // verify if they are updated by SC
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(1);
            deviceClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
            assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);
        }

        int actualReportedPropFound = readReportedProperties(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE);
        assertEquals(MAX_PROPERTIES_TO_TEST.intValue(), actualReportedPropFound);

    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedProperties() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        deviceClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        // act
        // Update RP
        deviceUnderTest.dCDeviceForTwin.updateAllExistingReportedProperties();
        deviceClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        // assert
        assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);

        // verify if they are received by SC
        int actualReportedPropFound = readReportedProperties(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE_UPDATE);
        assertEquals(MAX_PROPERTIES_TO_TEST.intValue(), actualReportedPropFound);
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesMultiThreaded() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_PROPERTIES_TO_TEST);

        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        deviceClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

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
                        deviceClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
                    }
                    catch (IOException e)
                    {
                        assertTrue(e.getMessage(), true);
                    }
                    assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);
                }
            });
        }
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        // assert
        assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);

        // verify if they are received by SC
        int actualReportedPropFound = readReportedProperties(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE_UPDATE);
        assertEquals(MAX_PROPERTIES_TO_TEST.intValue(), actualReportedPropFound);
    }


    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateReportedPropertiesSequential() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        deviceClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        // act
        // Update RP
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            deviceUnderTest.dCDeviceForTwin.updateExistingReportedProperty(i);
            deviceClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());
        }
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        // assert
        assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);

        // verify if they are received by SC
        int actualReportedPropFound = readReportedProperties(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE_UPDATE);
        assertEquals(MAX_PROPERTIES_TO_TEST.intValue(), actualReportedPropFound);
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testSubscribeToDesiredProperties() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            PropertyState propertyState = new PropertyState();
            propertyState.callBackTriggered = false;
            propertyState.property = new Property(PROPERTY_KEY + i, PROPERTY_VALUE);
            deviceUnderTest.dCDeviceForTwin.propertyStateList.add(propertyState);
            deviceUnderTest.dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, deviceUnderTest.dCDeviceForTwin, propertyState);
        }

        // act
        deviceClient.subscribeToDesiredProperties(deviceUnderTest.dCDeviceForTwin.getDesiredProp());
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        Set<Pair> desiredProperties = new HashSet<>();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            desiredProperties.add(new Pair(PROPERTY_KEY + i, PROPERTY_VALUE_UPDATE + UUID.randomUUID()));
        }
        deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
        sCDeviceTwin.updateTwin(deviceUnderTest.sCDeviceForTwin);
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        // assert
        assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);
        for (PropertyState propertyState : deviceUnderTest.dCDeviceForTwin.propertyStateList)
        {
            assertTrue(propertyState.property.toString(), propertyState.callBackTriggered);
            assertTrue(((String) propertyState.propertyNewValue).startsWith(PROPERTY_VALUE_UPDATE));
            assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);
        }
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testSubscribeToDesiredPropertiesMultiThreaded() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_PROPERTIES_TO_TEST);

        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            PropertyState propertyState = new PropertyState();
            propertyState.callBackTriggered = false;
            propertyState.property = new Property(PROPERTY_KEY + i, PROPERTY_VALUE);
            deviceUnderTest.dCDeviceForTwin.propertyStateList.add(propertyState);
            deviceUnderTest.dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, deviceUnderTest.dCDeviceForTwin, propertyState);
        }

        // act
        deviceClient.subscribeToDesiredProperties(deviceUnderTest.dCDeviceForTwin.getDesiredProp());
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

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
                        Set<Pair> desiredProperties = new HashSet<>();
                        desiredProperties.add(new Pair(PROPERTY_KEY + index, PROPERTY_VALUE_UPDATE + UUID.randomUUID()));
                        deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
                        sCDeviceTwin.updateTwin(deviceUnderTest.sCDeviceForTwin);
                    }
                    catch (IotHubException | IOException e)
                    {
                        assertTrue(e.getMessage(), true);
                    }
                }
            });
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        // assert
        for (PropertyState propertyState : deviceUnderTest.dCDeviceForTwin.propertyStateList)
        {
            assertTrue(propertyState.property.toString(), propertyState.callBackTriggered);
            assertTrue(((String) propertyState.propertyNewValue).startsWith(PROPERTY_VALUE_UPDATE));
            assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);
        }
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testSubscribeToDesiredPropertiesSequentially() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            PropertyState propertyState = new PropertyState();
            propertyState.callBackTriggered = false;
            propertyState.property = new Property(PROPERTY_KEY + i, PROPERTY_VALUE);
            deviceUnderTest.dCDeviceForTwin.propertyStateList.add(propertyState);
            deviceUnderTest.dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, deviceUnderTest.dCDeviceForTwin, propertyState);
        }

        // act
        deviceClient.subscribeToDesiredProperties(deviceUnderTest.dCDeviceForTwin.getDesiredProp());
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(PROPERTY_KEY + i, PROPERTY_VALUE_UPDATE + UUID.randomUUID()));
            deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
            sCDeviceTwin.updateTwin(deviceUnderTest.sCDeviceForTwin);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        }

        // assert
        for (PropertyState propertyState : deviceUnderTest.dCDeviceForTwin.propertyStateList)
        {
            assertTrue(propertyState.property.toString(), propertyState.callBackTriggered);
            assertTrue(((String) propertyState.propertyNewValue).startsWith(PROPERTY_VALUE_UPDATE));
            assertEquals(deviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);
        }
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testAddTagUpdates() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException
    {
        addMultipleDevices();

        // Update tag for multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> tags = new HashSet<>();
            tags.add(new Pair(TAG_KEY + i, TAG_VALUE + i));
            devicesUnderTest[i].sCDeviceForTwin.setTags(tags);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

            for (Pair t : devicesUnderTest[i].sCDeviceForTwin.getTags())
            {
                assertEquals(t.getKey(), TAG_KEY + i);
                assertEquals(t.getValue(), TAG_VALUE + i);
            }
        }
        removeMultipleDevices();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateTagUpdates() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException
    {
        addMultipleDevices();

        // Add tag for multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> tags = new HashSet<>();
            tags.add(new Pair(TAG_KEY + i, TAG_VALUE + i));
            devicesUnderTest[i].sCDeviceForTwin.setTags(tags);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Update Tags on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Set<Pair> tags = devicesUnderTest[i].sCDeviceForTwin.getTags();
            for (Pair tag : tags)
            {
                tag.setValue(TAG_VALUE_UPDATE + i);
            }
            devicesUnderTest[i].sCDeviceForTwin.setTags(tags);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);

            for (Pair t : devicesUnderTest[i].sCDeviceForTwin.getTags())
            {
                assertEquals(t.getKey(), TAG_KEY + i);
                assertEquals(t.getValue(), TAG_VALUE_UPDATE + i);
            }
        }
        removeMultipleDevices();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateDesiredUpdates() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException
    {
        addMultipleDevices();

        // Add desired properties for multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(PROPERTY_KEY + i, PROPERTY_VALUE + i));
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Update desired properties on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Set<Pair> desiredProperties = devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties();
            for (Pair dp : desiredProperties)
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

            for (Pair dp : devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties())
            {
                assertEquals(dp.getKey(), PROPERTY_KEY + i);
                assertEquals(dp.getValue(), PROPERTY_VALUE_UPDATE + i);
            }
        }
        removeMultipleDevices();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testGetTwinUpdates() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException
    {
        addMultipleDevices();

        // Add tag and desired for multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> tags = new HashSet<>();
            tags.add(new Pair(TAG_KEY + i, TAG_VALUE + i));
            devicesUnderTest[i].sCDeviceForTwin.setTags(tags);

            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(PROPERTY_KEY + i, PROPERTY_VALUE + i));
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Update Tags and desired properties on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Set<Pair> tags = devicesUnderTest[i].sCDeviceForTwin.getTags();
            for (Pair tag : tags)
            {
                tag.setValue(TAG_VALUE_UPDATE + i);
            }
            devicesUnderTest[i].sCDeviceForTwin.setTags(tags);

            Set<Pair> desiredProperties = devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties();
            for (Pair dp : desiredProperties)
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

            for (Pair t : devicesUnderTest[i].sCDeviceForTwin.getTags())
            {
                assertEquals(t.getKey(), TAG_KEY + i);
                assertEquals(t.getValue(), TAG_VALUE_UPDATE + i);
            }

            for (Pair dp : devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties())
            {
                assertEquals(dp.getKey(), PROPERTY_KEY + i);
                assertEquals(dp.getValue(), PROPERTY_VALUE_UPDATE + i);
            }
        }
        removeMultipleDevices();
    }

    @Test
    public void testRawQueryTwin() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException
    {
        addMultipleDevices();
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls().create();

        // Add same desired on multiple devices
        final String queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        final double actualNumOfDevices = MAX_DEVICES;

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(queryProperty, queryPropertyValue));
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Raw Query for multiple devices having same property
        final String select = "properties.desired." + queryProperty + " AS " + queryProperty + "," + " COUNT() AS numberOfDevices";
        final String groupBy = "properties.desired." + queryProperty ;
        final SqlQuery sqlQuery = SqlQuery.createSqlQuery(select, SqlQuery.FromType.DEVICES, null, groupBy);
        Query rawTwinQuery = scRawTwinQueryClient.query(sqlQuery.getQuery(), PAGE_SIZE);

        while (scRawTwinQueryClient.hasNext(rawTwinQuery))
        {
            String result = scRawTwinQueryClient.next(rawTwinQuery);
            assertNotNull(result);
            Map map = gson.fromJson(result, Map.class);
            if (map.containsKey("numberOfDevices") && map.containsKey(queryProperty))
            {
                double value = (double) map.get("numberOfDevices");
                assertEquals(value, actualNumOfDevices, 0);
            }
        }

        removeMultipleDevices();
    }

    @Test
    public void testRawQueryMultipleInParallelTwin() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException
    {
        addMultipleDevices();
        final Gson gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls().create();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Add same desired on multiple devices
        final String queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        final double actualNumOfDevices = MAX_DEVICES;

        final String queryPropertyEven = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValueEven = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        int noOfEvenDevices = 0;

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(queryProperty, queryPropertyValue));
            if (i % 2 == 0)
            {
                desiredProperties.add(new Pair(queryPropertyEven, queryPropertyValueEven));
                noOfEvenDevices++;
            }
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // Raw Query for multiple devices having same property
                    final String select = "properties.desired." + queryProperty + " AS " + queryProperty + "," + " COUNT() AS numberOfDevices";
                    final String groupBy = "properties.desired." + queryProperty ;
                    final SqlQuery sqlQuery = SqlQuery.createSqlQuery(select, SqlQuery.FromType.DEVICES, null, groupBy);
                    Query rawTwinQuery = scRawTwinQueryClient.query(sqlQuery.getQuery(), PAGE_SIZE);

                    while (scRawTwinQueryClient.hasNext(rawTwinQuery))
                    {
                        String result = scRawTwinQueryClient.next(rawTwinQuery);
                        assertNotNull(result);
                        Map map = gson.fromJson(result, Map.class);
                        if (map.containsKey("numberOfDevices") && map.containsKey(queryProperty))
                        {
                            double value = (double) map.get("numberOfDevices");
                            assertEquals(value, actualNumOfDevices, 0);
                        }
                    }
                }
                catch (Exception e)
                {
                    assertTrue(e.getMessage(), true);
                }

            }
        });

        final double actualNumOfDevicesEven = noOfEvenDevices;
        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // Raw Query for multiple devices having same property
                    final String select = "properties.desired." + queryPropertyEven + " AS " + queryPropertyEven + "," + " COUNT() AS numberOfDevices";
                    final String groupBy = "properties.desired." + queryPropertyEven ;
                    final SqlQuery sqlQuery = SqlQuery.createSqlQuery(select, SqlQuery.FromType.DEVICES, null, groupBy);
                    Query rawTwinQuery = scRawTwinQueryClient.query(sqlQuery.getQuery(), PAGE_SIZE);

                    while (scRawTwinQueryClient.hasNext(rawTwinQuery))
                    {
                        String result = scRawTwinQueryClient.next(rawTwinQuery);
                        assertNotNull(result);
                        Map map = gson.fromJson(result, Map.class);
                        if (map.containsKey("numberOfDevices") && map.containsKey(queryPropertyEven))
                        {
                            double value = (double) map.get("numberOfDevices");
                            assertEquals(value, actualNumOfDevicesEven, 0);
                        }
                    }
                }
                catch (Exception e)
                {
                    assertTrue(e.getMessage(), true);
                }
            }
        });

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        removeMultipleDevices();
    }

    @Test
    public void testQueryTwin() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException
    {
        addMultipleDevices();

        // Add same desired on multiple devices
        final String queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(queryProperty, queryPropertyValue));
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Query multiple devices having same property
        final String where = "is_defined(properties.desired." + queryProperty + ")";
        SqlQuery sqlQuery = SqlQuery.createSqlQuery("*", SqlQuery.FromType.DEVICES, where, null);
        Query twinQuery = sCDeviceTwin.queryTwin(sqlQuery.getQuery(), PAGE_SIZE);

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            if (sCDeviceTwin.hasNextDeviceTwin(twinQuery))
            {
                DeviceTwinDevice d = sCDeviceTwin.getNextDeviceTwin(twinQuery);

                assertNotNull(d.getVersion());
                for (Pair dp : d.getDesiredProperties())
                {
                    assertEquals(dp.getKey(), queryProperty);
                    assertEquals(dp.getValue(), queryPropertyValue);
                }
            }
        }
        assertFalse(sCDeviceTwin.hasNextDeviceTwin(twinQuery));
        removeMultipleDevices();
    }

    @Test
    public void testMultipleQueryTwinInParallel() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException
    {
        addMultipleDevices();

        // Add same desired on multiple devices
        final String queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyEven = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValueEven = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        int noOfEvenDevices = 0;
        ExecutorService executor = Executors.newFixedThreadPool(2);

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(queryProperty, queryPropertyValue));
            if (i % 2 == 0)
            {
                desiredProperties.add(new Pair(queryProperty, queryPropertyValue));
                noOfEvenDevices++;
            }
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Query multiple devices having same property

        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final String where = "is_defined(properties.desired." + queryProperty + ")";
                    SqlQuery sqlQuery = SqlQuery.createSqlQuery("*", SqlQuery.FromType.DEVICES, where, null);
                    final Query twinQuery = sCDeviceTwin.queryTwin(sqlQuery.getQuery(), PAGE_SIZE);

                    for (int i = 0; i < MAX_DEVICES; i++)
                    {
                        try
                        {
                            if (sCDeviceTwin.hasNextDeviceTwin(twinQuery))
                            {
                                DeviceTwinDevice d = sCDeviceTwin.getNextDeviceTwin(twinQuery);

                                assertNotNull(d.getVersion());
                                for (Pair dp : d.getDesiredProperties())
                                {
                                    assertEquals(dp.getKey(), queryProperty);
                                    assertEquals(dp.getValue(), queryPropertyValue);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            assertTrue(e.getMessage(), true);
                        }

                        assertFalse(sCDeviceTwin.hasNextDeviceTwin(twinQuery));
                    }
                }
                catch (Exception e)
                {
                    assertTrue(e.getMessage(), true);
                }
            }
        });

        final int maximumEvenDevices = noOfEvenDevices;
        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final String whereEvenDevices = "is_defined(properties.desired." + queryPropertyEven + ")";
                    SqlQuery sqlQueryEvenDevices = SqlQuery.createSqlQuery("*", SqlQuery.FromType.DEVICES, whereEvenDevices, null);
                    final Query twinQueryEven = sCDeviceTwin.queryTwin(sqlQueryEvenDevices.getQuery(), PAGE_SIZE);

                    for (int i = 0; i < maximumEvenDevices; i++)
                    {
                        try
                        {
                            if (sCDeviceTwin.hasNextDeviceTwin(twinQueryEven))
                            {
                                DeviceTwinDevice d = sCDeviceTwin.getNextDeviceTwin(twinQueryEven);

                                assertNotNull(d.getVersion());
                                for (Pair dp : d.getDesiredProperties())
                                {
                                    assertEquals(dp.getKey(), queryPropertyEven);
                                    assertEquals(dp.getValue(), queryPropertyValueEven);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            assertTrue(e.getMessage(), true);
                        }

                        assertFalse(sCDeviceTwin.hasNextDeviceTwin(twinQueryEven));
                    }
                }
                catch (Exception e)
                {
                    assertTrue(e.getMessage(), true);
                }
            }
        });

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }
        removeMultipleDevices();
    }
}
