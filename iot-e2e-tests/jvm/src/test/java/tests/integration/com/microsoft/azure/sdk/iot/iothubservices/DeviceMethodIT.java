/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.ErrorInjectionHelper;
import com.microsoft.azure.sdk.iot.common.MessageAndResult;
import com.microsoft.azure.sdk.iot.common.iothubservices.IotHubServicesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubGatewayTimeoutException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.MethodNameLoggingIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceEmulator;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceTestManager;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.X509Cert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assert.*;

/**
 * Integration E2E test for Device Method on the service client.
 */
@RunWith(Parameterized.class)
public class DeviceMethodIT extends MethodNameLoggingIntegrationTest
{
    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static String publicKeyCert;
    private static String privateKey;
    private static String x509Thumbprint;

    private static DeviceMethod methodServiceClient;
    private static RegistryManager registryManager;

    private static Device device;
    private static Module module;

    private static Device deviceX509;
    private static Module moduleX509;

    private static final Long RESPONSE_TIMEOUT = TimeUnit.SECONDS.toSeconds(200);
    private static final Long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toSeconds(5);
    private static final String PAYLOAD_STRING = "This is a valid payload";

    private static ArrayList<DeviceTestManager> deviceTestManagers;

    private static final int NUMBER_INVOKES_PARALLEL = 10;
    private static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;
    // How much to wait until a message makes it to the server, in milliseconds
    private static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    //How many milliseconds between retry
    private static final Integer RETRY_MILLISECONDS = 100;

    private DeviceMethodIT.DeviceMethodITRunner testInstance;
    private static final long ERROR_INJECTION_WAIT_TIMEOUT = 1 * 60 * 1000; // 1 minute
    private static final long ERROR_INJECTION_EXECUTION_TIMEOUT = 2* 60 * 1000; // 2 minute

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{1} with {2} auth using {3}")
    public static Collection inputs() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, InterruptedException, ModuleClientException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        X509Cert cert = new X509Cert(0, false, "TestLeaf", "TestRoot");
        privateKey =  cert.getPrivateKeyLeafPem();
        publicKeyCert = cert.getPublicCertLeafPem();
        x509Thumbprint = cert.getThumbPrintLeaf();

        methodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        String uuid = UUID.randomUUID().toString();
        deviceTestManagers = new ArrayList<>();

        String TEST_UUID = UUID.randomUUID().toString();

        /* Create unique device name */
        String deviceId = "java-method-e2e-test-device".concat("-" + TEST_UUID);
        String moduleId = "java-method-e2e-test-module".concat("-" + TEST_UUID);
        String deviceX509Id = "java-method-e2e-test-device-x509".concat("-" + TEST_UUID);
        String moduleX509Id = "java-method-e2e-test-module-x509".concat("-" + TEST_UUID);

        /* Create device on the service */
        device = Device.createFromId(deviceId, null, null);
        module = Module.createFromId(deviceId, moduleId, null);

        deviceX509 = Device.createDevice(deviceX509Id, AuthenticationType.SELF_SIGNED);
        deviceX509.setThumbprint(x509Thumbprint, x509Thumbprint);
        moduleX509 = Module.createModule(deviceX509Id, moduleX509Id, AuthenticationType.SELF_SIGNED);
        moduleX509.setThumbprint(x509Thumbprint, x509Thumbprint);

        Collection<Object[]> inputs = new ArrayList<>();

        /* Add devices to the IoTHub */
        device = registryManager.addDevice(device);
        module = registryManager.addModule(module);
        deviceX509 = registryManager.addDevice(deviceX509);
        moduleX509 = registryManager.addModule(moduleX509);

