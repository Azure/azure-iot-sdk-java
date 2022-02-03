package com.microsoft.azure.sdk.iot.service.configurations;

import com.microsoft.azure.sdk.iot.service.ParserUtility;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashMap;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class ConfigurationParserTest
{
    //Tests_SRS_CONFIGURATION_PARSER_28_001: [If the provided json is null or empty, an IllegalArgumentException shall be thrown.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsForNullJson()
    {
        //act
        new ConfigurationParser(null);
    }

    //Codes_SRS_CONFIGURATION_PARSER_28_002: [This constructor shall take the provided json and convert it into
    // a new ConfigurationParser and return it.]
    @Test
    public void constructorFromJson()
    {
        //arrange
        String json = "{\"id\":\"someconfig\",\"schemaVersion\":\"1.0\",\"etag\":\"MQ==\",\"" +
                "labels\":{\"App\":\"label2\"},\"content\":{\"modulesContent\":{}, \"deviceContent\":{\"properties.desired.settings1\": {\"c\":3,\"d\":4}}}," +
                "\"targetCondition\":\"*\", \"createdTimeUtc\":\"0001-01-01T00:00:00\", \"lastUpdatedTimeUtc\":\"0001-01-01T00:00:00\"," +
                "\"priority\":10, \"systemMetrics\":{\"results\":{\"targetedCount\":3, \"appliedCount\":3}, " +
                "\"queries\":{}}, \"metrics\":{\"results\":{\"customMetric\":3}," +
                "\"queries\":{}}}";

        //act
        ConfigurationParser parser = new ConfigurationParser(json);

        //assert
        assertNotNull(parser);
        assertEquals("someconfig", parser.getId());
        assertEquals("1.0", parser.getSchemaVersion());
        assertEquals("MQ==", parser.getETag());
        TestCase.assertEquals(ParserUtility.getDateTimeUtc("0001-01-01T00:00:00"), parser.getCreatedTimeUtc());
        assertEquals(ParserUtility.getDateTimeUtc("0001-01-01T00:00:00"), parser.getLastUpdatedTimeUtc());
        assertNotNull(parser.getContent());
        assertNotNull(parser.getSystemMetrics());
        assertNotNull(parser.getMetrics());
    }

    //Codes_SRS_CONFIGURATION_PARSER_28_003: [This constructor shall take the provided json and convert it into
    // a new ConfigurationParser and return it even when a schemaVersion is not provided.]
    @Test
    public void constructorSucceedsWithoutSchemaVersion()
    {
        //arrange
        String json = "{\"id\":\"someconfig\",\"etag\":\"MQ==\",\"" +
                "labels\":{\"App\":\"label2\"},\"content\":{\"modulesContent\":{}, \"deviceContent\":{\"properties.desired.settings1\": {\"c\":3,\"d\":4}}}," +
                "\"targetCondition\":\"*\", \"createdTimeUtc\":\"0001-01-01T00:00:00\", \"lastUpdatedTimeUtc\":\"0001-01-01T00:00:00\"," +
                "\"priority\":10, \"systemMetrics\":{\"results\":{\"targetedCount\":3, \"appliedCount\":3}, " +
                "\"queries\":{}}, \"metrics\":{\"results\":{\"customMetric\":3}," +
                "\"queries\":{}}}";

        //act
        ConfigurationParser parser = new ConfigurationParser(json);

        //assert
        assertNotNull(parser);
        assertEquals("someconfig", parser.getId());
        assertEquals("MQ==", parser.getETag());
        assertEquals(ParserUtility.getDateTimeUtc("0001-01-01T00:00:00"), parser.getCreatedTimeUtc());
        assertEquals(ParserUtility.getDateTimeUtc("0001-01-01T00:00:00"), parser.getLastUpdatedTimeUtc());
        assertNotNull(parser.getContent());
        assertNotNull(parser.getSystemMetrics());
        assertNotNull(parser.getMetrics());
    }

    //Tests_SRS_CONFIGURATION_PARSER_28_005: [If the provided json cannot be parsed into a ConfigurationParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidJson()
    {
        //arrange
        String json = "{";

        //act
        new ConfigurationParser(json);
    }

    //Codes_SRS_CONFIGURATION_PARSER_28_003: [If the provided json is missing the id field or its value is empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorFromJsonEmptyId()
    {
        //arrange
        String json = "{\"id\":\"\",\"schemaVersion\":\"1.0\",\"etag\":\"MQ==\",\"" +
                "labels\":{\"App\":\"label2\"},\"content\":{\"modulesContent\":{}, \"deviceContent\":{\"properties.desired.settings1\": {\"c\":3,\"d\":4}}}," +
                "\"targetCondition\":\"*\", \"createdTimeUtc\":\"0001-01-01T00:00:00\", \"lastUpdatedTimeUtc\":\"0001-01-01T00:00:00\"," +
                "\"priority\":10, \"systemMetrics\":{\"results\":{\"targetedCount\":3, \"appliedCount\":3}, " +
                "\"queries\":{}}, \"metrics\":{\"results\":{\"customMetric\":3}," +
                "\"queries\":{}}}";

        //act
        new ConfigurationParser(json);
    }

    //Codes_SRS_CONFIGURATION_PARSER_28_003: [If the provided json is missing the id field or its value is empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorFromJsonNullId()
    {
        //arrange
        String json = "{\"schemaVersion\":\"1.0\",\"etag\":\"MQ==\",\"" +
                "labels\":{\"App\":\"label2\"},\"content\":{\"modulesContent\":{}, \"deviceContent\":{\"properties.desired.settings1\": {\"c\":3,\"d\":4}}}," +
                "\"targetCondition\":\"*\", \"createdTimeUtc\":\"0001-01-01T00:00:00\", \"lastUpdatedTimeUtc\":\"0001-01-01T00:00:00\"," +
                "\"priority\":10, \"systemMetrics\":{\"results\":{\"targetedCount\":3, \"appliedCount\":3}, " +
                "\"queries\":{}}, \"metrics\":{\"results\":{\"customMetric\":3}," +
                "\"queries\":{}}}";

        //act
        new ConfigurationParser(json);
    }

    //Codes_SRS_CONFIGURATION_PARSER_28_003: [If the provided json is missing the schemaVersion or its value is empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorFromJsonEmptySchemaVersion()
    {
        //arrange
        String json = "{\"id\":\"\",\"schemaVersion\":\"\",\"etag\":\"MQ==\",\"" +
                "labels\":{\"App\":\"label2\"},\"content\":{\"modulesContent\":{}, \"deviceContent\":{\"properties.desired.settings1\": {\"c\":3,\"d\":4}}}," +
                "\"targetCondition\":\"*\", \"createdTimeUtc\":\"0001-01-01T00:00:00\", \"lastUpdatedTimeUtc\":\"0001-01-01T00:00:00\"," +
                "\"priority\":10, \"systemMetrics\":{\"results\":{\"targetedCount\":3, \"appliedCount\":3}, " +
                "\"queries\":{}}, \"metrics\":{\"results\":{\"customMetric\":3}," +
                "\"queries\":{}}}";

        //act
        new ConfigurationParser(json);
    }

    //Tests_SRS_CONFIGURATION_PARSER_28_009: [This method shall set the value of id to the provided value.]
    //Tests_SRS_CONFIGURATION_PARSER_28_010: [This method shall return the value of this object's schemaVersion.]
    //Tests_SRS_CONFIGURATION_PARSER_28_012: [This method shall set the value of schemaVersion to the provided value.]
    //Tests_SRS_CONFIGURATION_PARSER_28_013: [This method shall return the value of this object's labels.]
    //Tests_SRS_CONFIGURATION_PARSER_28_014: [This method shall set the value of labels to the provided value.]
    //Tests_SRS_CONFIGURATION_PARSER_28_015: [This method shall return the value of this object's contentParser.]
    //Tests_SRS_CONFIGURATION_PARSER_28_016: [This method shall set the value of contentParser to the provided value.]
    //Tests_SRS_CONFIGURATION_PARSER_28_019: [This method shall return the value of this object's targetCondition.]
    //Tests_SRS_CONFIGURATION_PARSER_28_020: [This method shall set the value of targetCondition to the provided value.]
    //Tests_SRS_CONFIGURATION_PARSER_28_021: [This method shall return the value of this object's createdTimeUtc.]
    //Tests_SRS_CONFIGURATION_PARSER_28_022: [This method shall set the value of this object's statusUpdatedTime equal to the provided value.]
    //Tests_SRS_CONFIGURATION_PARSER_28_023: [This method shall return the value of this object's lastUpdatedTimeUtc.]
    //Tests_SRS_CONFIGURATION_PARSER_28_024: [This method shall set the value of this object's lastUpdatedTimeUtc equal to the provided value.]
    //Tests_SRS_CONFIGURATION_PARSER_28_025: [This method shall return the value of this object's priority.]
    //Tests_SRS_CONFIGURATION_PARSER_28_026: [This method shall set the value of priority to the provided value.]
    //Codes_SRS_CONFIGURATION_PARSER_28_031: [This method shall return the value of this object's systemMetricsParser.]
    //Codes_SRS_CONFIGURATION_PARSER_28_032: [This method shall set the value of systemMetricsParser to the provided value.]
    //Codes_SRS_CONFIGURATION_PARSER_28_027: [This method shall return the value of this object's metrics.]
    //Codes_SRS_CONFIGURATION_PARSER_28_028: [This method shall set the value of metrics to the provided value.]
    //Codes_SRS_CONFIGURATION_PARSER_28_029: [This method shall return the value of this object's ETag.]
    //Codes_SRS_CONFIGURATION_PARSER_28_030: [This method shall set the value of this object's ETag equal to the provided value.]
    @Test
    public void gettersAndSetters()
    {
        //arrange
        String id = "someId";
        String schemaVersion = "1.0";
        ConfigurationParser parser = new ConfigurationParser();
        String timeDate = "0001-01-01T00:00:00";

        //act
        parser.setId(id);
        parser.setSchemaVersion(schemaVersion);
        parser.setLabels(new HashMap<String, String>(){{put("xyz", "123");}});
        parser.setContent(new ConfigurationContentParser());
        parser.setTargetCondition("condition");
        parser.setCreatedTimeUtc(ParserUtility.getDateTimeUtc(timeDate));
        parser.setLastUpdatedTimeUtc(ParserUtility.getDateTimeUtc(timeDate));
        parser.setPriority(10);
        parser.setSystemMetrics(new ConfigurationMetricsParser());
        parser.setMetrics(new ConfigurationMetricsParser());
        parser.setETag("etag1");
        parser.setContentType("text/html");

        //assert
        assertEquals(id, parser.getId());
        assertEquals(schemaVersion, parser.getSchemaVersion());
        assertEquals("123", parser.getLabels().get("xyz"));
        assertNotNull(parser.getContent());
        assertEquals("condition", parser.getTargetCondition());
        assertEquals(ParserUtility.getDateTimeUtc(timeDate), parser.getCreatedTimeUtc());
        assertEquals(ParserUtility.getDateTimeUtc(timeDate), parser.getLastUpdatedTimeUtc());
        assertEquals((Integer)10, parser.getPriority());
        assertNotNull(parser.getSystemMetrics());
        assertNotNull(parser.getMetrics());
        assertEquals("etag1", parser.getETag());
        assertEquals("text/html", parser.getContentType());
    }
}
