/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.QueryResponseParser;
import com.microsoft.azure.sdk.iot.service.devicetwin.QueryResponse;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/*
 *  Unit test for Query Response
 *  Coverage : 100% method, 100% line
 */
public class QueryResponseTest
{
    @Mocked
    QueryResponseParser mockedQueryResponseParser;

    private void assertEqualsIterator(Iterator test, Iterator actual)
    {
        while(test.hasNext())
        {
            assertTrue(actual.hasNext());
            assertEquals(test.next(), actual.next());
        }

        assertFalse(actual.hasNext());
    }

    //Tests_SRS__QUERY_RESPONSE_25_001: [The constructor shall parse the json response using QueryResponseParser and set the iterator.]
    @Test
    public void constructorSucceeds() throws IOException
    {
        //arrange
        final String json = "testJson";
        final List<String> testList = new LinkedList();
        testList.add("testValue");

        new NonStrictExpectations()
        {
            {
                mockedQueryResponseParser.getJsonItems();
                result = testList;
            }
        };

        //act
        QueryResponse testResponse = Deencapsulation.newInstance(QueryResponse.class, json);

        //assert
        assertEqualsIterator(testList.iterator(), Deencapsulation.getField(testResponse, "responseElementsIterator" ));
    }

    //**SRS_QUERY_RESPONSE_25_002: [**If the jsonString is null or empty, the constructor shall throw an IllegalArgumentException.**]**
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullString() throws IOException
    {
        //arrange
        final String json = null;

        //act
        QueryResponse testResponse = Deencapsulation.newInstance(QueryResponse.class, String.class);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyString() throws IOException
    {
        //arrange
        final String json = "";

        //act
        QueryResponse testResponse = Deencapsulation.newInstance(QueryResponse.class, json);
    }

    @Test (expected = IOException.class)
    public void constructorThrowsWhenParserThrows() throws IOException
    {
        //arrange
        final String json = "testJson";
        final List<String> testList = new LinkedList();
        testList.add("testValue");

        new NonStrictExpectations()
        {
            {
                new QueryResponseParser(anyString);
                result = new IOException();
            }
        };

        //act
        QueryResponse testResponse = Deencapsulation.newInstance(QueryResponse.class, json);
    }

    //Tests_SRS__QUERY_RESPONSE_25_003: [The method shall return true if next element from QueryResponse is available and false otherwise.]
    @Test
    public void hasNextReturnsTrue() throws IOException
    {
        //arrange
        final String json = "testJson";
        final List<String> testList = new LinkedList();
        testList.add("testValue");

        new NonStrictExpectations()
        {
            {
                mockedQueryResponseParser.getJsonItems();
                result = testList;
            }
        };
        QueryResponse testResponse = Deencapsulation.newInstance(QueryResponse.class, json);

        //act
        assertTrue(testResponse.hasNext());

        //assert
        assertEqualsIterator(testList.iterator(), Deencapsulation.getField(testResponse, "responseElementsIterator" ));
    }

    @Test
    public void hasNextReturnsFalse() throws IOException
    {
        //arrange
        final String json = "testJson";
        final List<String> testList = new LinkedList<>();

        new NonStrictExpectations()
        {
            {
                mockedQueryResponseParser.getJsonItems();
                result = testList;
            }
        };
        QueryResponse testResponse = Deencapsulation.newInstance(QueryResponse.class, json);

        //act
        assertFalse(testResponse.hasNext());
    }

    //Tests_SRS__QUERY_RESPONSE_25_004: [The method shall return the next element for this QueryResponse.]
    @Test
    public void nextReturnsWhenFound() throws IOException
    {
        //arrange
        final String json = "testJson";
        final List<String> testList = new LinkedList<>();
        testList.add("testValue");

        new NonStrictExpectations()
        {
            {
                mockedQueryResponseParser.getJsonItems();
                result = testList;
            }
        };
        QueryResponse testResponse = Deencapsulation.newInstance(QueryResponse.class, json);

        //act
        assertTrue(testResponse.hasNext());
        assertNotNull(testResponse.next());
    }

    @Test (expected = NoSuchElementException.class)
    public void nextThrowsWhenNotFound() throws IOException
    {
        //arrange
        final String json = "testJson";
        final List<String> testList = new LinkedList<>();

        new NonStrictExpectations()
        {
            {
                mockedQueryResponseParser.getJsonItems();
                result = testList;
            }
        };
        QueryResponse testResponse = Deencapsulation.newInstance(QueryResponse.class, json);

        //act
        assertFalse(testResponse.hasNext());
        assertNull(testResponse.next());
    }
}
