/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.dps.device.*;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSClientException;
import com.microsoft.azure.sdk.iot.dps.security.DPSHsmType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Device Twin Sample for an IoT Hub. Default protocol is to use
 * MQTT transport.
 */
public class DPSTpmSample
{
    //private static final String scopeId = "[Your scope ID here]";
    //private static final String scopeId = "0ne00000026";
    private static final String scopeId = "0ne00000020";
    //private static final String scopeId = "0ne00000014";
    //private static final String dpsUri = "[Your DPS HUB here]";
    private static final String dpsUri = "global.azure-devices-provisioning.net";
    //private static final String dpsUri = "global.df.azure-devices-provisioning-int.net";
    //private static final String dpsUri = "10.83.117.151";
    private static final DPSTransportProtocol dpsTransportProtocol = DPSTransportProtocol.HTTPS;

    private static DPSRegistrationInfo dpsRegistrationInfoClient = new DPSRegistrationInfo();
    private static final int MAX_TIME_TO_WAIT_FOR_DPS_REGISTRATION = 1000; // in milli seconds

    static class DPSStatus
    {
        DPSDeviceStatus status;
        String reason;
    }

    static class DPSStatusCallbackImpl implements DpsStatusCallback
    {
        @Override
        public void run(DPSDeviceStatus status, String reason, Object context)
        {
            System.out.println("DPS status " + status );
            if (reason != null)
            {
                System.out.println("because " + reason);
            }
            if (context instanceof DPSStatus)
            {
                DPSStatus dpsStatus = (DPSStatus) context;
                dpsStatus.status = status;
                dpsStatus.reason = reason;
            }
        }
    }

    static class DPSRegistrationCallbackImpl implements DPSRegistrationCallback
    {
        @Override
        public void run(DPSRegistrationInfo dpsRegistrationInfo, Object context)
        {
            if (context instanceof DPSRegistrationInfo)
            {
                dpsRegistrationInfoClient = dpsRegistrationInfo;
            }
            else
            {
                System.out.println("Received unknown context");
            }
        }
    }

    public static void main(String[] args)
            throws IOException, URISyntaxException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");
        DpsDeviceClient dpsDeviceClient = null;
        try
        {
            DPSStatus dpsStatus = new DPSStatus();
            DPSConfig dpsConfig = new DPSConfig(dpsUri, scopeId, dpsTransportProtocol, DPSHsmType.TPM_EMULATOR);

            dpsDeviceClient = new DpsDeviceClient(dpsConfig, new DPSStatusCallbackImpl(), dpsStatus);

            dpsDeviceClient.registerDevice(new DPSRegistrationCallbackImpl(), dpsRegistrationInfoClient);

            while (dpsStatus.status != DPSDeviceStatus.DPS_DEVICE_STATUS_ASSIGNED)
            {
                if (dpsStatus.status == DPSDeviceStatus.DPS_DEVICE_STATUS_ERROR)
                {
                    System.out.println("Dps error, bailing out");
                    break;
                }
                System.out.println("Waiting for Dps Hub to register");
                Thread.sleep(MAX_TIME_TO_WAIT_FOR_DPS_REGISTRATION);
            }

            if (dpsRegistrationInfoClient.getDpsStatus() == DPSDeviceStatus.DPS_DEVICE_STATUS_ASSIGNED)
            {
                System.out.println("IotHUb Uri : " + dpsRegistrationInfoClient.getIothubUri());
                System.out.println("Device ID : " + dpsRegistrationInfoClient.getDeviceId());
                // connect to iothub
            }
        }
        catch (DPSClientException | InterruptedException e)
        {
            System.out.println("DPS threw a exception" + e.getMessage());
            if (dpsDeviceClient != null)
            {
                dpsDeviceClient.close();
            }
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        if (dpsDeviceClient != null)
        {
            dpsDeviceClient.close();
        }

        System.out.println("Shutting down...");

    }
}
