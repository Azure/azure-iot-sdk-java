/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;
import com.microsoft.azure.sdk.iot.ConnectionStatusCallback;
import com.microsoft.azure.sdk.iot.common.EventCallback;
import com.microsoft.azure.sdk.iot.common.Success;
import com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.*;
import org.junit.runner.RunWith;
import tests.integration.com.microsoft.azure.sdk.iot.DeviceConnectionString;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.azure.sdk.iot.common.helpers.SasTokenGenerator.generateSasTokenForIotDevice;
import static com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon.sendMessages;
import static com.microsoft.azure.sdk.iot.helper.Tools.retrieveEnvironmentVariableValue;
import static junit.framework.TestCase.fail;


@MediumTest
@RunWith(AndroidJUnit4.class)
public class SendMessagesIT
{

    public SendMessagesIT()
    {
    }

    //How much devices the multithreaded test will create in parallel.
    private static final Integer MAX_DEVICE_PARALLEL = 5;

    //Huw much sequential connections each device will open and close in the multithreaded test.
    private static final Integer NUM_CONNECTIONS_PER_DEVICE = 10;

    //How many keys each message will cary.
    private static final Integer NUM_KEYS_PER_MESSAGE = 10;

    private static RegistryManager registryManager;

    private static Device deviceHttps;
    private static Device deviceAmqps;
    private static Device deviceAmqpsWs;
    private static Device deviceMqtt;
    private static Device deviceMqttWs;
    private static Device deviceMqttX509;

    private static String x509Thumbprint;
    private static String publicKeyCert;
    private static String privateKey;

    private static String hostName;

    private static Device[] deviceListAmqps = new Device[MAX_DEVICE_PARALLEL];

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static final String PUBLIC_KEY_CERTIFICATE_BASE64_ENCODED_ENV_VAR_NAME = "IOTHUB_E2E_X509_CERT_BASE64";
    private static final String PRIVATE_KEY_BASE64_ENCODED_ENV_VAR_NAME = "IOTHUB_E2E_X509_PRIVATE_KEY_BASE64";
    private static final String X509_THUMBPRINT_ENV_VAR_NAME = "IOTHUB_E2E_X509_THUMBPRINT";

    private static String iotHubConnectionString = "HostName=iot-sdks-gate.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=Uv5mwn8l804P7joOowkZDJbilC0QF9yCsuqObeuqgtw=";

    //How much messages each device will send to the hub for each connection.
    private static final Integer NUM_MESSAGES_PER_CONNECTION = 10;

    // How much to wait until a message makes it to the server, in milliseconds
    private static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;

    //How many milliseconds between retry
    private static final Integer RETRY_MILLISECONDS = 100;

    private static final AtomicBoolean succeed = new AtomicBoolean();

    //Device Connection String
    private String connString = "";

    //Some tests below involve creating a short-lived sas token to test how expired tokens are handled
    private static final long SECONDS_FOR_SAS_TOKEN_TO_LIVE = 4;
    private static final long MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE = 6000;
    private static final long SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL = 1;

    @BeforeClass
    public static void setUp() throws Exception
    {
        Bundle bundle = InstrumentationRegistry.getArguments();
        String privateKeyBase64Encoded = "";
        String publicKeyCertBase64Encoded = "";
        Log.d("Test Log", "Logs start");
        if (bundle != null)
        {
            iotHubConnectionString = retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME, bundle);
            privateKeyBase64Encoded = retrieveEnvironmentVariableValue(PRIVATE_KEY_BASE64_ENCODED_ENV_VAR_NAME, bundle);
            publicKeyCertBase64Encoded = retrieveEnvironmentVariableValue(PUBLIC_KEY_CERTIFICATE_BASE64_ENCODED_ENV_VAR_NAME, bundle);
            x509Thumbprint = retrieveEnvironmentVariableValue(X509_THUMBPRINT_ENV_VAR_NAME, bundle);

        }
        else
        {
            Log.d("Test Log", "No extras");
        }

