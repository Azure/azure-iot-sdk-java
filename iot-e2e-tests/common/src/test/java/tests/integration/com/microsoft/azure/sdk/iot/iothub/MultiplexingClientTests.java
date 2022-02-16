/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub;


import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse;
import com.microsoft.azure.sdk.iot.device.twin.Pair;
import com.microsoft.azure.sdk.iot.device.twin.Property;
import com.microsoft.azure.sdk.iot.device.twin.TwinPropertyCallback;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MultiplexingClient;
import com.microsoft.azure.sdk.iot.device.MultiplexingClientOptions;
import com.microsoft.azure.sdk.iot.device.ProxySettings;
import com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationAuthenticationException;
import com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.UnauthorizedException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClient;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClient;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClientOptions;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.twin.TwinClientOptions;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.BasicProxyAuthenticator;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ErrorInjectionHelper;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.EventCallback;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Success;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestDeviceIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.TestCase.*;

/**
 * Test class containing all tests to be run on JVM and android pertaining to multiplexing with the MultiplexingClient class
 */
@Slf4j
@IotHubTest
@MultiplexingClientTest
@RunWith(Parameterized.class)
public class MultiplexingClientTests extends IntegrationTest
{
    private static final int DEVICE_MULTIPLEX_COUNT = 3;

    private static final int MESSAGE_SEND_TIMEOUT_MILLIS = 60 * 1000;
    private static final int FAULT_INJECTION_RECOVERY_TIMEOUT_MILLIS = 2 * 60 * 1000;
    private static final int FAULT_INJECTION_TIMEOUT_MILLIS = 60 * 1000;
    private static final int DEVICE_METHOD_SUBSCRIBE_TIMEOUT_MILLISECONDS = 60 * 1000;
    private static final int TWIN_SUBSCRIBE_TIMEOUT_MILLIS = 60 * 1000;
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_DESIRED_PROPERTY_SUBSCRIPTION_ACKNOWLEDGEMENT = 500; // .5 seconds
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_REPORTED_PROPERTY_ACKNOWLEDGEMENT = 1000; // 1 second
    private static final int DESIRED_PROPERTY_CALLBACK_TIMEOUT_MILLIS = 60 * 1000;
    private static final int DEVICE_SESSION_OPEN_TIMEOUT = 60 * 1000;
    private static final int DEVICE_SESSION_CLOSE_TIMEOUT = 60 * 1000;

    protected static String iotHubConnectionString = "";

    private static MessagingClient messagingClient;
    private static RegistryClient registryClient;

    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8849;

    // Semmle flags this as a security issue, but this is a test username so the warning can be suppressed
    protected static final String testProxyUser = "proxyUsername"; // lgtm

    // Semmle flags this as a security issue, but this is a test password so the warning can be suppressed
    protected static final char[] testProxyPass = "1234".toCharArray(); // lgtm


    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        registryClient = new RegistryClient(iotHubConnectionString, RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
        messagingClient = new MessagingClient(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);

        return Arrays.asList(
                new Object[][]
                        {
                                {IotHubClientProtocol.AMQPS},
                                {IotHubClientProtocol.AMQPS_WS}
                        });
    }

    public MultiplexingClientTests(IotHubClientProtocol protocol)
    {
        this.testInstance = new MultiplexingClientTestInstance(protocol);
    }

    public MultiplexingClientTestInstance testInstance;

    public static class MultiplexingClientTestInstance
    {
        public IotHubClientProtocol protocol;
        public List<Device> deviceIdentityArray;
        public List<DeviceClient> deviceClientArray;
        public List<TestDeviceIdentity> testDevicesArrayIdentity;
        public MultiplexingClient multiplexingClient;

        public MultiplexingClientTestInstance(IotHubClientProtocol protocol)
        {
            this.protocol = protocol;
        }

        public void setup(int multiplexingDeviceSessionCount) throws InterruptedException, IotHubException, IOException, URISyntaxException, MultiplexingClientException, GeneralSecurityException
        {
            setup(multiplexingDeviceSessionCount, null, false);
        }

        public void setup(int multiplexingDeviceSessionCount, MultiplexingClientOptions options, boolean needCleanTwin) throws InterruptedException, IotHubException, IOException, URISyntaxException, MultiplexingClientException, GeneralSecurityException
        {
            deviceIdentityArray = new ArrayList<>(multiplexingDeviceSessionCount);
            deviceClientArray = new ArrayList<>(multiplexingDeviceSessionCount);
            testDevicesArrayIdentity = new ArrayList<>(multiplexingDeviceSessionCount);

            for (int i = 0; i < multiplexingDeviceSessionCount; i++)
            {
                TestDeviceIdentity testDeviceIdentity = Tools.getTestDevice(iotHubConnectionString, this.protocol, AuthenticationType.SAS, needCleanTwin);
                testDevicesArrayIdentity.add(testDeviceIdentity);
                deviceIdentityArray.add(i, testDeviceIdentity.getDevice());
            }

            IotHubConnectionString connectionString = IotHubConnectionString.createIotHubConnectionString(iotHubConnectionString);
            this.multiplexingClient = new MultiplexingClient(connectionString.getHostName(), this.protocol, options);
            for (int i = 0; i < multiplexingDeviceSessionCount; i++)
            {
                this.deviceClientArray.add(i, new DeviceClient(Tools.getDeviceConnectionString(iotHubConnectionString, deviceIdentityArray.get(i)), this.protocol));
            }

            this.multiplexingClient.registerDeviceClients(this.deviceClientArray);
        }

        public void dispose()
        {
            if (this.multiplexingClient != null)
            {
                try
                {
                    this.multiplexingClient.close();
                }
                catch (MultiplexingClientException e)
                {
                    log.error("Failed to close multiplexing client", e);
                }
            }

            if (this.testDevicesArrayIdentity != null)
            {
                Tools.disposeTestIdentities(this.testDevicesArrayIdentity, iotHubConnectionString);
            }
        }
    }

    @After
    public void tearDownTest()
    {
        if (testInstance != null)
        {
            testInstance.dispose();
        }
    }

    @BeforeClass
    public static void startProxy()
    {
        proxyServer = DefaultHttpProxyServer.bootstrap()
                .withPort(testProxyPort)
                .withProxyAuthenticator(new BasicProxyAuthenticator(testProxyUser, testProxyPass))
                .start();
    }

    @AfterClass
    public static void stopProxy()
    {
        proxyServer.stop();
    }

