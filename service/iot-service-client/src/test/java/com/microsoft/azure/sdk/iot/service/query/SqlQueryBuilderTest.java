/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.query;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
    SqlQueryBuilder Unit Tests
    Coverage : 100% method, 100% line

 */
public class SqlQueryBuilderTest
{
    private static final String VALID_SELECTION = "validSelection";
    private static final SqlQueryBuilder.FromType VALID_FROM = SqlQueryBuilder.FromType.DEVICES;
    private static final String VALID_WHERE = "validWhere";
    private static final String VALID_GROUPBY = "validGroupBy";

    //Tests_SRS_SQL_QUERY_25_002: [ The constructor shall build the sql query string from the given Input ]
    //Tests_SRS_SQL_QUERY_25_005: [ The constructor shall create a new SqlQueryBuilder instance and return it ]
    //Tests_SRS_SQL_QUERY_25_006: [ The method shall return the sql query string built ]
    @Test
    public void createQuerySelectFromSucceeds() throws IOException
    {
        //act
        String sqlQueryTest = SqlQueryBuilder.createSqlQuery(VALID_SELECTION, SqlQueryBuilder.FromType.DEVICES, null, null);

        //assert
        assertTrue(sqlQueryTest.toLowerCase().contains("select"));
        assertTrue(sqlQueryTest.contains(VALID_SELECTION));
        assertTrue(sqlQueryTest.toLowerCase().contains("from"));
        assertTrue(sqlQueryTest.contains(VALID_FROM.getValue()));
    }

    //Tests_SRS_SQL_QUERY_25_001: [ The constructor shall throw IllegalArgumentException if either input string selection or fromType is null or empty ]
    @Test (expected = IllegalArgumentException.class)
    public void createQueryNoSelectThrows() throws IOException
    {
        //act
        SqlQueryBuilder.createSqlQuery(null, SqlQueryBuilder.FromType.DEVICES, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createQueryNoFROMThrows() throws IOException
    {
        //act
        SqlQueryBuilder.createSqlQuery(VALID_SELECTION, null, null, null);
    }

    //Tests_SRS_SQL_QUERY_25_003: [ The constructor shall append where to the sql query string only when provided ]
    @Test
    public void createQuerySelectFromWhereSucceeds() throws IOException
    {
        //act
        String sqlQueryTest = SqlQueryBuilder.createSqlQuery(VALID_SELECTION, SqlQueryBuilder.FromType.DEVICES, VALID_WHERE, null);

        //assert
        assertTrue(sqlQueryTest.toLowerCase().contains("select"));
        assertTrue(sqlQueryTest.contains(VALID_SELECTION));
        assertTrue(sqlQueryTest.toLowerCase().contains("from"));
        assertTrue(sqlQueryTest.contains(VALID_FROM.getValue()));
        assertTrue(sqlQueryTest.toLowerCase().contains("where"));
        assertTrue(sqlQueryTest.contains(VALID_WHERE));
        assertFalse(sqlQueryTest.toLowerCase().contains("group by"));
        assertFalse(sqlQueryTest.contains(VALID_GROUPBY));
    }

    //Tests_SRS_SQL_QUERY_25_004: [ The constructor shall append groupby to the sql query string only when provided ]
    @Test
    public void createQuerySelectFromGroupBySucceeds() throws IOException
    {
        //act
        String sqlQueryTest = SqlQueryBuilder.createSqlQuery(VALID_SELECTION, SqlQueryBuilder.FromType.DEVICES, null, VALID_GROUPBY);

        //assert
        assertTrue(sqlQueryTest.toLowerCase().contains("select"));
        assertTrue(sqlQueryTest.contains(VALID_SELECTION));
        assertTrue(sqlQueryTest.toLowerCase().contains("from"));
        assertTrue(sqlQueryTest.contains(VALID_FROM.getValue()));
        assertFalse(sqlQueryTest.toLowerCase().contains("where"));
        assertFalse(sqlQueryTest.contains(VALID_WHERE));
        assertTrue(sqlQueryTest.toLowerCase().contains("group by"));
        assertTrue(sqlQueryTest.contains(VALID_GROUPBY));
    }

    @Test
    public void createQuerySelectFromWhereGroupBySucceeds() throws IOException
    {
        //act
        String sqlQueryTest = SqlQueryBuilder.createSqlQuery(VALID_SELECTION, SqlQueryBuilder.FromType.DEVICES, VALID_WHERE, VALID_GROUPBY);

        //assert
        assertTrue(sqlQueryTest.toLowerCase().contains("select"));
        assertTrue(sqlQueryTest.contains(VALID_SELECTION));
        assertTrue(sqlQueryTest.toLowerCase().contains("from"));
        assertTrue(sqlQueryTest.contains(VALID_FROM.getValue()));
        assertTrue(sqlQueryTest.toLowerCase().contains("where"));
        assertTrue(sqlQueryTest.contains(VALID_WHERE));
        assertTrue(sqlQueryTest.toLowerCase().contains("group by"));
        assertTrue(sqlQueryTest.contains(VALID_GROUPBY));
    }

    @Test
    public void createQueryMultipleSucceeds() throws IOException
    {
        //act
        String sqlQueryTest = SqlQueryBuilder.createSqlQuery(VALID_SELECTION, SqlQueryBuilder.FromType.DEVICES, VALID_WHERE, VALID_GROUPBY);
        String sqlQueryTest_1 = SqlQueryBuilder.createSqlQuery(VALID_SELECTION + 1, SqlQueryBuilder.FromType.DEVICES, VALID_WHERE + 1, VALID_GROUPBY + 1);

        //assert
        assertTrue(sqlQueryTest.toLowerCase().contains("select"));
        assertTrue(sqlQueryTest.contains(VALID_SELECTION));
        assertTrue(sqlQueryTest.toLowerCase().contains("from"));
        assertTrue(sqlQueryTest.contains(VALID_FROM.getValue()));
        assertTrue(sqlQueryTest.toLowerCase().contains("where"));
        assertTrue(sqlQueryTest.contains(VALID_WHERE));
        assertTrue(sqlQueryTest.toLowerCase().contains("group by"));
        assertTrue(sqlQueryTest.contains(VALID_GROUPBY));

        assertTrue(sqlQueryTest_1.toLowerCase().contains("select"));
        assertTrue(sqlQueryTest_1.contains(VALID_SELECTION + 1));
        assertTrue(sqlQueryTest_1.toLowerCase().contains("from"));
        assertTrue(sqlQueryTest_1.contains(VALID_FROM.getValue()));
        assertTrue(sqlQueryTest_1.toLowerCase().contains("where"));
        assertTrue(sqlQueryTest_1.contains(VALID_WHERE + 1));
        assertTrue(sqlQueryTest_1.toLowerCase().contains("group by"));
        assertTrue(sqlQueryTest_1.contains(VALID_GROUPBY + 1));
    }
}
