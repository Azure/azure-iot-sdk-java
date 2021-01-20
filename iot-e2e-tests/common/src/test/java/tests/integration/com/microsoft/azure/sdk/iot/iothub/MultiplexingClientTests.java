/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub;


import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
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
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinClientOptions;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
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
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
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
@RunWith(Parameterized.class)
public class MultiplexingClientTests extends IntegrationTest
{
    private static final int DEVICE_MULTIPLEX_COUNT = 10;

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

    private static ServiceClient serviceClient;
    private static RegistryManager registryManager;

    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8849;
    protected static final String testProxyUser = "proxyUsername";
    protected static final char[] testProxyPass = "1234".toCharArray();

    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        serviceClient = ServiceClient.createFromConnectionString(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);
        serviceClient.open();

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

    public class MultiplexingClientTestInstance
    {
        public IotHubClientProtocol protocol;
        public List<Device> deviceIdentityArray;
        public List<DeviceClient> deviceClientArray;
        public MultiplexingClient multiplexingClient;

        public MultiplexingClientTestInstance(IotHubClientProtocol protocol)
        {
            this.protocol = protocol;
        }

        public void setup(int multiplexingDeviceSessionCount) throws InterruptedException, IotHubException, IOException, URISyntaxException, MultiplexingClientException
        {
            setup(multiplexingDeviceSessionCount, null);
        }

        public void setup(int multiplexingDeviceSessionCount, MultiplexingClientOptions options) throws InterruptedException, IotHubException, IOException, URISyntaxException, MultiplexingClientException
        {
            deviceIdentityArray = new ArrayList<>(multiplexingDeviceSessionCount);
            deviceClientArray = new ArrayList<>(multiplexingDeviceSessionCount);
            String uuid = UUID.randomUUID().toString();

            for (int i = 0; i < multiplexingDeviceSessionCount; i++)
            {
                String deviceId = "java-device-client-e2e-test-multiplexing".concat(i + "-" + uuid);
                deviceIdentityArray.add(i, Device.createFromId(deviceId, DeviceStatus.Enabled, new SymmetricKey()));
            }

            Tools.addDevicesWithRetry(deviceIdentityArray, iotHubConnectionString);

            IotHubConnectionString connectionString = IotHubConnectionString.createConnectionString(iotHubConnectionString);
            this.multiplexingClient = new MultiplexingClient(connectionString.getHostName(), this.protocol, options);
            for (int i = 0; i < multiplexingDeviceSessionCount; i++)
            {
                this.deviceClientArray.add(i, new DeviceClient(registryManager.getDeviceConnectionString(deviceIdentityArray.get(i)), this.protocol));
            }

            this.multiplexingClient.registerDeviceClients(this.deviceClientArray);
        }

        public void dispose()
        {
            try
            {
                for (Device deviceIdentity : deviceIdentityArray)
                {
                    registryManager.removeDevice(deviceIdentity);
                }
            }
            catch (Exception e)
            {
                // ignore the exception, don't care if tear down wasn't successful for this test
            }
        }
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        if (registryManager != null)
        {
            registryManager.close();
        }

        if (serviceClient != null)
        {
            serviceClient.close();
        }
    }

    @After
    public void tearDownTest()
    {
        testInstance.dispose();
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
    public void sendMessages() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.open();

        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

        testInstance.multiplexingClient.close();
    }

    // MultiplexingClient should be able to open an AMQP connection to IoTHub with no device sessions, and should
    // allow for device sessions to be added and used later.
    @Test
    public void openMultiplexingClientWithoutAnyRegisteredDevices() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.unregisterDeviceClients(testInstance.deviceClientArray);
        testInstance.multiplexingClient.open();

        testInstance.multiplexingClient.registerDeviceClients(testInstance.deviceClientArray);

        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

