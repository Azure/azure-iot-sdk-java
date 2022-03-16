// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonElement;
import mockit.Deencapsulation;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service query Specification serializer
 * 100% methods, 100% lines covered
 */
public class QuerySpecificationTest
{
    private static final String VALID_QUERY = "SELECT * FROM enrollments";
    private static final String QUERY_JSON = "{\"query\":\"" + VALID_QUERY + "\"}";
    private static final int VALID_PAGE_SIZE = 20;
    private static final String VALID_CONTINUATION_TOKEN = "{\"token\":\"+RID:Defghij6KLMNOPQ==#RS:1#TRC:2#FPC:AUAAAAAAAAAJQABAAAAAAAk=\",\"range\":{\"min\":\"0123456789abcd\",\"max\":\"FF\"}}";

    /* SRS_QUERY_SPECIFICATION_21_001: [The constructor shall throw IllegalArgumentException if the provided query is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullQuery()
    {
        // arrange

        // act
        new QuerySpecification(null);

        // assert
    }

    /* SRS_QUERY_SPECIFICATION_21_001: [The constructor shall throw IllegalArgumentException if the provided query is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyQuery()
    {
        // arrange

        // act
        new QuerySpecification("");

        // assert
    }

    /* SRS_QUERY_SPECIFICATION_21_001: [The constructor shall throw IllegalArgumentException if the provided query is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidQuery()
    {
        // arrange

        // act
        new QuerySpecification("Invalid query");

        // assert
    }

    /* SRS_QUERY_SPECIFICATION_21_002: [The constructor shall store the provided `query`.] */
    @Test
    public void constructorStoreParameters()
    {
        // arrange

        // act
        QuerySpecification querySpecification = new QuerySpecification(VALID_QUERY);

        // assert
        assertEquals(VALID_QUERY, Deencapsulation.getField(querySpecification, "query"));
    }

    /* SRS_QUERY_SPECIFICATION_21_003: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementSucceed()
    {
        // arrange
        QuerySpecification querySpecification = new QuerySpecification(VALID_QUERY);

        // act
        JsonElement json = querySpecification.toJsonElement();

        // assert
        Helpers.assertJson(QUERY_JSON, json.toString());
    }

    /* SRS_QUERY_SPECIFICATION_21_004: [The getQuery shall return a String with the stored query.] */
    @Test
    public void gettersSucceed()
    {
        // arrange

        // act
        QuerySpecification querySpecification = new QuerySpecification(VALID_QUERY);

        // assert
        assertEquals(VALID_QUERY, querySpecification.getQuery());
    }

    /* SRS_QUERY_SPECIFICATION_21_005: [The QuerySpecification shall provide an empty constructor to make GSON happy.] */
    @Test
    public void emptyConstructorSucceed()
    {
        // arrange

        // act
        QuerySpecification querySpecification = Deencapsulation.newInstance(QuerySpecification.class);

        // assert
        assertNotNull(querySpecification);
    }
}
