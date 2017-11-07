/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceConnectionException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityClientTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityClientTPMEmulator;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import java.util.Scanner;

/**
 * TPM sample
 */
public class ProvisioningTpmSample
{
    //private static final String scopeId = "[Your scope ID here]";
    private static final String scopeId = "0ne00001D71";
    //private static final String globalEndpoint = "[Your Provisioning Service Global Endpoint here]";
    private static final String globalEndpoint = "global.azure-devices-provisioning.net";
    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
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

    public static void main(String[] args) throws ProvisioningDeviceConnectionException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");
        SecurityClientTpm securityClientTPMEmulator = null;

        try
        {
            securityClientTPMEmulator = new SecurityClientTPMEmulator();
            System.out.println("EK - " + new String(Base64.encodeBase64Local(securityClientTPMEmulator.getDeviceEnrollmentKey())));
            System.out.println("Registration Id - " + securityClientTPMEmulator.getRegistrationId());
        }
        catch (SecurityClientException e)
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
                    System.out.println("Dps error, bailing out");
                    break;
                }
                System.out.println("Waiting for Dps Hub to register");
                Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
            }

            if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                System.out.println("IotHUb Uri : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
                System.out.println("Device ID : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());
                // connect to iothub
            }
        }
        catch (ProvisioningDeviceClientException | InterruptedException e)
        {
            System.out.println("DPS threw a exception" + e.getMessage());
            if (provisioningDeviceClient != null)
            {
                provisioningDeviceClient.closeNow();
            }
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        if (provisioningDeviceClient != null)
        {
            provisioningDeviceClient.closeNow();
        }

        System.out.println("Shutting down...");
    }
}
