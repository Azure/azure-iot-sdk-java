# Notes

This sample demonstrates how to use the Microsoft Azure Event Hubs Client for Java to 
read messages sent from a device by using the built-in event hubs that exists by default for
every Iot Hub instance. 

## Get Event Hubs-compatible connection string

You can get the Event Hubs-compatible connection string to your IotHub instance via the Azure portal or
by using the Azure CLI.

If using the Azure portal, see [Built in endpoints for IotHub](https://docs.microsoft.com/azure/iot-hub/iot-hub-devguide-messages-read-builtin#read-from-the-built-in-endpoint) to get the Event Hubs-compatible
connection string and assign it to the constant `connectionString` in the sample. You can skip the Azure CLI
instructions in the sample after this.

If using the Azure CLI, you will need to run the following commands before running this sample to get 
the details required to form the Event Hubs compatible connection string.

- `az iot hub show --query properties.eventHubEndpoints.events.endpoint --name {your IoT Hub name}`
- `az iot hub show --query properties.eventHubEndpoints.events.path --name {your IoT Hub name}`
- `az iot hub policy show --name service --query primaryKey --hub-name {your IoT Hub name}`

## Checkpointing

For an example that uses checkpointing, follow up this sample with the [sample that uses
Azure Storage Blob to create checkpoints](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/src/samples/java/com/azure/messaging/eventhubs/checkpointstore/blob/EventProcessorBlobCheckpointStoreSample.java).

Note that this requires adding a new dependency in your `pom.xml` to use the [Azure Storage Blob checkpoint store](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/README.md).

## WebSocket and proxy

If using WebSockets, configure the `EventHubClientBuilder` to use transport type `AmqpTransportType.AMQP_WEB_SOCKETS`.

If your application runs behind a proxy server, then, in addition to setting the transport type to 
`AmqpTransportType.AMQP_WEB_SOCKETS`, you also need to configure the proxy options as shown in the `setupProxy` method in 
[the sample](./src/main/java/com/microsoft/docs/iothub/samples/ReadDeviceToCloudMessages.java).
