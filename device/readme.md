# Microsoft Azure IoT device SDK for Java

The Microsoft Azure IoT device SDK for Java facilitates building devices and applications that connect and are managed by Azure IoT Suite services.

## Features

 * Sends event data to Azure IoT based services.
 * Receives messages from Azure IoT Hub.
 * Maps server commands to device functions.
 * Buffers data when network connection is down.
 * Batches messages to improve communication efficiency.
 * Supports pluggable transport protocols.

## How to use the Azure IoT Device SDK for Java

For more information on how to use this library refer to the documents below:
- [Setup your development environment][devbox-setup]
- [Run the samples][run-java-sample]
- [How to build an end-to-end sample IoTHub Java sample from scratch on Windows][how-to-build-a-java-app-from-scratch]
- [Java Device API reference][java-api-reference]


## Folder structure of repository

All of the Java specific resources are located in the **java** folder.

### /iot-device-client

This folder contains the client library for Java.

### /iot-device-samples

This folder contains various Java samples that illustrate how to use the client library.

[devbox-setup]: ../doc/java-devbox-setup.md
[run-java-sample]: ./iot-device-samples/
[how-to-build-a-java-app-from-scratch]: https://azure.microsoft.com/documentation/articles/iot-hub-java-java-getstarted/
[java-api-reference]: https://azure.github.io/azure-iot-sdk-java/master/device/
