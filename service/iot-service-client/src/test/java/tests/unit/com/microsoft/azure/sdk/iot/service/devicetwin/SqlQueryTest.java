/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.devicetwin.SqlQuery;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
    SqlQuery Unit Tests
    Coverage : 100% method, 100% line

 */
public class SqlQueryTest
{
    private static final String VALID_SELECTION = "validSelection";
    private static final SqlQuery.FromType VALID_FROM = SqlQuery.FromType.DEVICES;
    private static final String VALID_WHERE = "validWhere";
    private static final String VALID_GROUPBY = "validGroupBy";

    //Tests_SRS_SQL_QUERY_25_002: [ The constructor shall build the sql query string from the given Input ]
    //Tests_SRS_SQL_QUERY_25_005: [ The constructor shall create a new SqlQuery instance and return it ]
    //Tests_SRS_SQL_QUERY_25_006: [ The method shall return the sql query string built ]
    @Test
    public void createQuerySelectFromSucceeds() throws IOException
    {
        //act
        SqlQuery sqlQueryTest = SqlQuery.createSqlQuery(VALID_SELECTION, SqlQuery.FromType.DEVICES, null, null);

        //assert
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("select"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_SELECTION));
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("from"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_FROM.getValue()));
    }

    //Tests_SRS_SQL_QUERY_25_001: [ The constructor shall throw IllegalArgumentException if either input string selection or fromType is null or empty ]
    @Test (expected = IllegalArgumentException.class)
    public void createQueryNoSelectThrows() throws IOException
    {
        //act
        SqlQuery sqlQueryTest = SqlQuery.createSqlQuery(null, SqlQuery.FromType.DEVICES, null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void createQueryNoFROMThrows() throws IOException
    {
        //act
        SqlQuery sqlQueryTest = SqlQuery.createSqlQuery(VALID_SELECTION, null, null, null);
    }

    //Tests_SRS_SQL_QUERY_25_003: [ The constructor shall append where to the sql query string only when provided ]
    @Test
    public void createQuerySelectFromWhereSucceeds() throws IOException
    {
        //act
        SqlQuery sqlQueryTest = SqlQuery.createSqlQuery(VALID_SELECTION, SqlQuery.FromType.DEVICES, VALID_WHERE, null);

        //assert
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("select"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_SELECTION));
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("from"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_FROM.getValue()));
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("where"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_WHERE));
        assertFalse(sqlQueryTest.getQuery().toLowerCase().contains("group by"));
        assertFalse(sqlQueryTest.getQuery().contains(VALID_GROUPBY));
    }

    //Tests_SRS_SQL_QUERY_25_004: [ The constructor shall append groupby to the sql query string only when provided ]
    @Test
    public void createQuerySelectFromGroupBySucceeds() throws IOException
    {
        //act
        SqlQuery sqlQueryTest = SqlQuery.createSqlQuery(VALID_SELECTION, SqlQuery.FromType.DEVICES, null, VALID_GROUPBY);

        //assert
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("select"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_SELECTION));
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("from"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_FROM.getValue()));
        assertFalse(sqlQueryTest.getQuery().toLowerCase().contains("where"));
        assertFalse(sqlQueryTest.getQuery().contains(VALID_WHERE));
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("group by"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_GROUPBY));
    }

    @Test
    public void createQuerySelectFromWhereGroupBySucceeds() throws IOException
    {
        //act
        SqlQuery sqlQueryTest = SqlQuery.createSqlQuery(VALID_SELECTION, SqlQuery.FromType.DEVICES, VALID_WHERE, VALID_GROUPBY);

        //assert
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("select"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_SELECTION));
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("from"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_FROM.getValue()));
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("where"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_WHERE));
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("group by"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_GROUPBY));
    }

    @Test
    public void createQueryMultipleSucceeds() throws IOException
    {
        //act
        SqlQuery sqlQueryTest = SqlQuery.createSqlQuery(VALID_SELECTION, SqlQuery.FromType.DEVICES, VALID_WHERE, VALID_GROUPBY);
        SqlQuery sqlQueryTest_1 = SqlQuery.createSqlQuery(VALID_SELECTION + 1, SqlQuery.FromType.DEVICES, VALID_WHERE + 1, VALID_GROUPBY + 1);

        //assert
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("select"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_SELECTION));
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("from"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_FROM.getValue()));
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("where"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_WHERE));
        assertTrue(sqlQueryTest.getQuery().toLowerCase().contains("group by"));
        assertTrue(sqlQueryTest.getQuery().contains(VALID_GROUPBY));

        assertTrue(sqlQueryTest_1.getQuery().toLowerCase().contains("select"));
        assertTrue(sqlQueryTest_1.getQuery().contains(VALID_SELECTION + 1));
        assertTrue(sqlQueryTest_1.getQuery().toLowerCase().contains("from"));
        assertTrue(sqlQueryTest_1.getQuery().contains(VALID_FROM.getValue()));
        assertTrue(sqlQueryTest_1.getQuery().toLowerCase().contains("where"));
        assertTrue(sqlQueryTest_1.getQuery().contains(VALID_WHERE + 1));
        assertTrue(sqlQueryTest_1.getQuery().toLowerCase().contains("group by"));
        assertTrue(sqlQueryTest_1.getQuery().contains(VALID_GROUPBY + 1));
    }
}