        for (IotHubClientProtocol protocol : IotHubClientProtocol.values())
        {
            if (protocol != HTTPS)
            {
                //sas device client
                DeviceClient deviceClient = new DeviceClient(registryManager.getDeviceConnectionString(device), protocol);
                DeviceTestManager deviceClientSasTestManager = new DeviceTestManager(deviceClient);
                deviceTestManagers.add(deviceClientSasTestManager);
                inputs.add(makeSubArray(deviceClientSasTestManager, protocol, SAS, "DeviceClient", device, null));

                //sas module client
                ModuleClient moduleClient = new ModuleClient(registryManager.getDeviceConnectionString(device) + ";ModuleId=" + module.getId(), protocol);
                DeviceTestManager moduleClientSasTestManager = new DeviceTestManager(moduleClient);
                deviceTestManagers.add(moduleClientSasTestManager);
                inputs.add(makeSubArray(moduleClientSasTestManager, protocol, SAS, "ModuleClient", device, module));


                if (protocol != MQTT_WS && protocol != AMQPS_WS)
                {
                    //x509 device client
                    DeviceClient deviceClientX509 = new DeviceClient(registryManager.getDeviceConnectionString(deviceX509), protocol, publicKeyCert, false, privateKey, false);
                    DeviceTestManager deviceClientX509TestManager = new DeviceTestManager(deviceClientX509);
                    deviceTestManagers.add(deviceClientX509TestManager);
                    inputs.add(makeSubArray(deviceClientX509TestManager, protocol, SELF_SIGNED, "DeviceClient", deviceX509, null));

                    //x509 module client
                    ModuleClient moduleClientX509 = new ModuleClient(registryManager.getDeviceConnectionString(deviceX509) + ";ModuleId=" + moduleX509.getId(), protocol, publicKeyCert, false, privateKey, false);
                    DeviceTestManager moduleClientX509TestManager = new DeviceTestManager(moduleClientX509);
                    deviceTestManagers.add(moduleClientX509TestManager);
                    inputs.add(makeSubArray(moduleClientX509TestManager, protocol, SELF_SIGNED, "ModuleClient", deviceX509, moduleX509));
                }
            }
        }

