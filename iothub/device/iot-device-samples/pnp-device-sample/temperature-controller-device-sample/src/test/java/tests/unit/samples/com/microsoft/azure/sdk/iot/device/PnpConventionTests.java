// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.samples.com.microsoft.azure.sdk.iot.device;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.twin.Property;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.twin.TwinCollection;
import org.junit.Test;
import samples.com.microsoft.azure.sdk.iot.device.PnpConvention;
import samples.com.microsoft.azure.sdk.iot.device.WritablePropertyResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class PnpConventionTests
{
    private static final String TELEMETRY_COMPONENT_NAME = "$.sub";
    private static final String ENCODING_UTF_8 = StandardCharsets.UTF_8.name();
    private static final String CONTENT_APPLICATION_JSON = "application/json";
    private static final String PROPERTY_COMPONENT_IDENTIFIER_KEY = "__t";
    private static final String PROPERTY_COMPONENT_IDENTIFIER_VALUE = "c";

    @Test
    public void createIotHubMessageSetsProperty()
    {
        // arrange
        String componentName = "testComponent";

        // act
        Message message = PnpConvention.createIotHubMessageUtf8("prop1", 1.0, componentName);

        // assert
        assertEquals(componentName, message.getProperty(TELEMETRY_COMPONENT_NAME));
        assertEquals(ENCODING_UTF_8, message.getContentEncoding());
        assertEquals(CONTENT_APPLICATION_JSON, message.getContentType());
    }

    @Test
    public void createRootLevelPropertyPatch()
    {
        // arrange
        String propertyName = "testProperty";
        String propertyValue = "testValue";
        Gson gson = new Gson();
        Property testProperty = new Property(propertyName, propertyValue);
        String actualString = gson.toJson(testProperty);

        // act
        TwinCollection propertyPatch = PnpConvention.createPropertyPatch(propertyName, propertyValue);
        String patchString= gson.toJson(propertyPatch);

        // assert
        assertEquals(1, propertyPatch.size());
        assertTrue(patchString.contains(propertyName));
        assertTrue(patchString.contains(propertyValue));
    }

    @Test
    public void createComponentPropertyPatch()
    {
        // arrange
        String propertyName = "testProperty";
        String propertyValue = "testValue";
        String componentName = "testComponent";
        Gson gson = new Gson();
        Property testProperty = new Property(componentName, new HashMap<String, Object>()
        {{
            put(PROPERTY_COMPONENT_IDENTIFIER_KEY, PROPERTY_COMPONENT_IDENTIFIER_VALUE);
            put(propertyName, propertyValue);
        }});

        // act
        TwinCollection propertyPatch = PnpConvention.createComponentPropertyPatch(propertyName, propertyValue, componentName);
        String patchString= gson.toJson(propertyPatch);

        // assert
        assertEquals(1, propertyPatch.size());
        assertTrue(patchString.contains(PROPERTY_COMPONENT_IDENTIFIER_KEY));
        assertTrue(patchString.contains(PROPERTY_COMPONENT_IDENTIFIER_VALUE));
        assertTrue(patchString.contains(propertyValue));
        assertTrue(patchString.contains(propertyName));
    }

    @Test
    public void createWritablePropertyResponse()
    {
        // arrange
        Gson gson = new Gson();
        String propertyName = "testProperty";
        Object propertyValue = "testValue";
        Integer ackCode = 1;
        Long ackVersion = 100L;
        String ackDescription = "Update Completed";
        Property property = new Property(propertyName, new WritablePropertyResponse(propertyValue, ackCode, ackVersion, ackDescription));
        String actualProperty = gson.toJson(property);

        // act
        Set<Property> writablePropertyResponse = PnpConvention.createWritablePropertyResponse(propertyName, propertyValue, ackCode, ackVersion, ackDescription);
        String writablePropertyString = gson.toJson(writablePropertyResponse);

        // assert
        assertEquals(1, writablePropertyResponse.size());
        assertTrue(writablePropertyString.contains(actualProperty));
    }

    @Test (expected = IllegalArgumentException.class)
    public void createWritablePropertythrowsNull()
    {
        // arrange
        HashMap<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(null, null);

        // act
        PnpConvention.createWritablePropertyResponse(propertyMap, 1, 100L, null);
    }

    @Test
    public void createComponentWritablePropertyResponse()
    {
        // arrange
        Gson gson = new Gson();
        String propertyName = "testProperty";
        Object propertyValue = "testValue";
        String componentName = "testComponent";
        Integer ackCode = 1;
        Long ackVersion = 100L;
        String ackDescription = "Update Completed";

        Map<String, Object> componentValue = new HashMap<String, Object>() {{
            put(PROPERTY_COMPONENT_IDENTIFIER_KEY, PROPERTY_COMPONENT_IDENTIFIER_VALUE);
            put(propertyName, new WritablePropertyResponse(propertyValue, ackCode, ackVersion, ackDescription));
        }};

        Property property = new Property(componentName, componentValue);

        // act
        TwinCollection writablePropertyResponse = PnpConvention.createComponentWritablePropertyResponse(propertyName, propertyValue, componentName, ackCode, ackVersion, ackDescription);
        String writablePropertyString = gson.toJson(writablePropertyResponse);
        String a = new WritablePropertyResponse(propertyValue, ackCode, ackVersion, ackDescription).toString();
        // assert
        assertEquals(1, writablePropertyResponse.size());
        assertTrue(writablePropertyString.contains(PROPERTY_COMPONENT_IDENTIFIER_KEY));
        assertTrue(writablePropertyString.contains(PROPERTY_COMPONENT_IDENTIFIER_VALUE));
        assertTrue(writablePropertyString.contains(propertyValue.toString()));
        assertTrue(writablePropertyString.contains(ackCode.toString()));
        assertTrue(writablePropertyString.contains(ackVersion.toString()));
        assertTrue(writablePropertyString.contains(ackDescription));
        assertTrue(writablePropertyString.contains(propertyName));
    }

    @Test (expected = IllegalArgumentException.class)
    public void createComponentWritablePropertythrowsNull()
    {
        // arrange
        HashMap<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(null, null);

        // act
        PnpConvention.createComponentWritablePropertyResponse(propertyMap, "testComponent", 1, 100L, null);
    }
}
