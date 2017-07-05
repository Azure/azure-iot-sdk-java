/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;
import com.microsoft.azure.sdk.iot.service.devicetwin.QueryType;
import com.microsoft.azure.sdk.iot.service.devicetwin.RawTwinQuery;
import com.microsoft.azure.sdk.iot.service.devicetwin.SqlQuery;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/*
    Unit Tests for Raw Query
    Coverage : 100% methods, 100% lines
 */
public class RawTwinQueryTest
{
    @Mocked Query mockedQuery;
    @Mocked IotHubConnectionStringBuilder mockedIotHubConnectionStringBuilder;
    @Mocked IotHubConnectionString mockedIotHubConnectionString;

    static final String VALID_CONNECTION_STRING = "testConnectionStaring";
    static String VALID_SQL_QUERY = null;

    @Before
    public void setUp() throws IOException
    {
        VALID_SQL_QUERY = SqlQuery.createSqlQuery("tags.Floor, AVG(properties.reported.temperature) AS AvgTemperature",
                                                  SqlQuery.FromType.DEVICES, "tags.building = '43'", null).getQuery();
    }

    //Tests_SRS_RAW_QUERY_25_001: [ The constructor shall throw IllegalArgumentException if the input string is null or empty ]
    //Tests_SRS_RAW_QUERY_25_003: [ The constructor shall create a new RawTwinQuery instance and return it ]
    @Test
    public void constructorSucceeds() throws IOException
    {
        //act
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        //assert
        assertNotNull(Deencapsulation.getField(rawTwinQuery, "iotHubConnectionString"));
    }

