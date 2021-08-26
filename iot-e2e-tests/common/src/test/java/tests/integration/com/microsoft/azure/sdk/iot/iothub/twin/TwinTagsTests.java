/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.twin;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.TwinCommon;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/**
 * Test class containing all tests to be run on JVM and android pertaining to the tags in twin.
 */
@IotHubTest
@RunWith(Parameterized.class)
public class TwinTagsTests extends TwinCommon
{
    public TwinTagsTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException
    {
        super(protocol, authenticationType, clientType);
    }

    @Before
    @SuppressWarnings("EmptyMethod")
    public void setUpNewDeviceAndModule() throws IOException, IotHubException, URISyntaxException, InterruptedException, ModuleClientException, GeneralSecurityException
    {
        super.setUpNewDeviceAndModule();
    }

    @Test
    @StandardTierHubOnlyTest
    public void testGetTwinUpdates() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(MAX_DEVICES);

        // Add tag and desired for multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> tags = new HashSet<>();
            tags.add(new Pair(TAG_KEY + i, TAG_VALUE + i));
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setTags(tags);

            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(PROPERTY_KEY + i, PROPERTY_VALUE + i));
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            testInstance.twinServiceClient.updateTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            testInstance.devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Update Tags and desired properties on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            Set<Pair> tags = testInstance.devicesUnderTest[i].sCDeviceForTwin.getTags();
            for (Pair tag : tags)
            {
                tag.setValue(TAG_VALUE_UPDATE + i);
            }
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setTags(tags);

            Set<Pair> desiredProperties = testInstance.devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties();
            for (Pair dp : desiredProperties)
            {
                dp.setValue(PROPERTY_VALUE_UPDATE + i);
            }
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            testInstance.twinServiceClient.updateTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            testInstance.devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);

            for (Pair t : testInstance.devicesUnderTest[i].sCDeviceForTwin.getTags())
            {
                assertEquals(buildExceptionMessage("unexpected tag key, expected " + TAG_KEY + i + " but was " + t.getKey(), testInstance.testIdentity.getClient()), TAG_KEY + i, t.getKey());
                assertEquals(buildExceptionMessage("Unexpected tag value, expected " + TAG_VALUE_UPDATE + i + " but was " + t.getValue(), testInstance.testIdentity.getClient()), TAG_VALUE_UPDATE + i, t.getValue());
            }

            for (Pair dp : testInstance.devicesUnderTest[i].sCDeviceForTwin.getDesiredProperties())
            {
                assertEquals(buildExceptionMessage("Unexpected desired property key, expected " + PROPERTY_KEY + i + " but was " + dp.getKey(), testInstance.testIdentity.getClient()), PROPERTY_KEY + i, dp.getKey());
                assertEquals(buildExceptionMessage("Unexpected desired property value, expected " + PROPERTY_VALUE_UPDATE + i + " but was " + dp.getValue(), testInstance.testIdentity.getClient()), PROPERTY_VALUE_UPDATE + i, dp.getValue());
            }
            Integer version = testInstance.devicesUnderTest[i].sCDeviceForTwin.getDesiredPropertiesVersion();
            assertNotNull(buildExceptionMessage("Version was null", testInstance.testIdentity.getClient()), version);
        }
    }

    @Test
    @StandardTierHubOnlyTest
    public void testAddTagUpdates() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(MAX_DEVICES);

        // Update tag for multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> tags = new HashSet<>();
            tags.add(new Pair(TAG_KEY + i, TAG_VALUE + i));
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setTags(tags);
            testInstance.twinServiceClient.updateTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            Thread.sleep(DELAY_BETWEEN_OPERATIONS);
            testInstance.devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            Thread.sleep(DELAY_BETWEEN_OPERATIONS);

            for (Pair t : testInstance.devicesUnderTest[i].sCDeviceForTwin.getTags())
            {
                assertEquals(buildExceptionMessage("unexpected tag key, expected " + TAG_KEY + i + " but was " + t.getKey(), testInstance.testIdentity.getClient()), TAG_KEY + i, t.getKey());
                assertEquals(buildExceptionMessage("Unexpected tag value, expected " + TAG_VALUE + i + " but was " + t.getValue(), testInstance.testIdentity.getClient()), TAG_VALUE + i, t.getValue());
            }
        }
    }

    @Test
    @StandardTierHubOnlyTest
    public void testUpdateTagUpdates() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(MAX_DEVICES);

        // Add tag for multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> tags = new HashSet<>();
            tags.add(new Pair(TAG_KEY + i, TAG_VALUE + i));
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setTags(tags);
            testInstance.twinServiceClient.updateTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            testInstance.devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Update Tags on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            Set<Pair> tags = testInstance.devicesUnderTest[i].sCDeviceForTwin.getTags();
            for (Pair tag : tags)
            {
                tag.setValue(TAG_VALUE_UPDATE + i);
            }
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setTags(tags);
            testInstance.twinServiceClient.updateTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            testInstance.devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Read updates on multiple devices
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);

            for (Pair t : testInstance.devicesUnderTest[i].sCDeviceForTwin.getTags())
            {
                assertEquals(buildExceptionMessage("unexpected tag key, expected " + TAG_KEY + i + " but was " + t.getKey(), testInstance.testIdentity.getClient()), TAG_KEY + i, t.getKey());
                assertEquals(buildExceptionMessage("Unexpected tag value, expected " + TAG_VALUE + i + " but was " + t.getValue(), testInstance.testIdentity.getClient()), TAG_VALUE_UPDATE + i, t.getValue());
            }
        }

        // Delete tags
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            Set<Pair> tags = testInstance.devicesUnderTest[i].sCDeviceForTwin.getTags();
            for (Pair tag : tags)
            {
                tag.setValue(null);
            }
            testInstance.devicesUnderTest[i].sCDeviceForTwin.setTags(tags);
            testInstance.twinServiceClient.updateTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);
            testInstance.devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Verify tags were deleted successfully
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testInstance.twinServiceClient.getTwin(testInstance.devicesUnderTest[i].sCDeviceForTwin);

            assertEquals(buildExceptionMessage("Tags were not deleted by being set null", testInstance.testIdentity.getClient()), 0, testInstance.devicesUnderTest[i].sCDeviceForTwin.getTags().size());
        }
    }
}
