// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Handles messages from an IoT Hub. Default protocol is to use
 * MQTT transport.
 */
public class SendReceive
{
    /** Used as a counter in the message callback. */
    protected static class Counter
    {
        protected int num;

        public Counter(int num)
        {
            this.num = num;
        }

        public int get()
        {
            return this.num;
        }

        public void increment()
        {
            this.num++;
        }

        @Override
        public String toString()
        {
            return Integer.toString(this.num);
        }
    }

    protected static class MessageCallback
            implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg,
                Object context)
        {
            Counter counter = (Counter) context;
            System.out.println(
                    "Received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

            int switchVal = counter.get() % 3;
            IotHubMessageResult res;
            switch (switchVal)
            {
                case 0:
                    res = IotHubMessageResult.COMPLETE;
                    break;
                case 1:
                    res = IotHubMessageResult.ABANDON;
                    break;
                case 2:
                    res = IotHubMessageResult.REJECT;
                    break;
                default:
                    // should never happen.
                    throw new IllegalStateException(
                            "Invalid message result specified.");
            }

            System.out.println(
                    "Responding to message " + counter.toString()
                            + " with " + res.name());

            counter.increment();

            return res;
        }
    }

    // Our MQTT doesn't support abandon/reject, so we will only display the messaged received
    // from IoTHub and return COMPLETE
    protected static class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            Counter counter = (Counter) context;
            System.out.println(
                    "Received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

            counter.increment();

            return IotHubMessageResult.COMPLETE;
        }
    }

    protected static class EventCallback implements IotHubEventCallback{
        public void execute(IotHubStatusCode status, Object context){
            Integer i = (Integer) context;
            System.out.println("IoT Hub responded to message "+i.toString()
                + " with status " + status.name());
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
  
    public static void main(String[] args)
            throws IOException, URISyntaxException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        String pathToCertificate = null;
        if (args.length <= 1 || args.length >= 5)
        {
            System.out.format(
                    "Expected 2 or 3 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "2. [number of requests to send]\n"
                            + "3. (mqtt | https | amqps | amqps_ws)\n"
                            + "4. (optional) path to certificate to enable one-way authentication over ssl for amqps \n",
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
        if (args.length == 2)
        {
            protocol = IotHubClientProtocol.MQTT;
        }
        else
        {
            String protocolStr = args[2];
            if (protocolStr.equals("https"))
            {
                protocol = IotHubClientProtocol.HTTPS;
            }
            else if (protocolStr.equals("amqps"))
            {
                protocol = IotHubClientProtocol.AMQPS;
            }
            else if (protocolStr.equals("mqtt"))
            {
                protocol = IotHubClientProtocol.MQTT;
            }
            else if (protocolStr.equals("amqps_ws"))
            {
                protocol = IotHubClientProtocol.AMQPS_WS;
            }
            else
            {
                System.out.format(
                        "Expected argument 2 to be one of 'mqtt', 'https', 'amqps' or 'amqps_ws' but received %s\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "2. [number of requests to send]\n"
                            + "3. (mqtt | https | amqps | amqps_ws)\n"
                            + "4. (optional) path to certificate to enable one-way authentication over ssl for amqps \n",
                        protocolStr);
                return;
            }

            if (args.length == 3)
            {
                pathToCertificate = null;
            }
            else
            {
                pathToCertificate = args[3];
            }
        }

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n",
                protocol.name());

        DeviceClient client = new DeviceClient(connString, protocol);
        if (pathToCertificate != null)
        {
            client.setOption("SetCertificatePath", pathToCertificate);
        }

        System.out.println("Successfully created an IoT Hub client.");

        if (protocol == IotHubClientProtocol.MQTT)
        {
            MessageCallbackMqtt callback = new MessageCallbackMqtt();
            Counter counter = new Counter(0);
            client.setMessageCallback(callback, counter);
        }
        else
        {
            MessageCallback callback = new MessageCallback();
            Counter counter = new Counter(0);
            client.setMessageCallback(callback, counter);
        }

        System.out.println("Successfully set message callback.");

        // Set your token expiry time limit here
        long time = 2400;
        client.setOption("SetSASTokenExpiryTime", time);

        client.open();

        System.out.println("Opened connection to IoT Hub.");

        System.out.println("Beginning to receive messages...");

        System.out.println("Sending the following event messages: ");

        System.out.println("Updated token expiry time to " + time);

        for (int i = 0; i < numRequests; ++i)
        {
            String msgStr = "Event Message " + Integer.toString(i);
            try
            {
                Message msg = new Message(msgStr);
                msg.setProperty("messageCount", Integer.toString(i));
                msg.setProperty("key1", "value1");
                msg.setProperty("key2", "value2");
                msg.setMessageId(java.util.UUID.randomUUID().toString());
                msg.setExpiryTime(5000);
                System.out.println(msgStr);
                EventCallback eventCallback = new EventCallback();
                client.sendEventAsync(msg, eventCallback, i);
            } catch (Exception e)
            {
				e.printStackTrace(); // Trace the exception
            }
            try
            {
                Thread.sleep(200);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        client.close();

        System.out.println("Shutting down...");
    }
}
