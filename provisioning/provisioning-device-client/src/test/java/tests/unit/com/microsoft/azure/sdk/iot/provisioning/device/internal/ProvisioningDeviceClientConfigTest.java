/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal;

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
    private static final String END_POINT = "testEndPoint";
    private static final String SCOPE_ID = "testScopeId";
    private static final ProvisioningDeviceClientTransportProtocol TEST_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    private static final String CUSTOM_PAYLOAD = "{ \"dpsCustomPayload\": \"dataValue\" }";

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
        testConfig.setProtocol(TEST_PROTOCOL);

        //assert
        assertEquals(TEST_PROTOCOL, testConfig.getProtocol());
    }

    //SRS_ProvisioningDeviceClientConfig_25_004: [ This method shall retrieve provisioningServiceGlobalEndpoint. ]
    //SRS_ProvisioningDeviceClientConfig_25_005: [ This method shall set provisioningServiceGlobalEndpoint. ]
    @Test
    public void setterAndGetterForEndpointCBSucceeds() throws Exception
    {
        //arrange
        ProvisioningDeviceClientConfig testConfig = new ProvisioningDeviceClientConfig();

        //act
        testConfig.setProvisioningServiceGlobalEndpoint(END_POINT);

        //assert
        assertEquals(END_POINT, testConfig.getProvisioningServiceGlobalEndpoint());
    }

    //SRS_ProvisioningDeviceClientConfig_25_006: [ This method shall retrieve scopeId. ]
    //SRS_ProvisioningDeviceClientConfig_25_007: [ This method shall set scopeId. ]
    @Test
    public void setterAndGetterForScopeIdSucceeds() throws Exception
    {
        //arrange
        ProvisioningDeviceClientConfig testConfig = new ProvisioningDeviceClientConfig();

        //act
        testConfig.setIdScope(SCOPE_ID);

        //assert
        assertEquals(SCOPE_ID, testConfig.getIdScope());
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

    @Test
    public void setterAndGetterForCustomPayloadSucceeds() throws Exception
    {
        //arrange
        ProvisioningDeviceClientConfig testConfig = new ProvisioningDeviceClientConfig();

        //act
        testConfig.setCustomPayload(CUSTOM_PAYLOAD);

        //assert
        assertEquals(CUSTOM_PAYLOAD, testConfig.getCustomPayload());
    }

}
