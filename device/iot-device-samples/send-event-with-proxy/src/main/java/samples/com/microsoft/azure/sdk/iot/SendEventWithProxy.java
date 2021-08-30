// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/** Sends a number of event messages to an IoT Hub. */
public class SendEventWithProxy
{
    private static final List<String> failedMessageListOnClose = new ArrayList<>(); // List of messages that failed on close

    protected static class EventCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            Message msg = (Message) context;

            System.out.println("IoT Hub responded to message "+ msg.getMessageId()  + " with status " + status.name());

            if (status==IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE)
            {
                failedMessageListOnClose.add(msg.getMessageId());
            }
        }
    }

    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
    {
        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
        {
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
     * Sends a number of messages to an IoT or Edge Hub. Default protocol is to
     * use MQTT transport.
     *
     * @param args
     * args[0] = IoT Hub or Edge Hub connection string
     * args[1] = number of messages to send
     * args[2] = protocol (optional, one of 'https', 'mqtt_ws' or 'amqps_ws')
     * args[3] = proxy host name ie: "127.0.0.1", "localhost", etc.
     * args[4] = proxy port number ie "8888", "3128", etc
     * args[5] = (optional) proxy username
     * args[6] = (optional) proxy password
     */
    public static void main(String[] args)
            throws IOException, URISyntaxException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        if (args.length != 5 && args.length != 7)
        {
            System.out.format(
                    "Expected 5 or 7 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key> or HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>;GatewayHostName=<gateway> \n"
                            + "2. [number of requests to send]\n"
                            + "3. (https | amqps_ws | mqtt_ws)\n"
                            + "4. proxy hostname (ie: '127.0.0.1', 'localhost', etc.)\n"
                            + "5. proxy port number\n"
                            + "6. (optional) username for the proxy \n"
                            + "7. (optional) password for the proxy \n",
                    args.length);
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
        String protocolStr = args[2];
        if (protocolStr.equalsIgnoreCase("https"))
        {
            protocol = IotHubClientProtocol.HTTPS;
        }
        else if (protocolStr.equalsIgnoreCase("amqps"))
        {
            throw new UnsupportedOperationException("AMQPS does not have proxy support");
        }
        else if (protocolStr.equalsIgnoreCase("mqtt"))
        {
            throw new UnsupportedOperationException("MQTT does not have proxy support");
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
            System.out.format(
                    "Received a protocol string that could not be understood: %s.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key> or HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>;GatewayHostName=<gateway> \n"
                            + "2. [number of requests to send]\n"
                            + "3. (https | amqps_ws | mqtt_ws)\n"
                            + "4. proxy hostname (ie: '127.0.0.1', 'localhost', etc.)\n"
                            + "5. proxy port number\n"
                            + "6. (optional) username for the proxy \n"
                            + "7. (optional) password for the proxy \n",
                    protocolStr);
            return;
        }

        String proxyHostname = args[3];
        int proxyPort;
        try
        {
            proxyPort = Integer.parseInt(args[4]);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Expected argument 5 (port number) to be an integer");
        }

        String proxyUsername;
        char[] proxyPassword;
        if (args.length == 7)
        {
            proxyUsername = args[5];
            proxyPassword = args[6].toCharArray();
        }
        else
        {
            proxyUsername = null;
            proxyPassword = null;
        }

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n", protocol.name());

        DeviceClient client = new DeviceClient(connString, protocol);

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHostname, proxyPort));
        ProxySettings httpProxySettings = new ProxySettings(proxy, proxyUsername, proxyPassword);
        System.out.println("Using proxy address: " + proxyHostname + ":" + proxyPort);
        client.setProxySettings(httpProxySettings);

        System.out.println("Successfully created an IoT Hub client.");

        client.setConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());

        client.open();

        System.out.println("Opened connection to IoT Hub.");
        System.out.println("Sending the following event messages:");

        for (int i = 0; i < numRequests; ++i)
        {
            String msgStr = "This is a message sent over proxy";

            try
            {
                Message msg = new Message(msgStr);
                System.out.println(msgStr);

                EventCallback callback = new EventCallback();
                client.sendEventAsync(msg, callback, msg);
            }
            catch (Exception e)
            {
                e.printStackTrace(); // Trace the exception
            }
        }

        System.out.println("Wait for a response from the IoT Hub...");

        // Wait for IoT Hub to respond.
        try
        {
            System.out.println("Waiting 10 seconds for all responses to return...");
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        // close the connection
        System.out.println("Closing");
        client.close();

        if (!failedMessageListOnClose.isEmpty())
        {
            System.out.println("List of messages that were cancelled on close:" + failedMessageListOnClose.toString());
        }

        System.out.println("Shutting down...");
    }
}
