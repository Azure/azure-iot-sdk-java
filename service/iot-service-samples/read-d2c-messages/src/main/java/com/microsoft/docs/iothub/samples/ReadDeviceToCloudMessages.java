// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

// This application uses the Microsoft Azure Event Hubs Client for Java
// For samples see: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/eventhubs/azure-messaging-eventhubs/src/samples
// For documentation see: https://docs.microsoft.com/azure/event-hubs/

package com.microsoft.docs.iothub.samples;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * A sample demonstrating how to receive events from Event Hubs sent from an IoT Hub device.
 */
public class ReadDeviceToCloudMessages {

  private static final String EH_COMPATIBLE_CONNECTION_STRING_FORMAT = "Endpoint=%s/;EntityPath=%s;"
      + "SharedAccessKeyName=%s;SharedAccessKey=%s";

  // az iot hub show --query properties.eventHubEndpoints.events.endpoint --name {your IoT Hub name}
  private static final String EVENT_HUBS_COMPATIBLE_ENDPOINT = "{your Event Hubs compatible endpoint}";

  // az iot hub show --query properties.eventHubEndpoints.events.path --name {your IoT Hub name}
  private static final String EVENT_HUBS_COMPATIBLE_PATH = "{your Event Hubs compatible name}";

  // az iot hub policy show --name service --query primaryKey --hub-name {your IoT Hub name}
  private static final String IOT_HUB_SAS_KEY = "{your service primary key}";
  private static final String IOT_HUB_SAS_KEY_NAME = "service";

  /**
   * The main method to start the sample application that receives events from Event Hubs sent from an IoT Hub device.
   *
   * @param args ignored args.
   * @throws Exception if there's an error running the application.
   */
  public static void main(String[] args) throws Exception {

    // Build the Event Hubs compatible connection string.
    String eventHubCompatibleConnectionString = String.format(EH_COMPATIBLE_CONNECTION_STRING_FORMAT,
        EVENT_HUBS_COMPATIBLE_ENDPOINT, EVENT_HUBS_COMPATIBLE_PATH, IOT_HUB_SAS_KEY_NAME, IOT_HUB_SAS_KEY);

    // Setup the EventHubBuilder by configuring various options as needed.
    EventHubClientBuilder eventHubClientBuilder = new EventHubClientBuilder()
        .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
        .connectionString(eventHubCompatibleConnectionString);

    // uncomment to setup proxy
    // setupProxy(eventHubClientBuilder);

    // uncomment to use Web Sockets
    // eventHubClientBuilder.transportType(AmqpTransportType.AMQP_WEB_SOCKETS);

    // Create an async consumer client as configured in the builder.
    try (EventHubConsumerAsyncClient eventHubConsumerAsyncClient = eventHubClientBuilder.buildAsyncConsumerClient()) {

      receiveFromAllPartitions(eventHubConsumerAsyncClient);

      // uncomment to run these samples
      // receiveFromSinglePartition(eventHubConsumerAsyncClient);
      // receiveFromSinglePartitionInBatches(eventHubConsumerAsyncClient);

      // Shut down cleanly.
      System.out.println("Press ENTER to exit.");
      System.in.read();
      System.out.println("Shutting down...");
    }
  }

  /**
   * This method receives events from all partitions asynchronously starting from the newly available events in
   * each partition.
   *
   * @param eventHubConsumerAsyncClient The {@link EventHubConsumerAsyncClient}.
   */
  private static void receiveFromAllPartitions(EventHubConsumerAsyncClient eventHubConsumerAsyncClient) {

    eventHubConsumerAsyncClient
        .receive(false) // set this to false to read only the newly available events
        .subscribe(partitionEvent -> {
          System.out.println();
          System.out.printf("%nTelemetry received from partition %s:%n%s",
              partitionEvent.getPartitionContext().getPartitionId(), partitionEvent.getData().getBodyAsString());
          System.out.printf("%nApplication properties (set by device):%n%s", partitionEvent.getData().getProperties());
          System.out.printf("%nSystem properties (set by IoT Hub):%n%s",
              partitionEvent.getData().getSystemProperties());
        }, ex -> {
          System.out.println("Error receiving events " + ex);
        }, () -> {
          System.out.println("Completed receiving events");
        });
  }

