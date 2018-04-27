/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.ErrorInjectionHelper;
import com.microsoft.azure.sdk.iot.common.MessageAndResult;
import com.microsoft.azure.sdk.iot.common.iothubservices.IotHubServicesCommon;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
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

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS_WS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration E2E test for Device Method on the service client.
 */
@RunWith(Parameterized.class)
public class DeviceMethodIT
{
    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static String publicKeyCert;
    private static String privateKey;
    private static String x509Thumbprint;

    private static DeviceMethod methodServiceClient;
    private static RegistryManager registryManager;

    //private static final int MAX_DEVICES = 1;

    private static final String DEVICE_ID_NAME = "E2EJavaMethodMqtt";

    private static final Long RESPONSE_TIMEOUT = TimeUnit.SECONDS.toSeconds(200);
    private static final Long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toSeconds(5);
    private static final String PAYLOAD_STRING = "This is a valid payload";

    private static DeviceTestManager deviceTestManagerAmqps;
    private static DeviceTestManager deviceTestManagerMqtt;
    private static DeviceTestManager deviceTestManagerAmqpsWs;
    private static DeviceTestManager deviceTestManagerMqttWs;
    private static DeviceTestManager x509deviceTestManagerMqtt;
    private static DeviceTestManager x509deviceTestManagerAmqps;

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
    @Parameterized.Parameters(name = "{1} with {2} auth")
    public static Collection inputs() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, InterruptedException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        X509Cert cert = new X509Cert(0, false, "TestLeaf", "TestRoot");
        privateKey =  cert.getPrivateKeyLeafPem();
        publicKeyCert = cert.getPublicCertLeafPem();
        x509Thumbprint = cert.getThumbPrintLeaf();

        methodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        String uuid = UUID.randomUUID().toString();
        deviceTestManagerAmqps = new DeviceTestManager(registryManager, "java-device-client-e2e-test-amqps".concat("-" + uuid), IotHubClientProtocol.AMQPS);
        deviceTestManagerAmqpsWs = new DeviceTestManager(registryManager, "java-device-client-e2e-test-amqpsws".concat("-" + uuid), AMQPS_WS);
        deviceTestManagerMqtt = new DeviceTestManager(registryManager, "java-device-client-e2e-test-mqtt".concat("-" + uuid), IotHubClientProtocol.MQTT);
        deviceTestManagerMqttWs = new DeviceTestManager(registryManager,"java-device-client-e2e-test-mqttws".concat("-" + uuid),IotHubClientProtocol.MQTT_WS);
        x509deviceTestManagerAmqps = new DeviceTestManager(registryManager,
                "java-device-client-e2e-test-amqps-x509".concat("-" + uuid),
                IotHubClientProtocol.AMQPS,
                publicKeyCert,
                privateKey,
                x509Thumbprint);
        x509deviceTestManagerMqtt = new DeviceTestManager(registryManager,
                "java-device-client-e2e-test-mqtt-x509".concat("-" + uuid),
                IotHubClientProtocol.MQTT,
                publicKeyCert,
                privateKey,
                x509Thumbprint);

