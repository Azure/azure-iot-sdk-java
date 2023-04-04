/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.provisioning;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.X509CertificateGenerator;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.DeviceProvisioningServiceTest;
import tests.integration.com.microsoft.azure.sdk.iot.provisioning.setup.ProvisioningCommon;

import javax.net.ssl.SSLHandshakeException;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol.*;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@DeviceProvisioningServiceTest
@RunWith(Parameterized.class)
public class ProvisioningTests extends ProvisioningCommon
{
    public ProvisioningTests(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        super(protocol, attestationType);
    }

    @Test
    @ContinuousIntegrationTest
    public void individualEnrollmentRegistration() throws Exception
    {
        basicRegistrationFlow(EnrollmentType.INDIVIDUAL);
    }

    @Test
    @ContinuousIntegrationTest
    public void enrollmentGroupRegistration() throws Exception
    {
        // test code not written for the x509 group scenario, and enrollment groups don't support tpm attestation
        assumeTrue("Skipping this test because only Symmetric Key attestion can be tested for enrollment groups",
            this.testInstance.attestationType == AttestationType.SYMMETRIC_KEY);

        basicRegistrationFlow(EnrollmentType.GROUP);
    }

    @Test
    @ContinuousIntegrationTest
    public void individualEnrollmentWithInvalidRemoteServerCertificateFails() throws Exception
    {
        Assume.assumeTrue(Tools.isLinux());
        enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType.INDIVIDUAL);
    }

    @Test
    @ContinuousIntegrationTest
    public void groupEnrollmentWithInvalidRemoteServerCertificateFails() throws Exception
    {
        Assume.assumeTrue(Tools.isLinux());
        enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType.GROUP);
    }

    @Test
    public void individualEnrollmentGetAttestationMechanismTPM() throws ProvisioningServiceClientException, SecurityProviderException
    {
        //This test fits in better with the other provisioning service client tests, but it needs to be run sequentially
        // with the other TPM tests, so it lives here with them
        assumeTrue("Skipping because this test is only applicable to TPM attestation", testInstance.attestationType == AttestationType.TPM);

        //The test protocol has no bearing on this test since it only uses the provisioning service client, so the test should only run once.
        assumeTrue(testInstance.protocol == HTTPS);

        SecurityProviderTpm securityProvider = new SecurityProviderTPMEmulator(testInstance.registrationId, MAX_TPM_CONNECT_RETRY_ATTEMPTS);
        Attestation attestation = new TpmAttestation(new String(encodeBase64(securityProvider.getEndorsementKey())));
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
        assumeTrue("Skipping test because it is only applicable for x509 attestation", testInstance.attestationType == AttestationType.X509);

        // ECC cert generation is broken for Android. "ECDSA KeyPairGenerator is not available"
        assumeFalse("Skipping test because it is being run on Android", Tools.isAndroid());

        testInstance.certificateAlgorithm = X509CertificateGenerator.CertificateAlgorithm.ECC;
        basicRegistrationFlow(EnrollmentType.INDIVIDUAL);
    }

    private void basicRegistrationFlow(EnrollmentType enrollmentType) throws Exception
    {
        testInstance.securityProvider = getSecurityProviderInstance(enrollmentType);
        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, false, null, false, null);
    }

    private void basicRegistrationFlowUsingOperationalClientCertificate(EnrollmentType enrollmentType) throws Exception
    {
        testInstance.securityProvider = getSecurityProviderInstance(enrollmentType);
        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, false, null, true, null);
    }

    private void enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType enrollmentType) throws Exception
    {
        // test code not written for the x509 group scenario, and enrollment groups don't support tpm attestation
        assumeTrue("Skipping this test because only Symmetric Key attestion can be tested for enrollment groups",
            this.testInstance.attestationType == AttestationType.SYMMETRIC_KEY);

        boolean expectedExceptionEncountered = false;
        testInstance.securityProvider = getSecurityProviderInstance(enrollmentType);

        // Register identity
        try
        {
            registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpointWithInvalidCert, false, null, false, null);
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

    // TODO -- move these to ProvisioningCommon.java?
    private String GenerateClientCertKeyPairAndCsr(String registrationId)
    {
        return "";
    }

    private void CleanupCerts()
    {
        return;
    }
}
