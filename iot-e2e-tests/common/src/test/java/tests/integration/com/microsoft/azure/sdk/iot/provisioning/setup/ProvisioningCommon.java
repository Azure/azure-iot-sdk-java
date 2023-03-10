/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.provisioning.setup;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceHubException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED;
import static junit.framework.TestCase.fail;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.junit.Assert.*;

@Slf4j
public class ProvisioningCommon extends IntegrationTest
{
    // Called from JVM runner
    @Parameterized.Parameters(name = "{0}_{1}")
    public static Collection inputs()
    {
        getEnvironmentVariables();

        // Intentionally not doing TPM tests here. There is a separate class for running those
        // tests in serial
        return Arrays.asList(
            new Object[][]
                {
                    {ProvisioningDeviceClientTransportProtocol.HTTPS, AttestationType.X509},
                    {ProvisioningDeviceClientTransportProtocol.AMQPS, AttestationType.X509},
                    {ProvisioningDeviceClientTransportProtocol.MQTT, AttestationType.X509},

                    {ProvisioningDeviceClientTransportProtocol.HTTPS, AttestationType.SYMMETRIC_KEY},
                    {ProvisioningDeviceClientTransportProtocol.AMQPS, AttestationType.SYMMETRIC_KEY},
                    {ProvisioningDeviceClientTransportProtocol.MQTT, AttestationType.SYMMETRIC_KEY},
                });
    }

    // Called from android runner to only run this test suite for a particular attestation type
    public static Collection inputs(AttestationType attestationType)
    {
        getEnvironmentVariables();

        if (attestationType == AttestationType.SYMMETRIC_KEY || attestationType == AttestationType.X509)
        {
            return Arrays.asList(
                new Object[][]
                    {
                        {ProvisioningDeviceClientTransportProtocol.HTTPS, attestationType},
                        {ProvisioningDeviceClientTransportProtocol.AMQPS, attestationType},
                        {ProvisioningDeviceClientTransportProtocol.MQTT, attestationType},
                    });
        }
        else if (attestationType == AttestationType.TPM)
        {
            return Collections.emptyList(); // TPM tests are run in the ProvisioningTPMTests file so they can be run in serial
        }
        else
        {
            throw new IllegalArgumentException("Unknown attestation type provided");
        }
    }

