/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.provisioning;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup8;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.setup.ProvisioningCommon;
import com.microsoft.azure.sdk.iot.common.tests.provisioning.ProvisioningTests;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;

import org.junit.After;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@TestGroup8
@RunWith(Parameterized.class)
public class ProvisioningClientSymmetricKeyAndroidRunner extends ProvisioningTests
{
    @Rule
    public Rerun count = new Rerun(3);
    
    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    public ProvisioningClientSymmetricKeyAndroidRunner(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        super(protocol, attestationType);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0} with {1}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isBasicTierHub = Boolean.parseBoolean(BuildConfig.IsBasicTierHub);
        provisioningServiceConnectionString = BuildConfig.DeviceProvisioningServiceConnectionString;
        provisioningServiceGlobalEndpoint = BuildConfig.DeviceProvisioningServiceGlobalEndpoint;
        provisioningServiceIdScope = BuildConfig.DeviceProvisioningServiceIdScope;
        provisioningServiceGlobalEndpointWithInvalidCert = BuildConfig.InvalidDeviceProvisioningServiceGlobalEndpoint;
        provisioningServiceWithInvalidCertConnectionString = BuildConfig.InvalidDeviceProvisioningServiceConnectionString;
        farAwayIotHubConnectionString = BuildConfig.FarAwayIotHubConnectionString;
        customAllocationWebhookUrl = BuildConfig.CustomAllocationWebhookUrl;

        return ProvisioningCommon.inputs(AttestationType.SYMMETRIC_KEY);
    }

    @After
    public void labelSnapshot()
    {
        reportHelper.label("Stopping App");
    }
}