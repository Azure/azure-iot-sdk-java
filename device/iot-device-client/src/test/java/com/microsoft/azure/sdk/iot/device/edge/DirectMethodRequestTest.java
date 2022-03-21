/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.edge;

import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

public class DirectMethodRequestTest
{
    final static String expectedMethodName = "some method";
    final static String expectedPayload = "some payload";

    // Tests_SRS_DIRECTMETHODREQUEST_34_001: [This constructor shall invoke the overloaded constructor with default values of responseTimeout=0 and connectionTimeout=0.]
    @Test
    public void constructorUsesDefaultTimeouts()
    {
        //act
        DirectMethodRequest request = new DirectMethodRequest(expectedMethodName, expectedPayload);

        //assert
        assertNull(Deencapsulation.getField(request, "responseTimeoutInSeconds"));
        assertNull(Deencapsulation.getField(request, "connectionTimeoutInSeconds"));
    }

    // Tests_SRS_DIRECTMETHODREQUEST_34_002: [If the provided methodName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyMethodName()
    {
        //act
        DirectMethodRequest request = new DirectMethodRequest("", expectedPayload);
    }

    // Tests_SRS_DIRECTMETHODREQUEST_34_002: [If the provided methodName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullMethodName()
    {
        //act
        DirectMethodRequest request = new DirectMethodRequest(null, expectedPayload);
    }

    // Tests_SRS_DIRECTMETHODREQUEST_34_003: [This constructor shall save the provided payload, methodname, and timeouts.]
    @Test
    public void constructorSavesArguments()
    {
        //arrange
        int expectedResponseTimeout = 3;
        int expectedConnectionTimeout = 4;

        //act
        DirectMethodRequest request = new DirectMethodRequest(expectedMethodName, expectedPayload, expectedResponseTimeout, expectedConnectionTimeout);

        //assert
        assertEquals(expectedResponseTimeout, (int) Deencapsulation.getField(request, "responseTimeoutInSeconds"));
        assertEquals(expectedConnectionTimeout, (int) Deencapsulation.getField(request, "connectionTimeoutInSeconds"));
        assertEquals(expectedMethodName, Deencapsulation.getField(request, "methodName"));
        assertEquals(expectedPayload, Deencapsulation.getField(request, "payload"));
    }
}
