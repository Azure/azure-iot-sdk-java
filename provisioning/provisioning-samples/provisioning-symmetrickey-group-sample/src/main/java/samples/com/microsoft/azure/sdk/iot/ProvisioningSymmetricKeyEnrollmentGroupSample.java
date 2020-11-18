/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Symmetric Key authenticated enrollment group sample
 */
public class ProvisioningSymmetricKeyEnrollmentGroupSample
{
    // The scope Id of your DPS instance. This value can be retrieved from the Azure Portal
    private static final String SCOPE_ID = "[Your scope ID here]";

    // Typically "global.azure-devices-provisioning.net"
    private static final String GLOBAL_ENDPOINT = "[Your Provisioning Service Global Endpoint here]";

    // The symmetric key of the enrollment group. Unlike with individual enrollments, this key cannot be used directly when provisioning a device.
    // Instead, this sample will demonstrate how to derive the symmetric key for your particular device within the enrollment group.
    private static final String ENROLLMENT_GROUP_SYMMETRIC_KEY = "[Enter your Symmetric Key here]";

    // The Id to assign to this device when it is provisioned to an IoT Hub. This value is arbitrary outside of some
    // character limitations. For sample purposes, this value is filled in for you, but it may be changed.
    private static final String PROVISIONED_DEVICE_ID = "myProvisionedDevice";

    // Uncomment one line to choose which protocol you'd like to use
    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT_WS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS_WS;

    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 10000; // in milliseconds

    static class ProvisioningStatus
    {
        ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationInfoClient = new ProvisioningDeviceClientRegistrationResult();
        Exception exception;
    }

    static class ProvisioningDeviceClientRegistrationCallbackImpl implements ProvisioningDeviceClientRegistrationCallback
    {
        @Override
        public void run(ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult, Exception exception, Object context)
        {
            if (context instanceof ProvisioningStatus)
            {
                ProvisioningStatus status = (ProvisioningStatus) context;
                status.provisioningDeviceClientRegistrationInfoClient = provisioningDeviceClientRegistrationResult;
                status.exception = exception;
            }
            else
            {
                System.out.println("Received unknown context");
            }
        }
    }

    private static class IotHubEventCallbackImpl implements IotHubEventCallback
    {
        @Override
        public void execute(IotHubStatusCode responseStatus, Object callbackContext)
        {
            System.out.println("Message received! Response status: " + responseStatus);
        }
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");
        SecurityProviderSymmetricKey securityClientSymmetricKey = null;
        Scanner scanner = new Scanner(System.in);
        DeviceClient deviceClient = null;

        // Since enrollment groups can be used to provision more than one device, the service requires you to derive the
        // symmetric key for your device to provision based on the symmetric key of the enrollment group, and the desired
        // device Id of the device you are provisioning

        // For the sake of security, you shouldn't save keys into String variables as that places them in heap memory. For the sake
        // of simplicity within this sample, though, we will save it as a string. Typically this key would be loaded as byte[] so that
        // it can be removed from stack memory.
        byte[] derivedSymmetricKey = SecurityProviderSymmetricKey.ComputeDerivedSymmetricKey(ENROLLMENT_GROUP_SYMMETRIC_KEY.getBytes(StandardCharsets.UTF_8), PROVISIONED_DEVICE_ID);

        securityClientSymmetricKey = new SecurityProviderSymmetricKey(derivedSymmetricKey, PROVISIONED_DEVICE_ID);

        ProvisioningDeviceClient provisioningDeviceClient = null;
        try
        {
            ProvisioningStatus provisioningStatus = new ProvisioningStatus();

            provisioningDeviceClient = ProvisioningDeviceClient.create(GLOBAL_ENDPOINT, SCOPE_ID, PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL, securityClientSymmetricKey);

            provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus);
            while (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() != ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ERROR ||
                        provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_DISABLED ||
                        provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_FAILED)
                {
                    provisioningStatus.exception.printStackTrace();
                    System.out.println("Registration error, bailing out");
                    break;
                }
                System.out.println("Waiting for Provisioning Service to register");
                Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
            }

            if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                System.out.println("IotHUb Uri : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
                System.out.println("Device ID : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

                // connect to iothub
                String iotHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();
                String deviceId = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId();
                try
                {
                    deviceClient = DeviceClient.createFromSecurityProvider(iotHubUri, deviceId, securityClientSymmetricKey, IotHubClientProtocol.MQTT);
                    deviceClient.open();
                    Message messageToSendFromDeviceToHub =  new Message("Whatever message you would like to send");

                    System.out.println("Sending message from device to IoT Hub...");
                    deviceClient.sendEventAsync(messageToSendFromDeviceToHub, new IotHubEventCallbackImpl(), null);
                }
                catch (IOException e)
                {
                    System.out.println("Device client threw an exception: " + e.getMessage());
                    if (deviceClient != null)
                    {
                        deviceClient.closeNow();
                    }
                }
            }
        }
        catch (ProvisioningDeviceClientException | InterruptedException e)
        {
            System.out.println("Provisioning Device Client threw an exception" + e.getMessage());
            if (provisioningDeviceClient != null)
            {
                provisioningDeviceClient.closeNow();
            }
        }

        System.out.println("Press any key to exit...");

        scanner.nextLine();
        if (provisioningDeviceClient != null)
        {
            provisioningDeviceClient.closeNow();
        }
        if (deviceClient != null)
        {
            deviceClient.closeNow();
        }

        System.out.println("Shutting down...");
    }
}
