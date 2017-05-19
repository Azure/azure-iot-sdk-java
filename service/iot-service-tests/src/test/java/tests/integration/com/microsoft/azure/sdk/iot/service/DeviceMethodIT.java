/*
* Copyright (c) Microsoft. All rights reserved.
* Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubGatewayTimeoutException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration E2E test for Device Method on the service client.
 */
public class DeviceMethodIT
{
    private static String iotHubConnectionStringEnvVarName = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static RegistryManager registryManager;
    private static DeviceMethod methodServiceClient;

    private static final int MAX_DEVICES = 1;

    private static String DEVICE_ID_NAME = "E2EJavaMQTT";
    private static final String METHOD_RESET = "reset";
    private static final String METHOD_LOOPBACK = "loopback";
    private static final String METHOD_DELAY_IN_MILLISECONDS = "delayInMilliseconds";
    private static final String METHOD_UNKNOWN = "unknown";

    private static final Long RESPONSE_TIMEOUT = TimeUnit.SECONDS.toSeconds(200);
    private static final Long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toSeconds(5);
    private static final String PAYLOAD_STRING = "This is a valid payload";

    private static List<TestDevice> devices = new LinkedList<>();

    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_THROWS = 403;
    private static final int METHOD_NOT_DEFINED = 404;

    private static final int NUMBER_INVOKES_PARALLEL = 10;

    private static final String uuid = UUID.randomUUID().toString();

    private static class TestDevice
    {
        private static final int OPEN_CONNECTION_TIMEOUT_IN_SECONDS = 10;
        private static final int STOP_DEVICE_TIMEOUT_IN_MILLISECONDS = 10000;
        private static final int SECOND_IN_MILLISECONDS = 1000;

        /* deviceOnServiceClient is the device definition on the service `End`. */
        private Device deviceOnServiceClient;

        /* deviceEmulator is the device definition on the device `End`. */
        private DeviceEmulator deviceEmulator;
        private Thread deviceThread;

        /* Device connection string. */
        private String connectionString;

        /* Device protocol */
        private IotHubClientProtocol protocol;

        protected TestDevice(String iotHubConnectionString, String deviceName, IotHubClientProtocol protocol)
                throws NoSuchAlgorithmException, IotHubException, IOException,URISyntaxException, InterruptedException
        {
            /* Create unique device name */
            String deviceId = deviceName.concat("-" + uuid);

            /* Create device on the service */
            deviceOnServiceClient = Device.createFromId(deviceId, null, null);

            /* Add device to the IoTHub */
            deviceOnServiceClient = registryManager.addDevice(deviceOnServiceClient);

            /* Create a emulator for the device client, and connect it to the IoTHub */
            this.connectionString = registryManager.getDeviceConnectionString(deviceOnServiceClient);
            this.protocol = protocol;
            deviceEmulator = new DeviceEmulator(this.connectionString, this.protocol);

            /* Enable DeviceMethod on the device client using the callbacks from the DeviceEmulator */
            deviceEmulator.enableDeviceMethod();

            deviceThread = new Thread(deviceEmulator);
            deviceThread.start();

            /* Wait until the device complete the connection with the IoTHub. */
            waitIotHub(1, OPEN_CONNECTION_TIMEOUT_IN_SECONDS);
        }

        protected void waitIotHub(int numberOfEvents, long timeoutInSeconds) throws InterruptedException, IOException
        {
            long countRetry = 0;
            while(getStatusOk() + getStatusError() < numberOfEvents)
            {
                if((countRetry++) >= timeoutInSeconds)
                {
                    throw new IOException("Connection timeout");
                }
                Thread.sleep(SECOND_IN_MILLISECONDS);
            }
        }

        protected void clearDevice()
        {
            this.deviceEmulator.clearStatus();
        }

        protected String getDeviceId()
        {
            return this.deviceOnServiceClient.getDeviceId();
        }

        protected void stop() throws IOException, IotHubException, InterruptedException
        {
            deviceEmulator.stop();
            deviceThread.join(STOP_DEVICE_TIMEOUT_IN_MILLISECONDS);
            registryManager.removeDevice(deviceOnServiceClient.getDeviceId());
        }

