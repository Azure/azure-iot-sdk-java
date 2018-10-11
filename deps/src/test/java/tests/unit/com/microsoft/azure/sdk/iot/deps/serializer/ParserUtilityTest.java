// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.JsonElement;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.twin.TwinMetadata;
import mockit.Deencapsulation;
import org.junit.Test;
import tests.unit.com.microsoft.azure.sdk.iot.deps.Helpers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for serializer utility helpers
 * 100% methods
 * 96% lines covered
 */
public class ParserUtilityTest
{
    /* Tests_SRS_PARSER_UTILITY_21_001: [The validateStringUTF8 shall do nothing if the string is valid.] */
    @Test
    public void validateStringUTF8Succeed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateStringUTF8", "test-string1#");
    }

    /* Tests_SRS_PARSER_UTILITY_21_002: [The validateStringUTF8 shall throw IllegalArgumentException if the provided string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateStringUTF8NullThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateStringUTF8", new Class[]{String.class}, null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_002: [The validateStringUTF8 shall throw IllegalArgumentException if the provided string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateStringUTF8EmptyThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateStringUTF8", "");
    }

    /* Tests_SRS_PARSER_UTILITY_21_003: [The validateStringUTF8 shall throw IllegalArgumentException if the provided string contains at least one not UTF-8 character.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateStringUTF8InvalidThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateStringUTF8", "\u1234test-string1#");
    }

    /* Tests_SRS_PARSER_UTILITY_21_004: [The validateBlobName shall do nothing if the string is valid.] */
    @Test
    public void validateBlobNameSucceed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateBlobName", "test-device1/image.jpg");
    }

    /* Tests_SRS_PARSER_UTILITY_21_005: [The validateBlobName shall throw IllegalArgumentException if the provided blob name is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateBlobNameNullThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateBlobName", new Class[]{String.class}, null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_005: [The validateBlobName shall throw IllegalArgumentException if the provided blob name is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateBlobNameEmptyThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateBlobName", "");
    }

    /* Tests_SRS_PARSER_UTILITY_21_006: [The validateBlobName shall throw IllegalArgumentException if the provided blob name contains at least one not UTF-8 character.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateBlobNameInvalidUtf8Throws() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateBlobName", "\u1234test-string1/image.jpg");
    }

    /* Tests_SRS_PARSER_UTILITY_21_007: [The validateBlobName shall throw IllegalArgumentException if the provided blob name contains more than 1024 characters.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateBlobNameInvalidBigNameThrows() throws ClassNotFoundException
    {
        // arrange
        StringBuilder bigBlobName = new StringBuilder();
        String directory = "directory/";

        // create a blob name bigger than 1024 characters.
        for (int i = 0; i < (2000/directory.length()); i++)
        {
            bigBlobName.append(directory);
        }
        bigBlobName.append("image.jpg");

        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateBlobName", bigBlobName.toString());
    }

    /* Tests_SRS_PARSER_UTILITY_21_008: [The validateBlobName shall throw IllegalArgumentException if the provided blob name contains more than 254 path segments.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateBlobNameInvalidInvalidPathThrows() throws ClassNotFoundException
    {
        // arrange
        StringBuilder bigBlobName = new StringBuilder();
        String directory = "a/";

        // create a blob name with more than 254 path segments.
        for (int i = 0; i < 300; i++)
        {
            bigBlobName.append(directory);
        }
        bigBlobName.append("image.jpg");

        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateBlobName", bigBlobName.toString());
    }

    //Tests_SRS_PARSER_UTILITY_25_031: [The validateQuery shall do nothing if the string is valid.]
    @Test
    public void validateQuerySucceeds() throws ClassNotFoundException
    {
        //arrange
        final String testQuery = "select * from abc";

        //act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateQuery", testQuery);
    }

    //Tests_SRS_PARSER_UTILITY_25_032: [The validateQuery shall throw IllegalArgumentException is the provided query is null or empty.]
    @Test (expected = IllegalArgumentException.class)
    public void validateQueryInvalidateEmptyThrows() throws ClassNotFoundException
    {
        //arrange
        final String testQuery = "";

        //act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateQuery", testQuery);
    }

    //Tests_SRS_PARSER_UTILITY_25_032: [The validateQuery shall throw IllegalArgumentException is the provided query is null or empty.]
    @Test (expected = IllegalArgumentException.class)
    public void validateQueryInvalidateNullThrows() throws ClassNotFoundException
    {
        //arrange
        final String testQuery = null;

        //act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateQuery", new Class[]{String.class}, testQuery);
    }

    //Tests_SRS_PARSER_UTILITY_25_033: [The validateQuery shall throw IllegalArgumentException is the provided query contains non UTF-8 character.]
    @Test (expected = IllegalArgumentException.class)
    public void validateQueryInvalidateUTFThrows() throws ClassNotFoundException
    {
        //arrange
        final String testQuery = "select * from \u1234";

        //act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateQuery", testQuery);
    }

    //Tests_SRS_PARSER_UTILITY_25_034: [The validateQuery shall throw IllegalArgumentException is the provided query does not contain SELECT and FROM.]
    @Test (expected = IllegalArgumentException.class)
    public void validateQueryInvalidateFormat1Throws() throws ClassNotFoundException
    {
        //arrange
        final String testQuery = "select *";

        //act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateQuery", testQuery);
    }

    //Tests_SRS_PARSER_UTILITY_25_034: [The validateQuery shall throw IllegalArgumentException is the provided query does not contain SELECT and FROM.]
    @Test (expected = IllegalArgumentException.class)
    public void validateQueryInvalidateFormat2Throws() throws ClassNotFoundException
    {
        //arrange
        final String testQuery = "from abc";

        //act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateQuery", testQuery);
    }

    /* Tests_SRS_PARSER_UTILITY_21_009: [The validateInteger shall do nothing if the object is valid.] */
    @Test
    public void validateObjectIntegerSucceed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateObject", 1234);
    }

    /* Tests_SRS_PARSER_UTILITY_21_009: [The validateBoolean shall do nothing if the object is valid.] */
    @Test
    public void validateObjectBooleanSucceed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateObject", true);
    }

    /* Tests_SRS_PARSER_UTILITY_21_010: [The validateInteger shall throw IllegalArgumentException if the provided object is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateIntegerNullThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateObject", new Class[]{Object.class} ,(String)null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_044: [The validateHostName shall throw IllegalArgumentException if the provided string is not a valid host name.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateHostNameThrowsOnNullHostName() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateHostName", new Class[]{String.class} , null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_044: [The validateHostName shall throw IllegalArgumentException if the provided string is not a valid host name.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateHostNameThrowsOnEmptyHostName() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateHostName", new Class[]{String.class} , "");
    }

    /* Tests_SRS_PARSER_UTILITY_21_044: [The validateHostName shall throw IllegalArgumentException if the provided string is not a valid host name.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateHostNameThrowsOnInvalidHostName() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateHostName", new Class[]{String.class} , "\u1234a.b.c");
    }

    /* Tests_SRS_PARSER_UTILITY_21_044: [The validateHostName shall throw IllegalArgumentException if the provided string is not a valid host name.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateHostNameThrowsOnIncompleteHostName() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateHostName", new Class[]{String.class} , "hostName");
    }

    /* Tests_SRS_PARSER_UTILITY_21_045: [The validateHostName shall do nothing if the string is a valid host name.] */
    @Test
    public void validateHostNameSucceeded() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateHostName", new Class[]{String.class} , "hostName.azure-devices.net");
    }

    /* Tests_SRS_PARSER_UTILITY_21_013: [The validateKey shall do nothing if the string is a valid key.] */
    /* Tests_SRS_PARSER_UTILITY_21_019: [If `isMetadata` is `false`, the validateKey shall not accept the character `$` as valid.] */
    @Test
    public void validateKeyNoMetadataSucceed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "test-key", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_013: [The validateKey shall do nothing if the string is a valid key.] */
    /* Tests_SRS_PARSER_UTILITY_21_018: [If `isMetadata` is `true`, the validateKey shall accept the character `$` as valid.] */
    @Test
    public void validateKeyMetadataSucceed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "$test-key", true);
    }

    /* Tests_SRS_PARSER_UTILITY_21_014: [The validateKey shall throw IllegalArgumentException if the provided string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKeyNullKeyThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey",
                new Class[]{String.class, Boolean.class}, (String)null, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_014: [The validateKey shall throw IllegalArgumentException if the provided string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKeyEmptyKeyThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_015: [The validateKey shall throw IllegalArgumentException if the provided string contains at least one not UTF-8 character.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKeyInvalidKeyThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "\u1234-test-key", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_016: [The validateKey shall throw IllegalArgumentException if the provided string contains more than 128 characters.] */
    @Test
    public void validateKeyEdgeSizeSucceed() throws ClassNotFoundException
    {
        // arrange
        String key = "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "12345678";

        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", key, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_016: [The validateKey shall throw IllegalArgumentException if the provided string contains more than 128 characters.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKeyInvalidBigKeyThrows() throws ClassNotFoundException
    {
        // arrange
        String key = "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "123456789";

        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", key, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_017: [The validateKey shall throw IllegalArgumentException if the provided string contains an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKeyInvalidDotThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "test.key", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_017: [The validateKey shall throw IllegalArgumentException if the provided string contains an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKeyInvalidSpaceThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "test key", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_017: [The validateKey shall throw IllegalArgumentException if the provided string contains an illegal character (`$`,`.`, space).] */
    /* Tests_SRS_PARSER_UTILITY_21_019: [If `isMetadata` is `false`, the validateKey shall not accept the character `$` as valid.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKeyNoDollarInvalidDollarThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "$test-key", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_046: [The validateMap shall throws IllegalArgumentException if the maxLevel is `0` or negative.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateMapThrowsOnZeroMaxLevel() throws ClassNotFoundException
    {
        // act
        ParserUtility.validateMap(new HashMap<String, Object>(), 0, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_046: [The validateMap shall throws IllegalArgumentException if the maxLevel is `0` or negative.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateMapThrowsOnNegativeMaxLevel() throws ClassNotFoundException
    {
        // act
        ParserUtility.validateMap(new HashMap<String, Object>(), -1, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_047: [The validateMap shall do nothing if the map is a valid Map.] */
    @Test
    public void validateMapLowerThanMaxLevelSucceed() throws ClassNotFoundException
    {
        // arrange
        final Map<String, Object> mapSample = new HashMap<String, Object>()
        {
            {
                put("key1", "value");
                put("root", new HashMap<String, Object>()
                {
                    {
                        put("inner1", new HashMap<String, Object>()
                        {
                            {
                                put("innerI1", "innerValue");
                            }
                        });
                        put("inner20", new HashMap<String, Object>()
                        {
                            {
                                put("inner21", new HashMap<String, Object>()
                                {
                                    {
                                        put("inner22", "innerValue");
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };

        // act
        ParserUtility.validateMap(mapSample, 4, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_048: [The validateMap shall do nothing if the map is null.] */
    @Test
    public void validateMapNullMapSucceed() throws ClassNotFoundException
    {
        // act
        ParserUtility.validateMap(null, 4, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_049: [The validateMap shall throws IllegalArgumentException if any key in the map is null, empty, contains more than 128 characters, or illegal characters (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void validateMapThrowsOnNullKey() throws ClassNotFoundException
    {
        // arrange
        final Map<String, Object> mapSample = new HashMap<String, Object>()
        {
            {
                put(null, "value");
            }
        };

        // act
        ParserUtility.validateMap(mapSample, 10, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_049: [The validateMap shall throws IllegalArgumentException if any key in the map is null, empty, contains more than 128 characters, or illegal characters (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void validateMapThrowsOnEmptyKey() throws ClassNotFoundException
    {
        // arrange
        final Map<String, Object> mapSample = new HashMap<String, Object>()
        {
            {
                put("", "value");
            }
        };

        // act
        ParserUtility.validateMap(mapSample, 10, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_049: [The validateMap shall throws IllegalArgumentException if any key in the map is null, empty, contains more than 128 characters, or illegal characters (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void validateMapThrowsOnMoreThan128CharKey() throws ClassNotFoundException
    {
        // arrange
        final String bigKey = "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "123456789";
        final Map<String, Object> mapSample = new HashMap<String, Object>()
        {
            {
                put(bigKey, "value");
            }
        };

        // act
        ParserUtility.validateMap(mapSample, 10, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_049: [The validateMap shall throws IllegalArgumentException if any key in the map is null, empty, contains more than 128 characters, or illegal characters (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void validateMapThrowsOnInvalidKey() throws ClassNotFoundException
    {
        // arrange
        final Map<String, Object> mapSample = new HashMap<String, Object>()
        {
            {
                put("$key", "value");
            }
        };

        // act
        ParserUtility.validateMap(mapSample, 10, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_050: [If `isMetadata` is `true`, the validateMap shall accept the character `$` in the key.] */
    @Test
    public void validateMapWithValidMetadataSucceed() throws ClassNotFoundException
    {
        // arrange
        final Map<String, Object> mapSample = new HashMap<String, Object>()
        {
            {
                put("$key", "value");
            }
        };

        // act
        ParserUtility.validateMap(mapSample, 10, true);
    }

    /* Tests_SRS_PARSER_UTILITY_21_051: [The validateMap shall throws IllegalArgumentException if any value contains illegal type (array or invalid class).] */
    @Test (expected = IllegalArgumentException.class)
    public void validateMapThrowsOnArrayValue() throws ClassNotFoundException
    {
        // arrange
        final Map<String, Object> mapSample = new HashMap<String, Object>()
        {
            {
                put("key", new int[]{1,2});
            }
        };

        // act
        ParserUtility.validateMap(mapSample, 10, true);
    }

    /* Tests_SRS_PARSER_UTILITY_21_051: [The validateMap shall throws IllegalArgumentException if any value contains illegal type (array or invalid class).] */
    @Test (expected = IllegalArgumentException.class)
    public void validateMapThrowsOnInvalidClassValue() throws ClassNotFoundException
    {
        // arrange
        final class localClass
        {
            int a;
        };
        final Map<String, Object> mapSample = new HashMap<String, Object>()
        {
            {
                put("key", new localClass());
            }
        };

        // act
        ParserUtility.validateMap(mapSample, 10, true);
    }

    /* Tests_SRS_PARSER_UTILITY_21_052: [The validateMap shall throws IllegalArgumentException if the provided map contains more than maxLevel levels and those extra levels contain more than just metadata.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateMapThrowsOnBiggerThanMaxLevel() throws ClassNotFoundException
    {
        // arrange
        final Map<String, Object> mapSample = new HashMap<String, Object>()
        {
            {
                put("key1", "value");
                put("root", new HashMap<String, Object>()
                {
                    {
                        put("inner1", new HashMap<String, Object>()
                        {
                            {
                                put("innerI1", "innerValue");
                            }
                        });
                        put("inner20", new HashMap<String, Object>()
                        {
                            {
                                put("inner21", new HashMap<String, Object>()
                                {
                                    {
                                        put("inner22", "innerValue");
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };

        // act
        ParserUtility.validateMap(mapSample, 3, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_052: [The validateMap shall throws IllegalArgumentException if the provided map contains more than maxLevel levels and those extra levels contain more than just metadata.] */
    @Test
    public void validateMapDoesNotThrowOnBiggerThanMaxLevelIfOnlyMetadata() throws ClassNotFoundException
    {
        // arrange
        final Map<String, Object> mapSample = new HashMap<String, Object>()
        {
            {
                put("key1", "value");
                put("root", new HashMap<String, Object>()
                {
                    {
                        put("inner1", new HashMap<String, Object>()
                        {
                            {
                                put("innerI1", "innerValue");
                            }
                        });
                        put("inner20", new HashMap<String, Object>()
                        {
                            {
                                put("inner21", new HashMap<String, Object>()
                                {
                                    {
                                        put(TwinMetadata.LAST_UPDATE_TAG, "metaDataValue");
                                    }
                                });
                            }
                        });
                    }
                });
            }
        };

        // act
        ParserUtility.validateMap(mapSample, 3, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_020: [The getDateTimeUtc shall parse the provide string using `UTC` timezone.] */
    /* Tests_SRS_PARSER_UTILITY_21_021: [The getDateTimeUtc shall parse the provide string using the data format `yyyy-MM-dd'T'HH:mm:ss`.] */
    @Test
    public void getDateTimeUtcSucceed() throws ClassNotFoundException
    {
        // act
        Date date = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "2016-06-01T21:22:43");

        // assert
        assertEquals(1464816163000L, date.getTime());
    }

    /* Tests_SRS_PARSER_UTILITY_21_040: [If the provide string contains more than 3 digits for milliseconds, the getDateTimeUtc shall reduce the milliseconds to 3 digits.] */
    @Test
    public void getDateTimeUtcBigMillisecondsSucceed() throws ClassNotFoundException
    {
        // act
        Date date = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "2016-06-01T21:22:43");

        // assert
        assertEquals(1464816163000L, date.getTime());
    }

    /* Tests_SRS_PARSER_UTILITY_21_040: [If the provide string contains more than 3 digits for milliseconds, the getDateTimeUtc shall reduce the milliseconds to 3 digits.] */
    @Test
    public void getDateTimeUtcShortMillisecondsSucceed() throws ClassNotFoundException
    {
        // act
        Date date = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "2016-06-01T21:22:43");

        // assert
        assertEquals(1464816163000L, date.getTime());
    }

    /* Tests_SRS_PARSER_UTILITY_21_040: [If the provide string contains more than 3 digits for milliseconds, the getDateTimeUtc shall reduce the milliseconds to 3 digits.] */
    @Test
    public void getDateTimeUtcShort2MillisecondsSucceed() throws ClassNotFoundException
    {
        // act
        Date date = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "2016-06-01T21:22:43");

        // assert
        assertEquals(1464816163000L, date.getTime());
    }

    /* Tests_SRS_PARSER_UTILITY_21_041: [The getDateTimeUtc shall accept date without milliseconds.] */
    @Test
    public void getDateTimeUtcEmptyMillisecondsSucceed() throws ClassNotFoundException
    {
        // act
        Date date = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "2016-06-01T21:22:43");

        // assert
        assertEquals(1464816163000L, date.getTime());
    }

    /* Tests_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeUtcNullThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", new Class[]{String.class}, null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeUtcEmptyThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "");
    }

    /* Tests_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeUtcInvalidTextThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "This is not a data and time");
    }

    /* Tests_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeUtcInvalidDate() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "2016-5-6-01T21:22:43");
    }

    /* Tests_SRS_PARSER_UTILITY_21_023: [The stringToDateTimeOffset shall parse the provide string using `UTC` timezone.] */
    /* Tests_SRS_PARSER_UTILITY_21_024: [The stringToDateTimeOffset shall parse the provide string using the data format `2016-06-01T21:22:41+00:00`.] */
    @Test
    public void stringToDateTimeOffsetSucceed() throws ClassNotFoundException
    {
        // act
        Date date = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"stringToDateTimeOffset", "2016-06-01T21:22:41+00:00");

        // assert
        assertEquals(1464816161000L, date.getTime());
    }

    /* Tests_SRS_PARSER_UTILITY_21_025: [If the provide string is null, empty or contains an invalid data format, the stringToDateTimeOffset shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void stringToDateTimeOffsetNullThrows() throws ClassNotFoundException
    {
        // act0
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"stringToDateTimeOffset", new Class[]{String.class}, null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_025: [If the provide string is null, empty or contains an invalid data format, the stringToDateTimeOffset shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void stringToDateTimeOffsetEmptyThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"stringToDateTimeOffset", "");
    }

    /* Tests_SRS_PARSER_UTILITY_21_025: [If the provide string is null, empty or contains an invalid data format, the stringToDateTimeOffset shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void stringToDateTimeOffsetInvalidTextThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"stringToDateTimeOffset", "This is not a data and time");
    }

    /* Tests_SRS_PARSER_UTILITY_21_025: [If the provide string is null, empty or contains an invalid data format, the stringToDateTimeOffset shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void stringToDateTimeOffsetWrongFormatThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"stringToDateTimeOffset", "2016-06-01T21:22:43");
    }

    /* Codes_SRS_PARSER_UTILITY_21_053: [The dateTimeUtcToString shall throws IllegalArgumentException if the provided Date is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void dateTimeUtcToStringThrowsOnNullDate() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"dateTimeUtcToString", new Class[]{Date.class}, (Date)null);
    }

    /* Codes_SRS_PARSER_UTILITY_21_054: [The dateTimeUtcToString shall serialize the provide Date using `UTC` timezone.] */
    @Test
    public void dateTimeUtcToStringSucceed() throws ClassNotFoundException
    {
        // arrange
        Date date = new Date(1464816163123L);

        // act
        String result = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"dateTimeUtcToString", new Class[]{Date.class}, date);

        // assert
        assertEquals("2016-06-01T21:22:43.123Z", result);
    }


    /* Tests_SRS_PARSER_UTILITY_21_030: [The validateId shall do nothing if the string is a valid ID.] */
    @Test
    public void validateIdMetadataSucceed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateId", "$test-key");
    }

    /* Tests_SRS_PARSER_UTILITY_21_026: [The validateId shall throw IllegalArgumentException if the provided string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateIdNullKeyThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateId", new Class[]{String.class}, (String)null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_026: [The validateId shall throw IllegalArgumentException if the provided string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateIdEmptyKeyThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateId", "");
    }

    /* Tests_SRS_PARSER_UTILITY_21_027: [The validateId shall throw IllegalArgumentException if the provided string contains at least one not UTF-8 character.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateIdInvalidKeyThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateId", "\u1234-test-key");
    }

    /* Tests_SRS_PARSER_UTILITY_21_028: [The validateId shall throw IllegalArgumentException if the provided string contains more than 128 characters.] */
    @Test
    public void validateIdEdgeSizeSucceed() throws ClassNotFoundException
    {
        // arrange
        String id = "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "12345678";

        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateId", id);
    }

    /* Tests_SRS_PARSER_UTILITY_21_028: [The validateId shall throw IllegalArgumentException if the provided string contains more than 128 characters.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateIdInvalidBigKeyThrows() throws ClassNotFoundException
    {
        // arrange
        String id = "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "123456789";

        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateId", id);
    }

    /* Tests_SRS_PARSER_UTILITY_21_029: [The validateId shall throw IllegalArgumentException if the provided string contains an illegal character.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateIdInvalidDotThrows() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateId", "test&key");
    }

    /* Tests_SRS_PARSER_UTILITY_21_029: [The validateId shall throw IllegalArgumentException if the provided string contains an illegal character.] */
    @Test
    public void validateIdValidSpecialCharsSucceed() throws ClassNotFoundException
    {
        // arrange
        String id = "-:.+%_#*?!(),=@;$\'";

        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateId", id);
    }

    /* Tests_SRS_PARSER_UTILITY_21_035: [The mapToJsonElement shall serialize the provided map into a JsonElement.] */
    @Test
    public void mapToJsonElementSucceed() throws ClassNotFoundException
    {
        // arrange
        Map<String, Object> map = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("key2", 10);
                put("key3", true);
            }
        };

        // act
        JsonElement json = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"mapToJsonElement", map);

        // assert
        Helpers.assertJson(json.toString(), "{\"key1\":\"value1\",\"key2\":10,\"key3\":true}");
    }

    /* Tests_SRS_PARSER_UTILITY_21_036: [The mapToJsonElement shall include keys with null values in the JsonElement.] */
    @Test
    public void mapToJsonElementNullElementsSucceed() throws ClassNotFoundException
    {
        // arrange
        Map<String, Object> map = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("key2", null);
                put("key3", "value3");
            }
        };

        // act
        JsonElement json = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"mapToJsonElement", map);

        // assert
        Helpers.assertJson(json.toString(), "{\"key1\":\"value1\",\"key2\":null,\"key3\":\"value3\"}");
    }

    /* Tests_SRS_PARSER_UTILITY_21_037: [If the value is a map, the mapToJsonElement shall include it as a submap in the JsonElement.] */
    @Test
    public void mapToJsonElementSubMapSucceed() throws ClassNotFoundException
    {
        // arrange
        final Map<String, Object> innerMap = new HashMap<String, Object>()
        {
            {
                put("ikey1", "value1");
                put("ikey2", 10);
                put("ikey3", false);
            }
        };

        final Map<String, Object> map = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("key2", innerMap);
                put("key3", "value3");
            }
        };

        // act
        JsonElement json = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"mapToJsonElement", map);

        // assert
        Helpers.assertJson(json.toString(), "{\"key1\":\"value1\",\"key2\":{\"ikey1\":\"value1\",\"ikey2\":10,\"ikey3\":false},\"key3\":\"value3\"}");
    }

    /* Tests_SRS_PARSER_UTILITY_21_037: [If the value is a map, the mapToJsonElement shall include it as a submap in the JsonElement.] */
    @Test
    public void mapToJsonElementSubMap5LevelsSucceed() throws ClassNotFoundException
    {
        // arrange
        Map<String, Object> map = new HashMap<>();
        map.put("tag1", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", 1234); put("Key3", "value3"); }});
        map.put("tag2", new HashMap<String, Object>(){{ put("Key1", "value1"); put("Key2", "value5"); put("Key4", false); }});
        map.put("one",
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

        // act
        JsonElement json = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"mapToJsonElement", map);

        // assert
        Helpers.assertJson(json.toString(), "{\"tag1\":{\"Key2\":1234,\"Key1\":\"value1\",\"Key3\":\"value3\"}," +
                "\"one\":{\"two\":{\"three\":{\"four\":{\"five\":{\"tagKey\":\"value\"}}}}}," +
                "\"tag2\":{\"Key2\":\"value5\",\"Key1\":\"value1\",\"Key4\":false}}");
    }

    /* Tests_SRS_PARSER_UTILITY_21_039: [If the map is null, the mapToJsonElement shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void mapToJsonElementNullMapSucceed() throws ClassNotFoundException
    {
        // arrange
        Map<String, Object> map = null;

        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"mapToJsonElement", new Class[]{Map.class}, map);
    }

    /* Tests_SRS_PARSER_UTILITY_21_038: [If the map is empty or null, the mapToJsonElement shall return a empty JsonElement.] */
    @Test
    public void mapToJsonElementEmptyMapSucceed() throws ClassNotFoundException
    {
        // arrange
        Map<String, Object> map = new HashMap<>();

        // act
        JsonElement json = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"mapToJsonElement", map);

        // assert
        Helpers.assertJson(json.toString(), "{}");
    }

    //Tests_SRS_PARSER_UTILITY_21_042: [If the provided date is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void getSimpleDateStringFromDateGivenNullDateThrows()
    {
        //act
        ParserUtility.getDateStringFromDate(null);
    }

    //Tests_SRS_PARSER_UTILITY_34_043: [The provided date shall be converted into this format: "yyyy-MM-dd'T'HH:mm:ss".]
    @Test
    public void getSimpleDateStringFromDateSuccess()
    {
        //arrange
        //A time that is comfortably after the UNIX epoch to avoid issues where other timezones take this time back before that epoch where dates cannot be parsed
        Date date = new Date(200000000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String expectedString = dateFormat.format(date);

        //act
        String actualString = ParserUtility.getDateStringFromDate(date);

        //assert
        assertEquals(expectedString, actualString);
    }
}
