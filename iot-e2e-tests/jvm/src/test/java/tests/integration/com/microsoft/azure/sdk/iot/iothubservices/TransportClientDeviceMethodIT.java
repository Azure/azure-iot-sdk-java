package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS_WS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransportClientDeviceMethodIT
{
    private static final int MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_PER_CALL_MS = 1000;
    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_NOT_DEFINED = 404;
    public static final String METHOD_NAME = "methodName";
    public static final String METHOD_PAYLOAD = "This is a good payload";
    private static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;

    protected static class SampleDeviceMethodCallback implements com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback
    {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            int status;

            if (methodName.equals(METHOD_NAME))
            {
                status = METHOD_SUCCESS;
            }
            else
            {
                status = METHOD_NOT_DEFINED;
            }

            return new DeviceMethodData(status, METHOD_PAYLOAD);
        }
    }

    protected static class DeviceMethodStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("Device Client: IoT Hub responded to device method operation with status " + status.name());
        }
    }

    protected static class RunnableInvoke implements Runnable
    {
        private String deviceId;
        private String methodName;
        private String methodPayload;
        private CountDownLatch latch;
        private MethodResult result = null;
        private DeviceMethod methodServiceClient;

        private Exception exception = null;

        RunnableInvoke(DeviceMethod methodServiceClient, String deviceId, String methodName, String methodPayload, CountDownLatch latch)
        {
            this.methodServiceClient = methodServiceClient;
            this.deviceId = deviceId;
            this.methodName = methodName;
            this.methodPayload = methodPayload;
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
                result = methodServiceClient.invoke(deviceId, methodName, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, methodPayload);
            }
            catch (Exception e)
            {
                exception = e;
            }

            latch.countDown();
        }

        String getExpectedPayload()
        {
            return methodName + ":" + methodPayload;
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

    private static final Integer MAX_DEVICE_MULTIPLEX = 3;

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";

    private static DeviceMethod methodServiceClient;

    private static final Long RESPONSE_TIMEOUT = TimeUnit.SECONDS.toSeconds(200);
    private static final Long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toSeconds(5);

    private static RegistryManager registryManager;

    private static Device[] deviceListAmqps = new Device[MAX_DEVICE_MULTIPLEX];

    private static ArrayList<String> clientConnectionStringArrayList = new ArrayList<>();

    @BeforeClass
    public static void setUp() throws NoSuchAlgorithmException, IotHubException, IOException, URISyntaxException, InterruptedException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            String uuid = UUID.randomUUID().toString();
            String deviceId = "java-device-client-e2e-test-multiplexing-methods-amqps".concat(i + "-" + uuid);

            deviceListAmqps[i] = Device.createFromId(deviceId, null, null);
            registryManager.addDevice(deviceListAmqps[i]);
            clientConnectionStringArrayList.add(registryManager.getDeviceConnectionString(deviceListAmqps[i]));
        }

        methodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        if (registryManager != null)
        {
            for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
            {
                registryManager.removeDevice(deviceListAmqps[i].getDeviceId());
            }
            registryManager.close();
            registryManager = null;
        }
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

    @Test
    public void invokeMethodAMQPSSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        SendMessagesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            clientArrayList.get(i).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);

            CountDownLatch countDownLatch = new CountDownLatch(1);
            TransportClientDeviceMethodIT.RunnableInvoke runnableInvoke = new TransportClientDeviceMethodIT.RunnableInvoke(methodServiceClient, deviceListAmqps[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            countDownLatch.await();

            MethodResult result = runnableInvoke.getResult();
            assertNotNull((runnableInvoke.getException() == null ? "Runnable returns null without exception information" : runnableInvoke.getException().getMessage()), result);
            assertEquals((long)METHOD_SUCCESS,(long)result.getStatus());
            assertEquals(runnableInvoke.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }

    @Test
    public void invokeMethodAMQPSInvokeParallelSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        SendMessagesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            clientArrayList.get(i).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);
        }

        List<TransportClientDeviceMethodIT.RunnableInvoke> runs = new LinkedList<>();
        CountDownLatch countDownLatch = new CountDownLatch(MAX_DEVICE_MULTIPLEX);
        for (int i = 0; i < clientArrayList.size(); i++)
        {
            TransportClientDeviceMethodIT.RunnableInvoke runnableInvoke = new TransportClientDeviceMethodIT.RunnableInvoke(methodServiceClient, deviceListAmqps[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            runs.add(runnableInvoke);
        }
        countDownLatch.await();

        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_PER_CALL_MS * MAX_DEVICE_MULTIPLEX);

        for (TransportClientDeviceMethodIT.RunnableInvoke run:runs)
        {
            MethodResult result = run.getResult();
            assertNotNull((run.getException() == null ? "Runnable returns null without exception information" : run.getException().getMessage()), result);
            assertEquals((long)METHOD_SUCCESS,(long)result.getStatus());
            assertEquals(run.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }

    @Test
    public void invokeMethodAMQPSWSSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        SendMessagesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            clientArrayList.get(i).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);

            CountDownLatch countDownLatch = new CountDownLatch(1);
            TransportClientDeviceMethodIT.RunnableInvoke runnableInvoke = new TransportClientDeviceMethodIT.RunnableInvoke(methodServiceClient, deviceListAmqps[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            countDownLatch.await();

            MethodResult result = runnableInvoke.getResult();
            assertNotNull((runnableInvoke.getException() == null ? "Runnable returns null without exception information" : runnableInvoke.getException().getMessage()), result);
            assertEquals((long)METHOD_SUCCESS,(long)result.getStatus());
            assertEquals(runnableInvoke.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }

    @Test
    public void invokeMethodAMQPSWSInvokeParallelSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        SendMessagesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            clientArrayList.get(i).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);
        }

        List<TransportClientDeviceMethodIT.RunnableInvoke> runs = new LinkedList<>();
        CountDownLatch countDownLatch = new CountDownLatch(MAX_DEVICE_MULTIPLEX);
        for (int i = 0; i < clientArrayList.size(); i++)
        {
            TransportClientDeviceMethodIT.RunnableInvoke runnableInvoke = new TransportClientDeviceMethodIT.RunnableInvoke(methodServiceClient, deviceListAmqps[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            runs.add(runnableInvoke);
        }
        countDownLatch.await();

        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_PER_CALL_MS * MAX_DEVICE_MULTIPLEX);

        for (TransportClientDeviceMethodIT.RunnableInvoke run:runs)
        {
            MethodResult result = run.getResult();
            assertNotNull((run.getException() == null ? "Runnable returns null without exception information" : run.getException().getMessage()), result);
            assertEquals((long)METHOD_SUCCESS,(long)result.getStatus());
            assertEquals(run.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }
}
