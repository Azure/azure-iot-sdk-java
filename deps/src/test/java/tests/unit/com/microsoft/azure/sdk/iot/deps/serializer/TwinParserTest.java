// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.JsonElement;
import com.microsoft.azure.sdk.iot.deps.serializer.*;
import mockit.Deencapsulation;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;


/**
 * Unit tests for TwinParser serializer
 * 100% methods, 96% lines covered
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

    private static void assertTwin(TwinParser twinParser, Map<String, Object> desired, Map<String, Object> reported, Map<String, Object> tags)
    {
        if(desired != null)
        {
            Helpers.assertMap(twinParser.getDesiredPropertyMap(), desired, null);
        }
        if(reported != null)
        {
            Helpers.assertMap(twinParser.getReportedPropertyMap(), reported, null);
        }
        if(tags != null)
        {
            try
            {
                Helpers.assertMap(twinParser.getTagsMap(), tags, null);
            }
            catch (IOException e)
            {
                assertTrue("getTagsMap throws IOException", true);
            }
        }
    }

    /* Tests_SRS_TWINPARSER_21_001: [The constructor shall create an instance of the properties.] */
    /* Tests_SRS_TWINPARSER_21_002: [The constructor shall set OnDesiredCallback as null.] */
    /* Tests_SRS_TWINPARSER_21_003: [The constructor shall set OnReportedCallback as null.] */
    /* Tests_SRS_TWINPARSER_21_004: [The constructor shall set Tags as null.] */
    @Test
    public void constructorSucceed()
    {
        // Arrange
        // Act
        TwinParser twinParser = new TwinParser();

        // Assert
        assertNotNull(twinParser);
    }

    /* Tests_SRS_TWINPARSER_21_013: [The setDesiredCallback shall set OnDesiredCallback with the provided Callback function.] */
    @Test
    public void setDesiredCallbackSucceed()
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
    public void setDesiredCallbackSecondInstanceSucceed()
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
    public void setDesiredCallbackNullSucceed()
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
    public void setReportedCallbackSucceed()
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
    public void setReportedCallbackSecondInstanceSucceed()
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
    public void setReportedCallbackNullSucceed()
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
    public void setTagsCallbackSucceed()
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
    public void setTagsCallbackSecondInstanceSucceed()
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
    public void setTagsCallbackNullSucceed()
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
    public void toJsonEmptyClassSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        String json = twinParser.toJson();

        // Assert
        Helpers.assertJson(json, "{\"properties\":{\"desired\":{},\"reported\":{}}}");
    }

    /* Tests_SRS_TWINPARSER_21_017: [The toJsonElement shall return a JsonElement with information in the TwinParser using json format.] */
    /* Tests_SRS_TWINPARSER_21_018: [The toJsonElement shall not include null fields.] */
    /* Tests_SRS_TWINPARSER_21_086: [The toJsonElement shall include the `properties` in the json even if it has no content.] */
    /* Tests_SRS_TWINPARSER_21_087: [The toJsonElement shall include the `desired` property in the json even if it has no content.] */
    /* Tests_SRS_TWINPARSER_21_088: [The toJsonElement shall include the `reported` property in the json even if it has no content.] */
    @Test
    public void toJsonElementEmptyClassSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        JsonElement jsonElement = twinParser.toJsonElement();

        // Assert
        Helpers.assertJson(jsonElement.toString(), "{\"properties\":{\"desired\":{},\"reported\":{}}}");
    }

    /* Tests_SRS_TWINPARSER_21_019: [The enableTags shall enable tags in the twin collection.] */
    /* Tests_SRS_TWINPARSER_21_085: [If `tags` is enable, the toJsonElement shall include the tags in the json even if it has no content.] */
    @Test
    public void toJsonEmptyClassWithTagsSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.enableTags();

        // Assert
        String json = twinParser.toJson();
        Helpers.assertJson(json, "{\"tags\":{},\"properties\":{\"desired\":{},\"reported\":{}}}");
    }

    /* Tests_SRS_TWINPARSER_21_161: [It tags is already enabled, the enableTags shall not do anything.] */
    @Test
    public void toJsonEmptyClassCallTwiceSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();

        // Act
        twinParser.enableTags();

        // Assert
        String json = twinParser.toJson();
        Helpers.assertJson(json, "{\"tags\":{},\"properties\":{\"desired\":{},\"reported\":{}}}");
    }

    /* Tests_SRS_TWINPARSER_21_020: [The enableMetadata shall enable report metadata in Json for the Desired and for the Reported Properties.] */
    @Test
    public void toJsonEmptyClassWithMetadataSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.enableMetadata();

        // Assert
        String json = twinParser.toJson();
        Helpers.assertJson(json, "{\"properties\":{\"desired\":{\"$metadata\":{}},\"reported\":{\"$metadata\":{}}}}");
    }

    /* Tests_SRS_TWINPARSER_21_021: [The updateDesiredProperty shall add all provided properties to the Desired property.] */
    /* Tests_SRS_TWINPARSER_21_156: [A valid `value` shall contains types of boolean, number, string, or object.] */
    @Test
    public void updateDesiredPropertySucceed()
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
        Helpers.assertJson(json, "{\"key1\":\"value1\",\"key2\":1234,\"keyBool\":false,\"keyChar\":\"c\",\"keyString\":\"value3\",\"keyEnum\":\"val1\",\"keyDouble\":1234.456}");
        assertTwin(twinParser, newValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_050: [The getDesiredPropertyMap shall return a map with all desired property key value pairs.] */
    @Test
    public void getDesiredPropertyMapSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        String json = "{\n" +
                "  \"deviceId\": \"149933hgt335\",\n" +
                "  \"etag\": \"AAAA22AAAAE=\",\n" +
                "  \"properties\": {\n" +
                "    \"desired\": {\n" +
                "        \"sensors\": {\n" +
                "          \"sensor0\": \"10\",\n" +
                "          \"sensor1\": \"11\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"reported\": {\n" +
                "        \"sensors\": {\n" +
                "          \"sensor4\": \"4\",\n" +
                "          \"sensor5\": \"5\",\n" +
                "          \"sensor2\": \"2\",\n" +
                "          \"sensor3\": \"3\",\n" +
                "          \"sensor8\": \"8\",\n" +
                "          \"sensor9\": \"9\",\n" +
                "          \"sensor6\": \"6\",\n" +
                "          \"sensor7\": \"7\",\n" +
                "          \"sensor0\": \"0\",\n" +
                "          \"sensor1\": \"1\"\n" +
                "        }\n" +
                "    }\n" +
                "  },\n" +
                "  \"$version\": 2\n" +
                "}";
        twinParser.updateTwin(json);

        // Act
        Map<String, Object> resultValues = twinParser.getDesiredPropertyMap();

        // Assert
        Map<String, Object> expectedValues = new HashMap<String, Object>()
        {
            {
                put("sensors", new HashMap<String, Object>()
                {
                    {
                        put("sensor0", "10");
                        put("sensor1", "11");
                    }
                });
            }
        };
        Helpers.assertMap(resultValues, expectedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_051: [The getReportedPropertyMap shall return a map with all reported property key value pairs.] */
    @Test
    public void getReportedPropertyMapSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        String json = "{\n" +
                "  \"deviceId\": \"149933hgt335\",\n" +
                "  \"etag\": \"AAAA22AAAAE=\",\n" +
                "  \"properties\": {\n" +
                "    \"desired\": {\n" +
                "        \"sensors\": {\n" +
                "          \"sensor0\": \"10\",\n" +
                "          \"sensor1\": \"11\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"reported\": {\n" +
                "        \"sensors\": {\n" +
                "          \"sensor4\": \"4\",\n" +
                "          \"sensor5\": \"5\",\n" +
                "          \"sensor2\": \"2\",\n" +
                "          \"sensor3\": \"3\",\n" +
                "          \"sensor8\": \"8\",\n" +
                "          \"sensor9\": \"9\",\n" +
                "          \"sensor6\": \"6\",\n" +
                "          \"sensor7\": \"7\",\n" +
                "          \"sensor0\": \"0\",\n" +
                "          \"sensor1\": \"1\"\n" +
                "        }\n" +
                "    }\n" +
                "  },\n" +
                "  \"$version\": 2\n" +
                "}";
        twinParser.updateTwin(json);

        // Act
        Map<String, Object> resultValues = twinParser.getReportedPropertyMap();

        // Assert
        Map<String, Object> expectedValues = new HashMap<String, Object>()
        {
            {
                put("sensors", new HashMap<String, Object>()
                {
                    {
                        put("sensor0", "0");
                        put("sensor1", "1");
                        put("sensor2", "2");
                        put("sensor3", "3");
                        put("sensor4", "4");
                        put("sensor5", "5");
                        put("sensor6", "6");
                        put("sensor7", "7");
                        put("sensor8", "8");
                        put("sensor9", "9");
                    }
                });
            }
        };
        Helpers.assertMap(resultValues, expectedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_052: [The getTagsMap shall return a map with all tags in the collection.] */
    @Test
    public void getTagsMapSucceed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        String json = "{\n" +
                "  \"deviceId\": \"149933hgt335\",\n" +
                "  \"etag\": \"AAAA22AAAAE=\",\n" +
                "  \"tags\":{\n" +
                "    \"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}" +
                "  },\n" +
                "  \"properties\": {\n" +
                "    \"desired\": {\n" +
                "    },\n" +
                "    \"reported\": {\n" +
                "    }\n" +
                "  },\n" +
                "  \"$version\": 2\n" +
                "}";
        twinParser.updateTwin(json);

        // Act
        Map<String, Object> resultValues = twinParser.getTagsMap();

        // Assert
        Map<String, Object> expectedValues = new HashMap<String, Object>()
        {
            {
                put("tag1", new HashMap<String, Object>()
                {
                    {
                        put("Key1", "newValue1");
                        put("KEY3", "value3");
                    }
                });
            }
        };
        Helpers.assertMap(resultValues, expectedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_152: [A valid `key` shall not be null.] */
    @Test
    public void updateDesiredPropertyNullKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        oldValues.put("key7", false);
        oldValues.put("key8", 1234.456);
        twinParser.updateDesiredProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, oldValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_153: [A valid `key` shall not be empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredPropertyEmptyKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put("", "value");

        // Act
        twinParser.updateDesiredProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_154: [A valid `key` shall be less than 128 characters long.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredPropertyBigKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(BIG_STRING_150CHARS, "value");

        // Act
        twinParser.updateDesiredProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredPropertyIllegalSpaceKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_SPACE, "value");

        // Act
        twinParser.updateDesiredProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredPropertyIllegalDotKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOT, "value");

        // Act
        twinParser.updateDesiredProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_073: [If the map is invalid, the updateDesiredProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredPropertyIllegalDollarKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOLLAR, "value");

        // Act
        twinParser.updateDesiredProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_156: [A valid `value` shall contains types of boolean, number, string, or object.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredPropertyIllegalValueTypeFailed()
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
        twinParser.updateDesiredProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_158: [A valid `value` shall contains less than 5 levels of sub-maps.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredProperty_6levelsFailed()
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
        twinParser.updateDesiredProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_157: [A valid `value` can contains sub-maps.] */
    /* Tests_SRS_TWINPARSER_21_158: [A valid `value` shall contains less than 5 levels of sub-maps.] */
    @Test
    public void updateDesiredProperty5levelsSucceed()
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
        Helpers.assertJson(json, "{\"key1\":\"value\",\"one\":{\"two\":{\"three\":{\"four\":{\"five\":{\"propertyKey\":\"value\"}}}}}}");
        assertTwin(twinParser, newValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_078: [If any `value` is null, the updateDesiredProperty shall delete it from the collection and report on Json.] */
    @Test
    public void updateDesiredPropertyNullValuesSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", null);
        newValues.put("key2", null);

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":null,\"key2\":null}");
        Map<String, Object> result = twinParser.getDesiredPropertyMap();
        assertNull(result);
    }

    /* Tests_SRS_TWINPARSER_21_078: [If any `value` is null, the updateDesiredProperty shall delete it from the collection and report on Json.] */
    @Test
    public void updateDesiredPropertyDeleteValuesSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", "value2");
        twinParser.updateDesiredProperty(oldValues);
        assertTwin(twinParser, oldValues, null, null);

        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", null);    //delete key1

        oldValues.remove("key1");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":null}");
        assertTwin(twinParser, oldValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_152: [A valid `key` shall not be null.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedPropertyNullKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(null, "value");

        // Act
        twinParser.updateReportedProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_153: [A valid `key` shall not be empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedPropertyEmptyKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put("", "value");

        // Act
        twinParser.updateReportedProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_154: [A valid `key` shall be less than 128 characters long.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedPropertyBigKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(BIG_STRING_150CHARS, "value");

        // Act
        twinParser.updateReportedProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedPropertyIllegalSpaceKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_SPACE, "value");

        // Act
        twinParser.updateReportedProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedPropertyIllegalDotKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOT, "value");

        // Act
        twinParser.updateReportedProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_079: [If the map is invalid, the updateReportedProperty shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedPropertyIllegalDollarKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("validKey", "value");
        newValues.put(ILLEGAL_STRING_DOLLAR, "value");

        // Act
        twinParser.updateReportedProperty(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_084: [If any `value` is null, the updateReportedProperty shall delete it from the collection and report on Json.] */
    @Test
    public void updateReportedPropertyNullValuesSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", null);
        newValues.put("key2", null);

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":null,\"key2\":null}");
        Map<String, Object> result = twinParser.getReportedPropertyMap();
        assertNull(result);
    }

    /* Tests_SRS_TWINPARSER_21_084: [If any `value` is null, the updateReportedProperty shall delete it from the collection and report on Json.] */
    @Test
    public void updateReportedPropertyDeleteValuesSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", "value2");
        twinParser.updateReportedProperty(oldValues);
        assertTwin(twinParser, null, oldValues, null);

        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", null);    //delete key1

        oldValues.remove("key1");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":null}");
        assertTwin(twinParser, null, oldValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_021: [The updateDesiredProperty shall add all provided properties to the Desired property.] */
    @Test
    public void updateDesiredPropertyWithMetadataSucceed()
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
        Helpers.assertJson(json, "{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}");
        assertTwin(twinParser, newValues, null, null);
        TwinProperties resultProperties = Deencapsulation.getField(twinParser, "properties");
        TwinProperty resultDesired = Deencapsulation.getField(resultProperties, "desired");
        TwinMetadata resultMetadataKey1 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key1");
        TwinMetadata resultMetadataKey2 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key2");
        TwinMetadata resultMetadataKey3 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key3");

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
    }

    /* Tests_SRS_TWINPARSER_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredPropertyOnlyMetadataChangesSucceed()
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
        Helpers.assertJson(json, "{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}");
        assertTwin(twinParser, newValues, null, null);
        TwinProperties resultProperties = Deencapsulation.getField(twinParser, "properties");
        TwinProperty resultDesired = Deencapsulation.getField(resultProperties, "desired");
        TwinMetadata resultMetadataKey1 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key1");
        TwinMetadata resultMetadataKey2 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key2");
        TwinMetadata resultMetadataKey3 = (TwinMetadata)Deencapsulation.invoke(resultDesired, "getMetadata", "key3");

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
    }

    /* Tests_SRS_TWINPARSER_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredPropertyNewKeySucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateDesiredProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key4", "value4");

        oldValues.put("key4", "value4");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key4\":\"value4\"}");
        assertTwin(twinParser, oldValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    /* Tests_SRS_TWINPARSER_21_059: [The updateDesiredProperty shall only change properties in the map, keep the others as is.] */
    /* Tests_SRS_TWINPARSER_21_077: [If any `key` already exists, the updateDesiredProperty shall replace the existed value by the new one.] */
    @Test
    public void updateDesiredPropertyNewValueSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateDesiredProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value4");
        oldValues.put("key1", "value4");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":\"value4\"}");
        assertTwin(twinParser, oldValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_061: [All `key` and `value` in property shall be case sensitive.] */
    @Test
    public void updateDesiredPropertyCaseSensitiveSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateDesiredProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value4");
        newValues.put("kEy1", "value1");
        oldValues.put("key1", "value4");
        oldValues.put("kEy1", "value1");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":\"value4\",\"kEy1\":\"value1\"}");
        assertTwin(twinParser, oldValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_063: [If the provided `property` map is empty, the updateDesiredProperty shall not change the collection and return null.] */
    @Test
    public void updateDesiredPropertyUpdateExistedValuesWithEmptyMapSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateDesiredProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        assertNull(json);
        assertTwin(twinParser, oldValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredPropertyNewAndOldSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateDesiredProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key5", "value5");
        oldValues.put("key1", "value4");
        oldValues.put("key5", "value5");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":\"value4\",\"key5\":\"value5\"}");
        assertTwin(twinParser, oldValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_022: [The updateDesiredProperty shall return a string with json representing the desired properties with changes.] */
    @Test
    public void updateDesiredPropertyMixDesiredAndReportedSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> reportedValues = new HashMap<>();
        reportedValues.put("key1", "value1");
        reportedValues.put("key2", 1234);
        reportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(reportedValues);
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key6", "value6");
        oldValues.put("key7", true);
        twinParser.updateDesiredProperty(oldValues);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value4");
        newValues.put("key6", "value6");
        newValues.put("key5", "value5");
        oldValues.put("key1", "value4");
        oldValues.put("key5", "value5");

        // Act
        String json = twinParser.updateDesiredProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":\"value4\",\"key5\":\"value5\"}");
        assertTwin(twinParser, oldValues, reportedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_023: [If the provided `property` map is null, the updateDesiredProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredPropertyNullMapFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.updateDesiredProperty((Map)null);
    }

    /* Tests_SRS_TWINPARSER_21_024: [If no Desired property changed its value, the updateDesiredProperty shall return null.] */
    @Test
    public void updateDesiredPropertyEmptyMapSucceed()
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
    public void updateDesiredPropertyNoChangesSucceed()
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
        assertTwin(twinParser, newValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_120: [The resetDesiredProperty shall cleanup the desired collection and add all provided properties to the Desired property.] */
    /* Tests_SRS_TWINPARSER_21_121: [The resetDesiredProperty shall return a string with json representing the added desired properties.] */
    @Test
    public void resetDesiredPropertyNewAndOldSucceed()
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
        Helpers.assertJson(json, "{\"key1\":\"value4\",\"key2\":1234,\"key5\":\"value5\"}");
        assertTwin(twinParser, newValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_122: [If the provided `propertyMap` is null, the resetDesiredProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void resetDesiredPropertyNullSucceed()
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
        assertTwin(twinParser, newValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_123: [The `key` and `value` in property shall be case sensitive.] */
    @Test
    public void resetDesiredPropertyCaseSensitiveSucceed()
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
        Helpers.assertJson(json, "{\"key1\":\"vAlUE1\",\"key2\":1234,\"kEy1\":\"value5\"}");
        assertTwin(twinParser, newValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_124: [If the provided `propertyMap` is empty, the resetDesiredProperty shall cleanup the desired collection and return `{}`.] */
    @Test
    public void resetDesiredPropertyEmptySucceed()
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
        Helpers.assertJson(json, "{}");
        assertNull(twinParser.getDesiredPropertyMap());
    }

    /* Tests_SRS_TWINPARSER_21_125: [If the map is invalid, the resetDesiredProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void resetDesiredPropertyInvalidMapFailed()
    {
        // Arrange
        class Bar
        {
            public int intFoo = 10;
            public String strFoo;
        }

        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateDesiredProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, oldValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_129: [If any `value` is null, the resetDesiredProperty shall delete it from the collection and report on Json.] */
    @Test
    public void resetDesiredPropertyValueNullSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateDesiredProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value4");
        newValues.put("key2", null);
        newValues.put("key5", null);

        // Act
        String json = twinParser.resetDesiredProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":\"value4\",\"key2\":null,\"key5\":null}");
        newValues.remove("key2");
        newValues.remove("key5");
        assertTwin(twinParser, newValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_130: [The resetReportedProperty shall cleanup the reported collection and add all provided properties to the Reported property.] */
    /* Tests_SRS_TWINPARSER_21_131: [The resetReportedProperty shall return a string with json representing the added reported properties.] */
    @Test
    public void resetReportedPropertyNewAndOldSucceed()
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
        Helpers.assertJson(json, "{\"key1\":\"value4\",\"key2\":1234,\"key5\":\"value5\"}");
        assertTwin(twinParser, null, newValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_132: [If the provided `propertyMap` is null, the resetReportedProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void resetReportedPropertyNullSucceed()
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
        assertTwin(twinParser, null, newValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_133: [The `key` and `value` in property shall be case sensitive.] */
    @Test
    public void resetReportedPropertyCaseSensitiveSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "vAlUE1");
        newValues.put("key2", 1234);
        newValues.put("kEy1", "value5");

        // Act
        String json = twinParser.resetReportedProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":\"vAlUE1\",\"key2\":1234,\"kEy1\":\"value5\"}");
        assertTwin(twinParser, null, newValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_134: [If the provided `propertyMap` is empty, the resetReportedProperty shall cleanup the reported collection and return `{}`.] */
    @Test
    public void resetReportedPropertyEmptySucceed()
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
        Helpers.assertJson(json, "{}");
        assertNull(twinParser.getReportedPropertyMap());
    }

    /* Tests_SRS_TWINPARSER_21_135: [If the map is invalid, the resetReportedProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void resetReportedPropertyInvalidMapFailed()
    {
        // Arrange
        class Bar
        {
            int intFoo = 10;
            String strFoo;
        }

        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, oldValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_139: [If any `value` is null, the resetReportedProperty shall delete it from the collection and report on Json.] */
    @Test
    public void resetReportedPropertyValueNullSucceed()
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
        Helpers.assertJson(json, "{\"key1\":\"value4\",\"key2\":null,\"key5\":null} ");
        newValues.remove("key2");
        newValues.remove("key5");
        assertTwin(twinParser, null, newValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_025: [The updateReportedProperty shall add all provided properties to the Reported property.] */
    @Test
    public void updateReportedPropertySucceed()
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
        Helpers.assertJson(json, "{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}");
        assertTwin(twinParser, null, newValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_026: [The updateReportedProperty shall return a string with json representing the Reported properties with changes.] */
    /* Tests_SRS_TWINPARSER_21_060: [The updateReportedProperty shall only change properties in the map, keep the others as is.] */
    /* Tests_SRS_TWINPARSER_21_083: [If any `key` already exists, the updateReportedProperty shall replace the existed value by the new one.] */
    @Test
    public void updateReportedPropertyNewAndOldSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", 898989);
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", 7654);
        newValues.put("key2", 1234);
        newValues.put("key5", "value5");
        oldValues.put("key1", 7654);
        oldValues.put("key5", "value5");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":7654,\"key5\":\"value5\"}");
        assertTwin(twinParser, null, oldValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_062: [All `key` and `value` in property shall be case sensitive.] */
    @Test
    public void updateReportedPropertyCaseSensitiveSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value4");
        newValues.put("kEy1", "value1");
        oldValues.put("key1", "value4");
        oldValues.put("kEy1", "value1");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":\"value4\",\"kEy1\":\"value1\"}");
        assertTwin(twinParser, null, oldValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_064: [If the provided `property` map is empty, the updateReportedProperty shall not change the collection and return null.] */
    @Test
    public void updateReportedPropertyEmptyMapSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldValues);

        Map<String, Object> newValues = new HashMap<>();

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        assertNull(json);
        assertTwin(twinParser, null, oldValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_026: [The updateReportedProperty shall return a string with json representing the Reported properties with changes.] */
    @Test
    public void updateReportedPropertyMixDesiredAndReportedSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("key1", "value1");
        oldValues.put("key2", 1234);
        oldValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldValues);
        Map<String, Object> desiredValues = new HashMap<>();
        desiredValues.put("key1", "value4");
        desiredValues.put("key6", "value6");
        desiredValues.put("key7", "value7");
        twinParser.updateDesiredProperty(desiredValues);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value4");
        newValues.put("key2", 1234);
        newValues.put("key5", "value5");
        oldValues.put("key1", "value4");
        oldValues.put("key5", "value5");

        // Act
        String json = twinParser.updateReportedProperty(newValues);

        // Assert
        Helpers.assertJson(json, "{\"key1\":\"value4\",\"key5\":\"value5\"}");
        assertTwin(twinParser, desiredValues, oldValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_027: [If the provided `property` map is null, the updateReportedProperty shall not change the collection and throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateReportedPropertyNullMapFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.updateReportedProperty((Map)null);
    }

    /* Tests_SRS_TWINPARSER_21_028: [If no Reported property changed its value, the updateReportedProperty shall return null.] */
    @Test
    public void updateReportedPropertyEmptyMapFailed()
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
    public void updateReportedPropertyJsonEmptyClassSucceed()
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
    public void updateReportedPropertyJsonMissingCommaFailed()
    {
        // Arrange
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser();
        twinParser.setReportedCallback(onReportedCallback);

        String json = "{\"key1\":\"value1\"\"key2\":1234,\"key3\":\"value3\"}";

        // Act
        twinParser.updateReportedProperty(json);
    }

    /* Tests_SRS_TWINPARSER_21_034: [The updateReportedProperty shall update the Reported property using the information provided in the json.] */
    /* Tests_SRS_TWINPARSER_21_035: [The updateReportedProperty shall generate a map with all pairs key value that had its content changed.] */
    /* Tests_SRS_TWINPARSER_21_036: [The updateReportedProperty shall send the map with all changed pairs to the upper layer calling onReportedCallback (TwinChangedCallback).] */
    @Test
    public void updateReportedPropertyJsonMixDesiredAndReportedSucceed()
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
    public void updateReportedPropertyJsonNoCallbackEmptyClassSucceed()
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
    public void updateReportedPropertyJsonNoChangesSucceed()
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
    public void updateReportedPropertyJsonDuplicatedKeyFailed()
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
    public void updateReportedPropertyJsonEmptySucceed()
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
    public void updateReportedPropertyJsonNullSucceed()
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
        assertTwin(twinParser, null, newValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_005: [The constructor shall call the standard constructor.] */
    /* Tests_SRS_TWINPARSER_21_007: [The constructor shall set OnReportedCallback as null.] */
    /* Tests_SRS_TWINPARSER_21_008: [The constructor shall set Tags as null.] */
    /* Tests_SRS_TWINPARSER_21_006: [The constructor shall set OnDesiredCallback with the provided Callback function.] */
    /* Tests_SRS_TWINPARSER_21_029: [The updateDesiredProperty shall update the Desired property using the information provided in the json.] */
    /* Tests_SRS_TWINPARSER_21_030: [The updateDesiredProperty shall generate a map with all pairs key value that had its content changed.] */
    /* Tests_SRS_TWINPARSER_21_031: [The updateDesiredProperty shall send the map with all changed pairs to the upper layer calling onDesiredCallback (TwinChangedCallback).] */
    @Test
    public void updateDesiredPropertyJsonEmptyClassSucceed()
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
    public void updateDesiredPropertyJsonMixDesiredAndProvidedSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> reportedValues = new HashMap<>();
        reportedValues.put("key1", "value1");
        reportedValues.put("key2", 1234);
        reportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(reportedValues);
        Map<String, Object> newValues = new HashMap<>();
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
        newValues.put("key2", 4321.0);
        newValues.put("key5", "value5");
        assertTwin(twinParser, newValues, reportedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_096: [If the provided json have any duplicated `key`, the updateDesiredProperty shall throws IllegalArgumentException.] */
    @Test
    public void updateDesiredPropertyJsonDuplicatedKeySucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> reportedValues = new HashMap<>();
        reportedValues.put("key1", "value1");
        reportedValues.put("key2", 1234);
        reportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(reportedValues);
        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, newValues, reportedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_032: [If the OnDesiredCallback is set as null, the updateDesiredProperty shall discard the map with the changed pairs.] */
    @Test
    public void updateDesiredPropertyJsonNoCallbackEmptyClassSucceed()
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
    public void updateDesiredPropertyJsonNoChangesSucceed()
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

        assertTwin(twinParser, newValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_092: [If the provided json is not valid, the updateDesiredProperty shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateDesiredPropertyJsonMissingCommaFailed()
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
    }

    /* Tests_SRS_TWINPARSER_21_065: [If the provided json is empty, the updateDesiredProperty shall not change the collection and not call the OnDesiredCallback.] */
    @Test
    public void updateDesiredPropertyJsonEmptySucceed()
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

        assertTwin(twinParser, newValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_066: [If the provided json is null, the updateDesiredProperty shall not change the collection, not call the OnDesiredCallback, and throws IllegalArgumentException.] */
    @Test
    public void updateDesiredPropertyJsonNullFailed()
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

        assertTwin(twinParser, newValues, null, null);
    }

    /* Tests_SRS_TWINPARSER_21_159: [The updateDeviceManager shall replace the `deviceId` by the provided one.] */
    /* Tests_SRS_TWINPARSER_21_166: [The updateDeviceManager shall return a json with the new device management information.] */
    @Test
    public void updateDeviceManagerEmptyClassSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        String json = twinParser.updateDeviceManager("DeviceName", null, null);

        // Assert
        Helpers.assertJson(json, 
                "{" +
                        "\"deviceId\":\"DeviceName\"," +
                        "\"properties\":{" +
                                "\"desired\":{}," +
                                "\"reported\":{}" +
                        "}" +
                "}");

        assertThat(twinParser.getDeviceId(), is("DeviceName"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertNull(twinParser.getVersion());
        assertNull(twinParser.getStatus());
        assertNull(twinParser.getStatusReason());
        assertNull(twinParser.getStatusUpdatedTime());
        assertNull(twinParser.getConnectionState());
        assertNull(twinParser.getConnectionStateUpdatedTime());
        assertNull(twinParser.getLastActivityTime());
    }

    /* Tests_SRS_TWINPARSER_21_166: [The updateDeviceManager shall return a json with the new device management information.] */
    @Test
    public void updateDeviceManagerChangeDeviceIdSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.updateDeviceManager("DeviceName", null, null);

        // Act
        String json = twinParser.updateDeviceManager("Device_Name", null, null);

        // Assert
        Helpers.assertJson(json, 
                "{" +
                        "\"deviceId\":\"Device_Name\"," +
                        "\"properties\":{" +
                        "\"desired\":{}," +
                        "\"reported\":{}" +
                        "}" +
                        "}");

        assertThat(twinParser.getDeviceId(), is("Device_Name"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertNull(twinParser.getVersion());
        assertNull(twinParser.getStatus());
        assertNull(twinParser.getStatusReason());
        assertNull(twinParser.getStatusUpdatedTime());
        assertNull(twinParser.getConnectionState());
        assertNull(twinParser.getConnectionStateUpdatedTime());
        assertNull(twinParser.getLastActivityTime());
    }

    /* Tests_SRS_TWINPARSER_21_167: [If nothing change in the management collection, The updateDeviceManager shall return null.] */
    @Test
    public void updateDeviceManagerNoChangesSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.updateDeviceManager("DeviceName", null, null);

        // Act
        String json = twinParser.updateDeviceManager("DeviceName", null, null);

        // Assert
        assertNull(json);

        assertThat(twinParser.getDeviceId(), is("DeviceName"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertNull(twinParser.getVersion());
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
    public void updateDeviceManagerNewStatusSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        String json = twinParser.updateDeviceManager("DeviceName", TwinStatus.disabled, "starting system");

        // Assert
        assertThat(twinParser.getDeviceId(), is("DeviceName"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertNull(twinParser.getVersion());
        assertThat(twinParser.getStatus(), is(TwinStatus.disabled));
        assertThat(twinParser.getStatusReason(), is("starting system"));
        assertNull(twinParser.getStatusUpdatedTime());
        assertNull(twinParser.getConnectionState());
        assertNull(twinParser.getConnectionStateUpdatedTime());
        assertNull(twinParser.getLastActivityTime());

        TwinParser resultTwinParser = new TwinParser();
        resultTwinParser.updateTwin(json);
        assertThat(resultTwinParser.getDeviceId(), is("DeviceName"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertNull(twinParser.getVersion());
        assertThat(resultTwinParser.getStatus(), is(TwinStatus.disabled));
        assertThat(resultTwinParser.getStatusReason(), is("starting system"));
        assertNull(resultTwinParser.getStatusUpdatedTime());
    }

    /* Tests_SRS_TWINPARSER_21_165: [If the provided `status` is different than the previous one, and the `statusReason` is null, The updateDeviceManager shall throw IllegalArgumentException.] */
    @Test
    public void updateDeviceManagerNewStatusNoReasonFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.updateDeviceManager("DeviceName", TwinStatus.disabled, "starting system");

        // Act
        try
        {
            twinParser.updateDeviceManager("DeviceName", TwinStatus.enabled, null);
            assert true;
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything. Throw expected.
        }

        // Assert
        assertThat(twinParser.getDeviceId(), is("DeviceName"));
        assertNull(twinParser.getGenerationId());
        assertNull(twinParser.getETag());
        assertNull(twinParser.getVersion());
        assertThat(twinParser.getStatus(), is(TwinStatus.disabled));
        assertThat(twinParser.getStatusReason(), is("starting system"));
        assertNull(twinParser.getStatusUpdatedTime());
        assertNull(twinParser.getConnectionState());
        assertNull(twinParser.getConnectionStateUpdatedTime());
        assertNull(twinParser.getLastActivityTime());
    }

    /* Tests_SRS_TWINPARSER_21_116: [The updateTwin shall add all provided properties and tags to the collection.] */
    /* Tests_SRS_TWINPARSER_21_117: [The updateTwin shall return a string with json representing the properties and tags with changes.] */
    /* Tests_SRS_TWINPARSER_21_082: [If any `value` is null, the updateTwin shall delete it from the collection and report on Json.] */
    @Test
    public void updateTwinEmptyClassSucceed() throws IOException
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
        Helpers.assertJson(json, "{\"tags\":{" +
                "\"tag1\":{\"KeyChar\":\"c\",\"KeyBool\":true,\"keyString\":\"value1\",\"keyEnum\":\"val1\",\"keyDouble\":1234.456}}," +
                "\"properties\":{" +
                    "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}," +
                    "\"reported\":{\"key1\":\"value1\",\"key2\":null,\"key3\":\"value3\"}}}");

        newReportedValues.remove("key2");
        assertTwin(twinParser, newDesiredValues, newReportedValues, newTagsValues);
    }

    /* Tests_SRS_TWINPARSER_21_081: [If any `key` already exists, the updateTwin shall replace the existed value by the new one.] */
    /* Tests_SRS_TWINPARSER_21_126: [The updateTwin shall only change properties and tags in the map, keep the others as is.] */
    /* Tests_SRS_TWINPARSER_21_127: [The `key` and `value` in the maps shall be case sensitive.] */
    @Test
    public void updateTwinChangeKeysAndValuesSucceed() throws IOException
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
        Helpers.assertJson(json, "{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                    "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                    "\"reported\":{\"key1\":\"value 10.\",\"key3\":\"VALUE3\"}}}");

        newDesiredValues.put("key7", true);
        ((Map<String, Object>)newTagsValues.get("tag1")).put("Key2", true);
        assertTwin(twinParser, newDesiredValues, newReportedValues, newTagsValues);
    }

    /* Tests_SRS_TWINPARSER_21_118: [If one of the provided map is null, the updateTwin shall not change that part of the collection.] */
    @Test
    public void updateTwinNullTagsSucceed() throws IOException
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
        Helpers.assertJson(json, 
                "{\"tags\":{}," +
                        "\"properties\":{" +
                            "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                            "\"reported\":{\"key1\":\"value10\",\"key3\":\"VALUE3\"}}}");

        newDesiredValues.put("key7", true);
        assertTwin(twinParser, newDesiredValues, newReportedValues, newValues);
    }


    @Test
    public void updateTwinNonNullTagsSucceed() throws IOException
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
        newTagsValues.put("tag2", "newValue2");

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);

        // Assert
        Helpers.assertJson(json, 
                "{\"tags\":{\"tag2\":\"newValue2\"}," +
                        "\"properties\":{" +
                        "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                        "\"reported\":{\"key1\":\"value10\",\"key3\":\"VALUE3\"}}}");

        newDesiredValues.put("key7", true);
        newValues.put("tag2", "newValue2");
        assertTwin(twinParser, newDesiredValues, newReportedValues, newValues);
    }

    /* Tests_SRS_TWINPARSER_21_118: [If one of the provided map is null, the updateTwin shall not change that part of the collection.] */
    @Test
    public void updateTwinNullDesiredSucceed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("key1", "value1");
        newValues.put("key2", 1234);
        newValues.put("key3", "value3");
        twinParser.updateReportedProperty(newValues);
        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(newDesiredValues);
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
        Helpers.assertJson(json, "{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{\"key1\":\"value10\",\"key3\":\"VALUE3\"}}}");

        ((Map<String, Object>)newValues.get("tag1")).put("Key1", "newValue1");
        ((Map<String, Object>)newValues.get("tag1")).put("KEY3", "value3");
        assertTwin(twinParser, newDesiredValues, newReportedValues, newValues);
    }

    /* Tests_SRS_TWINPARSER_21_118: [If one of the provided map is null, the updateTwin shall not change that part of the collection.] */
    @Test
    public void updateTwinNullReportedSucceed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value1");
        newReportedValues.put("key2", 1234);
        newReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(newReportedValues);
        Map<String, Object> newValues = new HashMap<>();
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
        Helpers.assertJson(json, "{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                "\"reported\":{}}}");

        newDesiredValues.put("key7", true);
        ((Map<String, Object>)newValues.get("tag1")).put("Key1", "newValue1");
        ((Map<String, Object>)newValues.get("tag1")).put("KEY3", "value3");
        assertTwin(twinParser, newDesiredValues, newReportedValues, newValues);
    }

    /* Tests_SRS_TWINPARSER_21_118: [If one of the provided map is null, the updateTwin shall not change that part of the collection.] */
    @Test
    public void updateTwinNullDesiredAndReportedSucceed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value1");
        newReportedValues.put("key2", 1234);
        newReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(newReportedValues);
        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(newDesiredValues);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        String json = twinParser.updateTwin(null, null, newTagsValues);

        // Assert
        Helpers.assertJson(json, "{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{}}}");

        ((Map<String, Object>)newValues.get("tag1")).put("Key1", "newValue1");
        ((Map<String, Object>)newValues.get("tag1")).put("KEY3", "value3");
        assertTwin(twinParser, newDesiredValues, newReportedValues, newValues);
    }

    /* Tests_SRS_TWINPARSER_21_160: [If all of the provided map is null, the updateTwin shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTwinAllNullFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value1");
        newReportedValues.put("key2", 1234);
        newReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(newReportedValues);
        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(newDesiredValues);
        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, newDesiredValues, newReportedValues, newValues);
    }

    /* Tests_SRS_TWINPARSER_21_119: [If no property or tags changed its value, the updateTwin shall return null.] */
    @Test
    public void updateTwinNoChangesSucceed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);
        Map<String, Object> oldTagsValues = new HashMap<>();
        oldTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(oldTagsValues);

        Map<String, Object> newReportedValues = new HashMap<>();
        newReportedValues.put("key1", "value1");
        newReportedValues.put("key3", "value3");
        Map<String, Object> newDesiredValues = new HashMap<>();
        newDesiredValues.put("key7", true);
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key3", "value3"); }});

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);

        // Assert
        assertNull(json);
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, oldTagsValues);
    }

    /* Tests_SRS_TWINPARSER_21_128: [If one of the provided map is empty, the updateTwin shall not change its the collection.] */
    @Test
    public void updateTwinEmptyTagsSucceed() throws IOException
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
        Helpers.assertJson(json, 
                "{\"tags\":{}," +
                        "\"properties\":{" +
                        "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                        "\"reported\":{\"key1\":\"value10\",\"key3\":\"VALUE3\"}}}");
        newDesiredValues.put("key7", true);
        assertTwin(twinParser, newDesiredValues, newReportedValues, newValues);
    }

    /* Tests_SRS_TWINPARSER_21_128: [If one of the provided map is empty, the updateTwin shall not change its the collection.] */
    @Test
    public void updateTwinEmptyDesiredSucceed() throws IOException
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
        Helpers.assertJson(json, "{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{\"key1\":\"value10\",\"key3\":\"VALUE3\"}}}");
        newDesiredValues.put("key7", true);
        ((Map<String, Object>)newValues.get("tag1")).put("Key1", "newValue1");
        ((Map<String, Object>)newValues.get("tag1")).put("KEY3", "value3");
        assertTwin(twinParser, newDesiredValues, newReportedValues, newValues);
    }

    /* Tests_SRS_TWINPARSER_21_128: [If one of the provided map is empty, the updateTwin shall not change its the collection.] */
    @Test
    public void updateTwinEmptyReportedSucceed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> newValues = new HashMap<>();
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
        Helpers.assertJson(json, "{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{\"key1\":\"newValue1\",\"key3\":\"value30\"}," +
                "\"reported\":{}}}");
        newDesiredValues.put("key7", true);
        ((Map<String, Object>)newValues.get("tag1")).put("Key1", "newValue1");
        ((Map<String, Object>)newValues.get("tag1")).put("KEY3", "value3");
        assertTwin(twinParser, newDesiredValues, oldReportedValues, newValues);
    }

    /* Tests_SRS_TWINPARSER_21_128: [If one of the provided map is empty, the updateTwin shall not change its the collection.] */
    @Test
    public void updateTwinEmptyDesiredAndReportedSucceed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(newValues);

        Map<String, Object> newDesiredValues = new HashMap<>();
        Map<String, Object> newReportedValues = new HashMap<>();
        Map<String, Object> newTagsValues = new HashMap<>();
        newTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "newValue1"); put("Key3", "value3");  put("KEY3", "value3"); }});

        // Act
        String json = twinParser.updateTwin(newDesiredValues, newReportedValues, newTagsValues);

        // Assert
        Helpers.assertJson(json, "{\"tags\":{" +
                "\"tag1\":{\"Key1\":\"newValue1\",\"KEY3\":\"value3\"}}," +
                "\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{}}}");
        ((Map<String, Object>)newValues.get("tag1")).put("Key1", "newValue1");
        ((Map<String, Object>)newValues.get("tag1")).put("KEY3", "value3");
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, newValues);
    }

    /* Tests_SRS_TWINPARSER_21_080: [If one of the maps is invalid, the updateTwin shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTwinInvalidValueFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);
        Map<String, Object> oldTagsValues = new HashMap<>();
        oldTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(oldTagsValues);

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
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, oldTagsValues);
    }

    /* Tests_SRS_TWINPARSER_21_080: [If one of the maps is invalid, the updateTwin shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTwinInvalidDotKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);
        Map<String, Object> oldTagsValues = new HashMap<>();
        oldTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(oldTagsValues);

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
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, oldTagsValues);
    }

    /* Tests_SRS_TWINPARSER_21_080: [If one of the maps is invalid, the updateTwin shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTwinInvalidDollarKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);
        Map<String, Object> oldTagsValues = new HashMap<>();
        oldTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(oldTagsValues);

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
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, oldTagsValues);
    }

    /* Tests_SRS_TWINPARSER_21_080: [If one of the maps is invalid, the updateTwin shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTwinInvalidSpaceKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);
        Map<String, Object> oldTagsValues = new HashMap<>();
        oldTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(oldTagsValues);

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
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, oldTagsValues);
    }

    /* Tests_SRS_TWINPARSER_21_075: [If Tags is not enable and `tagsMap` is not null, the updateTwin shall throw IOException.] */
    @Test
    public void updateTwinTagsMapTagDisabledFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);

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
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, null);
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
    public void updateTwinJsonEmptyClassNoMetadataSucceed()
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
    public void updateTwinJsonEmptyClass_PropertyOnlyJsonStartDesiredSucceed()
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
    public void updateTwinJsonEmptyClass_PropertyOnlyJsonStartReportedSucceed()
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
    public void updateTwinJsonEmptyClass_PropertyOnlyJsonWithPropertiesFailed()
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
    public void updateTwinJsonEmptyClass_FullTwinJsonWithDesiredFailed()
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
    public void updateTwinJsonEmptyClassNoCallbackSucceed()
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
        Helpers.assertJson(resultJson, json);
    }

    /* Tests_SRS_TWINPARSER_21_069: [If there is no change in the Desired property, the updateTwin shall not change the reported collection and not call the OnReportedCallback.] */
    @Test
    public void updateTwinJsonEmptyClassNoChangeOnDesiredSucceed()
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
        Helpers.assertJson(resultJson, json);
    }

    /* Tests_SRS_TWINPARSER_21_069: [If there is no change in the Desired property, the updateTwin shall not change the reported collection and not call the OnReportedCallback.] */
    @Test
    public void updateTwinJsonEmptyClassNoDesiredSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        String resultJson = twinParser.toJson();
        Helpers.assertJson(resultJson, "{\"properties\":{" +
                "\"desired\":{}," +
                "\"reported\":{\"key1\":\"value1\",\"key2\":1234.124,\"key5\":\"value5\",\"key7\":true}}}");
    }

    /* Tests_SRS_TWINPARSER_21_070: [If there is no change in the Reported property, the updateTwin shall not change the reported collection and not call the OnReportedCallback.] */
    @Test
    public void updateTwinJsonEmptyClassNoChangeOnReportedSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{\"desired\":{\"key1\":\"value1\",\"key2\":1234.0,\"key3\":\"value3\"},\"reported\":{}}}";

        // Act
        twinParser.updateTwin(json);

        // Assert
        String resultJson = twinParser.toJson();
        Helpers.assertJson(resultJson, json);
    }

    /* Tests_SRS_TWINPARSER_21_071: [If the provided json is empty, the updateTwin shall not change the collection and not call the OnDesiredCallback or the OnReportedCallback.] */
    @Test
    public void updateTwinJsonEmptySucceed()
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
    public void updateTwinJsonNullSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.updateTwin(null);

        // Assert
    }

    /* Tests_SRS_TWINPARSER_21_172: [If the provided json contains `deviceId`, `generationId`, `etag`, `status`, `statusReason`, `statusUpdatedTime`, `connectionState`, `connectionStateUpdatedTime`, `lastActivityTime`, and `lastAcceptingIpFilterRule`, the updateTwin shall store its value.] */
    /* Tests_SRS_TWINPARSER_21_112: [The `getDeviceId` shall return the DeviceName.] */
    /* Tests_SRS_TWINPARSER_21_150: [The `getGenerationId` shall return the device generation name.] */
    /* Tests_SRS_TWINPARSER_21_113: [The `getETag` shall return the string representing a weak ETAG version.] */
    /* Tests_SRS_TWINPARSER_21_173: [The `getVersion` shall return the Integer representing a twin version.] */
    /* Tests_SRS_TWINPARSER_21_136: [The `getStatus` shall return the device status.] */
    /* Tests_SRS_TWINPARSER_21_137: [The `getStatusReason` shall return the device status reason.] */
    /* Tests_SRS_TWINPARSER_21_138: [The `getStatusUpdatedTime` shall return the device status update date and time.] */
    /* Tests_SRS_TWINPARSER_21_147: [The `getConnectionState` shall return the connection state.] */
    /* Tests_SRS_TWINPARSER_21_148: [The `getConnectionStateUpdatedTime` shall return the connection state update date and time.] */
    /* Tests_SRS_TWINPARSER_21_151: [The `getLastActivityTime` shall return the last activity date and time.] */
    @Test
    public void updateTwinJsonEmptyClassManagerParametersWithPropertiesSucceed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);

        String json =
                "{" +
                    "\"deviceId\":\"DeviceName\"," +
                    "\"generationId\":\"generation name\"," +
                    "\"etag\":\"AAAAAAAAAAU=\"," +
                    "\"version\":13," +
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
        assertThat(twinParser.getDeviceId(), is("DeviceName"));
        assertThat(twinParser.getGenerationId(), is("generation name"));
        assertThat(twinParser.getETag(), is("AAAAAAAAAAU="));
        assertThat(twinParser.getVersion(), is(13));
        assertThat(twinParser.getStatus(), is(TwinStatus.enabled));
        assertThat(twinParser.getStatusReason(), is("because it is not disabled"));
        assertThat(twinParser.getStatusUpdatedTime(), is("2015-02-28T16:24:48.789Z"));
        assertThat(twinParser.getConnectionState(), is(TwinConnectionState.connected));
        assertThat(twinParser.getConnectionStateUpdatedTime(), is("2015-02-28T16:24:48.789Z"));
        assertThat(twinParser.getLastActivityTime(), is("2017-02-16T21:59:56.631406Z"));

        Helpers.assertJson(twinParser.toJson(), json);
    }

    @Test
    public void updateTwinJsonEmptyClassManagerParametersNoPropertiesSucceed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);

        String json =
                "{" +
                    "\"deviceId\":\"DeviceName\"," +
                    "\"generationId\":\"generation name\"," +
                    "\"etag\":\"AAAAAAAAAAU=\"," +
                    "\"version\":0," +
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
        assertThat(twinParser.getDeviceId(), is("DeviceName"));
        assertThat(twinParser.getGenerationId(), is("generation name"));
        assertThat(twinParser.getETag(), is("AAAAAAAAAAU="));
        assertThat(twinParser.getVersion(), is(0));
        assertThat(twinParser.getStatus(), is(TwinStatus.enabled));
        assertThat(twinParser.getStatusReason(), is("because it is not disabled"));
        assertThat(twinParser.getStatusUpdatedTime(), is("2015-02-28T16:24:48.789Z"));
        assertThat(twinParser.getConnectionState(), is(TwinConnectionState.connected));
        assertThat(twinParser.getConnectionStateUpdatedTime(), is("2015-02-28T16:24:48.789Z"));
        assertThat(twinParser.getLastActivityTime(), is("2017-02-16T21:59:56.631406Z"));

        Helpers.assertJson(twinParser.toJson(), json);
    }

    /* Tests_SRS_TWINPARSER_21_039: [The updateTwin shall fill the fields the properties in the TwinParser class with the keys and values provided in the json string.] */
    /* Tests_SRS_TWINPARSER_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
    /* Tests_SRS_TWINPARSER_21_048: [The getDesiredPropertyVersion shall return the desired property version.] */
    /* Tests_SRS_TWINPARSER_21_049: [The getReportedPropertyVersion shall return the reported property version.] */
    @Test
    public void updateTwinJsonEmptyClassWithFullMetadataAndTagsSucceed()
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

        assertThat(twinParser.getDesiredPropertyVersion(), is(3));
        assertThat(twinParser.getReportedPropertyVersion(), is(5));

        String resultJson = twinParser.toJson();
        Helpers.assertJson(resultJson, json);
    }

    /* Tests_SRS_TWINPARSER_21_172: [If the provided json contains `deviceId`, `generationId`, `etag`, `status`, `statusReason`, `statusUpdatedTime`, `connectionState`, `connectionStateUpdatedTime`, `lastActivityTime`, and `lastAcceptingIpFilterRule`, the updateTwin shall store its value.] */
    @Test
    public void updateTwinJsonEmptyClassServiceRealCaseSucceed()
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
                    "\"version\":2," +
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
                                "\"reportedMaxSpeed\":100," +
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
                                    "\"reportedMaxSpeed\":" +
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
        assertThat(Double.parseDouble(innerMap.get("reportedMaxSpeed").toString()), is(100.0));
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
        assertThat(twinParser.getVersion(), is(2));

        // TODO: Test disabled with bug.
