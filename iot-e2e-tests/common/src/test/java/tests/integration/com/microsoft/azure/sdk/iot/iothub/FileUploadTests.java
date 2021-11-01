/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub;


import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.serializer.FileUploadCompletionNotification;
import com.microsoft.azure.sdk.iot.device.serializer.FileUploadSasUriRequest;
import com.microsoft.azure.sdk.iot.device.serializer.FileUploadSasUriResponse;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static org.junit.Assert.*;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static tests.integration.com.microsoft.azure.sdk.iot.iothub.FileUploadTests.STATUS.FAILURE;
import static tests.integration.com.microsoft.azure.sdk.iot.iothub.FileUploadTests.STATUS.SUCCESS;

/**
 * Test class containing all tests to be run on JVM and android pertaining to FileUpload.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class FileUploadTests extends IntegrationTest
{
    // Max time to wait to see it on Hub
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS = 180000; // 3 minutes
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

    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8897;

    // Semmle flags this as a security issue, but this is a test username so the warning can be suppressed
    protected static final String testProxyUser = "proxyUsername"; // lgtm

    // Semmle flags this as a security issue, but this is a test password so the warning can be suppressed
    protected static final char[] testProxyPass = "1234".toCharArray(); // lgtm

    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        registryManager = new RegistryManager(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());

        serviceClient = new ServiceClient(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        serviceClient.open();

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
        private FileUploadState[] fileUploadState;
        private MessageState[] messageStates;
        private final boolean withProxy;

        public FileUploadTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean withProxy)
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.withProxy = withProxy;
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
        boolean isCallbackTriggered;
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
                fileUploadState.isCallbackTriggered = true;

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
    public void setUpFileUploadState()
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
            testInstance.fileUploadState[i].isCallbackTriggered = false;

            testInstance.messageStates[i] = new MessageState();
            testInstance.messageStates[i].messageBody = new String(buf, StandardCharsets.UTF_8);
            testInstance.messageStates[i].messageStatus = SUCCESS;
        }
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
        TestDeviceIdentity testDeviceIdentity = Tools.getTestDevice(iotHubConnectionString, protocol, testInstance.authenticationType, false);
        DeviceClient deviceClient = testDeviceIdentity.getDeviceClient();

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
        deviceClient.close();
    }

    @Test (timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void getAndCompleteSasUri() throws URISyntaxException, IOException, InterruptedException, IotHubException, GeneralSecurityException
    {
        // arrange
        DeviceClient deviceClient = setUpDeviceClient(testInstance.protocol);

        // act
        FileUploadSasUriResponse sasUriResponse = deviceClient.getFileUploadSasUri(new FileUploadSasUriRequest(testInstance.fileUploadState[0].blobName));

        FileUploadCompletionNotification fileUploadCompletionNotification = new FileUploadCompletionNotification();
        fileUploadCompletionNotification.setCorrelationId(sasUriResponse.getCorrelationId());
        fileUploadCompletionNotification.setStatusCode(0);

        // Since we don't care to need to test the Azure Storage SDK here though, we'll forgo actually uploading the file.
        // Because of that, this value needs to be false, otherwise hub throws 400 error since the file wasn't actually uploaded.
        fileUploadCompletionNotification.setSuccess(false);

        fileUploadCompletionNotification.setStatusDescription("Succeed to upload to storage.");

        deviceClient.completeFileUpload(fileUploadCompletionNotification);

        // assert
        assertEquals(buildExceptionMessage("File upload status should be SUCCESS but was " + testInstance.fileUploadState[0].fileUploadStatus, deviceClient), SUCCESS, testInstance.fileUploadState[0].fileUploadStatus);

        tearDownDeviceClient(deviceClient);
    }

    private void waitForFileUploadStatusCallbackTriggered(int fileUploadStateIndex, DeviceClient deviceClient) throws InterruptedException
    {
        if (!testInstance.fileUploadState[fileUploadStateIndex].isCallbackTriggered)
        {
            //wait until file upload callback is triggered
            long startTime = System.currentTimeMillis();
            while (!testInstance.fileUploadState[fileUploadStateIndex].isCallbackTriggered)
            {
                Thread.sleep(300);
                if (System.currentTimeMillis() - startTime > MAXIMUM_TIME_TO_WAIT_FOR_CALLBACK_MILLISECONDS)
                {
                    assertTrue(buildExceptionMessage("File upload callback was not triggered", deviceClient), testInstance.fileUploadState[fileUploadStateIndex].isCallbackTriggered);
                }
            }
        }
    }
}
