/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import java.io.IOException;

public class SqlQuery
{
    private static final String SPACE = " ";
    private static final String SELECT = "select" + SPACE;
    private static final String FROM = "from" + SPACE;
    private static final String WHERE = "where" + SPACE;
    private static final String GROUP_BY = "group by" + SPACE;

    private final StringBuilder query;

    /**
     * From clause for Query
     */
    public enum FromType
    {
        DEVICES("devices"),
        JOBS("devices.jobs");

        private final String type;

        FromType(String type)
        {
            this.type = type;
        }

        public String getValue()
        {
            return type;
        }
    }

    private SqlQuery()
    {
        this.query = new StringBuilder();
    }

    /**
     * Creates a Sql style query for IotHub
     * @param selection Select clause for query. Cannot be {@code null}.
     * @param fromType From enum for Query. Cannot be {@code null}.
     * @param where Where clause for Query. Can be {@code null}.
     * @param groupby GroupBy clause for query. Can be {@code null}
     * @return SqlQuery Object as specified by param
     * @throws IOException If input parameter is invalid
     */
    public static SqlQuery createSqlQuery(String selection, FromType fromType, String where, String groupby) throws IOException
    {
        if (selection == null || fromType == null)
        {
            //Codes_SRS_SQL_QUERY_25_001: [ The constructor shall throw IllegalArgumentException if either input string selection or fromType is null or empty ]
            throw new IllegalArgumentException("selection and from are mandatory");
        }

        SqlQuery sqlQuery = new SqlQuery();

        //Codes_SRS_SQL_QUERY_25_002: [ The constructor shall build the sql query string from the given Input ]
        sqlQuery.query.append(SELECT)
                .append(selection)
                .append(SPACE)
                .append(FROM)
                .append(fromType.getValue())
                .append(SPACE);


        if (where != null && where.length() > 0)
        {
            //Codes_SRS_SQL_QUERY_25_003: [ The constructor shall append where to the sql query string only when provided ]
            sqlQuery.query.append(WHERE)
                    .append(where)
                    .append(SPACE);
        }

        if (groupby != null && groupby.length() > 0)
        {
            //Codes_SRS_SQL_QUERY_25_004: [ The constructor shall append groupby to the sql query string only when provided ]
            sqlQuery.query.append(GROUP_BY)
                    .append(groupby);
        }

        //Codes_SRS_SQL_QUERY_25_005: [ The constructor shall create a new SqlQuery instance and return it ]
        return sqlQuery;
    }

    /**
     * Getter for the String corresponding to Sql Query String created
     * @return String corresponding to Sql Query created
     */
    public String getQuery()
    {
        //Codes_SRS_SQL_QUERY_25_006: [ The method shall return the sql query string built ]
        return query.toString();
    }
}
