// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.SymmetricKeyParser;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Code Coverage:
 * Methods: 100%
 * Lines: 100%
 */
public class SymmetricKeyParserTest
{
    private static final String TEST_KEY1 = "000000000000000000000000";
    private static final String TEST_KEY2 = "111111111111111111111111";

    //Tests_SRS_SymmetricKeyParser_34_008: [This constructor shall create and return an instance of a SymmetricKeyParser object that holds the provided primary and secondary keys.]
    @Test
    public void testConstructorWithKeys()
    {
        //act
        SymmetricKeyParser parser = new SymmetricKeyParser(TEST_KEY1, TEST_KEY2);

        //assert
        String actualPrimaryKey = Deencapsulation.getField(parser, "primaryKey");
        String actualSecondaryKey = Deencapsulation.getField(parser, "secondaryKey");
        assertEquals(TEST_KEY1, actualPrimaryKey);
        assertEquals(TEST_KEY2, actualSecondaryKey);
    }

    //Tests_SRS_SymmetricKeyParser_34_009: [This constructor shall create and return an instance of a SymmetricKeyParser object based off the provided json.]
    @Test
    public void testConstructorWithJson()
    {
        //arrange
        String json = "{\"primaryKey\":\"" + TEST_KEY1 + "\",\"secondaryKey\":\"" + TEST_KEY2 + "\"}";

        //act
        SymmetricKeyParser parser = new SymmetricKeyParser(json);

        //assert
        assertEquals(TEST_KEY1, parser.getPrimaryKey());
        assertEquals(TEST_KEY2, parser.getSecondaryKey());
    }

    //Tests_SRS_SymmetricKeyParser_34_007: [The parser shall return a json representation of the provided SymmetricKeyParser.]
    @Test
    public void testToJson()
    {
        //arrange
        SymmetricKeyParser parser = new SymmetricKeyParser(TEST_KEY1, TEST_KEY2);
        String expectedJson = "{\"primaryKey\":\"" + TEST_KEY1 + "\",\"secondaryKey\":\"" + TEST_KEY2 + "\"}";

        //act
        String actualJson = parser.toJson();

        //assert
        assertEquals(expectedJson, actualJson);
    }

    //Tests_SRS_SymmetricKeyParser_34_001: [This method shall return the value of primaryKey]
    //Tests_SRS_SymmetricKeyParser_34_003: [This method shall set the value of primaryKey to the provided value.]
    //Tests_SRS_SymmetricKeyParser_34_004: [This method shall return the value of secondaryKey]
    //Tests_SRS_SymmetricKeyParser_34_006: [This method shall set the value of secondaryKey to the provided value.]
    @Test
    public void testGettersAndSetters()
    {
        //arrange
        SymmetricKeyParser parser = new SymmetricKeyParser(TEST_KEY1, TEST_KEY2);

        //act
        parser.setPrimaryKey(TEST_KEY2);
        parser.setSecondaryKey(TEST_KEY1);

        //assert
        assertEquals(TEST_KEY2, parser.getPrimaryKey());
        assertEquals(TEST_KEY1, parser.getSecondaryKey());
    }

    //Tests_SRS_SymmetricKeyParser_34_002: [If the provided primaryKey value is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void setPrimaryKeyNullValueThrowsIllegalArgumentException()
    {
        //arrange
        SymmetricKeyParser parser = new SymmetricKeyParser("","");

        //act
        parser.setPrimaryKey(null);
    }

    //Tests_SRS_SymmetricKeyParser_34_005: [If the provided secondaryKey value is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void setSecondaryKeyNullValueThrowsIllegalArgumentException()
    {
        //arrange
        SymmetricKeyParser parser = new SymmetricKeyParser("","");

        //act
        parser.setSecondaryKey(null);
    }

    //Tests_SRS_SYMMETRIC_KEY_PARSER_34_011: [If the provided json null, empty, or cannot be parsed to a SymmetricKeyParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullJson()
    {
        //act
        new SymmetricKeyParser(null);
    }

    //Tests_SRS_SYMMETRIC_KEY_PARSER_34_011: [If the provided json null, empty, or cannot be parsed to a SymmetricKeyParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyJson()
    {
        //act
        new SymmetricKeyParser("");
    }

    //Tests_SRS_SYMMETRIC_KEY_PARSER_34_011: [If the provided json null, empty, or cannot be parsed to a SymmetricKeyParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidJson()
    {
        //act
        new SymmetricKeyParser("{");
    }

    //Tests_SRS_SYMMETRIC_KEY_PARSER_34_010: [If the provided json is missing the field for either PrimaryKey or SecondaryKey, or either is missing a value, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void missingPrimaryKeyInJsonThrows()
    {
        //arrange
        String json = "{\"secondaryKey\":\"" + TEST_KEY2 + "\"}";

        //act
        new SymmetricKeyParser(json);
    }

    //Tests_SRS_SYMMETRIC_KEY_PARSER_34_010: [If the provided json is missing the field for either PrimaryKey or SecondaryKey, or either is missing a value, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void missingSecondaryKeyInJsonThrows()
    {
        //arrange
        String json = "{\"primaryKey\":\"" + TEST_KEY1 + "\"}";

        //act
        new SymmetricKeyParser(json);
    }
}
