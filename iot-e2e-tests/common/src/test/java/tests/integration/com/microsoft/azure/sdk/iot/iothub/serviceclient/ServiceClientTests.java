/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.CbsAuthorizationType;
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
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import reactor.core.publisher.Mono;
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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
        cloudToDeviceTelemetry(false, true, false, false);
    }

    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithCustomSSLContext() throws Exception
    {
        cloudToDeviceTelemetry(false, true, true, false);
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

        cloudToDeviceTelemetry(true, true, false, false);
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

        cloudToDeviceTelemetry(true, true, true, false);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void cloudToDeviceTelemetryWithNoPayload() throws Exception
    {
        cloudToDeviceTelemetry(false, false, false, false);
    }

    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithTokenCredential() throws Exception
    {
        cloudToDeviceTelemetry(false, true, false, true);
    }

    public void cloudToDeviceTelemetry(boolean withProxy, boolean withPayload, boolean withCustomSSLContext, boolean withTokenCredential) throws Exception
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

        SSLContext sslContext = null;
        if (withCustomSSLContext)
        {
            sslContext = new IotHubSSLContext().getSSLContext();
        }

        ServiceClientOptions serviceClientOptions = ServiceClientOptions.builder().proxyOptions(proxyOptions).sslContext(sslContext).build();
        ServiceClient serviceClient;
        if (withTokenCredential)
        {
            IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString);
            IotHubServiceSasToken iotHubServiceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);

            TokenCredential authenticationTokenProvider = tokenRequestContext ->
            {
                // generates new token each time, probably optimize this later
                OffsetDateTime sasTokenExpiryOffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(iotHubServiceSasToken.getExpiryTimeMillis()), ZoneId.systemDefault());
                AccessToken accessToken = new AccessToken(iotHubServiceSasToken.toString(), sasTokenExpiryOffsetDateTime);
                return Mono.just(accessToken);
            };

            serviceClient = ServiceClient.createFromTokenCredential(iotHubConnectionStringObj.getHostName(), authenticationTokenProvider, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, testInstance.protocol, serviceClientOptions);
        }
        else
        {
            serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, testInstance.protocol, serviceClientOptions);
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
