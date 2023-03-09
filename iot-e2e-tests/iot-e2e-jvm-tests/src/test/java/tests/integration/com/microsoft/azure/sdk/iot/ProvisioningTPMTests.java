// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClient;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationResult;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceHubException;
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
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.azure.sdk.iot.provisioning.service.configs.AttestationMechanismType.TPM;
import static junit.framework.TestCase.*;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

@Slf4j
public class ProvisioningTPMTests
{
    public static final String DPS_CONNECTION_STRING_ENV_VAR_NAME = "IOT_DPS_CONNECTION_STRING";
    public static String provisioningServiceConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_ENV_VAR_NAME);

    public static String provisioningServiceGlobalEndpoint = "global.azure-devices-provisioning.net";

    public static final String DPS_ID_SCOPE_ENV_VAR_NAME = "IOT_DPS_ID_SCOPE";
    public static String provisioningServiceIdScope = Tools.retrieveEnvironmentVariableValue(DPS_ID_SCOPE_ENV_VAR_NAME);

    private static final int REGISTRATION_TIMEOUT_SECONDS = 60;

    @Ignore //Test is very flakey
    @Test
    public void provisioningTpmFlow() throws SecurityProviderException, ProvisioningServiceClientException, ProvisioningDeviceClientException, InterruptedException
    {
        ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClient(provisioningServiceConnectionString);

        String registrationId = UUID.randomUUID().toString();
        String provisionedDeviceId = "Some-Provisioned-Device-" + TPM + "-" + UUID.randomUUID().toString();
        SecurityProvider securityProvider = new SecurityProviderTPMEmulator(registrationId);
        Attestation attestation = new TpmAttestation(new String(encodeBase64(((SecurityProviderTpm) securityProvider).getEndorsementKey())));

        IndividualEnrollment individualEnrollment = new IndividualEnrollment(registrationId, attestation);
        individualEnrollment.setDeviceId(provisionedDeviceId);
        provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);

        ProvisioningDeviceClient provisioningDeviceClient =
            ProvisioningDeviceClient.create(
                provisioningServiceGlobalEndpoint,
                provisioningServiceIdScope,
                ProvisioningDeviceClientTransportProtocol.AMQPS,
                securityProvider);

        final CountDownLatch registrationLatch = new CountDownLatch(1);
        AtomicReference<ProvisioningDeviceClientRegistrationResult> registrationResultReference = new AtomicReference<>();
        AtomicReference<Exception> registrationExceptionReference = new AtomicReference<>();
        provisioningDeviceClient.registerDevice(
            (provisioningDeviceClientRegistrationResult, e, context) ->
            {
                log.debug("Provisioning registration callback fired with result {}", provisioningDeviceClientRegistrationResult.getProvisioningDeviceClientStatus());

                registrationResultReference.set(provisioningDeviceClientRegistrationResult);
                if (e != null)
                {
                    registrationExceptionReference.set(e);
                }

                registrationLatch.countDown();
            },
            null);

        boolean timedOut = !registrationLatch.await(REGISTRATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (timedOut)
        {
            fail("Timed out waiting for device registration to complete.");
        }

        provisioningDeviceClient.close();

        ProvisioningDeviceClientRegistrationResult registrationResult = registrationResultReference.get();
        Exception registrationException = registrationExceptionReference.get();
        log.info("Registration completed with status {}", registrationResult.getStatus());

        if (registrationException != null)
        {
            String errorContext = "";
            errorContext += " Status=" + registrationResult.getStatus();
            errorContext += " Substatus=" + registrationResult.getSubstatus();
            if (registrationException instanceof ProvisioningDeviceClientException)
            {
                errorContext += " Error code=" + ((ProvisioningDeviceHubException) registrationException).getErrorCode();
            }
            fail("Registration finished with exception." + errorContext);
        }

        assertEquals("Registration completed, but not successfully", registrationResult.getProvisioningDeviceClientStatus(), ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED);
    }
}
