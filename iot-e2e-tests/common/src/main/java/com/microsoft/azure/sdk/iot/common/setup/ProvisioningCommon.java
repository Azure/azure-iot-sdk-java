/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.common.setup;

import com.microsoft.azure.sdk.iot.common.helpers.IntegrationTest;
import com.microsoft.azure.sdk.iot.common.helpers.MessageAndResult;
import com.microsoft.azure.sdk.iot.common.helpers.X509CertificateGenerator;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

public class ProvisioningCommon extends IntegrationTest
{
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

    public final IotHubClientProtocol [] iotHubClientProtocols = {IotHubClientProtocol.MQTT, IotHubClientProtocol.MQTT_WS, IotHubClientProtocol.AMQPS, IotHubClientProtocol.AMQPS_WS, IotHubClientProtocol.HTTPS};

    public static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    public static String iotHubConnectionString = "";

    public static final String FAR_AWAY_IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "FAR_AWAY_IOTHUB_CONNECTION_STRING";
    public static String farAwayIotHubConnectionString = "";

    public static final String CUSTOM_ALLOCATION_WEBHOOK_URL_VAR_NAME = "CUSTOM_ALLOCATION_POLICY_WEBHOOK";
    public static String customAllocationWebhookUrl = "";

    public static final String DPS_CONNECTION_STRING_ENV_VAR_NAME = "IOT_DPS_CONNECTION_STRING";
    public static String provisioningServiceConnectionString = "";

    public static final String DPS_CONNECTION_STRING_WITH_INVALID_CERT_ENV_VAR_NAME = "PROVISIONING_CONNECTION_STRING_INVALIDCERT";
    public static String provisioningServiceWithInvalidCertConnectionString = "";

    public static final String DPS_GLOBAL_ENDPOINT_ENV_VAR_NAME = "IOT_DPS_GLOBAL_ENDPOINT";
    public static String provisioningServiceGlobalEndpoint = "";

    public static final String DPS_GLOBAL_ENDPOINT_WITH_INVALID_CERT_ENV_VAR_NAME = "DPS_GLOBALDEVICEENDPOINT_INVALIDCERT";
    public static String provisioningServiceGlobalEndpointWithInvalidCert = "";

    public static final String DPS_ID_SCOPE_ENV_VAR_NAME = "IOT_DPS_ID_SCOPE";
    public static String provisioningServiceIdScope = "";

    public static final String TPM_SIMULATOR_IP_ADDRESS_ENV_NAME = "IOT_DPS_TPM_SIMULATOR_IP_ADDRESS"; // ip address of TPM simulator
    public static String tpmSimulatorIpAddress = "";

    public static final long MAX_TIME_TO_WAIT_FOR_REGISTRATION = 2 * 60 * 1000; // one registration could take up to 2 mins

    public static final long TPM_CONNECTION_TIMEOUT = 1 * 60 * 1000;

    public static final Integer IOTHUB_NUM_OF_MESSAGES_TO_SEND = 3; // milli secs of time to wait
    public final List<MessageAndResult> messagesToSendAndResultsExpected = new ArrayList<>();

    public static final String HMAC_SHA256 = "HmacSHA256";

    public ProvisioningServiceClient provisioningServiceClient = null;
    public RegistryManager registryManager = null;

    // How much to wait until a message makes it to the server, in milliseconds
    public static final Integer IOTHUB_MAX_SEND_TIMEOUT = 120000; // milli secs of time to wait

    //How many milliseconds between retry
    public static final Integer IOTHUB_RETRY_MILLISECONDS = 100;

    public static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;

    //Android tests need buffer between enrollment creation and device registration
    public static final int ENROLLMENT_PROPAGATION_DELAY_MS = 5000;

    //buffer between device registration, and using that device
    public static final int DEVICE_REGISTRATION_PROPAGATION_DELAY_MILLIS = 3000;

    //sending reported properties for twin operations takes some time to get the appropriate callback
    public static final int MAX_TWIN_PROPAGATION_WAIT_SECONDS = 60;

