package com.microsoft.azure.sdk.iot.service.configurations;

import com.google.gson.JsonElement;
import com.microsoft.azure.sdk.iot.service.configurations.serializers.ConfigurationContentParser;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigurationContentParserTest
{
    //Tests_SRS_CONFIGURATION_CONTENT_PARSER_28_001: [If the provided json is null, empty, an IllegalArgumentException shall be thrown.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsForNullJson()
    {
        //act
        new ConfigurationContentParser(null);
    }

    //Tests_SRS_CONFIGURATION_CONTENT_PARSER_28_001: [If the provided json is null, empty, an IllegalArgumentException shall be thrown.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyJson()
    {
        //act
        new ConfigurationContentParser("");
    }

    //Tests_SRS_CONFIGURATION_CONTENT_PARSER_28_002: [The constructor shall take the provided json and convert
    // it into a new ConfigurationContentParser and return it.]
    @Test
    public void constructorFromJson()
    {
        //arrange
        String json = "{\"modulesContent\":{\"properties\":{\"c\":\"abc\",\"d\":\"def\"}}, " +
                "\"deviceContent\":{\"properties.desired.settings1\": {\"c\":3,\"d\":4}}}";

        //act
        ConfigurationContentParser parser = new ConfigurationContentParser(json);

        //assert
        assertNotNull(parser);
        Map<String, Object> moduleContentMap = parser.getModulesContent().get("properties");
        assertEquals("abc", moduleContentMap.get("c"));
        assertEquals("def", moduleContentMap.get("d"));
        Map<String, Object> deviceContentMap = ((Map<String,Object>)(parser.getDeviceContent().get("properties.desired.settings1")));
        assertEquals((double)3, deviceContentMap.get("c"));
        assertEquals((double)4, deviceContentMap.get("d"));
    }

    //Tests_SRS_CONFIGURATION_CONTENT_PARSER_28_003: [If the provided json cannot be parsed into a ConfigurationContentParser
    // object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidJson()
    {
        //arrange
        String json = "{";

        //act
        new ConfigurationContentParser(json);
    }

    //Tests_SRS_CONFIGURATION_METRICS_PARSER_28_005: [This method shall return the value of this object's modulesContent.]
    //Tests_SRS_CONFIGURATION_METRICS_PARSER_28_006: [This method shall set the value of results to the provided value.]
    //Tests_SRS_CONFIGURATION_METRICS_PARSER_28_007: [This method shall return the value of this object's deviceContent.]
    //Tests_SRS_CONFIGURATION_METRICS_PARSER_28_008: [This method shall set the value of queries to the provided value.]
    @Test
    public void gettersAndSetters()
    {
        //arrange
        Map<String, Map<String, Object>> mc = new HashMap<String, Map<String, Object>>() {{put("mproperty",
                new HashMap<String, Object>(){{put("abc", "123"); put("cde", "456");}});}};
        Map<String, Object> dc = new HashMap<String, Object>(){{put("dproperty",
                new HashMap<String, Integer>(){{put("c", 3);put("d", 4);}});}};
        ConfigurationContentParser parser = new ConfigurationContentParser();

        //act
        parser.setDeviceContent(dc);
        parser.setModulesContent(mc);

        //assert
        Map<String, Object> moduleContentMap = (parser.getModulesContent().get("mproperty"));
        assertEquals("123", moduleContentMap.get("abc"));
        assertEquals("456", moduleContentMap.get("cde"));
        Map<String, Object> deviceContentMap = ((Map<String,Object>)(parser.getDeviceContent().get("dproperty")));
        assertEquals(3, deviceContentMap.get("c"));
        assertEquals(4, deviceContentMap.get("d"));
    }

    /* Codes_SRS_CONFIGURATION_METRICS_PARSER_28_009: [If the modulesContent is null, the toJsonElement shall not include the `modulesContent` in the final JSON.] */
    @Test
    public void toJsonNoModulesContent()
    {
        //arrange
        HashMap<String, HashMap<String, Object>> mc = new HashMap<String, HashMap<String, Object>>(){{put("mproperty",
                new HashMap<String, Object>(){{put("abc", "123"); put("cde", "456");}});}};
        HashMap<String, Object> dc = new HashMap<String, Object>(){{put("dproperty",
                new HashMap<String, Integer>(){{put("c", 3);put("d", 4);}});}};
        ConfigurationContentParser parser = new ConfigurationContentParser();
        String modulesContent = "modulesContent";
        String deviceContent = "deviceContent";
        parser.setDeviceContent(dc);

        // act
        JsonElement contentElement = parser.toJsonElement();

        // assert
        assertTrue(contentElement.toString().contains(deviceContent));
        assertFalse(contentElement.toString().contains(modulesContent));
    }

    /* Codes_SRS_CONFIGURATION_METRICS_PARSER_28_010: [If the deviceContent is null, the toJsonElement shall not include the `deviceContent` in the final JSON.]*/
    @Test
    public void toJsonNoDeviceContent()
    {
        //arrange
        Map<String, Map<String, Object>> mc = new HashMap<String, Map<String, Object>>(){{put("mproperty",
                new HashMap<String, Object>(){{put("abc", "123"); put("cde", "456");}});}};
        Map<String, Object> dc = new HashMap<String, Object>(){{put("dproperty",
                new HashMap<String, Integer>(){{put("c", 3);put("d", 4);}});}};
        ConfigurationContentParser parser = new ConfigurationContentParser();
        String modulesContent = "modulesContent";
        String deviceContent = "deviceContent";
        parser.setModulesContent(mc);

        // act
        JsonElement contentElement = parser.toJsonElement();

        // assert
        assertFalse(contentElement.toString().contains(deviceContent));
        assertTrue(contentElement.toString().contains(modulesContent));
    }
}