        testInstance.multiplexingClient.close();
    }

    @Test
    public void canUnregisterAllClientsThenReregisterAllClientsOnOpenConnection() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.open();

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
        testInstance.multiplexingClient.open();
        testInstance.multiplexingClient.close();

        // Re-open the connection and verify that it can still send telemetry
        testInstance.multiplexingClient.open();
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

        testInstance.multiplexingClient.open();

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
                    testInstances[finalI].multiplexingClient.open();
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
            testInstances[i].multiplexingClient.open();
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

    @ContinuousIntegrationTest
    @Test
    public void sendMessagesMaxDevicesAllowedRegisterAfterOpen() throws Exception
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

        // unregister all but the 0th device so that they can all be registered after opening the connection
        testInstance.multiplexingClient.unregisterDeviceClients(testInstance.deviceClientArray.subList(1, testInstance.deviceClientArray.size()));

        testInstance.multiplexingClient.open();

        testInstance.multiplexingClient.registerDeviceClients(testInstance.deviceClientArray);

        testSendingMessagesFromMultiplexedClients(testInstance.deviceClientArray);

        testInstance.multiplexingClient.close();
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
        testInstance.setup(DEVICE_MULTIPLEX_COUNT, MultiplexingClientOptions.builder().proxySettings(proxySettings).build());
        testInstance.multiplexingClient.open();

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
        testInstance.multiplexingClient.open();

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
        testInstance.multiplexingClient.open();
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
        com.microsoft.azure.sdk.iot.service.Message serviceMessage = new com.microsoft.azure.sdk.iot.service.Message("some payload");
        serviceMessage.setCorrelationId(expectedMessageCorrelationId);
        serviceClient.send(deviceId, serviceMessage);

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

    private class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
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
        testInstance.multiplexingClient.open();
        DeviceMethod deviceMethodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // Subscribe to methods on the multiplexed client
            String expectedMethodName = UUID.randomUUID().toString();
            DeviceMethodCallback deviceMethodCallback = new DeviceMethodCallback(expectedMethodName);
            subscribeToDeviceMethod(testInstance.deviceClientArray.get(i), deviceMethodCallback);
            testDeviceMethod(deviceMethodServiceClient, testInstance.deviceIdentityArray.get(i).getDeviceId(), expectedMethodName, deviceMethodCallback);
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
        testInstance.multiplexingClient.open();
        DeviceMethod deviceMethodServiceClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);
        List<DeviceMethodCallback> deviceMethodCallbacks = new ArrayList<>();
        List<String> expectedMethodNames = new ArrayList<>();

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // Subscribe to methods on the multiplexed client
            String expectedMethodName = UUID.randomUUID().toString();
            expectedMethodNames.add(expectedMethodName);
            DeviceMethodCallback deviceMethodCallback = new DeviceMethodCallback(expectedMethodName);
            deviceMethodCallbacks.add(deviceMethodCallback);
            subscribeToDeviceMethod(testInstance.deviceClientArray.get(i), deviceMethodCallback);
            testDeviceMethod(deviceMethodServiceClient, testInstance.deviceIdentityArray.get(i).getDeviceId(), expectedMethodNames.get(i), deviceMethodCallbacks.get(i));
        }

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            deviceMethodCallbacks.get(i).resetExpectations();
        }

        testInstance.multiplexingClient.unregisterDeviceClients(testInstance.deviceClientArray);
        testInstance.multiplexingClient.registerDeviceClients(testInstance.deviceClientArray);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            subscribeToDeviceMethod(testInstance.deviceClientArray.get(i), deviceMethodCallbacks.get(i));
            testDeviceMethod(deviceMethodServiceClient, testInstance.deviceIdentityArray.get(i).getDeviceId(), expectedMethodNames.get(i), deviceMethodCallbacks.get(i));
        }

        testInstance.multiplexingClient.close();
    }

    private static void testDeviceMethod(DeviceMethod deviceMethodServiceClient, String deviceId, String expectedMethodName, DeviceMethodCallback deviceMethodCallback) throws IOException, IotHubException, InterruptedException {
        // Give the method subscription some extra buffer time before invoking the method
        Thread.sleep(1000);

        // Invoke method on the multiplexed device
        deviceMethodServiceClient.invoke(deviceId, expectedMethodName, 200L, 200L, null);

        // No need to wait for the device to receive the method invocation since the service client call does that already
        assertTrue("Device method callback never fired on device", deviceMethodCallback.deviceMethodCallbackFired);
        assertTrue("Device method callback fired, but unexpected method name was received", deviceMethodCallback.expectedMethodReceived);
    }

    private static void subscribeToDeviceMethod(DeviceClient deviceClient, DeviceMethodCallback deviceMethodCallback) throws InterruptedException, IOException
    {
        Success methodsSubscribedSuccess = new Success();
        deviceClient.subscribeToDeviceMethod(deviceMethodCallback, null, new IotHubEventCallback() {
            @Override
            public void execute(IotHubStatusCode responseStatus, Object callbackContext) {
                ((Success) callbackContext).setCallbackStatusCode(responseStatus);
                ((Success) callbackContext).setResult(responseStatus == IotHubStatusCode.OK_EMPTY);
                ((Success) callbackContext).callbackWasFired();
            }
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

    private class DeviceMethodCallback implements com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback
    {
        public boolean deviceMethodCallbackFired = false;
        public boolean expectedMethodReceived = false;

        final String expectedMethodName;

        public DeviceMethodCallback(String expectedMethodName)
        {
            this.expectedMethodName = expectedMethodName;
        }

        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context) {
            deviceMethodCallbackFired = true;
            if (methodName.equals(expectedMethodName))
            {
                expectedMethodReceived = true;
            }

            return new DeviceMethodData(200, null);
        }

        public void resetExpectations()
        {
            deviceMethodCallbackFired = false;
            expectedMethodReceived = false;
        }
    }

    class TwinPropertyCallBackImpl implements TwinPropertyCallBack
    {
        String expectedKey;
        String expectedValue;

        public boolean receivedCallback = false;
        public boolean receivedExpectedKey = false;
        public boolean receivedExpectedValue = false;

        public String actualKey;
        public String actualValue;

        public TwinPropertyCallBackImpl(String expectedKey, String expectedValue)
        {
            this.expectedKey = expectedKey;
            this.expectedValue = expectedValue;
        }

        @Override
        public void TwinPropertyCallBack(Property property, Object context)
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
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.open();

        DeviceTwin deviceTwinServiceClient = DeviceTwin.createFromConnectionString(iotHubConnectionString, DeviceTwinClientOptions.builder().httpReadTimeout(0).build());

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            String expectedPropertyKey = UUID.randomUUID().toString();
            String expectedPropertyValue = UUID.randomUUID().toString();
            TwinPropertyCallBackImpl twinPropertyCallBack = new TwinPropertyCallBackImpl(expectedPropertyKey, expectedPropertyValue);
            startTwin(testInstance.deviceClientArray.get(i), new EventCallback(IotHubStatusCode.OK), twinPropertyCallBack);

            // Testing subscribing to desired properties
            Map<Property, Pair<TwinPropertyCallBack, Object>> onDesiredPropertyChange = new HashMap<>();
            onDesiredPropertyChange.put(new Property(expectedPropertyKey, null), new Pair<>(twinPropertyCallBack, null));
            testInstance.deviceClientArray.get(i).subscribeToTwinDesiredProperties(onDesiredPropertyChange);

            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_DESIRED_PROPERTY_SUBSCRIPTION_ACKNOWLEDGEMENT);

            // Send desired property update to multiplexed device
            testDesiredPropertiesFlow(testInstance.deviceClientArray.get(i), deviceTwinServiceClient, twinPropertyCallBack, expectedPropertyKey, expectedPropertyValue);

            // Testing sending reported properties
            testReportedPropertiesFlow(testInstance.deviceClientArray.get(i), deviceTwinServiceClient, expectedPropertyKey, expectedPropertyValue);
        }
    }

    // Twin subscriptions and callbacks should not be preserved between registrations by default
    @Test
    @ContinuousIntegrationTest
    @StandardTierHubOnlyTest
    public void twinSubscriptionNotPreservedByDeviceClientAfterUnregistration() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        testInstance.multiplexingClient.open();

        DeviceTwin deviceTwinServiceClient = DeviceTwin.createFromConnectionString(iotHubConnectionString, DeviceTwinClientOptions.builder().httpReadTimeout(0).build());
        String expectedPropertyKey = UUID.randomUUID().toString();
        String expectedPropertyValue = UUID.randomUUID().toString();
        List<TwinPropertyCallBackImpl> twinPropertyCallBacks = new ArrayList<>();
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++) {

            TwinPropertyCallBackImpl twinPropertyCallBack = new TwinPropertyCallBackImpl(expectedPropertyKey, expectedPropertyValue);
            twinPropertyCallBacks.add(twinPropertyCallBack);
            startTwin(testInstance.deviceClientArray.get(i), new EventCallback(IotHubStatusCode.OK), twinPropertyCallBack);
        }

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++) {
            // Testing subscribing to desired properties
            testDesiredPropertiesFlow(testInstance.deviceClientArray.get(i), deviceTwinServiceClient, twinPropertyCallBacks.get(i), expectedPropertyKey, expectedPropertyValue);

            // Testing sending reported properties
            testReportedPropertiesFlow(testInstance.deviceClientArray.get(i), deviceTwinServiceClient, expectedPropertyKey, expectedPropertyValue);
        }

        // unregister and then re-register the clients to see if their subscriptions were preserved or not
        testInstance.multiplexingClient.unregisterDeviceClients(testInstance.deviceClientArray);
        testInstance.multiplexingClient.registerDeviceClients(testInstance.deviceClientArray);

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++) {
            boolean expectedExceptionThrown = false;
            try
            {
                // Testing sending reported properties
                testReportedPropertiesFlow(testInstance.deviceClientArray.get(i), deviceTwinServiceClient, expectedPropertyKey, expectedPropertyValue);
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

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++) {

            TwinPropertyCallBackImpl twinPropertyCallBack = new TwinPropertyCallBackImpl(expectedPropertyKey, expectedPropertyValue);
            twinPropertyCallBacks.add(twinPropertyCallBack);
            startTwin(testInstance.deviceClientArray.get(i), new EventCallback(IotHubStatusCode.OK), twinPropertyCallBack);
        }

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++) {
            // Testing subscribing to desired properties
            testDesiredPropertiesFlow(testInstance.deviceClientArray.get(i), deviceTwinServiceClient, twinPropertyCallBacks.get(i), expectedPropertyKey, expectedPropertyValue);

            // Testing sending reported properties
            testReportedPropertiesFlow(testInstance.deviceClientArray.get(i), deviceTwinServiceClient, expectedPropertyKey, expectedPropertyValue);
        }
    }

    private static void startTwin(DeviceClient deviceClient, IotHubEventCallback twinEventCallback, TwinPropertyCallBack twinPropertyCallBack) throws IOException, InterruptedException {
        Success twinStarted = new Success();
        deviceClient.startDeviceTwin(twinEventCallback, twinStarted, twinPropertyCallBack, null);

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

    private static void testDesiredPropertiesFlow(DeviceClient deviceClient, DeviceTwin deviceTwinServiceClient, TwinPropertyCallBackImpl twinPropertyCallBack, String expectedPropertyKey, String expectedPropertyValue) throws IOException, IotHubException, InterruptedException {
        DeviceTwinDevice serviceClientTwin = new DeviceTwinDevice(deviceClient.getConfig().getDeviceId());
        Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new com.microsoft.azure.sdk.iot.service.devicetwin.Pair(expectedPropertyKey, expectedPropertyValue));
        serviceClientTwin.setDesiredProperties(desiredProperties);
        deviceTwinServiceClient.updateTwin(serviceClientTwin);

        long startTime = System.currentTimeMillis();
        while (!twinPropertyCallBack.receivedCallback)
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > DESIRED_PROPERTY_CALLBACK_TIMEOUT_MILLIS)
            {
                fail("Timed out waiting for desired property callback to fire");
            }
        }

        assertTrue("Desired property callback fired with unexpected key. Expected " + expectedPropertyKey + " but was " + twinPropertyCallBack.actualKey, twinPropertyCallBack.receivedExpectedKey);
        assertTrue("Desired property callback fired with unexpected value. Expected " + expectedPropertyValue + " but was " + twinPropertyCallBack.actualValue, twinPropertyCallBack.receivedExpectedValue);
    }

    private static void testReportedPropertiesFlow(DeviceClient deviceClient, DeviceTwin deviceTwinServiceClient, String expectedPropertyKey, String expectedPropertyValue) throws IOException, IotHubException, InterruptedException {
        DeviceTwinDevice serviceClientTwin = new DeviceTwinDevice(deviceClient.getConfig().getDeviceId());
        String expectedReportedPropertyValue = expectedPropertyValue + "-reported";
        Set<Property> reportedProperties = new HashSet<>();
        reportedProperties.add(new Property(expectedPropertyKey, expectedReportedPropertyValue));
        deviceClient.sendReportedProperties(reportedProperties);

        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_REPORTED_PROPERTY_ACKNOWLEDGEMENT);

        // Verify that the new reported property value can be seen from the service client
        deviceTwinServiceClient.getTwin(serviceClientTwin);

        Set<com.microsoft.azure.sdk.iot.service.devicetwin.Pair> retrievedReportedProperties = serviceClientTwin.getReportedProperties();
        assertEquals(1, retrievedReportedProperties.size());
        com.microsoft.azure.sdk.iot.service.devicetwin.Pair retrievedReportedPropertyPair = retrievedReportedProperties.iterator().next();
        assertTrue(retrievedReportedPropertyPair.getKey().equalsIgnoreCase(expectedPropertyKey));
        String actualReportedPropertyValue = retrievedReportedPropertyPair.getValue().toString();
        assertEquals(expectedReportedPropertyValue, actualReportedPropertyValue);
    }

    class ConnectionStatusChangeTracker implements IotHubConnectionStatusChangeCallback
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

        testInstance.multiplexingClient.open();

        ConnectionStatusChangeTracker connectionStatusChangeTracker = new ConnectionStatusChangeTracker();
        clientToRegisterAfterOpen.registerConnectionStatusChangeCallback(connectionStatusChangeTracker, null);

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
        clientToUnregisterAfterOpen.registerConnectionStatusChangeCallback(connectionStatusChangeTracker, null);

        testInstance.multiplexingClient.open();

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
    public void multiplexedConnectionRecoversFromDeviceSessionDropsSequential() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        ConnectionStatusChangeTracker[] connectionStatusChangeTrackers = new ConnectionStatusChangeTracker[DEVICE_MULTIPLEX_COUNT];

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            connectionStatusChangeTrackers[i] = new ConnectionStatusChangeTracker();
            testInstance.deviceClientArray.get(i).registerConnectionStatusChangeCallback(connectionStatusChangeTrackers[i], null);
        }

        testInstance.multiplexingClient.open();

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            assertTrue("Multiplexing client opened successfully, but connection status change callback didn't execute.", connectionStatusChangeTrackers[i].isOpen);
        }

        // For each multiplexed device, use fault injection to drop the session and see if it can recover, one device at a time
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            log.info("Starting loop for device {}", testInstance.deviceClientArray.get(i).getConfig().getDeviceId());
            Message errorIjectionMessage = ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(1, 10);
            Success messageSendSuccess = testSendingMessageFromDeviceClient(testInstance.deviceClientArray.get(i), errorIjectionMessage);
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

        testInstance.multiplexingClient.close();

        assertMultiplexedDevicesClosedGracefully(connectionStatusChangeTrackers);
    }

    // Fault every device session basically at once, make sure that the clients all recover
    @Test
    public void multiplexedConnectionRecoversFromDeviceSessionDropsParallel() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        ConnectionStatusChangeTracker[] connectionStatusChangeTrackers = new ConnectionStatusChangeTracker[DEVICE_MULTIPLEX_COUNT];

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            connectionStatusChangeTrackers[i] = new ConnectionStatusChangeTracker();
            testInstance.deviceClientArray.get(i).registerConnectionStatusChangeCallback(connectionStatusChangeTrackers[i], null);
        }

        testInstance.multiplexingClient.open();

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            assertTrue("Multiplexing client opened successfully, but connection status change callback didn't execute.", connectionStatusChangeTrackers[i].isOpen);
        }

        // For each multiplexed device, use fault injection to drop the session and see if it can recover, one device at a time
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            Message errorIjectionMessage = ErrorInjectionHelper.amqpsSessionDropErrorInjectionMessage(1, 10);
            Success messageSendSuccess = testSendingMessageFromDeviceClient(testInstance.deviceClientArray.get(i), errorIjectionMessage);
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
    public void multiplexedConnectionRecoversFromTcpConnectionDrop() throws Exception
    {
        testInstance.setup(DEVICE_MULTIPLEX_COUNT);
        ConnectionStatusChangeTracker multiplexedConnectionStatusChangeTracker = new ConnectionStatusChangeTracker();
        ConnectionStatusChangeTracker[] connectionStatusChangeTrackers = new ConnectionStatusChangeTracker[DEVICE_MULTIPLEX_COUNT];

        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            connectionStatusChangeTrackers[i] = new ConnectionStatusChangeTracker();
            testInstance.deviceClientArray.get(i).registerConnectionStatusChangeCallback(connectionStatusChangeTrackers[i], null);
        }

        testInstance.multiplexingClient.registerConnectionStatusChangeCallback(multiplexedConnectionStatusChangeTracker, null);

        testInstance.multiplexingClient.open();

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

        testInstance.multiplexingClient.open();

        // Get a valid connection string, but swap out the deviceId for a deviceId that does exist, but whose symmetric key is different
        String incorrectConnectionString = registryManager.getDeviceConnectionString(testInstance.deviceIdentityArray.get(1)).replace(testInstance.deviceIdentityArray.get(1).getDeviceId(), testInstance.deviceIdentityArray.get(0).getDeviceId());

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
        String incorrectConnectionString = registryManager.getDeviceConnectionString(testInstance.deviceIdentityArray.get(1)).replace(testInstance.deviceIdentityArray.get(1).getDeviceId(), testInstance.deviceIdentityArray.get(0).getDeviceId());

        DeviceClient clientWithIncorrectCredentials = new DeviceClient(incorrectConnectionString, testInstance.protocol);
        testInstance.multiplexingClient.registerDeviceClient(clientWithIncorrectCredentials);

        boolean expectedExceptionThrown = false;
        try
        {
            testInstance.multiplexingClient.open();
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

        testInstance.multiplexingClient.open();

        List<DeviceClient> clientsWithIncorrectCredentials = new ArrayList<>();
        for (int i = 0; i < DEVICE_MULTIPLEX_COUNT; i++)
        {
            // shift the keys for each device so that device n uses key for device n + 1 (and final device uses key for device 0)
            String incorrectConnectionString;
            if (i == DEVICE_MULTIPLEX_COUNT - 1)
            {
                incorrectConnectionString = registryManager.getDeviceConnectionString(testInstance.deviceIdentityArray.get(0));
                incorrectConnectionString = incorrectConnectionString.replace(testInstance.deviceIdentityArray.get(0).getDeviceId(), testInstance.deviceIdentityArray.get(i).getDeviceId());
            }
            else
            {
                incorrectConnectionString = registryManager.getDeviceConnectionString(testInstance.deviceIdentityArray.get(i+1));
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
                incorrectConnectionString = registryManager.getDeviceConnectionString(testInstance.deviceIdentityArray.get(0));
                incorrectConnectionString = incorrectConnectionString.replace(testInstance.deviceIdentityArray.get(0).getDeviceId(), testInstance.deviceIdentityArray.get(i).getDeviceId());
            }
            else
            {
                incorrectConnectionString = registryManager.getDeviceConnectionString(testInstance.deviceIdentityArray.get(i+1));
                incorrectConnectionString =incorrectConnectionString.replace(testInstance.deviceIdentityArray.get(i+1).getDeviceId(), testInstance.deviceIdentityArray.get(i).getDeviceId());
            }
            DeviceClient clientWithIncorrectCredentials = new DeviceClient(incorrectConnectionString, testInstance.protocol);
            clientsWithIncorrectCredentials.add(clientWithIncorrectCredentials);
        }
        testInstance.multiplexingClient.registerDeviceClients(clientsWithIncorrectCredentials);

        boolean expectedExceptionThrown = false;
        try
        {
            testInstance.multiplexingClient.open();
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
        Device newDevice = Device.createDevice(UUID.randomUUID().toString(), AuthenticationType.SAS);
        registryManager.addDevice(newDevice);
        String deviceConnectionString = registryManager.getDeviceConnectionString(newDevice);

        // MQTT clients should throw UnsupportedOperationException when registered
        DeviceClient mqttDeviceClient = new DeviceClient(deviceConnectionString, IotHubClientProtocol.MQTT);
        registrationsUnwindForUnsupportedOperationExceptions(mqttDeviceClient);
    }

    @Test
    public void registrationsUnwindForX509Client() throws Exception
    {
        // Create a new device client that uses x509 auth, which should throw an UnsupportedOperationException
        // since x509 auth isn't supported while multiplexing
        Device x509Device = Device.createDevice(UUID.randomUUID().toString(), AuthenticationType.SELF_SIGNED);
        registryManager.addDevice(x509Device);
        String deviceConnectionString = registryManager.getDeviceConnectionString(x509Device);
        DeviceClient x509DeviceClient = new DeviceClient(deviceConnectionString, testInstance.protocol, new IotHubSSLContext().getSSLContext());
        registrationsUnwindForUnsupportedOperationExceptions(x509DeviceClient);
    }

    @Test
    public void registrationsUnwindForAlreadyOpenClient() throws Exception
    {
        Device nonMultiplexedDevice = Device.createDevice(UUID.randomUUID().toString(), AuthenticationType.SAS);
        registryManager.addDevice(nonMultiplexedDevice);
        String deviceConnectionString = registryManager.getDeviceConnectionString(nonMultiplexedDevice);
        DeviceClient nonMultiplexedDeviceClient = new DeviceClient(deviceConnectionString, testInstance.protocol);

        //By opening the client once, this client can no longer be registered to a multiplexing client
        nonMultiplexedDeviceClient.open();
        registrationsUnwindForUnsupportedOperationExceptions(nonMultiplexedDeviceClient);
        nonMultiplexedDeviceClient.closeNow();
    }

    @Test
    public void registrationsUnwindForClientOfDifferentHostName() throws Exception
    {
        Device nonMultiplexedDevice = Device.createDevice(UUID.randomUUID().toString(), AuthenticationType.SAS);
        registryManager.addDevice(nonMultiplexedDevice);
        String deviceConnectionString = registryManager.getDeviceConnectionString(nonMultiplexedDevice);

        // intentionally change the hostname of the device to simulate registering a device with a different hostname
        // to a multiplexing client. It shouldn't matter that the hostname itself isn't tied to an actual IoT Hub since
        // no network requests should be made before this hostname validation.

        String actualHostName = IotHubConnectionString.createConnectionString(iotHubConnectionString).getHostName();
        deviceConnectionString = deviceConnectionString.replace(actualHostName, "some-fake-host-name.azure-devices.net");

        DeviceClient deviceClientWithDifferentHostName = new DeviceClient(deviceConnectionString, testInstance.protocol);

        registrationsUnwindForUnsupportedOperationExceptions(deviceClientWithDifferentHostName);
    }

    @Test
    public void registrationsUnwindForDifferentProtocolClient() throws Exception
    {
        Device newDevice = Device.createDevice(UUID.randomUUID().toString(), AuthenticationType.SAS);
        registryManager.addDevice(newDevice);
        String deviceConnectionString = registryManager.getDeviceConnectionString(newDevice);

        // Protocol for the new client is AMQPS if the test parameters are for AMQPS_WS, and vice versa. MultiplexingClient
        // should throw an exception since this new client's protocol doesn't match, even though both AMQPS and AMQPS_WS are valid
        // protocols
        IotHubClientProtocol protocol = testInstance.protocol == IotHubClientProtocol.AMQPS ? IotHubClientProtocol.AMQPS_WS : IotHubClientProtocol.AMQPS;

        DeviceClient differentProtocolDeviceClient = new DeviceClient(deviceConnectionString, protocol);
        registrationsUnwindForUnsupportedOperationExceptions(differentProtocolDeviceClient);
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
