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
import java.util.concurrent.*;


@Slf4j
public class DeviceClientManagerSample {

    private static final String DEVICE_CONNECTION_STRING = System.getenv("IOTHUB_DEVICE_CONNECTION_STRING");
    private static int NUM_REQUESTS = 3;
    private static int SLEEP_DURATION_IN_SECONDS = 10;
    private static int TIMEOUT_IN_MINUTES = 1;
    private static String CMD_HELP = "\nUsage:\n"
        + "The program should be called with the following args:\n"
        + "1. [Device connection string]: (Required, default to environment variable \"IOTHUB_DEVICE_CONNECTION_STRING\")\n"
        + "2. [Transport protocol]: (default to \"mqtt\") Protocol choice [mqtt | https | amqps | amqps_ws | mqtt_ws]\n"
        + "3. [Number of requests]: (default to \"3\")\n"
        + "4. [Sleep duration in seconds]: (default to \"10\")\n"
        + "5. [Timeout in minutes]: (default to \"1\")\n"
        + "\n";
    // Can be configured to use any protocol from HTTPS, AMQPS, MQTT, AMQPS_WS, MQTT_WS. Note: HTTPS does not support status callback, device methods and device twins.

    final static List<String> failedMessageListOnClose = new ArrayList<>(); // List of messages that failed on close
    private static DeviceClientManager deviceClientManager;

    /**
     * Sends a number of messages to an IoT. Default protocol is to use AMQP transport.
     */
    public static void main(String[] args)
            throws URISyntaxException, IOException {

        log.info("Starting...");
        
        String argDeviceConnectionString = (args.length >= 1) ? args[0] : DEVICE_CONNECTION_STRING;
        
        if (argDeviceConnectionString == null && (args.length < 1 || args.length >=5) )
        {
            log.error("Expect arguments but received: {}\n" + CMD_HELP, args.length);
            return;
        }

        log.info("Setup parameters...");
        log.debug("Setup parameter: Connection String from {}", (args.length >= 1) ? "command line" : "environment variable (\"IOTHUB_DEVICE_CONNECTION_STRING\")");

        IotHubClientProtocol argProtocol;
        if (args.length >= 2)
        {
            switch (args[1].toLowerCase())
            {
                case "https":
                    argProtocol = IotHubClientProtocol.HTTPS;
                    break;
                case "amqps":
                    argProtocol = IotHubClientProtocol.AMQPS;
                    break;
                case "amqps_ws":
                    argProtocol = IotHubClientProtocol.AMQPS_WS;
                    break;
                case "mqtt":
                    argProtocol = IotHubClientProtocol.MQTT;
                    break;
                case "mqtt_ws":
                    argProtocol = IotHubClientProtocol.MQTT_WS;
                    break;
                default:
                    throw new IllegalArgumentException("[ERROR] Do not support protocol: [" + args[1] + "]");
            }
        }
        else
        {
            argProtocol = IotHubClientProtocol.MQTT;
            log.debug("Setup parameter: Did not specify protocol. Default transport protocol to [{}]", argProtocol.name());
            log.debug(CMD_HELP);
        }
        log.debug("Setup parameter: Protocol = [{}]", argProtocol.name());

        int argNumRequest = (args.length > 2) ? Integer.parseInt(args[2]) : NUM_REQUESTS;
        log.debug("Setup parameter: Requests = [{}]", argNumRequest);
        int argSleepDuration = (args.length > 3) ? Integer.parseInt(args[3]) : SLEEP_DURATION_IN_SECONDS;
        log.debug("Setup parameter: Sleep Duration = [{}]", argSleepDuration);
        int argTimeout = (args.length > 4) ? Integer.parseInt(args[4]) : TIMEOUT_IN_MINUTES;
        log.debug("Setup parameter: Timeout = [{}]", argTimeout);

        DeviceClient client = new DeviceClient(argDeviceConnectionString, argProtocol);
        log.info("Successfully created an IoT Hub client.");

        deviceClientManager = new DeviceClientManager(client);
        deviceClientManager.setOperationTimeout(argTimeout);

        deviceClientManager.open();
        log.info("Opened connection to IoT Hub.");

        log.debug("Setting C2D message handler...");
        deviceClientManager.setMessageCallback(new SampleMessageReceiveCallback(), new Object());

        log.debug("Start sending telemetry ...");
        startSendingTelemetry(argNumRequest, argSleepDuration);

        // close the connection
        log.info("Closing");
        deviceClientManager.closeNow();

        if (! failedMessageListOnClose.isEmpty()) {
            log.error("List of messages that were cancelled on close:");
            log.error(failedMessageListOnClose.toString());
        }

        log.info("Shutting down...");
    }

    private static void startSendingTelemetry(int numRequest, int sleepTime) {
        log.debug("Sending the following event messages:");
        for (int i = 0; i < numRequest; ++i) {
            Message msg = composeMessage(i);
            SampleMessageSendCallback callback = new SampleMessageSendCallback();
            try {
                deviceClientManager.sendEventAsync(msg, callback, msg);
            } catch (Exception e) {
                failedMessageListOnClose.add(msg.getMessageId());
                log.error("Exception thrown while sending telemetry: ", e);
            }
            try {
                log.debug("Sleeping for {} secs before sending next message.", sleepTime);
                TimeUnit.SECONDS.sleep(sleepTime);
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
