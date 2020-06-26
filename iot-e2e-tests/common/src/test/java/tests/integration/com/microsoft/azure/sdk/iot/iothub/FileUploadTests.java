/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub;


import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadCompletionNotification;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriRequest;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriResponse;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.FlakeyTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static tests.integration.com.microsoft.azure.sdk.iot.iothub.FileUploadTests.STATUS.FAILURE;
import static tests.integration.com.microsoft.azure.sdk.iot.iothub.FileUploadTests.STATUS.SUCCESS;

/**
 * Test class containing all tests to be run on JVM and android pertaining to FileUpload.
 */
@FlakeyTest
@IotHubTest
@RunWith(Parameterized.class)
public class FileUploadTests extends IntegrationTest
{
    // Max time to wait to see it on Hub
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS = 180000; // 3 minutes
    private static final long FILE_UPLOAD_QUEUE_POLLING_INTERVAL_MILLISECONDS = 4000; // 4 sec
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_CALLBACK_MILLISECONDS = 5000; // 5 sec

    //Max time to wait before timing out test
    private static final long MAX_MILLISECS_TIMEOUT_KILL_TEST = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS + 50000; // 50 secs

    //Max devices to test
    private static final Integer MAX_FILES_TO_UPLOAD = 5;

    // remote name of the file
    private static final String REMOTE_FILE_NAME = "File";
    private static final String REMOTE_FILE_NAME_EXT = ".txt";

    protected static String iotHubConnectionString = "";

    // States of SDK
    private static RegistryManager registryManager;
    private static ServiceClient serviceClient;

    private static String publicKeyCertificate;
    private static String privateKeyCertificate;
    private static String x509Thumbprint;

    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8897;
    protected static final String testProxyUser = "proxyUsername";
    protected static final char[] testProxyPass = "1234".toCharArray();

    static Queue<FileUploadNotification> activeFileUploadNotifications = new ConcurrentLinkedQueue<>();

    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        serviceClient.open();

        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        publicKeyCertificate = certificateGenerator.getPublicCertificate();
        privateKeyCertificate = certificateGenerator.getPrivateKey();
        x509Thumbprint = certificateGenerator.getX509Thumbprint();

