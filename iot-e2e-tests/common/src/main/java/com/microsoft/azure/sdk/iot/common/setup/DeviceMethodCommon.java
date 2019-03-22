/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.common.setup;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Utility functions, setup and teardown for all device method integration tests. This class should not contain any tests,
 * but any children class should.
 */
public class DeviceMethodCommon extends IntegrationTest
{
    protected static String iotHubConnectionString = "";

    protected static DeviceMethod methodServiceClient;
    protected static RegistryManager registryManager;

    protected static final Long RESPONSE_TIMEOUT = TimeUnit.SECONDS.toSeconds(200);
    protected static final Long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toSeconds(5);
    protected static final String PAYLOAD_STRING = "This is a valid payload";

    protected static final int NUMBER_INVOKES_PARALLEL = 10;
    protected static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;
    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    private List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates;

    protected DeviceMethodTestInstance testInstance;
    protected static final long ERROR_INJECTION_WAIT_TIMEOUT = 1 * 60 * 1000; // 1 minute

    protected static Collection inputsCommon(ClientType clientType) throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, InterruptedException, ModuleClientException
    {
        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        return inputsCommon(clientType, certificateGenerator.getPublicCertificate(), certificateGenerator.getPrivateKey(), certificateGenerator.getX509Thumbprint());
    }

