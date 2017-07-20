/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.microsoft.azure.sdk.iot.deps.serializer.QueryResponseParser;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static tests.unit.com.microsoft.azure.sdk.iot.deps.serializer.Helpers.assertListEquals;

/*
    Unit tests for QueryResponseParser
    Coverage result : method - 100%, line - 100%
 */
public class QueryResponseParserTest
{
    private static final String VALID_JSON = "{" +
                                             "\"deviceId\":\"devA\"" +
                                                "}";
    private static final String INVALID_JSON = "{\u1234}";
    private static final String MALFORMED_JSON = "abc : abc}";
    private static final String VALID_JSON_ARRAY_1 = "[" + VALID_JSON + "]";
    private static final String VALID_JSON_ARRAY_2 = "[" + VALID_JSON + "," + VALID_JSON + "]";
    private static final String VALID_TWIN_JSON = "{\n" +
            "\t\t\"deviceId\": \"devA\",\n" +
            "\t\t\"generationId\": \"123\",\n" +
            "\t\t\"status\": \"enabled\",\n" +
            "\t\t\"statusReason\": \"provisioned\",\n" +
            "\t\t\"connectionState\": \"connected\",\n" +
            "\t\t\"connectionStateUpdatedTime\": \"2015-02-28T16:24:48.789Z\",\n" +
            "\t\t\"lastActivityTime\": \"2015-02-30T16:24:48.789Z\",\n" +
            "\n" +
            "\t\t\"tags\": {\n" +
            "\t\t\t\"$etag\": \"123\",\n" +
            "\t\t\t\"deploymentLocation\": {\n" +
            "\t\t\t\t\"building\": \"43\",\n" +
            "\t\t\t\t\"floor\": \"1\"\n" +
            "\t\t\t}\n" +
            "\t\t},\n" +
            "\t\t\"properties\": {\n" +
            "\t\t\t\"desired\": {\n" +
            "\t\t\t\t\"telemetryConfig\": {\n" +
            "\t\t\t\t\t\"sendFrequency\": \"5m\"\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"$metadata\": {},\n" +
            "\t\t\t\t\"$version\": 1\n" +
            "\t\t\t},\n" +
            "\t\t\t\"reported\": {\n" +
            "\t\t\t\t\"telemetryConfig\": {\n" +
            "\t\t\t\t\t\"sendFrequency\": \"5m\",\n" +
            "\t\t\t\t\t\"status\": \"success\"\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t\"batteryLevel\": 55,\n" +
            "\t\t\t\t\"$metadata\": {},\n" +
            "\t\t\t\t\"$version\": 4\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t}";
    private static final String VALID_TWIN_JSON_ARRAY_1 = "[" + VALID_TWIN_JSON + "]";
    private static final String VALID_TWIN_JSON_ARRAY_2 = "[" + VALID_TWIN_JSON + ","
                                                            + VALID_TWIN_JSON + "]";

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private static String buildJsonInputArrayFromJson(String itemsArray)
    {
        return "[" + itemsArray + "]";
    }

    private static List<?> buildListFromJsonArray(String jsonArrayString)
    {
        JsonObject[] jsonItems = gson.fromJson(jsonArrayString, JsonObject[].class);
        List<String> list = new LinkedList<>();

        for(JsonObject json : jsonItems)
        {
            list.add(gson.toJson(json));
        }
        return list;
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_001: [The constructor shall create an instance of the QueryResponseParser.]
    //Tests_SRS_QUERY_RESPONSE_PARSER_25_002: [The constructor shall save the type provided.]
    @Test
    public void constructorSucceeds() throws IllegalArgumentException
    {
        //act
        QueryResponseParser testParser = new QueryResponseParser(VALID_JSON_ARRAY_1);

        //assert
        assertListEquals(buildListFromJsonArray(VALID_JSON_ARRAY_1), testParser.getJsonItems());
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_003: [If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnMalformedJson() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInputArrayFromJson(MALFORMED_JSON);

        //act
        QueryResponseParser testParser = new QueryResponseParser(testJson);

        //assert
        assertListEquals(buildListFromJsonArray(testJson), testParser.getJsonItems());
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidUTF8Json() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInputArrayFromJson(INVALID_JSON);
        //act
        QueryResponseParser testParser = new QueryResponseParser(testJson);
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_008: [The getJsonItems shall return the list of json items as strings .]
    @Test
    public void getJsonItemsGets() throws IllegalArgumentException
    {
        //arrange
        QueryResponseParser testParser = new QueryResponseParser(VALID_TWIN_JSON_ARRAY_2);

        //act/assert
        assertListEquals(buildListFromJsonArray(VALID_TWIN_JSON_ARRAY_2), testParser.getJsonItems());
    }

    @Test
    public void getJsonItemsGets2() throws IllegalArgumentException
    {
        //arrange
        QueryResponseParser testParser = new QueryResponseParser(VALID_JSON_ARRAY_2);

        //act/assert
        assertListEquals(buildListFromJsonArray(VALID_JSON_ARRAY_2), testParser.getJsonItems());
    }

}
