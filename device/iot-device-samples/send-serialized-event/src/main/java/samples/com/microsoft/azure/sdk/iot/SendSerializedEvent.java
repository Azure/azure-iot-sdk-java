// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;

import java.io.IOException;
import java.net.URISyntaxException;

/** Sends a number of event messages to an IoT Hub. */
public class SendSerializedEvent {
    private static class ParsedArguments {
        // Data
        private static final String DEFAULT_DEVICEID = "myFirstDevice";
        private static final double DEFAULT_WINDSPEED = 10; // m/s

        public String[] rawArguments;
        public boolean parsedSuccessfully;
        public String connectionString;
        public IotHubClientProtocol protocol;
        public String deviceId;
        public double windSpeed;

        // Constructors
        public ParsedArguments(String[] args) {
            rawArguments = args;
            parsedSuccessfully = false;
            parse();
        }

        // Internal methods
        private void parse() {
            if (rawArguments != null && rawArguments.length >= 1)
            {
                try {
                    connectionString = rawArguments[0];

                    if (rawArguments.length >= 2) {
                        if (rawArguments[1].equals("https")) {
                            protocol = IotHubClientProtocol.HTTPS;
                        } else if (rawArguments[1].equals("amqps")) {
                            protocol = IotHubClientProtocol.AMQPS;
                        } else if (rawArguments[1].equals("mqtt")) {
                            protocol = IotHubClientProtocol.MQTT;
                        } else if (rawArguments[1].equals("amqps_ws")) {
                            protocol = IotHubClientProtocol.AMQPS_WS;
                        } else {
                            throw new Exception("Invalid protocol: " + rawArguments[2]);
                        }
                    } else {
                        protocol = IotHubClientProtocol.MQTT;
                    }

                    if(rawArguments.length >= 3) {
                        deviceId = rawArguments[2];
                    } else {
                        deviceId = DEFAULT_DEVICEID;
                    }

                    if(rawArguments.length >= 4) {
                        windSpeed = Double.parseDouble(rawArguments[3]);
                    } else {
                        windSpeed = DEFAULT_WINDSPEED;
                    }

                    this.parsedSuccessfully = true;
                }
                catch (Exception ex) {
                    this.parsedSuccessfully = false;
                }
            }
        }
    }

    protected static class EventCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to message with status " + status.name());

            if (context != null) {
                synchronized (context) {
                    context.notify();
                }
            }
        }
    }

    private static void printUsage(){
        System.out.println(
               "The program should be called with the following args: \n"
                    + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                    + "2. [mqtt | https | amqps | amqps_ws]\n"
                    + "3. [deviceId] \n"
                    + "4. [windSpeed] \n");
     }

    /**
     * Sends a number of messages to an IoT Hub. Default protocol is to
     * use MQTT transport.
     *
     * @param args 
     * args[0] = IoT Hub connection string
     * args[1] = protocol (optional, one of 'mqtt' or 'amqps' or 'https' or 'amqps_ws')
     * args[2] = temperature (integer; default = 65)
     * args[3] = humidity (integer; default = 72)
     */
    public static void main(String[] args)
            throws IOException, URISyntaxException {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        ParsedArguments arguments = new ParsedArguments(args);

        if (!arguments.parsedSuccessfully) {
            printUsage();
            return;
        }

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n", arguments.protocol.name());

        try {
            DeviceClient client = new DeviceClient(arguments.connectionString, arguments.protocol);

            System.out.println("Successfully created an IoT Hub client.");

            client.open();

            System.out.println("Opened connection to IoT Hub.");

            ContosoAnemometer data = new ContosoAnemometer();
            data.deviceId = arguments.deviceId;
            data.windSpeed = arguments.windSpeed;

            String msgStr = data.serialize();

            Message msg = new Message(msgStr);
            msg.setExpiryTime(5000);

            Object lockobj = new Object();
            EventCallback callback = new EventCallback();
            client.sendEventAsync(msg, callback, lockobj);

            synchronized (lockobj) {
                lockobj.wait();
            }

            client.close();
        } catch (Exception e) {
            System.out.println("Failure: " + e.toString());
        }

        System.out.println("Shutting down...");
    }
}
