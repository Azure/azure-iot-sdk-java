package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Device;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TransportClientDeviceTwinIT
{
    // Max time to wait to see it on Hub
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 500; // 0.2 sec

    private static final long MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB*10; // 2 sec

    //Max time to wait before timing out test
    private static final long MAX_MILLISECS_TIMEOUT_KILL_TEST = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB + 50000; // 50 secs

    // Max reported properties to be tested
    private static final Integer MAX_PROPERTIES_TO_TEST = 3;

    //Max devices to test
    private static final Integer MAX_DEVICES = 3;

    //Default Page Size for Query
    private static final Integer PAGE_SIZE = 2;

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;

    // Constants used in for Testing
    private static final String PROPERTY_KEY = "Key";
    private static final String PROPERTY_KEY_QUERY = "KeyQuery";
    private static final String PROPERTY_VALUE = "Value";
    private static final String PROPERTY_VALUE_QUERY = "ValueQuery";
    private static final String PROPERTY_VALUE_UPDATE = "Update";
    private static final String TAG_KEY = "Tag_Key";
    private static final String TAG_VALUE = "Tag_Value";
    private static final String TAG_VALUE_UPDATE = "Tag_Value_Update";

    private static RegistryManager registryManager;

    // Service client objects
    private static RawTwinQuery scRawTwinQueryClient;
    private static DeviceTwin sCDeviceTwin;

    TransportClient transportClient = null;

    // Test devices
    private static ArrayList<TransportClientDeviceTwinIT.DeviceState> devicesUnderTest = new ArrayList<>();

    private enum STATUS
    {
        SUCCESS, FAILURE
    }

    protected class DeviceTwinStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            TransportClientDeviceTwinIT.DeviceState state = (TransportClientDeviceTwinIT.DeviceState) context;

            //On failure, Don't update status any further
            if ((status == OK || status == OK_EMPTY ) && state.deviceTwinStatus != TransportClientDeviceTwinIT.STATUS.FAILURE)
            {
                state.deviceTwinStatus = TransportClientDeviceTwinIT.STATUS.SUCCESS;
            }
            else
            {
                state.deviceTwinStatus = TransportClientDeviceTwinIT.STATUS.FAILURE;
            }
        }
    }

    @After
    public void delayTests()
    {
        try
        {
            Thread.sleep(INTERTEST_GUARDIAN_DELAY_MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    class DeviceState
    {
        DeviceClient deviceClient;
        String connectionString;
        TransportClientDeviceTwinIT.DeviceExtension dCDeviceForTwin;
        TransportClientDeviceTwinIT.STATUS deviceTwinStatus;

        com.microsoft.azure.sdk.iot.service.Device sCDeviceForRegistryManager;
        DeviceTwinDevice sCDeviceForTwin;
    }

    class PropertyState
    {
        boolean callBackTriggered;
        Property property;
        Object propertyNewValue;
    }

    class DeviceExtension extends Device
    {
        List<TransportClientDeviceTwinIT.PropertyState> propertyStateList = new LinkedList<>();

        @Override
        public void PropertyCall(String propertyKey, Object propertyValue, Object context)
        {
            TransportClientDeviceTwinIT.PropertyState propertyState = (TransportClientDeviceTwinIT.PropertyState) context;
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

    private void setUp() throws IOException, NoSuchAlgorithmException, IotHubException, InterruptedException, URISyntaxException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        sCDeviceTwin = DeviceTwin.createFromConnectionString(iotHubConnectionString);
        scRawTwinQueryClient = RawTwinQuery.createFromConnectionString(iotHubConnectionString);

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            DeviceState deviceState = new TransportClientDeviceTwinIT.DeviceState();
            String deviceIdAmqps = "java-device-twin-e2e-test-multiplexing-twin-amqps".concat(UUID.randomUUID().toString());
            deviceState.sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceIdAmqps, null, null);
            deviceState.sCDeviceForRegistryManager = registryManager.addDevice(deviceState.sCDeviceForRegistryManager);
            deviceState.connectionString = registryManager.getDeviceConnectionString(deviceState.sCDeviceForRegistryManager);
            devicesUnderTest.add(deviceState);

            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        }

        TransportClient transportClient = new TransportClient(IotHubClientProtocol.AMQPS);

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            DeviceState deviceState = devicesUnderTest.get(i);
            DeviceClient deviceClient = new DeviceClient(deviceState.connectionString, transportClient);
            deviceState.deviceClient = deviceClient;
            devicesUnderTest.get(i).dCDeviceForTwin = new TransportClientDeviceTwinIT.DeviceExtension();
        }

        SendMessagesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            devicesUnderTest.get(i).deviceClient.startDeviceTwin(new TransportClientDeviceTwinIT.DeviceTwinStatusCallBack(), devicesUnderTest.get(i), devicesUnderTest.get(i).dCDeviceForTwin, devicesUnderTest.get(i));
            devicesUnderTest.get(i).deviceTwinStatus = STATUS.SUCCESS;
            devicesUnderTest.get(i).sCDeviceForTwin = new DeviceTwinDevice(devicesUnderTest.get(i).sCDeviceForRegistryManager.getDeviceId());
            sCDeviceTwin.getTwin(devicesUnderTest.get(i).sCDeviceForTwin);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        }
    }

    private void tearDown() throws IOException, IotHubException, InterruptedException
    {
        if (transportClient != null)
        {
            transportClient.closeNow();
        }

        for (int i = 0 ; i < MAX_DEVICES; i++)
        {
            registryManager.removeDevice(devicesUnderTest.get(i).sCDeviceForRegistryManager.getDeviceId());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        }

        if (registryManager != null)
        {
            registryManager.close();
            registryManager = null;
        }

        sCDeviceTwin = null;
    }

    private int readReportedProperties(TransportClientDeviceTwinIT.DeviceState deviceState, String startsWithKey, String startsWithValue) throws IOException , IotHubException, InterruptedException
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
    public void testTwin() throws IOException, InterruptedException, IotHubException, URISyntaxException, NoSuchAlgorithmException
    {
        setUp();

        ExecutorService executor = Executors.newFixedThreadPool(MAX_PROPERTIES_TO_TEST);

        // testSubscribeToDesiredProperties
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            for (int j = 0; j < MAX_PROPERTIES_TO_TEST; j++)
            {
                TransportClientDeviceTwinIT.PropertyState propertyState = new TransportClientDeviceTwinIT.PropertyState();
                propertyState.callBackTriggered = false;
                propertyState.property = new Property(PROPERTY_KEY + j, PROPERTY_VALUE);
                devicesUnderTest.get(i).dCDeviceForTwin.propertyStateList.add(propertyState);
                devicesUnderTest.get(i).dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, devicesUnderTest.get(i).dCDeviceForTwin, propertyState);
            }

            // act
            devicesUnderTest.get(i).deviceClient.subscribeToDesiredProperties(devicesUnderTest.get(i).dCDeviceForTwin.getDesiredProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

            Set<Pair> desiredProperties = new HashSet<>();
            for (int j = 0; j < MAX_PROPERTIES_TO_TEST; j++)
            {
                desiredProperties.add(new Pair(PROPERTY_KEY + j, PROPERTY_VALUE_UPDATE + UUID.randomUUID()));
            }
            devicesUnderTest.get(i).sCDeviceForTwin.setDesiredProperties(desiredProperties);
            sCDeviceTwin.updateTwin(devicesUnderTest.get(i).sCDeviceForTwin);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

            // assert
            assertEquals(TransportClientDeviceTwinIT.STATUS.SUCCESS, devicesUnderTest.get(i).deviceTwinStatus);
            for (TransportClientDeviceTwinIT.PropertyState propertyState : devicesUnderTest.get(i).dCDeviceForTwin.propertyStateList)
            {
                assertTrue("One or more property callbacks were not triggered", propertyState.callBackTriggered);
                assertTrue(((String) propertyState.propertyNewValue).startsWith(PROPERTY_VALUE_UPDATE));
                assertEquals(TransportClientDeviceTwinIT.STATUS.SUCCESS, devicesUnderTest.get(i).deviceTwinStatus);
            }
        }

        // testUpdateReportedProperties
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            // act
            // send max_prop RP all at once
            devicesUnderTest.get(i).dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
            devicesUnderTest.get(i).deviceClient.sendReportedProperties(devicesUnderTest.get(i).dCDeviceForTwin.getReportedProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

            // act
            // Update RP
            devicesUnderTest.get(i).dCDeviceForTwin.updateAllExistingReportedProperties();
            devicesUnderTest.get(i).deviceClient.sendReportedProperties(devicesUnderTest.get(i).dCDeviceForTwin.getReportedProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

            // assert
            assertEquals(devicesUnderTest.get(i).deviceTwinStatus, TransportClientDeviceTwinIT.STATUS.SUCCESS);

            // verify if they are received by SC
            Thread.sleep(MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS);
            int actualReportedPropFound = readReportedProperties(devicesUnderTest.get(i), PROPERTY_KEY, PROPERTY_VALUE_UPDATE);
            assertEquals(MAX_PROPERTIES_TO_TEST.intValue(), actualReportedPropFound);
        }

        // send max_prop RP one at a time in parallel
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            final int finalI = i;
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    // testSendReportedPropertiesMultiThreaded
                    try
                    {
                        devicesUnderTest.get(finalI).dCDeviceForTwin.createNewReportedProperties(1);
                        devicesUnderTest.get(finalI).deviceClient.sendReportedProperties(devicesUnderTest.get(finalI).dCDeviceForTwin.getReportedProp());
                    }
                    catch (IOException e)
                    {
                        fail(e.getMessage());
                    }
                    assertEquals(devicesUnderTest.get(finalI).deviceTwinStatus, TransportClientDeviceTwinIT.STATUS.SUCCESS);

                    // testUpdateReportedPropertiesMultiThreaded
                    try
                    {
                        devicesUnderTest.get(finalI).dCDeviceForTwin.updateExistingReportedProperty(finalI);
                        devicesUnderTest.get(finalI).deviceClient.sendReportedProperties(devicesUnderTest.get(finalI).dCDeviceForTwin.getReportedProp());
                    }
                    catch (IOException e)
                    {
                        fail(e.getMessage());
                    }
                    assertEquals(devicesUnderTest.get(finalI).deviceTwinStatus, TransportClientDeviceTwinIT.STATUS.SUCCESS);
                }
            });
        }
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        tearDown();
    }
}
