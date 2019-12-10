#  Invoke Command Sample

This sample demonstrates how to invoke a command onto a digital twin that is actively listening for commands

## How to run the sample

Note that the samples for Windows and Linux both use Maven, and the below commands work in both windows command line and in
a linux bash shell.

### Setup environment
Prepare your platform following the instructions [here][devbox-setup] to install Java and Maven.
If you want to run these samples in Intellij, follow [these][intellij-setup] instructions to prepare your Intellij settings

### Sample Arguments

In order to run this sample, you must set environment variables for:
- "IOTHUB_CONNECTION_STRING" - Your IoT Hub's connection string
- "DEVICE_ID" - The ID of the device to invoke the command onto
- "INTERFACE_INSTANCE_NAME" - The interface the command belongs to
- "COMMAND_NAME" - The name of the command to invoke on your digital twin
- "PAYLOAD" - (optional) The Json payload to include in the command

### Other Prerequisites
In order to run this sample, you will need an IoT Hub. You will also need at least one device registered in this hub so that the sample can invoke a command on that device
* [Setup Your IoT Hub][lnk-setup-iot-hub]

This device must also be registered to implement the interface whose command you want to invoke, and the device
must be online to receive the command invocation.

To see sample code that demonstrates how to register a device to implement an interface and to listen for commands, see the device sample [here](../device-samples)

### Run the sample

From the base digital-twin folder in this repo, run the following command:

```sh
mvn clean install -DskipTests
```

This will construct the jar file that you will need to run this sample, which contains all dependencies already included within the invoke-digital-twin-command-with-deps.jar file located in the target folder of the sample.

After building the jar succeeds, run this command to run the sample:
```sh
java -jar service-samples/invoke-digital-twin-command/target/invoke-digital-twin-command-with-deps.jar
```

This command will execute the sample which will invoke the specified command on the specified digital twin's interface, and will print out the command response

The command response will depend on how the device is setup to respond to your command invocation, but an example response is below:

```sh
Command someCommand invoked on the device successfully, the returned status was 200 and the request id was 0b7ff5f4-6245-4c6b-a891-f2d3dc802a41
The returned payload was
{"someKey" : "someValue"}
```

If your device is not online, then you will see an exception like the below:
```
Exception in thread "main" com.microsoft.rest.RestException: Status code 404, {"Message":"{\"errorCode\":404103,\"trackingId\":\"XXXXXXXXXXXXX-TimeStamp:1/1/1019 23:27:46\",\"message\":\"Timed out waiting for device to connect.\",\"info\":{\"timeout\":\"00:00:00\"},\"timestampUtc\":\"2019-11-12T22:27:46.5615872Z\"}","ExceptionMessage":""}
```

[lnk-setup-iot-hub]: https://aka.ms/howtocreateazureiothub
[devbox-setup]: ../../../doc/java-devbox-setup.md
[intellij-setup]: ../../../doc/building_sdk.md
