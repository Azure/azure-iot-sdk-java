/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;

import com.google.gson.JsonParser;
import com.microsoft.azure.sdk.iot.deps.twin.TwinConnectionState;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Device;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.After;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assert.*;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/**
 * Utility functions, setup and teardown for all device twin integration tests. This class should not contain any tests,
 * but any child class should.
 */
public class DeviceTwinCommon extends IntegrationTest
{
    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        IntegrationTest.isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        IntegrationTest.isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        String publicKeyCert = certificateGenerator.getPublicCertificate();
        String privateKey = certificateGenerator.getPrivateKey();
        String x509Thumbprint = certificateGenerator.getX509Thumbprint();

        List inputs = new ArrayList();
        for (ClientType clientType : ClientType.values())
        {
            if (clientType == ClientType.DEVICE_CLIENT)
            {
                inputs.addAll(Arrays.asList(
                        new Object[][]
                                {
                                        //sas token, device client
                                        {AMQPS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                        {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                        {MQTT, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                        {MQTT_WS, SAS, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},

                                        //x509, device client
                                        {AMQPS, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                        {MQTT, SELF_SIGNED, ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                }
                ));
            }
            else
            {
                inputs.addAll(Arrays.asList(
                        new Object[][]
                                {
                                        //sas token, module client
                                        {AMQPS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                        {AMQPS_WS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                        {MQTT, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint},
                                        {MQTT_WS, SAS, ClientType.MODULE_CLIENT, publicKeyCert, privateKey, x509Thumbprint}
                                }
                ));
            }
        }

        return inputs;
    }

    // Max time to wait to see it on Hub
    protected static final long PERIODIC_WAIT_TIME_FOR_VERIFICATION = 1000; // 1 sec
    protected static final long MAX_WAIT_TIME_FOR_VERIFICATION_MILLISECONDS = 60 * 1000; // 1 minute
    protected static final long DELAY_BETWEEN_OPERATIONS = 200; // 0.2 sec
    protected static final long REPORTED_PROPERTIES_PROPAGATION_DELAY_MILLISECONDS = 2000; // 2 seconds
    public static final long MULTITHREADED_WAIT_TIMEOUT_MILLISECONDS = 60 * 1000; // 1 minute

    public static final long DESIRED_PROPERTIES_PROPAGATION_TIME_MILLISECONDS = 5 * 1000; //5 seconds

    protected static final long MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS = 5000; // 5 sec

    // Max reported properties to be tested
    protected static final Integer MAX_PROPERTIES_TO_TEST = 5;

    //Max devices to test
    protected static final Integer MAX_DEVICES = 3;

    //Default Page Size for Query
    protected static final Integer PAGE_SIZE = 2;

    protected static String iotHubConnectionString = "";

    // Constants used in for Testing
    protected static final String PROPERTY_KEY = "Key";
    protected static final String PROPERTY_KEY_QUERY = "KeyQuery";
    protected static final String PROPERTY_VALUE = "Value";
    protected static final String PROPERTY_VALUE_ARRAY = "[\"Value\",\"Value2\"]" ;
    protected static final String PROPERTY_VALUE_QUERY = "ValueQuery";
    protected static final String PROPERTY_VALUE_UPDATE = "Update";
    protected static final String PROPERTY_VALUE_UPDATE2 = "Update2";
    protected static final String PROPERTY_VALUE_UPDATE_ARRAY = "[\"1stUpdate1\",\"1stUpdate2\"]";
    protected static final String PROPERTY_VALUE_UPDATE2_ARRAY = "[\"2ndUpdate1\",\"2ndUpdate2\"]";
    protected static final String PROPERTY_VALUE_UPDATE_ARRAY_PREFIX = "1stUpdate";
    protected static final String PROPERTY_VALUE_UPDATE2_ARRAY_PREFIX = "2ndUpdate";
    protected static final String TAG_KEY = "Tag_Key";
    protected static final String TAG_VALUE = "Tag_Value";
    protected static final String TAG_VALUE_UPDATE = "Tag_Value_Update";

    protected static String deviceIdPrefix = "java-twin-e2e-test-device";
    protected static String moduleIdPrefix = "java-twin-e2e-test-module";

    // States of SDK
    protected InternalClient internalClient;
    protected DeviceState deviceUnderTest = null;

    protected DeviceState[] devicesUnderTest;

    protected DeviceTwinTestInstance testInstance;
    protected static final long ERROR_INJECTION_WAIT_TIMEOUT_MILLISECONDS = 1 * 60 * 1000; // 1 minute

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    public class DeviceTwinStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            DeviceState state = (DeviceState) context;

            if (status == OK_EMPTY)
            {
                // MQTT returns OK_EMPTY, but AMQP returns OK. Consolidate them for consistency here
                state.deviceTwinStatus = OK;
            }
            else
            {
                state.deviceTwinStatus = status;
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
        public IotHubStatusCode deviceTwinStatus;
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

        public synchronized Set<Property> createNewReportedProperties(int maximumPropertiesToCreate)
        {
            Set<Property> newProperties = new HashSet<>();
            for (int i = 0; i < maximumPropertiesToCreate; i++)
            {
                UUID randomUUID = UUID.randomUUID();
                Property newReportedProperty = new Property(PROPERTY_KEY + randomUUID, PROPERTY_VALUE + randomUUID);
                this.setReportedProp(newReportedProperty);
                newProperties.add(newReportedProperty);
            }

            return newProperties;
        }

        public synchronized void createNewReportedArrayProperties(int maximumPropertiesToCreate)
        {
            JsonParser jsonParser = new JsonParser();
            for (int i = 0; i < maximumPropertiesToCreate; i++)
            {
                this.setReportedProp(new Property(PROPERTY_KEY + UUID.randomUUID(), jsonParser.parse(PROPERTY_VALUE_ARRAY)));
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

        public synchronized Set<Property> updateExistingReportedProperty(int index)
        {
            Set<Property> updatedProp = new HashSet<>();
            Set<Property> reportedProp = this.getReportedProp();
            int i = 0;
            for (Property p : reportedProp)
            {
                if (i == index)
                {
                    UUID randomUUID = UUID.randomUUID();
                    p.setValue(PROPERTY_VALUE_UPDATE + randomUUID);
                    updatedProp.add(p);
                    break;
                }
                i++;
            }

            return updatedProp;
        }
    }

    protected void addMultipleDevices(int numberOfDevices) throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(numberOfDevices, true);
    }

    protected void addMultipleDevices(int numberOfDevices, boolean openDeviceClients) throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        devicesUnderTest = new DeviceState[numberOfDevices];

        for (int i = 0; i < numberOfDevices; i++)
        {
            devicesUnderTest[i] = new DeviceState();
            String id = "java-device-twin-e2e-test-" + this.testInstance.protocol.toString() + UUID.randomUUID().toString();
            devicesUnderTest[i].sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(id, null, null);
            devicesUnderTest[i].sCModuleForRegistryManager = com.microsoft.azure.sdk.iot.service.Module.createFromId(id, "module", null);
            devicesUnderTest[i].sCDeviceForRegistryManager = Tools.addDeviceWithRetry(testInstance.registryManager, devicesUnderTest[i].sCDeviceForRegistryManager);
            devicesUnderTest[i].sCModuleForRegistryManager = Tools.addModuleWithRetry(testInstance.registryManager, devicesUnderTest[i].sCModuleForRegistryManager);
            setUpTwin(devicesUnderTest[i], openDeviceClients);
        }

        Thread.sleep(2000);
    }

    protected void removeMultipleDevices(int numberOfDevices) throws IOException, IotHubException, InterruptedException
    {
        for (int i = 0; i < numberOfDevices; i++)
        {
            tearDownTwin(devicesUnderTest[i]);
            testInstance.registryManager.removeDevice(devicesUnderTest[i].sCDeviceForRegistryManager.getDeviceId());
        }
    }

    protected void setUpTwin(DeviceState deviceState, boolean openDeviceClient) throws IOException, URISyntaxException, IotHubException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        // set up twin on DeviceClient
        if (internalClient == null)
        {
            deviceState.dCDeviceForTwin = new DeviceExtension();
            if (this.testInstance.authenticationType == SAS)
            {
                if (this.testInstance.clientType == ClientType.DEVICE_CLIENT)
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
                SSLContext sslContext = SSLContextBuilder.buildSSLContext(testInstance.publicKeyCert, testInstance.privateKey);
                if (this.testInstance.clientType == ClientType.DEVICE_CLIENT)
                {
                    internalClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceUnderTest.sCDeviceForRegistryManager),
                            this.testInstance.protocol,
                            sslContext);
                }
                else
                {
                    internalClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceState.sCDeviceForRegistryManager, deviceState.sCModuleForRegistryManager),
                            this.testInstance.protocol,
                            sslContext);
                }
            }

            if ((this.testInstance.protocol == AMQPS || this.testInstance.protocol == AMQPS_WS) && this.testInstance.authenticationType == SAS)
            {
                internalClient.setOption("SetAmqpOpenAuthenticationSessionTimeout", AMQP_AUTHENTICATION_SESSION_TIMEOUT_SECONDS);
                internalClient.setOption("SetAmqpOpenDeviceSessionsTimeout", AMQP_DEVICE_SESSION_TIMEOUT_SECONDS);
            }
            if (openDeviceClient)
            {
                internalClient.open();
                if (internalClient instanceof DeviceClient)
                {
                    ((DeviceClient) internalClient).startDeviceTwin(new DeviceTwinStatusCallBack(), deviceState, deviceState.dCDeviceForTwin, deviceState);
                }
                else
                {
                    ((ModuleClient) internalClient).startTwin(new DeviceTwinStatusCallBack(), deviceState, deviceState.dCDeviceForTwin, deviceState);
                }
            }

            deviceState.deviceTwinStatus = IotHubStatusCode.ERROR;
        }

        // set up twin on ServiceClient
        if (testInstance.twinServiceClient != null)
        {
            if (testInstance.clientType == ClientType.DEVICE_CLIENT)
            {
                deviceState.sCDeviceForTwin = new DeviceTwinDevice(deviceState.sCDeviceForRegistryManager.getDeviceId());
            }
            else
            {
                deviceState.sCDeviceForTwin = new DeviceTwinDevice(deviceState.sCDeviceForRegistryManager.getDeviceId(), deviceState.sCModuleForRegistryManager.getId());
            }

            testInstance.twinServiceClient.getTwin(deviceState.sCDeviceForTwin);
            Thread.sleep(DELAY_BETWEEN_OPERATIONS);
        }
    }

    public void tearDownTwin(DeviceState deviceState) throws IOException
    {
        // tear down twin on device client
        if (deviceState != null)
        {
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
        }
        if (internalClient != null)
        {
            internalClient.closeNow();
            internalClient = null;
        }
    }

    public DeviceTwinCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws IOException
    {
        this.testInstance = new DeviceTwinTestInstance(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    public class DeviceTwinTestInstance
    {
        public IotHubClientProtocol protocol;
        public AuthenticationType authenticationType;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public String uuid;
        public ClientType clientType;
        public DeviceTwin twinServiceClient;
        public RegistryManager registryManager;
        public RawTwinQuery rawTwinQueryClient;


        public DeviceTwinTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws IOException
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.publicKeyCert = publicKeyCert;
            this.privateKey = privateKey;
            this.x509Thumbprint = x509Thumbprint;
            this.clientType = clientType;
            this.twinServiceClient = DeviceTwin.createFromConnectionString(iotHubConnectionString, DeviceTwinClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());

            registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
            rawTwinQueryClient = RawTwinQuery.createFromConnectionString(iotHubConnectionString);
        }
    }

    public void setUpNewDeviceAndModule() throws IOException, IotHubException, URISyntaxException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        setUpNewDeviceAndModule(true);
    }

    public void setUpNewDeviceAndModule(boolean openDeviceClient) throws IOException, IotHubException, URISyntaxException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        deviceUnderTest = new DeviceState();
        this.testInstance.uuid = UUID.randomUUID().toString();

        if (this.testInstance.authenticationType == SAS)
        {
            deviceUnderTest.sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceIdPrefix + this.testInstance.uuid, null, null);

            if (this.testInstance.clientType == ClientType.MODULE_CLIENT)
            {
                deviceUnderTest.sCModuleForRegistryManager = com.microsoft.azure.sdk.iot.service.Module.createFromId(deviceIdPrefix + this.testInstance.uuid, moduleIdPrefix + this.testInstance.uuid, null);
            }
        }
        else if (this.testInstance.authenticationType == SELF_SIGNED)
        {
            deviceUnderTest.sCDeviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createDevice(deviceIdPrefix + this.testInstance.uuid, SELF_SIGNED);
            deviceUnderTest.sCDeviceForRegistryManager.setThumbprintFinal(testInstance.x509Thumbprint, testInstance.x509Thumbprint);
        }


