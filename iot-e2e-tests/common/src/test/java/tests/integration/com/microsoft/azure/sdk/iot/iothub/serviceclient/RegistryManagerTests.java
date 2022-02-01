/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;


import com.azure.core.credential.AzureSasCredential;
import com.microsoft.azure.sdk.iot.service.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.service.Configuration;
import com.microsoft.azure.sdk.iot.service.ConfigurationContent;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubBadFormatException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenTools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.UUID;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/**
 * Test class containing all tests to be run on JVM and android pertaining to identity CRUD.
 */
@Slf4j
@IotHubTest
public class RegistryManagerTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";

    private static final String deviceIdPrefix = "java-crud-e2e-test-";
    private static final String moduleIdPrefix = "java-crud-module-e2e-test-";
    private static final String configIdPrefix = "java-crud-adm-e2e-test-";
    private static String hostName;
    private static final String primaryThumbprint =   "0000000000000000000000000000000000000000";
    private static final String secondaryThumbprint = "1111111111111111111111111111111111111111";
    private static final String primaryThumbprint2 =   "2222222222222222222222222222222222222222";
    private static final String secondaryThumbprint2 = "3333333333333333333333333333333333333333";

    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8879;

    @BeforeClass
    public static void setUp() throws IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString).getHostName();
    }

    public RegistryManagerTests.RegistryManagerTestInstance testInstance;

    public static class RegistryManagerTestInstance
    {
        public String deviceId;
        public String moduleId;
        public String configId;
        private RegistryManager registryManager;

        public RegistryManagerTestInstance()
        {
            this(RegistryManagerOptions.builder().build());
        }

        public RegistryManagerTestInstance(RegistryManager registryManager)
        {
            String uuid = UUID.randomUUID().toString();
            this.deviceId = deviceIdPrefix + uuid;
            this.moduleId = moduleIdPrefix + uuid;
            this.configId = configIdPrefix + uuid;
            this.registryManager = registryManager;
        }

        public RegistryManagerTestInstance(RegistryManagerOptions options)
        {
            String uuid = UUID.randomUUID().toString();
            this.deviceId = deviceIdPrefix + uuid;
            this.moduleId = moduleIdPrefix + uuid;
            this.configId = configIdPrefix + uuid;
            this.registryManager = new RegistryManager(iotHubConnectionString, options);
        }
    }

    @BeforeClass
    public static void startProxy()
    {
        proxyServer = DefaultHttpProxyServer.bootstrap()
                .withPort(testProxyPort)
                .start();
    }

    @AfterClass
    public static void stopProxy()
    {
        proxyServer.stop();
    }

    @Test
    public void deviceLifecycleWithConnectionString() throws Exception
    {
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        deviceLifecycle(testInstance);
    }

    @Test
    public void serviceValidatesSymmetricKey() throws IOException, IotHubException
    {
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        Device device = Device.createDevice(testInstance.deviceId, AuthenticationType.SAS);
        SymmetricKey symmetricKey = new SymmetricKey();
        symmetricKey.setPrimaryKey("1");
        symmetricKey.setSecondaryKey("2");
        device.setSymmetricKey(symmetricKey);
        try
        {
            testInstance.registryManager.addDevice(device);
            fail("Adding the device should have failed since an invalid symmetric key was provided");
        }
        catch (IotHubBadFormatException ex)
        {
            // expected throw
        }
    }

    @Test
    public void deviceLifecycleWithAzureSasCredential() throws Exception
    {
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance(buildRegistryManagerWithAzureSasCredential());
        deviceLifecycle(testInstance);
    }

    @Test
    public void registryManagerTokenRenewalWithAzureSasCredential() throws Exception
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential azureSasCredential = new AzureSasCredential(serviceSasToken.toString());
        RegistryManager registryManager = new RegistryManager(iotHubConnectionStringObj.getHostName(), azureSasCredential);

        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance(registryManager);

        Device device1 = Device.createDevice(testInstance.deviceId + "-1", AuthenticationType.SAS);
        Device device2 = Device.createDevice(testInstance.deviceId + "-2", AuthenticationType.SAS);
        Device device3 = Device.createDevice(testInstance.deviceId + "-3", AuthenticationType.SAS);

        azureSasCredential.update(serviceSasToken.toString());

        // add first device just to make sure that the first credential update worked
        testInstance.registryManager.addDevice(device1);

        // deliberately expire the SAS token to provoke a 401 to ensure that the registry manager is using the shared
        // access signature that is set here.
        azureSasCredential.update(SasTokenTools.makeSasTokenExpired(serviceSasToken.toString()));

        try
        {
            testInstance.registryManager.addDevice(device2);
            fail("Expected adding a device to throw unauthorized exception since an expired SAS token was used, but no exception was thrown");
        }
        catch (IotHubUnathorizedException e)
        {
            log.debug("IotHubUnauthorizedException was thrown as expected, continuing test");
        }

        // Renew the expired shared access signature
        serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        azureSasCredential.update(serviceSasToken.toString());

        // adding the final device should succeed since the shared access signature has been renewed
        testInstance.registryManager.addDevice(device3);
    }

    public static void deviceLifecycle(RegistryManagerTestInstance testInstance) throws Exception
    {
        //-Create-//
        Device deviceAdded = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(testInstance.registryManager, deviceAdded);

        //-Read-//
        Device deviceRetrieved = testInstance.registryManager.getDevice(testInstance.deviceId);

        //-Update-//
        Device deviceUpdated = testInstance.registryManager.getDevice(testInstance.deviceId);
        deviceUpdated.setStatus(DeviceStatus.Disabled);
        deviceUpdated = testInstance.registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        testInstance.registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), DeviceStatus.Disabled, deviceUpdated.getStatus());
        assertTrue(buildExceptionMessage("", hostName), deviceWasDeletedSuccessfully(testInstance.registryManager, testInstance.deviceId));
    }

    @Test
    public void deviceLifecycleWithProxy() throws Exception
    {
        Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
        ProxyOptions proxyOptions = new ProxyOptions(testProxy);
        RegistryManagerOptions registryManagerOptions = RegistryManagerOptions.builder().proxyOptions(proxyOptions).build();
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance(registryManagerOptions);

        //-Create-//
        Device deviceAdded = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(testInstance.registryManager, deviceAdded);

        //-Read-//
        Device deviceRetrieved = testInstance.registryManager.getDevice(testInstance.deviceId);

        //-Update-//
        Device deviceUpdated = testInstance.registryManager.getDevice(testInstance.deviceId);
        deviceUpdated.setStatus(DeviceStatus.Disabled);
        deviceUpdated = testInstance.registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        testInstance.registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), DeviceStatus.Disabled, deviceUpdated.getStatus());
        assertTrue(buildExceptionMessage("", hostName), deviceWasDeletedSuccessfully(testInstance.registryManager, testInstance.deviceId));
    }

    @Test
    public void crud_device_e2e_X509_CA_signed() throws Exception
    {
        //-Create-//
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        Device deviceAdded = Device.createDevice(testInstance.deviceId, AuthenticationType.CERTIFICATE_AUTHORITY);
        Tools.addDeviceWithRetry(testInstance.registryManager, deviceAdded);

        //-Read-//
        Device deviceRetrieved = testInstance.registryManager.getDevice(testInstance.deviceId);

        //-Update-//
        Device deviceUpdated = testInstance.registryManager.getDevice(testInstance.deviceId);
        deviceUpdated.setStatus(DeviceStatus.Disabled);
        deviceUpdated = testInstance.registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        testInstance.registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.CERTIFICATE_AUTHORITY, deviceRetrieved.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), DeviceStatus.Disabled, deviceUpdated.getStatus());
        assertNull(buildExceptionMessage("", hostName), deviceAdded.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), deviceAdded.getSecondaryKey());
        assertNull(buildExceptionMessage("", hostName), deviceRetrieved.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), deviceRetrieved.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), deviceWasDeletedSuccessfully(testInstance.registryManager, testInstance.deviceId));
    }

    @Test
    public void crud_device_e2e_X509_self_signed() throws Exception
    {
        //-Create-//
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        Device deviceAdded = Device.createDevice(testInstance.deviceId, AuthenticationType.SELF_SIGNED);
        deviceAdded.setThumbprint(primaryThumbprint, secondaryThumbprint);
        Tools.addDeviceWithRetry(testInstance.registryManager, deviceAdded);

        //-Read-//
        Device deviceRetrieved = testInstance.registryManager.getDevice(testInstance.deviceId);

        //-Update-//
        Device deviceUpdated = testInstance.registryManager.getDevice(testInstance.deviceId);
        deviceUpdated.setThumbprint(primaryThumbprint2, secondaryThumbprint2);
        deviceUpdated = testInstance.registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        testInstance.registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, deviceAdded.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, deviceRetrieved.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, deviceAdded.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, deviceAdded.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, deviceRetrieved.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, deviceRetrieved.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint2, deviceUpdated.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint2, deviceUpdated.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), deviceWasDeletedSuccessfully(testInstance.registryManager, testInstance.deviceId));
    }

    //TODO what is this testing?
    @Test
    @ContinuousIntegrationTest
    public void getDeviceStatisticsTest() throws Exception
    {
        RegistryManager registryManager = new RegistryManager(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        Tools.getStatisticsWithRetry(registryManager);
    }

    @Test
    @StandardTierHubOnlyTest
    public void crud_module_e2e() throws Exception
    {
        // Arrange
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        SymmetricKey expectedSymmetricKey = new SymmetricKey();

        Device deviceSetup = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(testInstance.registryManager, deviceSetup);
        deleteModuleIfItExistsAlready(testInstance.registryManager, testInstance.deviceId, testInstance.moduleId);

        //-Create-//
        Module moduleAdded = Module.createFromId(testInstance.deviceId, testInstance.moduleId, null);
        Tools.addModuleWithRetry(testInstance.registryManager, moduleAdded);

        //-Read-//
        Module moduleRetrieved = testInstance.registryManager.getModule(testInstance.deviceId, testInstance.moduleId);

        //-Update-//
        Module moduleUpdated = testInstance.registryManager.getModule(testInstance.deviceId, testInstance.moduleId);
        moduleUpdated.getSymmetricKey().setPrimaryKey(expectedSymmetricKey.getPrimaryKey());
        moduleUpdated = testInstance.registryManager.updateModule(moduleUpdated);

        //-Delete-//
        testInstance.registryManager.removeModule(testInstance.deviceId, testInstance.moduleId);
        testInstance.registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleRetrieved.getId());
        assertEquals(buildExceptionMessage("", hostName), expectedSymmetricKey.getPrimaryKey(), moduleUpdated.getPrimaryKey());
        assertTrue(buildExceptionMessage("", hostName), moduleWasDeletedSuccessfully(testInstance.registryManager, testInstance.deviceId, testInstance.moduleId));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void crud_module_e2e_X509_CA_signed() throws Exception
    {
        // Arrange
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        deleteModuleIfItExistsAlready(testInstance.registryManager, testInstance.deviceId, testInstance.moduleId);
        Device deviceSetup = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(testInstance.registryManager, deviceSetup);
        deleteModuleIfItExistsAlready(testInstance.registryManager, testInstance.deviceId, testInstance.moduleId);

        //-Create-//
        Module moduleAdded = Module.createModule(testInstance.deviceId, testInstance.moduleId, AuthenticationType.CERTIFICATE_AUTHORITY);
        Tools.addModuleWithRetry(testInstance.registryManager, moduleAdded);

        //-Read-//
        Module moduleRetrieved = testInstance.registryManager.getModule(testInstance.deviceId, testInstance.moduleId);

        //-Delete-//
        testInstance.registryManager.removeModule(testInstance.deviceId, testInstance.moduleId);
        testInstance.registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleRetrieved.getId());
        assertNull(buildExceptionMessage("", hostName), moduleAdded.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), moduleAdded.getSecondaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), moduleRetrieved.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), moduleRetrieved.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), moduleWasDeletedSuccessfully(testInstance.registryManager, testInstance.deviceId, testInstance.moduleId));
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void crud_module_e2e_X509_self_signed() throws Exception
    {
        // Arrange
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        Device deviceSetup = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(testInstance.registryManager, deviceSetup);
        deleteModuleIfItExistsAlready(testInstance.registryManager, testInstance.deviceId, testInstance.moduleId);

        //-Create-//
        Module moduleAdded = Module.createModule(testInstance.deviceId, testInstance.moduleId, AuthenticationType.SELF_SIGNED);
        moduleAdded.setThumbprint(primaryThumbprint, secondaryThumbprint);
        Tools.addModuleWithRetry(testInstance.registryManager, moduleAdded);

        //-Read-//
        Module moduleRetrieved = testInstance.registryManager.getModule(testInstance.deviceId, testInstance.moduleId);

        //-Update-//
        Module moduleUpdated = testInstance.registryManager.getModule(testInstance.deviceId, testInstance.moduleId);
        moduleUpdated.setThumbprint(primaryThumbprint2, secondaryThumbprint2);
        moduleUpdated = testInstance.registryManager.updateModule(moduleUpdated);

        //-Delete-//
        testInstance.registryManager.removeModule(testInstance.deviceId, testInstance.moduleId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleRetrieved.getId());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, moduleAdded.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, moduleRetrieved.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, moduleAdded.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, moduleAdded.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, moduleRetrieved.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, moduleRetrieved.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint2, moduleUpdated.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint2, moduleUpdated.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), moduleWasDeletedSuccessfully(testInstance.registryManager, testInstance.deviceId, testInstance.moduleId));
    }

    @Test
    @StandardTierHubOnlyTest
    public void crud_adm_configuration_e2e() throws Exception
    {
        // Arrange
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        final HashMap<String, Object> testDeviceContent = new HashMap<String, Object>()
        {
            {
                put("properties.desired.chiller-water", new HashMap<String, Object>()
                        {
                            {
                                put("temperature", 66);
                                put("pressure", 28);
                            }
                        }
                );
            }
        };

        //-Create-//
        Configuration configAdded = new Configuration(testInstance.configId);

        ConfigurationContent content = new ConfigurationContent();
        content.setDeviceContent(testDeviceContent);
        configAdded.setContent(content);
        configAdded.getMetrics().setQueries(new HashMap<String, String>(){{put("waterSettingsPending",
                "SELECT deviceId FROM devices WHERE properties.reported.chillerWaterSettings.status=\'pending\'");}});
        configAdded.setTargetCondition("properties.reported.chillerProperties.model=\'4000x\'");
        configAdded.setPriority(20);
        testInstance.registryManager.addConfiguration(configAdded);

        //-Read-//
        Configuration configRetrieved = testInstance.registryManager.getConfiguration(testInstance.configId);

        //-Update-//
        Configuration configUpdated = testInstance.registryManager.getConfiguration(testInstance.configId);
        configUpdated.setPriority(1);
        configUpdated = testInstance.registryManager.updateConfiguration(configUpdated);

        //-Delete-//
        testInstance.registryManager.removeConfiguration(testInstance.configId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.configId, configAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.configId, configRetrieved.getId());
        String actualString = configRetrieved.getContent().getDeviceContent().get("properties.desired.chiller-water").toString();
        actualString = actualString.substring(1, actualString.length()-1);
        String[] keyValuePairs = actualString.split(",");
        HashMap<String, String> actualMap = new HashMap<>();
        for (String pair : keyValuePairs)
        {
            String[] entry = pair.split("=");
            actualMap.put(entry[0].trim(), entry[1].trim());
        }
        assertEquals(buildExceptionMessage("", hostName), "66.0", actualMap.get("temperature"));
        assertEquals(buildExceptionMessage("", hostName), "28.0", actualMap.get("pressure"));
        assertEquals(buildExceptionMessage("", hostName), "SELECT deviceId FROM devices WHERE properties.reported.chillerWaterSettings.status=\'pending\'",
                configRetrieved.getMetrics().getQueries().get("waterSettingsPending"));
        assertEquals(buildExceptionMessage("", hostName), "properties.reported.chillerProperties.model=\'4000x\'",
                configRetrieved.getTargetCondition());
        assertEquals(buildExceptionMessage("", hostName), new Integer(20), configRetrieved.getPriority());
        assertEquals(buildExceptionMessage("", hostName), testInstance.configId, configUpdated.getId());
        assertEquals(buildExceptionMessage("", hostName), new Integer(1), configUpdated.getPriority());
        assertTrue(buildExceptionMessage("", hostName), configWasDeletedSuccessfully(testInstance.registryManager, testInstance.configId));
    }

    @Test
    @StandardTierHubOnlyTest
    public void apply_configuration_e2e() throws Exception
    {
        // Arrange
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        Device deviceSetup = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(testInstance.registryManager, deviceSetup);
        final HashMap<String, Object> testDeviceContent = new HashMap<String, Object>()
        {
            {
                put("properties.desired.chiller-water", new HashMap<String, Object>()
                        {
                            {
                                put("temperature", 66);
                                put("pressure", 28);
                            }
                        }
                );
            }
        };
        ConfigurationContent content = new ConfigurationContent();
        content.setDeviceContent(testDeviceContent);

        boolean expectedExceptionThrown = false;

        // Act
        try
        {
            testInstance.registryManager.applyConfigurationContentOnDevice(testInstance.deviceId, content);
        }
        catch (IotHubBadFormatException e)
        {
            expectedExceptionThrown = true;
        }

        assertTrue("Bad format exception wasn't thrown but was expected", expectedExceptionThrown);
    }

    @StandardTierHubOnlyTest
    @Test
    @ContinuousIntegrationTest
    public void deviceCreationWithDeviceScope() throws IOException, InterruptedException, IotHubException, URISyntaxException
    {
        // Arrange
        this.testInstance = new RegistryManagerTestInstance();
        String edge1Id = deviceIdPrefix + UUID.randomUUID().toString();
        String edge2Id = deviceIdPrefix + UUID.randomUUID().toString();
        String deviceId = this.testInstance.deviceId;

        //-Create-//
        Device edgeDevice1 = Device.createFromId(edge1Id, DeviceStatus.Enabled, null);
        DeviceCapabilities capabilities = new DeviceCapabilities();
        capabilities.setIotEdge(true);
        edgeDevice1.setCapabilities(capabilities);
        edgeDevice1 = Tools.addDeviceWithRetry(this.testInstance.registryManager, edgeDevice1);

        Device edgeDevice2 = Device.createFromId(edge2Id, DeviceStatus.Enabled, null);
        capabilities.setIotEdge(true);
        edgeDevice2.setCapabilities(capabilities);
        edgeDevice2.getParentScopes().add(edgeDevice1.getScope()); // set edge1 as parent
        edgeDevice2 = Tools.addDeviceWithRetry(this.testInstance.registryManager, edgeDevice2);

        Device leafDevice = Device.createFromId(
                deviceId,
                DeviceStatus.Enabled,
                null);
        assertNotNull(edgeDevice1.getScope());
        leafDevice.setScope(edgeDevice1.getScope());
        Tools.addDeviceWithRetry(this.testInstance.registryManager, leafDevice);

        //-Read-//
        Device deviceRetrieved = this.testInstance.registryManager.getDevice(deviceId);

        //-Delete-//
        this.testInstance.registryManager.removeDevice(edge1Id);
        this.testInstance.registryManager.removeDevice(edge2Id);
        this.testInstance.registryManager.removeDevice(deviceId);

        // Assert
        assertEquals(
                buildExceptionMessage(
                        "Edge parent scope did not match parent's device scope",
                        hostName),
                edgeDevice2.getParentScopes().get(0),
                edgeDevice1.getScope());
        assertNotEquals(
                buildExceptionMessage(
                        "Child edge device scope should be it's own",
                        hostName),
                edgeDevice2.getScope(),
                edgeDevice1.getScope());
        assertEquals(
                buildExceptionMessage(
                    "Registered device Id is not correct",
                    hostName),
                deviceId,
                leafDevice.getDeviceId());
        assertEquals(
                buildExceptionMessage(
                        "Device scopes did not match",
                        hostName),
                deviceRetrieved.getScope(),
                edgeDevice1.getScope());
        assertEquals(
                buildExceptionMessage(
                        "Device's first parent scope did not match device scope",
                        hostName),
                deviceRetrieved.getParentScopes().get(0),
                deviceRetrieved.getScope());
    }
    
    private static void deleteDeviceIfItExistsAlready(RegistryManager registryManager, String deviceId) throws IOException, InterruptedException
    {
        try
        {
            Tools.getDeviceWithRetry(registryManager, deviceId);

            //if no exception yet, identity exists so it can be deleted
            try
            {
                registryManager.removeDevice(deviceId);
            }
            catch (IotHubException | IOException e)
            {
                System.out.println("Initialization failed, could not remove device: " + deviceId);
            }
        }
        catch (IotHubException e)
        {
        }
    }

    private static void deleteModuleIfItExistsAlready(RegistryManager registryManager, String deviceId, String moduleId) throws IOException, InterruptedException
    {
        try
        {
            Tools.getModuleWithRetry(registryManager, deviceId, moduleId);

            //if no exception yet, identity exists so it can be deleted
            try
            {
                registryManager.removeModule(deviceId, moduleId);
            }
            catch (IotHubException | IOException e)
            {
                System.out.println("Initialization failed, could not remove deviceId/moduleId: " + deviceId + "/" + moduleId);
            }
        }
        catch (IotHubException e)
        {
        }
    }

    private static boolean deviceWasDeletedSuccessfully(RegistryManager registryManager, String deviceId) throws IOException
    {
        try
        {
            registryManager.getDevice(deviceId);
        }
        catch (IotHubException e)
        {
            // identity should have been deleted, so this catch is expected
            return true;
        }

        // identity could still be retrieved, so it was not deleted successfully
        return false;
    }

    private static boolean moduleWasDeletedSuccessfully(RegistryManager registryManager, String deviceId, String moduleId) throws IOException
    {
        try
        {
            registryManager.getModule(deviceId, moduleId);
        }
        catch (IotHubException e)
        {
            // module should have been deleted, so this catch is expected
            return true;
        }

        // module could still be retrieved, so it was not deleted successfully
        return false;
    }

    private static boolean configWasDeletedSuccessfully(RegistryManager registryManager, String configId) throws IOException
    {
        try
        {
            registryManager.getConfiguration(configId);
        }
        catch (IotHubException e)
        {
            // configuration should have been deleted, so this catch is expected
            return true;
        }

        // configuration could still be retrieved, so it was not deleted successfully
        return false;
    }

    private static RegistryManager buildRegistryManagerWithAzureSasCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential azureSasCredential = new AzureSasCredential(serviceSasToken.toString());
        RegistryManagerOptions options = RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build();
        return new RegistryManager(iotHubConnectionStringObj.getHostName(), azureSasCredential, options);
    }
}