        return inputs;
    }

    private static Object[] makeSubArray(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType, String clientType, Device device, Module module)
    {
        Object[] inputSubArray = new Object[6];
        inputSubArray[0] = deviceTestManager;
        inputSubArray[1] = protocol;
        inputSubArray[2] = authenticationType;
        inputSubArray[3] = clientType;
        inputSubArray[4] = device;
        inputSubArray[5] = module;
        return inputSubArray;
    }

    public DeviceMethodIT(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType, String clientType, Device device, Module module)
    {
        super();
        this.testInstance = new DeviceMethodITRunner(deviceTestManager, protocol, authenticationType, clientType, device, module);
    }

    private class DeviceMethodITRunner
    {
        private DeviceTestManager deviceTestManager;
        private IotHubClientProtocol protocol;
        private AuthenticationType authenticationType;
        private String clientType;
        private Device device;
        private Module module;

        public DeviceMethodITRunner(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType, String clientType, Device device, Module module)
        {
            this.deviceTestManager = deviceTestManager;
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.device = device;
            this.module = module;
        }
    }

    @Before
    public void cleanToStart()
    {
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
            this.testInstance.deviceTestManager.start();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
            fail(e.getMessage());
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
            throw new RuntimeException(e);
        }
    }

    protected static class RunnableInvoke implements Runnable
    {
        private String deviceId;
        private String moduleId;
        private String testName;
        private CountDownLatch latch;
        private MethodResult result = null;
        private DeviceMethod methodServiceClient;
        private Exception exception = null;

        RunnableInvoke(DeviceMethod methodServiceClient, String deviceId, String moduleId, String testName, CountDownLatch latch)
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

        String getExpectedPayload()
        {
            return DeviceEmulator.METHOD_LOOPBACK + ":" + testName;
        }

        MethodResult getResult()
        {
            return result;
        }

        Exception getException()
        {
            return exception;
        }
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        for (DeviceTestManager deviceTestManager : deviceTestManagers)
        {
            if (deviceTestManager != null)
            {
                deviceTestManager.stop();
            }
        }

        registryManager.close();
    }

    @Test
    public void invokeMethodSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.module != null)
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }

        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(DeviceEmulator.METHOD_LOOPBACK + ":" + PAYLOAD_STRING, result.getPayload());
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test
    public void invokeMethodInvokeParallelSucceed() throws Exception
    {
        // Arrange
        CountDownLatch cdl = new CountDownLatch(NUMBER_INVOKES_PARALLEL);
        List<RunnableInvoke> runs = new LinkedList<>();

        for (int i = 0; i < NUMBER_INVOKES_PARALLEL; i++)
        {
            RunnableInvoke runnableInvoke;
            if (testInstance.module != null)
            {
                runnableInvoke = new RunnableInvoke(methodServiceClient, testInstance.device.getDeviceId(), testInstance.module.getId(),"Thread" + i, cdl);
            }
            else
            {
                runnableInvoke = new RunnableInvoke(methodServiceClient, testInstance.device.getDeviceId(), null,"Thread" + i, cdl);
            }
            new Thread(runnableInvoke).start();
            runs.add(runnableInvoke);
        }

        cdl.await();

        for (RunnableInvoke run:runs)
        {
            MethodResult result = run.getResult();
            assertNotNull((run.getException() == null ? "Runnable returns null without exception information" : run.getException().getMessage()), result);
            assertEquals((long)DeviceEmulator.METHOD_SUCCESS,(long)result.getStatus());
            assertEquals(run.getExpectedPayload(), result.getPayload().toString());
        }
    }

    @Test
    public void invokeMethodStandardTimeoutSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.module != null)
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_LOOPBACK, null, null, PAYLOAD_STRING);
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK, null, null, PAYLOAD_STRING);
        }

        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(DeviceEmulator.METHOD_LOOPBACK + ":" + PAYLOAD_STRING, result.getPayload());
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test
    public void invokeMethodNullPayloadSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.module != null)
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(DeviceEmulator.METHOD_LOOPBACK + ":null", result.getPayload());
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test
    public void invokeMethodNumberSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.module != null)
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test
    public void invokeMethodThrowsNumberFormatExceptionFailed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.module != null)
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)DeviceEmulator.METHOD_THROWS, (long)result.getStatus());
        assertEquals("java.lang.NumberFormatException: For input string: \"" + PAYLOAD_STRING + "\"", result.getPayload());
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test
    public void invokeMethodUnknownFailed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.module != null)
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_UNKNOWN, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_UNKNOWN, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)DeviceEmulator.METHOD_NOT_DEFINED, (long)result.getStatus());
        assertEquals("unknown:" + DeviceEmulator.METHOD_UNKNOWN, result.getPayload());
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test
    public void invokeMethodRecoverFromTimeoutSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        try
        {
            if (testInstance.module != null)
            {
                methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
            }
            else
            {
                methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
            }
            assert true;
        }
        catch(IotHubGatewayTimeoutException expected)
        {
            //Don't do anything. Expected throw.
        }

        // Act
        MethodResult result;
        if (testInstance.module != null)
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test
    public void invokeMethodDefaultResponseTimeoutSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.module != null)
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, null, CONNECTION_TIMEOUT, "100");
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, null, CONNECTION_TIMEOUT, "100");
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test
    public void invokeMethodDefaultConnectionTimeoutSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result;
        if (testInstance.module != null)
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, null, "100");
        }
        else
        {
            result = methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, null, "100");
        }
        deviceTestManger.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)DeviceEmulator.METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test (expected = IotHubGatewayTimeoutException.class)
    public void invokeMethodResponseTimeoutFailed() throws Exception
    {
        // Arrange
        // Act
        if (testInstance.module != null)
        {
            methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
        }
        else
        {
            methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
        }
    }

    @Test (expected = IotHubNotFoundException.class)
    public void invokeMethodUnknownDeviceFailed() throws Exception
    {
        if (testInstance.module != null)
        {
            methodServiceClient.invoke(testInstance.device.getDeviceId(), "someModuleThatDoesNotExistOnADeviceThatDoesExist", DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        else
        {
            methodServiceClient.invoke("someDeviceThatDoesNotExist", DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
    }

    @Test
    public void invokeMethodResetDeviceFailed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        try
        {
            MethodResult result;
            if (testInstance.module != null)
            {
                methodServiceClient.invoke(testInstance.device.getDeviceId(), testInstance.module.getId(), DeviceEmulator.METHOD_RESET, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
                deviceTestManger.restartDevice(registryManager.getDeviceConnectionString(testInstance.device) + ";ModuleId=" + testInstance.module.getId(), testInstance.protocol, publicKeyCert, privateKey);
            }
            else
            {
                methodServiceClient.invoke(testInstance.device.getDeviceId(), DeviceEmulator.METHOD_RESET, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
                deviceTestManger.restartDevice(registryManager.getDeviceConnectionString(testInstance.device), testInstance.protocol, publicKeyCert, privateKey);
            }

            throw new Exception("Reset device do not affect the method invoke on the service");
        }
        catch (IotHubNotFoundException expected)
        {
            // Don't do anything, expected throw.
        }

        if (testInstance.module != null)
        {
            deviceTestManger.restartDevice(registryManager.getDeviceConnectionString(testInstance.device) + ";ModuleId=" + testInstance.module.getId(), testInstance.protocol, publicKeyCert, privateKey);
        }
        else
        {
            deviceTestManger.restartDevice(registryManager.getDeviceConnectionString(testInstance.device), testInstance.protocol, publicKeyCert, privateKey);
        }
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromTcpConnectionDrop() throws Exception
    {
        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromAmqpsConnectionDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromAmqpsSessionDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromAmqpsCBSReqLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSReqLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromAmqpsCBSRespLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.authenticationType != SAS)
        {
            //CBS links are only established when using sas authentication
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsCBSRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromAmqpsD2CLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsD2CTelemetryLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromAmqpsC2DLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. C2D link is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsC2DLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromAmqpsMethodReqLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Method Req is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromAmqpsMethodRespLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Method Resp is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsMethodRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromAmqpsTwinReqLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Twin Req is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsTwinReqLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromAmqpsTwinRespLinkDrop() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        if (testInstance.protocol == AMQPS && testInstance.authenticationType == SELF_SIGNED)
        {
            //TODO error injection seems to fail under these circumstances. Twin Resp is never dropped even if waiting a long time
            // Need to talk to service folks about this strange behavior
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsTwinRespLinkDropErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromGracefulShutdownAmqp() throws Exception
    {
        if (!(testInstance.protocol == AMQPS || testInstance.protocol == AMQPS_WS))
        {
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.amqpsGracefulShutdownErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    @Test(timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void invokeMethodRecoveredFromGracefulShutdownMqtt() throws Exception
    {
        if (!(testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS))
        {
            return;
        }

        this.errorInjectionTestFlow(ErrorInjectionHelper.mqttGracefulShutdownErrorInjectionMessage(
                ErrorInjectionHelper.DefaultDelayInSec,
                ErrorInjectionHelper.DefaultDurationInSec));
    }

    private void setConnectionStatusCallBack(final List actualStatusUpdates)
    {

        IotHubConnectionStatusChangeCallback connectionStatusUpdateCallback = new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
                actualStatusUpdates.add(status);
            }
        };

        this.testInstance.deviceTestManager.client.registerConnectionStatusChangeCallback(connectionStatusUpdateCallback, null);
    }

    private void errorInjectionTestFlow(Message errorInjectionMessage) throws Exception
    {
        // Arrange
        final List<IotHubConnectionStatus> actualStatusUpdates = new ArrayList<>();
        setConnectionStatusCallBack(actualStatusUpdates);
        invokeMethodSucceed();

        // Act
        errorInjectionMessage.setExpiryTime(200);
        MessageAndResult errorInjectionMsgAndRet = new MessageAndResult(errorInjectionMessage, null);
        this.testInstance.deviceTestManager.sendMessageAndWaitForResponse(
                errorInjectionMsgAndRet,
                RETRY_MILLISECONDS,
                SEND_TIMEOUT_MILLISECONDS,
                this.testInstance.protocol);

        // Assert
        IotHubServicesCommon.waitForStabilizedConnection(actualStatusUpdates, ERROR_INJECTION_WAIT_TIMEOUT);
        invokeMethodSucceed();
    }
}
