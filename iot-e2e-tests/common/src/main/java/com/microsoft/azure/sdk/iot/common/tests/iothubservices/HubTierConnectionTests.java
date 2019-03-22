package com.microsoft.azure.sdk.iot.common.tests.iothubservices;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.setup.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import org.junit.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.CERTIFICATE_AUTHORITY;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertNotNull;

public class HubTierConnectionTests extends IntegrationTest
{
    protected static final long WAIT_FOR_DISCONNECT_TIMEOUT = 1 * 60 * 1000; // 1 minute

    //How much devices the multithreaded test will create in parallel.
    protected static final Integer MAX_DEVICE_PARALLEL = 3;

    //How many keys each message will cary.
    protected static final Integer NUM_KEYS_PER_MESSAGE = 3;

    // How much to wait until a message makes it to the server, in milliseconds
    protected static final Integer SEND_TIMEOUT_MILLISECONDS = 180000;

    //How many milliseconds between retry
    protected static final Integer RETRY_MILLISECONDS = 100;

    protected static String iotHubConnectionString = "";
    protected static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;

    protected static String hostName;

    //The messages to be sent in these tests. Some contain error injection messages surrounded by normal messages
    protected static final List<MessageAndResult> NORMAL_MESSAGES_TO_SEND = new ArrayList<>();

    public HubTierConnectionTestInstance testInstance;

    //How much messages each device will send to the hub for each connection.
    protected static final Integer NUM_MESSAGES_PER_CONNECTION = 6;

    protected static final AtomicBoolean succeed = new AtomicBoolean();

    protected static RegistryManager registryManager;

    public HubTierConnectionTests(DeviceClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        this.testInstance = new HubTierConnectionTestInstance(client, protocol, identity, authenticationType, publicKeyCert, privateKey, x509Thumbprint);
    }

    @BeforeClass
    public static void classSetup()
    {
        try
        {
            registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Unexpected exception encountered");
        }
    }

    public class HubTierConnectionTestInstance
    {
        public DeviceClient client;
        public IotHubClientProtocol protocol;
        public BaseDevice identity;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public CorrelationDetailsLoggingAssert correlationDetailsLoggingAssert;

        public HubTierConnectionTestInstance(DeviceClient client, IotHubClientProtocol protocol, BaseDevice identity, AuthenticationType authenticationType, String publicKeyCert, String privateKey, String x509Thumbprint)
        {
            this.client = client;
            this.protocol = protocol;
            this.identity = identity;
            this.authenticationType = authenticationType;
            this.publicKeyCert = publicKeyCert;
            this.privateKey = privateKey;
            this.x509Thumbprint = x509Thumbprint;
            String deviceId = identity.getDeviceId();

            this.correlationDetailsLoggingAssert = new CorrelationDetailsLoggingAssert(this.client.getConfig().getIotHubHostname(), deviceId, protocol.toString(), null);
        }
    }

    protected static Collection inputsCommon() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException, InterruptedException
    {
        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator();
        return inputsCommon(certificateGenerator.getPublicCertificate(), certificateGenerator.getPrivateKey(), certificateGenerator.getX509Thumbprint());
    }

    protected static Collection inputsCommon(String publicKeyCert, String privateKey, String x509Thumbprint) throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException, InterruptedException
    {
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        String uuid = UUID.randomUUID().toString();
        String deviceId = "java-tier-connection-e2e-test".concat("-" + uuid);
        String deviceIdX509 = "java-tier-connection-e2e-test-X509".concat("-" + uuid);

        Device device = Device.createFromId(deviceId, null, null);
        Device deviceX509 = Device.createDevice(deviceIdX509, SELF_SIGNED);

        deviceX509.setThumbprintFinal(x509Thumbprint, x509Thumbprint);

        Tools.addDeviceWithRetry(registryManager, device);
        Tools.addDeviceWithRetry(registryManager, deviceX509);

        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();

        List inputs = Arrays.asList(
            new Object[][]
                {
                    //sas token device client
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS), AMQPS, device, SAS, publicKeyCert, privateKey, x509Thumbprint},
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), AMQPS_WS), AMQPS_WS, device, SAS, publicKeyCert, privateKey, x509Thumbprint},

                    //x509 device client
                    {new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509), AMQPS, publicKeyCert, false, privateKey, false), AMQPS, deviceX509, SELF_SIGNED, publicKeyCert, privateKey, x509Thumbprint}
                }
        );

        Thread.sleep(2000);

        return inputs;
    }

    protected static class DeviceMethodCallback implements com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback
    {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            return new DeviceMethodData(200, "payload");
        }
    }

    protected static class DeviceMethodStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("Device Client: IoT Hub responded to device method operation with status " + status.name());
        }
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BasicTierOnlyRule.class)
    public void enableMethodFailedWithBasicTier() throws IOException, InterruptedException
    {
        //arrange
        List<Pair<IotHubConnectionStatus, Throwable>> connectionStatusUpdates = new ArrayList<>();
        testInstance.client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback()
        {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
            {
                connectionStatusUpdates.add(new Pair<>(status, throwable));
            }
        }, null);

        IotHubServicesCommon.openClientWithRetry(testInstance.client);

        //act
        testInstance.client.subscribeToDeviceMethod(new DeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);

        //assert
        waitForDisconnect(connectionStatusUpdates, WAIT_FOR_DISCONNECT_TIMEOUT, testInstance.client);
        testInstance.client.closeNow();
    }

    public static Collection<BaseDevice> getIdentities(Collection inputs)
    {
        Set<BaseDevice> identities = new HashSet<>();

        Object[] inputArray = inputs.toArray();
        for (int i = 0; i < inputs.size(); i++)
        {
            Object[] inputsInstance = (Object[]) inputArray[i];
            identities.add((BaseDevice) inputsInstance[2]);
        }

        return identities;
    }

    protected static void tearDown(Collection<BaseDevice> identitiesToDispose)
    {
        if (registryManager != null)
        {
            Tools.removeDevicesAndModules(registryManager, identitiesToDispose);
            registryManager.close();
            registryManager = null;
        }
    }

    public static void waitForDisconnect(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, long timeout, InternalClient client) throws InterruptedException
    {
        //Wait for DISCONNECTED_RETRYING
        long startTime = System.currentTimeMillis();
        while (!actualStatusUpdatesContainsStatus(actualStatusUpdates, IotHubConnectionStatus.DISCONNECTED))
        {
            Thread.sleep(200);
            long timeElapsed = System.currentTimeMillis() - startTime;
            if (timeElapsed > timeout)
            {
                Assert.fail(buildExceptionMessage("Timed out waiting for disconnection to take effect", client));
            }
        }
    }

    public static boolean actualStatusUpdatesContainsStatus(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, IotHubConnectionStatus status)
    {
        for (int i = 0; i < actualStatusUpdates.size(); i++)
        {
            if (actualStatusUpdates.get(i).getKey() == status)
            {
                return true;
            }
        }

        return false;
    }
}