    @Test
    public void openClientWithRetry() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.open(true);
        testInstance.multiplexingClient.close();
    }

    @Test
    public void openClientWithRetryWithoutRegisteredDevices() throws Exception
    {
        testInstance.setup(0);
        testInstance.multiplexingClient.open(true);
        testInstance.multiplexingClient.close();
    }

    @Test
    public void sendMessages() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.open(false);

        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

        testInstance.multiplexingClient.close();
    }

    @Test
    public void connectionStatusCallbackExecutedWithNoDevices() throws Exception
    {
        // Even with no devices registered to a multiplexed connection, the connection status callback should execute
        // when the multiplexed connection opens and closes.
        testInstance.setup(0);
        ConnectionStatusChangeTracker connectionStatusChangeTracker = new ConnectionStatusChangeTracker();
        testInstance.multiplexingClient.setConnectionStatusChangeCallback(connectionStatusChangeTracker, null);
        testInstance.multiplexingClient.open(false);

        assertTrue(
            "Connection status callback never executed with CONNECTED status after opening multiplexing client with no devices registered",
            connectionStatusChangeTracker.isOpen);

        testInstance.multiplexingClient.close();

        assertTrue(
            "Connection status callback never executed with DISCONNECTED status and CLIENT_CLOSED reason.",
            connectionStatusChangeTracker.clientClosedGracefully);
    }

    // MultiplexingClient should be able to open an AMQP connection to IoTHub with no device sessions, and should
    // allow for device sessions to be added and used later.
    @Test
    public void openMultiplexingClientWithoutAnyRegisteredDevices() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.unregisterDeviceClients(testInstance.deviceClientArray);
        testInstance.multiplexingClient.open(false);

        testInstance.multiplexingClient.registerDeviceClients(testInstance.deviceClientArray);

        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

        testInstance.multiplexingClient.close();
    }

    @Test
    public void canUnregisterAllClientsThenReregisterAllClientsOnOpenConnection() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.open(false);

        testInstance.multiplexingClient.unregisterDeviceClients(testInstance.deviceClientArray);
        testInstance.multiplexingClient.registerDeviceClients(testInstance.deviceClientArray);

        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

        testInstance.multiplexingClient.close();
    }

    @ContinuousIntegrationTest
    @Test
    public void canReopenClosedMultiplexingClient() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);

        // Open and close the connection once
        testInstance.multiplexingClient.open(false);
        testInstance.multiplexingClient.close();

        // Re-open the connection and verify that it can still send telemetry
        testInstance.multiplexingClient.open(false);
        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);
        testInstance.multiplexingClient.close();
    }

    @ContinuousIntegrationTest
    @Test
    public void sendMessagesMaxDevicesAllowed() throws Exception
    {
        // Right now, AMQP connections can do up to 1000 devices which is consistent with the IoTHub advertised limit
        // But AMQPS_WS is limited to ~500 for some reason. Still needs investigation.
        if (testInstance.protocol == IotHubClientProtocol.AMQPS)
        {
            testInstance.setup(MultiplexingClient.MAX_MULTIPLEX_DEVICE_COUNT_AMQPS);
        }
        else
        {
            testInstance.setup(MultiplexingClient.MAX_MULTIPLEX_DEVICE_COUNT_AMQPS_WS);
        }

        testInstance.multiplexingClient.open(false);

        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

        testInstance.multiplexingClient.close();
    }

    @Ignore  // This is more of a performance test than a typical test. It should only be run locally, not at the gate
    @ContinuousIntegrationTest
    @Test
    public void sendMessagesMaxDevicesAllowedTimes10MultiplexingClientsParallelOpen() throws Exception
    {
        int multiplexingClientCount = 10;
        MultiplexingClientTestInstance[] testInstances = new MultiplexingClientTestInstance[multiplexingClientCount];
        long startSetupTime = System.currentTimeMillis();
        for (int i = 0; i < multiplexingClientCount; i++)
        {
            testInstances[i] = new MultiplexingClientTestInstance(testInstance.protocol);
            if (testInstances[i].protocol == IotHubClientProtocol.AMQPS)
            {
                testInstances[i].setup(MultiplexingClient.MAX_MULTIPLEX_DEVICE_COUNT_AMQPS);
            }
            else
            {
                testInstances[i].setup(MultiplexingClient.MAX_MULTIPLEX_DEVICE_COUNT_AMQPS_WS);
            }
        }
        long finishSetupTime = System.currentTimeMillis();


        long startOpenTime = System.currentTimeMillis();
        Thread[] openThreads = new Thread[multiplexingClientCount];
        AtomicReference<MultiplexingClientException>[] openExceptions = new AtomicReference[multiplexingClientCount];
        for (int i = 0; i < multiplexingClientCount; i++)
        {
            int finalI = i;
            openThreads[i] = new Thread(() -> {
                try
                {
                    openExceptions[finalI] = new AtomicReference<>();
                    testInstances[finalI].multiplexingClient.open(false);
                }
                catch (MultiplexingClientException e)
                {
                    openExceptions[finalI].set(e);
                }
            });

            openThreads[i].start();
        }

        for (int i = 0; i < multiplexingClientCount; i++)
        {
            openThreads[i].join();
        }

        for (int i = 0; i < multiplexingClientCount; i++)
        {
            if (openExceptions[i] != null && openExceptions[i].get() != null)
            {
                throw openExceptions[i].get();
            }
        }

        long finishOpenTime = System.currentTimeMillis();

        long startSendTime = System.currentTimeMillis();
        for (int i = 0; i < multiplexingClientCount; i++)
        {
            testSendingMessagesFromMultiplexedClients(testInstances[i].deviceClientArray);
        }
        long finishSendTime = System.currentTimeMillis();

        long startCloseTime = System.currentTimeMillis();
        for (int i = 0; i < multiplexingClientCount; i++)
        {
            testInstances[i].multiplexingClient.close();
        }
        long finishCloseTime = System.currentTimeMillis();

        // Mostly for looking at perf manually. No requirements are set on how low these values should be, so we
        // don't have any assertions tied to them.
        log.debug("Multiplexed client count : " + multiplexingClientCount);
        log.debug("Setup time: " + (finishSetupTime - startSetupTime) / 1000.0);
        log.debug("Open time: " + (finishOpenTime - startOpenTime) / 1000.0);
        log.debug("Send time: " + (finishSendTime - startSendTime) / 1000.0);
        log.debug("Close time: " + (finishCloseTime - startCloseTime) / 1000.0);
    }

    @Ignore  // This is more of a performance test than a typical test. It should only be run locally, not at the gate
    @ContinuousIntegrationTest
    @Test
    public void sendMessagesMaxDevicesAllowedTimes10MultiplexingClientsSerialOpen() throws Exception
    {
        int multiplexingClientCount = 10;
        MultiplexingClientTestInstance[] testInstances = new MultiplexingClientTestInstance[multiplexingClientCount];
        long startSetupTime = System.currentTimeMillis();
        for (int i = 0; i < multiplexingClientCount; i++)
        {
            testInstances[i] = new MultiplexingClientTestInstance(testInstance.protocol);
            if (testInstances[i].protocol == IotHubClientProtocol.AMQPS)
            {
                testInstances[i].setup(MultiplexingClient.MAX_MULTIPLEX_DEVICE_COUNT_AMQPS);
            }
            else
            {
                testInstances[i].setup(MultiplexingClient.MAX_MULTIPLEX_DEVICE_COUNT_AMQPS_WS);
            }
        }
        long finishSetupTime = System.currentTimeMillis();


        long startOpenTime = System.currentTimeMillis();
        for (int i = 0; i < multiplexingClientCount; i++)
        {
            testInstances[i].multiplexingClient.open(false);
        }

        long finishOpenTime = System.currentTimeMillis();

        long startSendTime = System.currentTimeMillis();
        for (int i = 0; i < multiplexingClientCount; i++)
        {
            testSendingMessagesFromMultiplexedClients(testInstances[i].deviceClientArray);
        }
        long finishSendTime = System.currentTimeMillis();

        long startCloseTime = System.currentTimeMillis();
        for (int i = 0; i < multiplexingClientCount; i++)
        {
            testInstances[i].multiplexingClient.close();
        }
        long finishCloseTime = System.currentTimeMillis();

        // Mostly for looking at perf manually. No requirements are set on how low these values should be, so we
        // don't have any assertions tied to them.
        log.debug("Multiplexed client count : " + multiplexingClientCount);
        log.debug("Setup time: " + (finishSetupTime - startSetupTime) / 1000.0);
        log.debug("Open time: " + (finishOpenTime - startOpenTime) / 1000.0);
        log.debug("Send time: " + (finishSendTime - startSendTime) / 1000.0);
        log.debug("Close time: " + (finishCloseTime - startCloseTime) / 1000.0);
    }

    @Test
    public void sendMessagesWithProxy() throws Exception
    {
        if (testInstance.protocol != IotHubClientProtocol.AMQPS_WS)
        {
            // only AMQPS_WS supports proxies
            return;
        }

        Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
        ProxySettings proxySettings = new ProxySettings(testProxy, testProxyUser, testProxyPass);

        //re-setup test instance to use proxy instead
        testInstance.setup(DEVICE_MULTIPLEX_COUNT, MultiplexingClientOptions.builder().proxySettings(proxySettings).build(), false);
        testInstance.multiplexingClient.open(false);

        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

        testInstance.multiplexingClient.close();
    }

    private static void testSendingMessagesFromMultiplexedClients(List<DeviceClient> multiplexedClients) throws InterruptedException
    {
        Success[] messageSendResults = new Success[multiplexedClients.size()];
        for (int i = 0; i < multiplexedClients.size(); i++)
        {
            messageSendResults[i] = testSendingMessageFromDeviceClient(multiplexedClients.get(i));
        }

        for (int i = 0; i < multiplexedClients.size(); i++)
        {
            waitForMessageToBeAcknowledged(messageSendResults[i]);
        }
    }

    private static Success testSendingMessageFromDeviceClient(DeviceClient multiplexedClient)
    {
        return testSendingMessageFromDeviceClient(multiplexedClient, new Message("some payload"));
    }

    private static Success testSendingMessageFromDeviceClient(DeviceClient multiplexedClient, Message message)
    {
        Success messageSendSuccess = new Success();
        EventCallback messageSentCallback = new EventCallback(IotHubStatusCode.OK_EMPTY);
        multiplexedClient.sendEventAsync(message, messageSentCallback, messageSendSuccess);
        return messageSendSuccess;
    }

    private static void waitForMessageToBeAcknowledged(Success messageSendSuccess) throws InterruptedException
    {
        waitForMessageToBeAcknowledged(messageSendSuccess, "Timed out waiting for sent message to be acknowledged");
    }

    private static void waitForMessageToBeAcknowledged(Success messageSendSuccess, String timeoutErrorMessage) throws InterruptedException
    {
        long startTime = System.currentTimeMillis();
        while (!messageSendSuccess.wasCallbackFired())
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > MESSAGE_SEND_TIMEOUT_MILLIS)
            {
                fail(timeoutErrorMessage);
            }
        }

        assertTrue("Unexpected callback result: " + messageSendSuccess.getCallbackStatusCode(), messageSendSuccess.getResult());
    }

    @Test
    @StandardTierHubOnlyTest
    public void receiveMessagesIncludingProperties() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.open(false);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            String expectedMessageCorrelationId = UUID.randomUUID().toString();
            MessageCallback messageCallback = new MessageCallback(expectedMessageCorrelationId);
            testInstance.deviceClientArray.get(i).setMessageCallback(messageCallback, null);

            testReceivingCloudToDeviceMessage(testInstance.deviceIdentityArray.get(i).getDeviceId(), messageCallback, expectedMessageCorrelationId);

            assertTrue("Message callback fired, but unexpected message was received", messageCallback.expectedMessageReceived);
        }

        testInstance.multiplexingClient.close();
    }

    // MessageCallback for cloud to device messages should not be preserved between registrations by default
    @Test
    @StandardTierHubOnlyTest
    public void cloudToDeviceMessageSubscriptionNotPreservedByDeviceClientAfterUnregistration() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.open(false);
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            String expectedMessageCorrelationId = UUID.randomUUID().toString();
            MessageCallback messageCallback = new MessageCallback(expectedMessageCorrelationId);
            testInstance.deviceClientArray.get(i).setMessageCallback(messageCallback, null);

            testReceivingCloudToDeviceMessage(testInstance.deviceIdentityArray.get(i).getDeviceId(), messageCallback, expectedMessageCorrelationId);
        }

        testInstance.multiplexingClient.unregisterDeviceClients(testInstance.deviceClientArray);
        testInstance.multiplexingClient.registerDeviceClients(testInstance.deviceClientArray);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            String expectedMessageCorrelationId = UUID.randomUUID().toString();
            MessageCallback messageCallback = new MessageCallback(expectedMessageCorrelationId);
            testInstance.deviceClientArray.get(i).setMessageCallback(messageCallback, null);
            testReceivingCloudToDeviceMessage(testInstance.deviceIdentityArray.get(i).getDeviceId(), messageCallback, expectedMessageCorrelationId);
        }

        testInstance.multiplexingClient.close();
    }

    private static void testReceivingCloudToDeviceMessage(String deviceId, MessageCallback messageCallback, String expectedMessageCorrelationId) throws IOException, IotHubException, InterruptedException
    {
        com.microsoft.azure.sdk.iot.service.messaging.Message serviceMessage = new com.microsoft.azure.sdk.iot.service.messaging.Message("some payload");
        serviceMessage.setCorrelationId(expectedMessageCorrelationId);
        messagingClient.open();
        messagingClient.send(deviceId, serviceMessage);
        messagingClient.close();

        long startTime = System.currentTimeMillis();
        while (!messageCallback.messageCallbackFired)
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > MESSAGE_SEND_TIMEOUT_MILLIS)
            {
                fail("Timed out waiting for message to be received");
            }
        }

        assertTrue("Message callback fired, but unexpected message was received", messageCallback.expectedMessageReceived);
    }

    private static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public boolean messageCallbackFired = false;
        public boolean expectedMessageReceived = false;

        String expectedCorrelationId;

        public MessageCallback(String expectedMessageCorrelationId)
        {
            this.expectedCorrelationId = expectedMessageCorrelationId;
        }

        @Override
        public IotHubMessageResult execute(Message message, Object callbackContext)
        {
            messageCallbackFired = true;
            if (message.getCorrelationId().equals(expectedCorrelationId))
            {
                expectedMessageReceived = true;
            }

            return IotHubMessageResult.COMPLETE;
        }

        public void resetExpectations(String newExpectedMessageCorrelationId)
        {
            this.messageCallbackFired = false;
            this.expectedMessageReceived = false;
            this.expectedCorrelationId = newExpectedMessageCorrelationId;
        }
    }

    @Test
    @StandardTierHubOnlyTest
    public void invokeMethodSucceed() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.open(false);
        DirectMethodsClient directMethodServiceClientClient = new DirectMethodsClient(iotHubConnectionString);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // Subscribe to methods on the multiplexed client
            String expectedMethodName = UUID.randomUUID().toString();
            DirectMethodCallback directMethodCallback = new DirectMethodCallback(expectedMethodName);
            subscribeToDirectMethod(testInstance.deviceClientArray.get(i), directMethodCallback);
            testDirectMethods(directMethodServiceClientClient, testInstance.deviceIdentityArray.get(i).getDeviceId(), expectedMethodName, directMethodCallback);
        }

        testInstance.multiplexingClient.close();
    }

    // Methods subscriptions and callbacks should not be preserved between registrations by default
    @Test
    @ContinuousIntegrationTest
    @StandardTierHubOnlyTest
    public void methodsSubscriptionNotPreservedByDeviceClientAfterUnregistration() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.open(false);
        DirectMethodsClient directMethodServiceClientClient = new DirectMethodsClient(iotHubConnectionString);
        List<DirectMethodCallback> directDirectMethodCallbacks = new ArrayList<>();
        List<String> expectedMethodNames = new ArrayList<>();

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // Subscribe to methods on the multiplexed client
            String expectedMethodName = UUID.randomUUID().toString();
            expectedMethodNames.add(expectedMethodName);
            DirectMethodCallback deviceDirectMethodCallback = new DirectMethodCallback(expectedMethodName);
            directDirectMethodCallbacks.add(deviceDirectMethodCallback);
            subscribeToDirectMethod(testInstance.deviceClientArray.get(i), deviceDirectMethodCallback);
            testDirectMethods(directMethodServiceClientClient, testInstance.deviceIdentityArray.get(i).getDeviceId(), expectedMethodNames.get(i), directDirectMethodCallbacks.get(i));
        }

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            directDirectMethodCallbacks.get(i).resetExpectations();
        }

        testInstance.multiplexingClient.unregisterDeviceClients(testInstance.deviceClientArray);
        testInstance.multiplexingClient.registerDeviceClients(testInstance.deviceClientArray);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            subscribeToDirectMethod(testInstance.deviceClientArray.get(i), directDirectMethodCallbacks.get(i));
            testDirectMethods(directMethodServiceClientClient, testInstance.deviceIdentityArray.get(i).getDeviceId(), expectedMethodNames.get(i), directDirectMethodCallbacks.get(i));
        }

        testInstance.multiplexingClient.close();
    }

    private static void testDirectMethods(DirectMethodsClient directMethodServiceClientClient, String deviceId, String expectedMethodName, DirectMethodCallback deviceDirectMethodCallback) throws IOException, IotHubException, InterruptedException {
        // Give the method subscription some extra buffer time before invoking the method
        Thread.sleep(1000);

        // Invoke method on the multiplexed device
        directMethodServiceClientClient.invoke(deviceId, expectedMethodName);

        // No need to wait for the device to receive the method invocation since the service client call does that already
        assertTrue("Device method callback never fired on device", deviceDirectMethodCallback.directMethodCallbackFired);
        assertTrue("Device method callback fired, but unexpected method name was received", deviceDirectMethodCallback.expectedMethodReceived);
    }

    private static void subscribeToDirectMethod(DeviceClient deviceClient, DirectMethodCallback deviceDirectMethodCallback) throws InterruptedException, IOException
    {
        Success methodsSubscribedSuccess = new Success();
        deviceClient.subscribeToMethodsAsync(deviceDirectMethodCallback, null, (responseStatus, callbackContext) -> {
            ((Success) callbackContext).setCallbackStatusCode(responseStatus);
            ((Success) callbackContext).setResult(responseStatus == IotHubStatusCode.OK_EMPTY);
            ((Success) callbackContext).callbackWasFired();
        }, methodsSubscribedSuccess);

        // Wait for methods subscription to be acknowledged by hub
        long startTime = System.currentTimeMillis();
        while (methodsSubscribedSuccess.wasCallbackFired())
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > DEVICE_METHOD_SUBSCRIBE_TIMEOUT_MILLISECONDS)
            {
                throw new AssertionError("Timed out waiting for device method subscription to be acknowledged");
            }
        }
    }

    private static class DirectMethodCallback implements com.microsoft.azure.sdk.iot.device.twin.MethodCallback
    {
        public boolean directMethodCallbackFired = false;
        public boolean expectedMethodReceived = false;

        final String expectedMethodName;

        public DirectMethodCallback(String expectedMethodName)
        {
            this.expectedMethodName = expectedMethodName;
        }

        @Override
        public DirectMethodResponse call(String methodName, Object methodData, Object context) {
            directMethodCallbackFired = true;
            if (methodName.equals(expectedMethodName))
            {
                expectedMethodReceived = true;
            }

            return new DirectMethodResponse(200, null);
        }

        public void resetExpectations()
        {
            directMethodCallbackFired = false;
            expectedMethodReceived = false;
        }
    }

    static class TwinPropertyCallbackImpl implements TwinPropertyCallback
    {
        String expectedKey;
        String expectedValue;

        public boolean receivedCallback = false;
        public boolean receivedExpectedKey = false;
        public boolean receivedExpectedValue = false;

        public String actualKey;
        public String actualValue;

        public TwinPropertyCallbackImpl(String expectedKey, String expectedValue)
        {
            this.expectedKey = expectedKey;
            this.expectedValue = expectedValue;
        }

        @Override
        public void onPropertyChanged(Property property, Object context)
        {
            actualKey = property.getKey();
            if (actualKey.equals(expectedKey))
            {
                receivedExpectedKey = true;

                actualValue = property.getValue().toString();
                if (actualValue.equals(expectedValue))
                {
                    receivedExpectedValue = true;
                }
            }

            receivedCallback = true;
        }
    }

    @Test
    @StandardTierHubOnlyTest
    public void testTwin() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT, MultiplexingClientOptions.builder().build(), true);
        testInstance.multiplexingClient.open(false);

        TwinClient twinClientServiceClient = new TwinClient(iotHubConnectionString, TwinClientOptions.builder().httpReadTimeoutSeconds(0).build());

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // The twin for this test identity is about to be modified. Set this flag so that the test identity recycler re-uses this identity only for tests
            // that don't care about the initial twin state of an identity
            testInstance.testDevicesArrayIdentity.get(i).twinUpdated = true;
            String expectedPropertyKey = UUID.randomUUID().toString();
            String expectedPropertyValue = UUID.randomUUID().toString();
            TwinPropertyCallbackImpl twinPropertyCallback = new TwinPropertyCallbackImpl(expectedPropertyKey, expectedPropertyValue);
            startTwin(testInstance.deviceClientArray.get(i), new EventCallback(IotHubStatusCode.OK), twinPropertyCallback);

            // Testing subscribing to desired properties
            Map<Property, Pair<TwinPropertyCallback, Object>> onDesiredPropertyChange = new HashMap<>();
            onDesiredPropertyChange.put(new Property(expectedPropertyKey, null), new Pair<>(twinPropertyCallback, null));
            testInstance.deviceClientArray.get(i).subscribeToTwinDesiredPropertiesAsync(onDesiredPropertyChange);

            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_DESIRED_PROPERTY_SUBSCRIPTION_ACKNOWLEDGEMENT);

            // Send desired property update to multiplexed device
            testDesiredPropertiesFlow(testInstance.deviceClientArray.get(i), twinClientServiceClient, twinPropertyCallback, expectedPropertyKey, expectedPropertyValue);

            // Testing sending reported properties
            testReportedPropertiesFlow(testInstance.deviceClientArray.get(i), twinClientServiceClient, expectedPropertyKey, expectedPropertyValue);
        }
    }

    // Twin subscriptions and callbacks should not be preserved between registrations by default
    @Test
    @ContinuousIntegrationTest
    @StandardTierHubOnlyTest
    public void twinSubscriptionNotPreservedByDeviceClientAfterUnregistration() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT, MultiplexingClientOptions.builder().build(), true);
        testInstance.multiplexingClient.open(false);

        TwinClient twinClientServiceClient = new TwinClient(iotHubConnectionString, TwinClientOptions.builder().httpReadTimeoutSeconds(0).build());
        String expectedPropertyKey = UUID.randomUUID().toString();
        String expectedPropertyValue = UUID.randomUUID().toString();
        List<TwinPropertyCallbackImpl> twinPropertyCallbacks = new ArrayList<>();
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // The twin for this test identity is about to be modified. Set this flag so that the test identity recycler re-uses this identity only for tests
            // that don't care about the initial twin state of an identity
            testInstance.testDevicesArrayIdentity.get(i).twinUpdated = true;
            TwinPropertyCallbackImpl twinPropertyCallback = new TwinPropertyCallbackImpl(expectedPropertyKey, expectedPropertyValue);
            twinPropertyCallbacks.add(twinPropertyCallback);
            startTwin(testInstance.deviceClientArray.get(i), new EventCallback(IotHubStatusCode.OK), twinPropertyCallback);
        }

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // Testing subscribing to desired properties
            testDesiredPropertiesFlow(testInstance.deviceClientArray.get(i), twinClientServiceClient, twinPropertyCallbacks.get(i), expectedPropertyKey, expectedPropertyValue);

            // Testing sending reported properties
            testReportedPropertiesFlow(testInstance.deviceClientArray.get(i), twinClientServiceClient, expectedPropertyKey, expectedPropertyValue);
        }

        // unregister and then re-register the clients to see if their subscriptions were preserved or not
        testInstance.multiplexingClient.unregisterDeviceClients(testInstance.deviceClientArray);
        testInstance.multiplexingClient.registerDeviceClients(testInstance.deviceClientArray);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            boolean expectedExceptionThrown = false;
            try
            {
                // Testing sending reported properties
                testReportedPropertiesFlow(testInstance.deviceClientArray.get(i), twinClientServiceClient, expectedPropertyKey, expectedPropertyValue);
            }
            catch (IOException e)
            {
                // IOException seems odd here, since we are testing what should be an IllegalStateException or UnsupportedOperationException,
                // but it would be a breaking change to modify the device clien to throw that exception when a twin method is
                // called without first starting twin.
                expectedExceptionThrown = true;
            }

            assertTrue("Expected twin method to throw since twin has not been started since re-registering client", expectedExceptionThrown);
        }

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // The twin for this test identity is about to be modified. Set this flag so that the test identity recycler re-uses this identity only for tests
            // that don't care about the initial twin state of an identity
            testInstance.testDevicesArrayIdentity.get(i).twinUpdated = true;
            TwinPropertyCallbackImpl twinPropertyCallback = new TwinPropertyCallbackImpl(expectedPropertyKey, expectedPropertyValue);
            twinPropertyCallbacks.add(twinPropertyCallback);
            startTwin(testInstance.deviceClientArray.get(i), new EventCallback(IotHubStatusCode.OK), twinPropertyCallback);
        }

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // Testing subscribing to desired properties
            testDesiredPropertiesFlow(testInstance.deviceClientArray.get(i), twinClientServiceClient, twinPropertyCallbacks.get(i), expectedPropertyKey, expectedPropertyValue);

            // Testing sending reported properties
            testReportedPropertiesFlow(testInstance.deviceClientArray.get(i), twinClientServiceClient, expectedPropertyKey, expectedPropertyValue);
        }
    }

    private static void startTwin(DeviceClient deviceClient, IotHubEventCallback twinEventCallback, TwinPropertyCallback twinPropertyCallback) throws IOException, InterruptedException {
        Success twinStarted = new Success();
        deviceClient.startTwinAsync(twinEventCallback, twinStarted, twinPropertyCallback, null);

        long startTime = System.currentTimeMillis();
        while (!twinStarted.wasCallbackFired())
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > TWIN_SUBSCRIBE_TIMEOUT_MILLIS)
            {
                fail("Timed out waiting for twin to start");
            }
        }

        assertTrue("Failed to start twin. Unexpected status code " + twinStarted.getCallbackStatusCode(), twinStarted.getResult());
    }

    private static void testDesiredPropertiesFlow(DeviceClient deviceClient, TwinClient twinClientServiceClient, TwinPropertyCallbackImpl twinPropertyCallback, String expectedPropertyKey, String expectedPropertyValue) throws IOException, IotHubException, InterruptedException {
        Twin serviceClientTwin = new Twin(deviceClient.getConfig().getDeviceId());
        Set<com.microsoft.azure.sdk.iot.service.twin.Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new com.microsoft.azure.sdk.iot.service.twin.Pair(expectedPropertyKey, expectedPropertyValue));
        serviceClientTwin.setDesiredProperties(desiredProperties);
        twinClientServiceClient.patch(serviceClientTwin);

        long startTime = System.currentTimeMillis();
        while (!twinPropertyCallback.receivedCallback)
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > DESIRED_PROPERTY_CALLBACK_TIMEOUT_MILLIS)
            {
                fail("Timed out waiting for desired property callback to fire");
            }
        }

        assertTrue("Desired property callback fired with unexpected key. Expected " + expectedPropertyKey + " but was " + twinPropertyCallback.actualKey, twinPropertyCallback.receivedExpectedKey);
        assertTrue("Desired property callback fired with unexpected value. Expected " + expectedPropertyValue + " but was " + twinPropertyCallback.actualValue, twinPropertyCallback.receivedExpectedValue);
    }

    private static void testReportedPropertiesFlow(DeviceClient deviceClient, TwinClient twinClientServiceClient, String expectedPropertyKey, String expectedPropertyValue) throws IOException, IotHubException, InterruptedException {
        String expectedReportedPropertyValue = expectedPropertyValue + "-reported";
        Set<Property> reportedProperties = new HashSet<>();
        reportedProperties.add(new Property(expectedPropertyKey, expectedReportedPropertyValue));
        deviceClient.sendReportedPropertiesAsync(reportedProperties);

        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_REPORTED_PROPERTY_ACKNOWLEDGEMENT);

        // Verify that the new reported property value can be seen from the service client
        Twin serviceClientTwin = twinClientServiceClient.get(deviceClient.getConfig().getDeviceId());

        Set<com.microsoft.azure.sdk.iot.service.twin.Pair> retrievedReportedProperties = serviceClientTwin.getReportedProperties();
        assertEquals(1, retrievedReportedProperties.size());
        com.microsoft.azure.sdk.iot.service.twin.Pair retrievedReportedPropertyPair = retrievedReportedProperties.iterator().next();
        assertTrue(retrievedReportedPropertyPair.getKey().equalsIgnoreCase(expectedPropertyKey));
        String actualReportedPropertyValue = retrievedReportedPropertyPair.getValue().toString();
        assertEquals(expectedReportedPropertyValue, actualReportedPropertyValue);
    }

    static class ConnectionStatusChangeTracker implements IotHubConnectionStatusChangeCallback
    {
        public boolean isOpen = false;

        // flags that, at some point, this device went into disconnected retrying state. May have recovered, though
        public boolean wentDisconnectedRetrying = false;

        public boolean clientClosedGracefully = false;

        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
        {
            if (status == IotHubConnectionStatus.CONNECTED)
            {
                isOpen = true;
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                isOpen = false;

                // client may close due to unexpected exception. For our test purposes, we want to validate that this callback gets fired
                // with reason CLIENT_CLOSE since that is the happy-path close status
                if (statusChangeReason == IotHubConnectionStatusChangeReason.CLIENT_CLOSE)
                {
                    clientClosedGracefully = true;
                }
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                wentDisconnectedRetrying = true;
                isOpen = false;
            }
        }
    }

    // Unregister a single device from an active multiplexed connection, test that other devices on that connection
    // can still be used to send telemetry.
    @Test
    @StandardTierHubOnlyTest
    public void registerClientAfterOpen() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);

        // Unregister one client so that it can be registered after the open call
        DeviceClient clientToRegisterAfterOpen = testInstance.deviceClientArray.get(DEVICE_MULTIPLEX_COUNT - 1);
        testInstance.multiplexingClient.unregisterDeviceClient(clientToRegisterAfterOpen);

        testInstance.multiplexingClient.open(false);

        ConnectionStatusChangeTracker connectionStatusChangeTracker = new ConnectionStatusChangeTracker();
        clientToRegisterAfterOpen.setConnectionStatusChangeCallback(connectionStatusChangeTracker, null);

        testInstance.multiplexingClient.registerDeviceClient(clientToRegisterAfterOpen);

        assertConnectionStateCallbackFiredConnected(connectionStatusChangeTracker, DEVICE_SESSION_OPEN_TIMEOUT);

        testSendingMessageFromDeviceClient(clientToRegisterAfterOpen);
    }

    // Unregister a single device from an active multiplexed connection, test that other devices on that connection
    // can still be used to send telemetry.
    @Test
    @StandardTierHubOnlyTest
    public void unregisterClientAfterOpen() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);

        // pick the 0th client to unregister after open. This will help make sure we don't have any dependencies on the 0th registered client
        DeviceClient clientToUnregisterAfterOpen = testInstance.deviceClientArray.get(0);

        ConnectionStatusChangeTracker connectionStatusChangeTracker = new ConnectionStatusChangeTracker();
        clientToUnregisterAfterOpen.setConnectionStatusChangeCallback(connectionStatusChangeTracker, null);

        testInstance.multiplexingClient.open(false);

        assertConnectionStateCallbackFiredConnected(connectionStatusChangeTracker, DEVICE_SESSION_OPEN_TIMEOUT);

        testInstance.multiplexingClient.unregisterDeviceClient(clientToUnregisterAfterOpen);

        assertDeviceSessionClosesGracefully(connectionStatusChangeTracker, DEVICE_SESSION_CLOSE_TIMEOUT);

        // start index from 1 since the 0th client was deliberately unregistered
        for (int i = 1; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            testSendingMessageFromDeviceClient(testInstance.deviceClientArray.get(i));
        }

        // verify that unregistered clients don't attempt to send messages on the active multiplexed connection after unregistration
        boolean exceptionThrown;
        try
        {
            testInstance.deviceClientArray.get(0).sendEventAsync(new Message("This message shouldn't be sent"), new EventCallback(IotHubStatusCode.OK_EMPTY), null);
            exceptionThrown = false;
        }
        catch (UnsupportedOperationException e)
        {
            exceptionThrown = true;
        }

        assertTrue("Expected exception to be thrown when sending a message from an unregistered client", exceptionThrown);
    }

    // Fault every device session, wait for it to recover, test sending from it, and verify that no other device sessions were dropped
    // other than the deliberately dropped session.
    @Test
    @ErrInjTest
    @IotHubTest
    public void multiplexedConnectionRecoversFromDeviceSessionDropsSequential() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        ConnectionStatusChangeTracker multiplexedConnectionStatusChangeTracker = new ConnectionStatusChangeTracker();
        testInstance.multiplexingClient.setConnectionStatusChangeCallback(multiplexedConnectionStatusChangeTracker, null);
        ConnectionStatusChangeTracker[] connectionStatusChangeTrackers = new ConnectionStatusChangeTracker[DEVICE_MULTIPLEX_COUNT];

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            connectionStatusChangeTrackers[i] = new ConnectionStatusChangeTracker();
            testInstance.deviceClientArray.get(i).setConnectionStatusChangeCallback(connectionStatusChangeTrackers[i], null);
        }

        testInstance.multiplexingClient.open(false);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            assertTrue("Multiplexing client opened successfully, but connection status change callback didn't execute.", connectionStatusChangeTrackers[i].isOpen);
        }

        // For each multiplexed device, use fault injection to drop the session and see if it can recover, one device at a time
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            log.info("Starting loop for device {}", testInstance.deviceClientArray.get(i).getConfig().getDeviceId());
            Message errorInjectionMessage = ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(1, 10);
            Success messageSendSuccess = testSendingMessageFromDeviceClient(testInstance.deviceClientArray.get(i), errorInjectionMessage);
            waitForMessageToBeAcknowledged(messageSendSuccess, "Timed out waiting for error injection message to be acknowledged");

            // Now that error injection message has been sent, need to wait for the device session to drop
            assertConnectionStateCallbackFiredDisconnectedRetrying(connectionStatusChangeTrackers[i]);

            // Next, the faulted device should eventually recover
            log.info("Waiting for device {} to reconnect", testInstance.deviceClientArray.get(i).getConfig().getDeviceId());
            assertConnectionStateCallbackFiredConnected(connectionStatusChangeTrackers[i], FAULT_INJECTION_RECOVERY_TIMEOUT_MILLIS);

            for (int j = i + 1; j < DEVICE_MULTIPLEX_COUNT; j++)
            {
                // devices above index i have not been deliberately faulted yet, so make sure they haven't seen a DISCONNECTED_RETRYING event yet.
                assertFalse("Multiplexed device that hasn't been deliberately faulted yet saw an unexpected DISCONNECTED_RETRYING connection status callback", connectionStatusChangeTrackers[j].wentDisconnectedRetrying);
            }

            // Try to send a message over the now-recovered device session
            testSendingMessageFromDeviceClient(testInstance.deviceClientArray.get(i));
        }

        // double check that the recovery of any particular device did not cause a device earlier in the array to lose connection
        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

        assertFalse(multiplexedConnectionStatusChangeTracker.wentDisconnectedRetrying);

        testInstance.multiplexingClient.close();

        assertMultiplexedDevicesClosedGracefully(connectionStatusChangeTrackers);
    }

    // Fault every device session basically at once, make sure that the clients all recover
    @Test
    @ErrInjTest
    @IotHubTest
    public void multiplexedConnectionRecoversFromDeviceSessionDropsParallel() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        ConnectionStatusChangeTracker[] connectionStatusChangeTrackers = new ConnectionStatusChangeTracker[DEVICE_MULTIPLEX_COUNT];

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            connectionStatusChangeTrackers[i] = new ConnectionStatusChangeTracker();
            testInstance.deviceClientArray.get(i).setConnectionStatusChangeCallback(connectionStatusChangeTrackers[i], null);
        }

        testInstance.multiplexingClient.open(false);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            assertTrue("Multiplexing client opened successfully, but connection status change callback didn't execute.", connectionStatusChangeTrackers[i].isOpen);
        }

        // For each multiplexed device, use fault injection to drop the session and see if it can recover, one device at a time
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            Message errorInjectionMessage = ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(1, 10);
            Success messageSendSuccess = testSendingMessageFromDeviceClient(testInstance.deviceClientArray.get(i), errorInjectionMessage);
            waitForMessageToBeAcknowledged(messageSendSuccess, "Timed out waiting for error injection message to be acknowledged");
        }

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // Now that error injection message has been sent, need to wait for the device session to drop
            assertConnectionStateCallbackFiredDisconnectedRetrying(connectionStatusChangeTrackers[i]);

            // Next, the faulted device should eventually recover
            assertConnectionStateCallbackFiredConnected(connectionStatusChangeTrackers[i], FAULT_INJECTION_RECOVERY_TIMEOUT_MILLIS);

            // Try to send a message over the now-recovered device session
            testSendingMessageFromDeviceClient(testInstance.deviceClientArray.get(i));
        }

        // double check that the recovery of any particular device did not cause a device earlier in the array to lose connection
        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

        testInstance.multiplexingClient.close();

        assertMultiplexedDevicesClosedGracefully(connectionStatusChangeTrackers);
    }

    // Open a multiplexed connection, send a fault injection message to drop the TCP connection, and ensure that the multiplexed
    // connection recovers
    @Test
    @ErrInjTest
    @IotHubTest
    public void multiplexedConnectionRecoversFromTcpConnectionDrop() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        ConnectionStatusChangeTracker multiplexedConnectionStatusChangeTracker = new ConnectionStatusChangeTracker();
        ConnectionStatusChangeTracker[] connectionStatusChangeTrackers = new ConnectionStatusChangeTracker[DEVICE_MULTIPLEX_COUNT];

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            connectionStatusChangeTrackers[i] = new ConnectionStatusChangeTracker();
            testInstance.deviceClientArray.get(i).setConnectionStatusChangeCallback(connectionStatusChangeTrackers[i], null);
        }

        testInstance.multiplexingClient.setConnectionStatusChangeCallback(multiplexedConnectionStatusChangeTracker, null);

        testInstance.multiplexingClient.open(false);

        assertTrue(
                "Multiplexed level connection status callback should have fired with CONNECTED after opening the multiplexing client",
                multiplexedConnectionStatusChangeTracker.isOpen);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            assertTrue("Multiplexing client opened successfully, but connection status change callback didn't execute.", connectionStatusChangeTrackers[i].isOpen);
        }

        Message errorInjectionMessage = ErrorInjectionHelper.tcpConnectionDropErrorInjectionMessage(1, 10);
        Success messageSendSuccess = testSendingMessageFromDeviceClient(testInstance.deviceClientArray.get(0), errorInjectionMessage);
        waitForMessageToBeAcknowledged(messageSendSuccess, "Timed out waiting for error injection message to be acknowledged");

        // Now that error injection message has been sent, need to wait for the device session to drop
        // Every registered device level connection status change callback should have fired with DISCONNECTED_RETRYING
        // and so should the multiplexing level connection status change callback
        assertConnectionStateCallbackFiredDisconnectedRetrying(multiplexedConnectionStatusChangeTracker);
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            assertConnectionStateCallbackFiredDisconnectedRetrying(connectionStatusChangeTrackers[i]);
        }

        // Now that the fault injection has taken place, make sure that the multiplexed connection and all of its device
        // sessions recover. Once recovered, try sending telemetry on each device.
        assertConnectionStateCallbackFiredConnected(multiplexedConnectionStatusChangeTracker, FAULT_INJECTION_RECOVERY_TIMEOUT_MILLIS);
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // The faulted device should eventually recover
            assertConnectionStateCallbackFiredConnected(connectionStatusChangeTrackers[i], FAULT_INJECTION_RECOVERY_TIMEOUT_MILLIS);

            // Try to send a message over the now-recovered device session
            testSendingMessageFromDeviceClient(testInstance.deviceClientArray.get(i));
        }

        // double check that the recovery of any particular device did not cause a device earlier in the array to lose connection
        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

        testInstance.multiplexingClient.close();

        assertTrue(
                "Multiplexed level connection status callback should have fired with DISCONNECTED after closing the multiplexing client",
                multiplexedConnectionStatusChangeTracker.clientClosedGracefully);

        assertMultiplexedDevicesClosedGracefully(connectionStatusChangeTrackers);
    }

    // Attempt to register a single device with the wrong connection string. The thrown exception
    // should contain all the exceptions thrown by the service.
    @ContinuousIntegrationTest
    @Test
    public void registerDeviceWithIncorrectCredentialsAfterOpenThrows() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);

        testInstance.multiplexingClient.unregisterDeviceClient(testInstance.deviceClientArray.get(0));

        testInstance.multiplexingClient.open(false);

        // Get a valid connection string, but swap out the deviceId for a deviceId that does exist, but whose symmetric key is different
        String incorrectConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, testInstance.deviceIdentityArray.get(1)).replace(testInstance.deviceIdentityArray.get(1).getDeviceId(), testInstance.deviceIdentityArray.get(0).getDeviceId());

        DeviceClient clientWithIncorrectCredentials = new DeviceClient(incorrectConnectionString, testInstance.protocol);

        boolean expectedExceptionThrown = false;
        try
        {
            testInstance.multiplexingClient.registerDeviceClient(clientWithIncorrectCredentials);
        }
        catch (MultiplexingClientDeviceRegistrationAuthenticationException e)
        {
            Map<String, Exception> registrationExceptions = e.getRegistrationExceptions();
            assertEquals(1, registrationExceptions.size());
            String deviceId = testInstance.deviceIdentityArray.get(0).getDeviceId();
            assertTrue(registrationExceptions.containsKey(deviceId));
            assertTrue(registrationExceptions.get(deviceId) instanceof UnauthorizedException);
            expectedExceptionThrown = true;
        }

        testInstance.multiplexingClient.close();

        assertTrue("Expected exception was not thrown", expectedExceptionThrown);
    }

    // Before opening the multiplexed connection, register a single device with incorrect credentials. Opening the client
    // should throw and the thrown exception should have details on why the open failed
    @ContinuousIntegrationTest
    @Test
    public void registerDeviceWithIncorrectCredentialsBeforeOpenThrowsOnOpen() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);

        testInstance.multiplexingClient.unregisterDeviceClient(testInstance.deviceClientArray.get(0));

        // Get a valid connection string, but swap out the deviceId for a deviceId that does exist, but whose symmetric key is different
        String incorrectConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, testInstance.deviceIdentityArray.get(1)).replace(testInstance.deviceIdentityArray.get(1).getDeviceId(), testInstance.deviceIdentityArray.get(0).getDeviceId());

        DeviceClient clientWithIncorrectCredentials = new DeviceClient(incorrectConnectionString, testInstance.protocol);
        testInstance.multiplexingClient.registerDeviceClient(clientWithIncorrectCredentials);

        boolean expectedExceptionThrown = false;
        try
        {
            testInstance.multiplexingClient.open(false);
        }
        catch (MultiplexingClientDeviceRegistrationAuthenticationException e)
        {
            Map<String, Exception> registrationExceptions = e.getRegistrationExceptions();
            assertEquals(1, registrationExceptions.size());
            String deviceId = testInstance.deviceIdentityArray.get(0).getDeviceId();
            assertTrue(registrationExceptions.containsKey(deviceId));
            assertTrue(registrationExceptions.get(deviceId) instanceof UnauthorizedException);
            expectedExceptionThrown = true;
        }

        testInstance.multiplexingClient.close();

        assertTrue("Expected exception was not thrown", expectedExceptionThrown);
    }

    // Attempt to register a batch of devices, all with the wrong connection string. The thrown exception
    // should contain all the exceptions thrown by the service.
    @ContinuousIntegrationTest
    @Test
    public void registerDevicesWithIncorrectCredentialsAfterOpenThrows() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            testInstance.multiplexingClient.unregisterDeviceClient(testInstance.deviceClientArray.get(i));
        }

        testInstance.multiplexingClient.open(false);

        List<DeviceClient> clientsWithIncorrectCredentials = new ArrayList<>();
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // shift the keys for each device so that device n uses key for device n + 1 (and final device uses key for device 0)
            String incorrectConnectionString;
            if (i == DEVICE_MULTIPLEX_COUNT - 1)
            {
                incorrectConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, testInstance.deviceIdentityArray.get(0));
                incorrectConnectionString = incorrectConnectionString.replace(testInstance.deviceIdentityArray.get(0).getDeviceId(), testInstance.deviceIdentityArray.get(i).getDeviceId());
            }
            else
            {
                incorrectConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, testInstance.deviceIdentityArray.get(i+1));
                incorrectConnectionString = incorrectConnectionString.replace(testInstance.deviceIdentityArray.get(i+1).getDeviceId(), testInstance.deviceIdentityArray.get(i).getDeviceId());
            }

            DeviceClient clientWithIncorrectCredentials = new DeviceClient(incorrectConnectionString, testInstance.protocol);
            clientsWithIncorrectCredentials.add(clientWithIncorrectCredentials);
        }

        boolean expectedExceptionThrown = false;
        try
        {
            testInstance.multiplexingClient.registerDeviceClients(clientsWithIncorrectCredentials);
        }
        catch (MultiplexingClientDeviceRegistrationAuthenticationException e)
        {
            Map<String, Exception> registrationExceptions = e.getRegistrationExceptions();
            assertEquals(DEVICE_MULTIPLEX_COUNT, registrationExceptions.size());
            for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
            {
                String deviceId = testInstance.deviceIdentityArray.get(i).getDeviceId();
                assertTrue(registrationExceptions.containsKey(deviceId));
                assertTrue(registrationExceptions.get(deviceId) instanceof UnauthorizedException);
            }

            expectedExceptionThrown = true;
        }

        testInstance.multiplexingClient.close();

        assertTrue("Expected exception was not thrown", expectedExceptionThrown);
    }

    @ContinuousIntegrationTest
    @Test
    public void registerDevicesWithIncorrectCredentialsBeforeOpenThrowsOnOpen() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            testInstance.multiplexingClient.unregisterDeviceClient(testInstance.deviceClientArray.get(i));
        }

        List<DeviceClient> clientsWithIncorrectCredentials = new ArrayList<>();
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // shift the keys for each device so that device n uses key for device n + 1 (and final device uses key for device 0)
            String incorrectConnectionString;
            if (i == DEVICE_MULTIPLEX_COUNT - 1)
            {
                incorrectConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, testInstance.deviceIdentityArray.get(0));
                incorrectConnectionString = incorrectConnectionString.replace(testInstance.deviceIdentityArray.get(0).getDeviceId(), testInstance.deviceIdentityArray.get(i).getDeviceId());
            }
            else
            {
                incorrectConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, testInstance.deviceIdentityArray.get(i+1));
                incorrectConnectionString =incorrectConnectionString.replace(testInstance.deviceIdentityArray.get(i+1).getDeviceId(), testInstance.deviceIdentityArray.get(i).getDeviceId());
            }
            DeviceClient clientWithIncorrectCredentials = new DeviceClient(incorrectConnectionString, testInstance.protocol);
            clientsWithIncorrectCredentials.add(clientWithIncorrectCredentials);
        }
        testInstance.multiplexingClient.registerDeviceClients(clientsWithIncorrectCredentials);

        boolean expectedExceptionThrown = false;
        try
        {
            testInstance.multiplexingClient.open(false);
        }
        catch (MultiplexingClientDeviceRegistrationAuthenticationException e)
        {
            Map<String, Exception> registrationExceptions = e.getRegistrationExceptions();
            assertEquals(DEVICE_MULTIPLEX_COUNT, registrationExceptions.size());
            for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
            {
                String deviceId = testInstance.deviceIdentityArray.get(i).getDeviceId();
                assertTrue(registrationExceptions.containsKey(deviceId));
                assertTrue(registrationExceptions.get(deviceId) instanceof UnauthorizedException);
            }

            expectedExceptionThrown = true;
        }

        testInstance.multiplexingClient.close();

        assertTrue("Expected exception was not thrown", expectedExceptionThrown);
    }

    @Test
    public void registrationsUnwindForMqttClient() throws Exception
    {
        Device mqttDevice = Tools.getTestDevice(iotHubConnectionString, IotHubClientProtocol.MQTT, AuthenticationType.SAS, false).getDevice();
        String deviceConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, mqttDevice);

        // MQTT clients should throw UnsupportedOperationException when registered
        DeviceClient mqttDeviceClient = new DeviceClient(deviceConnectionString, IotHubClientProtocol.MQTT);
        registrationsUnwindForUnsupportedOperationExceptions(mqttDeviceClient);
    }

    @Test
    public void registrationsUnwindForX509Client() throws Exception
    {
        // Create a new device client that uses x509 auth, which should throw an UnsupportedOperationException
        // since x509 auth isn't supported while multiplexing
        Device x509Device = Tools.getTestDevice(iotHubConnectionString, IotHubClientProtocol.MQTT, AuthenticationType.SELF_SIGNED, false).getDevice();
        String deviceConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, x509Device);
        ClientOptions options = ClientOptions.builder().sslContext(new IotHubSSLContext().getSSLContext()).build();
        DeviceClient x509DeviceClient = new DeviceClient(deviceConnectionString, testInstance.protocol, options);
        registrationsUnwindForUnsupportedOperationExceptions(x509DeviceClient);
    }

    @Test
    public void registrationsUnwindForAlreadyOpenClient() throws Exception
    {
        Device nonMultiplexedDevice = Tools.getTestDevice(iotHubConnectionString, testInstance.protocol, AuthenticationType.SAS, false).getDevice();
        String deviceConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, nonMultiplexedDevice);
        DeviceClient nonMultiplexedDeviceClient = new DeviceClient(deviceConnectionString, testInstance.protocol);

        //By opening the client once, this client can no longer be registered to a multiplexing client
        nonMultiplexedDeviceClient.open(false);
        registrationsUnwindForUnsupportedOperationExceptions(nonMultiplexedDeviceClient);
        nonMultiplexedDeviceClient.close();
    }

    @Test
    public void registrationsUnwindForClientOfDifferentHostName() throws Exception
    {
        Device nonMultiplexedDevice = Tools.getTestDevice(iotHubConnectionString, testInstance.protocol, AuthenticationType.SAS, false).getDevice();
        String deviceConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, nonMultiplexedDevice);

        // intentionally change the hostname of the device to simulate registering a device with a different hostname
        // to a multiplexing client. It shouldn't matter that the hostname itself isn't tied to an actual IoT Hub since
        // no network requests should be made before this hostname validation.

        String actualHostName = IotHubConnectionString.createIotHubConnectionString(iotHubConnectionString).getHostName();
        deviceConnectionString = deviceConnectionString.replace(actualHostName, "some-fake-host-name.azure-devices.net");

        DeviceClient deviceClientWithDifferentHostName = new DeviceClient(deviceConnectionString, testInstance.protocol);

        registrationsUnwindForUnsupportedOperationExceptions(deviceClientWithDifferentHostName);
    }

    @Test
    public void registrationsUnwindForDifferentProtocolClient() throws Exception
    {
        // Protocol for the new client is AMQPS if the test parameters are for AMQPS_WS, and vice versa. MultiplexingClient
        // should throw an exception since this new client's protocol doesn't match, even though both AMQPS and AMQPS_WS are valid
        // protocols
        IotHubClientProtocol protocol = testInstance.protocol == IotHubClientProtocol.AMQPS ? IotHubClientProtocol.AMQPS_WS : IotHubClientProtocol.AMQPS;

        Device newDevice = Tools.getTestDevice(iotHubConnectionString, protocol, AuthenticationType.SAS, false).getDevice();
        String deviceConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, newDevice);

        DeviceClient differentProtocolDeviceClient = new DeviceClient(deviceConnectionString, protocol);
        registrationsUnwindForUnsupportedOperationExceptions(differentProtocolDeviceClient);
    }

    // If you disable a device on an active multiplexed connection, that device session should drop and all the other
    // device sessions should be unaffected.
    @ContinuousIntegrationTest
    @Test
    public void disableDeviceAfterOpenAndAfterRegistration() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        ConnectionStatusChangeTracker multiplexedConnectionStatusChangeTracker = new ConnectionStatusChangeTracker();
        testInstance.multiplexingClient.setConnectionStatusChangeCallback(multiplexedConnectionStatusChangeTracker, null);

        ConnectionStatusChangeTracker[] connectionStatusChangeTrackers = new ConnectionStatusChangeTracker[DEVICE_MULTIPLEX_COUNT];
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            connectionStatusChangeTrackers[i] = new ConnectionStatusChangeTracker();
            testInstance.deviceClientArray.get(i).setConnectionStatusChangeCallback(connectionStatusChangeTrackers[i], null);
        }

        testInstance.multiplexingClient.open(false);

        // Disable a device that is on the multiplexed connection and that already has an open session
        Device deviceToDisable = registryClient.getDevice(testInstance.deviceIdentityArray.get(0).getDeviceId());
        deviceToDisable.setStatus(DeviceStatus.Disabled);
        registryClient.updateDevice(deviceToDisable);

        try
        {
            // verify that the disabled device loses its device session
            long startTime = System.currentTimeMillis();
            while (!connectionStatusChangeTrackers[0].wentDisconnectedRetrying)
            {
                Thread.sleep(200);

                if (System.currentTimeMillis() - startTime > FAULT_INJECTION_TIMEOUT_MILLIS)
                {
                    fail("Timed out waiting for the disabled device's client to report DISCONNECTED_RETRYING");
                }
            }

            assertFalse(connectionStatusChangeTrackers[0].isOpen);

            // Verify that the other devices on the multiplexed connection were unaffected
            for (int i = 1; i < DEVICE_MULTIPLEX_COUNT; i++)
            {
                assertFalse(connectionStatusChangeTrackers[i].wentDisconnectedRetrying);
                assertTrue(connectionStatusChangeTrackers[i].isOpen);
            }

            // Verify that the multiplexed connection itself was unaffected
            assertFalse(multiplexedConnectionStatusChangeTracker.wentDisconnectedRetrying);
            assertTrue(multiplexedConnectionStatusChangeTracker.isOpen);

            // Verify that the other devices can still send telemetry
            testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray.subList(1, DEVICE_MULTIPLEX_COUNT));

            testInstance.multiplexingClient.close();
        }
        finally
        {
            deviceToDisable.setStatus(DeviceStatus.Enabled); // re enable the device in case it gets recycled
            registryClient.updateDevice(deviceToDisable);
        }
    }

    // If you register a disabled device to a multiplexed connection that hasn't opened yet, the open call should succeed
    // but the disabled device's session should drop shortly afterwards and the other devices on the multiplexed connection
    // should be unaffected.
    @ContinuousIntegrationTest
    @Test
    public void disableDeviceBeforeOpen() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);

        String deviceIdToDisable = testInstance.deviceIdentityArray.get(0).getDeviceId();

        ConnectionStatusChangeTracker multiplexedConnectionStatusChangeTracker = new ConnectionStatusChangeTracker();
        testInstance.multiplexingClient.setConnectionStatusChangeCallback(multiplexedConnectionStatusChangeTracker, null);

        ConnectionStatusChangeTracker[] connectionStatusChangeTrackers = new ConnectionStatusChangeTracker[DEVICE_MULTIPLEX_COUNT];
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            connectionStatusChangeTrackers[i] = new ConnectionStatusChangeTracker();
            testInstance.deviceClientArray.get(i).setConnectionStatusChangeCallback(connectionStatusChangeTrackers[i], null);
        }

        // Disable a device that will be on the multiplexed connection when the multiplexed connection hasn't opened yet
        Device deviceToDisable = registryClient.getDevice(deviceIdToDisable);
        deviceToDisable.setStatus(DeviceStatus.Disabled);
        registryClient.updateDevice(deviceToDisable);

        try
        {
            testInstance.multiplexingClient.open(false);

            // verify that the disabled device eventually loses its device session
            long startTime = System.currentTimeMillis();
            while (!connectionStatusChangeTrackers[0].wentDisconnectedRetrying)
            {
                Thread.sleep(200);

                if (System.currentTimeMillis() - startTime > FAULT_INJECTION_TIMEOUT_MILLIS)
                {
                    fail("Timed out waiting for the disabled device's client to report DISCONNECTED_RETRYING");
                }
            }

            // Verify that the other devices on the multiplexed connection were unaffected
            for (int i = 1; i < DEVICE_MULTIPLEX_COUNT; i++)
            {
                assertTrue(connectionStatusChangeTrackers[i].isOpen);
            }

            // Verify that the multiplexed connection itself was unaffected
            assertFalse(multiplexedConnectionStatusChangeTracker.wentDisconnectedRetrying);
            assertTrue(multiplexedConnectionStatusChangeTracker.isOpen);

            // Verify that the other devices can still send telemetry
            testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray.subList(1, DEVICE_MULTIPLEX_COUNT));

            testInstance.multiplexingClient.close();
        }
        finally
        {
            deviceToDisable.setStatus(DeviceStatus.Enabled); // re enable the device in case it gets recycled
            registryClient.updateDevice(deviceToDisable);
        }
    }

    // If you register a disabled device to an active multiplexed connection, the other devices on the connection
    // should not be affected nor should the multiplexed connection itself.
    @ContinuousIntegrationTest
    @Test
    public void disableDeviceAfterOpenBeforeRegister() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);

        // Only register the soon-to-be-disabled device after opening the multiplexing client
        testInstance.multiplexingClient.unregisterDeviceClient(testInstance.deviceClientArray.get(0));

        ConnectionStatusChangeTracker multiplexedConnectionStatusChangeTracker = new ConnectionStatusChangeTracker();
        testInstance.multiplexingClient.setConnectionStatusChangeCallback(multiplexedConnectionStatusChangeTracker, null);

        ConnectionStatusChangeTracker[] connectionStatusChangeTrackers = new ConnectionStatusChangeTracker[DEVICE_MULTIPLEX_COUNT];
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            connectionStatusChangeTrackers[i] = new ConnectionStatusChangeTracker();
            testInstance.deviceClientArray.get(i).setConnectionStatusChangeCallback(connectionStatusChangeTrackers[i], null);
        }

        testInstance.multiplexingClient.open(false);

        // Disable a device that will be on the multiplexed connection
        Device deviceToDisable = registryClient.getDevice(testInstance.deviceIdentityArray.get(0).getDeviceId());
        deviceToDisable.setStatus(DeviceStatus.Disabled);
        registryClient.updateDevice(deviceToDisable);

        try
        {
            try
            {
                testInstance.multiplexingClient.registerDeviceClient(testInstance.deviceClientArray.get(0));
                fail("Registering a disabled device to an active multiplexing connection should have thrown an exception");
            }
            catch (MultiplexingClientDeviceRegistrationAuthenticationException ex)
            {
                assertTrue(ex.getRegistrationExceptions().containsKey(deviceToDisable.getDeviceId()));
            }

            // verify that the disabled device eventually loses its device session
            long startTime = System.currentTimeMillis();
            while (!connectionStatusChangeTrackers[0].wentDisconnectedRetrying)
            {
                Thread.sleep(200);

                if (System.currentTimeMillis() - startTime > FAULT_INJECTION_TIMEOUT_MILLIS)
                {
                    fail("Timed out waiting for the disabled device's client to report DISCONNECTED_RETRYING");
                }
            }

            assertFalse("Device failed to be registered, but the multiplexing client still reports it as registered",
                testInstance.multiplexingClient.isDeviceRegistered(deviceToDisable.getDeviceId()));

            for (int i = 1; i < DEVICE_MULTIPLEX_COUNT; i++)
            {
                assertTrue("One device failed to be registered, but the other devices should still have been registered.",
                    testInstance.multiplexingClient.isDeviceRegistered(testInstance.deviceClientArray.get(i).getConfig().getDeviceId()));
            }

            // Verify that the other devices on the multiplexed connection were unaffected
            for (int i = 1; i < DEVICE_MULTIPLEX_COUNT; i++)
            {
                assertFalse(connectionStatusChangeTrackers[i].wentDisconnectedRetrying);
                assertTrue(connectionStatusChangeTrackers[i].isOpen);
            }

            // Verify that the multiplexed connection itself was unaffected
            assertFalse(multiplexedConnectionStatusChangeTracker.wentDisconnectedRetrying);
            assertTrue(multiplexedConnectionStatusChangeTracker.isOpen);

            // Verify that the other devices can still send telemetry
            testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray.subList(1, DEVICE_MULTIPLEX_COUNT));

            testInstance.multiplexingClient.close();
        }
        finally
        {
            deviceToDisable.setStatus(DeviceStatus.Enabled); // re enable the device in case it gets recycled
            registryClient.updateDevice(deviceToDisable);
        }
    }

    public void registrationsUnwindForUnsupportedOperationExceptions(DeviceClient unsupportedDeviceClient) throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        this.testInstance.multiplexingClient.unregisterDeviceClients(this.testInstance.deviceClientArray);

        this.testInstance.deviceClientArray.add(unsupportedDeviceClient);

        boolean expectedExceptionThrown = false;
        try
        {
            this.testInstance.multiplexingClient.registerDeviceClients(this.testInstance.deviceClientArray);
        }
        catch (UnsupportedOperationException e)
        {
            expectedExceptionThrown = true;
        }

        assertTrue("Expected bulk registration to throw an UnsupportedOperationException", expectedExceptionThrown);

        // Because one of the devices in the bulk registration has some unsupported feature (MQTT protocol, X509 auth, etc.), none of the other clients
        // should have been registered.
        for (int i = 0; i < this.testInstance.deviceClientArray.size(); i++)
        {
            assertFalse(this.testInstance.multiplexingClient.isDeviceRegistered(this.testInstance.deviceClientArray.get(i).getConfig().getDeviceId()));
        }
    }

    @ContinuousIntegrationTest
    @Test
    public void failedRegistrationDoesNotAffectSubsequentRegistrations() throws Exception
    {
        testInstance.setup(0);
        testInstance.multiplexingClient.open(false);

        TestDeviceIdentity testDeviceIdentity =
            Tools.getTestDevice(iotHubConnectionString, this.testInstance.protocol, AuthenticationType.SAS, false);

        String deviceConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, testDeviceIdentity.getDevice());
        String deviceNotFoundConnectionString = deviceConnectionString.replace(testDeviceIdentity.getDeviceId(), testDeviceIdentity.getDeviceId().toUpperCase());
        DeviceClient validDeviceClient = new DeviceClient(deviceConnectionString, testInstance.protocol);
        DeviceClient invalidDeviceClient = new DeviceClient(deviceNotFoundConnectionString, testInstance.protocol);

        try
        {
            testInstance.multiplexingClient.registerDeviceClient(invalidDeviceClient);
            fail("Expected multiplexingClient to throw since it registered a device that did not exist.");
        }
        catch (MultiplexingClientDeviceRegistrationAuthenticationException e)
        {
            // expected throw since the deviceId in the connection string does not exist, ignore
        }

        testInstance.multiplexingClient.registerDeviceClient(validDeviceClient);

        testSendingMessageFromDeviceClient(validDeviceClient);

        testInstance.multiplexingClient.close();
    }

    // If a multiplexed device is subscribed to twin and/or methods and/or cloud to device messages, then loses its
    // session due to network issues, it should still be subscribed to twin and/or methods and/or cloud to device messages
    // after it finishes reconnection
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    @ErrInjTest
    @IotHubTest
    @Test
    public void multiplexedSessionsRecoverSubscriptionsFromDeviceSessionDrops() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT, MultiplexingClientOptions.builder().build(), true);
        ConnectionStatusChangeTracker multiplexedConnectionStatusChangeTracker = new ConnectionStatusChangeTracker();
        testInstance.multiplexingClient.setConnectionStatusChangeCallback(multiplexedConnectionStatusChangeTracker, null);
        ConnectionStatusChangeTracker[] connectionStatusChangeTrackers = new ConnectionStatusChangeTracker[DEVICE_MULTIPLEX_COUNT];

        TwinClient twinClientServiceClient =
            new TwinClient(iotHubConnectionString, TwinClientOptions.builder().httpReadTimeoutSeconds(0).build());

        DirectMethodsClient directMethodServiceClientClient =
            new DirectMethodsClient(iotHubConnectionString, DirectMethodsClientOptions.builder().httpReadTimeoutSeconds(0).build());

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            connectionStatusChangeTrackers[i] = new ConnectionStatusChangeTracker();
            testInstance.deviceClientArray.get(i).setConnectionStatusChangeCallback(connectionStatusChangeTrackers[i], null);
        }

        testInstance.multiplexingClient.open(false);

        // Subscribe to methods for all multiplexed clients
        DirectMethodCallback[] deviceDirectMethodCallbacks = new DirectMethodCallback[DEVICE_MULTIPLEX_COUNT];
        String[] expectedMethodNames = new String[DEVICE_MULTIPLEX_COUNT];
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            expectedMethodNames[i] = UUID.randomUUID().toString();
            deviceDirectMethodCallbacks[i] = new DirectMethodCallback(expectedMethodNames[i]);
            subscribeToDirectMethod(testInstance.deviceClientArray.get(i), deviceDirectMethodCallbacks[i]);
        }

        // Start twin for all multiplexed clients
        String[] expectedPropertyKeys = new String[DEVICE_MULTIPLEX_COUNT];
        String[] expectedPropertyValues = new String[DEVICE_MULTIPLEX_COUNT];
        TwinPropertyCallbackImpl[] twinPropertyCallbacks = new TwinPropertyCallbackImpl[DEVICE_MULTIPLEX_COUNT];
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // The twin for this test identity is about to be modified. Set this flag so that the test identity recycler re-uses this identity only for tests
            // that don't care about the initial twin state of an identity
            testInstance.testDevicesArrayIdentity.get(i).twinUpdated = true;
            expectedPropertyKeys[i] = UUID.randomUUID().toString();
            expectedPropertyValues[i] = UUID.randomUUID().toString();
            twinPropertyCallbacks[i] = new TwinPropertyCallbackImpl(expectedPropertyKeys[i], expectedPropertyValues[i]);
            startTwin(testInstance.deviceClientArray.get(i), new EventCallback(IotHubStatusCode.OK), twinPropertyCallbacks[i]);
        }

        // Subscribe to cloud to device messages for all multiplexed clients
        String[] expectedMessageCorrelationIds = new String[DEVICE_MULTIPLEX_COUNT];
        MessageCallback[] messageCallbacks = new MessageCallback[DEVICE_MULTIPLEX_COUNT];
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            expectedMessageCorrelationIds[i] = UUID.randomUUID().toString();
            messageCallbacks[i] = new MessageCallback(expectedMessageCorrelationIds[i]);
            testInstance.deviceClientArray.get(i).setMessageCallback(messageCallbacks[i], null);
        }

        // For each multiplexed device, use fault injection to drop the session and see if it can recover, one device at a time
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            log.info("Starting loop for device {}", testInstance.deviceClientArray.get(i).getConfig().getDeviceId());
            Message errorInjectionMessage = ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(1, 10);
            Success messageSendSuccess = testSendingMessageFromDeviceClient(testInstance.deviceClientArray.get(i), errorInjectionMessage);
            waitForMessageToBeAcknowledged(messageSendSuccess, "Timed out waiting for error injection message to be acknowledged");

            // Now that error injection message has been sent, need to wait for the device session to drop
            assertConnectionStateCallbackFiredDisconnectedRetrying(connectionStatusChangeTrackers[i]);

            // Next, the faulted device should eventually recover
            log.info("Waiting for device {} to reconnect", testInstance.deviceClientArray.get(i).getConfig().getDeviceId());
            assertConnectionStateCallbackFiredConnected(connectionStatusChangeTrackers[i], FAULT_INJECTION_RECOVERY_TIMEOUT_MILLIS);

            for (int j = i + 1; j < DEVICE_MULTIPLEX_COUNT; j++)
            {
                // devices above index i have not been deliberately faulted yet, so make sure they haven't seen a DISCONNECTED_RETRYING event yet.
                assertFalse("Multiplexed device that hasn't been deliberately faulted yet saw an unexpected DISCONNECTED_RETRYING connection status callback", connectionStatusChangeTrackers[j].wentDisconnectedRetrying);
            }
        }

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // test d2c telemetry
            testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

            // test receiving direct methods
            testDirectMethods(directMethodServiceClientClient, testInstance.deviceIdentityArray.get(i).getDeviceId(), expectedMethodNames[i], deviceDirectMethodCallbacks[i]);

            // Send desired property update to multiplexed device
            testDesiredPropertiesFlow(testInstance.deviceClientArray.get(i), twinClientServiceClient, twinPropertyCallbacks[i], expectedPropertyKeys[i], expectedPropertyValues[i]);

            // Testing sending reported properties
            testReportedPropertiesFlow(testInstance.deviceClientArray.get(i), twinClientServiceClient, expectedPropertyKeys[i], expectedPropertyValues[i]);

            testReceivingCloudToDeviceMessage(testInstance.deviceIdentityArray.get(i).getDeviceId(), messageCallbacks[i], expectedMessageCorrelationIds[i]);
        }

        assertFalse(multiplexedConnectionStatusChangeTracker.wentDisconnectedRetrying);

        testInstance.multiplexingClient.close();

        assertMultiplexedDevicesClosedGracefully(connectionStatusChangeTrackers);
    }

    private static void assertMultiplexedDevicesClosedGracefully(ConnectionStatusChangeTracker[] connectionStatusChangeTrackers)
    {
        for (ConnectionStatusChangeTracker connectionStatusChangeTracker : connectionStatusChangeTrackers)
        {
            assertTrue("Multiplexed device never reported closing as expected", connectionStatusChangeTracker.clientClosedGracefully);
        }
    }

    private static void assertConnectionStateCallbackFiredConnected(ConnectionStatusChangeTracker connectionStatusChangeTracker, int timeoutMillis) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (!connectionStatusChangeTracker.isOpen)
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > timeoutMillis)
            {
                fail("Timed out waiting for faulted device to become reconnected");
            }
        }
    }

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private static void assertDeviceSessionClosesGracefully(ConnectionStatusChangeTracker connectionStatusChangeTracker, int timeoutMillis) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (connectionStatusChangeTracker.isOpen)
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > timeoutMillis)
            {
                fail("Timed out waiting for faulted device to become reconnected");
            }
        }

        assertTrue("Device session was closed, but did not close gracefully", connectionStatusChangeTracker.clientClosedGracefully);
    }

    private static void assertConnectionStateCallbackFiredDisconnectedRetrying(ConnectionStatusChangeTracker connectionStatusChangeTracker) throws InterruptedException
    {
        long startTime = System.currentTimeMillis();
        while (!connectionStatusChangeTracker.wentDisconnectedRetrying)
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > FAULT_INJECTION_TIMEOUT_MILLIS)
            {
                fail("Timed out waiting for device to become disconnected-retrying");
            }
        }
    }
}
