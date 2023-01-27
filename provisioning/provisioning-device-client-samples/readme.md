# Samples for the Azure Provisioning Device Client SDK for Java

## Overview

This folder contains samples showing how to use the various features of the Microsoft Azure IoT Hub Device Provisioning 
Service running Java code.

## Prerequisites

In order to run the samples or the tutorials on Linux or Windows, you will first need the following prerequisites:
* [Setup your IoT hub][lnk-setup-iot-hub]
* [Setup your Device Provisioning Service][lnk-setup-provisioning-service]

## Setup environment

Prepare your platform following the instructions [here][lnk-devbox-setup] to install Java and Maven.

## List of samples

#### Provisioning Device Client

* [Provisioning Sample for X509](./provisioning-X509-sample): Shows how to register a device using X509 certificates.
* [Provisioning Sample for TPM](./provisioning-tpm-sample): Shows how to register a device using TPM with endorsement key.
* [Provisioning Sample for Symmetric Key Enrollment Group](./provisioning-symmetrickey-group-sample): Shows how to register a device in an enrollment group using Symmetric key authentication
* [Provisioning Sample for Symmetric Key Individual Enrollment](./provisioning-symmetrickey-group-sample): Shows how to register a device in an individual enrollment using Symmetric key authentication

[lnk-devbox-setup]: ../../doc/java-devbox-setup.md
[lnk-setup-iot-hub]: https://aka.ms/howtocreateazureiothub
[lnk-setup-provisioning-service]: https://docs.microsoft.com/en-us/azure/iot-dps/quick-setup-auto-provision
[lnk-manage-iot-hub]: https://aka.ms/manageiothub
