/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.deps.auth.TokenCredentialType;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.FeedbackReceiver;
import com.microsoft.azure.sdk.iot.service.FileUploadNotificationReceiver;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.ServiceClientOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringCredential;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/**
 * Test class containing all tests to be run on JVM and android pertaining to C2D communication using the service client.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class ServiceClientTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";
    protected static String invalidCertificateServerConnectionString = "";
    private static final String deviceIdPrefix = "java-service-client-e2e-test";
    private static final String content = "abcdefghijklmnopqrstuvwxyz1234567890";
    private static String hostName;

    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8869;

    private static final int TOKEN_RENEWAL_TEST_TIMEOUT_MILLISECONDS = 2 * 60 * 1000;

    public ServiceClientTests(IotHubServiceClientProtocol protocol)
    {
        this.testInstance = new ServiceClientITRunner(protocol);
    }

    private static class ServiceClientITRunner
    {
        private final IotHubServiceClientProtocol protocol;
        private final String deviceId;

        public ServiceClientITRunner(IotHubServiceClientProtocol protocol)
        {
            this.protocol = protocol;
            this.deviceId = deviceIdPrefix.concat("-" + UUID.randomUUID().toString());

        }
    }

    private final ServiceClientITRunner testInstance;

    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs() throws IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        invalidCertificateServerConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.UNTRUSTWORTHY_IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();

        return Arrays.asList(
                new Object[][]
                        {
                                {IotHubServiceClientProtocol.AMQPS},
                                {IotHubServiceClientProtocol.AMQPS_WS}
                        }
        );
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

    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetry() throws Exception
    {
        cloudToDeviceTelemetry(false, true, false, false, false);
    }

    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithCustomSSLContext() throws Exception
    {
        cloudToDeviceTelemetry(false, true, true, false, false);
    }

    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithProxy() throws Exception
    {
        if (testInstance.protocol != IotHubServiceClientProtocol.AMQPS_WS)
        {
            //Proxy support only exists for AMQPS_WS currently
            return;
        }

        cloudToDeviceTelemetry(true, true, false, false, false);
    }

    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithProxyAndCustomSSLContext() throws Exception
    {
        if (testInstance.protocol != IotHubServiceClientProtocol.AMQPS_WS)
        {
            //Proxy support only exists for AMQPS_WS currently
            return;
        }

        cloudToDeviceTelemetry(true, true, true, false, false);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void cloudToDeviceTelemetryWithNoPayload() throws Exception
    {
        cloudToDeviceTelemetry(false, false, false, false, false);
    }

    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithTokenCredential() throws Exception
    {
        cloudToDeviceTelemetry(false, true, false, true, false);
    }

    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithAzureSasCredential() throws Exception
    {
        cloudToDeviceTelemetry(false, true, false, false, true);
    }

    public void cloudToDeviceTelemetry(
            boolean withProxy,
            boolean withPayload,
            boolean withCustomSSLContext,
            boolean withTokenCredential,
            boolean withAzureSasCredential) throws Exception
    {
        // We remove and recreate the device for a clean start
        RegistryManager registryManager =
                RegistryManager.createFromConnectionString(
                        iotHubConnectionString,
                        RegistryManagerOptions.builder()
                                .httpReadTimeout(HTTP_READ_TIMEOUT)
                                .build());

        Device deviceAdded = Device.createFromId(testInstance.deviceId, null, null);
        Tools.addDeviceWithRetry(registryManager, deviceAdded);

        Device deviceGetBefore = registryManager.getDevice(testInstance.deviceId);

        // Create service client
        ProxyOptions proxyOptions = null;
        if (withProxy)
        {
            Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
            proxyOptions = new ProxyOptions(testProxy);
        }

        SSLContext sslContext = null;
        if (withCustomSSLContext)
        {
            sslContext = new IotHubSSLContext().getSSLContext();
        }

        ServiceClientOptions serviceClientOptions =
                ServiceClientOptions.builder()
                        .proxyOptions(proxyOptions)
                        .sslContext(sslContext)
                        .build();

        ServiceClient serviceClient;
        IotHubConnectionString iotHubConnectionStringObj =
                IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);

        if (withTokenCredential)
        {
            TokenCredential authenticationTokenProvider = new IotHubConnectionStringCredential(iotHubConnectionString);
            serviceClient = new ServiceClient(
                    iotHubConnectionStringObj.getHostName(),
                    authenticationTokenProvider,
                    TokenCredentialType.SHARED_ACCESS_SIGNATURE,
                    testInstance.protocol,
                    serviceClientOptions);
        }
        else if (withAzureSasCredential)
        {
            serviceClient = new ServiceClient(
                    iotHubConnectionStringObj.getHostName(),
                    new AzureSasCredential(new IotHubServiceSasToken(iotHubConnectionStringObj).toString()),
                    testInstance.protocol,
                    serviceClientOptions);
        }
        else
        {
            serviceClient = new ServiceClient(iotHubConnectionString, testInstance.protocol, serviceClientOptions);
        }

        CompletableFuture<Void> futureOpen = serviceClient.openAsync();
        futureOpen.get();

        Message message;
        if (withPayload)
        {
            message = new Message(content.getBytes());
        }
        else
        {
            message = new Message();
        }

        CompletableFuture<Void> completableFuture = serviceClient.sendAsync(testInstance.deviceId, message);
        completableFuture.get();

        Device deviceGetAfter = registryManager.getDevice(testInstance.deviceId);
        CompletableFuture<Void> futureClose = serviceClient.closeAsync();
        futureClose.get();

        registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), deviceGetBefore.getDeviceId(), deviceGetAfter.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), 0, deviceGetBefore.getCloudToDeviceMessageCount());
        assertEquals(buildExceptionMessage("", hostName), 1, deviceGetAfter.getCloudToDeviceMessageCount());

        registryManager.close();
    }

    @Test
    @StandardTierHubOnlyTest
    public void feedbackMessageReceiverTokenRenewal() throws Exception
    {
        // We remove and recreate the device for a clean start
        RegistryManager registryManager =
                RegistryManager.createFromConnectionString(
                        iotHubConnectionString,
                        RegistryManagerOptions.builder()
                                .httpReadTimeout(HTTP_READ_TIMEOUT)
                                .build());

        ServiceClientOptions serviceClientOptions = ServiceClientOptions.builder().build();
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);

        long tokenLifespanSeconds = 10;
        long tokenLifespanMilliseconds = tokenLifespanSeconds * 1000;
        TokenCredential authenticationTokenProvider = new IotHubConnectionStringCredential(iotHubConnectionString, tokenLifespanSeconds);

        ServiceClient serviceClient = new ServiceClient(
                    iotHubConnectionStringObj.getHostName(),
                    authenticationTokenProvider,
                    TokenCredentialType.SHARED_ACCESS_SIGNATURE,
                    testInstance.protocol,
                    serviceClientOptions);

        FeedbackReceiver feedbackReceiver = serviceClient.getFeedbackReceiver();
        feedbackReceiver.open();

        boolean connectionStayedOpenLongerThanTokenExpiryTime = false;
        long loopStartTimeMilliseconds = System.currentTimeMillis();
        while (!connectionStayedOpenLongerThanTokenExpiryTime)
        {
            // Timing this call to feedbackReceiver.receive because it will end prematurely if a feedback message is
            // received. This test needs to open a connection for longer than the initial token would be valid for
            // in order to test that the AMQP layer is proactively renewing the connection. This loop will run until
            // there is a point in time where there are no feedback messages to receive, so the connection will stay
            // open.

            long startTimeMilliseconds = System.currentTimeMillis();

            // received feedback messages can be ignored since we no longer have any tests that need to consume them
            feedbackReceiver.receive(tokenLifespanMilliseconds * 3);

            long finishTimeMilliseconds = System.currentTimeMillis();

            if (finishTimeMilliseconds - startTimeMilliseconds >= tokenLifespanMilliseconds)
            {
                connectionStayedOpenLongerThanTokenExpiryTime = true;
            }

            if (System.currentTimeMillis() - loopStartTimeMilliseconds >= TOKEN_RENEWAL_TEST_TIMEOUT_MILLISECONDS)
            {
                fail("Timed out waiting for feedback receiver to open a connection that lasted longer than the token expiry time");
            }
        }

        feedbackReceiver.close();
        serviceClient.close();
        registryManager.close();
    }

    @Test
    @StandardTierHubOnlyTest
    public void fileUploadNotificationReceiverTokenRenewal() throws Exception
    {
        // We remove and recreate the device for a clean start
        RegistryManager registryManager =
                RegistryManager.createFromConnectionString(
                        iotHubConnectionString,
                        RegistryManagerOptions.builder()
                                .httpReadTimeout(HTTP_READ_TIMEOUT)
                                .build());

        ServiceClientOptions serviceClientOptions = ServiceClientOptions.builder().build();
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);

        long tokenLifespanSeconds = 10;
        long tokenLifespanMilliseconds = tokenLifespanSeconds * 1000;
        TokenCredential authenticationTokenProvider = new IotHubConnectionStringCredential(iotHubConnectionString, tokenLifespanSeconds);

        ServiceClient serviceClient = new ServiceClient(
                iotHubConnectionStringObj.getHostName(),
                authenticationTokenProvider,
                TokenCredentialType.SHARED_ACCESS_SIGNATURE,
                testInstance.protocol,
                serviceClientOptions);

        FileUploadNotificationReceiver fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();
        fileUploadNotificationReceiver.open();

        boolean connectionStayedOpenLongerThanTokenExpiryTime = false;
        long loopStartTimeMilliseconds = System.currentTimeMillis();
        while (!connectionStayedOpenLongerThanTokenExpiryTime)
        {
            // Timing this call to fileUploadNotificationReceiver.receive because it will end prematurely if a file
            // upload notification message is received. This test needs to open a connection for longer than the initial
            // token would be valid for in order to test that the AMQP layer is proactively renewing the connection.
            // This loop will run until there is a point in time where there are no file upload notifications to receive
            // so the connection will stay open.

            long startTimeMilliseconds = System.currentTimeMillis();

            // received file upload notifications can be ignored since we no longer have any tests that need to consume them
            fileUploadNotificationReceiver.receive(tokenLifespanMilliseconds * 3);

            long finishTimeMilliseconds = System.currentTimeMillis();

            if (finishTimeMilliseconds - startTimeMilliseconds >= tokenLifespanMilliseconds)
            {
                connectionStayedOpenLongerThanTokenExpiryTime = true;
            }

            if (System.currentTimeMillis() - loopStartTimeMilliseconds >= TOKEN_RENEWAL_TEST_TIMEOUT_MILLISECONDS)
            {
                fail("Timed out waiting for file upload notification receiver to open a connection that lasted longer than the token expiry time");
            }
        }

        fileUploadNotificationReceiver.close();
        serviceClient.close();
        registryManager.close();
    }

    @Test
    @ContinuousIntegrationTest
    public void serviceClientValidatesRemoteCertificateWhenSendingTelemetry() throws IOException
    {
        boolean expectedExceptionWasCaught = false;

        ServiceClient serviceClient = ServiceClient.createFromConnectionString(invalidCertificateServerConnectionString, testInstance.protocol);

        try
        {
            serviceClient.open();
            serviceClient.send(testInstance.deviceId, new Message("some message"));
        }
        catch (IOException e)
        {
            expectedExceptionWasCaught = true;
        }
        catch (Exception e)
        {
            fail(buildExceptionMessage("Expected IOException, but received: " + Tools.getStackTraceFromThrowable(e), hostName));
        }

        assertTrue(buildExceptionMessage("Expected an exception due to service presenting invalid certificate", hostName), expectedExceptionWasCaught);
    }

    @Test
    @ContinuousIntegrationTest
    public void serviceClientValidatesRemoteCertificateWhenGettingFeedbackReceiver() throws IOException
    {
        boolean expectedExceptionWasCaught = false;

        ServiceClient serviceClient = ServiceClient.createFromConnectionString(invalidCertificateServerConnectionString, testInstance.protocol);

        try
        {
            serviceClient.open();
            FeedbackReceiver receiver = serviceClient.getFeedbackReceiver();
            receiver.open();
            receiver.receive(1000);
        }
        catch (IOException e)
        {
            expectedExceptionWasCaught = true;
        }
        catch (Exception e)
        {
            fail(buildExceptionMessage("Expected IOException, but received: " + Tools.getStackTraceFromThrowable(e), hostName));
        }

        assertTrue(buildExceptionMessage("Expected an exception due to service presenting invalid certificate", hostName), expectedExceptionWasCaught);
    }

    @Test
    @ContinuousIntegrationTest
    public void serviceClientValidatesRemoteCertificateWhenGettingFileUploadFeedbackReceiver() throws IOException
    {
        boolean expectedExceptionWasCaught = false;

        ServiceClient serviceClient = ServiceClient.createFromConnectionString(invalidCertificateServerConnectionString, testInstance.protocol);

        try
        {
            serviceClient.open();
            FileUploadNotificationReceiver receiver = serviceClient.getFileUploadNotificationReceiver();
            receiver.open();
            receiver.receive(1000);
        }
        catch (IOException e)
        {
            expectedExceptionWasCaught = true;
        }
        catch (Exception e)
        {
            fail(buildExceptionMessage("Expected IOException, but received: " + Tools.getStackTraceFromThrowable(e), hostName));
        }

        assertTrue(buildExceptionMessage("Expected an exception due to service presenting invalid certificate", hostName), expectedExceptionWasCaught);
    }
}
