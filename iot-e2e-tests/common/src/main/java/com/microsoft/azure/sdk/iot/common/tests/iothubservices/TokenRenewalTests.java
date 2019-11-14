/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static junit.framework.TestCase.assertTrue;

public class TokenRenewalTests extends IntegrationTest
{
    protected static String iotHubConnectionString;
    private static RegistryManager registryManager;
    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8898;
    protected static final String testProxyUser = "proxyUsername";
    protected static final char[] testProxyPass = "1234".toCharArray();

    private static final Integer SEND_TIMEOUT_MILLISECONDS = 60000;
    private static final Integer RETRY_MILLISECONDS = 100;

    public static void setup() throws IOException
    {
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
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

    /**
     * Note, this test takes at least 10 minutes to run as a token has a 10 minute grace period from the service to stay alive after it has technically expired
     * This test behaves a bit differently across protocol due to amqp proactively renewing sas tokens, and http not having connection status callback
     * Amqps/Amqps_ws : Expect the cbs link to send a new sas token before the old token expires
     * Mqtt/Mqtt_ws   : Expect the connection to be lost briefly, but re-established with a new sas token
     * Http           : No connection status callback, but should be able to send a message after the first generated sas token has expired
     * @throws Exception if the test fails in any way
     */
    @Test
    public void tokenRenewalWorks() throws Exception
    {
        final long SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL = 60;
        final long EXPIRED_SAS_TOKEN_GRACE_PERIOD_SECONDS = 600;
        final long EXTRA_BUFFER_TO_ENSURE_TOKEN_EXPIRED_SECONDS = 30;

        List<InternalClient> clients = createClientsToTest();

        //service grants a 5 minute grace period beyond when sas token expires, this test attempts to send a message after that grace period
        // to ensure that the first sas token has expired, and that the sas token was renewed successfully.
        final long WAIT_BUFFER_FOR_TOKEN_TO_EXPIRE = EXPIRED_SAS_TOKEN_GRACE_PERIOD_SECONDS + EXTRA_BUFFER_TO_ENSURE_TOKEN_EXPIRED_SECONDS;

        for (InternalClient client : clients)
        {
            //set it so a newly generated sas token only lasts for a small amount of time
            client.setOption("SetSASTokenExpiryTime", SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL);
        }


        Success[] amqpDisconnectDidNotHappenSuccesses = new Success[clients.size()];
        Success[] mqttDisconnectDidHappenSuccesses = new Success[clients.size()];
        Success[] shutdownWasGracefulSuccesses = new Success[clients.size()];
        for (int clientIndex = 0; clientIndex < clients.size(); clientIndex++)
        {
            amqpDisconnectDidNotHappenSuccesses[clientIndex] = new Success();
            mqttDisconnectDidHappenSuccesses[clientIndex] = new Success();
            shutdownWasGracefulSuccesses[clientIndex] = new Success();

            amqpDisconnectDidNotHappenSuccesses[clientIndex].setResult(true); //assume success until unexpected DISCONNECTED_RETRYING
            mqttDisconnectDidHappenSuccesses[clientIndex].setResult(false); //assume failure until DISCONNECTED_RETRYING is triggered by token expiring
            shutdownWasGracefulSuccesses[clientIndex].setResult(true); //assume success until DISCONNECTED callback without CLIENT_CLOSE

            clients.get(clientIndex).registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeTokenRenewalCallbackVerifier(clients.get(clientIndex).getConfig().getProtocol(), amqpDisconnectDidNotHappenSuccesses[clientIndex], mqttDisconnectDidHappenSuccesses[clientIndex], shutdownWasGracefulSuccesses[clientIndex]), clients.get(clientIndex));
        }

        openEachClient(clients);

        sendMessageFromEachClient(clients);

        //wait until old sas token has expired, this should force the config to generate a new one from the device key
        System.out.println("Sleeping..." + System.currentTimeMillis());
        Thread.sleep((SECONDS_FOR_SAS_TOKEN_TO_LIVE_BEFORE_RENEWAL + WAIT_BUFFER_FOR_TOKEN_TO_EXPIRE) * 1000);
        System.out.println("Awake!" + System.currentTimeMillis());

        sendMessageFromEachClient(clients);

        closeClients(clients);

        verifyClientsConnectivityBehavedCorrectly(clients, amqpDisconnectDidNotHappenSuccesses, mqttDisconnectDidHappenSuccesses, shutdownWasGracefulSuccesses);
    }

    private void closeClients(List<InternalClient> clients)
    {
        try
        {
            for (int clientIndex = 0; clientIndex < clients.size(); clientIndex++)
            {
                InternalClient client = clients.get(clientIndex);
                client.closeNow();
                registryManager.removeDevice(client.getConfig().getDeviceId());
            }
        }
        catch (Exception e)
        {
            //Don't care, this test is not for testing registry manager device cleanup
        }
    }

