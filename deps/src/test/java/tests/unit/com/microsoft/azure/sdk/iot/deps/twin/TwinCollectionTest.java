// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.deps.twin.TwinMetadata;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;
import tests.unit.com.microsoft.azure.sdk.iot.deps.Helpers;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for the TwinCollection
 * 100% methods, 100% lines covered
 */
public class TwinCollectionTest
{
    private static final String VALID_KEY_NAME = "Brand";
    private static final String VALID_VALUE_NAME = "NiceCar";
    private static final Integer VERSION = 4;
    private static final Map<String, Object> PROPERTIES_SAMPLE = new HashMap<String, Object>()
    {
        {
            put(VALID_KEY_NAME, VALID_VALUE_NAME);
            put("MaxSpeed", new HashMap<String, Object>()
            {
                {
                    put("Value", 500.0);
                    put("NewValue", 300.0);
                    put("Inner1", new HashMap<String, Object>()
                    {
                        {
                            put("Inner2", "FinalInnerValue");
                        }
                    });
                }
            });
        }
    };

    private static final String JSON_SAMPLE =
            "    {  \n" +
            "      \"Brand\":\"NiceCar\",\n" +
            "      \"MaxSpeed\":{  \n" +
            "        \"Value\":500,\n" +
            "        \"NewValue\":300,\n" +
            "        \"Inner1\":{" +
            "          \"Inner2\":\"FinalInnerValue\"" +
            "        }\n" +
            "      }\n" +
            "    }\n";

    private static final String JSON_FULL_SAMPLE =
        "    {  \n" +
        "      \"Brand\":\"NiceCar\",\n" +
        "      \"MaxSpeed\":{  \n" +
        "        \"Value\":500,\n" +
        "        \"NewValue\":300,\n" +
        "        \"Inner1\":{" +
        "          \"Inner2\":\"FinalInnerValue\"" +
        "        }\n" +
        "      },\n" +
        "      \"$metadata\":{  \n" +
        "        \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
        "        \"$lastUpdatedVersion\":1,\n" +
        "        \"Brand\":{" +
        "          \"$lastUpdated\":\"2017-08-09T02:07:44.238Z\",\n" +
        "          \"$lastUpdatedVersion\":2" +
        "        },\n" +
        "        \"MaxSpeed\":{  \n" +
        "          \"$lastUpdated\":\"2017-10-21T02:07:44.238Z\",\n" +
        "          \"$lastUpdatedVersion\":3,\n" +
        "          \"Value\":{  \n" +
        "            \"$lastUpdated\":\"2017-11-21T02:07:44.238Z\",\n" +
        "            \"$lastUpdatedVersion\":4\n" +
        "          },\n" +
        "          \"NewValue\":{  \n" +
        "            \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
        "            \"$lastUpdatedVersion\":5\n" +
        "          },\n" +
        "          \"Inner1\":{  \n" +
        "            \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
        "            \"$lastUpdatedVersion\":6,\n" +
        "            \"Inner2\":{  \n" +
        "              \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
        "              \"$lastUpdatedVersion\":7\n" +
        "            }\n" +
        "          }\n" +
        "        }\n" +
        "      },\n" +
        "      \"$version\":" + VERSION + "\n" +
        "    }\n";

    /* SRS_TWIN_COLLECTION_21_001: [The constructor shall create a new instance of the super class.] */
    @Test
    public void constructorCreatesSuperSucceed()
    {
        // arrange
        // act
        TwinCollection twinCollection = new TwinCollection();

        // assert
        assertNotNull(twinCollection);
        assertEquals(0, twinCollection.size());
    }

    /* SRS_TWIN_COLLECTION_21_002: [If the Map is null or empty, the constructor shall create a new empty instance.] */
    @Test
    public void constructorMapCreatesEmptySuperOnNullSucceed()
    {
        // arrange
        // act
        TwinCollection twinCollection = new TwinCollection((HashMap<String, Object>)null);

        // assert
        assertNotNull(twinCollection);
        assertEquals(0, twinCollection.size());
    }

