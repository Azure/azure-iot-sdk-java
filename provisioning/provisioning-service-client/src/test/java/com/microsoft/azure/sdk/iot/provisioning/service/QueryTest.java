// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.service.*;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.*;
import mockit.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * Unit tests for Query iterator.
 * 100% methods, 100% lines covered
 */
public class QueryTest
{
    @Mocked
    private ContractApiHttp mockedContractApiHttp;

    @Mocked
    private QuerySpecification mockedQuerySpecification;

    @Mocked
    private QueryResult mockedQueryResult;

    @Mocked
    private HttpResponse mockedHttpResponse;

    /* SRS_QUERY_21_001: [The constructor shall throw IllegalArgumentException if the provided contractApiHttp is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnContractApiHttpNull()
    {
        // arrange
        final ContractApiHttp contractApiHttp = null;
        final String targetPath = "enrollments";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final int pageSize = 10;

        // act
        Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        // assert
    }

    /* SRS_QUERY_21_002: [The constructor shall throw IllegalArgumentException if the provided targetPath is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnTargetPathNull()
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = null;
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final int pageSize = 10;

        // act
        Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        // assert
    }

    /* SRS_QUERY_21_002: [The constructor shall throw IllegalArgumentException if the provided targetPath is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnTargetPathEmpty()
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final int pageSize = 10;

        // act
        Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        // assert
    }

    /* SRS_QUERY_21_003: [The constructor shall throw IllegalArgumentException if the provided querySpecification is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnQuerySpecificationNull()
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final QuerySpecification querySpecification = null;
        final int pageSize = 10;

        // act
        Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        // assert
    }

    /* SRS_QUERY_21_004: [The constructor shall throw IllegalArgumentException if the provided pageSize is negative.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnPageSizeNegative()
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final int pageSize = -10;

        // act
        Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        // assert
    }

    /* SRS_QUERY_21_005: [The constructor shall store the provided `contractApiHttp` and `pageSize`.] */
    /* SRS_QUERY_21_006: [The constructor shall create and store a JSON from the provided querySpecification.] */
    /* SRS_QUERY_21_007: [The constructor shall create and store a queryPath adding `/query` to the provided `targetPath`.] */
    /* SRS_QUERY_21_008: [The constructor shall set continuationToken as null.] */
    /* SRS_QUERY_21_009: [The constructor shall set hasNext as true.] */
    @Test
    public void constructorStoresContractApiHttpAndPageSize()
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final String queryPath = targetPath + "/query";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final int pageSize = 10;

