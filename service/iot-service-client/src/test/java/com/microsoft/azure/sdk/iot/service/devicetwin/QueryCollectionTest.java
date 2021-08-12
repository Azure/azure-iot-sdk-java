/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.serializer.QueryRequestParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

/**
 * Unit tests for QueryCollection.java
 * Methods: 100%
 * Lines: 100%
 */
public class QueryCollectionTest
{
    @Mocked
    ParserUtility mockParserUtility;

    @Mocked
    QueryRequestParser mockQueryRequestParser;

    @Mocked
    IotHubConnectionString iotHubConnectionString;

    @Mocked
    HttpResponse mockHttpResponse;

    @Mocked
    QueryCollectionResponse mockQueryCollectionResponse;

    @Mocked
    QueryOptions mockQueryOptions;

    @Mocked
    IotHubConnectionString mockConnectionString;

    @Mocked
    HttpMethod mockHttpMethod;

    @Mocked
    DeviceOperations mockDeviceOperations;

    @Mocked
    IotHubServiceSasToken mockIotHubServiceSasToken;

    @Mocked
    Proxy mockedProxy;

    private static final String expectedRequestContinuationToken = "someContinuationToken";
    private static final String expectedResponseContinuationToken = "someNewContinuationToken";

    private static final int expectedTimeout = 10000;
    private static HashMap<String, String> expectedValidRequestHeaders;
    private static HashMap<String, String> expectedValidResponseHeaders;
    private static HashMap<String, String> expectedResponseHeadersUnknownQueryType;

    private static final int expectedPageSize = 22;

    @BeforeClass
    public static void initializeExpectedValues()
    {
        expectedValidRequestHeaders = new HashMap<>();
        expectedValidRequestHeaders.put("x-ms-max-item-count", String.valueOf(expectedPageSize));
        expectedValidRequestHeaders.put("x-ms-continuation", expectedRequestContinuationToken);

        expectedValidResponseHeaders = new HashMap<>();
        expectedValidResponseHeaders.put("x-ms-continuation", expectedResponseContinuationToken);
        expectedValidResponseHeaders.put("x-ms-item-type", "raw");

        HashMap<String, String> expectedResponseHeadersMismatchedType = new HashMap<>();
        expectedResponseHeadersMismatchedType.put("x-ms-continuation", expectedResponseContinuationToken);
        expectedResponseHeadersMismatchedType.put("x-ms-item-type", "twin");

        expectedResponseHeadersUnknownQueryType = new HashMap<>();
        expectedResponseHeadersUnknownQueryType.put("x-ms-continuation", expectedResponseContinuationToken);
        expectedResponseHeadersUnknownQueryType.put("x-ms-item-type", "Unknown");

    }

