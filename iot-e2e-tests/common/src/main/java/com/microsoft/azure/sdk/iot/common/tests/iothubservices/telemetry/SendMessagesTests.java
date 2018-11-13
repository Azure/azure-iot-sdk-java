/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.telemetry;

import com.microsoft.azure.sdk.iot.common.helpers.EventCallback;
import com.microsoft.azure.sdk.iot.common.helpers.IotHubServicesCommon;
import com.microsoft.azure.sdk.iot.common.helpers.Success;
import com.microsoft.azure.sdk.iot.common.setup.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.common.helpers.SasTokenGenerator.generateSasTokenForIotDevice;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.*;
import static junit.framework.TestCase.fail;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to sending messages from a device/module. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class SendMessagesTests extends SendMessagesCommon
{
    public SendMessagesTests(InternalClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, String clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(client, protocol, identity, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void sendMessages() throws IOException, InterruptedException
    {

        if (testInstance.protocol == MQTT_WS && (testInstance.authenticationType == SELF_SIGNED || testInstance.authenticationType == CERTIFICATE_AUTHORITY))
        {
            //mqtt_ws does not support x509 auth currently
            return;
        }

        IotHubServicesCommon.sendMessages(testInstance.client, testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void tokenRenewalWorks() throws InterruptedException
    {
        if (testInstance.authenticationType != SAS)
        {
            //this scenario is not applicable for x509 auth
            return;
        }

        //set it so a newly generated sas token only lasts for a small amount of time
        testInstance.client.setOption("SetSASTokenExpiryTime", SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL);
        IotHubServicesCommon.openClientWithRetry(testInstance.client);

        for (int messageAttempt = 0; messageAttempt < NUM_MESSAGES_PER_CONNECTION; messageAttempt++)
        {
            //wait until old sas token has expired, this should force the config to generate a new one from the device key
            Thread.sleep(SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL * 1000);

            Success messageSent = new Success();
            EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
            testInstance.client.sendEventAsync(new Message("some message body"), callback, messageSent);

            long startTime = System.currentTimeMillis();
            while(!messageSent.wasCallbackFired())
            {
                Thread.sleep(RETRY_MILLISECONDS);
                if (System.currentTimeMillis() - startTime > SEND_TIMEOUT_MILLISECONDS)
                {
                    fail("Timed out waiting for successful message callback");
                }
            }

            if (messageSent.getCallbackStatusCode() != IotHubStatusCode.OK_EMPTY)
            {
                fail("Sending messages over " + testInstance.protocol + " failed: expected OK_EMPTY message callback but received " + messageSent.getCallbackStatusCode());
            }
        }
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void sendMessagesOverAmqpsMultithreaded() throws InterruptedException, IOException, IotHubException
    {
        if (!(testInstance.protocol == AMQPS && testInstance.authenticationType == SAS))
        {
            //this test only applicable for AMQPS with SAS auth
            return;
        }

        Device[] deviceListAmqps = new Device[MAX_DEVICE_PARALLEL];
        String uuid = UUID.randomUUID().toString();
        for (int i = 0; i < MAX_DEVICE_PARALLEL; i++)
        {
            String deviceIdAmqps = "java-device-client-e2e-test-amqps".concat(i + "-" + uuid);
            deviceListAmqps[i] = Device.createFromId(deviceIdAmqps, null, null);
            registryManager.addDevice(deviceListAmqps[i]);
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
                            cdl));
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
            Assert.fail("Sending message over AMQP protocol in parallel failed");
        }
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void tokenExpiredAfterOpenButBeforeSendHttp() throws InvalidKeyException, IOException, InterruptedException, URISyntaxException
    {
        if (testInstance.protocol != HTTPS || testInstance.authenticationType != SAS)
        {
            //This scenario only applies to HTTP since MQTT and AMQP allow expired sas tokens for 30 minutes after open
            // as long as token did not expire before open. X509 doesn't apply either
            return;
        }

        String soonToBeExpiredSASToken = generateSasTokenForIotDevice(hostName, testInstance.identity.getDeviceId(), testInstance.identity.getPrimaryKey(), SECONDS_FOR_SAS_TOKEN_TO_LIVE);
        DeviceClient client = new DeviceClient(soonToBeExpiredSASToken, testInstance.protocol);
        IotHubServicesCommon.openClientWithRetry(client);

        //Force the SAS token to expire before sending messages
        Thread.sleep(MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE);
        IotHubServicesCommon.sendMessagesExpectingSASTokenExpiration(client, testInstance.protocol.toString(), 1, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.authenticationType);
        client.closeNow();
    }

    @Test (timeout = DEFAULT_TEST_TIMEOUT)
    public void expiredMessagesAreNotSent() throws IOException
    {
        IotHubServicesCommon.sendExpiredMessageExpectingMessageExpiredCallback(testInstance.client, testInstance.protocol, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.authenticationType);
    }
}
