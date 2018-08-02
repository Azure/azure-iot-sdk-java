/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.edge;

import com.microsoft.azure.sdk.iot.device.edge.MethodRequest;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

public class MethodRequestTest
{
    final static String expectedMethodName = "some method";
    final static String expectedPayload = "some payload";

    // Tests_SRS_DIRECTMETHODREQUEST_34_001: [This constructor shall invoke the overloaded constructor with default values of responseTimeout=0 and connectionTimeout=0.]
    @Test
    public void constructorUsesDefaultTimeouts()
    {
        //act
        MethodRequest request = new MethodRequest(expectedMethodName, expectedPayload);

        //assert
        assertNull(Deencapsulation.getField(request, "responseTimeoutInSeconds"));
        assertNull(Deencapsulation.getField(request, "connectionTimeoutInSeconds"));
    }

    // Tests_SRS_DIRECTMETHODREQUEST_34_002: [If the provided methodName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyMethodName()
    {
        //act
        MethodRequest request = new MethodRequest("", expectedPayload);
    }

    // Tests_SRS_DIRECTMETHODREQUEST_34_002: [If the provided methodName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullMethodName()
    {
        //act
        MethodRequest request = new MethodRequest(null, expectedPayload);
    }

    // Tests_SRS_DIRECTMETHODREQUEST_34_003: [This constructor shall save the provided payload, methodname, and timeouts.]
    @Test
    public void constructorSavesArguments()
    {
        //arrange
        int expectedResponseTimeout = 3;
        int expectedConnectionTimeout = 4;

        //act
        MethodRequest request = new MethodRequest(expectedMethodName, expectedPayload, expectedResponseTimeout, expectedConnectionTimeout);

        //assert
        assertEquals(expectedResponseTimeout, Deencapsulation.getField(request, "responseTimeoutInSeconds"));
        assertEquals(expectedConnectionTimeout, Deencapsulation.getField(request, "connectionTimeoutInSeconds"));
        assertEquals(expectedMethodName, Deencapsulation.getField(request, "methodName"));
        assertEquals(expectedPayload, Deencapsulation.getField(request, "payload"));

    }
}
