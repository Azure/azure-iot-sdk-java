/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for method result class
 */
public class MethodResultTest 
{
    /* Tests_SRS_METHODRESULT_21_001: [The constructor shall save the status and payload representing the method invoke result.] */
    /* Tests_SRS_METHODRESULT_21_002: [There is no restrictions for these values, it can be empty, or null.] */
    /* Tests_SRS_METHODRESULT_21_003: [The getStatus shall return the status stored by the constructor.] */
    /* Tests_SRS_METHODRESULT_21_004: [The getPayload shall return the payload stored by the constructor.] */
    @Test
    public void constructorCreatesNewMethodResult()
    {
        //act
        MethodResult methodResult = new MethodResult(123, "TestObject");

        //assert
        assertNotNull(methodResult);
        assertThat(methodResult.getStatus(), is(123));
        assertThat(methodResult.getPayload().toString(), is("TestObject"));
    }

    @Test
    public void constructorCreatesNewMethodResult_NullStatus()
    {
        //act
        MethodResult methodResult = new MethodResult(null, "TestObject");

        //assert
        assertNotNull(methodResult);
        assertNull(methodResult.getStatus());
        assertThat(methodResult.getPayload().toString(), is("TestObject"));
    }
    
    @Test
    public void constructorCreatesNewMethodResult_NullPayload()
    {
        //act
        MethodResult methodResult = new MethodResult(123, null);

        //assert
        assertNotNull(methodResult);
        assertThat(methodResult.getStatus(), is(123));
        assertNull(methodResult.getPayload());
    }
}
