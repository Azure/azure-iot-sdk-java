/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.QueryResponseParser;
import com.microsoft.azure.sdk.iot.deps.serializer.TwinParser;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/*
    Unit tests for QueryResponseParser
    Coverage result : method - 83%, line - 89%
 */
public class QueryResponseParserTest
{
    private static final String [] VALID_TYPE = {"unknown", "twin", "deviceJob", "jobResponse", "raw" };
    private static final String INVALID_TYPE = "invalid";
    private static final String VALID_JSON = "{" +
                                             "\"deviceId\":\"devA\"" +
                                                "}";
    private static final String INVALID_JSON = "{\u1234}";
    private static final String MALFORMED_JSON = "{abc : abc}";
    private static final String VALID_JSON_ARRAY_1 = "[" + VALID_JSON + "]";
    private static final String VALID_JSON_ARRAY_2 = "[" + VALID_JSON + "," + VALID_JSON + "]";
    private static final String VALID_CONTINUATION_TOKEN = UUID.randomUUID().toString();
    private static final String INVALID_CONTINUATION_TOKEN = "\u1234";
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

    private static String buildJsonInput(String type, String itemsArray, String token)
    {
        return "{\n" +
                "    \"type\": \"" + type + "\",\n" +
                "    \"items\": " + itemsArray + ",\n" +
                "    \"continuationToken\": \"" + token + "\"\n" +
                "}";
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_001: [The constructor shall create an instance of the QueryResponseParser.]
    //Tests_SRS_QUERY_RESPONSE_PARSER_25_002: [The constructor shall parse the provided json and initialize type, continuationToken and jsonItems using the information in the json.]
    @Test
    public void constructorSucceeds() throws IllegalArgumentException
    {
        for (int i = 1; i < VALID_TYPE.length; i++)
        {
            //arrange
            final String testJson = buildJsonInput(VALID_TYPE[i], VALID_JSON_ARRAY_1, VALID_CONTINUATION_TOKEN);

            //act
            QueryResponseParser testParser = new QueryResponseParser(testJson);

            //assert
            assertEquals(VALID_TYPE[i], testParser.getType());
            assertEquals(VALID_JSON_ARRAY_1, testParser.getJsonItemsArray());
            assertEquals(VALID_CONTINUATION_TOKEN, testParser.getContinuationToken());
        }
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_003: [If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidJson() throws IllegalArgumentException
    {
        for(int i = 0; i < VALID_TYPE.length; i++)
        {
            //arrange
            final String testJson = buildJsonInput(VALID_TYPE[i], MALFORMED_JSON, VALID_CONTINUATION_TOKEN);

            //act
            QueryResponseParser testParser = new QueryResponseParser(testJson);

            //assert
            assertEquals(VALID_TYPE[i], testParser.getType());
            assertEquals(VALID_JSON_ARRAY_1, testParser.getJsonItemsArray());
            assertEquals(VALID_CONTINUATION_TOKEN, testParser.getContinuationToken());
        }
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidUTF8Json() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInput(VALID_TYPE[0], INVALID_JSON, VALID_CONTINUATION_TOKEN);

        //act
        QueryResponseParser testParser = new QueryResponseParser(testJson);
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_005: [If the provided json do not contains one of the keys type, continuationToken and jsonItems, the constructor shall throws IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidType() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInput(INVALID_TYPE, VALID_JSON_ARRAY_1, VALID_CONTINUATION_TOKEN);

        //act
        QueryResponseParser testParser = new QueryResponseParser(testJson);
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_006: [If the provided json is of type other than twin, raw, deviceJob or jobResponse, the constructor shall throws IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnUnknownType() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInput(VALID_TYPE[0], VALID_JSON_ARRAY_1, VALID_CONTINUATION_TOKEN);

        //act
        QueryResponseParser testParser = new QueryResponseParser(testJson);
    }
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidUTF8JsonToken() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInput(VALID_TYPE[0], VALID_JSON_ARRAY_1, INVALID_CONTINUATION_TOKEN);

        //act
        QueryResponseParser testParser = new QueryResponseParser(testJson);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidUTF8JsonItems() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInput(VALID_TYPE[0], INVALID_JSON, VALID_CONTINUATION_TOKEN);

        //act
        QueryResponseParser testParser = new QueryResponseParser(testJson);
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_007: [The getType shall return the string stored in type enum.]
    @Test
    public void getTypeGets() throws IllegalArgumentException
    {
        for (int i = 1; i < VALID_TYPE.length; i++)
        {
            //arrange
            final String testJson = buildJsonInput(VALID_TYPE[i], VALID_JSON_ARRAY_1, VALID_CONTINUATION_TOKEN);
            QueryResponseParser testParser = new QueryResponseParser(testJson);

            //act
            String actualType = testParser.getType();

            //assert
            assertEquals(VALID_TYPE[i], actualType);
        }
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_008: [The getJsonItemsArray shall return the array of json items as string .]
    @Test
    public void getJsonItemsGets() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInput(VALID_TYPE[1], VALID_JSON_ARRAY_1, VALID_CONTINUATION_TOKEN);
        QueryResponseParser testParser = new QueryResponseParser(testJson);

        //act
        String actualItems = testParser.getJsonItemsArray();

        //assert
        assertEquals(VALID_JSON_ARRAY_1, actualItems);
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_009: [The getContinuationToken shall return the string stored in continuationToken.]
    @Test
    public void getContinuationTokenGets() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInput(VALID_TYPE[1], VALID_JSON_ARRAY_1, VALID_CONTINUATION_TOKEN);
        QueryResponseParser testParser = new QueryResponseParser(testJson);

        //act
        String actualItems = testParser.getContinuationToken();

        //assert
        assertEquals(VALID_CONTINUATION_TOKEN, actualItems);
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_010: [The getTwins shall return the collection of twin parsers as retrieved and parsed from json.]
    @Test
    public void getTwinGets() throws IllegalArgumentException, IOException
    {
        //arrange
        final String testJson = buildJsonInput(VALID_TYPE[1], VALID_TWIN_JSON_ARRAY_1, VALID_CONTINUATION_TOKEN);
        final TwinParser testTwin = new TwinParser();
        testTwin.enableTags();
        testTwin.updateTwin(VALID_TWIN_JSON);
        QueryResponseParser testParser = new QueryResponseParser(testJson);

        //act
        List<TwinParser> actualTwins = testParser.getTwins();
        TwinParser twin = actualTwins.get(0);

        //assert
        assertEquals(twin.getDeviceId(), testTwin.getDeviceId());
        assertEquals(twin.getGenerationId(), testTwin.getGenerationId());
        assertEquals(twin.getStatus(), testTwin.getStatus());
        assertEquals(twin.getStatusReason(), testTwin.getStatusReason());
        assertEquals(twin.getConnectionState(), testTwin.getConnectionState());
        assertEquals(twin.getConnectionStateUpdatedTime(), testTwin.getConnectionStateUpdatedTime());
        assertEquals(twin.getLastActivityTime(), testTwin.getLastActivityTime());
        assertEquals(twin.getTagsMap(), testTwin.getTagsMap());
        assertEquals(twin.getDesiredPropertyMap(), testTwin.getDesiredPropertyMap());
        assertEquals(twin.getReportedPropertyMap(), testTwin.getReportedPropertyMap());
        assertEquals(twin.getReportedPropertyMap(), testTwin.getReportedPropertyMap());
    }

    @Test
    public void getTwinArrayGets() throws IllegalArgumentException, IOException
    {
        //arrange
        final String testJson = buildJsonInput(VALID_TYPE[1], VALID_TWIN_JSON_ARRAY_2, VALID_CONTINUATION_TOKEN);
        final TwinParser testTwin = new TwinParser();
        testTwin.enableTags();
        testTwin.updateTwin(VALID_TWIN_JSON);
        QueryResponseParser testParser = new QueryResponseParser(testJson);

        //act
        List<TwinParser> actualTwins = testParser.getTwins();

        TwinParser twin1 = actualTwins.get(0);
        TwinParser twin2 = actualTwins.get(1);

        //assert
        assertEquals(actualTwins.size(), 2);
        assertEquals(twin1.getDeviceId(), testTwin.getDeviceId());
        assertEquals(twin1.getGenerationId(), testTwin.getGenerationId());
        assertEquals(twin1.getStatus(), testTwin.getStatus());
        assertEquals(twin1.getStatusReason(), testTwin.getStatusReason());
        assertEquals(twin1.getConnectionState(), testTwin.getConnectionState());
        assertEquals(twin1.getConnectionStateUpdatedTime(), testTwin.getConnectionStateUpdatedTime());
        assertEquals(twin1.getLastActivityTime(), testTwin.getLastActivityTime());
        assertEquals(twin1.getTagsMap(), testTwin.getTagsMap());
        assertEquals(twin1.getDesiredPropertyMap(), testTwin.getDesiredPropertyMap());
        assertEquals(twin1.getReportedPropertyMap(), testTwin.getReportedPropertyMap());
        assertEquals(twin1.getReportedPropertyMap(), testTwin.getReportedPropertyMap());

        assertEquals(twin2.getDeviceId(), testTwin.getDeviceId());
        assertEquals(twin2.getGenerationId(), testTwin.getGenerationId());
        assertEquals(twin2.getStatus(), testTwin.getStatus());
        assertEquals(twin2.getStatusReason(), testTwin.getStatusReason());
        assertEquals(twin2.getConnectionState(), testTwin.getConnectionState());
        assertEquals(twin2.getConnectionStateUpdatedTime(), testTwin.getConnectionStateUpdatedTime());
        assertEquals(twin2.getLastActivityTime(), testTwin.getLastActivityTime());
        assertEquals(twin2.getTagsMap(), testTwin.getTagsMap());
        assertEquals(twin2.getDesiredPropertyMap(), testTwin.getDesiredPropertyMap());
        assertEquals(twin2.getReportedPropertyMap(), testTwin.getReportedPropertyMap());
        assertEquals(twin2.getReportedPropertyMap(), testTwin.getReportedPropertyMap());
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_011: [The getTwins shall throw IllegalStateException if the type represented by json is not "twin"]
    @Test (expected = IllegalStateException.class)
    public void getTwinThrowsIfNotTwin() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInput(VALID_TYPE[2], VALID_TWIN_JSON_ARRAY_1, VALID_CONTINUATION_TOKEN);

        QueryResponseParser testParser = new QueryResponseParser(testJson);

        //act
        List<TwinParser> actualTwins = testParser.getTwins();
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_019: [The getRawData shall return the collection of raw data json as string as retrieved and parsed from json.]
    @Test
    public void getRawDataGets() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInput(VALID_TYPE[4], VALID_JSON_ARRAY_2, VALID_CONTINUATION_TOKEN);
        QueryResponseParser testParser = new QueryResponseParser(testJson);

        //act
        List<String> actualRaws = testParser.getRawData();
        String jsonRaw = actualRaws.get(0);

        //assert
        assertEquals(jsonRaw, VALID_JSON);
    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_020: [The getRawData shall throw IllegalStateException if the type represented by json is not "raw"]
    @Test (expected = IllegalStateException.class)
    public void getRawDataThrowsIfNotRaw() throws IllegalArgumentException
    {
        //arrange
        final String testJson = buildJsonInput(VALID_TYPE[1], VALID_JSON_ARRAY_2, VALID_CONTINUATION_TOKEN);

        QueryResponseParser testParser = new QueryResponseParser(testJson);

        //act
        List<String> actualRaws = testParser.getRawData();
    }

    /*
    *  Waiting for Jobs and device jobs parser to enable these tests
    */
    //Tests_SRS_QUERY_RESPONSE_PARSER_25_013: [The getDeviceJobs shall return the collection of device jobs parsers as retrieved and parsed from json.]
    @Ignore
    @Test
    public void getDeviceJobsGets() throws IllegalArgumentException
    {

    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_014: [The getDeviceJobs shall throw IllegalStateException if the type represented by json is not "deviceJobs"]
    @Ignore
    @Test
    public void getDeviceJobsThrowsIfNotDeviceJobs() throws IllegalArgumentException
    {

    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_016: [The getJobs shall return the collection of jobs parsers as retrieved and parsed from json.]
    @Ignore
    @Test
    public void getJobsGets() throws IllegalArgumentException
    {

    }

    //Tests_SRS_QUERY_RESPONSE_PARSER_25_017: [The getJobs shall throw IllegalStateException if the type represented by json is not "jobResponse"]
    @Ignore
    @Test
    public void getJobsThrowsIfNotJobs() throws IllegalArgumentException
    {

    }
}
