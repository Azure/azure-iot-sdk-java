// Copyright (c) Microsoft. All rights reserved.Licensed under the MIT license.
// See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static samples.com.microsoft.azure.sdk.iot.DeviceTwinHelper.*;
import static samples.com.microsoft.azure.sdk.iot.MethodHelper.DeviceMethodStatusCallBack;
import static samples.com.microsoft.azure.sdk.iot.MethodHelper.SampleDeviceMethodCallback;
import static samples.com.microsoft.azure.sdk.iot.ReceiveMessageHelper.MessageCallback;
import static samples.com.microsoft.azure.sdk.iot.TelemetryHelper.EventCallback;
import static samples.com.microsoft.azure.sdk.iot.TelemetryHelper.composeMessage;

@Slf4j
public class Application {
    private static final String deviceConnectionString = System.getenv("DEVICE_CONNECTION_STRING");
    private static final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
    private static final int DEVICE_OPERATION_TIMEOUT_IN_MINUTES = 2;
    private static final int numRequests = 20;
    private static final int MAX_APPLICATION_RUN_DURATION_IN_MINUTES = 5;
    private static final int SLEEP_DURATION_IN_SECS = 60;
    private static AtomicBoolean stopRunning = new AtomicBoolean(false);

    static boolean reconnectIndefinitely = true;
    static List<String> failedMessageListOnClose = new ArrayList<>(); // List of messages that failed on close
    private static CountDownLatch countDownLatch = new CountDownLatch(2);
    private static DeviceClientManager deviceClientManager;

    /**
     * Sends a number of messages to an IoT. Default protocol is to use AMQP transport.
     *
     */
    public static void main(String[] args)
            throws IOException, URISyntaxException, InterruptedException {
        log.debug("Starting the sample...");
        log.debug("Using communication protocol: {}", protocol.name());

        DeviceClient client = new DeviceClient(deviceConnectionString, protocol);
        log.debug("Successfully created an IoT Hub client.");

        deviceClientManager = new DeviceClientManager(client);
        deviceClientManager.setOperationTimeout(DEVICE_OPERATION_TIMEOUT_IN_MINUTES);

        deviceClientManager.connect();
        log.debug("Opened connection to IoT Hub.");

        log.debug("Setting C2D message handler...");
        deviceClientManager.setMessageCallback(new MessageCallback(), new Object());

        log.debug("Setting method handler...");
        deviceClientManager.subscribeToDeviceMethod(new SampleDeviceMethodCallback(), null, new DeviceMethodStatusCallBack(), null);

        log.debug("Start device Twin and get remaining properties...");
        // Properties already set in the Service will shows up in the generic onProperty callback, with value and version.
        Succeed.set(false);
        client.startDeviceTwin(new DeviceTwinStatusCallBack(), null, new onProperty(), null);
        do {
            Thread.sleep(1000);
        }
        while (! Succeed.get());

        log.debug("Subscribe to Desired properties on device Twin: PropertyKey={}...", TwinPropertyKey);
        Map<Property, Pair<TwinPropertyCallBack, Object>> propertyPairMap =
                singletonMap(new Property(TwinPropertyKey, null), new Pair<TwinPropertyCallBack, Object>(new onProperty(), null));
        deviceClientManager.subscribeToTwinDesiredProperties(propertyPairMap);

        log.debug("Start sending telemetry and reported property updates...");
        new Thread(new Runnable() {

            @Override
            public void run() {
                updateReportedProperties();
                countDownLatch.countDown();
            }
        }).start();

        new Thread(new Runnable() {

            @Override
            public void run() {
                sendTelemetry();
                countDownLatch.countDown();
            }
        }).start();

        countDownLatch.await(MAX_APPLICATION_RUN_DURATION_IN_MINUTES, TimeUnit.MINUTES);
        stopRunning.set(true);
        log.debug("Cancelling reported property updates and telemetry operation, application is ready to shutdown...");

        // close the connection
        log.debug("Closing");
        deviceClientManager.disconnect();

        if (! failedMessageListOnClose.isEmpty()) {
            log.debug("List of messages that were cancelled on close: {}", failedMessageListOnClose.toString());
        }

        log.debug("Shutting down...");
    }

    private static void updateReportedProperties() {
        log.debug("Updating reported properties...");
        for (int i = 0; i < numRequests; ++ i) {
            if (stopRunning.get()) break;

            int nextValue = ThreadLocalRandom.current().nextInt(70, 90);
            Set<Property> reportProperties = new HashSet<>(singletonList(new Property(TwinPropertyKey, nextValue)));
            try {
                deviceClientManager.sendReportedProperties(reportProperties);
                log.debug(">> Updating reported properties: {}={}...", TwinPropertyKey, nextValue);
                log.debug("Get device Twin...");
                deviceClientManager.getDeviceTwin();
            }
            catch (IOException e) {
                log.error("Error occurred while updating reported property...");
                e.printStackTrace();
            }
            try {
                log.debug("Sleeping for {} secs before sending the next property update.", SLEEP_DURATION_IN_SECS);
                Thread.sleep(SLEEP_DURATION_IN_SECS * 1000);
            }
            catch (InterruptedException e) {
                log.error("Exception thrown while sleeping...");
                e.printStackTrace();
            }
        }
    }

    private static void sendTelemetry() {
        log.debug("Sending the following event messages:");
        for (int i = 0; i < numRequests; ++ i) {
            if (stopRunning.get()) break;

            try {
                Message msg = composeMessage(i);
                EventCallback callback = new EventCallback();
                deviceClientManager.sendEventAsync(msg, callback, msg);
            }
            catch (Exception e) {
                log.error("Exception thrown while sending telemetry...");
                e.printStackTrace();
            }
            try {
                log.debug("Sleeping for {} secs before sending next message.", SLEEP_DURATION_IN_SECS);
                Thread.sleep(SLEEP_DURATION_IN_SECS * 1000);
            }
            catch (InterruptedException e) {
                log.error("Exception thrown while sleeping...");
                e.printStackTrace();
            }
        }
    }
}
