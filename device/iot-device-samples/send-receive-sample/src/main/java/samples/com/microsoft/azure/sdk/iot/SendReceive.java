// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

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

    private static final Set<String> KEY_SET;

    static {
        KEY_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                "HostName=MokaFeatherM0Suite.azure-devices.net;DeviceId=JavaDevice1;SharedAccessKey=2tRTqmjechvTyFe9SzP4qRGXzkZLSyjwdpXdJaWdWGY=",
                "HostName=MokaFeatherM0Suite.azure-devices.net;DeviceId=JavaDevice2;SharedAccessKey=nLjD7ziGAy8RDrmZrXHsUITWHvRIkOI0ogiNnpHMveI=",
                "HostName=MokaFeatherM0Suite.azure-devices.net;DeviceId=JavaDevice3;SharedAccessKey=tzoCq4sEU6zoNmGWZ01t/8kSgY5MYfQttkqikbYY0lE=",
                "HostName=MokaFeatherM0Suite.azure-devices.net;DeviceId=JavaDevice4;SharedAccessKey=gkRTfe0FU1laPEJfC4+IjPrGiXbQ1n4Ga0mzchwgshM=",
                "HostName=MokaFeatherM0Suite.azure-devices.net;DeviceId=JavaDevice5;SharedAccessKey=pphgSkq0wAj0m0IT0rYUbkS5dPRVGwMqugRhsJQidK4="
        )));
    }

    protected static class TestDevice implements Runnable
    {
        private DeviceClient client;
        private String deviceid;
        private String connString;
        private IotHubClientProtocol protocol;
        private String pathToCertificate;
        private int numRequests;
        private int numKeys;

        @Override
        public void run()
        {
            for (int i = 0; i < 100; i++) {

                try {
                    this.OpenConnection();
                } catch (Exception e) {
                    System.out.println("Open throws " + e);
                }

                this.SendAndRecieve_n();

                try {
                    this.CloseConnection();
                } catch (Exception e) {
                    System.out.println("close throws " + e);
                }
            }
        }

        public TestDevice(String connString, IotHubClientProtocol protocol, String pathToCertificate, int numRequests, int numKeys)
        {
            this.connString = connString;
            this.protocol = protocol;
            this.pathToCertificate = pathToCertificate;
            this.numRequests = numRequests;
            this.numKeys = numKeys;
            deviceid = connString.split(";")[1];
        }

        private void OpenConnection() throws URISyntaxException, IOException
        {
            System.out.println();
            System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("START TEST FOR: " + deviceid);
            System.out.format("Using communication protocol %s.\n", protocol.name());
            System.out.format("Using path to certificate %s.\n", pathToCertificate);

            client = new DeviceClient(connString, protocol);

            if (pathToCertificate != null) {
                client.setOption("SetCertificatePath", pathToCertificate);
            }

            System.out.println("Successfully created an IoT Hub client.");

            if (protocol == IotHubClientProtocol.MQTT) {
                MessageCallbackMqtt callback = new MessageCallbackMqtt();
                Counter counter = new Counter(0);
                client.setMessageCallback(callback, counter);
            } else {
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

        }

        public void SendAndRecieve_n() {
            for (int i = 0; i < numRequests; ++i) {
                String msgStr = "Event Message " + Integer.toString(i) + " to " + deviceid;
                try {
                    Message msg = new Message(msgStr);
                    msg.setProperty("messageCount", Integer.toString(i));
                    for (int j = 0; j < numKeys; j++) {
                        msg.setProperty("key"+j, "value"+j);
                    }
                    msg.setExpiryTime(5000);
                    System.out.println(msgStr);
                    EventCallback eventCallback = new EventCallback();
                    client.sendEventAsync(msg, eventCallback, i);
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void CloseConnection() throws IOException
        {
            client.close();
            System.out.println("CLOSE CONNECTION.");
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

        Thread thread = new Thread(new TestDevice(connString, protocol, pathToCertificate, 10, 1000));
        thread.start();

        for(String connectionString : KEY_SET)
        {
            thread = new Thread(new TestDevice(connectionString, protocol, pathToCertificate, 10, 1000));
            thread.start();
        }

        System.out.println("Shutting down...");
    }
}