    private static void getEnvironmentVariables()
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        provisioningServiceConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_ENV_VAR_NAME);
        provisioningServiceIdScope = Tools.retrieveEnvironmentVariableValue(DPS_ID_SCOPE_ENV_VAR_NAME);
        provisioningServiceGlobalEndpointWithInvalidCert = Tools.retrieveEnvironmentVariableValue(DPS_GLOBAL_ENDPOINT_WITH_INVALID_CERT_ENV_VAR_NAME);
        provisioningServiceWithInvalidCertConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_WITH_INVALID_CERT_ENV_VAR_NAME);
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));
    }

    public enum AttestationType
    {
        X509,
        TPM,
        SYMMETRIC_KEY
    }

    public enum EnrollmentType
    {
        INDIVIDUAL,
        GROUP
    }

    public static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    public static String iotHubConnectionString = "";

    public static final String DPS_CONNECTION_STRING_ENV_VAR_NAME = "IOT_DPS_CONNECTION_STRING";
    public static String provisioningServiceConnectionString = "";

    public static final String DPS_CONNECTION_STRING_WITH_INVALID_CERT_ENV_VAR_NAME = "PROVISIONING_CONNECTION_STRING_INVALIDCERT";
    public static String provisioningServiceWithInvalidCertConnectionString = "";

    public static String provisioningServiceGlobalEndpoint = "global.azure-devices-provisioning.net";

    public static final String DPS_GLOBAL_ENDPOINT_WITH_INVALID_CERT_ENV_VAR_NAME = "DPS_GLOBALDEVICEENDPOINT_INVALIDCERT";
    public static String provisioningServiceGlobalEndpointWithInvalidCert = "";

    public static final String DPS_ID_SCOPE_ENV_VAR_NAME = "IOT_DPS_ID_SCOPE";
    public static String provisioningServiceIdScope = "";

    public static final long MAX_TIME_TO_WAIT_FOR_REGISTRATION_SECONDS = 60;

    public static final int MAX_TPM_CONNECT_RETRY_ATTEMPTS = 10;

    public RegistryClient registryClient = null;

    public ProvisioningCommon(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        this.testInstance = new ProvisioningTestInstance(protocol, attestationType);
    }

    public ProvisioningTestInstance testInstance;

    public static class ProvisioningTestInstance
    {
        public ProvisioningDeviceClientTransportProtocol protocol;
        public AttestationType attestationType;
        public String groupId;
        public IndividualEnrollment individualEnrollment;
        public EnrollmentGroup enrollmentGroup;
        public String registrationId;
        public String provisionedDeviceId;
        public SecurityProvider securityProvider;
        public String provisionedIotHubUri;
        public ProvisioningServiceClient provisioningServiceClient;
        public X509CertificateGenerator.CertificateAlgorithm certificateAlgorithm;

        public ProvisioningTestInstance(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
        {
            this.protocol = protocol;
            this.attestationType = attestationType;
            this.groupId = "";// by default, assume enrollment has no group id
            this.registrationId = "java-provisioning-test-" + this.attestationType.toString().toLowerCase().replace("_", "-") + "-" + UUID.randomUUID().toString();
            this.provisioningServiceClient =
                    new ProvisioningServiceClient(provisioningServiceConnectionString);

            this.certificateAlgorithm = X509CertificateGenerator.CertificateAlgorithm.RSA;
        }
    }

    @Before
    public void setUp() throws Exception
    {
        registryClient = new RegistryClient(iotHubConnectionString, RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());

        this.testInstance = new ProvisioningTestInstance(this.testInstance.protocol, this.testInstance.attestationType);
    }

    @After
    public void tearDown()
    {
        registryClient = null;

        if (testInstance != null && testInstance.securityProvider != null && testInstance.securityProvider instanceof SecurityProviderTPMEmulator)
        {
            try
            {
                //TPM security provider MUST be shutdown between tests
                ((SecurityProviderTPMEmulator) testInstance.securityProvider).shutDown();
            }
            catch (SecurityProviderException e)
            {
                e.printStackTrace();
                fail("Failed to shutdown the tpm security provider emulator");
            }
        }

        try
        {
            if (testInstance != null && testInstance.groupId != null && !testInstance.groupId.isEmpty())
            {
                log.debug("Deleting enrollment group with group Id {}", testInstance.groupId);
                testInstance.provisioningServiceClient.deleteEnrollmentGroup(testInstance.groupId);
            }

            if (testInstance != null && testInstance.individualEnrollment != null)
            {
                log.debug("Deleting individual enrollment with registration Id {}", testInstance.individualEnrollment.getRegistrationId());
                testInstance.provisioningServiceClient.deleteIndividualEnrollment(testInstance.individualEnrollment.getRegistrationId());
            }
        }
        catch (Exception e)
        {
            log.error("Failed to clean up enrollments after test run", e);
        }
    }

    public void registerDevice(ProvisioningDeviceClientTransportProtocol protocol, SecurityProvider securityProvider, String globalEndpoint) throws Exception
    {
        ProvisioningDeviceClient provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, provisioningServiceIdScope,
            protocol,
            securityProvider);

        try
        {

            final CountDownLatch registrationLatch = new CountDownLatch(1);
            AtomicReference<ProvisioningDeviceClientRegistrationResult> registrationResultReference = new AtomicReference<>();
            AtomicReference<Exception> registrationExceptionReference = new AtomicReference<>();
            provisioningDeviceClient.registerDevice(
                (provisioningDeviceClientRegistrationResult, e, context) ->
                {
                    registrationResultReference.set(provisioningDeviceClientRegistrationResult);
                    registrationExceptionReference.set(e);
                    registrationLatch.countDown();
                },
                null);

            // Wait until registration finishes or for a max amount of time
            boolean timedOut = !registrationLatch.await(MAX_TIME_TO_WAIT_FOR_REGISTRATION_SECONDS, TimeUnit.SECONDS);
            if (timedOut)
            {
                fail("Timed out waiting for device registration to complete.");
            }

            ProvisioningDeviceClientRegistrationResult registrationResult = registrationResultReference.get();
            Exception registrationException = registrationExceptionReference.get();

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

            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected status", Tools.getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), PROVISIONING_DEVICE_STATUS_ASSIGNED, registrationResult.getProvisioningDeviceClientStatus());
            testInstance.provisionedDeviceId = registrationResult.getDeviceId();
            testInstance.provisionedIotHubUri = registrationResult.getIothubUri();

            assertEquals(registrationResult.getProvisioningDeviceClientStatus(), PROVISIONING_DEVICE_STATUS_ASSIGNED);
            assertEquals(registrationResult.getRegistrationId(), testInstance.registrationId);
            assertNotNull(registrationResult.getCreatedDateTimeUtc());
            assertNotNull(registrationResult.getLastUpdatesDateTimeUtc());
            assertNotNull(registrationResult.getLastUpdatesDateTimeUtc());
            assertNotNull(registrationResult.getSubstatus());

            assertNotNull(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Expected a device id", Tools.getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.provisionedDeviceId);
            assertFalse(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Expected a device id", Tools.getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.provisionedDeviceId.isEmpty());
            assertNotNull(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Expected uri", Tools.getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.provisionedIotHubUri);
            assertFalse(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Expected uri", Tools.getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.provisionedIotHubUri.isEmpty());

            assertSame(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected status", Tools.getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), registrationResult.getProvisioningDeviceClientStatus(), PROVISIONING_DEVICE_STATUS_ASSIGNED);
            assertFalse(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected deviceId", Tools.getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), registrationResult.getDeviceId().isEmpty());
            assertFalse(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected uri", Tools.getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), registrationResult.getIothubUri().isEmpty());
            assertProvisionedDeviceWorks(registrationResult.getIothubUri(), registrationResult.getDeviceId());
        }
        finally
        {
            provisioningDeviceClient.close();
        }
    }

    private void assertProvisionedDeviceWorks(String iothubUri, String deviceId) throws IOException, IotHubClientException, URISyntaxException
    {
        DeviceClient deviceClient = new DeviceClient(iothubUri, deviceId, testInstance.securityProvider, IotHubClientProtocol.MQTT);
        deviceClient.open(true);
        deviceClient.close();
    }

    public SecurityProvider getSecurityProviderInstance(EnrollmentType enrollmentType) throws ProvisioningServiceClientException, GeneralSecurityException, SecurityProviderException, IOException
    {
        SecurityProvider securityProvider = null;

        if (enrollmentType == EnrollmentType.GROUP)
        {
            if (testInstance.attestationType == AttestationType.TPM)
            {
                throw new UnsupportedOperationException("Group enrollments cannot use tpm attestation");
            }
            else if (testInstance.attestationType == AttestationType.X509)
            {
                throw new UnsupportedOperationException("Test code hasn't been written to test Group x509 enrollments yet");
            }
            else if (testInstance.attestationType == AttestationType.SYMMETRIC_KEY)
            {
                testInstance.groupId = "java-provisioning-test-group-id-" + testInstance.attestationType.toString().toLowerCase().replace("_", "-") + "-" + UUID.randomUUID().toString();

                testInstance.enrollmentGroup = new EnrollmentGroup(testInstance.groupId, new SymmetricKeyAttestation(null, null));
                testInstance.enrollmentGroup = testInstance.provisioningServiceClient.createOrUpdateEnrollmentGroup(testInstance.enrollmentGroup);
                Attestation attestation = testInstance.enrollmentGroup.getAttestation();
                assertTrue(attestation instanceof SymmetricKeyAttestation);

                SymmetricKeyAttestation symmetricKeyAttestation = (SymmetricKeyAttestation) attestation;
                byte[] derivedPrimaryKey = SecurityProviderSymmetricKey.ComputeDerivedSymmetricKey(symmetricKeyAttestation.getPrimaryKey().getBytes(StandardCharsets.UTF_8), testInstance.registrationId);
                securityProvider = new SecurityProviderSymmetricKey(derivedPrimaryKey, testInstance.registrationId);
            }
        }
        else if (enrollmentType == EnrollmentType.INDIVIDUAL)
        {
            testInstance.provisionedDeviceId = "Some-Provisioned-Device-" + testInstance.attestationType + "-" +UUID.randomUUID().toString();
            if (testInstance.attestationType == AttestationType.TPM)
            {
                securityProvider = new SecurityProviderTPMEmulator(testInstance.registrationId, MAX_TPM_CONNECT_RETRY_ATTEMPTS);
                Attestation attestation = new TpmAttestation(new String(encodeBase64(((SecurityProviderTpm) securityProvider).getEndorsementKey())));
                createTestIndividualEnrollment(attestation);
            }
            else if (testInstance.attestationType == AttestationType.X509)
            {
                X509CertificateGenerator certificateGenerator = new X509CertificateGenerator(testInstance.certificateAlgorithm, testInstance.registrationId);
                String leafPublicPem = certificateGenerator.getPublicCertificatePEM();

                Collection<X509Certificate> signerCertificates = new LinkedList<>();
                Attestation attestation = X509Attestation.createFromClientCertificates(leafPublicPem);
                createTestIndividualEnrollment(attestation);

                securityProvider = new SecurityProviderX509Cert(certificateGenerator.getX509Certificate(), certificateGenerator.getPrivateKey(), signerCertificates);
            }
            else if (testInstance.attestationType == AttestationType.SYMMETRIC_KEY)
            {
                Attestation attestation = new SymmetricKeyAttestation(null, null);
                createTestIndividualEnrollment(attestation);
                assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Expected symmetric key attestation", Tools.getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.individualEnrollment.getAttestation() instanceof  SymmetricKeyAttestation);
                SymmetricKeyAttestation symmetricKeyAttestation = (SymmetricKeyAttestation) testInstance.individualEnrollment.getAttestation();
                securityProvider = new SecurityProviderSymmetricKey(symmetricKeyAttestation.getPrimaryKey().getBytes(StandardCharsets.UTF_8), testInstance.registrationId);
            }

            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected device id assigned", Tools.getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.provisionedDeviceId, testInstance.individualEnrollment.getDeviceId());
        }

        return securityProvider;
    }

    private void createTestIndividualEnrollment(Attestation attestation) throws ProvisioningServiceClientException
    {
        testInstance.individualEnrollment = new IndividualEnrollment(testInstance.registrationId, attestation);
        testInstance.individualEnrollment.setDeviceId(testInstance.provisionedDeviceId);
        testInstance.individualEnrollment = testInstance.provisioningServiceClient.createOrUpdateIndividualEnrollment(testInstance.individualEnrollment);
    }
}
