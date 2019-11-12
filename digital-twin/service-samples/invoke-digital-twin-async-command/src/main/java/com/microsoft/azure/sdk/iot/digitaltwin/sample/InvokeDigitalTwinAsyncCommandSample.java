// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class InvokeDigitalTwinAsyncCommandSample {
    private static final int THREAD_POOL_MAX_NUMBER = 100;
    private static final int RECEIVE_TIMEOUT = 5;
    private static final String COMMAND_REQUEST_ID_PROPERTY_NAME = "iothub-command-request-id";

    private static String CONNECTION_STRING = System.getenv("CONNECTION_STRING");
    private static String DIGITAL_TWIN_ID = System.getenv("DIGITAL_TWIN_ID");
    private static String INTERFACE_INSTANCE_NAME = System.getenv("INTERFACE_INSTANCE_NAME");
    private static String ASYNC_COMMAND_NAME = System.getenv("ASYNC_COMMAND_NAME");
    private static String EVENTHUB_CONNECTION_STRING = System.getenv("EVENTHUB_CONNECTION_STRING");
    private static String PAYLOAD = System.getenv("PAYLOAD"); //optional

    private static final String usage =
            "In order to run this sample, you must set environment variables for \n" +
                    "IOTHUB_CONNECTION_STRING - Your IoT Hub's connection string\n" +
                    "DIGITAL_TWIN_ID - Your digital twin id to invoke the command onto\n" +
                    "INTERFACE_INSTANCE_NAME - The interface the command belongs to\n" +
                    "ASYNC_COMMAND_NAME - The name of the command to invoke on your digital twin\n" +
                    "EVENTHUB_CONNECTION_STRING - The connection string to the EventHub associated to your IoT Hub\n" +
                    "PAYLOAD - (optional) The json payload to include in the command";

    public static void main(String[] args) throws InterruptedException, ExecutionException, EventHubException, IOException {
        verifyInputs();

        DigitalTwinServiceClient digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString().connectionString(CONNECTION_STRING).build();

        log.info("Invoking " + ASYNC_COMMAND_NAME + " on device " + DIGITAL_TWIN_ID + " with interface instance name " + INTERFACE_INSTANCE_NAME);

        DigitalTwinCommandResponse digitalTwinCommandResponse = digitalTwinServiceClient.invokeCommand(DIGITAL_TWIN_ID, INTERFACE_INSTANCE_NAME, ASYNC_COMMAND_NAME, PAYLOAD);

        log.info("Command invoked on the device successfully, the returned status was " + digitalTwinCommandResponse.getStatus() + " and the request id was " + digitalTwinCommandResponse.getRequestId());
        log.info("The returned payload was ");
        log.info(digitalTwinCommandResponse.getPayload());

        listenForAsyncCommandUpdates(digitalTwinCommandResponse.getRequestId());
    }

    private static void listenForAsyncCommandUpdates(String requestId) throws ExecutionException, InterruptedException, EventHubException, IOException {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(THREAD_POOL_MAX_NUMBER);
        EventHubClient eventHubClient = EventHubClient.createFromConnectionStringSync(EVENTHUB_CONNECTION_STRING, executorService);
        EventHubRuntimeInformation eventHubInfo = eventHubClient.getRuntimeInformation().get();
        String[] partitionIds = eventHubInfo.getPartitionIds();

        for (String partitionId : partitionIds) {
            Instant lookbackInSeconds = Instant.now().minusSeconds(0);
            PartitionReceiver partitionReceiver = eventHubClient.createReceiverSync(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId, EventPosition.fromEnqueuedTime(lookbackInSeconds));
            partitionReceiver.setReceiveTimeout(Duration.ofSeconds(RECEIVE_TIMEOUT));

            log.debug("EventHub receiver created for partition {}, listening from {} [OperationTimeout: {} secs]", partitionId, lookbackInSeconds, RECEIVE_TIMEOUT);
            new Thread(() -> {
                while (!executorService.isShutdown()) {
                    try {
                        receiveMessages(partitionReceiver, executorService, requestId);
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        log.info("Eventhub receiver thread interrupted, gracefully closing the receiver now");
                        try {
                            partitionReceiver.closeSync();
                        }
                        catch (EventHubException ex) {
                            log.error("Failed to close partition receiver", ex);
                        }
                    }
                    catch (ExecutionException e) {
                        log.error("Eventhub receiver thread encountered ExecutionException, gracefully closing the receiver now", e);
                        try {
                            partitionReceiver.closeSync();
                        }
                        catch (EventHubException ex) {
                            log.error("Failed to close partition receiver", ex);
                        }
                    }
                }
            }).start();
        }

        //Wait for user to enter a key to exit the program
        new Scanner(System.in).nextLine();
        executorService.shutdownNow();
        System.exit(0);
    }

    private static void receiveMessages(PartitionReceiver partitionReceiver, ScheduledExecutorService executorService, String requestId) throws InterruptedException, ExecutionException {
        log.trace("Receiving from partition: {}", partitionReceiver.getPartitionId());
        partitionReceiver.receive(1).thenAcceptAsync(eventData -> {
            if (eventData == null) {
                log.trace("No events received.");
            }
            else {
                long batchSize = eventData.spliterator().getExactSizeIfKnown();
                log.trace("ReceivedBatch Size: {}", batchSize);

                for (EventData receivedEvent : eventData) {
                    if (receivedEvent.getProperties() != null && receivedEvent.getProperties().keySet().contains(COMMAND_REQUEST_ID_PROPERTY_NAME)) {
                        String payload = new String(receivedEvent.getBytes(), Charset.forName("UTF-8"));

                        log.info("Received an update on the async command:");
                        for (String propertyKey : receivedEvent.getProperties().keySet()) {
                            log.info("    " + propertyKey + ":" + receivedEvent.getProperties().get(propertyKey));
                        }
                        log.info("    " + "Update Payload: ");
                        log.info("        " + payload);

                        if (payload.contains("100%")) {
                            log.info("Async command has finished, enter any key to finish\n");
                        }
                    }
                }
            }
        }, executorService).get();
    }

    private static void verifyInputs() {
        if (isNullOrEmpty(CONNECTION_STRING) || isNullOrEmpty(DIGITAL_TWIN_ID) || isNullOrEmpty(INTERFACE_INSTANCE_NAME) || isNullOrEmpty(ASYNC_COMMAND_NAME)) {
            log.warn(usage);
            System.exit(0);
        }
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }
}
