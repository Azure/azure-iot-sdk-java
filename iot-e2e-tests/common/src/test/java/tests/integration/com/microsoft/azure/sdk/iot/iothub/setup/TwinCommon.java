/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;

import com.azure.core.credential.AzureSasCredential;
import com.google.gson.JsonParser;
import com.microsoft.azure.sdk.iot.service.twin.TwinConnectionState;
import com.microsoft.azure.sdk.iot.device.twin.Device;
import com.microsoft.azure.sdk.iot.device.twin.Property;
import com.microsoft.azure.sdk.iot.device.twin.TwinPropertyCallback;
import com.microsoft.azure.sdk.iot.device.InternalClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.twin.TwinClientOptions;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestModuleIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
@Slf4j
public class TwinCommon extends IntegrationTest
{
    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs()
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        IntegrationTest.isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        IntegrationTest.isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        List inputs = new ArrayList();
        for (ClientType clientType : ClientType.values())
        {
            if (clientType == ClientType.DEVICE_CLIENT)
            {
                inputs.addAll(Arrays.asList(
                        new Object[][]
                                {
                                        //sas token, device client
                                        {AMQPS, SAS, ClientType.DEVICE_CLIENT},
                                        {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT},
                                        {MQTT, SAS, ClientType.DEVICE_CLIENT},
                                        {MQTT_WS, SAS, ClientType.DEVICE_CLIENT},

                                        //x509, device client
                                        {AMQPS, SELF_SIGNED, ClientType.DEVICE_CLIENT},
                                        {MQTT, SELF_SIGNED, ClientType.DEVICE_CLIENT},
                                }
                ));
            }
            else
            {
                inputs.addAll(Arrays.asList(
                        new Object[][]
                                {
                                        //sas token, module client
                                        {AMQPS, SAS, ClientType.MODULE_CLIENT},
                                        {AMQPS_WS, SAS, ClientType.MODULE_CLIENT},
                                        {MQTT, SAS, ClientType.MODULE_CLIENT},
                                        {MQTT_WS, SAS, ClientType.MODULE_CLIENT}
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
    public static final long START_TWIN_TIMEOUT_MILLISECONDS = 30 * 1000; // 30 seconds

    public static final long DESIRED_PROPERTIES_PROPAGATION_TIME_MILLISECONDS = 5 * 1000; //5 seconds

    // Max reported properties to be tested
    protected static final Integer MAX_PROPERTIES_TO_TEST = 5;

    //Max devices to test
    protected static final Integer MAX_DEVICES = 3;

    protected static String iotHubConnectionString = "";

    // Constants used in for Testing
    protected static final String PROPERTY_KEY = "Key";
    protected static final String PROPERTY_VALUE = "Value";
    protected static final String PROPERTY_VALUE_ARRAY = "[\"Value\",\"Value2\"]" ;
    protected static final String PROPERTY_VALUE_UPDATE = "Update";
    protected static final String PROPERTY_VALUE_UPDATE2 = "Update2";
    protected static final String PROPERTY_VALUE_UPDATE_ARRAY = "[\"1stUpdate1\",\"1stUpdate2\"]";
    protected static final String PROPERTY_VALUE_UPDATE2_ARRAY = "[\"2ndUpdate1\",\"2ndUpdate2\"]";
    protected static final String PROPERTY_VALUE_UPDATE_ARRAY_PREFIX = "1stUpdate";
    protected static final String PROPERTY_VALUE_UPDATE2_ARRAY_PREFIX = "2ndUpdate";
    protected static final String TAG_KEY = "Tag_Key";
    protected static final String TAG_VALUE = "Tag_Value";
    protected static final String TAG_VALUE_UPDATE = "Tag_Value_Update";

    protected DeviceTwinTestInstance testInstance;
    protected static final long ERROR_INJECTION_WAIT_TIMEOUT_MILLISECONDS = 60 * 1000; // 1 minute

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    public class DeviceTwinStatusCallback implements IotHubEventCallback
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
        public Twin sCDeviceForTwin;
        public DeviceExtension dCDeviceForTwin;
        public OnProperty dCOnProperty = new OnProperty();
        public IotHubStatusCode deviceTwinStatus;
    }

    public static class PropertyState
    {
        public boolean callBackTriggered;
        public Property property;
        public Object propertyNewValue;
        public Integer propertyNewVersion;
    }

    public class OnProperty implements TwinPropertyCallback
    {
        @Override
        public void onPropertyChanged(Property property, Object context)
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
        public void onPropertyChanged(String propertyKey, Object propertyValue, Object context)
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

    @SuppressWarnings("SameParameterValue") // Since this is a helper method "numberOfDevices" can be passed any value.
    protected void addMultipleDevices(int numberOfDevices) throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(numberOfDevices, true);
    }

    protected void addMultipleDevices(int numberOfDevices, boolean openDeviceClients) throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        testInstance.devicesUnderTest = new DeviceState[numberOfDevices];
        testInstance.testIdentities = new TestIdentity[numberOfDevices];

        for (int i = 0; i < numberOfDevices; i++)
        {
            testInstance.devicesUnderTest[i] = new DeviceState();

            if (testInstance.clientType == ClientType.DEVICE_CLIENT)
            {
                testInstance.testIdentities[i] = Tools.getTestDevice(iotHubConnectionString, testInstance.protocol, testInstance.authenticationType, true);
                testInstance.devicesUnderTest[i].sCDeviceForRegistryManager = testInstance.testIdentities[i].getDevice();
            }
            else
            {
                testInstance.testIdentities[i] = Tools.getTestModule(iotHubConnectionString, testInstance.protocol, testInstance.authenticationType, true);
                testInstance.devicesUnderTest[i].sCDeviceForRegistryManager = testInstance.testIdentities[i].getDevice();
                testInstance.devicesUnderTest[i].sCModuleForRegistryManager = ((TestModuleIdentity) testInstance.testIdentities[i]).getModule();
            }

            setUpTwin(testInstance.devicesUnderTest[i], openDeviceClients, testInstance.testIdentities[i].getClient());
        }
    }

    protected void setUpTwin(DeviceState deviceState, boolean openDeviceClient, InternalClient client) throws IOException, IotHubException, InterruptedException
    {
        // set up twin on DeviceClient
        deviceState.dCDeviceForTwin = new DeviceExtension();

        if (openDeviceClient)
        {
            client.open(false);
            client.startTwinAsync(new DeviceTwinStatusCallback(), deviceState, deviceState.dCDeviceForTwin, deviceState);
        }

        deviceState.deviceTwinStatus = IotHubStatusCode.ERROR;

        // set up twin on ServiceClient
        if (testInstance.twinServiceClient != null)
        {
            if (testInstance.clientType == ClientType.DEVICE_CLIENT)
            {
                deviceState.sCDeviceForTwin = new Twin(deviceState.sCDeviceForRegistryManager.getDeviceId());
            }
            else
            {
                deviceState.sCDeviceForTwin = new Twin(deviceState.sCDeviceForRegistryManager.getDeviceId(), deviceState.sCModuleForRegistryManager.getId());
            }

            if (testInstance.testIdentity instanceof TestModuleIdentity)
            {
                deviceState.sCDeviceForTwin = testInstance.twinServiceClient.getTwin(deviceState.sCDeviceForTwin.getDeviceId(), deviceState.sCDeviceForTwin.getModuleId());
            }
            else
            {
                deviceState.sCDeviceForTwin = testInstance.twinServiceClient.getTwin(deviceState.sCDeviceForTwin.getDeviceId());
            }

            Thread.sleep(DELAY_BETWEEN_OPERATIONS);
        }
    }

    public TwinCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException
    {
        this.testInstance = new DeviceTwinTestInstance(protocol, authenticationType, clientType);
    }

    public static class DeviceTwinTestInstance
    {
        public IotHubClientProtocol protocol;
        public AuthenticationType authenticationType;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public ClientType clientType;
        public TwinClient twinServiceClient;
        public RegistryManager registryManager;
        public QueryClient queryClient;
        public DeviceState deviceUnderTest;
        public DeviceState[] devicesUnderTest;
        public TestIdentity testIdentity;
        public TestIdentity[] testIdentities; // maps 1:1 to devicesUnderTest array

        public DeviceTwinTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.publicKeyCert = x509CertificateGenerator.getPublicCertificatePEM();
            this.privateKey = x509CertificateGenerator.getPrivateKeyPEM();
            this.x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();
            this.clientType = clientType;
            
            this.twinServiceClient = new TwinClient(iotHubConnectionString, TwinClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
            this.registryManager = new RegistryManager(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
            this.queryClient = new QueryClient(iotHubConnectionString);
        }
    }

    public void setUpNewDeviceAndModule() throws IOException, IotHubException, URISyntaxException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        setUpNewDeviceAndModule(true);
    }

    public void setUpNewDeviceAndModule(boolean openDeviceClient) throws IOException, IotHubException, URISyntaxException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        testInstance.deviceUnderTest = new DeviceState();

        if (testInstance.clientType == ClientType.DEVICE_CLIENT)
        {
            testInstance.testIdentity = Tools.getTestDevice(iotHubConnectionString, testInstance.protocol, testInstance.authenticationType, true);
            testInstance.deviceUnderTest.sCDeviceForRegistryManager = testInstance.testIdentity.getDevice();
        }
        else
        {
            testInstance.testIdentity = Tools.getTestModule(iotHubConnectionString, testInstance.protocol, testInstance.authenticationType, true);
            testInstance.deviceUnderTest.sCDeviceForRegistryManager = testInstance.testIdentity.getDevice();
            testInstance.deviceUnderTest.sCModuleForRegistryManager = ((TestModuleIdentity) testInstance.testIdentity).getModule();
        }

        setUpTwin(testInstance.deviceUnderTest, openDeviceClient, testInstance.testIdentity.getClient());
    }

    @After
    public void cleanup()
    {
        if (testInstance != null)
        {
            if (testInstance.testIdentity != null && testInstance.testIdentity.getClient() != null)
            {
                testInstance.testIdentity.getClient().close();
            }

            Tools.disposeTestIdentity(testInstance.testIdentity, iotHubConnectionString);

            if (testInstance.testIdentities != null)
            {
                for (TestIdentity testIdentity : testInstance.testIdentities)
                {
                    if (testIdentity != null && testIdentity.getClient() != null)
                    {
                        testIdentity.getClient().close();
                    }
                }

                Tools.disposeTestIdentities(Arrays.asList(testInstance.testIdentities), iotHubConnectionString);
            }
        }
    }

    protected void readReportedPropertiesAndVerify(DeviceState deviceState, String startsWithValue, int expectedReportedPropCount) throws IOException, IotHubException, InterruptedException
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

            if (testInstance.testIdentity instanceof TestModuleIdentity)
            {
                deviceState.sCDeviceForTwin = testInstance.twinServiceClient.getTwin(deviceState.sCDeviceForTwin.getDeviceId(), deviceState.sCDeviceForTwin.getModuleId());
            }
            else
            {
                deviceState.sCDeviceForTwin = testInstance.twinServiceClient.getTwin(deviceState.sCDeviceForTwin.getDeviceId());
            }

            Set<Pair> repProperties = deviceState.sCDeviceForTwin.getReportedProperties();

            for (Pair p : repProperties)
            {
                String val = (String) p.getValue();
                if (p.getKey().startsWith(TwinCommon.PROPERTY_KEY) && val.startsWith(startsWithValue))
                {
                    actualCount++;
                }
            }
        }

        assertEquals(buildExceptionMessage("Expected " + expectedReportedPropCount + " but had " + actualCount, testInstance.testIdentity.getClient()), expectedReportedPropCount, actualCount);
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

            if (testInstance.testIdentity instanceof TestModuleIdentity)
            {
                deviceState.sCDeviceForTwin = testInstance.twinServiceClient.getTwin(deviceState.sCDeviceForTwin.getDeviceId(), deviceState.sCDeviceForTwin.getModuleId());
            }
            else
            {
                deviceState.sCDeviceForTwin = testInstance.twinServiceClient.getTwin(deviceState.sCDeviceForTwin.getDeviceId());
            }

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
        assertEquals(buildExceptionMessage("Expected " + expectedReportedPropCount + " but had " + actualCount, testInstance.testIdentity.getClient()), expectedReportedPropCount, actualCount);
    }

