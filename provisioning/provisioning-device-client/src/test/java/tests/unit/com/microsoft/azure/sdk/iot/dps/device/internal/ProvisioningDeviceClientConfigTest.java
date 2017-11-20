/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.dps.device.internal;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import mockit.Mocked;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/*
    Unit test for ProvisioningDeviceClientConfigTest
    Coverage : 100% Method, 100% lines
 */
public class ProvisioningDeviceClientConfigTest
{
    private static final String endPoint = "testEndPoint";
    private static final String scopeId = "testScopeId";
    private static final ProvisioningDeviceClientTransportProtocol testProtocol = ProvisioningDeviceClientTransportProtocol.HTTPS;

    @Mocked
    SecurityProvider mockedSecurityProvider;

    @Mocked
    ProvisioningDeviceClientRegistrationCallback mockedRegistrationCB;

    //SRS_ProvisioningDeviceClientConfig_25_008: [ This method shall retrieve ProvisioningDeviceClientTransportProtocol. ]
    //SRS_ProvisioningDeviceClientConfig_25_009: [ This method shall set ProvisioningDeviceClientTransportProtocol. ]
    @Test
    public void setterAndGetterForProtocolSucceeds() throws Exception
    {
        //arrange
        ProvisioningDeviceClientConfig testConfig = new ProvisioningDeviceClientConfig();

        //act
        testConfig.setProtocol(testProtocol);

        //assert
        assertEquals(testProtocol, testConfig.getProtocol());
    }

    //SRS_ProvisioningDeviceClientConfig_25_004: [ This method shall retrieve provisioningServiceGlobalEndpoint. ]
    //SRS_ProvisioningDeviceClientConfig_25_005: [ This method shall set provisioningServiceGlobalEndpoint. ]
    @Test
    public void setterAndGetterForEndpointCBSucceeds() throws Exception
    {
        //arrange
        ProvisioningDeviceClientConfig testConfig = new ProvisioningDeviceClientConfig();

        //act
        testConfig.setProvisioningServiceGlobalEndpoint(endPoint);

        //assert
        assertEquals(endPoint, testConfig.getProvisioningServiceGlobalEndpoint());
    }

    //SRS_ProvisioningDeviceClientConfig_25_006: [ This method shall retrieve scopeId. ]
    //SRS_ProvisioningDeviceClientConfig_25_007: [ This method shall set scopeId. ]
    @Test
    public void setterAndGetterForScopeIdSucceeds() throws Exception
    {
        //arrange
        ProvisioningDeviceClientConfig testConfig = new ProvisioningDeviceClientConfig();

        //act
        testConfig.setIdScope(scopeId);

        //assert
        assertEquals(scopeId, testConfig.getIdScope());
    }

    //SRS_ProvisioningDeviceClientConfig_25_010: [ This method shall retrieve securityClient. ]
    //SRS_ProvisioningDeviceClientConfig_25_011: [ This method shall set securityClient. ]
    @Test
    public void setterAndGetterForSecurityClientSucceeds() throws Exception
    {
        //arrange
        ProvisioningDeviceClientConfig testConfig = new ProvisioningDeviceClientConfig();

        //act
        testConfig.setSecurityProvider(mockedSecurityProvider);

        //assert
        assertEquals(mockedSecurityProvider, testConfig.getSecurityProvider());
    }

    //SRS_ProvisioningDeviceClientConfig_25_001: [ This method shall save registrationCallback and registrationCallbackContext. ]
    //SRS_ProvisioningDeviceClientConfig_25_002: [ This method shall retrieve registrationCallback. ]
    //SRS_ProvisioningDeviceClientConfig_25_003: [ This method shall retrieve registrationCallbackContext. ]
    @Test
    public void setterAndGetterForRegistrationCBSucceeds() throws Exception
    {
        //arrange
        ProvisioningDeviceClientConfig testConfig = new ProvisioningDeviceClientConfig();

        //act
        testConfig.setRegistrationCallback(mockedRegistrationCB, null);

        //assert
        assertEquals(mockedRegistrationCB, testConfig.getRegistrationCallback());
        assertNull(testConfig.getRegistrationCallbackContext());
    }
}
