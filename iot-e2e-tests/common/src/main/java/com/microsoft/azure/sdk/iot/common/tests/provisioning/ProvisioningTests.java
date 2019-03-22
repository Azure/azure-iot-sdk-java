/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.provisioning;

import com.microsoft.azure.sdk.iot.common.helpers.*;
import com.microsoft.azure.sdk.iot.common.setup.ProvisioningCommon;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
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
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessageDpsIndividualOrGroup;
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
        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, null, getHostName(iotHubConnectionString), getHostName(farAwayIotHubConnectionString));
        cleanUpReprovisionedDeviceAndEnrollment(testInstance.provisionedDeviceId, EnrollmentType.INDIVIDUAL);
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

        ArrayList<String> expectedHubsToProvisionTo = new ArrayList<>();
        expectedHubsToProvisionTo.add(getHostName(iotHubConnectionString));
        expectedHubsToProvisionTo.add(getHostName(farAwayIotHubConnectionString));
        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, expectedHubsToProvisionTo);
        cleanUpReprovisionedDeviceAndEnrollment(testInstance.provisionedDeviceId, EnrollmentType.GROUP);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void individualEnrollmentProvisioningFlowWithEdgeDevice() throws Exception
    {
        DeviceCapabilities expectedDeviceCapabilities = new DeviceCapabilities();
        expectedDeviceCapabilities.setIotEdge(true);
        ArrayList<String> expectedHubsToProvisionTo = new ArrayList<>();
        expectedHubsToProvisionTo.add(getHostName(iotHubConnectionString));
        testInstance.securityProvider = getSecurityProviderInstance(EnrollmentType.INDIVIDUAL, null, null, null, expectedHubsToProvisionTo, expectedDeviceCapabilities);
        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, null, getHostName(iotHubConnectionString), getHostName(farAwayIotHubConnectionString));

        assertProvisionedDeviceCapabilitiesAreExpected(expectedDeviceCapabilities);

        cleanUpReprovisionedDeviceAndEnrollment(testInstance.provisionedDeviceId, EnrollmentType.INDIVIDUAL);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void enrollmentGroupProvisioningFlowWithEdgeDevice() throws Exception
    {
        if (testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            //tpm doesn't support group, and x509 group test has not been implemented yet
            return;
        }

        ArrayList<String> expectedHubsToProvisionTo = new ArrayList<>();
        expectedHubsToProvisionTo.add(getHostName(iotHubConnectionString));

        DeviceCapabilities expectedDeviceCapabilities = new DeviceCapabilities();
        expectedDeviceCapabilities.setIotEdge(true);
        testInstance.securityProvider = getSecurityProviderInstance(EnrollmentType.GROUP, AllocationPolicy.HASHED, null, null, expectedHubsToProvisionTo, expectedDeviceCapabilities);
        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, expectedHubsToProvisionTo);

        assertProvisionedDeviceCapabilitiesAreExpected(expectedDeviceCapabilities);

        cleanUpReprovisionedDeviceAndEnrollment(testInstance.provisionedDeviceId, EnrollmentType.GROUP);
    }

    @Test
    public void individualEnrollmentProvisioningFlowWithNonEdgeDevice() throws Exception
    {
        DeviceCapabilities expectedDeviceCapabilities = new DeviceCapabilities();
        expectedDeviceCapabilities.setIotEdge(false);
        ArrayList<String> expectedHubsToProvisionTo = new ArrayList<>();
        expectedHubsToProvisionTo.add(getHostName(iotHubConnectionString));
        testInstance.securityProvider = getSecurityProviderInstance(EnrollmentType.INDIVIDUAL, null, null, null, expectedHubsToProvisionTo, expectedDeviceCapabilities);
        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, null, getHostName(iotHubConnectionString), getHostName(farAwayIotHubConnectionString));

        assertProvisionedDeviceCapabilitiesAreExpected(expectedDeviceCapabilities);

        // delete enrollment
        cleanUpReprovisionedDeviceAndEnrollment(testInstance.provisionedDeviceId, EnrollmentType.INDIVIDUAL);
    }

    @Test
    public void enrollmentGroupProvisioningFlowWithNonEdgeDevice() throws Exception
    {
        if (testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            //tpm doesn't support group, and x509 group test has not been implemented yet
            return;
        }

        ArrayList<String> expectedHubsToProvisionTo = new ArrayList<>();
        expectedHubsToProvisionTo.add(getHostName(iotHubConnectionString));

        DeviceCapabilities expectedDeviceCapabilities = new DeviceCapabilities();
        expectedDeviceCapabilities.setIotEdge(false);
        testInstance.securityProvider = getSecurityProviderInstance(EnrollmentType.GROUP, AllocationPolicy.HASHED, null, null, expectedHubsToProvisionTo, expectedDeviceCapabilities);
        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, expectedHubsToProvisionTo);

        assertProvisionedDeviceCapabilitiesAreExpected(expectedDeviceCapabilities);

        // delete enrollment
        cleanUpReprovisionedDeviceAndEnrollment(testInstance.provisionedDeviceId, EnrollmentType.GROUP);
    }

    @Test
    public void individualEnrollmentWithInvalidRemoteServerCertificateFails() throws Exception
    {
        enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType.INDIVIDUAL);
    }

    @Test
    public void groupEnrollmentWithInvalidRemoteServerCertificateFails() throws Exception
    {
        enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType.GROUP);
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void groupEnrollmentProvisioningReprovisioningKeepTwin() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setMigrateDeviceData(true);
        reprovisionPolicy.setUpdateHubAssignment(true);

        reprovisioningFlow(EnrollmentType.GROUP, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
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
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void individualEnrollmentProvisioningReprovisioningKeepTwin() throws Exception
    {
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setMigrateDeviceData(true);
        reprovisionPolicy.setUpdateHubAssignment(true);

        reprovisioningFlow(EnrollmentType.INDIVIDUAL, null, reprovisionPolicy, null, getStartingHubs(), getHubsToReprovisionTo());
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
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
        customAllocationDefinition.setApiVersion(CUSTOM_ALLOCATION_WEBHOOK_API_VERSION);
        customAllocationDefinition.setWebhookUrl(customAllocationWebhookUrl);

        testInstance.securityProvider = getSecurityProviderInstance(enrollmentType, AllocationPolicy.CUSTOM, null, customAllocationDefinition, possibleStartingHubHostNames);

        registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, null, expectedHubToProvisionTo);
    }

    protected void reprovisioningFlow(EnrollmentType enrollmentType, AllocationPolicy allocationPolicy, ReprovisionPolicy reprovisionPolicy, CustomAllocationDefinition customAllocationDefinition, List<String> iothubsToStartAt, List<String> iothubsToFinishAt) throws Exception
    {
        if (enrollmentType == EnrollmentType.GROUP && testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            //tpm doesn't support group, and x509 group test has not been implemented yet
            return;
        }

        testInstance.securityProvider = getSecurityProviderInstance(enrollmentType, allocationPolicy, reprovisionPolicy, customAllocationDefinition, iothubsToStartAt);

        ProvisioningStatus provisioningStatus = registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpoint, true, iothubsToStartAt);

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

        cleanUpReprovisionedDeviceAndEnrollment(testInstance.provisionedDeviceId, enrollmentType);
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
            registerDevice(testInstance.protocol, testInstance.securityProvider, provisioningServiceGlobalEndpointWithInvalidCert, false, null, null);
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



    private void assertTwinIsCorrect(ReprovisionPolicy reprovisionPolicy, String expectedPropertyName, String expectedPropertyValue, boolean inFarAwayHub) throws IOException, IotHubException {
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

            DeviceTwinDevice device = new DeviceTwinDevice(testInstance.provisionedDeviceId);

            twinClient.getTwin(device);

            if (reprovisionPolicy.getMigrateDeviceData())
            {
                //twin change from before reprovisioning was migrated
                assertEquals(buildExceptionMessageDpsIndividualOrGroup("Twin size is unexpected value", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId),
                        1, device.getReportedProperties().size());
                boolean expectedKeyPairFound = false;
                for (Pair pair: device.getReportedProperties())
                {
                    expectedKeyPairFound |= (pair.getKey().equals(expectedPropertyName) && pair.getValue().equals(expectedPropertyValue));
                }
                assertTrue(buildExceptionMessageDpsIndividualOrGroup("Reported property that was sent was not found", getHostName(provisioningServiceConnectionString), testInstance.groupId, testInstance.registrationId),
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

    private void cleanUpReprovisionedDeviceAndEnrollment(String deviceId, EnrollmentType enrollmentType) throws IOException, IotHubException, ProvisioningServiceClientException {
        //delete provisioned device
        RegistryManager registryManagerFarAway = RegistryManager.createFromConnectionString(farAwayIotHubConnectionString);
        RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        if (deviceId != null && !deviceId.isEmpty())
        {
            try
            {
                registryManager.removeDevice(deviceId);
            }
            catch (IotHubNotFoundException e)
            {
                //device wasn't in hub, can ignore this exception
            }

            try
            {
                registryManagerFarAway.removeDevice(deviceId);
            }
            catch (IotHubNotFoundException e)
            {
                //device wasn't in hub, can ignore this exception
            }
        }

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

    private void sendReportedPropertyUpdate(String expectedReportedPropertyName, String expectedReportedPropertyValue, String iothubUri, String deviceId) throws InterruptedException, IOException, URISyntaxException {
        //hardcoded AMQP here only because we aren't testing this connection. We just need to open a connection to send a twin update so that
        // we can test if the twin updates carry over after reprovisioning
        DeviceClient deviceClient = DeviceClient.createFromSecurityProvider(iothubUri, deviceId, testInstance.securityProvider, IotHubClientProtocol.AMQPS);
        IotHubServicesCommon.openClientWithRetry(deviceClient);
        CountDownLatch twinLock = new CountDownLatch(2);
        deviceClient.startDeviceTwin(new StubTwinCallback(twinLock), null, new StubTwinCallback(twinLock), null);
        Set<Property> reportedProperties = new HashSet<>();
        reportedProperties.add(new Property(expectedReportedPropertyName, expectedReportedPropertyValue));
        deviceClient.sendReportedProperties(reportedProperties);
        twinLock.await(MAX_TWIN_PROPAGATION_WAIT_SECONDS, TimeUnit.SECONDS);
        deviceClient.close();
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
