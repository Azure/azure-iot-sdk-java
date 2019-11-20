// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.provisioning.device.AdditionalData;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClient;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationResult;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.*;
import static java.util.Collections.singletonList;

@Slf4j
public class Application {
    private static final String DCM_ID = "urn:azureiot:samplemodel:1";
    private static final String GLOBAL_ENDPOINT = System.getenv("GLOBAL_ENDPOINT");
    private static final String ID_SCOPE = System.getenv("ID_SCOPE");
    private static final String SYMMETRIC_PRIMARY_KEY = System.getenv("SYMMETRIC_PRIMARY_KEY");
    private static final String REGISTRATION_ID = System.getenv("REGISTRATION_ID");
    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT;
    private static final IotHubClientProtocol DEVICE_CLIENT_TRANSPORT_PROTOCOL = IotHubClientProtocol.MQTT;
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION_IN_MILLISECONDS = 10000;

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
                log.error("Received unknown context");
            }
        }
    }

    public static void main(String args[]) throws ProvisioningDeviceClientException, InterruptedException, IOException, URISyntaxException {
        String interfaceRegistrationJson =
                "{" +
                "\"__iot:interfaces\":" +
                "       {" +
                "           \"CapabilityModelId\": \"" + DCM_ID + "\"" +
                "       }" +
                "}";

        AdditionalData interfaceRegistrationData = new AdditionalData();
        interfaceRegistrationData.setProvisioningPayload(interfaceRegistrationJson);

        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(SYMMETRIC_PRIMARY_KEY.getBytes(), REGISTRATION_ID);
        ProvisioningDeviceClient provisioningDeviceClient = ProvisioningDeviceClient.create(GLOBAL_ENDPOINT, ID_SCOPE, PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL, securityProviderSymmetricKey);

        ProvisioningStatus provisioningStatus = new ProvisioningStatus();
        provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus, interfaceRegistrationData);

        log.debug("Waiting for Provisioning Service to register");
        Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION_IN_MILLISECONDS);

        ProvisioningDeviceClientRegistrationResult registrationResult = provisioningStatus.provisioningDeviceClientRegistrationInfoClient;

        if (registrationResult.getProvisioningDeviceClientStatus() == PROVISIONING_DEVICE_STATUS_ERROR ||
                registrationResult.getProvisioningDeviceClientStatus() == PROVISIONING_DEVICE_STATUS_DISABLED ||
                registrationResult.getProvisioningDeviceClientStatus() == PROVISIONING_DEVICE_STATUS_FAILED) {
            log.error("Registration error, bailing out", provisioningStatus.exception);
        } else {
            String iothubUri = registrationResult.getIothubUri();
            String deviceId = registrationResult.getDeviceId();
            log.debug("Created device client with IoT Hub URI={} and device ID={}", iothubUri, deviceId);

            DeviceClient deviceClient = DeviceClient.createFromSecurityProvider(iothubUri, deviceId, securityProviderSymmetricKey, DEVICE_CLIENT_TRANSPORT_PROTOCOL);

            DigitalTwinDeviceClient digitalTwinDeviceClient = new DigitalTwinDeviceClient(deviceClient);

            final DeviceInformation deviceInformation = DeviceInformation.builder()
                                                                         .manufacturer("Microsoft")
                                                                         .model("1.0.0")
                                                                         .osName(System.getProperty("os.name"))
                                                                         .processorArchitecture(System.getProperty ("os.arch"))
                                                                         .processorManufacturer("Intel(R) Core(TM)")
                                                                         .softwareVersion("JDK" + System.getProperty ("java.version"))
                                                                         .totalMemory(16e9)
                                                                         .totalStorage(1e12)
                                                                         .build();
            DigitalTwinClientResult result = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(deviceInformation)).blockingGet();
            log.info("Register interfaces result: {}.", result);

            log.info("Waiting for service updates...");
            log.info("Enter any key to finish");
            new Scanner(System.in).nextLine();

        }

    }

}
