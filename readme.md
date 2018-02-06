# Microsoft Azure IoT SDKs for Java
This repository contains the following:
* **Azure IoT Hub device SDK for Java**: to connect client devices to Azure IoT Hub (supports Java 7+ and Android API 17+)
* **Azure IoT Hub service SDK for Java**: enables developing back-end applications for Azure IoT (supports Java 7+)

The API reference documentation for the device SDK is [here][java-api-reference-device].

The API reference documentation for the service SDK is [here][java-api-reference-service].

To find SDKs in other languages for Azure IoT, please refer to the [azure-iot-sdks][azure-iot-sdks] repository

## Developing applications for Azure IoT
Visit [Azure IoT Dev Center](http://azure.com/iotdev) to learn more about developing applications for Azure IoT.

## How to use the Azure IoT SDKs for Java

Devices and data sources in an IoT solution can range from a simple network-connected sensor to a powerful, standalone computing device. Devices may have limited processing capability, memory, communication bandwidth, and communication protocol support. The IoT device SDKs enable you to implement client applications for a wide variety of devices.
* On Linux and Windows:
   * **Using Maven**: the simplest way to use the Azure IoT SDKs for Java to develop apps is to leverage Maven packages:
      * [Device SDK][device-maven]
      * [Service SDK][service-maven]
   * **Clone the repository**: 
   ```
    git clone  https://github.com/Azure/azure-iot-sdk-java.git  
   ```
   * **Working with the SDKs code**: if you are working with the SDKs code to modify it or contribute changes, then you can clone the repository and build the libraries:
      * [Build Device SDK from code][device-code]
      * [Build Service SDK from code][service-code]
* On Android: our Java device SDK can be used on Android using the API version 17 and higher:
   * [Device SDK][device-android]

## Key features and roadmap

### Device client SDK
:heavy_check_mark: feature available  :heavy_multiplication_x: feature planned but not supported  :heavy_minus_sign: no support planned

| Features                                                                                                         | mqtt                     | mqtt-ws                  | amqp                     | amqp-ws                  | https                    | Description                                                                                                                                                                                                                                                                                                                                 |
|------------------------------------------------------------------------------------------------------------------|--------------------------|--------------------------|--------------------------|--------------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Authentication](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-security-deployment)                     | :heavy_check_mark:       | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:       | Connect your device to IoT Hub securely with supported authentication, including private key, SASToken, X-509 Self Signed over MQTT, AMQPS and HTTPS, and X-509 Certificate Authority (CA) Signed over MQTT and AMQPS.                                                      |
| [Send device-to-cloud message](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-messages-d2c)     | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:*      | Send device-to-cloud messages (max 256KB) to IoT Hub with the option to add custom properties.  IoT Hub only supports batch send over AMQP and HTTPS only at the moment.  This SDK supports batch send over HTTP.  * Batch send over AMQP and AMQP-WS, and add system properties on D2C messages are in progress.                           |
| [Receive cloud-to-device messages](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-messages-c2d) | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:       | Receive cloud-to-device messages and read associated custom and system properties from IoT Hub, with the option to complete/reject/abandon C2D messages.  *C2D message size limit of 256 KB over MQTT and AMQP is in progress.  IoT Hub supports the option to complete/reject/abandon C2D messages over HTTPS and AMQP only at the moment. |
| [Device Twins](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins)                     | :heavy_check_mark:* | :heavy_check_mark:* |:heavy_check_mark:* | :heavy_check_mark:* | :heavy_minus_sign:       | IoT Hub persists a device twin for each device that you connect to IoT Hub.  The device can perform operations like get twin tags, subscribe to desired properties.  *Send reported properties version and desired properties version are in progress.                                                                                      |
| [Direct Methods](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-direct-methods)                 | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_minus_sign:       | IoT Hub gives you the ability to invoke direct methods on devices from the cloud.  The SDK supports handler for generic operation.                                                                                                                                                                                                          |
| [Upload file to Blob](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload)               | :heavy_minus_sign:       | :heavy_minus_sign:       | :heavy_minus_sign:       | :heavy_minus_sign:       | :heavy_check_mark:*      | A device can initiate a file upload and notifies IoT Hub when the upload is complete.   File upload requires HTTPS connection, but can be initiated from client using any protocol for other operations.  *Upload file to Blog with X509 certificate is in progress.                                                                        |
| [Connection Status and Error reporting](https://docs.microsoft.com/en-us/rest/api/iothub/common-error-codes)     | :heavy_multiplication_x: | :heavy_multiplication_x: | :heavy_multiplication_x: | :heavy_multiplication_x: | :heavy_multiplication_x: | Error reporting for IoT Hub supported error code.                                                                                                                                                                                                                                                                                           |
| Retry policies                                                                                                   | :heavy_multiplication_x: | :heavy_multiplication_x: | :heavy_multiplication_x: | :heavy_multiplication_x: | :heavy_multiplication_x: | Retry policy for unsuccessful device-to-cloud messages have three options: no try, exponential backoff with jitter (default) and custom.                                                                                                                                                                                                    |
| Devices multiplexing over single connection                                                                      | :heavy_minus_sign:       | :heavy_minus_sign:       | :heavy_check_mark: | :heavy_check_mark: | :heavy_multiplication_x: |                                                                                                                                                                                                                                                                                                                                             |
| Connection Pooling - Specifying number of connections                                                            | :heavy_minus_sign:       | :heavy_minus_sign:       | :heavy_multiplication_x:       | :heavy_multiplication_x:       | :heavy_multiplication_x:       |                                                                                                                                                                                                                                                                                                                                             |

### Service client SDK
:heavy_check_mark: feature available  :heavy_multiplication_x: feature planned but not supported  :heavy_minus_sign: no support planned

| Features                                                                                                      | Support             | Description                                                                                                                        |
|---------------------------------------------------------------------------------------------------------------|---------------------|------------------------------------------------------------------------------------------------------------------------------------|
| [Identity registry (CRUD)](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-identity-registry) | :heavy_check_mark:  | Use your backend app to perform CRUD operation for individual device or in bulk.                                                   |
| [Cloud-to-device messaging](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-messages-c2d)     | :heavy_check_mark:  | Use your backend app to send cloud-to-device messages in AMQP and AMQP-WS, and set up cloud-to-device message receivers.           |
| [Direct Methods operations](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-direct-methods)   | :heavy_check_mark:  | Use your backend app to invoke direct method on device.                                                                            |
| [Device Twins operations](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins)       | :heavy_check_mark:* | Use your backend app to perform device twin operations.  *Twin reported property update callback and replace twin are in progress. |
| [Query](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language)                       | :heavy_check_mark:  | Use your backend app to perform query for information.                                                                             |
| [Jobs](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-jobs)                                  | :heavy_check_mark:  | Use your backend app to perform job operation.                                                                                     |
| [File Upload](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload)                    | :heavy_check_mark:  | Set up your backend app to send file upload notification receiver.                                                                 |

### Provisioning client SDK
:heavy_check_mark: feature available  :heavy_multiplication_x: feature planned but not supported  :heavy_minus_sign: no support planned
This repository contains [provisioning device client SDK](./provisioning-device-client) for the [Device Provisioning Service](https://docs.microsoft.com/en-us/azure/iot-dps/). 

| Features                    | mqtt               | mqtt-ws            | amqp               | amqp-ws            | https              | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|-----------------------------|--------------------|--------------------|--------------------|--------------------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| TPM Individual Enrollment   |  :heavy_minus_sign: |  :heavy_minus_sign: | :heavy_check_mark: | :heavy_multiplication_x: | :heavy_check_mark: | This SDK supports connecting your device to the Device Provisioning Service via [individual enrollment](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-service#enrollment) using [Trusted Platform Module](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-security#trusted-platform-module-tpm).   Please visit the [samples folder](./provisioning/provisioning-samples/) and this [quickstart](https://docs.microsoft.com/en-us/azure/iot-dps/quick-create-simulated-device-tpm-java) on how to create a device client.  Websocket connection over AMQP is currently not available. TPM over MQTT is currently not supported by the Device Provisioning Service.                                                                                                                                                                                                              |
| X.509 Individual Enrollment | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | This SDK supports connecting your device to the Device Provisioning Service via [individual enrollment](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-service#enrollment) using [X.509 leaf certificate](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-security#leaf-certificate).   Please visit the [samples folder](./provisioning/provisioning-samples/) and this [quickstart](https://docs.microsoft.com/en-us/azure/iot-dps/quick-create-simulated-device-x509-java) on how to create a device client. |
| X.509 Enrollment Group      | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | This SDK supports connecting your device to the Device Provisioning Service via [enrollment group](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-service#enrollment) using [X.509 root certificate](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-security#root-certificate).   Please visit the [samples folder](./provisioning/provisioning-samples/) to learn more about this feature.                                                                                                                                                                                            |



### Provisioniong service client SDK
This repository contains [provisioning service client SDK](./provisioning/provisioning-service-client) for the Device Provisioning Service to [programmatically enroll devices](https://docs.microsoft.com/en-us/azure/iot-dps/how-to-manage-enrollments-sdks).

| Feature                                            | Support            | Description                                                                                                                                                                                                                                            |
|----------------------------------------------------|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| CRUD Operation with TPM Individual Enrollment      | :heavy_check_mark: | Programmatically manage device enrollment using TPM with the service SDK.  Please visit the [samples folder](./provisioning/provisioning-samples/) and this [quickstart](https://docs.microsoft.com/en-us/azure/iot-dps/quick-enroll-device-tpm-java) to learn more about this feature. |
| Bulk CRUD Operation with TPM Individual Enrollment | :heavy_check_mark: | Programmatically bulk manage device enrollment using TPM with the service SDK.  Please visit the [samples folder](./provisioning/provisioning-samples/) to learn more about this feature. |
| CRUD Operation with X.509 Individual Enrollment    | :heavy_check_mark: | Programmatically manage device enrollment using X.509 individual enrollment with the service SDK.  Please visit the [samples folder](./provisioning/provisioning-samples/) and this [quickstart](https://docs.microsoft.com/en-us/azure/iot-dps/quick-enroll-device-x509-java) to learn more about this feature. |
| CRUD Operation with X.509 Group Enrollment         | :heavy_check_mark: | Programmatically manage device enrollment using X.509 group enrollment with the service SDK.  Please visit the [samples folder](./provisioning/provisioning-samples/) to learn more about this feature. |
| Query enrollments                                  | :heavy_check_mark: | Programmatically query registration states with the service SDK.  Please visit the [samples folder](./provisioning/provisioning-samples/) to learn more about this feature.                                                                            |

## Samples
Whithin the repository, you can find various types of simple samples that can help you get started.
* [Device SDK Samples](./device/iot-device-samples/)
* [Service SDK Samples](./service/iot-service-samples)

## Contribution, feedback and issues
If you encounter any bugs, have suggestions for new features or if you would like to become an active contributor to this project please follow the instructions provided in the [contribution guidelines](.github/CONTRIBUTING.md).

## Support
If you are having issues using one of the packages or using the Azure IoT Hub service that go beyond simple bug fixes or help requests that would be dealt within the [issues section](https://github.com/Azure/azure-iot-sdk-java/issues) of this project, the Microsoft Customer Support team will try and help out on a best effort basis.
To engage Microsoft support, you can create a support ticket directly from the [Azure portal](https://ms.portal.azure.com/#blade/Microsoft_Azure_Support/HelpAndSupportBlade).
Escalated support requests for Azure IoT Hub SDKs development questions will only be available Monday thru Friday during normal coverage hours of 6 a.m. to 6 p.m. PST.
Here is what you can expect Microsoft Support to be able to help with:
* **Client SDKs issues**: If you are trying to compile and run the libraries on a supported platform, the Support team will be able to assist with troubleshooting or questions related to compiler issues and communications to and from the IoT Hub.  They will also try to assist with questions related to porting to an unsupported platform, but will be limited in how much assistance can be provided.  The team will be limited with trouble-shooting the hardware device itself or drivers and or specific properties on that device. 
* **IoT Hub / Connectivity Issues**: Communication from the device client to the Azure IoT Hub service and communication from the Azure IoT Hub service to the client.  Or any other issues specifically related to the Azure IoT Hub.
* **Portal Issues**: Issues related to the portal, that includes access, security, dashboard, devices, Alarms, Usage, Settings and Actions.
* **REST/API Issues**: Using the IoT Hub REST/APIs that are documented in the [documentation]( https://msdn.microsoft.com/library/mt548492.aspx).

## Read more
* [Azure IoT Hub documentation][iot-hub-documentation]
* [Prepare your development environment to use the Azure IoT device SDK for Java][devbox-setup]
* [Setup IoT Hub][setup-iothub]
* [Java SDKs API reference][java-api-reference]

## SDK folder structure

### /deps

This folder contains dependencies that are common across all clients

### /device

Contains Azure IoT Hub client components that provide the raw messaging capabilities of the library. Refer to the API documentation and samples for information on how to use it.

### /doc

This folder contains application development guides and device setup instructions.

### /iot-e2e-tests

This folder contains end to end tests source code for running on jvm and android.

### /jenkins

This folder contains scripts to build and run Java SDK provided proper environmental variables are set

### /service

Contains libraries that enable interactions with the IoT Hub service to perform operations such as sending messages to devices and managing the device identity registry. Refer to API documentation and samples for more details.

# Long Term Support

The project offers a Long Term Support (LTS) version to allow users that do not need the latest features to be shielded from unwanted changes.

A new LTS version will be created every 6 months. The lifetime of an LTS branch is currently planned for one year. LTS branches receive all bug fixes that fall in one of these categories:

- security bugfixes
- critical bugfixes (crashes, memory leaks, etc.)

No new features or improvements will be picked up in an LTS branch.

LTS branches are named lts_*mm*_*yyyy*, where *mm* and *yyyy* are the month and year when the branch was created. An example of such a branch is *lts_07_2017*.

## Schedule<sup>1</sup>

Below is a table showing the mapping of the LTS branches to the packages released

| Maven Package | Github Branch | LTS Status | LTS Start Date | Maintenance End Date | Removed Date |
| :-----------: | :-----------: | :--------: | :------------: | :------------------: | :----------: |
| 1.x.x         | lts_07_2017   | Active     | 2017-07-01     | 2017-12-31           | 2018-06-30   |

* <sup>1</sup> All scheduled dates are subject to change by the Azure IoT SDK team.

### Planned Release Schedule
![](./lts_branches.png)

---
This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

[iot-hub-documentation]: https://docs.microsoft.com/en-us/azure/iot-hub/
[azure-iot-sdks]: http://github.com/azure/azure-iot-sdks
[device-maven]: ./doc/java-devbox-setup.md#installiotmaven
[service-maven]: ./doc/java-devbox-setup.md#installiotmaven
[device-code]: ./doc/java-devbox-setup.md#installiotsource
[service-code]: ./doc/java-devbox-setup.md#installiotsource
[device-android]: ./doc/java-devbox-setup.md#installiotandroid
[java-api-reference-device]: https://azure.github.io/azure-iot-sdk-java/device/
[java-api-reference-service]: https://azure.github.io/azure-iot-sdk-java/service/
[devbox-setup]: doc/java-devbox-setup.md
[java-api-reference]: https://azure.github.io/azure-iot-sdk-java/
[setup-iothub]: https://aka.ms/howtocreateazureiothub

