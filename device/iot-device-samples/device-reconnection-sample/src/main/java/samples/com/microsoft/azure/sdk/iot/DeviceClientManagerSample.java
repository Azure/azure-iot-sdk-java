// Copyright (c) Microsoft. All rights reserved.Licensed under the MIT license.
// See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DeviceClientManagerSample {

    private static int _NUM_REQUESTS = 3;
    private static int _SLEEP_DURATION_IN_SECONDS = 10;
    private static int _TIMEOUT_IN_MINUTES = 1;
    private static String _CMD_LINE = "Expected 1 or 2 arguments but received: %d.\n"
        + "The program should be called with the following args: \n"
        + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key> or HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>;GatewayHostName=<gateway> \n"
        + "2. (optional, default = mqtt) [mqtt | https | amqps | amqps_ws | mqtt_ws]\n";
    // Can be configured to use any protocol from HTTPS, AMQPS, MQTT, AMQPS_WS, MQTT_WS. Note: HTTPS does not support status callback, device methods and device twins.

    final static List<String> failedMessageListOnClose = new ArrayList<>(); // List of messages that failed on close
    private static DeviceClientManager deviceClientManager;

    /**
     * Sends a number of messages to an IoT. Default protocol is to use AMQP transport.
     */
    public static void main(String[] args)
            throws URISyntaxException, IOException {

        System.out.println("Starting...");
        System.out.println("Beginning setup.");
        
        if (args.length < 1 || args.length > 2)
        {
            System.out.format(_CMD_LINE, args.length);
            return;
        }

        String argDeviceConnectionString = args[0];

        IotHubClientProtocol argProtocol;
        String strProtocol = (args.length >1)?args[1]:"mqtt";

        if (strProtocol.equals("https"))
            argProtocol = IotHubClientProtocol.HTTPS;
        else if (strProtocol.equals("amqps"))
            argProtocol = IotHubClientProtocol.AMQPS;
        else if (strProtocol.equals("amqps_ws"))
            argProtocol = IotHubClientProtocol.AMQPS_WS;
        else if (strProtocol.equals("mqtt_ws"))
            argProtocol = IotHubClientProtocol.MQTT_WS;
        else
            argProtocol = IotHubClientProtocol.MQTT;        
        
        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n", argProtocol.name());
        
        DeviceClient client = new DeviceClient(argDeviceConnectionString, argProtocol);
        System.out.println("Successfully created an IoT Hub client.");

        deviceClientManager = new DeviceClientManager(client);
        deviceClientManager.setOperationTimeout(_TIMEOUT_IN_MINUTES);

        deviceClientManager.open();
        System.out.println("Opened connection to IoT Hub.");

        System.out.println("Setting C2D message handler...");
        deviceClientManager.setMessageCallback(new SampleMessageReceiveCallback(), new Object());

        System.out.println("Start sending telemetry ...");
        startSendingTelemetry();

        // close the connection
        System.out.println("Closing");
        deviceClientManager.closeNow();

        if (! failedMessageListOnClose.isEmpty()) {
            System.out.println("List of messages that were cancelled on close:");
            System.out.println(failedMessageListOnClose.toString());
        }

        System.out.println("Shutting down...");
    }

    private static void startSendingTelemetry() {
        log.debug("Sending the following event messages:");
        for (int i = 0; i < _NUM_REQUESTS; ++i) {
            Message msg = composeMessage(i);
            SampleMessageSendCallback callback = new SampleMessageSendCallback();
            try {
                deviceClientManager.sendEventAsync(msg, callback, msg);
            } catch (Exception e) {
                failedMessageListOnClose.add(msg.getMessageId());
                log.error("Exception thrown while sending telemetry: ", e);
            }
            try {
                log.debug("Sleeping for {} secs before sending next message.", _SLEEP_DURATION_IN_SECONDS);
                Thread.sleep(_SLEEP_DURATION_IN_SECONDS * 1000);
            } catch (InterruptedException e) {
                log.error("Exception thrown while sleeping: ", e);
            }
        }
    }

    private static Message composeMessage(int counter) {
        double temperature;
        double humidity;

        temperature = 20 + Math.random() * 10;
        humidity = 30 + Math.random() * 20;
        String messageId = java.util.UUID.randomUUID().toString();

        String msgStr = String.format(">> {\"count\": %d, \"messageId\": %s, \"temperature\": %f, \"humidity\": %f}", counter, messageId, temperature, humidity);
        Message msg = new Message(msgStr);
        msg.setProperty("temperatureAlert", temperature > 28 ? "true" : "false");
        msg.setMessageId(messageId);
        log.debug(msgStr);

        return msg;
    }
}
