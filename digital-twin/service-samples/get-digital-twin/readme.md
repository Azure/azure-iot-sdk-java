# Get Digital Twin Sample

This sample code demonstrates how to check the state of a single digital twin.

## How to run the sample

Note that the samples for Windows and Linux both use Maven, and the below commands work in both windows command line and in
a linux bash shell.

### Setup environment
Prepare your platform following the instructions [here][devbox-setup] to install Java and Maven.
If you want to run these samples in Intellij, follow [these][intellij-setup] instructions to prepare your Intellij settings

### Sample Arguments

In order to run this sample, you must set environment variables for:
- "IOTHUB_CONNECTION_STRING" : Your IoT Hub's connection string
- "DEVICE_ID" : The ID of the device to get the twin of

### Other Prerequisites
In order to run this sample, you will need an IoT Hub. You will also need at least one device registered in this hub so that the sample can lookup the state of that device
* [Setup Your IoT Hub][lnk-setup-iot-hub]

### Run the sample

From the base digital-twin folder in this repo, run the following command:

```sh
mvn clean install -DskipTests
```

This will construct the jar file that you will need to run this sample, which contains all dependencies already included within the get-digital-twin-with-deps.jar file located in the target folder of the sample.

After building the jar succeeds, run this command to run the sample:
```sh
java -jar service-samples/get-digital-twin/target/get-digital-twin-with-deps.jar
```

This command will execute the sample which will print out the state of the digital twin that was queried

```sh
Got the status of the digital twin successfully, the returned string was:
{
  "interfaces": {
    "urn_azureiot_ModelDiscovery_DigitalTwin": {
      "name": "urn_azureiot_ModelDiscovery_DigitalTwin",
      "properties": {
        "modelInformation": {
          "reported": {
            "value": {
              "interfaces": {
                "urn_azureiot_ModelDiscovery_DigitalTwin": "urn:azureiot:ModelDiscovery:DigitalTwin:1"
              }
            }
          }
        }
      }
    }
  },
  "version": 1
}
```

[lnk-setup-iot-hub]: https://aka.ms/howtocreateazureiothub
[devbox-setup]: ../../../doc/java-devbox-setup.md
[intellij-setup]: ../../../doc/building_sdk.md