        return Arrays.asList(
            new Object[][]
            {
                {deviceTestManagerAmqps, IotHubClientProtocol.AMQPS, AuthenticationType.SAS},
                {deviceTestManagerAmqpsWs, AMQPS_WS, AuthenticationType.SAS},
                {deviceTestManagerMqtt, IotHubClientProtocol.MQTT, AuthenticationType.SAS},
                {deviceTestManagerMqttWs, IotHubClientProtocol.MQTT_WS, AuthenticationType.SAS},
                {x509deviceTestManagerAmqps, IotHubClientProtocol.AMQPS, AuthenticationType.SELF_SIGNED},
                {x509deviceTestManagerMqtt, IotHubClientProtocol.MQTT, AuthenticationType.SELF_SIGNED},
            }
        );
    }

    public DeviceMethodIT(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType)
    {
        super();
        this.testInstance = new DeviceMethodITRunner(deviceTestManager, protocol, authenticationType);
    }

    private class DeviceMethodITRunner
    {
        private DeviceTestManager deviceTestManager;
        private IotHubClientProtocol protocol;
        private AuthenticationType authenticationType;

        public DeviceMethodITRunner(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType)
        {
            this.deviceTestManager = deviceTestManager;
            this.protocol = protocol;
            this.authenticationType = authenticationType;
        }
    }

    @Before
    public void cleanToStart()
    {
        this.testInstance.deviceTestManager.clearDevice();
    }

    @After
    public void delayTests()
    {
        try
        {
            Thread.sleep(INTERTEST_GUARDIAN_DELAY_MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected static class RunnableInvoke implements Runnable
    {
        private String deviceId;
        private String testName;
        private CountDownLatch latch;
        private MethodResult result = null;
        private DeviceMethod methodServiceClient;

        private Exception exception = null;

        RunnableInvoke(DeviceMethod methodServiceClient, String deviceId, String testName, CountDownLatch latch)
        {
            this.methodServiceClient = methodServiceClient;
            this.deviceId = deviceId;
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
                result = methodServiceClient.invoke(deviceId, DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, testName);
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
        if (deviceTestManagerAmqps != null)
        {
            deviceTestManagerAmqps.stop();
        }

        if (deviceTestManagerAmqpsWs != null)
        {
            deviceTestManagerAmqpsWs.stop();
        }

        if (deviceTestManagerMqtt != null)
        {
            deviceTestManagerMqtt.stop();
        }

        if (deviceTestManagerMqttWs != null)
        {
            deviceTestManagerMqttWs.stop();
        }

        if (x509deviceTestManagerAmqps != null)
        {
            x509deviceTestManagerAmqps.stop();
        }

        if (x509deviceTestManagerMqtt != null)
        {
            x509deviceTestManagerMqtt.stop();
        }

        registryManager.close();
    }

    @Test
    public void invokeMethodSucceed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result = methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
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
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;
        CountDownLatch cdl = new CountDownLatch(NUMBER_INVOKES_PARALLEL);
        List<RunnableInvoke> runs = new LinkedList<>();

        for (int i = 0; i < NUMBER_INVOKES_PARALLEL; i++)
        {
            RunnableInvoke runnableInvoke = new RunnableInvoke(methodServiceClient, deviceTestManger.getDeviceId(), "Thread" + i, cdl);
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
        MethodResult result = methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK, null, null, PAYLOAD_STRING);
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
        MethodResult result = methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
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
        MethodResult result = methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
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
        MethodResult result = methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
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
        MethodResult result = methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_UNKNOWN, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
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
            methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
            assert true;
        }
        catch(IotHubGatewayTimeoutException expected)
        {
            //Don't do anything. Expected throw.
        }

        // Act
        MethodResult result = methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
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
        MethodResult result = methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, null, CONNECTION_TIMEOUT, "100");
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
        MethodResult result = methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, null, "100");
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
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        MethodResult result = methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
    }

    @Test (expected = IotHubNotFoundException.class)
    public void invokeMethodUnknownDeviceFailed() throws Exception
    {
        // Arrange

        // Act
        MethodResult result = methodServiceClient.invoke("UnknownDevice", DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
    }

    @Test
    public void invokeMethodResetDeviceFailed() throws Exception
    {
        // Arrange
        DeviceTestManager deviceTestManger = this.testInstance.deviceTestManager;

        // Act
        try
        {
            MethodResult result = methodServiceClient.invoke(deviceTestManger.getDeviceId(), DeviceEmulator.METHOD_RESET, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
            if (testInstance.authenticationType == AuthenticationType.SELF_SIGNED)
            {
                deviceTestManger.restartDevice(publicKeyCert, privateKey);
            }
            else
            {
                deviceTestManger.restartDevice(null, null);
            }
            throw new Exception("Reset device do not affect the method invoke on the service");
        }
        catch (IotHubNotFoundException expected)
        {
            // Don't do anything, expected throw.
        }

        if (testInstance.authenticationType == AuthenticationType.SELF_SIGNED)
        {
            deviceTestManger.restartDevice(publicKeyCert, privateKey);
        }
        else
        {
            deviceTestManger.restartDevice(null, null);
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

    private void setConnectionStatusCallBack(final List actualStatusUpdates)
    {

        IotHubConnectionStatusChangeCallback connectionStatusUpdateCallback = new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
                actualStatusUpdates.add(status);
            }
        };

        this.testInstance.deviceTestManager.getDeviceClient().registerConnectionStatusChangeCallback(connectionStatusUpdateCallback, null);
    }

    private void errorInjectionTestFlow(Message errorInjectionMessage) throws Exception
    {
        // Arrange
        final List<IotHubConnectionStatus> actualStatusUpdates = new ArrayList<>();
        setConnectionStatusCallBack(actualStatusUpdates);
        invokeMethodSucceed();

        // Act
        errorInjectionMessage.setExpiryTime(100);
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
