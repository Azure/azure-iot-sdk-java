/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.EventCallback;
import com.microsoft.azure.sdk.iot.common.Success;
import com.microsoft.azure.sdk.iot.common.iothubservices.IotHubServicesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.*;
import com.microsoft.azure.sdk.iot.common.iothubservices.MethodNameLoggingIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.azure.sdk.iot.common.iothubservices.IotHubServicesCommon.openTransportClientWithRetry;
import static com.microsoft.azure.sdk.iot.common.iothubservices.IotHubServicesCommon.sendMessagesMultiplex;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS_WS;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;
import static tests.integration.com.microsoft.azure.sdk.iot.iothubservices.TransportClientIT.STATUS.FAILURE;
import static tests.integration.com.microsoft.azure.sdk.iot.iothubservices.TransportClientIT.STATUS.SUCCESS;

//Ignoring these tests as they frequently fail. Needs investigations
public class TransportClientIT extends MethodNameLoggingIntegrationTest
{
    //how many devices to test multiplexing with
    private static final int MAX_DEVICE_MULTIPLEX = 3;

    private static final int NUM_KEYS_PER_MESSAGE = 10;
    private static final int NUM_MESSAGES_PER_CONNECTION = 3;

    private static final long RETRY_MILLISECONDS = 100; //.1 seconds
    private static final long SEND_TIMEOUT_MILLISECONDS = 5 * 60 * 1000; // 5 minutes
    private static final long INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2 * 1000; // 2 seconds
    private static final long RECEIVE_MESSAGE_TIMEOUT = 5 * 60 * 1000; // 5 minutes
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 10 * 1000; // 10 seconds
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION = 500; // .5 seconds
    private static final long RESPONSE_TIMEOUT_SECONDS = 200; // 200 seconds
    private static final long CONNECTION_TIMEOUT_SECONDS = 5; //5 seconds
    private static final long MAX_MILLISECS_TIMEOUT_KILL_TEST = 2 * 60 * 1000; // 2 minutes
    private static final long MAX_MILLISECS_TIMEOUT_FLUSH_NOTIFICATION = 10 * 1000; // 10 secs
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_PER_CALL_MS = 1000; // 1 second
    private static final long MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION * 10; // 2 sec

    private static Device[] deviceListAmqps = new Device[MAX_DEVICE_MULTIPLEX];
    private static final AtomicBoolean succeed = new AtomicBoolean();

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";

    private static Map<String, String> messageProperties = new HashMap<>(3);

    private static ServiceClient serviceClient;
    private static FileUploadState[] fileUploadState;
    private static MessageState[] messageStates;
    private static FileUploadNotificationReceiver fileUploadNotificationReceiver;
    private static RegistryManager registryManager;

    private static String expectedCorrelationId = "1234";
    private static String expectedMessageId = "5678";

    private static final String REMOTE_FILE_NAME = "File";
    private static final String REMOTE_FILE_NAME_EXT = ".txt";

    private static final Integer MAX_FILES_TO_UPLOAD = 1;

    private static ArrayList<String> clientConnectionStringArrayList = new ArrayList<>();

    private static DeviceMethod methodServiceClient;

    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_NOT_DEFINED = 404;
    private static final String METHOD_NAME = "methodName";
    private static final String METHOD_PAYLOAD = "This is a good payload";

    private static ArrayList<DeviceState> devicesUnderTest = new ArrayList<>();
    private static DeviceTwin sCDeviceTwin;

    private static final String PROPERTY_KEY = "Key";
    private static final String PROPERTY_VALUE = "Value";
    private static final String PROPERTY_VALUE_UPDATE = "Update";

    private static final Integer MAX_PROPERTIES_TO_TEST = 3;
    private static final Integer MAX_DEVICES = 3;

    @BeforeClass
    public static void setUp() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            String uuid = UUID.randomUUID().toString();
            String deviceId = "java-device-client-e2e-test-multiplexing".concat(i + "-" + uuid);

