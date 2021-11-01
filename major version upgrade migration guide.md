# Migration guide

This document outlines the changes made from this library's 1.X.X releases to its 2.X.X releases. Since this is
a major version upgrade, there are a number of breaking changes that will affect your ability to compile. This
document will be split into a section per package for simplicity.

## IoT hub Device Client

Breaking changes:
- Trust certificates are read from the physical device's trusted root certification authorities certificate store rather than from source.
  - Users are expected to install the required public certificates into this certificate store if they are not present already.
  - See [this document](./upcoming_certificate_changes_readme.md) for additional context on which certificates need to be installed.
- DeviceClient and ModuleClient constructors that take public certificates and private keys as strings have been removed.
  - Users must provide an instance of SSLContext that has their public certificates and private keys loaded into it instead.
  - See [this sample](./device/iot-device-samples/send-event-x509) for the recommended way to create this SSLContext and how to construct your DeviceClient and/or ModuleClient with it.
- deviceClient.uploadToBlobAsync() has been removed.
  - Users can still use deviceClient.getFileUploadSasUri() to get a SAS URI that can be used with the Azure Storage SDK to upload the file.
  - See [this sample](./device/iot-device-samples/file-upload-sample) for the recommended way to upload files.
- DeviceClient and ModuleClient APIs have been unified for consistent naming.
- DeviceClient and ModuleClient APIs that behave asynchronously have been renamed to include an "Async" suffix. The API's behavior was not changed, however.
  - This was done because several APIs such as startDeviceTwin were being used by users as though they were synchronous methods when they were not.
- All other deprecated APIs have also been removed.
- The Bouncycastle and Azure Storage SDK dependencies have been removed.
- The "com.microsoft.azure.sdk.iot.device.DeviceTwin" namespace has been changed to "com.microsoft.azure.sdk.iot.device.twin" to remove uppercase letters from it.
- DeviceClient and ModuleClient deprecated closeNow() and moved it's functionality into close() to match other language SDKs.
- Renamed some twin desired property callback method names to make them match a more standard callback method naming convention.
- Reduced access levels to classes and methods that were never intended to be public where possible.
- Device/ModuleClient method "registerConnectionStatusChangeCallback" has been renamed to "setConnectionStatusChangeCallback".
- Several callback interfaces and methods have been renamed to fix the spelling of callback from "CallBack" to "Callback".


## IoT hub Service Client
 
Breaking changes:
- Trust certificates are read from the physical device's trusted root certification authorities certificate store rather than from source.
  - Users are expected to install the required public certificates into this certificate store if they are not present already
  - See [this document](./upcoming_certificate_changes_readme.md) for additional context on which certificates need to be installed.
- All deprecated APIs have been removed
  - Each deprecated API in the 1.X.X versions describes which API you should use instead.
- The Bouncycastle dependencies have been removed
  - The Bouncycastle dependencies were used for some certificate parsing logic that has been removed from the SDK.
- Reduced access levels to classes and methods that were never intended to be public where possible 
- Removed service error code descriptions that the service would never return the error code for
- Reduce default SAS token time to live from 1 year to 1 hour for security purposes
- Removed unnecessary synchronization on service client APIs to allow for a single client to make multiple service APIs simultaneously
- Removed asynchronous APIs for service client APIs 
  - These were wrappers on top of the existing sync APIs. Users are expected to write async wrappers that better fit their preferred async framework.
- Removed asynchronous APIs for service client APIs 
- Removed ```open()``` and ```close()``` APIs for registryManager since they do nothing anymore
 
## Device Provisioning Service Device Client

