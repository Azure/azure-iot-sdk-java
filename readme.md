# Microsoft Azure IoT SDKs for Java
This repository contains the following:
* **Azure IoT Hub Device SDK**: to connect client devices to Azure IoT Hub (supports Java 7+ and Android API 17+)
* **Azure IoT Hub Service SDK**: enables developing back-end applications for Azure IoT (supports Java 7+)

To find SDKs in other languages for Azure IoT, please refer o the [azure-iot-sdks][azure-iot-sdks] repository

## Developing applications for Azure IoT
Visit [Azure IoT Dev Center](http://azure.com/iotdev) to learn more about developing applications for Azure IoT.

## How to use the Azure IoT SDKs for Java
Devices and data sources in an IoT solution can range from a simple network-connected sensor to a powerful, standalone computing device. Devices may have limited processing capability, memory, communication bandwidth, and communication protocol support. The IoT device SDKs enable you to implement client applications for a wide variety of devices.
The API reference documentation for the device SDK is [here][java-api-reference-device].
The API reference documentation for the service SDK is [here][java-api-reference-service].
* On Linux and Windows:
   * **Using Maven**: the simplest way to use the Azure IoT SDKs for Java to develop apps is to leverage Maven packages:
      * [Device SDK][device-maven]
      * [Service SDK][service-maven]
   * **Working with the SDKs code**: if you are working with the SDKs code to modify it or contribute changes, then you can clone the repository and build the libraries:
      * [Build Device SDK from code][device-code]
      * [Build Service SDK from code][service-code]
* On Android: our Java device SDK can be used on Android using the API version 17 and higher:
   * [Device SDK][device-android]

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