# Azure IoT Digital Twins Device SDK Sample

**PREVIEW - WILL LIKELY HAVE BREAKING CHANGES**

This project contains a sample implementation of a simulated environmental sensor. It shows how to:

  * Implement the environmental sensor interface
  * Create an interfaceInstance for this interface
  * Use the digital twin device client to register this interfaceInstance and interact with the Digital Twins services.
  * How to respond to command invocations
  * How to respond to property updates
  
For Android, please see [here](./Android)
## How to run the sample

### Setup environment
Prepare your platform following the instructions [here][devbox-setup] to install Java and Maven.
If you want to run these samples in Intellij, follow [these][intellij-setup] instructions to prepare your Intellij settings

### Sample Arguments

In order to run this sample, you must set environment variables for:
- "DIGITAL_TWIN_DEVICE_CONNECTION_STRING" : Your IoT Hub device's connection string

### Other Prerequisites
In order to run this sample, you will need an IoT Hub. You will also need at least one device registered in this hub so that the sample can register as that device
* [Setup Your IoT Hub][lnk-setup-iot-hub]

### Run the sample

From the base digital-twin folder in this repo, run the following command:

```sh
mvn clean install -DskipTests
```

This will construct the jar file that you will need to run this sample, which contains all dependencies already included within the environmental-sensor-with-deps.jar file located in the target folder of the sample.

After building the jar succeeds, run this command to run the sample:
```sh
java -jar device-samples\target\environmental-sensor-sample-with-deps.jar
```

The sample will register to use the Environmental Sensor interface, report some properties on the interface, send some telemetry on the
interface, and then will sit idle and wait for updates from the cloud such as command invocations and writable property updates

Note that the DigitalTwinClient depends on the DeviceClient class from the com.microsoft.azure.sdk.iot.iot-device-client library to communicate with the hub. The sample shows how to compose these two together.

[lnk-setup-iot-hub]: https://aka.ms/howtocreateazureiothub
[devbox-setup]: ../doc/java-devbox-setup.md
[intellij-setup]: ../doc/building_sdk.md
