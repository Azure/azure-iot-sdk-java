/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.QueryRequestParser;

import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/*
    Unit tests for QueryResponseParser
    Coverage result : method - 100%, line - 100%
 */
public class QueryRequestParserTest
{
    //Tests_SRS_QUERY_REQUEST_PARSER_25_001: [The constructor shall create an instance of the QueryRequestParser.]
    //Tests_SRS_QUERY_REQUEST_PARSER_25_002: [The constructor shall set the query value with the provided query.]
    @Test
    public void constructorSavesQuery() throws IllegalArgumentException
    {
        //arrange
        final String testQuery = "select * from abc";

        //act
        QueryRequestParser testParser = new QueryRequestParser(testQuery);

        //assert
        String actualQuery = Deencapsulation.getField(testParser, "query");
        assertEquals(testQuery, actualQuery);
    }

    //Tests_SRS_QUERY_REQUEST_PARSER_25_003: [If the provided query is null, empty, or not valid, the constructor shall throws IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsInvalidQuery1() throws IllegalArgumentException
    {
        //arrange
        final String testQuery = "from abc";

        //act
        QueryRequestParser testParser = new QueryRequestParser(testQuery);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsInvalidQuery2() throws IllegalArgumentException
    {
        //arrange
        final String testQuery = "select *";

        //act
        QueryRequestParser testParser = new QueryRequestParser(testQuery);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsInvalidQuery3() throws IllegalArgumentException
    {
        //arrange
        final String testQuery = "select \u1234 from abc";

        //act
        QueryRequestParser testParser = new QueryRequestParser(testQuery);
    }

    //Tests_SRS_QUERY_REQUEST_PARSER_25_004: [The toJson shall return a string with a json that represents the contents of the QueryRequestParser.]
    @Test
    public void parserReturnsProperJson() throws IllegalArgumentException
    {
        //arrange
        final String testQuery = "select * from abc";
        final String actualJsonQuery = "{\n" +
                "    \"query\": \"" + testQuery + "\"\n" +
                "}";
        QueryRequestParser testParser = new QueryRequestParser(testQuery);

        //act
        String testJsonQuery = testParser.toJson();

        //assert
        Helpers.assertJson(actualJsonQuery, testJsonQuery);
    }
}
