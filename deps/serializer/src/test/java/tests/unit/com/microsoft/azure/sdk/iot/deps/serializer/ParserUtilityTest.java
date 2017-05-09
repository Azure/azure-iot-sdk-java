// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import mockit.Deencapsulation;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for serializer utility helpers
 */
public class ParserUtilityTest
{
    /* Tests_SRS_PARSER_UTILITY_21_001: [The validateStringUTF8 shall do nothing if the string is valid.] */
    @Test
    public void validateStringUTF8_success() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateStringUTF8", "test-string1#");
    }

    /* Tests_SRS_PARSER_UTILITY_21_002: [The validateStringUTF8 shall throw IllegalArgumentException is the provided string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateStringUTF8_null_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateStringUTF8", null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_002: [The validateStringUTF8 shall throw IllegalArgumentException is the provided string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateStringUTF8_empty_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateStringUTF8", "");
    }

    /* Tests_SRS_PARSER_UTILITY_21_003: [The validateStringUTF8 shall throw IllegalArgumentException is the provided string contains at least one not UTF-8 character.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateStringUTF8_invalid_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateStringUTF8", "\u1234test-string1#");
    }

    /* Tests_SRS_PARSER_UTILITY_21_004: [The validateBlobName shall do nothing if the string is valid.] */
    @Test
    public void validateBlobName_success() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateBlobName", "test-device1/image.jpg");
    }

    /* Tests_SRS_PARSER_UTILITY_21_005: [The validateBlobName shall throw IllegalArgumentException is the provided blob name is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateBlobName_null_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateBlobName", null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_005: [The validateBlobName shall throw IllegalArgumentException is the provided blob name is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateBlobName_empty_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateBlobName", "");
    }

    /* Tests_SRS_PARSER_UTILITY_21_006: [The validateBlobName shall throw IllegalArgumentException is the provided blob name contains at least one not UTF-8 character.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateBlobName_invalid_utf8_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateBlobName", "\u1234test-string1/image.jpg");
    }

    /* Tests_SRS_PARSER_UTILITY_21_007: [The validateBlobName shall throw IllegalArgumentException is the provided blob name contains more than 1024 characters.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateBlobName_invalid_bigName_failed() throws ClassNotFoundException
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

    /* Tests_SRS_PARSER_UTILITY_21_008: [The validateBlobName shall throw IllegalArgumentException is the provided blob name contains more than 254 path segments.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateBlobName_invalid_invalidPath_failed() throws ClassNotFoundException
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

    /* Tests_SRS_PARSER_UTILITY_21_009: [The validateInteger shall do nothing if the object is valid.] */
    @Test
    public void validateObject_integer_success() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateObject", 1234);
    }

    /* Tests_SRS_PARSER_UTILITY_21_009: [The validateBoolean shall do nothing if the object is valid.] */
    @Test
    public void validateObject_boolean_success() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateObject", true);
    }

    /* Tests_SRS_PARSER_UTILITY_21_010: [The validateInteger shall throw IllegalArgumentException is the provided object is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateInteger_null_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateObject", null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_013: [The validateKey shall do nothing if the string is a valid key.] */
    /* Tests_SRS_PARSER_UTILITY_21_019: [If `isMetadata` is `false`, the validateKey shall not accept the character `$` as valid.] */
    @Test
    public void validateKey_noMetadata_success() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "test-key", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_013: [The validateKey shall do nothing if the string is a valid key.] */
    /* Tests_SRS_PARSER_UTILITY_21_018: [If `isMetadata` is `true`, the validateKey shall accept the character `$` as valid.] */
    @Test
    public void validateKey_metadata_success() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "$test-key", true);
    }

    /* Tests_SRS_PARSER_UTILITY_21_014: [The validateKey shall throw IllegalArgumentException is the provided string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKey_nullKey_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", (String)null, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_014: [The validateKey shall throw IllegalArgumentException is the provided string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKey_emptyKey_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_015: [The validateKey shall throw IllegalArgumentException is the provided string contains at least one not UTF-8 character.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKey_invalidKey_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "\u1234-test-key", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_016: [The validateKey shall throw IllegalArgumentException is the provided string contains more than 128 characters.] */
    @Test
    public void validateKey_edgeSize_success() throws ClassNotFoundException
    {
        // arrange
        String key = "1234567890123456789012345678901234567890" +
                     "1234567890123456789012345678901234567890" +
                     "1234567890123456789012345678901234567890" +
                     "12345678";

        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", key, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_016: [The validateKey shall throw IllegalArgumentException is the provided string contains more than 128 characters.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKey_invalid_bigKey_failed() throws ClassNotFoundException
    {
        // arrange
        String key = "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "123456789";

        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", key, false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_017: [The validateKey shall throw IllegalArgumentException is the provided string contains an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKey_invalidDot_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "test.key", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_017: [The validateKey shall throw IllegalArgumentException is the provided string contains an illegal character (`$`,`.`, space).] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKey_invalidSpace_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "test key", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_017: [The validateKey shall throw IllegalArgumentException is the provided string contains an illegal character (`$`,`.`, space).] */
    /* Tests_SRS_PARSER_UTILITY_21_019: [If `isMetadata` is `false`, the validateKey shall not accept the character `$` as valid.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateKey_noDollar_invalidDollar_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"validateKey", "$test-key", false);
    }

    /* Tests_SRS_PARSER_UTILITY_21_020: [The getDateTimeUtc shall parse the provide string using `UTC` timezone.] */
    /* Tests_SRS_PARSER_UTILITY_21_021: [The getDateTimeUtc shall parse the provide string using the data format `yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'`.] */
    @Test
    public void getDateTimeUtc_success() throws ClassNotFoundException
    {
        // act
        Date date = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "2016-06-01T21:22:43.7996883Z");

        // assert
        assertEquals(1464824159883L, date.getTime());
    }

    /* Tests_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeUtc_null_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeUtc_empty_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "");
    }

    /* Tests_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeUtc_invalid_text_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "This is not a data and time");
    }

    /* Tests_SRS_PARSER_UTILITY_21_022: [If the provide string is null, empty or contains an invalid data format, the getDateTimeUtc shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeUtc_wrong_format_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeUtc", "2016-06-01T21:22:43");
    }

    /* Tests_SRS_PARSER_UTILITY_21_023: [The getDateTimeOffset shall parse the provide string using `UTC` timezone.] */
    /* Tests_SRS_PARSER_UTILITY_21_024: [The getDateTimeOffset shall parse the provide string using the data format `2016-06-01T21:22:41+00:00`.] */
    @Test
    public void getDateTimeOffset_success() throws ClassNotFoundException
    {
        // act
        Date date = Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeOffset", "2016-06-01T21:22:41+00:00");

        // assert
        assertEquals(1464816161000L, date.getTime());
    }

    /* Tests_SRS_PARSER_UTILITY_21_025: [If the provide string is null, empty or contains an invalid data format, the getDateTimeOffset shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeOffset_null_failed() throws ClassNotFoundException
    {
        // act0
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeOffset", null);
    }

    /* Tests_SRS_PARSER_UTILITY_21_025: [If the provide string is null, empty or contains an invalid data format, the getDateTimeOffset shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeOffset_empty_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeOffset", "");
    }

    /* Tests_SRS_PARSER_UTILITY_21_025: [If the provide string is null, empty or contains an invalid data format, the getDateTimeOffset shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeOffset_invalid_text_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeOffset", "This is not a data and time");
    }

    /* Tests_SRS_PARSER_UTILITY_21_025: [If the provide string is null, empty or contains an invalid data format, the getDateTimeOffset shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getDateTimeOffset_wrong_format_failed() throws ClassNotFoundException
    {
        // act
        Deencapsulation.invoke(Class.forName("com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility"),"getDateTimeOffset", "2016-06-01T21:22:43");
    }
}
