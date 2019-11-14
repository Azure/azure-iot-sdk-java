#  Update Digital Twin Sample

This sample demonstrates how to update a single property on a single interface on a digital twin. There is also unused
code that shows how to update multiple properties on a single interface on a digital twin.

## How to run the sample

Note that the samples for Windows and Linux both use Maven, and the below commands work in both windows command line and in
a linux bash shell.

### Setup environment
Prepare your platform following the instructions [here][devbox-setup] to install Java and Maven.
If you want to run these samples in Intellij, follow [these][intellij-setup] instructions to prepare your Intellij settings

### Sample Arguments

In order to run this sample, you must set environment variables for:
- "IOTHUB_CONNECTION_STRING" : Your IoT Hub's connection string
- "DEVICE_ID" : The ID of the device to update the property on
- "INTERFACE_INSTANCE_NAME" : The interface the property belongs to
- "PROPERTY_NAME" : The name of the property to update on your digital twin
- "PROPERTY_VALUE" : The value of the property to set

### Other Prerequisites
In order to run this sample, you will need an IoT Hub. You will also need at least one device registered in this hub so that the sample can update properties on that device
* [Setup Your IoT Hub][lnk-setup-iot-hub]

While not mandatory, it is recommended that this registered device is online when the service client updates the property so the device can get a callback when its property is updated.
The property update will still work when the device is offline, though.

To see sample code that demonstrates how to register a device to implement an interface, see the device sample [here](../device-samples)

### Run the sample

From the base digital-twin folder in this repo, run the following command:

```sh
mvn clean install -DskipTests
```

This will construct the jar file that you will need to run this sample, which contains all dependencies already included within the update-digital-twin-with-deps.jar file located in the target folder of the sample.

After building the jar succeeds, run this command to run the sample:
```sh
java -jar service-samples/update-digital-twin/target/update-digital-twin-with-deps.jar
```

This command will execute the sample which will invoke the specified command on the specified digital twin's interface, and will print out the command response

```sh
Property updated on the device successfully, the returned payload was
{
  "interfaces": {
    "urn_azureiot_ModelDiscovery_DigitalTwin": {
      "name": "urn_azureiot_ModelDiscovery_DigitalTwin",
      "properties": {
        "modelInformation": {
          "reported": {
            "value": {
              "modelId": "urn:contoso:azureiot:sdk:testinterface:cm:1",
              "interfaces": {
                "urn_azureiot_ModelDiscovery_ModelInformation": "urn:azureiot:ModelDiscovery:ModelInformation:1",
                "urn_azureiot_Client_SDKInformation": "urn:azureiot:Client:SDKInformation:1",
                "deviceInformation": "urn:azureiot:DeviceManagement:DeviceInformation:1",
                "testInterfaceInstanceName": "urn:contoso:azureiot:sdk:testinterface:1",
                "urn_azureiot_ModelDiscovery_DigitalTwin": "urn:azureiot:ModelDiscovery:DigitalTwin:1"
              }
            }
          }
        }
      }
    },
    "testInterfaceInstanceName": {
      "name": "testInterfaceInstanceName",
      "properties": {
        "writableProperty": {
          "desired": {
            "value": "someString"
          }
        }
      }
    },
    "urn_azureiot_Client_SDKInformation": {
      "name": "urn_azureiot_Client_SDKInformation",
      "properties": {
        "language": {
          "reported": {
            "value": "Csharp"
          }
        },
        "version": {
          "reported": {
            "value": "0.0.1"
          }
        },
        "vendor": {
          "reported": {
            "value": "Microsoft"
          }
        }
      }
    }
  },
  "version": 2
}
```

[lnk-setup-iot-hub]: https://aka.ms/howtocreateazureiothub
[devbox-setup]: ../../../doc/java-devbox-setup.md
[intellij-setup]: ../../../doc/building_sdk.md