    protected void waitAndVerifyTwinStatusBecomesSuccess() throws InterruptedException
    {
        // Check status periodically for success or until timeout
        long startTime = System.currentTimeMillis();
        long timeElapsed;
        while (testInstance.deviceUnderTest.deviceTwinStatus != OK)
        {
            Thread.sleep(PERIODIC_WAIT_TIME_FOR_VERIFICATION);
            timeElapsed = System.currentTimeMillis() - startTime;
            if (timeElapsed > MAX_WAIT_TIME_FOR_VERIFICATION_MILLISECONDS)
            {
                break;
            }
        }
        assertEquals(buildExceptionMessage("Expected OK but was " + testInstance.deviceUnderTest.deviceTwinStatus, testInstance.testIdentity.getClient()), OK, testInstance.deviceUnderTest.deviceTwinStatus);
    }

    protected void sendReportedPropertiesAndVerify(int numOfProp) throws IOException, IotHubException, InterruptedException
    {
        // Act
        // send max_prop RP all at once
        testInstance.deviceUnderTest.dCDeviceForTwin.createNewReportedProperties(numOfProp);
        testInstance.testIdentity.getClient().sendReportedPropertiesAsync(testInstance.deviceUnderTest.dCDeviceForTwin.getReportedProp());

        // Assert
        waitAndVerifyTwinStatusBecomesSuccess();
        // verify if they are received by SC
        readReportedPropertiesAndVerify(testInstance.deviceUnderTest, PROPERTY_VALUE, numOfProp);
    }