  /**
   * This method queries all available partitions in the Event Hub and picks a single partition to receive
   * events asynchronously starting from the newly available event in that partition.
   *
   * @param eventHubConsumerAsyncClient The {@link EventHubConsumerAsyncClient}.
   */
  private static void receiveFromSinglePartition(EventHubConsumerAsyncClient eventHubConsumerAsyncClient) {
    eventHubConsumerAsyncClient
        .getPartitionIds() // get all available partitions
        .take(1) // pick a single partition
        .flatMap(partitionId -> {
          System.out.println("Receiving events from partition id " + partitionId);
          return eventHubConsumerAsyncClient
              .receiveFromPartition(partitionId, EventPosition.latest());
        }).subscribe(partitionEvent -> {
          System.out.println();
          System.out.printf("%nTelemetry received from partition %s:%n%s",
              partitionEvent.getPartitionContext().getPartitionId(), partitionEvent.getData().getBodyAsString());
          System.out.printf("%nApplication properties (set by device):%n%s", partitionEvent.getData().getProperties());
          System.out.printf("%nSystem properties (set by IoT Hub):%n%s",
              partitionEvent.getData().getSystemProperties());
        }, ex -> {
          System.out.println("Error receiving events " + ex);
        }, () -> {
          System.out.println("Completed receiving events");
        }
    );
  }

  /**
   * This method queries all available partitions in the Event Hub and picks a single partition to receive
   * events asynchronously in batches of 100 events, starting from the newly available event in that partition.
   *
   * @param eventHubConsumerAsyncClient The {@link EventHubConsumerAsyncClient}.
   */
  private static void receiveFromSinglePartitionInBatches(EventHubConsumerAsyncClient eventHubConsumerAsyncClient) {
    int batchSize = 100;
    eventHubConsumerAsyncClient
        .getPartitionIds()
        .take(1)
        .flatMap(partitionId -> {
          System.out.println("Receiving events from partition id " + partitionId);
          return eventHubConsumerAsyncClient
              .receiveFromPartition(partitionId, EventPosition.latest());
        }).window(batchSize) // batch the events
        .subscribe(partitionEvents -> {
              partitionEvents.toIterable().forEach(partitionEvent -> {
                System.out.println();
                System.out.printf("%nTelemetry received from partition %s:%n%s",
                    partitionEvent.getPartitionContext().getPartitionId(), partitionEvent.getData().getBodyAsString());
                System.out.printf("%nApplication properties (set by device):%n%s",
                    partitionEvent.getData().getProperties());
                System.out.printf("%nSystem properties (set by IoT Hub):%n%s",
                        partitionEvent.getData().getSystemProperties());
              });
            }, ex -> {
              System.out.println("Error receiving events " + ex);
            }, () -> {
              System.out.println("Completed receiving events");
            }
        );
  }

  /**
   * This method sets up proxy options and updates the {@link EventHubClientBuilder}.
   *
   * @param eventHubClientBuilder The {@link EventHubClientBuilder}.
   */
  private static void setupProxy(EventHubClientBuilder eventHubClientBuilder) {
    int proxyPort = 8000; // replace with right proxy port
    String proxyHost = "{hostname}";
    Proxy proxyAddress = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
    String userName = "{username}";
    String password = "{password}";
    ProxyOptions proxyOptions = new ProxyOptions(ProxyAuthenticationType.BASIC, proxyAddress,
        userName, password);

    eventHubClientBuilder.proxyOptions(proxyOptions);

    // To use proxy, the transport type has to be Web Sockets.
    eventHubClientBuilder.transportType(AmqpTransportType.AMQP_WEB_SOCKETS);
  }
}
