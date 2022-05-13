/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub;


import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.FileUploadCompletionNotification;
import com.microsoft.azure.sdk.iot.device.FileUploadSasUriRequest;
import com.microsoft.azure.sdk.iot.device.FileUploadSasUriResponse;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.ProxySettings;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationProcessorClient;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationProcessorClientOptions;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.HttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestDeviceIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol.AMQPS_WS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;

/**
 * Test class containing all tests to be run on JVM and android pertaining to FileUpload.
 */
@Slf4j
@IotHubTest
@RunWith(Parameterized.class)
@ContinuousIntegrationTest // Tests are very flakey in Canary in particular
public class FileUploadTests extends IntegrationTest
{
    // Max time to wait to see it on Hub
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS = 180000; // 3 minutes

    //Max time to wait before timing out test
    private static final long MAX_MILLISECS_TIMEOUT_KILL_TEST = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS + 50000; // 50 secs

    private static final int NOTIFICATION_TIMEOUT_MILLIS = 3 * 60 * 1000; // 3 minutes

    // remote name of the file
    private static final String REMOTE_FILE_NAME = "File";
    private static final String REMOTE_FILE_NAME_EXT = ".txt";

    protected static String iotHubConnectionString = "";

    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8897;

    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

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

    public static class FileUploadTestInstance
    {
        public IotHubClientProtocol protocol;
        public AuthenticationType authenticationType;
        private final FileUploadState fileUploadState;
        private final boolean withProxy;

