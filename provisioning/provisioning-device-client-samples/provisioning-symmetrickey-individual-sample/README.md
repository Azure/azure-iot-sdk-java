# DPS and IoT hub Certificate Signing Sample

This sample demonstrates the certificate signing features available when provisioning a device with the Device Provisioning Service and when connected to IoT Hub.

*Note that this feature is currently only available over MQTT/MQTT_WS for both DPS and IoT Hub*

## DPS feature demonstrated

When provisioning a device, you may optionally include a certificate signing request such that the provisioned device can use those certificates when connecting to IoT hub after provisioning completes.

```java
AdditionalData provisioningAdditionalData = new AdditionalData();
provisioningAdditionalData.setClientCertificateSigningRequest(...);
ProvisioningDeviceClientRegistrationResult provisioningResult = provisioningDeviceClient.registerDeviceSync(provisioningAdditionalData);
List<String> issuedClientCertificates = provisioningResult.getIssuedClientCertificateChain();
```

## IoT hub feature demonstrated

If your device needs to renew its certificates for any reason, it can send a certificate signing request to IoT hub

```java
DeviceClient client = new DeviceClient(...);
client.open(false);
IotHubCertificateSigningRequest iothubCsr = new IotHubCertificateSigningRequest(...);
client.sendCertificateSigningRequest(iothubCsr);
```

Once IoT hub has accepted and completed this certificate signing response, you can close the connection to IoT hub, create a new client for the device that uses these renewed certificates, and then re-open the connection.

