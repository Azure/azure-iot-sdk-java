/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.QueryResponseParser;
import com.microsoft.azure.sdk.iot.service.devicetwin.QueryCollectionResponse;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit Tests for QueryCollectionResponse.java
 * Methods: 100%
 * Lines: 100%
 */
public class QueryCollectionResponseTest
{
    @Mocked
    QueryResponseParser mockQueryResponseParser;

    //Tests_SRS_QUERY_COLLECTION_RESPONSE_34_001: [If the provided jsonString is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullJson()
    {
        //act
        Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {String.class, String.class}, null, "some continuation token");
    }

    //Tests_SRS_QUERY_COLLECTION_RESPONSE_34_001: [If the provided jsonString is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyJson()
    {
        //act
        Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {String.class, String.class}, "", "some continuation token");
    }

    //Tests_SRS_QUERY_COLLECTION_RESPONSE_34_002: [This constructor shall parse the provided jsonString using the QueryResponseParser class into a Collection and save it.]
    //Tests_SRS_QUERY_COLLECTION_RESPONSE_34_003: [This constructor shall save the provided continuation token.]
    @Test
    public void constructorWithJsonSuccess()
    {
        //arrange
        String expectedJson = "some valid json";
        String expectedToken = "some valid token";

        new NonStrictExpectations()
        {
            {
                new QueryResponseParser(expectedJson);
                result = mockQueryResponseParser;
            }
        };

        //act
        QueryCollectionResponse actualResponse = Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {String.class, String.class}, expectedJson, expectedToken);

        //assert
        Collection actualCollection = Deencapsulation.getField(actualResponse, "responseElementsCollection");
        String actualContinuationToken = Deencapsulation.getField(actualResponse, "continuationToken");

        assertNotNull(actualCollection);
        assertEquals(expectedToken, actualContinuationToken);
    }

    //Tests_SRS_QUERY_COLLECTION_RESPONSE_34_004: [This constructor shall save the provided continuation token and Collection.]
    @Test
    public void constructorWithCollectionSuccess()
    {
        //arrange
        String expectedToken = "some valid token";

        ArrayList<String> expectedList = new ArrayList<>();
        expectedList.add("asdf");
        expectedList.add("asdfasdf");

        //act
        QueryCollectionResponse actualResponse = Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {Collection.class, String.class}, expectedList, expectedToken);

        //assert
        Collection actualCollection = Deencapsulation.getField(actualResponse, "responseElementsCollection");
        String actualContinuationToken = Deencapsulation.getField(actualResponse, "continuationToken");

        assertEquals(expectedList, actualCollection);
        assertEquals(expectedToken, actualContinuationToken);
    }

    //Tests_SRS_QUERY_COLLECTION_RESPONSE_34_007: [If the provided Collection is null or has not items to iterate over, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithCollectionThrowsForNullCollection()
    {
        //arrange
        String expectedToken = "some valid token";

        //act
        Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {Collection.class, String.class}, null, expectedToken);
    }

    //Tests_SRS_QUERY_COLLECTION_RESPONSE_34_007: [If the provided Collection is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithCollectionThrowsForEmptyCollection()
    {
        //arrange
        String expectedToken = "some valid token";
        Collection emptyCollection = new ArrayList<String>();

        //act
        Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {Collection.class, String.class}, emptyCollection, expectedToken);
    }


    //Tests_SRS_QUERY_COLLECTION_RESPONSE_34_005: [This function shall return the saved continuation token.]
    @Test
    public void getContinuationTokenSuccess()
    {
        //arrange
        String expectedContinuationToken = "some token";
        ArrayList<String> expectedList = new ArrayList<>();
        expectedList.add("asdf");
        QueryCollectionResponse actualResponse = Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {Collection.class, String.class}, expectedList, expectedContinuationToken);

        //act
        String actualContinuationToken = actualResponse.getContinuationToken();

        //assert
        assertEquals(expectedContinuationToken, actualContinuationToken);
    }

    //Tests_SRS_QUERY_COLLECTION_RESPONSE_34_006: [This function shall return the saved Collection.]
    @Test
    public void getCollectionSuccess()
    {
        //arrange
        ArrayList<String> expectedList = new ArrayList<>();
        expectedList.add("asdf");
        QueryCollectionResponse actualResponse = Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {Collection.class, String.class}, expectedList, "any token");

        //act
        Collection actualCollection = actualResponse.getCollection();

        //assert
        assertEquals(expectedList, actualCollection);
    }
}
