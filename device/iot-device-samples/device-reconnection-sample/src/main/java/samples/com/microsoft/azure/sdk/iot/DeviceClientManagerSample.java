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

    final static List<String> failedMessageListOnClose = new ArrayList<>(); // List of messages that failed on close
    private static DeviceClientManager deviceClientManager;

    /**
     * Sends a number of messages to an IoT. Default protocol is to use AMQP transport.
     */
    public static void main(String[] args)
            throws URISyntaxException, IOException {

        SampleParameters params = new SampleParameters(args);

        log.info("Starting...");
        log.info("Setup parameters...");

        String argDeviceConnectionString = (params.getConnectionStrings())[0];

        IotHubClientProtocol argProtocol;
        String protocol = params.getTransport().toLowerCase();
        switch (protocol)
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
                throw new IllegalArgumentException("[ERROR] Do not support protocol: [" + protocol + "]");
        }
        log.debug("Setup parameter: Protocol = [{}]", protocol);

        int argNumRequest = Integer.parseInt(params.getNumRequests());
        log.debug("Setup parameter: Requests = [{}]", argNumRequest);
        int argSleepDuration = Integer.parseInt(params.getSleepDuration());
        log.debug("Setup parameter: Sleep Duration = [{}]", argSleepDuration);
        int argTimeout = Integer.parseInt(params.getTimeout());
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
