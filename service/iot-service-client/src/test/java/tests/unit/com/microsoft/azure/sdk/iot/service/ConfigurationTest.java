package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.*;
import com.microsoft.azure.sdk.iot.service.Configuration;
import com.microsoft.azure.sdk.iot.service.ConfigurationMetrics;
import mockit.Deencapsulation;
import mockit.Mocked;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigurationTest
{
    // Tests_SRS_SERVICE_SDK_JAVA_CONFIGURATION_28_001: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void constructor_string_null() throws IllegalArgumentException
    {
        // Arrange
        String configurationId = null;

        // Act
        Deencapsulation.newInstance(Configuration.class, configurationId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_CONFIGURATION_28_001: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void constructor_string_empty() throws IllegalArgumentException
    {
        // Arrange
        String configurationId = "";

        // Act
        Deencapsulation.newInstance(Configuration.class, configurationId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_002: [The constructor shall initialize all properties to default values]
    @Test
    public void constructor_initialize_properties()
    {
        // Arrange
        String utcTimeDefault = "0001-01-01T00:00:00";
        String configurationId = "xxx-configuration";

        // Act
        Configuration config = Deencapsulation.newInstance(Configuration.class, configurationId);

        // Assert
        assertNotEquals(null, config);

        assertEquals(configurationId, config.getId());
        assertEquals("1.0", config.getSchemaVersion());
        assertNotNull(config.getMetrics());
        assertEquals("", config.getEtag());

        assertEquals(utcTimeDefault, config.getCreatedTimeUtc());
        assertEquals(utcTimeDefault, config.getLastUpdatedTimeUtc());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_003: [The Configuration class shall have the following properties: id, schemaVersion,
    // labels, content, targetCondition, createdTimeUtc, lastUpdatedTimeUtc, priority, systemMetrics, metrics, etag
    @Test(expected = IllegalArgumentException.class)
    public void config_get_all_properties()
    {
        // Arrange
        String configurationId = "xxx-configuration";

        // Act
        Configuration config = Deencapsulation.newInstance(Configuration.class, configurationId);

        // Assert
        config.getId();
        config.getSchemaVersion();
        config.getLabels();
        config.getPriority();
        config.getContent();
        config.getCreatedTimeUtc();
        config.getLastUpdatedTimeUtc();
        config.getSystemMetrics();
        config.getMetrics();
        config.getEtag();
        config.getTargetCondition();
        config.setForceUpdate(true);
        config.setForceUpdate(null);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_CONFIGURATION_28_004: [This method shall return a new instance of a ConfigurationParser
    //object that is populated using the properties of this.]
    @Test
    public void conversionToConfigurationParser()
    {
        // arrange
        String expectedConfigId = "configuration";
        String expectedSchemaVersion = "2.0";
        String expectedETag = "1234";
        String expectedLastUpdatedTimeUtc = "2001-09-09T09:09:09";
        String expectedCreatedTimeUtc = "2001-09-09T09:09:09";
        String expectedTargetCondition = "*";
        Integer expectedPriority = 100;
        HashMap<String, String> expectedLabels = new HashMap<String, String>(){{put("label1","val1");}};

        Configuration config = Deencapsulation.newInstance(Configuration.class, expectedConfigId);
        Deencapsulation.setField(config, "schemaVersion", expectedSchemaVersion);
        Deencapsulation.setField(config, "createdTimeUtc", expectedCreatedTimeUtc);
        Deencapsulation.setField(config, "lastUpdatedTimeUtc", expectedLastUpdatedTimeUtc);
        config.setEtag(expectedETag);
        config.setTargetCondition(expectedTargetCondition);
        config.setPriority(expectedPriority);
        config.setLabels(expectedLabels);

        // act
        ConfigurationParser parser = Deencapsulation.invoke(config, "toConfigurationParser");

        // assert
        assertEquals(expectedConfigId, parser.getId());
        assertEquals(ParserUtility.getDateTimeUtc(expectedCreatedTimeUtc), parser.getCreatedTimeUtc());
        assertEquals(ParserUtility.getDateTimeUtc(expectedLastUpdatedTimeUtc), parser.getLastUpdatedTimeUtc());
        assertEquals(expectedETag, parser.getETag());
        assertEquals(expectedPriority, parser.getPriority());
        assertEquals(expectedSchemaVersion, parser.getSchemaVersion());
        assertEquals(expectedTargetCondition, parser.getTargetCondition());
        assertEquals(expectedLabels, parser.getLabels());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_CONFIGURATION_28_005: [This constructor shall create a new Configuration object using the values within the provided parser.]
    @Test
    public void conversionFromConfigurationParser()
    {
        // arrange
        String expectedConfigId = "configuration";
        String expectedSchemaVersion = "2.0";
        String expectedETag = "1234";
        String expectedTargetCondition = "*";
        Integer expectedPriority = 100;
        HashMap<String, String> expectedLabels = new HashMap<String, String>()
        {{
            put("label1", "val1");
        }};

        // arrange
        ConfigurationParser parserCA = new ConfigurationParser();
        parserCA.setId(expectedConfigId);
        parserCA.setSchemaVersion(expectedSchemaVersion);
        parserCA.setETag(expectedETag);
        parserCA.setTargetCondition(expectedTargetCondition);
        parserCA.setPriority(expectedPriority);
        parserCA.setLabels(expectedLabels);
        ConfigurationMetricsParser parserMetric = new ConfigurationMetricsParser();
        parserMetric.setQueries(new HashMap<String, String>()
        {{
            put("queryKey", "queryVal");
        }});
        parserMetric.setResults(new HashMap<String, Long>()
        {{
            put("resultKey", new Long(100));
        }});
        parserCA.setMetrics(parserMetric);
        ConfigurationMetricsParser parserSystemMetric = new ConfigurationMetricsParser();
        parserSystemMetric.setQueries(new HashMap<String, String>()
        {{
            put("squeryKey", "squeryVal");
        }});
        parserSystemMetric.setResults(new HashMap<String, Long>()
        {{
            put("sresultKey", new Long(101));
        }});
        parserCA.setSystemMetrics(parserSystemMetric);

        // act
        Configuration configurationCA = Deencapsulation.newInstance(Configuration.class, new Class[]{ConfigurationParser.class}, parserCA);

        // assert
        assertEquals(expectedConfigId, configurationCA.getId());
        assertEquals(expectedSchemaVersion, configurationCA.getSchemaVersion());
        assertEquals(expectedETag, configurationCA.getEtag());
        assertEquals(expectedTargetCondition, configurationCA.getTargetCondition());
        assertEquals(expectedPriority, configurationCA.getPriority());
        assertEquals(expectedLabels, parserCA.getLabels());
        assertEquals(1, configurationCA.getMetrics().getQueries().size());
        assertEquals("queryVal", configurationCA.getMetrics().getQueries().get("queryKey"));
        assertEquals(new Long(100), configurationCA.getMetrics().getResults().get("resultKey"));
        assertEquals("squeryVal", configurationCA.getSystemMetrics().getQueries().get("squeryKey"));
        assertEquals(new Long(101), configurationCA.getSystemMetrics().getResults().get("sresultKey"));
    }

    @Test
    public void canSetConfigurationMetrics(@Mocked final ConfigurationMetrics mockedConfigurationMetrics)
    {
        Configuration configuration = new Configuration("asdf");

        configuration.setMetrics(mockedConfigurationMetrics);

        Assert.assertEquals(mockedConfigurationMetrics, configuration.getMetrics());
    }
}
