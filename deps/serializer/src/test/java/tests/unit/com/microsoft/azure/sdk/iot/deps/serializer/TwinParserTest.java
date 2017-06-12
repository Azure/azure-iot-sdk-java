// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;


import com.google.gson.JsonElement;
import com.microsoft.azure.sdk.iot.deps.serializer.*;
import mockit.Deencapsulation;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.*;


/**
 * Unit tests for TwinParser serializer
 */
public class TwinParserTest {

    private static final String BIG_STRING_150CHARS =
            "01234567890123456789012345678901234567890123456789" +
                    "01234567890123456789012345678901234567890123456789" +
                    "01234567890123456789012345678901234567890123456789";
    private static final String ILLEGAL_STRING_DOT = "illegal.key";
    private static final String ILLEGAL_STRING_SPACE = "illegal key";
    private static final String ILLEGAL_STRING_DOLLAR = "illegal$key";

    private enum myEnum
    {
        val1,
        val2,
        val3
    }


    protected static class OnDesiredCallback implements TwinChangedCallback
    {
        private Map<String, Object> diff = null;
        public void execute(Map<String , Object> propertyMap)
        {
            diff = propertyMap;
        }
    }

    protected static class OnReportedCallback implements TwinChangedCallback
    {
        private Map<String, Object> diff = null;
        public void execute(Map<String , Object> propertyMap)
        {
            diff = propertyMap;
        }
    }

    protected static class OnTagsCallback implements TwinChangedCallback
    {
        private Map<String, Object> diff = null;
        public void execute(Map<String , Object> tagsMap)
        {
            diff = tagsMap;
        }
    }

    /* Tests_SRS_TWINPARSER_21_001: [The constructor shall create an instance of the properties.] */
    /* Tests_SRS_TWINPARSER_21_002: [The constructor shall set OnDesiredCallback as null.] */
    /* Tests_SRS_TWINPARSER_21_003: [The constructor shall set OnReportedCallback as null.] */
    /* Tests_SRS_TWINPARSER_21_004: [The constructor shall set Tags as null.] */
    @Test
    public void constructor_succeed()
    {
        // Arrange
        // Act
        TwinParser twinParser = new TwinParser();

        // Assert
        assertNotNull(twinParser);
    }

    /* Tests_SRS_TWINPARSER_21_013: [The setDesiredCallback shall set OnDesiredCallback with the provided Callback function.] */
    @Test
    public void setDesiredCallback_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.setDesiredCallback(onDesiredCallback);

