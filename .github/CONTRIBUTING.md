**The Azure IoT SDKs team wants to hear from you!**

- [Ask a question](#ask-a-question)
- [File a bug](#file-a-bug-code-or-documentation)
- [Contribute documentation](#contribute-documentation)
- [Contribute code](#contribute-code)
- [Need support?](#need-support)

# Need Support?
* Have a feature request for SDKs? Please post it on [User Voice](https://feedback.azure.com/forums/321918-azure-iot) to help us prioritize.
* Have a technical question? Ask on [Stack Overflow](https://stackoverflow.com/questions/tagged/azure-iot-hub) with tag “azure-iot-hub”
* Need Support? Every customer with an active Azure subscription has access to support with guaranteed response time.  Consider submitting a ticket and get assistance from Microsoft support team
* Found a bug? Please help us fix it by thoroughly documenting it and filing an issue on GitHub (C, Java, .NET, Node.js, Python).


# Ask a question
Our team monitors Stack Overflow, especially the [azure-iot-hub](http://stackoverflow.com/questions/tagged/azure-iot-hub) tag. It really is the best place to ask.

We monitor the Github issues section specifically for bugs found with our SDK, however we will reply to questions asked using Github issues too.

# File a bug (code or documentation)
That is definitely something we want to hear about. Please open an issue on github, we'll address it as fast as possible. Typically here's the information we're going to ask for to get started:

- What version of the SDK are you using?
- Do you have a snippet of code that would help us reproduce the bug?
- Do you have logs showing what's happening?
- And please share all repro steps for your issue.

Our SDK is entirely open-source and we do accept pull-requests if you feel like taking a stab at fixing the bug and maybe adding your name to our commit history :) Please mention
any relevant issue number in the pull request description. Please see [Contribute code](#contribute-code) code below.

# Contribute documentation
For simple markdown files, we accept documentation pull requests submitted against the `main` branch, if it's about existing SDK features.
If your PR is about future changes or has changes to the comments in the code itself, we'll treat is as a code change (see the next section).

# Contribute code
Pull-requests for code to be submitted against the `main` branch. We will review the request and once approved we will be running it in our gated build system. We try to maintain a high bar for code quality and maintainability, we insist on having tests associated with the code, and if necessary, additions/modifications to the requirement documents. Please refer to our [coding guidelines](https://github.com/Azure/azure-iot-sdk-java/blob/main/.github/CODING_GUIDELINES.md) before you begin contribution.

Also, have you signed the [Contribution License Agreement](https://cla.microsoft.com/) ([CLA](https://cla.microsoft.com/))? A friendly bot will remind you about it when you submit your pull-request.

**If your contribution is going to be a major effort, you should give us a heads-up first. We have a lot of items captured in our backlog and we release every two weeks, so before you spend the time, just check with us to make sure your plans and ours are in sync :) Just open an issue on github and tag it as "contribution".**

## Editing class requirements
We use requirement documents to describe the expected behavior for each class. It works as a basis to understand what tests need to be written.

Each requirement has a unique tag that is re-used in the code comments to identify where it's implemented and where it's tested.

Each unique tag is in the following form:
SRS_<MODULE_NAME>_<DEVELOPER_ID>_<REQUIREMENT_ID>

When contributing to requirement docs, you can use `99` as a DEVELOPER_ID, and just increment the requirement ID to be unique.

For an example see the template in the [Adding new files](#adding-new-files)

## Adding new files
If your contribution is not part of an already existed code, you must create a new requirement file and a new unit test. Our team created a template to help you on it. 

The template is located at [Template.java](https://github.com/Azure/azure-iot-sdk-java/tree/main/device/iot-device-client/src/main/java/com/microsoft/azure/sdk/iot/device/Template.java). For the requirements you can copy the [template_requirements.md](https://github.com/Azure/azure-iot-sdk-java/tree/main/device/iot-device-client/devdoc/requirement_docs/com/microsoft/azure/iothub/template_requirements.md) to the appropriate `devdoc` directory and change it to fit your needs.
For the unit test, the template is located at [TemplateTest.java](https://github.com/Azure/azure-iot-sdk-java/tree/main/device/iot-device-client/src/test/java/tests/unit/com/microsoft/azure/sdk/iot/device). This test template explains how the [template](https://github.com/Azure/azure-iot-sdk-java/tree/main/device/iot-device-client/src/main/java/com/microsoft/azure/sdk/iot/device/Template.java) is tested.

## Review Process
We expect all guidelines to be met before accepting a pull request. As such, we will work with you to address issues we find by leaving comments in your code. Please understand that it may take a few iterations before the code is accepted as we maintain high standards on code quality. Once we feel comfortable with a contribution, we will validate the change and accept the pull request.

Thank you for any contributions! Please let the team know if you have any questions or concerns about our contribution policy.
