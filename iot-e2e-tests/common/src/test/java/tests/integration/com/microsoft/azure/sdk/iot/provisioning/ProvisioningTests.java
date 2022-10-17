/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.provisioning;


import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.twin.TwinClientOptions;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.X509CertificateGenerator;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.DeviceProvisioningServiceTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.provisioning.setup.ProvisioningCommon;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol.*;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@DeviceProvisioningServiceTest
@RunWith(Parameterized.class)
public class ProvisioningTests extends ProvisioningCommon
{
    public ProvisioningTests(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        super(protocol, attestationType);
    }

    @Ignore // The DPS instance we use for this test is currently offline, so this test cannot be run
    @Test
    @ContinuousIntegrationTest
    public void individualEnrollmentWithInvalidRemoteServerCertificateFails() throws Exception
    {
        enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType.INDIVIDUAL);
    }

    @Test
    public void ProvisioningWithCustomPayloadFlow() throws Exception
    {
        String jsonPayload = "{\"a\":\"b\"}";
        String expectedHubToProvisionTo;
        String farAwayIotHubHostname = IotHubConnectionString.createIotHubConnectionString(farAwayIotHubConnectionString).getHostName();
        String iothubHostName = IotHubConnectionString.createIotHubConnectionString(iotHubConnectionString).getHostName();

        if (farAwayIotHubHostname.length() > iothubHostName.length())
        {
            expectedHubToProvisionTo = farAwayIotHubHostname;
        }
        else if (iothubHostName.length() > farAwayIotHubHostname.length())
        {
            expectedHubToProvisionTo = iothubHostName;
        }
        else
        {
            throw new IllegalArgumentException("Both possible hub's cannot have a host name of the same length for this test to work");
        }

        CustomAllocationDefinition allocDefinition = new CustomAllocationDefinition();
        allocDefinition.setApiVersion(CUSTOM_ALLOCATION_WEBHOOK_API_VERSION);
        allocDefinition.setWebhookUrl(customAllocationWebhookUrl);

        testInstance.securityProvider = getSecurityProviderInstance(EnrollmentType.INDIVIDUAL, AllocationPolicy.CUSTOM, null, allocDefinition, null);
        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, jsonPayload, expectedHubToProvisionTo);
    }

    @Ignore // The DPS instance we use for this test is currently offline, so this test cannot be run
    @Test
    @ContinuousIntegrationTest
    public void groupEnrollmentWithInvalidRemoteServerCertificateFails() throws Exception
    {
        enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType.GROUP);
    }

    @Test
    @StandardTierHubOnlyTest
    public void groupEnrollmentReprovisioningCanKeepTwin() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setMigrateDeviceData(true);
        reprovisionPolicy.setUpdateHubAssignment(true);

        DeviceCapabilities deviceCapabilities = new DeviceCapabilities();
        deviceCapabilities.setIotEdge(true);

        reprovisioningFlow(EnrollmentType.GROUP, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo(), deviceCapabilities);
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void groupEnrollmentReprovisioningCanResetTwin() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setMigrateDeviceData(false);
        reprovisionPolicy.setUpdateHubAssignment(true);

        reprovisioningFlow(EnrollmentType.GROUP, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    @ContinuousIntegrationTest
    public void groupEnrollmentCanBlockReprovisioning() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setUpdateHubAssignment(false);

        reprovisioningFlow(EnrollmentType.GROUP, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    public void groupEnrollmentWithCustomAllocationPolicy() throws Exception
    {
        customAllocationFlow(EnrollmentType.GROUP);
    }

    @Test
    @StandardTierHubOnlyTest
    public void individualEnrollmentReprovisioningCanKeepTwin() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setMigrateDeviceData(true);
        reprovisionPolicy.setUpdateHubAssignment(true);

        reprovisioningFlow(EnrollmentType.INDIVIDUAL, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    @StandardTierHubOnlyTest
    @ContinuousIntegrationTest
    public void individualEnrollmentReprovisioningCanResetTwin() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setMigrateDeviceData(false);
        reprovisionPolicy.setUpdateHubAssignment(true);

        reprovisioningFlow(EnrollmentType.INDIVIDUAL, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    @ContinuousIntegrationTest
    public void individualEnrollmentCanBlockReprovisioning() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setUpdateHubAssignment(false);

        reprovisioningFlow(EnrollmentType.INDIVIDUAL, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    public void individualEnrollmentWithCustomAllocationPolicy() throws Exception
    {
        customAllocationFlow(EnrollmentType.INDIVIDUAL);
    }

    @Test
    public void individualEnrollmentGetAttestationMechanismTPM() throws ProvisioningServiceClientException, SecurityProviderException
    {
        //This test fits in better with the other provisioning service client tests, but it needs to be run sequentially
        // with the other TPM tests, so it lives here with them
        if (testInstance.attestationType != AttestationType.TPM)
        {
            return;
        }

        if (testInstance.protocol != HTTPS)
        {
            //The test protocol has no bearing on this test since it only uses the provisioning service client, so the test should only run once.
            return;
        }

        SecurityProvider securityProvider = new SecurityProviderTPMEmulator(testInstance.registrationId, MAX_TPM_CONNECT_RETRY_ATTEMPTS);
        Attestation attestation = new TpmAttestation(new String(encodeBase64(((SecurityProviderTpm) securityProvider).getEndorsementKey())));
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(testInstance.registrationId, attestation);
        testInstance.provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);

        AttestationMechanism retrievedAttestationMechanism = testInstance.provisioningServiceClient.getIndividualEnrollmentAttestationMechanism(testInstance.registrationId);
        assertEquals(retrievedAttestationMechanism.getType(), AttestationMechanismType.TPM);
        assertTrue(retrievedAttestationMechanism.getAttestation() instanceof TpmAttestation);
        TpmAttestation retrievedTpmAttestation = (TpmAttestation) retrievedAttestationMechanism.getAttestation();
        assertNotNull(retrievedTpmAttestation.getEndorsementKey());
    }

    @ContinuousIntegrationTest
    @Test
    public void individualEnrollmentWithECCCertificates() throws Exception
    {
        if (testInstance.attestationType != AttestationType.X509)
        {
            // test is only relevant for x509 authentication
            return;
        }

        if (Tools.isAndroid())
        {
            // ECC cert generation is broken for Android. "ECDSA KeyPairGenerator is not available"
            return;
        }

        testInstance.certificateAlgorithm = X509CertificateGenerator.CertificateAlgorithm.ECC;
        customAllocationFlow(EnrollmentType.INDIVIDUAL);
    }

    private void enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType enrollmentType) throws Exception
    {
        if (enrollmentType == EnrollmentType.GROUP && testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            return; // test code not written for the x509 group scenario, and group enrollment does not support tpm attestation
        }

        boolean expectedExceptionEncountered = false;
        testInstance.securityProvider = getSecurityProviderInstance(enrollmentType);

        // Register identity
        try
        {
            registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpointWithInvalidCert, false, null, null, null);
        }
        catch (Exception | AssertionError e)
        {
            if (testInstance.protocol == HTTPS)
            {
                //SSLHandshakeException is buried in the message, not the cause, for HTTP
                if (e.getMessage().contains("SSLHandshakeException"))
                {
                    expectedExceptionEncountered = true;
                }
                else
                {
                    fail("Expected an SSLHandshakeException, but received " + e.getMessage());
                }
            }
            else if (testInstance.protocol == MQTT || testInstance.protocol == MQTT_WS)
            {
                if (Tools.isCause(SSLHandshakeException.class, e))
                {
                    expectedExceptionEncountered = true;
                }
                else
                {
                    fail("Expected an SSLHandshakeException, but received " + e.getMessage());
                }
            }
            else //amqp and amqps_ws
            {
                //Exception will never have any hint that it was due to SSL failure since proton-j only logs this issue, and closes the transport head.
                expectedExceptionEncountered = true;
            }
        }

        assertTrue("Expected an exception to be thrown due to invalid server certificates", expectedExceptionEncountered);
    }

    private void sendReportedPropertyUpdate(String expectedReportedPropertyName, String expectedReportedPropertyValue, String iothubUri, String deviceId) throws InterruptedException, IOException, URISyntaxException, TimeoutException, IotHubClientException
    {
        //hardcoded AMQP here only because we aren't testing this connection. We just need to open a connection to send a twin update so that
        // we can test if the twin updates carry over after reprovisioning
        DeviceClient deviceClient = new DeviceClient(iothubUri, deviceId, testInstance.securityProvider, IotHubClientProtocol.AMQPS);
        deviceClient.open(false);
        deviceClient.subscribeToDesiredProperties(
            (twin, context) ->
            {
                // don't care about handling desired properties for this test
            },
            null);

        TwinCollection twinCollection = new TwinCollection();
        twinCollection.put(expectedReportedPropertyName, expectedReportedPropertyValue);
        deviceClient.updateReportedProperties(twinCollection);
        deviceClient.close();
    }
}
