/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.hsm.parser;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignRequest;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Ignore;
import org.junit.Test;

import javax.crypto.Mac;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class SignRequestTest
{
    @Mocked
    Mac mockedMac;

    @Mocked
    Base64 mockedBase64;

    // Tests_SRS_HTTPHSMSIGNREQUEST_34_001: [This function shall save the provided keyId.]
    // Tests_SRS_HTTPHSMSIGNREQUEST_34_002: [This function shall return the saved data.]
    // Tests_SRS_HTTPHSMSIGNREQUEST_34_003: [This function shall save the provided data after base64 encoding it.]
    // Tests_SRS_HTTPHSMSIGNREQUEST_34_004: [This function shall save the provided algo.]
    @Test
    public void gettersAndSettersWork()
    {
        //arrange
        final String expectedAlgoString = "some algorithm";
        final String expectedKeyId = "some key id";
        final byte[] expectedData = "some data".getBytes();
        final String expectedEncodedData = "some base64 encoded string";

        new NonStrictExpectations()
        {
            {
                mockedMac.getAlgorithm();
                result = expectedAlgoString;

                Base64.encodeBase64StringLocal(expectedData);
                result = expectedEncodedData;
            }
        };
        SignRequest request = new SignRequest();

        //act
        request.setAlgo(mockedMac);
        request.setKeyId(expectedKeyId);
        request.setData(expectedData);

        //assert
        assertEquals(mockedMac, Deencapsulation.getField(request, "algo"));
        assertEquals(expectedKeyId, Deencapsulation.getField(request, "keyId"));
        assertTrue(Arrays.equals(expectedEncodedData.getBytes(), request.getData()));
    }
}
