/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.common.setup;

import com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert;
import com.microsoft.azure.sdk.iot.common.helpers.IntegrationTest;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.helpers.X509CertificateGenerator;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
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
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup;
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

    public static final long MAX_TIME_TO_WAIT_FOR_REGISTRATION = 30 * 1000;

    public static final String HMAC_SHA256 = "HmacSHA256";

    public static final int MAX_TPM_CONNECT_RETRY_ATTEMPTS = 10;

    protected static final String CUSTOM_ALLOCATION_WEBHOOK_API_VERSION = "2019-03-31";

    public ProvisioningServiceClient provisioningServiceClient = null;
    public RegistryManager registryManager = null;

    public static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;

    //sending reported properties for twin operations takes some time to get the appropriate callback
    public static final int MAX_TWIN_PROPAGATION_WAIT_SECONDS = 60;

    @Parameterized.Parameters(name = "{0} using {1}")
    public static Collection inputs(AttestationType attestationType) throws Exception
    {
        if (attestationType == AttestationType.SYMMETRIC_KEY || attestationType == AttestationType.X509)
        {
            return Arrays.asList(
                    new Object[][]
                            {
                                    {ProvisioningDeviceClientTransportProtocol.HTTPS, attestationType},
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS, attestationType},
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS_WS, attestationType},
                                    {ProvisioningDeviceClientTransportProtocol.MQTT, attestationType},
                                    {ProvisioningDeviceClientTransportProtocol.MQTT_WS, attestationType}
                            });
        }
        else if (attestationType == AttestationType.TPM)
        {
            return Arrays.asList(
                    new Object[][]
                            {
                                    {ProvisioningDeviceClientTransportProtocol.HTTPS, attestationType},
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS, attestationType},
                                    {ProvisioningDeviceClientTransportProtocol.AMQPS_WS, attestationType}

                                    //MQTT/MQTT_WS does not support tpm attestation
                                    //{ProvisioningDeviceClientTransportProtocol.MQTT, attestationType},
                                    //{ProvisioningDeviceClientTransportProtocol.MQTT_WS, attestationType},
                            });
        }
        else
        {
            throw new IllegalArgumentException("Unknown attestation type provided");
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
        public String provisionedIotHubUri;

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
                //TPM security provider MUST be shutdown between tests
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
                throw new ProvisioningDeviceClientException(provisioningStatus.exception);
            }

            Thread.sleep(1000);

            System.out.println("Waiting for Provisioning Service to register");

            if (System.currentTimeMillis() - startTime > MAX_TIME_TO_WAIT_FOR_REGISTRATION)
            {
                fail("Timed out waiting for registration to succeed");
            }
        }

        assertEquals(PROVISIONING_DEVICE_STATUS_ASSIGNED, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus());
        testInstance.provisionedDeviceId = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId();
        testInstance.provisionedIotHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();
        assertNotNull(testInstance.provisionedDeviceId);
        assertFalse(testInstance.provisionedDeviceId.isEmpty());
        assertNotNull(testInstance.provisionedIotHubUri);
        assertFalse(testInstance.provisionedIotHubUri.isEmpty());
    }

    public ProvisioningStatus registerDevice(ProvisioningDeviceClientTransportProtocol protocol, SecurityProvider securityProvider, String globalEndpoint, boolean withRetry, String... expectedIotHubsToProvisionTo) throws Exception
    {
        ArrayList<String> expectedHubsToProvisionTo = new ArrayList<>();
        for (String iothubToProvisionTo : expectedIotHubsToProvisionTo)
        {
            expectedHubsToProvisionTo.add(iothubToProvisionTo);
        }
        return registerDevice(protocol, securityProvider, globalEndpoint, withRetry, expectedHubsToProvisionTo);
    }

    public ProvisioningStatus registerDevice(ProvisioningDeviceClientTransportProtocol protocol, SecurityProvider securityProvider, String globalEndpoint, boolean withRetry, List<String> expectedIotHubsToProvisionTo) throws Exception
    {
        ProvisioningStatus provisioningStatus = null;
        long startTime = System.currentTimeMillis();
        long timeoutInMillis = 180*1000; //3 minutes
        boolean deviceRegisteredSuccessfully = false;
        Thread.sleep(10*1000);
        do
        {
            try
            {
                provisioningStatus = new ProvisioningStatus();
                provisioningStatus.provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, provisioningServiceIdScope,
                        protocol,
                        securityProvider);

                provisioningStatus.provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus);
                waitForRegistrationCallback(provisioningStatus);

                String deviceId = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId();
                String provisionedHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();

                assertTrue(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED);
                assertFalse(deviceId.isEmpty());
                assertFalse(provisionedHubUri.isEmpty());

                assertProvisionedIntoCorrectHub(expectedIotHubsToProvisionTo, provisionedHubUri);
                assertProvisionedDeviceWorks(provisionedHubUri, deviceId);
                deviceRegisteredSuccessfully = true;
            }
            catch (Exception | AssertionFailedError e)
            {
                if (withRetry)
                {
                    if (((System.currentTimeMillis() - startTime) < timeoutInMillis))
                    {
                        fail(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Timed out waiting for device to register successfully", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId));
                    }
                    
                    System.out.println("Encountered an exception while registering device, trying again: " + Tools.getStackTraceFromThrowable(e));
                    Thread.sleep(10*1000);
                }
                else
                {
                    throw e;
                }
            }
            finally
            {
                if (provisioningStatus != null && provisioningStatus.provisioningDeviceClient != null)
                {
                    provisioningStatus.provisioningDeviceClient.closeNow();
                }
            }
        }
        while (withRetry && !deviceRegisteredSuccessfully);

        return provisioningStatus;
    }

    private void assertProvisionedIntoCorrectHub(List<String> iothubsToFinishAt, String actualReprovisionedHub)
    {
            assertTrue(buildExceptionMessageDpsIndividualOrGroup("Device was not provisioned into an expected hub: " + actualReprovisionedHub, getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId),
                    iothubsToFinishAt.contains(actualReprovisionedHub));
    }

    private void assertProvisionedDeviceWorks(String iothubUri, String deviceId) throws IOException, URISyntaxException
    {
        for (IotHubClientProtocol iotHubClientProtocol: IotHubClientProtocol.values())
        {
            if (iotHubClientProtocol == IotHubClientProtocol.MQTT_WS || iotHubClientProtocol == IotHubClientProtocol.AMQPS_WS)
            {
                // MQTT_WS/AMQP_WS does not support X509 because of a bug on service
                continue;
            }

            DeviceClient deviceClient = DeviceClient.createFromSecurityProvider(iothubUri, deviceId, testInstance.securityProvider, iotHubClientProtocol);
            deviceClient.closeNow();
        }
    }

    protected void assertProvisionedDeviceCapabilitiesAreExpected(DeviceCapabilities expectedDeviceCapabilities) throws IOException, IotHubException
    {
        DeviceTwin deviceTwin = DeviceTwin.createFromConnectionString(iotHubConnectionString);
        Query query = deviceTwin.queryTwin("SELECT * FROM devices WHERE deviceId = '" + testInstance.provisionedDeviceId +"'");
        assertTrue(deviceTwin.hasNextDeviceTwin(query));
        DeviceTwinDevice provisionedDevice = deviceTwin.getNextDeviceTwin(query);
        if (expectedDeviceCapabilities.isIotEdge())
        {
            assertTrue(provisionedDevice.getCapabilities().isIotEdge());
        }
        else
        {
            assertTrue(provisionedDevice.getCapabilities() == null || !provisionedDevice.getCapabilities().isIotEdge());
        }
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
        return getSecurityProviderInstance(enrollmentType, allocationPolicy, reprovisionPolicy, customAllocationDefinition, iothubs, null);
    }

    public SecurityProvider getSecurityProviderInstance(EnrollmentType enrollmentType, AllocationPolicy allocationPolicy, ReprovisionPolicy reprovisionPolicy, CustomAllocationDefinition customAllocationDefinition, List<String> iothubs, DeviceCapabilities deviceCapabilities) throws ProvisioningServiceClientException, GeneralSecurityException, IOException, SecurityProviderException, InterruptedException
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
                testInstance.enrollmentGroup.setCapabilities(deviceCapabilities);
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
                securityProvider = new SecurityProviderTPMEmulator(testInstance.registrationId, MAX_TPM_CONNECT_RETRY_ATTEMPTS);
                Attestation attestation = new TpmAttestation(new String(com.microsoft.azure.sdk.iot.deps.util.Base64.encodeBase64Local(((SecurityProviderTpm) securityProvider).getEndorsementKey())));
                createTestIndividualEnrollment(attestation, allocationPolicy, reprovisionPolicy, customAllocationDefinition, iothubs, twinState, deviceCapabilities);
            }
            else if (testInstance.attestationType == AttestationType.X509)
            {
                X509CertificateGenerator certificateGenerator = new X509CertificateGenerator(testInstance.registrationId);
                String leafPublicPem = certificateGenerator.getPublicCertificate();
                String leafPrivateKey = certificateGenerator.getPrivateKey();

                Collection<String> signerCertificates = new LinkedList<>();
                Attestation attestation = X509Attestation.createFromClientCertificates(leafPublicPem);
                createTestIndividualEnrollment(attestation, allocationPolicy, reprovisionPolicy, customAllocationDefinition, iothubs, twinState, deviceCapabilities);
                securityProvider = new SecurityProviderX509Cert(leafPublicPem, leafPrivateKey, signerCertificates);
            }
            else if (testInstance.attestationType == AttestationType.SYMMETRIC_KEY)
            {
                Attestation attestation = new SymmetricKeyAttestation(null, null);
                createTestIndividualEnrollment(attestation, allocationPolicy, reprovisionPolicy, customAllocationDefinition, iothubs, twinState, deviceCapabilities);
                assertTrue(testInstance.individualEnrollment.getAttestation() instanceof  SymmetricKeyAttestation);
                SymmetricKeyAttestation symmetricKeyAttestation = (SymmetricKeyAttestation) testInstance.individualEnrollment.getAttestation();
                securityProvider = new SecurityProviderSymmetricKey(symmetricKeyAttestation.getPrimaryKey().getBytes(), testInstance.registrationId);
            }

            assertEquals(testInstance.provisionedDeviceId, testInstance.individualEnrollment.getDeviceId());
            assertNotNull(testInstance.individualEnrollment.getInitialTwin());
            assertEquals(TEST_VALUE_TAG, testInstance.individualEnrollment.getInitialTwin().getTags().get(TEST_KEY_TAG));
            assertEquals(TEST_VALUE_DP, testInstance.individualEnrollment.getInitialTwin().getDesiredProperty().get(TEST_KEY_DP));
        }

        return securityProvider;
    }

    private void createTestIndividualEnrollment(Attestation attestation, AllocationPolicy allocationPolicy, ReprovisionPolicy reprovisionPolicy, CustomAllocationDefinition customAllocationDefinition, List<String> iothubs, TwinState twinState, DeviceCapabilities deviceCapabilities) throws ProvisioningServiceClientException
    {
        testInstance.individualEnrollment = new IndividualEnrollment(testInstance.registrationId, attestation);
        testInstance.individualEnrollment.setDeviceIdFinal(testInstance.provisionedDeviceId);
        testInstance.individualEnrollment.setCapabilitiesFinal(deviceCapabilities);
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
