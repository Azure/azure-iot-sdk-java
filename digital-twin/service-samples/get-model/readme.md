# Get Model Sample

This sample demonstrates how to retrieve a model definition from the model repo

## How to run the sample

Note that the samples for Windows and Linux both use Maven, and the below commands work in both windows command line and in
a linux bash shell.

### Setup environment
Prepare your platform following the instructions [here][devbox-setup] to install Java and Maven.
If you want to run these samples in Intellij, follow [these][intellij-setup] instructions to prepare your Intellij settings

### Sample Arguments

In order to run this sample, you must set environment variables for:
- "IOTHUB_CONNECTION_STRING" : Your IoT Hub's connection string
- "MODEL_ID" : Your model id to look up the full definition for

### Other Prerequisites
In order to run this sample, you will need an IoT Hub. You will also need at least one device registered in this hub so that the sample can lookup the state of that device
* [Setup Your IoT Hub][lnk-setup-iot-hub]

### Run the sample

From the base digital-twin folder in this repo, run the following command:

```sh
mvn clean install -DskipTests
```

This will construct the jar file that you will need to run this sample, which contains all dependencies already included within the get-model-with-deps.jar file located in the target folder of the sample.

After building the jar succeeds, run this command to run the sample:
```sh
java -jar service-samples/get-model/target/get-model-with-deps.jar
```

This command will execute the sample which will print out the model definition that the provided model id belongs to

```sh
Successfully retrieved the model, the definition is:

{
  "@id": "urn:azureiot:DeviceManagement:DeviceInformation:1",
  "@type": "Interface",
  "displayName": "Device Information",
  "contents": [
    {
      "@type": "Property",
      "name": "manufacturer",
      "displayName": "Manufacturer",
      "schema": "string",
      "description": "Company name of the device manufacturer. This could be the same as the name of the original equipment manufacturer (OEM). Ex. Contoso."
    },
    {
      "@type": "Property",
      "name": "model",
      "displayName": "Device model",
      "schema": "string",
      "description": "Device model name or ID. Ex. Surface Book 2."
    },
    {
      "@type": "Property",
      "name": "swVersion",
      "displayName": "Software version",
      "schema": "string",
      "description": "Version of the software on your device. This could be the version of your firmware. Ex. 1.3.45"
    },
    {
      "@type": "Property",
      "name": "osName",
      "displayName": "Operating system name",
      "schema": "string",
      "description": "Name of the operating system on the device. Ex. Windows 10 IoT Core."
    },
    {
      "@type": "Property",
      "name": "processorArchitecture",
      "displayName": "Processor architecture",
      "schema": "string",
      "description": "Architecture of the processor on the device. Ex. x64 or ARM."
    },
    {
      "@type": "Property",
      "name": "processorManufacturer",
      "displayName": "Processor manufacturer",
      "schema": "string",
      "description": "Name of the manufacturer of the processor on the device. Ex. Intel."
    },
    {
      "@type": "Property",
      "name": "totalStorage",
      "displayName": "Total storage",
      "schema": "long",
      "displayUnit": "kilobytes",
      "description": "Total available storage on the device in kilobytes. Ex. 2048000 kilobytes."
    },
    {
      "@type": "Property",
      "name": "totalMemory",
      "displayName": "Total memory",
      "schema": "long",
      "displayUnit": "kilobytes",
      "description": "Total available memory on the device in kilobytes. Ex. 256000 kilobytes."
    }
  ],
  "@context": "http://azureiot.com/v1/contexts/IoTModel.json"
}
```

[lnk-setup-iot-hub]: https://aka.ms/howtocreateazureiothub
[devbox-setup]: ../../../doc/java-devbox-setup.md
[intellij-setup]: ../../../doc/building_sdk.md
