/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import junit.framework.TestCase;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

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

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static junit.framework.TestCase.fail;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.IotHubServicesCommon.sendMessagesMultiplex;
import static tests.integration.com.microsoft.azure.sdk.iot.iothub.TransportClientTests.STATUS.FAILURE;
import static tests.integration.com.microsoft.azure.sdk.iot.iothub.TransportClientTests.STATUS.SUCCESS;

/**
 * Test class containing all tests to be run on JVM and android pertaining to multiplexing with the TransportClient class.
 * Class needs to be extended in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
@IotHubTest
@RunWith(Parameterized.class)
public class TransportClientTests extends IntegrationTest
{
    //how many devices to test multiplexing with
    private static final int MAX_DEVICE_MULTIPLEX = 3;

    private static final int NUM_KEYS_PER_MESSAGE = 10;
    private static final int NUM_MESSAGES_PER_CONNECTION = 3;

    private static final long RETRY_MILLISECONDS = 100; //.1 seconds
    private static final long SEND_TIMEOUT_MILLISECONDS = 20 * 1000; // 20 seconds
    private static final long RECEIVE_MESSAGE_TIMEOUT_MILLISECONDS = 20 * 1000; // 20 seconds
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS = 10 * 1000; // 10 seconds
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION_MILLISECONDS = 500; // .5 seconds
    private static final long RESPONSE_TIMEOUT_SECONDS = 200; // 200 seconds
    private static final long CONNECTION_TIMEOUT_SECONDS = 5; //5 seconds
    private static final long MULTITHREADED_WAIT_TIMEOUT_MILLISECONDS = 5 * 60 * 1000; // 5 minutes
    private static final long MAX_TIMEOUT_FLUSH_NOTIFICATIONS_MILLISECONDS = 10 * 1000; // 10 secs
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_PER_CALL_MILLISECONDS = 1000; // 1 second
    private static final long MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS_MILLISECONDS = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION_MILLISECONDS * 10; // 2 sec
    private static final long REGISTRY_MANAGER_DEVICE_CREATION_DELAY_MILLISECONDS = 3 * 1000;
    private static final long METHOD_SUBSCRIBE_TIMEOUT_MILLISECONDS = 15 * 1000; // 15 seconds

    protected static String iotHubConnectionString = "";

    private static ServiceClient serviceClient;

    private static RegistryManager registryManager;

    private static String expectedCorrelationId = "1234";
    private static String expectedMessageId = "5678";

    private static final String REMOTE_FILE_NAME = "File";
    private static final String REMOTE_FILE_NAME_EXT = ".txt";

    private static final Integer MAX_FILES_TO_UPLOAD = 1;

    private static DeviceMethod methodServiceClient;

    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_NOT_DEFINED = 404;
    private static final String METHOD_NAME = "methodName";
    private static final String METHOD_PAYLOAD = "This is a good payload";

    private static final String PROPERTY_KEY = "Key";
    private static final String PROPERTY_VALUE = "Value";
    private static final String PROPERTY_VALUE_UPDATE = "Update";

    private static final Integer MAX_PROPERTIES_TO_TEST = 3;
    private static final Integer MAX_DEVICES = 3;

    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        serviceClient.open();

        methodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);

        return Arrays.asList(
                new Object[][]
                        {
                                {IotHubClientProtocol.AMQPS},
                                {IotHubClientProtocol.AMQPS_WS},
                        });
    }

    public TransportClientTests(IotHubClientProtocol protocol) throws InterruptedException, IOException, IotHubException, URISyntaxException
    {
        this.testInstance = new TransportClientTestInstance(protocol);
    }

    public TransportClientTestInstance testInstance;

    public class TransportClientTestInstance
    {
        public IotHubClientProtocol protocol;
        public AtomicBoolean succeed;
        public Map messageProperties;
        public Device[] devicesList = new Device[MAX_DEVICE_MULTIPLEX];
        public String[] clientConnectionStringArrayList = new String[MAX_DEVICE_MULTIPLEX];
        public ArrayList<InternalClient> clientArrayList;
        public TransportClient transportClient;
        private DeviceState[] devicesUnderTest = new DeviceState[MAX_DEVICE_MULTIPLEX];
        private FileUploadState[] fileUploadState;
        private MessageState[] messageStates;
        private FileUploadNotificationReceiver fileUploadNotificationReceiver;
        private DeviceTwin deviceTwinClient;

        public TransportClientTestInstance(IotHubClientProtocol protocol)
        {
            this.protocol = protocol;
        }

        public void setup() throws InterruptedException, IotHubException, IOException, URISyntaxException
        {
            fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();
            Assert.assertNotNull(fileUploadNotificationReceiver);

            deviceTwinClient = DeviceTwin.createFromConnectionString(iotHubConnectionString);

            String uuid = UUID.randomUUID().toString();

            succeed = new AtomicBoolean();

            System.out.println("TransportClientTests UUID: " + uuid);

            messageProperties = new HashMap<>(3);
            messageProperties.put("name1", "value1");
            messageProperties.put("name2", "value2");
            messageProperties.put("name3", "value3");

            for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
            {
                String deviceId = "java-device-client-e2e-test-multiplexing".concat(i + "-" + uuid);

                devicesList[i] = Device.createFromId(deviceId, null, null);
                Tools.addDeviceWithRetry(registryManager, devicesList[i]);
                clientConnectionStringArrayList[i] = registryManager.getDeviceConnectionString(devicesList[i]);
            }

            this.clientArrayList = new ArrayList<>();

            this.transportClient = new TransportClient(this.protocol);
            for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
            {
                this.clientArrayList.add(new DeviceClient(this.clientConnectionStringArrayList[i], transportClient));
            }

            Thread.sleep(REGISTRY_MANAGER_DEVICE_CREATION_DELAY_MILLISECONDS);
        }

        public void dispose()
        {
            try
            {
                for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
                {
                    registryManager.removeDevice(devicesList[i]);
                }
            }
            catch (Exception e)
            {
                //ignore the exception, don't care if tear down wasn't successful for this test
            }
        }
    }

    @AfterClass
    public static void tearDown() throws IOException, IotHubException, InterruptedException
    {
        if (registryManager != null)
        {
            registryManager.close();

            registryManager = null;
        }

        if (serviceClient != null)
        {
            serviceClient.close();
        }
    }

    @Before
    public void setupTestInstance() throws InterruptedException, IotHubException, URISyntaxException, IOException
    {
        testInstance.setup();
    }

    @After
    public void tearDownTest()
    {
        testInstance.dispose();
    }

    @Test
    public void sendMessages() throws URISyntaxException, IOException, InterruptedException
    {
        testInstance.transportClient.open();

        for (int i = 0; i < testInstance.clientArrayList.size(); i++)
        {
            sendMessagesMultiplex(testInstance.clientArrayList.get(i), IotHubClientProtocol.AMQPS, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
        }

        testInstance.transportClient.closeNow();
    }

    @Test
    @StandardTierHubOnlyTest
    public void receiveMessagesIncludingProperties() throws Exception
    {
        testInstance.transportClient.open();

        for (int i = 0; i < testInstance.clientArrayList.size(); i++)
        {
            Success messageReceived = new Success();
            com.microsoft.azure.sdk.iot.device.MessageCallback callback = new MessageCallback(testInstance.messageProperties);
            ((DeviceClient)testInstance.clientArrayList.get(i)).setMessageCallback(callback, messageReceived);

            sendMessageToDevice(testInstance.devicesList[i].getDeviceId(), "AMQPS");
            waitForMessageToBeReceived(messageReceived, "AMQPS", testInstance.clientArrayList.get(i));

            Thread.sleep(200);
        }

        testInstance.transportClient.closeNow();
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesMultithreaded() throws InterruptedException, IOException
    {
        testInstance.transportClient.open();
        Thread[] threads = new Thread[testInstance.clientArrayList.size()];

        for (int i = 0; i < testInstance.clientArrayList.size(); i++)
        {
            threads[i] = new Thread(new MultiplexRunnable(testInstance.devicesList[i], testInstance.clientArrayList.get(i), testInstance.succeed));
            threads[i].start();
        }

        for (int i = 0; i < testInstance.clientArrayList.size(); i++)
        {
            threads[i].join(SEND_TIMEOUT_MILLISECONDS);
        }

        if(!testInstance.succeed.get())
        {
            Assert.fail(CorrelationDetailsLoggingAssert.buildExceptionMessage("Sending message over AMQP protocol in parallel failed", testInstance.clientArrayList));
        }

        testInstance.transportClient.closeNow();
    }

    @Test
    @ContinuousIntegrationTest
    public void uploadToBlobAsyncSingleFileAndTelemetry() throws Exception
    {
        // arrange
        setUpFileUploadState();

        testInstance.transportClient.open();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // act
        for (int j = 0; j < testInstance.clientArrayList.size(); j++)
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
                            ((DeviceClient)testInstance.clientArrayList.get(indexJ)).uploadToBlobAsync(testInstance.fileUploadState[indexI].blobName, testInstance.fileUploadState[indexI].fileInputStream, testInstance.fileUploadState[indexI].fileLength, new FileUploadCallback(), testInstance.fileUploadState[indexI]);
                        }
                        catch (IOException e)
                        {
                            Assert.fail(buildExceptionMessage(e.getMessage(), testInstance.clientArrayList.get(indexJ)));
                        }
                    }
                });

                executor.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        testInstance.clientArrayList.get(indexJ).sendEventAsync(new com.microsoft.azure.sdk.iot.device.Message(testInstance.messageStates[indexI].messageBody), new FileUploadCallback(), testInstance.messageStates[indexI]);
                    }
                });

                // assert
                FileUploadNotification fileUploadNotification = testInstance.fileUploadNotificationReceiver.receive(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
                Assert.assertNotNull(buildExceptionMessage("file upload notification was null", testInstance.clientArrayList.get(indexJ)), fileUploadNotification);
                verifyNotification(fileUploadNotification, testInstance.fileUploadState[i], testInstance.clientArrayList.get(indexJ));
                Assert.assertTrue(buildExceptionMessage("File upload callback was not triggered for file upload attempt " + i, testInstance.clientArrayList.get(indexJ)), testInstance.fileUploadState[i].isCallBackTriggered);
                Assert.assertEquals(buildExceptionMessage("File upload status was not successful on attempt " + i + ", status was: " + testInstance.fileUploadState[i].fileUploadStatus, testInstance.clientArrayList.get(indexJ)), testInstance.fileUploadState[i].fileUploadStatus, SUCCESS);
                Assert.assertEquals(buildExceptionMessage("Message status was not successful on attempt " + i + ", status was: " + testInstance.messageStates[i].messageStatus, testInstance.clientArrayList.get(indexJ)), testInstance.messageStates[i].messageStatus, SUCCESS);
            }
        }

        executor.shutdown();
        if (!executor.awaitTermination(MULTITHREADED_WAIT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("File" + i + " has no notification", testInstance.clientArrayList), testInstance.fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
        }

        testInstance.transportClient.closeNow();
        tearDownFileUploadState();
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodSucceed() throws Exception
    {
        testInstance.transportClient.open();

        for (int i = 0; i < testInstance.clientArrayList.size(); i++)
        {
            DeviceMethodStatusCallBack subscribedCallback = new DeviceMethodStatusCallBack();

            ((DeviceClient)testInstance.clientArrayList.get(i)).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, subscribedCallback, null);

            long startTime = System.currentTimeMillis();
            while (!subscribedCallback.isSubscribed)
            {
                Thread.sleep(200);

                if (System.currentTimeMillis() - startTime > METHOD_SUBSCRIBE_TIMEOUT_MILLISECONDS)
                {
                    fail(buildExceptionMessage("Timed out waiting for device to subscribe to methods", testInstance.clientArrayList.get(i)));
                }
            }

            CountDownLatch countDownLatch = new CountDownLatch(1);
            RunnableInvoke runnableInvoke = new RunnableInvoke(methodServiceClient, testInstance.devicesList[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            countDownLatch.await(3, TimeUnit.MINUTES);

            MethodResult result = runnableInvoke.getResult();
            Assert.assertNotNull(buildExceptionMessage(runnableInvoke.getException() == null ? "Runnable returns null without exception information" : runnableInvoke.getException().getMessage(), testInstance.clientArrayList.get(i)), result);
            Assert.assertEquals(buildExceptionMessage("result was not success, but was: " + result.getStatus(), testInstance.clientArrayList.get(i)), (long)METHOD_SUCCESS,(long)result.getStatus());
            Assert.assertEquals(buildExceptionMessage("Received unexpected payload", testInstance.clientArrayList.get(i)), runnableInvoke.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        testInstance.transportClient.closeNow();
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void invokeMethodInvokeParallelSucceed() throws Exception
    {
        testInstance.transportClient.open();

        for (int i = 0; i < testInstance.clientArrayList.size(); i++)
        {
            DeviceMethodStatusCallBack subscribedCallback = new DeviceMethodStatusCallBack();

            ((DeviceClient)testInstance.clientArrayList.get(i)).subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, subscribedCallback, null);

            long startTime = System.currentTimeMillis();
            while (!subscribedCallback.isSubscribed)
            {
                Thread.sleep(200);

                if (System.currentTimeMillis() - startTime > METHOD_SUBSCRIBE_TIMEOUT_MILLISECONDS)
                {
                    fail(buildExceptionMessage("Timed out waiting for device to subscribe to methods", testInstance.clientArrayList.get(i)));
                }
            }
        }

        List<RunnableInvoke> runs = new LinkedList<>();
        CountDownLatch countDownLatch = new CountDownLatch(MAX_DEVICE_MULTIPLEX);
        for (int i = 0; i < testInstance.clientArrayList.size(); i++)
        {
            RunnableInvoke runnableInvoke = new RunnableInvoke(methodServiceClient, testInstance.devicesList[i].getDeviceId(), METHOD_NAME, METHOD_PAYLOAD, countDownLatch);
            new Thread(runnableInvoke).start();
            runs.add(runnableInvoke);
        }
        countDownLatch.await(3, TimeUnit.MINUTES);

        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_PER_CALL_MILLISECONDS * MAX_DEVICE_MULTIPLEX);

        for (RunnableInvoke runnableInvoke:runs)
        {
            MethodResult result = runnableInvoke.getResult();
            Assert.assertNotNull(CorrelationDetailsLoggingAssert.buildExceptionMessage(runnableInvoke.getException() == null ? "Runnable returns null without exception information" : runnableInvoke.getException().getMessage(), testInstance.clientArrayList), result);
            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("result was not success, but was: " + result.getStatus(), testInstance.clientArrayList), (long)METHOD_SUCCESS,(long)result.getStatus());
            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessage("Received unexpected payload", testInstance.clientArrayList), runnableInvoke.getExpectedPayload(), METHOD_NAME + ":" + result.getPayload().toString());
        }

        testInstance.transportClient.closeNow();
    }

    @Test
    @StandardTierHubOnlyTest
    public void testTwin() throws IOException, InterruptedException, IotHubException, URISyntaxException
    {
        testInstance.transportClient = setUpTwin();

        ExecutorService executor = Executors.newFixedThreadPool(MAX_PROPERTIES_TO_TEST);

        //Testing subscribing to desired properties.
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.devicesUnderTest[i].dCDeviceForTwin.getDesiredProp().clear();
            for (int j = 0; j < MAX_PROPERTIES_TO_TEST; j++)
            {
                PropertyState propertyState = new PropertyState();
                propertyState.callBackTriggered = false;
                propertyState.property = new Property(PROPERTY_KEY + j, PROPERTY_VALUE);
                testInstance.devicesUnderTest[i].dCDeviceForTwin.propertyStateList.add(propertyState);
                testInstance.devicesUnderTest[i].dCDeviceForTwin.setDesiredPropertyCallback(propertyState.property, testInstance.devicesUnderTest[i].dCDeviceForTwin, propertyState);
            }

            // act
            testInstance.devicesUnderTest[i].deviceClient.subscribeToDesiredProperties(testInstance.devicesUnderTest[i].dCDeviceForTwin.getDesiredProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION_MILLISECONDS);

            Set<Pair> desiredProperties = new HashSet<>();
            for (int j = 0; j < MAX_PROPERTIES_TO_TEST; j++)
            {
                desiredProperties.add(new Pair(PROPERTY_KEY + j, PROPERTY_VALUE_UPDATE + UUID.randomUUID()));
            }
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);
            testInstance.deviceTwinClient.updateTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION_MILLISECONDS);

            // assert
            Assert.assertEquals(buildExceptionMessage("Device twin status expected to be SUCCESS, but was: " + testInstance.devicesUnderTest[i].deviceTwinStatus, testInstance.devicesUnderTest[i].deviceClient), SUCCESS, testInstance.devicesUnderTest[i].deviceTwinStatus);
            for (PropertyState propertyState : testInstance.devicesUnderTest[i].dCDeviceForTwin.propertyStateList)
            {
                Assert.assertTrue(buildExceptionMessage("One or more property callbacks were not triggered", testInstance.devicesUnderTest[i].deviceClient), propertyState.callBackTriggered);
                Assert.assertTrue(buildExceptionMessage("Property new value did not start with " + PROPERTY_VALUE_UPDATE, testInstance.devicesUnderTest[i].deviceClient), ((String) propertyState.propertyNewValue).startsWith(PROPERTY_VALUE_UPDATE));
                Assert.assertEquals(buildExceptionMessage("Expected SUCCESS but device twin status was " + testInstance.devicesUnderTest[i].deviceTwinStatus, testInstance.devicesUnderTest[i].deviceClient), SUCCESS, testInstance.devicesUnderTest[i].deviceTwinStatus);
            }
        }

        //Testing updating reported properties
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            // act
            // send max_prop RP all at once
            testInstance.devicesUnderTest[i].dCDeviceForTwin.createNewReportedProperties(MAX_PROPERTIES_TO_TEST);
            testInstance.devicesUnderTest[i].deviceClient.sendReportedProperties(testInstance.devicesUnderTest[i].dCDeviceForTwin.getReportedProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION_MILLISECONDS);

            // act
            // Update RP
            testInstance.devicesUnderTest[i].dCDeviceForTwin.updateAllExistingReportedProperties();
            testInstance.devicesUnderTest[i].deviceClient.sendReportedProperties(testInstance.devicesUnderTest[i].dCDeviceForTwin.getReportedProp());
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION_MILLISECONDS);

            // assert
            Assert.assertEquals(buildExceptionMessage("Expected status SUCCESS but was " + testInstance.devicesUnderTest[i].deviceTwinStatus, testInstance.devicesUnderTest[i].deviceClient), SUCCESS, testInstance.devicesUnderTest[i].deviceTwinStatus);

            // verify if they are received by SC
            Thread.sleep(MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS_MILLISECONDS);
            int actualReportedPropFound = readReportedProperties(testInstance.devicesUnderTest[i], PROPERTY_KEY, PROPERTY_VALUE_UPDATE);
            Assert.assertEquals(buildExceptionMessage("Missing reported properties on the " + (i+1) + " device out of " + MAX_DEVICE_MULTIPLEX,testInstance.devicesUnderTest[i].deviceClient), MAX_PROPERTIES_TO_TEST.intValue(), actualReportedPropFound);
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
                        testInstance.devicesUnderTest[finalI].dCDeviceForTwin.createNewReportedProperties(1);
                        testInstance.devicesUnderTest[finalI].deviceClient.sendReportedProperties(testInstance.devicesUnderTest[finalI].dCDeviceForTwin.getReportedProp());
                    }
                    catch (IOException e)
                    {
                        Assert.fail(buildExceptionMessage(e.getMessage(), testInstance.devicesUnderTest[finalI].deviceClient));
                    }
                    Assert.assertEquals(buildExceptionMessage("Expected SUCCESS but was " + testInstance.devicesUnderTest[finalI].deviceTwinStatus, testInstance.devicesUnderTest[finalI].deviceClient), SUCCESS, testInstance.devicesUnderTest[finalI].deviceTwinStatus);

                    // testUpdateReportedPropertiesMultiThreaded
                    try
                    {
                        testInstance.devicesUnderTest[finalI].dCDeviceForTwin.updateExistingReportedProperty(finalI);
                        testInstance.devicesUnderTest[finalI].deviceClient.sendReportedProperties(testInstance.devicesUnderTest[finalI].dCDeviceForTwin.getReportedProp());
                    }
                    catch (IOException e)
                    {
                        Assert.fail(buildExceptionMessage(e.getMessage(), testInstance.devicesUnderTest[finalI].deviceClient));
                    }
                    Assert.assertEquals(buildExceptionMessage("Expected SUCCESS but was " + testInstance.devicesUnderTest[finalI].deviceTwinStatus, testInstance.devicesUnderTest[finalI].deviceClient), SUCCESS, testInstance.devicesUnderTest[finalI].deviceTwinStatus);
                }
            });
        }
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION_MILLISECONDS);
        executor.shutdown();
        if (!executor.awaitTermination(MULTITHREADED_WAIT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)) //4 minutes
        {
            executor.shutdownNow();
            fail(buildExceptionMessage("Test threads did not finish before timeout", testInstance.devicesUnderTest[0].deviceClient));
        }

        tearDownTwin(testInstance.transportClient);
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
        Map messageProperties;

        public MessageCallback(Map messageProperties)
        {
            this.messageProperties = messageProperties;
        }

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
        serviceMessage.setProperties(testInstance.messageProperties);
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

                if (System.currentTimeMillis() - startTime > RECEIVE_MESSAGE_TIMEOUT_MILLISECONDS)
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
        public boolean isSubscribed = false;

        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("Device Client: IoT Hub responded to device method operation with status " + status.name());
            isSubscribed = status == OK_EMPTY || status == OK;
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
        private AtomicBoolean success;

        @Override
        public void run()
        {
            try
            {
                this.sendMessages();
            }
            catch (Exception e)
            {
                this.success.lazySet(false);
            }
        }

        private MultiplexRunnable(Device deviceAmqps, InternalClient client, AtomicBoolean success)
        {
            this.client = client;
            this.success = success;

            messageString = "Java client " + deviceAmqps.getDeviceId() + " test e2e message over AMQP protocol";
        }

        private void sendMessages() throws Exception
        {
            for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; ++i)
            {
                Message msgSend = new Message(messageString);
                msgSend.setProperty("messageCount", Integer.toString(i));
                for (int j = 0; j < NUM_KEYS_PER_MESSAGE; j++)
                {
                    msgSend.setProperty("key"+j, "value"+j);
                }

                Success messageSent = new Success();
                EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
                client.sendEventAsync(msgSend, callback, messageSent);

                long startTime = System.currentTimeMillis();
                while (!messageSent.wasCallbackFired())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if ((System.currentTimeMillis() - startTime) > SEND_TIMEOUT_MILLISECONDS)
                    {
                        fail(buildExceptionMessage("Timed out waiting for OK_EMPTY response for sent message", client));
                    }
                }

                if (messageSent.getCallbackStatusCode() != IotHubStatusCode.OK_EMPTY)
                {
                    fail(buildExceptionMessage("Unexpected iot hub status code! Expected OK_EMPTY but got " + messageSent.getCallbackStatusCode(), client));
                }
            }

            this.success.lazySet(true);
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
            for (int i = 0; i < MAX_DEVICES; i++)
            {
                DeviceState deviceState = new DeviceState();
                deviceState.sCDeviceForRegistryManager = testInstance.devicesList[i];
                deviceState.connectionString = registryManager.getDeviceConnectionString(deviceState.sCDeviceForRegistryManager);
                testInstance.devicesUnderTest[i] = deviceState;

                Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION_MILLISECONDS);
            }

            transportClient = new TransportClient(IotHubClientProtocol.AMQPS);

            testInstance.clientArrayList = new ArrayList<>();
            for (int i = 0; i < MAX_DEVICES; i++)
            {
                DeviceState deviceState = testInstance.devicesUnderTest[i];
                deviceState.deviceClient = new DeviceClient(deviceState.connectionString, transportClient);
                testInstance.devicesUnderTest[i].dCDeviceForTwin = new DeviceExtension();
                testInstance.clientArrayList.add(deviceState.deviceClient);
            }

            transportClient.open();

            for (int i = 0; i < MAX_DEVICES; i++)
            {
                testInstance.devicesUnderTest[i].deviceClient.startDeviceTwin(new DeviceTwinStatusCallBack(), testInstance.devicesUnderTest[i], testInstance.devicesUnderTest[i].dCDeviceForTwin, testInstance.devicesUnderTest[i]);
                testInstance.devicesUnderTest[i].deviceTwinStatus = SUCCESS;
                testInstance.devicesUnderTest[i].sCDeviceForTwin = new DeviceTwinDevice(testInstance.devicesUnderTest[i].sCDeviceForRegistryManager.getDeviceId());
                testInstance.deviceTwinClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
                Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION_MILLISECONDS);
            }
        }
        catch (Exception e)
        {
            TestCase.fail("Encountered exception during setUpTwin: " + Tools.getStackTraceFromThrowable(e));
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
            // No need to fail the test just because cleanup failed.
            e.printStackTrace();
        }
    }

    private void setUpFileUploadState() throws Exception
    {
        // Start receiver for a test
        testInstance.fileUploadNotificationReceiver.open();
        testInstance.fileUploadNotificationReceiver.receive(MAX_TIMEOUT_FLUSH_NOTIFICATIONS_MILLISECONDS);
        testInstance.fileUploadNotificationReceiver.close();
        testInstance.fileUploadNotificationReceiver.open();

        testInstance.fileUploadState = new FileUploadState[MAX_FILES_TO_UPLOAD];
        testInstance.messageStates = new MessageState[MAX_FILES_TO_UPLOAD];
        for (int i = 0; i < MAX_FILES_TO_UPLOAD; i++)
        {
            byte[] buf = new byte[i];
            new Random().nextBytes(buf);
            testInstance.fileUploadState[i] = new FileUploadState();
            testInstance.fileUploadState[i].blobName = REMOTE_FILE_NAME + i + REMOTE_FILE_NAME_EXT;
            testInstance.fileUploadState[i].fileInputStream = new ByteArrayInputStream(buf);
            testInstance.fileUploadState[i].fileLength = buf.length;
            testInstance.fileUploadState[i].fileUploadStatus = SUCCESS;
            testInstance.fileUploadState[i].fileUploadNotificationReceived = FAILURE;
            testInstance.fileUploadState[i].isCallBackTriggered = false;

            testInstance.messageStates[i] = new MessageState();
            testInstance.messageStates[i].messageBody = new String(buf);
            testInstance.messageStates[i].messageStatus = SUCCESS;
        }
    }

    private void tearDownFileUploadState()
    {
        testInstance.fileUploadState = null;
        testInstance.messageStates = null;
    }

    private int readReportedProperties(DeviceState deviceState, String startsWithKey, String startsWithValue) throws IOException , IotHubException, InterruptedException
    {
        int totalCount = 0;
        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_TWIN_OPERATION_MILLISECONDS);
        testInstance.deviceTwinClient.getTwin(deviceState.sCDeviceForTwin);
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