        Log.d("Test Log", "iotHubConnectionString:" + iotHubConnectionString);
        Log.d("Test Log", "privateKeyBase64Encoded:" + privateKeyBase64Encoded);
        Log.d("Test Log", "publicKeyCertBase64Encoded:" + publicKeyCertBase64Encoded);
        Log.d("Test Log", "x509Thumbprint:" + x509Thumbprint);
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

//        byte[] publicCertBytes = Base64.decodeBase64Local(publicKeyCertBase64Encoded.getBytes());
//        publicKeyCert = new String(publicCertBytes);
//
//        byte[] privateKeyBytes = Base64.decodeBase64Local(privateKeyBase64Encoded.getBytes());
//        privateKey = new String(privateKeyBytes);

        String uuid = UUID.randomUUID().toString();
        String deviceIdHttps = "java-device-client-e2e-test-https".concat("-" + uuid);
        String deviceIdAmqps = "java-device-client-e2e-test-amqps".concat("-" + uuid);
        String deviceIdAmqpsWs = "java-device-client-e2e-test-amqpsws".concat("-" + uuid);
        String deviceIdMqtt = "java-device-client-e2e-test-mqtt".concat("-" + uuid);
        String deviceIdMqttWs = "java-device-client-e2e-test-mqttws".concat("-" + uuid);
        String deviceIdMqttX509 = "java-device-client-e2e-test-mqtt-X509".concat("-" + uuid);

        deviceHttps = Device.createFromId(deviceIdHttps, null, null);
        deviceAmqps = Device.createFromId(deviceIdAmqps, null, null);
        deviceAmqpsWs = Device.createFromId(deviceIdAmqpsWs, null, null);
        deviceMqtt = Device.createFromId(deviceIdMqtt, null, null);
        deviceMqttWs = Device.createFromId(deviceIdMqttWs, null, null);
        deviceMqttX509 = Device.createDevice(deviceIdMqttX509, AuthenticationType.SELF_SIGNED);

        //deviceMqttX509.setThumbprint(x509Thumbprint, x509Thumbprint);
        registryManager.addDevice(deviceHttps);
        registryManager.addDevice(deviceAmqps);
        registryManager.addDevice(deviceAmqpsWs);
        registryManager.addDevice(deviceMqtt);
        registryManager.addDevice(deviceMqttWs);
        registryManager.addDevice(deviceMqttX509);

