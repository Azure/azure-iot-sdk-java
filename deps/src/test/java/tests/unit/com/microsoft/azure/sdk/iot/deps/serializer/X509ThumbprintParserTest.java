// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.X509ThumbprintParser;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Code Coverage:
 * Methods: 100%
 * Lines: 100%
 */
public class X509ThumbprintParserTest
{
    private static final String SAMPLE_THUMBPRINT1 = "0000000000000000000000000000000000000000";
    private static final String SAMPLE_THUMBPRINT2 = "1111111111111111111111111111111111111111";

    //Tests_SRS_X509ThumbprintParser_34_007: [This method shall return a json representation of this.]
    @Test
    public void testToJson()
    {
        //arrange
        String expectedJson = "{\"primaryThumbprint\":\"" + SAMPLE_THUMBPRINT1 + "\",\"secondaryThumbprint\":\"" + SAMPLE_THUMBPRINT2 + "\"}";
        X509ThumbprintParser parser = new X509ThumbprintParser(SAMPLE_THUMBPRINT1, SAMPLE_THUMBPRINT2);

        //act
        String actualJson = parser.toJson();

        //assert
        assertEquals(expectedJson, actualJson);
    }

    //Tests_SRS_X509ThumbprintParser_34_008: [The parser shall create and return an instance of a X509ThumbprintParser object that holds the provided primary and secondary thumbprints.]
    @Test
    public void testFromThumbprints()
    {
        //act
        X509ThumbprintParser parser = new X509ThumbprintParser(SAMPLE_THUMBPRINT1, SAMPLE_THUMBPRINT2);

        //assert
        String actualPrimaryThumbprint = Deencapsulation.getField(parser, "primaryThumbprint");
        String actualSecondaryThumbprint = Deencapsulation.getField(parser, "secondaryThumbprint");
        assertEquals(SAMPLE_THUMBPRINT1, actualPrimaryThumbprint);
        assertEquals(SAMPLE_THUMBPRINT2, actualSecondaryThumbprint);
    }

    //Tests_SRS_X509ThumbprintParser_34_009: [The parser shall create and return an instance of a X509ThumbprintParser object based off the provided json.]
    @Test
    public void testFromJson()
    {
        //arrange
        String json = "{\"primaryThumbprint\":\"" + SAMPLE_THUMBPRINT1 + "\",\"secondaryThumbprint\":\"" + SAMPLE_THUMBPRINT2 + "\"}";

        //act
        X509ThumbprintParser parser = new X509ThumbprintParser(json);

        //assert
        String actualPrimaryThumbprint = Deencapsulation.getField(parser, "primaryThumbprint");
        String actualSecondaryThumbprint = Deencapsulation.getField(parser, "secondaryThumbprint");
        assertEquals(SAMPLE_THUMBPRINT1, actualPrimaryThumbprint);
        assertEquals(SAMPLE_THUMBPRINT2, actualSecondaryThumbprint);
    }

    //Tests_SRS_X509ThumbprintParser_34_002: [If the provided primaryThumbprint value is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void testSetPrimaryThumbprintNullThrows()
    {
        //act
        new X509ThumbprintParser().setPrimaryThumbprint(null);
    }

    //Tests_SRS_X509ThumbprintParser_34_005: [If the provided secondaryThumbprint value is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void testSetSecondaryThumbprintNullThrows()
    {
        //act
        new X509ThumbprintParser().setSecondaryThumbprint(null);
    }

    //Tests_SRS_X509ThumbprintParser_34_001: [This method shall return the value of primaryThumbprint]
    //Tests_SRS_X509ThumbprintParser_34_003: [This method shall set the value of primaryThumbprint to the provided value.]
    //Tests_SRS_X509ThumbprintParser_34_004: [This method shall return the value of secondaryThumbprint]
    //Tests_SRS_X509ThumbprintParser_34_006: [This method shall set the value of secondaryThumbprint to the provided value.]
    @Test
    public void testGettersAndSetters()
    {
        //arrange
        X509ThumbprintParser parser = new X509ThumbprintParser();

        //act
        parser.setPrimaryThumbprint(SAMPLE_THUMBPRINT1);
        parser.setSecondaryThumbprint(SAMPLE_THUMBPRINT2);

        //assert
        assertEquals(SAMPLE_THUMBPRINT1, parser.getPrimaryThumbprint());
        assertEquals(SAMPLE_THUMBPRINT2, parser.getSecondaryThumbprint());
    }

    //Tests_SRS_X509ThumbprintParser_34_010: [If the provided json is null or empty or cannot be parsed into an X509Thumbprint object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullJson()
    {
        //act
        new X509ThumbprintParser(null);
    }

    //Tests_SRS_X509ThumbprintParser_34_010: [If the provided json is null or empty or cannot be parsed into an X509Thumbprint object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyJson()
    {
        //act
        new X509ThumbprintParser("");
    }

    //Tests_SRS_X509ThumbprintParser_34_010: [If the provided json is null or empty or cannot be parsed into an X509Thumbprint object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidJson()
    {
        //act
        new X509ThumbprintParser("{");
    }
}
