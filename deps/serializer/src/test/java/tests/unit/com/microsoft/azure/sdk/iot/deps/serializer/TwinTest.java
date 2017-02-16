// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;


import com.google.gson.JsonElement;
import com.microsoft.azure.sdk.iot.deps.serializer.*;
import mockit.Deencapsulation;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * Unit tests for Twin serializer
 */
public class TwinTest {

    private static final String BIG_STRING_150CHARS =
            "01234567890123456789012345678901234567890123456789" +
                    "01234567890123456789012345678901234567890123456789" +
                    "01234567890123456789012345678901234567890123456789";
    private static final String SPECIAL_CHAR_STRING = "value special chars !@#$%^&*()_";
    private static final String ILLEGAL_STRING_DOT = "illegal.key";
    private static final String ILLEGAL_STRING_SPACE = "illegal key";
    private static final String ILLEGAL_STRING_DOLLAR = "illegal$key";
    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'";
    private static final String TIMEZONE = "UTC";

    protected static class OnDesiredCallback implements TwinPropertiesChangeCallback
    {
        private Map<String, Object> diff = null;
        public void execute(Map<String , Object> propertyMap)
        {
            diff = propertyMap;
        }
    }

    protected static class OnReportedCallback implements TwinPropertiesChangeCallback
    {
        private Map<String, Object> diff = null;
        public void execute(Map<String , Object> propertyMap)
        {
            diff = propertyMap;
        }
    }

    private void assetWithError(String dt1Str, String dt2Str)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        Date dt1 = null;
        Date dt2 = null;

        try
        {
            dt1 = dateFormat.parse(dt1Str);
            dt2 = dateFormat.parse(dt2Str);
        }
        catch (ParseException e)
        {
            assert(true);
        }

        long error = Math.abs(dt1.getTime()-dt2.getTime());

