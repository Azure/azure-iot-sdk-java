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
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationReceiver;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.ServiceClient;
import com.microsoft.azure.sdk.iot.service.messaging.ServiceClientOptions;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.BasicProxyAuthenticator;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Success;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class containing all tests to be run on JVM and android pertaining to FileUpload.
 */
@Slf4j
@IotHubTest
@RunWith(Parameterized.class)
public class FileUploadTests extends IntegrationTest
{
    // Max time to wait to see it on Hub
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS = 180000; // 3 minutes

    //Max time to wait before timing out test
    private static final long MAX_MILLISECS_TIMEOUT_KILL_TEST = MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS + 50000; // 50 secs

    private static final int NOTIFICATION_TIMEOUT_MILLIS = 60 * 1000; // 1 minute

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
        private FileUploadState fileUploadState;
        private final boolean withProxy;

        public FileUploadTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean withProxy)
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.withProxy = withProxy;
        }
    }

    private static class FileUploadState
    {
        String blobName;
        InputStream fileInputStream;
        long fileLength;
    }

    @Before
    public void setUpFileUploadState()
    {
        testInstance.fileUploadState = new FileUploadState();
        byte[] buf = new byte[10];
        new Random().nextBytes(buf);
        testInstance.fileUploadState = new FileUploadState();
        testInstance.fileUploadState.blobName = REMOTE_FILE_NAME + REMOTE_FILE_NAME_EXT;
        testInstance.fileUploadState.fileInputStream = new ByteArrayInputStream(buf);
        testInstance.fileUploadState.fileLength = buf.length;
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

    private DeviceClient setUpDeviceClient(IotHubClientProtocol protocol) throws URISyntaxException, InterruptedException, IOException, IotHubException, GeneralSecurityException
    {
        ClientOptions.ClientOptionsBuilder optionsBuilder = ClientOptions.builder();
        if (testInstance.withProxy)
        {
            Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
            optionsBuilder.proxySettings(new ProxySettings(testProxy));
        }

        TestDeviceIdentity testDeviceIdentity = Tools.getTestDevice(iotHubConnectionString, protocol, testInstance.authenticationType, false, optionsBuilder);
        DeviceClient deviceClient = testDeviceIdentity.getDeviceClient();


        Thread.sleep(5000);

        deviceClient.open(false);
        return deviceClient;
    }

    private void tearDownDeviceClient(DeviceClient deviceClient) throws IOException
    {
        deviceClient.close();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void getAndCompleteSasUriWithoutUpload() throws URISyntaxException, IOException, InterruptedException, IotHubException, GeneralSecurityException
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
    public void getAndCompleteSasUriWithUpload() throws URISyntaxException, IOException, InterruptedException, IotHubException, GeneralSecurityException, StorageException
    {
        // arrange
        DeviceClient deviceClient = setUpDeviceClient(testInstance.protocol);

        // act
        FileUploadSasUriResponse sasUriResponse = deviceClient.getFileUploadSasUri(new FileUploadSasUriRequest(testInstance.fileUploadState.blobName));

        CloudBlockBlob blob = new CloudBlockBlob(sasUriResponse.getBlobUri());
        blob.upload(testInstance.fileUploadState.fileInputStream, testInstance.fileUploadState.fileLength);

        FileUploadCompletionNotification fileUploadCompletionNotification = new FileUploadCompletionNotification();
        fileUploadCompletionNotification.setCorrelationId(sasUriResponse.getCorrelationId());
        fileUploadCompletionNotification.setStatusCode(0);

        fileUploadCompletionNotification.setSuccess(true);

        fileUploadCompletionNotification.setStatusDescription("Succeed to upload to storage.");

        deviceClient.completeFileUpload(fileUploadCompletionNotification);

        // assert
        waitForFileUploadNotification(deviceClient.getConfig().getDeviceId());

        tearDownDeviceClient(deviceClient);
    }

    private void waitForFileUploadNotification(String expectedDeviceId) throws InterruptedException, IOException
    {
        ServiceClientOptions.ServiceClientOptionsBuilder optionsBuilder = ServiceClientOptions.builder();

        if (testInstance.withProxy)
        {
            Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
            optionsBuilder.proxyOptions(new ProxyOptions(testProxy));
        }

        ServiceClient serviceClient = new ServiceClient(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS_WS, optionsBuilder.build());
        final Success notificationReceived = new Success();

        FileUploadNotificationReceiver receiver = serviceClient.getFileUploadNotificationReceiver(notification ->
        {
            String deviceId = notification.getDeviceId();
            log.info("notification received for device {}", deviceId);
            if (deviceId.equals(expectedDeviceId))
            {
                notificationReceived.setResult(true);
                notificationReceived.callbackWasFired();
                return IotHubMessageResult.COMPLETE;
            }

            return IotHubMessageResult.ABANDON;
        });

        receiver.open();

        long startTime = System.currentTimeMillis();
        while (!notificationReceived.wasCallbackFired())
        {
            Thread.sleep(1000);

            if (System.currentTimeMillis() - startTime > NOTIFICATION_TIMEOUT_MILLIS)
            {
                fail("Timed out waiting on notification for device " + expectedDeviceId);
            }
        }

        receiver.close();
        assertTrue(notificationReceived.getResult());
    }
}