    private void verifyClientsConnectivityBehavedCorrectly(List<InternalClient> clients, Success[] amqpDisconnectDidNotHappenSuccesses, Success[] mqttDisconnectDidHappenSuccesses, Success[] shutdownWasGracefulSuccesses)
    {
        for (int clientIndex = 0; clientIndex < clients.size(); clientIndex++)
        {
            InternalClient client = clients.get(clientIndex);
            IotHubClientProtocol protocol = client.getConfig().getProtocol();

            if (protocol == MQTT || protocol == MQTT_WS)
            {
                assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessage("MQTT connection was never lost, token renewal was not tested", client), mqttDisconnectDidHappenSuccesses[clientIndex].result);
                assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessage("Connection was lost during test, or shutdown was not graceful", client), shutdownWasGracefulSuccesses[clientIndex].result);
            }
            else if (protocol == AMQPS || protocol == AMQPS_WS)
            {
                assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessage("AMQPS connection was lost at least once, token renewal must have failed", client), amqpDisconnectDidNotHappenSuccesses[clientIndex].result);
                assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessage("Connection was lost during test, or shutdown was not graceful", client), shutdownWasGracefulSuccesses[clientIndex].result);
            }
        }
    }

    private List<InternalClient> createClientsToTest() throws IotHubException, IOException, URISyntaxException, ModuleClientException
    {
        List<InternalClient> clients = new ArrayList<>();
        Proxy testProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(testProxyHostname, testProxyPort));
        for (IotHubClientProtocol protocol: IotHubClientProtocol.values())
        {
            clients.add(createDeviceClient(protocol));
            if (protocol == HTTPS || protocol == MQTT_WS || protocol == AMQPS_WS)
            {
                InternalClient client = createDeviceClient(protocol);
                ProxySettings proxySettings = new ProxySettings(testProxy, testProxyUser, testProxyPass);
                client.setProxySettings(proxySettings);
                clients.add(client);
            }

            if (protocol != IotHubClientProtocol.HTTPS && !isBasicTierHub)
            {
                clients.add(createModuleClient(protocol));
            }
        }

        return clients;
    }

    private void sendMessageFromEachClient(List<InternalClient> clients)
    {
        for (InternalClient client : clients)
        {
            System.out.println("Sending test message for client " + client.getConfig().getDeviceId());
            IotHubServicesCommon.sendMessageAndWaitForResponse(client, new MessageAndResult(new Message("some message"), IotHubStatusCode.OK_EMPTY), RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, client.getConfig().getProtocol());
        }
    }

    private void openEachClient(List<InternalClient> clients) throws IOException
    {
        for (int clientIndex = 0; clientIndex < clients.size(); clientIndex++)
        {
            clients.get(clientIndex).open();
        }
    }

    /**
     * Expected callback order by protocol:
     * amqps/amqps_ws : Connected -> Disconnected (Client_closed)
     * mqtt/mqtt_ws   : Connected -> Disconnected_Retrying -> Connected -> Disconnected (Client_closed)
     */
    private static class IotHubConnectionStatusChangeTokenRenewalCallbackVerifier implements IotHubConnectionStatusChangeCallback
    {
        IotHubClientProtocol protocol;
        Success amqpDisconnectDidNotHappen;
        Success mqttDisconnectDidHappen;
        Success shutdownWasGraceful;

        public IotHubConnectionStatusChangeTokenRenewalCallbackVerifier(IotHubClientProtocol protocol, Success amqpDisconnectDidNotHappen, Success mqttDisconnectDidHappen, Success shutdownWasGraceful)
        {
            this.protocol = protocol;
            this.mqttDisconnectDidHappen = mqttDisconnectDidHappen;
            this.amqpDisconnectDidNotHappen = amqpDisconnectDidNotHappen;
            this.shutdownWasGraceful = shutdownWasGraceful;
        }

        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
        {
            System.out.println();
            System.out.println("CONNECTION STATUS UPDATE: " + status);
            System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
            System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            if (callbackContext instanceof InternalClient)
            {
                System.out.println("CONNECTION BELONGS TO: " + ((InternalClient) callbackContext).getConfig().getDeviceId());
            }
            System.out.println("CONNECTION PROTOCOL: " + this.protocol);
            System.out.println();

            if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                if (statusChangeReason != IotHubConnectionStatusChangeReason.CLIENT_CLOSE)
                {
                    shutdownWasGraceful.setResult(false);
                }
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                //AMQPS/AMQPS_WS is expected to not lose connection at any point except for when the client is closed
                // MQTT does need to tear down the expired connection in order to send the new sas token, so this
                // DISCONNECTED RETRYING is expected at least once.
                mqttDisconnectDidHappen.setResult(true);

                if (protocol == AMQPS || protocol == AMQPS_WS)
                {
                    amqpDisconnectDidNotHappen.setResult(false);
                }
            }
        }
    }

    private InternalClient createModuleClient(IotHubClientProtocol protocol) throws IOException, IotHubException, ModuleClientException, URISyntaxException
    {
        UUID uuid = UUID.randomUUID();
        String deviceId = "token-renewal-test-device-" + protocol + "-" + uuid.toString();
        String moduleId = "token-renewal-test-module-" + protocol + "-" + uuid.toString();
        com.microsoft.azure.sdk.iot.service.Device device = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceId, DeviceStatus.Enabled, null);
        device = registryManager.addDevice(device);
        Module module = Module.createModule(deviceId, moduleId, AuthenticationType.SAS);
        module = registryManager.addModule(module);
        return new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), protocol);
    }

    private InternalClient createDeviceClient(IotHubClientProtocol protocol) throws URISyntaxException, IOException, IotHubException {
        UUID uuid = UUID.randomUUID();
        String deviceId = "token-renewal-test-device-" + protocol + "-" + uuid.toString();
        com.microsoft.azure.sdk.iot.service.Device device = com.microsoft.azure.sdk.iot.service.Device.createFromId(deviceId, DeviceStatus.Enabled, null);
        device = registryManager.addDevice(device);
        return new DeviceClient(DeviceConnectionString.get(iotHubConnectionString, device), protocol);
    }
}