        // Assert
        TwinChangedCallback resultDesiredCallback = (TwinChangedCallback)Deencapsulation.getField(twinParser, "onDesiredCallback");
        assertEquals(resultDesiredCallback, onDesiredCallback);

    }

    /* Tests_SRS_TWINPARSER_21_053: [The setDesiredCallback shall keep only one instance of the callback.] */
    /* Tests_SRS_TWINPARSER_21_054: [If the OnDesiredCallback is already set, the setDesiredCallback shall replace the first one.] */
    @Test
    public void setDesiredCallback_secondInstance_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback1 = new OnDesiredCallback();
        OnDesiredCallback onDesiredCallback2 = new OnDesiredCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setDesiredCallback(onDesiredCallback1);

        // Act
        twinParser.setDesiredCallback(onDesiredCallback2);

        // Assert
        TwinChangedCallback resultDesiredCallback = (TwinChangedCallback)Deencapsulation.getField(twinParser, "onDesiredCallback");
        assertEquals(resultDesiredCallback, onDesiredCallback2);
    }

    /* Tests_SRS_TWINPARSER_21_055: [If callback is null, the setDesiredCallback will set the OnDesiredCallback as null.] */
    @Test
    public void setDesiredCallback_null_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setDesiredCallback(onDesiredCallback);

        // Act
        twinParser.setDesiredCallback(null);

        // Assert
        TwinChangedCallback resultDesiredCallback = (TwinChangedCallback)Deencapsulation.getField(twinParser, "onDesiredCallback");
        assertNull(resultDesiredCallback);
    }

    /* Tests_SRS_TWINPARSER_21_014: [The setReportedCallback shall set OnReportedCallback with the provided Callback function.] */
    @Test
    public void setReportedCallback_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.setReportedCallback(onReportedCallback);

        // Assert
        TwinChangedCallback resultReportedCallback = (TwinChangedCallback)Deencapsulation.getField(twinParser, "onReportedCallback");
        assertEquals(resultReportedCallback, onReportedCallback);
    }

    /* Tests_SRS_TWINPARSER_21_056: [The setReportedCallback shall keep only one instance of the callback.] */
    /* Tests_SRS_TWINPARSER_21_057: [If the OnReportedCallback is already set, the setReportedCallback shall replace the first one.] */
    @Test
    public void setReportedCallback_secondInstance_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback1 = new OnReportedCallback();
        OnReportedCallback onReportedCallback2 = new OnReportedCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setReportedCallback(onReportedCallback1);

        // Act
        twinParser.setReportedCallback(onReportedCallback2);

        // Assert
        TwinChangedCallback resultReportedCallback = (TwinChangedCallback)Deencapsulation.getField(twinParser, "onReportedCallback");
        assertEquals(resultReportedCallback, onReportedCallback2);
    }

    /* Tests_SRS_TWINPARSER_21_058: [If callback is null, the setReportedCallback will set the OnReportedCallback as null.] */
    @Test
    public void setReportedCallback_null_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setReportedCallback(onReportedCallback);

        // Act
        twinParser.setReportedCallback(null);

        // Assert
        TwinChangedCallback resultReportedCallback = (TwinChangedCallback)Deencapsulation.getField(twinParser, "onReportedCallback");
        assertNull(resultReportedCallback);
    }


    /* Tests_SRS_TWINPARSER_21_099: [The setTagsCallback shall set onTagsCallback with the provided callback function.] */
    @Test
    public void setTagsCallback_succeed()
    {
        // Arrange
        OnTagsCallback onTagsCallback = new OnTagsCallback();
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.setTagsCallback(onTagsCallback);

        // Assert
        TwinChangedCallback resultTagsCallback = (TwinChangedCallback)Deencapsulation.getField(twinParser, "onTagsCallback");
        assertEquals(resultTagsCallback, onTagsCallback);
    }

    /* Tests_SRS_TWINPARSER_21_100: [The setTagsCallback shall keep only one instance of the callback.] */
    /* Tests_SRS_TWINPARSER_21_101: [If the onTagsCallback is already set, the setTagsCallback shall replace the first one.] */
    @Test
    public void setTagsCallback_secondInstance_succeed()
    {
        // Arrange
        OnTagsCallback onTagsCallback1 = new OnTagsCallback();
        OnTagsCallback onTagsCallback2 = new OnTagsCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setTagsCallback(onTagsCallback1);

        // Act
        twinParser.setTagsCallback(onTagsCallback2);

        // Assert
        TwinChangedCallback resultTagsCallback = (TwinChangedCallback)Deencapsulation.getField(twinParser, "onTagsCallback");
        assertEquals(resultTagsCallback, onTagsCallback2);
    }

    /* Tests_SRS_TWINPARSER_21_102: [If callback is null, the setTagsCallback will set the onTagsCallback as null.] */
    @Test
    public void setTagsCallback_null_succeed()
    {
        // Arrange
        OnTagsCallback onTagsCallback = new OnTagsCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setTagsCallback(onTagsCallback);

        // Act
        twinParser.setTagsCallback(null);

        // Assert
        TwinChangedCallback resultTagsCallback = (TwinChangedCallback)Deencapsulation.getField(twinParser, "onTagsCallback");
        assertNull(resultTagsCallback);
    }

    /* Tests_SRS_TWINPARSER_21_015: [The toJson shall create a String with information in the TwinParser using json format.] */
    /* Tests_SRS_TWINPARSER_21_016: [The toJson shall not include null fields.] */
    @Test
    public void toJson_emptyClass_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        String json = twinParser.toJson();

        // Assert
        assertThat(json, is("{\"properties\":{\"desired\":{},\"reported\":{}}}"));
    }

    /* Tests_SRS_TWINPARSER_21_017: [The toJsonElement shall return a JsonElement with information in the TwinParser using json format.] */
    /* Tests_SRS_TWINPARSER_21_018: [The toJsonElement shall not include null fields.] */
    /* Tests_SRS_TWINPARSER_21_086: [The toJsonElement shall include the `properties` in the json even if it has no content.] */
    /* Tests_SRS_TWINPARSER_21_087: [The toJsonElement shall include the `desired` property in the json even if it has no content.] */
    /* Tests_SRS_TWINPARSER_21_088: [The toJsonElement shall include the `reported` property in the json even if it has no content.] */
    @Test
    public void toJsonElement_emptyClass_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        JsonElement jsonElement = twinParser.toJsonElement();

        // Assert
        assertThat(jsonElement.toString(), is("{\"properties\":{\"desired\":{},\"reported\":{}}}"));
    }

    /* Tests_SRS_TWINPARSER_21_019: [The enableTags shall enable tags in the twin collection.] */
    /* Tests_SRS_TWINPARSER_21_085: [If `tags` is enable, the toJsonElement shall include the tags in the json even if it has no content.] */
    @Test
    public void toJson_emptyClass_withTags_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.enableTags();

        // Assert
        String json = twinParser.toJson();
        assertThat(json, is("{\"tags\":{},\"properties\":{\"desired\":{},\"reported\":{}}}"));
    }

    /* Tests_SRS_TWINPARSER_21_161: [It tags is already enabled, the enableTags shall not do anything.] */
    @Test
    public void toJson_emptyClass_callTwice_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();

        // Act
        twinParser.enableTags();

        // Assert
        String json = twinParser.toJson();
        assertThat(json, is("{\"tags\":{},\"properties\":{\"desired\":{},\"reported\":{}}}"));
    }

    /* Tests_SRS_TWINPARSER_21_020: [The enableMetadata shall enable report metadata in Json for the Desired and for the Reported Properties.] */
    @Test
    public void toJson_emptyClass_withMetadata_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.enableMetadata();

        // Assert
        String json = twinParser.toJson();
        assertThat(json, is("{\"properties\":{\"desired\":{\"$metadata\":{}},\"reported\":{\"$metadata\":{}}}}"));
    }

    /* Tests_SRS_TWINPARSER_21_021: [The updateDesiredProperty shall add all provided properties to the Desired property.] */
    /* Tests_SRS_TWINPARSER_21_050: [The getDesiredPropertyMap shall return a map with all desired property key value pairs.] */
    /* Tests_SRS_TWINPARSER_21_156: [A valid `value` shall contains types of boolean, number, string, or object.] */
    @Test
    public void updateDesiredProperty_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("keyString", "value3");
        newValues.put("keyBool", false);
        newValues.put("keyDouble", 1234.456);
        newValues.put("keyChar", 'c');
        newValues.put("keyEnum", myEnum.val1);

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value1\",\"key2\":1234,\"keyBool\":false,\"keyChar\":\"c\",\"keyString\":\"value3\",\"keyEnum\":\"val1\",\"keyDouble\":1234.456}"));
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(7));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("keyString").toString(), is("value3"));
        assertThat(Boolean.parseBoolean(result.get("keyBool").toString()), is(false));
        assertThat(Double.parseDouble(result.get("keyDouble").toString()), is(1234.456));
        assertThat(result.get("keyChar").toString(), is("c"));
        assertThat(myEnum.valueOf(result.get("keyEnum").toString()), is(myEnum.val1));
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_152: [A valid `key` shall not be null.] */
    @Test
    public void updateDesiredProperty_nullKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        newValues.put("key7", false);
        newValues.put("key8", 1234.456);
        twinParser.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("validKey", "value");
        newValues.put("key1", "value4");
        newValues.put(null, "value");
        newValues.put("key2", 978);

        // Act
        try
        {
            twinParser.updateDesiredProperty(newValues);
            assert(true);
        }
        catch (IllegalArgumentException expected)
        {
            // Expected throw IllegalArgumentException.
        }

        // Assert
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(5));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(Double.parseDouble(result.get("key8").toString()), is(1234.456));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key7").toString(), is("false"));
        assertNull(result.get("validKey"));
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_153: [A valid `key` shall not be empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_emptyKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put("", "value");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_154: [A valid `key` shall be less than 128 characters long.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_bigKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(BIG_STRING_150CHARS, "value");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_illegalSpaceKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_SPACE, "value");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_illegalDotKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOT, "value");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_illegalDollarKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOLLAR, "value");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_156: [A valid `value` shall contains types of boolean, number, string, or object.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_illegalValueType_failed()
    {
        // Arrange
        class Bar
        {
            int intFoo = 10;
            String strFoo;
        }

        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value");
        newValues.put("key2", new Bar());

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_158: [A valid `value` shall contains less than 5 levels of sub-maps.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_6levels_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value");
        newValues.put("one",
                new HashMap<String, Object>(){{ put("two",
                        new HashMap<String, Object>(){{ put("three",
                                new HashMap<String, Object>(){{ put("four",
                                        new HashMap<String, Object>(){{ put("five",
                                                new HashMap<String, Object>(){{ put("six",
                                                        new HashMap<String, Object>(){{ put("propertyKey", "value");
                                                }});
                                        }});
                                }});
                        }});
                }});
        }});

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_157: [A valid `value` can contains sub-maps.] */
    /* Tests_SRS_TWINPARSER_21_158: [A valid `value` shall contains less than 5 levels of sub-maps.] */
    @Test
    public void updateDesiredProperty_5levels_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value");
        newValues.put("one",
                new HashMap<String, Object>(){{ put("two",
                        new HashMap<String, Object>(){{ put("three",
                                new HashMap<String, Object>(){{ put("four",
                                        new HashMap<String, Object>(){{ put("five",
                                                new HashMap<String, Object>(){{ put("propertyKey", "value");
                                        }});
                                }});
                        }});
                }});
        }});

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert

        // TODO:Implement test for property with multiple level.
    }

    /* Tests_SRS_TWINPARSER_21_078: [If any `value` is null, the updateDesiredProperty shall store it but do not report on Json.] */
    @Test
    public void updateDesiredProperty_nullValues_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", null);
        newValues.put("key2", null);

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(2));
        assertNull(result.get("key1"));
        assertNull(result.get("key2"));
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_152: [A valid `key` shall not be null.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_nullKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(null, "value");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_153: [A valid `key` shall not be empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_emptyKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put("", "value");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_154: [A valid `key` shall be less than 128 characters long.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_bigKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(BIG_STRING_150CHARS, "value");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_IllegalSpaceKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_SPACE, "value");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_IllegalDotKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOT, "value");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_IllegalDollarKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOLLAR, "value");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_084: [If any `value` is null, the updateReportedProperty shall store it but do not report on Json.] */
    @Test
    public void updateReportedProperty_nullValues_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", null);
        newValues.put("key2", null);

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(2));
        assertNull(result.get("key1"));
        assertNull(result.get("key2"));
    }

    /* Tests_SRS_TWINPARSER_21_021: [The updateDesiredProperty shall add all provided properties to the Desired property.] */
    @Test
    public void updateDesiredProperty_withMetadata_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableMetadata();
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
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        TwinParser resultJson = new TwinParser();
        resultJson.updateDesiredProperty(json);
        TwinProperties resultProperties = Deencapsulation.getField(twinParser, "properties");
        TwinProperty resultDesired = Deencapsulation.getField(resultProperties, "desired");
        TwinMetadata resultMetadataKey1 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key1");
        TwinMetadata resultMetadataKey2 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key2");
        TwinMetadata resultMetadataKey3 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key3");
        assertThat((Integer)Deencapsulation.invoke(resultDesired, "size"), is(3));
        assertThat(Deencapsulation.invoke(resultDesired, "get", "key1").toString(), is("value1"));
        assertThat((Integer)Deencapsulation.invoke(resultDesired, "get", "key2"), is(1234));
        assertThat(Deencapsulation.invoke(resultDesired, "get", "key3").toString(), is("value3"));

        TwinProperties properties = Deencapsulation.getField(twinParser, "properties");
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

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredProperty_OnlyMetadataChanges_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableMetadata();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            //Don't do anything
        }

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        TwinParser resultJson = new TwinParser();
        resultJson.updateDesiredProperty(json);
        TwinProperties resultProperties = Deencapsulation.getField(twinParser, "properties");
        TwinProperty resultDesired = Deencapsulation.getField(resultProperties, "desired");
        TwinMetadata resultMetadataKey1 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key1");
        TwinMetadata resultMetadataKey2 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key2");
        TwinMetadata resultMetadataKey3 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key3");
        assertThat((Integer)Deencapsulation.invoke(resultDesired, "size"), is(3));
        assertThat(Deencapsulation.invoke(resultDesired, "get", "key1").toString(), is("value1"));
        assertThat((Integer)Deencapsulation.invoke(resultDesired, "get", "key2"), is(1234));
        assertThat(Deencapsulation.invoke(resultDesired, "get", "key3").toString(), is("value3"));

        TwinProperties properties = Deencapsulation.getField(twinParser, "properties");
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

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredProperty_newKey_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key4", "value4");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key4\":\"value4\"}"));
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key4").toString(), is("value4"));
    }

    /* Tests_SRS_TWINPARSER_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    /* Tests_SRS_TWINPARSER_21_059: [The updateDesiredProperty shall only change properties in the map, keep the others as is.] */
    /* Tests_SRS_TWINPARSER_21_077: [If any `key` already exists, the updateDesiredProperty shall replace the existed value by the new one.] */
    @Test
    public void updateDesiredProperty_newValue_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\"}"));
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_061: [All `key` and `value` in property shall be case sensitive.] */
    @Test
    public void updateDesiredProperty_caseSensitive_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("kEy1", "value1");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"kEy1\":\"value1\"}"));
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(result.get("kEy1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_063: [If the provided `property` map is empty, the updateDesiredProperty shall not change the collection and return null.] */
    @Test
    public void updateDesiredProperty_emptyMap_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        newValues.clear();

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredProperty_newAndOld_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key5", "value5");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"key5\":\"value5\"}"));
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key5").toString(), is("value5"));
    }

    /* Tests_SRS_TWINPARSER_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredProperty_mixDesiredAndReported_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value1");
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key6", "value6");
        newValues.put("key5", "value5");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"key5\":\"value5\"}"));
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(result.get("key5").toString(), is("value5"));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWINPARSER_21_023: [If the provided `property` map is null, the updateDesiredProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_nullMap_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();

        // Act
        twinParser.updateDesiredProperty((Map)null);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_024: [If no Desired property changed its value, the updateDesiredProperty shall return null.] */
    @Test
    public void updateDesiredProperty_emptyMap_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertNull(result);
    }

    /* Tests_SRS_TWINPARSER_21_024: [If no Desired property changed its value, the updateDesiredProperty shall return null.] */
    @Test
    public void updateDesiredProperty_noChanges_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_120: [The resetDesiredProperty shall cleanup the desired collection and add all provided properties to the Desired property.] */
    /* Tests_SRS_TWINPARSER_21_121: [The resetDesiredProperty shall return a string with json representing the added desired properties.] */
    @Test
    public void resetDesiredProperty_newAndOld_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key5", "value5");

        // Act
        String json = twinParser.resetDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"key2\":1234,\"key5\":\"value5\"}"));
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key5").toString(), is("value5"));
    }

    /* Tests_SRS_TWINPARSER_21_122: [If the provided `propertyMap` is null, the resetDesiredProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void resetDesiredProperty_null_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        // Act
        try
        {
            twinParser.resetDesiredProperty(null);
            assert(true);
        }
        catch ( IllegalArgumentException expected)
        {
            //Expected behavior, don't do anything
        }

        // Assert
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_123: [The `key` and `value` in property shall be case sensitive.] */
    @Test
    public void resetDesiredProperty_caseSensitive_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key1", "vAlUE1");
        newValues.put("key2", 1234);
        newValues.put("kEy1", "value5");

        // Act
        String json = twinParser.resetDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"vAlUE1\",\"key2\":1234,\"kEy1\":\"value5\"}"));
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("vAlUE1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("kEy1").toString(), is("value5"));
    }

    /* Tests_SRS_TWINPARSER_21_124: [If the provided `propertyMap` is empty, the resetDesiredProperty shall cleanup the desired collection and return `{}`.] */
    @Test
    public void resetDesiredProperty_empty_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();

        // Act
        String json = twinParser.resetDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{}"));
        assertNull(twinParser.getDesiredPropertyMap());
    }

    /* Tests_SRS_TWINPARSER_21_125: [If the map is invalid, the resetDesiredProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void resetDesiredProperty_invalidMap_failed()
    {
        // Arrange
        class Bar
        {
            public int intFoo = 10;
            public String strFoo;
        }

        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key5", new Bar());

        // Act
        try
        {
            String json = twinParser.resetDesiredProperty(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_129: [If any `value` is null, the resetDesiredProperty shall store it but do not report on Json.] */
    @Test
    public void resetDesiredProperty_valueNull_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", null);
        newValues.put("key5", null);

        // Act
        String json = twinParser.resetDesiredProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\"}"));
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        assertNull(result.get("key2"));
        assertNull(result.get("key5"));
    }

    /* Tests_SRS_TWINPARSER_21_130: [The resetReportedProperty shall cleanup the reported collection and add all provided properties to the Reported property.] */
    /* Tests_SRS_TWINPARSER_21_131: [The resetReportedProperty shall return a string with json representing the added reported properties.] */
    @Test
    public void resetReportedProperty_newAndOld_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key5", "value5");

        // Act
        String json = twinParser.resetReportedProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"key2\":1234,\"key5\":\"value5\"}"));
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key5").toString(), is("value5"));
    }

    /* Tests_SRS_TWINPARSER_21_132: [If the provided `propertyMap` is null, the resetReportedProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void resetReportedProperty_null_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);

        // Act
        try
        {
            twinParser.resetReportedProperty(null);
            assert(true);
        }
        catch ( IllegalArgumentException expected)
        {
            //Expected behavior, don't do anything
        }

        // Assert
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_133: [The `key` and `value` in property shall be case sensitive.] */
    @Test
    public void resetReportedProperty_caseSensitive_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);

        newValues.clear();
        newValues.put("key1", "vAlUE1");
        newValues.put("key2", 1234);
        newValues.put("kEy1", "value5");

        // Act
        String json = twinParser.resetReportedProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"vAlUE1\",\"key2\":1234,\"kEy1\":\"value5\"}"));
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("vAlUE1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("kEy1").toString(), is("value5"));
    }

    /* Tests_SRS_TWINPARSER_21_134: [If the provided `propertyMap` is empty, the resetReportedProperty shall cleanup the reported collection and return `{}`.] */
    @Test
    public void resetReportedProperty_empty_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();

        // Act
        String json = twinParser.resetReportedProperty(newValues);

        // Assert
        assertThat(json, is("{}"));
        assertNull(twinParser.getReportedPropertyMap());
    }

    /* Tests_SRS_TWINPARSER_21_135: [If the map is invalid, the resetReportedProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void resetReportedProperty_invalidMap_failed()
    {
        // Arrange
        class Bar
        {
            int intFoo = 10;
            String strFoo;
        }

        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key5", new Bar());

        // Act
        try
        {
            String json = twinParser.resetReportedProperty(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_139: [If any `value` is null, the resetReportedProperty shall store it but do not report on Json.] */
    @Test
    public void resetReportedProperty_valueNull_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", null);
        newValues.put("key5", null);

        // Act
        String json = twinParser.resetReportedProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\"}"));
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        assertNull(result.get("key2"));
        assertNull(result.get("key5"));
    }

    /* Tests_SRS_TWINPARSER_21_025: [The updateReportedProperty shall add all provided properties to the Reported property.] */
    /* Tests_SRS_TWINPARSER_21_051: [The getReportedPropertyMap shall return a map with all reported property key value pairs.] */
    @Test
    public void updateReportedProperty_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}"));
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_026: [The updateReportedProperty shall return a string with json representing the Reported properties with changes.] */
    /* Tests_SRS_TWINPARSER_21_060: [The updateReportedProperty shall only change properties in the map, keep the others as is.] */
    /* Tests_SRS_TWINPARSER_21_083: [If any `key` already exists, the updateReportedProperty shall replace the existed value by the new one.] */
    @Test
    public void updateReportedProperty_newAndOld_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", 898989);
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);

        newValues.clear();
        newValues.put("key1", 7654);
        newValues.put("key2", 1234);
        newValues.put("key5", "value5");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":7654,\"key5\":\"value5\"}"));
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(Double.parseDouble(result.get("key1").toString()), is(7654.0));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key5").toString(), is("value5"));
    }

    /* Tests_SRS_TWINPARSER_21_062: [All `key` and `value` in property shall be case sensitive.] */
    @Test
    public void updateReportedProperty_caseSensitive_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);

        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("kEy1", "value1");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"kEy1\":\"value1\"}"));
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(result.get("kEy1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_064: [If the provided `property` map is empty, the updateReportedProperty shall not change the collection and return null.] */
    @Test
    public void updateReportedProperty_emptyMap_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);

        newValues.clear();

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_026: [The updateReportedProperty shall return a string with json representing the Reported properties with changes.] */
    @Test
    public void updateReportedProperty_mixDesiredAndReported_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key6", "value6");
        newValues.put("key7", "value7");
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key5", "value5");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        assertThat(json, is("{\"key1\":\"value4\",\"key5\":\"value5\"}"));
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key5").toString(), is("value5"));
    }

    /* Tests_SRS_TWINPARSER_21_027: [If the provided `property` map is null, the updateReportedProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_nullMap_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.updateReportedProperty((Map)null);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_028: [If no Reported property changed its value, the updateReportedProperty shall return null.] */
    @Test
    public void updateReportedProperty_emptyMap_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        assertNull(json);
    }


    /* Tests_SRS_TWINPARSER_21_034: [The updateReportedProperty shall update the Reported property using the information provided in the json.] */
    /* Tests_SRS_TWINPARSER_21_035: [The updateReportedProperty shall generate a map with all pairs key value that had its content changed.] */
    /* Tests_SRS_TWINPARSER_21_036: [The updateReportedProperty shall send the map with all changed pairs to the upper layer calling onReportedCallback (TwinChangedCallback).] */
    @Test
    public void updateReportedProperty_json_emptyClass_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setReportedCallback(onReportedCallback);

        String json = "{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}";

        // Act
        twinParser.updateReportedProperty(json);

        // Assert
        assertThat(onReportedCallback.diff.size(), is(3));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onReportedCallback.diff.get("key2").toString()), is(1234.0));
        assertThat(onReportedCallback.diff.get("key3").toString(), is("value3"));

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_093: [If the provided json is not valid, the updateReportedProperty shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedProperty_json_missingComma_failed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setReportedCallback(onReportedCallback);

        String json = "{\"key1\":\"value1\"\"key2\":1234,\"key3\":\"value3\"}";

        // Act
        twinParser.updateReportedProperty(json);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_034: [The updateReportedProperty shall update the Reported property using the information provided in the json.] */
    /* Tests_SRS_TWINPARSER_21_035: [The updateReportedProperty shall generate a map with all pairs key value that had its content changed.] */
    /* Tests_SRS_TWINPARSER_21_036: [The updateReportedProperty shall send the map with all changed pairs to the upper layer calling onReportedCallback (TwinChangedCallback).] */
    @Test
    public void updateReportedProperty_json_mixDesiredAndReported_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key6", "value6");
        newValues.put("key7", "value7");
        twinParser.updateDesiredProperty(newValues);

        OnReportedCallback onReportedCallback = new OnReportedCallback();
        twinParser.setReportedCallback(onReportedCallback);

        String json = "{\"key1\":\"value4\",\"key2\":4321,\"key5\":\"value5\"}";

        // Act
        twinParser.updateReportedProperty(json);

        // Assert
        assertThat(onReportedCallback.diff.size(), is(3));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(onReportedCallback.diff.get("key2").toString()), is(4321.0));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(4321.0));
        assertThat(result.get("key3").toString(), is("value3"));
        assertThat(result.get("key5").toString(), is("value5"));
    }

    /* Tests_SRS_TWINPARSER_21_037: [If the OnReportedCallback is set as null, the updateReportedProperty shall discard the map with the changed pairs.] */
    @Test
    public void updateReportedProperty_json_noCallback_emptyClass_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}";

        // Act
        twinParser.updateReportedProperty(json);

        // Assert
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_038: [If there is no change in the Reported property, the updateReportedProperty shall not change the collection and not call the OnReportedCallback.] */
    @Test
    public void updateReportedProperty_json_noChanges_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setReportedCallback(onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);

        String json = "{\"key1\":\"value1\",\"key2\":1234.0,\"key3\":\"value3\"}";

        // Act
        twinParser.updateReportedProperty(json);

        // Assert
        assertNull(onReportedCallback.diff);

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_095: [If the provided json have any duplicated `key`, the updateReportedProperty shall throws IllegalArgumentException.] */
    @Test
    public void updateReportedProperty_json_duplicatedKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twinParser.updateReportedProperty(newValues);

        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        twinParser.setDesiredCallback(onDesiredCallback);
        twinParser.setReportedCallback(onReportedCallback);

        String json = "{\"key1\":\"value9\",\"key1\":\"value4\",\"key2\":4321,\"key5\":\"value5\"}";

        // Act
        try
        {
            twinParser.updateReportedProperty(json);
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
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWINPARSER_21_067: [If the provided json is empty, the updateReportedProperty shall not change the collection and not call the OnReportedCallback.] */
    @Test
    public void updateReportedProperty_json_empty_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setReportedCallback(onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);

        String json = "";

        // Act
        twinParser.updateReportedProperty(json);

        // Assert
        assertNull(onReportedCallback.diff);

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_068: [If the provided json is null, the updateReportedProperty shall not change the collection, not call the OnReportedCallback, and throws IllegalArgumentException.] */
    @Test
    public void updateReportedProperty_json_null_succeed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setReportedCallback(onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);

        String json = null;

        // Act
        try
        {
            twinParser.updateReportedProperty(json);
            assert(true);
        }
        catch ( IllegalArgumentException expected)
        {
            //Expected behavior, don't do anything
        }

        // Assert
        assertNull(onReportedCallback.diff);

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_005: [The constructor shall call the standard constructor.] */
    /* Tests_SRS_TWINPARSER_21_007: [The constructor shall set OnReportedCallback as null.] */
    /* Tests_SRS_TWINPARSER_21_008: [The constructor shall set Tags as null.] */
    /* Tests_SRS_TWINPARSER_21_006: [The constructor shall set OnDesiredCallback with the provided Callback function.] */
    /* Tests_SRS_TWINPARSER_21_029: [The updateDesiredProperty shall update the Desired property using the information provided in the json.] */
    /* Tests_SRS_TWINPARSER_21_030: [The updateDesiredProperty shall generate a map with all pairs key value that had its content changed.] */
    /* Tests_SRS_TWINPARSER_21_031: [The updateDesiredProperty shall send the map with all changed pairs to the upper layer calling onDesiredCallback (TwinChangedCallback).] */
    @Test
    public void updateDesiredProperty_json_emptyClass_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback);
        twinParser.setDesiredCallback(onDesiredCallback);

        String json = "{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}";

        // Act
        twinParser.updateDesiredProperty(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onDesiredCallback.diff.get("key2").toString()), is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_029: [The updateDesiredProperty shall update the Desired property using the information provided in the json.] */
    /* Tests_SRS_TWINPARSER_21_030: [The updateDesiredProperty shall generate a map with all pairs key value that had its content changed.] */
    /* Tests_SRS_TWINPARSER_21_031: [The updateDesiredProperty shall send the map with all changed pairs to the upper layer calling onDesiredCallback (TwinChangedCallback).] */
    @Test
    public void updateDesiredProperty_json_mixDesiredAndProvided_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);

        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        twinParser.setDesiredCallback(onDesiredCallback);

        String json = "{\"key1\":\"value4\",\"key2\":4321,\"key5\":\"value5\"}";

        // Act
        twinParser.updateDesiredProperty(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(2));
        assertThat(Double.parseDouble(onDesiredCallback.diff.get("key2").toString()), is(4321.0));
        assertThat(onDesiredCallback.diff.get("key5").toString(), is("value5"));
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(5));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(4321.0));
        assertThat(result.get("key5").toString(), is("value5"));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWINPARSER_21_096: [If the provided json have any duplicated `key`, the updateDesiredProperty shall throws IllegalArgumentException.] */
    @Test
    public void updateDesiredProperty_json_duplicatedKey_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);

        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        twinParser.setDesiredCallback(onDesiredCallback);
        twinParser.setReportedCallback(onReportedCallback);

        String json = "{\"key1\":\"value9\",\"key1\":\"value4\",\"key2\":4321,\"key5\":\"value5\"}";

        // Act
        try
        {
            twinParser.updateDesiredProperty(json);
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
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWINPARSER_21_032: [If the OnDesiredCallback is set as null, the updateDesiredProperty shall discard the map with the changed pairs.] */
    @Test
    public void updateDesiredProperty_json_noCallback_emptyClass_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}";

        // Act
        twinParser.updateDesiredProperty(json);

        // Assert
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_033: [If there is no change in the Desired property, the updateDesiredProperty shall not change the collection and not call the OnDesiredCallback.] */
    @Test
    public void updateDesiredProperty_json_noChanges_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setDesiredCallback(onDesiredCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        String json = "{\"key1\":\"value1\",\"key2\":1234.0,\"key3\":\"value3\"}";

        // Act
        twinParser.updateDesiredProperty(json);

        // Assert
        assertNull(onDesiredCallback.diff);

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_092: [If the provided json is not valid, the updateDesiredProperty shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_json_missingComma_failed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setDesiredCallback(onDesiredCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        String json = "{\"key1\":\"value1\"\"key2\":1234.0,\"key3\":\"value3\"}";

        // Act
        twinParser.updateDesiredProperty(json);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_065: [If the provided json is empty, the updateDesiredProperty shall not change the collection and not call the OnDesiredCallback.] */
    @Test
    public void updateDesiredProperty_json_empty_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setDesiredCallback(onDesiredCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        String json = "";

        // Act
        twinParser.updateDesiredProperty(json);

        // Assert
        assertNull(onDesiredCallback.diff);

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_066: [If the provided json is null, the updateDesiredProperty shall not change the collection, not call the OnDesiredCallback, and throws IllegalArgumentException.] */
    @Test
    public void updateDesiredProperty_json_null_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setDesiredCallback(onDesiredCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234.0);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);

        String json = null;

        // Act
        try
        {
            twinParser.updateDesiredProperty(json);
            assert(true);
        }
        catch ( IllegalArgumentException expected)
        {
            //Expected behavior, don't do anything
        }

        // Assert
        assertNull(onDesiredCallback.diff);

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_159: [The updateDeviceManager shall replace the `deviceId` by the provided one.] */
    /* Tests_SRS_TWINPARSER_21_166: [The updateDeviceManager shall return a json with the new device management information.] */
    @Test
    public void updateDeviceManager_emptyClass_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        String json = twinParser.updateDeviceManager("Device name", null, null);

        // Assert
        assertThat(json, is(
                "{" +
                        "\"deviceId\":\"Device name\"," +
                        "\"properties\":{" +
                                "\"desired\":{}," +
                                "\"reported\":{}" +
                        "}" +
                "}"));

        assertThat(twinParser.getDeviceId(), is("Device name"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertNull(twinParser.getStatus());
        assertNull(twinParser.getStatusReason());
        assertNull(twinParser.getStatusUpdatedTime());
        assertNull(twinParser.getConnectionState());
        assertNull(twinParser.getConnectionStateUpdatedTime());
        assertNull(twinParser.getLastActivityTime());
    }

    /* Tests_SRS_TWINPARSER_21_166: [The updateDeviceManager shall return a json with the new device management information.] */
    @Test
    public void updateDeviceManager_changeDeviceId_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.updateDeviceManager("Device name", null, null);

        // Act
        String json = twinParser.updateDeviceManager("Device_Name", null, null);

        // Assert
        assertThat(json, is(
                "{" +
                        "\"deviceId\":\"Device_Name\"," +
                        "\"properties\":{" +
                        "\"desired\":{}," +
                        "\"reported\":{}" +
                        "}" +
                        "}"));

        assertThat(twinParser.getDeviceId(), is("Device_Name"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertNull(twinParser.getStatus());
        assertNull(twinParser.getStatusReason());
        assertNull(twinParser.getStatusUpdatedTime());
        assertNull(twinParser.getConnectionState());
        assertNull(twinParser.getConnectionStateUpdatedTime());
        assertNull(twinParser.getLastActivityTime());
    }

    /* Tests_SRS_TWINPARSER_21_167: [If nothing change in the management collection, The updateDeviceManager shall return null.] */
    @Test
    public void updateDeviceManager_noChanges_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.updateDeviceManager("Device name", null, null);

        // Act
        String json = twinParser.updateDeviceManager("Device name", null, null);

        // Assert
        assertNull(json);

        assertThat(twinParser.getDeviceId(), is("Device name"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertNull(twinParser.getStatus());
        assertNull(twinParser.getStatusReason());
        assertNull(twinParser.getStatusUpdatedTime());
        assertNull(twinParser.getConnectionState());
        assertNull(twinParser.getConnectionStateUpdatedTime());
        assertNull(twinParser.getLastActivityTime());
    }

    /* Tests_SRS_TWINPARSER_21_162: [The updateDeviceManager shall replace the `status` by the provided one.] */
    /* Tests_SRS_TWINPARSER_21_163: [If the provided `status` is different than the previous one, The updateDeviceManager shall replace the `statusReason` by the provided one.] */
    /* Tests_SRS_TWINPARSER_21_164: [If the provided `status` is different than the previous one, The updateDeviceManager shall set the `statusUpdatedTime` with the current date and time.] */
    @Test
    public void updateDeviceManager_newStatus_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        String json = twinParser.updateDeviceManager("Device name", TwinStatus.disabled, "starting system");

        // Assert
        assertThat(twinParser.getDeviceId(), is("Device name"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertThat(twinParser.getStatus(), is(TwinStatus.disabled));
        assertThat(twinParser.getStatusReason(), is("starting system"));
        assertNull(twinParser.getStatusUpdatedTime());
        assertNull(twinParser.getConnectionState());
        assertNull(twinParser.getConnectionStateUpdatedTime());
        assertNull(twinParser.getLastActivityTime());

        TwinParser resultTwinParser = new TwinParser();
        resultTwinParser.updateTwin(json);
        assertThat(resultTwinParser.getDeviceId(), is("Device name"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertThat(resultTwinParser.getStatus(), is(TwinStatus.disabled));
        assertThat(resultTwinParser.getStatusReason(), is("starting system"));
        assertNull(resultTwinParser.getStatusUpdatedTime());
    }

    /* Tests_SRS_TWINPARSER_21_165: [If the provided `status` is different than the previous one, and the `statusReason` is null, The updateDeviceManager shall throw IllegalArgumentException.] */
    @Test
    public void updateDeviceManager_newStatus_noReason_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.updateDeviceManager("Device name", TwinStatus.disabled, "starting system");

        // Act
        try
        {
            twinParser.updateDeviceManager("Device name", TwinStatus.enabled, null);
            assert true;
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything. Throw expected.
        }

        // Assert
        assertThat(twinParser.getDeviceId(), is("Device name"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertThat(twinParser.getStatus(), is(TwinStatus.disabled));
        assertThat(twinParser.getStatusReason(), is("starting system"));
        assertNull(twinParser.getStatusUpdatedTime());
        assertNull(twinParser.getConnectionState());
        assertNull(twinParser.getConnectionStateUpdatedTime());
        assertNull(twinParser.getLastActivityTime());
    }

    /* Tests_SRS_TWINPARSER_21_116: [The updateTwin shall add all provided properties and tags to the collection.] */
    /* Tests_SRS_TWINPARSER_21_117: [The updateTwin shall return a string with json representing the properties and tags with changes.] */
    /* Tests_SRS_TWINPARSER_21_082: [If any `value` is null, the updateTwin shall store it but do not report on Json.] */
    @Test
    public void updateTwin_emptyClass_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "value1");
        newDesiredValues.put("key2", 1234);
        newDesiredValues.put("key3", "value3");
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value1");
        newReportedValues.put("key2", null);
        newReportedValues.put("key3", "value3");
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{
            put("keyString", "value1");
            put("KeyBool", true);
            put("keyDouble", 1234.456);
            put("KeyChar", 'c');
            put("keyEnum", myEnum.val1);
        }});

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);

        // Assert
        assertThat(json, is("{\"tags\":{" +
                "\"tag1\":{\"KeyChar\":\"c\",\"KeyBool\":true,\"keyString\":\"value1\",\"keyEnum\":\"val1\",\"keyDouble\":1234.456}}," +
                "\"properties\":{" +
                    "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}," +
                    "\"reported\":{\"key1\":\"value1\",\"key3\":\"value3\"}}}"));

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertTrue(result.containsKey("key2"));
        assertNull(result.get("key2"));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(5));
        assertThat(innerMap.get("keyString").toString(), is("value1"));
        assertThat(Boolean.parseBoolean(innerMap.get("KeyBool").toString()), is(true));
        assertThat(Double.parseDouble(innerMap.get("keyDouble").toString()), is(1234.456));
        assertThat(innerMap.get("KeyChar").toString(), is("c"));
        assertThat(myEnum.valueOf(innerMap.get("keyEnum").toString()), is(myEnum.val1));
    }

    /* Tests_SRS_TWINPARSER_21_081: [If any `key` already exists, the updateTwin shall replace the existed value by the new one.] */
    /* Tests_SRS_TWINPARSER_21_126: [The updateTwin shall only change properties and tags in the map, keep the others as is.] */
    /* Tests_SRS_TWINPARSER_21_127: [The `key` and `value` in the maps shall be case sensitive.] */
    @Test
    public void updateTwin_changeKeysAndValues_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "newValue1");
        newDesiredValues.put("key3", "value30");
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value 10.");
        newReportedValues.put("key2", 1234);
        newReportedValues.put("key3", "VALUE3");
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);

        // Assert
        assertThat(json, is("{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                    "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                    "\"reported\":{\"key1\":\"value 10.\",\"key3\":\"VALUE3\"}}}"));

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value 10."));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("VALUE3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("newValue1"));
        assertThat(result.get("key3").toString(), is("value30"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(4));
        assertThat(innerMap.get("Key1").toString(), is("newValue1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        assertThat(innerMap.get("KEY3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_118: [If one of the provided map is null, the updateTwin shall not change that part of the collection.] */
    @Test
    public void updateTwin_nullTags_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "newValue1");
        newDesiredValues.put("key3", "value30");
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value10");
        newReportedValues.put("key2", 1234);
        newReportedValues.put("key3", "VALUE3");

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, null);

        // Assert
        assertThat(json, is(
                "{\"tags\":{}," +
                        "\"properties\":{" +
                            "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                            "\"reported\":{\"key1\":\"value10\",\"key3\":\"VALUE3\"}}}"));

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value10"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("VALUE3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("newValue1"));
        assertThat(result.get("key3").toString(), is("value30"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
    }


    @Test
    public void updateTwin_NonNullTags_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "newValue1");
        newDesiredValues.put("key3", "value30");
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value10");
        newReportedValues.put("key2", 1234);
        newReportedValues.put("key3", "VALUE3");

        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("key1", "newValue1");

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);

        // Assert
        assertThat(json, is(
                "{\"tags\":{\"key1\":\"newValue1\"}," +
                        "\"properties\":{" +
                        "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                        "\"reported\":{\"key1\":\"value10\",\"key3\":\"VALUE3\"}}}"));

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value10"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("VALUE3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("newValue1"));
        assertThat(result.get("key3").toString(), is("value30"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_118: [If one of the provided map is null, the updateTwin shall not change that part of the collection.] */
    @Test
    public void updateTwin_nullDesired_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value10");
        newReportedValues.put("key2", 1234);
        newReportedValues.put("key3", "VALUE3");
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        String json = twinParser.updateTwin(null, newReportedValues, newTagsValues);

        // Assert
        assertThat(json, is("{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{\"key1\":\"value10\",\"key3\":\"VALUE3\"}}}"));

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value10"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("VALUE3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(4));
        assertThat(innerMap.get("Key1").toString(), is("newValue1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        assertThat(innerMap.get("KEY3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_118: [If one of the provided map is null, the updateTwin shall not change that part of the collection.] */
    @Test
    public void updateTwin_nullReported_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "newValue1");
        newDesiredValues.put("key3", "value30");
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        String json = twinParser.updateTwin(newDesiredValues, null, newTagsValues);

        // Assert
        assertThat(json, is("{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                "\"reported\":{}}}"));

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("newValue1"));
        assertThat(result.get("key3").toString(), is("value30"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(4));
        assertThat(innerMap.get("Key1").toString(), is("newValue1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        assertThat(innerMap.get("KEY3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_118: [If one of the provided map is null, the updateTwin shall not change that part of the collection.] */
    @Test
    public void updateTwin_nullDesiredAndReported_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        String json = twinParser.updateTwin(null, null, newTagsValues);

        // Assert
        assertThat(json, is("{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{}}}"));

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(4));
        assertThat(innerMap.get("Key1").toString(), is("newValue1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        assertThat(innerMap.get("KEY3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_160: [If all of the provided map is null, the updateTwin shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTwin_allNull_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        // Act
        try
        {
            twinParser.updateTwin(null, null, null);
            assert true;
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything. Expected throw.
        }

        // Assert
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_119: [If no property or tags changed its value, the updateTwin shall return null.] */
    @Test
    public void updateTwin_noChanges_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "value1");
        newDesiredValues.put("key3", "value3");
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key7", true);
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key3", "value3"); }});

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);

        // Assert
        assertNull(json);

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_128: [If one of the provided map is empty, the updateTwin shall not change its the collection.] */
    @Test
    public void updateTwin_emptyTags_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "newValue1");
        newDesiredValues.put("key3", "value30");
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value10");
        newReportedValues.put("key2", 1234);
        newReportedValues.put("key3", "VALUE3");
        Map<String, Object> newTagsValues = new HashMap<>();

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);

        // Assert
        assertThat(json, is(
                "{\"tags\":{}," +
                        "\"properties\":{" +
                        "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                        "\"reported\":{\"key1\":\"value10\",\"key3\":\"VALUE3\"}}}"));

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value10"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("VALUE3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("newValue1"));
        assertThat(result.get("key3").toString(), is("value30"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_128: [If one of the provided map is empty, the updateTwin shall not change its the collection.] */
    @Test
    public void updateTwin_emptyDesired_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value10");
        newReportedValues.put("key2", 1234);
        newReportedValues.put("key3", "VALUE3");
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);

        // Assert
        assertThat(json, is("{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{\"key1\":\"value10\",\"key3\":\"VALUE3\"}}}"));

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value10"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("VALUE3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(4));
        assertThat(innerMap.get("Key1").toString(), is("newValue1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        assertThat(innerMap.get("KEY3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_128: [If one of the provided map is empty, the updateTwin shall not change its the collection.] */
    @Test
    public void updateTwin_emptyReported_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "newValue1");
        newDesiredValues.put("key3", "value30");
        Map<String, Object> newReportedValues = new HashMap<>();
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);

        // Assert
        assertThat(json, is("{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                "\"reported\":{}}}"));

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("newValue1"));
        assertThat(result.get("key3").toString(), is("value30"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(4));
        assertThat(innerMap.get("Key1").toString(), is("newValue1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        assertThat(innerMap.get("KEY3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_128: [If one of the provided map is empty, the updateTwin shall not change its the collection.] */
    @Test
    public void updateTwin_emptyDesiredAndReported_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        Map<String, Object> newReportedValues = new HashMap<>();
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);

        // Assert
        assertThat(json, is("{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{}}}"));

        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(4));
        assertThat(innerMap.get("Key1").toString(), is("newValue1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        assertThat(innerMap.get("KEY3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_080: [If one of the maps is invalid, the updateTwin shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTwin_invalidValue_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", new int[]{1,2,3});
        newDesiredValues.put("key3", "value30");
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value10");
        newReportedValues.put("key2", 1234);
        newReportedValues.put("key3", "VALUE3");
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        try
        {
            twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);
            assert true;
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything. Expected throw.
        }

        // Assert
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_080: [If one of the maps is invalid, the updateTwin shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTwin_invalidDotKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "value1");
        newDesiredValues.put("key3", "value30");
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value10");
        newReportedValues.put("key2", 1234);
        newReportedValues.put("key3", "VALUE3");
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put(ILLEGAL_STRING_DOT, "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        try
        {
            twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);
            assert true;
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything. Expected throw.
        }

        // Assert
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_080: [If one of the maps is invalid, the updateTwin shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTwin_invalidDollarKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "value1");
        newDesiredValues.put("key3", "value30");
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value10");
        newReportedValues.put(ILLEGAL_STRING_DOLLAR, 1234);
        newReportedValues.put("key3", "VALUE3");
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        try
        {
            twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);
            assert true;
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything. Expected throw.
        }

        // Assert
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_080: [If one of the maps is invalid, the updateTwin shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTwin_invalidSpaceKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "value1");
        newDesiredValues.put("key3", "value30");
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value10");
        newReportedValues.put(ILLEGAL_STRING_SPACE, 1234);
        newReportedValues.put("key3", "VALUE3");
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        try
        {
            twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);
            assert true;
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything. Expected throw.
        }

        // Assert
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_075: [If Tags is not enable and `tagsMap` is not null, the updateTwin shall throw IOException.] */
    @Test
    public void updateTwin_tagsMap_tagDisabled_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key1", "value10");
        newDesiredValues.put("key2", 12345);
        newDesiredValues.put("key3", "value30");
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value10");
        newReportedValues.put("key2", 12340);
        newReportedValues.put("key3", "value30");
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});

        // Act
        try
        {
            twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);
            assert true;
        }
        catch (IOException expected)
        {
            // Don't do anything. Expected throw.
        }

        // Assert
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(2));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));

        result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key7").toString(), is("true"));
    }


    /* Tests_SRS_TWINPARSER_21_009: [The constructor shall call the standard constructor.] */
    /* Tests_SRS_TWINPARSER_21_012: [The constructor shall set Tags as null.] */
    /* Tests_SRS_TWINPARSER_21_010: [The constructor shall set OnDesiredCallback with the provided Callback function.] */
    /* Tests_SRS_TWINPARSER_21_011: [The constructor shall set OnReportedCallback with the provided Callback function.] */
    /* Tests_SRS_TWINPARSER_21_039: [The updateTwin shall fill the fields the properties in the TwinParser class with the keys and values provided in the json string.] */
    /* Tests_SRS_TWINPARSER_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
    /* Tests_SRS_TWINPARSER_21_044: [If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.] */
    /* Tests_SRS_TWINPARSER_21_045: [If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.] */
    @Test
    public void updateTwin_json_emptyClass_noMetadata_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);

        String json = "{\"properties\":{" +
                "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value 3\"}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value.5\",\"key7\":true}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onDesiredCallback.diff.get("key2").toString()), is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value 3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onReportedCallback.diff.get("key2").toString()), is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value.5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value 3"));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.124));
        assertThat(result.get("key5").toString(), is("value.5"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWINPARSER_21_089: [If the provided json contains `desired` or `reported` in its first level, the updateTwin shall parser the json as properties only.] */
    @Test
    public void updateTwin_json_emptyClass_PropertyOnlyJson_startDesired_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);

        String json = "{\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onDesiredCallback.diff.get("key2").toString()), is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onReportedCallback.diff.get("key2").toString()), is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.124));
        assertThat(result.get("key5").toString(), is("value5"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWINPARSER_21_089: [If the provided json contains `desired` or `reported` in its first level, the updateTwin shall parser the json as properties only.] */
    @Test
    public void updateTwin_json_emptyClass_PropertyOnlyJson_startReported_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);

        String json = "{\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}," +
                "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onDesiredCallback.diff.get("key2").toString()), is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onReportedCallback.diff.get("key2").toString()), is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.124));
        assertThat(result.get("key5").toString(), is("value5"));
        assertThat(result.get("key7").toString(), is("true"));
    }

    /* Tests_SRS_TWINPARSER_21_090: [If the provided json is properties only and contains other tag different than `desired` or `reported`, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_emptyClass_PropertyOnlyJson_withProperties_failed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);

        String json = ("{\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}," +
                "\"properties\":{}}");

        // Act
        twinParser.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_091: [If the provided json is NOT properties only and contains `desired` or `reported` in its first level, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_emptyClass_FullTwinJson_withDesired_failed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);

        String json = ("{\"tags\":{}," +
                "\"properties\":{" +
                    "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}," +
                    "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}," +
                "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}}");

        // Act
        twinParser.updateTwin(json);

        // Assert
    }


    /* Tests_SRS_TWINPARSER_21_046: [If OnDesiredCallback was not provided, the updateTwin shall not do anything with the list of updated desired properties.] */
    /* Tests_SRS_TWINPARSER_21_047: [If OnReportedCallback was not provided, the updateTwin shall not do anything with the list of updated reported properties.] */
    @Test
    public void updateTwin_json_emptyClass_noCallback_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"desired\":{\"key1\":\"value1\",\"key2\":1234.0,\"key3\":\"value3\"}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        String resultJson = twinParser.toJson();
        assertThat(resultJson, is(json));
    }

    /* Tests_SRS_TWINPARSER_21_069: [If there is no change in the Desired property, the updateTwin shall not change the reported collection and not call the OnReportedCallback.] */
    @Test
    public void updateTwin_json_emptyClass_noChangeOnDesired_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        String resultJson = twinParser.toJson();
        assertThat(resultJson, is(json));
    }

    /* Tests_SRS_TWINPARSER_21_069: [If there is no change in the Desired property, the updateTwin shall not change the reported collection and not call the OnReportedCallback.] */
    @Test
    public void updateTwin_json_emptyClass_noDesired_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        String resultJson = twinParser.toJson();
        assertThat(resultJson, is("{\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}}"));
    }

    /* Tests_SRS_TWINPARSER_21_070: [If there is no change in the Reported property, the updateTwin shall not change the reported collection and not call the OnReportedCallback.] */
    @Test
    public void updateTwin_json_emptyClass_noChangeOnReported_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{\"desired\":{\"key1\":\"value1\",\"key2\":1234.0,\"key3\":\"value3\"},\"reported\":{}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        String resultJson = twinParser.toJson();
        assertThat(resultJson, is(json));
    }

    /* Tests_SRS_TWINPARSER_21_071: [If the provided json is empty, the updateTwin shall not change the collection and not call the OnDesiredCallback or the OnReportedCallback.] */
    @Test
    public void updateTwin_json_empty_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "";

        // Act
        twinParser.updateTwin(json);

        // Assert
        assertNull(twinParser.getDesiredPropertyMap());
        assertNull(twinParser.getReportedPropertyMap());
    }

    /* Tests_SRS_TWINPARSER_21_072: [If the provided json is null, the updateTwin shall not change the collection, not call the OnDesiredCallback or the OnReportedCallback, and throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_null_succeed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.updateTwin(null);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_112: [If the provided json contains `deviceId`, `generationId`, `etag`, `status`, `statusReason`, `statusUpdatedTime`, `connectionState`, `connectionStateUpdatedTime`, `lastActivityTime`, and `lastAcceptingIpFilterRule`, the updateTwin shall store its value.] */
    /* Tests_SRS_TWINPARSER_21_112: [The `getDeviceId` shall return the device name.] */
    /* Tests_SRS_TWINPARSER_21_150: [The `getGenerationId` shall return the device generation name.] */
    /* Tests_SRS_TWINPARSER_21_113: [The `getETag` shall return the string representing a weak ETAG version.] */
    /* Tests_SRS_TWINPARSER_21_136: [The `getStatus` shall return the device status.] */
    /* Tests_SRS_TWINPARSER_21_137: [The `getStatusReason` shall return the device status reason.] */
    /* Tests_SRS_TWINPARSER_21_138: [The `getStatusUpdatedTime` shall return the device status update date and time.] */
    /* Tests_SRS_TWINPARSER_21_147: [The `getConnectionState` shall return the connection state.] */
    /* Tests_SRS_TWINPARSER_21_148: [The `getConnectionStateUpdatedTime` shall return the connection state update date and time.] */
    /* Tests_SRS_TWINPARSER_21_151: [The `getLastActivityTime` shall return the last activity date and time.] */
    @Test
    public void updateTwin_json_emptyClass_managerParameters_withProperties_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);

        String json =
                "{" +
                    "\"deviceId\":\"device name\"," +
                    "\"generationId\":\"generation name\"," +
                    "\"etag\":\"AAAAAAAAAAU=\"," +
                    "\"status\":\"enabled\"," +
                    "\"statusReason\":\"because it is not disabled\"," +
                    "\"statusUpdatedTime\":\"2015-02-28T16:24:48.789Z\"," +
                    "\"connectionState\":\"connected\"," +
                    "\"connectionStateUpdatedTime\":\"2015-02-28T16:24:48.789Z\"," +
                    "\"lastActivityTime\":\"2017-02-16T21:59:56.631406Z\"," +
                    "\"properties\":{"+
                        "\"desired\":{" +
                            "\"key3\":\"value3\"" +
                        "}," +
                        "\"reported\":{" +
                            "\"key1\":\"value1\"," +
                            "\"key2\":1234.124" +
                        "}" +
                    "}" +
                "}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        assertThat(twinParser.getDeviceId(), is("device name"));
        assertThat(twinParser.getGenerationId(), is("generation name"));
        assertThat(twinParser.getETag(), is("AAAAAAAAAAU="));
        assertThat(twinParser.getStatus(), is(TwinStatus.enabled));
        assertThat(twinParser.getStatusReason(), is("because it is not disabled"));
        assertThat(twinParser.getStatusUpdatedTime(), is("2015-02-28T16:24:48.789Z"));
        assertThat(twinParser.getConnectionState(), is(TwinConnectionState.connected));
        assertThat(twinParser.getConnectionStateUpdatedTime(), is("2015-02-28T16:24:48.789Z"));
        assertThat(twinParser.getLastActivityTime(), is("2017-02-16T21:59:56.631406Z"));

        assertThat(twinParser.toJson(), is(json));
    }

    @Test
    public void updateTwin_json_emptyClass_managerParameters_noProperties_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);

        String json =
                "{" +
                    "\"deviceId\":\"device name\"," +
                    "\"generationId\":\"generation name\"," +
                    "\"etag\":\"AAAAAAAAAAU=\"," +
                    "\"status\":\"enabled\"," +
                    "\"statusReason\":\"because it is not disabled\"," +
                    "\"statusUpdatedTime\":\"2015-02-28T16:24:48.789Z\"," +
                    "\"connectionState\":\"connected\"," +
                    "\"connectionStateUpdatedTime\":\"2015-02-28T16:24:48.789Z\"," +
                    "\"lastActivityTime\":\"2017-02-16T21:59:56.631406Z\"," +
                    "\"properties\":{"+
                        "\"desired\":{}," +
                        "\"reported\":{}" +
                    "}" +
                "}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        assertThat(twinParser.getDeviceId(), is("device name"));
        assertThat(twinParser.getGenerationId(), is("generation name"));
        assertThat(twinParser.getETag(), is("AAAAAAAAAAU="));
        assertThat(twinParser.getStatus(), is(TwinStatus.enabled));
        assertThat(twinParser.getStatusReason(), is("because it is not disabled"));
        assertThat(twinParser.getStatusUpdatedTime(), is("2015-02-28T16:24:48.789Z"));
        assertThat(twinParser.getConnectionState(), is(TwinConnectionState.connected));
        assertThat(twinParser.getConnectionStateUpdatedTime(), is("2015-02-28T16:24:48.789Z"));
        assertThat(twinParser.getLastActivityTime(), is("2017-02-16T21:59:56.631406Z"));

        assertThat(twinParser.toJson(), is(json));
    }

    /* Tests_SRS_TWINPARSER_21_039: [The updateTwin shall fill the fields the properties in the TwinParser class with the keys and values provided in the json string.] */
    /* Tests_SRS_TWINPARSER_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
    /* Tests_SRS_TWINPARSER_21_048: [The getDesiredPropertyVersion shall return the desired property version.] */
    /* Tests_SRS_TWINPARSER_21_049: [The getReportedPropertyVersion shall return the reported property version.] */
    @Test
    public void updateTwin_json_emptyClass_withFullMetadata_andTags_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        OnTagsCallback onTagsCallback = new OnTagsCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        twinParser.setTagsCallback(onTagsCallback);
        twinParser.enableMetadata();
        twinParser.enableTags();

        String json =
            "{" +
                "\"tags\":{"+
                    "\"tag1\":{" +
                        "\"innerKey1\":\"value1\"," +
                        "\"innerKey2\":\"value2\"" +
                    "}" +
                "},"+
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
        twinParser.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onDesiredCallback.diff.get("key2").toString()), is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onReportedCallback.diff.get("key2").toString()), is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        assertThat(onTagsCallback.diff.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)onTagsCallback.diff.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(2));
        assertThat(innerMap.get("innerKey1").toString(), is("value1"));
        assertThat(innerMap.get("innerKey2").toString(), is("value2"));

        String resultJson = twinParser.toJson();
        assertThat(resultJson, is(json));
    }

    /* Tests_SRS_TWINPARSER_21_112: [If the provided json contains `deviceId`, `generationId`, `etag`, `status`, `statusReason`, `statusUpdatedTime`, `connectionState`, `connectionStateUpdatedTime`, `lastActivityTime`, and `lastAcceptingIpFilterRule`, the updateTwin shall store its value.] */
    @Test
    public void updateTwin_json_emptyClass_service_realCase_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        OnTagsCallback onTagsCallback = new OnTagsCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        twinParser.setTagsCallback(onTagsCallback);
        twinParser.enableMetadata();
        twinParser.enableTags();

        String json =
                "{" +
                    "\"deviceId\":\"TwinDevice\"," +
                    "\"etag\":\"AAAAAAAAAAU=\"," +
                    "\"properties\":" +
                    "{" +
                        "\"desired\":" +
                        "{" +
                            "\"telemetryInterval\":30," +
                            "\"$metadata\":" +
                            "{" +
                                "\"$lastUpdated\":\"2017-02-16T21:59:56.631406Z\"," +
                                "\"$lastUpdatedVersion\":5," +
                                "\"telemetryInterval\":" +
                                "{" +
                                    "\"$lastUpdated\":\"2017-02-16T21:59:56.631406Z\"," +
                                    "\"$lastUpdatedVersion\":5" +
                                "}" +
                            "}," +
                            "\"$version\":5" +
                        "}," +
                        "\"reported\":" +
                        "{" +
                            "\"state\":" +
                            "{" +
                                "\"softwareVersion\":1," +
                                "\"reported_maxSpeed\":100," +
                                "\"vanityPlate\":\"1I1\"" +
                            "}," +
                            "\"maker\":" +
                            "{" +
                                "\"makerName\":\"Fabrikam\"," +
                                "\"style\":\"sedan\"," +
                                "\"year\":2014" +
                            "}," +
                            "\"lastOilChangeDate\":\"2016\"," +
                            "\"name\":\"Phone\"," +
                            "\"temp\":100," +
                            "\"pressure\":10.5," +
                            "\"$metadata\":" +
                            "{" +
                                "\"$lastUpdated\":\"2017-02-14T18:16:08.8445885Z\"," +
                                "\"state\":" +
                                "{" +
                                    "\"$lastUpdated\":\"2017-02-14T18:16:08.8445885Z\"," +
                                    "\"softwareVersion\":" +
                                    "{" +
                                        "\"$lastUpdated\":\"2017-02-14T18:16:08.8445885Z\"" +
                                    "}," +
                                    "\"reported_maxSpeed\":" +
                                    "{" +
                                        "\"$lastUpdated\":\"2017-02-14T18:16:08.8445885Z\"" +
                                    "}," +
                                    "\"vanityPlate\":" +
                                    "{" +
                                        "\"$lastUpdated\":\"2017-02-14T18:16:08.8445885Z\"" +
                                    "}" +
                                "}," +
                                "\"maker\":" +
                                "{" +
                                    "\"$lastUpdated\":\"2017-02-14T18:16:08.8445885Z\"," +
                                    "\"makerName\":" +
                                    "{" +
                                        "\"$lastUpdated\":\"2017-02-14T18:16:08.8445885Z\"" +
                                    "}," +
                                    "\"style\":" +
                                    "{" +
                                        "\"$lastUpdated\":\"2017-02-14T18:16:08.8445885Z\"" +
                                    "}," +
                                    "\"year\":" +
                                    "{" +
                                        "\"$lastUpdated\":\"2017-02-14T18:16:08.8445885Z\"" +
                                    "}" +
                                "}," +
                                "\"lastOilChangeDate\":" +
                                "{" +
                                    "\"$lastUpdated\":\"2017-02-14T18:16:08.8445885Z\"" +
                                "}," +
                                "\"name\":" +
                                "{" +
                                    "\"$lastUpdated\":\"2017-02-09T02:15:53.5541078Z\"" +
                                "}," +
                                "\"temp\":" +
                                "{" +
                                    "\"$lastUpdated\":\"2017-02-09T02:15:53.5541078Z\"" +
                                "}," +
                                "\"pressure\":" +
                                "{" +
                                    "\"$lastUpdated\":\"2017-02-09T02:15:53.5541078Z\"" +
                                "}" +
                            "}," +
                            "\"$version\":34" +
                        "}" +
                    "}" +
                "}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(1));
        assertThat(Double.parseDouble(onDesiredCallback.diff.get("telemetryInterval").toString()), is(30.0));

        assertThat(onReportedCallback.diff.size(), is(6));

        // TODO: Test disabled with bug.
