# DPS and IoT hub Certificate Signing Sample

This sample demonstrates the certificate signing features available when provisioning a device with the Device Provisioning Service and when connected to IoT Hub.

*Note that this feature is currently only available over MQTT/MQTT_WS for both DPS and IoT Hub*

## Prerequisites

1. **Azure IoT Hub** - An Azure IoT Hub instance
1. **Azure Device Provisioning Service (DPS)** - Linked to your IoT Hub (for initial provisioning)
1. **DPS Enrollment** - Configured for certificate issuance

## How to run

Simply fill in your specific values for the fields defined at the beginning:

```java
private static final String SAMPLE_CERTIFICATES_OUTPUT_PATH = "~/SampleCertificates";
private static final String DPS_ID_SCOPE = "<>";
private static final String DPS_SYMMETRIC_KEY = "<>";
```

And optionally specify other values (which have defaults):

```java
private static final String PROVISIONED_DEVICE_ID = "myCsrProvisionedDevice";
private static final CertificateType certificateType = CertificateType.RSA; // ECC vs RSA

// Certificate signing feature is currently only supported over MQTT/MQTT_WS
private static final IotHubClientProtocol iotHubProtocol = IotHubClientProtocol.MQTT;
private static final ProvisioningDeviceClientTransportProtocol dpsProtocol = ProvisioningDeviceClientTransportProtocol.MQTT;
```

## DPS feature demonstrated

When provisioning a device, you may optionally include a certificate signing request such that the provisioned device can use those certificates when connecting to IoT hub after provisioning completes.

```java
AdditionalData provisioningAdditionalData = new AdditionalData();
provisioningAdditionalData.setClientCertificateSigningRequest(...);
ProvisioningDeviceClientRegistrationResult provisioningResult = provisioningDeviceClient.registerDeviceSync(provisioningAdditionalData);
List<String> issuedClientCertificates = provisioningResult.getIssuedClientCertificateChain();
```

*Note that the provisioning device client itself does not use these soon-to-be-signed certificates when authenticating with DPS. It must use one of the TPM/Symmetric Key/x509 authentication mechanisms detailed in other samples in this directory.*

## IoT hub feature demonstrated

If your device needs to renew its certificates for any reason, it can send a certificate signing request to IoT hub

```java
DeviceClient client = new DeviceClient(...);
client.open(false);
IotHubCertificateSigningRequest iothubCsr = new IotHubCertificateSigningRequest(...);
... = client.sendCertificateSigningRequest(iothubCsr);
```

Once IoT hub has accepted and completed this certificate signing response, you can close the connection to IoT hub, create a new client for the device that uses these renewed certificates, and then re-open the connection.

## Additional feature notes

- This certificate signing feature works for both RSA and ECC certificates
- The DPS flow is applicable for any combination of 
  - Individual enrollment vs enrollment group
  - Symmetric key vs TPM vs x509 authentication