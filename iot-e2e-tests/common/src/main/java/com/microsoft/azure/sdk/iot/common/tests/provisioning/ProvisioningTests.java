/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.provisioning;

import com.microsoft.azure.sdk.iot.common.helpers.IotHubServicesCommon;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.setup.ProvisioningCommon;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.PropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.AllocationPolicy;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.CustomAllocationDefinition;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.ReprovisionPolicy;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol.*;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

public class ProvisioningTests extends ProvisioningCommon
{
    public ProvisioningTests(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        super(protocol, attestationType);
    }

    @Test
    public void individualEnrollmentProvisioningFlow() throws Exception
    {
        testInstance.securityProvider = getSecurityProviderInstance(EnrollmentType.INDIVIDUAL);
        Thread.sleep(ENROLLMENT_PROPAGATION_DELAY_MS);
        ProvisioningStatus provisioningStatus = registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint);

        assertEquals(testInstance.provisionedDeviceId, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

        String provisionedHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();
        boolean deviceProvisionedIntoExpectedHub = getHostName(iotHubConnectionString).equalsIgnoreCase(provisionedHubUri) || getHostName(farAwayIotHubConnectionString).equalsIgnoreCase(provisionedHubUri);
        assertTrue("Iothub Linked to provisioning service and IotHub in connection String are not same", deviceProvisionedIntoExpectedHub);

        // send messages over all protocols
        assertProvisionedDeviceWorks(provisioningStatus, provisionedHubUri);

        // delete enrollment
        cleanUpReprovisionedDeviceAndEnrollment(provisionedHubUri.equalsIgnoreCase(getHostName(farAwayIotHubConnectionString)), provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId(), EnrollmentType.INDIVIDUAL);
    }

    @Test
    public void enrollmentGroupProvisioningFlow() throws Exception
    {
        if (testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            //tpm doesn't support group, and x509 group test has not been implemented yet
            return;
        }

        testInstance.securityProvider = getSecurityProviderInstance(EnrollmentType.GROUP);
        Thread.sleep(ENROLLMENT_PROPAGATION_DELAY_MS);

        ProvisioningStatus provisioningStatus = registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint);

        assertEquals(testInstance.registrationId, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

        // Tests will not pass if the linked iothub to provisioning service and iothub setup to send/receive messages isn't same.
        String provisionedHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();
        boolean deviceProvisionedIntoExpectedHub = getHostName(iotHubConnectionString).equalsIgnoreCase(provisionedHubUri) || getHostName(farAwayIotHubConnectionString).equalsIgnoreCase(provisionedHubUri);
        assertTrue("Iothub Linked to provisioning service and IotHub in connection String are not same", deviceProvisionedIntoExpectedHub);

        // send messages over all protocols
        assertProvisionedDeviceWorks(provisioningStatus, provisionedHubUri);

        // delete enrollment
        cleanUpReprovisionedDeviceAndEnrollment(provisionedHubUri.equalsIgnoreCase(getHostName(farAwayIotHubConnectionString)), provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId(), EnrollmentType.GROUP);
    }