No notable changes, but the security providers that are used in conjunction with this client have changed. See [this section](#security-provider-clients) for more details.

## Device Provisioning Service Service Client

Breaking changes:
- Trust certificates are read from the physical device's trusted root certification authorities certificate store rather than from source.
  - Users are expected to install the required public certificates into this certificate store if they are not present already
  - See [this document](./upcoming_certificate_changes_readme.md) for additional context on which certificates need to be installed.
- All deprecated APIs have been removed
  - Each deprecated API in the 1.X.X versions describes which API you should use instead.
- Reduced access levels to classes and methods that were never intended to be public where possible
- Reduce default SAS token time to live from 1 year to 1 hour for security purposes

## Security Provider Clients

Breaking changes:
- Trust certificates are read from the physical device's trusted root certification authorities certificate store rather than from source.
  - Users are expected to install the required public certificates into this certificate store if they are not present already
  - See [this document](./upcoming_certificate_changes_readme.md) for additional context on which certificates need to be installed.
  - Users of the X509 SecurityProvider are expected to pass in the parsed certificates and keys as Java security primitives rather than as strings
    - See [this sample](./provisioning/provisioning-samples/provisioning-X509-sample) for a demonstration on how to create these Java security primitives from strings
  
## IoT Deps package changes
Breaking changes:
- The IoT Deps package has been removed, and all of its classes have been moved to the packages that had a dependency on them.
  - For example, the ```FileUploadSasUriRequest``` class that previously lived in package ```com.microsoft.azure.sdk.iot.deps.serializer``` now lives in package ```com.microsoft.azure.sdk.iot.device.serializer```

## Frequently Asked Questions

Question:
> What do I gain by upgrading to the 2.X.X release?

Answer:
> You get a smaller set of dependencies which makes for a lighter SDK overall. You also get a more concise and clear API surface
> since all deprecated APIs have been removed. Lastly, and most importantly, you get an SDK that is decoupled from a particular
> IoT hub and Device Provisioning Service root certificate. This makes these versions more future proof since they aren't tied to a root certificate
> that will be changed within a few years and may be changed again beyond then. 

Question:
> Will the 1.X.X releases still be supported in any way?

Answer:
> We will continue to support the long term support releases of the 1.X.X SDK for their lifespans, but we will not bring 
> newer features to the 1.X.X SDK. Users who want access to the upcoming features are encouraged to upgrade to the 2.X.X SDK.

Question:
> After upgrading, when I try to open my client, I get an exception like:
> ```
> javax.net.ssl.SSLHandshakeException: 
>     sun.security.validator.ValidatorException: PKIX path building failed: 
>     sun.security.provider.certpath.SunCertPathBuilderException: 
>     unable to find valid certification path to requested target
>         
>  Caused by: sun.security.validator.ValidatorException: 
>     PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: 
>     unable to find valid certification path to requested target
>        
>  Caused by: sun.security.provider.certpath.SunCertPathBuilderException: 
>     unable to find valid certification path to requested target
> ```

Answer:
> You will need to install the public certificate for IoT hub to your device's trusted root certification authorities certificate store.
>
> For a link to the necessary public certificates and more details on a critical upcoming change related to these certificates, see [this document](./upcoming_certificate_changes_readme.md).

Question:
> If the SDK now reads certificates from my device's trusted root certification authorities certificate store, does it also read any private keys that I have installed on my device?

Answer:
> No. This SDK only reads the public certificates from your device's trusted root certification authorities certificate store. 
>
> For x509 authenticated connections, you will need to construct the SSLContext with your trusted certificates and private keys and provide it to the SDK.
> See [this sample](./device/iot-device-samples/send-event-x509) for an example of how to construct an SSLContext instance with your public certificates and private keys.

Question:
> Can I still upload files to Azure Storage using this SDK now that deviceClient.uploadToBlobAsync() has been removed?

Answer:
> Yes, you will still be able to upload files to Azure Storage after upgrading. 
>
> This SDK allows you to get the necessary credentials to upload your files to Azure Storage, but you will need to bring in the Azure Storage SDK as a dependency to do the actual file upload step. 
> 
> For an example of how to do file upload after upgrading, see [this sample](./device/iot-device-samples/file-upload-sample).

Question:
> I was using a deprecated API that was removed in the 2.X.X upgrade, what should I do?

Answer:
> The deprecated API in the 1.X.X version documents which API you should use instead of the deprecated API.

Question:
> After upgrading, some of my catch statements no longer work because the API I was using no longer declares that it throws that exception. Do I still need to catch something there?

Answer:
> In this upgrade, we removed any thrown exceptions from our APIs if the API never threw that exception. Because of that, you are safe to simply remove
> the catch clause in cases like this.

Question:
> What if I don't want this SDK to read from my device's trusted root certification authorities certificate store? Is there a way to override this behavior?

Answer:
> Yes, there is a way to override this behavior. For a given client, there is an optional parameter that allows you to provide
> the SSLContext to the client rather than allow the client to build the SSLContext for you from the trusted root certification 
> authorities certificate store. In this SSLContext, you have complete control over what certificates to trust. 
>
> For an example of injecting your own SSLContext, see [this sample](./device/iot-device-samples/send-event-x509).

Question:
> Does this major version bump bring any changes to what platforms this SDK supports?

Answer:
> No. If you are using a platform that is supported in our 1.X.X releases, then your platform will still be supported in our 2.X.X releases.

Question:
> I was using the TransportClient class to do multiplexing in the IoT hub device SDK and it was removed. Does this library no longer support multiplexing?

Answer:
> Yes, this library still supports multiplexing. The MultiplexingClient has replaced the TransportClient here. See [this sample](./device/iot-device-samples/multiplexing-sample) for reference.