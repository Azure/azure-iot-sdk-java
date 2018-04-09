/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.integration.com.microsoft.azure.sdk.iot.provisioning;

import com.microsoft.azure.sdk.iot.common.MessageAndResult;
import com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.X509Cert;

import java.io.IOException;
import java.util.*;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED;
import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ProvisioningClientIT
{
    private final IotHubClientProtocol [] iotHubClientProtocols = {IotHubClientProtocol.MQTT, IotHubClientProtocol.MQTT_WS, IotHubClientProtocol.AMQPS, IotHubClientProtocol.AMQPS_WS, IotHubClientProtocol.HTTPS};
    private final ProvisioningDeviceClientTransportProtocol [] provisioningDeviceClientTransportProtocols = {MQTT, MQTT_WS, AMQPS, AMQPS_WS, HTTPS};

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";

    private static final String DPS_CONNECTION_STRING_ENV_VAR_NAME = "IOT_DPS_CONNECTION_STRING";
    private static String provisioningServiceConnectionString = "";

    private static final String DPS_GLOBAL_ENDPOINT_ENV_VAR_NAME = "IOT_DPS_GLOBAL_ENDPOINT";
    private static String provisioningServiceGlobalEndpoint = "";

    private static final String DPS_ID_SCOPE_ENV_VAR_NAME = "IOT_DPS_ID_SCOPE";
    private static String provisioningServiceIdScope = "";

    private static final String TPM_SIMULATOR_IP_ADDRESS_ENV_NAME = "IOT_DPS_TPM_SIMULATOR_IP_ADDRESS"; // ip address of TPM simulator
    private static String tpmSimulatorIpAddress = "";

    private static final long MAX_TIME_TO_WAIT_FOR_REGISTRATION = 10000; // milli secs of time to wait

    private static final Integer IOTHUB_NUM_OF_MESSAGES_TO_SEND = 3; // milli secs of time to wait
    private static final List<MessageAndResult> messagesToSendAndResultsExpected = new ArrayList<>();

    // How much to wait until a message makes it to the server, in milliseconds
    private static final Integer IOTHUB_MAX_SEND_TIMEOUT = 60000; // milli secs of time to wait

    //How many milliseconds between retry
    private static final Integer IOTHUB_RETRY_MILLISECONDS = 100;

    private static final String REGISTRATION_ID_TPM_PREFIX = "java-tpm-registration-id-";
    private static final String DEVICE_ID_TPM_PREFIX = "java-tpm-device-id-";
    private static final String REGISTRATION_ID_X509_PREFIX = "java-x509-registration-id-";
    private static final String DEVICE_ID_X509_PREFIX = "java-x509-device-id-%s";

    private ProvisioningDeviceClient provisioningDeviceClient = null;
    private ProvisioningServiceClient provisioningServiceClient = null;
    private RegistryManager registryManager = null;

    private static final int INTERTEST_GUARDIAN_DELAY_MILLISECONDS = 2000;

    @Before
    public void setUp() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        provisioningServiceConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_ENV_VAR_NAME);
        provisioningServiceGlobalEndpoint = Tools.retrieveEnvironmentVariableValue(DPS_GLOBAL_ENDPOINT_ENV_VAR_NAME);
        provisioningServiceIdScope = Tools.retrieveEnvironmentVariableValue(DPS_ID_SCOPE_ENV_VAR_NAME);
        tpmSimulatorIpAddress = Tools.retrieveEnvironmentVariableValue(TPM_SIMULATOR_IP_ADDRESS_ENV_NAME);

        provisioningServiceClient =
                ProvisioningServiceClient.createFromConnectionString(provisioningServiceConnectionString);
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        for (int i = 0; i < IOTHUB_NUM_OF_MESSAGES_TO_SEND; i++)
        {
            messagesToSendAndResultsExpected.add(new MessageAndResult(new Message("Java client e2e test message"), IotHubStatusCode.OK_EMPTY));
        }
    }

    @After
    public void tearDown() throws Exception
    {
        try
        {
            Thread.sleep(INTERTEST_GUARDIAN_DELAY_MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        provisioningServiceClient = null;
        registryManager = null;
        provisioningDeviceClient = null;
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

    private void waitForRegistrationCallback(ProvisioningStatus provisioningStatus) throws InterruptedException
    {
        while (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() != PROVISIONING_DEVICE_STATUS_ASSIGNED)
        {
            if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ERROR ||
                    provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_DISABLED ||
                    provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_FAILED)
            {
                provisioningStatus.exception.printStackTrace();
                System.out.println("Registration error, bailing out");
                throw new InterruptedException(provisioningStatus.exception.getMessage());
            }
            System.out.println("Waiting for Provisioning Service to register");
            Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
        }

        assertEquals(PROVISIONING_DEVICE_STATUS_ASSIGNED, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus());
        assertNotNull(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
        assertNotNull(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());
    }

    private ProvisioningStatus registerDevice(ProvisioningDeviceClientTransportProtocol protocol, SecurityProvider securityProvider) throws ProvisioningDeviceClientException
    {
        ProvisioningStatus provisioningStatus = new ProvisioningStatus();

        ProvisioningDeviceClient provisioningDeviceClient = ProvisioningDeviceClient.create(provisioningServiceGlobalEndpoint, provisioningServiceIdScope,
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

    private IndividualEnrollment createIndividualEnrollmentTPM(String endorsementKey, String registrationId, String deviceId, TwinState twinState) throws ProvisioningServiceClientException
    {
        System.out.println("Creating Individual Enrollment For TPM with Registration ID " + registrationId);
        Attestation attestation = new TpmAttestation(endorsementKey);
        IndividualEnrollment individualEnrollment =
                new IndividualEnrollment(
                        registrationId,
                        attestation);

        individualEnrollment.setDeviceId(deviceId);
        if (twinState != null)
        {
            individualEnrollment.setInitialTwin(twinState);
        }

        IndividualEnrollment individualEnrollmentResult =  provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);
        assertNotNull(individualEnrollmentResult);
        assertNotNull(individualEnrollmentResult.getRegistrationId()); // Registration ID
        assertEquals(registrationId, individualEnrollmentResult.getRegistrationId()); // Registration ID
        assertNotNull((individualEnrollmentResult.getAttestation())); // Endorsement key
        assertNotNull(((TpmAttestation)individualEnrollmentResult.getAttestation()).getEndorsementKey()); // Endorsement key
        assertEquals(endorsementKey, ((TpmAttestation)individualEnrollmentResult.getAttestation()).getEndorsementKey()); // Endorsement key
        assertNotNull(individualEnrollmentResult.getEtag()); //
        assertNotNull(individualEnrollmentResult.getCreatedDateTimeUtc()); //
        assertNotNull(individualEnrollmentResult.getLastUpdatedDateTimeUtc()); //
        assertNotNull(individualEnrollmentResult.getDeviceId()); // expected device ID
        assertEquals(deviceId, individualEnrollmentResult.getDeviceId()); // expected device ID
        assertNull(individualEnrollmentResult.getProvisioningStatus()); // null
        if (twinState == null)
        {
            assertNull(individualEnrollmentResult.getInitialTwin()); // expected null
        }
        else
        {
            assertNotNull(individualEnrollmentResult.getInitialTwin()); // expected not null
        }
        System.out.println("Successfully created Individual Enrollment for TPM");
        return individualEnrollmentResult;
    }

    private IndividualEnrollment createIndividualEnrollmentX509(String clientCertPem, String registrationId, String deviceId, TwinState twinState) throws ProvisioningServiceClientException
    {
        System.out.println("Creating Individual Enrollment for X509 with Registration ID " + registrationId);
        Attestation attestation = X509Attestation.createFromClientCertificates(clientCertPem);
        IndividualEnrollment individualEnrollment =
                new IndividualEnrollment(
                        registrationId,
                        attestation);

        individualEnrollment.setDeviceId(deviceId);
        if (twinState != null)
        {
            individualEnrollment.setInitialTwin(twinState);
        }

        IndividualEnrollment individualEnrollmentResult =  provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);
        assertNotNull(individualEnrollmentResult);
        assertNotNull(individualEnrollmentResult.getRegistrationId()); // Registration ID
        assertEquals(registrationId, individualEnrollmentResult.getRegistrationId()); // Registration ID
        assertNotNull((individualEnrollmentResult.getAttestation())); // ?
        assertNotNull(((X509Attestation)individualEnrollmentResult.getAttestation()).getPrimaryX509CertificateInfo()); // Client Cert Info
        assertNotNull(individualEnrollmentResult.getEtag()); //
        assertNotNull(individualEnrollmentResult.getCreatedDateTimeUtc()); //
        assertNotNull(individualEnrollmentResult.getLastUpdatedDateTimeUtc()); //
        assertNotNull(individualEnrollmentResult.getDeviceId()); // expected device ID
        assertEquals(deviceId, individualEnrollmentResult.getDeviceId()); // expected device ID
        assertNull(individualEnrollmentResult.getProvisioningStatus()); // null
        if (twinState == null)
        {
            assertNull(individualEnrollmentResult.getInitialTwin()); // expected null
        }
        else
        {
            assertNotNull(individualEnrollmentResult.getInitialTwin()); // expected not null
        }
        System.out.println("Successfully created Individual Enrollment for X509");
        return individualEnrollmentResult;
    }

    private void deleteEnrollment(String registrationId) throws ProvisioningServiceClientException
    {
        provisioningServiceClient.deleteIndividualEnrollment(registrationId);
    }

    private void deleteDeviceFromIotHub(String deviceId) throws IotHubException, IOException
    {
        registryManager.removeDevice(deviceId);
    }

    @Test
    public void individualEnrollmentTPMSimulator() throws Exception
    {
        for (ProvisioningDeviceClientTransportProtocol protocol : provisioningDeviceClientTransportProtocols)
        {
            if (protocol == MQTT || protocol == MQTT_WS)
            {
                // MQTT and MQTT_WS are not supported for TPM from service
                continue;
            }

            String registrationId = REGISTRATION_ID_TPM_PREFIX + UUID.randomUUID().toString();
            SecurityProvider securityProviderTPMEmulator = new SecurityProviderTPMEmulator(registrationId, tpmSimulatorIpAddress);

            String deviceID = DEVICE_ID_TPM_PREFIX + UUID.randomUUID().toString();

            // setup service client with a unique registration id
            assertEquals(registrationId, securityProviderTPMEmulator.getRegistrationId());

            //
            TwinCollection tags = new TwinCollection();
            final String TEST_KEY_TAG = "testTag";
            final String TEST_VALUE_TAG = "testValue";
            tags.put(TEST_KEY_TAG, TEST_VALUE_TAG);

            final String TEST_KEY_DP = "testDP";
            final String TEST_VALUE_DP = "testDPValue";
            TwinCollection desiredProperties = new TwinCollection();
            desiredProperties.put(TEST_KEY_DP, TEST_VALUE_DP);

            TwinState twinState = new TwinState(tags, desiredProperties);
            IndividualEnrollment individualEnrollmentResult = createIndividualEnrollmentTPM(new String(Base64.encodeBase64Local(((SecurityProviderTPMEmulator) securityProviderTPMEmulator).getEndorsementKey())),
                                          registrationId, deviceID, twinState);

            assertNotNull(individualEnrollmentResult.getInitialTwin());
            assertEquals(TEST_VALUE_TAG, individualEnrollmentResult.getInitialTwin().getTags().get(TEST_KEY_TAG));
            assertEquals(TEST_VALUE_DP, individualEnrollmentResult.getInitialTwin().getDesiredProperty().get(TEST_KEY_DP));

            // Register device
            ProvisioningStatus provisioningStatus = registerDevice(protocol, securityProviderTPMEmulator);
            waitForRegistrationCallback(provisioningStatus);
            provisioningStatus.provisioningDeviceClient.closeNow();

            assertEquals(deviceID, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

            // Tests will not pass if the linked iothub to provisioning service and iothub setup to send/receive messages isn't same.
            assertEquals("Iothub Linked to provisioning service and IotHub in connection String are not same", getHostName(iotHubConnectionString),
                         provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());

            // send messages over all protocols
            for (IotHubClientProtocol iotHubClientProtocol: iotHubClientProtocols)
            {
                DeviceClient deviceClient = DeviceClient.createFromSecurityProvider(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri(),
                                                                                    provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId(),
                                                                                    securityProviderTPMEmulator, iotHubClientProtocol);
                SendMessagesCommon.sendMessages(deviceClient, iotHubClientProtocol, messagesToSendAndResultsExpected, IOTHUB_RETRY_MILLISECONDS, IOTHUB_MAX_SEND_TIMEOUT, 200);

                System.out.println("Send Messages over " + iotHubClientProtocol + " for TPM registration over " + protocol + " succeeded");
            }

            // delete enrollment
            deleteEnrollment(registrationId);
            deleteDeviceFromIotHub(deviceID);
            ((SecurityProviderTPMEmulator) securityProviderTPMEmulator).shutDown();
            System.out.println("Running TPM registration over " + protocol + " succeeded");
        }
    }

    @Test
    public void individualEnrollmentX509() throws Exception
    {
        for (ProvisioningDeviceClientTransportProtocol protocol : provisioningDeviceClientTransportProtocols)
        {
            String registrationId = REGISTRATION_ID_X509_PREFIX + UUID.randomUUID().toString();
            X509Cert certs = new X509Cert(0, false, registrationId, null);
            final String leafPublicPem =  certs.getPublicCertLeafPem();
            String leafPrivateKey = certs.getPrivateKeyLeafPem();
            Collection<String> signerCertificates = new LinkedList<>();
            SecurityProvider securityProviderX509 = new SecurityProviderX509Cert(leafPublicPem, leafPrivateKey, signerCertificates);

            // Create a device with Zero Root, Zero Intermediate and 1 leaf
            String deviceID = String.format(DEVICE_ID_X509_PREFIX, "R0-I0-L1") + UUID.randomUUID().toString();

            // setup service client with a unique registration id
            assertEquals(registrationId, securityProviderX509.getRegistrationId());

            //
            TwinCollection tags = new TwinCollection();
            final String TEST_KEY_TAG = "testTag";
            final String TEST_VALUE_TAG = "testValue";
            tags.put(TEST_KEY_TAG, TEST_VALUE_TAG);

            final String TEST_KEY_DP = "testDP";
            final String TEST_VALUE_DP = "testDPValue";
            TwinCollection desiredProperties = new TwinCollection();
            desiredProperties.put(TEST_KEY_DP, TEST_VALUE_DP);

            TwinState twinState = new TwinState(tags, desiredProperties);
            IndividualEnrollment individualEnrollmentResult = createIndividualEnrollmentX509(leafPublicPem, registrationId, deviceID, twinState);

            assertNotNull(individualEnrollmentResult.getInitialTwin());
            assertEquals(TEST_VALUE_TAG, individualEnrollmentResult.getInitialTwin().getTags().get(TEST_KEY_TAG));
            assertEquals(TEST_VALUE_DP, individualEnrollmentResult.getInitialTwin().getDesiredProperty().get(TEST_KEY_DP));

            // Register device
            ProvisioningStatus provisioningStatus = registerDevice(protocol, securityProviderX509);
            waitForRegistrationCallback(provisioningStatus);
            provisioningStatus.provisioningDeviceClient.closeNow();

            assertEquals(deviceID, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());
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
                                                                                    securityProviderX509, iotHubClientProtocol);
                SendMessagesCommon.sendMessages(deviceClient, iotHubClientProtocol, messagesToSendAndResultsExpected, IOTHUB_RETRY_MILLISECONDS, IOTHUB_MAX_SEND_TIMEOUT, 200);

                System.out.println("Send Messages over " + iotHubClientProtocol + " for X509 registration over " + protocol + " succeeded");
            }

            // delete enrollment
            deleteEnrollment(registrationId);
            deleteDeviceFromIotHub(deviceID);
            System.out.println("Running X509 registration over " + protocol + " succeeded");
        }
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
