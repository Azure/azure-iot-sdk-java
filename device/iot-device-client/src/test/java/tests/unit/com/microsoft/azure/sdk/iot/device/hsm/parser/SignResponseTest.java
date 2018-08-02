/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.hsm.parser;

import com.microsoft.azure.sdk.iot.device.hsm.parser.SignResponse;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class SignResponseTest
{
    // Tests_SRS_HTTPHSMSIGNRESPONSE_34_001: [This function shall return the saved digest.]
    @Test
    public void getDigestWorks()
    {
        //arrange
        String expectedDigest = "some digest";
        SignResponse response = new SignResponse();
        Deencapsulation.setField(response, "digest", expectedDigest);

        //act
        String actualDigest = response.getDigest();

        //assert
        assertEquals(expectedDigest, actualDigest);
    }
}
