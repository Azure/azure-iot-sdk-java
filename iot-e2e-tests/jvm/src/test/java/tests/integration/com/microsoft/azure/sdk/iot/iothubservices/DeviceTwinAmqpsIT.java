/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Device;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.devicetwin.RawTwinQuery;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.*;
import tests.integration.com.microsoft.azure.sdk.iot.DeviceConnectionString;
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

public class DeviceTwinAmqpsIT
{
    // Max time to wait to see it on Hub
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 200; // 0.2 sec

    private static final long MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB*10; // 2 sec

    //Max time to wait before timing out test
    private static final long MAX_MILLISECS_TIMEOUT_KILL_TEST = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB + 50000; // 50 secs

    // Max reported properties to be tested
    private static final Integer MAX_PROPERTIES_TO_TEST = 5;

    //Max devices to test
    private static final Integer MAX_DEVICES = 5;

    //Default Page Size for Query
    private static final Integer PAGE_SIZE = 2;

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";

    private static final String PUBLIC_KEY_CERTIFICATE_BASE64_ENCODED_ENV_VAR_NAME = "IOTHUB_E2E_X509_CERT_BASE64";
    private static final String PRIVATE_KEY_BASE64_ENCODED_ENV_VAR_NAME = "IOTHUB_E2E_X509_PRIVATE_KEY_BASE64";
    private static final String X509_THUMBPRINT_ENV_VAR_NAME = "IOTHUB_E2E_X509_THUMBPRINT";

