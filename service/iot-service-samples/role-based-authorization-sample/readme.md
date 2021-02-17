# Role Based Authentication Sample

## Summary

This sample demonstrates how to instantiate the service clients so that they use role-based access control 
credentials for authentication. For more details on what role-based access control is, 
please see [this overview][rbac-overview].

From the SDK side, users are required to implement the [TokenCredential][token-credential] interface so that the client 
code can use role-based access tokens to authenticate with the service.

## TokenCredential implementations

While users can create their own implementations of the TokenCredential interface, the Azure Identity SDK has several 
implementations already created that can be leveraged. A few of these implementations are described below:
- [DefaultAzureCredential][default-azure-credential]: Acquires the token from environment variables or the shared token cache.
- [ClientSecretCredential][client-secret-credential]: Acquires a token with a client secret for an AAD application.
- [InteractiveBrowserCredential][interactive-browser-credential]: Acquires a token for an AAD application by prompting the login in the default browser.

The Azure Identity SDK also contains implementations of TokenCredential that acquire the token from Intellij, Visual Studio Code, and many other sources.

This sample demonstrates how to use the ClientSecretCredential class to acquire access tokens from your AAD application's 
client secret. Each service client (ServiceClient, RegistryManager, JobClient, DeviceTwin, DeviceMethod, DigitalTwinClient, and 
DigitalTwinAsyncClient) contains a constructor that takes the TokenCredential interface, so whichever implementation you 
create, that implementation can be used to construct every service client. This sample demonstrates this by creating one
ClientSecretCredential instance and using it when constructing multiple service client instances.

[rbac-overview]: https://docs.microsoft.com/en-us/azure/role-based-access-control/overview
[token-credential]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/credential/TokenCredential.java
[default-azure-credential]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/src/main/java/com/azure/identity/DefaultAzureCredential.java
[client-secret-credential]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/src/main/java/com/azure/identity/ClientSecretCredential.java
[interactive-browser-credential]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/src/main/java/com/azure/identity/InteractiveBrowserCredential.java
