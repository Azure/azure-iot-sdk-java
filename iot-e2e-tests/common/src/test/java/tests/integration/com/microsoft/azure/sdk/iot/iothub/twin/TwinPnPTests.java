package tests.integration.com.microsoft.azure.sdk.iot.iothub.twin;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.Module;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceConnectionString;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static org.junit.Assert.assertEquals;

/**
 * Test class containing all tests to be run for PnP.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class TwinPnPTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";
    private static RegistryManager registryManager;
    private static final String ModelId = "dtmi:com:test:e2e;1";

    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs() throws IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        List inputs =  Arrays.asList(
                new Object[][]
                        {
                                {MQTT},
                                {MQTT_WS},
                        }
        );

        return inputs;
    }

    private static class TestDevice
    {
        String deviceId;
        String moduleId;
        DeviceClient deviceClient;
        ModuleClient moduleClient;
    }

    public TwinPnPTests.TwinPnPTestInstance testInstance;

    public TwinPnPTests(IotHubClientProtocol protocol) throws IOException
    {
        this.testInstance = new TwinPnPTestInstance(protocol);
    }

    public class TwinPnPTestInstance
    {
        public IotHubClientProtocol protocol;
        private Device deviceForRegistryManager;
        private Module moduleForRegistryManager;

        private DeviceTwin sCDeviceTwin;
        private TwinPnPTests.TestDevice testDevice;

        public TwinPnPTestInstance(IotHubClientProtocol protocol) throws IOException
        {
            this.protocol = protocol;
            this.sCDeviceTwin = DeviceTwin.createFromConnectionString(iotHubConnectionString);
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

    private void createDeviceWithClientOptions(IotHubClientProtocol protocol, ClientOptions options) throws IOException, URISyntaxException, InterruptedException
    {
        testInstance.testDevice.deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, testInstance.deviceForRegistryManager), protocol, options);
        testInstance.testDevice.deviceClient.open();
    }

    private void createModuleWithClientOptions(IotHubClientProtocol protocol, ClientOptions options) throws IOException, URISyntaxException, InterruptedException, ModuleClientException {
        testInstance.testDevice.moduleClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, testInstance.deviceForRegistryManager, testInstance.moduleForRegistryManager), protocol, options);
        testInstance.testDevice.moduleClient.open();
    }

    @Before
    public void createDevice() throws Exception
    {
        testInstance.testDevice = new TestDevice();
        testInstance.testDevice.deviceId = "java-twin-PnP-e2e-test-".concat(UUID.randomUUID().toString());
        testInstance.testDevice.moduleId = "java-twin-PnP-module-e2e-test-".concat(UUID.randomUUID().toString());

        testInstance.deviceForRegistryManager = com.microsoft.azure.sdk.iot.service.Device.createFromId(testInstance.testDevice.deviceId, null, null);
        testInstance.deviceForRegistryManager = Tools.addDeviceWithRetry(registryManager, testInstance.deviceForRegistryManager);
        testInstance.moduleForRegistryManager = com.microsoft.azure.sdk.iot.service.Module.createFromId(testInstance.testDevice.deviceId, testInstance.testDevice.moduleId,null);
        testInstance.moduleForRegistryManager = Tools.addModuleWithRetry(registryManager, testInstance.moduleForRegistryManager);

    }

    @After
    public void destroyDevice() throws Exception
    {
        if (testInstance.testDevice.deviceClient != null)
        {
            testInstance.testDevice.deviceClient.closeNow();
            testInstance.testDevice.deviceClient = null;
        }
        if (testInstance.testDevice.moduleClient != null)
        {
            testInstance.testDevice.moduleClient.closeNow();
            testInstance.testDevice.moduleClient = null;
        }

        if (testInstance != null && testInstance.testDevice != null)
        {
            if (registryManager != null && testInstance.testDevice.deviceId != null && testInstance.testDevice.moduleId !=null)
            {
                registryManager.removeModule(testInstance.testDevice.deviceId, testInstance.testDevice.moduleId);
                registryManager.removeDevice(testInstance.testDevice.deviceId);
            }
        }
    }

    @Test
    @StandardTierHubOnlyTest
    public void testDeviceGetTwinWithModelId() throws IOException, InterruptedException, URISyntaxException, IotHubException
    {
        // arrange
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setModelId(ModelId);
        createDeviceWithClientOptions(testInstance.protocol, clientOptions);
        DeviceTwinDevice twin = new DeviceTwinDevice(testInstance.testDevice.deviceId);

        // act
        testInstance.sCDeviceTwin.getTwin(twin);

        // assert
        assertEquals(ModelId, twin.getModelId());
    }

    @Test
    @StandardTierHubOnlyTest
    public void testModuleGetTwinWithModelId() throws IOException, InterruptedException, URISyntaxException, IotHubException, ModuleClientException {
        // arrange
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setModelId(ModelId);
        createModuleWithClientOptions(testInstance.protocol, clientOptions);
        DeviceTwinDevice twin = new DeviceTwinDevice(testInstance.testDevice.deviceId, testInstance.testDevice.moduleId);

        // act
        testInstance.sCDeviceTwin.getTwin(twin);

        // assert
        assertEquals(ModelId, twin.getModelId());
    }
}
