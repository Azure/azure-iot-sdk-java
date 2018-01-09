// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.deps.twin.TwinProperties;
import mockit.Deencapsulation;
import org.junit.Test;
import tests.unit.com.microsoft.azure.sdk.iot.deps.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for the TwinProperties
 * 100% methods, 100% lines covered
 */
public class TwinPropertiesTest
{
    private static final Integer VERSION = 4;

    private final static TwinCollection PROPERTIES = new TwinCollection()
    {
        {
            put("prop1", "val1");
            put("prop2", "val2");
            put("prop3", "val3");
        }
    };
    private final static String JSON_ONLY_DESIRED_PROPERTIES =
            "{" +
                "\"desired\":{" +
                    "\"prop1\":\"val1\"," +
                    "\"prop2\":\"val2\"," +
                    "\"prop3\":\"val3\"" +
                "}" +
            "}";

    private final static String JSON_ONLY_REPORTED_PROPERTIES =
            "{" +
                "\"reported\":{" +
                    "\"prop1\":\"val1\"," +
                    "\"prop2\":\"val2\"," +
                    "\"prop3\":\"val3\"" +
                "}" +
            "}";

    private final static String JSON_ONLY_PROPERTIES =
            "{" +
                "\"desired\":{" +
                    "\"prop1\":\"val1\"," +
                    "\"prop2\":\"val2\"," +
                    "\"prop3\":\"val3\"" +
                "}," +
                "\"reported\":{" +
                    "\"prop1\":\"val1\"," +
                    "\"prop2\":\"val2\"," +
                    "\"prop3\":\"val3\"" +
                "}" +
            "}";

    private static final String JSON_FULL_SAMPLE =
            "    {  \n" +
            "      \"Brand\":\"NiceCar\",\n" +
            "      \"MaxSpeed\":{  \n" +
            "        \"Value\":500,\n" +
            "        \"NewValue\":300,\n" +
            "        \"Inner1\":{" +
            "          \"Inner2\":\"FinalInnerValue\"" +
            "        }\n" +
            "      },\n" +
            "      \"$metadata\":{  \n" +
            "        \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
            "        \"$lastUpdatedVersion\":1,\n" +
            "        \"Brand\":{" +
            "          \"$lastUpdated\":\"2017-08-09T02:07:44.238Z\",\n" +
            "          \"$lastUpdatedVersion\":2" +
            "        },\n" +
            "        \"MaxSpeed\":{  \n" +
            "          \"$lastUpdated\":\"2017-10-21T02:07:44.238Z\",\n" +
            "          \"$lastUpdatedVersion\":3,\n" +
            "          \"Value\":{  \n" +
            "            \"$lastUpdated\":\"2017-11-21T02:07:44.238Z\",\n" +
            "            \"$lastUpdatedVersion\":4\n" +
            "          },\n" +
            "          \"NewValue\":{  \n" +
            "            \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
            "            \"$lastUpdatedVersion\":5\n" +
            "          },\n" +
            "          \"Inner1\":{  \n" +
            "            \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
            "            \"$lastUpdatedVersion\":6,\n" +
            "            \"Inner2\":{  \n" +
            "              \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
            "              \"$lastUpdatedVersion\":7\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      },\n" +
            "      \"$version\":" + VERSION + "\n" +
            "    }\n";

    /* SRS_TWIN_PROPERTIES_21_001: [The constructor shall throw IllegalArgumentException if the provided desired and reported properties is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullDesiredProperty()
    {
        // arrange
        // act
        Deencapsulation.newInstance(TwinProperties.class, new Class[]{TwinCollection.class}, new Class[]{TwinCollection.class, TwinCollection.class}, (TwinCollection)null, (TwinCollection)null);

        // assert
    }

    /* SRS_TWIN_PROPERTIES_21_002: [The constructor shall store the provided desired and reported properties converting from the row collection.] */
    @Test
    public void constructorStoreDesiredProperty()
    {
        // arrange
        // act
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class, new Class[]{TwinCollection.class, TwinCollection.class}, PROPERTIES, (TwinCollection)null);

