// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import org.junit.Test;

import static org.junit.Assert.*;

public class PairTest
{
    /*
    **Tests_SRS_Pair_25_001: [**The constructor shall save the key and value representing this Pair.**]**
     */
    @Test
    public void constructorSetsKeyAndValue()
    {
        //act
        Pair<PairTest, String> testPair = new Pair<>(this, "thisObject");
        //assert
        assertNotNull(testPair);
        assertEquals(testPair.getKey(), this);
        assertEquals(testPair.getValue(), "thisObject");
    }

    /*
    **Tests_SRS_Pair_25_002: [**The function shall return the value of the key corresponding to this Pair.**]**
     */
    @Test
    public void getKeyGets()
    {
        //arrange
        Pair<PairTest, String> testPair = new Pair<>(this, "thisObject");
        //assert
        assertNotNull(testPair);
        assertEquals(testPair.getKey(), this);
    }

    /*
    **Tests_SRS_Pair_25_003: [**The function shall return the value for this Pair.**]**
     */
    @Test
    public void getValueGets()
    {
        //arrange
        Pair<PairTest, String> testPair = new Pair<>(this, "thisObject");
        //assert
        assertNotNull(testPair);
        assertEquals(testPair.getValue(), "thisObject");
    }

    /*
    **Tests_SRS_Pair_25_004: [**The function shall overwrite the new value for old and return old value.**]**
     */
    @Test
    public void setValueSets()
    {
        //arrange
        Pair<PairTest, String> testPair = new Pair<>(this, "thisObject");
        //act
        String value = testPair.setValue("thisNewObject");
        //assert
        assertNotNull(testPair);
        assertEquals(testPair.getValue(), "thisNewObject");
        assertTrue(value.equals("thisObject"));
    }
}
