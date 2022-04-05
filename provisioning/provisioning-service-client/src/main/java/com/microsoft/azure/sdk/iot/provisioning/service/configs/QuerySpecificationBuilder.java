// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

/**
 * This is a helper to create a new instance of the {@link QuerySpecification}.
 *
 * <p> This helper will create a query forcing the correct sql format. It expects the <B>SELECT</B> and <B>FROM</B>,
 *     but optionally accepts <b>WHERE</b> and <B>GROUP BY</B>. As a result, it will return a {@link QuerySpecification}
 *     object, accepted by the provisioning service.
 *
 * <p> <b>Sample:</b>
 * <p> The follow line will create a {@link QuerySpecification}.
 * <pre>
 * {@code
 *     QuerySpecification querySpecification = new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS)
 *             .where("iotHubHostName=`ContosoIoTHub.azure-devices.net`").createSqlQuery();
 * }
 * </pre>
 * <p> Will generate the sql query:
 * <pre>
 * {@code
 * {
 *     "query":"select * from enrollments where iotHubHostName=`ContosoIoTHub.azure-devices.net`"
 * }
 * }
 * </pre>
 *
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language">Query Language</a>
 */
public class QuerySpecificationBuilder
{
    private static final String SPACE = " ";
    private static final String SELECT = "select" + SPACE;
    private static final String FROM = "from" + SPACE;
    private static final String WHERE = "where" + SPACE;
    private static final String GROUP_BY = "group by" + SPACE;

    private final String selection;
    private final FromType fromType;
    private String where;
    private String groupBy;

    /**
     * From clause for Query
     */
    public enum FromType
    {
        ENROLLMENTS("enrollments"),
        ENROLLMENT_GROUPS("enrollmentGroups"),
        DEVICE_REGISTRATIONS("deviceRegistrations");

        private final String type;

        FromType(String type)
        {
            this.type = type;
        }

        public String getValue()
        {
            return this.type;
        }
    }

    /**
     * CONSTRUCTOR
     *
     * <p> Creates a new instance of the builder, receiving the mandatory parameters.
     *
     * @param selection the {@code String} with the mandatory SELECT clause. It cannot be {@code null} or empty.
     * @param fromType the {@link FromType} with the mandatory FROM clause. It cannot be {@code null}.
     * @throws IllegalArgumentException if one of the provided clauses is invalid.
     */
    public QuerySpecificationBuilder(String selection, FromType fromType)
    {
        /* SRS_QUERY_SPECIFICATION_BUILDER_21_001: [The constructor shall throw IllegalArgumentException if the provided `selection` is null, empty, or `fromType` is null.] */
        if (selection == null || selection.isEmpty() || fromType == null)
        {
            throw new IllegalArgumentException("selection and from are mandatory");
        }

        /* SRS_QUERY_SPECIFICATION_BUILDER_21_002: [The constructor shall store the provided `selection` and `fromType` clauses.] */
        this.selection = selection;
        this.fromType = fromType;
    }

    /**
     * Setter for the `where` clause.
     *
     * @param where the {@code String} with the new clause `where`. It can be {@code null} or empty.
     * @return The same instance of the {@code QuerySpecificationBuilder}.
     */
    public QuerySpecificationBuilder where(String where)
    {
        /* SRS_QUERY_SPECIFICATION_BUILDER_21_003: [The where shall store the provided `where` clause.] */
        this.where = where;
        return this;
    }

    /**
     * Setter for the `groupBy` clause.
     * @param groupBy the {@code String} with the new clause `group by`. It can be {@code null} or empty.
     * @return The same instance of the {@code QuerySpecificationBuilder}.
     */
    public QuerySpecificationBuilder groupBy(String groupBy)
    {
        /* SRS_QUERY_SPECIFICATION_BUILDER_21_004: [The groupBy shall store the provided `groupBy` clause.] */
        this.groupBy = groupBy;
        return this;
    }

    /**
     * Creates a new instance of the {@link QuerySpecification} using the provided clauses to make the sql query.
     *
     * @return A {@link QuerySpecification} that contains a sql query with the provided clauses.
     */
    public QuerySpecification createSqlQuery()
    {
        /* SRS_QUERY_SPECIFICATION_BUILDER_21_005: [The createSqlQuery shall create a String with the `selection` and `fromType` clause using the sql format.] */
        StringBuilder query = new StringBuilder();
        query.append(SELECT)
                .append(selection)
                .append(SPACE)
                .append(FROM)
                .append(fromType.getValue());


        /* SRS_QUERY_SPECIFICATION_BUILDER_21_006: [If the `where` is not null or empty, the createSqlQuery shall add the clause `where` with its value in the sql query.] */
        if (where != null && where.length() > 0)
        {
            query.append(SPACE)
                    .append(WHERE)
                    .append(where);
        }

        /* SRS_QUERY_SPECIFICATION_BUILDER_21_007: [If the `groupBy` is not null or empty, the createSqlQuery shall add the clause `group by` with its value in the sql query.] */
        if (this.groupBy != null && this.groupBy.length() > 0)
        {
            query.append(SPACE)
                    .append(GROUP_BY)
                    .append(this.groupBy);
        }

        /* SRS_QUERY_SPECIFICATION_BUILDER_21_008: [The createSqlQuery shall create a new instance of the QuerySpecification with the String built with the provided clauses.] */
        return new QuerySpecification(query.toString());
    }
}
