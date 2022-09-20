/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry;


import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IotHubServicesCommon;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.MessageAndResult;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SSLContextBuilder;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Success;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.X509CertificateGenerator;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.SendMessagesCommon;

import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static junit.framework.TestCase.*;
import static org.junit.Assume.assumeFalse;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to sending messages from a device/module.
 */
@IotHubTest
@Slf4j
@RunWith(Parameterized.class)
public class SendMessagesTests extends SendMessagesCommon
{
    public SendMessagesTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, boolean withProxy) throws Exception
    {
        super(protocol, authenticationType, clientType, withProxy);
    }

    @Test
    public void sendMessages() throws Exception
    {
        this.testInstance.setup();

        IotHubServicesCommon.sendMessages(testInstance.identity.getClient(), testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    // Ensure that a user can use a device/module client as soon as the connection status callback executes with status CONNECTED
    // even from the callback itself
    @Test
    public void sendMessagesFromConnectionStatusChangeCallback() throws Exception
    {
        Success messageSent = new Success();
        messageSent.setResult(false);
        testInstance.setup();
        testInstance.identity.getClient().setConnectionStatusChangeCallback((context) ->
        {
            if (context.getNewStatus() == IotHubConnectionStatus.CONNECTED)
            {
                try
                {
                    testInstance.identity.getClient().sendEventAsync(
                        new Message("test message"),
                        (responseStatus, exception, callbackContext1) ->
                        {
                            ((Success) callbackContext1).setResult(true);
                            ((Success) callbackContext1).setCallbackStatusCode(exception == null ? IotHubStatusCode.OK : exception.getStatusCode());
                            ((Success) callbackContext1).callbackWasFired();
                        },
                        messageSent);
                }
                catch (Exception e)
                {
                    log.error("Encountered an error when sending the message", e);
                    messageSent.setResult(false);
                    messageSent.setCallbackStatusCode(IotHubStatusCode.ERROR);
                    messageSent.callbackWasFired();
                }
            }
        }, null);

        testInstance.identity.getClient().open(false);

        long startTime = System.currentTimeMillis();
        while (!messageSent.wasCallbackFired())
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > SEND_TIMEOUT_MILLISECONDS)
            {
                fail("Timed out waiting for sent message to be acknowledged");
            }
        }

        assertTrue(messageSent.getResult());
        testInstance.identity.getClient().close();
    }

    @Test
    public void openClientWithRetry() throws Exception
    {
        this.testInstance.setup();
        this.testInstance.identity.getClient().open(true);
        this.testInstance.identity.getClient().close();
    }

    @Test
    public void sendMessagesWithCustomSasTokenProvider() throws Exception
    {
        Assume.assumeTrue(testInstance.authenticationType == SAS);

        this.testInstance.setup(true);

        IotHubServicesCommon.sendMessages(testInstance.identity.getClient(), testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    @Test
    public void sendBulkMessages() throws Exception
    {
        this.testInstance.setup();

        IotHubServicesCommon.sendBulkMessages(testInstance.identity.getClient(), testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendManySmallMessagesAsBatch() throws Exception
    {
        // Only send batch messages in large quantities when using HTTPS protocol.
        assumeFalse(this.testInstance.protocol != HTTPS);

        this.testInstance.setup();

        IotHubServicesCommon.sendBulkMessages(testInstance.identity.getClient(), testInstance.protocol, MULTIPLE_SMALL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendLargestMessages() throws Exception
    {
        this.testInstance.setup();

        IotHubServicesCommon.sendMessages(testInstance.identity.getClient(), testInstance.protocol, LARGE_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesWithUnusualApplicationProperties() throws Exception
    {
        this.testInstance.setup();
        this.testInstance.identity.getClient().open(false);
        Message msg = new Message("asdf");

        //All of these characters should be allowed within application properties
        msg.setProperty("TestKey1234!#$%&'*+-^_`|~", "TestValue1234!#$%&'*+-^_`|~()<>@,;:\\\"[]?={} \t");
        // ()<>@,;:\"[]?={}
        IotHubServicesCommon.sendMessageAndWaitForResponse(this.testInstance.identity.getClient(), new MessageAndResult(msg, IotHubStatusCode.OK), testInstance.protocol);
        this.testInstance.identity.getClient().close();
    }

    @Test
    @ContinuousIntegrationTest
    public void expiredMessagesAreNotSent() throws Exception
    {
        // Not worth testing for both w/ and w/o proxy
        assumeFalse(testInstance.useHttpProxy);

        this.testInstance.setup();

        IotHubServicesCommon.sendExpiredMessageExpectingMessageExpiredCallback(testInstance.identity.getClient(), testInstance.protocol, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesWithCustomSSLContextAndSasAuth() throws Exception
    {
        //only testing sas based auth with custom ssl context here
        assumeFalse(testInstance.authenticationType != SAS);

        this.testInstance.setup(SSLContextBuilder.buildSSLContext());

        IotHubServicesCommon.sendMessages(testInstance.identity.getClient(), testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    @ContinuousIntegrationTest
    @Test
    public void sendMessagesWithECCCertificate() throws GeneralSecurityException, IOException, IotHubException, URISyntaxException, InterruptedException, IotHubClientException
    {
        // test is only applicable for self-signed device clients
        assumeFalse(testInstance.authenticationType != AuthenticationType.selfSigned || testInstance.clientType != ClientType.DEVICE_CLIENT);

        // ECC cert generation is broken for Android. "ECDSA KeyPairGenerator is not available"
        assumeFalse(Tools.isAndroid());

        X509CertificateGenerator eccCertGenerator =
            new X509CertificateGenerator(X509CertificateGenerator.CertificateAlgorithm.ECC);

        SSLContext sslContext = SSLContextBuilder.buildSSLContext(eccCertGenerator.getX509Certificate(), eccCertGenerator.getPrivateKey());

        Device eccDevice = new Device(UUID.randomUUID().toString(), AuthenticationType.selfSigned);
        eccDevice.setThumbprint(eccCertGenerator.getX509Thumbprint(), eccCertGenerator.getX509Thumbprint());
        eccDevice = registryClient.addDevice(eccDevice);

        ClientOptions clientOptions = ClientOptions.builder().sslContext(sslContext).build();
        DeviceClient deviceClient = new DeviceClient(Tools.getDeviceConnectionString(iotHubConnectionString, eccDevice), testInstance.protocol, clientOptions);

        deviceClient.open(false);
        deviceClient.close();
    }

    @Test
    @ContinuousIntegrationTest
    public void sendTooLargeMessage() throws Exception
    {
        // The service responds to a excessively large message by killing the TCP connection when connecting over MQTT
        // and doesn't have a mechanism for providing an error code, so this scenario can't be tested over MQTT
        assumeFalse(testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS);

        this.testInstance.setup();

        this.testInstance.identity.getClient().open(true);

        try
        {
            this.testInstance.identity.getClient().sendEvent(new Message(new byte[MAX_MESSAGE_PAYLOAD_SIZE + 10000]));
            fail("Expected client to throw an exception for sending a message that was too large");
        }
        catch (IotHubClientException e)
        {
            assertEquals(IotHubStatusCode.REQUEST_ENTITY_TOO_LARGE, e.getStatusCode());
        }
    }
}