    @SuppressWarnings("SameParameterValue") // Since this is a helper method "numOfProp" can be passed any value.
    protected void sendReportedArrayPropertiesAndVerify(int numOfProp) throws IOException, IotHubException, InterruptedException
    {
        // Act
        // send max_prop RP all at once
        testInstance.deviceUnderTest.dCDeviceForTwin.createNewReportedArrayProperties(numOfProp);
        testInstance.testIdentity.getClient().sendReportedPropertiesAsync(testInstance.deviceUnderTest.dCDeviceForTwin.getReportedProp());

        // Assert
        waitAndVerifyTwinStatusBecomesSuccess();
        // verify if they are received by SC
        readReportedArrayPropertiesAndVerify(testInstance.deviceUnderTest, numOfProp);
    }

    protected void waitAndVerifyDesiredPropertyCallback(String propPrefix, boolean withVersion) throws InterruptedException
    {
        // Check status periodically for success or until timeout
        long startTime = System.currentTimeMillis();
        long timeElapsed;

        for (int i = 0; i < testInstance.deviceUnderTest.dCDeviceForTwin.propertyStateList.length; i++)
        {
            PropertyState propertyState = testInstance.deviceUnderTest.dCDeviceForTwin.propertyStateList[i];
            while (!propertyState.callBackTriggered || propertyState.propertyNewValue == null)
            {
                Thread.sleep(PERIODIC_WAIT_TIME_FOR_VERIFICATION);
                timeElapsed = System.currentTimeMillis() - startTime;
                if (timeElapsed > MAX_WAIT_TIME_FOR_VERIFICATION_MILLISECONDS)
                {
                    break;
                }
            }
            assertTrue(buildExceptionMessage("Callback was not triggered for one or more properties", testInstance.testIdentity.getClient()), propertyState.callBackTriggered);

            if(propertyState.propertyNewValue instanceof ArrayList)
            {
                ArrayList<String> propertyValues = (ArrayList<String>)propertyState.propertyNewValue;
                for (String propValue: propertyValues)
                {
                    assertTrue(buildExceptionMessage("Missing the expected prefix, was " + propertyState.propertyNewValue, testInstance.testIdentity.getClient()), propValue.startsWith(propPrefix));
                }
            }

            if(propertyState.propertyNewValue instanceof String)
            {
                assertTrue(buildExceptionMessage("Missing the expected prefix, was " + propertyState.propertyNewValue, testInstance.testIdentity.getClient()), ((String) propertyState.propertyNewValue).startsWith(propPrefix));
            }

            if (withVersion)
            {
                assertNotEquals(buildExceptionMessage("Version was not set in the callback", testInstance.testIdentity.getClient()), (int) propertyState.propertyNewVersion, -1);
            }
        }
    }

