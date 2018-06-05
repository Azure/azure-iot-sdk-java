/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.HttpHsmSignResponse;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class HttpHsmSignResponseTest
{
    // Tests_SRS_HTTPHSMSIGNRESPONSE_34_001: [This function shall return the saved digest.]
    @Test
    public void getDigestWorks()
    {
        //arrange
        byte[] expectedDigest = "some digest".getBytes();
        HttpHsmSignResponse response = new HttpHsmSignResponse();
        Deencapsulation.setField(response, "digest", expectedDigest);

        //act
        byte[] actualDigest = response.getDigest();

        //assert
        assertEquals(expectedDigest, actualDigest);
    }
}
