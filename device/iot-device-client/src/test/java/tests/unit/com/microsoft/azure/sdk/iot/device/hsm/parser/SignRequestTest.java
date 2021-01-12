/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.hsm.parser;

import com.microsoft.azure.sdk.iot.device.hsm.parser.SignRequest;
import junit.framework.TestCase;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import javax.crypto.Mac;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertArrayEquals;

public class SignRequestTest
{
    @Mocked
    Mac mockedMac;

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
        final String expectedEncodedData = encodeBase64String(expectedData);

        new NonStrictExpectations()
        {
            {
                mockedMac.getAlgorithm();
                result = expectedAlgoString;
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
        assertArrayEquals(expectedEncodedData.getBytes(), request.getData());
    }
}
