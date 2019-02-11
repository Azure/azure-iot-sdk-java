/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.integration.com.microsoft.azure.sdk.iot.provisioning;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
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
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED;
import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol.*;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ProvisioningClientJVMRunner extends IntegrationTest
{
    private enum AttestationType
    {
        X509,
        TPM,
        SYMMETRIC_KEY
    };

    private enum EnrollmentType
    {
        INDIVIDUAL,
        GROUP
    };

    private final IotHubClientProtocol [] iotHubClientProtocols = {IotHubClientProtocol.MQTT, IotHubClientProtocol.MQTT_WS, IotHubClientProtocol.AMQPS, IotHubClientProtocol.AMQPS_WS, IotHubClientProtocol.HTTPS};

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";

    private static final String DPS_CONNECTION_STRING_ENV_VAR_NAME = "IOT_DPS_CONNECTION_STRING";
    private static String provisioningServiceConnectionString = "";

    private static final String DPS_CONNECTION_STRING_WITH_INVALID_CERT_ENV_VAR_NAME = "PROVISIONING_CONNECTION_STRING_INVALIDCERT";
    private static String provisioningServiceWithInvalidCertConnectionString = "";

    private static final String DPS_GLOBAL_ENDPOINT_ENV_VAR_NAME = "IOT_DPS_GLOBAL_ENDPOINT";
    private static String provisioningServiceGlobalEndpoint = "";

    private static final String DPS_GLOBAL_ENDPOINT_WITH_INVALID_CERT_ENV_VAR_NAME = "DPS_GLOBALDEVICEENDPOINT_INVALIDCERT";
    private static String provisioningServiceGlobalEndpointWithInvalidCert = "";

    private static final String DPS_ID_SCOPE_ENV_VAR_NAME = "IOT_DPS_ID_SCOPE";
    private static String provisioningServiceIdScope = "";

    private static final String TPM_SIMULATOR_IP_ADDRESS_ENV_NAME = "IOT_DPS_TPM_SIMULATOR_IP_ADDRESS"; // ip address of TPM simulator
    private static String tpmSimulatorIpAddress = "";

    private static final long MAX_TIME_TO_WAIT_FOR_REGISTRATION = 2 * 60 * 1000; // one registration could take up to 2 mins

    private static final long TPM_CONNECTION_TIMEOUT = 1 * 60 * 1000;

    private static final Integer IOTHUB_NUM_OF_MESSAGES_TO_SEND = 3; // milli secs of time to wait
    private static final List<MessageAndResult> messagesToSendAndResultsExpected = new ArrayList<>();

    // How much to wait until a message makes it to the server, in milliseconds
    private static final Integer IOTHUB_MAX_SEND_TIMEOUT = 120000; // milli secs of time to wait

    //How many milliseconds between retry
    private static final Integer IOTHUB_RETRY_MILLISECONDS = 100;

    private static final String HMAC_SHA256 = "HmacSHA256";

    private static final String REGISTRATION_ID_TPM_PREFIX = "java-tpm-registration-id-";
    private static final String DEVICE_ID_TPM_PREFIX = "java-tpm-device-id-";
    private static final String REGISTRATION_ID_X509_PREFIX = "java-x509-registration-id-";
    private static final String DEVICE_ID_X509_PREFIX = "java-x509-device-id-%s";

    private ProvisioningServiceClient provisioningServiceClient = null;
    private RegistryManager registryManager = null;

    private static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;
    private static final int OVERALL_TEST_TIMEOUT = 5 * 60 * 1000; // 5 minutes

    @Parameterized.Parameters(name = "{0} using {1}")
    public static Collection inputs()
    {
        return Arrays.asList(
                new Object[][]
                {
                        {HTTPS, AttestationType.SYMMETRIC_KEY},
                        //{HTTPS, AttestationType.TPM}, disabled: missing test infrastructure on VSTS
                        {HTTPS, AttestationType.X509},

                        {MQTT, AttestationType.SYMMETRIC_KEY},
                        //{MQTT, AttestationType.TPM}, NOT SUPPORTED BY SERVICE
                        {MQTT, AttestationType.X509},

                        {MQTT_WS, AttestationType.SYMMETRIC_KEY},
                        //{MQTT_WS, AttestationType.TPM}, NOT SUPPORTED BY SERVICE
                        {MQTT_WS, AttestationType.X509},

                        {AMQPS, AttestationType.SYMMETRIC_KEY},
                        //{AMQPS, AttestationType.TPM}, //disabled: missing test infrastructure on VSTS
                        {AMQPS, AttestationType.X509},

                        {AMQPS_WS, AttestationType.SYMMETRIC_KEY},
                        //{AMQPS_WS, AttestationType.TPM}, //disabled: missing test infrastructure on VSTS
                        {AMQPS_WS, AttestationType.X509},
                }
        );
    }

    public ProvisioningClientJVMRunner(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        this.testInstance = new ProvisioningClientITRunner(protocol, attestationType);
    }

    private ProvisioningClientITRunner testInstance;

    private class ProvisioningClientITRunner
    {
        private ProvisioningDeviceClientTransportProtocol protocol;
        private AttestationType attestationType;
        private String groupId;
        private IndividualEnrollment individualEnrollment;
        private EnrollmentGroup enrollmentGroup;
        private String registrationId;
        private String provisionedDeviceId;

        public ProvisioningClientITRunner(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
        {
            this.protocol = protocol;
            this.attestationType = attestationType;
            this.groupId = "";// by default, assume individual enrollment which has no group id
            this.registrationId = "java-provisioning-test-" + this.attestationType.toString().toLowerCase().replace("_", "-") + "-" + UUID.randomUUID().toString();
        }
    }

    @Before
    public void setUp() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        provisioningServiceConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_ENV_VAR_NAME);
        provisioningServiceGlobalEndpoint = Tools.retrieveEnvironmentVariableValue(DPS_GLOBAL_ENDPOINT_ENV_VAR_NAME);
        provisioningServiceIdScope = Tools.retrieveEnvironmentVariableValue(DPS_ID_SCOPE_ENV_VAR_NAME);
        tpmSimulatorIpAddress = Tools.retrieveEnvironmentVariableValue(TPM_SIMULATOR_IP_ADDRESS_ENV_NAME);
        provisioningServiceGlobalEndpointWithInvalidCert = Tools.retrieveEnvironmentVariableValue(DPS_GLOBAL_ENDPOINT_WITH_INVALID_CERT_ENV_VAR_NAME);
        provisioningServiceWithInvalidCertConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_WITH_INVALID_CERT_ENV_VAR_NAME);

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

        provisioningServiceClient = null;
        registryManager = null;
    }

    static class ProvisioningStatus
    {
        ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationInfoClient = new ProvisioningDeviceClientRegistrationResult();
        Exception exception;
        ProvisioningDeviceClient provisioningDeviceClient;
    }

    class ProvisioningDeviceClientRegistrationCallbackImpl implements ProvisioningDeviceClientRegistrationCallback
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

    private void waitForRegistrationCallback(ProvisioningStatus provisioningStatus) throws Exception
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

    private ProvisioningStatus registerDevice(ProvisioningDeviceClientTransportProtocol protocol, SecurityProvider securityProvider, String globalEndpoint) throws ProvisioningDeviceClientException
    {
        ProvisioningStatus provisioningStatus = new ProvisioningStatus();

        ProvisioningDeviceClient provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, provisioningServiceIdScope,
                                                                                                protocol,
                                                                                                securityProvider);
        provisioningStatus.provisioningDeviceClient = provisioningDeviceClient;
        provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus);

        return provisioningStatus;
    }

    //Parses connection String to retrieve iothub hostname
    private String getHostName(String connectionString)
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

    private SecurityProvider getSecurityProviderInstance(EnrollmentType enrollmentType) throws ProvisioningServiceClientException, GeneralSecurityException, IOException, SecurityProviderException, InterruptedException
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
                securityProvider = connectToTpmEmulator();
                Attestation attestation = new TpmAttestation(new String(Base64.encodeBase64Local(((SecurityProviderTpm) securityProvider).getEndorsementKey())));
                testInstance.individualEnrollment = new IndividualEnrollment(testInstance.registrationId, attestation);
                testInstance.individualEnrollment.setDeviceIdFinal(testInstance.provisionedDeviceId);
                testInstance.individualEnrollment.setInitialTwin(twinState);
                testInstance.individualEnrollment =  provisioningServiceClient.createOrUpdateIndividualEnrollment(testInstance.individualEnrollment);
            }
            else if (testInstance.attestationType == AttestationType.X509)
            {
                X509Cert certs = new X509Cert(0, false, testInstance.registrationId, null);
                final String leafPublicPem =  certs.getPublicCertLeafPem();
                String leafPrivateKey = certs.getPrivateKeyLeafPem();
                Collection<String> signerCertificates = new LinkedList<>();
                Attestation attestation = X509Attestation.createFromClientCertificates(leafPublicPem);
                testInstance.individualEnrollment = new IndividualEnrollment(testInstance.registrationId, attestation);
                testInstance.individualEnrollment.setDeviceIdFinal(testInstance.provisionedDeviceId);
                testInstance.individualEnrollment.setInitialTwin(twinState);
                testInstance.individualEnrollment = provisioningServiceClient.createOrUpdateIndividualEnrollment(testInstance.individualEnrollment);
                securityProvider = new SecurityProviderX509Cert(leafPublicPem, leafPrivateKey, signerCertificates);
            }
            else if (testInstance.attestationType == AttestationType.SYMMETRIC_KEY)
            {
                Attestation attestation = new SymmetricKeyAttestation(null, null);
                testInstance.individualEnrollment = new IndividualEnrollment(testInstance.registrationId, attestation);
                testInstance.individualEnrollment.setDeviceIdFinal(testInstance.provisionedDeviceId);
                testInstance.individualEnrollment =  provisioningServiceClient.createOrUpdateIndividualEnrollment(testInstance.individualEnrollment);
                testInstance.individualEnrollment.setInitialTwin(twinState);
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

    private SecurityProviderTPMEmulator connectToTpmEmulator() throws InterruptedException
    {
        SecurityProviderTPMEmulator securityProviderTPMEmulator = null;
        long startTime = System.currentTimeMillis();
        while (securityProviderTPMEmulator == null)
        {
            try
            {
                if (System.currentTimeMillis() - startTime > TPM_CONNECTION_TIMEOUT)
                {
                    fail("Timed out trying to reach TPM emulator");
                }

                securityProviderTPMEmulator = new SecurityProviderTPMEmulator(testInstance.registrationId, tpmSimulatorIpAddress);
                return securityProviderTPMEmulator;
            }
            catch (Exception e)
            {
                System.out.println("Encountered exception while connecting to TPM, trying again: \n");
                e.printStackTrace();

                //2 second buffer before attempting to connect again
                Thread.sleep(2000);
            }
        }

        return null;
    }

    private static byte[] ComputeDerivedSymmetricKey(String masterKey, String registrationId) throws InvalidKeyException, NoSuchAlgorithmException
    {
        byte[] masterKeyBytes = Base64.decodeBase64Local(masterKey.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKey = new SecretKeySpec(masterKeyBytes, HMAC_SHA256);
        Mac hMacSha256 = Mac.getInstance(HMAC_SHA256);
        hMacSha256.init(secretKey);
        return Base64.encodeBase64Local(hMacSha256.doFinal(registrationId.getBytes()));
    }

    @Test (timeout = OVERALL_TEST_TIMEOUT)
    public void IndividualEnrollmentProvisioningFlow() throws Exception
    {
        SecurityProvider securityProvider = getSecurityProviderInstance(EnrollmentType.INDIVIDUAL);

        // Register identity
        ProvisioningStatus provisioningStatus = registerDevice(testInstance.protocol, securityProvider, provisioningServiceGlobalEndpoint);
        waitForRegistrationCallback(provisioningStatus);
        provisioningStatus.provisioningDeviceClient.closeNow();

        assertEquals(testInstance.provisionedDeviceId, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

        // Tests will not pass if the linked iothub to provisioning service and iothub setup to send/receive messages isn't same.
        assertEquals("Iothub Linked to provisioning service and IotHub in connection String are not same", getHostName(iotHubConnectionString),
                provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());

        // send messages over all protocols
        for (IotHubClientProtocol iotHubClientProtocol: iotHubClientProtocols)
        {
            if (iotHubClientProtocol == IotHubClientProtocol.MQTT_WS || iotHubClientProtocol == IotHubClientProtocol.AMQPS_WS)
            {
                // MQTT_WS/AMQP_WS does not support X509 because of a bug on service
                continue;
            }

            DeviceClient deviceClient = DeviceClient.createFromSecurityProvider(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri(),
                    provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId(),
                    securityProvider, iotHubClientProtocol);
            IotHubServicesCommon.sendMessages(deviceClient, iotHubClientProtocol, messagesToSendAndResultsExpected, IOTHUB_RETRY_MILLISECONDS, IOTHUB_MAX_SEND_TIMEOUT, 200, null);
        }

        // delete enrollment
        provisioningServiceClient.deleteIndividualEnrollment(testInstance.registrationId);
        registryManager.removeDevice(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());
    }

    @Test (timeout = OVERALL_TEST_TIMEOUT)
    public void EnrollmentGroupProvisioningFlow() throws Exception
    {
        if (testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            //tpm doesn't support group, and x509 group test has not been implemented yet
            return;
        }

        SecurityProvider securityProvider = getSecurityProviderInstance(EnrollmentType.GROUP);

        // Register identity
        ProvisioningStatus provisioningStatus = registerDevice(testInstance.protocol, securityProvider, provisioningServiceGlobalEndpoint);
        waitForRegistrationCallback(provisioningStatus);
        provisioningStatus.provisioningDeviceClient.closeNow();

        assertEquals(testInstance.registrationId, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

        // Tests will not pass if the linked iothub to provisioning service and iothub setup to send/receive messages isn't same.
        assertEquals("Iothub Linked to provisioning service and IotHub in connection String are not same", getHostName(iotHubConnectionString),
                provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());

        // send messages over all protocols
        for (IotHubClientProtocol iotHubClientProtocol: iotHubClientProtocols)
        {
            if (iotHubClientProtocol == IotHubClientProtocol.MQTT_WS || iotHubClientProtocol == IotHubClientProtocol.AMQPS_WS)
            {
                // MQTT_WS/AMQP_WS does not support X509 because of a bug on service
                continue;
            }

            DeviceClient deviceClient = DeviceClient.createFromSecurityProvider(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri(),
                    provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId(),
                    securityProvider, iotHubClientProtocol);
            IotHubServicesCommon.sendMessages(deviceClient, iotHubClientProtocol, messagesToSendAndResultsExpected, IOTHUB_RETRY_MILLISECONDS, IOTHUB_MAX_SEND_TIMEOUT, 200, null);
        }

        // delete enrollment
        provisioningServiceClient.deleteEnrollmentGroup(testInstance.groupId);
        registryManager.removeDevice(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());
    }

    @Test (timeout = OVERALL_TEST_TIMEOUT)
    public void individualEnrollmentWithInvalidRemoteServerCertificateFails() throws Exception
    {
        enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType.INDIVIDUAL);
    }

    @Test (timeout = OVERALL_TEST_TIMEOUT)
    public void groupEnrollmentWithInvalidRemoteServerCertificateFails() throws Exception
    {
        enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType.GROUP);
    }

    public void enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType enrollmentType) throws Exception
    {
        if (enrollmentType == EnrollmentType.GROUP && testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            return; // test code not written for the x509 group scenario, and group enrollment does not support tpm attestation
        }

        boolean expectedExceptionEncountered = false;
        SecurityProvider securityProvider = getSecurityProviderInstance(enrollmentType);

        // Register identity
        try
        {
            ProvisioningStatus provisioningStatus = registerDevice(testInstance.protocol, securityProvider, provisioningServiceGlobalEndpointWithInvalidCert);
            waitForRegistrationCallback(provisioningStatus);
        }
        catch (Exception e)
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

    // Following test are defined by Provisioning Spec (currently not implemented)
    @Ignore
    @Test
    public void updateRegistrationAndReRegisterTPM() throws Exception
    {

    }

    @Ignore
    @Test
    public void updateRegistrationAndReRegisterX509() throws Exception
    {

    }

    @Ignore
    @Test
    public void individualEnrollmentTPMHardware() throws Exception
    {

    }

    @Ignore
    @Test
    public void groupEnrollmentX509WithZeroIntermediate() throws Exception
    {

    }

    @Ignore
    @Test
    public void groupEnrollmentX509WithOneIntermediate() throws Exception
    {

    }

    @Ignore
    @Test
    public void groupEnrollmentX509WithTwoIntermediate() throws Exception
    {

    }

    @Ignore
    @Test
    public void groupEnrollmentDiceWithOneIntermediateEmulator() throws Exception
    {

    }

    @Ignore
    @Test
    public void individualEnrollmentDiceEmulator() throws Exception
    {

    }

    @Ignore
    @Test
    public void groupEnrollmentDiceWithOneIntermediate() throws Exception
    {

    }

    @Ignore
    @Test
    public void individualEnrollmentDice() throws Exception
    {

    }
}
