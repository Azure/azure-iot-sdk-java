/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.provisioning;


import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import net.jcip.annotations.NotThreadSafe;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.DeviceProvisioningServiceTest;
import tests.integration.com.microsoft.azure.sdk.iot.provisioning.setup.ProvisioningCommon;

import java.util.Collection;

// TPM tests cannot be parallelized because each test consumes the TPM and only releases it when the test is done.
@NotThreadSafe
@DeviceProvisioningServiceTest
@RunWith(Parameterized.class)
public class ProvisioningTPMTests extends ProvisioningTests
{
    public ProvisioningTPMTests(ProvisioningDeviceClientTransportProtocol protocol, AttestationType attestationType)
    {
        super(protocol, attestationType);
    }

    //This overrides the inputs defined in the super class. This is done to only run the TPM tests.
    @Parameterized.Parameters(name = "{0}_{1}")
    public static Collection inputs()
    {
        return ProvisioningCommon.inputs(AttestationType.TPM);
    }
}