        return Arrays.asList(
                new Object[][]
                        {
                                //without proxy
                                {IotHubClientProtocol.HTTPS, AuthenticationType.SAS, false},
                                {IotHubClientProtocol.HTTPS, AuthenticationType.SELF_SIGNED, false},

                                //with proxy
                                {IotHubClientProtocol.HTTPS, AuthenticationType.SAS, true},
                                {IotHubClientProtocol.HTTPS, AuthenticationType.SELF_SIGNED, true}
                        });
    }

    public FileUploadTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean withProxy) throws IOException
    {
        this.testInstance = new FileUploadTestInstance(protocol, authenticationType, withProxy);
    }

    public FileUploadTestInstance testInstance;

    public class FileUploadTestInstance
    {
        public IotHubClientProtocol protocol;
        public AuthenticationType authenticationType;
        private FileUploadState[] fileUploadState;
        private MessageState[] messageStates;
        private boolean withProxy;
        private FileUploadNotificationReceiver fileUploadNotificationReceiver;

        public FileUploadTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean withProxy) throws IOException
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.withProxy = withProxy;
            fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();
            fileUploadNotificationReceiver.open();
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
            FileUploadState f = (FileUploadState) context;
            System.out.println("Callback fired with status " + responseStatus);
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

    @Before
    public void setUpFileUploadState() throws Exception
    {
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

    @AfterClass
    public static void tearDown()
    {
        if (registryManager != null)
        {
            registryManager.close();
            registryManager = null;
        }

        serviceClient = null;
    }

    @BeforeClass
    public static void startProxy()
    {
        proxyServer = DefaultHttpProxyServer.bootstrap()
                .withPort(testProxyPort)
                .withProxyAuthenticator(new BasicProxyAuthenticator(testProxyUser, testProxyPass))
                .start();
    }

    @AfterClass
    public static void stopProxy()
    {
        proxyServer.stop();
    }

    private DeviceClient setUpDeviceClient(IotHubClientProtocol protocol) throws URISyntaxException, InterruptedException, IOException, IotHubException, GeneralSecurityException
    {
        DeviceClient deviceClient;
        if (testInstance.authenticationType == AuthenticationType.SAS)
        {
            String deviceId = "java-file-upload-e2e-test-".concat(UUID.randomUUID().toString());
            Device scDevice = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceId, null, null);
            scDevice = Tools.addDeviceWithRetry(registryManager, scDevice);

            deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, scDevice), protocol);

        }
        else if (testInstance.authenticationType == AuthenticationType.SELF_SIGNED)
        {
            String deviceIdX509 = "java-file-upload-e2e-test-x509-".concat(UUID.randomUUID().toString());
            Device scDevicex509 = com.microsoft.azure.sdk.iot.service.Device.createDevice(deviceIdX509, AuthenticationType.SELF_SIGNED);
            scDevicex509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);
            scDevicex509 = Tools.addDeviceWithRetry(registryManager, scDevicex509);

            SSLContext sslContext = SSLContextBuilder.buildSSLContext(publicKeyCertificate, privateKeyCertificate);
            deviceClient = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, scDevicex509), protocol, sslContext);
        }
        else
        {
            throw new IllegalArgumentException("Test code has not been written for this authentication type yet");
        }

        if (testInstance.withProxy)
        {
            Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
            deviceClient.setProxySettings(new ProxySettings(testProxy, testProxyUser, testProxyPass));
        }

        Thread.sleep(5000);

        deviceClient.open();
        return deviceClient;
    }

    private void tearDownDeviceClient(DeviceClient deviceClient) throws IOException
    {
        deviceClient.closeNow();
    }

    private void verifyNotification(FileUploadNotification fileUploadNotification, FileUploadState fileUploadState, DeviceClient deviceClient) throws IOException
    {
        assertTrue(buildExceptionMessage("File upload notification blob size not equal to expected file length", deviceClient), fileUploadNotification.getBlobSizeInBytes() == fileUploadState.fileLength);

        URL u = new URL(fileUploadNotification.getBlobUri());
        try (InputStream inputStream = u.openStream())
        {
            byte[] testBuf = new byte[(int)fileUploadState.fileLength];
            inputStream.read(testBuf,  0, (int)fileUploadState.fileLength);
            int testLen = (int)fileUploadState.fileLength;
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
    @ContinuousIntegrationTest
    public void uploadToBlobAsyncSingleFileZeroLength() throws URISyntaxException, IOException, InterruptedException, IotHubException, GeneralSecurityException
    {
        // arrange
        DeviceClient deviceClient = setUpDeviceClient(testInstance.protocol);

        // act
        deviceClient.uploadToBlobAsync(testInstance.fileUploadState[0].blobName, testInstance.fileUploadState[0].fileInputStream, testInstance.fileUploadState[0].fileLength, new FileUploadCallback(), testInstance.fileUploadState[0]);

        // assert
        if (!isBasicTierHub)
        {
            FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(deviceClient, 0);
            verifyNotification(fileUploadNotification, testInstance.fileUploadState[0], deviceClient);
        }
        waitForFileUploadStatusCallbackTriggered(0, deviceClient);
        assertEquals(buildExceptionMessage("File upload status expected SUCCESS but was " + testInstance.fileUploadState[0].fileUploadStatus, deviceClient), SUCCESS, testInstance.fileUploadState[0].fileUploadStatus);
        tearDownDeviceClient(deviceClient);
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFileGranular() throws URISyntaxException, IOException, InterruptedException, IotHubException, GeneralSecurityException, StorageException
    {
        // arrange
        DeviceClient deviceClient = setUpDeviceClient(testInstance.protocol);

        // act
        FileUploadSasUriResponse sasUriResponse = deviceClient.getFileUploadSasUri(new FileUploadSasUriRequest(testInstance.fileUploadState[0].blobName));

        CloudBlockBlob blob = new CloudBlockBlob(sasUriResponse.getBlobUri());
        blob.upload(testInstance.fileUploadState[0].fileInputStream, testInstance.fileUploadState[0].fileLength);
        FileUploadCompletionNotification fileUploadCompletionNotification = new FileUploadCompletionNotification();
        fileUploadCompletionNotification.setCorrelationId(sasUriResponse.getCorrelationId());
        fileUploadCompletionNotification.setStatusCode(0);
        fileUploadCompletionNotification.setSuccess(true);
        fileUploadCompletionNotification.setStatusDescription("Succeed to upload to storage.");

        deviceClient.completeFileUploadAsync(fileUploadCompletionNotification);

        // assert
        if (!isBasicTierHub)
        {
            FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(deviceClient, (int) testInstance.fileUploadState[0].fileLength);
            assertNotNull(buildExceptionMessage("file upload notification was null", deviceClient), fileUploadNotification);
            verifyNotification(fileUploadNotification, testInstance.fileUploadState[0], deviceClient);
        }

        assertEquals(buildExceptionMessage("File upload status should be SUCCESS but was " + testInstance.fileUploadState[0].fileUploadStatus, deviceClient), SUCCESS, testInstance.fileUploadState[0].fileUploadStatus);

        tearDownDeviceClient(deviceClient);
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void uploadToBlobAsyncSingleFile() throws URISyntaxException, IOException, InterruptedException, IotHubException, GeneralSecurityException
    {
        // arrange
        DeviceClient deviceClient = setUpDeviceClient(testInstance.protocol);

        // act
        deviceClient.uploadToBlobAsync(testInstance.fileUploadState[MAX_FILES_TO_UPLOAD - 1].blobName, testInstance.fileUploadState[MAX_FILES_TO_UPLOAD - 1].fileInputStream, testInstance.fileUploadState[MAX_FILES_TO_UPLOAD - 1].fileLength, new FileUploadCallback(), testInstance.fileUploadState[MAX_FILES_TO_UPLOAD - 1]);

        // assert
        if (!isBasicTierHub)
        {
            FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(deviceClient, MAX_FILES_TO_UPLOAD - 1);
            assertNotNull(buildExceptionMessage("file upload notification was null", deviceClient), fileUploadNotification);
            verifyNotification(fileUploadNotification, testInstance.fileUploadState[MAX_FILES_TO_UPLOAD - 1], deviceClient);
        }
        waitForFileUploadStatusCallbackTriggered(MAX_FILES_TO_UPLOAD - 1, deviceClient);
        assertEquals(buildExceptionMessage("File upload status should be SUCCESS but was " + testInstance.fileUploadState[MAX_FILES_TO_UPLOAD - 1].fileUploadStatus, deviceClient), SUCCESS, testInstance.fileUploadState[MAX_FILES_TO_UPLOAD - 1].fileUploadStatus);

        tearDownDeviceClient(deviceClient);
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    @ContinuousIntegrationTest
    public void uploadToBlobAsyncMultipleFilesParallel() throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException, IotHubException, GeneralSecurityException
    {
        if (testInstance.withProxy)
        {
            //No need to do performance test both with and without proxy
            return;
        }
        
        // arrange
        DeviceClient deviceClient = setUpDeviceClient(testInstance.protocol);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // act
        for (int i = 1; i < MAX_FILES_TO_UPLOAD; i++)
        {
            final int index = i;
            executor.submit(() ->
            {
                try
                {
                    deviceClient.uploadToBlobAsync(testInstance.fileUploadState[index].blobName, testInstance.fileUploadState[index].fileInputStream, testInstance.fileUploadState[index].fileLength, new FileUploadCallback(), testInstance.fileUploadState[index]);
                }
                catch (IOException e)
                {
                    fail(buildExceptionMessage("IOException occurred during upload: " + Tools.getStackTraceFromThrowable(e), deviceClient));
                }
            });

            // assert
            if (!isBasicTierHub)
            {
                FileUploadNotification fileUploadNotification = getFileUploadNotificationForThisDevice(deviceClient, i);
                verifyNotification(fileUploadNotification, testInstance.fileUploadState[i], deviceClient);
            }
            waitForFileUploadStatusCallbackTriggered(i, deviceClient);
            assertEquals(buildExceptionMessage("Expected SUCCESS but file upload status " + i + " was " + testInstance.fileUploadState[i].fileUploadStatus, deviceClient), SUCCESS, testInstance.fileUploadState[i].fileUploadStatus);
            assertEquals(buildExceptionMessage("Expected SUCCESS but message status " + i + " was " + testInstance.messageStates[i].messageStatus, deviceClient), SUCCESS, testInstance.messageStates[i].messageStatus);
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
                assertEquals(buildExceptionMessage("File" + i + " has no notification", deviceClient), testInstance.fileUploadState[i].fileUploadNotificationReceived, SUCCESS);
            }
        }

        tearDownDeviceClient(deviceClient);
    }

    private FileUploadNotification getFileUploadNotificationForThisDevice(DeviceClient deviceClient, int expectedBlobSizeInBytes) throws InterruptedException, IOException
    {
        //wait until the notification is added to the set of retrieved notifications, or until a timeout
        long startTime = System.currentTimeMillis();
        FileUploadNotification matchingNotification = null;
        do
        {
            for (FileUploadNotification notification : activeFileUploadNotifications)
            {
                if (notification.getDeviceId().equals(deviceClient.getConfig().getDeviceId()))
                {
                    if (notification.getBlobSizeInBytes().intValue() == expectedBlobSizeInBytes)
                    {
                        matchingNotification = notification;
                    }
                }
            }

            FileUploadNotification fileUploadNotification = testInstance.fileUploadNotificationReceiver.receive(FILE_UPLOAD_QUEUE_POLLING_INTERVAL_MILLISECONDS);

            if (fileUploadNotification != null)
            {
                activeFileUploadNotifications.add(fileUploadNotification);
            }

            if (System.currentTimeMillis() - startTime > MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS)
            {
                Assert.fail(CorrelationDetailsLoggingAssert.buildExceptionMessage("Timed out waiting for file upload notification for device", deviceClient));
            }

        } while (matchingNotification == null);

        if (matchingNotification != null)
        {
            activeFileUploadNotifications.remove(matchingNotification);
        }

        return matchingNotification;
    }

    private void waitForFileUploadStatusCallbackTriggered(int fileUploadStateIndex, DeviceClient deviceClient) throws InterruptedException
    {
        if (!testInstance.fileUploadState[fileUploadStateIndex].isCallBackTriggered)
        {
            //wait until file upload callback is triggered
            long startTime = System.currentTimeMillis();
            while (!testInstance.fileUploadState[fileUploadStateIndex].isCallBackTriggered)
            {
                Thread.sleep(300);
                if (System.currentTimeMillis() - startTime > MAXIMUM_TIME_TO_WAIT_FOR_CALLBACK_MILLISECONDS)
                {
                    assertTrue(buildExceptionMessage("File upload callback was not triggered", deviceClient), testInstance.fileUploadState[fileUploadStateIndex].isCallBackTriggered);
                }
            }
        }
    }
}
