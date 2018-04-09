package tests.integration.com.microsoft.azure.sdk.iot.iothubservices;

import com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.*;
import tests.integration.com.microsoft.azure.sdk.iot.DeviceConnectionString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS_WS;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static tests.integration.com.microsoft.azure.sdk.iot.iothubservices.TransportClientFileUploadIT.STATUS.FAILURE;
import static tests.integration.com.microsoft.azure.sdk.iot.iothubservices.TransportClientFileUploadIT.STATUS.SUCCESS;

public class TransportClientFileUploadIT
{
    private static final Integer MAX_DEVICE_MULTIPLEX = 2;

    // Max time to wait to see it on Hub
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 10000; // 10 sec

    //Max time to wait before timing out test
    private static final long MAX_MILLISECS_TIMEOUT_KILL_TEST = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB + 50000; // 50 secs

    // Max time to flush iothub notification from previous failing tests
    private static final long MAX_MILLISECS_TIMEOUT_FLUSH_NOTIFICATION = 10000; // 10 secs

    //Max devices to test
    private static final Integer MAX_FILES_TO_UPLOAD = 1;

    // remote name of the file
    private static final String REMOTE_FILE_NAME = "File";
    private static final String REMOTE_FILE_NAME_EXT = ".txt";

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;

    // States of SDK
    private static RegistryManager registryManager;
    private static ServiceClient serviceClient;
    private static Device scDevice;
    private static TransportClientFileUploadIT.FileUploadState[] fileUploadState;
    private static TransportClientFileUploadIT.MessageState[] messageStates;
    private static FileUploadNotificationReceiver fileUploadNotificationReceiver;

    private static Device[] deviceListAmqps = new Device[MAX_DEVICE_MULTIPLEX];
    private static ArrayList<String> clientConnectionStringArrayList = new ArrayList<>();

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
        TransportClientFileUploadIT.STATUS fileUploadStatus;
        TransportClientFileUploadIT.STATUS fileUploadNotificationReceived;
    }

    private static class MessageState
    {
        String messageBody;
        TransportClientFileUploadIT.STATUS messageStatus;
    }

    private static class FileUploadCallback implements IotHubEventCallback
    {
        @Override
        public void execute(IotHubStatusCode responseStatus, Object context)
        {
            if (context instanceof TransportClientFileUploadIT.FileUploadState)
            {
                TransportClientFileUploadIT.FileUploadState fileUploadState = (TransportClientFileUploadIT.FileUploadState) context;
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
            else if (context instanceof TransportClientFileUploadIT.MessageState)
            {
                TransportClientFileUploadIT.MessageState messageState = (TransportClientFileUploadIT.MessageState) context;
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
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet())
        {
            if (envName.equals(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME))
            {
                iotHubConnectionString = env.get(envName);
            }
        }

        assertNotNull(iotHubConnectionString);
        assertFalse(iotHubConnectionString.isEmpty());

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            String uuid = UUID.randomUUID().toString();
            String deviceId = "java-device-client-e2e-test-multiplexing-fileupload-amqps".concat(i + "-" + uuid);

            deviceListAmqps[i] = Device.createFromId(deviceId, null, null);
            registryManager.addDevice(deviceListAmqps[i]);
            clientConnectionStringArrayList.add(registryManager.getDeviceConnectionString(deviceListAmqps[i]));
        }

        serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();
        assertNotNull(fileUploadNotificationReceiver);

        // flush pending notifications before every test to prevent random test failures
        // because of notifications received from other failed test
        fileUploadNotificationReceiver.open();
        fileUploadNotificationReceiver.receive(MAX_MILLISECS_TIMEOUT_FLUSH_NOTIFICATION);
        fileUploadNotificationReceiver.close();
    }

    @Before
    public void setUpFileUploadState() throws Exception
    {
        // Start receiver for a test
        fileUploadNotificationReceiver.open();
        fileUploadState = new TransportClientFileUploadIT.FileUploadState[MAX_FILES_TO_UPLOAD];
        messageStates = new TransportClientFileUploadIT.MessageState[MAX_FILES_TO_UPLOAD];
        for (int i = 0; i < MAX_FILES_TO_UPLOAD; i++)
        {
            byte[] buf = new byte[i];
            new Random().nextBytes(buf);
            fileUploadState[i] = new TransportClientFileUploadIT.FileUploadState();
            fileUploadState[i].blobName = REMOTE_FILE_NAME + i + REMOTE_FILE_NAME_EXT;
            fileUploadState[i].fileInputStream = new ByteArrayInputStream(buf);
            fileUploadState[i].fileLength = buf.length;
            fileUploadState[i].fileUploadStatus = SUCCESS;
            fileUploadState[i].fileUploadNotificationReceived = FAILURE;
            fileUploadState[i].isCallBackTriggered = false;

            messageStates[i] = new TransportClientFileUploadIT.MessageState();
            messageStates[i].messageBody = new String(buf);
            messageStates[i].messageStatus = SUCCESS;
        }
    }

    @After
    public void tearDownFileUploadState() throws Exception
    {
        fileUploadState = null;
        messageStates = null;
        try
        {
            Thread.sleep(INTERTEST_GUARDIAN_DELAY_MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
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
            serviceClient = null;
        }
    }

    private void verifyNotification(FileUploadNotification fileUploadNotification, TransportClientFileUploadIT.FileUploadState fileUploadState) throws IOException, URISyntaxException
    {
        assertEquals("Received notification for a different device not belonging to this test", scDevice.getDeviceId(), fileUploadNotification.getDeviceId());

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

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileAndTelemetryOnAMQP() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        // arrange
        TransportClient transportClient = new TransportClient(AMQPS);
        final ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        SendMessagesCommon.openTransportClientWithRetry(transportClient);

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
                            clientArrayList.get(indexJ).uploadToBlobAsync(fileUploadState[indexI].blobName, fileUploadState[indexI].fileInputStream, fileUploadState[indexI].fileLength, new TransportClientFileUploadIT.FileUploadCallback(), fileUploadState[indexI]);
                        } catch (IOException e)
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
                        clientArrayList.get(indexJ).sendEventAsync(new com.microsoft.azure.sdk.iot.device.Message(messageStates[indexI].messageBody), new TransportClientFileUploadIT.FileUploadCallback(), messageStates[indexI]);
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
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileAndTelemetryOnAMQPWS() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException
    {
        // arrange
        TransportClient transportClient = new TransportClient(AMQPS_WS);
        final ArrayList<DeviceClient> clientArrayList = new ArrayList<>();

        for (int i = 0; i < MAX_DEVICE_MULTIPLEX; i++)
        {
            clientArrayList.add(new DeviceClient(clientConnectionStringArrayList.get(i), transportClient));
        }

        SendMessagesCommon.openTransportClientWithRetry(transportClient);

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
                            clientArrayList.get(indexJ).uploadToBlobAsync(fileUploadState[indexI].blobName, fileUploadState[indexI].fileInputStream, fileUploadState[indexI].fileLength, new TransportClientFileUploadIT.FileUploadCallback(), fileUploadState[indexI]);
                        } catch (IOException e)
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
                        clientArrayList.get(indexJ).sendEventAsync(new com.microsoft.azure.sdk.iot.device.Message(messageStates[indexI].messageBody), new TransportClientFileUploadIT.FileUploadCallback(), messageStates[indexI]);
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
    }
}