/*
        Map<String, Object> innerMap = (Map<String, Object>)onTagsCallback.diff.get("state");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(Double.parseDouble(innerMap.get("softwareVersion").toString()), is(1.0));
        assertThat(Double.parseDouble(innerMap.get("reported_maxSpeed").toString()), is(100.0));
        assertThat(innerMap.get("vanityPlate").toString(), is("1I1"));

        innerMap = (Map<String, Object>)onTagsCallback.diff.get("maker");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("makerName").toString(), is("Fabrikam"));
        assertThat(innerMap.get("style").toString(), is("sedan"));
        assertThat(innerMap.get("year").toString(), is("2014"));
*/
        assertThat(onReportedCallback.diff.get("lastOilChangeDate").toString(), is("2016"));
        assertThat(onReportedCallback.diff.get("name").toString(), is("Phone"));
        assertThat(Double.parseDouble(onReportedCallback.diff.get("temp").toString()), is(100.0));
        assertThat(Double.parseDouble(onReportedCallback.diff.get("pressure").toString()), is(10.5));

        assertNull(onTagsCallback.diff);

        assertThat(twinParser.getDeviceId(), is("TwinDevice"));
        assertThat(twinParser.getETag(), is("AAAAAAAAAAU="));

        // TODO: Test disabled with bug.
