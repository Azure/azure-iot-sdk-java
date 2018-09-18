/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.twin.ConfigurationInfo;
import com.microsoft.azure.sdk.iot.deps.twin.ConfigurationStatus;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit test for device twin device
 * 100% methods, 100% lines covered
 */
public class DeviceTwinDeviceTest
{
    /*
    **Tests_SRS_DEVICETWINDEVICE_25_003: [** The constructor shall create a new instance of twin object for this device and store the device id.**]**
     */
    @Test
    public void constructorCreatesNewDeviceGroup()
    {
        //act
        DeviceTwinDevice testDevice = new DeviceTwinDevice();

        //assert
        assertNotNull(testDevice);
        TwinCollection tagsMap = Deencapsulation.getField(testDevice, "tag");
        assertNull(tagsMap);
        TwinCollection repPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertNull(repPropMap);
        TwinCollection desPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertNull(desPropMap);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_003: [** The constructor shall create a new instance of twin object for this device and store the device id.**]**
     */
    @Test
    public void constructorCreatesNewDevice()
    {
        //arrange
        final String deviceId = "testDevice";

        //act
        DeviceTwinDevice testDevice = new DeviceTwinDevice(deviceId);

        //assert
        assertEquals(deviceId, Deencapsulation.getField(testDevice, "deviceId"));
        assertNotNull(testDevice);
        TwinCollection tagsMap = Deencapsulation.getField(testDevice, "tag");
        assertNull(tagsMap);
        TwinCollection repPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertNull(repPropMap);
        TwinCollection desPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertNull(desPropMap);
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_25_002: [** The constructor shall throw IllegalArgumentException if the input string is empty or null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCreatesNewDeviceEmptyDeviceId()
    {
        //arrange
        final String deviceId = "";

        //act
        new DeviceTwinDevice(deviceId);
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_25_002: [** The constructor shall throw IllegalArgumentException if the input string is empty or null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCreatesNewDeviceNullDeviceId()
    {
        //act
        new DeviceTwinDevice(null);
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_25_003: [** The constructor shall create a new instance of twin object for this device and store the device id.**]**
     */
    @Test
    public void constructorCreatesNewDeviceForModule()
    {
        //arrange
        final String deviceId = "testDevice";
        final String moduleId = "testModule";

        //act
        DeviceTwinDevice testDevice = new DeviceTwinDevice(deviceId, moduleId);

        //assert
        assertEquals(deviceId, Deencapsulation.getField(testDevice, "deviceId"));
        assertEquals(moduleId, Deencapsulation.getField(testDevice, "moduleId"));
        assertNotNull(testDevice);
        TwinCollection tagsMap = Deencapsulation.getField(testDevice, "tag");
        assertNull(tagsMap);
        TwinCollection repPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertNull(repPropMap);
        TwinCollection desPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertNull(desPropMap);
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_28__005: [** The constructor shall throw IllegalArgumentException if the deviceId is empty or null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCreatesNewDeviceForModuleEmptyDeviceId()
    {
        //arrange
        final String deviceId = "";
        final String moduleId = "somemodule";

        //act
        new DeviceTwinDevice(deviceId, moduleId);
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_28_005: [** The constructor shall throw IllegalArgumentException if the deviceId is empty or null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCreatesNewDeviceForModuleNullDeviceId()
    {
        //arrange
        final String moduleId = "somemodule";

        //act
        new DeviceTwinDevice(null, moduleId);
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_28__006: [** The constructor shall throw IllegalArgumentException if the moduleId is empty or null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCreatesNewDeviceForModuleEmptyModuleId()
    {
        //arrange
        final String deviceId = "somedevice";
        final String moduleId = "";

        //act
        new DeviceTwinDevice(deviceId, moduleId);
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_28_006: [** The constructor shall throw IllegalArgumentException if the moduleId is empty or null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCreatesNewDeviceForModuleNullModuleId()
    {
        //arrange
        final String deviceId = "somedevice";

        //act
        new DeviceTwinDevice(deviceId, null);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_004: [** This method shall return the device id **]**
     */
    @Test
    public void getDeviceIdGets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        String devId = testDevice.getDeviceId();

        //assert
        assertTrue(devId.equals("testDevice"));

    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_28_001: [** This method shall return the module id **]**
     */
    @Test
    public void getModuleIdGets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice", "testModule");

        //act
        String moduleId = testDevice.getModuleId();

        //assert
        assertTrue(moduleId.equals("testModule"));

    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_21_030: [** The seteTag shall store the eTag.**]**
     */
    @Test
    public void setETagSets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        testDevice.setETag("validETag");

        //assert
        assertEquals("validETag", Deencapsulation.getField(testDevice, "eTag"));
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_21_029: [** The seteTag shall throw IllegalArgumentException if the input string is empty or null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void setETagNullThrows()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        testDevice.setETag(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void setETagEmptyThrows()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        testDevice.setETag("");
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_21_031: [** The geteTag shall return the stored eTag.**]**
     */
    @Test
    public void getETagGets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        testDevice.setETag("validETag");

        //act
        String etag = testDevice.getETag();

        //assert
        assertEquals("validETag", etag);

    }

    @Test
    public void getETagGetsNull()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        String etag = testDevice.getETag();

        //assert
        assertNull(etag);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_21_032: [** The setVersion shall store the Twin version.**]**
     */
    @Test
    public void setVersionSets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        Deencapsulation.invoke(testDevice, "setVersion", 10);

        //assert
        assertEquals(10L, (long)(Integer)Deencapsulation.getField(testDevice, "version"));
    }

    @Test
    public void setVersionSetsZero()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        Deencapsulation.invoke(testDevice, "setVersion", 0);

        //assert
        assertEquals(0L, (long)(Integer)Deencapsulation.getField(testDevice, "version"));
    }

    @Test
    public void setVersionSetsNull()
    {
        //arrange
        Integer version = null;
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        Deencapsulation.invoke(testDevice, "setVersion", new Class[]{Integer.class}, version);

        //assert
        assertNull(Deencapsulation.getField(testDevice, "version"));
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_21_033: [** The getVersion shall return the stored Twin version.**]**
     */
    @Test
    public void getVersionGets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Deencapsulation.invoke(testDevice, "setVersion", 10);

        //act
        Integer version = testDevice.getVersion();

        //assert
        assertEquals(10L, (long)version);

    }

    @Test
    public void getVersionGetsNull()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        Integer version = testDevice.getVersion();

        //assert
        assertNull(version);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_009: [** This method shall convert the tags map to a set of pairs and return with it. **]**
     */
    @Test
    public void getTagsGets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag", "tagObject"));
        testDevice.setTags(testTags);

        //act
        Set<Pair> actualTags = testDevice.getTags();

        //assert
        assertEquals(testTags.size(), actualTags.size());
        for(Pair test : actualTags)
        {
            assertTrue(test.getKey().equals("testTag"));
            assertTrue(test.getValue().equals("tagObject"));
        }
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_010: [** If the tags map is null then this method shall return empty set of pairs.**]**
     */
    @Test
    public void getTagsGetsEmptyIfNotPresent()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        Set<Pair> actualTags = testDevice.getTags();

        //assert
        assertNotNull(actualTags);
        assertTrue(actualTags.size() == 0);
    }

    /*
     **Codes_SRS_DEVICETWINDEVICE_21_034: [** If the tags map is null then this method shall throw IllegalArgumentException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void getTagsVersionReturnsNullIfNoTags()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        Integer version = testDevice.getTagsVersion();

        //assert
    }

    @Test
    public void getTagsVersionReturnsNullIfNoVersionInTheTags()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag", "tagObject"));
        testDevice.setTags(testTags);

        //act
        Integer version = testDevice.getTagsVersion();

        //assert
        assertNull(version);
    }

    /*
     **Codes_SRS_DEVICETWINDEVICE_21_035: [** The method shall return the version in the tag TwinCollection.**]**
     */
    @Test
    public void getTagsVersionReturnsValidVersion(@Mocked final TwinCollection mockedTwinCollection)
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Deencapsulation.invoke(testDevice, "setTags", mockedTwinCollection);

        new NonStrictExpectations()
        {
            {
                mockedTwinCollection.getVersion();
                result = 5;
            }
        };

        //act
        Integer version = testDevice.getTagsVersion();

        //assert
        assertEquals(5, (int)version);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_013: [** This method shall convert the desiredProperties map to a set of pairs and return with it. **]**
     */
    @Test
    public void getDesiredPropGets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Set<Pair> testDesProp = new HashSet<>();
        testDesProp.add(new Pair("testDes", "desObject"));
        testDevice.setDesiredProperties(testDesProp);

        //act
        Set<Pair> actualDesProp = testDevice.getDesiredProperties();

        //assert
        assertEquals(testDesProp.size(), actualDesProp.size());
        for(Pair test : actualDesProp)
        {
            assertTrue(test.getKey().equals("testDes"));
            assertTrue(test.getValue().equals("desObject"));
        }
    }

    /*
    Tests_SRS_DEVICETWINDEVICE_25_014: [ If the desiredProperties map is null then this method shall return empty set of pairs.]
     */
    @Test
    public void getDesiredReturnsEmptyIfNullMap()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        Set<Pair> actualDesProp = testDevice.getDesiredProperties();

        //assert
        assertNotNull(actualDesProp);
        assertTrue(actualDesProp.size() == 0);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_005: [** This method shall convert the reported properties map to a set of pairs and return with it. **]**
     */
    @Test
    public void getReportedPropGets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        TwinCollection repMap = new TwinCollection();
        repMap.put("testRep", "repObject");
        Deencapsulation.setField(testDevice, "reportedProperties", repMap);

        //act
        Set<Pair> actualRepProp = testDevice.getReportedProperties();

        //assert
        assertEquals(repMap.size(), actualRepProp.size());
        for(Pair test : actualRepProp)
        {
            assertTrue(test.getKey().equals("testRep"));
            assertTrue(test.getValue().equals("repObject"));
        }
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_006: [** If the reported properties map is null then this method shall return empty set of pairs.**]**
     */
    @Test
    public void getReportedPropGetsEmptyIfNotPresent()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        Set<Pair> actualRepProp = testDevice.getReportedProperties();

        //assert
        assertNotNull(actualRepProp);
        assertTrue(actualRepProp.size() == 0);
    }

    /*
     **Codes_SRS_DEVICETWINDEVICE_21_038: [** If the reported properties is null then this method shall throw IllegalArgumentException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void getReportedPropertiesVersionReturnsNullIfNoReportedProperties()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        Integer version = testDevice.getReportedPropertiesVersion();

        //assert
    }

    @Test
    public void getReportedPropertiesVersionReturnsNullIfNoVersionInTheReportedProperties(@Mocked final TwinCollection mockedTwinCollection)
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Set<Pair> testReported = new HashSet<>();
        testReported.add(new Pair("testTag", "tagObject"));
        Deencapsulation.invoke(testDevice, "setReportedProperties", mockedTwinCollection);
        new NonStrictExpectations()
        {
            {
                mockedTwinCollection.getVersion();
                result = null;
            }
        };

        //act
        Integer version = testDevice.getReportedPropertiesVersion();

        //assert
        assertNull(version);
    }

    /*
     **Codes_SRS_DEVICETWINDEVICE_21_039: [** The method shall return the version in the reported properties TwinCollection.**]**
     */
    @Test
    public void getReportedPropertiesVersionReturnsValidVersion(@Mocked final TwinCollection mockedTwinCollection)
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Deencapsulation.invoke(testDevice, "setReportedProperties", mockedTwinCollection);

        new NonStrictExpectations()
        {
            {
                mockedTwinCollection.getVersion();
                result = 5;
            }
        };

        //act
        Integer version = testDevice.getReportedPropertiesVersion();

        //assert
        assertEquals(5, (int)version);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_007: [** This method shall convert the set of pairs of tags to a map and save it. **]**
     */
    @Test
    public void setTagsSets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag", "tagObject"));

        //act
        testDevice.setTags(testTags);

        //assert
        Set<Pair> actualTags = testDevice.getTags();
        assertEquals(testTags.size(), actualTags.size());
        for(Pair test : actualTags)
        {
            assertTrue(test.getKey().equals("testTag"));
            assertTrue(test.getValue().equals("tagObject"));
        }

    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_008: [** If the tags Set is null then this method shall throw IllegalArgumentException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void setTagsThrowsIsNullInput()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Set<Pair> testTags = null;

        //act
        testDevice.setTags(testTags);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_011: [** This method shall convert the set of pairs of desiredProperties to a map and save it. **]**
     */
    @Test
    public void setDesiredPropSets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Set<Pair> testDesProp = new HashSet<>();
        testDesProp.add(new Pair("testDes", "desObject"));

        //act
        testDevice.setDesiredProperties(testDesProp);

        //assert
        Set<Pair> actualDesProp = testDevice.getDesiredProperties();
        assertEquals(testDesProp.size(), actualDesProp.size());
        for(Pair test : actualDesProp)
        {
            assertTrue(test.getKey().equals("testDes"));
            assertTrue(test.getValue().equals("desObject"));
        }

    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_012: [** If the desiredProperties Set is null then this method shall throw IllegalArgumentException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void setDesiredThrowsIfNullInput()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Set<Pair> testDesProp = null;


        //act
        testDevice.setDesiredProperties(testDesProp);

    }

    /*
     **Codes_SRS_DEVICETWINDEVICE_21_036: [** If the desired properties is null then this method shall throw IllegalArgumentException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void getDesiredPropertiesVersionReturnsNullIfNoDesiredProperties()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        Integer version = testDevice.getDesiredPropertiesVersion();

        //assert
    }

    @Test
    public void getDesiredPropertiesVersionReturnsNullIfNoVersionInTheDesiredProperties()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Set<Pair> testDesired = new HashSet<>();
        testDesired.add(new Pair("testTag", "tagObject"));
        testDevice.setDesiredProperties(testDesired);

        //act
        Integer version = testDevice.getDesiredPropertiesVersion();

        //assert
        assertNull(version);
    }

    /*
     **Codes_SRS_DEVICETWINDEVICE_21_037: [** The method shall return the version in the desired properties TwinCollection.**]**
     */
    @Test
    public void getDesiredPropertiesVersionReturnsValidVersion(@Mocked final TwinCollection mockedTwinCollection)
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Deencapsulation.invoke(testDevice, "setDesiredProperties", mockedTwinCollection);

        new NonStrictExpectations()
        {
            {
                mockedTwinCollection.getVersion();
                result = 5;
            }
        };

        //act
        Integer version = testDevice.getDesiredPropertiesVersion();

        //assert
        assertEquals(5, (int)version);
    }

    @Test
    public void setReportedPropSets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        TwinCollection testRepProp = new TwinCollection();
        testRepProp.put("testRep", "repObject");

        //act
        Deencapsulation.invoke(testDevice, "setReportedProperties", testRepProp);

        //assert
        Set<Pair> actualRepProp = testDevice.getReportedProperties();
        assertEquals(testRepProp.size(), actualRepProp.size());
        for(Pair test : actualRepProp)
        {
            assertTrue(test.getKey().equals("testRep"));
            assertTrue(test.getValue().equals("repObject"));
        }

    }

    @Test
    public void settersAlwaysCreatesNewMaps()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Set<Pair> testDesProp = new HashSet<>();
        testDesProp.add(new Pair("testDes", "desObject"));

        //act
        testDevice.setDesiredProperties(testDesProp);

        //assert
        Set<Pair> actualDesProp = testDevice.getDesiredProperties();
        assertEquals(testDesProp.size(), actualDesProp.size());
        assertNotEquals(testDesProp, actualDesProp);

    }

    @Test
    public void gettersAlwaysCreatesNewSets()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        Set<Pair> testDesProp = new HashSet<>();
        testDesProp.add(new Pair("testDes", "desObject"));
        testDevice.setDesiredProperties(testDesProp);

        //act
        Set<Pair> actualDesProp = testDevice.getDesiredProperties();

        //assert
        assertEquals(testDesProp.size(), actualDesProp.size());
        assertNotEquals(testDesProp, actualDesProp);

    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_025: [** This method shall return the tags map**]**
     */
    @Test
    public void getterGetsMapsTags()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");
        Deencapsulation.invoke(testDevice, "setTags", testTags);

        //act
        TwinCollection actualTags = Deencapsulation.invoke(testDevice, "getTagsMap");

        //assert
        assertEquals(testTags.size(), actualTags.size());

        for(Map.Entry<String, Object> test : actualTags.entrySet())
        {
            assertTrue(test.getKey().equals("testTag"));
            assertTrue(test.getValue().equals("tagObject"));
        }
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_024: [** This method shall save the tags map**]**
     */
    @Test
    public void setterSetsMapsTags()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");

        //act
        Deencapsulation.invoke(testDevice, "setTags", testTags);

        //assert
        TwinCollection actualTags = Deencapsulation.invoke(testDevice, "getTagsMap");
        assertEquals(testTags.size(), actualTags.size());

        for(Map.Entry<String, Object> test : actualTags.entrySet())
        {
            assertTrue(test.getKey().equals("testTag"));
            assertTrue(test.getValue().equals("tagObject"));
        }
    }

    @Test
    public void clearClearsMapsTags()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");
        Deencapsulation.invoke(testDevice, "setTags", testTags);

        //act
        testDevice.clearTags();

        //assert
        TwinCollection actualTags = Deencapsulation.invoke(testDevice, "getDesiredMap");
        assertNull(actualTags);
        Set<Pair> actualTagsSet = testDevice.getTags();
        assertTrue(actualTagsSet.size() == 0);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_026: [** This method shall return the reportedProperties map**]**
     */
    @Test
    public void getterGetsMapsRep()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        TwinCollection testRep = new TwinCollection();
        testRep.put("testRep", "repObject");
        Deencapsulation.invoke(testDevice, "setReportedProperties", testRep);

        //act
        TwinCollection actualTags = Deencapsulation.invoke(testDevice, "getReportedMap");

        //assert
        assertEquals(testRep.size(), actualTags.size());

        for(Map.Entry<String, Object> test : actualTags.entrySet())
        {
            assertTrue(test.getKey().equals("testRep"));
            assertTrue(test.getValue().equals("repObject"));
        }
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_022: [** This method shall save the reportedProperties map**]**
     */
    @Test
    public void setterSetsMapsRep()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        TwinCollection testRep = new TwinCollection();
        testRep.put("testRep", "repObject");

        //act
        Deencapsulation.invoke(testDevice, "setReportedProperties", testRep);

        //assert
        TwinCollection actualTags = Deencapsulation.invoke(testDevice, "getReportedMap");

        assertEquals(testRep.size(), actualTags.size());

        for(Map.Entry<String, Object> test : actualTags.entrySet())
        {
            assertTrue(test.getKey().equals("testRep"));
            assertTrue(test.getValue().equals("repObject"));
        }
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_25_026: [** This method shall return the reportedProperties map**]**
     */
    @Test
    public void getterGetsConfigurations()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        ConfigurationInfo info = new ConfigurationInfo();
        info.setStatus(ConfigurationStatus.TARGETED);
        Map<String, ConfigurationInfo> configs = new HashMap<String, ConfigurationInfo>(){{put("abc",info);}};
        Deencapsulation.setField(testDevice, "configurations", configs);

        //act
        Map<String, ConfigurationInfo> actual = testDevice.getConfigurations();

        //assert
        assertEquals(1, actual.size());

        for(Map.Entry<String, ConfigurationInfo> test : actual.entrySet())
        {
            assertTrue(test.getKey().equals("abc"));
            assertTrue(test.getValue().equals(info));
        }
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_28_003: [** The setCapabilities shall store the device capabilities.**]**
     */
    @Test
    public void setterSetsCapabilities()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        DeviceCapabilities cap = new DeviceCapabilities();
        cap.setIotEdge(true);

        //act
        Deencapsulation.invoke(testDevice, "setCapabilities", new Class[]{DeviceCapabilities.class}, cap);

        //assert
        assertEquals(cap, Deencapsulation.getField(testDevice, "capabilities"));
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_28_004: [** The getCapabilities shall return the stored capabilities.**]**
     */
    @Test
    public void getterGetsCapabilities()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        DeviceCapabilities cap = new DeviceCapabilities();
        cap.setIotEdge(true);
        Deencapsulation.setField(testDevice, "capabilities", cap);

        //act
        DeviceCapabilities actual = testDevice.getCapabilities();

        //assert
        assertEquals(cap, actual);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_027: [** This method shall return the desiredProperties map**]**
     */
    @Test
    public void getterGetsMapsDes()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        TwinCollection testRep = new TwinCollection();
        testRep.put("testRep", "repObject");
        Deencapsulation.invoke(testDevice, "setDesiredProperties", testRep);

        //act
        TwinCollection actualTags = Deencapsulation.invoke(testDevice, "getDesiredMap");

        //assert
        assertEquals(testRep.size(), actualTags.size());

        for(Map.Entry<String, Object> test : actualTags.entrySet())
        {
            assertTrue(test.getKey().equals("testRep"));
            assertTrue(test.getValue().equals("repObject"));
        }
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_023: [** This method shall save the desiredProperties map**]**
     */
    @Test
    public void setterSetsMapsDes()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        TwinCollection testRep = new TwinCollection();
        testRep.put("testRep", "repObject");

        //act
        Deencapsulation.invoke(testDevice, "setDesiredProperties", testRep);

        //assert
        TwinCollection actualTags = Deencapsulation.invoke(testDevice, "getDesiredMap");

        assertEquals(testRep.size(), actualTags.size());

        for(Map.Entry<String, Object> test : actualTags.entrySet())
        {
            assertTrue(test.getKey().equals("testRep"));
            assertTrue(test.getValue().equals("repObject"));
        }
    }

    @Test
    public void clearClearsMapsDes()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        TwinCollection testRep = new TwinCollection();
        testRep.put("testRep", "repObject");
        Deencapsulation.invoke(testDevice, "setDesiredProperties", testRep);

        //act
        testDevice.clearDesiredProperties();

        //assert
        TwinCollection actualTags = Deencapsulation.invoke(testDevice, "getDesiredMap");

        assertNull(actualTags);
        Set<Pair> actualTagsSet = testDevice.getDesiredProperties();
        assertTrue(actualTagsSet.size() == 0);
    }

    @Test
    public void clearTwinClears()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");
        TwinCollection testRep = new TwinCollection();
        testRep.put("testKey", "testObject");
        Deencapsulation.invoke(testDevice, "setDesiredProperties", testRep);
        Deencapsulation.invoke(testDevice, "setTags", testRep);

        //act
        testDevice.clearTwin();

        //assert
        TwinCollection actualTags = Deencapsulation.invoke(testDevice, "getTagsMap");
        TwinCollection actualDes = Deencapsulation.invoke(testDevice, "getDesiredMap");

        assertNull(actualTags);
        assertNull(actualDes);
        Set<Pair> actualTagsSet = testDevice.getTags();
        assertTrue(actualTagsSet.size() == 0);
        Set<Pair> actualDesSet = testDevice.getDesiredProperties();
        assertTrue(actualDesSet.size() == 0);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_015: [** This method shall append device id, etag, version, tags, desired and reported properties to string (if present) and return **]**
    */
    @Test
    public void toStringReturnsAllNoETag()
    {
        //arrange
        final String expectedModuleId = "someModuleId";
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        Set<Pair> testDesProp = new HashSet<>();
        testDesProp.add(new Pair("testDes1", "desObject1"));
        testDesProp.add(new Pair("testDes2", "desObject2"));
        testDevice.setDesiredProperties(testDesProp);

        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag1", "tagObject1"));
        testTags.add(new Pair("testTag2", "tagObject2"));
        testDevice.setTags(testTags);

        Deencapsulation.setField(testDevice, "moduleId", expectedModuleId);

        //act
        String testDeviceString = testDevice.toString();

        //assert
        assertTrue(testDeviceString.contains("testDevice"));
        assertTrue(testDeviceString.contains("testDes1"));
        assertTrue(testDeviceString.contains("desObject1"));
        assertTrue(testDeviceString.contains("testDes2"));
        assertTrue(testDeviceString.contains("desObject2"));
        assertTrue(testDeviceString.contains("testTag1"));
        assertTrue(testDeviceString.contains("tagObject1"));
        assertTrue(testDeviceString.contains("testTag2"));
        assertTrue(testDeviceString.contains("tagObject2"));
        assertTrue(testDeviceString.contains(expectedModuleId));
    }

    @Test
    public void toStringReturnsAll()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        testDevice.setETag("validEtag");
        Deencapsulation.invoke(testDevice, "setVersion", 10);

        Set<Pair> testDesProp = new HashSet<>();
        testDesProp.add(new Pair("testDes1", "desObject1"));
        testDesProp.add(new Pair("testDes2", "desObject2"));
        testDevice.setDesiredProperties(testDesProp);

        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag1", "tagObject1"));
        testTags.add(new Pair("testTag2", "tagObject2"));
        testDevice.setTags(testTags);

        //act
        String testDeviceString = testDevice.toString();

        //assert
        assertTrue(testDeviceString.contains("testDevice"));
        assertTrue(testDeviceString.contains("validEtag"));
        assertTrue(testDeviceString.contains("10"));
        assertTrue(testDeviceString.contains("testDes1"));
        assertTrue(testDeviceString.contains("desObject1"));
        assertTrue(testDeviceString.contains("testDes2"));
        assertTrue(testDeviceString.contains("desObject2"));
        assertTrue(testDeviceString.contains("testTag1"));
        assertTrue(testDeviceString.contains("tagObject1"));
        assertTrue(testDeviceString.contains("testTag2"));
        assertTrue(testDeviceString.contains("tagObject2"));
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_016: [** This method shall convert the tags map to string (if present) and return **]**
     */
    @Test
    public void tagsToStringReturnsTags()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag1", "tagObject1"));
        testTags.add(new Pair("testTag2", "tagObject2"));
        testDevice.setTags(testTags);

        //act
        String testDeviceString = testDevice.tagsToString();

        //assert
        assertTrue(testDeviceString.contains("testTag1"));
        assertTrue(testDeviceString.contains("tagObject1"));
        assertTrue(testDeviceString.contains("testTag2"));
        assertTrue(testDeviceString.contains("tagObject2"));
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_017: [** This method shall return an empty string if tags map is empty or null and return **]**
     */
    @Test
    public void tagsToStringReturnsEmptyIfTagsEmpty()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        String testDeviceString = testDevice.tagsToString();

        //assert
        assertTrue(testDeviceString.length() == 0);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_018: [** This method shall convert the desiredProperties map to string (if present) and return **]**
     */
    @Test
    public void desiredToStringReturnsDesired()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        Set<Pair> testDesProp = new HashSet<>();
        testDesProp.add(new Pair("testDes1", "desObject1"));
        testDesProp.add(new Pair("testDes2", "desObject2"));
        testDevice.setDesiredProperties(testDesProp);

        //act
        String testDeviceString = testDevice.desiredPropertiesToString();

        //assert
        assertTrue(testDeviceString.contains("testDes1"));
        assertTrue(testDeviceString.contains("desObject1"));
        assertTrue(testDeviceString.contains("testDes2"));
        assertTrue(testDeviceString.contains("desObject2"));

    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_019: [** This method shall return an empty string if desiredProperties map is empty or null and return **]**
     */
    @Test
    public void desToStringReturnsEmptyIfTagsEmpty()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        String testDeviceString = testDevice.desiredPropertiesToString();

        //assert
        assertTrue(testDeviceString.length() == 0);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_020: [** This method shall convert the reportedProperties map to string (if present) and return **]**
     */
    @Test
    public void reportedToStringReturnsReported()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        TwinCollection testRepProp = new TwinCollection();
        testRepProp.put("testRep1", "repObject1");
        testRepProp.put("testRep2", "repObject2");

        Deencapsulation.invoke(testDevice, "setReportedProperties", testRepProp);

        //act
        String testDeviceString = testDevice.reportedPropertiesToString();

        //assert
        assertTrue(testDeviceString.contains("testRep1"));
        assertTrue(testDeviceString.contains("repObject1"));
        assertTrue(testDeviceString.contains("testRep2"));
        assertTrue(testDeviceString.contains("repObject2"));

    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_021: [** This method shall return an empty string if reportedProperties map is empty or null and return **]**
     */
    @Test
    public void repToStringReturnsEmptyIfTagsEmpty()
    {
        //arrange
        DeviceTwinDevice testDevice = new DeviceTwinDevice("testDevice");

        //act
        String testDeviceString = testDevice.reportedPropertiesToString();

        //assert
        assertTrue(testDeviceString.length() == 0);
    }

    // Tests_SRS_DEVICETWINDEVICE_34_040: [This method shall save the provided moduleId.]
    @Test
    public void setModuleIdSets()
    {
        //arrange
        final String expectedModuleId = "someModuleId";
        DeviceTwinDevice testDeviceTwinDevice = new DeviceTwinDevice();

        //act
        Deencapsulation.invoke(testDeviceTwinDevice, "setModuleId", expectedModuleId);

        //assert
        assertEquals(expectedModuleId, Deencapsulation.getField(testDeviceTwinDevice, "moduleId"));
    }
}
