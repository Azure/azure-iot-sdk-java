/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;
import com.microsoft.azure.sdk.iot.service.devicetwin.QueryResponse;
import com.microsoft.azure.sdk.iot.service.devicetwin.QueryType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.Assert.*;

/*
    Unit Tests for Query
    Coverage - method : 100%, line : 100%
 */
public class QueryTest
{
    private static final QueryType DEFAULT_QUERY_TYPE = QueryType.TWIN;
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final long DEFAULT_TIMEOUT = 100;
    private static final String DEFAULT_QUERY = "select * from devices";

    @Mocked
    IotHubConnectionString mockIotHubConnectionString;

    @Mocked
    URL mockUrl;

    @Mocked
    HttpMethod mockHttpMethod;

    @Mocked
    HttpResponse mockHttpResponse;

    @Mocked
    HttpRequest mockHttpRequest;

    @Mocked
    QueryResponse mockedQueryResponse;

    @Mocked
    IotHubServiceSasToken mockedSasToken;

    //Tests_SRS_QUERY_25_001: [The constructor shall validate query and save query, pagesize and request type]
    @Test
    public void constructorWithSQLQuerySucceeds() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = DEFAULT_QUERY;

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        //assert
        assertNotNull(Deencapsulation.getField(testQuery, "pageSize"));
        assertEquals(DEFAULT_PAGE_SIZE, (int) Deencapsulation.getField(testQuery, "pageSize"));

