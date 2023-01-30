/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm.parser;

import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ErrorResponseTest
{
    // Tests_SRS_HTTPHSMERRORRESPONSE_34_001: [This function shall return the saved message.]
    @Test
    public void getMessageWorks()
    {
        //arrange
        String expectedMessage = "some error message";
        ErrorResponse errorResponse = new ErrorResponse();
        Deencapsulation.setField(errorResponse, "message", expectedMessage);

        //act
        String actualMessage = errorResponse.getMessage();

        //assert
        assertEquals(expectedMessage, actualMessage);
    }
}
