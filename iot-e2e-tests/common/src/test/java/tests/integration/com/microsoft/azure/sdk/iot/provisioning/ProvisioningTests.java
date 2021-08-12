/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.provisioning;


import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.PropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinClientOptions;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.DeviceProvisioningServiceTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.provisioning.setup.ProvisioningCommon;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    /***
     * This test flow uses a custom allocation policy to decide which of the two hubs a device should be provisioned to.
     * The custom allocation policy has a webhook to an Azure function, and that function will always dictate to provision
     * the device to the hub with the longest host name. This test verifies that an enrollment with a custom allocation policy
     * pointing to that Azure function will always enroll to the hub with the longest name
     * @param enrollmentType The type of the enrollment to test
     * @throws Exception if an exception occurs during provisioning or while creating the security provider
     */
    protected void customAllocationFlow(EnrollmentType enrollmentType) throws Exception
    {
        if (enrollmentType == EnrollmentType.GROUP && testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            //tpm doesn't support group, and x509 group test has not been implemented yet
            return;
        }

        List<String> possibleStartingHubHostNames = new ArrayList<>();
        String farAwayIotHubHostname = IotHubConnectionString.createIotHubConnectionString(farAwayIotHubConnectionString).getHostName();
        String iothubHostName = IotHubConnectionString.createIotHubConnectionString(iotHubConnectionString).getHostName();
        possibleStartingHubHostNames.add(farAwayIotHubHostname);
        possibleStartingHubHostNames.add(iothubHostName);

        String expectedHubToProvisionTo;
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

        CustomAllocationDefinition customAllocationDefinition = new CustomAllocationDefinition();
        customAllocationDefinition.setApiVersion(CUSTOM_ALLOCATION_WEBHOOK_API_VERSION);
        customAllocationDefinition.setWebhookUrl(customAllocationWebhookUrl);

        testInstance.securityProvider = getSecurityProviderInstance(enrollmentType, AllocationPolicy.CUSTOM, null, customAllocationDefinition, possibleStartingHubHostNames);

        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, null, expectedHubToProvisionTo, null);
    }

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    protected void reprovisioningFlow(EnrollmentType enrollmentType, AllocationPolicy allocationPolicy, ReprovisionPolicy reprovisionPolicy, CustomAllocationDefinition customAllocationDefinition, List<String> iothubsToStartAt, List<String> iothubsToFinishAt) throws Exception
    {
        DeviceCapabilities capabilities = new DeviceCapabilities();
        capabilities.setIotEdge(false);
        reprovisioningFlow(enrollmentType, allocationPolicy, reprovisionPolicy, customAllocationDefinition, iothubsToStartAt, iothubsToFinishAt, capabilities);
    }

    protected void reprovisioningFlow(EnrollmentType enrollmentType, AllocationPolicy allocationPolicy, ReprovisionPolicy reprovisionPolicy, CustomAllocationDefinition customAllocationDefinition, List<String> iothubsToStartAt, List<String> iothubsToFinishAt, DeviceCapabilities capabilities) throws Exception
    {
        if (enrollmentType == EnrollmentType.GROUP && testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            //tpm doesn't support group, and x509 group test has not been implemented yet
            return;
        }

        testInstance.securityProvider = getSecurityProviderInstance(enrollmentType, allocationPolicy, reprovisionPolicy, customAllocationDefinition, iothubsToStartAt, capabilities);

        ProvisioningStatus provisioningStatus = registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, iothubsToStartAt);

        assertProvisionedDeviceCapabilitiesAreExpected(capabilities, farAwayIotHubConnectionString);

        String expectedReportedPropertyName = "someProperty";
        String expectedReportedPropertyValue = "someValue";
        sendReportedPropertyUpdate(expectedReportedPropertyName, expectedReportedPropertyValue, testInstance.provisionedIotHubUri, testInstance.provisionedDeviceId);

        updateEnrollmentToForceReprovisioning(enrollmentType, iothubsToFinishAt);

        if (testInstance.securityProvider instanceof SecurityProviderTPMEmulator)
        {
            ((SecurityProviderTPMEmulator) testInstance.securityProvider).shutDown();
            testInstance.securityProvider = new SecurityProviderTPMEmulator(testInstance.registrationId);
        }

        //re-register device, test which hub it was provisioned to
        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, reprovisionPolicy.getUpdateHubAssignment() ? iothubsToFinishAt : iothubsToStartAt);
        assertTwinIsCorrect(reprovisionPolicy, expectedReportedPropertyName, expectedReportedPropertyValue, !reprovisionPolicy.getUpdateHubAssignment());
    }

    private static class StubTwinCallback implements IotHubEventCallback, PropertyCallBack
    {
        private final CountDownLatch twinLock;

        public StubTwinCallback(CountDownLatch twinLock)
        {
                this.twinLock = twinLock;
        }

        @Override
        public void execute(IotHubStatusCode responseStatus, Object callbackContext)
        {
            twinLock.countDown(); //this will be called once upon twin start, and once for sending a single reported property
        }

        @Override
        public void PropertyCall(Object propertyKey, Object propertyValue, Object context)
        {
            //do nothing
        }
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

    private void assertTwinIsCorrect(ReprovisionPolicy reprovisionPolicy, String expectedPropertyName, String expectedPropertyValue, boolean inFarAwayHub) throws IOException, IotHubException
    {
        if (reprovisionPolicy != null && reprovisionPolicy.getMigrateDeviceData())
        {
            DeviceTwin twinClient;
            if (inFarAwayHub)
            {
                twinClient = new DeviceTwin(farAwayIotHubConnectionString, DeviceTwinClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
            }
            else
            {
                twinClient = new DeviceTwin(iotHubConnectionString, DeviceTwinClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
            }

            DeviceTwinDevice device = new DeviceTwinDevice(testInstance.provisionedDeviceId);

            twinClient.getTwin(device);

            if (reprovisionPolicy.getMigrateDeviceData())
            {
                //twin change from before reprovisioning was migrated
                Assert.assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Twin size is unexpected value", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId),
                        1, device.getReportedProperties().size());
                boolean expectedKeyPairFound = false;
                for (Pair pair: device.getReportedProperties())
                {
                    expectedKeyPairFound |= (pair.getKey().equals(expectedPropertyName) && pair.getValue().equals(expectedPropertyValue));
                }
                assertTrue(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Reported property that was sent was not found", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId),
                        expectedKeyPairFound);
            }
            else
            {
                //twin change from before reprovisioning was reset
                assertEquals(CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup("Twin size is unexpected value", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId),
                        0, device.getReportedProperties().size());
            }
        }
    }

    private List<String> getStartingHubs()
    {
        String farAwayIotHubHostname = IotHubConnectionString.createIotHubConnectionString(farAwayIotHubConnectionString).getHostName();
        List<String> iotHubsToStartAt = new ArrayList<>();
        iotHubsToStartAt.add(farAwayIotHubHostname);
        return iotHubsToStartAt;
    }

    private List<String> getHubsToReprovisionTo()
    {
        String iothubHostName = IotHubConnectionString.createIotHubConnectionString(iotHubConnectionString).getHostName();
        List<String> iotHubsToReprovisionTo = new ArrayList<>();
        iotHubsToReprovisionTo.add(iothubHostName);
        return iotHubsToReprovisionTo;
    }

    private void sendReportedPropertyUpdate(String expectedReportedPropertyName, String expectedReportedPropertyValue, String iothubUri, String deviceId) throws InterruptedException, IOException, URISyntaxException
    {
        //hardcoded AMQP here only because we aren't testing this connection. We just need to open a connection to send a twin update so that
        // we can test if the twin updates carry over after reprovisioning
        DeviceClient deviceClient = DeviceClient.createFromSecurityProvider(iothubUri, deviceId, testInstance.securityProvider, IotHubClientProtocol.AMQPS);
        deviceClient.open();
        CountDownLatch twinLock = new CountDownLatch(2);
        deviceClient.startTwinAsync(new StubTwinCallback(twinLock), null, new StubTwinCallback(twinLock), null);
        Set<Property> reportedProperties = new HashSet<>();
        reportedProperties.add(new Property(expectedReportedPropertyName, expectedReportedPropertyValue));
        deviceClient.sendReportedPropertiesAsync(reportedProperties);
        twinLock.await(MAX_TWIN_PROPAGATION_WAIT_SECONDS, TimeUnit.SECONDS);
        deviceClient.closeNow();
    }

    private void updateEnrollmentToForceReprovisioning(EnrollmentType enrollmentType, List<String> iothubsToFinishAt) throws ProvisioningServiceClientException
    {
        if (enrollmentType == EnrollmentType.GROUP)
        {
            testInstance.enrollmentGroup.setIotHubs(iothubsToFinishAt);
            testInstance.provisioningServiceClient.createOrUpdateEnrollmentGroup(testInstance.enrollmentGroup);
        }
        else
        {
            testInstance.individualEnrollment.setIotHubs(iothubsToFinishAt);
            testInstance.provisioningServiceClient.createOrUpdateIndividualEnrollment(testInstance.individualEnrollment);
        }
    }
}
