// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Sample that demonstrates creating a multiplexed connection to IoT Hub using AMQPS / AMQPS_WS. It also demonstrates
 * removing and adding device clients from the multiplexed connection while it is open.
 *
 * This sample also demonstrates how reconnection can be handled by introducing {@link MultiplexClientManager} and {@link DeviceClientManager}
 * both of which extend {@link ClientManagerBase}.
 *
 * ClientManagerBase class implements two separate interfaces
 *
 * 1. {@link IotHubConnectionStatusChangeCallback} which is an SDK type for handling a connection status change callback
 * 2. {@link ConnectionStatusTracker} which allows for a dependent connection check the underlying connection status before attempting a reconnection.
 *
 * Both {@link DeviceClientManager} and {@link MultiplexClientManager} classes delegate all but 3 API calls to their underlying SDK client (MultiplexingClient or DeviceClient) they are wrapping around.
 * open(), close(), registerConnectionStatusChangeCallback() are the 3 APIs that are handled by the ClientManager instance to allow for dynamic reconnection logic.
 *
 * These client managers are in charge of handling the reconnection logic since they are the connection status callback handler.
 *
 * DeviceClientManager also takes an instance of {@link ConnectionStatusTracker} to identify whether or not the underlying
 * Multiplexing connection is established or not before they attempt a re-connection in an event of a disconnection.
 * This will avoid unnecessary reconnection attempts by DeviceClient since it cannot be connected unless the Multiplexing connection is established.
 *
 */
public class MultiplexingSample
{
    /**
     * Multiplex devices an IoT Hub using AMQPS / AMQPS_WS
     *
     * @param args
     * args[0] = protocol ("amqps" or "amqps_ws")
     * args[1] = host name to connect to (for instance, "my-iot-hub.azure-devices.net")
     * args[2] = IoT Hub connection string - Device Client 1
     * args[3] = IoT Hub connection string - Device Client 2
     * Add up to 998 device connection strings from args[4] on.
     *
     * Any additional arguments will be interpreted as additional connections strings. This allows this sample to be
     * run with more than 2 devices. At
     */
    public static void main(String[] args)
            throws IOException, URISyntaxException, InterruptedException {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        if (args.length < 4)
        {
            System.out.format(
                    "Expected at least 3 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Protocol]                   - amqps | amqps_ws\n"
                            + "2. [HostName]                   - my-iot-hub.azure-devices.net\n"
                            + "3. [Device 1 connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "4. [Device 2 connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + ".\n"
                            + ".\n"
                            + ".\n",
                    args.length);
            return;
        }

        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        if (args[0].equalsIgnoreCase("amqps_ws"))
        {
            protocol = IotHubClientProtocol.AMQPS_WS;
        }

        String hostName = args[1];

        // This sample requires users for pass in at least 2 device connection strings, so this sample may multiplex
        final int multiplexedDeviceCount = args.length - 2;

        // Options include setting a custom SSLContext to use for the SSL handshake, configuring proxy support, and setting threading strategies
        MultiplexingClientOptions options = MultiplexingClientOptions.builder().build();
        final MultiplexingClient multiplexingClient = new MultiplexingClient(hostName, protocol, options);

        MultiplexClientManager multiplexClientManager = new MultiplexClientManager(multiplexingClient, "MultiplexingClient");

        System.out.println("Creating device clients that will be multiplexed");
        final List<String> deviceIds = new ArrayList<>();
        final Map<String, DeviceClient> multiplexedDeviceClients = new HashMap<>(multiplexedDeviceCount);
        final Map<String, DeviceClientManager> deviceManagers = new HashMap<>(multiplexedDeviceCount);

        for (int i = 0; i < multiplexedDeviceCount; i++)
        {
            DeviceClient clientToMultiplex = new DeviceClient(args[i+2], protocol);
            String deviceId = clientToMultiplex.getConfig().getDeviceId();
            deviceIds.add(deviceId);

            multiplexedDeviceClients.put(deviceId, clientToMultiplex);

            deviceManagers.put(deviceId, new DeviceClientManager(clientToMultiplex, multiplexClientManager));
        }

        System.out.println("Opening multiplexed connection");
        // All previously registered device clients will be opened alongside this multiplexing client
        multiplexClientManager.open();
        System.out.println("Multiplexed connection opened successfully");

        // Note that all the clients are registered at once. This method will asynchronously start the registration
        // process for each device client, and then it will block until all registrations are complete before returning.
        // If instead each client was registered separately through multiplexingClient.registerDeviceClient(), it would
        // take a longer time since it would block on each registration completing, rather than block on all registrations completing
        System.out.println("Registering " + multiplexedDeviceCount + " clients to the multiplexing client...");
        multiplexClientManager.registerDeviceClients(multiplexedDeviceClients.values());

        System.out.println("Successfully registered " + multiplexedDeviceCount + " clients to the multiplexing client");


        for (String deviceId : deviceIds)
        {
            System.out.printf("Sending message from device %s%n", deviceId);
            Message message = new Message("some payload");
            multiplexedDeviceClients.get(deviceId).sendEventAsync(message, new TelemetryAcknowledgedEventCallback(), message.getMessageId());
        }

        System.out.println("Waiting while messages get sent asynchronously...");
        while (acknowledgedSentMessages < multiplexedDeviceCount)
        {
            System.out.printf(
                    "Waiting on %d messages to be acknowledged out of %d%n",
                    multiplexedDeviceCount - acknowledgedSentMessages,
                    multiplexedDeviceCount);
            Thread.sleep(200);
        }

        System.out.println("All messages sent successfully");

        // This code demonstrates how to remove a device from an active multiplexed connection without shutting down
        // the whole multiplexed connection or any of the other devices.
        String deviceIdToUnregister = deviceIds.get(0);

        System.out.println("Unregistering device " + deviceIdToUnregister + " from multiplexed connection...");
        multiplexClientManager.unregisterDeviceClient(multiplexedDeviceClients.get(deviceIdToUnregister));
        System.out.println("Successfully unregistered device " + deviceIdToUnregister + " from an active multiplexed connection.");

        // This code demonstrates how to add a device to an active multiplexed connection without shutting down
        // the whole multiplexed connection or any of the other devices.
        System.out.println("Re-registering device " + deviceIdToUnregister + " to an active multiplexed connection...");
        multiplexClientManager.registerDeviceClient(multiplexedDeviceClients.get(deviceIdToUnregister));
        System.out.println("Successfully registered " + deviceIdToUnregister + " to an active multiplexed connection");

        // Before closing a multiplexing client, you do not need to unregister all of the registered clients.
        // If they are not unregistered, then you can re-open the multiplexing client later and it will still
        // have all of your registered devices
        System.out.println("Closing entire multiplexed connection...");
        // This call will close all multiplexed device client instances as well
        multiplexClientManager.closeClient();
        System.out.println("Successfully closed the multiplexed connection");

        System.out.println("Shutting down...");
    }

    private static int acknowledgedSentMessages = 0;
    private static class TelemetryAcknowledgedEventCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            String messageId = (String) context;
            System.out.println("IoT Hub responded to message "+ messageId  + " with status " + status.name());
            acknowledgedSentMessages++;
        }
    }
}