/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.query;

public class SqlQueryBuilder
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
        MODULES("devices.modules"),
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

    private SqlQueryBuilder()
    {
        this.query = new StringBuilder();
    }

    /**
     * Creates a Sql style query for IotHub
     * @param selection Select clause for query. Cannot be {@code null}.
     * @param fromType From enum for Query. Cannot be {@code null}.
     * @param where Where clause for Query. Can be {@code null}.
     * @param groupby GroupBy clause for query. Can be {@code null}
     * @return SqlQueryBuilder Object as specified by param
     */
    public static String createSqlQuery(String selection, FromType fromType, String where, String groupby)
    {
        if (selection == null || fromType == null)
        {
            throw new IllegalArgumentException("selection and from are mandatory");
        }

        SqlQueryBuilder sqlQuery = new SqlQueryBuilder();

        sqlQuery.query.append(SELECT)
                .append(selection)
                .append(SPACE)
                .append(FROM)
                .append(fromType.getValue())
                .append(SPACE);


        if (where != null && where.length() > 0)
        {
            sqlQuery.query.append(WHERE)
                    .append(where)
                    .append(SPACE);
        }

        if (groupby != null && groupby.length() > 0)
        {
            sqlQuery.query.append(GROUP_BY)
                    .append(groupby);
        }

        return sqlQuery.query.toString();
    }
}
