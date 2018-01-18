/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;

import java.io.IOException;
import java.util.Scanner;

/**
 * TPM sample
 */
public class ProvisioningTpmSample
{
    private static final String scopeId = "[Your scope ID here]";
    private static final String globalEndpoint = "[Your Provisioning Service Global Endpoint here]";
    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS_WS;
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 10000; // in milli seconds

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
        SecurityProviderTpm securityClientTPMEmulator = null;
        Scanner scanner = new Scanner(System.in);
        DeviceClient deviceClient = null;

        try
        {
            securityClientTPMEmulator = new SecurityProviderTPMEmulator();
            System.out.println("Endorsement Key : \n" + new String(Base64.encodeBase64Local(securityClientTPMEmulator.getEndorsementKey())));
            System.out.println("Registration Id : \n" + securityClientTPMEmulator.getRegistrationId());
            System.out.println("Please visit Azure Portal (https://portal.azure.com/) and create a TPM Individual Enrollment with the information above i.e EndorsementKey and RegistrationId \n" +
                                       "Press enter when you are ready to run registration after enrolling with the service");
            scanner.nextLine();
        }
        catch (SecurityProviderException e)
        {
            e.printStackTrace();
        }

        ProvisioningDeviceClient provisioningDeviceClient = null;
        try
        {
            ProvisioningStatus provisioningStatus = new ProvisioningStatus();

            provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, scopeId, PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL, securityClientTPMEmulator);

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
                    deviceClient = DeviceClient.createFromSecurityProvider(iotHubUri, deviceId, securityClientTPMEmulator, IotHubClientProtocol.MQTT);
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
