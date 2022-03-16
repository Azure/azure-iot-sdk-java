/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.methods;

import com.google.gson.JsonPrimitive;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for method result class
 */
public class DirectMethodResponseTest
{
    /* Tests_SRS_METHODRESULT_21_001: [The constructor shall save the status and payload representing the method invoke result.] */
    /* Tests_SRS_METHODRESULT_21_002: [There is no restrictions for these values, it can be empty, or null.] */
    /* Tests_SRS_METHODRESULT_21_003: [The getStatus shall return the status stored by the constructor.] */
    /* Tests_SRS_METHODRESULT_21_004: [The getPayload shall return the payload stored by the constructor.] */
    @Test
    public void constructorCreatesNewMethodResult()
    {
        //act
        DirectMethodResponse directMethodResponse = new DirectMethodResponse(123, new JsonPrimitive("TestObject"));

        //assert
        assertNotNull(directMethodResponse);
        assertThat(directMethodResponse.getStatus(), is(123));
        assertThat(directMethodResponse.getPayloadAsJsonElement(), is(new JsonPrimitive("TestObject")));


    }

    @Test
    public void constructorCreatesNewMethodResult_NullStatus()
    {
        //act
        DirectMethodResponse directMethodResponse = new DirectMethodResponse(null, new JsonPrimitive("TestObject"));

        //assert
        assertNotNull(directMethodResponse);
        assertNull(directMethodResponse.getStatus());
        assertThat(directMethodResponse.getPayloadAsJsonElement(), is(new JsonPrimitive("TestObject")));
    }
    
    @Test
    public void constructorCreatesNewMethodResult_NullPayload()
    {
        //act
        DirectMethodResponse directMethodResponse = new DirectMethodResponse(123, null);

        //assert
        assertNotNull(directMethodResponse);
        assertThat(directMethodResponse.getStatus(), is(123));
        assertNull(directMethodResponse.getPayloadAsJsonElement());
    }
}