        assertThat(error, lessThanOrEqualTo(100L));
    }

    /* Tests_SRS_TWIN_21_001: [The constructor shall create an instance of the properties.] */
    /* Tests_SRS_TWIN_21_002: [The constructor shall set OnDesiredCallback as null.] */
    /* Tests_SRS_TWIN_21_003: [The constructor shall set OnReportedCallback as null.] */
    @Test
    public void constructor_succeed()
    {
        // Arrange
        // Act
        Twin twin = new Twin();

        // Assert
        assertNotNull(twin);
    }

    /* Tests_SRS_TWIN_21_013: [The setDesiredCallback shall set OnDesiredCallback with the provided Callback function.] */
    @Test
    public void setDesiredCallback_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        Twin twin = new Twin();

        // Act
        twin.setDesiredCallback(onDesiredCallback);

        // Assert
        TwinPropertiesChangeCallback resultDesiredCallback = (TwinPropertiesChangeCallback)Deencapsulation.getField(twin, "onDesiredCallback");
        assertEquals(resultDesiredCallback, onDesiredCallback);

    }

    /* Tests_SRS_TWIN_21_053: [The setDesiredCallback shall keep only one instance of the callback.] */
    /* Tests_SRS_TWIN_21_054: [If the OnDesiredCallback is already set, the setDesiredCallback shall replace the first one.] */
    @Test
    public void setDesiredCallback_secondInstance_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback1 = new OnDesiredCallback();
        OnDesiredCallback onDesiredCallback2 = new OnDesiredCallback();
        Twin twin = new Twin();
        twin.setDesiredCallback(onDesiredCallback1);

        // Act
        twin.setDesiredCallback(onDesiredCallback2);

        // Assert
        TwinPropertiesChangeCallback resultDesiredCallback = (TwinPropertiesChangeCallback)Deencapsulation.getField(twin, "onDesiredCallback");
        assertEquals(resultDesiredCallback, onDesiredCallback2);
    }

    /* Tests_SRS_TWIN_21_055: [If callback is null, the setDesiredCallback will set the OnDesiredCallback as null.] */
    @Test
    public void setDesiredCallback_null_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        Twin twin = new Twin();
        twin.setDesiredCallback(onDesiredCallback);

        // Act
        twin.setDesiredCallback(null);

        // Assert
        TwinPropertiesChangeCallback resultDesiredCallback = (TwinPropertiesChangeCallback)Deencapsulation.getField(twin, "onDesiredCallback");
        assertNull(resultDesiredCallback);
    }

    /* Tests_SRS_TWIN_21_014: [The setReportedCallback shall set OnReportedCallback with the provided Callback function.] */
    @Test
    public void setReportedCallback_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin();

        // Act
        twin.setReportedCallback(onReportedCallback);

        // Assert
        TwinPropertiesChangeCallback resultReportedCallback = (TwinPropertiesChangeCallback)Deencapsulation.getField(twin, "onReportedCallback");
        assertEquals(resultReportedCallback, onReportedCallback);
    }

    /* Tests_SRS_TWIN_21_056: [The setReportedCallback shall keep only one instance of the callback.] */
    /* Tests_SRS_TWIN_21_057: [If the OnReportedCallback is already set, the setReportedCallback shall replace the first one.] */
    @Test
    public void setReportedCallback_secondInstance_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback1 = new OnReportedCallback();
        OnReportedCallback onReportedCallback2 = new OnReportedCallback();
        Twin twin = new Twin();
        twin.setReportedCallback(onReportedCallback1);

        // Act
        twin.setReportedCallback(onReportedCallback2);

        // Assert
        TwinPropertiesChangeCallback resultReportedCallback = (TwinPropertiesChangeCallback)Deencapsulation.getField(twin, "onReportedCallback");
        assertEquals(resultReportedCallback, onReportedCallback2);
    }

    /* Tests_SRS_TWIN_21_058: [If callback is null, the setReportedCallback will set the OnReportedCallback as null.] */
    @Test
    public void setReportedCallback_null_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin();
        twin.setReportedCallback(onReportedCallback);

        // Act
        twin.setReportedCallback(null);

        // Assert
        TwinPropertiesChangeCallback resultReportedCallback = (TwinPropertiesChangeCallback)Deencapsulation.getField(twin, "onReportedCallback");
        assertNull(resultReportedCallback);
    }

    /* Tests_SRS_TWIN_21_015: [The toJson shall create a String with information in the Twin using json format.] */
    /* Tests_SRS_TWIN_21_016: [The toJson shall not include null fields.] */
    @Test
    public void toJson_emptyClass_succeed()
    {
        // Arrange
        Twin twin = new Twin();

        // Act
        String json = twin.toJson();

        // Assert
        assertThat(json, is("{\"properties\":{\"desired\":{},\"reported\":{}}}"));
    }

    /* Tests_SRS_TWIN_21_017: [The toJsonElement shall return a JsonElement with information in the Twin using json format.] */
    /* Tests_SRS_TWIN_21_018: [The toJsonElement shall not include null fields.] */
    /* Tests_SRS_TWIN_21_086: [The toJsonElement shall include the `properties` in the json even if it has no content.] */
    /* Tests_SRS_TWIN_21_087: [The toJsonElement shall include the `desired` property in the json even if it has no content.] */
    /* Tests_SRS_TWIN_21_088: [The toJsonElement shall include the `reported` property in the json even if it has no content.] */
    @Test
    public void toJsonElement_emptyClass_succeed()
    {
        // Arrange
        Twin twin = new Twin();

        // Act
        JsonElement jsonElement = twin.toJsonElement();

        // Assert
        assertThat(jsonElement.toString(), is("{\"properties\":{\"desired\":{},\"reported\":{}}}"));
    }

    /* Tests_SRS_TWIN_21_020: [The enableMetadata shall enable report metadata in Json for the Desired and for the Reported Properties.] */
    @Test
    public void toJson_emptyClass_withMetadata_succeed()
    {
        // Arrange
        Twin twin = new Twin();

        // Act
        twin.enableMetadata();

        // Assert
        String json = twin.toJson();
        assertThat(json, is("{\"properties\":{\"desired\":{\"$metadata\":{}},\"reported\":{\"$metadata\":{}}}}"));
    }

    /* Tests_SRS_TWIN_21_021: [The updateDesiredProperty shall add all provided properties to the Desired property.] */
    /* Tests_SRS_TWIN_21_050: [The getDesiredPropertyMap shall return a map with all desired property key value pairs.] */
    @Test
    public void updateDesiredProperty_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        newValues.put("key7", false);
        newValues.put("key8", 1234.456);

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\",\"key7\":false,\"key8\":1234.456}"));
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(5));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        keydb = Double.parseDouble(result.get("key8").toString());
        assertThat(keydb, is(1234.456));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key7").toString(), is("false"));
    }

    /* Tests_SRS_TWIN_21_073: [If any `key` is null, the updateDesiredProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_nullKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(null, "value");

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_074: [If any `key` is empty, the updateDesiredProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_emptyKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put("", "value");

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_075: [If any `key` is more than 128 characters long, the updateDesiredProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_bigKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(BIG_STRING_150CHARS, "value");

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_076: [If any `key` has an illegal character (`$`,`.`, space), the updateDesiredProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_illegalSpaceKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_SPACE, "value");

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_076: [If any `key` has an illegal character (`$`,`.`, space), the updateDesiredProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_illegalDotKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOT, "value");

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_076: [If any `key` has an illegal character (`$`,`.`, space), the updateDesiredProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_illegalDollarKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOLLAR, "value");

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_078: [If any `value` is null, the updateDesiredProperty shall store it but do not report on Json.] */
    @Test
    public void updateDesiredProperty_nullValues_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", null);
        newValues.put("key2", null);

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(2));
        assertNull(result.get("key1"));
        assertNull(result.get("key2"));
    }

    /* Tests_SRS_TWIN_21_079: [If any `key` is null, the updateReportedProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_nullKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(null, "value");

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_080: [If any `key` is empty, the updateReportedProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_emptyKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put("", "value");

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_081: [If any `key` is more than 128 characters long, the updateReportedProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_bigKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(BIG_STRING_150CHARS, "value");

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_082: [If any `key` has an illegal character (`$`,`.`, space), the updateReportedProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_IllegalSpaceKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_SPACE, "value");

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_082: [If any `key` has an illegal character (`$`,`.`, space), the updateReportedProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_IllegalDotKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOT, "value");

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_082: [If any `key` has an illegal character (`$`,`.`, space), the updateReportedProperty shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_IllegalDollarKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOLLAR, "value");

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWIN_21_084: [If any `value` is null, the updateReportedProperty shall store it but do not report on Json.] */
    @Test
    public void updateReportedProperty_nullValues_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", null);
        newValues.put("key2", null);

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(2));
        assertNull(result.get("key1"));
        assertNull(result.get("key2"));
    }

    /* Tests_SRS_TWIN_21_021: [The updateDesiredProperty shall add all provided properties to the Desired property.] */
    @Test
    public void updateDesiredProperty_withMetadata_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        twin.enableMetadata();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");

        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            // Don't do anything
        }

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        Twin resultJson = new Twin();
        resultJson.updateDesiredProperty(json);
        TwinProperties resultProperties = Deencapsulation.getField(twin, "properties");
        TwinProperty resultDesired = Deencapsulation.getField(resultProperties, "desired");
        TwinMetadata resultMetadataKey1 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key1");
        TwinMetadata resultMetadataKey2 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key2");
        TwinMetadata resultMetadataKey3 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key3");
        assertThat((Integer)Deencapsulation.invoke(resultDesired, "size"), is(3));
        assertThat(Deencapsulation.invoke(resultDesired, "get", "key1").toString(), is("value1"));
        assertThat((Integer)Deencapsulation.invoke(resultDesired, "get", "key2"), is(1234));
        assertThat(Deencapsulation.invoke(resultDesired, "get", "key3").toString(), is("value3"));

        TwinProperties properties = Deencapsulation.getField(twin, "properties");
        TwinProperty originJson = Deencapsulation.getField(properties, "desired");
        TwinMetadata originMetadataKey1 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key1");
        TwinMetadata originMetadataKey2 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key2");
        TwinMetadata originMetadataKey3 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key3");
        assertThat(Deencapsulation.invoke(resultMetadataKey1, "getLastUpdateVersion"),
                is(Deencapsulation.invoke(originMetadataKey1, "getLastUpdateVersion")));
        assertThat(Deencapsulation.invoke(resultMetadataKey2, "getLastUpdateVersion"),
                is(Deencapsulation.invoke(originMetadataKey2, "getLastUpdateVersion")));
        assertThat(Deencapsulation.invoke(resultMetadataKey3, "getLastUpdateVersion"),
                is(Deencapsulation.invoke(originMetadataKey3, "getLastUpdateVersion")));
        assertThat(Deencapsulation.invoke(resultMetadataKey1, "getLastUpdate"),
                is(Deencapsulation.invoke(originMetadataKey1, "getLastUpdate")));
        assertThat(Deencapsulation.invoke(resultMetadataKey2, "getLastUpdate"),
                is(Deencapsulation.invoke(originMetadataKey2, "getLastUpdate")));
        assertThat(Deencapsulation.invoke(resultMetadataKey3, "getLastUpdate"),
                is(Deencapsulation.invoke(originMetadataKey3, "getLastUpdate")));

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredProperty_OnlyMetadataChanges_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        twin.enableMetadata();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);

        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            //Don't do anything
        }

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        Twin resultJson = new Twin();
        resultJson.updateDesiredProperty(json);
        TwinProperties resultProperties = Deencapsulation.getField(twin, "properties");
        TwinProperty resultDesired = Deencapsulation.getField(resultProperties, "desired");
        TwinMetadata resultMetadataKey1 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key1");
        TwinMetadata resultMetadataKey2 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key2");
        TwinMetadata resultMetadataKey3 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key3");
        assertThat((Integer)Deencapsulation.invoke(resultDesired, "size"), is(3));
        assertThat(Deencapsulation.invoke(resultDesired, "get", "key1").toString(), is("value1"));
        assertThat((Integer)Deencapsulation.invoke(resultDesired, "get", "key2"), is(1234));
        assertThat(Deencapsulation.invoke(resultDesired, "get", "key3").toString(), is("value3"));

        TwinProperties properties = Deencapsulation.getField(twin, "properties");
        TwinProperty originJson = Deencapsulation.getField(properties, "desired");
        TwinMetadata originMetadataKey1 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key1");
        TwinMetadata originMetadataKey2 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key2");
        TwinMetadata originMetadataKey3 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key3");
        assertThat(Deencapsulation.invoke(resultMetadataKey1, "getLastUpdateVersion"),
                is(Deencapsulation.invoke(originMetadataKey1, "getLastUpdateVersion")));
        assertThat(Deencapsulation.invoke(resultMetadataKey2, "getLastUpdateVersion"),
                is(Deencapsulation.invoke(originMetadataKey2, "getLastUpdateVersion")));
        assertThat(Deencapsulation.invoke(resultMetadataKey3, "getLastUpdateVersion"),
                is(Deencapsulation.invoke(originMetadataKey3, "getLastUpdateVersion")));
        assertThat(Deencapsulation.invoke(resultMetadataKey1, "getLastUpdate"),
                is(Deencapsulation.invoke(originMetadataKey1, "getLastUpdate")));
        assertThat(Deencapsulation.invoke(resultMetadataKey2, "getLastUpdate"),
                is(Deencapsulation.invoke(originMetadataKey2, "getLastUpdate")));
        assertThat(Deencapsulation.invoke(resultMetadataKey3, "getLastUpdate"),
                is(Deencapsulation.invoke(originMetadataKey3, "getLastUpdate")));

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredProperty_newKey_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key4", "value4");

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key4\":\"value4\"}"));
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key4").toString(), is("value4"));
    }

    /* Tests_SRS_TWIN_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    /* Tests_SRS_TWIN_21_059: [The updateDesiredProperty shall only change properties in the map, keep the others as is.] */
    /* Tests_SRS_TWIN_21_077: [If any `key` already exists, the updateDesiredProperty shall replace the existed value by the new one.] */
    @Test
    public void updateDesiredProperty_newValue_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\"}"));
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_061: [All `key` and `value` in property shall be case sensitive.] */
    @Test
    public void updateDesiredProperty_caseSensitive_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("kEy1", "value1");

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"kEy1\":\"value1\"}"));
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(result.get("kEy1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_063: [If the provided `property` map is empty, the updateDesiredProperty shall not change the database and return null.] */
    @Test
    public void updateDesiredProperty_emptyMap_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);

        newValues.clear();

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredProperty_newAndOld_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key5", "value5");

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"key5\":\"value5\"}"));
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key5").toString(), is("value5"));
    }

    /* Tests_SRS_TWIN_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredProperty_mixDesiredAndReported_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value1");
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twin.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key6", "value6");
        newValues.put("key5", "value5");

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"key5\":\"value5\"}"));
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(result.get("key5").toString(), is("value5"));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWIN_21_023: [If the provided `property` map is null, the updateDesiredProperty shall not change the database and return null.] */
    /* Tests_SRS_TWIN_21_024: [If no Desired property changed its value, the updateDesiredProperty shall return null.] */
    @Test
    public void updateDesiredProperty_emptyMap_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertNull(result);
    }

    /* Tests_SRS_TWIN_21_024: [If no Desired property changed its value, the updateDesiredProperty shall return null.] */
    @Test
    public void updateDesiredProperty_noChanges_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);

        // Act
        String json = twin.updateDesiredProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_025: [The updateReportedProperty shall add all provided properties to the Reported property.] */
    /* Tests_SRS_TWIN_21_051: [The getReportedPropertyMap shall return a map with all reported property key value pairs.] */
    @Test
    public void updateReportedProperty_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}"));
        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_026: [The updateReportedProperty shall return a string with json representing the Reported properties with changes.] */
    /* Tests_SRS_TWIN_21_060: [The updateReportedProperty shall only change properties in the map, keep the others as is.] */
    /* Tests_SRS_TWIN_21_083: [If any `key` already exists, the updateReportedProperty shall replace the existed value by the new one.] */
    @Test
    public void updateReportedProperty_newAndOld_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", 898989);
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);

        newValues.clear();
        newValues.put("key1", 7654);
        newValues.put("key2", 1234);
        newValues.put("key5", "value5");

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":7654,\"key5\":\"value5\"}"));
        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        double keydb = Double.parseDouble(result.get("key1").toString());
        assertThat(keydb, is(7654.0));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key5").toString(), is("value5"));
    }

    /* Tests_SRS_TWIN_21_062: [All `key` and `value` in property shall be case sensitive.] */
    @Test
    public void updateReportedProperty_caseSensitive_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("kEy1", "value1");

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"kEy1\":\"value1\"}"));
        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(result.get("kEy1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_064: [If the provided `property` map is empty, the updateReportedProperty shall not change the database and return null.] */
    @Test
    public void updateReportedProperty_emptyMap_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);

        newValues.clear();

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_026: [The updateReportedProperty shall return a string with json representing the Reported properties with changes.] */
    @Test
    public void updateReportedProperty_mixDesiredAndReported_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key6", "value6");
        newValues.put("key7", "value7");
        twin.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key5", "value5");

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"key5\":\"value5\"}"));
        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key5").toString(), is("value5"));
    }

    /* Tests_SRS_TWIN_21_027: [If the provided `property` map is null, the updateReportedProperty shall not change the database and return null.] */
    /* Tests_SRS_TWIN_21_028: [If no Reported property changed its value, the updateReportedProperty shall return null.] */
    @Test
    public void updateReportedProperty_emptyMap_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();

        // Act
        String json = twin.updateReportedProperty(newValues);

        // Assert
        assertNull(json);
    }


    /* Tests_SRS_TWIN_21_034: [The updateReportedProperty shall update the Reported property using the information provided in the json.] */
    /* Tests_SRS_TWIN_21_035: [The updateReportedProperty shall generate a map with all pairs key value that had its content changed.] */
    /* Tests_SRS_TWIN_21_036: [The updateReportedProperty shall send the map with all changed pairs to the upper layer calling onReportedCallback (TwinPropertiesChangeCallback).] */
    @Test
    public void updateReportedProperty_json_emptyClass_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin();
        twin.setReportedCallback(onReportedCallback);

        String json = "{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}";

        // Act
        twin.updateReportedProperty(json);

        // Assert
        assertThat(onReportedCallback.diff.size(), is(3));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(onReportedCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(onReportedCallback.diff.get("key3").toString(), is("value3"));

        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_093: [If the provided json is not valid, the updateReportedProperty shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_json_missingComma_failed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin();
        twin.setReportedCallback(onReportedCallback);

        String json = "{\"key1\":\"value1\"\"key2\":1234,\"key3\":\"value3\"}";

        // Act
        twin.updateReportedProperty(json);

        // Assert
    }

    /* Tests_SRS_TWIN_21_034: [The updateReportedProperty shall update the Reported property using the information provided in the json.] */
    /* Tests_SRS_TWIN_21_035: [The updateReportedProperty shall generate a map with all pairs key value that had its content changed.] */
    /* Tests_SRS_TWIN_21_036: [The updateReportedProperty shall send the map with all changed pairs to the upper layer calling onReportedCallback (TwinPropertiesChangeCallback).] */
    @Test
    public void updateReportedProperty_json_mixDesiredAndReported_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key6", "value6");
        newValues.put("key7", "value7");
        twin.updateDesiredProperty(newValues);

        OnReportedCallback onReportedCallback = new OnReportedCallback();
        twin.setReportedCallback(onReportedCallback);

        String json = "{\"key1\":\"value4\",\"key2\":4321,\"key5\":\"value5\"}";

        // Act
        twin.updateReportedProperty(json);

        // Assert
        assertThat(onReportedCallback.diff.size(), is(3));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value4"));
        double keydb = Double.parseDouble(onReportedCallback.diff.get("key2").toString());
        assertThat(keydb, is(4321.0));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(4321.0));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key5").toString(), is("value5"));
    }

    /* Tests_SRS_TWIN_21_037: [If the OnReportedCallback is set as null, the updateReportedProperty shall discard the map with the changed pairs.] */
    @Test
    public void updateReportedProperty_json_noCallback_emptyClass_succeed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}";

        // Act
        twin.updateReportedProperty(json);

        // Assert
        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_038: [If there is no change in the Reported property, the updateReportedProperty shall not change the database and not call the OnReportedCallback.] */
    @Test
    public void updateReportedProperty_json_noChanges_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin();
        twin.setReportedCallback(onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);

        String json = "{\"key1\":\"value1\",\"key2\":1234.0,\"key3\":\"value3\"}";

        // Act
        twin.updateReportedProperty(json);

        // Assert
        assertNull(onReportedCallback.diff);

        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Codes_SRS_TWIN_21_095: [If the provided json have any duplicated `key`, the updateReportedProperty shall throws IllegalArgumentException.] */
    @Test
    public void updateReportedProperty_json_duplicatedKey_failed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twin.updateReportedProperty(newValues);

        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        twin.setDesiredCallback(onDesiredCallback);
        twin.setReportedCallback(onReportedCallback);

        String json = "{\"key1\":\"value9\",\"key1\":\"value4\",\"key2\":4321,\"key5\":\"value5\"}";

        // Act
        try
        {
            twin.updateReportedProperty(json);
            assert(true);
        }
        catch ( IllegalArgumentException expected)
        {
            //Expected behavior, don't do anything
        }

        // Assert
        /**
         * Shall not trigger callback.
         */
        assertNull(onDesiredCallback.diff);
        assertNull(onReportedCallback.diff);

        /**
         * Shall not change any value.
         */
        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWIN_21_067: [If the provided json is empty, the updateReportedProperty shall not change the database and not call the OnReportedCallback.] */
    @Test
    public void updateReportedProperty_json_empty_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin();
        twin.setReportedCallback(onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);

        String json = "";

        // Act
        twin.updateReportedProperty(json);

        // Assert
        assertNull(onReportedCallback.diff);

        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_068: [If the provided json is null, the updateReportedProperty shall not change the database and not call the OnReportedCallback.] */
    @Test
    public void updateReportedProperty_json_null_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin();
        twin.setReportedCallback(onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);

        String json = null;

        // Act
        twin.updateReportedProperty(json);

        // Assert
        assertNull(onReportedCallback.diff);

        Map<String, Object> result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_005: [The constructor shall call the standard constructor.] */
    /* Tests_SRS_TWIN_21_007: [The constructor shall set OnReportedCallback as null.] */
    /* Tests_SRS_TWIN_21_006: [The constructor shall set OnDesiredCallback with the provided Callback function.] */
    /* Tests_SRS_TWIN_21_029: [The updateDesiredProperty shall update the Desired property using the information provided in the json.] */
    /* Tests_SRS_TWIN_21_030: [The updateDesiredProperty shall generate a map with all pairs key value that had its content changed.] */
    /* Tests_SRS_TWIN_21_031: [The updateDesiredProperty shall send the map with all changed pairs to the upper layer calling onDesiredCallback (TwinPropertiesChangeCallback).] */
    @Test
    public void updateDesiredProperty_json_emptyClass_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        Twin twin = new Twin(onDesiredCallback);
        twin.setDesiredCallback(onDesiredCallback);

        String json = "{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}";

        // Act
        twin.updateDesiredProperty(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(onDesiredCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_029: [The updateDesiredProperty shall update the Desired property using the information provided in the json.] */
    /* Tests_SRS_TWIN_21_030: [The updateDesiredProperty shall generate a map with all pairs key value that had its content changed.] */
    /* Tests_SRS_TWIN_21_031: [The updateDesiredProperty shall send the map with all changed pairs to the upper layer calling onDesiredCallback (TwinPropertiesChangeCallback).] */
    @Test
    public void updateDesiredProperty_json_mixDesiredAndProvided_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twin.updateDesiredProperty(newValues);

        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        twin.setDesiredCallback(onDesiredCallback);

        String json = "{\"key1\":\"value4\",\"key2\":4321,\"key5\":\"value5\"}";

        // Act
        twin.updateDesiredProperty(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(2));
        double keydb = Double.parseDouble(onDesiredCallback.diff.get("key2").toString());
        assertThat(keydb, is(4321.0));
        assertThat(onDesiredCallback.diff.get("key5").toString(), is("value5"));
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(5));
        assertThat(result.get("key1").toString(), is("value4"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(4321.0));
        assertThat(result.get("key5").toString(), is("value5"));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Codes_SRS_TWIN_21_096: [If the provided json have any duplicated `key`, the updateDesiredProperty shall throws IllegalArgumentException.] */
    @Test
    public void updateDesiredProperty_json_duplicatedKey_succeed()
    {
        // Arrange
        Twin twin = new Twin();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twin.updateDesiredProperty(newValues);

        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        twin.setDesiredCallback(onDesiredCallback);
        twin.setReportedCallback(onReportedCallback);

        String json = "{\"key1\":\"value9\",\"key1\":\"value4\",\"key2\":4321,\"key5\":\"value5\"}";

        // Act
        try
        {
            twin.updateDesiredProperty(json);
            assert(true);
        }
        catch ( IllegalArgumentException expected)
        {
            //Expected behavior, don't do anything
        }

        // Assert
        /**
         * Shall not trigger callback.
         */
        assertNull(onDesiredCallback.diff);
        assertNull(onReportedCallback.diff);

        /**
         * Shall not change any value.
         */
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWIN_21_032: [If the OnDesiredCallback is set as null, the updateDesiredProperty shall discard the map with the changed pairs.] */
    @Test
    public void updateDesiredProperty_json_noCallback_emptyClass_succeed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}";

        // Act
        twin.updateDesiredProperty(json);

        // Assert
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_033: [If there is no change in the Desired property, the updateDesiredProperty shall not change the database and not call the OnDesiredCallback.] */
    @Test
    public void updateDesiredProperty_json_noChanges_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        Twin twin = new Twin();
        twin.setDesiredCallback(onDesiredCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);

        String json = "{\"key1\":\"value1\",\"key2\":1234.0,\"key3\":\"value3\"}";

        // Act
        twin.updateDesiredProperty(json);

        // Assert
        assertNull(onDesiredCallback.diff);

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_092: [If the provided json is not valid, the updateDesiredProperty shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_json_missingComma_failed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        Twin twin = new Twin();
        twin.setDesiredCallback(onDesiredCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);

        String json = "{\"key1\":\"value1\"\"key2\":1234.0,\"key3\":\"value3\"}";

        // Act
        twin.updateDesiredProperty(json);

        // Assert
    }

    /* Tests_SRS_TWIN_21_065: [If the provided json is empty, the updateDesiredProperty shall not change the database and not call the OnDesiredCallback.] */
    @Test
    public void updateDesiredProperty_json_empty_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        Twin twin = new Twin();
        twin.setDesiredCallback(onDesiredCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);

        String json = "";

        // Act
        twin.updateDesiredProperty(json);

        // Assert
        assertNull(onDesiredCallback.diff);

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_066: [If the provided json is null, the updateDesiredProperty shall not change the database and not call the OnDesiredCallback.] */
    @Test
    public void updateDesiredProperty_json_null_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        Twin twin = new Twin();
        twin.setDesiredCallback(onDesiredCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);

        String json = null;

        // Act
        twin.updateDesiredProperty(json);

        // Assert
        assertNull(onDesiredCallback.diff);

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }


    /* Tests_SRS_TWIN_21_009: [The constructor shall call the standard constructor.] */
    /* Tests_SRS_TWIN_21_010: [The constructor shall set OnDesiredCallback with the provided Callback function.] */
    /* Tests_SRS_TWIN_21_011: [The constructor shall set OnReportedCallback with the provided Callback function.] */
    /* Tests_SRS_TWIN_21_039: [The updateTwin shall fill the fields the properties in the Twin class with the keys and values provided in the json string.] */
    /* Tests_SRS_TWIN_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
    /* Tests_SRS_TWIN_21_044: [If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.] */
    /* Tests_SRS_TWIN_21_045: [If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.] */
    @Test
    public void updateTwin_json_emptyClass_noMetadata_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);

        String json = "{\"properties\":{" +
                "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}}";

        // Act
        twin.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(onDesiredCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(onReportedCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.124));
        assertThat(result.get("key5").toString(), is("value5"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWIN_21_089: [If the provided json contains `desired` or `reported` in its first level, the updateTwin shall parser the json as properties only.] */
    @Test
    public void updateTwin_json_emptyClass_PropertyOnlyJson_startDesired_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);

        String json = "{\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}";

        // Act
        twin.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(onDesiredCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(onReportedCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.124));
        assertThat(result.get("key5").toString(), is("value5"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWIN_21_089: [If the provided json contains `desired` or `reported` in its first level, the updateTwin shall parser the json as properties only.] */
    @Test
    public void updateTwin_json_emptyClass_PropertyOnlyJson_startReported_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);

        String json = "{\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}," +
                "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}}";

        // Act
        twin.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(onDesiredCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(onReportedCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.124));
        assertThat(result.get("key5").toString(), is("value5"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWIN_21_090: [If the provided json is properties only and contains other tag different than `desired` or `reported`, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_emptyClass_PropertyOnlyJson_withProperties_failed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);

        String json = ("{\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}," +
                "\"properties\":{}}");

        // Act
        twin.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWIN_21_091: [If the provided json is NOT properties only and contains `desired` or `reported` in its first level, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_emptyClass_FullTwinJson_withDesired_failed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);

        String json = ("{" +
                "\"properties\":{" +
                    "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}," +
                    "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}," +
                "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}}");

        // Act
        twin.updateTwin(json);

        // Assert
    }


    /* Tests_SRS_TWIN_21_046: [If OnDesiredCallback was not provided, the updateTwin shall not do anything with the list of updated desired properties.] */
    /* Tests_SRS_TWIN_21_047: [If OnReportedCallback was not provided, the updateTwin shall not do anything with the list of updated reported properties.] */
    @Test
    public void updateTwin_json_emptyClass_noCallback_succeed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"properties\":{" +
                "\"desired\":{\"key1\":\"value1\",\"key2\":1234.0,\"key3\":\"value3\"}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}}";

        // Act
        twin.updateTwin(json);

        // Assert
        String resultJson = twin.toJson();
        assertThat(resultJson, is(json));
    }

    /* Tests_SRS_TWIN_21_069: [If there is no change in the Desired property, the updateTwin shall not change the reported database and not call the OnReportedCallback.] */
    @Test
    public void updateTwin_json_emptyClass_noChangeOnDesired_succeed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}}";

        // Act
        twin.updateTwin(json);

        // Assert
        String resultJson = twin.toJson();
        assertThat(resultJson, is(json));
    }

    /* Tests_SRS_TWIN_21_069: [If there is no change in the Desired property, the updateTwin shall not change the reported database and not call the OnReportedCallback.] */
    @Test
    public void updateTwin_json_emptyClass_noDesired_succeed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"properties\":{" +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}}";

        // Act
        twin.updateTwin(json);

        // Assert
        String resultJson = twin.toJson();
        assertThat(resultJson, is("{\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}}"));
    }

    /* Tests_SRS_TWIN_21_070: [If there is no change in the Reported property, the updateTwin shall not change the reported database and not call the OnReportedCallback.] */
    @Test
    public void updateTwin_json_emptyClass_noChangeOnReported_succeed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"properties\":{\"desired\":{\"key1\":\"value1\",\"key2\":1234.0,\"key3\":\"value3\"},\"reported\":{}}}";

        // Act
        twin.updateTwin(json);

        // Assert
        String resultJson = twin.toJson();
        assertThat(resultJson, is(json));
    }

    /* Tests_SRS_TWIN_21_071: [If the provided json is empty, the updateTwin shall not change the database and not call the OnDesiredCallback or the OnReportedCallback.] */
    @Test
    public void updateTwin_json_empty_succeed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "";

        // Act
        twin.updateTwin(json);

        // Assert
        assertNull(twin.getDesiredPropertyMap());
        assertNull(twin.getReportedPropertyMap());
    }

    /* Tests_SRS_TWIN_21_072: [If the provided json is null, the updateTwin shall not change the database and not call the OnDesiredCallback or the OnReportedCallback.] */
    @Test
    public void updateTwin_json_null_succeed()
    {
        // Arrange
        Twin twin = new Twin();

        // Act
        twin.updateTwin(null);

        // Assert
        assertNull(twin.getDesiredPropertyMap());
        assertNull(twin.getReportedPropertyMap());
    }


    /* Tests_SRS_TWIN_21_039: [The updateTwin shall fill the fields the properties in the Twin class with the keys and values provided in the json string.] */
    /* Tests_SRS_TWIN_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
    /* Tests_SRS_TWIN_21_048: [The getDesiredPropertyVersion shall return the desired property version.] */
    /* Tests_SRS_TWIN_21_049: [The getReportedPropertyVersion shall return the reported property version.] */
    @Test
    public void updateTwin_json_emptyClass_withFullMetadata_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);
        twin.enableMetadata();

        String json =
            "{" +
                "\"properties\":{"+
                    "\"desired\":{" +
                        "\"key1\":\"value1\"," +
                        "\"key2\":1234.0," +
                        "\"key3\":\"value3\"," +
                        "\"$version\":3," +
                        "\"$metadata\":{" +
                            "\"key1\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3456Z\"," +
                                "\"$lastUpdatedVersion\":3" +
                            "}," +
                            "\"key2\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3457Z\"," +
                                "\"$lastUpdatedVersion\":5" +
                            "}," +
                            "\"key3\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3458Z\"," +
                                "\"$lastUpdatedVersion\":3" +
                            "}" +
                        "}" +
                    "}," +
                    "\"reported\":{" +
                        "\"key1\":\"value1\"," +
                        "\"key2\":1234.124," +
                        "\"key5\":\"value5\"," +
                        "\"key7\":true," +
                        "\"$version\":5," +
                        "\"$metadata\":{" +
                            "\"key1\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3456Z\"," +
                                "\"$lastUpdatedVersion\":3" +
                            "}," +
                            "\"key2\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3457Z\"," +
                                "\"$lastUpdatedVersion\":5" +
                            "}," +
                            "\"key5\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3457Z\"," +
                                "\"$lastUpdatedVersion\":5" +
                            "}," +
                            "\"key7\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3458Z\"," +
                                "\"$lastUpdatedVersion\":3" +
                            "}" +
                        "}" +
                    "}" +
                "}" +
            "}";

        // Act
        twin.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(onDesiredCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(onReportedCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        String resultJson = twin.toJson();
        assertThat(resultJson, is(json));
        assertThat(twin.getReportedPropertyVersion(), is(5));
        assertThat(twin.getDesiredPropertyVersion(), is(3));
    }

    /* Tests_SRS_TWIN_21_039: [The updateTwin shall fill the fields the properties in the Twin class with the keys and values provided in the json string.] */
    /* Tests_SRS_TWIN_21_040: [The updateTwin shall not change fields that is not reported in the json string.] */
    @Test
    public void updateTwin_json_emptyClass_withMetadataNoUpdateVersion_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);
        twin.enableMetadata();

        String json =
            "{" +
                "\"properties\":{"+
                    "\"desired\":{" +
                        "\"key1\":\"value1\"," +
                        "\"key2\":1234.0," +
                        "\"key3\":\"value3\"," +
                        "\"$version\":0," +
                        "\"$metadata\":{" +
                            "\"key1\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3456Z\"" +
                            "}," +
                            "\"key2\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3457Z\"" +
                            "}," +
                            "\"key3\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3458Z\"" +
                            "}" +
                        "}" +
                    "}," +
                    "\"reported\":{" +
                        "\"key1\":\"value1\"," +
                        "\"key2\":1234.124," +
                        "\"key5\":\"value5\"," +
                        "\"key7\":true," +
                        "\"$version\":0," +
                        "\"$metadata\":{" +
                            "\"key1\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3456Z\"" +
                            "}," +
                            "\"key2\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3457Z\"" +
                            "}," +
                            "\"key5\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3457Z\"" +
                            "}," +
                            "\"key7\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3458Z\"" +
                            "}" +
                        "}" +
                    "}" +
                "}" +
            "}";

        // Act
        twin.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(onDesiredCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(onReportedCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        String resultJson = twin.toJson();
        assertThat(resultJson, is(json));
    }

    /* Tests_SRS_TWIN_21_039: [The updateTwin shall fill the fields the properties in the Twin class with the keys and values provided in the json string.] */
    /* Tests_SRS_TWIN_21_040: [The updateTwin shall not change fields that is not reported in the json string.] */
    @Test
    public void updateTwin_json_emptyClass_withFullMetadataNoVersion_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);
        twin.enableMetadata();

        String json =
            "{" +
                "\"properties\":{"+
                    "\"desired\":{" +
                        "\"key1\":\"value1\"," +
                        "\"key2\":1234.0," +
                        "\"key3\":\"value3\"," +
                        "\"$metadata\":{" +
                            "\"key1\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3456Z\"," +
                                "\"$lastUpdatedVersion\":3" +
                            "}," +
                            "\"key2\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3457Z\"," +
                                "\"$lastUpdatedVersion\":5" +
                            "}," +
                            "\"key3\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3458Z\"," +
                                "\"$lastUpdatedVersion\":3" +
                            "}" +
                        "}" +
                    "}," +
                    "\"reported\":{" +
                        "\"key1\":\"value1\"," +
                        "\"key2\":1234.124," +
                        "\"key5\":\"value5\"," +
                        "\"key7\":true," +
                        "\"$metadata\":{" +
                            "\"key1\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3456Z\"," +
                                "\"$lastUpdatedVersion\":3" +
                            "}," +
                            "\"key2\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3457Z\"," +
                                "\"$lastUpdatedVersion\":5" +
                            "}," +
                            "\"key5\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3457Z\"," +
                                "\"$lastUpdatedVersion\":5" +
                            "}," +
                            "\"key7\":{" +
                                "\"$lastUpdated\":\"2017-02-09T08:13:12.3458Z\"," +
                                "\"$lastUpdatedVersion\":3" +
                            "}" +
                        "}" +
                    "}" +
                "}" +
            "}";

        // Act
        twin.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(onDesiredCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(onReportedCallback.diff.get("key2").toString());
        assertThat(keydb, is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        String resultJson = twin.toJson();
        assertThat(resultJson, is(json));
    }

    /* Tests_SRS_TWIN_21_040: [The updateTwin shall not change fields that is not reported in the json string.] */
    /* Tests_SRS_TWIN_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
    /* Tests_SRS_TWIN_21_044: [If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.] */
    /* Tests_SRS_TWIN_21_045: [If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.] */
    @Test
    public void updateTwin_json_changeOneField_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twin.updateDesiredProperty(newValues);

        String json = "{\"properties\":{" +
                "\"desired\":{\"key2\":9875}," +
                "\"reported\":{\"key1\":\"value4\"}}}";

        // Act
        twin.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(1));
        double keydb = Double.parseDouble(onDesiredCallback.diff.get("key2").toString());
        assertThat(keydb, is(9875.0));

        assertThat(onReportedCallback.diff.size(), is(1));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value4"));

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(9875.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_097: [If the provided json have any duplicated `properties`, the updateTwin shall throw IllegalArgumentException.] */
    @Test
    public void updateTwin_json_duplicatedProperties_failed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twin.updateDesiredProperty(newValues);

        String json =
            "{" +
                "\"properties\":{" +
                    "\"desired\":{\"key2\":9875}," +
                    "\"reported\":{\"key1\":\"value4\"}" +
                "}," +
                "\"properties\":{" +
                    "\"desired\":{\"key1\":\"value5\"}" +
                "}" +
            "}";

        // Act
        try {
            twin.updateTwin(json);
            assert (true);  //Shall throw IllegalArgumentException.
        }
        catch (IllegalArgumentException expected) { }

        // Assert
        /**
         * Shall not trigger callback.
         */
        assertNull(onDesiredCallback.diff);
        assertNull(onReportedCallback.diff);

        /**
         * Shall not change any value.
         */
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_098: [If the provided json is properties only and contains duplicated `desired` or `reported`, the updateTwin shall throws IllegalArgumentException.] */
    @Test
    public void updateTwin_json_duplicatedDesiredFirsLevel_failed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twin.updateDesiredProperty(newValues);

        String json =
                "{" +
                    "\"desired\":{\"key2\":9875}," +
                    "\"desired\":{\"key1\":\"value1\"}," +
                    "\"reported\":{\"key1\":\"value4\"}" +
                "}";

        // Act
        try
        {
            twin.updateTwin(json);
            assert(true);
        }
        catch ( IllegalArgumentException expected)
        {
            //Expected behavior, don't do anything
        }

        // Assert
        /**
         * Shall not trigger callback.
         */
        assertNull(onDesiredCallback.diff);
        assertNull(onReportedCallback.diff);

        /**
         * Shall not change any value.
         */
        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_094: [If the provided json have any duplicated `key`, the updateTwin shall use the content of the last one in the String.] */
    @Test
    public void updateTwin_json_duplicatedProperty_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twin.updateDesiredProperty(newValues);

        String json =
                "{" +
                    "\"properties\":{" +
                        "\"desired\":{\"key2\":9875}," +
                        "\"desired\":{\"key1\":\"value1\"}," +
                        "\"reported\":{\"key1\":\"value4\"}" +
                    "}" +
                "}";

        // Act
        twin.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(1));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));

        assertThat(onReportedCallback.diff.size(), is(1));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value4"));

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value1"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_094: [If the provided json have any duplicated `key`, the updateTwin shall use the content of the last one in the String.] */
    @Test
    public void updateTwin_json_duplicateDesiredKey_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twin.updateDesiredProperty(newValues);

        String json = "{\"properties\":{" +
                "\"desired\":{\"key2\":8,\"key2\":9875}," +
                "\"reported\":{\"key1\":\"value4\"}}}";

        // Act
        twin.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(1));
        double keydb = Double.parseDouble(onDesiredCallback.diff.get("key2").toString());
        assertThat(keydb, is(9875.0));

        assertThat(onReportedCallback.diff.size(), is(1));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value4"));

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(9875.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWIN_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
    /* Tests_SRS_TWIN_21_042: [If a valid key has a null value, the updateTwin shall delete this property.] */
    /* Tests_SRS_TWIN_21_044: [If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.] */
    /* Tests_SRS_TWIN_21_045: [If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.] */
    @Test
    public void updateTwin_json_deleteField_noMetadata_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        Twin twin = new Twin(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twin.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twin.updateReportedProperty(newValues);

        String json = "{\"properties\":{" +
                "\"desired\":{\"key3\":null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twin.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(2));
        assertNull(onDesiredCallback.diff.get("key3"));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value4"));

        assertThat(onReportedCallback.diff.size(), is(2));
        assertNull(onReportedCallback.diff.get("key1"));
        assertNull(onReportedCallback.diff.get("key7"));

        Map<String, Object> result = twin.getDesiredPropertyMap();
        assertThat(result.size(), is(2));
        assertThat(result.get("key1").toString(), is("value4"));
        double keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));

        result = twin.getReportedPropertyMap();
        assertThat(result.size(), is(2));
        keydb = Double.parseDouble(result.get("key2").toString());
        assertThat(keydb, is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
    }

    /* Tests_SRS_TWIN_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_missing_comma_failed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"properties\":{" +
                "\"desired\":{\"key3\":null,\"key1\":\"value4\"}" +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twin.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWIN_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_badProperties_failed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"property\":{" +
                "\"desired\":{\"key3\":null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twin.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWIN_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_unknownProperty_failed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"property\":{" +
                "\"barProperty\":{\"key3\":null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twin.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWIN_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_InvalidKey_failed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"properties\":{" +
                "\"desired\":{\"\":null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twin.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWIN_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_noKey_failed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"properties\":{" +
                "\"desired\":{:null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twin.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWIN_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_InvalidValue_failed()
    {
        // Arrange
        Twin twin = new Twin();

        String json = "{\"properties\":{" +
                "\"desired\":{\"Key3\":,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twin.updateTwin(json);

        // Assert
    }
}
