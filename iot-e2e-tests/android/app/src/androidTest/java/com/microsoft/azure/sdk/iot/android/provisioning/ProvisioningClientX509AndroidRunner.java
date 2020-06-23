/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.provisioning;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup2;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Rerun;
import tests.integration.com.microsoft.azure.sdk.iot.provisioning.ProvisioningTests;
import tests.integration.com.microsoft.azure.sdk.iot.provisioning.setup.ProvisioningCommon;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@TestGroup2
@RunWith(Parameterized.class)
public class ProvisioningClientX509AndroidRunner extends ProvisioningTests
{
    @Rule
    public Rerun count = new Rerun(3);

    public ProvisioningClientX509AndroidRunner(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        super(protocol, attestationType);
    }

    //This overrides the inputs defined in the super class. This is done to split this large test group into symmetric key and x509 runners.
    @Parameterized.Parameters(name = "{0}_{1}")
    public static Collection inputs()
    {
        return ProvisioningCommon.inputs(AttestationType.X509); //tpm tests can't be run on Android until infrastructure is setup
    }
}