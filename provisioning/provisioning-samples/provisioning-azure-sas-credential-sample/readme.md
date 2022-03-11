# Azure Sas Credential Sample

## Summary

This sample demonstrates how to instantiate the [ProvisioningServiceClient](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable)
using an sas credential instead of the standard Provisioning connection string. For a more thorough overview on what
shared access signatures are, see [this article][sas-overview].

The service client has a constructor that takes in connection strings for your DPS instance, and the SDK
will derive shared access signatures for you from these connection strings. Many users may prefer to generate their own
shared access signatures, though, so there are constructors for the same service clients that take in the AzureSasCredential
type to enable this. The AzureSasCredential type allows for users to create the shared access signature to be used
for authentication, and to choose when to update that shared access signature.

## Creating shared access signatures

Within this sample, a single instance of the AzureSasCredential type is created and used to construct instances of the
various service clients within this SDK. In order to run the sample, you only need a single shared access signature.


Below is some sample code that demonstrates how to create a shared access signature from the
symmetric key and from a resource URI. This function can be used to help you generate the shared access signature to
run this sample with.

```java
/**
 * Create a shared access signature for a given resource and key. This method isn't used by the sample,
 * but it is a good reference for how to create shared access signatures in Java.
 *
 * @param resourceUri The resource that the shared access token should grant access to. 
 * @param key The shared access key for your DPS instance.
 * @param policyName (optional) The name of the policy. For instance, "iothubowner", "registryRead", "registryReadWrite"
 * @return The shared access signature. This value can be used as is to build a {@link AzureSasCredential} instance
 * @throws UnsupportedEncodingException If UTF_8 is not a supported encoding on your device.
 * @throws NoSuchAlgorithmException If HmacSHA256 algorithm isn't found.
 * @throws InvalidKeyException If initializing the HmacSHA256 SHA fails.
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

[scoping-overview]: https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-security#use-security-tokens-from-service-components
[azure-sas-credential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureSasCredential.java