/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.androidthings.provisioning;

import com.microsoft.azure.sdk.iot.androidthings.BuildConfig;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.setup.ProvisioningCommon;
import com.microsoft.azure.sdk.iot.common.tests.provisioning.ProvisioningTests;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ProvisioningClientX509ThingsRunner extends ProvisioningTests
{
    @Rule
    public Rerun count = new Rerun(3);

    public ProvisioningClientX509ThingsRunner(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        super(protocol, attestationType);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0} with {1}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        provisioningServiceConnectionString = BuildConfig.DeviceProvisioningServiceConnectionString;
        provisioningServiceGlobalEndpoint = BuildConfig.DeviceProvisioningServiceGlobalEndpoint;
        provisioningServiceIdScope = BuildConfig.DeviceProvisioningServiceIdScope;
        provisioningServiceGlobalEndpointWithInvalidCert = BuildConfig.InvalidDeviceProvisioningServiceGlobalEndpoint;
        provisioningServiceWithInvalidCertConnectionString = BuildConfig.InvalidDeviceProvisioningServiceConnectionString;

        return ProvisioningCommon.inputs(AttestationType.X509);
    }
}