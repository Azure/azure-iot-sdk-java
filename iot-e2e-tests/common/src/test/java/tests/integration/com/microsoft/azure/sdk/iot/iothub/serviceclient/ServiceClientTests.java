/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
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
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException;
import lombok.extern.slf4j.Slf4j;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenTools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestDeviceIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/**
 * Test class containing all tests to be run on JVM and android pertaining to C2D communication using the service client.
 */
@Slf4j
@IotHubTest
@RunWith(Parameterized.class)
public class ServiceClientTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";
    protected static String invalidCertificateServerConnectionString = "";
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

        public ServiceClientITRunner(IotHubServiceClientProtocol protocol)
        {
            this.protocol = protocol;
        }
    }

    private final ServiceClientITRunner testInstance;

    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs()
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        invalidCertificateServerConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.UNTRUSTWORTHY_IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));
        hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString).getHostName();

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
    public void cloudToDeviceTelemetryWithAzureSasCredential() throws Exception
    {
        cloudToDeviceTelemetry(false, true, false, true);
    }

    public void cloudToDeviceTelemetry(
            boolean withProxy,
            boolean withPayload,
            boolean withCustomSSLContext,
            boolean withAzureSasCredential) throws Exception
    {
        // We remove and recreate the device for a clean start
        RegistryManager registryManager =
                RegistryManager.createFromConnectionString(
                        iotHubConnectionString,
                        RegistryManagerOptions.builder()
                                .httpReadTimeout(HTTP_READ_TIMEOUT)
                                .build());

        TestDeviceIdentity testDeviceIdentity =
            Tools.getTestDevice(
                iotHubConnectionString,
                IotHubClientProtocol.AMQPS,
                AuthenticationType.SAS,
                false);

        Device device = testDeviceIdentity.getDevice();

        Device deviceGetBefore = registryManager.getDevice(device.getDeviceId());

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
        if (withAzureSasCredential)
        {
            serviceClient = buildServiceClientWithAzureSasCredential(testInstance.protocol, serviceClientOptions);
        }
        else
        {
            serviceClient = new ServiceClient(iotHubConnectionString, testInstance.protocol, serviceClientOptions);
        }

        serviceClient.open();

        Message message;
        if (withPayload)
        {
            message = new Message(content.getBytes(StandardCharsets.UTF_8));
        }
        else
        {
            message = new Message();
        }

        serviceClient.send(device.getDeviceId(), message);

        Device deviceGetAfter = registryManager.getDevice(device.getDeviceId());
        serviceClient.close();

        Tools.disposeTestIdentity(testDeviceIdentity, iotHubConnectionString);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), deviceGetBefore.getDeviceId(), deviceGetAfter.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), 0, deviceGetBefore.getCloudToDeviceMessageCount());
        assertEquals(buildExceptionMessage("", hostName), 1, deviceGetAfter.getCloudToDeviceMessageCount());

        registryManager.close();
    }

    @Test
    @StandardTierHubOnlyTest
    public void serviceClientTokenRenewalWithAzureSasCredential() throws Exception
    {
        RegistryManager registryManager = new RegistryManager(
            iotHubConnectionString,
            RegistryManagerOptions.builder()
                .httpReadTimeout(HTTP_READ_TIMEOUT)
                .build());

        TestDeviceIdentity testDeviceIdentity =
            Tools.getTestDevice(
                iotHubConnectionString,
                IotHubClientProtocol.AMQPS,
                AuthenticationType.SAS,
                false);

        Device device = testDeviceIdentity.getDevice();

        ServiceClient serviceClient;
        IotHubConnectionString iotHubConnectionStringObj =
            IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);

        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential sasCredential = new AzureSasCredential(serviceSasToken.toString());
        serviceClient = new ServiceClient(iotHubConnectionStringObj.getHostName(), sasCredential, testInstance.protocol);
        serviceClient.open();

        Message message = new Message(content.getBytes(StandardCharsets.UTF_8));
        serviceClient.send(device.getDeviceId(), message);

        // deliberately expire the SAS token to provoke a 401 to ensure that the registry manager is using the shared
        // access signature that is set here.
        sasCredential.update(SasTokenTools.makeSasTokenExpired(serviceSasToken.toString()));

        try
        {
            serviceClient.send(device.getDeviceId(), message);
            fail("Expected sending cloud to device message to throw unauthorized exception since an expired SAS token was used, but no exception was thrown");
        }
        catch (IOException e)
        {
            // For service client, the unauthorized exception is wrapped by an IOException, so we need to unwrap it here
            if (e.getCause() instanceof IotHubUnathorizedException)
            {
                log.debug("IotHubUnauthorizedException was thrown as expected, continuing test");
            }
            else
            {
                throw e;
            }
        }

        // Renew the expired shared access signature
        serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        sasCredential.update(serviceSasToken.toString());

        // The final c2d send should succeed since the shared access signature has been renewed
        serviceClient.send(device.getDeviceId(), message);

        serviceClient.close();
        registryManager.close();
        Tools.disposeTestIdentity(testDeviceIdentity, iotHubConnectionString);
    }

    @Test
    @StandardTierHubOnlyTest
    public void feedbackMessageReceiverWithAzureSasCredential() throws Exception
    {
        RegistryManager registryManager =
                RegistryManager.createFromConnectionString(
                        iotHubConnectionString,
                        RegistryManagerOptions.builder()
                                .httpReadTimeout(HTTP_READ_TIMEOUT)
                                .build());

        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);

        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential sasTokenProvider = new AzureSasCredential(serviceSasToken.toString());

        ServiceClient serviceClient = new ServiceClient(
                iotHubConnectionStringObj.getHostName(),
                sasTokenProvider,
                testInstance.protocol);

        FeedbackReceiver feedbackReceiver = serviceClient.getFeedbackReceiver();
        feedbackReceiver.open();

        // received feedback message can be ignored since we no longer have any tests that need to consume them
        // All this test cares about is that this API doesn't result in an unauthorized exception
        feedbackReceiver.receive(2 * 1000);

        feedbackReceiver.close();
        serviceClient.close();
        registryManager.close();
    }

    @Test
    @StandardTierHubOnlyTest
    public void fileUploadNotificationReceiverWithAzureSasCredential() throws Exception
    {
        RegistryManager registryManager =
                RegistryManager.createFromConnectionString(
                        iotHubConnectionString,
                        RegistryManagerOptions.builder()
                                .httpReadTimeout(HTTP_READ_TIMEOUT)
                                .build());

        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);

        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential sasTokenProvider = new AzureSasCredential(serviceSasToken.toString());

        ServiceClient serviceClient = new ServiceClient(
                iotHubConnectionStringObj.getHostName(),
                sasTokenProvider,
                testInstance.protocol);

        FileUploadNotificationReceiver fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();
        fileUploadNotificationReceiver.open();

        // received file upload notifications can be ignored since we no longer have any tests that need to consume them
        // All this test cares about is that this API doesn't result in an unauthorized exception
        fileUploadNotificationReceiver.receive(2 * 1000);

        fileUploadNotificationReceiver.close();
        serviceClient.close();
        registryManager.close();
    }

    @Ignore // The IoT Hub instance we use for this test is currently offline, so this test cannot be run
    @Test
    @ContinuousIntegrationTest
    public void serviceClientValidatesRemoteCertificateWhenSendingTelemetry() throws IOException
    {
        boolean expectedExceptionWasCaught = false;

        ServiceClient serviceClient = ServiceClient.createFromConnectionString(invalidCertificateServerConnectionString, testInstance.protocol);

        try
        {
            serviceClient.open();
            // don't need a real device Id since the request is sent to a fake service
            serviceClient.send("some deviceId", new Message("some message"));
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

    @Ignore // The IoT Hub instance we use for this test is currently offline, so this test cannot be run
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

    @Ignore // The IoT Hub instance we use for this test is currently offline, so this test cannot be run
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

    private static ServiceClient buildServiceClientWithAzureSasCredential(IotHubServiceClientProtocol protocol, ServiceClientOptions options)
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential azureSasCredential = new AzureSasCredential(serviceSasToken.toString());
        return new ServiceClient(iotHubConnectionStringObj.getHostName(), azureSasCredential, protocol, options);
    }
}
