# IoT hub and Device Provisioning Service Certificate Migration

All Azure IoT SDK users are advised to be aware of upcoming certificate changes for IoT hub and Device Provisioning Service 
that will impact the SDK's ability to connect. In October 2022, both services will migrate away from the current 
[Baltimore CyberTrust CA Root](https://baltimore-cybertrust-root.chain-demos.digicert.com/info/index.html) to the 
[DigiCert Global G2 CA root](https://global-root-g2.chain-demos.digicert.com/info/index.html). There will be a 
transition period beforehand where your IoT devices are expected to have both the Baltimore and Digicert public 
certificates installed on your device in order to prevent connectivity issues. 

For a more in depth explanation as to why the service is doing this, please see
[this article](https://techcommunity.microsoft.com/t5/internet-of-things/azure-iot-tls-critical-changes-are-almost-here-and-why-you/ba-p/2393169).

## Java SDK Migration Steps

Because this SDK used to read its trusted certificates from source code rather than reading them from
your physical device's certificate store, simply installing the DigiCert Global G2 CA root to your device's certificate 
store isn't sufficient to handle this transition. There are two ways to ensure that your device using this SDK can 
continue to connect to the service once this transition starts:

- Upgrade your SDK package versions to be, at minimum, those defined in [this release](https://github.com/Azure/azure-iot-sdk-java/releases/tag/lts_7_2021)
  - The packages in this release and all later releases will have both the Baltimore and the DigiCert CA roots saved in 
  the SDK, so they can connect now and will still be able to connect once this transition starts.
  - Note that this SDK version will only function correctly while the IoT services use either the Baltimore or DigiCert CA root certificates. If the IoT service migrates to new root certificates again in the future, then this SDK version change is **not** sufficient.
- Upgrade your SDK package versions to at least the 2.0.0 packages that are found [here](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-3-30) 
once they include a long term support release.
  - The packages in this planned release will no longer store any certificates in source code, and instead will read the 
  trusted certificates from your device's certificate store. With this change, the SDK will become decoupled from the service's 
  root certificates. Because of that, users who use this approach will need to install both the Baltimore public certificate 
  and the DigiCert public certificate on their device so that the SDK will continue to trust the service as expected. 
  
  
This team will continue to support the 1.X.X [Long Term Support releases](https://github.com/Azure/azure-iot-sdk-java#long-term-support)
as we have in the past with important bug fixes, but newer features will only be added to the new 2.X.X versions, 
so users are strongly recommended to upgrade to the 2.X.X releases once they have a published long term support release.

**Users who don't upgrade to the packages in [this release](https://github.com/Azure/azure-iot-sdk-java/releases/tag/lts_7_2021)
or to the 2.X.X packages will begin experiencing unrecoverable, consistent connection failures from their devices starting June 2022.**

If you need any help migrating your code to try out the new 2.X.X clients, please see this [migration guide](https://github.com/Azure/azure-iot-sdk-java/blob/preview/major%20version%20upgrade%20migration%20guide.md).

If you have any questions, comments, or concerns about this upcoming change, please let us know on our [discussions page](https://github.com/Azure/azure-iot-sdk-java/discussions).