    protected static Collection inputsCommon(ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, InterruptedException, ModuleClientException
    {
        methodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        ArrayList<DeviceTestManager> deviceTestManagers = new ArrayList<>();

        String TEST_UUID = UUID.randomUUID().toString();

        /* Create unique device names */
        String deviceId = "java-method-e2e-test-device".concat("-" + TEST_UUID);
        String moduleId = "java-method-e2e-test-module".concat("-" + TEST_UUID);
        String deviceX509Id = "java-method-e2e-test-device-x509".concat("-" + TEST_UUID);
        String moduleX509Id = "java-method-e2e-test-module-x509".concat("-" + TEST_UUID);

        /* Create device on the service */
        Device device = Device.createFromId(deviceId, null, null);
        Module module = Module.createFromId(deviceId, moduleId, null);

        Device deviceX509 = Device.createDevice(deviceX509Id, AuthenticationType.SELF_SIGNED);
        deviceX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
        Module moduleX509 = Module.createModule(deviceX509Id, moduleX509Id, AuthenticationType.SELF_SIGNED);
        moduleX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);

        Collection<Object[]> inputs = new ArrayList<>();

        /* Add devices to the IoTHub */
        device = Tools.addDeviceWithRetry(registryManager, device);
        deviceX509 = Tools.addDeviceWithRetry(registryManager, deviceX509);

        if (clientType == ClientType.MODULE_CLIENT)
        {
            module = Tools.addModuleWithRetry(registryManager, module);
            moduleX509 = Tools.addModuleWithRetry(registryManager, moduleX509);
        }

        Thread.sleep(2000);

        for (IotHubClientProtocol protocol : IotHubClientProtocol.values())
        {
            if (protocol != HTTPS)
            {
                if (clientType == ClientType.DEVICE_CLIENT)
                {
                    //sas device client
                    DeviceClient deviceClient = new DeviceClient(registryManager.getDeviceConnectionString(device), protocol);
                    DeviceTestManager deviceClientSasTestManager = new DeviceTestManager(deviceClient);
                    deviceTestManagers.add(deviceClientSasTestManager);
                    inputs.add(makeSubArray(deviceClientSasTestManager, protocol, SAS, ClientType.DEVICE_CLIENT, device, publicKeyCert, privateKey, x509Thumbprint));
                }
                else if (clientType == ClientType.MODULE_CLIENT)
                {
                    //sas module client
                    ModuleClient moduleClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), protocol);
                    DeviceTestManager moduleClientSasTestManager = new DeviceTestManager(moduleClient);
                    deviceTestManagers.add(moduleClientSasTestManager);
                    inputs.add(makeSubArray(moduleClientSasTestManager, protocol, SAS, ClientType.MODULE_CLIENT, module, publicKeyCert, privateKey, x509Thumbprint));
                }

                if (protocol != MQTT_WS && protocol != AMQPS_WS)
                {
                    if (clientType == ClientType.DEVICE_CLIENT)
                    {
                        //x509 device client
                        DeviceClient deviceClientX509 = new DeviceClient(registryManager.getDeviceConnectionString(deviceX509), protocol, publicKeyCert, false, privateKey, false);
                        DeviceTestManager deviceClientX509TestManager = new DeviceTestManager(deviceClientX509);
                        deviceTestManagers.add(deviceClientX509TestManager);
                        inputs.add(makeSubArray(deviceClientX509TestManager, protocol, SELF_SIGNED, ClientType.DEVICE_CLIENT, deviceX509, publicKeyCert, privateKey, x509Thumbprint));
                    }
                    else if (clientType == ClientType.MODULE_CLIENT)
                    {
                        //x509 module client
                        ModuleClient moduleClientX509 = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), protocol, publicKeyCert, false, privateKey, false);
                        DeviceTestManager moduleClientX509TestManager = new DeviceTestManager(moduleClientX509);
                        deviceTestManagers.add(moduleClientX509TestManager);
                        inputs.add(makeSubArray(moduleClientX509TestManager, protocol, SELF_SIGNED, ClientType.MODULE_CLIENT, moduleX509, publicKeyCert, privateKey, x509Thumbprint));
                    }
                }
            }
        }

        return inputs;
    }

    private static Object[] makeSubArray(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, BaseDevice identity, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        Object[] inputSubArray = new Object[8];
        inputSubArray[0] = deviceTestManager;
        inputSubArray[1] = protocol;
        inputSubArray[2] = authenticationType;
        inputSubArray[3] = clientType;
        inputSubArray[4] = identity;
        inputSubArray[5] = publicKeyCert;
        inputSubArray[6] = privateKey;
        inputSubArray[7] = x509Thumbprint;
        return inputSubArray;
    }

    protected DeviceMethodCommon(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, BaseDevice identity, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        this.testInstance = new DeviceMethodTestInstance(deviceTestManager, protocol, authenticationType, clientType, identity, publicKeyCert, privateKey, x509Thumbprint);
    }

    public class DeviceMethodTestInstance
    {
        public DeviceTestManager deviceTestManager;
        public IotHubClientProtocol protocol;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public BaseDevice identity;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;

        protected DeviceMethodTestInstance(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, BaseDevice identity, String publicKeyCert, String privateKey, String x509Thumbprint)
        {
            this.deviceTestManager = deviceTestManager;
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.identity = identity;
            this.publicKeyCert = publicKeyCert;
            this.privateKey = privateKey;
            this.x509Thumbprint = x509Thumbprint;
        }
    }

    protected static Collection<BaseDevice> getIdentities(Collection inputs)
    {
        Set<BaseDevice> identities = new HashSet<>();

        Object[] inputArray = inputs.toArray();
        for (int i = 0; i < inputs.size(); i++)
        {
            Object[] inputsInstance = (Object[]) inputArray[i];
            identities.add((BaseDevice) inputsInstance[4]);
        }

        return identities;
    }

    @BeforeClass
    public static void classSetup()
    {
        try
        {
            methodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);
            registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            TestCase.fail("Unexpected exception encountered");
        }
    }

    @Before
    public void cleanToStart()
    {
        actualStatusUpdates = new ArrayList<Pair<IotHubConnectionStatus, Throwable>>();
        setConnectionStatusCallBack(actualStatusUpdates);

        try
        {
            this.testInstance.deviceTestManager.stop();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        this.testInstance.deviceTestManager.clearDevice();

        try
        {
            this.testInstance.deviceTestManager.start(true, false);
            IotHubServicesCommon.confirmOpenStabilized(actualStatusUpdates, 120000, this.testInstance.deviceTestManager.client);
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
            fail(buildExceptionMessage("Unexpected exception occurred during sending reported properties: " + e.getMessage(), this.testInstance.deviceTestManager.client));
        }
        catch (UnsupportedOperationException e)
        {
            //Only thrown when twin was already initialized. Safe to ignore
        }
    }

    @After
    public void delayTests()
    {
        try
        {
            this.testInstance.deviceTestManager.stop();
            Thread.sleep(INTERTEST_GUARDIAN_DELAY_MILLISECONDS);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unexpected exception encountered");
        }
    }

    protected static class RunnableInvoke implements Runnable
    {
        protected String deviceId;
        protected String moduleId;
        protected String testName;
        protected CountDownLatch latch;
        protected MethodResult result = null;
        protected DeviceMethod methodServiceClient;
        protected Exception exception = null;

        public RunnableInvoke(DeviceMethod methodServiceClient, String deviceId, String moduleId, String testName, CountDownLatch latch)
        {
            this.methodServiceClient = methodServiceClient;
            this.deviceId = deviceId;
            this.moduleId = moduleId;
            this.testName = testName;
            this.latch = latch;
        }

        @Override
        public void run()
        {
            // Arrange
            exception = null;

            // Act
            try
            {
                if (moduleId != null)
                {
                    result = methodServiceClient.invoke(deviceId, moduleId, DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, testName);

                }
                else
                {
                    result = methodServiceClient.invoke(deviceId, DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, testName);
                }
            }
            catch (Exception e)
            {
                exception = e;
            }

            latch.countDown();
        }

        public String getExpectedPayload()
        {
            return DeviceEmulator.METHOD_LOOPBACK + ":" + testName;
        }

        public MethodResult getResult()
        {
            return result;
        }

        public Exception getException()
        {
            return exception;
        }
    }

    protected static void tearDown(Collection<BaseDevice> identitiesToDispose, ArrayList<DeviceTestManager> deviceTestManagers)
    {
        try
        {
            if (identitiesToDispose != null && !identitiesToDispose.isEmpty())
            {
                for (DeviceTestManager deviceTestManager : deviceTestManagers)
                {
                    if (deviceTestManager != null)
                    {
                        deviceTestManager.stop();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Failed to stop device test managers");
        }

        if (registryManager != null)
        {
            Tools.removeDevicesAndModules(registryManager, identitiesToDispose);
            registryManager.close();
            registryManager = null;
        }
    }

    protected void setConnectionStatusCallBack(final List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates)
    {

        IotHubConnectionStatusChangeCallback connectionStatusUpdateCallback = new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
            {
                actualStatusUpdates.add(new Pair<>(status, throwable));
            }
        };

        this.testInstance.deviceTestManager.client.registerConnectionStatusChangeCallback(connectionStatusUpdateCallback, null);
    }

    protected void invokeMethodSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.identity instanceof Module)
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module)testInstance.identity).getId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }

        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(DeviceEmulator.METHOD_LOOPBACK + ":" + PAYLOAD_STRING, result.getPayload());
        Assert.assertEquals(0, deviceTestManger.getStatusError());
    }

    protected String getModuleConnectionString(Module module) throws IotHubException, IOException
    {
        return DeviceConnectionString.get(iotHubConnectionString, registryManager.getDevice(module.getDeviceId()), module);
    }
}