    protected void subscribeToDesiredPropertiesAndVerify(int numOfProp, Object propertyValue, Object propertyUpdateValue, String propertyNewValuePrefix) throws IOException, InterruptedException, IotHubException
    {
        // arrange
        if (testInstance.deviceUnderTest != null)
        {
            if (testInstance.deviceUnderTest.sCDeviceForTwin != null)
            {
                testInstance.deviceUnderTest.sCDeviceForTwin.clearDesiredProperties();
            }

            if (testInstance.deviceUnderTest.dCDeviceForTwin != null && testInstance.deviceUnderTest.dCDeviceForTwin.getReportedProp() != null)
            {
                testInstance.deviceUnderTest.dCDeviceForTwin.getDesiredProp().clear();
            }
        }
        testInstance.deviceUnderTest.dCDeviceForTwin.propertyStateList = new PropertyState[numOfProp];
        for (int i = 0; i < numOfProp; i++)
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

        Set<Pair> desiredProperties = new HashSet<>();
        for (int i = 0; i < numOfProp; i++)
        {
            desiredProperties.add(new Pair(PROPERTY_KEY + i, propertyUpdateValue));
        }
        testInstance.deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
        testInstance.twinServiceClient.updateTwin(testInstance.deviceUnderTest.sCDeviceForTwin);

        // assert
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(propertyNewValuePrefix, false);
    }