        new NonStrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
                times = 1;
            }
        };

        // act
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        // assert
        assertEquals(contractApiHttp, Deencapsulation.getField(query, "contractApiHttp"));
        assertEquals(pageSize, (int)Deencapsulation.getField(query, "pageSize"));
        assertEquals(querySpecificationJson, Deencapsulation.getField(query, "querySpecificationJson"));
        assertEquals(queryPath, Deencapsulation.getField(query, "queryPath"));
        assertNull(Deencapsulation.getField(query, "continuationToken"));
        assertTrue(Deencapsulation.getField(query, "hasNext"));
    }

    /* SRS_QUERY_21_010: [The hasNext shall return the store hasNext.] */
    @Test
    public void hasNextReturnsTrue()
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final int pageSize = 10;

        new NonStrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        // act - assert
        assertTrue(query.hasNext());
    }

    /* SRS_QUERY_21_010: [The hasNext shall return the store hasNext.] */
    @Test
    public void hasNextReturnsFalse() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final String queryPath = targetPath + "/query";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final int pageSize = 10;
        final Map<String, String> headersResult = new HashMap<String, String>()
        {
            {
                put("x-ms-item-type", "enrollment");
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
                mockedContractApiHttp.request(HttpMethod.POST, queryPath, (Map)any, querySpecificationJson);
                result = mockedHttpResponse;
                mockedHttpResponse.getBody();
                result = "result".getBytes();
                mockedHttpResponse.getHeaderFields();
                result = headersResult;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        query.next();

        // act - assert
        assertFalse(query.hasNext());
    }

    /* SRS_QUERY_21_011: [The next shall throw NoSuchElementException if the hasNext is false.] */
    @Test (expected = NoSuchElementException.class)
    public void nextThrowsOnFalseHasNext() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final String queryPath = targetPath + "/query";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final int pageSize = 10;
        final Map<String, String> headersResult = new HashMap<String, String>()
        {
            {
                put("x-ms-item-type", "enrollment");
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
                mockedContractApiHttp.request(HttpMethod.POST, queryPath, (Map)any, querySpecificationJson);
                result = mockedHttpResponse;
                mockedHttpResponse.getBody();
                result = "result".getBytes();
                mockedHttpResponse.getHeaderFields();
                result = headersResult;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        query.next();
        assertFalse(query.hasNext());

        // act
        query.next();

        // assert
    }

    /* SRS_QUERY_21_012: [If the pageSize is not 0, the next shall send the Http request with `x-ms-max-item-count=[pageSize]` in the header.] */
    /* SRS_QUERY_21_013: [If the continuationToken is not null or empty, the next shall send the Http request with `x-ms-continuation=[continuationToken]` in the header.] */
    /* SRS_QUERY_21_014: [The next shall send a Http request with a Http verb `POST`.] */
    @Test
    public void nextSendPageSizeAndContinuationTokeanIntoHttpHeader() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final String queryPath = targetPath + "/query";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final String continuationToken = "validToken";
        final int pageSize = 10;
        final Map<String, String> headersSend = new HashMap<String, String>()
        {
            {
                put("x-ms-max-item-count", "10");
                put("x-ms-continuation", continuationToken);
            }
        };
        final Map<String, String> headersResult = new HashMap<String, String>()
        {
            {
                put("x-ms-item-type", "enrollment");
            }
        };

        new StrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
                mockedContractApiHttp.request(HttpMethod.POST, queryPath, headersSend, querySpecificationJson);
                times = 1;
                result = mockedHttpResponse;
                mockedHttpResponse.getBody();
                result = "result".getBytes();
                mockedHttpResponse.getHeaderFields();
                result = headersResult;
                times = 1;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        // act
        query.next(continuationToken);

        // assert
    }

    /* SRS_QUERY_21_012: [If the pageSize is not 0, the next shall send the Http request with `x-ms-max-item-count=[pageSize]` in the header.] */
    /* SRS_QUERY_21_013: [If the continuationToken is not null or empty, the next shall send the Http request with `x-ms-continuation=[continuationToken]` in the header.] */
    @Test
    public void nextSendWithoutPageSizeOrContinuationTokenIntoHttpHeader() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final String queryPath = targetPath + "/query";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final Map<String, String> headersSend = new HashMap<>();
        final Map<String, String> headersResult = new HashMap<String, String>()
        {
            {
                put("x-ms-item-type", "enrollment");
            }
        };

        new StrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
                mockedContractApiHttp.request(HttpMethod.POST, queryPath, headersSend, querySpecificationJson);
                times = 1;
                result = mockedHttpResponse;
                mockedHttpResponse.getBody();
                result = "result".getBytes();
                mockedHttpResponse.getHeaderFields();
                result = headersResult;
                times = 1;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, 0);

        // act
        query.next();

        // assert
    }

    /* SRS_QUERY_21_015: [The next shall throw IllegalArgumentException if the Http request throws any ProvisioningServiceClientException.] */
    @Test (expected = IllegalArgumentException.class)
    public void nextThrowsOnRequestFailed() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final String queryPath = targetPath + "/query";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";

        new StrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
                mockedContractApiHttp.request(HttpMethod.POST, queryPath, (Map)any, querySpecificationJson);
                result = new ProvisioningServiceClientBadFormatException();
                times = 1;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, 0);

        // act
        query.next();

        // assert
    }

    /* SRS_QUERY_21_016: [The next shall create and return a new instance of the QueryResult using the `x-ms-item-type` as type, `x-ms-continuation` as the next continuationToken, and the message body.] */
    /* SRS_QUERY_21_017: [The next shall set hasNext as true if the continuationToken is not null, or false if it is null.] */
    @Test
    public void nextReturnsQueryResultWithContinuationToken() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final String queryPath = targetPath + "/query";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final String continuationToken = "validToken";
        final String type = "validToken";
        final String bodyResult = "result";
        final Map<String, String> headersSend = new HashMap<>();
        final Map<String, String> headersResult = new HashMap<String, String>()
        {
            {
                put("x-ms-item-type", type);
                put("x-ms-continuation", continuationToken);
            }
        };

        new StrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
                mockedContractApiHttp.request(HttpMethod.POST, queryPath, headersSend, querySpecificationJson);
                result = mockedHttpResponse;
                mockedHttpResponse.getBody();
                result = bodyResult.getBytes();
                mockedHttpResponse.getHeaderFields();
                result = headersResult;
                Deencapsulation.newInstance(QueryResult.class, type, bodyResult, continuationToken);
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, 0);

        // act
        QueryResult queryResult = query.next();

        // assert
        assertNotNull(queryResult);
        assertTrue(query.hasNext());
    }

    /* SRS_QUERY_21_016: [The next shall create and return a new instance of the QueryResult using the `x-ms-item-type` as type, `x-ms-continuation` as the next continuationToken, and the message body.] */
    @Test
    public void nextSucceedOnNullType() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final String queryPath = targetPath + "/query";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final String continuationToken = "validToken";
        final String type = "validToken";
        final String bodyResult = "result";
        final Map<String, String> headersSend = new HashMap<>();
        final Map<String, String> headersResult = new HashMap<String, String>()
        {
            {
                put("x-ms-item-type", null);
                put("x-ms-continuation", continuationToken);
            }
        };

        new StrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
                mockedContractApiHttp.request(HttpMethod.POST, queryPath, headersSend, querySpecificationJson);
                result = mockedHttpResponse;
                mockedHttpResponse.getBody();
                result = bodyResult.getBytes();
                mockedHttpResponse.getHeaderFields();
                result = headersResult;
                Deencapsulation.newInstance(QueryResult.class, new Class[]{String.class, String.class, String.class}, null, bodyResult, continuationToken);
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, 0);

        // act
        QueryResult queryResult = query.next();

        // assert
        assertNotNull(queryResult);
        assertTrue(query.hasNext());
    }

    /* SRS_QUERY_21_016: [The next shall create and return a new instance of the QueryResult using the `x-ms-item-type` as type, `x-ms-continuation` as the next continuationToken, and the message body.] */
    /* SRS_QUERY_21_017: [The next shall set hasNext as true if the continuationToken is not null, or false if it is null.] */
    @Test
    public void nextSucceedOnNullContinuationToken() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final String queryPath = targetPath + "/query";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final String continuationToken = "validToken";
        final String type = "validToken";
        final String bodyResult = "result";
        final Map<String, String> headersSend = new HashMap<>();
        final Map<String, String> headersResult = new HashMap<String, String>()
        {
            {
                put("x-ms-item-type", type);
                put("x-ms-continuation", null);
            }
        };

        new StrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
                mockedContractApiHttp.request(HttpMethod.POST, queryPath, headersSend, querySpecificationJson);
                result = mockedHttpResponse;
                mockedHttpResponse.getBody();
                result = bodyResult.getBytes();
                mockedHttpResponse.getHeaderFields();
                result = headersResult;
                Deencapsulation.newInstance(QueryResult.class, new Class[]{String.class, String.class, String.class}, type, bodyResult, null);
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, 0);

        // act
        QueryResult queryResult = query.next();

        // assert
        assertNotNull(queryResult);
        assertFalse(query.hasNext());
    }

    /* SRS_QUERY_21_024: [The next shall throw IllegalArgumentException if the heepResponse contains a null body.] */
    @Test (expected = IllegalArgumentException.class)
    public void nextThrowsOnNullBody() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final String queryPath = targetPath + "/query";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final Map<String, String> headersSend = new HashMap<>();

        new StrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
                mockedContractApiHttp.request(HttpMethod.POST, queryPath, headersSend, querySpecificationJson);
                result = mockedHttpResponse;
                mockedHttpResponse.getBody();
                result = null;
                times = 1;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, 0);

        // act
        query.next();

        // assert
    }

    /* SRS_QUERY_21_017: [The next shall set hasNext as true if the continuationToken is not null, or false if it is null.] */
    /* SRS_QUERY_21_017: [The next shall set hasNext as true if the continuationToken is not null, or false if it is null.] */
    @Test
    public void nextReturnsQueryResultWithoutContinuationToken() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final String queryPath = targetPath + "/query";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final String type = "validToken";
        final String bodyResult = "result";
        final Map<String, String> headersSend = new HashMap<>();
        final Map<String, String> headersResult = new HashMap<String, String>()
        {
            {
                put("x-ms-item-type", type);
            }
        };

        new StrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
                mockedContractApiHttp.request(HttpMethod.POST, queryPath, headersSend, querySpecificationJson);
                result = mockedHttpResponse;
                mockedHttpResponse.getBody();
                result = bodyResult.getBytes();
                mockedHttpResponse.getHeaderFields();
                result = headersResult;
                Deencapsulation.newInstance(QueryResult.class, new Class[]{String.class, String.class, String.class}, type, bodyResult, null);
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, 0);

        // act
        QueryResult queryResult = query.next();

        // assert
        assertNotNull(queryResult);
        assertFalse(query.hasNext());
    }

    /* SRS_QUERY_21_018: [The next shall throw NoSuchElementException if the provided continuationToken is null or empty.] */
    @Test (expected = NoSuchElementException.class)
    public void nextThrowsOnNullContinuationToken() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";

        new StrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, 0);

        // act
        query.next(null);

        // assert
    }

    /* SRS_QUERY_21_018: [The next shall throw NoSuchElementException if the provided continuationToken is null or empty.] */
    @Test (expected = NoSuchElementException.class)
    public void nextThrowsOnEmptyContinuationToken() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";

        new StrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, 0);

        // act
        query.next("");

        // assert
    }

    /* SRS_QUERY_21_019: [The next shall store the provided continuationToken.] */
    /* SRS_QUERY_21_020: [The next shall return the next page of results by calling the next().] */
    @Test
    public void nextStoreContinuationToken() throws ProvisioningServiceClientException
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String continuationToken = "validToken";
        final String querySpecificationJson = "validJson";

        class MockedQuery extends Query
        {
            MockedQuery()
            {
                super(contractApiHttp, targetPath, querySpecification, 0);
            }

            @Override
            public QueryResult next()
            {
                return mockedQueryResult;
            }
        }

        new StrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
            }
        };
        MockedQuery mockedQuery = new MockedQuery();

        // act
        QueryResult queryResult = mockedQuery.next(continuationToken);

        // assert
        assertEquals(continuationToken, Deencapsulation.getField(mockedQuery, "continuationToken"));
        assertEquals(mockedQueryResult, queryResult);
    }

    /* SRS_QUERY_21_021: [The getPageSize shall return the stored pageSize.] */
    @Test
    public void getPageSizeReturnsPageSize()
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final int pageSize = 10;

        new NonStrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        // act - assert
        assertEquals(pageSize, query.getPageSize());
    }

    /* SRS_QUERY_21_022: [The setPageSize shall throw IllegalArgumentException if the provided pageSize is negative.] */
    @Test (expected = IllegalArgumentException.class)
    public void setPageSizeThrowsOnNegativePageSize()
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final int pageSize = 10;

        new NonStrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        // act
        query.setPageSize(-10);

        // assert
    }

    /* SRS_QUERY_21_023: [The setPageSize shall store the new pageSize value.] */
    @Test
    public void setPageSizeChangePageSize()
    {
        // arrange
        final ContractApiHttp contractApiHttp = mockedContractApiHttp;
        final String targetPath = "enrollments";
        final QuerySpecification querySpecification = mockedQuerySpecification;
        final String querySpecificationJson = "validJson";
        final int pageSize = 10;

        new NonStrictExpectations()
        {
            {
                mockedQuerySpecification.toJson();
                result = querySpecificationJson;
            }
        };
        Query query = Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                contractApiHttp, targetPath, querySpecification, pageSize);

        // act
        query.setPageSize(20);

        // assert
        assertEquals(20, (int)Deencapsulation.getField(query, "pageSize"));
    }
}
