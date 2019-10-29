// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.synchronizedList;

@Slf4j
public class EventHubListener {
    private static final String EVENTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.EVENT_HUB_CONNECTION_STRING_VAR_NAME);
    private static final int LOOKBACK_TIME_IN_SECONDS = 30;;
    private static final int RECEIVE_EVENTS_FOR_SECONDS = 60;
    private static final int OPERATION_TIMEOUT_IN_SECONDS = 5;
    private static final int MAX_EVENTS_RECEIVED_PER_CALL = 100;
    private static final String TELEMETRY_PROPERTY_DEVICE_ID = "$.cdid";

    private static List<EventData> receivedEventData = new CopyOnWriteArrayList<>();
    private static List<PartitionReceiver> partitionReceivers = new ArrayList<>();
    private static EventHubClient ehClient;
    private static ScheduledExecutorService executorService;

    private static EventHubListener eventHubListener;
    private static final Object lock = new Object();
    private static AtomicBoolean isReceivingEvents = new AtomicBoolean(false);

    private EventHubListener() throws IOException, EventHubException {
        executorService = Executors.newScheduledThreadPool(4);
        ehClient = EventHubClient.createFromConnectionStringSync(EVENTHUB_CONNECTION_STRING, executorService);
    }

    public static EventHubListener getInstance() throws EventHubException, IOException {
        synchronized (lock) {
            if (eventHubListener == null) {
                eventHubListener = new EventHubListener();
            }
            return eventHubListener;
        }
    }

    public void startReceivingEvents() throws EventHubException, ExecutionException, InterruptedException {
        if (isReceivingEvents.get()) {
            return;
        }

        isReceivingEvents.set(true);

        EventHubRuntimeInformation eventHubInfo = ehClient.getRuntimeInformation().get();
        String[] partitionIds = eventHubInfo.getPartitionIds();

        for (String partitionId : partitionIds) {
            Instant lookbackInSeconds = Instant.now().minusSeconds(LOOKBACK_TIME_IN_SECONDS);
            PartitionReceiver partitionReceiver = ehClient.createReceiverSync(
                    EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
                    partitionId,
                    EventPosition.fromEnqueuedTime(lookbackInSeconds));
            partitionReceiver.setReceiveTimeout(Duration.ofSeconds(OPERATION_TIMEOUT_IN_SECONDS));
            partitionReceivers.add(partitionReceiver);

            log.debug("EventHub receiver created for partition {}, listening from {} [OperationTimeout: {}secs]", partitionId, lookbackInSeconds, OPERATION_TIMEOUT_IN_SECONDS);
            new Thread(() -> {
                try {
                    receiveMessages(partitionReceiver);
                }
                catch (ExecutionException | InterruptedException e) {
                    log.error("An exception was thrown while receiving messages: ", e);
                }
            }).start();
        }
    }

    private static void receiveMessages(PartitionReceiver partitionReceiver) throws ExecutionException, InterruptedException {
        while(isReceivingEvents.get()) {
            log.debug("Receiving from partition: {}", partitionReceiver.getPartitionId());
            partitionReceiver.receive(MAX_EVENTS_RECEIVED_PER_CALL).thenAcceptAsync(eventData -> {
                if (eventData == null) {
                    log.debug("No events received.");
                } else {
                    long batchSize = eventData.spliterator().getExactSizeIfKnown();
                    log.debug("ReceivedBatch Size: {}", batchSize);
                    for (EventData receivedEvent : eventData) {
                        String receivedDeviceId = receivedEvent.getProperties().get(TELEMETRY_PROPERTY_DEVICE_ID).toString();
                        String payload = new String(receivedEvent.getBytes(), Charset.defaultCharset());
                        log.info(">> EventData Received: deviceId={}: payload={}", receivedDeviceId, payload);
                        receivedEventData.add(receivedEvent);
                    }
                }
            }, executorService).get();
        }
    }

    public boolean verifyThatMessageWasReceived(String deviceId, String expectedPayload) {
        long receiveEndTime = System.currentTimeMillis() + RECEIVE_EVENTS_FOR_SECONDS * 1000;
        List<EventData> dataAlreadyChecked = synchronizedList(new ArrayList<>());
        log.debug(">> Expected payload: deviceId={}: payload={}", deviceId, expectedPayload);
        while (true) {
            for (EventData eventData : receivedEventData) {
                if (!dataAlreadyChecked.contains(eventData)) {
                    if (verifyTelemetryEvent(eventData, deviceId, expectedPayload)) {
                        log.debug(">> The expected payload was received");
                        receivedEventData.remove(eventData); // Have to remove since EventData "equals" is currently only on payload
                        return true;
                    }
                    dataAlreadyChecked.add(eventData);
                }
            }
            if (System.currentTimeMillis() > receiveEndTime) {
                log.error(">> The expected message was not received within the specified time of {} secs", RECEIVE_EVENTS_FOR_SECONDS);
                return false;
            }
        }
    }

    private static boolean verifyTelemetryEvent(EventData receivedEvent, String deviceId, String expectedPayload) {
        Map<String, Object> properties = receivedEvent.getProperties();
        String receivedDeviceId = properties.get(TELEMETRY_PROPERTY_DEVICE_ID).toString();
        String payload = new String(receivedEvent.getBytes(), Charset.defaultCharset());
        log.debug(">> Now verifying: deviceId={}: payload={}", receivedDeviceId, payload);

        return receivedDeviceId.equals(deviceId) && payload.equals(expectedPayload);
    }

    public void close() throws ExecutionException, InterruptedException {
        isReceivingEvents.set(false);
        for (PartitionReceiver partitionReceiver : partitionReceivers) {
            // cleaning up receivers is paramount;
            // Quota limitation on maximum number of concurrent receivers per consumergroup per partition is 5
            partitionReceiver.close()
                             .thenComposeAsync(aVoid -> ehClient.close(), executorService)
                             .whenCompleteAsync((aVoid, throwable) -> {
                                 if (throwable != null) {
                                     log.error("Closing partition with ID {} failed with error: {}", partitionReceiver.getPartitionId(), throwable);
                                 }
                             }, executorService).get();
        }
        executorService.shutdown();
    }
}
