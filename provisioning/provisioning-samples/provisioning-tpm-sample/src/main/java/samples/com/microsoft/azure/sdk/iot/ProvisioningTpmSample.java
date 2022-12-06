/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

/**
 * TPM sample
 */
@SuppressWarnings("CommentedOutCode") // Ignored in samples as we use these comments to show other options.
public class ProvisioningTpmSample
{
    private static final String SCOPE_ID = "[Your scope ID here]";
    private static final String GLOBAL_ENDPOINT = "[Your Provisioning Service Global Endpoint here]";
    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT_WS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS_WS;
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 10000; // in milliseconds

    private static class MessageSentCallbackImpl implements MessageSentCallback
    {
        @Override
        public void onMessageSent(Message sentMessage, IotHubClientException exception, Object callbackContext)
        {
            IotHubStatusCode status = exception == null ? IotHubStatusCode.OK : exception.getStatusCode();
            System.out.println("Message received! Response status: " + status);
        }
    }

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

        ProvisioningDeviceClient provisioningDeviceClient = null;
        try
        {

            provisioningDeviceClient = ProvisioningDeviceClient.create(GLOBAL_ENDPOINT, SCOPE_ID, PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL, securityClientTPMEmulator);

            final Object deviceRegistrationLock = new Object();
            AtomicReference<ProvisioningDeviceClientRegistrationResult> registrationResultReference = new AtomicReference<>();
            AtomicReference<Exception> registrationExceptionReference = new AtomicReference<>();

            // This function will notify the provided callback once registration has finished (successfully or unsuccessfully)
            provisioningDeviceClient.registerDevice(
                (callbackRegistrationResult, callbackException, callbackContext) -> {
                    // save the returned registration result and exception (if there was one)
                    registrationResultReference.set(callbackRegistrationResult);
                    registrationExceptionReference.set(callbackException);

                    synchronized (deviceRegistrationLock)
                    {
                        // Unlock the deviceRegistrationLock so the sample can continue
                        deviceRegistrationLock.notify();
                    }
                },
                null);

            System.out.println("Waiting for Provisioning Service to register");
            synchronized (deviceRegistrationLock)
            {
                deviceRegistrationLock.wait(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
            }

            ProvisioningDeviceClientRegistrationResult registrationResult = registrationResultReference.get();
            Exception registrationException = registrationExceptionReference.get();

            if (registrationException != null)
            {
                System.out.println("Encountered an exception while registering your device");
                registrationException.printStackTrace();
                System.exit(-1);
            }

            if (registrationResult.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                System.out.println("Device provisioning completed successfully");
                System.out.println("IotHUb Uri : " + registrationResult.getIothubUri());
                System.out.println("Device ID : " + registrationResult.getDeviceId());

                // connect to iothub
                String iotHubUri = registrationResult.getIothubUri();
                String deviceId = registrationResult.getDeviceId();
                DeviceClient deviceClient = null;
                try
                {
                    deviceClient = new DeviceClient(iotHubUri, deviceId, securityClientTPMEmulator, IotHubClientProtocol.MQTT);
                    deviceClient.open(false);
                    Message messageToSendFromDeviceToHub =  new Message("Whatever message you would like to send");

                    System.out.println("Sending message from device to IoT Hub...");
                    deviceClient.sendEventAsync(messageToSendFromDeviceToHub, new MessageSentCallbackImpl(), null);
                }
                catch (IOException e)
                {
                    System.out.println("Device client threw an exception: " + e.getMessage());
                    if (deviceClient != null)
                    {
                        deviceClient.close();
                    }
                }
            }
            else
            {
                System.out.println("Device provisioning completed unsuccessfully. Encountered an unexpected registration status: " + registrationResult.getStatus());
                System.exit(-1);
            }
        }
        catch (ProvisioningDeviceClientException | InterruptedException e)
        {
            System.out.println("Provisioning Device Client threw an exception" + e.getMessage());
            if (provisioningDeviceClient != null)
            {
                provisioningDeviceClient.close();
            }
        }

        System.out.println("Press any key to exit...");
        scanner.nextLine();

        System.out.println("Shutting down...");
        if (provisioningDeviceClient != null)
        {
            provisioningDeviceClient.close();
        }
    }
}
