package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.ConfigurationContent;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class ConfigurationContentTest
{
    // Tests_SRS_SERVICE_SDK_JAVA_CONFIGURATION_METRICS_28_001: [The constructor shall initialize results and queries fields.]
    @Test
    public void constructor_initialize()
    {
        // Act
        ConfigurationContent cm = new ConfigurationContent();

        // Assert
        Assert.assertNotNull(cm);
        Assert.assertNotNull(cm.getModulesContent());
        Assert.assertNotNull(cm.getDeviceContent());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_CONFIGURATION_CONTENT_28_002: [The ConfigurationContent class shall have the following properties: modulesContent and deviceContent
    @Test
    public void getterAndSetter()
    {
        //arrange
        HashMap<String, HashMap<String, Object>> mc = new HashMap<String, HashMap<String, Object>>(){{put("mproperty",
                new HashMap<String, Object>(){{put("abc", "123"); put("cde", "456");}});}};
        HashMap<String, Object> dc = new HashMap<String, Object>(){{put("dproperty",
                new HashMap<String, Integer>(){{put("c", 3);put("d", 4);}});}};

        ConfigurationContent cc= new ConfigurationContent();

        // Act
        cc.setDeviceContent(dc);
        cc.setModulesContent(mc);

        // Assert
        Assert.assertNotNull(cc);
        Map<String, Object> moduleContentMap = (cc.getModulesContent().get("mproperty"));
        assertEquals("123", moduleContentMap.get("abc"));
        assertEquals("456", moduleContentMap.get("cde"));
        Map<String, Object> deviceContentMap = ((Map<String,Object>)(cc.getDeviceContent().get("dproperty")));
        assertEquals(3, deviceContentMap.get("c"));
        assertEquals(4, deviceContentMap.get("d"));
    }
}