            deviceListAmqps[i] = Device.createFromId(deviceId, null, null);
            registryManager.addDevice(deviceListAmqps[i]);
            clientConnectionStringArrayList.add(registryManager.getDeviceConnectionString(deviceListAmqps[i]));
        }

        messageProperties = new HashMap<>(3);
        messageProperties.put("name1", "value1");
        messageProperties.put("name2", "value2");
        messageProperties.put("name3", "value3");

        serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        serviceClient.open();

        fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();
        assertNotNull(fileUploadNotificationReceiver);

        // flush pending notifications before every test to prevent random test failures
        // because of notifications received from other failed test
        fileUploadNotificationReceiver.open();
        fileUploadNotificationReceiver.receive(MAX_MILLISECS_TIMEOUT_FLUSH_NOTIFICATION);
        fileUploadNotificationReceiver.close();

        methodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);
    }

    @AfterClass
    public static void tearDown() throws IOException, IotHubException, InterruptedException
    {
        // flush all the notifications caused by this test suite to avoid failures running on different test suite attempt
        assertNotNull(fileUploadNotificationReceiver);
        fileUploadNotificationReceiver.open();
        fileUploadNotificationReceiver.receive(MAX_MILLISECS_TIMEOUT_FLUSH_NOTIFICATION);
        fileUploadNotificationReceiver.close();

        if (registryManager != null)
        {
            for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
            {
                registryManager.removeDevice(deviceListAmqps[i].getDeviceId());
            }
            registryManager.close();
            registryManager = null;
        }

        if (serviceClient != null)
        {
            serviceClient.close();
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

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void sendMessagesOverAmqps() throws URISyntaxException, IOException, InterruptedException
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            sendMessagesMultiplex(clientArrayList.get(i), IotHubClientProtocol.AMQPS, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
        }

        transportClient.closeNow();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void sendMessagesOverAmqpsWs() throws URISyntaxException, IOException, InterruptedException
    {
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            sendMessagesMultiplex(clientArrayList.get(i), IotHubClientProtocol.AMQPS_WS, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
        }

        transportClient.closeNow();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void receiveMessagesOverAmqpsIncludingProperties() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        final ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            Success messageReceived = new Success();
            com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();
            clientArrayList.get(i).setMessageCallback(callback, messageReceived);

            sendMessageToDevice(deviceListAmqps[i].getDeviceId(), "AMQPS");
            waitForMessageToBeReceived(messageReceived, "AMQPS");

            Thread.sleep(200);
        }

        transportClient.closeNow();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void receiveMessagesOverAmqpWSIncludingProperties() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        final ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            Success messageReceived = new Success();
            com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();
            clientArrayList.get(i).setMessageCallback(callback, messageReceived);

            sendMessageToDevice(deviceListAmqps[i].getDeviceId(), "AMQPS_WS");
            waitForMessageToBeReceived(messageReceived, "AMQPS_WS");

            Thread.sleep(200);
        }

        transportClient.closeNow();
    }

    @Test
    public void sendMessagesOverAmqpsMultithreaded() throws InterruptedException, URISyntaxException, IOException
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }
        CountDownLatch cdl = new CountDownLatch(clientArrayList.size());

        IotHubServicesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            new Thread(
                    new MultiplexRunnable(
                            deviceListAmqps[i], clientArrayList.get(i), NUM_MESSAGES_PER_CONNECTION, NUM_KEYS_PER_MESSAGE, SEND_TIMEOUT_MILLISECONDS, cdl))
                    .start();
        }
        cdl.await();

        if(!succeed.get())
        {
            Assert.fail("Sending message over AMQP protocol in parallel failed");
        }

        transportClient.closeNow();
    }

    @Test
    public void sendMessagesOverAmqpsWsMultithreaded() throws InterruptedException, URISyntaxException, IOException
    {
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        CountDownLatch cdl = new CountDownLatch(clientArrayList.size());

        IotHubServicesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            new Thread(
                    new MultiplexRunnable(
                            deviceListAmqps[i], clientArrayList.get(i), NUM_MESSAGES_PER_CONNECTION, NUM_KEYS_PER_MESSAGE, SEND_TIMEOUT_MILLISECONDS, cdl))
                    .start();
        }
        cdl.await();

        if(!succeed.get())
        {
            Assert.fail("Sending message over AMQP protocol in parallel failed");
        }

        transportClient.closeNow();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileAndTelemetryOnAMQP() throws Exception
    {
        // arrange
        setUpFileUploadState();
        TransportClient transportClient = new TransportClient(AMQPS);
        final ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // act
        for (int j = 0; j < clientArrayList.size(); j++)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                final int indexI = i;
                final int indexJ = j;
                executor.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            clientArrayList.get(indexJ).uploadToBlobAsync(fileUploadState[indexI].blobName, fileUploadState[indexI].fileInputStream, fileUploadState[indexI].fileLength, new FileUploadCallback(), fileUploadState[indexI]);
                        } catch (IOException e)
                        {
                            Assert.fail(e.getMessage());
                        }
                    }
                });

                executor.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        clientArrayList.get(indexJ).sendEventAsync(new com.microsoft.azure.sdk.iot.device.Message(messageStates[indexI].messageBody), new FileUploadCallback(), messageStates[indexI]);
                    }
                });

                // assert
                FileUploadNotification fileUploadNotification = fileUploadNotificationReceiver.receive(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
                assertNotNull(fileUploadNotification);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
                assertTrue(fileUploadState[i].isCallBackTriggered);
                assertEquals(fileUploadState[i].fileUploadStatus, SUCCESS);
                assertEquals(messageStates[i].messageStatus, SUCCESS);
            }
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            assertEquals("File" + i + " has no notification", fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
        }

        transportClient.closeNow();
        tearDownFileUploadState();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileAndTelemetryOnAMQPWS() throws Exception
    {
        // arrange
        setUpFileUploadState();
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        final ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // act
        for (int j = 0; j < clientArrayList.size(); j++)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                final int indexI = i;
                final int indexJ = j;
                executor.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            clientArrayList.get(indexJ).uploadToBlobAsync(fileUploadState[indexI].blobName, fileUploadState[indexI].fileInputStream, fileUploadState[indexI].fileLength, new FileUploadCallback(), fileUploadState[indexI]);
                        } catch (IOException e)
                        {
                            Assert.fail(e.getMessage());
                        }
                    }
                });

                executor.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        clientArrayList.get(indexJ).sendEventAsync(new com.microsoft.azure.sdk.iot.device.Message(messageStates[indexI].messageBody), new FileUploadCallback(), messageStates[indexI]);
                    }
                });

                // assert
                FileUploadNotification fileUploadNotification = fileUploadNotificationReceiver.receive(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
                assertNotNull(fileUploadNotification);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
                assertTrue(fileUploadState[i].isCallBackTriggered);
                assertEquals(fileUploadState[i].fileUploadStatus, SUCCESS);
                assertEquals(messageStates[i].messageStatus, SUCCESS);
            }
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            assertEquals("File" + i + " has no notification", fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
        }

        transportClient.closeNow();
        tearDownFileUploadState();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void invokeMethodAMQPSSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            clientArrayList.get(i).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);

            CountDownLatch countDownLatch = new CountDownLatch(1);
            RunnableInvoke runnableInvoke = new RunnableInvoke(methodServiceClient, deviceListAmqps[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            countDownLatch.await();

            MethodResult result = runnableInvoke.getResult();
            assertNotNull((runnableInvoke.getException() == null ? "Runnable returns null without exception information" : runnableInvoke.getException().getMessage()), result);
            assertEquals((long)METHOD_SUCCESS,(long)result.getStatus());
            assertEquals(runnableInvoke.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void invokeMethodAMQPSInvokeParallelSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            clientArrayList.get(i).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);
        }

        List<RunnableInvoke> runs = new LinkedList<>();
        CountDownLatch countDownLatch = new CountDownLatch(MAX_DEVICE_MULTIPLEX);
        for (int i = 0; i < clientArrayList.size(); i++)
        {
            RunnableInvoke runnableInvoke = new RunnableInvoke(methodServiceClient, deviceListAmqps[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            runs.add(runnableInvoke);
        }
        countDownLatch.await();

        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_PER_CALL_MS * MAX_DEVICE_MULTIPLEX);

        for (RunnableInvoke run:runs)
        {
            MethodResult result = run.getResult();
            assertNotNull((run.getException() == null ? "Runnable returns null without exception information" : run.getException().getMessage()), result);
            assertEquals((long)METHOD_SUCCESS,(long)result.getStatus());
            assertEquals(run.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void invokeMethodAMQPSWSSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            clientArrayList.get(i).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);

            CountDownLatch countDownLatch = new CountDownLatch(1);
            RunnableInvoke runnableInvoke = new RunnableInvoke(methodServiceClient, deviceListAmqps[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            countDownLatch.await();

            MethodResult result = runnableInvoke.getResult();
            assertNotNull((runnableInvoke.getException() == null ? "Runnable returns null without exception information" : runnableInvoke.getException().getMessage()), result);
            assertEquals((long)METHOD_SUCCESS,(long)result.getStatus());
            assertEquals(runnableInvoke.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void invokeMethodAMQPSWSInvokeParallelSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            clientArrayList.get(i).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);
        }

        List<RunnableInvoke> runs = new LinkedList<>();
        CountDownLatch countDownLatch = new CountDownLatch(MAX_DEVICE_MULTIPLEX);
        for (int i = 0; i < clientArrayList.size(); i++)
        {
            RunnableInvoke runnableInvoke = new RunnableInvoke(methodServiceClient, deviceListAmqps[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            runs.add(runnableInvoke);
        }
        countDownLatch.await();

        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_PER_CALL_MS * MAX_DEVICE_MULTIPLEX);

        for (RunnableInvoke run:runs)
        {
            MethodResult result = run.getResult();
            assertNotNull((run.getException() == null ? "Runnable returns null without exception information" : run.getException().getMessage()), result);
            assertEquals((long)METHOD_SUCCESS,(long)result.getStatus());
            assertEquals(run.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testTwin() throws IOException, InterruptedException, IotHubException, URISyntaxException
    {
        TransportClient transportClient = setUpTwin();

        ExecutorService executor = Executors.newFixedThreadPool(MAX_PROPERTIES_TO_TEST);

        System.out.println("Testing subscribing to desired properties...");
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            for (int j = 0; j < MAX_PROPERTIES_TO_TEST; j++)
            {
                PropertyState propertyState = new PropertyState();
                propertyState.callBackTriggered = false;
                propertyState.property = new Property(PROPERTY_KEY + j, PROPERTY_VALUE);
                devicesUnderTest.get(i).dCDeviceForTwin.propertyStateList.add(propertyState);
                devicesUnderTest.get(i).dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, devicesUnderTest.get(i).dCDeviceForTwin, propertyState);
            }

            // act
            devicesUnderTest.get(i).deviceClient.subscribeToDesiredProperties(devicesUnderTest.get(i).dCDeviceForTwin.getDesiredProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);

            Set<Pair> desiredProperties = new HashSet<>();
            for (int j = 0; j < MAX_PROPERTIES_TO_TEST; j++)
            {
                desiredProperties.add(new Pair(PROPERTY_KEY + j, PROPERTY_VALUE_UPDATE + UUID.randomUUID()));
            }
            devicesUnderTest.get(i).sCDeviceForTwin.setDesiredProperties(desiredProperties);
            sCDeviceTwin.updateTwin(devicesUnderTest.get(i).sCDeviceForTwin);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);

            // assert
            assertEquals(STATUS.SUCCESS, devicesUnderTest.get(i).deviceTwinStatus);
            for (PropertyState propertyState : devicesUnderTest.get(i).dCDeviceForTwin.propertyStateList)
            {
                assertTrue("One or more property callbacks were not triggered", propertyState.callBackTriggered);
                assertTrue(((String) propertyState.propertyNewValue).startsWith(PROPERTY_VALUE_UPDATE));
                assertEquals(STATUS.SUCCESS, devicesUnderTest.get(i).deviceTwinStatus);
            }
        }

        System.out.println("Testing updating reported properties...");
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            // act
            // send max_prop RP all at once
            devicesUnderTest.get(i).dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
            devicesUnderTest.get(i).deviceClient.sendReportedProperties(devicesUnderTest.get(i).dCDeviceForTwin.getReportedProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);

            // act
            // Update RP
            devicesUnderTest.get(i).dCDeviceForTwin.updateAllExistingReportedProperties();
            devicesUnderTest.get(i).deviceClient.sendReportedProperties(devicesUnderTest.get(i).dCDeviceForTwin.getReportedProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);

            // assert
            assertEquals(devicesUnderTest.get(i).deviceTwinStatus, STATUS.SUCCESS);

            // verify if they are received by SC
            Thread.sleep(MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS);
            int actualReportedPropFound = readReportedProperties(devicesUnderTest.get(i), PROPERTY_KEY, PROPERTY_VALUE_UPDATE);
            assertEquals("Missing reported properties on the " + (i+1) + " device out of " + MAX_DEVICE_MULTIPLEX, MAX_PROPERTIES_TO_TEST.intValue(), actualReportedPropFound);
        }

        // send max_prop RP one at a time in parallel
        System.out.println("Testing sending reported properties...");
        for (int i = 0; i < MAX_PROPERTIES_TO_TEST; i++)
        {
            final int finalI = i;
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    // testSendReportedPropertiesMultiThreaded
                    try
                    {
                        devicesUnderTest.get(finalI).dCDeviceForTwin.createNewReportedProperties(1);
                        devicesUnderTest.get(finalI).deviceClient.sendReportedProperties(devicesUnderTest.get(finalI).dCDeviceForTwin.getReportedProp());
                    }
                    catch (IOException e)
                    {
                        Assert.fail(e.getMessage());
                    }
                    assertEquals(devicesUnderTest.get(finalI).deviceTwinStatus, STATUS.SUCCESS);

                    // testUpdateReportedPropertiesMultiThreaded
                    try
                    {
                        devicesUnderTest.get(finalI).dCDeviceForTwin.updateExistingReportedProperty(finalI);
                        devicesUnderTest.get(finalI).deviceClient.sendReportedProperties(devicesUnderTest.get(finalI).dCDeviceForTwin.getReportedProp());
                    }
                    catch (IOException e)
                    {
                        Assert.fail(e.getMessage());
                    }
                    assertEquals(devicesUnderTest.get(finalI).deviceTwinStatus, STATUS.SUCCESS);
                }
            });
        }
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);
        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        System.out.println("Tearing down twin status...");
        tearDownTwin(transportClient);
    }
    
    private void verifyNotification(FileUploadNotification fileUploadNotification, FileUploadState fileUploadState) throws IOException
    {
        assertTrue(fileUploadNotification.getBlobSizeInBytes() == fileUploadState.fileLength);

        URL u = new URL(fileUploadNotification.getBlobUri());
        try (InputStream inputStream = u.openStream())
        {
            byte[] testBuf = new byte[(int)fileUploadState.fileLength];
            int testLen = inputStream.read(testBuf,  0, (int)fileUploadState.fileLength);
            byte[] actualBuf = new byte[(int)fileUploadState.fileLength];
            fileUploadState.fileInputStream.reset();
            int actualLen = (fileUploadState.fileLength == 0) ? (int) fileUploadState.fileLength : fileUploadState.fileInputStream.read(actualBuf, 0, (int) fileUploadState.fileLength);
            assertEquals(testLen, actualLen);
            assertTrue(Arrays.equals(testBuf, actualBuf));
        }

        assertTrue(fileUploadNotification.getBlobName().contains(fileUploadState.blobName));
        fileUploadState.fileUploadNotificationReceived = SUCCESS;
    }

    private static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            Boolean resultValue = true;
            Success messageReceived = (Success)context;
            if (!hasExpectedProperties(msg, messageProperties) || !hasExpectedSystemProperties(msg))
            {
                resultValue = false;
            }

            messageReceived.callbackWasFired();

            messageReceived.setResult(resultValue);
            return IotHubMessageResult.COMPLETE;
        }
    }

    private static boolean hasExpectedProperties(Message msg, Map<String, String> messageProperties)
    {
        for (String key : messageProperties.keySet())
        {
            if (msg.getProperty(key) == null || !msg.getProperty(key).equals(messageProperties.get(key)))
            {
                return false;
            }
        }

        return true;
    }

    private static boolean hasExpectedSystemProperties(Message msg)
    {
        if (msg.getCorrelationId() == null || !msg.getCorrelationId().equals(expectedCorrelationId))
        {
            return false;
        }

        if (msg.getMessageId() == null || !msg.getMessageId().equals(expectedMessageId))
        {
            return false;
        }

        //all system properties are as expected
        return true;
    }

    private void sendMessageToDevice(String deviceId, String protocolName) throws IotHubException, IOException
    {
        String messageString = "Java service e2e test message to be received over " + protocolName + " protocol";
        com.microsoft.azure.sdk.iot.service.Message serviceMessage = new com.microsoft.azure.sdk.iot.service.Message(messageString);
        serviceMessage.setCorrelationId(expectedCorrelationId);
        serviceMessage.setMessageId(expectedMessageId);
        serviceMessage.setProperties(messageProperties);
        serviceClient.send(deviceId, serviceMessage);
    }

    private void waitForMessageToBeReceived(Success messageReceived, String protocolName)
    {
        try
        {
            long startTime = System.currentTimeMillis();
            while (!messageReceived.wasCallbackFired())
            {
                Thread.sleep(100);

                if (System.currentTimeMillis() - startTime > RECEIVE_MESSAGE_TIMEOUT)
                {
                    fail("Timed out waiting for message to be received");
                }
            }

            if (!messageReceived.getResult())
            {
                Assert.fail("Receiving message over " + protocolName + " protocol failed. Received message was missing one or more properties");
            }
        }
        catch (InterruptedException e)
        {
            Assert.fail("Receiving message over " + protocolName + " protocol failed");
        }
    }

    enum STATUS
    {
        SUCCESS, FAILURE
    }

    private static class FileUploadState
    {
        String blobName;
        InputStream fileInputStream;
        long fileLength;
        boolean isCallBackTriggered;
        STATUS fileUploadStatus;
        STATUS fileUploadNotificationReceived;
    }

    private static class MessageState
    {
        String messageBody;
        STATUS messageStatus;
    }

    private static class FileUploadCallback implements IotHubEventCallback
    {
        @Override
        public void execute(IotHubStatusCode responseStatus, Object context)
        {
            if (context instanceof FileUploadState)
            {
                FileUploadState fileUploadState = (FileUploadState) context;
                fileUploadState.isCallBackTriggered = true;

                // On failure, Don't update fileUploadStatus any further
                if ((responseStatus == OK || responseStatus == OK_EMPTY) && fileUploadState.fileUploadStatus != FAILURE)
                {
                    fileUploadState.fileUploadStatus = SUCCESS;
                }
                else
                {
                    fileUploadState.fileUploadStatus = FAILURE;
                }
            }
            else if (context instanceof MessageState)
            {
                MessageState messageState = (MessageState) context;
                // On failure, Don't update message status any further
                if ((responseStatus == OK || responseStatus == OK_EMPTY) && messageState.messageStatus != FAILURE)
                {
                    messageState.messageStatus = SUCCESS;
                }
                else
                {
                    messageState.messageStatus = FAILURE;
                }
            }
        }
    }

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
                result = methodServiceClient.invoke(deviceId, methodName, RESPONSE_TIMEOUT_SECONDS, CONNECTION_TIMEOUT_SECONDS, methodPayload);
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

    protected static class MultiplexRunnable implements Runnable
    {
        private DeviceClient client;
        private String messageString;

        private long numMessagesPerDevice;
        private long sendTimeout;
        private long numKeys;
        private CountDownLatch latch;

        @Override
        public void run()
        {
            try
            {
                this.sendMessages();
            }
            catch (Exception e)
            {
                succeed.set(false);
                for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
                {
                    //test has already failed, don't need to wait for other threads to finish
                    this.latch.countDown();
                }
            }
            latch.countDown();
        }

        private MultiplexRunnable(Device deviceAmqps,
                                 DeviceClient deviceClient,
                                 long numMessagesPerConnection,
                                 long numKeys,
                                 long sendTimeout,
                                 CountDownLatch latch)
        {
            this.client = deviceClient;
            this.numMessagesPerDevice = numMessagesPerConnection;
            this.sendTimeout = sendTimeout;
            this.numKeys = numKeys;
            this.latch = latch;

            succeed.set(true);

            messageString = "Java client " + deviceAmqps.getDeviceId() + " test e2e message over AMQP protocol";
        }

        private void sendMessages() throws Exception
        {
            for (int i = 0; i < numMessagesPerDevice; ++i)
            {
                Message msgSend = new Message(messageString);
                msgSend.setProperty("messageCount", Integer.toString(i));
                for (int j = 0; j < numKeys; j++)
                {
                    msgSend.setProperty("key"+j, "value"+j);
                }

                Success messageSent = new Success();
                EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
                client.sendEventAsync(msgSend, callback, messageSent);

                long startTime = System.currentTimeMillis();
                while(!messageSent.wasCallbackFired())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if ((System.currentTimeMillis() - startTime) > sendTimeout)
                    {
                        throw new Exception("Timed out waiting for OK_EMPTY response for sent message");
                    }
                }

                if (messageSent.getCallbackStatusCode() != IotHubStatusCode.OK_EMPTY)
                {
                    throw new Exception("Unexpected iot hub status code! Expected OK_EMPTY but got " + messageSent.getCallbackStatusCode());
                }
            }
        }
    }

    class DeviceState
    {
        DeviceClient deviceClient;
        String connectionString;
        DeviceExtension dCDeviceForTwin;
        STATUS deviceTwinStatus;

        com.microsoft.azure.sdk.iot.service.Device sCDeviceForRegistryManager;
        DeviceTwinDevice sCDeviceForTwin;
    }

    class PropertyState
    {
        boolean callBackTriggered;
        Property property;
        Object propertyNewValue;
    }

    class DeviceExtension extends com.microsoft.azure.sdk.iot.device.DeviceTwin.Device
    {
        List<PropertyState> propertyStateList = new LinkedList<>();

        @Override
        public void PropertyCall(String propertyKey, Object propertyValue, Object context)
        {
            PropertyState propertyState = (PropertyState) context;
            if (propertyKey.equals(propertyState.property.getKey()))
            {
                propertyState.callBackTriggered = true;
                propertyState.propertyNewValue = propertyValue;
            }
        }

        synchronized void createNewReportedProperties(int maximumPropertiesToCreate)
        {
            for( int i = 0; i < maximumPropertiesToCreate; i++)
            {
                UUID randomUUID = UUID.randomUUID();
                this.setReportedProp(new Property(PROPERTY_KEY + randomUUID, PROPERTY_VALUE + randomUUID));
            }
        }

        synchronized void updateAllExistingReportedProperties()
        {
            Set<Property> reportedProp = this.getReportedProp();

            for (Property p : reportedProp)
            {
                UUID randomUUID = UUID.randomUUID();
                p.setValue(PROPERTY_VALUE_UPDATE + randomUUID);
            }
        }

        synchronized void updateExistingReportedProperty(int index)
        {
            Set<Property> reportedProp = this.getReportedProp();
            int i = 0;
            for (Property p : reportedProp)
            {
                if (i == index)
                {
                    UUID randomUUID = UUID.randomUUID();
                    p.setValue(PROPERTY_VALUE_UPDATE + randomUUID);
                    break;
                }
                i++;
            }
        }
    }

    private TransportClient setUpTwin() throws IOException, IotHubException, InterruptedException, URISyntaxException
    {
        sCDeviceTwin = DeviceTwin.createFromConnectionString(iotHubConnectionString);

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            DeviceState deviceState = new DeviceState();
            deviceState.sCDeviceForRegistryManager = deviceListAmqps[i];
            deviceState.connectionString = registryManager.getDeviceConnectionString(deviceState.sCDeviceForRegistryManager);
            devicesUnderTest.add(deviceState);

            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);
        }

        TransportClient transportClient = new TransportClient(IotHubClientProtocol.AMQPS);

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            DeviceState deviceState = devicesUnderTest.get(i);
            deviceState.deviceClient = new DeviceClient(deviceState.connectionString, transportClient);
            devicesUnderTest.get(i).dCDeviceForTwin = new DeviceExtension();
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient);

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            devicesUnderTest.get(i).deviceClient.startDeviceTwin(new DeviceTwinStatusCallBack(), devicesUnderTest.get(i), devicesUnderTest.get(i).dCDeviceForTwin, devicesUnderTest.get(i));
            devicesUnderTest.get(i).deviceTwinStatus = STATUS.SUCCESS;
            devicesUnderTest.get(i).sCDeviceForTwin = new DeviceTwinDevice(devicesUnderTest.get(i).sCDeviceForRegistryManager.getDeviceId());
            sCDeviceTwin.getTwin(devicesUnderTest.get(i).sCDeviceForTwin);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);
        }

        return transportClient;
    }

    private void tearDownTwin(TransportClient transportClient) throws IOException
    {
        transportClient.closeNow();
        sCDeviceTwin = null;
    }

    private void setUpFileUploadState() throws Exception
    {
        // Start receiver for a test
        fileUploadNotificationReceiver.open();
        fileUploadState = new FileUploadState[MAX_FILES_TO_UPLOAD];
        messageStates = new MessageState[MAX_FILES_TO_UPLOAD];
        for (int i = 0; i < MAX_FILES_TO_UPLOAD; i++)
        {
            byte[] buf = new byte[i];
            new Random().nextBytes(buf);
            fileUploadState[i] = new FileUploadState();
            fileUploadState[i].blobName = REMOTE_FILE_NAME + i + REMOTE_FILE_NAME_EXT;
            fileUploadState[i].fileInputStream = new ByteArrayInputStream(buf);
            fileUploadState[i].fileLength = buf.length;
            fileUploadState[i].fileUploadStatus = SUCCESS;
            fileUploadState[i].fileUploadNotificationReceived = FAILURE;
            fileUploadState[i].isCallBackTriggered = false;

            messageStates[i] = new MessageState();
            messageStates[i].messageBody = new String(buf);
            messageStates[i].messageStatus = SUCCESS;
        }
    }

    private void tearDownFileUploadState()
    {
        fileUploadState = null;
        messageStates = null;
    }

    private int readReportedProperties(DeviceState deviceState, String startsWithKey, String startsWithValue) throws IOException , IotHubException, InterruptedException
    {
        int totalCount = 0;
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);
        sCDeviceTwin.getTwin(deviceState.sCDeviceForTwin);
        Set<Pair> repProperties = deviceState.sCDeviceForTwin.getReportedProperties();

        for (Pair p : repProperties)
        {
            String val = (String) p.getValue();
            if (p.getKey().startsWith(startsWithKey) && val.startsWith(startsWithValue))
            {
                totalCount++;
            }
        }
        return totalCount;
    }

    protected class DeviceTwinStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            DeviceState state = (DeviceState) context;

            //On failure, Don't update status any further
            if ((status == OK || status == OK_EMPTY ) && state.deviceTwinStatus != STATUS.FAILURE)
            {
                state.deviceTwinStatus = STATUS.SUCCESS;
            }
            else
            {
                state.deviceTwinStatus = STATUS.FAILURE;
            }
        }
    }
    
}