        deviceUnderTest.sCDeviceForRegistryManager = Tools.addDeviceWithRetry(testInstance.registryManager, deviceUnderTest.sCDeviceForRegistryManager);

        if (deviceUnderTest.sCModuleForRegistryManager != null)
        {
            Tools.addModuleWithRetry(testInstance.registryManager, deviceUnderTest.sCModuleForRegistryManager);
        }

        Thread.sleep(2000);

        setUpTwin(deviceUnderTest, openDeviceClient);
    }

    @After
    public void tearDownNewDeviceAndModule()
    {
        try
        {
            tearDownTwin(deviceUnderTest);
            testInstance.registryManager.removeDevice(deviceUnderTest.sCDeviceForRegistryManager.getDeviceId());
        }
        catch (Exception e)
        {
            //Don't care if tear down failed. Nightly job will clean up these identities
        }
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
            if (timeElapsed > MAX_WAIT_TIME_FOR_VERIFICATION_MILLISECONDS)
            {
                break;
            }

            actualCount = 0;
            testInstance.twinServiceClient.getTwin(deviceState.sCDeviceForTwin);
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

    protected void readReportedArrayPropertiesAndVerify(DeviceState deviceState, int expectedReportedPropCount) throws IOException, IotHubException, InterruptedException
    {
        int actualCount = 0;

        // Check status periodically for success or until timeout
        long startTime = System.currentTimeMillis();
        long timeElapsed;
        while (expectedReportedPropCount != actualCount)
        {
            Thread.sleep(PERIODIC_WAIT_TIME_FOR_VERIFICATION);
            timeElapsed = System.currentTimeMillis() - startTime;
            if (timeElapsed > MAX_WAIT_TIME_FOR_VERIFICATION_MILLISECONDS)
            {
                break;
            }

            actualCount = 0;
            testInstance.twinServiceClient.getTwin(deviceState.sCDeviceForTwin);
            Set<Pair> repProperties = deviceState.sCDeviceForTwin.getReportedProperties();

            for (Pair p : repProperties)
            {
                // If the contents of the properties are arrays with values, we count them as an correctly set reported property.
                ArrayList<String> val = (ArrayList<String>)p.getValue();
                if (val.size() != 0){
                    actualCount ++;
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
        while (deviceUnderTest.deviceTwinStatus != OK)
        {
            Thread.sleep(PERIODIC_WAIT_TIME_FOR_VERIFICATION);
            timeElapsed = System.currentTimeMillis() - startTime;
            if (timeElapsed > MAX_WAIT_TIME_FOR_VERIFICATION_MILLISECONDS)
            {
                break;
            }
        }
        assertEquals(buildExceptionMessage("Expected OK but was " + deviceUnderTest.deviceTwinStatus, internalClient), OK, deviceUnderTest.deviceTwinStatus);
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

    protected void sendReportedArrayPropertiesAndVerify(int numOfProp) throws IOException, IotHubException, InterruptedException
    {
        // Act
        // send max_prop RP all at once
        deviceUnderTest.dCDeviceForTwin.createNewReportedArrayProperties(numOfProp);
        internalClient.sendReportedProperties(deviceUnderTest.dCDeviceForTwin.getReportedProp());

        // Assert
        waitAndVerifyTwinStatusBecomesSuccess();
        // verify if they are received by SC
        readReportedArrayPropertiesAndVerify(deviceUnderTest, numOfProp);
    }

    protected void waitAndVerifyDesiredPropertyCallback(String propPrefix, boolean withVersion) throws InterruptedException
    {
        // Check status periodically for success or until timeout
        long startTime = System.currentTimeMillis();
        long timeElapsed = 0;

        for (int i = 0; i < deviceUnderTest.dCDeviceForTwin.propertyStateList.length; i++)
        {
            PropertyState propertyState = deviceUnderTest.dCDeviceForTwin.propertyStateList[i];
            while (!propertyState.callBackTriggered || propertyState.propertyNewValue == null)
            {
                Thread.sleep(PERIODIC_WAIT_TIME_FOR_VERIFICATION);
                timeElapsed = System.currentTimeMillis() - startTime;
                if (timeElapsed > MAX_WAIT_TIME_FOR_VERIFICATION_MILLISECONDS)
                {
                    break;
                }
            }
            assertTrue(buildExceptionMessage("Callback was not triggered for one or more properties", internalClient), propertyState.callBackTriggered);

            if(propertyState.propertyNewValue instanceof ArrayList)
            {
                ArrayList<String> propertyValues = (ArrayList<String>)propertyState.propertyNewValue;
                for (String propValue: propertyValues)
                {
                    assertTrue(buildExceptionMessage("Missing the expected prefix, was " + propertyState.propertyNewValue, internalClient), propValue.startsWith(propPrefix));
                }
            }

            if(propertyState.propertyNewValue instanceof String)
            {
                assertTrue(buildExceptionMessage("Missing the expected prefix, was " + propertyState.propertyNewValue, internalClient), ((String) propertyState.propertyNewValue).startsWith(propPrefix));
            }

            if (withVersion)
            {
                assertNotEquals(buildExceptionMessage("Version was not set in the callback", internalClient), (int) propertyState.propertyNewVersion, -1);
            }
        }
    }

    protected void subscribeToDesiredPropertiesAndVerify(int numOfProp, Object propertyValue, Object propertyUpdateValue, String propertyNewValuePrefix) throws IOException, InterruptedException, IotHubException
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
            propertyState.property = new Property(PROPERTY_KEY + i, propertyValue);
            deviceUnderTest.dCDeviceForTwin.propertyStateList[i] = propertyState;
            deviceUnderTest.dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, deviceUnderTest.dCDeviceForTwin, propertyState);
        }

        // act
        internalClient.subscribeToDesiredProperties(deviceUnderTest.dCDeviceForTwin.getDesiredProp());
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        Set<Pair> desiredProperties = new HashSet<>();
        for (int i = 0; i < numOfProp; i++)
        {
            desiredProperties.add(new Pair(PROPERTY_KEY + i, propertyUpdateValue));
        }
        deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
        testInstance.twinServiceClient.updateTwin(deviceUnderTest.sCDeviceForTwin);

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(propertyNewValuePrefix, false);
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

    protected void testGetDeviceTwin() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
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
        testInstance.twinServiceClient.updateTwin(deviceUnderTest.sCDeviceForTwin);
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
        assertEquals(TwinConnectionState.CONNECTED.toString(), deviceUnderTest.sCDeviceForTwin.getConnectionState());
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(PROPERTY_VALUE_UPDATE, true);
    }
}
