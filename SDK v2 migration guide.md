# SDK v2 Migration guide

This document outlines the changes made from this library's 1.X.X releases to its 2.X.X releases. Since this is
a major version upgrade, there are a number of breaking changes that will affect your ability to compile. Provided here
are outlines of the notable breaking changes as well as a mapping from v1 APIs to v2 APIs to aid you in migrating.

## Table Of Contents

 - [Why the v1 SDK is being replaced](#Why-the-v1-sdk-is-being-replaced)
 - [What will happen to the v1 SDK](#What-will-happen-to-the-v1-sdk)
 - [Migration Guide](#migration-guide)
   - [IoT Hub Device Client](#iot-hub-device-client)
   - [IoT Hub Service Client](#iot-hub-service-client)
   - [DPS Device Client](#dps-device-client)
   - [DPS Service Client](#dps-service-client)
   - [Security Provider Clients](#security-provider-clients)
   - [Deps](#deps)
 - [Frequently asked questions](#frequently-asked-questions)

## Why the v1 SDK is being replaced

There are a number of reasons why the Azure IoT SDK team chose to do a major version revision. Here are a few of the more important reasons:
  - [Upcoming certificate changes](./upcoming_certificate_changes_readme.md) dictated that the SDK needed to stop pinning on a specific IoT Hub public certificate and start reading certificates from the device certificate store.
  - Several 3rd party dependencies (Bouncycastle, JNR Unixsocket, Azure Storage SDK) were becoming harder to carry due to security concerns and they could only be removed by removing or alterring existing APIs.
  - Many existing client classes (RegistryManager, DeviceTwin, DeviceMethod, ServiceClient, etc.) were confusingly named and contained methods that weren't always consistent with the client's assumed responsibilities.
  - Many existing clients had a mix of standard constructors (```new DeviceClient(...)```) and static builder constructors (```DeviceClient.createFromSecurityProvider(...)```) that caused some confusion among users.
  - ```DeviceClient``` and ```ModuleClient``` had unneccessarily different method names for the same operations (```deviceClient.startDeviceTwin(...)``` vs ```moduleClient.startTwin(...)```) that could be easily unified for consistency.
  - ```DeviceClient``` and ```ModuleClient``` had many asynchronous methods whose naming did not reflect that they were asynchronous. This led to some users calling these methods as though they were synchronous.

## What will happen to the v1 SDK

We have released [one final LTS version](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-03-04) of the v1 SDK that
we will support like any other LTS release (security bug fixes, some non-security bug fixes as needed), but users are still encouraged
to migrate to v2 when they have the chance. For more details on LTS releases, see [this document](./readme.md#long-term-support).

## Migration Guide

### IoT Hub Device Client

#### DeviceClient

| V1 class#method | Changed? | Equivalent V2 class#method |
|:---|:---|:---|
| DeviceClient#createFromSecurityProvider(String, String, SecurityProvider, IotHubClientProtocol); | yes | new DeviceClient(String, String, SecurityProvider, IotHubClientProtocol);  |
| DeviceClient#createFromSecurityProvider(String, String, SecurityProvider, IotHubClientProtocol, ClientOptions); | yes | new DeviceClient(String, String, SecurityProvider, IotHubClientProtocol, ClientOptions);  |
| DeviceClient#setMessageCallback(MessageCallback, Object); | yes | DeviceClient#setMessageCallback(MessageCallback, Object);  |
| DeviceClient#open(); | yes | DeviceClient#open(boolean);  |
| DeviceClient#open(boolean); | no | DeviceClient#open(boolean);  |
| DeviceClient#closeNow(); | yes | DeviceClient#close();  |
| DeviceClient#close(); | no | DeviceClient#close();  |
| DeviceClient#getFileUploadSasUri(FileUploadSasUriRequest); | no | DeviceClient#getFileUploadSasUri(FileUploadSasUriRequest);  |
| DeviceClient#completeFileUpload(FileUploadCompletionNotification); | no | DeviceClient#completeFileUpload(FileUploadCompletionNotification);  |
| DeviceClient#uploadToBlobAsync(String, InputStream, long, IotHubEventCallback, Object); | yes | no equivalent method**  |
| DeviceClient#isMultiplexed(); | no | DeviceClient#isMultiplexed();  |
| DeviceClient#sendEventAsync(Message, IotHubEventCallback, Object); | no | DeviceClient#sendEventAsync(Message, IotHubEventCallback, Object);  |
| DeviceClient#registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback, Object); | yes | DeviceClient#setConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback, Object);  |
| DeviceClient#setRetryPolicy(RetryPolicy); | no | DeviceClient#setRetryPolicy(RetryPolicy);  |
| DeviceClient#getProductInfo(); | yes | DeviceClient#getProductInfo();  |
| DeviceClient#subscribeToDeviceMethod(DeviceMethodCallback , Object, IotHubEventCallback, Object); | yes | DeviceClient#subscribeToMethodsAsync(MethodCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| DeviceClient#startDeviceTwin(IotHubEventCallback, Object, TwinPropertyCallback, Object); | yes | DeviceClient#subscribeToDesiredPropertiesAsync(DesiredPropertiesCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| DeviceClient#startDeviceTwin(IotHubEventCallback, Object, TwinPropertiesCallback, Object); | yes | DeviceClient#subscribeToDesiredPropertiesAsync(DesiredPropertiesCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| DeviceClient#startDeviceTwin(IotHubEventCallback, Object, PropertyCallBack<Type1, Type2>, Object); | yes | DeviceClient#subscribeToDesiredPropertiesAsync(DesiredPropertiesCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| DeviceClient#getDeviceTwin(); | yes | DeviceClient#getTwinAsync();  |
| DeviceClient#subscribeToDesiredProperties(Map<Property, Pair<PropertyCallBack<String, Object>, Object>>); | yes | DeviceClient#subscribeToDesiredPropertiesAsync(DesiredPropertiesCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| DeviceClient#subscribeToTwinDesiredProperties(Map<Property, Pair<TwinPropertyCallBack, Object>>(); | yes | DeviceClient#subscribeToDesiredPropertiesAsync(DesiredPropertiesCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| DeviceClient#sendReportedProperties(Set<Property>); | yes | DeviceClient#updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesCallback, Object);  |
| DeviceClient#sendReportedProperties(Set<Property>, int); | yes | DeviceClient#updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesCallback, Object);  |
| DeviceClient#sendReportedProperties(ReportedPropertiesParameters); | yes | DeviceClient#updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesUpdateCorrelatingMessageCallback, Object);  |
| DeviceClient#sendReportedProperties(Set<Property>, Integer, CorrelatingMessageCallback, Object, IotHubEventCallback, Object); | yes | DeviceClient#updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesUpdateCorrelatingMessageCallback, Object);  |
| DeviceClient#setOption(String, Object); | yes | no equivalent method***  |
| DeviceClient#setProxySettings(ProxySettings); | yes | no equivalent method****  |

** This method has been split into the three individual steps that this method used to take. See [this file upload sample](./device/iot-device-samples/file-upload-sample) for an example of how to do file upload using these discrete steps.

*** The options that were previously set in this method are now set at DeviceClient constructor time in the optional ClientOptions parameter.

**** Proxy settings are now set at DeviceClient constructor time in the optional ClientOptions parameter,

#### ModuleClient

| V1 class#method | Changed? | Equivalent V2 class#method |
|:---|:---|:---|
| ModuleClient#createFromEnvironment(); | yes | ModuleClient#createFromEnvironment(UnixDomainSocketChannel);**  |
| ModuleClient#sendEventAsync(Message, IotHubEventCallback, Object); | no | ModuleClient#sendEventAsync(Message, IotHubEventCallback, Object);  |
| ModuleClient#sendEventAsync(Message, IotHubEventCallback, Object, String); | no | ModuleClient#sendEventAsync(Message, IotHubEventCallback, Object, String);  |
| ModuleClient#setMessageCallback(MessageCallback, Object); | no | ModuleClient#setMessageCallback(MessageCallback, Object);  |
| ModuleClient#setMessageCallback(String, MessageCallback, Object); | no | ModuleClient#setMessageCallback(String, MessageCallback, Object);  |
| ModuleClient#invokeMethod(String, MethodRequest); | no | ModuleClient#invokeMethod(String, MethodRequest);  |
| ModuleClient#invokeMethod(String, String, MethodRequest); | no | ModuleClient#invokeMethod(String, String, MethodRequest);  |
| ModuleClient#open(); | yes | ModuleClient#open(boolean);  |
| ModuleClient#open(boolean); | no | ModuleClient#open(boolean);  |
| ModuleClient#closeNow(); | yes | ModuleClient#close();  |
| ModuleClient#close(); | no | ModuleClient#close();  |
| ModuleClient#registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback, Object); | yes | ModuleClient#setConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback, Object);  |
| ModuleClient#setRetryPolicy(RetryPolicy); | no | ModuleClient#setRetryPolicy(RetryPolicy);  |
| ModuleClient#setOperationTimeout(long); | no | ModuleClient#setOperationTimeout(long);  |
| ModuleClient#getProductInfo(); | no | ModuleClient#getProductInfo();  |
| ModuleClient#subscribeToMethod(DeviceMethodCallback , Object, IotHubEventCallback, Object); | yes | ModuleClient#subscribeToMethodsAsync(MethodCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| ModuleClient#startTwin(IotHubEventCallback, Object, TwinPropertyCallback, Object); | yes | ModuleClient#subscribeToDesiredPropertiesAsync(DesiredPropertiesCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| ModuleClient#startTwin(IotHubEventCallback, Object, TwinPropertiesCallback, Object); | yes | ModuleClient#subscribeToDesiredPropertiesAsync(DesiredPropertiesCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| ModuleClient#startTwin(IotHubEventCallback, Object, PropertyCallBack<Type1, Type2>, Object); | yes | ModuleClient#subscribeToDesiredPropertiesAsync(DesiredPropertiesCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| ModuleClient#subscribeToDesiredProperties(Map<Property, Pair<PropertyCallBack<String, Object>, Object>>); | yes | ModuleClient#subscribeToDesiredPropertiesAsync(DesiredPropertiesCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| ModuleClient#subscribeToTwinDesiredProperties(Map<Property, Pair<TwinPropertyCallBack, Object>>(); | yes | ModuleClient#subscribeToDesiredPropertiesAsync(DesiredPropertiesCallback, Object, SubscriptionAcknowledgedCallback, Object);  |
| ModuleClient#sendReportedProperties(Set<Property>); | yes | ModuleClient#updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesCallback, Object);  |
| ModuleClient#sendReportedProperties(Set<Property>, int); | yes | ModuleClient#updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesCallback, Object);  |
| ModuleClient#sendReportedProperties(ReportedPropertiesParameters); | yes | ModuleClient#updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesUpdateCorrelatingMessageCallback, Object);  |
| ModuleClient#sendReportedProperties(Set<Property>, Integer, CorrelatingMessageCallback, Object, IotHubEventCallback, Object); | yes | ModuleClient#updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesUpdateCorrelatingMessageCallback, Object);  |
| ModuleClient#setOption(String, Object); | yes | no equivalent method***  |
| ModuleClient#setProxySettings(ProxySettings); | yes | no equivalent method****  |

** ```ModuleClient.createFromEnvironment(...)``` APIs now require a unix domain socket implementation to be provided, 
rather than the SDK providing one. A sample [here](./device/iot-device-samples/unix-domain-socket-sample) demonstrates 
one such implementation that can be provided. This change was made so that users can control how the SDK communicates over
unix domain sockets if they want, and so that users who don't use modules aren't given a dependency on a unix domain 
socket communication library that their application would never need.

*** The options that were previously set in this method are now set at ModuleClient constructor time in the optional ClientOptions parameter.

**** Proxy settings are now set at ModuleClient constructor time in the optional ClientOptions parameter,

#### MultiplexingClient

No API surface changes have been made to the MultiplexingClient class

#### TransportClient

This client has been removed in v2. It is replaced by the MultiplexingClient. See [this sample](./device/iot-device-samples/multiplexing-sample) for more information.

#### Other notable breaking changes

- Trust certificates are read from the physical device's trusted root certification authorities certificate store rather than from source.
  - Users are expected to install the required public certificates into this certificate store if they are not present already.
  - See [this document](./upcoming_certificate_changes_readme.md) for additional context on which certificates need to be installed.
  - For most users, no action is needed here since IoT Hub uses the [DigiCert Global G2 CA root](https://global-root-g2.chain-demos.digicert.com/info/index.html) certificate which is already installed on most devices.
- DeviceClient and ModuleClient constructors that take public certificates and private keys as strings have been removed.
  - Users must provide an instance of SSLContext that has their public certificates and private keys loaded into it instead.
  - See [this sample](./device/iot-device-samples/send-event-x509) for the recommended way to create this SSLContext and how to construct your DeviceClient and/or ModuleClient with it.
- deviceClient.uploadToBlobAsync() has been removed.
  - Users can still use deviceClient.getFileUploadSasUri() to get a SAS URI that can be used with the Azure Storage SDK to upload the file.
  - See [this sample](./device/iot-device-samples/file-upload-sample) for the recommended way to upload files.
- The Bouncycastle, JNR Unixsocket, and Azure Storage SDK dependencies have been removed.
  - Bouncycastle dependencies were removed as they were large and only used for parsing certificates. Now that users are expected to parse certificates, it was safe to remove these dependencies.
  - JNR unixsocket dependency was removed because it was only used for one specific use case when constructing module clients, and because other unix domain socket libraries could be used instead depending on the user's preference.
  - Azure Storage SDK dependency was removed because it made more sense for the user to pick which version of the Azure Storage SDK works best for their application rather than forcing a particular version onto them.
- Reduced access levels to classes and methods that were never intended to be public where possible.


### IoT hub Service Client
 
| V1 class  | Equivalent V2 Class(es)|
|:---|:---|
| RegistryManager   | RegistryClient, ConfigurationsClient  |
| DeviceTwin   | TwinClient  |
| DeviceMethod |  DirectMethodsClient | 
| ServiceClient   | MessagingClient, FileUploadNotificationProcessorClient, MessageFeedbackProcessorClient   |   
| JobClient  |  BlockBlobClient, RegistryClient  |  

For v1 classes with more than one equivalent v2 classes, the methods that were in the v1 class have been split up to 
create clients with more cohesive capabilities. For instance, configurations CRUD was in the v1 RegistryManager, but has 
been moved to a new ConfigurationsClient in v2.


#### RegistryManager

| V1 class#method | Changed? | Equivalent V2 class#method |
|:---|:---|:---|
| RegistryManager#open(); | yes | no equivalent method**  |
| RegistryManager#close(); | yes | no equivalent method**  |
| RegistryManager#addDevice(Device); | yes | RegistryClient#addDevice(Device);  |
| RegistryManager#addDeviceAsync(Device); | yes | no equivalent method***  |
| RegistryManager#getDevice(String); | yes | RegistryClient#getDevice(String);  |
| RegistryManager#getDeviceAsync(String); | yes | no equivalent method***  |
| RegistryManager#updateDevice(Device); | yes | RegistryClient#updateDevice(Device);  |
| RegistryManager#updateDeviceAsync(Device); | yes | no equivalent method*** |
| RegistryManager#removeDevice(String); | yes | RegistryClient#removeDevice(String);  |
| RegistryManager#removeDeviceAsync(String); | yes | no equivalent method*** |
| RegistryManager#getDevices(Integer); | yes | no equivalent method**** |
| RegistryManager#getDevicesAsync(Integer); | yes | no equivalent method**** |
| RegistryManager#getDeviceConnectionString(); | yes | no equivalent method. Removed since this was not a service call.  |
| RegistryManager#getStatistics(); | yes | RegistryClient#getStatistics();  |
| RegistryManager#getStatisticsAsync(); | yes | no equivalent method*** |
| RegistryManager#exportDevices(String, Boolean); | yes | RegistryClient#exportDevices(String, boolean);  |
| RegistryManager#exportDevicesAsync(String, Boolean); | yes | no equivalent method*** |
| RegistryManager#exportDevices(JobProperties); | yes | RegistryClient#exportDevices(RegistryJob);  |
| RegistryManager#exportDevicesAsync(JobProperties); | yes | no equivalent method*** |
| RegistryManager#importDevices(String, String); | yes | RegistryClient#importDevices(String, String);  |
| RegistryManager#importDevicesAsync(String, String); | yes | no equivalent method*** |
| RegistryManager#importDevices(JobProperties); | yes | RegistryClient#importDevices(JobProperties);  |
| RegistryManager#importDevicesAsync(JobProperties); | yes | no equivalent method*** |
| RegistryManager#getJob(String); | yes | RegistryClient#getJob(String);  |
| RegistryManager#addModule(Module); | yes | RegistryClient#addModule(Module);  |
| RegistryManager#getModule(String); | yes | RegistryClient#getModule(String);  |
| RegistryManager#getModulesOnDevice(String); | yes | RegistryClient#getModulesOnDevice(String);  |
| RegistryManager#updateModule(Module); | yes | RegistryClient#updateModule(Module);  |
| RegistryManager#updateModule(Module, Boolean); | yes | RegistryClient#updateModule(Module);  |
| RegistryManager#removeModule(String, String); | yes | RegistryClient#removeModule(String, String);  |
| RegistryManager#addConfiguration(Configuration); | yes | ConfigurationsClient#create(Configuration);  |
| RegistryManager#getConfiguration(String); | yes | ConfigurationsClient#get(String);  |
| RegistryManager#getConfigurations(int); | yes | ConfigurationsClient#getConfigurations(int);  |
| RegistryManager#updateConfiguration(Configuration); | yes | ConfigurationsClient#replace(Configuration);  |
| RegistryManager#updateConfiguration(Configuration, Boolean); | yes |  ConfigurationsClient#updateConfiguration(Configuration); |
| RegistryManager#removeConfiguration(String); | yes | ConfigurationsClient#delete(String);  |
| RegistryManager#applyConfigurationContentOnDevice(String, ConfigurationContent); | yes | ConfigurationsClient#applyConfigurationContentOnDevice(String, ConfigurationContent);  |

** This method did nothing so it didn't need a v2 equivalent

*** This method was a wrapper on the synchronous version of the method. Users can write their own wrapper instead

**** Iot Hub does not have a useable "list devices" operation


#### DeviceTwin

| V1 class#method | Changed? | Equivalent V2 class#method |
|:---|:---|:---|
| DeviceTwin#getTwin(DeviceTwinDevice); | yes | TwinClient#get(String), TwinClient#get(String, String);  |
| DeviceTwin#updateTwin(DeviceTwinDevice); | yes | TwinClient#patch(Twin);  |
| DeviceTwin#replaceTwin(DeviceTwinDevice); | yes | TwinClient#replace(Twin);  |
| DeviceTwin#queryTwin(String); | yes | TwinClient#query(String), QueryClient.queryTwins(String)  |
| DeviceTwin#scheduleUpdateTwin(String, DeviceTwinDevice, Date, long); | yes | ScheduledJobsClient#scheduleUpdateTwin(String, String, Twin, Date, long);  |


#### DeviceMethod

| V1 class#method | Changed? | Equivalent V2 class#method |
|:---|:---|:---|
| DeviceMethod#invoke(String, String, Long, Long, Object); | yes | DirectMethodsClient#invoke(String, String, DirectMethodRequestOptions);  |
| DeviceMethod#invoke(String, String, String, Long, Long, Object); | yes | DirectMethodsClient#invoke(String, String, String, DirectMethodRequestOptions);  |
| DeviceMethod#scheduleDeviceMethod(String, String, Long, Long, Object, Date, long); | yes | ScheduledJobsClient#scheduleDirectMethod(String, String, String, Date, DirectMethodsJobOptions);  |


#### JobClient

| V1 class#method | Changed? | Equivalent V2 class#method |
|:---|:---|:---|
| JobClient#scheduleUpdateTwin(String, String, DeviceTwinDevice, Date, long); | yes | ScheduledJobsClient#scheduleUpdateTwin(String, String, Twin, Date, long);  |
| JobClient#scheduleDeviceMethod(String, String, Long, Long, Object, Date, long); | yes | ScheduledJobsClient#scheduleDirectMethod(String, String, String, Date, DirectMethodsJobOptions);  |
| JobClient#getJob(String); | yes | ScheduledJobsClient#getJob(String);  |
| JobClient#cancelJob(String); | yes | ScheduledJobsClient#cancelJob(String);  |
| JobClient#queryDeviceJob(String); | yes | ScheduledJobsClient#query(String), QueryClient#queryJobs(String);  |


#### ServiceClient

| V1 class#method | Changed? | Equivalent V2 class#method |
|:---|:---|:---|
| ServiceClient#open(); | yes | MessagingClient#open();  |
| ServiceClient#close(); | yes | MessagingClient#close();  |
| ServiceClient#send(String, Message); | yes | MessagingClient#send(String, Message);  |
| ServiceClient#send(String, String, Message); | yes | MessagingClient#send(String, String, Message);  |
| ServiceClient#getFileUploadNotificationReceiver(); | yes | new FileUploadNotificationProcessorClient(String, IotHubServiceClientProtocol, Function<FeedbackBatch, AcknowledgementType>);  |
| FileUploadNotificationReceiver#open(); | yes | FileUploadNotificationProcessorClient#start();  |
| FileUploadNotificationReceiver#close(); | yes | FileUploadNotificationProcessorClient#stop();  |
| FileUploadNotificationReceiver#receive(); | yes | no equivalent method**  |
| FileUploadNotificationReceiver#receive(int); | yes | no equivalent method**  |
| ServiceClient#getFeedbackReceiver(); | yes | new MessageFeedbackProcessorClient(String, IotHubServiceClientProtocol, Function<FileUploadNotification, AcknowledgementType>);  |
| FeedbackReceiver#open(); | yes | MessageFeedbackProcessorClient#start();  |
| FeedbackReceiver#close(); | yes | MessageFeedbackProcessorClient#stop();  |
| FeedbackReceiver#receive(); | yes | no equivalent method**  |
| FeedbackReceiver#receive(int); | yes | no equivalent method**  |

** FileUploadNotificationProcessorClient and MessageFeedbackProcessorClient will start receiving as soon as the client is started. Events are received through the callback provided at constructor time.


#### Other notable breaking changes

- Trust certificates are read from the physical device's trusted root certification authorities certificate store rather than from source.
  - Users are expected to install the required public certificates into this certificate store if they are not present already.
  - See [this document](./upcoming_certificate_changes_readme.md) for additional context on which certificates need to be installed.
  - For most users, no action is needed here since IoT Hub uses the [DigiCert Global G2 CA root](https://global-root-g2.chain-demos.digicert.com/info/index.html) certificate which is already installed on most devices.
- The Bouncycastle dependencies have been removed.
  - The Bouncycastle dependencies were used for some certificate parsing logic that has been removed from the SDK.
- Reduced access levels to classes and methods that were never intended to be public where possible .
- Removed service error code descriptions that the service would never return the error code for.
- Reduce default SAS token time to live from 1 year to 1 hour for security purposes.
- Removed unnecessary synchronization on service client APIs to allow for a single client to make service API calls simultaneously.
- Removed asynchronous APIs for service client APIs.
  - These were wrappers on top of the existing sync APIs. Users are expected to write async wrappers that better fit their preferred async framework.
- Fixed a bug where dates retrieved by the client were converted to local time zone rather than keeping them in UTC time.  


### DPS Device Client

No notable changes, but the security providers that are used in conjunction with this client have changed. See [this section](#security-provider-clients) for more details.


### DPS Service Client

No client APIs have changed for this package, but there are a few notable breaking changes:

- Trust certificates are read from the physical device's trusted root certification authorities certificate store rather than from source.
  - Users are expected to install the required public certificates into this certificate store if they are not present already.
  - See [this document](./upcoming_certificate_changes_readme.md) for additional context on which certificates need to be installed.
  - For most users, no action is needed here since IoT Hub uses the [DigiCert Global G2 CA root](https://global-root-g2.chain-demos.digicert.com/info/index.html) certificate which is already installed on most devices.
- Reduced access levels to classes and methods that were never intended to be public where possible.
- Reduce default SAS token time to live from 1 year to 1 hour for security purposes.


### Security Provider Clients

Breaking changes:
- Trust certificates are read from the physical device's trusted root certification authorities certificate store rather than from source.
  - Users are expected to install the required public certificates into this certificate store if they are not present already.
  - See [this document](./upcoming_certificate_changes_readme.md) for additional context on which certificates need to be installed.
  - For most users, no action is needed here since IoT Hub uses the [DigiCert Global G2 CA root](https://global-root-g2.chain-demos.digicert.com/info/index.html) certificate which is already installed on most devices.
- Users of the X509 SecurityProvider are expected to pass in the parsed certificates and keys as Java security primitives rather than as strings.
  - See [this sample](./provisioning/provisioning-samples/provisioning-X509-sample) for a demonstration on how to create these Java security primitives from strings.


### Deps

Breaking changes:
- The deps package has been removed from the v2 SDK and its classes have been copied to the respective client libraries that used them
  

## Frequently Asked Questions

Question:
> What do I gain by upgrading to the 2.X.X release?

Answer:
> You get a smaller set of dependencies which makes for a lighter SDK overall. You also get a more concise and clear API surface. 
> Lastly, and most importantly, you get an SDK that is decoupled from a particular IoT hub and Device Provisioning 
> Service public certificate. This makes these versions more future proof since they aren't tied to a certificate
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
> The deprecated API in the 1.X.X version documents which API you should use instead of the deprecated API. This guide
also contains a mapping from v1 API to equivalent v2 API that should tell you which v2 API to use.

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

Question:
> Is the v2 library backwards compatible in any way?

Answer:
> No. Several APIs have been added to v2 that are not present in v1. On top of that, several APIs in v2 are expected to behave differently than they did in v1.