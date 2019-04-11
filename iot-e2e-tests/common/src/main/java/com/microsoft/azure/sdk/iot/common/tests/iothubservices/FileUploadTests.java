/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static com.microsoft.azure.sdk.iot.common.tests.iothubservices.FileUploadTests.STATUS.FAILURE;
import static com.microsoft.azure.sdk.iot.common.tests.iothubservices.FileUploadTests.STATUS.SUCCESS;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static org.junit.Assert.*;

/**
 * Test class containing all tests to be run on JVM and android pertaining to FileUpload. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class FileUploadTests extends IntegrationTest
{
    // Max time to wait to see it on Hub
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 20000; // 20 sec
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_CALLBACK = 5000; // 5 sec

    //Max time to wait before timing out test
    private static final long MAX_MILLISECS_TIMEOUT_KILL_TEST = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB + 50000; // 50 secs

    // Max time to flush iothub notification from previous failing tests
    private static final long MAX_MILLISECS_TIMEOUT_FLUSH_NOTIFICATION = 15000; // 15 secs

    //Max devices to test
    private static final Integer MAX_FILES_TO_UPLOAD = 5;

    // remote name of the file
    private static final String REMOTE_FILE_NAME = "File";
    private static final String REMOTE_FILE_NAME_EXT = ".txt";

    protected static String iotHubConnectionString = "";

    // States of SDK
    private static RegistryManager registryManager;
    private static DeviceClient deviceClient;
    private static ServiceClient serviceClient;
    private static Device scDevice;
    private static Device scDevicex509;
    private static FileUploadState[] fileUploadState;
    private static MessageState[] messageStates;
    private static FileUploadNotificationReceiver fileUploadNotificationReceiver;

    private static String publicKeyCertificate;
    private static String privateKeyCertificate;
    private static String x509Thumbprint;

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

    public static void setUp() throws IOException
    {
        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        setUp(certificateGenerator.getPublicCertificate(), certificateGenerator.getPrivateKey(), certificateGenerator.getX509Thumbprint());
    }

    public static void setUp(String publicK, String privateK, String thumbprint) throws IOException
    {
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);

        publicKeyCertificate = publicK;
        privateKeyCertificate = privateK;
        x509Thumbprint = thumbprint;
        
        fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();
        assertNotNull(fileUploadNotificationReceiver);
    }

    @Before
    public void setUpFileUploadState() throws Exception
    {
        String deviceId = "java-file-upload-e2e-test".concat(UUID.randomUUID().toString());
        scDevice = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceId, null, null);
        scDevice = Tools.addDeviceWithRetry(registryManager, scDevice);

        String deviceIdX509 = "java-file-upload-e2e-test-x509".concat(UUID.randomUUID().toString());
        scDevicex509 = com.microsoft.azure.sdk.iot.service.Device.createDevice(deviceIdX509, AuthenticationType.SELF_SIGNED);
        scDevicex509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
        scDevicex509 = Tools.addDeviceWithRetry(registryManager, scDevicex509);

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
            registryManager.removeDevice(scDevicex509);
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
        assertNotNull("file upload notification receiver was not null", fileUploadNotificationReceiver);
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

    private void setUpDeviceClient(IotHubClientProtocol protocol) throws URISyntaxException, InterruptedException
    {
        deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, scDevice), protocol);
        IotHubServicesCommon.openClientWithRetry(deviceClient);
    }

    private void setUpX509DeviceClient(IotHubClientProtocol protocol) throws URISyntaxException, InterruptedException
    {
        deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, scDevicex509), protocol, publicKeyCertificate, false, privateKeyCertificate, false);
        IotHubServicesCommon.openClientWithRetry(deviceClient);
    }

    private void tearDownDeviceClient() throws IOException
    {
        deviceClient.closeNow();
        deviceClient = null;
    }

    private void verifyNotification(FileUploadNotification fileUploadNotification, FileUploadState fileUploadState) throws IOException
    {
        assertTrue(buildExceptionMessage("File upload notification blob size not equal to expected file length", deviceClient), fileUploadNotification.getBlobSizeInBytes() == fileUploadState.fileLength);

        URL u = new URL(fileUploadNotification.getBlobUri());
        try (InputStream inputStream = u.openStream())
        {
            byte[] testBuf = new byte[(int)fileUploadState.fileLength];
            int testLen = inputStream.read(testBuf,  0, (int)fileUploadState.fileLength);
            byte[] actualBuf = new byte[(int)fileUploadState.fileLength];
            fileUploadState.fileInputStream.reset();
            int actualLen = (fileUploadState.fileLength == 0) ? (int) fileUploadState.fileLength : fileUploadState.fileInputStream.read(actualBuf, 0, (int) fileUploadState.fileLength);
            assertEquals(buildExceptionMessage("Expected length " + testLen + " but was " + actualLen, deviceClient), testLen, actualLen);
            assertTrue(buildExceptionMessage("testBuf was different from actualBuf", deviceClient), Arrays.equals(testBuf, actualBuf));
        }

        assertTrue(buildExceptionMessage("File upload notification did not contain the expected blob name", deviceClient), fileUploadNotification.getBlobName().contains(fileUploadState.blobName));
        fileUploadState.fileUploadNotificationReceived = SUCCESS;
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileZeroLength() throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        setUpDeviceClient(IotHubClientProtocol.MQTT);

        // act
        deviceClient.uploadToBlobAsync(fileUploadState[0].blobName, fileUploadState[0].fileInputStream, fileUploadState[0].fileLength, new FileUploadCallback(), fileUploadState[0]);

        // assert
        if (!isBasicTierHub)
        {
            FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevice);
            verifyNotification(fileUploadNotification, fileUploadState[0]);
        }
        waitForFileUploadStatusCallbackTriggered(0);
        assertEquals(buildExceptionMessage("File upload status expected SUCCESS but was " + fileUploadState[0].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[0].fileUploadStatus);
        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFile() throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        setUpDeviceClient(IotHubClientProtocol.MQTT);

        // act
        deviceClient.uploadToBlobAsync(fileUploadState[MAX_FILES_TO_UPLOAD - 1].blobName, fileUploadState[MAX_FILES_TO_UPLOAD - 1].fileInputStream, fileUploadState[MAX_FILES_TO_UPLOAD - 1].fileLength, new FileUploadCallback(), fileUploadState[MAX_FILES_TO_UPLOAD - 1]);

        // assert
        if (!isBasicTierHub)
        {
            FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevice);
            assertNotNull(buildExceptionMessage("file upload notification was null", deviceClient), fileUploadNotification);
            verifyNotification(fileUploadNotification, fileUploadState[MAX_FILES_TO_UPLOAD - 1]);
        }
        waitForFileUploadStatusCallbackTriggered(MAX_FILES_TO_UPLOAD - 1);
        assertEquals(buildExceptionMessage("File upload status should be SUCCESS but was " + fileUploadState[MAX_FILES_TO_UPLOAD - 1].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[MAX_FILES_TO_UPLOAD - 1].fileUploadStatus);

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
            // assert
            if (!isBasicTierHub)
            {
                FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevice);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
            }
            waitForFileUploadStatusCallbackTriggered(i);
            assertEquals(buildExceptionMessage("Expected SUCCESS but file upload status " + i + " was " + fileUploadState[i].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[i].fileUploadStatus);
            assertEquals(buildExceptionMessage("Expected SUCCESS but message status " + i + " was " + messageStates[i].messageStatus, deviceClient), SUCCESS, messageStates[i].messageStatus);
        }

        if (!isBasicTierHub)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                assertEquals(buildExceptionMessage("File" + i + " has no notification", deviceClient), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
            }
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
                        fail(buildExceptionMessage("IOException occurred during upload: " + e.getMessage(), deviceClient));
                    }
                }
            });

            // assert
            if (!isBasicTierHub)
            {
                FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevice);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
            }
            waitForFileUploadStatusCallbackTriggered(i);
            assertEquals(buildExceptionMessage("Expected SUCCESS but file upload status " + i + " was " + fileUploadState[i].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[i].fileUploadStatus);
            assertEquals(buildExceptionMessage("Expected SUCCESS but message status " + i + " was " + messageStates[i].messageStatus, deviceClient), SUCCESS, messageStates[i].messageStatus);
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        if (!isBasicTierHub)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                assertEquals(buildExceptionMessage("File" + i + " has no notification", deviceClient), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
            }
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
                        fail(buildExceptionMessage("IOException occurred during upload: " + e.getMessage(), deviceClient));
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

            // assert
            if (!isBasicTierHub)
            {
                FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevice);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
            }

            waitForFileUploadStatusCallbackTriggered(i);
            assertEquals(buildExceptionMessage("Expected SUCCESS but file upload status " + i + " was " + fileUploadState[i].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[i].fileUploadStatus);
            assertEquals(buildExceptionMessage("Expected SUCCESS but message status " + i + " was " + messageStates[i].messageStatus, deviceClient), SUCCESS, messageStates[i].messageStatus);
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        if (!isBasicTierHub)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                assertEquals(buildExceptionMessage("File" + i + " has no notification", deviceClient), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
            }
        }
        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncAndTelemetryOnMQTTWS() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        // arrange
        setUpDeviceClient(IotHubClientProtocol.MQTT_WS);
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
                        fail(buildExceptionMessage("IOException occurred during upload: " + e.getMessage(), deviceClient));
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

            // assert
            if (!isBasicTierHub)
            {
                FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevice);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
            }

            waitForFileUploadStatusCallbackTriggered(i);
            assertEquals(buildExceptionMessage("Expected SUCCESS but file upload status " + i + " was " + fileUploadState[i].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[i].fileUploadStatus);
            assertEquals(buildExceptionMessage("Expected SUCCESS but message status " + i + " was " + messageStates[i].messageStatus, deviceClient), SUCCESS, messageStates[i].messageStatus);
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        if (!isBasicTierHub)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                assertEquals(buildExceptionMessage("File" + i + " has no notification", deviceClient), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
            }
        }

        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileOnAMQP() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
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
                        fail(buildExceptionMessage("IOException occurred during upload: " + e.getMessage(), deviceClient));
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

            Thread.sleep(3000);
            // assert
            if (!isBasicTierHub)
            {
                FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevice);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
            }

            waitForFileUploadStatusCallbackTriggered(i);
            assertEquals(buildExceptionMessage("Expected SUCCESS but file upload status " + i + " was " + fileUploadState[i].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[i].fileUploadStatus);
            assertEquals(buildExceptionMessage("Expected SUCCESS but message status " + i + " was " + messageStates[i].messageStatus, deviceClient), SUCCESS, messageStates[i].messageStatus);
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        if (!isBasicTierHub)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                assertEquals(buildExceptionMessage("File" + i + " has no notification", deviceClient), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
            }
        }

        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileOnAMQPWS() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
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
                        fail(buildExceptionMessage("IOException occurred during upload: " + e.getMessage(), deviceClient));
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

            // assert
            if (!isBasicTierHub)
            {
                FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevice);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
            }
            waitForFileUploadStatusCallbackTriggered(i);
            assertEquals(buildExceptionMessage("Expected SUCCESS but file upload status " + i + " was " + fileUploadState[i].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[i].fileUploadStatus);
            assertEquals(buildExceptionMessage("Expected SUCCESS but message status " + i + " was " + messageStates[i].messageStatus, deviceClient), SUCCESS, messageStates[i].messageStatus);
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        if (!isBasicTierHub)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                assertEquals(buildExceptionMessage("File" + i + " has no notification", deviceClient), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
            }
        }

        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileOnHttp() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
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
                        fail(buildExceptionMessage("IOException occurred during upload: " + e.getMessage(), deviceClient));
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

            // assert
            if (!isBasicTierHub)
            {
                FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevice);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
            }
            waitForFileUploadStatusCallbackTriggered(i);
            assertEquals(buildExceptionMessage("Expected SUCCESS but file upload status " + i + " was " + fileUploadState[i].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[i].fileUploadStatus);
            assertEquals(buildExceptionMessage("Expected SUCCESS but message status " + i + " was " + messageStates[i].messageStatus, deviceClient), SUCCESS, messageStates[i].messageStatus);
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        if (!isBasicTierHub)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                assertEquals(buildExceptionMessage("File" + i + " has no notification", deviceClient), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
            }
        }

        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileOnHttpSelfSigned() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        // arrange
        setUpX509DeviceClient(IotHubClientProtocol.HTTPS);
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
                        fail(buildExceptionMessage("IOException occurred during upload: " + e.getMessage(), deviceClient));
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

            // assert
            if (!isBasicTierHub)
            {
                FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevicex509);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
            }
            waitForFileUploadStatusCallbackTriggered(i);
            assertEquals(buildExceptionMessage("Expected SUCCESS but file upload status " + i + " was " + fileUploadState[i].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[i].fileUploadStatus);
            assertEquals(buildExceptionMessage("Expected SUCCESS but message status " + i + " was " + messageStates[i].messageStatus, deviceClient), SUCCESS, messageStates[i].messageStatus);
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        if (!isBasicTierHub)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                assertEquals(buildExceptionMessage("File" + i + " has no notification", deviceClient), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
            }
        }

        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncAndTelemetryOnMQTTSelfSigned() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        // arrange
        setUpX509DeviceClient(IotHubClientProtocol.MQTT);
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
                        fail(buildExceptionMessage("IOException occurred during upload: " + e.getMessage(), deviceClient));
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

            // assert
            if (!isBasicTierHub)
            {
                FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevicex509);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
            }

            waitForFileUploadStatusCallbackTriggered(i);
            assertEquals(buildExceptionMessage("Expected SUCCESS but file upload status " + i + " was " + fileUploadState[i].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[i].fileUploadStatus);
            assertEquals(buildExceptionMessage("Expected SUCCESS but message status " + i + " was " + messageStates[i].messageStatus, deviceClient), SUCCESS, messageStates[i].messageStatus);
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        if (!isBasicTierHub)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                assertEquals(buildExceptionMessage("File" + i + " has no notification", deviceClient), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
            }
        }

        tearDownDeviceClient();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileOnAMQPSelfSigned() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        // arrange
        setUpX509DeviceClient(IotHubClientProtocol.AMQPS);
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
                        fail(buildExceptionMessage("IOException occurred during upload: " + e.getMessage(), deviceClient));
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

            // assert
            if (!isBasicTierHub)
            {
                FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(scDevicex509);
                verifyNotification(fileUploadNotification, fileUploadState[i]);
            }
            waitForFileUploadStatusCallbackTriggered(i);
            assertEquals(buildExceptionMessage("Expected SUCCESS but file upload status " + i + " was " + fileUploadState[i].fileUploadStatus, deviceClient), SUCCESS, fileUploadState[i].fileUploadStatus);
            assertEquals(buildExceptionMessage("Expected SUCCESS but message status " + i + " was " + messageStates[i].messageStatus, deviceClient), SUCCESS, messageStates[i].messageStatus);
        }

        executor.shutdown();
        if (!executor.awaitTermination(10000, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        if (!isBasicTierHub)
        {
            for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
            {
                assertEquals(buildExceptionMessage("File" + i + " has no notification", deviceClient), fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
            }
        }

        tearDownDeviceClient();
    }

    private FileUploadNotification getFileUploadNotificationForThisDevice(Device device) throws IOException, InterruptedException
    {
        FileUploadNotification fileUploadNotification;
        do
        {
            fileUploadNotification = fileUploadNotificationReceiver.receive(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
            assertNotNull(buildExceptionMessage("file upload notification was null", deviceClient), fileUploadNotification);

            //ignore any file upload notifications received that are not about this device
        } while (!fileUploadNotification.getDeviceId().equals(device.getDeviceId()));

        return fileUploadNotification;
    }

    private void waitForFileUploadStatusCallbackTriggered(int fileUploadStateIndex) throws InterruptedException
    {
        if (!fileUploadState[fileUploadStateIndex].isCallBackTriggered)
        {
            //wait until file upload callback is triggered
            long startTime = System.currentTimeMillis();
            while (!fileUploadState[fileUploadStateIndex].isCallBackTriggered)
            {
                Thread.sleep(300);
                if (System.currentTimeMillis() - startTime > MAXIMUM_TIME_TO_WAIT_FOR_CALLBACK)
                {
                    assertTrue(buildExceptionMessage("File upload callback was not triggered", deviceClient), fileUploadState[fileUploadStateIndex].isCallBackTriggered);
                }
            }
        }
    }
}