        assertEquals(sqlQuery, Deencapsulation.getField(testQuery, "query"));

        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "requestQueryType"));
        assertEquals(QueryType.UNKNOWN, Deencapsulation.getField(testQuery, "responseQueryType"));

        assertNull(Deencapsulation.getField(testQuery, "requestContinuationToken"));
        assertNull(Deencapsulation.getField(testQuery, "responseContinuationToken"));

        assertNull(Deencapsulation.getField(testQuery, "queryResponse"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithSQLQueryThrowsOnNullQuery() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = null;

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, String.class, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithSQLQueryThrowsOnEmptyQuery() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = "";

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);
    }

    //Tests_SRS_QUERY_25_002: [If the query is null or empty or is not a valid sql query (containing select and from), the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithSQLQueryThrowsOnInvalidQuery() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = "invalid query";

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);
    }

    //Tests_SRS_QUERY_25_003: [If the pagesize is zero or negative the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithSQLQueryThrowsNegativePageSize() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = DEFAULT_QUERY;

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, -1, DEFAULT_QUERY_TYPE);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithSQLQueryThrowsZeroPageSize() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = DEFAULT_QUERY;

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, 0, DEFAULT_QUERY_TYPE);

    }

    //Tests_SRS_QUERY_25_004: [If the QueryType is null or unknown then the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithSQLQueryThrowsOnUnknownQueryType() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = DEFAULT_QUERY;

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, DEFAULT_PAGE_SIZE, QueryType.UNKNOWN);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithSQLQueryThrowsOnNullQueryType() throws IllegalArgumentException
    {
        //arrange
        final String sqlQuery = DEFAULT_QUERY;

        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, sqlQuery, DEFAULT_PAGE_SIZE, null);
    }

    //Tests_SRS_QUERY_25_001: [The constructor shall validate query and save query, pagesize and request type]
    //Tests_SRS_QUERY_25_017: [If the query is avaliable then isSqlQuery shall be set to true, and false otherwise.]
    @Test
    public void constructorWithOutSQLQuerySucceeds() throws IllegalArgumentException
    {
        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        //assert
        assertNotNull(Deencapsulation.getField(testQuery, "pageSize"));
        assertEquals(DEFAULT_PAGE_SIZE, (int) Deencapsulation.getField(testQuery, "pageSize"));

        assertNull(Deencapsulation.getField(testQuery, "query"));
        assertFalse(Deencapsulation.getField(testQuery, "isSqlQuery"));

        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "requestQueryType"));
        assertEquals(QueryType.UNKNOWN, Deencapsulation.getField(testQuery, "responseQueryType"));

        assertNull(Deencapsulation.getField(testQuery, "requestContinuationToken"));
        assertNull(Deencapsulation.getField(testQuery, "responseContinuationToken"));

        assertNull(Deencapsulation.getField(testQuery, "queryResponse"));
    }

    //Tests_SRS_QUERY_25_003: [If the pagesize is zero or negative the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithOutSQLQueryThrowsNegativePageSize() throws IllegalArgumentException
    {
        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, -1, DEFAULT_QUERY_TYPE);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithOutSQLQueryThrowsZeroPageSize() throws IllegalArgumentException
    {
        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, 0, DEFAULT_QUERY_TYPE);

    }

    //Tests_SRS_QUERY_25_004: [If the QueryType is null or unknown then the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithOutSQLQueryThrowsOnUnknownQueryType() throws IllegalArgumentException
    {
        //act
        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_PAGE_SIZE, QueryType.UNKNOWN);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithOutSQLQueryThrowsOnNullQueryType() throws IllegalArgumentException
    {
           //act
        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_PAGE_SIZE, null);
    }

    private void setupSendQuery(Query testQuery, String testToken)
    {

        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };
    }

    //Tests_SRS_QUERY_25_005: [The method shall update the request continuation token and request pagesize which shall be used for processing subsequent query request.]
    //Tests_SRS_QUERY_25_018: [The method shall send the query request again.]
    @Test
    public void continueQuerySetsTokenAndSends() throws IOException, IotHubException
    {

        //arrange
        final String testToken = UUID.randomUUID().toString();
        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);
        setupSendQuery(testQuery, testToken);
        testQuery.sendQueryRequest(mockIotHubConnectionString, mockUrl, mockHttpMethod, DEFAULT_TIMEOUT);

        //act
        Deencapsulation.invoke(testQuery, "continueQuery", testToken);

        //assert
        assertEquals(testToken, Deencapsulation.getField(testQuery, "requestContinuationToken"));

        new Verifications()
        {
            {
                new HttpRequest(mockUrl, mockHttpMethod, (byte[]) any);
                times = 2;
                mockHttpRequest.setHeaderField("x-ms-max-item-count", String.valueOf(DEFAULT_PAGE_SIZE));
                times = 2;
                mockHttpRequest.setHeaderField("x-ms-continuation", testToken);
                times = 1;
                mockHttpRequest.setHeaderField(anyString, anyString);
                minTimes = 10;
            }
        };
    }

    @Test
    public void continueQuerySetsPageSize() throws IOException, IotHubException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final int testPageSize = 10;

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        setupSendQuery(testQuery, testToken);
        testQuery.sendQueryRequest(mockIotHubConnectionString, mockUrl, mockHttpMethod, DEFAULT_TIMEOUT);

        //act
        Deencapsulation.invoke(testQuery, "continueQuery", testToken, testPageSize);

        //assert
        assertEquals(testToken, Deencapsulation.getField(testQuery, "requestContinuationToken"));
        assertEquals(testPageSize, (int) Deencapsulation.getField(testQuery, "pageSize"));
    }

    //Tests_SRS_QUERY_25_006: [If the pagesize is zero or negative the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void continueQueryThrowsOnZeroPageSize() throws IOException, IotHubException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final int testPageSize = 0;

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        setupSendQuery(testQuery, testToken);
        testQuery.sendQueryRequest(mockIotHubConnectionString, mockUrl, mockHttpMethod, DEFAULT_TIMEOUT);

        //act
        Deencapsulation.invoke(testQuery, "continueQuery", testToken, testPageSize);
    }

    @Test (expected = IllegalArgumentException.class)
    public void continueQueryThrowsOnNegativePageSize() throws IOException, IotHubException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final int testPageSize = -10;

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        //act
        Deencapsulation.invoke(testQuery, "continueQuery", testToken, testPageSize);
    }

    //Tests_SRS_QUERY_25_008: [The method shall obtain the serilaized query by using QueryRequestParser.]
    //Tests_SRS_QUERY_25_009: [The method shall use the provided HTTP Method and send request to IotHub with the serialized body over the provided URL.]
    //Tests_SRS_QUERY_25_013: [The method shall create a QueryResponse object with the contents from the response body and save it.]
    //Tests_SRS_QUERY_25_020: [This method shall save all the parameters for future use.]
    @Test
    public void sendQueryRequestSucceeds() throws IotHubException, IOException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest",  mockIotHubConnectionString, mockUrl, mockHttpMethod, (long)0);

        //assert
        new Verifications()
        {
            {
                new HttpRequest(mockUrl, mockHttpMethod, (byte[]) any);
                times = 1;
                mockHttpRequest.setHeaderField("x-ms-max-item-count", String.valueOf(DEFAULT_PAGE_SIZE));
                times = 1;
                mockHttpRequest.setHeaderField("x-ms-continuation", anyString);
                times = 0;
                mockHttpRequest.setHeaderField(anyString, anyString);
                minTimes = 5;
            }
        };

        assertEquals(testToken, Deencapsulation.getField(testQuery, "responseContinuationToken"));
        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "responseQueryType"));
    }

    @Test
    public void sendQueryRequestForNoSqlSucceeds() throws IotHubException, IOException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest",  mockIotHubConnectionString, mockUrl, mockHttpMethod, (long)0);

        //assert
        new Verifications()
        {
            {
                new HttpRequest(mockUrl, mockHttpMethod, (byte[]) any);
                times = 1;
                mockHttpRequest.setHeaderField("x-ms-max-item-count", String.valueOf(DEFAULT_PAGE_SIZE));
                times = 1;
                mockHttpRequest.setHeaderField("x-ms-continuation", anyString);
                times = 0;
                mockHttpRequest.setHeaderField(anyString, anyString);
                minTimes = 5;
            }
        };

        assertEquals(testToken, Deencapsulation.getField(testQuery, "responseContinuationToken"));
        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "responseQueryType"));
    }

    //Tests_SRS_QUERY_25_007: [The method shall set the http headers x-ms-continuation and x-ms-max-item-count with request continuation token and page size if they were not null.]
    @Test
    public void sendQueryRequestSetsHeadersIfFound() throws IotHubException, IOException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final int newPageSize = 5;
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest",  mockIotHubConnectionString, mockUrl, mockHttpMethod, (long)0);
        Deencapsulation.invoke(testQuery, "continueQuery", testToken, newPageSize);

        //assert
        new Verifications()
        {
            {
                new HttpRequest(mockUrl, mockHttpMethod, (byte[]) any);
                times = 2;
                mockHttpRequest.setHeaderField("x-ms-max-item-count", String.valueOf(newPageSize));
                times = 1;
                mockHttpRequest.setHeaderField("x-ms-continuation", testToken);
                times = 1;
                mockHttpRequest.setHeaderField(anyString, anyString);
                minTimes = 5;
            }
        };

        assertEquals(testToken, Deencapsulation.getField(testQuery, "responseContinuationToken"));
        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "responseQueryType"));
    }

    @Test
    public void sendQueryRequestDoesNotSetNullHeaders() throws IotHubException, IOException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest",  mockIotHubConnectionString, mockUrl, mockHttpMethod, (long)0);
        Deencapsulation.invoke(testQuery, "continueQuery", String.class);

        //assert
        new Verifications()
        {
            {
                new HttpRequest(mockUrl, mockHttpMethod, (byte[]) any);
                times = 2;
                mockHttpRequest.setHeaderField("x-ms-max-item-count", String.valueOf(DEFAULT_PAGE_SIZE));
                times = 2;
                mockHttpRequest.setHeaderField("x-ms-continuation", anyString);
                times = 0;
                mockHttpRequest.setHeaderField(anyString, anyString);
                minTimes = 10;
            }
        };

        assertEquals(testToken, Deencapsulation.getField(testQuery, "responseContinuationToken"));
        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "responseQueryType"));
    }

    @Test (expected = IOException.class)
    public void sendQueryRequestThrowsWhenResponseThrows() throws IotHubException, IOException
    {

        //arrange
        final String testResponseToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testResponseToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
                Deencapsulation.newInstance(QueryResponse.class, anyString);
                result = new IOException("test");
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //assert
        assertEquals(testResponseToken, Deencapsulation.getField(testQuery, "responseContinuationToken"));
    }

    //Tests_SRS_QUERY_25_010: [The method shall read the continuation token (x-ms-continuation) and reponse type (x-ms-item-type) from the HTTP Headers and save it.]
    @Test
    public void sendQueryRequestSetsResContinuationTokenOnlyIfFound() throws IotHubException, IOException
    {

        //arrange
        final String testResponseToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testResponseToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //assert
        assertEquals(testResponseToken, Deencapsulation.getField(testQuery, "responseContinuationToken"));
    }

    @Test
    public void sendQueryRequestDoesNotSetResContinuationTokenIfNotFound() throws IotHubException, IOException
    {
        //arrange
        final String testResponseToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //assert
        assertNull(Deencapsulation.getField(testQuery, "responseContinuationToken"));
    }

    @Test
    public void sendQueryRequestSetsItemOnlyIfFound() throws IotHubException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //assert
        assertNull(Deencapsulation.getField(testQuery, "responseContinuationToken"));
        assertEquals(DEFAULT_QUERY_TYPE, Deencapsulation.getField(testQuery, "responseQueryType"));
    }

    //Tests_SRS_QUERY_25_012: [If the response type is Unknown or not found then this method shall throw IOException.]
    @Test (expected = IOException.class)
    public void sendQueryRequestThrowsIfItemNotFound() throws IotHubException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);
    }

    //Tests_SRS_QUERY_25_012: [If the reponse type is Unknown or not found then this method shall throw IOException.]
    @Test (expected = IOException.class)
    public void sendQueryRequestThrowsIfTypeUnknown() throws IotHubException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-item-type", QueryType.UNKNOWN.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);
    }

    //Tests_SRS_QUERY_25_011: [If the request type and response does not match then the method shall throw IOException.]
    @Test (expected = IOException.class)
    public void sendQueryRequestThrowsIfRequestAndResponseTypeDoesNotMatch() throws IotHubException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-item-type", QueryType.JOB_RESPONSE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);
    }

    //Tests_SRS_QUERY_25_019: [This method shall throw IllegalArgumentException if any of the parameters are null or empty.]
    @Test (expected = IllegalArgumentException.class)
    public void sendQueryRequestThrowsOnNullConnectionString() throws IotHubException, IOException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        testQuery.sendQueryRequest(null, mockUrl, mockHttpMethod, (long) 0);
    }

    @Test (expected = IllegalArgumentException.class)
    public void sendQueryRequestThrowsOnNullURL() throws IotHubException, IOException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        testQuery.sendQueryRequest(mockIotHubConnectionString, null, mockHttpMethod, (long) 0);
    }

    @Test (expected = IllegalArgumentException.class)
    public void sendQueryRequestThrowsOnNullHttpMethod() throws IotHubException, IOException
    {
        //arrange
        final String testToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        //act
        testQuery.sendQueryRequest(mockIotHubConnectionString, mockUrl, null, (long) 0);
    }

    //Tests_SRS_QUERY_25_014: [The method shall return the continuation token found in response to a query (which can be null).]
    @Test
    public void getTokenGets() throws IotHubException, IOException
    {
        //arrange
        final String testResponseToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();
        testHeaderResponseMap.put("x-ms-continuation", testResponseToken);
        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
            }
        };

        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //act
        String actualContinuationToken = Deencapsulation.invoke(testQuery, "getContinuationToken");

        //assert
        assertEquals(actualContinuationToken, testResponseToken);
    }

    //Tests_SRS_QUERY_25_015: [The method shall return true if next element from QueryResponse is available and false otherwise.]
    @Test
    public void hasNextReturnsTrueIfNextExists() throws IotHubException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();

        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
                mockedQueryResponse.hasNext();
                result = true;
            }
        };

        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //act
        boolean hasNext = Deencapsulation.invoke(testQuery, "hasNext");

        //assert
        assertEquals(true, hasNext);
    }

    //Tests_SRS_QUERY_25_021: [If no further query response is available, then this method shall continue to request query to IotHub if continuation token is available.]
    @Test
    public void hasNextReturnsFalseIfNextDoesNotExistsAndTokenIsNull() throws IotHubException, IOException
    {
        //arrange
        final Map<String, String> testHeaderResponseMap = new HashMap<>();

        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
                mockedQueryResponse.hasNext();
                result = false;
            }
        };

        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //act
        boolean hasNext = Deencapsulation.invoke(testQuery, "hasNext");

        //assert
        assertEquals(false, hasNext);
    }

    @Test
    public void hasNextReturnsFalseIfContinueQueryHasNothing() throws IotHubException, IOException
    {
        //arrange
        final String testResponseToken = UUID.randomUUID().toString();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();

        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());
        testHeaderResponseMap.put("x-ms-continuation", testResponseToken);

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
                mockedQueryResponse.hasNext();
                result = false;
            }
        };

        // continue query setup
        setupSendQuery(testQuery, testResponseToken);

        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //act
        boolean hasNext = Deencapsulation.invoke(testQuery, "hasNext");

        //assert
        assertEquals(false, hasNext);

        new Verifications()
        {
            {
                new HttpRequest(mockUrl, mockHttpMethod, (byte[]) any);
                times = 2;
                mockHttpRequest.setHeaderField("x-ms-max-item-count", String.valueOf(DEFAULT_PAGE_SIZE));
                times = 2;
                mockHttpRequest.setHeaderField("x-ms-continuation", anyString);
                times = 1;
                mockHttpRequest.setHeaderField(anyString, anyString);
                minTimes = 10;
            }
        };
    }

    //Tests_SRS_QUERY_25_016: [The method shall return the next element for this QueryResponse.]
    @Test
    public void nextReturnsIfNextExists() throws IotHubException, IOException
    {
        //arrange
        final Object mockObject = new Object();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();

        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
                mockedQueryResponse.hasNext();
                result = true;
                mockedQueryResponse.next();
                result = mockObject;
            }
        };

        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //act
        Object next = Deencapsulation.invoke(testQuery, "next");

        //assert
        assertEquals(mockObject, next);
    }

    //Tests_SRS_QUERY_25_022: [The method shall check if any further elements are available by calling hasNext and if none is available then it shall throw NoSuchElementException.]
    @Test (expected = NoSuchElementException.class)
    public void nextThrowsIfNextDoesNotExists() throws IotHubException, IOException
    {
        //arrange
        final Object mockObject = new Object();
        final Map<String, String> testHeaderResponseMap = new HashMap<>();

        testHeaderResponseMap.put("x-ms-item-type", DEFAULT_QUERY_TYPE.getValue());

        Query testQuery = Deencapsulation.newInstance(Query.class, DEFAULT_QUERY, DEFAULT_PAGE_SIZE, DEFAULT_QUERY_TYPE);

        new NonStrictExpectations()
        {
            {
                mockHttpResponse.getHeaderFields();
                result = testHeaderResponseMap;
                mockedQueryResponse.hasNext();
                result = false;
            }
        };

        Deencapsulation.invoke(testQuery, "sendQueryRequest", mockIotHubConnectionString, mockUrl, mockHttpMethod, (long) 0);

        //act
        Object next = Deencapsulation.invoke(testQuery, "next");
    }
}
