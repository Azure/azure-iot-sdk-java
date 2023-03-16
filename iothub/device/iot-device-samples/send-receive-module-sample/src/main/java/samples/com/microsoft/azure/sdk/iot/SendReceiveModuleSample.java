// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/** Sends a number of event messages to an IoT Hub from a module and then listen for messages sent to the module. */
public class SendReceiveModuleSample
{
    private static final String SAMPLE_USAGE = "The program should be called with the following args: \n"
            + "1. [Device connection string] - String containing Hostname, Device Id, Module Id & Device Key in one of the following formats: HostName=<iothub_host_name>;deviceId=<device_id>;SharedAccessKey=<device_key>;moduleId=<module_id>\n"
            + "2. [number of requests to send]\n"
            + "3. (mqtt | amqps | amqps_ws | mqtt_ws)\n";

    private static final String SAMPLE_USAGE_WITH_WRONG_ARGS = "Expected 2 or 3 arguments but received: %d.\n" + SAMPLE_USAGE;
    private static final String SAMPLE_USAGE_WITH_INVALID_PROTOCOL = "Expected argument 2 to be one of 'mqtt', 'mqtt_ws', 'amqps' or 'amqps_ws' but received %s\n" + SAMPLE_USAGE;

    private  static final int D2C_MESSAGE_TIMEOUT = 2000; // 2 seconds

    protected static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult onCloudToDeviceMessageReceived(Message msg, Object context)
        {
            System.out.println(
                "Received message with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

            // Other options here are to ABANDON the message (IoT hub will requeue it and send it again later)
            // or to REJECT the message (IoT hub will not requeue it). Note that clients using MQTT or MQTT_WS
            // cannot ABANDON or REJECT messages.
            return IotHubMessageResult.COMPLETE;
        }
    }

    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
    {
        @Override
        public void onStatusChanged(ConnectionStatusChangeContext connectionStatusChangeContext)
        {
            IotHubConnectionStatus status = connectionStatusChangeContext.getNewStatus();
            IotHubConnectionStatusChangeReason statusChangeReason = connectionStatusChangeContext.getNewStatusReason();
            Throwable throwable = connectionStatusChangeContext.getCause();

            System.out.println();
            System.out.println("CONNECTION STATUS UPDATE: " + status);
            System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
            System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            System.out.println();

            if (throwable != null)
            {
                throwable.printStackTrace();
            }

            if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                System.out.println("The connection was lost, and is not being re-established." +
                        " Look at provided exception for how to resolve this issue." +
                        " Cannot send messages until this issue is resolved, and you manually re-open the device client");
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                System.out.println("The connection was lost, but is being re-established." +
                        " Can still send messages, but they won't be sent until the connection is re-established");
            }
            else if (status == IotHubConnectionStatus.CONNECTED)
            {
                System.out.println("The connection was successfully established. Can send messages.");
            }
        }
    }

    /**
     * Receives requests from an IoT Hub. Default protocol is to use
     * use MQTT transport.
     *
     * @param args
     * args[0] = IoT Hub connection string
     * args[1] = number of requests to send
     * args[2] = protocol (optional, one of 'mqtt' or 'amqps' or 'https' or 'amqps_ws')
     * args[3] = path to certificate to enable one-way authentication over ssl for amqps (optional, default shall be used if unspecified).
     */
    public static void main(String[] args) throws IOException, URISyntaxException, IotHubClientException, InterruptedException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        if (args.length <= 1 || args.length >= 4)
        {
            System.out.format(SAMPLE_USAGE_WITH_WRONG_ARGS, args.length);
            return;
        }

        String connString = args[0];
        int numRequests;
        try
        {
            numRequests = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            System.out.format(
                    "Could not parse the number of requests to send. "
                            + "Expected an int but received:\n%s.\n", args[1]);
            return;
        }
        IotHubClientProtocol protocol;
        if (args.length == 2)
        {
            protocol = IotHubClientProtocol.MQTT;
        }
        else
        {
            String protocolStr = args[2];
            if (protocolStr.equalsIgnoreCase("amqps"))
            {
                protocol = IotHubClientProtocol.AMQPS;
            }
            else if (protocolStr.equalsIgnoreCase("mqtt"))
            {
                protocol = IotHubClientProtocol.MQTT;
            }
            else if (protocolStr.equalsIgnoreCase("amqps_ws"))
            {
                protocol = IotHubClientProtocol.AMQPS_WS;
            }
            else if (protocolStr.equalsIgnoreCase("mqtt_ws"))
            {
                protocol = IotHubClientProtocol.MQTT_WS;
            }
            else
            {
                System.out.format(SAMPLE_USAGE_WITH_INVALID_PROTOCOL, protocolStr);
                return;
            }
        }

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n", protocol.name());

        ModuleClient client = new ModuleClient(connString, protocol);

        System.out.println("Successfully created an IoT Hub client.");

        client.setMessageCallback(new MessageCallback(), null);

        System.out.println("Successfully set message callback.");

        client.setConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());

        client.open(true);

        System.out.println("Opened connection to IoT Hub.");

        System.out.println("Beginning to receive messages...");

        System.out.println("Sending the following event messages: ");

        for (int i = 0; i < numRequests; ++i)
        {
            double temperature = 20 + Math.random() * 10;
            double humidity = 30 + Math.random() * 20;

            String msgStr = "{\"temperature\":"+ temperature +",\"humidity\":"+ humidity +"}";

            try
            {
                Message msg = new Message(msgStr);
                msg.setContentType("application/json");
                msg.setProperty("temperatureAlert", temperature > 28 ? "true" : "false");
                msg.setMessageId(java.util.UUID.randomUUID().toString());
                msg.setExpiryTime(D2C_MESSAGE_TIMEOUT);
                System.out.println(msgStr);
                client.sendEvent(msg, D2C_MESSAGE_TIMEOUT);
                System.out.println("Successfully sent the message");
            }
            catch (IotHubClientException e)
            {
                System.out.println("Failed to send the message. Status code: " + e.getStatusCode());
            }
        }

        System.out.println("In receive mode. Waiting for receiving C2D messages. Press any key to exit.");
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
        scanner.nextLine();

        // close the connection
        System.out.println("Closing the client...");
        client.close();
    }
}
