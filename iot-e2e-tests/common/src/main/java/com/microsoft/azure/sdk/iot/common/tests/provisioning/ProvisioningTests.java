/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.provisioning;

import com.microsoft.azure.sdk.iot.common.helpers.IotHubServicesCommon;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.setup.ProvisioningCommon;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol.*;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProvisioningTests extends ProvisioningCommon
{
    public ProvisioningTests(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        super(protocol, attestationType);
    }

    @Test
    public void IndividualEnrollmentProvisioningFlow() throws Exception
    {
        SecurityProvider securityProvider = getSecurityProviderInstance(EnrollmentType.INDIVIDUAL);
        Thread.sleep(ENROLLMENT_PROPAGATION_DELAY_MS);
        ProvisioningStatus provisioningStatus = registerDevice(testInstance.protocol, securityProvider, provisioningServiceGlobalEndpoint);
        waitForRegistrationCallback(provisioningStatus);
        provisioningStatus.provisioningDeviceClient.closeNow();

        assertEquals(testInstance.provisionedDeviceId, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

        // Tests will not pass if the linked iothub to provisioning service and iothub setup to send/receive messages isn't same.
        assertEquals("Iothub Linked to provisioning service and IotHub in connection String are not same", getHostName(iotHubConnectionString),
                provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());

        // send messages over all protocols
        assertProvisionedDeviceWorks(provisioningStatus, securityProvider);

        // delete enrollment
        provisioningServiceClient.deleteIndividualEnrollment(testInstance.registrationId);
        registryManager.removeDevice(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());
    }

    @Test
    public void EnrollmentGroupProvisioningFlow() throws Exception
    {
        if (testInstance.attestationType != AttestationType.SYMMETRIC_KEY)
        {
            //tpm doesn't support group, and x509 group test has not been implemented yet
            return;
        }

        SecurityProvider securityProvider = getSecurityProviderInstance(EnrollmentType.GROUP);
        Thread.sleep(ENROLLMENT_PROPAGATION_DELAY_MS);

        ProvisioningStatus provisioningStatus = registerDevice(testInstance.protocol, securityProvider, provisioningServiceGlobalEndpoint);
        waitForRegistrationCallback(provisioningStatus);
        provisioningStatus.provisioningDeviceClient.closeNow();

        assertEquals(testInstance.registrationId, provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

        // Tests will not pass if the linked iothub to provisioning service and iothub setup to send/receive messages isn't same.
        assertEquals("Iothub Linked to provisioning service and IotHub in connection String are not same", getHostName(iotHubConnectionString),
                provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());

        // send messages over all protocols
        assertProvisionedDeviceWorks(provisioningStatus, securityProvider);

        // delete enrollment
        provisioningServiceClient.deleteEnrollmentGroup(testInstance.groupId);
        registryManager.removeDevice(provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());
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

    private void enrollmentWithInvalidRemoteServerCertificateFails(EnrollmentType enrollmentType) throws Exception
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

    private void assertProvisionedDeviceWorks(ProvisioningStatus provisioningStatus, SecurityProvider securityProvider) throws IOException, URISyntaxException, InterruptedException
    {
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
    }
}
