/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.hsm.parser;

import com.microsoft.azure.sdk.iot.device.hsm.parser.SignRequest;
import mockit.Deencapsulation;
import mockit.Mocked;
import org.junit.Test;

import javax.crypto.Mac;

import static junit.framework.TestCase.assertEquals;

public class SignRequestTest
{
    @Mocked
    Mac mockedMac;

    // Tests_SRS_HTTPHSMSIGNREQUEST_34_001: [This function shall save the provided keyId.]
    // Tests_SRS_HTTPHSMSIGNREQUEST_34_002: [This function shall return the saved data.]
    // Tests_SRS_HTTPHSMSIGNREQUEST_34_003: [This function shall save the provided data.]
    // Tests_SRS_HTTPHSMSIGNREQUEST_34_004: [This function shall save the provided algo.]
    @Test
    public void gettersAndSettersWork()
    {
        //arrange
        SignRequest request = new SignRequest();
        String expectedKeyId = "some key id";
        byte[] expectedData = "some data".getBytes();

        //act
        request.setAlgo(mockedMac);
        request.setKeyId(expectedKeyId);
        request.setData(expectedData);

        //assert
        assertEquals(mockedMac, Deencapsulation.getField(request, "algo"));
        assertEquals(expectedKeyId, Deencapsulation.getField(request, "keyId"));
        assertEquals(expectedData, request.getData());
    }
}
