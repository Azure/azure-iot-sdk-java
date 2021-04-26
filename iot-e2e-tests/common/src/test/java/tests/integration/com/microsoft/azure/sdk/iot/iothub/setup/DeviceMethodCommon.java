/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethodClientOptions;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
@Slf4j
public class DeviceMethodCommon extends IntegrationTest
{
    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs()
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

    protected static Collection inputsCommon()
    {
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
                            inputs.add(makeSubArray(protocol, authenticationType, clientType));
                        }
                        else if (authenticationType == SELF_SIGNED)
                        {
                            if (protocol != AMQPS_WS && protocol != MQTT_WS)
                            {
                                inputs.add(makeSubArray(protocol, authenticationType, clientType));
                            }
                        }
                    }
                }
            }
        }

        return inputs;
    }

    private static Object[] makeSubArray(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType)
    {
        Object[] inputSubArray = new Object[3];
        inputSubArray[0] = protocol;
        inputSubArray[1] = authenticationType;
        inputSubArray[2] = clientType;
        return inputSubArray;
    }

    protected DeviceMethodCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
    {
        this.testInstance = new DeviceMethodTestInstance(protocol, authenticationType, clientType);
    }

    public static class DeviceMethodTestInstance
    {
        public DeviceTestManager deviceTestManager;
        public IotHubClientProtocol protocol;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public TestIdentity identity;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public DeviceMethod methodServiceClient;
        public RegistryManager registryManager;

        protected DeviceMethodTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws Exception
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.publicKeyCert = x509CertificateGenerator.getPublicCertificate();
            this.privateKey = x509CertificateGenerator.getPrivateKey();
            this.x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();
            this.methodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString, DeviceMethodClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
            this.registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        }

        public void setup() throws Exception {

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                this.identity = Tools.getTestDevice(iotHubConnectionString, this.protocol, this.authenticationType, false);
                this.deviceTestManager = new DeviceTestManager(((TestDeviceIdentity) this.identity).getDeviceClient());
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                this.identity = Tools.getTestModule(iotHubConnectionString, this.protocol, this.authenticationType, false);
                this.deviceTestManager = new DeviceTestManager(((TestModuleIdentity) this.identity).getModuleClient());
            }

            if ((this.protocol == AMQPS || this.protocol == AMQPS_WS) && this.authenticationType == SAS)
            {
                this.deviceTestManager.client.setOption("SetAmqpOpenAuthenticationSessionTimeout", AMQP_AUTHENTICATION_SESSION_TIMEOUT_SECONDS);
                this.deviceTestManager.client.setOption("SetAmqpOpenDeviceSessionsTimeout", AMQP_DEVICE_SESSION_TIMEOUT_SECONDS);
            }
        }

        public void dispose()
        {
            try
            {
                if (this.deviceTestManager != null)
                {
                    this.deviceTestManager.tearDown();
                }
            }
            catch (IOException e)
            {
                log.error("Failed to close clients during cleanup", e);
            }

            Tools.disposeTestIdentity(this.identity, iotHubConnectionString);
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
        if (testInstance.identity instanceof TestModuleIdentity)
        {
            result = testInstance.methodServiceClient.invoke(testInstance.identity.getDeviceId(), ((TestModuleIdentity)testInstance.identity).getModule().getId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
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
}
