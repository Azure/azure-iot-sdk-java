// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.deps.twin.TwinProperties;
import com.microsoft.azure.sdk.iot.deps.twin.TwinState;
import mockit.Deencapsulation;
import org.junit.Test;
import tests.unit.com.microsoft.azure.sdk.iot.deps.Helpers;

import static org.junit.Assert.*;

/**
 * Unit tests for the TwinState
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

    private final static TwinCollection PROPERTIES_WITH_NULL_VALUES = new TwinCollection()
    {
        {
            put("prop1", null);
            put("prop2", null);
            put("prop3", null);
        }
    };

    private final static String REGISTER_MANAGER_SAMPLE =
            "\"deviceId\":\"validDeviceId\"," +
            "\"generationId\":\"validGenerationId\"," +
            "\"version\":3," +
            "\"status\":\"enabled\"," +
            "\"statusReason\":\"validStatusReason\"," +
            "\"statusUpdatedTime\":\"2016-06-01T21:22:41+00:00\"," +
            "\"connectionState\":\"disconnected\"," +
            "\"connectionStateUpdatedTime\":\"2016-06-01T21:22:41+00:00\"," +
            "\"lastActivityTime\":\"xxx\"," +
            "\"etag\":\"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx\"";

    private final static String TAGS_SAMPLE =
            "\"tags\":{" +
                "\"tag1\":\"val1\"," +
                "\"tag2\":\"val2\"," +
                "\"tag3\":\"val3\"" +
            "}";
    private final static String DESIRED_PROPERTY_SAMPLE =
            "\"desired\":{" +
                "\"prop1\":\"val1\"," +
                "\"prop2\":\"val2\"," +
                "\"prop3\":\"val3\"" +
            "}";
    private final static String REPORTED_PROPERTY_SAMPLE =
            "\"reported\":{" +
                "\"prop1\":\"val1\"," +
                "\"prop2\":\"val2\"," +
                "\"prop3\":\"val3\"" +
            "}";
    private final static String PROPERTIES_SAMPLE =
            "\"properties\":{" +
                DESIRED_PROPERTY_SAMPLE + "," +
                REPORTED_PROPERTY_SAMPLE +
            "}";

    /* SRS_TWIN_STATE_21_001: [The constructor shall store the provided tags, desiredProperty, and reportedProperty.] */
    @Test
    public void constructorStoreTagsAndProperties()
    {
        // arrange

        // act
        TwinState twinState = new TwinState(TAGS, PROPERTIES, PROPERTIES);

        // assert
        assertEquals(TAGS, Deencapsulation.getField(twinState, "tags"));
        TwinProperties propertiesResult = Deencapsulation.getField(twinState, "properties");
        assertNotNull(propertiesResult);
        assertEquals(PROPERTIES, propertiesResult.getDesired());
        assertEquals(PROPERTIES, propertiesResult.getReported());
    }

    /* SRS_TWIN_STATE_21_001: [The constructor shall store the provided tags, desiredProperty, and reportedProperty.] */
    @Test
    public void constructorSucceedOnNullTags()
    {
        // arrange

        // act
        TwinState twinState = new TwinState(null, PROPERTIES, PROPERTIES);

        // assert
        assertNull(Deencapsulation.getField(twinState, "tags"));
    }

    /* SRS_TWIN_STATE_21_001: [The constructor shall store the provided tags, desiredProperty, and reportedProperty.] */
    @Test
    public void constructorSucceedOnNullDesiredProperty()
    {
        // arrange

        // act
        TwinState twinState = new TwinState(TAGS, null, PROPERTIES);

        // assert
        TwinProperties propertiesResult = Deencapsulation.getField(twinState, "properties");
        assertNotNull(propertiesResult);
        assertNull(propertiesResult.getDesired());
        assertEquals(PROPERTIES, propertiesResult.getReported());
    }

    /* SRS_TWIN_STATE_21_001: [The constructor shall store the provided tags, desiredProperty, and reportedProperty.] */
    @Test
    public void constructorSucceedOnOnlyReportedProperty()
    {
        // arrange

        // act
        TwinState twinState = new TwinState(null, null, PROPERTIES);

        // assert
        assertNull(Deencapsulation.getField(twinState, "tags"));
        TwinProperties propertiesResult = Deencapsulation.getField(twinState, "properties");
        assertNotNull(propertiesResult);
        assertNull(propertiesResult.getDesired());
        assertEquals(PROPERTIES, propertiesResult.getReported());
    }

    /* SRS_TWIN_STATE_21_001: [The constructor shall store the provided tags, desiredProperty, and reportedProperty.] */
    @Test
    public void constructorSucceedOnNullDesiredAndReportedProperty()
    {
        // arrange

        // act
        TwinState twinState = new TwinState(TAGS, null, null);

        // assert
        assertNull(Deencapsulation.getField(twinState, "properties"));
    }

    /* SRS_TWIN_STATE_21_002: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementReturnsTagsAndProperties()
    {
        // arrange
        final String json =
            "{" +
                TAGS_SAMPLE + "," +
                PROPERTIES_SAMPLE +
            "}";
        TwinState twinState = new TwinState(TAGS, PROPERTIES, PROPERTIES);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinState, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), json);
    }

    /* SRS_TWIN_STATE_21_003: [If the tags is null, the toJsonElement shall not include the `tags` in the final JSON.] */
    @Test
    public void toJsonElementReturnsProperties()
    {
        // arrange
        final String json =
            "{" +
                PROPERTIES_SAMPLE +
            "}";
        TwinState twinState = new TwinState(null, PROPERTIES, PROPERTIES);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinState, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), json);
    }

    /* SRS_TWIN_STATE_21_004: [If the properties is null, the toJsonElement shall not include the `properties` in the final JSON.] */
    @Test
    public void toJsonElementReturnsTags()
    {
        // arrange
        final String json =
            "{" +
                TAGS_SAMPLE +
            "}";
        TwinState twinState = new TwinState(TAGS, null, null);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinState, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), json);
    }

    /* SRS_TWIN_STATE_21_003: [If the tags is null, the toJsonElement shall not include the `tags` in the final JSON.] */
    /* SRS_TWIN_STATE_21_004: [If the properties is null, the toJsonElement shall not include the `properties` in the final JSON.] */
    @Test
    public void toJsonElementReturnsEmptyJson()
    {
        // arrange
        TwinState twinState = new TwinState(null, null, null);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinState, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), "{}");
    }

    //Tests_SRS_TWIN_STATE_34_024: [The json element shall include all null desired and reported properties.]
    @Test
    public void toJsonElementPreservesNullDesiredProperties()
    {
        // arrange
        TwinState twinState = new TwinState(null, PROPERTIES_WITH_NULL_VALUES, PROPERTIES);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinState, "toJsonElement");

        // assert
        assertTrue(jsonElement.toString().contains(PROPERTIES_WITH_NULL_VALUES.toString()));
    }

    //Tests_SRS_TWIN_STATE_34_024: [The json element shall include all null desired and reported properties.]
    @Test
    public void toJsonElementPreservesNullReportedProperties()
    {
        // arrange
        TwinState twinState = new TwinState(null, PROPERTIES, PROPERTIES_WITH_NULL_VALUES);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinState, "toJsonElement");

        // assert
        assertTrue(jsonElement.toString().contains(PROPERTIES_WITH_NULL_VALUES.toString()));
    }

    /* SRS_TWIN_STATE_21_005: [The getTags shall return a TwinCollection with the stored tags.] */
    /* SRS_TWIN_STATE_21_006: [The getDesiredProperty shall return a TwinCollection with the stored desired property.] */
    /* SRS_TWIN_STATE_21_007: [The getReportedProperty shall return a TwinCollection with the stored reported property.] */
    @Test
    public void gettersSucceed()
    {
        // arrange
        TwinState twinState = new TwinState(TAGS, PROPERTIES, PROPERTIES);

        // act - assert
        assertEquals(TAGS, twinState.getTags());
        assertEquals(PROPERTIES, twinState.getDesiredProperty());
        assertEquals(PROPERTIES, twinState.getReportedProperty());
    }

    /* SRS_TWIN_STATE_21_006: [The getDesiredProperty shall return a TwinCollection with the stored desired property.] */
    /* SRS_TWIN_STATE_21_007: [The getReportedProperty shall return a TwinCollection with the stored reported property.] */
    @Test
    public void gettersWithNullSucceed()
    {
        // arrange
        TwinState twinState = new TwinState(TAGS, null, null);

        // act - assert
        assertEquals(TAGS, twinState.getTags());
        assertNull(twinState.getDesiredProperty());
        assertNull(twinState.getReportedProperty());
    }

    /* SRS_TWIN_STATE_21_008: [The toString shall return a String with the information in this class in a pretty print JSON.] */
    @Test
    public void toStringSucceed()
    {
        // arrange
        final String json =
            "{" +
                TAGS_SAMPLE + "," +
                PROPERTIES_SAMPLE +
            "}";
        TwinState twinState = new TwinState(TAGS, PROPERTIES, PROPERTIES);

        // act
        String result = twinState.toString();

        // assert
        Helpers.assertJson(result, json);
    }

    /* SRS_TWIN_STATE_21_009: [If the tags is null, the JSON shall not include the `tags`.] */
    @Test
    public void toStringSucceedOnTagsNull()
    {
        // arrange
        final String json =
            "{" +
                PROPERTIES_SAMPLE +
            "}";
        TwinState twinState = new TwinState(null, PROPERTIES, PROPERTIES);

        // act
        String result = twinState.toString();

        // assert
        Helpers.assertJson(result, json);
    }

    /* SRS_TWIN_STATE_21_010: [If the property is null, the JSON shall not include the `properties`.] */
    @Test
    public void toStringSucceedOnDesiredPropertyNull()
    {
        // arrange
        final String json =
            "{" +
                TAGS_SAMPLE +
            "}";
        TwinState twinState = new TwinState(TAGS, null, null);

        // act
        String result = twinState.toString();

        // assert
        Helpers.assertJson(result, json);
    }

    /* SRS_TWIN_STATE_21_011: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void createFromTwinJsonThrowsOnNull()
    {
        // arrange
        final String json = null;

        // act
        TwinState.createFromTwinJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_011: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void createFromTwinJsonThrowsOnEmpty()
    {
        // arrange
        final String json = "";

        // act
        TwinState.createFromTwinJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_012: [The factory shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected = JsonSyntaxException.class)
    public void createFromTwinJsonThrowsOnInvalidJSON()
    {
        // arrange
        final String json =
                "{" +
                    REGISTER_MANAGER_SAMPLE + //Missing comma.
                    TAGS_SAMPLE + "," +
                    PROPERTIES_SAMPLE +
                "}";

        // act
        TwinState.createFromTwinJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_013: [The factory shall deserialize the provided JSON for the Twin class and subclasses.] */
    @Test
    public void createFromTwinJson()
    {
        // arrange
        final String json =
                "{" +
                    REGISTER_MANAGER_SAMPLE + "," +
                    TAGS_SAMPLE + "," +
                    PROPERTIES_SAMPLE +
                "}";

        // act
        TwinState twinState = TwinState.createFromTwinJson(json);

        // assert
        Helpers.assertJson(Deencapsulation.invoke(twinState, "toJsonElement").toString(), json);
    }

    /* SRS_TWIN_STATE_21_013: [The factory shall deserialize the provided JSON for the Twin class and subclasses.] */
    @Test
    public void createFromTwinJsonWithTagsAndProperties()
    {
        // arrange
        final String json =
                "{" +
                    TAGS_SAMPLE + "," +
                    PROPERTIES_SAMPLE +
                "}";

        // act
        TwinState twinState = TwinState.createFromTwinJson(json);

        // assert
        Helpers.assertJson(Deencapsulation.invoke(twinState, "toJsonElement").toString(), json);
    }

    /* SRS_TWIN_STATE_21_014: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void createFromDesiredPropertyJsonThrowsOnNull()
    {
        // arrange
        final String json = null;

        // act
        TwinState.createFromDesiredPropertyJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_014: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void createFromDesiredPropertyJsonThrowsOnEmpty()
    {
        // arrange
        final String json = "";

        // act
        TwinState.createFromDesiredPropertyJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_015: [The factory shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected = JsonSyntaxException.class)
    public void createFromDesiredPropertyJsonThrowsOnInvalidJSON()
    {
        // arrange
        final String json =
                "{" +
                        "\"desired\":," +
                "}";

        // act
        TwinState.createFromDesiredPropertyJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_016: [The factory shall deserialize the provided JSON for the Twin class and subclasses.] */
    @Test
    public void createFromDesiredPropertyJson()
    {
        // arrange
        final String json =  "{" + DESIRED_PROPERTY_SAMPLE + "}";

        // act
        TwinState twinState = TwinState.createFromDesiredPropertyJson(json);

        // assert
        Helpers.assertJson(twinState.getDesiredProperty().toJsonElement().toString(), json);
    }

    /* SRS_TWIN_STATE_21_017: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void createFromReportedPropertyJsonThrowsOnNull()
    {
        // arrange
        final String json = null;

        // act
        TwinState.createFromReportedPropertyJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_017: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void createFromReportedPropertyJsonThrowsOnEmpty()
    {
        // arrange
        final String json = "";

        // act
        TwinState.createFromReportedPropertyJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_018: [The factory shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected = JsonSyntaxException.class)
    public void createFromReportedPropertyJsonThrowsOnInvalidJSON()
    {
        // arrange
        final String json =
                "{" +
                    "\"reported\":," +
                "}";

        // act
        TwinState.createFromReportedPropertyJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_019: [The factory shall deserialize the provided JSON for the Twin class and subclasses.] */
    @Test
    public void createFromReportedPropertyJson()
    {
        // arrange
        final String json =  "{" + REPORTED_PROPERTY_SAMPLE + "}";

        // act
        TwinState twinState = TwinState.createFromReportedPropertyJson(json);

        // assert
        Helpers.assertJson(twinState.getReportedProperty().toJsonElement().toString(), json);
    }

    /* SRS_TWIN_STATE_21_020: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void createFromPropertiesJsonThrowsOnNull()
    {
        // arrange
        final String json = null;

        // act
        TwinState.createFromPropertiesJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_020: [The factory shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void createFromPropertiesJsonThrowsOnEmpty()
    {
        // arrange
        final String json = "";

        // act
        TwinState.createFromPropertiesJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_021: [The factory shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected = JsonSyntaxException.class)
    public void createFromPropertiesJsonThrowsOnInvalidJSON()
    {
        // arrange
        final String json =
                "{" +
                        "\"reported\":," +
                        "}";

        // act
        TwinState.createFromPropertiesJson(json);

        // assert
    }

    /* SRS_TWIN_STATE_21_022: [The factory shall deserialize the provided JSON for the Twin class and subclasses.] */
    @Test
    public void createFromPropertiesJsonSucceed()
    {
        // arrange
        final String json =  "{" + DESIRED_PROPERTY_SAMPLE + ", " + REPORTED_PROPERTY_SAMPLE + "}";

        // act
        TwinState twinState = TwinState.createFromPropertiesJson(json);

        // assert
        Helpers.assertJson(Deencapsulation.invoke(twinState, "toJsonElement").toString(),"{" + PROPERTIES_SAMPLE + "}");
    }

    /* SRS_TWIN_STATE_21_023: [The TwinState shall provide an empty constructor to make GSON happy.] */
    @Test
    public void constructorSucceed()
    {
        // act
        TwinState twinState = Deencapsulation.newInstance(TwinState.class);

        // assert
        assertNotNull(twinState);
    }
}
