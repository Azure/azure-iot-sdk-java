/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.telemetry;


import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.SendMessagesCommon;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

    @Test
    public void sendMessagesWithCustomSasTokenProvider() throws Exception
    {
        if (testInstance.authenticationType != SAS)
        {
            // SAS token provider can't be used for x509 auth
            return;
        }
        
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
        if (this.testInstance.protocol != HTTPS)
        {
            return;
        }

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
        this.testInstance.identity.getClient().open();
        Message msg = new Message("asdf");

        //All of these characters should be allowed within application properties
        msg.setProperty("TestKey1234!#$%&'*+-^_`|~", "TestValue1234!#$%&'*+-^_`|~()<>@,;:\\\"[]?={} \t");
        // ()<>@,;:\"[]?={}
        IotHubServicesCommon.sendMessageAndWaitForResponse(this.testInstance.identity.getClient(), new MessageAndResult(msg, IotHubStatusCode.OK_EMPTY), RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.protocol);
        this.testInstance.identity.getClient().closeNow();
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

        String soonToBeExpiredSASToken = generateSasTokenForIotDevice(hostName, testInstance.identity.getDeviceId(), ((TestDeviceIdentity)testInstance.identity).getDevice().getPrimaryKey(), SECONDS_FOR_SAS_TOKEN_TO_LIVE);
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

        IotHubServicesCommon.sendExpiredMessageExpectingMessageExpiredCallback(testInstance.identity.getClient(), testInstance.protocol, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.authenticationType);
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

        IotHubServicesCommon.sendMessages(testInstance.identity.getClient(), testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }
}
