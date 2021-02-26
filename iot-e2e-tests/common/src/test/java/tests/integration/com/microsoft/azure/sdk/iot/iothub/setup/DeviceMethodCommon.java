/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;


import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethodClientOptions;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.After;
import org.junit.Assert;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceConnectionString;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceEmulator;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceTestManager;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SSLContextBuilder;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.X509CertificateGenerator;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/**
 * Utility functions, setup and teardown for all device method integration tests. This class should not contain any tests,
 * but any children class should.
 */
public class DeviceMethodCommon extends IntegrationTest
{
    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));
        return inputsCommon();
    }

    protected static String iotHubConnectionString = "";

    protected static final Long RESPONSE_TIMEOUT = TimeUnit.SECONDS.toSeconds(200);
    protected static final Long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toSeconds(5);
    protected static final String PAYLOAD_STRING = "This is a valid payload";

    protected static final int NUMBER_INVOKES_PARALLEL = 10;
    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    protected DeviceMethodTestInstance testInstance;
    protected static final long ERROR_INJECTION_WAIT_TIMEOUT_MILLISECONDS = 60 * 1000; // 1 minute

    protected static Collection inputsCommon() throws IOException
    {
        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        String publicKeyCert = certificateGenerator.getPublicCertificate();
        String privateKey = certificateGenerator.getPrivateKey();
        String x509Thumbprint = certificateGenerator.getX509Thumbprint();

        Collection<Object[]> inputs = new ArrayList<>();

        for (ClientType clientType : ClientType.values())
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

    public static class DeviceMethodTestInstance
    {
        public DeviceTestManager deviceTestManager;
        public IotHubClientProtocol protocol;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public BaseDevice identity;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public DeviceMethod methodServiceClient;
        public RegistryManager registryManager;

        protected DeviceMethodTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws Exception
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.publicKeyCert = publicKeyCert;
            this.privateKey = privateKey;
            this.x509Thumbprint = x509Thumbprint;
            this.methodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString, DeviceMethodClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
            this.registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        }

        public void setup() throws Exception {

            String TEST_UUID = UUID.randomUUID().toString();
            SSLContext sslContext = SSLContextBuilder.buildSSLContext(publicKeyCert, privateKey);
            if (clientType == ClientType.DEVICE_CLIENT)
            {
                if (authenticationType == SAS)
                {
                    //sas device client
                    String deviceId = "java-method-e2e-test-device".concat("-" + TEST_UUID);
                    Device device = Device.createFromId(deviceId, null, null);
                    device = Tools.addDeviceWithRetry(registryManager, device);
                    DeviceClient deviceClient = new DeviceClient(registryManager.getDeviceConnectionString(device), protocol);
                    this.deviceTestManager = new DeviceTestManager(deviceClient);
                    this.identity = device;
                }
                else if (authenticationType == SELF_SIGNED)
                {
                    //x509 device client
                    String deviceX509Id = "java-method-e2e-test-device-x509".concat("-" + TEST_UUID);
                    Device deviceX509 = Device.createDevice(deviceX509Id, AuthenticationType.SELF_SIGNED);
                    deviceX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
                    deviceX509 = Tools.addDeviceWithRetry(registryManager, deviceX509);
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
                    //sas device to house the sas module under test
                    String deviceId = "java-method-e2e-test-device".concat("-" + TEST_UUID);
                    Device device = Device.createFromId(deviceId, null, null);
                    device = Tools.addDeviceWithRetry(registryManager, device);

                    //sas module client under test
                    String moduleId = "java-method-e2e-test-module".concat("-" + TEST_UUID);
                    Module module = Module.createFromId(deviceId, moduleId, null);
                    module = Tools.addModuleWithRetry(registryManager, module);
                    ModuleClient moduleClient = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), protocol);
                    this.deviceTestManager = new DeviceTestManager(moduleClient);
                    this.identity = module;
                }
                else if (authenticationType == SELF_SIGNED)
                {
                    //x509 device to house the x509 module under test
                    String deviceX509Id = "java-method-e2e-test-device-x509".concat("-" + TEST_UUID);
                    Device deviceX509 = Device.createDevice(deviceX509Id, AuthenticationType.SELF_SIGNED);
                    deviceX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
                    deviceX509 = Tools.addDeviceWithRetry(registryManager, deviceX509);

                    //x509 module client under test
                    String moduleX509Id = "java-method-e2e-test-module-x509".concat("-" + TEST_UUID);
                    Module moduleX509 = Module.createModule(deviceX509Id, moduleX509Id, AuthenticationType.SELF_SIGNED);
                    moduleX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
                    moduleX509 = Tools.addModuleWithRetry(registryManager, moduleX509);
                    ModuleClient moduleClientX509 = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), protocol, sslContext);
                    this.deviceTestManager = new DeviceTestManager(moduleClientX509);
                    this.identity = moduleX509;
                }
                else
                {
                    throw new Exception("Test code has not been written for this path yet");
                }
            }

            if ((this.protocol == AMQPS || this.protocol == AMQPS_WS) && this.authenticationType == SAS)
            {
                this.deviceTestManager.client.setOption("SetAmqpOpenAuthenticationSessionTimeout", AMQP_AUTHENTICATION_SESSION_TIMEOUT_SECONDS);
                this.deviceTestManager.client.setOption("SetAmqpOpenDeviceSessionsTimeout", AMQP_DEVICE_SESSION_TIMEOUT_SECONDS);
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

    public void openDeviceClientAndSubscribeToMethods() throws Exception
    {
        testInstance.setup();
        testInstance.deviceTestManager.client.open();

        try
        {
            this.testInstance.deviceTestManager.subscribe(true, false);
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
            fail(buildExceptionMessage("Unexpected exception occurred during subscribe: " + Tools.getStackTraceFromThrowable(e), this.testInstance.deviceTestManager.client));
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

    protected void setConnectionStatusCallBack(final List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates)
    {

        IotHubConnectionStatusChangeCallback connectionStatusUpdateCallback = (status, statusChangeReason, throwable, callbackContext) -> actualStatusUpdates.add(new Pair<>(status, throwable));

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
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((Module)testInstance.identity).getId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        }
        else
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
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
        return DeviceConnectionString.get(iotHubConnectionString, testInstance.registryManager.getDevice(module.getDeviceId()), module);
    }

    protected static DeviceMethod buildDeviceMethodClientWithAzureSasCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential azureSasCredential = new AzureSasCredential(serviceSasToken.toString());
        DeviceMethodClientOptions options = DeviceMethodClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build();
        return new DeviceMethod(iotHubConnectionStringObj.getHostName(), azureSasCredential, options);
    }

    protected static DeviceMethod buildDeviceMethodClientWithTokenCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        TokenCredential tokenCredential = Tools.buildTokenCredentialFromEnvironment();
        DeviceMethodClientOptions options = DeviceMethodClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build();
        return new DeviceMethod(iotHubConnectionStringObj.getHostName(), tokenCredential, options);
    }
}
