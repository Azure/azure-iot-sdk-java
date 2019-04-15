/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.setup;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Device;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.devicetwin.RawTwinQuery;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * Utility functions, setup and teardown for all device twin integration tests. This class should not contain any tests,
 * but any child class should.
 */
public class DeviceTwinCommon extends IntegrationTest
{
    // Max time to wait to see it on Hub
    protected static final long PERIODIC_WAIT_TIME_FOR_VERIFICATION = 1000; // 1 sec
    protected static final long MAX_WAIT_TIME_FOR_VERIFICATION = 180000; // 180 sec
    protected static final long DELAY_BETWEEN_OPERATIONS = 200; // 0.2 sec
    public static final long MULTITHREADED_WAIT_TIMEOUT_MS  = 5 * 60 * 1000; // 5 minutes

    public static final long DESIRED_PROPERTIES_PROPAGATION_TIME_MILLIS = 5 * 1000; //5 seconds

    protected static final long MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS = 5000; // 5 sec

    // Max reported properties to be tested
    protected static final Integer MAX_PROPERTIES_TO_TEST = 5;

    //Max devices to test
    protected static final Integer MAX_DEVICES = 3;

    //Default Page Size for Query
    protected static final Integer PAGE_SIZE = 2;

    protected static String iotHubConnectionString = "";
    protected static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;

    // Constants used in for Testing
    protected static final String PROPERTY_KEY = "Key";
    protected static final String PROPERTY_KEY_QUERY = "KeyQuery";
    protected static final String PROPERTY_VALUE = "Value";
    protected static final String PROPERTY_VALUE_QUERY = "ValueQuery";
    protected static final String PROPERTY_VALUE_UPDATE = "Update";
    protected static final String PROPERTY_VALUE_UPDATE2 = "Update2";
    protected static final String TAG_KEY = "Tag_Key";
    protected static final String TAG_VALUE = "Tag_Value";
    protected static final String TAG_VALUE_UPDATE = "Tag_Value_Update";

    // States of SDK
    protected static RegistryManager registryManager;
    protected InternalClient internalClient;
    protected static RawTwinQuery scRawTwinQueryClient;
    protected static DeviceTwin sCDeviceTwin;
    protected DeviceState deviceUnderTest = null;

    protected DeviceState[] devicesUnderTest;

    protected DeviceTwinTestInstance testInstance;
    protected static final long ERROR_INJECTION_WAIT_TIMEOUT = 1 * 60 * 1000; // 1 minute

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    public enum STATUS
    {
        SUCCESS, FAILURE, UNKNOWN
    }

    public class DeviceTwinStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            DeviceState state = (DeviceState) context;

