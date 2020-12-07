/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry;


import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.SendMessagesCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static junit.framework.TestCase.fail;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenGenerator.generateSasTokenForIotDevice;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to sending messages from a device/module.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class SendMessagesTests extends SendMessagesCommon
{
    public SendMessagesTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint, boolean withProxy) throws Exception
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint, withProxy);
    }

    //TODO this test doesn't seem to check anything that the basic sendMessages test already checks. It just has a different payload. Needs
    // to check that something actually happened downstream because it was a security message
    @Ignore
    @Test
    public void sendSecurityMessages() throws Exception
    {
        this.testInstance.setup();

        IotHubServicesCommon.sendSecurityMessages(testInstance.client, testInstance.protocol, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, null);
    }

    @Test
    public void sendMessages() throws Exception
    {
        this.testInstance.setup();

        IotHubServicesCommon.sendMessages(testInstance.client, testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    @Test
    public void sendMessagesWithCustomSasTokenProvider() throws Exception
    {
        this.testInstance.setup(true);

        IotHubServicesCommon.sendMessages(testInstance.client, testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    @Test
    public void sendBulkMessages() throws Exception
    {
        this.testInstance.setup();

        IotHubServicesCommon.sendBulkMessages(testInstance.client, testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendManySmallMessagesAsBatch() throws Exception
    {
        // Only send batch messages in large quantities when using HTTPS protocol.
        if (this.testInstance.protocol != HTTPS)
        {
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendBulkMessages(testInstance.client, testInstance.protocol, MULTIPLE_SMALL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    @Test
    @ContinuousIntegrationTest
    public void sendLargestMessages() throws Exception
    {
        this.testInstance.setup();

        if (this.testInstance.protocol == AMQPS_WS)
        {
            // AMQPS_WS still has a bug that limits message size to 16 kb. All other protocols can do 256 kb
            IotHubServicesCommon.sendMessages(testInstance.client, testInstance.protocol, LARGE_MESSAGES_TO_SEND_AMQPS_WS, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
        }
        else
        {
            IotHubServicesCommon.sendMessages(testInstance.client, testInstance.protocol, LARGE_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
        }
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesWithUnusualApplicationProperties() throws Exception
    {
        this.testInstance.setup();
        this.testInstance.client.open();
        Message msg = new Message("asdf");

        //All of these characters should be allowed within application properties
        msg.setProperty("TestKey1234!#$%&'*+-^_`|~", "TestValue1234!#$%&'*+-^_`|~()<>@,;:\\\"[]?={} \t");
        // ()<>@,;:\"[]?={}
        IotHubServicesCommon.sendMessageAndWaitForResponse(this.testInstance.client, new MessageAndResult(msg, IotHubStatusCode.OK_EMPTY), RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.protocol);
        this.testInstance.client.closeNow();
    }

    @Test
    @ContinuousIntegrationTest
    public void sendMessagesOverAmqpsMultithreaded() throws InterruptedException, IOException, IotHubException
    {
        if (!(testInstance.protocol == AMQPS && testInstance.authenticationType == SAS && testInstance.clientType.equals(ClientType.DEVICE_CLIENT)))
        {
            //this test only applicable for AMQPS with SAS auth using device client
            return;
        }

        AtomicBoolean succeed = new AtomicBoolean();
        Device[] deviceListAmqps = new Device[MAX_DEVICE_PARALLEL];
        Collection<String> deviceIds = new ArrayList<>();
        String uuid = UUID.randomUUID().toString();
        for (int i = 0; i < MAX_DEVICE_PARALLEL; i++)
        {
            String deviceIdAmqps = "java-device-client-e2e-test-amqps".concat(i + "-" + uuid);
            deviceIds.add(deviceIdAmqps);
            deviceListAmqps[i] = Device.createFromId(deviceIdAmqps, null, null);
            Tools.addDeviceWithRetry(registryManager, deviceListAmqps[i]);
        }

        List<Thread> threads = new ArrayList<>(deviceListAmqps.length);
        CountDownLatch cdl = new CountDownLatch(deviceListAmqps.length);

        for(Device deviceAmqps: deviceListAmqps)
        {
            Thread thread = new Thread(
                    new testDevice(
                            deviceAmqps,
                            AMQPS,
                            NUM_CONNECTIONS_PER_DEVICE,
                            NUM_MESSAGES_PER_CONNECTION,
                            NUM_KEYS_PER_MESSAGE,
                            SEND_TIMEOUT_MILLISECONDS,
                            cdl,
                            succeed));
            thread.start();
            threads.add(thread);
        }

        cdl.await(1, TimeUnit.MINUTES);

        for (int i = 0; i < MAX_DEVICE_PARALLEL; i++)
        {
            registryManager.removeDevice(deviceListAmqps[i].getDeviceId());
        }

        if(!succeed.get())
        {
            fail(CorrelationDetailsLoggingAssert.buildExceptionMessage("Sending message over AMQP protocol in parallel failed", deviceIds, "amqp", hostName, new ArrayList<>()));
        }
    }

    @Test
    @ContinuousIntegrationTest
    public void tokenExpiredAfterOpenButBeforeSendHttp() throws Exception
    {
        final long SECONDS_FOR_SAS_TOKEN_TO_LIVE = 3;
        final long MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE = 5000;

        if (testInstance.protocol != HTTPS || testInstance.authenticationType != SAS || testInstance.useHttpProxy)
        {
            //This scenario only applies to HTTP since MQTT and AMQP allow expired sas tokens for 30 minutes after open
            // as long as token did not expire before open. X509 doesn't apply either
            return;
        }

        this.testInstance.setup();

        String soonToBeExpiredSASToken = generateSasTokenForIotDevice(hostName, testInstance.identity.getDeviceId(), testInstance.identity.getPrimaryKey(), SECONDS_FOR_SAS_TOKEN_TO_LIVE);
        DeviceClient client = new DeviceClient(soonToBeExpiredSASToken, testInstance.protocol);
        client.open();

        //Force the SAS token to expire before sending messages
        Thread.sleep(MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE);
        IotHubServicesCommon.sendMessagesExpectingSASTokenExpiration(client, testInstance.protocol.toString(), 1, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.authenticationType);
        client.closeNow();
    }

    @Test
    @ContinuousIntegrationTest
    public void expiredMessagesAreNotSent() throws Exception
    {
        if (testInstance.useHttpProxy)
        {
            //Not worth testing
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendExpiredMessageExpectingMessageExpiredCallback(testInstance.client, testInstance.protocol, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.authenticationType);
    }

    @Test
    public void sendMessagesWithCustomSSLContextAndSasAuth() throws Exception
    {
        if (testInstance.authenticationType != SAS)
        {
            //only testing sas based auth with custom ssl context here
            return;
        }

        this.testInstance.setup(SSLContextBuilder.buildSSLContext());

        IotHubServicesCommon.sendMessages(testInstance.client, testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }
}