        public FileUploadTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean withProxy)
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.withProxy = withProxy;
            this.fileUploadState = new FileUploadState();
        }
    }

    private static class FileUploadState
    {
        String blobName;
        InputStream fileInputStream;
        long fileLength;

        public FileUploadState()
        {
            byte[] buf = new byte[10];
            new Random().nextBytes(buf);
            blobName = REMOTE_FILE_NAME + UUID.randomUUID() + REMOTE_FILE_NAME_EXT;
            fileInputStream = new ByteArrayInputStream(buf);
            fileLength = buf.length;
        }
    }

    @BeforeClass
    public static void startProxy()
    {
        proxyServer = DefaultHttpProxyServer.bootstrap()
                .withPort(testProxyPort)
                .start();
    }

    @AfterClass
    public static void stopProxy()
    {
        proxyServer.stop();
    }

    private DeviceClient setUpDeviceClient(IotHubClientProtocol protocol) throws URISyntaxException, InterruptedException, IOException, IotHubException, GeneralSecurityException, IotHubClientException
    {
        ClientOptions.ClientOptionsBuilder optionsBuilder = ClientOptions.builder();
        if (testInstance.withProxy)
        {
            Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
            optionsBuilder.proxySettings(new ProxySettings(testProxy));
        }

        TestDeviceIdentity testDeviceIdentity = Tools.getTestDevice(iotHubConnectionString, protocol, testInstance.authenticationType, false, optionsBuilder);
        DeviceClient deviceClient = testDeviceIdentity.getDeviceClient();

        deviceClient.open(false);
        return deviceClient;
    }

    private void tearDownDeviceClient(DeviceClient deviceClient) throws IOException
    {
        deviceClient.close();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void getAndCompleteSasUriWithoutUpload() throws URISyntaxException, IOException, InterruptedException, IotHubException, GeneralSecurityException, IotHubClientException
    {
        // arrange
        DeviceClient deviceClient = setUpDeviceClient(testInstance.protocol);

        // act
        FileUploadSasUriResponse sasUriResponse = deviceClient.getFileUploadSasUri(new FileUploadSasUriRequest(testInstance.fileUploadState.blobName));

        FileUploadCompletionNotification fileUploadCompletionNotification = new FileUploadCompletionNotification();
        fileUploadCompletionNotification.setCorrelationId(sasUriResponse.getCorrelationId());
        fileUploadCompletionNotification.setStatusCode(0);

        // Since we don't care to need to test the Azure Storage SDK here though, we'll forgo actually uploading the file.
        // Because of that, this value needs to be false, otherwise hub throws 400 error since the file wasn't actually uploaded.
        fileUploadCompletionNotification.setSuccess(false);

        fileUploadCompletionNotification.setStatusDescription("Succeed to upload to storage.");

        deviceClient.completeFileUpload(fileUploadCompletionNotification);

        // assert
        tearDownDeviceClient(deviceClient);
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void getAndCompleteSasUriWithUpload() throws URISyntaxException, IOException, InterruptedException, IotHubException, GeneralSecurityException, StorageException, TimeoutException, IotHubClientException
    {
        // Android has some compatibility issues with the azure storage SDK
        assumeFalse(Tools.isAndroid());

        // arrange
        DeviceClient deviceClient = setUpDeviceClient(testInstance.protocol);

        // act
        FileUploadSasUriResponse sasUriResponse = deviceClient.getFileUploadSasUri(new FileUploadSasUriRequest(testInstance.fileUploadState.blobName));

        log.info("Uploading from device {} to blob {}", deviceClient.getConfig().getDeviceId(), sasUriResponse.getBlobName());
        CloudBlockBlob blob = new CloudBlockBlob(sasUriResponse.getBlobUri());
        blob.upload(testInstance.fileUploadState.fileInputStream, testInstance.fileUploadState.fileLength);

        FileUploadCompletionNotification fileUploadCompletionNotification = new FileUploadCompletionNotification();
        fileUploadCompletionNotification.setCorrelationId(sasUriResponse.getCorrelationId());
        fileUploadCompletionNotification.setStatusCode(0);

        fileUploadCompletionNotification.setSuccess(true);

        fileUploadCompletionNotification.setStatusDescription("Succeed to upload to storage.");

        deviceClient.completeFileUpload(fileUploadCompletionNotification);

        // assert
        List<String> expectedBlobNames = new ArrayList<>();
        expectedBlobNames.add(deviceClient.getConfig().getDeviceId() + "/" + testInstance.fileUploadState.blobName);
        waitForFileUploadNotifications(deviceClient.getConfig().getDeviceId(), expectedBlobNames);

        tearDownDeviceClient(deviceClient);
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void getAndCompleteSasUriWithMultipleUploads() throws URISyntaxException, IOException, InterruptedException, IotHubException, GeneralSecurityException, StorageException, TimeoutException, IotHubClientException
    {
        // Android has some compatibility issues with the azure storage SDK
        assumeFalse(Tools.isAndroid());

        // This test is moreso for the service client, so don't parameterize on device side options
        assumeFalse(testInstance.withProxy || testInstance.authenticationType == AuthenticationType.SELF_SIGNED);

        // arrange
        int fileUploadCount = 5;

        DeviceClient deviceClient = setUpDeviceClient(testInstance.protocol);
        FileUploadState[] fileUploadStates = new FileUploadState[fileUploadCount];
        List<String> expectedBlobNames = new ArrayList<>();
        for (int i = 0; i < fileUploadCount; i++)
        {
            fileUploadStates[i] = new FileUploadState();
            FileUploadSasUriResponse sasUriResponse = deviceClient.getFileUploadSasUri(new FileUploadSasUriRequest(fileUploadStates[i].blobName));

            CloudBlockBlob blob = new CloudBlockBlob(sasUriResponse.getBlobUri());
            blob.upload(fileUploadStates[i].fileInputStream, fileUploadStates[i].fileLength);

            expectedBlobNames.add(deviceClient.getConfig().getDeviceId() + "/" + fileUploadStates[i].blobName);

            FileUploadCompletionNotification fileUploadCompletionNotification = new FileUploadCompletionNotification();
            fileUploadCompletionNotification.setCorrelationId(sasUriResponse.getCorrelationId());
            fileUploadCompletionNotification.setStatusCode(0);

            fileUploadCompletionNotification.setSuccess(true);

            fileUploadCompletionNotification.setStatusDescription("Succeed to upload to storage.");

            deviceClient.completeFileUpload(fileUploadCompletionNotification);
        }

        // assert
        waitForFileUploadNotifications(deviceClient.getConfig().getDeviceId(), expectedBlobNames);

        tearDownDeviceClient(deviceClient);
    }

    private void waitForFileUploadNotifications(String expectedDeviceId, List<String> expectedBlobNames) throws InterruptedException, IOException, IotHubException, TimeoutException
    {
        Function<FileUploadNotification, AcknowledgementType> notificationProcessor = notification ->
        {
            // if one of the expected file upload notifications is found, remove it from the list of expected notifications
            String actualDeviceId = notification.getDeviceId();
            String actualBlobName = notification.getBlobName();
            log.info("notification received for device {} with blob name {}", actualDeviceId, actualBlobName);
            if (actualDeviceId.equals(expectedDeviceId))
            {
                String matchedBlobName = null;
                for (String expectedBlobName : expectedBlobNames)
                {
                    if (expectedBlobName.equals(actualBlobName))
                    {
                        matchedBlobName = expectedBlobName;
                        break;
                    }
                }

                if (matchedBlobName != null)
                {
                    expectedBlobNames.remove(matchedBlobName);
                    return AcknowledgementType.COMPLETE;
                }
            }

            return AcknowledgementType.ABANDON;
        };

        FileUploadNotificationProcessorClientOptions.FileUploadNotificationProcessorClientOptionsBuilder fileUploadNotificationProcessorClientOptionsBuilder =
            FileUploadNotificationProcessorClientOptions.builder();

        if (testInstance.withProxy)
        {
            Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
            fileUploadNotificationProcessorClientOptionsBuilder.proxyOptions(new ProxyOptions(testProxy));
        }

        FileUploadNotificationProcessorClient fileUploadNotificationProcessorClient =
            new FileUploadNotificationProcessorClient(iotHubConnectionString, AMQPS_WS, notificationProcessor, fileUploadNotificationProcessorClientOptionsBuilder.build());

        fileUploadNotificationProcessorClient.start();

        long startTime = System.currentTimeMillis();
        while (!expectedBlobNames.isEmpty())
        {
            if (System.currentTimeMillis() - startTime > NOTIFICATION_TIMEOUT_MILLIS)
            {
                fail("Timed out waiting on one or more file upload notifications for device " + expectedDeviceId + " with blob name " + expectedBlobNames.get(0));
            }

            Thread.sleep(1000);
        }

        fileUploadNotificationProcessorClient.stop();
    }
}
