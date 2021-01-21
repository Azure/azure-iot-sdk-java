// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;

/**
 * Representation of a single Device Provisioning Service query specification with a JSON serializer.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language">Query Language</a>
 */
public class QuerySpecification extends Serializable
{
    // the query specification
    private static final String QUERY_TAG = "query";
    @Expose
    @SerializedName(QUERY_TAG)
    private String query;

    /**
     * CONSTRUCTOR
     *
     * <p> This constructor creates an instance of the query specification.
     *
     * <p> The <b>query</b> must follow the provisioning service
     *     <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language">Query Language</a>
     *
     * <p> When serialized, this class will look like the following example:
     * <pre>
     * {@code
     * {
     *     "query":"SELECT * FROM enrollments",
     * }
     * }
     * </pre>
     *
     * @param query the {@code String} with the query. It cannot be {@code null}, empty or a invalid query.
     * @throws IllegalArgumentException If the provided query is not a valid query.
     */
    public QuerySpecification(String query)
    {
        /* SRS_QUERY_SPECIFICATION_21_001: [The constructor shall throw IllegalArgumentException if the provided query is null, empty, or invalid.] */
        ParserUtility.validateQuery(query);

        /* SRS_QUERY_SPECIFICATION_21_002: [The constructor shall store the provided `query`.] */
        this.query = query;
    }

    /**
     * Serializer
     *
     * <p>
     *     Creates a {@code JsonElement}, which the content represents
     *     the information in this class and its subclasses in a JSON format.
     *
     *     This is useful if the caller will integrate this JSON with jsons from
     *     other classes to generate a consolidated JSON.
     * </p>

     * @return The {@code JsonElement} with the content of this class.
     */
    public JsonElement toJsonElement()
    {
        /* SRS_QUERY_SPECIFICATION_21_003: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        return gson.toJsonTree(this);
    }

    /**
     * Getter for the query.
     *
     * @return The {@code String} with the information stored in the query. It cannot be {@code null}.
     */
    public String getQuery()
    {
        /* SRS_QUERY_SPECIFICATION_21_004: [The getQuery shall return a String with the stored query.] */
        return this.query;
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    QuerySpecification()
    {
        /* SRS_QUERY_SPECIFICATION_21_005: [The QuerySpecification shall provide an empty constructor to make GSON happy.] */
    }
}
