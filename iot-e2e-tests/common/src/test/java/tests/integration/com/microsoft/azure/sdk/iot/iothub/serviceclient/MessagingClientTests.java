/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.azure.core.credential.AzureSasCredential;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubMessageTooLargeException;
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClient;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClientOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.HttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.impl.DefaultHttpProxyServer;
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
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/**
 * Test class containing all tests to be run on JVM and android pertaining to C2D communication using the service client.
 */
@Slf4j
@IotHubTest
@RunWith(Parameterized.class)
public class MessagingClientTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";
    protected static String invalidCertificateServerConnectionString = "";
    private static final byte[] SMALL_PAYLOAD = new byte[1024];
    private static final byte[] LARGEST_PAYLOAD = new byte[65000]; // IoT Hub allows a max of 65 kb per message
    private static final byte[] TOO_LARGE_PAYLOAD = new byte[256000]; // IoT Hub allows a max of 65 kb per message
    private static String hostName;

    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8869;

    public MessagingClientTests(IotHubServiceClientProtocol protocol)
    {
        this.testInstance = new MessagingClientITRunner(protocol);
    }

    private static class MessagingClientITRunner
    {
        private final IotHubServiceClientProtocol protocol;

        public MessagingClientITRunner(IotHubServiceClientProtocol protocol)
        {
            this.protocol = protocol;
        }
    }

    private final MessagingClientITRunner testInstance;

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
        cloudToDeviceTelemetry(false, true, false, false, false);
    }

    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithCustomSSLContext() throws Exception
    {
        cloudToDeviceTelemetry(false, true, false, true, false);
    }

    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithProxy() throws Exception
    {
        //Proxy support only exists for AMQPS_WS currently
        assumeTrue(testInstance.protocol == IotHubServiceClientProtocol.AMQPS_WS);

        cloudToDeviceTelemetry(true, true, false, false, false);
    }

    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithProxyAndCustomSSLContext() throws Exception
    {
        //Proxy support only exists for AMQPS_WS currently
        assumeTrue(testInstance.protocol == IotHubServiceClientProtocol.AMQPS_WS);

        cloudToDeviceTelemetry(true, true, false, true, false);
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
    public void cloudToDeviceTelemetryWithAzureSasCredential() throws Exception
    {
        cloudToDeviceTelemetry(false, true, false, false, true);
    }

    @Test
    @ContinuousIntegrationTest
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithMaxPayloadSize() throws Exception
    {
        cloudToDeviceTelemetry(false, true, true, false, false);
    }

    @Test
    @ContinuousIntegrationTest
    @StandardTierHubOnlyTest
    public void cloudToDeviceTelemetryWithMaxPayloadSizeAndProxy() throws Exception
    {
        //Proxy support only exists for AMQPS_WS currently
        assumeTrue(testInstance.protocol == IotHubServiceClientProtocol.AMQPS_WS);

        cloudToDeviceTelemetry(true, true, true, false, false);
    }

    public void cloudToDeviceTelemetry(
            boolean withProxy,
            boolean withPayload,
            boolean withLargestPayload,
            boolean withCustomSSLContext,
            boolean withAzureSasCredential) throws Exception
    {
        // We remove and recreate the device for a clean start
        RegistryClient registryClient =
                new RegistryClient(
                        iotHubConnectionString,
                        RegistryClientOptions.builder()
                                .httpReadTimeoutSeconds(HTTP_READ_TIMEOUT)
                                .build());

        TestDeviceIdentity testDeviceIdentity =
            Tools.getTestDevice(
                iotHubConnectionString,
                IotHubClientProtocol.AMQPS,
                AuthenticationType.SAS,
                false);

        Device device = testDeviceIdentity.getDevice();

        Device deviceGetBefore = registryClient.getDevice(device.getDeviceId());

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

        MessagingClientOptions messagingClientOptions =
                MessagingClientOptions.builder()
                        .proxyOptions(proxyOptions)
                        .sslContext(sslContext)
                        .build();

        MessagingClient messagingClient;
        if (withAzureSasCredential)
        {
            messagingClient = buildMessagingClientWithAzureSasCredential(testInstance.protocol, messagingClientOptions);
        }
        else
        {
            messagingClient = new MessagingClient(iotHubConnectionString, testInstance.protocol, messagingClientOptions);
        }

        messagingClient.open();

        Message message;
        if (withPayload)
        {
            if (withLargestPayload)
            {
                message = new Message(LARGEST_PAYLOAD);
            }
            else
            {
                message = new Message(SMALL_PAYLOAD);
            }
        }
        else
        {
            message = new Message();
        }

        messagingClient.send(device.getDeviceId(), message);

        messagingClient.close();

        Device deviceGetAfter = registryClient.getDevice(device.getDeviceId());

        Tools.disposeTestIdentity(testDeviceIdentity, iotHubConnectionString);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), deviceGetBefore.getDeviceId(), deviceGetAfter.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), 0, deviceGetBefore.getCloudToDeviceMessageCount());
        assertEquals(buildExceptionMessage("", hostName), 1, deviceGetAfter.getCloudToDeviceMessageCount());
    }

    @Test
    @StandardTierHubOnlyTest
    public void messagingClientTokenRenewalWithAzureSasCredential() throws Exception
    {
        TestDeviceIdentity testDeviceIdentity =
            Tools.getTestDevice(
                iotHubConnectionString,
                IotHubClientProtocol.AMQPS,
                AuthenticationType.SAS,
                false);

        Device device = testDeviceIdentity.getDevice();

        MessagingClient messagingClient;
        IotHubConnectionString iotHubConnectionStringObj =
            IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);

        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential sasCredential = new AzureSasCredential(serviceSasToken.toString());
        messagingClient = new MessagingClient(iotHubConnectionStringObj.getHostName(), sasCredential, testInstance.protocol);
        messagingClient.open();

        Message message = new Message(SMALL_PAYLOAD);
        messagingClient.send(device.getDeviceId(), message);

        // deliberately expire the SAS token to provoke a 401 to ensure that the registry manager is using the shared
        // access signature that is set here.
        sasCredential.update(SasTokenTools.makeSasTokenExpired(serviceSasToken.toString()));

        messagingClient.close();

        try
        {
            messagingClient.open();
            fail("Expected opening messagingClient to throw unauthorized exception since an expired SAS token was used, but no exception was thrown");
        }
        catch (IotHubUnauthorizedException e)
        {
            log.debug("IotHubUnauthorizedException was thrown as expected, continuing test");
        }

        // Renew the expired shared access signature
        serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        sasCredential.update(serviceSasToken.toString());

        // The final c2d send should succeed since the shared access signature has been renewed
        messagingClient.open();
        messagingClient.send(device.getDeviceId(), message);

        messagingClient.close();

        Tools.disposeTestIdentity(testDeviceIdentity, iotHubConnectionString);
    }

    @Test
    @ContinuousIntegrationTest
    public void messagingClientValidatesRemoteCertificateWhenSendingTelemetry() throws InterruptedException
    {
        boolean expectedExceptionWasCaught = false;

        MessagingClient messagingClient = new MessagingClient(invalidCertificateServerConnectionString, testInstance.protocol);

        try
        {
            messagingClient.open();
        }
        catch (IOException e)
        {
            expectedExceptionWasCaught = true;
        }
        catch (Exception e)
        {
            fail(buildExceptionMessage("Expected IOException, but received: " + Tools.getStackTraceFromThrowable(e), hostName));
        }
        finally
        {
            messagingClient.close();
        }

        assertTrue(buildExceptionMessage("Expected an exception due to service presenting invalid certificate", hostName), expectedExceptionWasCaught);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void cloudToDeviceTelemetryWithTooLargeMessageThrows() throws IOException, GeneralSecurityException, IotHubException, URISyntaxException, TimeoutException, InterruptedException
    {
        // We remove and recreate the device for a clean start
        RegistryClient registryClient =
            new RegistryClient(
                iotHubConnectionString,
                RegistryClientOptions.builder()
                    .httpReadTimeoutSeconds(HTTP_READ_TIMEOUT)
                    .build());

        TestDeviceIdentity testDeviceIdentity =
            Tools.getTestDevice(
                iotHubConnectionString,
                IotHubClientProtocol.AMQPS,
                AuthenticationType.SAS,
                false);

        Device device = testDeviceIdentity.getDevice();

        MessagingClientOptions messagingClientOptions =
            MessagingClientOptions.builder()
                .build();

        MessagingClient messagingClient = new MessagingClient(iotHubConnectionString, testInstance.protocol, messagingClientOptions);

        messagingClient.open();

        Message message = new Message(TOO_LARGE_PAYLOAD);

        try
        {
            messagingClient.send(device.getDeviceId(), message);
            fail("Expected an exception to be thrown for sending a message with too large of a payload");
        }
        catch (IotHubMessageTooLargeException e)
        {
            // expected error thrown, ignore it.
        }

        messagingClient.close();

        Tools.disposeTestIdentity(testDeviceIdentity, iotHubConnectionString);
    }

    private static MessagingClient buildMessagingClientWithAzureSasCredential(IotHubServiceClientProtocol protocol, MessagingClientOptions options)
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential azureSasCredential = new AzureSasCredential(serviceSasToken.toString());
        return new MessagingClient(iotHubConnectionStringObj.getHostName(), azureSasCredential, protocol, options);
    }
}
