/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.common.setup.iothub;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
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
public class DeviceMethodCommon extends IotHubIntegrationTest
{
    protected static String iotHubConnectionString = "";

    protected static DeviceMethod methodServiceClient;
    protected static RegistryManager registryManager;

    protected static final Long RESPONSE_TIMEOUT = TimeUnit.SECONDS.toSeconds(200);
    protected static final Long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toSeconds(5);
    protected static final String PAYLOAD_STRING = "This is a valid payload";

    protected static final int NUMBER_INVOKES_PARALLEL = 10;
    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    private List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates;

    protected DeviceMethodTestInstance testInstance;
    protected static final long ERROR_INJECTION_WAIT_TIMEOUT = 1 * 60 * 1000; // 1 minute

    protected static Collection inputsCommon() throws IOException
    {
        return inputsCommon(ClientType.DEVICE_CLIENT, ClientType.MODULE_CLIENT);
    }

    protected static Collection inputsCommon(ClientType... clientTypes) throws IOException
    {
        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        return inputsCommon(certificateGenerator.getPublicCertificate(), certificateGenerator.getPrivateKey(), certificateGenerator.getX509Thumbprint(), clientTypes);
    }

    protected static Collection inputsCommon(String publicKeyCert, String privateKey, String x509Thumbprint, ClientType... clientTypes) throws IOException
    {
        methodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        Collection<Object[]> inputs = new ArrayList<>();

        for (ClientType clientType : clientTypes)
        {
            for (IotHubClientProtocol protocol : IotHubClientProtocol.values())
            {
                if (protocol != HTTPS)
                {
                    for (AuthenticationType authenticationType : AuthenticationType.values())
                    {
                        if (authenticationType == SAS)
                        {
                            inputs.add(makeSubArray(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint));
                        }
                        else if (authenticationType == SELF_SIGNED)
                        {
                            if (protocol != AMQPS_WS && protocol != MQTT_WS)
                            {
                                inputs.add(makeSubArray(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint));
                            }
                        }
                    }
                }
            }
        }

        return inputs;
    }

    private static Object[] makeSubArray(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        Object[] inputSubArray = new Object[6];
        inputSubArray[0] = protocol;
        inputSubArray[1] = authenticationType;
        inputSubArray[2] = clientType;
        inputSubArray[3] = publicKeyCert;
        inputSubArray[4] = privateKey;
        inputSubArray[5] = x509Thumbprint;
        return inputSubArray;
    }

    protected DeviceMethodCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws Exception
    {
        this.testInstance = new DeviceMethodTestInstance(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
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

        protected DeviceMethodTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws Exception
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.publicKeyCert = publicKeyCert;
            this.privateKey = privateKey;
            this.x509Thumbprint = x509Thumbprint;
        }

        public void setup() throws Exception {

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

            device = Tools.addDeviceWithRetry(registryManager, device);
            deviceX509 = Tools.addDeviceWithRetry(registryManager, deviceX509);

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                if (authenticationType == SAS)
                {
                    //sas device client
                    DeviceClient deviceClient = new DeviceClient(registryManager.getDeviceConnectionString(device), protocol);
                    this.deviceTestManager = new DeviceTestManager(deviceClient);
                    this.identity = device;
                }
                else if (authenticationType == SELF_SIGNED)
                {
                    //x509 device client
                    SSLContext sslContext = SSLContextBuilder.buildSSLContext(publicKeyCert, privateKey);
                    DeviceClient deviceClientX509 = new DeviceClient(registryManager.getDeviceConnectionString(deviceX509), protocol, sslContext);
                    this.deviceTestManager = new DeviceTestManager(deviceClientX509);
                    this.identity = deviceX509;
                }
                else
                {
                    throw new Exception("Test code has not been written for this path yet");
                }
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                if (authenticationType == SAS)
                {
                    //sas module client
                    module = Tools.addModuleWithRetry(registryManager, module);
                    ModuleClient moduleClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), protocol);
                    this.deviceTestManager = new DeviceTestManager(moduleClient);
                    this.identity = module;
                }
                else if (authenticationType == SELF_SIGNED)
                {
                    //x509 module client
                    moduleX509 = Tools.addModuleWithRetry(registryManager, moduleX509);
                    ModuleClient moduleClientX509 = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), protocol, publicKeyCert, false, privateKey, false);
                    this.deviceTestManager = new DeviceTestManager(moduleClientX509);
                    this.identity = moduleX509;
                }
                else
                {
                    throw new Exception("Test code has not been written for this path yet");
                }
            }

            Thread.sleep(2000);
        }

        public void dispose()
        {
            try
            {
                this.deviceTestManager.tearDown();
                registryManager.removeDevice(this.identity.getDeviceId()); //removes all modules associated with this device, too
            }
            catch (Exception e)
            {
                //not a big deal if dispose fails. This test suite is not testing the functions in this cleanup.
                // If identities are left registered, they will be deleted my nightly cleanup job anyways
            }
        }
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

    public void cleanToStart() throws Exception
    {
        testInstance.setup();
        actualStatusUpdates = new ArrayList<Pair<IotHubConnectionStatus, Throwable>>();
        setConnectionStatusCallBack(actualStatusUpdates);

        try
        {
            this.testInstance.deviceTestManager.tearDown();
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
            this.testInstance.deviceTestManager.setup(true, false);
            IotHubServicesCommon.confirmOpenStabilized(actualStatusUpdates, 120000, this.testInstance.deviceTestManager.client);
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
            fail(buildExceptionMessage("Unexpected exception occurred during sending reported properties: " + Tools.getStackTraceFromThrowable(e), this.testInstance.deviceTestManager.client));
        }
        catch (UnsupportedOperationException e)
        {
            //Only thrown when twin was already initialized. Safe to ignore
        }
    }

    @After
    public void afterTest()
    {
        this.testInstance.dispose();
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

    @AfterClass
    public static void tearDownClass()
    {
        registryManager.close();
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
