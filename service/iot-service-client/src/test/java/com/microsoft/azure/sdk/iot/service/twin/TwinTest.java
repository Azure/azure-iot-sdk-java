/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.twin;

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
public class TwinTest
{
    /*
    **Tests_SRS_DEVICETWINDEVICE_25_003: [** The constructor shall create a new instance of twin object for this device and store the device id.**]**
     */
    @Test
    public void constructorCreatesNewDeviceGroup()
    {
        //act
        Twin testDevice = new Twin();

        //assert
        assertNotNull(testDevice);
        TwinCollection tagsMap = Deencapsulation.getField(testDevice, "tags");
        assertEquals(0, tagsMap.size());
        TwinCollection repPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertEquals(0, repPropMap.size());
        TwinCollection desPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertEquals(0, desPropMap.size());
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
        Twin testDevice = new Twin(deviceId);

        //assert
        assertEquals(deviceId, Deencapsulation.getField(testDevice, "deviceId"));
        assertNotNull(testDevice);
        TwinCollection tagsMap = Deencapsulation.getField(testDevice, "tags");
        assertEquals(0, tagsMap.size());
        TwinCollection repPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertEquals(0, repPropMap.size());
        TwinCollection desPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertEquals(0, desPropMap.size());
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
        Twin testDevice = new Twin(deviceId, moduleId);

        //assert
        assertEquals(deviceId, Deencapsulation.getField(testDevice, "deviceId"));
        assertEquals(moduleId, Deencapsulation.getField(testDevice, "moduleId"));
        assertNotNull(testDevice);
        TwinCollection tagsMap = Deencapsulation.getField(testDevice, "tags");
        assertEquals(0, tagsMap.size());
        TwinCollection repPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertEquals(0, repPropMap.size());
        TwinCollection desPropMap = Deencapsulation.getField(testDevice, "reportedProperties");
        assertEquals(0, desPropMap.size());
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_004: [** This method shall return the device id **]**
     */
    @Test
    public void getDeviceIdGets()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");

        //act
        String devId = testDevice.getDeviceId();

        //assert
        assertEquals("testDevice", devId);
    }

    @Test
    public void getConnectionStateGets()
    {
        //arrange
        String expectedConnectionState = TwinConnectionState.DISCONNECTED.toString();
        Twin testDevice = new Twin("testDevice");
        Deencapsulation.setField(testDevice, "connectionState", expectedConnectionState);

        //act
        String actualConnectionState = testDevice.getConnectionState();

        //assert
        assertEquals(expectedConnectionState, actualConnectionState);
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_28_001: [** This method shall return the module id **]**
     */
    @Test
    public void getModuleIdGets()
    {
        //arrange
        Twin testDevice = new Twin("testDevice", "testModule");

        //act
        String moduleId = testDevice.getModuleId();

        //assert
        assertEquals("testModule", moduleId);

    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_21_030: [** The seteTag shall store the eTag.**]**
     */
    @Test
    public void setETagSets()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");

        //act
        testDevice.setETag("validETag");

        //assert
        assertEquals("validETag", Deencapsulation.getField(testDevice, "eTag"));
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_21_031: [** The geteTag shall return the stored eTag.**]**
     */
    @Test
    public void getETagGets()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");
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
        Twin testDevice = new Twin("testDevice");

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
        Twin testDevice = new Twin("testDevice");

        //act
        Deencapsulation.invoke(testDevice, "setVersion", 10);

        //assert
        assertEquals(10L, (long)(Integer)Deencapsulation.getField(testDevice, "version"));
    }

    @Test
    public void setVersionSetsZero()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");

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
        Twin testDevice = new Twin("testDevice");

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
        Twin testDevice = new Twin("testDevice");
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
        Twin testDevice = new Twin("testDevice");

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
        Twin testDevice = new Twin("testDevice");
        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");
        testDevice.getTags().putAll(testTags);

        //act
        TwinCollection actualTags = testDevice.getTags();

        //assert
        assertEquals(testTags.size(), actualTags.size());
        for (String key : actualTags.keySet())
        {
            assertEquals("testTag", key);
            assertEquals("tagObject", actualTags.get(key));
        }
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_010: [** If the tags map is null then this method shall return empty set of pairs.**]**
     */
    @Test
    public void getTagsGetsEmptyIfNotPresent()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");

        //act
        TwinCollection actualTags = testDevice.getTags();

        //assert
        assertNotNull(actualTags);
        assertEquals(0, actualTags.size());
    }

    /*
     **Codes_SRS_DEVICETWINDEVICE_21_034: [** If the tags map is null then this method shall throw IllegalArgumentException.**]**
     */
    @Test
    public void getTagsVersionReturnsNullIfNoTags()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");

        //act
        Integer version = testDevice.getTags().getVersion();