    protected void setConnectionStatusCallback(final List<com.microsoft.azure.sdk.iot.device.twin.Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates)
    {
        IotHubConnectionStatusChangeCallback connectionStatusUpdateCallback = (status, statusChangeReason, throwable, callbackContext) -> actualStatusUpdates.add(new com.microsoft.azure.sdk.iot.device.twin.Pair<>(status, throwable));

        this.testInstance.testIdentity.getClient().setConnectionStatusChangeCallback(connectionStatusUpdateCallback, null);
    }

    protected void testGetDeviceTwin() throws IOException, InterruptedException, IotHubException
    {
        // arrange
        Map<Property, com.microsoft.azure.sdk.iot.device.twin.Pair<TwinPropertyCallback, Object>> desiredPropertiesCB = new HashMap<>();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            PropertyState propertyState = new PropertyState();
            propertyState.property = new Property(PROPERTY_KEY + i, PROPERTY_VALUE);
            testInstance.deviceUnderTest.dCDeviceForTwin.propertyStateList[i] = propertyState;
            desiredPropertiesCB.put(propertyState.property, new com.microsoft.azure.sdk.iot.device.twin.Pair<>(testInstance.deviceUnderTest.dCOnProperty, propertyState));
        }
        testInstance.testIdentity.getClient().subscribeToTwinDesiredPropertiesAsync(desiredPropertiesCB);
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        Set<Pair> desiredProperties = new HashSet<>();
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            desiredProperties.add(new Pair(PROPERTY_KEY + i, PROPERTY_VALUE_UPDATE + UUID.randomUUID()));
        }
        testInstance.deviceUnderTest.sCDeviceForTwin.setDesiredProperties(desiredProperties);
        testInstance.twinServiceClient.updateTwin(testInstance.deviceUnderTest.sCDeviceForTwin);
        Thread.sleep(DELAY_BETWEEN_OPERATIONS);

        for (int i = 0; i < testInstance.deviceUnderTest.dCDeviceForTwin.propertyStateList.length; i++)
        {
            PropertyState propertyState = testInstance.deviceUnderTest.dCDeviceForTwin.propertyStateList[i];
            propertyState.callBackTriggered = false;
            propertyState.propertyNewVersion = -1;
        }

        // act
        testInstance.testIdentity.getClient().getTwinAsync();

        // assert
        assertEquals(TwinConnectionState.CONNECTED.toString(), testInstance.deviceUnderTest.sCDeviceForTwin.getConnectionState());
        waitAndVerifyTwinStatusBecomesSuccess();
        waitAndVerifyDesiredPropertyCallback(PROPERTY_VALUE_UPDATE, true);
    }

    protected static TwinClient buildDeviceTwinClientWithAzureSasCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential azureSasCredential = new AzureSasCredential(serviceSasToken.toString());
        TwinClientOptions options = TwinClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build();
        return new TwinClient(iotHubConnectionStringObj.getHostName(), azureSasCredential, options);
    }
}
