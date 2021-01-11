// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.


package tests.unit.com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Device;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.PropertyCallBack;
import mockit.Deencapsulation;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * Unit tests for Device.java
 * Methods: 100%
 * Lines: 100%
 */
public class DeviceTest
{
    @Test
    public void constructorCreatesNewMapsForProperties()
    {
        //act
        Device testDev = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };

        //assert
        HashSet reported = Deencapsulation.getField(testDev, "reportedProp");
        HashMap desired = Deencapsulation.getField(testDev, "desiredProp");

        assertNotNull(reported);
        assertNotNull(desired);

    }

    /*
    **Tests_SRS_DEVICE_25_001: [**This method shall return a HashSet of properties that user has set by calling setReportedProp.**]**
     */
    @Test
    public void getReportedPropertiesReturnsMapsOfProperties()
    {
        //arrange
        Device testDev = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };

        Property test = new Property("RepProp1", "RepValue1");

        //act
        testDev.setReportedProp(test);
        HashSet<Property> testRepProp = testDev.getReportedProp();

        //assert
        assertTrue(testRepProp.contains(test));

    }

    /*
    **Tests_SRS_DEVICE_25_002: [**The function shall add the new property to the map.**]**
     */
    @Test
    public void setReportedPropertyAddsToReportedProperty()
    {
        //arrange
        Device testDev = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };

        Property test = new Property("RepProp1", "RepValue1");

        //act
        testDev.setReportedProp(test);

        //assert
        HashSet<Property> testRepProp = testDev.getReportedProp();
        assertTrue(testRepProp.contains(test));

    }

    /*
    **Tests_SRS_DEVICE_25_003: [**If the already existing property is altered and added then the this method shall replace the old one.**]**
     */
    @Test
    public void setReportedPropertyAddsToAlteredReportedProperty()
    {
        //arrange
        Device testDev = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };

        Property test = new Property("RepProp1", "RepValue1");
        testDev.setReportedProp(test);
        test.setValue("RepValue2");

        Property test2 = new Property("RepProp1", "RepValue2");

        //act
        testDev.setReportedProp(test2);

        //assert
        HashSet<Property> testRepProp = testDev.getReportedProp();
        assertEquals("RepValue2", testRepProp.iterator().next().getValue().toString());
        assertEquals(1, testRepProp.size());
    }

    /*
    **Tests_SRS_DEVICE_25_004: [**If the parameter reportedProp is null then this method shall throw IllegalArgumentException**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void setReportedPropertyCannotAddNullProperty()
    {
        //arrange
        Device testDev = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };

        //act
        testDev.setReportedProp(null);
    }

    /*
    **Tests_SRS_DEVICE_25_005: [**The function shall return the HashMap containing the property and its callback and context pair set by the user so far.**]**
     */
    @Test
    public void getDesiredPropertiesReturnsMapsOfProperties()
    {
        //arrange
        Device testDev = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };

        Property test = new Property("DesiredProp1", null);
        testDev.setDesiredPropertyCallback(test, testDev, null);

        //act
        HashMap<Property, Pair<PropertyCallBack<String, Object>, Object>> testDesiredMap = testDev.getDesiredProp();

        //assert
        assertNotNull(testDesiredMap);
        assertEquals(testDesiredMap.get(test).getKey(), testDev);
    }

    /*
    **Tests_SRS_DEVICE_25_006: [**The function shall add the property and its callback and context pair to the user map of desired properties.**]**
     */
    @Test
    public void setDesiredPropertyCallbackAddsToDesiredProperty()
    {
        //arrange
        Device testDev = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };

        Property test = new Property("DesiredProp1", null);

        //act
        testDev.setDesiredPropertyCallback(test, testDev, null);

        //assert
        HashMap<Property, Pair<PropertyCallBack<String, Object>, Object>> testDesiredMap = testDev.getDesiredProp();
        assertNotNull(testDesiredMap);
        assertEquals(testDesiredMap.get(test).getKey(), testDev);
    }

    /*
    **Tests_SRS_DEVICE_25_007: [**If the parameter desiredProp is null then this method shall throw IllegalArgumentException**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void setDesiredPropertyCallbackCannotAddNullProperty()
    {
        //arrange
        Device testDev = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };

        Property test = new Property("DesiredProp1", null);

        //act
        testDev.setDesiredPropertyCallback(null, testDev, null);

    }

    /*
    **Tests_SRS_DEVICE_25_008: [**This method shall add the parameters to the map even if callback and object pair are null**]**
     */
    @Test
    public void setDesiredPropertyCallbackCanAddNullPair()
    {
        //arrange
        Device testDev = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };

        Property test = new Property("DesiredProp1", null);

        //act
        testDev.setDesiredPropertyCallback(test, null, null);

        //assert
        HashMap<Property, Pair<PropertyCallBack<String, Object>, Object>> testDesiredMap = testDev.getDesiredProp();
        assertNotNull(testDesiredMap);
        assertNull(testDesiredMap.get(test).getKey());
    }


    @Test
    public void setDesiredPropertyCallbackCanAddNullCB()
    {
        //arrange
        Device testDev = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };

        Property test = new Property("DesiredProp1", null);

        //act
        testDev.setDesiredPropertyCallback(test, null, null);

        //assert
        HashMap<Property, Pair<PropertyCallBack<String, Object>, Object>> testDesiredMap = testDev.getDesiredProp();
        assertNotNull(testDesiredMap);
        assertNull(testDesiredMap.get(test).getKey());

    }

    //Tests_SRS_DEVICE_34_009: [The method shall remove all the reported and desired properties set by the user so far.]
    @Test
    public void freeEmptiesAllProperties()
    {
        //arrange
        Device testDev = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };

        Property testDes = new Property("DesiredProp1", null);
        Property testRep = new Property("DesiredProp1", null);
        testDev.setDesiredPropertyCallback(testDes, testDev, null);
        testDev.setReportedProp(testRep);

        //act
        testDev.clean();

        //assert
        HashMap<Property, Pair<PropertyCallBack<String, Object>, Object>> testDesiredMap = testDev.getDesiredProp();
        HashSet<Property> testRepMap = testDev.getReportedProp();

        assertTrue(testDesiredMap.isEmpty());
        assertTrue(testRepMap.isEmpty());
    }

}