        for (int i = 0; i < MAX_DEVICE_PARALLEL; i++)
        {
            deviceIdAmqps = "java-device-client-e2e-test-amqps".concat(i + "-" + uuid);
            deviceListAmqps[i] = Device.createFromId(deviceIdAmqps, null, null);
            registryManager.addDevice(deviceListAmqps[i]);
        }

        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();
    }

    @AfterClass
    public static void tearDown() throws IOException, IotHubException
    {
        registryManager.removeDevice(deviceHttps.getDeviceId());
        registryManager.removeDevice(deviceAmqps.getDeviceId());
        registryManager.removeDevice(deviceAmqpsWs.getDeviceId());
        registryManager.removeDevice(deviceMqtt.getDeviceId());
        registryManager.removeDevice(deviceMqttWs.getDeviceId());
        registryManager.removeDevice(deviceMqttX509.getDeviceId());

        for (int i = 0; i < MAX_DEVICE_PARALLEL; i++)
        {
            registryManager.removeDevice(deviceListAmqps[i].getDeviceId());
        }
    }

    protected static class testDevice implements Runnable
    {
        private DeviceClient client;
        private String messageString;
        private String connString;

        private IotHubClientProtocol protocol;
        private Integer numMessagesPerConnection;
        private Integer numConnectionsPerDevice;
        private Integer sendTimeout;
        private Integer numKeys;
        private CountDownLatch latch;

        @Override
        public void run()
        {
            for (int i = 0; i < this.numConnectionsPerDevice; i++)
            {
                try
                {
                    this.openConnection();
                    this.sendMessages();
                    this.closeConnection();
                } catch (Exception e)
                {
                    succeed.set(false);
                }
            }
            latch.countDown();
        }

        public testDevice(Device deviceAmqps, IotHubClientProtocol protocol,
                          Integer numConnectionsPerDevice, Integer numMessagesPerConnection,
                          Integer numKeys, Integer sendTimeout, CountDownLatch latch)
        {
            this.protocol = protocol;
            this.numConnectionsPerDevice = numConnectionsPerDevice;
            this.numMessagesPerConnection = numMessagesPerConnection;
            this.sendTimeout = sendTimeout;
            this.numKeys = numKeys;
            this.latch = latch;

            succeed.set(true);

            this.connString = DeviceConnectionString.get(iotHubConnectionString, deviceAmqps);

            messageString = "Java client " + deviceAmqps.getDeviceId() + " test e2e message over AMQP protocol";
        }

        private void openConnection() throws URISyntaxException, IOException
        {
            client = new DeviceClient(connString, protocol);
            client.open();
        }

        public void sendMessages()
        {
            for (int i = 0; i < numMessagesPerConnection; ++i)
            {
                try
                {
                    Message msgSend = new Message(messageString);
                    msgSend.setProperty("messageCount", Integer.toString(i));
                    for (int j = 0; j < numKeys; j++)
                    {
                        msgSend.setProperty("key" + j, "value" + j);
                    }
                    msgSend.setExpiryTime(5000);

                    Success messageSent = new Success();
                    EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
                    client.sendEventAsync(msgSend, callback, messageSent);

                    Integer waitDuration = 0;
                    while (!messageSent.getResult())
                    {
                        Thread.sleep(RETRY_MILLISECONDS);
                        if ((waitDuration += RETRY_MILLISECONDS) > sendTimeout)
                        {
                            break;
                        }
                    }

                    if (!messageSent.getResult())
                    {
                        Assert.fail("Sending message over AMQPS protocol failed");
                    }
                } catch (Exception e)
                {
                    Assert.fail("Sending message over AMQPS protocol throws " + e);
                }
            }
        }

        public void closeConnection() throws IOException
        {
            client.closeNow();
        }
    }

    @Test
    public void SendMessagesOverAmqps() throws Exception
    {
        String messageString = "Java client e2e test message over Amqps protocol";
        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceAmqps), IotHubClientProtocol.AMQPS);
        SendMessagesCommon.sendMessages(client, IotHubClientProtocol.AMQPS, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
    }

    @Test
    public void sendMessagesOverAmqpsMultithreaded() throws URISyntaxException, IOException, InterruptedException
    {
        List<Thread> threads = new ArrayList<>(deviceListAmqps.length);
        CountDownLatch cdl = new CountDownLatch(deviceListAmqps.length);

        Integer count = 0;
        for (Device deviceAmqps : deviceListAmqps)
        {
            Thread thread = new Thread(
                    new testDevice(
                            deviceAmqps,
                            IotHubClientProtocol.AMQPS,
                            NUM_CONNECTIONS_PER_DEVICE,
                            NUM_MESSAGES_PER_CONNECTION,
                            NUM_KEYS_PER_MESSAGE,
                            SEND_TIMEOUT_MILLISECONDS,
                            cdl));
            thread.start();
            threads.add(thread);
            count++;
        }

        cdl.await();

        if (!succeed.get())
        {
            Assert.fail("Sending message over AMQP protocol in parallel failed");
        }
    }

    @Test
    public void SendMessagesOverMqtt() throws Exception
    {
        String messageString = "Java client e2e test message over Mqtt protocol";
        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqtt), IotHubClientProtocol.MQTT);
        SendMessagesCommon.sendMessages(client, IotHubClientProtocol.MQTT, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
    }

    @Test
    public void SendMessagesOverHttps() throws Exception
    {
        String messageString = "Java client e2e test message over Https protocol";
        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceHttps), IotHubClientProtocol.HTTPS);
        SendMessagesCommon.sendMessages(client, IotHubClientProtocol.HTTPS, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);
    }

    @Test
    public void sendMessagesOverAmqpsWs() throws IOException, InterruptedException, URISyntaxException
    {
        String messageString = "Java client e2e test message over Amqps WS protocol";
        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceAmqps), IotHubClientProtocol.AMQPS_WS);
        client.open();

        SendMessagesCommon.sendMessages(client, IotHubClientProtocol.AMQPS_WS, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);

        client.closeNow();
    }

    @Ignore
    @Test
    public void sendMessagesOverMqttWithX509() throws IOException, URISyntaxException, InterruptedException
    {
        String messageString = "Java client e2e test message over Mqtt protocol using x509 auth";

        Message msg = new Message(messageString);
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceMqttX509), IotHubClientProtocol.MQTT, publicKeyCert, false, privateKey, false);

        client.open();

        sendMessages(client, IotHubClientProtocol.MQTT, NUM_MESSAGES_PER_CONNECTION, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS);

        client.closeNow();
    }

    @Test
    public void tokenRenewalWorksForHTTPS() throws IOException, InterruptedException, URISyntaxException
    {
        DeviceClient client = new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceHttps), IotHubClientProtocol.HTTPS);

        //set it so a newly generated sas token only lasts for a small amount of time
        client.setOption("SetSASTokenExpiryTime", SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL);
        client.open();

        for (int messageAttempt = 0; messageAttempt < NUM_MESSAGES_PER_CONNECTION; messageAttempt++)
        {
            //wait until old sas token has expired, this should force the config to generate a new one from the device key
            Thread.sleep(SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL * 1000);

            Success messageSent = new Success();
            EventCallback callback = new EventCallback(IotHubStatusCode.OK_EMPTY);
            client.sendEventAsync(new Message("some message body"), callback, messageSent);

            Integer waitDuration = 0;
            while (!messageSent.getResult())
            {
                Thread.sleep(RETRY_MILLISECONDS);
                if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                {
                    fail("Timed out waiting for successful message callback");
                    break;
                }
            }
        }
    }

    @Test
    public void tokenExpiredAfterOpenButBeforeSendHTTPS() throws InvalidKeyException, IOException, InterruptedException, URISyntaxException
    {
        String soonToBeExpiredSASToken = generateSasTokenForIotDevice(hostName, deviceHttps.getDeviceId(), deviceHttps.getPrimaryKey(), SECONDS_FOR_SAS_TOKEN_TO_LIVE);

        DeviceClient client = new DeviceClient(soonToBeExpiredSASToken, IotHubClientProtocol.HTTPS);
        client.open();

        //Force the SAS token to expire before sending messages
        Thread.sleep(MILLISECONDS_TO_WAIT_FOR_TOKEN_TO_EXPIRE);

        sendMessagesExpectingSASTokenExpiration(client, IotHubClientProtocol.HTTPS.toString());

        client.closeNow();
    }

    /**
     * Send some messages that wait for callbacks to signify that the SAS token in the client config has expired.
     *
     * @param client   the client to send the messages from
     * @param protocol the protocol the client is using
     */
    private void sendMessagesExpectingSASTokenExpiration(DeviceClient client, String protocol)
    {
        for (int i = 0; i < NUM_MESSAGES_PER_CONNECTION; ++i)
        {
            try
            {
                Message messageToSend = new Message("Test message expecting SAS Token Expired callback for protocol: " + protocol);
                Success messageSent = new Success();
                Success statusUpdated = new Success();

                ConnectionStatusCallback stateCallback = new ConnectionStatusCallback(IotHubConnectionState.SAS_TOKEN_EXPIRED);
                EventCallback messageCallback = new EventCallback(IotHubStatusCode.UNAUTHORIZED);

                client.registerConnectionStateCallback(stateCallback, statusUpdated);
                client.sendEventAsync(messageToSend, messageCallback, messageSent);

                Integer waitDuration = 0;
                while (!messageSent.getResult() || !statusUpdated.getResult())
                {
                    Thread.sleep(RETRY_MILLISECONDS);
                    if ((waitDuration += RETRY_MILLISECONDS) > SEND_TIMEOUT_MILLISECONDS)
                    {
                        break;
                    }
                }

                if (!messageSent.getResult() || !statusUpdated.getResult())
                {
                    Assert.fail("Sending message over " + protocol + " protocol failed");
                }
            } catch (Exception e)
            {
                Assert.fail("Sending message over " + protocol + " protocol failed");
            }
        }
    }

}
