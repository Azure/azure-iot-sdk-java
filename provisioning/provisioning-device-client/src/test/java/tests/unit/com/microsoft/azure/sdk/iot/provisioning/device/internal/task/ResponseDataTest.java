/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ContractState;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ResponseData;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ContractState.DPS_REGISTRATION_UNKNOWN;
import static mockit.Deencapsulation.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/*
    Unit test for Response Data
    Coverage : 100% method, 100% line
 */
@RunWith(JMockit.class)
public class ResponseDataTest
{
    //SRS_ResponseData_25_001: [ Constructor shall create null responseData and set the ContractState to DPS_REGISTRATION_UNKNOWN. ]
    @Test
    public void constructorSucceeds() throws Exception
    {
        //act
        ResponseData testResponseData = newInstance(ResponseData.class);

        //assert
        assertNull(invoke(testResponseData, "getResponseData"));
        assertEquals(DPS_REGISTRATION_UNKNOWN, invoke(testResponseData, "getContractState"));
    }

    //SRS_ResponseData_25_004: [ This method shall save the value of ContractState. ]
    //SRS_ResponseData_25_005: [ This method shall return the saved value of ContractState. ]
    @Test
    public void setAndGetState(@Mocked ContractState mockedContractState) throws Exception
    {
        //arrange
        ResponseData testResponseData = newInstance(ResponseData.class);

        //act
        invoke(testResponseData, "setContractState", mockedContractState);
        //assert
        assertEquals(mockedContractState, invoke(testResponseData, "getContractState"));
    }

    //SRS_ResponseData_25_002: [ This method shall save the value of responseData. ]
    //SRS_ResponseData_25_003: [ This method shall return the saved value of responseData. ]
    @Test
    public void setAndGetResponseData() throws Exception
    {
        //arrange
        byte [] testData = "testData".getBytes();
        ResponseData testResponseData = newInstance(ResponseData.class);

        //act
        invoke(testResponseData, "setResponseData", testData);
        //assert
        assertEquals(testData, invoke(testResponseData, "getResponseData"));
    }
}
