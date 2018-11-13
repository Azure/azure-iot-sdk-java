/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.twin;

import com.microsoft.azure.sdk.iot.common.setup.DeviceTwinCommon;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class containing all tests to be run on JVM and android pertaining to the tags in twin. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class TwinTagsTests extends DeviceTwinCommon
{
    public TwinTagsTests(String deviceId, String moduleId, IotHubClientProtocol protocol, AuthenticationType authenticationType, String clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceId, moduleId, protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testGetTwinUpdates() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(MAX_DEVICES);

        // Add tag and desired for multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> tags = new HashSet<>();
            tags.add(new Pair(TAG_KEY + i, TAG_VALUE + i));
            devicesUnderTest[i].sCDeviceForTwin.setTags(tags);

            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(PROPERTY_KEY + i, PROPERTY_VALUE + i));
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Update Tags and desired properties on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Set<Pair> tags = devicesUnderTest[i].sCDeviceForTwin.getTags();
            for (Pair tag : tags)
            {
                tag.setValue(TAG_VALUE_UPDATE + i);
            }
            devicesUnderTest[i].sCDeviceForTwin.setTags(tags);

            Set<Pair> desiredProperties = devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties();
            for (Pair dp : desiredProperties)
            {
                dp.setValue(PROPERTY_VALUE_UPDATE + i);
            }
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);

            for (Pair t : devicesUnderTest[i].sCDeviceForTwin.getTags())
            {
                assertEquals(t.getKey(), TAG_KEY + i);
                assertEquals(t.getValue(), TAG_VALUE_UPDATE + i);
            }

            for (Pair dp : devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties())
            {
                assertEquals(dp.getKey(), PROPERTY_KEY + i);
                assertEquals(dp.getValue(), PROPERTY_VALUE_UPDATE + i);
            }
            Integer version = devicesUnderTest[i].sCDeviceForTwin.getDesiredPropertiesVersion();
            assertNotNull(version);
        }
        removeMultipleDevices(MAX_DEVICES);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testAddTagUpdates() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(MAX_DEVICES);

        // Update tag for multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> tags = new HashSet<>();
            tags.add(new Pair(TAG_KEY + i, TAG_VALUE + i));
            devicesUnderTest[i].sCDeviceForTwin.setTags(tags);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            Thread.sleep(DELAY_BETWEEN_OPERATIONS);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Thread.sleep(DELAY_BETWEEN_OPERATIONS);

            for (Pair t : devicesUnderTest[i].sCDeviceForTwin.getTags())
            {
                assertEquals(t.getKey(), TAG_KEY + i);
                assertEquals(t.getValue(), TAG_VALUE + i);
            }
        }
        removeMultipleDevices(MAX_DEVICES);
    }

    @Test(timeout = MAX_MILLISECS_TIMEOUT_KILL_TEST)
    public void testUpdateTagUpdates() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(MAX_DEVICES);

        // Add tag for multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> tags = new HashSet<>();
            tags.add(new Pair(TAG_KEY + i, TAG_VALUE + i));
            devicesUnderTest[i].sCDeviceForTwin.setTags(tags);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Update Tags on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Set<Pair> tags = devicesUnderTest[i].sCDeviceForTwin.getTags();
            for (Pair tag : tags)
            {
                tag.setValue(TAG_VALUE_UPDATE + i);
            }
            devicesUnderTest[i].sCDeviceForTwin.setTags(tags);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);

            for (Pair t : devicesUnderTest[i].sCDeviceForTwin.getTags())
            {
                assertEquals(t.getKey(), TAG_KEY + i);
                assertEquals(t.getValue(), TAG_VALUE_UPDATE + i);
            }
        }

        // Delete tags
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);
            Set<Pair> tags = devicesUnderTest[i].sCDeviceForTwin.getTags();
            for (Pair tag : tags)
            {
                tag.setValue(null);
            }
            devicesUnderTest[i].sCDeviceForTwin.setTags(tags);
            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Verify tags were deleted successfully
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            sCDeviceTwin.getTwin(devicesUnderTest[i].sCDeviceForTwin);

            assertEquals("Tags were not deleted by being set null", 0, devicesUnderTest[i].sCDeviceForTwin.getTags().size());
        }

        removeMultipleDevices(MAX_DEVICES);
    }

    @Test (timeout = ERROR_INJECTION_EXECUTION_TIMEOUT)
    public void setTagsAtMaxDepthAllowed() throws IOException, IotHubException
    {
        sCDeviceTwin.getTwin(deviceUnderTest.sCDeviceForTwin);

        //Update twin Tags and Desired Properties
        Set<Pair> tags = new HashSet<>();

        HashMap<String, String> map5 = new HashMap<>();
        map5.put("5", "this value is at an allowable depth");
        HashMap<String, Map> map4 = new HashMap<>();
        map4.put("4", map5);
        HashMap<String, Map> map3 = new HashMap<>();
        map3.put("3", map4);
        HashMap<String, Map> map2 = new HashMap<>();
        map2.put("2", map3);
        HashMap<String, Map> map1 = new HashMap<>();
        map1.put("1", map2);
        tags.add(new Pair("0", map1));
        deviceUnderTest.sCDeviceForTwin.setTags(tags);
        sCDeviceTwin.updateTwin(deviceUnderTest.sCDeviceForTwin);

        //This line will ensure that the SDK does not complain when receiving a valid, full twin depth from the service
        sCDeviceTwin.getTwin(deviceUnderTest.sCDeviceForTwin);
    }
}
