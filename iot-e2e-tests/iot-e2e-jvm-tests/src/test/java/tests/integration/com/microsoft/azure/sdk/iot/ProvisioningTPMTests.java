// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClient;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.Attestation;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.IndividualEnrollment;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.TpmAttestation;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.azure.sdk.iot.provisioning.service.configs.AttestationMechanismType.TPM;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

@Slf4j
public class ProvisioningTPMTests
{
    public static final String DPS_CONNECTION_STRING_ENV_VAR_NAME = "IOT_DPS_CONNECTION_STRING";
    public static String provisioningServiceConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_ENV_VAR_NAME);

    public static String provisioningServiceGlobalEndpoint = "global.azure-devices-provisioning.net";

    public static final String DPS_ID_SCOPE_ENV_VAR_NAME = "IOT_DPS_ID_SCOPE";
    public static String provisioningServiceIdScope = Tools.retrieveEnvironmentVariableValue(DPS_ID_SCOPE_ENV_VAR_NAME);

    private static final int REGISTRATION_TIMEOUT_MILLISECONDS = 60 * 1000;

    @Test
    public void provisioningTpmFlow() throws SecurityProviderException, ProvisioningServiceClientException, ProvisioningDeviceClientException, InterruptedException
    {
        ProvisioningServiceClient provisioningServiceClient = ProvisioningServiceClient.createFromConnectionString(provisioningServiceConnectionString);

        String registrationId = UUID.randomUUID().toString();
        String provisionedDeviceId = "Some-Provisioned-Device-" + TPM + "-" + UUID.randomUUID().toString();
        SecurityProvider securityProvider = new SecurityProviderTPMEmulator(registrationId);
        Attestation attestation = new TpmAttestation(new String(encodeBase64(((SecurityProviderTpm) securityProvider).getEndorsementKey())));

        IndividualEnrollment individualEnrollment = new IndividualEnrollment(registrationId, attestation);
        individualEnrollment.setDeviceIdFinal(provisionedDeviceId);
        provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);

        ProvisioningDeviceClient provisioningDeviceClient =
            ProvisioningDeviceClient.create(
                provisioningServiceGlobalEndpoint,
                provisioningServiceIdScope,
                ProvisioningDeviceClientTransportProtocol.AMQPS,
                securityProvider);

        AtomicBoolean registrationCompleted = new AtomicBoolean(false);
        AtomicBoolean registrationCompletedSuccessfully = new AtomicBoolean(false);
        provisioningDeviceClient.registerDevice(
            (provisioningDeviceClientRegistrationResult, e, context) ->
            {
                log.debug("Provisioning registration callback fired with result {}", provisioningDeviceClientRegistrationResult.getProvisioningDeviceClientStatus());
                if (e != null)
                {
                    log.error("Provisioning registration callback fired with exception {}", e);
                }

                ProvisioningDeviceClientStatus status = provisioningDeviceClientRegistrationResult.getProvisioningDeviceClientStatus();
                if (status == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
                {
                    registrationCompletedSuccessfully.set(true);
                }

                registrationCompleted.set(true);
            },
            null);

        long startTime = System.currentTimeMillis();
        while (!registrationCompleted.get())
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > REGISTRATION_TIMEOUT_MILLISECONDS)
            {
                fail("Timed out waiting for device registration to complete.");
            }
        }

        assertTrue("Registration completed, but not successfully", registrationCompletedSuccessfully.get());
    }
}
