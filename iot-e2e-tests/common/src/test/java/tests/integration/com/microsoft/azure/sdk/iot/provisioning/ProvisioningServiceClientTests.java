package tests.integration.com.microsoft.azure.sdk.iot.provisioning;

import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import org.junit.Before;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.X509CertificateGenerator;

import java.util.UUID;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ProvisioningServiceClientTests
{
    public static final String DPS_CONNECTION_STRING_ENV_VAR_NAME = "IOT_DPS_CONNECTION_STRING";
    public static String provisioningServiceConnectionString = "";

    public ProvisioningServiceClient provisioningServiceClient = null;

    private static final String testPrefix = "provisioningservicecliente2etests-";

    @Before
    public void setUp()
    {
        provisioningServiceConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_ENV_VAR_NAME);
        provisioningServiceClient = ProvisioningServiceClient.createFromConnectionString(provisioningServiceConnectionString);
    }

    @Test
    public void individualEnrollmentGetAttestationMechanismSymmetricKey() throws ProvisioningServiceClientException
    {
        String registrationId = testPrefix + UUID.randomUUID();
        Attestation attestation = new SymmetricKeyAttestation("", "");
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(registrationId, attestation);
        provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);

        AttestationMechanism retrievedAttestationMechanism = provisioningServiceClient.getIndividualEnrollmentAttestationMechanism(registrationId);
        assertEquals(retrievedAttestationMechanism.getType(), AttestationMechanismType.SYMMETRIC_KEY);
        assertTrue(retrievedAttestationMechanism.getAttestation() instanceof SymmetricKeyAttestation);
        SymmetricKeyAttestation retrievedSymmetricKeyAttestation = (SymmetricKeyAttestation) retrievedAttestationMechanism.getAttestation();
        assertNotNull(retrievedSymmetricKeyAttestation.getPrimaryKey());
        assertNotNull(retrievedSymmetricKeyAttestation.getSecondaryKey());
        assertTrue(retrievedSymmetricKeyAttestation.getPrimaryKey().length() > 0);
        assertTrue(retrievedSymmetricKeyAttestation.getSecondaryKey().length() > 0);
    }

    @Test
    public void individualEnrollmentGetAttestationMechanismX509() throws ProvisioningServiceClientException
    {
        String registrationId = testPrefix + UUID.randomUUID();
        X509CertificateGenerator certificateGenerator = new X509CertificateGenerator(registrationId);
        String leafPublicPem = certificateGenerator.getPublicCertificate();
        Attestation attestation = X509Attestation.createFromClientCertificates(leafPublicPem);
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(registrationId, attestation);
        provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);

        AttestationMechanism retrievedAttestationMechanism = provisioningServiceClient.getIndividualEnrollmentAttestationMechanism(registrationId);
        assertEquals(retrievedAttestationMechanism.getType(), AttestationMechanismType.X509);
        assertTrue(retrievedAttestationMechanism.getAttestation() instanceof X509Attestation);
        X509Attestation retrievedX509Attestation = (X509Attestation) retrievedAttestationMechanism.getAttestation();
        assertNotNull(retrievedX509Attestation.getClientCertificatesFinal());
        assertNotNull(retrievedX509Attestation.getClientCertificatesFinal().getPrimaryFinal());
        assertNotNull(retrievedX509Attestation.getClientCertificatesFinal().getPrimaryFinal().getInfo());
        assertNotNull(retrievedX509Attestation.getClientCertificatesFinal().getPrimaryFinal().getInfo().getSubjectName());
        assertTrue(retrievedX509Attestation.getClientCertificatesFinal().getPrimaryFinal().getInfo().getSubjectName().contains(registrationId));
    }

    @Test
    public void enrollmentGroupGetAttestationMechanismSymmetricKey() throws ProvisioningServiceClientException
    {
        String groupId = testPrefix + UUID.randomUUID();
        Attestation attestation = new SymmetricKeyAttestation("", "");
        EnrollmentGroup enrollment = new EnrollmentGroup(groupId, attestation);
        provisioningServiceClient.createOrUpdateEnrollmentGroup(enrollment);

        AttestationMechanism retrievedAttestationMechanism = provisioningServiceClient.getEnrollmentGroupAttestationMechanism(groupId);
        assertEquals(retrievedAttestationMechanism.getType(), AttestationMechanismType.SYMMETRIC_KEY);
        assertTrue(retrievedAttestationMechanism.getAttestation() instanceof SymmetricKeyAttestation);
        SymmetricKeyAttestation retrievedSymmetricKeyAttestation = (SymmetricKeyAttestation) retrievedAttestationMechanism.getAttestation();
        assertNotNull(retrievedSymmetricKeyAttestation.getPrimaryKey());
        assertNotNull(retrievedSymmetricKeyAttestation.getSecondaryKey());
        assertTrue(retrievedSymmetricKeyAttestation.getPrimaryKey().length() > 0);
        assertTrue(retrievedSymmetricKeyAttestation.getSecondaryKey().length() > 0);
    }
}
