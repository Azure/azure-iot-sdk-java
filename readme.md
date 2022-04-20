# Microsoft Azure IoT SDKs for Java

## 2.0.0 clients release notice

[A 2.0.0 release](https://github.com/Azure/azure-iot-sdk-java/releases) has been published for each package 
in this library. With this release, all future features will be brought only to this new major version, so users are highly
encouraged to migrate from the 1.X.X releases to the new 2.0.0 releases. We have released 
[one final LTS release](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-03-04) for the 1.X.X packages that will
be supported like any other LTS release.

If you need any help migrating your code to try out the new 2.X.X clients, please see this [migration guide](./SDK%20v2%20migration%20guide.md).

## Critical upcoming change notice

All Azure IoT SDK users are advised to be aware of upcoming TLS certificate changes for Azure IoT hub and Device Provisioning Service 
that will impact the SDK's ability to connect. In October 2022, both services will migrate from the current 
[Baltimore CyberTrust CA Root](https://baltimore-cybertrust-root.chain-demos.digicert.com/info/index.html) to the 
[DigiCert Global G2 CA root](https://global-root-g2.chain-demos.digicert.com/info/index.html). There will be a 
transition period beforehand where your IoT devices must have both the Baltimore and Digicert public certificates 
installed in their certificate store in order to prevent connectivity issues. 

For a more in depth explanation as to why the IoT services are doing this, please see
[this article](https://techcommunity.microsoft.com/t5/internet-of-things/azure-iot-tls-critical-changes-are-almost-here-and-why-you/ba-p/2393169).

Users of this Java IoT SDK in particular will need to follow slightly different instructions in order to handle this 
upcoming change. See [this document](./upcoming_certificate_changes_readme.md) for a more in depth explanation of how 
to prepare your devices for this certificate migration.

**Users who don't follow these instructions will begin experiencing unrecoverable, consistent connection failures from 
their devices starting June 2022.**

If you have any questions, comments, or concerns about this upcoming change, please let us know on our [discussions page](https://github.com/Azure/azure-iot-sdk-java/discussions).

## Build status

Due to security considerations, build logs are not publicly available.

| Service Environment      | Status |
| ---                      | ---    |
| Main                     | [![Build Status](https://azure-iot-sdks.visualstudio.com/azure-iot-sdks/_apis/build/status/java/pull_request_validation/Java%20Prod?branchName=main)](https://azure-iot-sdks.visualstudio.com/azure-iot-sdks/_build/latest?definitionId=252&branchName=main)|
| Preview                  | [![Build Status](https://azure-iot-sdks.visualstudio.com/azure-iot-sdks/_apis/build/status/java/pull_request_validation/Java%20Canary?branchName=preview)](https://azure-iot-sdks.visualstudio.com/azure-iot-sdks/_build/latest?definitionId=245&branchName=preview)|

This repository contains the following:

* **Azure IoT Hub device SDK for Java**: connect client devices to Azure IoT Hub
* **Azure IoT Hub service SDK for Java**: enables developing back-end applications for Azure IoT
* **Azure IoT Device Provisioning device SDK for Java**: provision devices to Azure IoT Hub using Azure IoT Device Provisioning
* **Azure IoT Device Provisioning service SDK for Java**: manage your Provisioning service instance from a back-end Java application

To find SDKs in other languages for Azure IoT, please refer to the [azure-iot-sdks][azure-iot-sdks] repository

## Developing applications for Azure IoT

Visit [Azure IoT Dev Center](http://azure.com/iotdev) to learn more about developing applications for Azure IoT.

## How to use the Azure IoT SDKs for Java

Devices and data sources in an IoT solution can range from a simple network-connected sensor to a powerful, standalone computing device. Devices may have limited processing capability, memory, communication bandwidth, and communication protocol support. The IoT device SDKs enable you to implement client applications for a wide variety of devices.

* On Linux and Windows:
  * **Using Maven**: the simplest way to use the Azure IoT SDKs for Java to develop apps is to leverage Maven packages:
    * [Device SDK][device-maven]
    * [Service SDK][service-maven]
  * **Clone the repository**: `git clone https://github.com/Azure/azure-iot-sdk-java.git`
  * **Working with the SDKs code**: if you are working with the SDKs code to modify it or contribute changes, then you can clone the repository and build the libraries:
    * [Build Device SDK from code][device-code]
    * [Build Service SDK from code][service-code]
* On Android: our Java device SDK can be used on Android:
  * [Device SDK][device-android]

For more details on what platforms this SDK supports, see [this document](./supported_platforms.md).

## API reference

* [Azure IoT Hub device SDK][java-api-reference-device]
* [Azure IoT Hub service SDK][java-api-reference-service]
* [Azure IoT Hub Device Provisioning device SDK][java-api-reference-device-dps]
* [Azure IoT Hub Device Provisioning service SDK][java-api-reference-service-dps]

## Key features and roadmap

## Device client SDK

:heavy_check_mark: feature available  :heavy_multiplication_x: feature planned but not supported  :heavy_minus_sign: no support planned

| Features                                                                                                         | mqtt                     | mqtt-ws                  | amqp                     | amqp-ws                  | https                    | Description                                                                                                                                                                                                                                                                                                                                 |
|------------------------------------------------------------------------------------------------------------------|--------------------------|--------------------------|--------------------------|--------------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Authentication](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-security-deployment)                     | :heavy_check_mark:       | :heavy_check_mark:      | :heavy_check_mark:      | :heavy_check_mark:*      | :heavy_check_mark:       | Connect your device to IoT Hub securely with supported authentication, including private key, SASToken, X-509 Self Signed over MQTT, AMQPS and HTTPS, and X-509 Certificate Authority (CA) Signed. *Java SDK does not support authentication over AMQP websockets.                                                     |
| [Send device-to-cloud message](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-messages-d2c)     | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:      | Send device-to-cloud messages (max 256KB) to IoT Hub with the option to add custom properties.  *IoT Hub supports batch send over AMQP and HTTPS at the moment, Java SDK only supports HTTPS. The MQTT and AMQP implementation loops over the batch and sends each message individually.                           |
| [Receive cloud-to-device messages](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-messages-c2d) | :heavy_check_mark:*      | :heavy_check_mark:*      | :heavy_check_mark:      | :heavy_check_mark:      | :heavy_check_mark:       | Receive cloud-to-device messages and read associated custom and system properties from IoT Hub, with the option to complete/reject/abandon C2D messages. *IoT Hub does not support option to reject/abandon C2D messages over MQTT at the moment. |
| [Device Twins](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins)                     | :heavy_check_mark: | :heavy_check_mark: |:heavy_check_mark: | :heavy_check_mark: | :heavy_minus_sign:       | IoT Hub persists a device twin for each device that you connect to IoT Hub.  The device can perform operations like get twin tags, subscribe to desired properties.                                                                                      |
| [Direct Methods](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-direct-methods)                 | :heavy_check_mark:      | :heavy_check_mark:      | :heavy_check_mark:      | :heavy_check_mark:      | :heavy_minus_sign:       | IoT Hub gives you the ability to invoke direct methods on devices from the cloud.  The SDK supports handler for generic operation.                                                                                                                                                                                                          |
| [Upload file to Blob](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload)               | :heavy_minus_sign:       | :heavy_minus_sign:       | :heavy_minus_sign:       | :heavy_minus_sign:       | :heavy_check_mark:      | A device can initiate a file upload and notifies IoT Hub when the upload is complete.   File upload requires HTTPS connection, but can be initiated from client using any protocol for other operations such as telemetry.                                                                        |
| [Connection Status and Error reporting](https://docs.microsoft.com/en-us/rest/api/iothub/common-error-codes)     | :heavy_multiplication_x: | :heavy_multiplication_x: | :heavy_multiplication_x: | :heavy_multiplication_x: | :heavy_multiplication_x: | Error reporting for IoT Hub supported error code.                                                                                                                                                                                                                                                                                           |
| Retry policies                                                                                                   | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | Retry policy for unsuccessful device-to-cloud messages have three options: no try, exponential backoff with jitter (default) and custom.  Detail implementation is documented [here](https://github.com/Azure/azure-iot-sdk-java/blob/main/device/iot-device-client/devdoc/requirement_docs/com/microsoft/azure/iothub/retryPolicy.md).                                                                                                                                                                                                    |
| Devices multiplexing over single connection                                                                      | :heavy_minus_sign:       | :heavy_minus_sign:       | :heavy_check_mark: | :heavy_check_mark: | :heavy_multiplication_x: |                                                                                                                                                                                                                                                                                                                                             |
| Connection Pooling * Specifying number of connections                                                            | :heavy_minus_sign:       | :heavy_minus_sign:       | :heavy_multiplication_x:       | :heavy_multiplication_x:       | :heavy_multiplication_x:       |                                                                                                                                                                                                                                                                                                                                             |

## Service client SDK

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
| [Digital Twin Client](https://docs.microsoft.com/en-us/azure/iot-pnp/overview-iot-plug-and-play)              | :heavy_check_mark:  | Set up your backend app to perform operations on plug and play devices.                                                                 |

## Provisioning client SDK

:heavy_check_mark: feature available  :heavy_multiplication_x: feature planned but not supported  :heavy_minus_sign: no support planned
This repository contains [provisioning device client SDK](./provisioning/provisioning-device-client) for the [Device Provisioning Service](https://docs.microsoft.com/en-us/azure/iot-dps/). 

| Features                    | mqtt               | mqtt-ws            | amqp               | amqp-ws            | https              | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|-----------------------------|--------------------|--------------------|--------------------|--------------------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| TPM Individual Enrollment   |  :heavy_minus_sign: |  :heavy_minus_sign: | :heavy_check_mark: | :heavy_multiplication_x: | :heavy_check_mark: | This SDK supports connecting your device to the Device Provisioning Service via [individual enrollment](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-service#enrollment) using [Trusted Platform Module](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-security#trusted-platform-module-tpm).   Please visit the [samples folder](./provisioning/provisioning-samples) and this [quickstart](https://docs.microsoft.com/en-us/azure/iot-dps/quick-create-simulated-device-tpm-java) on how to create a device client.  Websocket connection over AMQP is currently not available. TPM over MQTT is currently not supported by the Device Provisioning Service.                                                                                                                                                                                                              |
| X.509 Individual Enrollment | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | This SDK supports connecting your device to the Device Provisioning Service via [individual enrollment](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-service#enrollment) using [X.509 leaf certificate](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-security#leaf-certificate).   Please visit the [samples folder](./provisioning/provisioning-samples) and this [quickstart](https://docs.microsoft.com/en-us/azure/iot-dps/quick-create-simulated-device-x509-java) on how to create a device client. |
| X.509 Enrollment Group      | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | :heavy_check_mark: | This SDK supports connecting your device to the Device Provisioning Service via [enrollment group](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-service#enrollment) using [X.509 root certificate](https://docs.microsoft.com/en-us/azure/iot-dps/concepts-security#root-certificate).   Please visit the [samples folder](./provisioning/provisioning-samples) to learn more about this feature.                                                                                                                                                                                            |

## Provisioning service client SDK

This repository contains [provisioning service client SDK](./provisioning/provisioning-service-client) for the Device Provisioning Service to [programmatically enroll devices](https://docs.microsoft.com/en-us/azure/iot-dps/how-to-manage-enrollments-sdks).

| Feature                                            | Support            | Description                                                                                                                                                                                                                                            |
|----------------------------------------------------|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| CRUD Operation with TPM Individual Enrollment      | :heavy_check_mark: | Programmatically manage device enrollment using TPM with the service SDK.  Please visit the [samples folder](./provisioning/provisioning-samples) and this [quickstart](https://docs.microsoft.com/en-us/azure/iot-dps/quick-enroll-device-tpm-java) to learn more about this feature. |
| Bulk CRUD Operation with TPM Individual Enrollment | :heavy_check_mark: | Programmatically bulk manage device enrollment using TPM with the service SDK.  Please visit the [samples folder](./provisioning/provisioning-samples) to learn more about this feature. |
| CRUD Operation with X.509 Individual Enrollment    | :heavy_check_mark: | Programmatically manage device enrollment using X.509 individual enrollment with the service SDK.  Please visit the [samples folder](./provisioning/provisioning-samples) and this [quickstart](https://docs.microsoft.com/en-us/azure/iot-dps/quick-enroll-device-x509-java) to learn more about this feature. |
| CRUD Operation with X.509 Group Enrollment         | :heavy_check_mark: | Programmatically manage device enrollment using X.509 group enrollment with the service SDK.  Please visit the [samples folder](./provisioning/provisioning-samples) to learn more about this feature. |
| Query enrollments                                  | :heavy_check_mark: | Programmatically query registration states with the service SDK.  Please visit the [samples folder](./provisioning/provisioning-samples) to learn more about this feature.                                                                            |

## Samples

Within the repository, you can find various types of simple samples that can help you get started.

* [Device SDK Samples](./device/iot-device-samples)
* [Service SDK Samples](./service/iot-service-samples)
* [Provisioning SDK Samples](./provisioning/provisioning-samples)

## Logging

In order to learn more about logging within this SDK and how to capture its logs, see [here](./logging.md).

## Contribution, feedback and issues

If you encounter any bugs, have suggestions for new features or if you would like to become an active contributor to this project please follow the instructions provided in the [contribution guidelines](.github/CONTRIBUTING.md).

## Need support?

* Have a technical question? Ask on [Stack Overflow](https://stackoverflow.com/questions/tagged/azure-iot-hub) with tag “azure-iot-hub”
* Need Support? Every customer with an active Azure subscription has access to support with guaranteed response time.  Consider submitting a ticket and get assistance from Microsoft support team
* Found a bug? Please help us fix it by thoroughly documenting it and filing an issue on GitHub (C, Java, .NET, Node.js, Python).

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
* [Configure TLS Protocol Version and Ciphers](./configure_tls_protocol_version_and_ciphers.md)

## SDK folder structure

### /device

Contains Azure IoT Hub client components that provide the raw messaging capabilities of the library. Refer to the API documentation and samples for information on how to use it.

### /doc

This folder contains application development guides and device setup instructions.

### /iot-e2e-tests

This folder contains end to end tests source code for running on jvm and android.

### /vsts

This folder contains scripts to build and run Java SDK provided proper environmental variables are set, as well as azure pipeline resources used for our gating process

### /service

Contains libraries that enable interactions with the IoT Hub service to perform operations such as sending messages to devices and managing the device identity registry. Refer to API documentation and samples for more details.

## Certificates - Important to know

For guidance and important information about certificates, please refer to [this blog post](https://techcommunity.microsoft.com/t5/internet-of-things/azure-iot-tls-changes-are-coming-and-why-you-should-care/ba-p/1658456) from the security team.

## Long-term support

The project offers a Long-Term Support (LTS) releases to allow users that do not need the latest features to be shielded from unwanted changes.

Going forward, LTS repo tags are to be named lts_*yyyy*-*mm*-*dd*, where *yyyy*, *mm*, and *dd* are the year, month, and day when the tag was created. An example of such a tag is *lts_2021-03-18*.

The lifetime of an LTS release is 12 months. During this time, LTS releases may receive bug fixes that fall in these categories:

* security bug fixes
* critical bug fixes (e.g., unavoidable/unrecoverable crashes, significant memory leaks)

> No new features or improvements are in scope to be picked up in an LTS branch. A patch will not extend the maintenance or expiry date.

LTS releases may include additional extended support for security bug fixes as listed in the LTS schedule.

### Schedule

This table shows previous LTS releases and end dates.

|                                   Release Link                                    | GitHub Tag  | LTS Start Date | Maintenance End Date | LTS End Date |
|:---------------------------------------------------------------------------------:| :---------: |:--------------:|:--------------------:|:------------:|
| [2022-06-17](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-03-04) | 2022-03-04 |   2020-03-04   |      2021-09-31      |  2023-03-31  |
| [2021-06-17](https://github.com/Azure/azure-iot-sdk-java/releases/tag/lts_7_2021) | lts_06_2021 |   2020-06-17   |      2021-12-31      |  2022-06-30  |
| [2020-07-07](https://github.com/Azure/azure-iot-sdk-java/releases/tag/lts_7_2020) | lts_07_2020 |   2020-07-07   |      2020-12-31      |  2021-06-30  |

---
This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

Microsoft collects performance and usage information which may be used to provide and improve Microsoft products and services and enhance your experience.  To learn more, review the [privacy statement](https://go.microsoft.com/fwlink/?LinkId=521839&clcid=0x409). 

[iot-hub-documentation]: https://docs.microsoft.com/en-us/azure/iot-hub/
[azure-iot-sdks]: http://github.com/azure/azure-iot-sdks
[device-maven]: ./doc/java-devbox-setup.md#install-maven
[service-maven]: ./doc/java-devbox-setup.md#install-maven
[device-code]: ./doc/java-devbox-setup.md#build-azure-iot-device-and-service-sdks-for-java-from-the-source-code
[service-code]: ./doc/java-devbox-setup.md#build-azure-iot-device-and-service-sdks-for-java-from-the-source-code
[device-android]: ./doc/java-devbox-setup.md#building-for-android-device
[java-api-reference-device]: https://docs.microsoft.com/java/api/com.microsoft.azure.sdk.iot.device
[java-api-reference-service]: https://docs.microsoft.com/java/api/com.microsoft.azure.sdk.iot.service
[devbox-setup]: doc/java-devbox-setup.md
[java-api-reference]: https://azure.github.io/azure-iot-sdk-java/
[setup-iothub]: https://aka.ms/howtocreateazureiothub
[java-api-reference-device-dps]: https://docs.microsoft.com/java/api/com.microsoft.azure.sdk.iot.provisioning.device
[java-api-reference-service-dps]: https://docs.microsoft.com/java/api/com.microsoft.azure.sdk.iot.provisioning.service
