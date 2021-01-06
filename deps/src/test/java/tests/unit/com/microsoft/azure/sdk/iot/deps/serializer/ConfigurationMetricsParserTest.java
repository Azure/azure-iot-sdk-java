package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.ConfigurationMetricsParser;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class ConfigurationMetricsParserTest
{
    //Tests_SRS_CONFIGURATION_METRICS_PARSER_28_001: [If the provided json is null, empty, an IllegalArgumentException shall be thrown.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsForNullJson()
    {
        //act
        new ConfigurationMetricsParser(null);
    }

    //Tests_SRS_CONFIGURATION_METRICS_PARSER_28_001: [If the provided json is null, empty, an IllegalArgumentException shall be thrown.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyJson()
    {
        //act
        new ConfigurationMetricsParser("");
    }

    //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_002: [The constructor shall take the provided json and convert
    // it into a new ConfigurationMetricsParser and return it.]
    @Test
    public void constructorFromJson()
    {
        //arrange
        String json = "{\"results\":{\"abc\":3,\"def\":5}, " +
                "\"queries\":{\"c\":\"d\",\"e\":\"f\"}}";

        //act
        ConfigurationMetricsParser parser = new ConfigurationMetricsParser(json);

        //assert
        assertNotNull(parser);
        Map<String, Long> resultsMap = parser.getResults();
        assertEquals(new Long(3), resultsMap.get("abc"));
        assertEquals(new Long(5), resultsMap.get("def"));
        Map<String, String> queriesMap = parser.getQueries();
        assertEquals("d", queriesMap.get("c"));
        assertEquals("f", queriesMap.get("e"));
    }

    //Tests_SRS_CONFIGURATION_METRICS_PARSER_28_003: [If the provided json cannot be parsed into a ConfigurationMetricsParser
    // object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidJson()
    {
        //arrange
        String json = "{";

        //act
        new ConfigurationMetricsParser(json);
    }

    //Tests_SRS_CONFIGURATION_METRICS_PARSER_28_005: [This method shall return the value of this object's results.]
    //Tests_SRS_CONFIGURATION_METRICS_PARSER_28_006: [This method shall set the value of results to the provided value.]
    //Tests_SRS_CONFIGURATION_METRICS_PARSER_28_007: [This method shall return the value of this object's queries.]
    //Tests_SRS_CONFIGURATION_METRICS_PARSER_28_008: [This method shall set the value of queries to the provided value.]
    @Test
    public void gettersAndSetters()
    {
        //arrange
        HashMap<String, Long> results = new HashMap<String, Long>() {
            {
                put("abc", new Long(3));
                put("def", new Long(5));
            }
        };

        HashMap<String, String> queries = new HashMap<String, String>() {
            {
                put("c", "d");
                put("e", "f");
            }
        };
        ConfigurationMetricsParser parser = new ConfigurationMetricsParser();

        //act
        parser.setResults(results);
        parser.setQueries(queries);

        //assert

        //assert
        assertNotNull(parser);
        Map<String, Long> resultsMap = parser.getResults();
        assertEquals(new Long(3), resultsMap.get("abc"));
        assertEquals(new Long(5), resultsMap.get("def"));
        Map<String, String> queriesMap = parser.getQueries();
        assertEquals("d", queriesMap.get("c"));
        assertEquals("f", queriesMap.get("e"));
    }
}