            //On failure, Don't update status any further
            if ((status == OK || status == OK_EMPTY) && state.deviceTwinStatus != STATUS.FAILURE)
            {
                state.deviceTwinStatus = STATUS.SUCCESS;
            }
            else
            {
                state.deviceTwinStatus = STATUS.FAILURE;
            }
        }
    }

    public class DeviceState
    {
        public com.microsoft.azure.sdk.iot.service.Device sCDeviceForRegistryManager;
        public com.microsoft.azure.sdk.iot.service.Module sCModuleForRegistryManager;
        public DeviceTwinDevice sCDeviceForTwin;
        public DeviceExtension dCDeviceForTwin;
        public OnProperty dCOnProperty = new OnProperty();
        public STATUS deviceTwinStatus;
    }

    public class PropertyState
    {
        public boolean callBackTriggered;
        public Property property;
        public Object propertyNewValue;
        public Integer propertyNewVersion;
    }

    public class OnProperty implements TwinPropertyCallBack
    {
        @Override
        public void TwinPropertyCallBack(Property property,  Object context)
        {
            PropertyState propertyState = (PropertyState) context;
            if (property.getKey().equals(propertyState.property.getKey()))
            {
                propertyState.callBackTriggered = true;
                propertyState.propertyNewValue = property.getValue();
                propertyState.propertyNewVersion = property.getVersion();
            }
        }
    }

    public class DeviceExtension extends Device
    {
        public PropertyState[] propertyStateList = new PropertyState[MAX_PROPERTIES_TO_TEST];

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

        public synchronized void createNewReportedProperties(int maximumPropertiesToCreate)
        {
            for (int i = 0; i < maximumPropertiesToCreate; i++)
            {
                UUID randomUUID = UUID.randomUUID();
                this.setReportedProp(new Property(PROPERTY_KEY + randomUUID, PROPERTY_VALUE + randomUUID));
            }
        }

        public synchronized void updateAllExistingReportedProperties()
        {
            Set<Property> reportedProp = this.getReportedProp();

            for (Property p : reportedProp)
            {
                UUID randomUUID = UUID.randomUUID();
                p.setValue(PROPERTY_VALUE_UPDATE + randomUUID);
            }
        }

        public synchronized void updateExistingReportedProperty(int index)
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

    protected void addMultipleDevices(int numberOfDevices) throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException, ModuleClientException
    {
        devicesUnderTest = new DeviceState[numberOfDevices];

        for (int i = 0; i < numberOfDevices; i++)
        {
            devicesUnderTest[i] = new DeviceState();
            String id = "java-device-twin-e2e-test-" + this.testInstance.protocol.toString() + UUID.randomUUID().toString();
            devicesUnderTest[i].sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(id, null, null);
            devicesUnderTest[i].sCModuleForRegistryManager = com.microsoft.azure.sdk.iot.service.Module.createFromId(id, "module", null);
            devicesUnderTest[i].sCDeviceForRegistryManager = Tools.addDeviceWithRetry(registryManager, devicesUnderTest[i].sCDeviceForRegistryManager);
            devicesUnderTest[i].sCModuleForRegistryManager = Tools.addModuleWithRetry(registryManager, devicesUnderTest[i].sCModuleForRegistryManager);
            Thread.sleep(2000);
            setUpTwin(devicesUnderTest[i]);
        }
    }

    protected void removeMultipleDevices(int numberOfDevices) throws IOException, IotHubException, InterruptedException
    {
        for (int i = 0; i < numberOfDevices; i++)
        {
            tearDownTwin(devicesUnderTest[i]);
            registryManager.removeDevice(devicesUnderTest[i].sCDeviceForRegistryManager.getDeviceId());
            Thread.sleep(DELAY_BETWEEN_OPERATIONS);
        }
    }

    protected void setUpTwin(DeviceState deviceState) throws IOException, URISyntaxException, IotHubException, InterruptedException, ModuleClientException
    {
        // set up twin on DeviceClient
        if (internalClient == null)
        {
            deviceState.dCDeviceForTwin = new DeviceExtension();
            if (this.testInstance.authenticationType == SAS)
            {
                if (this.testInstance.moduleId == null)
                {
                    internalClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceState.sCDeviceForRegistryManager),
                            this.testInstance.protocol);
                }
                else
                {
                    internalClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceState.sCDeviceForRegistryManager, deviceState.sCModuleForRegistryManager),
                            this.testInstance.protocol);
                }
            }
            else if (this.testInstance.authenticationType == SELF_SIGNED)
            {
                if (this.testInstance.moduleId == null)
                {
                    internalClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceUnderTest.sCDeviceForRegistryManager),
                            this.testInstance.protocol,
                            testInstance.publicKeyCert,
                            false,
                            testInstance.privateKey,
                            false);
                }
                else
                {
                    internalClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceState.sCDeviceForRegistryManager, deviceState.sCModuleForRegistryManager),
                            this.testInstance.protocol,
                            testInstance.publicKeyCert,
                            false,
                            testInstance.privateKey,
                            false);
                }
            }
            IotHubServicesCommon.openClientWithRetry(internalClient);
            if (internalClient instanceof DeviceClient)
            {
                ((DeviceClient) internalClient).startDeviceTwin(new DeviceTwinStatusCallBack(), deviceState, deviceState.dCDeviceForTwin, deviceState);
            }
            else
            {
                ((ModuleClient) internalClient).startTwin(new DeviceTwinStatusCallBack(), deviceState, deviceState.dCDeviceForTwin, deviceState);
            }
            deviceState.deviceTwinStatus = STATUS.UNKNOWN;
        }

        // set up twin on ServiceClient
        if (sCDeviceTwin != null)
        {
            if (testInstance.moduleId == null)
            {
                deviceState.sCDeviceForTwin = new DeviceTwinDevice(deviceState.sCDeviceForRegistryManager.getDeviceId());
            }
            else
            {
                deviceState.sCDeviceForTwin = new DeviceTwinDevice(deviceState.sCDeviceForRegistryManager.getDeviceId(), deviceState.sCModuleForRegistryManager.getId());
            }

            sCDeviceTwin.getTwin(deviceState.sCDeviceForTwin);
            Thread.sleep(DELAY_BETWEEN_OPERATIONS);
        }
    }

    protected void tearDownTwin(DeviceState deviceState) throws IOException
    {
        // tear down twin on device client
        if (deviceState.sCDeviceForTwin != null)
        {
            deviceState.sCDeviceForTwin.clearTwin();
        }
        if (deviceState.dCDeviceForTwin != null)
        {
            deviceState.dCDeviceForTwin.clean();

            if (deviceState.dCDeviceForTwin.getDesiredProp() != null)
            {
                deviceState.dCDeviceForTwin.getDesiredProp().clear();
            }
        }
        if (internalClient != null)
        {
            internalClient.closeNow();
            internalClient = null;
        }
    }

    protected static Collection inputsCommon(ClientType clientType) throws IOException
    {
        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        return inputsCommon(clientType, certificateGenerator.getPublicCertificate(), certificateGenerator.getPrivateKey(), certificateGenerator.getX509Thumbprint());
    }

    protected static Collection inputsCommon(ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws IOException
    {
        sCDeviceTwin = DeviceTwin.createFromConnectionString(iotHubConnectionString);
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        scRawTwinQueryClient = RawTwinQuery.createFromConnectionString(iotHubConnectionString);

        //base names for devices and modules. Each test creates a device/module with this as the prefix, and a unique uuid as the suffix
        String deviceIdAmqps = "java-device-client-e2e-test-amqps-";
        String deviceIdAmqpsWs = "java-device-client-e2e-test-amqpsws-";
        String deviceIdMqtt = "java-device-client-e2e-test-mqtt-";
        String deviceIdMqttWs = "java-device-client-e2e-test-mqttws-";
        String deviceIdMqttX509 = "java-device-client-e2e-test-mqtt-X509-";
        String deviceIdAmqpsX509 = "java-device-client-e2e-test-amqps-X509-";

        String moduleIdAmqps = "java-device-client-e2e-test-amqps-module-";
        String moduleIdAmqpsWs = "java-device-client-e2e-test-amqpsws-module-";
        String moduleIdMqtt = "java-device-client-e2e-test-mqtt-module-";
        String moduleIdMqttWs = "java-device-client-e2e-test-mqttws-module-";

        List inputs;
        if (clientType == ClientType.DEVICE_CLIENT)
        {
            inputs =  Arrays.asList(
                    new Object[][]
                            {
                                    //sas token, device client
                                    {deviceIdAmqps, null, AMQPS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                    {deviceIdAmqpsWs, null, AMQPS_WS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                    {deviceIdMqtt, null, MQTT, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                    {deviceIdMqttWs,  null, MQTT_WS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},

                                    //x509, device client
                                    {deviceIdAmqpsX509, null, AMQPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                    {deviceIdMqttX509, null, MQTT, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                            }
            );
        }
        else
        {
            inputs =  Arrays.asList(
                    new Object[][]
                            {
                                    //sas token, module client
                                    {deviceIdAmqps, moduleIdAmqps, AMQPS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                    {deviceIdAmqpsWs, moduleIdAmqpsWs, AMQPS_WS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                    {deviceIdMqtt, moduleIdMqtt, MQTT, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                    {deviceIdMqttWs,  moduleIdMqttWs, MQTT_WS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint}
                            }
            );
        }

        return inputs;
    }

    public DeviceTwinCommon(String deviceId, String moduleId, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        this.testInstance = new DeviceTwinTestInstance(deviceId, moduleId, protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    public class DeviceTwinTestInstance
    {
        public String deviceId;
        public IotHubClientProtocol protocol;
        public AuthenticationType authenticationType;
        public String moduleId;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public String uuid;

        public DeviceTwinTestInstance(String deviceId, String moduleId, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
        {
            this.deviceId = deviceId;
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.moduleId = moduleId;
            this.publicKeyCert = publicKeyCert;
            this.privateKey = privateKey;
            this.x509Thumbprint = x509Thumbprint;
        }
    }

    @BeforeClass
    public static void classSetup()
    {
        try
        {
            sCDeviceTwin = DeviceTwin.createFromConnectionString(iotHubConnectionString);
            registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
            scRawTwinQueryClient = RawTwinQuery.createFromConnectionString(iotHubConnectionString);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Unexpected exception encountered");
        }
    }

    @Before
    public void setUpNewDeviceAndModule() throws IOException, IotHubException, URISyntaxException, InterruptedException, ModuleClientException
    {
        deviceUnderTest = new DeviceState();
        this.testInstance.uuid = UUID.randomUUID().toString();

        if (this.testInstance.authenticationType == SAS)
        {
            deviceUnderTest.sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(this.testInstance.deviceId + this.testInstance.uuid, null, null);

            if (this.testInstance.moduleId != null)
            {
                deviceUnderTest.sCModuleForRegistryManager = com.microsoft.azure.sdk.iot.service.Module.createFromId(this.testInstance.deviceId + this.testInstance.uuid, this.testInstance.moduleId + this.testInstance.uuid, null);
            }
        }
        else if (this.testInstance.authenticationType == SELF_SIGNED)
        {
            deviceUnderTest.sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createDevice(this.testInstance.deviceId + this.testInstance.uuid, SELF_SIGNED);
            deviceUnderTest.sCDeviceForRegistryManager.setThumbprintFinal(testInstance.x509Thumbprint, testInstance.x509Thumbprint);
        }


        deviceUnderTest.sCDeviceForRegistryManager = Tools.addDeviceWithRetry(registryManager, deviceUnderTest.sCDeviceForRegistryManager);

        if (deviceUnderTest.sCModuleForRegistryManager != null)
        {
            Tools.addModuleWithRetry(registryManager, deviceUnderTest.sCModuleForRegistryManager);
        }

        Thread.sleep(2000);

        setUpTwin(deviceUnderTest);
    }

    @After
    public void tearDownNewDeviceAndModule() throws IOException, IotHubException
    {
        tearDownTwin(deviceUnderTest);

        registryManager.removeDevice(deviceUnderTest.sCDeviceForRegistryManager.getDeviceId());

        try
        {
            Thread.sleep(INTERTEST_GUARDIAN_DELAY_MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            fail(buildExceptionMessage("Unexpected exception encountered", internalClient));
        }
    }

    protected static Collection<BaseDevice> getIdentities(Collection inputs)
    {
        //twin tests tear down and build up identities in between tests, not at the end of the suite
        return new HashSet<>();
    }

    protected static void tearDown(Collection<BaseDevice> identitiesToDispose)
    {
        if (registryManager != null)
        {
            Tools.removeDevicesAndModules(registryManager, identitiesToDispose);
            registryManager.close();
        }

        registryManager = null;
        sCDeviceTwin = null;
    }

    protected void readReportedPropertiesAndVerify(DeviceState deviceState, String startsWithKey, String startsWithValue, int expectedReportedPropCount) throws IOException, IotHubException, InterruptedException
    {
        int actualCount = 0;

        // Check status periodically for success or until timeout
        long startTime = System.currentTimeMillis();
        long timeElapsed;
        while (expectedReportedPropCount != actualCount)
        {
            Thread.sleep(PERIODIC_WAIT_TIME_FOR_VERIFICATION);
            timeElapsed = System.currentTimeMillis() - startTime;
            if (timeElapsed > MAX_WAIT_TIME_FOR_VERIFICATION)
            {
                break;
            }

            actualCount = 0;
            sCDeviceTwin.getTwin(deviceState.sCDeviceForTwin);
            Set<Pair> repProperties = deviceState.sCDeviceForTwin.getReportedProperties();

            for (Pair p : repProperties)
            {
                String val = (String) p.getValue();
                if (p.getKey().startsWith(startsWithKey) && val.startsWith(startsWithValue))
                {
                    actualCount++;
                }
            }
        }
        assertEquals(buildExceptionMessage("Expected " + expectedReportedPropCount + " but had " + actualCount, internalClient), expectedReportedPropCount, actualCount);
    }

    protected void waitAndVerifyTwinStatusBecomesSuccess() throws InterruptedException
    {
        // Check status periodically for success or until timeout
        long startTime = System.currentTimeMillis();
        long timeElapsed = 0;
        while (STATUS.SUCCESS != deviceUnderTest.deviceTwinStatus)
        {
            Thread.sleep(PERIODIC_WAIT_TIME_FOR_VERIFICATION);
            timeElapsed = System.currentTimeMillis() - startTime;
            if (timeElapsed > MAX_WAIT_TIME_FOR_VERIFICATION)
            {
                break;
            }
        }
        assertEquals(buildExceptionMessage("Expected SUCCESS but was " + deviceUnderTest.deviceTwinStatus, internalClient), STATUS.SUCCESS, deviceUnderTest.deviceTwinStatus);
    }

    protected void sendReportedPropertiesAndVerify(int numOfProp) throws IOException, IotHubException, InterruptedException
    {
        // Act
        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(numOfProp);
        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());

        // Assert
        waitAndVerifyTwinStatusBecomesSuccess();
        // verify if they are received by SC
        readReportedPropertiesAndVerify(deviceUnderTest, PROPERTY_KEY, PROPERTY_VALUE, numOfProp);
    }

    protected void waitAndVerifyDesiredPropertyCallback(String propPrefix, boolean withVersion) throws InterruptedException
    {
        // Check status periodically for success or until timeout
        long startTime = System.currentTimeMillis();
        long timeElapsed = 0;

        for (int i = 0; i < deviceUnderTest.dCDeviceForTwin.propertyStateList.length; i++)
        {
            PropertyState propertyState = deviceUnderTest.dCDeviceForTwin.propertyStateList[i];
            while (!propertyState.callBackTriggered || propertyState.propertyNewValue == null || !((String) propertyState.propertyNewValue).startsWith(propPrefix))
            {
                Thread.sleep(PERIODIC_WAIT_TIME_FOR_VERIFICATION);
                timeElapsed = System.currentTimeMillis() - startTime;
                if (timeElapsed > MAX_WAIT_TIME_FOR_VERIFICATION)
                {
                    break;
                }
            }
            assertTrue(buildExceptionMessage("Callback was not triggered for one or more properties", internalClient), propertyState.callBackTriggered);
            assertTrue(buildExceptionMessage("Missing the expected prefix, was " + propertyState.propertyNewValue, internalClient), ((String) propertyState.propertyNewValue).startsWith(propPrefix));
            if (withVersion)
            {
                assertNotEquals(buildExceptionMessage("Version was not set in the callback", internalClient), (int) propertyState.propertyNewVersion, -1);
            }
        }
    }

    protected void subscribeToDesiredPropertiesAndVerify(int numOfProp) throws IOException, InterruptedException, IotHubException
    {
        // arrange
        if (deviceUnderTest != null)
        {
            if (deviceUnderTest.sCDeviceForTwin != null)
            {
                deviceUnderTest.sCDeviceForTwin.clearDesiredProperties();
            }

            if (deviceUnderTest.dCDeviceForTwin != null && deviceUnderTest.dCDeviceForTwin.getReportedProp() != null)
            {
                deviceUnderTest.dCDeviceForTwin.getDesiredProp().clear();
            }
        }
        deviceUnderTest.dCDeviceForTwin.propertyStateList = new PropertyState[numOfProp];
        for (int i = 0; i < numOfProp; i++)
        {
            PropertyState propertyState = new PropertyState();
            propertyState.callBackTriggered = false;
            propertyState.property = new Property(PROPERTY_KEY + i, PROPERTY_VALUE);
            deviceUnderTest.dCDeviceForTwin.propertyStateList[i] = propertyState;
            deviceUnderTest.dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, deviceUnderTest.dCDeviceForTwin, propertyState);
        }

        // act
        internalClient.subscribeToDesiredProperties(deviceUnderTest.dCDeviceForTwin.getDesiredProp());
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        Set<Pair> desiredProperties = new HashSet<>();
        for (int i = 0; i < numOfProp; i++)
        {
            desiredProperties.add(new Pair(PROPERTY_KEY + i, PROPERTY_VALUE_UPDATE + UUID.randomUUID()));
        }
        deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
        sCDeviceTwin.updateTwin(deviceUnderTest.sCDeviceForTwin);

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(PROPERTY_VALUE_UPDATE, false);
    }

    protected void setConnectionStatusCallBack(final List<com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates)
    {
        IotHubConnectionStatusChangeCallback connectionStatusUpdateCallback = new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
            {
                actualStatusUpdates.add(new com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair<>(status, throwable));
            }
        };

        this.internalClient.registerConnectionStatusChangeCallback(connectionStatusUpdateCallback, null);
    }

    protected void testGetDeviceTwin() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        Map<Property, com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair<TwinPropertyCallBack, Object>> desiredPropertiesCB = new HashMap<>();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            PropertyState propertyState = new PropertyState();
            propertyState.property = new Property(PROPERTY_KEY + i, PROPERTY_VALUE);
            deviceUnderTest.dCDeviceForTwin.propertyStateList[i] = propertyState;
            desiredPropertiesCB.put(propertyState.property, new com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair<TwinPropertyCallBack, Object>(deviceUnderTest.dCOnProperty, propertyState));
        }
        internalClient.subscribeToTwinDesiredProperties(desiredPropertiesCB);
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        Set<Pair> desiredProperties = new HashSet<>();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            desiredProperties.add(new Pair(PROPERTY_KEY + i, PROPERTY_VALUE_UPDATE + UUID.randomUUID()));
        }
        deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
        sCDeviceTwin.updateTwin(deviceUnderTest.sCDeviceForTwin);
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        for (int i = 0; i < deviceUnderTest.dCDeviceForTwin.propertyStateList.length; i++)
        {
            PropertyState propertyState = deviceUnderTest.dCDeviceForTwin.propertyStateList[i];
            propertyState.callBackTriggered = false;
            propertyState.propertyNewVersion = -1;
        }

        // act
        if (internalClient instanceof DeviceClient)
        {
            ((DeviceClient)internalClient).getDeviceTwin();
        }
        else
        {
            ((ModuleClient)internalClient).getTwin();
        }

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(PROPERTY_VALUE_UPDATE, true);
    }
}
