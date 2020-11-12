// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Sample that demonstrates creating a multiplexed connection to IoT Hub using AMQPS / AMQPS_WS. It also demonstrates
 * removing and adding device clients from the multiplexed connection while it is open.
 */
public class MultiplexingSample
{
    // Every multiplexed device will maintain its own connection status callback. Because of that, you can monitor
    // if a particular device session goes offline unexpectedly. This connection status callback is also how you
    // confirm when a device client is connected after registering it to an active multiplexed connection since the .registerDeviceClients(...)
    // call behaves asynchronously when the multiplexing client is already open. Similarly, this callback is used to track
    // when a device client is closed when unregistering it from an active connection.
    public static class MultiplexedDeviceConnectionStatusChangeTracker implements IotHubConnectionStatusChangeCallback
    {
        private boolean isOpen = false;
        private boolean isDisconnectedRetrying = false;

        public boolean isOpen() {
            return isOpen;
        }

        public boolean isDisconnectedRetrying() {
            return isDisconnectedRetrying;
        }

        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
        {
            // In this sample, both the device clients and the multiplexing client use this class to track their respective connection statuses.
            // If the context is not null, it has the device id that the status belongs to. If the context is null, the status
            // update belongs to the multiplexed connection itself.
            String deviceId = (String) callbackContext;

            if (throwable == null)
            {
                if (deviceId == null)
                {
                    System.out.println("CONNECTION STATUS UPDATE FOR MULTIPLEXED CONNECTION - " + status + ", " + statusChangeReason);
                }
                else
                {
                    System.out.println("CONNECTION STATUS UPDATE FOR DEVICE " + deviceId + " - " + status + ", " + statusChangeReason);
                }
            }
            else
            {
                if (deviceId == null)
                {
                    System.out.println("CONNECTION STATUS UPDATE FOR MULTIPLEXED CONNECTION - " + status + ", " + statusChangeReason + ", " + throwable.getMessage());
                }
                else
                {
                    System.out.println("CONNECTION STATUS UPDATE FOR DEVICE " + deviceId + " - " + status + ", " + statusChangeReason + ", " + throwable.getMessage());
                }
            }

            if (status == IotHubConnectionStatus.CONNECTED)
            {
                isOpen = true;
                isDisconnectedRetrying = false;
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                isOpen = false;
                isDisconnectedRetrying = false;
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                isDisconnectedRetrying = true;
                isOpen = false;
            }
        }
    }

    public static int acknowledgedSentMessages = 0;
    protected static class TelemetryAcknowledgedEventCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            String messageId = (String) context;
            System.out.println("IoT Hub responded to message "+ messageId  + " with status " + status.name());
            acknowledgedSentMessages++;
        }
    }

    /**
     * Multiplex devices an IoT Hub using AMQPS / AMQPS_WS
     *
     * @param args
     * args[0] = protocol ("amqps" or "amqps_ws")
     * args[1] = host name to connect to (for instance, "my-iot-hub.azure-devices.net")
     * args[2] = IoT Hub connection string - Device Client 1
     * args[3] = IoT Hub connection string - Device Client 2
     * Add up to 998 device connection strings from args[4] and so on.
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
        MultiplexedDeviceConnectionStatusChangeTracker multiplexedConnectionStatusTracker = new MultiplexedDeviceConnectionStatusChangeTracker();
        multiplexingClient.registerConnectionStatusChangeCallback(multiplexedConnectionStatusTracker, null);

        System.out.println("Creating device clients that will be multiplexed");
        final List<String> deviceIds = new ArrayList<>();
        final Map<String, DeviceClient> multiplexedDeviceClients = new HashMap<>(multiplexedDeviceCount);
        List<MultiplexedDeviceConnectionStatusChangeTracker> deviceConnectionStatusTrackers = new ArrayList<>(multiplexedDeviceCount);

        for (int i = 0; i < multiplexedDeviceCount; i++)
        {
            DeviceClient clientToMultiplex = new DeviceClient(args[i+2], protocol);
            String deviceId = clientToMultiplex.getConfig().getDeviceId();
            deviceIds.add(deviceId);

            multiplexedDeviceClients.put(deviceId, clientToMultiplex);
            deviceConnectionStatusTrackers.add(new MultiplexedDeviceConnectionStatusChangeTracker());
            multiplexedDeviceClients.get(deviceId).registerConnectionStatusChangeCallback(deviceConnectionStatusTrackers.get(i), deviceId);
        }

        System.out.println("Opening multiplexed connection");
        // All previously registered device clients will be opened alongside this multiplexing client
        multiplexingClient.open();
        System.out.println("Multiplexed connection opened successfully");

        // Note that all the clients are registered at once. This method will asynchronously start the registration
        // process for each device client, and then it will block until all registrations are complete before returning.
        // If instead each client was registered separately through multiplexingClient.registerDeviceClient(), it would
        // take a longer time since it would block on each registration completing, rather than block on all registrations completing
        System.out.println("Registering " + multiplexedDeviceCount + " clients to the multiplexing client...");
        multiplexingClient.registerDeviceClients(multiplexedDeviceClients.values());
        System.out.println("Successfully registered " + multiplexedDeviceCount + " clients to the multiplexing client");

        // Spin off a new thread to keep sending messages to all devices
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final long startTime = System.currentTimeMillis();

                    while (true && System.currentTimeMillis() - startTime < 10000){
                        for (String deviceId : deviceIds) {
                            Thread.sleep(1000);
                            System.out.printf("Sending message from device %s%n", deviceId);
                            Message message = new Message("some payload");
                            multiplexedDeviceClients.get(deviceId).sendEventAsync(message, new TelemetryAcknowledgedEventCallback(), message.getMessageId());
                        }
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // This code demonstrates how to remove a device from an active multiplexed connection without shutting down
        // the whole multiplexed connection or any of the other devices.
        String deviceIdToUnregister = deviceIds.get(0);

        System.out.println("Unregistering device " + deviceIdToUnregister + " from multiplexed connection...");
        multiplexingClient.unregisterDeviceClient(multiplexedDeviceClients.get(deviceIdToUnregister));
        System.out.println("Successfully unregistered device " + deviceIdToUnregister + " from an active multiplexed connection.");

        // This code demonstrates how to add a device to an active multiplexed connection without shutting down
        // the whole multiplexed connection or any of the other devices.
        System.out.println("Re-registering device " + deviceIdToUnregister + " to an active multiplexed connection...");
        multiplexingClient.registerDeviceClient(multiplexedDeviceClients.get(deviceIdToUnregister));
        System.out.println("Successfully registered " + deviceIdToUnregister + " to an active multiplexed connection");

        // Before closing a multiplexing client, you do not need to unregister all of the registered clients.
        // If they are not unregistered, then you can re-open the multiplexing client later and it will still
        // have all of your registered devices

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        System.out.println("Closing entire multiplexed connection...");
        // This call will close all multiplexed device client instances as well
        multiplexingClient.close();
        System.out.println("Successfully closed the multiplexed connection");

        System.out.println("Shutting down...");
    }
}