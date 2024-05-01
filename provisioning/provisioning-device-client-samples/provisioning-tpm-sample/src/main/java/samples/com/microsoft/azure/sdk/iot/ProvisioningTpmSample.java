/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

/**
 * TPM sample
 */
@SuppressWarnings("CommentedOutCode") // Ignored in samples as we use these comments to show other options.
public class ProvisioningTpmSample
{
    private static final String ID_SCOPE = "[Your ID scope here]";

    // Note that a different value is required here when connecting to a private or government cloud instance. This
    // value is fine for most DPS instances otherwise.
    private static final String GLOBAL_ENDPOINT = "global.azure-devices-provisioning.net";

    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT_WS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS_WS;

    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 10000; // in milliseconds

    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");
        SecurityProviderTpm securityClientTPMEmulator = null;
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());

        try
        {
            securityClientTPMEmulator = new SecurityProviderTPMEmulator();
            System.out.println("Endorsement Key : \n" + new String(encodeBase64(securityClientTPMEmulator.getEndorsementKey()), StandardCharsets.UTF_8));
            System.out.println("Registration Id : \n" + securityClientTPMEmulator.getRegistrationId());
            System.out.println("Please visit Azure Portal (https://portal.azure.com/) and create a TPM Individual Enrollment with the information above i.e EndorsementKey and RegistrationId \n" +
                                       "Press enter when you are ready to run registration after enrolling with the service");
            scanner.nextLine();
        }
        catch (SecurityProviderException e)
        {
            System.out.println("Communication with the TPM failed. Exiting sample...");
            e.printStackTrace();
            System.exit(-1);
        }

        ProvisioningDeviceClient provisioningDeviceClient = ProvisioningDeviceClient.create(
            GLOBAL_ENDPOINT,
            ID_SCOPE,
            PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL,
            securityClientTPMEmulator);

        ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult = provisioningDeviceClient.registerDeviceSync();
        provisioningDeviceClient.close();

        if (provisioningDeviceClientRegistrationResult.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
        {
            System.out.println("IotHub Uri : " + provisioningDeviceClientRegistrationResult.getIothubUri());
            System.out.println("Device ID : " + provisioningDeviceClientRegistrationResult.getDeviceId());

            // connect to iothub
            String iotHubUri = provisioningDeviceClientRegistrationResult.getIothubUri();
            String deviceId = provisioningDeviceClientRegistrationResult.getDeviceId();
            DeviceClient deviceClient = new DeviceClient(iotHubUri, deviceId, securityClientTPMEmulator, IotHubClientProtocol.MQTT);
            deviceClient.open(false);

            System.out.println("Sending message from device to IoT Hub...");
            deviceClient.sendEvent(new Message("Hello world!"));
            deviceClient.close();
        }

        System.out.println("Press any key to exit...");
        scanner.nextLine();
    }
}
