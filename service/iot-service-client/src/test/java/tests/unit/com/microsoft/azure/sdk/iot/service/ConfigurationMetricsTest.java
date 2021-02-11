package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.ConfigurationMetrics;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigurationMetricsTest
{
    // Tests_SRS_SERVICE_SDK_JAVA_CONFIGURATION_METRICS_28_001: [The constructor shall initialize results and queries fields.]
    @Test
    public void constructor_initialize()
    {
        // Act
        ConfigurationMetrics cm = new ConfigurationMetrics();

        // Assert
        assertNotNull(cm);
        assertNotNull(cm.getQueries());
        assertNotNull(cm.getResults());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_CONFIGURATION_METRICS_28_002: [The ConfigurationMetrics class shall have the following properties: results and queries
    @Test
    public void getterAndSetter()
    {
        // Arrange
        HashMap<String, Long> results = new HashMap<String, Long>() {
            {
                put("abc", 3L);
                put("def", 5L);
            }
        };

        HashMap<String, String> queries = new HashMap<String, String>() {
            {
                put("c", "d");
                put("e", "f");
            }
        };

        ConfigurationMetrics cm = new ConfigurationMetrics();

        // Act
        cm.setQueries(queries);
        cm.setResults(results);


        // Assert
        assertNotNull(cm);
        Map<String, Long> resultsMap = cm.getResults();
        assertEquals(new Long(3), resultsMap.get("abc"));
        assertEquals(new Long(5), resultsMap.get("def"));
        Map<String, String> queriesMap = cm.getQueries();
        assertEquals("d", queriesMap.get("c"));
        assertEquals("f", queriesMap.get("e"));
    }
}
