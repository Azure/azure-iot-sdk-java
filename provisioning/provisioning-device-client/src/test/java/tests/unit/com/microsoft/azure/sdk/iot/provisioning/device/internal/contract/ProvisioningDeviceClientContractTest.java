/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.contract;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.http.ContractAPIHttp;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/*
    Unit tests for ProvisioningDeviceClientContract
    Coverage : 45% line, 100% method
 */
@RunWith(JMockit.class)
public class ProvisioningDeviceClientContractTest
{
    private static final String TEST_SCOPE_ID = "testScopeID";
    private static final String TEST_URI = "testUri";

    @Mocked
    ProvisioningDeviceClientConfig mockedProvisioningDeviceClientConfig;

    @Test
    public void createContractSucceedsHttp() throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {

                mockedProvisioningDeviceClientConfig.getIdScope();
                result = TEST_SCOPE_ID;
                mockedProvisioningDeviceClientConfig.getProvisioningServiceGlobalEndpoint();
                result = TEST_URI;
                mockedProvisioningDeviceClientConfig.getProtocol();
                result = ProvisioningDeviceClientTransportProtocol.HTTPS;
            }
        };

        //act
        ProvisioningDeviceClientContract clientContract = ProvisioningDeviceClientContract.createProvisioningContract(mockedProvisioningDeviceClientConfig);

        //assert
        assertTrue(clientContract instanceof  ContractAPIHttp);

    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void createContractThrowsIfLowerLayerThrowsHttp() throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {

                mockedProvisioningDeviceClientConfig.getIdScope();
                result = new ProvisioningDeviceClientException("lower layer Exception");
                mockedProvisioningDeviceClientConfig.getProvisioningServiceGlobalEndpoint();
                result = TEST_URI;
                mockedProvisioningDeviceClientConfig.getProtocol();
                result = ProvisioningDeviceClientTransportProtocol.HTTPS;
            }
        };

        //act
        ProvisioningDeviceClientContract clientContract = ProvisioningDeviceClientContract.createProvisioningContract(mockedProvisioningDeviceClientConfig);

        //assert
    }
}