    //Tests_SRS_RAW_QUERY_25_001: [ The constructor shall throw IllegalArgumentException if the input string is null or empty ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullConnectionString() throws IOException
    {
        //act
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyConnectionString() throws IOException
    {
        //act
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString("");
    }

    //Tests_SRS_RAW_QUERY_25_006: [ The method shall build the URL for this operation by calling getUrlTwinQuery ]
    //Tests_SRS_RAW_QUERY_25_007: [ The method shall create a new Query Object of Type Raw. ]
    //Tests_SRS_RAW_QUERY_25_008: [ The method shall send a Query Request to IotHub as HTTP Method Post on the query Object by calling sendQueryRequest.]
    //Tests_SRS_RAW_QUERY_25_009: [ If the pagesize if not provided then a default pagesize of 100 is used for the query.]
    @Test
    public void rawQuerySucceeds() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.RAW);
                result = mockedQuery;
            }
        };

        //act
        rawTwinQuery.query(VALID_SQL_QUERY);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.RAW);
                times = 1;
                Deencapsulation.invoke(mockedQuery, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, Long.class}, any, any, HttpMethod.POST, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_RAW_QUERY_25_004: [ The method shall throw IllegalArgumentException if the query is null or empty.]
    @Test (expected = IllegalArgumentException.class)
    public void rawQueryThrowsOnNullQuery() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        //act
        rawTwinQuery.query(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void rawQueryThrowsOnEmptyQuery() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        //act
        rawTwinQuery.query("");
    }

    //Tests_SRS_RAW_QUERY_25_005: [ The method shall throw IllegalArgumentException if the page size is zero or negative.]
    @Test (expected = IllegalArgumentException.class)
    public void rawQueryThrowsOnNegativePageSize() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        //act
        rawTwinQuery.query(VALID_SQL_QUERY, -1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void rawQueryThrowsOnZeroPageSize() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        //act
        rawTwinQuery.query(VALID_SQL_QUERY, 0);
    }

    @Test (expected = IotHubException.class)
    public void rawQueryThrowsOnNewQueryThrows() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.RAW);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, Long.class}, any, any, HttpMethod.POST, any);
                result = new IotHubException();
            }
        };

        //act
        rawTwinQuery.query(VALID_SQL_QUERY);
    }

    //Tests_SRS_RAW_QUERY_25_011: [ The method shall check if a response to query is avaliable by calling hasNext on the query object.]
    //Tests_SRS_RAW_QUERY_25_012: [ If a queryResponse is available, this method shall return true as is to the user. ]
    @Test
    public void hasNextSucceeds() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.RAW);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = true;
            }
        };

        Query testQuery = rawTwinQuery.query(VALID_SQL_QUERY);

        //act
        boolean result = rawTwinQuery.hasNext(testQuery);

        //assert
        new Verifications()
        {
            {
                 Deencapsulation.invoke(mockedQuery, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, Long.class}, any, any, HttpMethod.POST, any);
                times = 1;
            }
        };

        assertTrue(result);
    }

    //Tests_SRS_RAW_QUERY_25_010: [ The method shall throw IllegalArgumentException if query is null ]
    @Test (expected = IllegalArgumentException.class)
    public void hasNextThrowsOnNullQuery() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.RAW);
                result = mockedQuery;
            }
        };

        Query testQuery = rawTwinQuery.query(VALID_SQL_QUERY);

        //act
        rawTwinQuery.hasNext(null);
    }

    @Test (expected = IotHubException.class)
    public void hasNextThrowsIfQueryHasNextThrows() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.RAW);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = new IotHubException();
            }
        };

        Query testQuery = rawTwinQuery.query(VALID_SQL_QUERY);

        //act
        boolean result = rawTwinQuery.hasNext(testQuery);
    }

    //Tests_SRS_RAW_QUERY_25_016: [ The method shall return the next element from the query response.]
    @Test
    public void nextRetrievesCorrectly() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);
        final String expectedString = "testJsonAsNext";

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.RAW);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = true;
                Deencapsulation.invoke(mockedQuery, "next");
                result = expectedString;
            }
        };

        Query testQuery = rawTwinQuery.query(VALID_SQL_QUERY);

        //act
        String result = rawTwinQuery.next(testQuery);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedQuery, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, Long.class}, any, any, HttpMethod.POST, any);
                times = 1;
            }
        };
        assertEquals(expectedString, result);
    }

    //Tests_SRS_RAW_QUERY_25_018: [ If the input query is null, then this method shall throw IllegalArgumentException ]
    @Test (expected = IllegalArgumentException.class)
    public void nextThrowsOnNullQuery() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.RAW);
                result = mockedQuery;
            }
        };

        Query testQuery = rawTwinQuery.query(VALID_SQL_QUERY);

        //act
        rawTwinQuery.next(null);
    }

    @Test (expected = IotHubException.class)
    public void nextThrowsOnQueryNextThrows() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.RAW);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "next");
                result = new IotHubException();
            }
        };

        Query testQuery = rawTwinQuery.query(VALID_SQL_QUERY);

        //act
        rawTwinQuery.next(testQuery);
    }

    //Tests_SRS_RAW_QUERY_25_015: [ The method shall check if hasNext returns true and throw NoSuchElementException otherwise ]
    @Test (expected = NoSuchElementException.class)
    public void nextThrowsIfNoNewElements() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.RAW);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = false;
                Deencapsulation.invoke(mockedQuery, "next");
                result = new NoSuchElementException();
            }
        };

        Query testQuery = rawTwinQuery.query(VALID_SQL_QUERY);

        //act
        String result = rawTwinQuery.next(testQuery);
    }

    //Tests_SRS_RAW_QUERY_25_017: [ If the next element from the query response is an object other than String, then this method shall throw IOException ]
    @Test (expected = IOException.class)
    public void nextThrowsIfNonStringRetrieved() throws IotHubException, IOException
    {
        //arrange
        RawTwinQuery rawTwinQuery = RawTwinQuery.createFromConnectionString(VALID_CONNECTION_STRING);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.RAW);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = true;
                Deencapsulation.invoke(mockedQuery, "next");
                result = 5;
            }
        };

        Query testQuery = rawTwinQuery.query(VALID_SQL_QUERY);

        //act
        String result = rawTwinQuery.next(testQuery);
    }
}