/*
        String resultJson = twinParser.toJson();
        Helpers.assertJson(resultJson, json);
*/
    }

    /* Tests_SRS_TWINPARSER_21_039: [The updateTwin shall fill the fields the properties in the TwinParser class with the keys and values provided in the json string.] */
    /* Tests_SRS_TWINPARSER_21_040: [The updateTwin shall not change fields that is not reported in the json string.] */
    @Test
    public void updateTwinJsonEmptyClassWithMetadataNoUpdateVersion_And5LevelsTagsSucceed()
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
        Helpers.assertJson(resultJson, json);
    }

    @Test
    public void updateTwinJson_And6LevelsTagsFailed() throws IOException
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        OnTagsCallback onTagsCallback = new OnTagsCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        twinParser.setTagsCallback(onTagsCallback);
        twinParser.enableMetadata();
        twinParser.enableTags();
        Map<String, Object> oldTagsValues = new HashMap<>();
        oldTagsValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", true); put("Key3", "value3"); }});
        twinParser.updateTags(oldTagsValues);
        Map<String, Object> oldPropertiesValues = new HashMap<>();
        oldPropertiesValues.put("key555", "value1234");
        twinParser.updateReportedProperty(oldPropertiesValues);
        twinParser.updateDesiredProperty(oldPropertiesValues);

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
        assertTwin(twinParser, oldPropertiesValues, oldPropertiesValues, oldTagsValues);
    }

    /* Tests_SRS_TWINPARSER_21_039: [The updateTwin shall fill the fields the properties in the TwinParser class with the keys and values provided in the json string.] */
    /* Tests_SRS_TWINPARSER_21_040: [The updateTwin shall not change fields that is not reported in the json string.] */
    @Test
    public void updateTwinJsonEmptyClassWithFullMetadataNoVersionSucceed()
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
        Helpers.assertJson(resultJson, json);
    }

    /* Tests_SRS_TWINPARSER_21_040: [The updateTwin shall not change fields that is not reported in the json string.] */
    /* Tests_SRS_TWINPARSER_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
    /* Tests_SRS_TWINPARSER_21_044: [If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.] */
    /* Tests_SRS_TWINPARSER_21_045: [If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.] */
    @Test
    public void updateTwinJsonChangeOneFieldSucceed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key1", "value4");
        oldDesiredValues.put("key2", 1234);
        oldDesiredValues.put("key6", "value6");
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);

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

        oldDesiredValues.put("key2", 9875.0);
        oldReportedValues.put("key1", "value4");
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_097: [If the provided json have any duplicated `properties` or `tags`, the updateTwin shall throw IllegalArgumentException.] */
    @Test
    public void updateTwinJsonDuplicatedPropertiesFailed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key1", "value4");
        oldDesiredValues.put("key2", 1234);
        oldDesiredValues.put("key6", "value6");
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);

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
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_098: [If the provided json is properties only and contains duplicated `desired` or `reported`, the updateTwin shall throws IllegalArgumentException.] */
    @Test
    public void updateTwinJsonDuplicatedDesiredFirsLevelFailed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key1", "value4");
        oldDesiredValues.put("key2", 1234);
        oldDesiredValues.put("key6", "value6");
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);

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
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_094: [If the provided json have any duplicated `key`, the updateTwin shall use the content of the last one in the String.] */
    @Test
    public void updateTwinJsonDuplicatedPropertySucceed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key1", "value4");
        oldDesiredValues.put("key2", 1234);
        oldDesiredValues.put("key6", "value6");
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);

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

        oldDesiredValues.put("key1", "value1");
        oldReportedValues.put("key1", "value4");
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_094: [If the provided json have any duplicated `key`, the updateTwin shall use the content of the last one in the String.] */
    @Test
    public void updateTwinJsonDuplicateDesiredKeySucceed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value1");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key3", "value3");
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key1", "value4");
        oldDesiredValues.put("key2", 1234);
        oldDesiredValues.put("key6", "value6");
        oldDesiredValues.put("key7", true);
        twinParser.updateDesiredProperty(oldDesiredValues);

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

        oldDesiredValues.put("key2", 9875.0);
        oldReportedValues.put("key1", "value4");
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_041: [The updateTwin shall create a list with all properties that was updated (new key or value) by the new json.] */
    /* Tests_SRS_TWINPARSER_21_042: [If a valid key has a null value, the updateTwin shall delete this property.] */
    /* Tests_SRS_TWINPARSER_21_044: [If OnDesiredCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Desired property, and OnDesiredCallback passing this map as parameter.] */
    /* Tests_SRS_TWINPARSER_21_045: [If OnReportedCallback was provided, the updateTwin shall create a new map with a copy of all pars key values updated by the json in the Reported property, and OnReportedCallback passing this map as parameter.] */
    @Test
    public void updateTwinJsonDeleteFieldNoMetadataSucceed()
    {
        // Arrange
        OnDesiredCallback onDesiredCallback = new OnDesiredCallback();
        OnReportedCallback onReportedCallback = new OnReportedCallback();
        TwinParser twinParser = new TwinParser(onDesiredCallback, onReportedCallback);
        Map<String, Object> oldReportedValues = new HashMap<>();
        oldReportedValues.put("key1", "value4");
        oldReportedValues.put("key2", 1234);
        oldReportedValues.put("key6", "value6");
        oldReportedValues.put("key7", true);
        twinParser.updateReportedProperty(oldReportedValues);
        Map<String, Object> oldDesiredValues = new HashMap<>();
        oldDesiredValues.put("key1", "value1");
        oldDesiredValues.put("key2", 1234);
        oldDesiredValues.put("key3", "value3");
        twinParser.updateDesiredProperty(oldDesiredValues);

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

        oldDesiredValues.put("key1", "value4");
        oldDesiredValues.remove("key3");
        oldReportedValues.remove("key1");
        oldReportedValues.remove("key7");
        assertTwin(twinParser, oldDesiredValues, oldReportedValues, null);
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwinJsonMissingCommaFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"desired\":{\"key3\":null,\"key1\":\"value4\"}" +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwinJsonBadPropertiesFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"bar properties\":{" +
                "\"desired\":{\"key3\":null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwinJsonUnknownPropertyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"property\":{" +
                "\"bar Property\":{\"key3\":null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwinJsonInvalidKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"desired\":{\"\":null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwinJsonNoKeyFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"desired\":{:null,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwinJsonInvalidValueFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{\"properties\":{" +
                "\"desired\":{\"Key3\":,\"key1\":\"value4\"}," +
                "\"reported\":{\"key1\":null,\"key5\":null,\"key7\":null}}}";

        // Act
        twinParser.updateTwin(json);
    }

    /* Tests_SRS_TWINPARSER_21_043: [If the provided json is not valid, the updateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwinJsonNotJsonFailed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        String json = "{CreateDate\"[\u1223\\/Date(13508408267)\\/\"}";

        // Act
        twinParser.updateTwin(json);
    }

    /* Tests_SRS_TWINPARSER_21_074: [If Tags is not enable, the getTagsMap shall throw IOException.] */
    @Test (expected = IOException.class)
    public void getTagsMapDisabledFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();

        // Act
        twinParser.getTagsMap();
    }

    /* Tests_SRS_TWINPARSER_21_111: [If Tags is not enable, the updateTags shall throw IOException.] */
    @Test (expected = IOException.class)
    public void updateTagsDisabledFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});

        // Act
        twinParser.updateTags(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_146: [If Tags is not enable, the resetTags shall throw IOException.] */
    @Test (expected = IOException.class)
    public void resetTagsDisabledFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});

        // Act
        twinParser.resetTags(newValues);
    }

    /* Tests_SRS_TWINPARSER_21_103: [The updateTags shall add all provided tags to the collection.] */
    /* Tests_SRS_TWINPARSER_21_104: [The updateTags shall return a string with json representing the tags with changes.] */
    /* Tests_SRS_TWINPARSER_21_157: [A valid `value` can contains sub-maps.] */
    /* Tests_SRS_TWINPARSER_21_158: [A valid `value` shall contains less than 5 levels of sub-maps.] */
    @Test
    public void updateTagsEmptyClassSucceed() throws IOException
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
        Helpers.assertJson(json, "{\"tag1\":{\"Key2\":1234,\"Key1\":\"value1\",\"Key3\":\"value3\"}," +
                "\"one\":{\"two\":{\"three\":{\"four\":{\"five\":{\"tagKey\":\"value\"}}}}}," +
                "\"tag2\":{\"Key2\":\"value5\",\"Key1\":\"value1\",\"Key4\":false}}");

        assertTwin(twinParser, null, null, newValues);
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_158: [A valid `value` shall contains less than 5 levels of sub-maps.] */
    @Test (expected = IllegalArgumentException.class)
    public void updateTagsMapBiggerThan5LevelsFailed() throws IOException
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
    }

    /* Tests_SRS_TWINPARSER_21_103: [The updateTags shall add all provided tags to the collection.] */
    /* Tests_SRS_TWINPARSER_21_104: [The updateTags shall return a string with json representing the tags with changes.] */
    /* Tests_SRS_TWINPARSER_21_107: [The updateTags shall only change tags in the map, keep the others as is.] */
    /* Tests_SRS_TWINPARSER_21_108: [All `key` and `value` in tags shall be case sensitive.] */
    /* Tests_SRS_TWINPARSER_21_114: [If any `key` already exists, the updateTags shall replace the existed value by the new one.] */
    /* Tests_SRS_TWINPARSER_21_115: [If any `value` is null, the updateTags shall delete it from the collection and report on Json.] */
    @Test
    public void updateTagsAddKeyChangeValueSucceed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); put("Key7", true); }});
        oldValues.put("one",
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
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        Helpers.assertJson(json, "{\"tag1\":{\"Key1\":\"value4\"},\"one\":{\"two\":{\"three\":{\"four\":{\"FIVE\":{\"tagKey\":\"newValue\"},\"five\":{\"tagKey\":\"newValue\"}}}}},\"tag2\":{\"Key5\":true,\"Key7\":null}}");

        ((Map<String, Object>)oldValues.get("tag1")).put("Key1", "value4");
        ((Map<String, Object>)oldValues.get("tag2")).put("Key5", true);
        ((Map<String, Object>)oldValues.get("tag2")).remove("Key7");
        ((Map<String, Object>)((Map<String, Object>)((Map<String, Object>)((Map<String, Object>)oldValues.get("one")).get("two")).get("three")).get("four")).
                put("five", new HashMap<String, Object>(){{ put("tagKey", "newValue"); }});
        ((Map<String, Object>)((Map<String, Object>)((Map<String, Object>)((Map<String, Object>)oldValues.get("one")).get("two")).get("three")).get("four")).
                put("FIVE", new HashMap<String, Object>(){{ put("tagKey", "newValue"); }});
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_114: [If any `key` already exists, the updateTags shall replace the existed value by the new one.] */
    @Test
    public void updateTagsReplaceStringByMapSucceed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        oldValues.put("one",
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
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        Helpers.assertJson(json, "{\"tag1\":{\"Key1\":{\"innerKey2\":\"value2\",\"innerKey1\":\"value1\"}}}");

        ((Map<String, Object>)oldValues.get("tag1")).put("Key1",
                            new HashMap<String, Object>(){{
                                put("innerKey1", "value1");
                                put("innerKey2", "value2");
                            }});
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_105: [If the provided `tagsMap` is null, the updateTags shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void updateTagsNullFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_106: [If no tags changed its value, the updateTags shall return null.] */
    @Test
    public void updateTagsNoChangesSucceed() throws IOException
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
        assertTwin(twinParser, null, null, newValues);
    }
    
    /* Tests_SRS_TWINPARSER_21_109: [If the provided `tagsMap` is empty, the updateTags shall not change the collection and return null.] */
    @Test
    public void updateTagsEmptySucceed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);
        Map<String, Object> newValues = new HashMap<>();

        // Act
        String json = twinParser.updateTags(newValues);

        // Assert
        assertNull(json);
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_152: [A valid `key` shall not be null.] */
    @Test
    public void updateTagsNullKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_153: [A valid `key` shall not be empty.] */
    @Test
    public void updateTagsEmptyKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }


    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_154: [A valid `key` shall be less than 128 characters long.] */
    @Test
    public void updateTagsBigKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void updateTagsInvalidDollarKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void updateTagsInvalidDotKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void updateTagsInvalidSpaceKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_110: [If the map is invalid, the updateTags shall throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_156: [A valid `value` shall contains types of boolean, number, string, or object.] */
    @Test
    public void updateTagsInvalidValueTypeFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_140: [The resetTags shall add cleanup the tags collection and all provided tags to the tags.] */
    /* Tests_SRS_TWINPARSER_21_141: [The resetTags shall return a string with json representing the added tags.] */
    /* Tests_SRS_TWINPARSER_21_143: [The `key` and `value` in tags shall be case sensitive.] */
    /* Tests_SRS_TWINPARSER_21_149: [If any `value` is null, the resetTags shall delete it from the collection and report on Json.] */
    @Test
    public void resetTagsNewKeyValueSucceed() throws IOException
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
        Helpers.assertJson(json, "{\"tag1\":{\"Key1\":\"value4\"}," +
                "\"one\":{\"two\":{\"three333\":{\"four\":{" +
                        "\"FIVE\":{\"tagKey\":\"newValue\"}," +
                        "\"five\":{\"tagKey\":\"newValue\"}}}}}," +
                "\"tag2\":{\"Key1\":\"value1\",\"Key5\":true,\"Key7\":null}}");

        ((Map<String, Object>)newValues.get("tag2")).remove("Key7");
        assertTwin(twinParser, null, null, newValues);
    }


    /* Tests_SRS_TWINPARSER_21_142: [If the provided `tagsMap` is null, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    @Test
    public void resetTagsNullMapFailed() throws IOException
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
        assertTwin(twinParser, null, null, newValues);
    }

    /* Tests_SRS_TWINPARSER_21_144: [If the provided `tagsMap` is empty, the resetTags shall cleanup the tags collection and return `{}`.] */
    @Test
    public void resetTagsEmptyMapSucceed() throws IOException
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
        Helpers.assertJson(json, "{}");

        Map<String, Object> tagMap = twinParser.getTagsMap();
        assertThat(tagMap.size(), is(0));
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_152: [A valid `key` shall not be null.] */
    @Test
    public void resetTagsNullKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_153: [A valid `key` shall not be empty.] */
    @Test
    public void resetTagsEmptyKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_154: [A valid `key` shall be less than 128 characters long.] */
    @Test
    public void resetTagsBigKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void resetTagsInvalidDollarKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void resetTagsInvalidDotKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_155: [A valid `key` shall not have an illegal character (`$`,`.`, space).] */
    @Test
    public void resetTagsInvalidSpaceKeyFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_145: [If the map is invalid, the resetTags shall not change the collection and throw IllegalArgumentException.] */
    /* Tests_SRS_TWINPARSER_21_156: [A valid `value` shall contains types of boolean, number, string, or object.] */
    @Test
    public void resetTagsInvalidValueTypeFailed() throws IOException
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        oldValues.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        twinParser.updateTags(oldValues);

        Map<String, Object> newValues = new HashMap<>();
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
        assertTwin(twinParser, null, null, oldValues);
    }

    /* Tests_SRS_TWINPARSER_21_168: [The `setDeviceId` shall set the deviceId in the twin collection.] */
    @Test
    public void setDeviceIdSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        String deviceId = "DeviceName";

        // Act
        twinParser.setDeviceId(deviceId);

        // Assert
        assertThat(twinParser.getDeviceId(), is(deviceId));
    }

    /* Tests_SRS_TWINPARSER_21_169: [If the deviceId is empty, null, or not valid, the `setDeviceId` shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setDeviceIdNullThrows()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        String deviceId = null;

        // Act
        twinParser.setDeviceId(deviceId);
    }

    /* Tests_SRS_TWINPARSER_21_169: [If the deviceId is empty, null, or not valid, the `setDeviceId` shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setDeviceIdEmptyThrows()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        String deviceId = "";

        // Act
        twinParser.setDeviceId(deviceId);
    }

    /* Tests_SRS_TWINPARSER_21_169: [If the deviceId is empty, null, or not valid, the `setDeviceId` shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setDeviceIdInvalidThrows()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        String deviceId = "Device&name";

        // Act
        twinParser.setDeviceId(deviceId);
    }

    /* Tests_SRS_TWINPARSER_21_170: [The `setETag` shall set the ETag in the twin collection.] */
    @Test
    public void setETagSucceed()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        String eTag = "4564654-validEtag";

        // Act
        twinParser.setETag(eTag);

        // Assert
        assertThat(twinParser.getETag(), is(eTag));
    }

    /* Tests_SRS_TWINPARSER_21_171: [If the ETag is empty, null, or not valid, the `setETag` shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setETagNullThrows()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        String eTag = null;

        // Act
        twinParser.setETag(eTag);
    }

    /* Tests_SRS_TWINPARSER_21_171: [If the ETag is empty, null, or not valid, the `setETag` shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setETagEmptyThrows()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        String eTag = "";

        // Act
        twinParser.setETag(eTag);
    }
    /* Tests_SRS_TWINPARSER_21_171: [If the ETag is empty, null, or not valid, the `setETag` shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setETagInvalidThrows()
    {
        // Arrange
        TwinParser twinParser = new TwinParser();
        String eTag = "invalidEtag\u4564654-";

        // Act
        twinParser.setETag(eTag);
    }
}