    //Tests_SRS_QUERYCOLLECTION_34_001: [If the provided query string is invalid or does not contain both SELECT and FROM, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidSqlQueryString(@Mocked final URL mockUrl)
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                ParserUtility.validateQuery((String) any);
                result = new IllegalArgumentException();
            }
        };

        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},"anyString", 20, QueryType.DEVICE_JOB, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
    }
    
    //Tests_SRS_QUERYCOLLECTION_34_002: [If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNegativePageSize(@Mocked final URL mockUrl)
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},"anyString", -1, QueryType.DEVICE_JOB, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_003: [If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForZeroPageSize(@Mocked final URL mockUrl)
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},0, QueryType.DEVICE_JOB, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_004: [If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullQueryType(@Mocked final URL mockUrl)
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},"anyString", 20, null, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_005: [If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForUnknownQueryType(@Mocked final URL mockUrl)
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},20, QueryType.UNKNOWN, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_038: [If the provided connection string, url, or http method is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithQueryThrowsForNullConnectionString(@Mocked final URL mockUrl)
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class}, "any query",20, QueryType.JOB_RESPONSE, null, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_038: [If the provided connection string, url, or http method is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithQueryThrowsForNullUrl(@Mocked final URL mockUrl)
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},"any query", 20, QueryType.JOB_RESPONSE, mockConnectionString, null, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_038: [If the provided connection string, url, or http method is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithQueryThrowsForNullHttpMethod(@Mocked final URL mockUrl)
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},"any query", 20, QueryType.JOB_RESPONSE, mockConnectionString, mockUrl, null, expectedTimeout, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_038: [If the provided connection string, url, or http method is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullConnectionString(@Mocked final URL mockUrl)
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},20, QueryType.JOB_RESPONSE, null, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_038: [If the provided connection string, url, or http method is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullUrl()
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},20, QueryType.JOB_RESPONSE, mockConnectionString, null, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_038: [If the provided connection string, url, or http method is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullHttpMethod(@Mocked final URL mockUrl)
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},20, QueryType.JOB_RESPONSE, mockConnectionString, mockUrl, null, expectedTimeout, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_006: [This function shall save the provided query, pageSize, requestQueryType, iotHubConnectionString, url, httpMethod and timeout.]
    //Tests_SRS_QUERYCOLLECTION_34_008: [The constructed QueryCollection shall be a sql query type.]
    @Test
    public void constructorSavesQueryPageSizeAndQueryType(@Mocked final URL mockUrl)
    {
        //arrange
        String expectedQuery = "someQuery";
        int expectedPageSize = 22;
        QueryType expectedQueryType = QueryType.JOB_RESPONSE;

        //act
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class}, expectedQuery, expectedPageSize, expectedQueryType, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);

        //assert
        String actualQuery = Deencapsulation.getField(queryCollection, "query");
        int actualPageSize = Deencapsulation.getField(queryCollection, "pageSize");
        QueryType actualQueryType = Deencapsulation.getField(queryCollection, "requestQueryType");
        boolean actualIsSqlQuery = Deencapsulation.getField(queryCollection, "isSqlQuery");
        HttpMethod actualHttpMethod = Deencapsulation.getField(queryCollection, "httpMethod");
        URL actualUrl = Deencapsulation.getField(queryCollection, "url");

        assertEquals(expectedQuery, actualQuery);
        assertEquals(expectedPageSize, actualPageSize);
        assertEquals(expectedQueryType, actualQueryType);
        assertTrue(actualIsSqlQuery);
        assertEquals(actualHttpMethod, mockHttpMethod);
        assertEquals(actualUrl, mockUrl);
    }

        //this.iotHubConnectionString = iotHubConnectionString;
        //this.httpMethod = httpMethod;
        //this.timeout = timeout;
        //this.url = url;

    //Tests_SRS_QUERYCOLLECTION_34_007: [This function shall save the provided pageSize, requestQueryType, iotHubConnectionString, url, httpMethod and timeout.]
    //Tests_SRS_QUERYCOLLECTION_34_009: [The constructed QueryCollection shall not be a sql query type.]
    @Test
    public void constructorSavesPageSizeAndQueryType(@Mocked final URL mockUrl)
    {
        //arrange
        int expectedPageSize = 22;
        QueryType expectedQueryType = QueryType.JOB_RESPONSE;

        //act
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class}, expectedPageSize, expectedQueryType, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);

        //assert
        int actualPageSize = Deencapsulation.getField(queryCollection, "pageSize");
        QueryType actualQueryType = Deencapsulation.getField(queryCollection, "requestQueryType");
        boolean actualIsSqlQuery = Deencapsulation.getField(queryCollection, "isSqlQuery");
        HttpMethod actualHttpMethod = Deencapsulation.getField(queryCollection, "httpMethod");
        URL actualUrl = Deencapsulation.getField(queryCollection, "url");

        assertEquals(expectedPageSize, actualPageSize);
        assertEquals(expectedQueryType, actualQueryType);
        assertFalse(actualIsSqlQuery);
        assertEquals(actualHttpMethod, mockHttpMethod);
        assertEquals(actualUrl, mockUrl);
    }

    //Tests_SRS_QUERYCOLLECTION_34_010: [If the provided connection string, url, or method is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void sendQueryRequestThrowsForNullConnectionString(@Mocked final URL mockUrl)
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class}, 22, QueryType.JOB_RESPONSE);

        //act
        Deencapsulation.invoke(queryCollection, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class, QueryOptions.class}, null, mockUrl, mockHttpMethod, expectedTimeout, mockQueryOptions, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_010: [If the provided connection string, url, or method is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void sendQueryRequestThrowsForNullURL()
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class}, 22, QueryType.JOB_RESPONSE);

        //act
        Deencapsulation.invoke(queryCollection, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class, QueryOptions.class}, mockConnectionString, null, mockHttpMethod, expectedTimeout, mockQueryOptions, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_010: [If the provided connection string, url, or method is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void sendQueryRequestThrowsForNullMethod(@Mocked final URL mockUrl)
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class}, expectedPageSize, QueryType.JOB_RESPONSE);

        //act
        Deencapsulation.invoke(queryCollection, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class, QueryOptions.class}, mockConnectionString, mockUrl, null, expectedTimeout, mockQueryOptions, expectedTimeout, mockedProxy);
    }

    //Tests_SRS_QUERYCOLLECTION_34_012: [If a continuation token is not provided from the passed in query options, but there is a continuation token saved in the latest queryCollectionResponse, that token shall be put in the query headers to continue the query.]
    //Tests_SRS_QUERYCOLLECTION_34_014: [If the provided query options is null, this object's page size shall be included in the query headers.]
    @Test
    public void sendQueryRequestGetsContinuationTokenFromCurrentIfOptionsHasNone(@Mocked final URL mockUrl) throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class}, expectedPageSize, QueryType.RAW, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
        Deencapsulation.setField(queryCollection, "isSqlQuery", false);
        Deencapsulation.setField(queryCollection, "responseContinuationToken", expectedRequestContinuationToken);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = expectedValidResponseHeaders;
            }
        };

        QueryOptions options = null;

        //act
        Deencapsulation.invoke(queryCollection, "sendQueryRequest", new Class[] {QueryOptions.class}, options);

        //assert
        new Verifications()
        {
            {
                DeviceOperations.setHeaders(expectedValidRequestHeaders);
                times = 1;
            }
        };
    }

    //Tests_SRS_QUERYCOLLECTION_34_025: [If this query is the initial query, this function shall return true.]
    @Test
    public void hasNextReturnsTrueIfItIsTheInitialQuery(@Mocked final URL mockUrl)
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class}, "some query", expectedPageSize, QueryType.DEVICE_JOB, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
        Deencapsulation.setField(queryCollection, "isInitialQuery", true);

        //act
        boolean hasNext = Deencapsulation.invoke(queryCollection, "hasNext");

        //assert
        assertTrue(hasNext);
    }

    //Tests_SRS_QUERYCOLLECTION_34_026: [If this query is not the initial query, this function shall return true if there is a continuation token and false otherwise.]
    @Test
    public void hasNextReturnsFalseIfNoContinuationToken(@Mocked final URL mockUrl) throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class}, "some query", expectedPageSize, QueryType.DEVICE_JOB, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
        Deencapsulation.setField(queryCollection, "responseContinuationToken", null);
        Deencapsulation.setField(queryCollection, "isInitialQuery", false);

        //act
        boolean hasNext = Deencapsulation.invoke(queryCollection, "hasNext");

        //assert
        assertFalse(hasNext);
    }

    //Tests_SRS_QUERYCOLLECTION_34_026: [If this query is not the initial query, this function shall return true if there is a continuation token and false otherwise.]
    @Test
    public void hasNextReturnsTrueIfContinuationTokenSaved(@Mocked final URL mockUrl) throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class}, "some query", expectedPageSize, QueryType.DEVICE_JOB, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
        Deencapsulation.setField(queryCollection, "responseContinuationToken", "any continuation token");
        Deencapsulation.setField(queryCollection, "isInitialQuery", false);

        //act
        boolean hasNext = Deencapsulation.invoke(queryCollection, "hasNext");

        //assert
        assertTrue(hasNext);
    }

    //Tests_SRS_QUERYCOLLECTION_34_032: [If this object has a next set to return, this function shall return it.]
    @Test
    public void nextReturnsNextSetIfItHasOne() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},"anyString", expectedPageSize, QueryType.JOB_RESPONSE, mockConnectionString, new URL("http://www.microsoft.com"), mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);

        new MockUp<QueryCollection>()
        {
            @Mock boolean hasNext()
            {
                return true;
            }

            @Mock QueryCollectionResponse<String> sendQueryRequest(QueryOptions options) throws IOException, IotHubException
            {
                return mockQueryCollectionResponse;
            }
        };
        new NonStrictExpectations()
        {
            {
                new QueryOptions();
                result = mockQueryOptions;
            }
        };

        //act
        QueryCollectionResponse actualResponse = Deencapsulation.invoke(queryCollection, "next");

        //assert
        assertEquals(mockQueryCollectionResponse, actualResponse);
    }

    //Tests_SRS_QUERYCOLLECTION_34_033: [If this object does not have a next set to return, this function shall return null.]
    @Test
    public void nextReturnsNullIfItDoesNotHaveNext() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},"anyString", expectedPageSize, QueryType.JOB_RESPONSE, mockConnectionString, new URL("http://www.microsoft.com"), mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);

        new MockUp<QueryCollection>()
        {
            @Mock boolean hasNext()
            {
                return false;
            }
        };

        //act
        QueryCollectionResponse actualResponse = Deencapsulation.invoke(queryCollection, "next");

        //assert
        assertNull(actualResponse);
    }

    //Tests_SRS_QUERYCOLLECTION_34_034: [If this object has a next set to return using the provided query options, this function shall return it.]
    @Test
    public void nextWithOptionsReturnsNextSetIfItHasOne() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},"anyString", expectedPageSize, QueryType.JOB_RESPONSE, mockConnectionString, new URL("http://www.microsoft.com"), mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);

        new MockUp<QueryCollection>()
        {
            @Mock boolean hasNext()
            {
                return true;
            }

            @Mock QueryCollectionResponse<String> sendQueryRequest(QueryOptions options) throws IOException, IotHubException
            {
                return mockQueryCollectionResponse;
            }
        };

        //act
        QueryCollectionResponse actualResponse = Deencapsulation.invoke(queryCollection, "next", new Class[] {QueryOptions.class}, mockQueryOptions);

        //assert
        assertEquals(mockQueryCollectionResponse, actualResponse);
    }

    //Tests_SRS_QUERYCOLLECTION_34_035: [If this object does not have a next set to return, this function shall return null.]
    @Test
    public void nextWithOptionsReturnsNullIfItDoesNotHaveNext() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},"anyString", expectedPageSize, QueryType.JOB_RESPONSE, mockConnectionString, new URL("http://www.microsoft.com"), mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);

        //only mocking one method for QueryCollection, not the whole class
        new NonStrictExpectations(queryCollection)
        {
            {
                Deencapsulation.invoke(queryCollection, "hasNext");
                result = false;
            }
        };

        //act
        QueryCollectionResponse actualResponse = Deencapsulation.invoke(queryCollection, "next", new Class[] {QueryOptions.class}, mockQueryOptions);

        //assert
        assertNull(actualResponse);
    }

    //Tests_SRS_QUERYCOLLECTION_34_036: [This function shall return the saved page size.]
    @Test
    public void getPageSize(@Mocked final URL mockUrl)
    {
        //arrange
        Integer expectedPageSize = 290;
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, int.class, int.class, Proxy.class},"anyString", expectedPageSize, QueryType.JOB_RESPONSE, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, expectedTimeout, mockedProxy);
        Deencapsulation.setField(queryCollection, "pageSize", expectedPageSize);

        //act
        Integer actualPageSize = Deencapsulation.invoke(queryCollection, "getPageSize");

        //assert
        assertEquals(expectedPageSize, actualPageSize);
    }
}
