---
name: Bug report
about: Create a report to help us improve
title: '[Bug Report]'
labels: bug
assignees: ''

---

------------------------------- delete below -------------------------------

INSTRUCTIONS
==========

Please follow the instructions and template below to save us time requesting additional information from you. For more information on all of your support options, please see [Azure IoT support and help options](https://aka.ms/IoTHelp).

*If this is an issue encountered by following a Microsoft document, please open an issue on that page itself. This ensures the fastest targeted response, and you can do this by scrolling all the way down to the bottom of the page to the **Feedback** section to report the issue.*

1. Search existing issues to avoid creating duplicates.

2. If possible test using the [latest release](https://github.com/Azure/azure-iot-sdk-java/releases) to make sure your issue has not already been fixed.

3. Do not share information from your Azure subscription here (connection strings, service names (IoT Hub name, Device Provisioning Service scope ID), etc...). If you need to share any of this information, you can create a ticket and [get assistance from Microsoft Support](https://docs.microsoft.com/en-us/azure/azure-supportability/how-to-create-azure-support-request).

4. Include enough information for us to address the bug:
   -  A detailed description.
   -  A [Minimal Complete Reproducible Example](https://stackoverflow.com/help/mcve). This is code we can cut and paste into a readily available sample and run, or a link to a project you've written that we can compile to reproduce the bug. 
   -  Console logs (https://github.com/Azure/azure-iot-sdk-java/blob/main/logging.md).

5. Delete these instructions before submitting the bug.

Below is a generic bug report format. We recommend you use it as a template and replace the information below each header with your own. 

Note that bugs that only affect unsupported platforms will likely be treated as feature requests, and may be closed as "won't fix" if we have no plans to support that platform. See [this document](../../supported_platforms.md) for details on which platforms are officially supported. 

Note that the IoT hub service client SDK has been moved to [this repo](https://www.github.com/Azure/azure-iot-service-sdk-java) so if you want to file a bug on that package, please file it [here](https://www.github.com/Azure/azure-iot-service-sdk-java/issues) instead.

------------------------------- delete above -------------------------------


# Context

- **OS and version used:** <VERSION> (Windows 10, Ubuntu 18.04, etc. )
- **Java runtime used:** <VERSION> (in a command prompt: java -version )
- **SDK version used:** <VERSION> (Please include the version of the SDK used)

## Description of the issue
Please be as detailed as possible: which feature has a problem, how often does it fail, etc.

## Code sample exhibiting the issue
Please remove any connection string information!

## Console log of the issue
Consider setting the DEBUG environment variable to '*'. This will produce a much more verbose output that will help debugging
Don't forget to remove any connection string information!