    /* SRS_TWIN_COLLECTION_21_002: [If the Map is null or empty, the constructor shall create a new empty instance.] */
    @Test
    public void constructorMapCreatesEmptySuperOnEmptySucceed()
    {
        // arrange
        // act
        TwinCollection twinCollection = new TwinCollection(new HashMap<String, Object>());

        // assert
        assertNotNull(twinCollection);
        assertEquals(0, twinCollection.size());
    }

    /* SRS_TWIN_COLLECTION_21_003: [The constructor shall create a new instance of the super class and add the provided Map by calling putAll.] */
    @Test
    public void constructorWithMapSucceed()
    {
        // arrange
        final class MockedTwinCollection extends TwinCollection
        {
            private Map<? extends String, ?> mockedMap;
            private MockedTwinCollection(Map<? extends String, Object> map)
            {
                super(map);
            }

            @Override
            public void putAll(Map<? extends String, ?> map)
            {
                this.mockedMap = map;
            }
        }

        // act
        MockedTwinCollection twinCollection = new MockedTwinCollection(PROPERTIES_SAMPLE);

        // assert
        assertNotNull(twinCollection.mockedMap);
    }

    /* SRS_TWIN_COLLECTION_21_025: [If the Collection is null or empty, the constructor shall create a new empty instance.] */
    @Test
    public void constructorCollectionCreatesEmptySuperOnNullSucceed()
    {
        // arrange
        // act
        TwinCollection twinCollection = new TwinCollection((TwinCollection)null);

        // assert
        assertNotNull(twinCollection);
        assertEquals(0, twinCollection.size());
    }

    /* SRS_TWIN_COLLECTION_21_025: [If the Collection is null or empty, the constructor shall create a new empty instance.] */
    @Test
    public void constructorCollectionCreatesEmptySuperOnEmptySucceed()
    {
        // arrange
        // act
        TwinCollection twinCollection = new TwinCollection(new TwinCollection());

        // assert
        assertNotNull(twinCollection);
        assertEquals(0, twinCollection.size());
    }

    /* SRS_TWIN_COLLECTION_21_026: [The constructor shall create a new instance of the super class and add the provided Map by calling putAll.] */
    @Test
    public void constructorWithPureCollectionSucceed()
    {
        // arrange
        TwinCollection oldTwinCollection = new TwinCollection(PROPERTIES_SAMPLE);

        // act
        TwinCollection twinCollection = new TwinCollection(oldTwinCollection);

        // assert
        Helpers.assertMap(twinCollection, oldTwinCollection);
        assertNull(twinCollection.getVersion());
        assertNull(twinCollection.getTwinMetadata());
    }

    /* SRS_TWIN_COLLECTION_21_027: [The constructor shall copy the version and metadata from the provided TwinCollection.] */
    @Test
    public void constructorWithCollectionWithVersionAndMetadataSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(JSON_FULL_SAMPLE, TwinCollection.class);
        TwinCollection oldTwinCollection = Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // act
        TwinCollection twinCollection = new TwinCollection(oldTwinCollection);

