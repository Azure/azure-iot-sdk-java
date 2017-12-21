/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationResult;
import org.junit.Test;

import static org.junit.Assert.assertNull;

/*
    Unit tests for ProvisioningDeviceClientRegistrationResult
    Coverage : 100% methods, 100% lines
 */
public class ProvisioningDeviceClientRegistrationResultTest
{
    //SRS_ProvisioningDeviceClientRegistrationResult_25_001: [ The constructor shall instantiate empty and leave it inheritors to set appropriate values of private members. ]
    //SRS_ProvisioningDeviceClientRegistrationResult_25_002: [ This method shall retrieve iothubUri. ]
    //SRS_ProvisioningDeviceClientRegistrationResult_25_003: [ This method shall retrieve deviceId. ]
    //SRS_ProvisioningDeviceClientRegistrationResult_25_004: [ This method shall retrieve provisioningDeviceClientStatus. ]
    @Test
    public void constructorResultsInNullGetters() throws Exception
    {
        //act
        ProvisioningDeviceClientRegistrationResult testResult = new ProvisioningDeviceClientRegistrationResult();
        //assert
        assertNull(testResult.getDeviceId());
        assertNull(testResult.getIothubUri());
        assertNull(testResult.getProvisioningDeviceClientStatus());
    }
}
