/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.telemetry;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.common.setup.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.azure.sdk.iot.common.helpers.SasTokenGenerator.generateSasTokenForIotDevice;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.*;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

/**
 * Test class containing all non error injection tests to be run on JVM and android pertaining to sending messages from a device/module. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class SendMessagesTests extends SendMessagesCommon
{
    public SendMessagesTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint) throws Exception
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    @Test
    public void sendMessages() throws Exception
    {
        if (testInstance.protocol == MQTT_WS && (testInstance.authenticationType == SELF_SIGNED || testInstance.authenticationType == CERTIFICATE_AUTHORITY))
        {
            //mqtt_ws does not support x509 auth currently
            return;
        }

        this.testInstance.setup();

        IotHubServicesCommon.sendMessages(testInstance.client, testInstance.protocol, NORMAL_MESSAGES_TO_SEND, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, 0, null);
    }

    @Test
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
    public void tokenExpiredAfterOpenButBeforeSendHttp() throws Exception
    {
        final long SECONDS_FOR_SAS_TOKEN_TO_LIVE = 3;
        final long MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE = 5000;

        if (testInstance.protocol != HTTPS || testInstance.authenticationType != SAS)
        {
            //This scenario only applies to HTTP since MQTT and AMQP allow expired sas tokens for 30 minutes after open
            // as long as token did not expire before open. X509 doesn't apply either
            return;
        }

        this.testInstance.setup();

        String soonToBeExpiredSASToken = generateSasTokenForIotDevice(hostName, testInstance.identity.getDeviceId(), testInstance.identity.getPrimaryKey(), SECONDS_FOR_SAS_TOKEN_TO_LIVE);
        DeviceClient client = new DeviceClient(soonToBeExpiredSASToken, testInstance.protocol);
        IotHubServicesCommon.openClientWithRetry(client);

        //Force the SAS token to expire before sending messages
        Thread.sleep(MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE);
        IotHubServicesCommon.sendMessagesExpectingSASTokenExpiration(client, testInstance.protocol.toString(), 1, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.authenticationType);
        client.closeNow();
    }

    @Test
    public void expiredMessagesAreNotSent() throws Exception
    {
        this.testInstance.setup();

        IotHubServicesCommon.sendExpiredMessageExpectingMessageExpiredCallback(testInstance.client, testInstance.protocol, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, testInstance.authenticationType);
    }
}
