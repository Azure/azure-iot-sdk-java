// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonElement;
import mockit.Deencapsulation;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for Device Provisioning Service TwinState
 * 100% methods, 100% lines covered
 */
public class TwinStateTest
{
    private final static TwinCollection TAGS = new TwinCollection()
    {
        {
            put("tag1", "val1");
            put("tag2", "val2");
            put("tag3", "val3");
        }
    };

    private final static TwinCollection PROPERTIES = new TwinCollection()
    {
        {
            put("prop1", "val1");
            put("prop2", "val2");
            put("prop3", "val3");
        }
    };

    private final static String JSON =
            "{" +
                "\"tags\":{" +
                    "\"tag1\":\"val1\"," +
                    "\"tag2\":\"val2\"," +
                    "\"tag3\":\"val3\"" +
                "}," +
                "\"properties\":{" +
                    "\"desired\":{" +
                        "\"prop1\":\"val1\"," +
                        "\"prop2\":\"val2\"," +
                        "\"prop3\":\"val3\"" +
                    "}" +
                "}" +
            "}";

    private final static String JSON_ONLY_TAGS =
            "{" +
                "\"tags\":{" +
                    "\"tag1\":\"val1\"," +
                    "\"tag2\":\"val2\"," +
                    "\"tag3\":\"val3\"" +
                "}" +
            "}";

    private final static String JSON_ONLY_PROPERTIES =
            "{" +
                "\"properties\":{" +
                    "\"desired\":{" +
                        "\"prop1\":\"val1\"," +
                        "\"prop2\":\"val2\"," +
                        "\"prop3\":\"val3\"" +
                    "}" +
                "}" +
            "}";

    /* SRS_TWIN_STATE_21_001: [The constructor shall store the provided tags and desiredProperty.] */
    @Test
    public void constructorStoreTagsAndProperties()
    {
        // arrange

        // act
        TwinState twinState = new TwinState(TAGS, PROPERTIES);

        // assert
        assertEquals(TAGS, Deencapsulation.getField(twinState, "tags"));
        TwinProperties propertiesResult = Deencapsulation.getField(twinState, "properties");
        assertNotNull(propertiesResult);
        assertEquals(PROPERTIES, propertiesResult.getDesired());
    }

    /* SRS_TWIN_STATE_21_001: [The constructor shall store the provided tags and desiredProperty.] */
    @Test
    public void constructorSucceedOnNullTags()
    {
        // arrange

        // act
        TwinState twinState = new TwinState(null, PROPERTIES);

        // assert
        assertNull(Deencapsulation.getField(twinState, "tags"));
    }

    /* SRS_TWIN_STATE_21_001: [The constructor shall store the provided tags and desiredProperty.] */
    @Test
    public void constructorSucceedOnNullDesiredProperty()
    {
        // arrange

        // act
        TwinState twinState = new TwinState(TAGS, null);

        // assert
        assertNull(Deencapsulation.getField(twinState, "properties"));
    }

    /* SRS_TWIN_STATE_21_002: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementReturnsTagsAndProperties()
    {
        // arrange
        TwinState twinState = new TwinState(TAGS, PROPERTIES);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinState, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), JSON);
    }

    /* SRS_TWIN_STATE_21_003: [If the tags is null, the toJsonElement shall not include the `tags` in the final JSON.] */
    @Test
    public void toJsonElementReturnsProperties()
    {
        // arrange
        TwinState twinState = new TwinState(null, PROPERTIES);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinState, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), JSON_ONLY_PROPERTIES);
    }

    /* SRS_TWIN_STATE_21_004: [If the properties is null, the toJsonElement shall not include the `properties` in the final JSON.] */
    @Test
    public void toJsonElementReturnsTags()
    {
        // arrange
        TwinState twinState = new TwinState(TAGS, null);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinState, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), JSON_ONLY_TAGS);
    }

    /* SRS_TWIN_STATE_21_003: [If the tags is null, the toJsonElement shall not include the `tags` in the final JSON.] */
    /* SRS_TWIN_STATE_21_004: [If the properties is null, the toJsonElement shall not include the `properties` in the final JSON.] */
    @Test
    public void toJsonElementReturnsEmptyJson()
    {
        // arrange
        TwinState twinState = new TwinState(null, null);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinState, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), "{}");
    }

    /* SRS_TWIN_STATE_21_005: [The getTags shall return a TwinCollection with the stored tags.] */
    /* SRS_TWIN_STATE_21_006: [The getDesiredProperty shall return a TwinCollection with the stored desired property.] */
    @Test
    public void gettersSucceed()
    {
        // arrange
        TwinState twinState = new TwinState(TAGS, PROPERTIES);

        // act - assert
        assertEquals(TAGS, twinState.getTags());
        assertEquals(PROPERTIES, twinState.getDesiredProperties());
    }

    /* SRS_TWIN_STATE_21_007: [The toString shall return a String with the information in this class in a pretty print JSON.] */
    @Test
    public void toStringSucceed()
    {
        // arrange
        TwinState twinState = new TwinState(TAGS, PROPERTIES);

        // act
        String result = twinState.toString();

        // assert
        Helpers.assertJson(result, JSON);
    }

    /* SRS_TWIN_STATE_21_008: [If the tags is null, the JSON shall not include the `tags`.] */
    @Test
    public void toStringSucceedOnTagsNull()
    {
        // arrange
        TwinState twinState = new TwinState(null, PROPERTIES);

        // act
        String result = twinState.toString();

        // assert
        Helpers.assertJson(result, JSON_ONLY_PROPERTIES);
    }

    /* SRS_TWIN_STATE_21_009: [If the property is null, the JSON shall not include the `properties`.] */
    @Test
    public void toStringSucceedOnDesiredPropertyNull()
    {
        // arrange
        TwinState twinState = new TwinState(TAGS, null);

        // act
        String result = twinState.toString();

        // assert
        Helpers.assertJson(result, JSON_ONLY_TAGS);
    }

    /* SRS_TWIN_STATE_21_010: [The TwinState shall provide an empty constructor to make GSON happy.] */
    @Test
    public void constructorSucceed()
    {
        // act
        TwinState twinState = Deencapsulation.newInstance(TwinState.class);

        // assert
        assertNotNull(twinState);
    }
}
