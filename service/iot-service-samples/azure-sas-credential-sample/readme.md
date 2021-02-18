# Azure SAS Credential Sample

This sample demonstrates how to use the Azure.Core [AzureSasCredential][azure-sas-credential] type to create service client instances that 
use symmetric key based authentication.

## Summary

When connecting your service client instance to your IoT Hub, you can either choose to authenticate using role-based
access control tokens, or by using shared access signatures. This sample demonstrates one of the ways to create
your service client instance so that it uses shared access signature tokens. For a more thorough overview on what 
shared access signatures are, see [this article][sas-overview].

Service clients such as RegistryManager have constructors that take in connection strings for your IoT Hub, and the SDK
will derive shared access signatures for you from these connection strings. Many users may prefer to generate their own 
shared access signatures, though, so there are constructors for the same service clients that take in the AzureSasCredential
type to enable this. The AzureSasCredential type allows for users to create the shared access signature to be used
for authentication, and to choose when to update that shared access signature. It also allows users to not provide the 
full IoT Hub level connection string to the service client instances.

## Creating shared access signatures

Within this sample, a single instance of the AzureSasCredential type is created and used to construct instances of the 
various service clients within this SDK. In order to run the sample, you only need a single shared access signature. 


Below is some sample code that demonstrates how to create a shared access signature from the IoT Hub level 
symmetric key and from a resource URI. This function can be used to help you generate the shared access signature to 
run this sample with.

```java
/**
 * Create a shared access signature for a given resource and key. This method isn't used by the sample,
 * but it is a good reference for how to create shared access signatures in Java.
 *
 * @param resourceUri The resource that the shared access token should grant access to. For cases where the token
 * will be used for more than one function (i.e. used by registryManager to create a device and used by serviceClient
 * to send cloud to device messages), this value should be the hostName of your IoT Hub
 * ("my-azure-iot-hub.azure-devices.net" for example). Shared access signatures do support scoping of the resource
 * authorization by making this resourceUri more specific. For example, a resourceUri of "my-azure-iot-hub.azure-devices.net/devices"
 * will make this token only usable when creating/updating/deleting device identities.
 * For more examples, see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-security#use-security-tokens-from-service-components">Using security tokens from service components</a>
 * @param key The shared access key for your IoT Hub.
 * @param policyName (optional) The name of the policy. For instance, "iothubowner", "registryRead", "registryReadWrite"
 * @return The shared access signature. This value can be used as is to build a {@link AzureSasCredential} instance
 * @throws UnsupportedEncodingException If UTF_8 is not a supported encoding on your device.
 * @throws NoSuchAlgorithmException If HmacSHA256 algorithm isn't found.
 * @throws InvalidKeyException If initializing the HmacSHA256 SHA fails.
 * @see <a href="docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-security">Control access to IoT Hub</a>
 */
public static String generateSharedAccessSignature(String resourceUri, String key, String policyName)
    throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException
{
    String sharedAccessSignatureFormat = 
        "SharedAccessSignature sr=%s&sig=%s&se=%s";
    
    String sharedAccessSignatureFormatWithPolicyName = 
        "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s";
    
    Charset utf8 = StandardCharsets.UTF_8;
    String utf8Name = utf8.name();

    // Token will expire in one hour
    long expiry = Instant.now().getEpochSecond() + 3600;

    String stringToSign = URLEncoder.encode(resourceUri, utf8Name) + "\n" + expiry;
    byte[] decodedKey = Base64.getDecoder().decode(key);

    Mac sha256HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "HmacSHA256");
    sha256HMAC.init(secretKey);
    Base64.Encoder encoder = Base64.getEncoder();

    String signature = new String(encoder.encode(sha256HMAC.doFinal(stringToSign.getBytes(utf8))), utf8);

    String token =
        String.format(
            sharedAccessSignatureFormat,
            URLEncoder.encode(resourceUri, utf8Name),
            URLEncoder.encode(signature, utf8Name),
            expiry);

    if (policyName != null && !policyName.isEmpty())
    {
        token =
        String.format(
            sharedAccessSignatureFormatWithPolicyName,
            URLEncoder.encode(resourceUri, utf8Name),
            URLEncoder.encode(signature, utf8Name),
            expiry,
            policyName);
        
    }

    return token;
}
```

## Renewing shared access signatures

One important consideration for users of the AzureSasCredential constructors is that each shared access signature has a 
time to live. It is important to periodically update the shared access signature within the AzureSasCredential instance
in order to avoid authentication failures.

```java
String sharedAccessSignature = "some shared access signature that expires in 1 hour"; 
AzureSasCredential credential = new AzureSasCredential(sharedAccessSignature);

// Before 1 hour later, renewal code should be invoked

String newSharedAccessSignature = "some new shared access signature"; 
credential.update(newSharedAccessSignature);
``` 

For instances of ServiceClient, FeedbackReceiver, and FileUploadNotificationReceiver, users are advised to update their
AzureSasCredential instance proactively. These clients open a persistent AMQP connection which has a proactive token 
renewal mechanism that relies on the shared access signature being renewed before 85% of the previous token's lifespan.

For instances of the other service clients such as RegistryManager, the shared access signature does not have to be 
renewed as early, but it is still recommended. These clients communicate over HTTP, so there is no persistent connection
to worry about, but renewing the shared access signature proactively does help to avoid clock skew issues.


## Shared access signature scoping

One extra benefit to using the AzureSasCredential constructor rather than the connection string constructors is that they 
allow for the client to use shared access signatures with more specific security scoping. This allows for users to avoid
creating shared access signatures with more authority to access service functionality than necessary. More details about
this scoping can be seen [here][scoping-overview].

[sas-overview]: https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-security#security-token-structure
[scoping-overview]: https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-security#use-security-tokens-from-service-components
[azure-sas-credential]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureSasCredential.java