/*
        String resultJson = twinParser.toJson();
        assertThat(resultJson, is(json));
*/
    }

    /* Tests_SRS_TWINPARSER_21_039: [The updateTwin shall fill the fields the properties in the TwinParser class with the keys and values provided in the json string.] */
    /* Tests_SRS_TWINPARSER_21_040: [The updateTwin shall not change fields that is not reported in the json string.] */
    @Test
    public void updateTwin_json_emptyClass_withMetadataNoUpdateVersion_And5LevelsTags_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        OnTagsCallback onTagsCallback = new OnTagsCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        twinParser.setTagsCallback(onTagsCallback);
        twinParser.enableMetadata();
        twinParser.enableTags();

        String json =
            "{" +
                "\"tags\":{"+
                    "\"tag1\":{" +
                        "\"innerKey1\":\"value1\"," +
                        "\"innerKey2\":\"value2\"" +
                    "}," +
                    "\"one\":{" +
                        "\"two\":{" +
                            "\"three\":{" +
                                "\"four\":{" +
                                    "\"five\":{" +
                                        "\"innerKey\":\"value\"" +
                                    "}" +
                                "}" +
                            "}" +
                        "}" +
                    "}" +
                "},"+
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
        twinParser.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onDesiredCallback.diff.get("key2").toString()), is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onReportedCallback.diff.get("key2").toString()), is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        assertThat(onTagsCallback.diff.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)onTagsCallback.diff.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(2));
        assertThat(innerMap.get("innerKey1").toString(), is("value1"));
        assertThat(innerMap.get("innerKey2").toString(), is("value2"));

        innerMap = (Map<String, Object>)onTagsCallback.diff.get("one");
        innerMap = (Map<String, Object>)innerMap.get("two");
        innerMap = (Map<String, Object>)innerMap.get("three");
        innerMap = (Map<String, Object>)innerMap.get("four");
        innerMap = (Map<String, Object>)innerMap.get("five");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(1));
        assertThat(innerMap.get("innerKey").toString(), is("value"));

        String resultJson = twinParser.toJson();
        assertThat(resultJson, is(json));
    }

    @Test
    public void updateTwin_json_And6LevelsTags_failed() throws IOException
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        OnTagsCallback onTagsCallback = new OnTagsCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        twinParser.setTagsCallback(onTagsCallback);
        twinParser.enableMetadata();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);
        newValues.clear();
        newValues.put("key555", "value1234");
        twinParser.updateReportedProperty(newValues);
        twinParser.updateDesiredProperty(newValues);

        String json =
            "{" +
                "\"tags\":{"+
                    "\"tag1\":{" +
                        "\"innerKey1\":\"value1\"," +
                        "\"innerKey2\":\"value2\"" +
                    "}," +
                    "\"one\":{" +
                        "\"two\":{" +
                            "\"three\":{" +
                                "\"four\":{" +
                                    "\"five\":{" +
                                        "\"six\":{" +
                                            "\"innerKey\":\"value\"" +
                                        "}" +
                                    "}" +
                                "}" +
                            "}" +
                        "}" +
                    "}" +
                "},"+
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
        try
        {
            twinParser.updateTwin(json);
            assert true;
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything, expected throw.
        }

        // Assert
        assertNull(onDesiredCallback.diff);
        assertNull(onReportedCallback.diff);
        assertNull(onTagsCallback.diff);

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key555").toString(), is("value1234"));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(1));
        assertThat(result.get("key555").toString(), is("value1234"));

        result = twinParser.getTagsMap();
        assertThat(result.size(), is(1));
        Map<String, Object> innerMap = (Map<String, Object>)result.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("true"));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_039: [The updateTwin shall fill the fields the properties in the TwinParser class with the keys and values provided in the json string.] */
    /* Tests_SRS_TWINPARSER_21_040: [The updateTwin shall not change fields that is not reported in the json string.] */
    @Test
    public void updateTwin_json_emptyClass_withFullMetadataNoVersion_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        twinParser.enableMetadata();

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
        twinParser.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(3));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onDesiredCallback.diff.get("key2").toString()), is(1234.0));
        assertThat(onDesiredCallback.diff.get("key3").toString(), is("value3"));

        assertThat(onReportedCallback.diff.size(), is(4));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(onReportedCallback.diff.get("key2").toString()), is(1234.124));
        assertThat(onReportedCallback.diff.get("key5").toString(), is("value5"));
        assertThat(onReportedCallback.diff.get("key7").toString(), is("true"));

        String resultJson = twinParser.toJson();
        assertThat(resultJson, is(json));
    }

    /* Tests_SRS_TWINPARSER_21_040: [The updateTwin shall not change fields that is not reported in the json string.] */
    /* Tests_SRS_TWINPARSER_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
    /* Tests_SRS_TWINPARSER_21_044: [If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.] */
    /* Tests_SRS_TWINPARSER_21_045: [If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.] */
    @Test
    public void updateTwin_json_changeOneField_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);

        String json = "{\"properties\":{" +
                "\"desired\":{\"key2\":9875}," +
                "\"reported\":{\"key1\":\"value4\"}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(1));
        assertThat(Double.parseDouble(onDesiredCallback.diff.get("key2").toString()), is(9875.0));

        assertThat(onReportedCallback.diff.size(), is(1));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value4"));

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(9875.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_097: [If the provided json have any duplicated `properties` or `tags`, the updateTwin shall throw IllegalArgumentException.] */
    @Test
    public void updateTwin_json_duplicatedProperties_failed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);

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
            twinParser.updateTwin(json);
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
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_098: [If the provided json is properties only and contains duplicated `desired` or `reported`, the updateTwin shall throws IllegalArgumentException.] */
    @Test
    public void updateTwin_json_duplicatedDesiredFirsLevel_failed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);

        String json =
                "{" +
                    "\"desired\":{\"key2\":9875}," +
                    "\"desired\":{\"key1\":\"value1\"}," +
                    "\"reported\":{\"key1\":\"value4\"}" +
                "}";

        // Act
        try
        {
            twinParser.updateTwin(json);
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
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_094: [If the provided json have any duplicated `key`, the updateTwin shall use the content of the last one in the String.] */
    @Test
    public void updateTwin_json_duplicatedProperty_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);

        String json =
                "{" +
                    "\"properties\":{" +
                        "\"desired\":{\"key2\":9875}," +
                        "\"desired\":{\"key1\":\"value1\"}," +
                        "\"reported\":{\"key1\":\"value4\"}" +
                    "}" +
                "}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(1));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value1"));

        assertThat(onReportedCallback.diff.size(), is(1));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value4"));

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value1"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_094: [If the provided json have any duplicated `key`, the updateTwin shall use the content of the last one in the String.] */
    @Test
    public void updateTwin_json_duplicateDesiredKey_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twinParser.updateDesiredProperty(newValues);

        String json = "{\"properties\":{" +
                "\"desired\":{\"key2\":8,\"key2\":9875}," +
                "\"reported\":{\"key1\":\"value4\"}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(1));
        assertThat(Double.parseDouble(onDesiredCallback.diff.get("key2").toString()), is(9875.0));

        assertThat(onReportedCallback.diff.size(), is(1));
        assertThat(onReportedCallback.diff.get("key1").toString(), is("value4"));

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(4));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(9875.0));
        assertThat(result.get("key6").toString(), is("value6"));
        assertThat(result.get("key7").toString(), is("true"));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(3));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key3").toString(), is("value3"));
    }

    /* Tests_SRS_TWINPARSER_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
    /* Tests_SRS_TWINPARSER_21_042: [If a valid key has a null value, the updateTwin shall delete this property.] */
    /* Tests_SRS_TWINPARSER_21_044: [If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.] */
    /* Tests_SRS_TWINPARSER_21_045: [If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.] */
    @Test
    public void updateTwin_json_deleteField_noMetadata_succeed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateDesiredProperty(newValues);
        newValues.clear();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key6", "value6");
        newValues.put("key7", true);
        twinParser.updateReportedProperty(newValues);

        String json = "{\"properties\":{" +
                "\"desired\":{\"key3\":null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        assertThat(onDesiredCallback.diff.size(), is(2));
        assertNull(onDesiredCallback.diff.get("key3"));
        assertThat(onDesiredCallback.diff.get("key1").toString(), is("value4"));

        assertThat(onReportedCallback.diff.size(), is(2));
        assertNull(onReportedCallback.diff.get("key1"));
        assertNull(onReportedCallback.diff.get("key7"));

        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertThat(result.size(), is(2));
        assertThat(result.get("key1").toString(), is("value4"));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));

        result = twinParser.getReportedPropertyMap();
        assertThat(result.size(), is(2));
        assertThat(Double.parseDouble(result.get("key2").toString()), is(1234.0));
        assertThat(result.get("key6").toString(), is("value6"));
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_missing_comma_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"desired\":{\"key3\":null,\"key1\":\"value4\"}" +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_badProperties_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"bar properties\":{" +
                "\"desired\":{\"key3\":null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_unknownProperty_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"property\":{" +
                "\"bar Property\":{\"key3\":null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_InvalidKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"desired\":{\"\":null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_noKey_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"desired\":{:null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwin_json_InvalidValue_failed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"desired\":{\"Key3\":,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_074: [If Tags is not enable, the getTagsMap shall throw IOException.] */
    @Test (expected = IOException.class)
    public void getTagsMap_disabled_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.getTagsMap();

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_111: [If Tags is not enable, the updateTags shall throw IOException.] */
    @Test (expected = IOException.class)
    public void updateTags_disabled_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});

        // Act
        twinParser.updateTags(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_146: [If Tags is not enable, the resetTags shall throw IOException.] */
    @Test (expected = IOException.class)
    public void resetTags_disabled_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});

        // Act
        twinParser.resetTags(newValues);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_103: [The updateTags shall add all provided tags to the collection.] */
    /* Tests_SRS_TWINPARSER_21_104: [The updateTags shall return a string with json representing the tags with changes.] */
    /* Tests_SRS_TWINPARSER_21_052: [The getTagsMap shall return a map with all tags in the collection.] */
    /* Tests_SRS_TWINPARSER_21_157: [A valid `value` can contains sub-maps.] */
    /* Tests_SRS_TWINPARSER_21_158: [A valid `value` shall contains less than 5 levels of sub-maps.] */
    @Test
    public void updateTags_emptyClass_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        newValues.put("one",
                new HashMap<String, Object>(){{ put("two",
                        new HashMap<String, Object>(){{ put("three",
                                new HashMap<String, Object>(){{ put("four",
                                        new HashMap<String, Object>(){{ put("five",
                                                new HashMap<String, Object>(){{ put("tagKey", "value");
                                        }});
                                }});
                        }});
                }});
        }});

        // Act
        String json = twinParser.updateTags(newValues);

        // Assert
        assertThat(json, is("{\"tag1\":{\"Key2\":1234,\"Key1\":\"value1\",\"Key3\":\"value3\"}," +
                "\"one\":{\"two\":{\"three\":{\"four\":{\"five\":{\"tagKey\":\"value\"}}}}}," +
                "\"tag2\":{\"Key2\":\"value5\",\"Key1\":\"value1\",\"Key4\":false}}"));

        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(3));

        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));

        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));

        innerMap = (Map<String, Object>)tagMap.get("one");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(1));
        innerMap = (Map<String, Object>)innerMap.get("two");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(1));
        innerMap = (Map<String, Object>)innerMap.get("three");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(1));
        innerMap = (Map<String, Object>)innerMap.get("four");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(1));
        innerMap = (Map<String, Object>)innerMap.get("five");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(1));
        assertThat(innerMap.get("tagKey").toString(), is("value"));
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_158: [A valid `value` shall contains less than 5 levels of sub-maps.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTags_mapBiggerThan5Levels_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        newValues.put("one",
                new HashMap<String, Object>(){{ put("two",
                        new HashMap<String, Object>(){{ put("three",
                                new HashMap<String, Object>(){{ put("four",
                                        new HashMap<String, Object>(){{ put("five",
                                                new HashMap<String, Object>(){{ put("six",
                                                        new HashMap<String, Object>(){{ put("propertyKey", "value");
                                                        }});
                                                }});
                                        }});
                                }});
                        }});
                }});

        // Act
        String json = twinParser.updateTags(newValues);

        // Assert

    }

    /* Tests_SRS_TWINPARSER_21_103: [The updateTags shall add all provided tags to the collection.] */
    /* Tests_SRS_TWINPARSER_21_104: [The updateTags shall return a string with json representing the tags with changes.] */
    /* Tests_SRS_TWINPARSER_21_107: [The updateTags shall only change tags in the map, keep the others as is.] */
    /* Tests_SRS_TWINPARSER_21_108: [All `key` and `value` in tags shall be case sensitive.] */
    /* Tests_SRS_TWINPARSER_21_114: [If any `key` already exists, the updateTags shall replace the existed value by the new one.] */
    /* Tests_SRS_TWINPARSER_21_115: [If any `value` is null, the updateTags shall store it but do not report on Json.] */
    @Test
    public void updateTags_addKey_changeValue_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        newValues.put("one",
                new HashMap<String, Object>(){{ put("two",
                        new HashMap<String, Object>(){{ put("three",
                                new HashMap<String, Object>(){{ put("four",
                                        new HashMap<String, Object>(){{ put("five",
                                                new HashMap<String, Object>(){{ put("tagKey", "value");
                                        }});
                                }});
                        }});
                }});
        }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key5", true); put("Key7", null); }});
        newValues.put("one",
                new HashMap<String, Object>(){{ put("two",
                        new HashMap<String, Object>(){{ put("three",
                                new HashMap<String, Object>(){{ put("four",
                                        new HashMap<String, Object>(){{ put("five",
                                                new HashMap<String, Object>(){{ put("tagKey", "newValue");
                                        }});
                                        put("FIVE",
                                                new HashMap<String, Object>(){{ put("tagKey", "newValue");
                                        }});
                                }});
                        }});
                }});
        }});

        // Act
        String json = twinParser.updateTags(newValues);

        // Assert
        assertThat(json, is("{\"tag1\":{\"Key1\":\"value4\"},\"one\":{\"two\":{\"three\":{\"four\":{\"FIVE\":{\"tagKey\":\"newValue\"},\"five\":{\"tagKey\":\"newValue\"}}}}},\"tag2\":{\"Key5\":true}}"));
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(3));

        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value4"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));

        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
        assertThat(innerMap.get("Key5").toString(), is("true"));
        assertTrue(innerMap.containsKey("Key7"));
        assertNull(innerMap.get("Key7"));

        innerMap = (Map<String, Object>)tagMap.get("one");
        assertNotNull(innerMap);
        innerMap = (Map<String, Object>)innerMap.get("two");
        assertNotNull(innerMap);
        innerMap = (Map<String, Object>)innerMap.get("three");
        assertNotNull(innerMap);
        innerMap = (Map<String, Object>)innerMap.get("four");
        assertNotNull(innerMap);
        Map<String, Object> innerMap1 = (Map<String, Object>)innerMap.get("five");
        assertNotNull(innerMap1);
        assertThat(innerMap1.get("tagKey").toString(), is("newValue"));
        Map<String, Object> innerMap2 = (Map<String, Object>)innerMap.get("FIVE");
        assertNotNull(innerMap2);
        assertThat(innerMap2.get("tagKey").toString(), is("newValue"));
    }

    /* Tests_SRS_TWINPARSER_21_114: [If any `key` already exists, the updateTags shall replace the existed value by the new one.] */
    @Test
    public void updateTags_replaceStringByMap_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        newValues.put("one",
                new HashMap<String, Object>(){{ put("two",
                        new HashMap<String, Object>(){{ put("three",
                                new HashMap<String, Object>(){{ put("four",
                                        new HashMap<String, Object>(){{ put("five",
                                                new HashMap<String, Object>(){{ put("tagKey", "value");
                                                }});
                                        }});
                                }});
                        }});
                }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1",
                new HashMap<String, Object>(){{
                    put("Key1",
                        new HashMap<String, Object>(){{
                            put("innerKey1", "value1");
                            put("innerKey2", "value2");
                }});
        }});

        // Act
        String json = twinParser.updateTags(newValues);

        // Assert
        assertThat(json, is("{\"tag1\":{\"Key1\":{\"innerKey2\":\"value2\",\"innerKey1\":\"value1\"}}}"));
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(3));

        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        Map<String, Object> innerMap1 = (Map<String, Object>)innerMap.get("Key1");
        assertNotNull(innerMap1);
        assertThat(innerMap1.size(), is(2));
        assertThat(innerMap1.get("innerKey1").toString(), is("value1"));
        assertThat(innerMap1.get("innerKey2").toString(), is("value2"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));

        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));

        innerMap = (Map<String, Object>)tagMap.get("one");
        assertNotNull(innerMap);
        innerMap = (Map<String, Object>)innerMap.get("two");
        assertNotNull(innerMap);
        innerMap = (Map<String, Object>)innerMap.get("three");
        assertNotNull(innerMap);
        innerMap = (Map<String, Object>)innerMap.get("four");
        assertNotNull(innerMap);
        innerMap = (Map<String, Object>)innerMap.get("five");
        assertNotNull(innerMap);
        assertThat(innerMap.get("tagKey").toString(), is("value"));
    }

    /* Tests_SRS_TWINPARSER_21_105: [If the provided `tagsMap` is null, the updateTags shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTags_null_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        // Act
        try
        {
            String json = twinParser.updateTags(null);
            assert true;
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything. Expected exception.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_106: [If no tags changed its value, the updateTags shall return null.] */
    @Test
    public void updateTags_noChanges_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        // Act
        String json = twinParser.updateTags(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }
    
    /* Tests_SRS_TWINPARSER_21_109: [If the provided `tagsMap` is empty, the updateTags shall not change the collection and return null.] */
    @Test
    public void updateTags_empty_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);
        newValues.clear();

        // Act
        String json = twinParser.updateTags(newValues);

        // Assert
        assertNull(json);
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_152: [A valid `key` shall not be null.] */
    @Test
    public void updateTags_nullKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put(null, true); }});

        // Act
        try
        {
            twinParser.updateTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_153: [A valid `key` shall not be empty.] */
    @Test
    public void updateTags_emptyKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("", true); }});

        // Act
        try
        {
            twinParser.updateTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }


    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_154: [A valid `key` shall be less than 128 characters long.] */
    @Test
    public void updateTags_bigKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put(BIG_STRING_150CHARS, true); }});

        // Act
        try
        {
            twinParser.updateTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void updateTags_invalidDollarKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put(ILLEGAL_STRING_DOLLAR, true); }});

        // Act
        try
        {
            twinParser.updateTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void updateTags_invalidDotKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put(ILLEGAL_STRING_DOT, new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key5", true); }});

        // Act
        try
        {
            twinParser.updateTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void updateTags_invalidSpaceKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put(ILLEGAL_STRING_SPACE, "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); }});

        // Act
        try
        {
            twinParser.updateTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_156: [A valid `value` shall contains types of boolean, number, string, or object.] */
    @Test
    public void updateTags_invalidValueType_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", new int[]{1,2,3}); }});

        // Act
        try
        {
            twinParser.updateTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_140: [The resetTags shall add cleanup the tags collection and all provided tags to the tags.] */
    /* Tests_SRS_TWINPARSER_21_141: [The resetTags shall return a string with json representing the added tags.] */
    /* Tests_SRS_TWINPARSER_21_143: [The `key` and `value` in tags shall be case sensitive.] */
    /* Tests_SRS_TWINPARSER_21_149: [If any `value` is null, the resetTags shall store it but do not report on Json.] */
    @Test
    public void resetTags_newKeyValue_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        newValues.put("one",
                new HashMap<String, Object>(){{ put("two",
                        new HashMap<String, Object>(){{ put("three",
                                new HashMap<String, Object>(){{ put("four",
                                        new HashMap<String, Object>(){{ put("five",
                                                new HashMap<String, Object>(){{ put("tagKey", "value");
                                                }});
                                        }});
                                }});
                        }});
                }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key5", true); put("Key7", null); }});
        newValues.put("one",
                new HashMap<String, Object>(){{ put("two",
                        new HashMap<String, Object>(){{ put("three333",
                                new HashMap<String, Object>(){{ put("four",
                                        new HashMap<String, Object>(){{ put("five",
                                                new HashMap<String, Object>(){{ put("tagKey", "newValue");
                                                }});
                                            put("FIVE",
                                                new HashMap<String, Object>(){{ put("tagKey", "newValue");
                                                }});
                                        }});
                                }});
                        }});
                }});

        // Act
        String json = twinParser.resetTags(newValues);

        // Assert
        assertThat(json, is("{\"tag1\":{\"Key1\":\"value4\"}," +
                "\"one\":{\"two\":{\"three333\":{\"four\":{" +
                        "\"FIVE\":{\"tagKey\":\"newValue\"}," +
                        "\"five\":{\"tagKey\":\"newValue\"}}}}}," +
                "\"tag2\":{\"Key1\":\"value1\",\"Key5\":true}}"));
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(3));

        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(1));
        assertThat(innerMap.get("Key1").toString(), is("value4"));

        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key5").toString(), is("true"));
        assertTrue(innerMap.containsKey("Key7"));
        assertNull(innerMap.get("Key7"));

        innerMap = (Map<String, Object>)tagMap.get("one");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(1));
        innerMap = (Map<String, Object>)innerMap.get("two");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(1));
        innerMap = (Map<String, Object>)innerMap.get("three333");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(1));
        innerMap = (Map<String, Object>)innerMap.get("four");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(2));
        Map<String, Object> innerMap1 = (Map<String, Object>)innerMap.get("five");
        assertNotNull(innerMap1);
        assertThat(innerMap1.size(), is(1));
        assertThat(innerMap1.get("tagKey").toString(), is("newValue"));
        Map<String, Object> innerMap2 = (Map<String, Object>)innerMap.get("FIVE");
        assertNotNull(innerMap2);
        assertThat(innerMap2.size(), is(1));
        assertThat(innerMap2.get("tagKey").toString(), is("newValue"));
    }


    /* Tests_SRS_TWINPARSER_21_142: [If the provided `tagsMap` is null, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void resetTags_nullMap_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        newValues.put("one",
                new HashMap<String, Object>(){{ put("two",
                        new HashMap<String, Object>(){{ put("three",
                                new HashMap<String, Object>(){{ put("four",
                                        new HashMap<String, Object>(){{ put("five",
                                                new HashMap<String, Object>(){{ put("tagKey", "value");
                                                }});
                                        }});
                                }});
                        }});
                }});
        twinParser.updateTags(newValues);

        // Act
        try
        {
            twinParser.resetTags(null);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(3));

        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));

        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.size(), is(3));
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(Boolean.parseBoolean(innerMap.get("Key4").toString()), is(false));

        innerMap = (Map<String, Object>)tagMap.get("one");
        innerMap = (Map<String, Object>)innerMap.get("two");
        innerMap = (Map<String, Object>)innerMap.get("three");
        innerMap = (Map<String, Object>)innerMap.get("four");
        innerMap = (Map<String, Object>)innerMap.get("five");
        assertThat(innerMap.size(), is(1));
        assertThat(innerMap.get("tagKey").toString(), is("value"));
    }

    /* Tests_SRS_TWINPARSER_21_144: [If the provided `tagsMap` is empty, the resetTags shall cleanup the tags collection and return `{}`.] */
    @Test
    public void resetTags_emptyMap_succeed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        newValues.put("one",
                new HashMap<String, Object>(){{ put("two",
                        new HashMap<String, Object>(){{ put("three",
                                new HashMap<String, Object>(){{ put("four",
                                        new HashMap<String, Object>(){{ put("five",
                                                new HashMap<String, Object>(){{ put("tagKey", "value");
                                                }});
                                        }});
                                }});
                        }});
                }});
        twinParser.updateTags(newValues);
        newValues.clear();

        // Act
        String json = twinParser.resetTags(newValues);

        // Assert
        assertThat(json, is("{}"));

        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(0));
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_152: [A valid `key` shall not be null.] */
    @Test
    public void resetTags_nullKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put(null, true); }});

        // Act
        try
        {
            twinParser.resetTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_153: [A valid `key` shall not be empty.] */
    @Test
    public void resetTags_emptyKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("", true); }});

        // Act
        try
        {
            twinParser.resetTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_154: [A valid `key` shall be less than 128 characters long.] */
    @Test
    public void resetTags_bigKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put(BIG_STRING_150CHARS, true); }});

        // Act
        try
        {
            twinParser.resetTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void resetTags_invalidDollarKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put(ILLEGAL_STRING_DOLLAR, true); }});

        // Act
        try
        {
            twinParser.resetTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void resetTags_invalidDotKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put(ILLEGAL_STRING_DOT, new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key5", true); }});

        // Act
        try
        {
            twinParser.resetTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void resetTags_invalidSpaceKey_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put(ILLEGAL_STRING_SPACE, "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); }});

        // Act
        try
        {
            twinParser.resetTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_156: [A valid `value` shall contains types of boolean, number, string, or object.] */
    @Test
    public void resetTags_invalidValueType_failed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(newValues);

        newValues.clear();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value4"); }});
        newValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", new int[]{1,2,3}); }});

        // Act
        try
        {
            twinParser.resetTags(newValues);
            assert (true);
        }
        catch (IllegalArgumentException expected)
        {
            //Don't do anything, expected throw.
        }

        // Assert
        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(2));
        Map<String, Object> innerMap = (Map<String, Object>)tagMap.get("tag1");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(Double.parseDouble(innerMap.get("Key2").toString()), is(1234.0));
        assertThat(innerMap.get("Key3").toString(), is("value3"));
        innerMap = (Map<String, Object>)tagMap.get("tag2");
        assertNotNull(innerMap);
        assertThat(innerMap.get("Key1").toString(), is("value1"));
        assertThat(innerMap.get("Key2").toString(), is("value5"));
        assertThat(innerMap.get("Key4").toString(), is("false"));
    }

}
