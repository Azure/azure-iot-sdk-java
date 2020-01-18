// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventHubRuntimeInformation;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public final class EventHubListener {
    private static final String EVENT_HUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.EVENT_HUB_CONNECTION_STRING_VAR_NAME);
    private static final int LOOK_BACK_TIME_IN_SECONDS = 30;
    private static final int RECEIVE_EVENTS_FOR_SECONDS = 60;
    private static final int OPERATION_TIMEOUT_IN_SECONDS = 5;
    private static final int MAX_EVENTS_RECEIVED_PER_CALL = 100;
    private static final String TELEMETRY_PROPERTY_DEVICE_ID = "$.cdid";
    private static final Map<String, EventData> RECEIVED_EVENT_DATA = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(4);
    static {
        try {
            startReceivingEvents();
        }
        catch (Exception e) {
            log.error("StartReceivingEvents failed.", e);
        }
    }
    private static void startReceivingEvents() throws EventHubException, ExecutionException, InterruptedException, IOException {
        EventHubClient eventHubClient = EventHubClient.createFromConnectionStringSync(EVENT_HUB_CONNECTION_STRING, EXECUTOR_SERVICE);
        EventHubRuntimeInformation eventHubInfo = eventHubClient.getRuntimeInformation().get();
        String[] partitionIds = eventHubInfo.getPartitionIds();

        for (String partitionId : partitionIds) {
            Instant lookbackInSeconds = Instant.now().minusSeconds(LOOK_BACK_TIME_IN_SECONDS);
            PartitionReceiver partitionReceiver = eventHubClient.createReceiverSync(
                    EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
                    partitionId,
                    EventPosition.fromEnqueuedTime(lookbackInSeconds));
            partitionReceiver.setReceiveTimeout(Duration.ofSeconds(OPERATION_TIMEOUT_IN_SECONDS));

            log.debug("EventHub receiver created for partition {}, listening from {} [OperationTimeout: {}secs]", partitionId, lookbackInSeconds, OPERATION_TIMEOUT_IN_SECONDS);
            new Thread(() -> {
                while (true) {
                    try {
                        receiveMessages(partitionReceiver);
                        Thread.sleep(1000);
                    }
                    catch (ExecutionException | InterruptedException e) {
                        log.error("An exception was thrown while receiving messages: ", e);
                    }
                }
            }).start();
        }
    }

    private static void receiveMessages(PartitionReceiver partitionReceiver) throws ExecutionException, InterruptedException {
        log.trace("Receiving from partition: {}", partitionReceiver.getPartitionId());
        partitionReceiver.receive(MAX_EVENTS_RECEIVED_PER_CALL).thenAcceptAsync(eventData -> {
            if (eventData == null) {
                log.trace("No events received.");
            } else {
                long batchSize = eventData.spliterator().getExactSizeIfKnown();
                log.trace("ReceivedBatch Size: {}", batchSize);

                for (EventData receivedEvent : eventData) {
                    Map<String, Object> receivedEventProperties = receivedEvent.getProperties();
                    if (receivedEventProperties != null && receivedEventProperties.containsKey(TELEMETRY_PROPERTY_DEVICE_ID)) {
                        String receivedDeviceId = receivedEventProperties.get(TELEMETRY_PROPERTY_DEVICE_ID).toString();
                        String payload = new String(receivedEvent.getBytes(), Charset.defaultCharset());
                        log.trace(">> EventData Received: deviceId={}: payload={}", receivedDeviceId, payload);
                        RECEIVED_EVENT_DATA.put(payload, receivedEvent);
                    }
                }
            }
        }, EXECUTOR_SERVICE).get();
    }

    public static boolean verifyThatMessageWasReceived(String deviceId, String expectedPayload) throws InterruptedException {
        log.debug(">> Expected payload: deviceId={}, payload={}", deviceId, expectedPayload);

        long receiveEndTime = System.currentTimeMillis() + RECEIVE_EVENTS_FOR_SECONDS * 1000;
        while (!RECEIVED_EVENT_DATA.containsKey(expectedPayload) && System.currentTimeMillis() < receiveEndTime) {
            Thread.sleep(1000);
        }
        EventData eventData = RECEIVED_EVENT_DATA.remove(expectedPayload);
        boolean matched = Objects.nonNull(eventData) && verifyTelemetryEvent(eventData, deviceId, expectedPayload);
        if (!matched) {
            log.error(">> The expected message was not received within the specified time of {} secs", RECEIVE_EVENTS_FOR_SECONDS);
        }
        return matched;
    }

    private static boolean verifyTelemetryEvent(EventData receivedEvent, String deviceId, String expectedPayload) {
        Map<String, Object> properties = receivedEvent.getProperties();
        String receivedDeviceId = properties.get(TELEMETRY_PROPERTY_DEVICE_ID).toString();
        String payload = new String(receivedEvent.getBytes(), Charset.defaultCharset());
        log.trace(">> Now verifying: deviceId={}: payload={}", receivedDeviceId, payload);

        return receivedDeviceId.equals(deviceId) && payload.equals(expectedPayload);
    }
}
