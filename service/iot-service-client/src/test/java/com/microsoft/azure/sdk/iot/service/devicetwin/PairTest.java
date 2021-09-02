/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import org.junit.Test;

import static org.junit.Assert.*;

public class PairTest
{

    /*
    **Tests_SRS_Pair_25_001: [**The constructor shall save the key and value representing this Pair.**]**
     */
    @Test
    public void constructorCreatesNewPair()
    {
        //act
        Pair testPair = new Pair("TestKey", "TestObject");

        //assert
        assertNotNull(testPair);
        assertEquals("TestKey", testPair.getKey());
        assertEquals("TestObject", testPair.getValue());
    }

    /*
    **Tests_SRS_Pair_25_002: [**If the key is null or empty, the constructor shall throw an IllegalArgumentException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfKeyNull()
    {
        //act
        Pair testPair = new Pair(null, "TestObject");

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfKeyEmpty()
    {
        //act
        Pair testPair = new Pair("", "TestObject");
    }

    /*
    **Tests_SRS_Pair_25_005: [**The function shall return the value for this Pair.**]**
     */
    @Test
    public void getValueGets()
    {
        //arrange
        Pair testPair = new Pair("TestKey", "TestObject");

        //act
        Object value = testPair.getValue();

        //assert
        assertNotNull(testPair);
        assertEquals("TestObject", value);

    }

    /*
    **Tests_SRS_Pair_25_006: [**The function shall overwrite the new value for old and return old value.**]**
     */
    @Test
    public void setValueSets()
    {
        //arrange
        Pair testPair = new Pair("TestKey", "TestObject");

        //act
        testPair.setValue("newTestObject");

        //assert
        Object value = testPair.getValue();
        assertNotNull(testPair);
        assertEquals("newTestObject", value);

    }

    /*
    **Tests_SRS_Pair_25_004: [**The function shall return the value of the key corresponding to this Pair.**]**
     */
    @Test
    public void getKeyGets()
    {
        //arrange
        Pair testPair = new Pair("TestKey", "TestObject");

        //act
        String key = testPair.getKey();

        //assert
        assertNotNull(testPair);
        assertEquals("TestKey", key);

    }


}