    private static String publicKeyCert;
    private static String privateKey;
    private static String x509Thumbprint;

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
    private static DeviceClient x509DeviceClient;
    private static RawTwinQuery scRawTwinQueryClient;
    private static DeviceTwin sCDeviceTwin;
    private static DeviceState deviceUnderTest = null;
    private static DeviceState x509DeviceUnderTest = null;
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
            String deviceIdAmqps = "java-device-twin-e2e-test-amqps".concat(UUID.randomUUID().toString());
            devicesUnderTest[i].sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceIdAmqps, null, null);
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
        if (deviceClient == null)
        {
            deviceState.dCDeviceForTwin = new DeviceExtension();
            deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceState.sCDeviceForRegistryManager), IotHubClientProtocol.AMQPS);
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
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        String privateKeyBase64Encoded = Tools.retrieveEnvironmentVariableValue(PRIVATE_KEY_BASE64_ENCODED_ENV_VAR_NAME);
        String publicKeyCertBase64Encoded = Tools.retrieveEnvironmentVariableValue(PUBLIC_KEY_CERTIFICATE_BASE64_ENCODED_ENV_VAR_NAME);
        x509Thumbprint = Tools.retrieveEnvironmentVariableValue(X509_THUMBPRINT_ENV_VAR_NAME);

        byte[] publicCertBytes = com.microsoft.azure.sdk.iot.deps.util.Base64.decodeBase64Local(publicKeyCertBase64Encoded.getBytes());
        publicKeyCert = new String(publicCertBytes);

        byte[] privateKeyBytes = com.microsoft.azure.sdk.iot.deps.util.Base64.decodeBase64Local(privateKeyBase64Encoded.getBytes());
        privateKey = new String(privateKeyBytes);

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
        x509DeviceClient = null;
    }

    @Before
    public void setUpNewDevice() throws IOException , IotHubException, NoSuchAlgorithmException, URISyntaxException, InterruptedException
    {
        deviceUnderTest = new DeviceState();
        String deviceIdAmqps = "java-device-twin-e2e-test-amqps".concat(UUID.randomUUID().toString());
        deviceUnderTest.sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceIdAmqps, null, null);
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

        Thread.sleep(MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS);
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
        Thread.sleep(MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS);
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
        Thread.sleep(MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS);
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
        Thread.sleep(MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS);
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
    public void testUpdateDesiredUpdatesAMQPSWithX509() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException
    {
        //arrange
        setUpX509Device(IotHubClientProtocol.AMQPS);

        // Add desired properties for the device
        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair(PROPERTY_KEY, PROPERTY_VALUE));
        x509DeviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
        sCDeviceTwin.updateTwin(x509DeviceUnderTest.sCDeviceForTwin);
        x509DeviceUnderTest.sCDeviceForTwin.clearTwin();

        // Update desired properties on multiple devices
        sCDeviceTwin.getTwin(x509DeviceUnderTest.sCDeviceForTwin);
        Set<Pair> updatedDesiredProperties = x509DeviceUnderTest.sCDeviceForTwin.getDesiredProperties();
        for (Pair dp : updatedDesiredProperties)
        {
            dp.setValue(PROPERTY_VALUE_UPDATE);
        }
        x509DeviceUnderTest.sCDeviceForTwin.setDesiredProperties(updatedDesiredProperties);
        sCDeviceTwin.updateTwin(x509DeviceUnderTest.sCDeviceForTwin);
        x509DeviceUnderTest.sCDeviceForTwin.clearTwin();

        // Read updates on multiple devices
        sCDeviceTwin.getTwin(x509DeviceUnderTest.sCDeviceForTwin);

        for (Pair dp : x509DeviceUnderTest.sCDeviceForTwin.getDesiredProperties())
        {
            assertEquals(dp.getKey(), PROPERTY_KEY);
            assertEquals(dp.getValue(), PROPERTY_VALUE_UPDATE);
        }

        tearDownTwin(x509DeviceUnderTest);
        registryManager.removeDevice(x509DeviceUnderTest.sCDeviceForRegistryManager.getDeviceId());
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testSendReportedPropertiesAMQPSWithX509() throws IOException, IotHubException, InterruptedException, URISyntaxException
    {
        //arrange
        setUpX509Device(IotHubClientProtocol.AMQPS);

        // act
        // send max_prop RP all at once
        x509DeviceUnderTest.dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
        x509DeviceClient.sendReportedProperties(x509DeviceUnderTest.dCDeviceForTwin.getReportedProp());
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);

        // assert
        assertEquals(x509DeviceUnderTest.deviceTwinStatus, STATUS.SUCCESS);

        // verify if they are received by SC
        Thread.sleep(MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS);
        int actualReportedPropFound = readReportedProperties(x509DeviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE);
        assertEquals(MAX_PROPERTIES_TO_TEST.intValue(), actualReportedPropFound);

        tearDownTwin(x509DeviceUnderTest);
        registryManager.removeDevice(x509DeviceUnderTest.sCDeviceForRegistryManager.getDeviceId());
    }

    private void setUpX509Device(IotHubClientProtocol protocol) throws IOException, IotHubException, URISyntaxException, InterruptedException
    {
        x509DeviceUnderTest = new DeviceState();
        String deviceIdX509 = "java-device-twin-e2e-test-" + protocol + "-x509".concat(UUID.randomUUID().toString());
        x509DeviceUnderTest.sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createDevice(deviceIdX509, AuthenticationType.SELF_SIGNED);
        x509DeviceUnderTest.sCDeviceForRegistryManager.setThumbprint(x509Thumbprint, x509Thumbprint);
        x509DeviceUnderTest.sCDeviceForRegistryManager = registryManager.addDevice(x509DeviceUnderTest.sCDeviceForRegistryManager);
        setUpTwinForX509(x509DeviceUnderTest, protocol);
    }

    private void setUpTwinForX509(DeviceState deviceState, IotHubClientProtocol protocol) throws IOException, URISyntaxException, IotHubException, InterruptedException
    {
        // set up twin on DeviceClient
        deviceState.dCDeviceForTwin = new DeviceExtension();
        String connString = DeviceConnectionString.get(iotHubConnectionString, x509DeviceUnderTest.sCDeviceForRegistryManager);
        x509DeviceClient = new DeviceClient(connString, protocol, publicKeyCert, false, privateKey, false);
        x509DeviceClient.open();
        x509DeviceClient.startDeviceTwin(new DeviceTwinStatusCallBack(), deviceState, deviceState.dCDeviceForTwin, deviceState);
        deviceState.deviceTwinStatus = STATUS.SUCCESS;

        // set up twin on ServiceClient
        if (sCDeviceTwin != null)
        {
            deviceState.sCDeviceForTwin = new DeviceTwinDevice(deviceState.sCDeviceForRegistryManager.getDeviceId());
            sCDeviceTwin.getTwin(deviceState.sCDeviceForTwin);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
        }
    }
}
