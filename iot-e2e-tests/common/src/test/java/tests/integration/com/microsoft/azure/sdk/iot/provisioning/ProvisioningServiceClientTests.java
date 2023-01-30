package tests.integration.com.microsoft.azure.sdk.iot.provisioning;

import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import org.junit.Before;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.X509CertificateGenerator;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.DeviceProvisioningServiceTest;

import java.util.UUID;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static tests.integration.com.microsoft.azure.sdk.iot.provisioning.setup.ProvisioningCommon.DPS_CONNECTION_STRING_ENV_VAR_NAME;

@DeviceProvisioningServiceTest
public class ProvisioningServiceClientTests
{
    public static final String provisioningServiceConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_ENV_VAR_NAME);

    // This is not an actual webhook that works for provisioning, but is fine for the purposes of these tests as
    // they never actually provision a device using this custom allocation policy webhook.
    public static final String customAllocationWebhookUrl = "https://www.microsoft.com";

    public ProvisioningServiceClient provisioningServiceClient = null;

    private static final String testPrefix = "provisioningservicecliente2etests-";

    @Before
    public void setUp()
    {
        provisioningServiceClient = new ProvisioningServiceClient(provisioningServiceConnectionString);
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
        String leafPublicPem = certificateGenerator.getPublicCertificatePEM();
        Attestation attestation = X509Attestation.createFromClientCertificates(leafPublicPem);
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(registrationId, attestation);
        provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);

        AttestationMechanism retrievedAttestationMechanism = provisioningServiceClient.getIndividualEnrollmentAttestationMechanism(registrationId);
        assertEquals(retrievedAttestationMechanism.getType(), AttestationMechanismType.X509);
        assertTrue(retrievedAttestationMechanism.getAttestation() instanceof X509Attestation);
        X509Attestation retrievedX509Attestation = (X509Attestation) retrievedAttestationMechanism.getAttestation();
        assertNotNull(retrievedX509Attestation.getClientCertificates());
        assertNotNull(retrievedX509Attestation.getClientCertificates().getPrimary());
        assertNotNull(retrievedX509Attestation.getClientCertificates().getPrimary().getInfo());
        assertNotNull(retrievedX509Attestation.getClientCertificates().getPrimary().getInfo().getSubjectName());
        assertTrue(retrievedX509Attestation.getClientCertificates().getPrimary().getInfo().getSubjectName().contains(registrationId));
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

    @Test
    public void createdEnrollmentGroupMatchesSentEnrollmentGroup() throws ProvisioningServiceClientException
    {
        String groupId = testPrefix + UUID.randomUUID();
        Attestation attestation = new SymmetricKeyAttestation("", "");
        EnrollmentGroup enrollment = new EnrollmentGroup(groupId, attestation);
        enrollment.setAllocationPolicy(AllocationPolicy.CUSTOM);
        CustomAllocationDefinition customAllocationDefinition = new CustomAllocationDefinition();
        customAllocationDefinition.setApiVersion("2018-09-01-preview");
        customAllocationDefinition.setWebhookUrl(customAllocationWebhookUrl);
        enrollment.setCustomAllocationDefinition(customAllocationDefinition);
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setUpdateHubAssignment(true);
        reprovisionPolicy.setMigrateDeviceData(true);
        enrollment.setReprovisionPolicy(reprovisionPolicy);
        DeviceCapabilities capabilities = new DeviceCapabilities();
        capabilities.setIotEdge(true);
        enrollment.setCapabilities(capabilities);
        EnrollmentGroup returnedEnrollment = provisioningServiceClient.createOrUpdateEnrollmentGroup(enrollment);

        assertEquals(enrollment.getEnrollmentGroupId(), returnedEnrollment.getEnrollmentGroupId());
        assertEquals(enrollment.getReprovisionPolicy().getMigrateDeviceData(), returnedEnrollment.getReprovisionPolicy().getMigrateDeviceData());
        assertEquals(enrollment.getReprovisionPolicy().getUpdateHubAssignment(), returnedEnrollment.getReprovisionPolicy().getUpdateHubAssignment());
        assertEquals(enrollment.getCapabilities().isIotEdge(), returnedEnrollment.getCapabilities().isIotEdge());
        assertEquals(enrollment.getAttestation().getClass(), returnedEnrollment.getAttestation().getClass());
        assertEquals(enrollment.getAllocationPolicy(), returnedEnrollment.getAllocationPolicy());
        assertEquals(enrollment.getCustomAllocationDefinition().getApiVersion(), returnedEnrollment.getCustomAllocationDefinition().getApiVersion());
    }

    @Test
    public void createdIndividualEnrollmentMatchesSentIndividualEnrollment() throws ProvisioningServiceClientException
    {
        String registrationId = testPrefix + UUID.randomUUID();
        Attestation attestation = new SymmetricKeyAttestation("", "");
        IndividualEnrollment enrollment = new IndividualEnrollment(registrationId, attestation);
        enrollment.setAllocationPolicy(AllocationPolicy.GEOLATENCY);
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setUpdateHubAssignment(true);
        reprovisionPolicy.setMigrateDeviceData(true);
        enrollment.setReprovisionPolicy(reprovisionPolicy);
        DeviceCapabilities capabilities = new DeviceCapabilities();
        capabilities.setIotEdge(true);
        enrollment.setCapabilities(capabilities);
        IndividualEnrollment returnedEnrollment = provisioningServiceClient.createOrUpdateIndividualEnrollment(enrollment);

        assertEquals(enrollment.getRegistrationId(), returnedEnrollment.getRegistrationId());
        assertEquals(enrollment.getReprovisionPolicy().getMigrateDeviceData(), returnedEnrollment.getReprovisionPolicy().getMigrateDeviceData());
        assertEquals(enrollment.getReprovisionPolicy().getUpdateHubAssignment(), returnedEnrollment.getReprovisionPolicy().getUpdateHubAssignment());
        assertEquals(enrollment.getCapabilities().isIotEdge(), returnedEnrollment.getCapabilities().isIotEdge());
        assertEquals(enrollment.getAttestation().getClass(), returnedEnrollment.getAttestation().getClass());
        assertEquals(enrollment.getAllocationPolicy(), returnedEnrollment.getAllocationPolicy());
    }
}
