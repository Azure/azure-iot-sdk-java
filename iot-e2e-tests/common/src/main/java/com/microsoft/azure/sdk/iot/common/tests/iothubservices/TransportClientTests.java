/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

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

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static com.microsoft.azure.sdk.iot.common.helpers.IotHubServicesCommon.openTransportClientWithRetry;
import static com.microsoft.azure.sdk.iot.common.helpers.IotHubServicesCommon.sendMessagesMultiplex;
import static com.microsoft.azure.sdk.iot.common.tests.iothubservices.TransportClientTests.STATUS.FAILURE;
import static com.microsoft.azure.sdk.iot.common.tests.iothubservices.TransportClientTests.STATUS.SUCCESS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS_WS;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static junit.framework.TestCase.fail;

/**
 * Test class containing all tests to be run on JVM and android pertaining to multiplexing with the TransportClient class.
 * Class needs to be extended in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class TransportClientTests extends IntegrationTest
{
    //how many devices to test multiplexing with
    private static final int MAX_DEVICE_MULTIPLEX = 3;

    private static final int NUM_KEYS_PER_MESSAGE = 10;
    private static final int NUM_MESSAGES_PER_CONNECTION = 3;

    private static final long RETRY_MILLISECONDS = 100; //.1 seconds
    private static final long SEND_TIMEOUT_MILLISECONDS = 5 * 60 * 1000; // 5 minutes
    private static final long INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;
    private static final long RECEIVE_MESSAGE_TIMEOUT = 5 * 60 * 1000; // 5 minutes
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 10 * 1000; // 10 seconds
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION = 500; // .5 seconds
    private static final long RESPONSE_TIMEOUT_SECONDS = 200; // 200 seconds
    private static final long CONNECTION_TIMEOUT_SECONDS = 5; //5 seconds
    private static final long MULTITHREADED_WAIT_TIMEOUT_MS  = 5 * 60 * 1000; // 5 minutes
    private static final long MAX_MILLISECS_TIMEOUT_FLUSH_NOTIFICATION = 10 * 1000; // 10 secs
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_PER_CALL_MS = 1000; // 1 second
    private static final long MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION * 10; // 2 sec
    private static final long REGISTRY_MANAGER_DEVICE_CREATION_DELAY_MILLISECONDS = 3 * 1000;

    private static Device[] deviceListAmqps = new Device[MAX_DEVICE_MULTIPLEX];
    private static final AtomicBoolean succeed = new AtomicBoolean();

    protected static String iotHubConnectionString = "";

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

    private static String[] clientConnectionStringArrayList = new String[MAX_DEVICE_MULTIPLEX];

    private static DeviceMethod methodServiceClient;

    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_NOT_DEFINED = 404;
    private static final String METHOD_NAME = "methodName";
    private static final String METHOD_PAYLOAD = "This is a good payload";

    private static DeviceState[] devicesUnderTest = new DeviceState[MAX_DEVICE_MULTIPLEX];
    private static DeviceTwin sCDeviceTwin;

    private static final String PROPERTY_KEY = "Key";
    private static final String PROPERTY_VALUE = "Value";
    private static final String PROPERTY_VALUE_UPDATE = "Update";

    private static final Integer MAX_PROPERTIES_TO_TEST = 3;
    private static final Integer MAX_DEVICES = 3;

    public static void setUpCommon() throws Exception
    {
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        String uuid = UUID.randomUUID().toString();

        System.out.print("TransportClientTests UUID: " + uuid);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            String deviceId = "java-device-client-e2e-test-multiplexing".concat(i + "-" + uuid);

            deviceListAmqps[i] = Device.createFromId(deviceId, null, null);
            Tools.addDeviceWithRetry(registryManager, deviceListAmqps[i]);
            clientConnectionStringArrayList[i] = registryManager.getDeviceConnectionString(deviceListAmqps[i]);
        }

        Thread.sleep(MAX_DEVICE_MULTIPLEX * REGISTRY_MANAGER_DEVICE_CREATION_DELAY_MILLISECONDS);

        messageProperties = new HashMap<>(3);
        messageProperties.put("name1", "value1");
        messageProperties.put("name2", "value2");
        messageProperties.put("name3", "value3");

        serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        serviceClient.open();

        fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();
        Assert.assertNotNull(fileUploadNotificationReceiver);

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
        Assert.assertNotNull(fileUploadNotificationReceiver);
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
            e.printStackTrace();
            fail("Unexpected exception encountered");
        }
    }

    @Test
    public void sendMessagesOverAmqps() throws URISyntaxException, IOException, InterruptedException
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }

        openTransportClientWithRetry(transportClient, clientArrayList);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            sendMessagesMultiplex(clientArrayList.get(i), IotHubClientProtocol.AMQPS, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
        }

        transportClient.closeNow();
    }

    @Test
    public void sendMessagesOverAmqpsWs() throws URISyntaxException, IOException, InterruptedException
    {
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }

        openTransportClientWithRetry(transportClient, clientArrayList);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            sendMessagesMultiplex(clientArrayList.get(i), IotHubClientProtocol.AMQPS_WS, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
        }

        transportClient.closeNow();
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void receiveMessagesOverAmqpsIncludingProperties() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        final ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient, clientArrayList);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            Success messageReceived = new Success();
            com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();
            ((DeviceClient)clientArrayList.get(i)).setMessageCallback(callback, messageReceived);

            sendMessageToDevice(deviceListAmqps[i].getDeviceId(), "AMQPS");
            waitForMessageToBeReceived(messageReceived, "AMQPS", clientArrayList.get(i));

            Thread.sleep(200);
        }

        transportClient.closeNow();
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void receiveMessagesOverAmqpWSIncludingProperties() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        final ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient, clientArrayList);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            Success messageReceived = new Success();
            com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback();
            ((DeviceClient)clientArrayList.get(i)).setMessageCallback(callback, messageReceived);

            sendMessageToDevice(deviceListAmqps[i].getDeviceId(), "AMQPS_WS");
            waitForMessageToBeReceived(messageReceived, "AMQPS_WS", clientArrayList.get(i));

            Thread.sleep(200);
        }

        transportClient.closeNow();
    }

    @Test
    public void sendMessagesOverAmqpsMultithreaded() throws InterruptedException, URISyntaxException, IOException
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }
        CountDownLatch cdl = new CountDownLatch(clientArrayList.size());

        IotHubServicesCommon.openTransportClientWithRetry(transportClient, clientArrayList);

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
            Assert.fail(CorrelationDetailsLoggingAssert.buildExceptionMessage("Sending message over AMQP protocol in parallel failed", clientArrayList));
        }

        transportClient.closeNow();
    }

    @Test
    public void sendMessagesOverAmqpsWsMultithreaded() throws InterruptedException, URISyntaxException, IOException
    {
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }

        CountDownLatch cdl = new CountDownLatch(clientArrayList.size());

        IotHubServicesCommon.openTransportClientWithRetry(transportClient, clientArrayList);

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
            Assert.fail(CorrelationDetailsLoggingAssert.buildExceptionMessage("Sending message over AMQP protocol in parallel failed", clientArrayList));
        }

        transportClient.closeNow();
    }

    @Test
    public void uploadToBlobAsyncSingleFileAndTelemetryOnAMQP() throws Exception
    {
        // arrange
        setUpFileUploadState();
        TransportClient transportClient = new TransportClient(AMQPS);
        final ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient, clientArrayList);

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
                            ((DeviceClient)clientArrayList.get(indexJ)).uploadToBlobAsync(fileUploadState[indexI].blobName, fileUploadState[indexI].fileInputStream, fileUploadState[indexI].fileLength, new FileUploadCallback(), fileUploadState[indexI]);
                        }
                        catch (IOException e)
                        {
                            Assert.fail(buildExceptionMessage(e.getMessage(), clientArrayList.get(indexJ)));
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
                Assert.assertNotNull(buildExceptionMessage("file upload notification was null", clientArrayList.get(indexJ)), fileUploadNotification);
                verifyNotification(fileUploadNotification, fileUploadState[i], clientArrayList.get(indexJ));
                Assert.assertTrue(buildExceptionMessage("File upload callback was not triggered for file upload attempt " + i, clientArrayList.get(indexJ)), fileUploadState[i].isCallBackTriggered);
                Assert.assertEquals(buildExceptionMessage("File upload status was not successful on attempt " + i + ", status was: " + fileUploadState[i].fileUploadStatus, clientArrayList.get(indexJ)), fileUploadState[i].fileUploadStatus, SUCCESS);
                Assert.assertEquals(buildExceptionMessage("Message status was not successful on attempt " + i + ", status was: " + messageStates[i].messageStatus, clientArrayList.get(indexJ)), messageStates[i].messageStatus, SUCCESS);
            }
        }

        executor.shutdown();
        if (!executor.awaitTermination(MULTITHREADED_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("File" + i + " has no notification", clientArrayList), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
        }

        transportClient.closeNow();
        tearDownFileUploadState();
    }

    @Test
    public void uploadToBlobAsyncSingleFileAndTelemetryOnAMQPWS() throws Exception
    {
        // arrange
        setUpFileUploadState();
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        final ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient, clientArrayList);

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
                            ((DeviceClient)clientArrayList.get(indexJ)).uploadToBlobAsync(fileUploadState[indexI].blobName, fileUploadState[indexI].fileInputStream, fileUploadState[indexI].fileLength, new FileUploadCallback(), fileUploadState[indexI]);
                        }
                        catch (IOException e)
                        {
                            Assert.fail(buildExceptionMessage(e.getMessage(), clientArrayList.get(indexJ)));
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
                Assert.assertNotNull(buildExceptionMessage("File upload notification was null", clientArrayList.get(j)), fileUploadNotification);
                verifyNotification(fileUploadNotification, fileUploadState[i], clientArrayList.get(indexJ));
                Assert.assertTrue(buildExceptionMessage("file upload state callback was not triggered", clientArrayList.get(j)), fileUploadState[i].isCallBackTriggered);
                Assert.assertEquals(buildExceptionMessage("File upload status was not successful on attempt " + i + ", status was: " + fileUploadState[i].fileUploadStatus, clientArrayList.get(indexJ)), fileUploadState[i].fileUploadStatus, SUCCESS);
                Assert.assertEquals(buildExceptionMessage("Message status was not successful on attempt " + i + ", status was: " + messageStates[i].messageStatus, clientArrayList.get(indexJ)), messageStates[i].messageStatus, SUCCESS);

            }
        }

        executor.shutdown();
        if (!executor.awaitTermination(MULTITHREADED_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("File" + i + " has no notification", clientArrayList), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
        }

        transportClient.closeNow();
        tearDownFileUploadState();
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodAMQPSSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient, clientArrayList);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            ((DeviceClient)clientArrayList.get(i)).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);

            CountDownLatch countDownLatch = new CountDownLatch(1);
            RunnableInvoke runnableInvoke = new RunnableInvoke(methodServiceClient, deviceListAmqps[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            countDownLatch.await();

            MethodResult result = runnableInvoke.getResult();
            Assert.assertNotNull(buildExceptionMessage(runnableInvoke.getException() == null ? "Runnable returns null without exception information" : runnableInvoke.getException().getMessage(), clientArrayList.get(i)), result);
            Assert.assertEquals(buildExceptionMessage("result was not success, but was: " + result.getStatus(), clientArrayList.get(i)), (long)METHOD_SUCCESS,(long)result.getStatus());
            Assert.assertEquals(buildExceptionMessage("Received unexpected payload", clientArrayList.get(i)), runnableInvoke.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodAMQPSInvokeParallelSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS);
        ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient, clientArrayList);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            ((DeviceClient)clientArrayList.get(i)).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);
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

        for (RunnableInvoke runnableInvoke:runs)
        {
            MethodResult result = runnableInvoke.getResult();
            Assert.assertNotNull(CorrelationDetailsLoggingAssert.buildExceptionMessage(runnableInvoke.getException() == null ? "Runnable returns null without exception information" : runnableInvoke.getException().getMessage(), clientArrayList), result);
            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("result was not success, but was: " + result.getStatus(), clientArrayList), (long)METHOD_SUCCESS,(long)result.getStatus());
            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Received unexpected payload", clientArrayList), runnableInvoke.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodAMQPSWSSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient, clientArrayList);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            ((DeviceClient)clientArrayList.get(i)).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);

            CountDownLatch countDownLatch = new CountDownLatch(1);
            RunnableInvoke runnableInvoke = new RunnableInvoke(methodServiceClient, deviceListAmqps[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            countDownLatch.await();

            MethodResult result = runnableInvoke.getResult();
            Assert.assertNotNull(buildExceptionMessage(runnableInvoke.getException() == null ? "Runnable returns null without exception information" : runnableInvoke.getException().getMessage(), clientArrayList.get(i)), result);
            Assert.assertEquals(buildExceptionMessage("result was not success, but was: " + result.getStatus(), clientArrayList.get(i)), (long)METHOD_SUCCESS,(long)result.getStatus());
            Assert.assertEquals(buildExceptionMessage("Received unexpected payload", clientArrayList.get(i)), runnableInvoke.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void invokeMethodAMQPSWSInvokeParallelSucceed() throws Exception
    {
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        transportClient.open();
        ArrayList<InternalClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList[i], transportClient));
        }

        IotHubServicesCommon.openTransportClientWithRetry(transportClient, clientArrayList);

        for (int i = 0; i < clientArrayList.size(); i++)
        {
            ((DeviceClient)clientArrayList.get(i)).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);
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

        for (RunnableInvoke runnableInvoke:runs)
        {
            MethodResult result = runnableInvoke.getResult();
            Assert.assertNotNull(CorrelationDetailsLoggingAssert.buildExceptionMessage(runnableInvoke.getException() == null ? "Runnable returns null without exception information" : runnableInvoke.getException().getMessage(), clientArrayList), result);
            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("result was not success, but was: " + result.getStatus(), clientArrayList), (long)METHOD_SUCCESS,(long)result.getStatus());
            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Received unexpected payload", clientArrayList), runnableInvoke.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        transportClient.closeNow();
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void testTwin() throws IOException, InterruptedException, IotHubException, URISyntaxException
    {
        TransportClient transportClient = null;

        transportClient = setUpTwin();

        ExecutorService executor = Executors.newFixedThreadPool(MAX_PROPERTIES_TO_TEST);

        //Testing subscribing to desired properties.
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            devicesUnderTest[i].dCDeviceForTwin.getDesiredProp().clear();
            for (int j = 0; j < MAX_PROPERTIES_TO_TEST; j++)
            {
                PropertyState propertyState = new PropertyState();
                propertyState.callBackTriggered = false;
                propertyState.property = new Property(PROPERTY_KEY + j, PROPERTY_VALUE);
                devicesUnderTest[i].dCDeviceForTwin.propertyStateList.add(propertyState);
                devicesUnderTest[i].dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, devicesUnderTest[i].dCDeviceForTwin, propertyState);
            }

            // act
            devicesUnderTest[i].deviceClient.subscribeToDesiredProperties(devicesUnderTest[i].dCDeviceForTwin.getDesiredProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);

            Set<Pair> desiredProperties = new HashSet<>();
            for (int j = 0; j < MAX_PROPERTIES_TO_TEST; j++)
            {
                desiredProperties.add(new Pair(PROPERTY_KEY + j, PROPERTY_VALUE_UPDATE + UUID.randomUUID()));
            }
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);

            // assert
            Assert.assertEquals(buildExceptionMessage("Device twin status expected to be SUCCESS, but was: " + devicesUnderTest[i].deviceTwinStatus, devicesUnderTest[i].deviceClient), SUCCESS, devicesUnderTest[i].deviceTwinStatus);
            for (PropertyState propertyState : devicesUnderTest[i].dCDeviceForTwin.propertyStateList)
            {
                Assert.assertTrue(buildExceptionMessage("One or more property callbacks were not triggered", devicesUnderTest[i].deviceClient), propertyState.callBackTriggered);
                Assert.assertTrue(buildExceptionMessage("Property new value did not start with " + PROPERTY_VALUE_UPDATE, devicesUnderTest[i].deviceClient), ((String) propertyState.propertyNewValue).startsWith(PROPERTY_VALUE_UPDATE));
                Assert.assertEquals(buildExceptionMessage("Expected SUCCESS but device twin status was " + devicesUnderTest[i].deviceTwinStatus, devicesUnderTest[i].deviceClient), SUCCESS, devicesUnderTest[i].deviceTwinStatus);
            }
        }

        //Testing updating reported properties
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            // act
            // send max_prop RP all at once
            devicesUnderTest[i].dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
            devicesUnderTest[i].deviceClient.sendReportedProperties(devicesUnderTest[i].dCDeviceForTwin.getReportedProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);

            // act
            // Update RP
            devicesUnderTest[i].dCDeviceForTwin.updateAllExistingReportedProperties();
            devicesUnderTest[i].deviceClient.sendReportedProperties(devicesUnderTest[i].dCDeviceForTwin.getReportedProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);

            // assert
            Assert.assertEquals(buildExceptionMessage("Expected status SUCCESS but was " + devicesUnderTest[i].deviceTwinStatus, devicesUnderTest[i].deviceClient), SUCCESS, devicesUnderTest[i].deviceTwinStatus);

            // verify if they are received by SC
            Thread.sleep(MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS);
            int actualReportedPropFound = readReportedProperties(devicesUnderTest[i], PROPERTY_KEY, PROPERTY_VALUE_UPDATE);
            Assert.assertEquals(buildExceptionMessage("Missing reported properties on the " + (i+1) + " device out of " + MAX_DEVICE_MULTIPLEX,devicesUnderTest[i].deviceClient), MAX_PROPERTIES_TO_TEST.intValue(), actualReportedPropFound);
        }

        //Testing sending reported properties, send max_prop RP one at a time in parallel
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
                        devicesUnderTest[finalI].dCDeviceForTwin.createNewReportedProperties(1);
                        devicesUnderTest[finalI].deviceClient.sendReportedProperties(devicesUnderTest[finalI].dCDeviceForTwin.getReportedProp());
                    }
                    catch (IOException e)
                    {
                        Assert.fail(buildExceptionMessage(e.getMessage(), devicesUnderTest[finalI].deviceClient));
                    }
                    Assert.assertEquals(buildExceptionMessage("Expected SUCCESS but was " + devicesUnderTest[finalI].deviceTwinStatus, devicesUnderTest[finalI].deviceClient), SUCCESS, devicesUnderTest[finalI].deviceTwinStatus);

                    // testUpdateReportedPropertiesMultiThreaded
                    try
                    {
                        devicesUnderTest[finalI].dCDeviceForTwin.updateExistingReportedProperty(finalI);
                        devicesUnderTest[finalI].deviceClient.sendReportedProperties(devicesUnderTest[finalI].dCDeviceForTwin.getReportedProp());
                    }
                    catch (IOException e)
                    {
                        Assert.fail(buildExceptionMessage(e.getMessage(), devicesUnderTest[finalI].deviceClient));
                    }
                    Assert.assertEquals(buildExceptionMessage("Expected SUCCESS but was " + devicesUnderTest[finalI].deviceTwinStatus, devicesUnderTest[finalI].deviceClient), SUCCESS, devicesUnderTest[finalI].deviceTwinStatus);
                }
            });
        }
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);
        executor.shutdown();
        if (!executor.awaitTermination(MULTITHREADED_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) //4 minutes
        {
            executor.shutdownNow();
            fail(buildExceptionMessage("Test threads did not finish before timeout", devicesUnderTest[0].deviceClient));
        }

        tearDownTwin(transportClient);
    }
    
    private void verifyNotification(FileUploadNotification fileUploadNotification, FileUploadState fileUploadState, InternalClient client) throws IOException
    {
        Assert.assertTrue(buildExceptionMessage("Wrong file upload notification length", client), fileUploadNotification.getBlobSizeInBytes() == fileUploadState.fileLength);

        URL u = new URL(fileUploadNotification.getBlobUri());
        try (InputStream inputStream = u.openStream())
        {
            byte[] testBuf = new byte[(int)fileUploadState.fileLength];
            int testLen = inputStream.read(testBuf,  0, (int)fileUploadState.fileLength);
            byte[] actualBuf = new byte[(int)fileUploadState.fileLength];
            fileUploadState.fileInputStream.reset();
            int actualLen = (fileUploadState.fileLength == 0) ? (int) fileUploadState.fileLength : fileUploadState.fileInputStream.read(actualBuf, 0, (int) fileUploadState.fileLength);
            Assert.assertEquals(buildExceptionMessage("Incorrect file length", client), testLen, actualLen);
            Assert.assertTrue(buildExceptionMessage("Arrays are not equal", client), Arrays.equals(testBuf, actualBuf));
        }

        Assert.assertTrue(buildExceptionMessage("Incorrect blob name", client), fileUploadNotification.getBlobName().contains(fileUploadState.blobName));
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

    private void waitForMessageToBeReceived(Success messageReceived, String protocolName, InternalClient client)
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
                Assert.fail(buildExceptionMessage("Receiving message over " + protocolName + " protocol failed. Received message was missing one or more properties", client));
            }
        }
        catch (InterruptedException e)
        {
            Assert.fail(buildExceptionMessage("Receiving message over " + protocolName + " protocol failed", client));
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
        private InternalClient client;
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
                                 InternalClient client,
                                 long numMessagesPerConnection,
                                 long numKeys,
                                 long sendTimeout,
                                 CountDownLatch latch)
        {
            this.client = client;
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
                        fail(buildExceptionMessage("Timed out waiting for OK_EMPTY response for sent message", client));
                    }
                }

                if (messageSent.getCallbackStatusCode() != IotHubStatusCode.OK_EMPTY)
                {
                    fail(buildExceptionMessage("Unexpected iot hub status code! Expected OK_EMPTY but got " + messageSent.getCallbackStatusCode(), client));
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

    private TransportClient setUpTwin()
    {
        TransportClient transportClient = null;
        try
        {
            sCDeviceTwin = DeviceTwin.createFromConnectionString(iotHubConnectionString);

            for (int i = 0; i < MAX_DEVICES; i++)
            {
                DeviceState deviceState = new DeviceState();
                deviceState.sCDeviceForRegistryManager = deviceListAmqps[i];
                deviceState.connectionString = registryManager.getDeviceConnectionString(deviceState.sCDeviceForRegistryManager);
                devicesUnderTest[i] = deviceState;

                Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);
            }

            transportClient = new TransportClient(IotHubClientProtocol.AMQPS);

            ArrayList<InternalClient> clientArrayList = new ArrayList<>();
            for (int i = 0; i < MAX_DEVICES; i++)
            {
                DeviceState deviceState = devicesUnderTest[i];
                deviceState.deviceClient = new DeviceClient(deviceState.connectionString, transportClient);
                devicesUnderTest[i].dCDeviceForTwin = new DeviceExtension();
                clientArrayList.add(deviceState.deviceClient);
            }

            IotHubServicesCommon.openTransportClientWithRetry(transportClient, clientArrayList);

            for (int i = 0; i < MAX_DEVICES; i++)
            {
                devicesUnderTest[i].deviceClient.startDeviceTwin(new DeviceTwinStatusCallBack(), devicesUnderTest[i], devicesUnderTest[i].dCDeviceForTwin, devicesUnderTest[i]);
                devicesUnderTest[i].deviceTwinStatus = SUCCESS;
                devicesUnderTest[i].sCDeviceForTwin = new DeviceTwinDevice(devicesUnderTest[i].sCDeviceForRegistryManager.getDeviceId());
                sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);
                Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION);
            }
        }
        catch (Exception e)
        {
            fail("Encountered exception during setUpTwin: " + Tools.getStackTraceFromThrowable(e));
        }

        return transportClient;
    }

    private void tearDownTwin(TransportClient transportClient)
    {
        try
        {
            transportClient.closeNow();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Encountered exception during tearDownTwin: " + Tools.getStackTraceFromThrowable(e));
        }

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
            if ((status == OK || status == OK_EMPTY ) && state.deviceTwinStatus != FAILURE)
            {
                state.deviceTwinStatus = SUCCESS;
            }
            else
            {
                state.deviceTwinStatus = FAILURE;
            }
        }
    }
    
}