        protected void restartDevice() throws InterruptedException, IOException, URISyntaxException
        {
            if(deviceThread.getState() == Thread.State.RUNNABLE)
            {
                deviceEmulator.stop();
            }
            deviceThread.join(STOP_DEVICE_TIMEOUT_IN_MILLISECONDS);

            /* Create a emulator for the device client, and connect it to the IoTHub */
            deviceEmulator = new DeviceEmulator(connectionString, protocol);

            /* Enable DeviceMethod on the device client using the callbacks from the DeviceEmulator */
            deviceEmulator.enableDeviceMethod();

            deviceThread = new Thread(deviceEmulator);
            deviceThread.start();

            /* Wait until the device complete the connection with the IoTHub. */
            waitIotHub(1, OPEN_CONNECTION_TIMEOUT_IN_SECONDS);

        }

        protected int getStatusOk()
        {
            return deviceEmulator.getStatusOk();
        }

        protected int getStatusError()
        {
            return deviceEmulator.getStatusError();
        }
    }

    @BeforeClass
    public static void setUp() throws URISyntaxException, IOException, Exception
    {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet())
        {
            if (envName.equals(iotHubConnectionStringEnvVarName.toString()))
            {
                iotHubConnectionString = env.get(envName);
                break;
            }
        }

        if ((iotHubConnectionString == null) || iotHubConnectionString.isEmpty())
        {
            throw new IllegalArgumentException("Environment variable is not set: " + iotHubConnectionStringEnvVarName);
        }

        methodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        devices = addDevices(MAX_DEVICES);
    }

    @Before
    public void cleanToStart()
    {
        for (TestDevice device:devices)
        {
            device.clearDevice();
        }
    }

    private static List<TestDevice> addDevices(int numOfDevices) throws Exception
    {
        List<TestDevice> newDevices = new LinkedList<>();

        for (int i = 0; i < numOfDevices; i++)
        {
            TestDevice testDevice = new TestDevice(iotHubConnectionString, DEVICE_ID_NAME.concat("-" + i), IotHubClientProtocol.MQTT);

            newDevices.add(testDevice);
        }

        return newDevices;
    }

    private static void removeDevice()
    {
        for (TestDevice device:devices)
        {
            try
            {
                device.stop();
            }
            catch (Exception e)
            {
                System.out.println("Failed removing device " + device.getDeviceId() + " Exception:" + e.toString());
            }
        }
    }

    protected static class RunnableInvoke implements Runnable
    {
        private String deviceId;
        private String testName;
        private CountDownLatch latch;
        private MethodResult result = null;

        protected RunnableInvoke(String deviceId, String testName, CountDownLatch latch)
        {
            this.deviceId = deviceId;
            this.testName = testName;
            this.latch = latch;
        }

        @Override
        public void run()
        {
            // Act
            try
            {
                result = methodServiceClient.invoke(deviceId, METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, testName);
            }
            catch (Exception e)
            {
            }

            latch.countDown();
        }

        protected String getExpectedPayload()
        {
            return METHOD_LOOPBACK + ":" + testName;
        }

        protected MethodResult getResult()
        {
            return result;
        }
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        removeDevice();
    }

    @Test
    public void invokeMethod_succeed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        // Act
        MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        testDevice.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(METHOD_LOOPBACK + ":" + PAYLOAD_STRING, result.getPayload());
        assertEquals(0, testDevice.getStatusError());
    }

    @Test
    public void invokeMethod_invokeParallel_succeed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);
        CountDownLatch cdl = new CountDownLatch(NUMBER_INVOKES_PARALLEL);
        List<RunnableInvoke> runs = new LinkedList<>();

        for (int i = 0; i < NUMBER_INVOKES_PARALLEL; i++)
        {
            RunnableInvoke runnableInvoke = new RunnableInvoke(testDevice.getDeviceId(), "Thread" + i, cdl);
            new Thread(runnableInvoke).start();
            runs.add(runnableInvoke);
        }

        cdl.await();

        for (RunnableInvoke run:runs)
        {
            MethodResult result = run.getResult();
            assertNotNull(result);
            assertEquals((long)METHOD_SUCCESS,(long)result.getStatus());
            assertEquals(run.getExpectedPayload(), result.getPayload().toString());
        }
    }

    @Test
    public void invokeMethod_standardTimeout_succeed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        // Act
        MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_LOOPBACK, null, null, PAYLOAD_STRING);
        testDevice.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(METHOD_LOOPBACK + ":" + PAYLOAD_STRING, result.getPayload());
        assertEquals(0, testDevice.getStatusError());
    }

    @Test
    public void invokeMethod_nullPayload_succeed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        // Act
        MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
        testDevice.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(METHOD_LOOPBACK + ":null", result.getPayload());
        assertEquals(0, testDevice.getStatusError());
    }

    @Test
    public void invokeMethod_number_succeed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        // Act
        MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
        testDevice.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        assertEquals(0, testDevice.getStatusError());
    }

    @Test
    public void invokeMethod_throws_NumberFormatException_failed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        // Act
        MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        testDevice.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)METHOD_THROWS, (long)result.getStatus());
        assertEquals("java.lang.NumberFormatException: For input string: \"" + PAYLOAD_STRING + "\"", result.getPayload());
        assertEquals(0, testDevice.getStatusError());
    }

    @Test
    public void invokeMethod_unknown_failed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        // Act
        MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_UNKNOWN, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
        testDevice.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)METHOD_NOT_DEFINED, (long)result.getStatus());
        assertEquals("unknown:" + METHOD_UNKNOWN, result.getPayload());
        assertEquals(0, testDevice.getStatusError());
    }

    @Test
    public void invokeMethod_recoverFromTimeout_succeed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        try
        {
            methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
            assert true;
        }
        catch(IotHubGatewayTimeoutException expected)
        {
            //Don't do anything. Expected throw.
        }

        // Act
        MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, "100");
        testDevice.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        assertEquals(0, testDevice.getStatusError());
    }

    @Test
    public void invokeMethod_defaultResponseTimeout_succeed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        // Act
        MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_DELAY_IN_MILLISECONDS, null, CONNECTION_TIMEOUT, "100");
        testDevice.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        assertEquals(0, testDevice.getStatusError());
    }

    @Test
    public void invokeMethod_defaultConnectionTimeout_succeed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        // Act
        MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_DELAY_IN_MILLISECONDS, RESPONSE_TIMEOUT, null, "100");
        testDevice.waitIotHub(1, 10);

        // Assert
        assertNotNull(result);
        assertEquals((long)METHOD_SUCCESS, (long)result.getStatus());
        assertEquals(METHOD_DELAY_IN_MILLISECONDS + ":succeed", result.getPayload());
        assertEquals(0, testDevice.getStatusError());
    }

    @Test (expected = IotHubGatewayTimeoutException.class)
    public void invokeMethod_responseTimeout_failed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        // Act
        MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_DELAY_IN_MILLISECONDS, (long)5, CONNECTION_TIMEOUT, "7000");
    }

    @Test (expected = SocketTimeoutException.class)
    public void invokeMethod_connectionTimeout_failed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        // Act
        MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_DELAY_IN_MILLISECONDS, null, CONNECTION_TIMEOUT, "25000");
    }

    @Test (expected = IotHubNotFoundException.class)
    public void invokeMethod_unknownDevice_failed() throws Exception
    {
        // Arrange

        // Act
        MethodResult result = methodServiceClient.invoke("UnknownDevice", METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING);
    }

    @Test
    public void invokeMethod_resetDevice_failed() throws Exception
    {
        // Arrange
        TestDevice testDevice = devices.get(0);

        // Act
        try
        {
            MethodResult result = methodServiceClient.invoke(testDevice.getDeviceId(), METHOD_RESET, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, null);
            testDevice.restartDevice();
            throw new Exception("Reset device do not affect the method invoke on the service");
        }
        catch (IotHubNotFoundException expected)
        {
            // Don't do anything, expected throw.
        }
        testDevice.restartDevice();
    }
}
