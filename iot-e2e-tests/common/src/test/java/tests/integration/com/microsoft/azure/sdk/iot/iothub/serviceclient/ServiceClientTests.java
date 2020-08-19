/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.service.*;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
    private static String deviceIdPrefix = "java-service-client-e2e-test";
    private static String content = "abcdefghijklmnopqrstuvwxyz1234567890";
    private static String hostName;

    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8869;

    private static final long MAX_TEST_MILLISECONDS = 1 * 60 * 1000;

    public ServiceClientTests(IotHubServiceClientProtocol protocol)
    {
        this.testInstance = new ServiceClientITRunner(protocol);
    }

    private class ServiceClientITRunner
    {
        private IotHubServiceClientProtocol protocol;
        private String deviceId;

        public ServiceClientITRunner(IotHubServiceClientProtocol protocol)
        {
            this.protocol = protocol;
            this.deviceId = deviceIdPrefix.concat("-" + UUID.randomUUID().toString());

        }
    }

    private ServiceClientITRunner testInstance;

    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs() throws IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        invalidCertificateServerConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.UNTRUSTWORTHY_IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();

        List inputs = Arrays.asList(
                new Object[][]
                        {
                                {IotHubServiceClientProtocol.AMQPS},
                                {IotHubServiceClientProtocol.AMQPS_WS}
                        }
        );

        return inputs;
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
        cloudToDeviceTelemetry(false);
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

        cloudToDeviceTelemetry(true);
    }

    public void cloudToDeviceTelemetry(boolean withProxy) throws Exception
    {
        // Arrange

        // We remove and recreate the device for a clean start
        RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());

        Device deviceAdded = Device.createFromId(testInstance.deviceId, null, null);
        Tools.addDeviceWithRetry(registryManager, deviceAdded);

        Device deviceGetBefore = registryManager.getDevice(testInstance.deviceId);

        // Act

        // Create service client
        ProxyOptions proxyOptions = null;
        if (withProxy)
        {
            Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
            proxyOptions = new ProxyOptions(testProxy);
        }

        ServiceClientOptions serviceClientOptions = ServiceClientOptions.builder().proxyOptions(proxyOptions).build();

        ServiceClient serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, testInstance.protocol, serviceClientOptions);
        CompletableFuture<Void> futureOpen = serviceClient.openAsync();
        futureOpen.get();

        Message message = new Message(content.getBytes());

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