        // assert
        Helpers.assertTwinCollection(twinCollection, oldTwinCollection);
    }

    /* SRS_TWIN_COLLECTION_21_027: [The constructor shall copy the version and metadata from the provided TwinCollection.] */
    @Test
    public void constructorWithCollectionWithVersionAndIncompletedMetadataSucceed()
    {
        // arrange
        final String json =
                "    {  \n" +
                "      \"Brand\":\"NiceCar\",\n" +
                "      \"MaxSpeed\":{  \n" +
                "        \"Value\":500,\n" +
                "        \"NewValue\":300,\n" +
                "        \"Inner1\":{" +
                "          \"Inner2\":\"FinalInnerValue\"" +
                "        }\n" +
                "      },\n" +
                "      \"$metadata\":{  \n" +
                "        \"Brand\":{" +
                "          \"$lastUpdated\":\"2017-08-09T02:07:44.238Z\",\n" +
                "          \"$lastUpdatedVersion\":2" +
                "        },\n" +
                "        \"MaxSpeed\":{  \n" +
                "          \"$lastUpdated\":\"2017-10-21T02:07:44.238Z\",\n" +
                "          \"$lastUpdatedVersion\":3,\n" +
                "          \"Value\":{  \n" +
                "            \"$lastUpdated\":\"2017-11-21T02:07:44.238Z\",\n" +
                "            \"$lastUpdatedVersion\":4\n" +
                "          },\n" +
                "          \"NewValue\":{  \n" +
                "            \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
                "            \"$lastUpdatedVersion\":5\n" +
                "          },\n" +
                "          \"Inner1\":{  \n" +
                "            \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
                "            \"$lastUpdatedVersion\":6,\n" +
                "            \"Inner2\":{  \n" +
                "              \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
                "              \"$lastUpdatedVersion\":7\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"$version\":" + VERSION + "\n" +
                "    }\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(json, TwinCollection.class);
        TwinCollection oldTwinCollection = Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // act
        TwinCollection twinCollection = new TwinCollection(oldTwinCollection);

        // assert
        Helpers.assertTwinCollection(twinCollection, oldTwinCollection);
    }

    /* SRS_TWIN_COLLECTION_21_004: [The putAll shall throw IllegalArgumentException if the provided Map is null, empty or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void putAllThrowsOnNull()
    {
        // arrange
        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.putAll(null);

        // assert
    }

    /* SRS_TWIN_COLLECTION_21_004: [The putAll shall throw IllegalArgumentException if the provided Map is null, empty or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void putAllThrowsOnEmptyMap()
    {
        // arrange
        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.putAll(new HashMap<String, Object>());

        // assert
    }

    /* SRS_TWIN_COLLECTION_21_004: [The putAll shall throw IllegalArgumentException if the provided Map is null, empty or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void putAllThrowsOnIllegal6InnerMaps()
    {
        // arrange
        final Map<String, Object> rawMap = new HashMap<String, Object>()
        {
            {
                put(VALID_KEY_NAME, VALID_VALUE_NAME);
                put("MaxSpeed", new TwinCollection()
                {
                    {
                        put("Value", 500.0);
                        put("NewValue", 300.0);
                        put("Inner1", new TwinCollection()
                        {
                            {
                                put("Inner2", new TwinCollection()
                                {
                                    {
                                        put("Inner3", new TwinCollection()
                                        {
                                            {
                                                put("Inner4", new TwinCollection()
                                                {
                                                    {
                                                    	put("Inner5", new TwinCollection()
                                                        {
                                                    		{
                                                    			put("Inner6", "FinalInnerValue");
                                                    		}
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };
        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.putAll(rawMap);

        // assert
    }

    /* SRS_TWIN_COLLECTION_21_005: [The putAll shall copy all entries in the provided Map to the TwinCollection.] */
    @Test
    public void putAllSucceedOn5InnerMaps()
    {
        // arrange
        final Map<String, Object> rawMap = new HashMap<String, Object>()
        {
            {
                put(VALID_KEY_NAME, VALID_VALUE_NAME);
                put("MaxSpeed", new TwinCollection()
                {
                    {
                        put("Value", 500.0);
                        put("NewValue", 300.0);
                        put("Inner1", new TwinCollection()
                        {
                            {
                                put("Inner2", new TwinCollection()
                                {
                                    {
                                        put("Inner3", new TwinCollection()
                                        {
                                            {
                                                put("Inner4", "FinalInnerValue");
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };
        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.putAll(rawMap);

        // assert
        Helpers.assertMap(twinCollection, rawMap);
    }

    /* SRS_TWIN_COLLECTION_21_006: [The put shall return the previous value of the key.] */
    @Test
    public void putReturnsPreviousValueSucceed()
    {
        // arrange
        final Map<String, Object> rawMap = new HashMap<String, Object>()
        {
            {
                put(VALID_KEY_NAME, VALID_VALUE_NAME);
            }
        };
        TwinCollection twinCollection = new TwinCollection(rawMap);

        // act
        String lastBrand = (String)twinCollection.put(VALID_KEY_NAME, "NewNiceCar");

        // assert
        assertEquals(VALID_VALUE_NAME, lastBrand);
    }

    /* SRS_TWIN_COLLECTION_21_006: [The put shall return the previous value of the key.] */
    @Test
    public void putNoPreviousValueSucceed()
    {
        // arrange
        TwinCollection twinCollection = new TwinCollection();

        // act
        Object lastBrand = twinCollection.put(VALID_KEY_NAME, "NewNiceCar");

        // assert
        assertNull(lastBrand);
    }

    /* SRS_TWIN_COLLECTION_21_007: [The put shall add the new pair key value to the TwinCollection.] */
    @Test
    public void putAddNewPairSucceed()
    {
        // arrange
        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.put(VALID_KEY_NAME, "NewNiceCar");

        // assert
        assertEquals("NewNiceCar", twinCollection.get(VALID_KEY_NAME));
    }

    /* SRS_TWIN_COLLECTION_21_008: [If the value contains a Map, the put shall convert this map in inner TwinCollection.] */
    @Test
    public void putSucceedOn5InnerMaps()
    {
        // arrange
        final Map<String, Object> rawMap = new HashMap<String, Object>()
        {
            {
                put("Value", 500.0);
                put("NewValue", 300.0);
                put("Inner1", new TwinCollection()
                {
                    {
                        put("Inner2", new TwinCollection()
                        {
                            {
                                put("Inner3", new TwinCollection()
                                {
                                    {
                                        put("Inner4", "FinalInnerValue");
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };
        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.put("MaxSpeed", rawMap);

        // assert
        Object inner = twinCollection.get("MaxSpeed");
        assertTrue(inner instanceof TwinCollection);
    }

    /* SRS_TWIN_COLLECTION_21_009: [The put shall throw IllegalArgumentException if the final collection contains more that 5 levels.] */
    @Test (expected = IllegalArgumentException.class)
    public void putThrowsOn6InnerMaps()
    {
        // arrange
        final Map<String, Object> rawMap = new HashMap<String, Object>()
        {
            {
                put(VALID_KEY_NAME, VALID_VALUE_NAME);
                put("MaxSpeed", new TwinCollection()
                {
                    {
                        put("Value", 500.0);
                        put("NewValue", 300.0);
                        put("Inner1", new TwinCollection()
                        {
                            {
                                put("Inner2", new TwinCollection()
                                {
                                    {
                                        put("Inner3", new TwinCollection()
                                        {
                                            {
                                                put("Inner4", new TwinCollection()
                                                {
                                                    {
                                                        put("Inner5", "FinalInnerValue");
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };
        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.put("Key1", rawMap);

        // assert
    }

    /* Tests_SRS_TWIN_COLLECTION_34_028: [The put shall not validate the map if the provided key is a metadata tag, or a version tag.] */
    @Test
    public void putDoesNotThrowOn6InnerMapsIfHighestLevelIsMetaData(@Mocked final ParserUtility mockedParserUtility,
                                                                    @Mocked final JsonElement mockedJsonElement)
    {
        // arrange
        final Map<String, Object> rawMap = new HashMap<String, Object>()
        {
            {
                put(VALID_KEY_NAME, VALID_VALUE_NAME);
                put("MaxSpeed", new TwinCollection()
                {
                    {
                        put("Value", 500.0);
                        put("NewValue", 300.0);
                        put("Inner1", new TwinCollection()
                        {
                            {
                                put("Inner2", new TwinCollection()
                                {
                                    {
                                        put("Inner3", new TwinCollection()
                                        {
                                            {
                                                put("Inner4", new TwinCollection()
                                                {
                                                    {
                                                        put("Inner5", "FinalInnerValue");
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };
        final TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.put(Deencapsulation.getField(TwinCollection.class, "METADATA_TAG").toString(), rawMap);

        // assert
        new Verifications()
        {
            {
                mockedParserUtility.validateMap(twinCollection, anyInt, anyBoolean);
                times = 0;
            }
        };
    }

    /* Tests_SRS_TWIN_COLLECTION_34_028: [The put shall not validate the map if the provided key is a metadata tag, or a version tag.] */
    @Test
    public void putDoesNotThrowOn6InnerMapsIfHighestLevelIsVersionTag(@Mocked final ParserUtility mockedParserUtility,
                                                                    @Mocked final JsonElement mockedJsonElement)
    {
        // arrange
        final Map<String, Object> rawMap = new HashMap<String, Object>()
        {
            {
                put(VALID_KEY_NAME, VALID_VALUE_NAME);
                put("MaxSpeed", new TwinCollection()
                {
                    {
                        put("Value", 500.0);
                        put("NewValue", 300.0);
                        put("Inner1", new TwinCollection()
                        {
                            {
                                put("Inner2", new TwinCollection()
                                {
                                    {
                                        put("Inner3", new TwinCollection()
                                        {
                                            {
                                                put("Inner4", new TwinCollection()
                                                {
                                                    {
                                                        put("Inner5", "FinalInnerValue");
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };
        final TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.put(Deencapsulation.getField(TwinCollection.class, "VERSION_TAG").toString(), rawMap);

        // assert
        new Verifications()
        {
            {
                mockedParserUtility.validateMap(twinCollection, anyInt, anyBoolean);
                times = 0;
            }
        };
    }

    /* SRS_TWIN_COLLECTION_21_010: [The put shall throw IllegalArgumentException if the provided key is null, empty, or invalid, or if the value is invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void putThrowsOnKeyNull()
    {
        // arrange
        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.put(null, "NewNiceCar");

        // assert
    }

    /* SRS_TWIN_COLLECTION_21_010: [The put shall throw IllegalArgumentException if the provided key is null, empty, or invalid, or if the value is invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void putThrowsOnKeyEmpty()
    {
        // arrange
        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.put("", "NewNiceCar");

        // assert
    }

    /* SRS_TWIN_COLLECTION_21_010: [The put shall throw IllegalArgumentException if the provided key is null, empty, or invalid, or if the value is invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void putThrowsOnKeyInvalid()
    {
        // arrange
        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.put("Invalid space", "NewNiceCar");

        // assert
    }

    /* SRS_TWIN_COLLECTION_21_010: [The put shall throw IllegalArgumentException if the provided key is null, empty, or invalid, or if the value is invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void putThrowsOnValueInvalidArray()
    {
        // arrange
        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.put(VALID_KEY_NAME, new int[]{1,2,3});

        // assert
    }

    /* SRS_TWIN_COLLECTION_21_010: [The put shall throw IllegalArgumentException if the provided key is null, empty, or invalid, or if the value is invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void putThrowsOnValueInvalidType()
    {
        // arrange
        final class UserType
        {
            private int a = 10;
            protected String b = VALID_VALUE_NAME;
        }

        TwinCollection twinCollection = new TwinCollection();

        // act
        twinCollection.put(VALID_KEY_NAME, new UserType());

        // assert
    }

    /* SRS_TWIN_COLLECTION_21_011: [The constructor shall convert the provided rawCollection in a valid TwinCollection.] */
    @Test
    public void constructor2LevelsWithoutMetadataSucceed()
    {
        // arrange
        // act
        TwinCollection twinCollection = Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", PROPERTIES_SAMPLE);

        // assert
        Helpers.assertMap(twinCollection, PROPERTIES_SAMPLE);
    }

    /* SRS_TWIN_COLLECTION_21_012: [If the entity contains the key `$version`, the constructor shall set the version with the value of this entity.] */
    @Test
    public void constructor2LevelsWithVersionSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(JSON_FULL_SAMPLE, TwinCollection.class);

        // act
        TwinCollection twinCollection = Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // assert
        assertEquals(Deencapsulation.getField(twinCollection, "version") ,VERSION);
    }

    /* SRS_TWIN_COLLECTION_21_013: [The constructor shall throw IllegalArgumentException if the entity contains the key `$version` and its value is not a integer.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor2LevelsWithInvalidVersionFailed()
    {
        // arrange
        final Map<String, Object> property = new HashMap<>(PROPERTIES_SAMPLE);
        property.put("$version", "invalidString");

        // act
        Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", property);

        // assert
    }

    /* SRS_TWIN_COLLECTION_21_014: [If the entity contains the key `$metadata`, the constructor shall create a TwinMetadata with the value of this entity.] */
    /* SRS_TWIN_COLLECTION_21_021: [The getVersion shall return a Integer with the stored version.] */
    /* SRS_TWIN_COLLECTION_21_022: [The getTwinMetadata shall return the metadata of the whole TwinCollection.] */
    /* SRS_TWIN_COLLECTION_21_023: [The getTwinMetadata shall return the metadata of the entry that correspond to the provided key.] */
    @Test
    public void constructor2LevelsWithMetadataSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(JSON_FULL_SAMPLE, TwinCollection.class);

        // act
        TwinCollection twinCollection = Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // act(getters) - assert
        assertEquals(VERSION, twinCollection.getVersion());
        assertEquals(1L, (long)twinCollection.getTwinMetadata().getLastUpdatedVersion());

        assertEquals(VALID_VALUE_NAME, twinCollection.get(VALID_KEY_NAME));
        Helpers.assertDateWithError(twinCollection.getTwinMetadata(VALID_KEY_NAME).getLastUpdated(), "2017-08-09T02:07:44.238Z");
        assertEquals(2L, (long)twinCollection.getTwinMetadata(VALID_KEY_NAME).getLastUpdatedVersion());

        Helpers.assertDateWithError(twinCollection.getTwinMetadata("MaxSpeed").getLastUpdated(), "2017-10-21T02:07:44.238Z");
        assertEquals(3L, (long)twinCollection.getTwinMetadata("MaxSpeed").getLastUpdatedVersion());
        TwinCollection innerMaxSpeed = (TwinCollection) twinCollection.get("MaxSpeed");

        assertEquals(3L, (long)innerMaxSpeed.getTwinMetadata().getLastUpdatedVersion());

        assertEquals(500.0, innerMaxSpeed.get("Value"));
        Helpers.assertDateWithError(innerMaxSpeed.getTwinMetadata("Value").getLastUpdated(), "2017-11-21T02:07:44.238Z");
        assertEquals(4L, (long)innerMaxSpeed.getTwinMetadata("Value").getLastUpdatedVersion());
    }

    @Test
    public void constructorWithNullPropertiesSucceed()
    {
        // arrange
        final TwinCollection expected = new TwinCollection()
        {
            {
                put(VALID_KEY_NAME, VALID_VALUE_NAME);
                put("MaxSpeed", new TwinCollection()
                {
                    {
                        put("Value", null);
                        put("NewValue", 300.0);
                        put("Inner1", new TwinCollection()
                        {
                            {
                                put("Inner2", "FinalInnerValue");
                            }
                        });
                    }
                });
            }
        };
        final String json =
                "    {  \n" +
                "      \"Brand\":\"NiceCar\",\n" +
                "      \"MaxSpeed\":{  \n" +
                "        \"Value\":null,\n" +
                "        \"NewValue\":300,\n" +
                "        \"Inner1\":{" +
                "          \"Inner2\":\"FinalInnerValue\"" +
                "        }\n" +
                "      }\n" +
                "    }\n";

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(json, TwinCollection.class);

        // act
        TwinCollection twinCollection = Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // assert
        assertNotNull(twinCollection);
        Helpers.assertMap(twinCollection, expected);
    }

    /* SRS_TWIN_COLLECTION_21_014: [If the entity contains the key `$metadata`, the constructor shall create a TwinMetadata with the value of this entity.] */
    @Test
    public void constructorTwinCollectionWithoutBaseMetadataSucceed()
    {
        // arrange
        final String inconsistentJson =
                "    {  \n" +
                "      \"Brand\":\"NiceCar\",\n" +
                "      \"$metadata\":{  \n" +
                "        \"Brand\":{" +
                "          \"$lastUpdated\":\"2017-08-09T02:07:44.238Z\",\n" +
                "          \"$lastUpdatedVersion\":2" +
                "        }\n" +
                "      },\n" +
                "      \"$version\":" + VERSION + "\n" +
                "    }\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(inconsistentJson, TwinCollection.class);

        // act
        TwinCollection twinCollection = Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // act(getters) - assert
        assertEquals(VERSION, twinCollection.getVersion());
        assertNull(twinCollection.getTwinMetadata());

        assertEquals(VALID_VALUE_NAME, twinCollection.get(VALID_KEY_NAME));
        Helpers.assertDateWithError(twinCollection.getTwinMetadata(VALID_KEY_NAME).getLastUpdated(), "2017-08-09T02:07:44.238Z");
        assertEquals(2L, (long)twinCollection.getTwinMetadata(VALID_KEY_NAME).getLastUpdatedVersion());
    }

    /* SRS_TWIN_COLLECTION_21_024: [The constructor shall throw IllegalArgumentException if the metadata is inconsistent with the TwinCollection.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorInconsistentMetadataFailed()
    {
        // arrange
        final String inconsistentJson =
                "    {  \n" +
                "      \"Brand\":\"NiceCar\",\n" +
                "      \"MaxSpeed\":{  \n" +
                "        \"Value\":500,\n" +
                "        \"NewValue\":300,\n" +
                "        \"Inner1\":{" +
                "          \"Inner2\":\"FinalInnerValue\"" +
                "        }\n" +
                "      },\n" +
                "      \"$metadata\":{  \n" +
                "        \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
                "        \"$lastUpdatedVersion\":1,\n" +
                "        \"Brand\":{" +
                "          \"$lastUpdated\":\"2017-08-09T02:07:44.238Z\",\n" +
                "          \"$lastUpdatedVersion\":2" +
                "        },\n" +
                "        \"MaxSpeed\":{  \n" +
                "          \"$lastUpdated\":\"2017-10-21T02:07:44.238Z\",\n" +
                "          \"$lastUpdatedVersion\":3,\n" +
                "          \"Value\":{  \n" +
                "            \"$lastUpdated\":\"2017-11-21T02:07:44.238Z\",\n" +
                "            \"$lastUpdatedVersion\":4\n" +
                "          },\n" +
                "          \"WrongEntity\":{  \n" +
                "            \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
                "            \"$lastUpdatedVersion\":5\n" +
                "          },\n" +
                "          \"Inner1\":{  \n" +
                "            \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
                "            \"$lastUpdatedVersion\":6,\n" +
                "            \"Inner2\":{  \n" +
                "              \"$lastUpdated\":\"2017-09-21T02:07:44.238Z\",\n" +
                "              \"$lastUpdatedVersion\":7\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"$version\":" + VERSION + "\n" +
                "    }\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(inconsistentJson, TwinCollection.class);

        // act
        Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // assert
    }

    /* SRS_TWIN_COLLECTION_21_015: [The constructor shall throw IllegalArgumentException if the Twin collection contains more than 5 levels.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnMoreThan5Levels()
    {
        // arrange
        final Map<String, Object> rawMap = new HashMap<String, Object>()
        {
            {
                put(VALID_KEY_NAME, VALID_VALUE_NAME);
                put("MaxSpeed", new TwinCollection()
                {
                    {
                        put("Value", 500.0);
                        put("NewValue", 300.0);
                        put("Inner1", new TwinCollection()
                        {
                            {
                                put("Inner2", new TwinCollection()
                                {
                                    {
                                        put("Inner3", new TwinCollection()
                                        {
                                            {
                                                put("Inner4", new TwinCollection()
                                                {
                                                    {
                                                    	put("Inner5", new TwinCollection() 
                                                    	{
                                                    		{
                                                    			put("Inner6", "FinalInnerValue");
                                                    		}
                                                    	});
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };

        // act
        Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // assert
    }

    /* SRS_TWIN_COLLECTION_21_015: [The constructor shall throw IllegalArgumentException if the Twin collection contains more than 5 levels.] */
    @Test
    public void constructorSucceedOn5Levels()
    {
        // arrange
        final Map<String, Object> rawMap = new HashMap<String, Object>()
        {
            {
                put(VALID_KEY_NAME, VALID_VALUE_NAME);
                put("MaxSpeed", new TwinCollection()
                {
                    {
                        put("Value", 500.0);
                        put("NewValue", 300.0);
                        put("Inner1", new TwinCollection()
                        {
                            {
                                put("Inner2", new TwinCollection()
                                {
                                    {
                                        put("Inner3", new TwinCollection()
                                        {
                                            {
                                                put("Inner4", "FinalInnerValue");
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };

        // act
        TwinCollection twinCollection = Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // assert
        assertNotNull(twinCollection);
    }

    /* SRS_TWIN_COLLECTION_21_016: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementSerializeSucceed()
    {
        // arrange
        final TwinCollection twinCollection = new TwinCollection()
        {
            {
                put(VALID_KEY_NAME, VALID_VALUE_NAME);
                put("MaxSpeed", new TwinCollection()
                {
                    {
                        put("Value", 500.0);
                        put("NewValue", 300.0);
                        put("Inner1", new TwinCollection()
                        {
                            {
                                put("Inner2", "FinalInnerValue");
                            }
                        });
                    }
                });
            }
        };

        // act
        JsonElement jsonElement = twinCollection.toJsonElement();

        // assert
        Helpers.assertJson(jsonElement.toString(), JSON_SAMPLE);
    }

    @Test
    public void toJsonElementSerializeNullPropertySucceed()
    {
        // arrange
        final TwinCollection twinCollection = new TwinCollection()
        {
            {
                put(VALID_KEY_NAME, VALID_VALUE_NAME);
                put("MaxSpeed", new TwinCollection()
                {
                    {
                        put("Value", null);
                        put("NewValue", 300.0);
                        put("Inner1", new TwinCollection()
                        {
                            {
                                put("Inner2", "FinalInnerValue");
                            }
                        });
                    }
                });
            }
        };
        final String json =
                "    {  \n" +
                "      \"Brand\":\"NiceCar\",\n" +
                "      \"MaxSpeed\":{  \n" +
                "        \"Value\":null,\n" +
                "        \"NewValue\":300,\n" +
                "        \"Inner1\":{" +
                "          \"Inner2\":\"FinalInnerValue\"" +
                "        }\n" +
                "      }\n" +
                "    }\n";

        // act
        JsonElement jsonElement = twinCollection.toJsonElement();

        // assert
        Helpers.assertJson(jsonElement.toString(), json);
    }

    /* SRS_TWIN_COLLECTION_21_017: [The toJsonElement shall not include any metadata in the returned JsonElement.] */
    @Test
    public void toJsonElementNotIncludeMetadataOrVersion()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(JSON_FULL_SAMPLE, TwinCollection.class);
        TwinCollection twinCollection = Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // act
        JsonElement jsonElement = twinCollection.toJsonElement();

        // assert
        Helpers.assertJson(jsonElement.toString(), JSON_SAMPLE);
    }

    /* SRS_TWIN_COLLECTION_21_018: [The toJsonElementWithMetadata shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementWithMetadataIncludeMetadataOrVersion()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(JSON_FULL_SAMPLE, TwinCollection.class);
        TwinCollection twinCollection = Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinCollection, "toJsonElementWithMetadata");

        // assert
        Helpers.assertJson(jsonElement.toString(), JSON_FULL_SAMPLE);
    }

    /* SRS_TWIN_COLLECTION_21_019: [If version is not null, the toJsonElementWithMetadata shall include the $version in the returned jsonElement.] */
    /* SRS_TWIN_COLLECTION_21_020: [If twinMetadata is not null, the toJsonElementWithMetadata shall include the $metadata in the returned jsonElement.] */
    @Test
    public void toJsonElementWithMetadataIgnoreEmptyMetadataAndVersion()
    {
        // arrange
        TwinCollection twinCollection = new TwinCollection(PROPERTIES_SAMPLE);

        // act
        JsonElement jsonElement = Deencapsulation.invoke(twinCollection, "toJsonElementWithMetadata");

        // assert
        Helpers.assertJson(jsonElement.toString(), JSON_SAMPLE);
    }

    /* SRS_TWIN_COLLECTION_21_024: [The toString shall return a String with the information in this class in a pretty print JSON.] */
    @Test
    public void toStringSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        TwinCollection rawMap = gson.fromJson(JSON_FULL_SAMPLE, TwinCollection.class);
        TwinCollection twinCollection = Deencapsulation.invoke(TwinCollection.class, "createFromRawCollection", rawMap);

        // act - assert
        Helpers.assertJson(twinCollection.toString(), JSON_FULL_SAMPLE);
    }
}
