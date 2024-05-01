/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Symmetric Key authenticated enrollment group sample. In order to demonstrate best security practices, this sample
 * does not take the enrollment group level symmetric key as an input. Instead, it only takes the device specific derived
 * symmetric key. To learn how to derive this device specific symmetric key, see the {@link ComputeDerivedSymmetricKeySample}
 * in this same directory.
 */
@SuppressWarnings("CommentedOutCode") // Ignored in samples as we use these comments to show other options.
public class ProvisioningSymmetricKeyEnrollmentGroupSample
{
    // The scope Id of your DPS instance. This value can be retrieved from the Azure Portal
    private static final String ID_SCOPE = "[Your ID scope here]";

    // Note that a different value is required here when connecting to a private or government cloud instance. This
    // value is fine for most DPS instances otherwise.
    private static final String GLOBAL_ENDPOINT = "global.azure-devices-provisioning.net";

    // Not to be confused with the symmetric key of the enrollment group itself, this key is derived from the symmetric
    // key of the enrollment group and the desired device id of the device to provision. See the
    // "ComputeDerivedSymmetricKeySample" code in this same directory for instructions on how to derive this key.
    private static final String DERIVED_ENROLLMENT_GROUP_SYMMETRIC_KEY = "[Enter your derived symmetric key here]";

    // The Id to assign to this device when it is provisioned to an IoT Hub. This value is arbitrary outside of some
    // character limitations. For sample purposes, this value is filled in for you, but it may be changed. This value
    // must be consistent with the device id used when deriving the symmetric key that is used in this sample.
    private static final String PROVISIONED_DEVICE_ID = "myProvisionedDevice";

    // Uncomment one line to choose which protocol you'd like to use
    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT_WS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS_WS;

    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        // For the sake of security, you shouldn't save keys into String variables as that places them in heap memory. For the sake
        // of simplicity within this sample, though, we will save it as a string. Typically this key would be loaded as byte[] so that
        // it can be removed from stack memory.
        byte[] derivedSymmetricKey = DERIVED_ENROLLMENT_GROUP_SYMMETRIC_KEY.getBytes(StandardCharsets.UTF_8);

        SecurityProviderSymmetricKey securityClientSymmetricKey = new SecurityProviderSymmetricKey(derivedSymmetricKey, PROVISIONED_DEVICE_ID);

        ProvisioningDeviceClient provisioningDeviceClient = ProvisioningDeviceClient.create(
            GLOBAL_ENDPOINT,
            ID_SCOPE,
            PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL,
            securityClientSymmetricKey);

        ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult = provisioningDeviceClient.registerDeviceSync();
        provisioningDeviceClient.close();

        if (provisioningDeviceClientRegistrationResult.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
        {
            System.out.println("IotHub Uri : " + provisioningDeviceClientRegistrationResult.getIothubUri());
            System.out.println("Device ID : " + provisioningDeviceClientRegistrationResult.getDeviceId());

            // connect to iothub
            String iotHubUri = provisioningDeviceClientRegistrationResult.getIothubUri();
            String deviceId = provisioningDeviceClientRegistrationResult.getDeviceId();
            DeviceClient deviceClient = new DeviceClient(iotHubUri, deviceId, securityClientSymmetricKey, IotHubClientProtocol.MQTT);
            deviceClient.open(false);

            System.out.println("Sending message from device to IoT Hub...");
            deviceClient.sendEvent(new Message("Hello world!"));
            deviceClient.close();
        }

        System.out.println("Press any key to exit...");
        new Scanner(System.in, StandardCharsets.UTF_8.name()).nextLine();
    }
}
