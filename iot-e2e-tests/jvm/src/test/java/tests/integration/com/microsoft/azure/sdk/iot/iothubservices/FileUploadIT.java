/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.iothubservices.IotHubServicesCommon;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import org.junit.*;
import tests.integration.com.microsoft.azure.sdk.iot.DeviceConnectionString;
import tests.integration.com.microsoft.azure.sdk.iot.MethodNameLoggingIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static org.junit.Assert.*;
import static tests.integration.com.microsoft.azure.sdk.iot.iothubservices.FileUploadIT.STATUS.FAILURE;
import static tests.integration.com.microsoft.azure.sdk.iot.iothubservices.FileUploadIT.STATUS.SUCCESS;

public class FileUploadIT extends MethodNameLoggingIntegrationTest
{
    // Max time to wait to see it on Hub
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 10000; // 10 sec

    //Max time to wait before timing out test
    private static final long MAX_MILLISECS_TIMEOUT_KILL_TEST = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB + 50000; // 50 secs

    // Max time to flush iothub notification from previous failing tests
    private static final long MAX_MILLISECS_TIMEOUT_FLUSH_NOTIFICATION = 15000; // 15 secs

    //Max devices to test
    private static final Integer MAX_FILES_TO_UPLOAD = 5;

    // remote name of the file
    private static final String REMOTE_FILE_NAME = "File";
    private static final String REMOTE_FILE_NAME_EXT = ".txt";

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";

    // States of SDK
    private static RegistryManager registryManager;
    private static DeviceClient deviceClient;
    private static ServiceClient serviceClient;
    private static Device scDevice;
    private static FileUploadState[] fileUploadState;
    private static MessageState[] messageStates;
    private static FileUploadNotificationReceiver fileUploadNotificationReceiver;

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