        //assert
        assertNull(version);
    }

    @Test
    public void getTagsVersionReturnsNullIfNoVersionInTheTags()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");
        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");
        testDevice.getTags().putAll(testTags);

        //act
        Integer version = testDevice.getTags().getVersion();

        //assert
        assertNull(version);
    }

    /*
     **Codes_SRS_DEVICETWINDEVICE_21_035: [** The method shall return the version in the tags TwinCollection.**]**
     */
    @Test
    public void getTagsVersionReturnsValidVersion()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");
        testDevice.getTags().setVersion(5);

        //act
        Integer version = testDevice.getTags().getVersion();

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
        Twin testDevice = new Twin("testDevice");
        TwinCollection testDesProp = new TwinCollection();
        testDesProp.put("testDes", "desObject");
        testDevice.getDesiredProperties().putAll(testDesProp);

        //act
        TwinCollection actualDesProp = testDevice.getDesiredProperties();

        //assert
        assertEquals(testDesProp.size(), actualDesProp.size());
        for (String key : actualDesProp.keySet())
        {
            assertEquals("testDes", key);
            assertEquals("desObject", actualDesProp.get(key));
        }
    }

    /*
    Tests_SRS_DEVICETWINDEVICE_25_014: [ If the desiredProperties map is null then this method shall return empty set of pairs.]
     */
    @Test
    public void getDesiredReturnsEmptyIfNullMap()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");

        //act
        TwinCollection actualDesProp = testDevice.getDesiredProperties();

        //assert
        assertNotNull(actualDesProp);
        assertEquals(0, actualDesProp.size());
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_005: [** This method shall convert the reported properties map to a set of pairs and return with it. **]**
     */
    @Test
    public void getReportedPropGets()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");

        TwinCollection repMap = new TwinCollection();
        repMap.put("testRep", "repObject");
        Deencapsulation.setField(testDevice, "reportedProperties", repMap);

        //act
        TwinCollection actualRepProp = testDevice.getReportedProperties();

        //assert
        assertEquals(repMap.size(), actualRepProp.size());
        for (String key : actualRepProp.keySet())
        {
            assertEquals("testRep", key);
            assertEquals("repObject", actualRepProp.get(key));
        }
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_006: [** If the reported properties map is null then this method shall return empty set of pairs.**]**
     */
    @Test
    public void getReportedPropGetsEmptyIfNotPresent()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");

        //act
        TwinCollection actualRepProp = testDevice.getReportedProperties();

        //assert
        assertNotNull(actualRepProp);
        assertEquals(0, actualRepProp.size());
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_25_026: [** This method shall return the reportedProperties map**]**
     */
    @Test
    public void getterGetsConfigurations()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");
        ConfigurationInfo info = new ConfigurationInfo();
        info.setStatus(ConfigurationStatus.TARGETED);
        Map<String, ConfigurationInfo> configs = new HashMap<String, ConfigurationInfo>(){{put("abc",info);}};
        Deencapsulation.setField(testDevice, "configurations", configs);

        //act
        Map<String, ConfigurationInfo> actual = testDevice.getConfigurations();

        //assert
        assertEquals(1, actual.size());

        for (Map.Entry<String, ConfigurationInfo> test : actual.entrySet())
        {
            assertEquals("abc", test.getKey());
            assertEquals(test.getValue(), info);
        }
    }

    /*
     **Tests_SRS_DEVICETWINDEVICE_28_003: [** The setCapabilities shall store the device capabilities.**]**
     */
    @Test
    public void setterSetsCapabilities()
    {
        //arrange
        Twin testDevice = new Twin("testDevice");
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
        Twin testDevice = new Twin("testDevice");
        DeviceCapabilities cap = new DeviceCapabilities();
        cap.setIotEdge(true);
        Deencapsulation.setField(testDevice, "capabilities", cap);

        //act
        DeviceCapabilities actual = testDevice.getCapabilities();

        //assert
        assertEquals(cap, actual);
    }

    /*
    **Tests_SRS_DEVICETWINDEVICE_25_015: [** This method shall append device id, etag, version, tags, desired and reported properties to string (if present) and return **]**
    */
    @Test
    public void toStringReturnsAllNoETag()
    {
        //arrange
        final String expectedModuleId = "someModuleId";
        Twin testDevice = new Twin("testDevice");

        TwinCollection testDesProp = new TwinCollection();
        testDesProp.put("testDes1", "desObject1");
        testDesProp.put("testDes2", "desObject2");
        testDevice.getDesiredProperties().putAll((testDesProp));

        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag1", "tagObject1");
        testTags.put("testTag2", "tagObject2");
        testDevice.getTags().putAll(testTags);

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
        Twin testDevice = new Twin("testDevice");

        testDevice.setETag("validEtag");
        Deencapsulation.invoke(testDevice, "setVersion", 10);

        TwinCollection testDesProp = new TwinCollection();
        testDesProp.put("testDes1", "desObject1");
        testDesProp.put("testDes2", "desObject2");
        testDevice.getDesiredProperties().putAll((testDesProp));

        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag1", "tagObject1");
        testTags.put("testTag2", "tagObject2");
        testDevice.getTags().putAll(testTags);

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

    // Tests_SRS_DEVICETWINDEVICE_34_040: [This method shall save the provided moduleId.]
    @Test
    public void setModuleIdSets()
    {
        //arrange
        final String expectedModuleId = "someModuleId";
        Twin testTwin = new Twin();

        //act
        Deencapsulation.invoke(testTwin, "setModuleId", expectedModuleId);

        //assert
        assertEquals(expectedModuleId, Deencapsulation.getField(testTwin, "moduleId"));
    }


    // Twins returned by the service as query results won't necessarily have any particular field since a query
    // can filter on any field like status. This checks that we can still deserialize a twin even when it has no
    // device Id or when it only has a device Id
    @Test
    public void nullValueParsingWorks()
    {
        Twin twin = Twin.fromJson("{\"deviceId\":\"someDeviceId\"}");
        assertNull(twin.getETag());
        assertNull(twin.getConnectionState());
        assertNull(twin.getModuleId());
        assertNull(twin.getCloudToDeviceMessageCount());
        assertNull(twin.getLastActivityTime());
        assertNull(twin.getStatus());
        assertNull(twin.getVersion());
        assertNotNull(twin.getDeviceId());

        twin = Twin.fromJson("{\"status\":\"enabled\"}");
        assertNull(twin.getETag());
        assertNull(twin.getConnectionState());
        assertNull(twin.getModuleId());
        assertNull(twin.getCloudToDeviceMessageCount());
        assertNull(twin.getLastActivityTime());
        assertNull(twin.getVersion());
        assertNull(twin.getDeviceId());
        assertNotNull(twin.getStatus());
    }
}
