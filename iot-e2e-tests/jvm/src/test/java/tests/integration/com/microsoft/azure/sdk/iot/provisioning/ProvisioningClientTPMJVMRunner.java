/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.integration.com.microsoft.azure.sdk.iot.provisioning;

import com.microsoft.azure.sdk.iot.common.helpers.TestConstants;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.helpers.annotations.FlakeyTest;
import com.microsoft.azure.sdk.iot.common.setup.provisioning.ProvisioningCommon;
import com.microsoft.azure.sdk.iot.common.tests.provisioning.ProvisioningTests;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import net.jcip.annotations.NotThreadSafe;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@NotThreadSafe
@RunWith(Parameterized.class)
@FlakeyTest //The TPM simulator gets into a strange state sometimes which causes consecutive test failures. May be SDK bug, but needs investigation
public class ProvisioningClientTPMJVMRunner extends ProvisioningTests
{
    public ProvisioningClientTPMJVMRunner(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        super(protocol, attestationType);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0} with {1}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        provisioningServiceConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_ENV_VAR_NAME);
        provisioningServiceIdScope = Tools.retrieveEnvironmentVariableValue(DPS_ID_SCOPE_ENV_VAR_NAME);
        farAwayIotHubConnectionString = Tools.retrieveEnvironmentVariableValue(FAR_AWAY_IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        customAllocationWebhookUrl = Tools.retrieveEnvironmentVariableValue(CUSTOM_ALLOCATION_WEBHOOK_URL_VAR_NAME);
        provisioningServiceGlobalEndpointWithInvalidCert = Tools.retrieveEnvironmentVariableValue(DPS_GLOBAL_ENDPOINT_WITH_INVALID_CERT_ENV_VAR_NAME);
        provisioningServiceWithInvalidCertConnectionString = Tools.retrieveEnvironmentVariableValue(DPS_CONNECTION_STRING_WITH_INVALID_CERT_ENV_VAR_NAME);
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST, "false"));

        return ProvisioningCommon.inputs(AttestationType.TPM);
    }
}
