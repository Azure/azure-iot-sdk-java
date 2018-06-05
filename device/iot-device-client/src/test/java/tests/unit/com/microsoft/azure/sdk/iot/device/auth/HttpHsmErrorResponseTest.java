/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.HttpHsmErrorResponse;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class HttpHsmErrorResponseTest
{
    // Tests_SRS_HTTPHSMERRORRESPONSE_34_001: [This function shall return the saved message.]
    @Test
    public void getMessageWorks()
    {
        //arrange
        String expectedMessage = "some error message";
        HttpHsmErrorResponse errorResponse = new HttpHsmErrorResponse();
        Deencapsulation.setField(errorResponse, "message", expectedMessage);

        //act
        String actualMessage = errorResponse.getMessage();

        //assert
        assertEquals(expectedMessage, actualMessage);
    }
}