    @Parameterized.Parameters(name = "{0} using {1}")
    public static Collection inputs(boolean a, boolean b, boolean c) throws Exception
    {
        if (a && b && c)
        {
            return Arrays.asList(
                    new Object[][]
                            {
                                    {ProvisioningDeviceClientTransportProtocol.HTTPS, AttestationType.SYMMETRIC_KEY},
                                    {ProvisioningDeviceClientTransportProtocol.HTTPS, AttestationType.TPM},
                                    {ProvisioningDeviceClientTransportProtocol.HTTPS, AttestationType.X509},

                                    {ProvisioningDeviceClientTransportProtocol.MQTT, AttestationType.SYMMETRIC_KEY},
                                    //{ProvisioningDeviceClientTransportProtocol.MQTT, AttestationType.TPM}, NOT SUPPORTED BY SERVICE
                                    {ProvisioningDeviceClientTransportProtocol.MQTT, AttestationType.X509},

                                    {ProvisioningDeviceClientTransportProtocol.MQTT_WS, AttestationType.SYMMETRIC_KEY},
                                    //{ProvisioningDeviceClientTransportProtocol.MQTT_WS, AttestationType.TPM}, NOT SUPPORTED BY SERVICE
                                    {ProvisioningDeviceClientTransportProtocol.MQTT_WS, AttestationType.X509},

                                    {ProvisioningDeviceClientTransportProtocol.AMQPS, AttestationType.SYMMETRIC_KEY},
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS, AttestationType.TPM},
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS, AttestationType.X509},

                                    {ProvisioningDeviceClientTransportProtocol.AMQPS_WS, AttestationType.SYMMETRIC_KEY},
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS_WS, AttestationType.TPM},
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS_WS, AttestationType.X509}
                            }
            );

        }
        else if (a)
        {
            return Arrays.asList(
                    new Object[][]
                            {
                                    {ProvisioningDeviceClientTransportProtocol.HTTPS, AttestationType.SYMMETRIC_KEY},
                                    {ProvisioningDeviceClientTransportProtocol.HTTPS, AttestationType.TPM},
                                    {ProvisioningDeviceClientTransportProtocol.HTTPS, AttestationType.X509},
                            }
            );
        }
        else if (b)
        {
            return Arrays.asList(
                    new Object[][]
                            {
                                    {ProvisioningDeviceClientTransportProtocol.MQTT, AttestationType.SYMMETRIC_KEY},
                                    {ProvisioningDeviceClientTransportProtocol.MQTT, AttestationType.X509},

                                    {ProvisioningDeviceClientTransportProtocol.MQTT_WS, AttestationType.SYMMETRIC_KEY},
                            }
            );
        }
        else if (c)
        {
            return Arrays.asList(
                    new Object[][]
                            {
                                    {ProvisioningDeviceClientTransportProtocol.MQTT_WS, AttestationType.X509},

                                    {ProvisioningDeviceClientTransportProtocol.AMQPS, AttestationType.SYMMETRIC_KEY},
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS, AttestationType.TPM},
                            }
            );
        }
        else
        {
            return Arrays.asList(
                    new Object[][]
                            {
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS, AttestationType.X509},

                                    {ProvisioningDeviceClientTransportProtocol.AMQPS_WS, AttestationType.SYMMETRIC_KEY},
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS_WS, AttestationType.TPM},
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS_WS, AttestationType.X509}
                            }
            );
        }
    }

    public ProvisioningCommon(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        this.testInstance = new ProvisioningTestInstance(protocol, attestationType);
    }

    public ProvisioningTestInstance testInstance;

    public class ProvisioningTestInstance
    {
        public ProvisioningDeviceClientTransportProtocol protocol;
        public AttestationType attestationType;
        public String groupId;
        public IndividualEnrollment individualEnrollment;
        public EnrollmentGroup enrollmentGroup;
        public String registrationId;
        public String provisionedDeviceId;
        public SecurityProvider securityProvider;

        public ProvisioningTestInstance(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
        {
            this.protocol = protocol;
            this.attestationType = attestationType;
            this.groupId = "";// by default, assume enrollment has no group id
            this.registrationId = "java-provisioning-test-" + this.attestationType.toString().toLowerCase().replace("_", "-") + "-" + UUID.randomUUID().toString();
        }
    }

    @Before
    public void setUp() throws Exception
    {
        provisioningServiceClient =
                ProvisioningServiceClient.createFromConnectionString(provisioningServiceConnectionString);

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        for (int i = 0; i < IOTHUB_NUM_OF_MESSAGES_TO_SEND; i++)
        {
            messagesToSendAndResultsExpected.add(new MessageAndResult(new Message("Java client e2e test message"), IotHubStatusCode.OK_EMPTY));
        }
    }

    @After
    public void tearDown()
    {
        try
        {
            Thread.sleep(INTERTEST_GUARDIAN_DELAY_MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            fail("Unexpected exception encountered");
        }

        registryManager.close();
        provisioningServiceClient = null;
        registryManager = null;

        if (testInstance.securityProvider != null && testInstance.securityProvider instanceof SecurityProviderTPMEmulator)
        {
            try
            {
                ((SecurityProviderTPMEmulator) testInstance.securityProvider).shutDown();
            }
            catch (SecurityProviderException e)
            {
                e.printStackTrace();
                fail("Failed to shutdown the tpm security provider emulator");
            }
        }
    }

    public class ProvisioningStatus
    {
        public ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationInfoClient = new ProvisioningDeviceClientRegistrationResult();
        public Exception exception;
        public ProvisioningDeviceClient provisioningDeviceClient;
    }

    public class ProvisioningDeviceClientRegistrationCallbackImpl implements ProvisioningDeviceClientRegistrationCallback
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
                System.out.println("Received unknown context");
            }
        }
    }

    public void waitForRegistrationCallback(ProvisioningStatus provisioningStatus) throws Exception
    {
        long startTime = System.currentTimeMillis();
        while (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() != PROVISIONING_DEVICE_STATUS_ASSIGNED)
        {
            if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ERROR ||
                    provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_DISABLED ||
                    provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_FAILED)
            {
                provisioningStatus.exception.printStackTrace();
                System.out.println("Registration error, bailing out");
                throw new Exception(provisioningStatus.exception);
            }
            System.out.println("Waiting for Provisioning Service to register");

            Thread.sleep(2000);

            if (System.currentTimeMillis() - startTime > MAX_TIME_TO_WAIT_FOR_REGISTRATION)
            {
                fail("Timed out waiting for registration to succeed");
            }
        }

        assertEquals(PROVISIONING_DEVICE_STATUS_ASSIGNED, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus());
        assertNotNull(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
        assertNotNull(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());
    }

    public ProvisioningStatus registerDevice(ProvisioningDeviceClientTransportProtocol protocol, SecurityProvider securityProvider, String globalEndpoint) throws Exception
    {
        ProvisioningStatus provisioningStatus = new ProvisioningStatus();

        ProvisioningDeviceClient provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, provisioningServiceIdScope,
                protocol,
                securityProvider);
        provisioningStatus.provisioningDeviceClient = provisioningDeviceClient;
        provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus);

        waitForRegistrationCallback(provisioningStatus);
        provisioningStatus.provisioningDeviceClient.closeNow();

        Thread.sleep(DEVICE_REGISTRATION_PROPAGATION_DELAY_MILLIS);


        return provisioningStatus;
    }

    //Parses connection String to retrieve iothub hostname
    public String getHostName(String connectionString)
    {
        String[] tokens = connectionString.split(";");
        for (String token: tokens)
        {
            if (token.contains("HostName"))
            {
                String[] hName = token.split("=");
                return hName[1];
            }
        }

        return null;
    }

    public SecurityProvider getSecurityProviderInstance(EnrollmentType enrollmentType) throws ProvisioningServiceClientException, GeneralSecurityException, IOException, SecurityProviderException, InterruptedException
    {
        return getSecurityProviderInstance(enrollmentType, null, null, null, null);
    }

    public SecurityProvider getSecurityProviderInstance(EnrollmentType enrollmentType, AllocationPolicy allocationPolicy, ReprovisionPolicy reprovisionPolicy, CustomAllocationDefinition customAllocationDefinition, List<String> iothubs) throws ProvisioningServiceClientException, GeneralSecurityException, IOException, SecurityProviderException, InterruptedException
    {
        SecurityProvider securityProvider = null;
        TwinCollection tags = new TwinCollection();
        final String TEST_KEY_TAG = "testTag";
        final String TEST_VALUE_TAG = "testValue";
        tags.put(TEST_KEY_TAG, TEST_VALUE_TAG);

        final String TEST_KEY_DP = "testDP";
        final String TEST_VALUE_DP = "testDPValue";
        TwinCollection desiredProperties = new TwinCollection();
        desiredProperties.put(TEST_KEY_DP, TEST_VALUE_DP);

        TwinState twinState = new TwinState(tags, desiredProperties);

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
                testInstance.enrollmentGroup.setInitialTwinFinal(twinState);
                testInstance.enrollmentGroup.setAllocationPolicy(allocationPolicy);
                testInstance.enrollmentGroup.setReprovisionPolicy(reprovisionPolicy);
                testInstance.enrollmentGroup.setCustomAllocationDefinition(customAllocationDefinition);
                testInstance.enrollmentGroup.setIotHubs(iothubs);
                testInstance.enrollmentGroup = provisioningServiceClient.createOrUpdateEnrollmentGroup(testInstance.enrollmentGroup);
                Attestation attestation = testInstance.enrollmentGroup.getAttestation();
                assertTrue(attestation instanceof SymmetricKeyAttestation);

                assertNotNull(testInstance.enrollmentGroup.getInitialTwin());
                assertEquals(TEST_VALUE_TAG, testInstance.enrollmentGroup.getInitialTwin().getTags().get(TEST_KEY_TAG));
                assertEquals(TEST_VALUE_DP, testInstance.enrollmentGroup.getInitialTwin().getDesiredProperty().get(TEST_KEY_DP));

                SymmetricKeyAttestation symmetricKeyAttestation = (SymmetricKeyAttestation) attestation;
                byte[] derivedPrimaryKey = ComputeDerivedSymmetricKey(symmetricKeyAttestation.getPrimaryKey(), testInstance.registrationId);
                securityProvider = new SecurityProviderSymmetricKey(derivedPrimaryKey, testInstance.registrationId);
            }
        }
        else if (enrollmentType == EnrollmentType.INDIVIDUAL)
        {
            testInstance.provisionedDeviceId = "Some-Provisioned-Device-" + testInstance.attestationType + "-" +UUID.randomUUID().toString();
            if (testInstance.attestationType == AttestationType.TPM)
            {
                securityProvider = new SecurityProviderTPMEmulator(testInstance.registrationId);
                Attestation attestation = new TpmAttestation(new String(com.microsoft.azure.sdk.iot.deps.util.Base64.encodeBase64Local(((SecurityProviderTpm) securityProvider).getEndorsementKey())));
                createTestIndividualEnrollment(attestation, allocationPolicy, reprovisionPolicy, customAllocationDefinition, iothubs, twinState);
            }
            else if (testInstance.attestationType == AttestationType.X509)
            {
                X509CertificateGenerator certificateGenerator = new X509CertificateGenerator(testInstance.registrationId);
                String leafPublicPem = certificateGenerator.getPublicCertificate();
                String leafPrivateKey = certificateGenerator.getPrivateKey();

                Collection<String> signerCertificates = new LinkedList<>();
                Attestation attestation = X509Attestation.createFromClientCertificates(leafPublicPem);
                createTestIndividualEnrollment(attestation, allocationPolicy, reprovisionPolicy, customAllocationDefinition, iothubs, twinState);
                securityProvider = new SecurityProviderX509Cert(leafPublicPem, leafPrivateKey, signerCertificates);
            }
            else if (testInstance.attestationType == AttestationType.SYMMETRIC_KEY)
            {
                Attestation attestation = new SymmetricKeyAttestation(null, null);
                createTestIndividualEnrollment(attestation, allocationPolicy, reprovisionPolicy, customAllocationDefinition, iothubs, twinState);
                assertTrue(testInstance.individualEnrollment.getAttestation() instanceof  SymmetricKeyAttestation);
                SymmetricKeyAttestation symmetricKeyAttestation = (SymmetricKeyAttestation) testInstance.individualEnrollment.getAttestation();
                securityProvider = new SecurityProviderSymmetricKey(symmetricKeyAttestation.getPrimaryKey().getBytes(), testInstance.registrationId);
            }

            assertEquals(testInstance.provisionedDeviceId, testInstance.individualEnrollment.getDeviceId());
            assertNotNull(testInstance.individualEnrollment.getInitialTwin());
            assertEquals(TEST_VALUE_TAG, testInstance.individualEnrollment.getInitialTwin().getTags().get(TEST_KEY_TAG));
            assertEquals(TEST_VALUE_DP, testInstance.individualEnrollment.getInitialTwin().getDesiredProperty().get(TEST_KEY_DP));
        }

        //allow service extra time before security provider can be used
        Thread.sleep(ENROLLMENT_PROPAGATION_DELAY_MS);

        return securityProvider;
    }

    private void createTestIndividualEnrollment(Attestation attestation, AllocationPolicy allocationPolicy, ReprovisionPolicy reprovisionPolicy, CustomAllocationDefinition customAllocationDefinition, List<String> iothubs, TwinState twinState) throws ProvisioningServiceClientException
    {
        testInstance.individualEnrollment = new IndividualEnrollment(testInstance.registrationId, attestation);
        testInstance.individualEnrollment.setDeviceIdFinal(testInstance.provisionedDeviceId);
        testInstance.individualEnrollment.setAllocationPolicy(allocationPolicy);
        testInstance.individualEnrollment.setReprovisionPolicy(reprovisionPolicy);
        testInstance.individualEnrollment.setCustomAllocationDefinition(customAllocationDefinition);
        testInstance.individualEnrollment.setIotHubs(iothubs);
        testInstance.individualEnrollment.setInitialTwin(twinState);
        testInstance.individualEnrollment = provisioningServiceClient.createOrUpdateIndividualEnrollment(testInstance.individualEnrollment);
    }

    public static byte[] ComputeDerivedSymmetricKey(String masterKey, String registrationId) throws InvalidKeyException, NoSuchAlgorithmException
    {
        byte[] masterKeyBytes = com.microsoft.azure.sdk.iot.deps.util.Base64.decodeBase64Local(masterKey.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKey = new SecretKeySpec(masterKeyBytes, HMAC_SHA256);
        Mac hMacSha256 = Mac.getInstance(HMAC_SHA256);
        hMacSha256.init(secretKey);
        return com.microsoft.azure.sdk.iot.deps.util.Base64.encodeBase64Local(hMacSha256.doFinal(registrationId.getBytes()));
    }
}