        // assert
        assertEquals(PROPERTIES, Deencapsulation.getField(twinProperties, "desired"));
        assertNull(Deencapsulation.getField(twinProperties, "reported"));
    }

    /* SRS_TWIN_PROPERTIES_21_002: [The constructor shall store the provided desired and reported properties converting from the row collection.] */
    @Test
    public void constructorStoreReportedProperty()
    {
        // arrange
        // act
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class, new Class[]{TwinCollection.class, TwinCollection.class}, (TwinCollection)null, PROPERTIES);

        // assert
        assertEquals(PROPERTIES, Deencapsulation.getField(twinProperties, "reported"));
        assertNull(Deencapsulation.getField(twinProperties, "desired"));
    }

    /* SRS_TWIN_PROPERTIES_21_002: [The constructor shall store the provided desired and reported properties converting from the row collection.] */
    @Test
    public void constructorStoreDesiredAndReportedProperty()
    {
        // arrange
        // act
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class, new Class[]{TwinCollection.class, TwinCollection.class}, PROPERTIES, PROPERTIES);

        // assert
        assertEquals(PROPERTIES, Deencapsulation.getField(twinProperties, "desired"));
        assertEquals(PROPERTIES, Deencapsulation.getField(twinProperties, "reported"));
    }

    /* SRS_TWIN_PROPERTIES_21_003: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementReturnsJsonElement()
    {
        // arrange
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class, new Class[]{TwinCollection.class, TwinCollection.class}, PROPERTIES, PROPERTIES);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinProperties, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), JSON_ONLY_PROPERTIES);
    }

    /* SRS_TWIN_PROPERTIES_21_004: [If the desired property is null, the toJsonElement shall not include the `desired` in the final JSON.] */
    @Test
    public void toJsonElementReturnsDesiredJsonElement()
    {
        // arrange
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class, new Class[]{TwinCollection.class, TwinCollection.class}, PROPERTIES, (TwinCollection)null);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinProperties, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), JSON_ONLY_DESIRED_PROPERTIES);
    }

    /* SRS_TWIN_PROPERTIES_21_005: [If the reported property is null, the toJsonElement shall not include the `reported` in the final JSON.] */
    @Test
    public void toJsonElementReturnsReportedJsonElement()
    {
        // arrange
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class, new Class[]{TwinCollection.class, TwinCollection.class}, (TwinCollection)null, PROPERTIES);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinProperties, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), JSON_ONLY_REPORTED_PROPERTIES);
    }

    /* SRS_TWIN_PROPERTIES_21_004: [If the desired property is null, the toJsonElement shall not include the `desired` in the final JSON.] */
    /* SRS_TWIN_PROPERTIES_21_005: [If the reported property is null, the toJsonElement shall not include the `reported` in the final JSON.] */
    @Test
    public void toJsonElementReturnsEmptyJsonElement()
    {
        // arrange
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinProperties, "toJsonElement");

        // assert
        Helpers.assertJson(jsonElement.toString(), "{}");
    }

    /* SRS_TWIN_PROPERTIES_21_006: [The toJsonElementWithMetadata shall return a JsonElement with the information in this class, including metadata, in a JSON format.] */
    @Test
    public void toJsonElementWithMetadataReturnsJsonElement()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(JSON_FULL_SAMPLE, TwinCollection.class);
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class, new Class[]{TwinCollection.class, TwinCollection.class}, rawMap, rawMap);
        String expectedJson = "{\"desired\":" + JSON_FULL_SAMPLE + ", \"reported\":" + JSON_FULL_SAMPLE + "}";

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinProperties, "toJsonElementWithMetadata");

        // assert
        Helpers.assertJson(jsonElement.toString(), expectedJson);
    }

    /* SRS_TWIN_PROPERTIES_21_007: [If the desired property is null, the toJsonElementWithMetadata shall not include the `desired` in the final JSON.] */
    @Test
    public void toJsonElementWithMetadataReturnsDesiredJsonElement()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(JSON_FULL_SAMPLE, TwinCollection.class);
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class, new Class[]{TwinCollection.class, TwinCollection.class}, rawMap, (TwinCollection)null);
        String expectedJson = "{\"desired\":" + JSON_FULL_SAMPLE + "}";

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinProperties, "toJsonElementWithMetadata");

        // assert
        Helpers.assertJson(jsonElement.toString(), expectedJson);
    }

    /* SRS_TWIN_PROPERTIES_21_008: [If the reported property is null, the toJsonElementWithMetadata shall not include the `reported` in the final JSON.] */
    @Test
    public void toJsonElementWithMetadataReturnsReportedJsonElement()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(JSON_FULL_SAMPLE, TwinCollection.class);
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class, new Class[]{TwinCollection.class, TwinCollection.class}, (TwinCollection)null, rawMap);
        String expectedJson = "{\"reported\":" + JSON_FULL_SAMPLE + "}";

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinProperties, "toJsonElementWithMetadata");

        // assert
        Helpers.assertJson(jsonElement.toString(), expectedJson);
    }

    /* SRS_TWIN_PROPERTIES_21_007: [If the desired property is null, the toJsonElementWithMetadata shall not include the `desired` in the final JSON.] */
    /* SRS_TWIN_PROPERTIES_21_008: [If the reported property is null, the toJsonElementWithMetadata shall not include the `reported` in the final JSON.] */
    @Test
    public void toJsonElementWithMetadataReturnsEmptyJsonElement()
    {
        // arrange
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinProperties, "toJsonElementWithMetadata");

        // assert
        Helpers.assertJson(jsonElement.toString(), "{}");
    }

    /* SRS_TWIN_PROPERTIES_21_009: [The getDesired shall return a TwinCollection with the stored desired property.] */
    /* SRS_TWIN_PROPERTIES_21_010: [The getReported shall return a TwinCollection with the stored reported property.] */
    @Test
    public void getDesiredReturnsDesiredProperty()
    {
        // arrange
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class, new Class[]{TwinCollection.class, TwinCollection.class}, PROPERTIES, PROPERTIES);

        // act - assert
        Helpers.assertMap((TwinCollection)Deencapsulation.invoke(twinProperties, "getDesired"), PROPERTIES);
        Helpers.assertMap((TwinCollection)Deencapsulation.invoke(twinProperties, "getReported"), PROPERTIES);
    }

    /* SRS_TWIN_PROPERTIES_21_011: [The TwinProperties shall provide an empty constructor to make GSON happy.] */
    @Test
    public void constructorSucceed()
    {
        // act
        TwinProperties twinProperties = Deencapsulation.newInstance(TwinProperties.class);

        // assert
        assertNotNull(twinProperties);
    }
}
