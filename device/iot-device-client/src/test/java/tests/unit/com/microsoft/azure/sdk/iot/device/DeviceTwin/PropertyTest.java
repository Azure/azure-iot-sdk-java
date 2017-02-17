// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PropertyTest
{
    /*
    **Tests_SRS_Property_25_001: [**The constructor shall save the key and value representing this property.**]**
     */
    @Test
    public void constructorSetsKeyAndValue()
    {
        //act
        Property testProp = new Property("TestProp", 1);

        //assert
        assertNotNull(testProp);
        assertEquals(testProp.getKey(), "TestProp");
        assertEquals(testProp.getValue(), 1);
    }

    /*
    **Tests_SRS_Property_25_002: [**If the key is null, the constructor shall throw an IllegalArgumentException.**]
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullKey()
    {
        //act
        Property testProp = new Property(null, 1);

    }

    /*
    **Tests_SRS_Property_25_006: [**If the key contains illegal unicode control characters i.e ' ', '.', '$', the constructor shall throw an IllegalArgumentException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidKey()
    {
        //act
        Property testProp = new Property("Key with space", 1);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidKey_2()
    {
        //act
        Property testProp = new Property("KeyWith$", 1);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidKey_3()
    {
        //act
        Property testProp = new Property("KKeyWith.", 1);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidKey_4()
    {
        //act
        Property testProp = new Property("", 1);

    }

    /*
    **Tests_SRS_Property_25_003: [**The function shall return the value of the key corresponding to this property.**]**
     */
    @Test
    public void getKeyGets()
    {
        //arrange
        Property testProp = new Property("TestProp", 1);

        //assert
        assertNotNull(testProp);
        assertEquals(testProp.getKey(), "TestProp");
    }

    /*
    **Tests_SRS_Property_25_004: [**The function shall return the value for this property.**]**
    */
    @Test
    public void getValueGets()
    {
        //arrange
        Property testProp = new Property("TestProp", 1);

        //assert
        assertNotNull(testProp);
        assertEquals(testProp.getValue(), 1);

    }

    /*
    **Tests_SRS_Property_25_005: [**The function shall overwrite the new value for old and return old value.**]**
     */
    @Test
    public void setValueSets()
    {
        //arrange
        Property testProp = new Property("TestProp", 1);

        //act
        testProp.setValue(2);

        //assert
        assertNotNull(testProp);
        assertEquals(testProp.getValue(), 2);

    }
}
