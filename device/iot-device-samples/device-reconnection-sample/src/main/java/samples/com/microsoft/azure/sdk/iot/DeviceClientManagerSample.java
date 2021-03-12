// Copyright (c) Microsoft. All rights reserved.Licensed under the MIT license.
// See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DeviceClientManagerSample {
    private static final String deviceConnectionString = System.getenv("DEVICE_CONNECTION_STRING");
    // Can be configured to use any protocol from HTTPS, AMQPS, MQTT, AMQPS_WS, MQTT_WS. Note: HTTPS does not support status callback, device methods and device twins.
    private static final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
    private static final int DEVICE_OPERATION_TIMEOUT_IN_MINUTES = 2;
    private static final int NUM_REQUESTS = 20;
    private static final int SLEEP_DURATION_IN_SECS = 60;

    final static List<String> failedMessageListOnClose = new ArrayList<>(); // List of messages that failed on close
    private static DeviceClientManager deviceClientManager;

    /**
     * Sends a number of messages to an IoT. Default protocol is to use AMQP transport.
     */
    public static void main(String[] args)
            throws URISyntaxException, IOException {
        log.debug("Starting the sample...");
        log.debug("Using communication protocol: {}", protocol.name());

        DeviceClient client = new DeviceClient(deviceConnectionString, protocol);
        log.debug("Successfully created an IoT Hub client.");

        deviceClientManager = new DeviceClientManager(client);
        deviceClientManager.setOperationTimeout(DEVICE_OPERATION_TIMEOUT_IN_MINUTES);

        deviceClientManager.open();
        log.debug("Opened connection to IoT Hub.");

        log.debug("Setting C2D message handler...");
        deviceClientManager.setMessageCallback(new SampleMessageReceiveCallback(), new Object());

        log.debug("Start sending telemetry ...");
        startSendingTelemetry();

        // close the connection
        log.debug("Closing");
        deviceClientManager.closeNow();

        if (! failedMessageListOnClose.isEmpty()) {
            log.debug("List of messages that were cancelled on close: {}", failedMessageListOnClose.toString());
        }

        log.debug("Shutting down...");
    }

    private static void startSendingTelemetry() {
        log.debug("Sending the following event messages:");
        for (int i = 0; i < NUM_REQUESTS; ++i) {
            Message msg = composeMessage(i);
            SampleMessageSendCallback callback = new SampleMessageSendCallback();
            try {
                deviceClientManager.sendEventAsync(msg, callback, msg);
            } catch (Exception e) {
                failedMessageListOnClose.add(msg.getMessageId());
                log.error("Exception thrown while sending telemetry: ", e);
            }
            try {
                log.debug("Sleeping for {} secs before sending next message.", SLEEP_DURATION_IN_SECS);
                Thread.sleep(SLEEP_DURATION_IN_SECS * 1000);
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
