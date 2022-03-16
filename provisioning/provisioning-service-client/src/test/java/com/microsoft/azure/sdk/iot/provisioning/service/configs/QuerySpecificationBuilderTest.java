// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import mockit.Deencapsulation;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service query Specification builder
 * 100% methods, 100% lines covered
 */
public class QuerySpecificationBuilderTest
{
    /* SRS_QUERY_SPECIFICATION_BUILDER_21_001: [The constructor shall throw IllegalArgumentException if the provided `selection` is null, empty, or `fromType` is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullSelection()
    {
        // arrange
        // act
        new QuerySpecificationBuilder(null, QuerySpecificationBuilder.FromType.ENROLLMENTS);

        // assert
    }

    /* SRS_QUERY_SPECIFICATION_BUILDER_21_001: [The constructor shall throw IllegalArgumentException if the provided `selection` is null, empty, or `fromType` is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptySelection()
    {
        // arrange
        // act
        new QuerySpecificationBuilder("", QuerySpecificationBuilder.FromType.ENROLLMENTS);

        // assert
    }

    /* SRS_QUERY_SPECIFICATION_BUILDER_21_001: [The constructor shall throw IllegalArgumentException if the provided `selection` is null, empty, or `fromType` is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullFromType()
    {
        // arrange
        // act
        new QuerySpecificationBuilder("*", null);

        // assert
    }

    /* SRS_QUERY_SPECIFICATION_BUILDER_21_002: [The constructor shall store the provided `selection` and `fromType` clauses.] */
    @Test
    public void constructorStoreSucceed()
    {
        // arrange
        // act
        QuerySpecificationBuilder querySpecificationBuilder = new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS);

        // assert
        assertEquals("*", Deencapsulation.getField(querySpecificationBuilder, "selection"));
        assertEquals(QuerySpecificationBuilder.FromType.ENROLLMENTS, Deencapsulation.getField(querySpecificationBuilder, "fromType"));
    }

    /* SRS_QUERY_SPECIFICATION_BUILDER_21_003: [The where shall store the provided `where` clause.] */
    @Test
    public void whereStoreSucceed()
    {
        // arrange
        final String whereClause = "validWhere";
        // act
        QuerySpecificationBuilder querySpecificationBuilder = new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS).where(whereClause);

        // assert
        assertEquals(whereClause, Deencapsulation.getField(querySpecificationBuilder, "where"));
    }

    /* SRS_QUERY_SPECIFICATION_BUILDER_21_004: [The groupBy shall store the provided `groupBy` clause.] */
    @Test
    public void groupByStoreSucceed()
    {
        // arrange
        final String groupByClause = "validGroupBy";
        // act
        QuerySpecificationBuilder querySpecificationBuilder = new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS).groupBy(groupByClause);

        // assert
        assertEquals(groupByClause, Deencapsulation.getField(querySpecificationBuilder, "groupBy"));
    }

    /* SRS_QUERY_SPECIFICATION_BUILDER_21_005: [The createSqlQuery shall create a String with the `selection` and `fromType` clause using the sql format.] */
    @Test
    public void createSqlQuerySucceed()
    {
        // arrange
        // act
        QuerySpecification querySpecification = new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS).createSqlQuery();

        // assert
        Helpers.assertJson(querySpecification.toJson(), "{\"query\":\"select * from enrollments\"}");
    }

    /* SRS_QUERY_SPECIFICATION_BUILDER_21_006: [If the `where` is not null or empty, the createSqlQuery shall add the clause `where` with its value in the sql query.] */
    @Test
    public void createSqlQueryWithWhereSucceed()
    {
        // arrange
        // act
        QuerySpecification querySpecification = new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS).where("validWhere").createSqlQuery();

        // assert
        Helpers.assertJson(querySpecification.toJson(), "{\"query\":\"select * from enrollments where validWhere\"}");
    }

    /* SRS_QUERY_SPECIFICATION_BUILDER_21_007: [If the `groupBy` is not null or empty, the createSqlQuery shall add the clause `group by` with its value in the sql query.] */
    @Test
    public void createSqlQueryWithGroupBySucceed()
    {
        // arrange
        // act
        QuerySpecification querySpecification = new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS).groupBy("validGroupBy").createSqlQuery();

        // assert
        Helpers.assertJson(querySpecification.toJson(), "{\"query\":\"select * from enrollments group by validGroupBy\"}");
    }

    /* SRS_QUERY_SPECIFICATION_BUILDER_21_008: [The createSqlQuery shall create a new instance of the QuerySpecification with the String built with the provided clauses.] */
    @Test
    public void createSqlQueryWithWhereAndGroupBySucceed()
    {
        // arrange
        // act
        QuerySpecification querySpecification = new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS).where("validWhere").groupBy("validGroupBy").createSqlQuery();

        // assert
        Helpers.assertJson(querySpecification.toJson(), "{\"query\":\"select * from enrollments where validWhere group by validGroupBy\"}");
    }

}