    //@Test
    public void individualEnrollmentWithInvalidRemoteServerCertificateFails() throws Exception
    {
        enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType.INDIVIDUAL);
    }

    //@Test
    public void groupEnrollmentWithInvalidRemoteServerCertificateFails() throws Exception
    {
        enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType.GROUP);
    }

    @Test
    public void groupEnrollmentProvisioningReprovisioningKeepTwin() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setMigrateDeviceData(true);
        reprovisionPolicy.setUpdateHubAssignment(true);

        reprovisioningFlow(EnrollmentType.GROUP, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    public void groupEnrollmentProvisioningReprovisioningResetTwin() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setMigrateDeviceData(false);
        reprovisionPolicy.setUpdateHubAssignment(true);

        reprovisioningFlow(EnrollmentType.GROUP, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    public void groupEnrollmentProvisioningReprovisioningCanBlockReprovisioning() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setUpdateHubAssignment(false);

        reprovisioningFlow(EnrollmentType.GROUP, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    public void groupEnrollmentProvisioningCustomAllocationPolicy() throws Exception
    {
        customAllocationFlow(EnrollmentType.GROUP);
    }

    @Test
    public void individualEnrollmentProvisioningReprovisioningKeepTwin() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setMigrateDeviceData(true);
        reprovisionPolicy.setUpdateHubAssignment(true);

        reprovisioningFlow(EnrollmentType.INDIVIDUAL, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    public void individualEnrollmentProvisioningReprovisioningResetTwin() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setMigrateDeviceData(false);
        reprovisionPolicy.setUpdateHubAssignment(true);

        reprovisioningFlow(EnrollmentType.INDIVIDUAL, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    public void individualEnrollmentProvisioningReprovisioningCanBlockReprovisioning() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setUpdateHubAssignment(false);

        reprovisioningFlow(EnrollmentType.INDIVIDUAL, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    public void individualEnrollmentProvisioningCustomAllocationPolicy() throws Exception
    {
        customAllocationFlow(EnrollmentType.INDIVIDUAL);
    }

    /***
     * This test flow uses a custom allocation policy to decide which of the two hubs a device should be provisioned to.
     * The custom allocation policy has a webhook to an Azure function, and that function will always dictate to provision
     * the device to the hub with the longest host name. This test verifies that an enrollment with a custom allocation policy
     * pointing to that Azure function will always enroll to the hub with the longest name
     * @param enrollmentType
     */
    protected void customAllocationFlow(EnrollmentType enrollmentType) throws Exception {
        if (enrollmentType == EnrollmentType.GROUP && testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            //tpm doesn't support group, and x509 group test has not been implemented yet
            return;
        }

        List<String> possibleStartingHubHostNames = new ArrayList<>();
        String farAwayIotHubHostname = IotHubConnectionString.createConnectionString(farAwayIotHubConnectionString).getHostName();
        String iothubHostName = IotHubConnectionString.createConnectionString(iotHubConnectionString).getHostName();
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
        customAllocationDefinition.setApiVersion("2018-11-01");
        customAllocationDefinition.setWebhookUrl(customAllocationWebhookUrl);

        testInstance.securityProvider = getSecurityProviderInstance(enrollmentType, AllocationPolicy.CUSTOM, null, customAllocationDefinition, possibleStartingHubHostNames);
        Thread.sleep(ENROLLMENT_PROPAGATION_DELAY_MS);

        ProvisioningStatus provisioningStatus = registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint);

        assertEquals("Device was not provisioned into the expected hub", expectedHubToProvisionTo, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
    }

    protected void reprovisioningFlow(EnrollmentType enrollmentType, AllocationPolicy allocationPolicy, ReprovisionPolicy reprovisionPolicy, CustomAllocationDefinition customAllocationDefinition, List<String> iothubsToStartAt, List<String> iothubsToFinishAt) throws Exception
    {
        if (enrollmentType == EnrollmentType.GROUP && testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            //tpm doesn't support group, and x509 group test has not been implemented yet
            return;
        }

        testInstance.securityProvider = getSecurityProviderInstance(enrollmentType, allocationPolicy, reprovisionPolicy, customAllocationDefinition, iothubsToStartAt);

        ProvisioningStatus provisioningStatus = registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint);

        assertTrue("Device was not provisioned into an expected hub", iothubsToStartAt.contains(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri()));

        String expectedReportedPropertyName = "someProperty";
        String expectedReportedPropertyValue = "someValue";
        sendReportedPropertyUpdate(provisioningStatus, expectedReportedPropertyName, expectedReportedPropertyValue);

        updateEnrollmentToForceReprovisioning(enrollmentType, iothubsToFinishAt);

        if (testInstance.securityProvider instanceof SecurityProviderTPMEmulator)
        {
            ((SecurityProviderTPMEmulator) testInstance.securityProvider).shutDown();
            testInstance.securityProvider = new SecurityProviderTPMEmulator(testInstance.registrationId);
        }

        //re-register device, test which hub it was provisioned to
        provisioningStatus = registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint);

        assertReprovisionedIntoCorrectHub(reprovisionPolicy, iothubsToFinishAt, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
        //assertProvisionedDeviceWorks(provisioningStatus, securityProvider);
        assertTwinIsCorrect(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId(), reprovisionPolicy, expectedReportedPropertyName, expectedReportedPropertyValue, !reprovisionPolicy.getUpdateHubAssignment());

        cleanUpReprovisionedDeviceAndEnrollment(!reprovisionPolicy.getUpdateHubAssignment(), provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId(), enrollmentType);
    }

    private class StubTwinCallback implements IotHubEventCallback, PropertyCallBack
    {
        private CountDownLatch twinLock;

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
            registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpointWithInvalidCert);
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

    private void assertProvisionedDeviceWorks(ProvisioningStatus provisioningStatus, String iothubUri) throws IOException, URISyntaxException, InterruptedException
    {
        for (IotHubClientProtocol iotHubClientProtocol: iotHubClientProtocols)
        {
            if (iotHubClientProtocol == IotHubClientProtocol.MQTT_WS || iotHubClientProtocol == IotHubClientProtocol.AMQPS_WS)
            {
                // MQTT_WS/AMQP_WS does not support X509 because of a bug on service
                continue;
            }

            DeviceClient deviceClient = DeviceClient.createFromSecurityProvider(iothubUri,
                    provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId(),
                    testInstance.securityProvider, iotHubClientProtocol);
            deviceClient.closeNow();
            //IotHubServicesCommon.sendMessages(deviceClient, iotHubClientProtocol, messagesToSendAndResultsExpected, IOTHUB_RETRY_MILLISECONDS, IOTHUB_MAX_SEND_TIMEOUT, 200, null);
        }
    }

    private void assertTwinIsCorrect(String deviceId, ReprovisionPolicy reprovisionPolicy, String expectedPropertyName, String expectedPropertyValue, boolean inFarAwayHub) throws IOException, IotHubException {
        if (reprovisionPolicy != null && reprovisionPolicy.getMigrateDeviceData() == true)
        {
            DeviceTwin twinClient;
            if (inFarAwayHub)
            {
                twinClient = DeviceTwin.createFromConnectionString(farAwayIotHubConnectionString);
            }
            else
            {
                twinClient = DeviceTwin.createFromConnectionString(iotHubConnectionString);
            }

            DeviceTwinDevice device = new DeviceTwinDevice(deviceId);

            twinClient.getTwin(device);

            if (reprovisionPolicy.getMigrateDeviceData())
            {
                //twin change from before reprovisioning was migrated
                assertEquals("Twin size is unexpected value", 1, device.getReportedProperties().size());
                boolean expectedKeyPairFound = false;
                for (Pair pair: device.getReportedProperties())
                {
                    expectedKeyPairFound |= (pair.getKey().equals(expectedPropertyName) && pair.getValue().equals(expectedPropertyValue));
                }
                assertTrue("Reported property that was sent was not found", expectedKeyPairFound);
            }
            else
            {
                //twin change from before reprovisioning was reset
                assertEquals("Twin size is unexpected value",0, device.getReportedProperties().size());
            }
        }
    }

    private void cleanUpReprovisionedDeviceAndEnrollment(boolean inFarAwayHub, String deviceId, EnrollmentType enrollmentType) throws IOException, IotHubException, ProvisioningServiceClientException {
        //delete provisioned device
        RegistryManager registryManager;
        if (inFarAwayHub)
        {
            registryManager = RegistryManager.createFromConnectionString(farAwayIotHubConnectionString);
        }
        else
        {
            registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        }

        registryManager.removeDevice(deviceId);

        //delete enrollment
        if (enrollmentType == EnrollmentType.GROUP)
        {
            provisioningServiceClient.deleteEnrollmentGroup(testInstance.groupId);
        }
        else
        {
            provisioningServiceClient.deleteIndividualEnrollment(testInstance.individualEnrollment.getRegistrationId());
        }

    }

    private List<String> getStartingHubs() throws IOException
    {
        String farAwayIotHubHostname = IotHubConnectionString.createConnectionString(farAwayIotHubConnectionString).getHostName();
        List<String> iotHubsToStartAt = new ArrayList<>();
        iotHubsToStartAt.add(farAwayIotHubHostname);
        return iotHubsToStartAt;
    }

    private List<String> getHubsToReprovisionTo() throws IOException
    {
        String iothubHostName = IotHubConnectionString.createConnectionString(iotHubConnectionString).getHostName();
        List<String> iotHubsToReprovisionTo = new ArrayList<>();
        iotHubsToReprovisionTo.add(iothubHostName);
        return iotHubsToReprovisionTo;
    }

    private void sendReportedPropertyUpdate(ProvisioningStatus provisioningStatus, String expectedReportedPropertyName, String expectedReportedPropertyValue) throws InterruptedException, IOException, URISyntaxException {
        //hardcoded AMQP here only because we aren't testing this connection. We just need to open a connection to send a twin update so that
        // we can test if the twin updates carry over after reprovisioning
        DeviceClient deviceClient = DeviceClient.createFromSecurityProvider(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri(),
                provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId(),
                testInstance.securityProvider, IotHubClientProtocol.AMQPS);
        IotHubServicesCommon.openClientWithRetry(deviceClient);
        CountDownLatch twinLock = new CountDownLatch(2);
        deviceClient.startDeviceTwin(new StubTwinCallback(twinLock), null, new StubTwinCallback(twinLock), null);
        Set<Property> reportedProperties = new HashSet<>();
        reportedProperties.add(new Property(expectedReportedPropertyName, expectedReportedPropertyValue));
        deviceClient.sendReportedProperties(reportedProperties);
        twinLock.await(MAX_TWIN_PROPAGATION_WAIT_SECONDS, TimeUnit.SECONDS);
        deviceClient.close();
    }

    private void assertReprovisionedIntoCorrectHub(ReprovisionPolicy reprovisionPolicy, List<String> iothubsToFinishAt, String actualReprovisionedHub)
    {
        if (reprovisionPolicy.getUpdateHubAssignment())
        {
            assertTrue("Device was not reprovisioned into the expected hub", iothubsToFinishAt.contains(actualReprovisionedHub));
        }
        else
        {
            assertFalse("Device was reprovisioned when reprovisioning was supposed to blocked by the reprovisioning policy", iothubsToFinishAt.contains(actualReprovisionedHub));
        }
    }

    private void updateEnrollmentToForceReprovisioning(EnrollmentType enrollmentType, List<String> iothubsToFinishAt) throws ProvisioningServiceClientException
    {
        if (enrollmentType == EnrollmentType.GROUP)
        {
            testInstance.enrollmentGroup.setIotHubs(iothubsToFinishAt);
            provisioningServiceClient.createOrUpdateEnrollmentGroup(testInstance.enrollmentGroup);
        }
        else
        {
            testInstance.individualEnrollment.setIotHubs(iothubsToFinishAt);
            provisioningServiceClient.createOrUpdateIndividualEnrollment(testInstance.individualEnrollment);
        }
    }
}