    @BeforeClass
    public static void setUp() throws IOException, NoSuchAlgorithmException, IotHubException, InterruptedException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();
        assertNotNull(fileUploadNotificationReceiver);
    }

    @Before
    public void setUpFileUploadState() throws Exception
    {
        String deviceId = "java-file-upload-e2e-test".concat(UUID.randomUUID().toString());
        scDevice = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceId, null, null);
        scDevice = registryManager.addDevice(scDevice);

        // flush pending notifications before every test to prevent random test failures
        // because of notifications received from other failed test
        fileUploadNotificationReceiver.open();
        fileUploadNotificationReceiver.receive(MAX_MILLISECS_TIMEOUT_FLUSH_NOTIFICATION);
        fileUploadNotificationReceiver.close();

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

    @After
    public void tearDownFileUploadState() throws Exception
    {
        try
        {
            registryManager.removeDevice(scDevice);
        }
        catch (IotHubNotFoundException | IOException e)
        {
            //device was likely never added. Can ignore
        }

        fileUploadState = null;
        messageStates = null;
    }

    @AfterClass
    public static void tearDown() throws IotHubException, IOException, InterruptedException
    {
        // flush all the notifications caused by this test suite to avoid failures running on different test suite attempt
        assertNotNull(fileUploadNotificationReceiver);
        fileUploadNotificationReceiver.open();
        fileUploadNotificationReceiver.receive(MAX_MILLISECS_TIMEOUT_FLUSH_NOTIFICATION);
        fileUploadNotificationReceiver.close();

        if (registryManager != null)
        {
            registryManager.close();
            registryManager = null;
        }

        serviceClient = null;
        deviceClient = null;
    }

    private void setUpDeviceClient(IotHubClientProtocol protocol) throws URISyntaxException
    {
        deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, scDevice), protocol);
        IotHubServicesCommon.openClientWithRetry(deviceClient);
    }

    private void tearDownDeviceClient() throws IOException
    {
        deviceClient.closeNow();
        deviceClient = null;
    }

    private void verifyNotification(FileUploadNotification fileUploadNotification, FileUploadState fileUploadState) throws IOException
    {
        assertTrue("File upload notification blob size not equal to expected file length", fileUploadNotification.getBlobSizeInBytes() == fileUploadState.fileLength);

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

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileZeroLength() throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        setUpDeviceClient(IotHubClientProtocol.MQTT);

        // act
        deviceClient.uploadToBlobAsync(fileUploadState[0].blobName, fileUploadState[0].fileInputStream, fileUploadState[0].fileLength, new FileUploadCallback(), fileUploadState[0]);

        FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice();

        // assert
        verifyNotification(fileUploadNotification, fileUploadState[0]);
        assertTrue(fileUploadState[0].isCallBackTriggered);
        assertEquals(fileUploadState[0].fileUploadStatus, SUCCESS);
        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFile() throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        setUpDeviceClient(IotHubClientProtocol.MQTT);

        // act
        deviceClient.uploadToBlobAsync(fileUploadState[MAX_FILES_TO_UPLOAD - 1].blobName, fileUploadState[MAX_FILES_TO_UPLOAD - 1].fileInputStream, fileUploadState[MAX_FILES_TO_UPLOAD - 1].fileLength, new FileUploadCallback(), fileUploadState[MAX_FILES_TO_UPLOAD - 1]);
        FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice();

        // assert
        assertNotNull(fileUploadNotification);
        verifyNotification(fileUploadNotification, fileUploadState[MAX_FILES_TO_UPLOAD - 1]);
        assertTrue(fileUploadState[MAX_FILES_TO_UPLOAD - 1].isCallBackTriggered);
        assertEquals(fileUploadState[MAX_FILES_TO_UPLOAD - 1].fileUploadStatus, SUCCESS);

        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncMultipleFilesSequentially() throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        setUpDeviceClient(IotHubClientProtocol.MQTT);

        // act
        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            deviceClient.uploadToBlobAsync(fileUploadState[i].blobName, fileUploadState[i].fileInputStream, fileUploadState[i].fileLength, new FileUploadCallback(), fileUploadState[i]);
            FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice();

            // assert
            verifyNotification(fileUploadNotification, fileUploadState[i]);
            assertTrue(fileUploadState[i].isCallBackTriggered);
            assertEquals(fileUploadState[i].fileUploadStatus, SUCCESS);
        }

        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            assertEquals("File" + i + " has no notification", fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
        }

        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncMultipleFilesParallel() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        // arrange
        setUpDeviceClient(IotHubClientProtocol.MQTT);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // act
        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            final int index = i;
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        deviceClient.uploadToBlobAsync(fileUploadState[index].blobName, fileUploadState[index].fileInputStream, fileUploadState[index].fileLength, new FileUploadCallback(), fileUploadState[index]);
                    }
                    catch (IOException e)
                    {
                        fail(e.getMessage());
                    }
                }
            });

            FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice();

            // assert
            verifyNotification(fileUploadNotification, fileUploadState[i]);
            assertTrue(fileUploadState[i].isCallBackTriggered);
            assertEquals(fileUploadState[i].fileUploadStatus, SUCCESS);
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

        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncAndTelemetryOnMQTT() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        // arrange
        setUpDeviceClient(IotHubClientProtocol.MQTT);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // act
        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            final int index = i;
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        deviceClient.uploadToBlobAsync(fileUploadState[index].blobName, fileUploadState[index].fileInputStream, fileUploadState[index].fileLength, new FileUploadCallback(), fileUploadState[index]);
                    }
                    catch (IOException e)
                    {
                        fail(e.getMessage());
                    }
                }
            });

            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    deviceClient.sendEventAsync(new com.microsoft.azure.sdk.iot.device.Message(messageStates[index].messageBody), new FileUploadCallback(), messageStates[index]);
                }
            });

            FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice();

            // assert
            verifyNotification(fileUploadNotification, fileUploadState[i]);

            assertTrue(fileUploadState[i].isCallBackTriggered);
            assertEquals(fileUploadState[i].fileUploadStatus, SUCCESS);
            assertEquals(messageStates[i].messageStatus, SUCCESS);
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

        tearDownDeviceClient();
    }

    private FileUploadNotification getFileUploadNotificationForThisDevice() throws IOException, InterruptedException
    {
        FileUploadNotification fileUploadNotification;
        do
        {
            fileUploadNotification = fileUploadNotificationReceiver.receive(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
            assertNotNull(fileUploadNotification);

            //ignore any file upload notifications received that are not about this device
        } while (!fileUploadNotification.getDeviceId().equals(scDevice.getDeviceId()));

        return fileUploadNotification;
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileAndTelemetryOnAMQP() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        // arrange
        setUpDeviceClient(IotHubClientProtocol.AMQPS);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // act
        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            final int index = i;
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        deviceClient.uploadToBlobAsync(fileUploadState[index].blobName, fileUploadState[index].fileInputStream, fileUploadState[index].fileLength, new FileUploadCallback(), fileUploadState[index]);
                    }
                    catch (IOException e)
                    {
                        fail(e.getMessage());
                    }
                }
            });

            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    deviceClient.sendEventAsync(new com.microsoft.azure.sdk.iot.device.Message(messageStates[index].messageBody), new FileUploadCallback(), messageStates[index]);
                }
            });

            FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice();

            // assert
            verifyNotification(fileUploadNotification, fileUploadState[i]);
            assertTrue(fileUploadState[i].isCallBackTriggered);
            assertEquals(fileUploadState[i].fileUploadStatus, SUCCESS);
            assertEquals(messageStates[i].messageStatus, SUCCESS);
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

        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileAndTelemetryOnAMQPWS() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        // arrange
        setUpDeviceClient(IotHubClientProtocol.AMQPS_WS);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // act
        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            final int index = i;
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        deviceClient.uploadToBlobAsync(fileUploadState[index].blobName, fileUploadState[index].fileInputStream, fileUploadState[index].fileLength, new FileUploadCallback(), fileUploadState[index]);
                    }
                    catch (IOException e)
                    {
                        fail(e.getMessage());
                    }
                }
            });

            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    deviceClient.sendEventAsync(new com.microsoft.azure.sdk.iot.device.Message(messageStates[index].messageBody), new FileUploadCallback(), messageStates[index]);
                }
            });

            FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice();

            // assert
            verifyNotification(fileUploadNotification, fileUploadState[i]);
            assertTrue(fileUploadState[i].isCallBackTriggered);
            assertEquals(fileUploadState[i].fileUploadStatus, SUCCESS);
            assertEquals(messageStates[i].messageStatus, SUCCESS);
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

        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileAndTelemetryOnHttp() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        // arrange
        setUpDeviceClient(IotHubClientProtocol.HTTPS);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // act
        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            final int index = i;
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        deviceClient.uploadToBlobAsync(fileUploadState[index].blobName, fileUploadState[index].fileInputStream, fileUploadState[index].fileLength, new FileUploadCallback(), fileUploadState[index]);
                    }
                    catch (IOException e)
                    {
                        fail(e.getMessage());
                    }
                }
            });

            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    deviceClient.sendEventAsync(new com.microsoft.azure.sdk.iot.device.Message(messageStates[index].messageBody), new FileUploadCallback(), messageStates[index]);
                }
            });

            FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice();

            // assert
            verifyNotification(fileUploadNotification, fileUploadState[i]);
            assertTrue(fileUploadState[i].isCallBackTriggered);
            assertEquals(fileUploadState[i].fileUploadStatus, SUCCESS);
            assertEquals(messageStates[i].messageStatus, SUCCESS);
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

        tearDownDeviceClient();
    }
}
