/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.provisioning.setup;

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
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinClientOptions;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import junit.framework.AssertionFailedError;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED;
import static junit.framework.TestCase.fail;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.junit.Assert.*;
import static tests.integration.com.microsoft.azure.sdk.iot.iothub.twin.QueryTwinTests.QUERY_TIMEOUT_MILLISECONDS;

@Slf4j
public class ProvisioningCommon extends IntegrationTest
{
    // Called from JVM runner
    @Parameterized.Parameters(name = "{0}_{1}")
    public static Collection inputs()
    {
        getEnvironmentVariables();
        return inputsCommon();
    }

    private static void getEnvironmentVariables()
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        provisioningServiceConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_ENV_VAR_NAME);
        provisioningServiceIdScope = Tools.retrieveEnvironmentVariableValue(DPS_ID_SCOPE_ENV_VAR_NAME);
        farAwayIotHubConnectionString = Tools.retrieveEnvironmentVariableValue(FAR_AWAY_IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        customAllocationWebhookUrl = Tools.retrieveEnvironmentVariableValue(CUSTOM_ALLOCATION_WEBHOOK_URL_VAR_NAME);
        provisioningServiceGlobalEndpointWithInvalidCert = Tools.retrieveEnvironmentVariableValue(DPS_GLOBAL_ENDPOINT_WITH_INVALID_CERT_ENV_VAR_NAME);
        provisioningServiceWithInvalidCertConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_WITH_INVALID_CERT_ENV_VAR_NAME);
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));
    }

    // Called from android runner to only run this test suite for a particular attestation type
    public static Collection inputs(AttestationType attestationType)
    {
        getEnvironmentVariables();
        return inputsCommon(attestationType);
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

    public static final String FAR_AWAY_IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "FAR_AWAY_IOTHUB_CONNECTION_STRING";
    public static String farAwayIotHubConnectionString = "";

    public static final String CUSTOM_ALLOCATION_WEBHOOK_URL_VAR_NAME = "CUSTOM_ALLOCATION_POLICY_WEBHOOK";
    public static String customAllocationWebhookUrl = "";

    public static final String DPS_CONNECTION_STRING_ENV_VAR_NAME = "IOT_DPS_CONNECTION_STRING";
    public static String provisioningServiceConnectionString = "";

    public static final String DPS_CONNECTION_STRING_WITH_INVALID_CERT_ENV_VAR_NAME = "PROVISIONING_CONNECTION_STRING_INVALIDCERT";
    public static String provisioningServiceWithInvalidCertConnectionString = "";

    public static String provisioningServiceGlobalEndpoint = "global.azure-devices-provisioning.net";

    public static final String DPS_GLOBAL_ENDPOINT_WITH_INVALID_CERT_ENV_VAR_NAME = "DPS_GLOBALDEVICEENDPOINT_INVALIDCERT";
    public static String provisioningServiceGlobalEndpointWithInvalidCert = "";

    public static final String DPS_ID_SCOPE_ENV_VAR_NAME = "IOT_DPS_ID_SCOPE";
    public static String provisioningServiceIdScope = "";

    public static final long MAX_TIME_TO_WAIT_FOR_REGISTRATION_MILLISECONDS = 60 * 1000;

    public static final int MAX_TPM_CONNECT_RETRY_ATTEMPTS = 10;

    protected static final String CUSTOM_ALLOCATION_WEBHOOK_API_VERSION = "2019-03-31";

    public RegistryManager registryManager = null;

    //sending reported properties for twin operations takes some time to get the appropriate callback
    public static final int MAX_TWIN_PROPAGATION_WAIT_SECONDS = 60;

    public static Collection inputsCommon(AttestationType attestationType)
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
            if (!isPullRequest)
            {
                //TODO TPM tests are flakey, so only run them for CI and nightly builds
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
                //no tests to run for pull request builds
                return Collections.emptyList();
            }
        }
        else
        {
            throw new IllegalArgumentException("Unknown attestation type provided");
        }
    }

    public static Collection inputsCommon()
    {
        // Intentionally not doing TPM tests here. There is a separate class for running those
        // tests in serial
        return Arrays.asList(
                new Object[][]
                        {
                                {ProvisioningDeviceClientTransportProtocol.HTTPS, AttestationType.X509},
                                {ProvisioningDeviceClientTransportProtocol.AMQPS, AttestationType.X509},
                                {ProvisioningDeviceClientTransportProtocol.AMQPS_WS, AttestationType.X509},
                                {ProvisioningDeviceClientTransportProtocol.MQTT, AttestationType.X509},
                                {ProvisioningDeviceClientTransportProtocol.MQTT_WS, AttestationType.X509},

                                {ProvisioningDeviceClientTransportProtocol.HTTPS, AttestationType.SYMMETRIC_KEY},
                                {ProvisioningDeviceClientTransportProtocol.AMQPS, AttestationType.SYMMETRIC_KEY},
                                {ProvisioningDeviceClientTransportProtocol.AMQPS_WS, AttestationType.SYMMETRIC_KEY},
                                {ProvisioningDeviceClientTransportProtocol.MQTT, AttestationType.SYMMETRIC_KEY},
                                {ProvisioningDeviceClientTransportProtocol.MQTT_WS, AttestationType.SYMMETRIC_KEY}
                        });
    }

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

        public ProvisioningTestInstance(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
        {
            this.protocol = protocol;
            this.attestationType = attestationType;
            this.groupId = "";// by default, assume enrollment has no group id
            this.registrationId = "java-provisioning-test-" + this.attestationType.toString().toLowerCase().replace("_", "-") + "-" + UUID.randomUUID().toString();
            this.provisioningServiceClient =
                    ProvisioningServiceClient.createFromConnectionString(provisioningServiceConnectionString);
        }
    }

    @Before
    public void setUp() throws Exception
    {
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());

        this.testInstance = new ProvisioningTestInstance(this.testInstance.protocol, this.testInstance.attestationType);
    }

    @After
    public void tearDown()
    {
        if (registryManager != null)
        {
            registryManager.close();
        }

        registryManager = null;

        if (testInstance != null && testInstance.securityProvider != null && testInstance.securityProvider instanceof SecurityProviderTPMEmulator)
        {
            testInstance.provisioningServiceClient = null;
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
            if (testInstance.groupId != null && !testInstance.groupId.isEmpty())
            {
                log.debug("Deleting enrollment group with group Id {}", testInstance.groupId);
                testInstance.provisioningServiceClient.deleteEnrollmentGroup(testInstance.groupId);
            }

            if (testInstance.individualEnrollment != null)
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

    public static class ProvisioningStatus
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

            if (System.currentTimeMillis() - startTime > MAX_TIME_TO_WAIT_FOR_REGISTRATION_MILLISECONDS)
            {
                fail("Timed out waiting for registration to succeed");
            }
        }

        Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected status", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), PROVISIONING_DEVICE_STATUS_ASSIGNED, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus());
        testInstance.provisionedDeviceId = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId();
        testInstance.provisionedIotHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();
        assertNotNull(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Expected a device id", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.provisionedDeviceId);
        assertFalse(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Expected a device id", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.provisionedDeviceId.isEmpty());
        assertNotNull(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Expected uri", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.provisionedIotHubUri);
        assertFalse(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Expected uri", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.provisionedIotHubUri.isEmpty());
    }

    public ProvisioningStatus registerDevice(ProvisioningDeviceClientTransportProtocol protocol, SecurityProvider securityProvider, String globalEndpoint, boolean withRetry, String jsonPayload, String... expectedIotHubsToProvisionTo) throws Exception
    {
        ArrayList<String> expectedHubsToProvisionTo = new ArrayList<>(Arrays.asList(expectedIotHubsToProvisionTo));
        return registerDevice(protocol, securityProvider, globalEndpoint, withRetry, expectedHubsToProvisionTo, jsonPayload);
    }

    public ProvisioningStatus registerDevice(ProvisioningDeviceClientTransportProtocol protocol, SecurityProvider securityProvider, String globalEndpoint, boolean withRetry, List<String> expectedIotHubsToProvisionTo) throws Exception
    {
        return registerDevice(protocol, securityProvider, globalEndpoint, withRetry, expectedIotHubsToProvisionTo, null);
    }

    public ProvisioningStatus registerDevice(ProvisioningDeviceClientTransportProtocol protocol, SecurityProvider securityProvider, String globalEndpoint, boolean withRetry, List<String> expectedIotHubsToProvisionTo, String jsonPayload) throws Exception
    {
        ProvisioningStatus provisioningStatus = null;
        long startTime = System.currentTimeMillis();
        long timeoutInMillis = 180*1000; //3 minutes
        boolean deviceRegisteredSuccessfully = false;
        do
        {
            try
            {
                provisioningStatus = new ProvisioningStatus();
                provisioningStatus.provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, provisioningServiceIdScope,
                        protocol,
                        securityProvider);

                if (jsonPayload == null)
                {
                    provisioningStatus.provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus);
                }
                else
                {
                    AdditionalData additionalData = new AdditionalData();
                    additionalData.setProvisioningPayload(jsonPayload);
                    provisioningStatus.provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus, additionalData);
                }
                waitForRegistrationCallback(provisioningStatus);

                String deviceId = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId();
                String provisionedHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();

                assertSame(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected status", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus(), PROVISIONING_DEVICE_STATUS_ASSIGNED);
                assertFalse(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected deviceId", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), deviceId.isEmpty());
                assertFalse(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected uri", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), provisionedHubUri.isEmpty());

                if (jsonPayload != null && !jsonPayload.isEmpty())
                {
                    String returnJson = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningPayload();
                    assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Payload received from service is not the same values. Sent Json: " + jsonPayload + " returned json " + returnJson, getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), returnJson, jsonPayload);
                }
                assertProvisionedIntoCorrectHub(expectedIotHubsToProvisionTo, provisionedHubUri);
                assertProvisionedDeviceWorks(provisionedHubUri, deviceId);
                deviceRegisteredSuccessfully = true;
            }
            catch (Exception | AssertionFailedError e)
            {
                if (withRetry)
                {
                    if (((System.currentTimeMillis() - startTime) > timeoutInMillis))
                    {
                        fail(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Timed out waiting for device to register successfully, last exception: " + Tools.getStackTraceFromThrowable(e), getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId));
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
            assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Device was not provisioned into an expected hub: " + actualReprovisionedHub, getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId),
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

    protected void assertProvisionedDeviceCapabilitiesAreExpected(DeviceCapabilities expectedDeviceCapabilities, String provisionedHubConnectionString) throws IOException, IotHubException, InterruptedException {
        DeviceTwin deviceTwin = DeviceTwin.createFromConnectionString(provisionedHubConnectionString, DeviceTwinClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());

        boolean deviceFoundInCorrectHub = false;
        Query query = null;
        long startTime = System.currentTimeMillis();
        while (!deviceFoundInCorrectHub)
        {
            query = deviceTwin.queryTwin("SELECT * FROM devices WHERE deviceId = '" + testInstance.provisionedDeviceId +"'");
            deviceFoundInCorrectHub = deviceTwin.hasNextDeviceTwin(query);

            Thread.sleep(3000);

            if (System.currentTimeMillis() - startTime > QUERY_TIMEOUT_MILLISECONDS)
            {
                fail(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Timed out waiting for provisioned device " + testInstance.provisionedDeviceId + " to be found in expected hub", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId));
            }
        }

        DeviceTwinDevice provisionedDevice = deviceTwin.getNextDeviceTwin(query);
        if (expectedDeviceCapabilities.isIotEdge())
        {
            assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Provisioned device isn't edge device: " + testInstance.provisionedDeviceId, getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), provisionedDevice.getCapabilities().isIotEdge());
        }
        else
        {
            assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Provisioned device shouldn't be edge device " + testInstance.provisionedDeviceId, getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), provisionedDevice.getCapabilities() == null || !provisionedDevice.getCapabilities().isIotEdge());
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
                testInstance.enrollmentGroup = testInstance.provisioningServiceClient.createOrUpdateEnrollmentGroup(testInstance.enrollmentGroup);
                Attestation attestation = testInstance.enrollmentGroup.getAttestation();
                assertTrue(attestation instanceof SymmetricKeyAttestation);

                assertNotNull(testInstance.enrollmentGroup.getInitialTwin());
                assertEquals(TEST_VALUE_TAG, testInstance.enrollmentGroup.getInitialTwin().getTags().get(TEST_KEY_TAG));
                assertEquals(TEST_VALUE_DP, testInstance.enrollmentGroup.getInitialTwin().getDesiredProperty().get(TEST_KEY_DP));

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
                assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Expected symmetric key attestation", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.individualEnrollment.getAttestation() instanceof  SymmetricKeyAttestation);
                SymmetricKeyAttestation symmetricKeyAttestation = (SymmetricKeyAttestation) testInstance.individualEnrollment.getAttestation();
                securityProvider = new SecurityProviderSymmetricKey(symmetricKeyAttestation.getPrimaryKey().getBytes(), testInstance.registrationId);
            }

            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected device id assigned", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.provisionedDeviceId, testInstance.individualEnrollment.getDeviceId());
            assertNotNull(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Expected twin to not be null", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), testInstance.individualEnrollment.getInitialTwin());
            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected tags found", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), TEST_VALUE_TAG, testInstance.individualEnrollment.getInitialTwin().getTags().get(TEST_KEY_TAG));
            Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Unexpected desired properties", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId), TEST_VALUE_DP, testInstance.individualEnrollment.getInitialTwin().getDesiredProperty().get(TEST_KEY_DP));
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
        testInstance.individualEnrollment = testInstance.provisioningServiceClient.createOrUpdateIndividualEnrollment(testInstance.individualEnrollment);
    }
}
